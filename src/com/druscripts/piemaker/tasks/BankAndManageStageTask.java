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

public class BankAndManageStageTask extends Task {

    private static final Predicate<RSObject> BANK_QUERY = obj -> {
        if (obj.getName() == null || obj.getActions() == null) return false;
        if (Arrays.stream(Constants.BANK_NAMES).noneMatch(n -> n.equalsIgnoreCase(obj.getName()))) return false;
        return Arrays.stream(obj.getActions())
            .anyMatch(a -> Arrays.stream(Constants.BANK_ACTIONS).anyMatch(ba -> ba.equalsIgnoreCase((String) a)))
            && obj.canReach();
    };

    private final PieMaker pieMaker;

    public BankAndManageStageTask(PieMaker script) {
        super(script);
        this.pieMaker = script;
    }

    @Override
    public boolean activate() {
        return needsToBank();
    }

    private boolean needsToBank() {
        try {
            switch (pieMaker.stage) {
                case MAKE_DOUGH:
                    return needsBankForStage(Constants.FLOUR, pieMaker.waterSourceId, Constants.PASTRY_DOUGH);
                case MAKE_SHELL:
                    return needsBankForStage(Constants.PIE_DISH, Constants.PASTRY_DOUGH, Constants.PIE_SHELL);
                case MAKE_UNCOOKED:
                    return needsBankForStage(Constants.PIE_SHELL, pieMaker.pieType.getIngredientId(), pieMaker.pieType.getUncookedId());
                case COOK:
                    return needsBankForCooking();
                default:
                    return false;
            }
        } catch (CannotOpenWidgetException e) {
            script.log(getClass(), e.getMessage());
            return false;
        }
    }

    private boolean needsBankForCooking() throws CannotOpenWidgetException {
        boolean hasCooked = InventoryUtils.hasAnyItem(script, pieMaker.pieType.getCookedId());
        boolean hasUncooked = InventoryUtils.hasItem(script, pieMaker.pieType.getUncookedId());
        return hasCooked || !hasUncooked;
    }

    private boolean needsBankForStage(int input1, int input2, int output) throws CannotOpenWidgetException {
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
        if (!script.pollFramesHuman(() -> {
            try {
                return InventoryUtils.isEmpty(script);
            } catch (CannotOpenWidgetException e) {
                return false;
            }
        }, 3000, true)) {
            script.log(getClass(), "Deposit sanity check failed");
            return;
        }

        updateBankCounts();
        if (!setStage()) {
            showOutOfMaterialsAlert();
            return;
        }

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

    /**
     * Sets the stage based on bank contents.
     * Returns false if no materials are available.
     */
    private boolean setStage() {
        Stage newStage = null;

        // Check stages in order from earliest to latest
        if (canDoStage(Stage.MAKE_DOUGH)) newStage = Stage.MAKE_DOUGH;
        else if (canDoStage(Stage.MAKE_SHELL)) newStage = Stage.MAKE_SHELL;
        else if (canDoStage(Stage.MAKE_UNCOOKED)) newStage = Stage.MAKE_UNCOOKED;
        else if (pieMaker.isLumbridge() && canDoStage(Stage.COOK)) newStage = Stage.COOK;

        if (newStage == null) return false;

        if (newStage != pieMaker.stage) {
            script.log(getClass(), "Stage: " + pieMaker.stage + " -> " + newStage);
            pieMaker.stage = newStage;
        }
        return true;
    }

    private boolean canDoStage(Stage stage) {
        switch (stage) {
            case MAKE_DOUGH:
                return pieMaker.bankFlour > 0 && pieMaker.bankWater > 0;
            case MAKE_SHELL:
                return pieMaker.bankPastryDough > 0 && pieMaker.bankPieDishes > 0;
            case MAKE_UNCOOKED:
                return pieMaker.bankPieShells > 0 && pieMaker.bankIngredients > 0;
            case COOK:
                return pieMaker.bankUncookedPies > 0;
            default:
                return false;
        }
    }

    private boolean withdrawForStage() {
        switch (pieMaker.stage) {
            case MAKE_DOUGH:
                return withdraw(Constants.FLOUR, Constants.FLOUR_COUNT, "flour")
                    && withdraw(pieMaker.waterSourceId, Constants.WATER_COUNT, "water");
            case MAKE_SHELL:
                return withdraw(Constants.PIE_DISH, Constants.DISH_COUNT, "pie dishes")
                    && withdraw(Constants.PASTRY_DOUGH, Constants.DOUGH_COUNT, "pastry dough");
            case MAKE_UNCOOKED:
                return withdraw(Constants.PIE_SHELL, Constants.SHELL_COUNT, "pie shells")
                    && withdraw(pieMaker.pieType.getIngredientId(), Constants.INGREDIENT_COUNT, pieMaker.pieType.getIngredientName());
            case COOK:
                return withdraw(pieMaker.pieType.getUncookedId(), 28, "uncooked pies");
            default:
                return false;
        }
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
}
