package com.druscripts.piemaker.tasks;

import com.druscripts.piemaker.PieMaker;
import com.druscripts.piemaker.data.Constants;
import com.druscripts.piemaker.data.Stage;
import com.druscripts.utils.script.Task;
import com.druscripts.utils.widget.InventoryUtils;
import com.druscripts.utils.widget.exception.CannotOpenWidgetException;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.scene.RSObject;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class BankTask extends Task {

    private static final Predicate<RSObject> BANK_QUERY = obj -> {
        if (obj.getName() == null || obj.getActions() == null) return false;
        if (Arrays.stream(Constants.BANK_NAMES).noneMatch(n -> n.equalsIgnoreCase(obj.getName()))) return false;
        return Arrays.stream(obj.getActions())
            .anyMatch(a -> Arrays.stream(Constants.BANK_ACTIONS).anyMatch(ba -> ba.equalsIgnoreCase((String) a)))
            && obj.canReach();
    };

    private final PieMaker pieMaker;
    private boolean outOfMaterials = false;

    public BankTask(PieMaker script) {
        super(script);
        this.pieMaker = script;
    }

    @Override
    public boolean activate() {
        if (outOfMaterials) return false;
        return needsToBank();
    }

    private boolean needsToBank() {
        try {
            switch (pieMaker.stage) {
                case MAKE_DOUGH:
                    return checkNeedsBank(Constants.FLOUR, pieMaker.waterSourceId, Constants.PASTRY_DOUGH);
                case MAKE_SHELL:
                    return checkNeedsBank(Constants.PIE_DISH, Constants.PASTRY_DOUGH, Constants.PIE_SHELL);
                case MAKE_UNCOOKED:
                    return checkNeedsBank(Constants.PIE_SHELL, pieMaker.pieType.getIngredientId(), pieMaker.pieType.getUncookedId());
                case COOK:
                    return checkNeedsBankForCooking();
                default:
                    return false;
            }
        } catch (CannotOpenWidgetException e) {
            script.log(getClass(), e.getMessage());
            return false;
        }
    }

    private boolean checkNeedsBankForCooking() throws CannotOpenWidgetException {
        boolean hasCooked = InventoryUtils.hasAnyItem(script, pieMaker.pieType.getCookedId());
        boolean hasUncooked = InventoryUtils.hasItem(script, pieMaker.pieType.getUncookedId());
        return hasCooked || !hasUncooked;
    }

    private boolean checkNeedsBank(int input1, int input2, int output) throws CannotOpenWidgetException {
        boolean hasOutput = InventoryUtils.hasAnyItem(script, output);
        boolean hasAllInputs = InventoryUtils.hasAllItems(script, input1, input2);
        return hasOutput || !hasAllInputs;
    }

    @Override
    public void execute() {
        pieMaker.task = "Banking";

        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(getClass(), "Opening bank...");
            openBank();
            return;
        }

        pieMaker.task = "Depositing items";
        if (!script.getWidgetManager().getBank().depositAll(Collections.emptySet())) {
            script.log(getClass(), "Deposit failed, closing bank...");
            script.getWidgetManager().getBank().close();
            return;
        }

        // Sanity check
        script.pollFramesHuman(() -> {
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(Collections.emptySet());
            return inv == null;
        }, 3000, true);

        updateBankCounts();

        if (pieMaker.allInOne) checkForStageRegression();

        boolean success = withdrawForStage();

        if (!success) {
            script.log(getClass(), "Withdrawal failed, closing bank...");
            script.getWidgetManager().getBank().close();
            return;
        }

        updateBankCounts();
        script.log(getClass(), "Closing bank...");
        script.getWidgetManager().getBank().close();
    }

    private boolean withdrawForStage() {
        boolean success = false;

        switch (pieMaker.stage) {
            case MAKE_DOUGH:
                success = withdraw(Constants.FLOUR, Constants.FLOUR_COUNT, "flour")
                       && withdraw(pieMaker.waterSourceId, Constants.WATER_COUNT, "water");
                break;
            case MAKE_SHELL:
                success = withdraw(Constants.PIE_DISH, Constants.DISH_COUNT, "pie dishes")
                       && withdraw(Constants.PASTRY_DOUGH, Constants.DOUGH_COUNT, "pastry dough");
                break;
            case MAKE_UNCOOKED:
                success = withdraw(Constants.PIE_SHELL, Constants.SHELL_COUNT, "pie shells")
                       && withdraw(pieMaker.pieType.getIngredientId(), Constants.INGREDIENT_COUNT, pieMaker.pieType.getIngredientName());
                break;
            case COOK:
                success = withdraw(pieMaker.pieType.getUncookedId(), 28, "uncooked pies");
                break;
            default:
                return false;
        }

        if (!success) {
            if (pieMaker.allInOne && shouldProgress()) {
                script.log(getClass(), "Not enough materials. Trying next stage...");
                return tryNextStage();
            } else if (shouldProgress()) {
                script.log(getClass(), "Out of materials. Stopping.");
                outOfMaterials = true;
                showOutOfMaterialsAlert();
            }
            return false;
        }

        return true;
    }

    private boolean shouldProgress() {
        switch (pieMaker.stage) {
            case MAKE_DOUGH:
                return !hasBoth(Constants.FLOUR, pieMaker.waterSourceId);
            case MAKE_SHELL:
                return !hasBoth(Constants.PASTRY_DOUGH, Constants.PIE_DISH);
            case MAKE_UNCOOKED:
                return !hasBoth(Constants.PIE_SHELL, pieMaker.pieType.getIngredientId());
            case COOK:
                return !hasItem(pieMaker.pieType.getUncookedId());
            default:
                return true;
        }
    }

    private boolean hasItem(int itemId) {
        ItemGroupResult bank = script.getWidgetManager().getBank().search(Set.of(itemId));
        return bank != null && bank.contains(itemId) && bank.getAmount(new int[]{itemId}) > 0;
    }

    private boolean tryNextStage() {
        script.getWidgetManager().getBank().depositAll(Collections.emptySet());
        script.pollFramesHuman(() -> script.getWidgetManager().getInventory().search(Collections.emptySet()) == null, 3000, true);

        Stage[] stages = Stage.values();
        int currentOrdinal = pieMaker.stage.ordinal();
        int maxOrdinal = pieMaker.isLumbridge() ? Stage.COOK.ordinal() : Stage.MAKE_UNCOOKED.ordinal();

        for (int i = currentOrdinal + 1; i <= maxOrdinal; i++) {
            pieMaker.stage = stages[i];
            boolean success = false;

            if (pieMaker.stage == Stage.MAKE_SHELL) {
                success = withdraw(Constants.PIE_DISH, Constants.DISH_COUNT, "pie dishes")
                       && withdraw(Constants.PASTRY_DOUGH, Constants.DOUGH_COUNT, "pastry dough");
            } else if (pieMaker.stage == Stage.MAKE_UNCOOKED) {
                success = withdraw(Constants.PIE_SHELL, Constants.SHELL_COUNT, "pie shells")
                       && withdraw(pieMaker.pieType.getIngredientId(), Constants.INGREDIENT_COUNT, pieMaker.pieType.getIngredientName());
            } else if (pieMaker.stage == Stage.COOK) {
                success = withdraw(pieMaker.pieType.getUncookedId(), 28, "uncooked pies");
            }

            if (success) return true;
        }

        outOfMaterials = true;
        showOutOfMaterialsAlert();
        return false;
    }

    private boolean withdraw(int itemId, int count, String name) {
        ItemGroupResult bank = script.getWidgetManager().getBank().search(Set.of(itemId));
        if (bank == null || !bank.contains(itemId)) {
            script.log(getClass(), "No " + name + " in bank");
            return false;
        }
        if (!script.getWidgetManager().getBank().withdraw(itemId, count)) {
            script.log(getClass(), "Failed to withdraw " + name);
            return false;
        }
        return true;
    }

    private void openBank() {
        List<RSObject> banks = script.getObjectManager().getObjects(BANK_QUERY);
        if (banks.isEmpty()) {
            script.log(getClass(), "No bank found.");
            return;
        }

        RSObject bank = (RSObject) script.getUtils().getClosest(banks);
        if (!bank.interact(Constants.BANK_ACTIONS)) {
            script.log(getClass(), "Failed to interact with bank.");
            return;
        }

        double dist = bank.distance(script.getWorldPosition());
        script.pollFramesHuman(() -> script.getWidgetManager().getBank().isVisible(), (int)(dist * 1000 + 500), true);
    }

    private void updateBankCounts() {
        pieMaker.bankFlour = getBankAmount(Constants.FLOUR);
        pieMaker.bankWater = getBankAmount(pieMaker.waterSourceId);
        pieMaker.bankPastryDough = getBankAmount(Constants.PASTRY_DOUGH);
        pieMaker.bankPieDishes = getBankAmount(Constants.PIE_DISH);
        pieMaker.bankPieShells = getBankAmount(Constants.PIE_SHELL);
        pieMaker.bankIngredients = getBankAmount(pieMaker.pieType.getIngredientId());
        pieMaker.bankUncookedPies = getBankAmount(pieMaker.pieType.getUncookedId());
    }

    private int getBankAmount(int itemId) {
        ItemGroupResult bank = script.getWidgetManager().getBank().search(Set.of(itemId));
        if (bank == null || !bank.contains(itemId)) return 0;
        return bank.getAmount(new int[]{itemId});
    }

    private void checkForStageRegression() {
        if (hasBoth(Constants.FLOUR, pieMaker.waterSourceId)) {
            if (pieMaker.stage != Stage.MAKE_DOUGH) {
                script.log(getClass(), "Regressing to MAKE_DOUGH stage");
                pieMaker.stage = Stage.MAKE_DOUGH;
            }
            return;
        }
        if (hasBoth(Constants.PASTRY_DOUGH, Constants.PIE_DISH)) {
            if (pieMaker.stage != Stage.MAKE_SHELL) {
                script.log(getClass(), "Regressing to MAKE_SHELL stage");
                pieMaker.stage = Stage.MAKE_SHELL;
            }
            return;
        }
        if (hasBoth(Constants.PIE_SHELL, pieMaker.pieType.getIngredientId())) {
            if (pieMaker.stage != Stage.MAKE_UNCOOKED) {
                script.log(getClass(), "Regressing to MAKE_UNCOOKED stage");
                pieMaker.stage = Stage.MAKE_UNCOOKED;
            }
            return;
        }
        if (pieMaker.isLumbridge() && hasItem(pieMaker.pieType.getUncookedId())) {
            if (pieMaker.stage != Stage.COOK) {
                script.log(getClass(), "Advancing to COOK stage");
                pieMaker.stage = Stage.COOK;
            }
        }
    }

    private boolean hasBoth(int id1, int id2) {
        ItemGroupResult bank = script.getWidgetManager().getBank().search(Set.of(id1, id2));
        if (bank == null) return false;
        return bank.contains(id1) && bank.contains(id2)
            && bank.getAmount(new int[]{id1}) > 0
            && bank.getAmount(new int[]{id2}) > 0;
    }

    private void showOutOfMaterialsAlert() {
        script.log(getClass(), "Out of materials!");
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Script Complete");
            alert.setHeaderText("Out of Materials");
            alert.setContentText("Processed all available materials.\n\nItems made: " + pieMaker.itemsMade);
            alert.showAndWait();
        });
        script.stop();
    }
}
