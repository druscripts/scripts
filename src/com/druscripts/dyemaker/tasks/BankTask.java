package com.druscripts.dyemaker.tasks;

import com.druscripts.utils.FreeScript;
import com.druscripts.utils.Task;
import com.druscripts.dyemaker.Constants;
import com.druscripts.dyemaker.DyeMaker;
import com.druscripts.dyemaker.DyeType;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.scene.RSObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BankTask extends Task {

    public BankTask(FreeScript script) {
        super(script);
    }

    @Override
    public boolean activate() {
        DyeMaker dm = (DyeMaker) script;
        if (!dm.isInBankArea(script.getWorldPosition())) return false;
        return dm.hasDyes() || !dm.hasMaterials();
    }

    @Override
    public boolean execute() {
        DyeMaker dm = (DyeMaker) script;
        DyeType dyeType = DyeMaker.selectedDyeType;

        if (dm.hasDyes() && DyeMaker.runStartTime > 0) {
            script.sendStat(Constants.STAT_RUN_COMPLETED, System.currentTimeMillis() - DyeMaker.runStartTime);
            DyeMaker.runStartTime = 0;
        }

        if (!script.getWidgetManager().getBank().isVisible()) {
            DyeMaker.task = "Opening bank";
            openBank();
            return false;
        }

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Collections.emptySet());
        if (inv != null && inv.getFreeSlots() < script.getWidgetManager().getInventory().getGroupSize()) {
            DyeMaker.task = "Depositing";
            script.getWidgetManager().getBank().depositAll(Collections.emptySet());
            script.pollFramesHuman(() -> {
                ItemGroupResult check = script.getWidgetManager().getInventory().search(Collections.emptySet());
                return check == null || check.getFreeSlots() == script.getWidgetManager().getInventory().getGroupSize();
            }, 3000, true);
        }

        if (!withdrawMaterials(dyeType)) {
            DyeMaker.task = "Out of materials";
            showOutOfMaterialsAlert();
            return false;
        }

        DyeMaker.runStartTime = System.currentTimeMillis();
        script.getWidgetManager().getBank().close();
        return false;
    }

    private boolean withdrawMaterials(DyeType dyeType) {
        DyeMaker.task = "Withdrawing";

        ItemGroupResult bankCoins = script.getWidgetManager().getBank().search(Set.of(Constants.COINS_ID));
        if (bankCoins == null || !bankCoins.contains(Constants.COINS_ID)) return false;
        int coinsInBank = bankCoins.getAmount(new int[]{Constants.COINS_ID});

        ItemGroupResult bankIngredients = script.getWidgetManager().getBank().search(Set.of(dyeType.getIngredientId()));
        if (bankIngredients == null || !bankIngredients.contains(dyeType.getIngredientId())) return false;
        int ingredientsInBank = bankIngredients.getAmount(new int[]{dyeType.getIngredientId()});

        int batches = Math.min(coinsInBank / Constants.COINS_PER_DYE, ingredientsInBank / dyeType.getIngredientCount());
        if (!dyeType.isStackable()) {
            batches = Math.min(batches, 27 / dyeType.getIngredientCount());
        }
        if (batches <= 0) return false;

        int coins = batches * Constants.COINS_PER_DYE;
        if (!script.getWidgetManager().getBank().withdraw(Constants.COINS_ID, coins)) return false;
        script.pollFramesHuman(() -> {
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(Constants.COINS_ID));
            return inv != null && inv.contains(Constants.COINS_ID);
        }, 3000, true);

        int ingredients = batches * dyeType.getIngredientCount();
        return script.getWidgetManager().getBank().withdraw(dyeType.getIngredientId(), ingredients);
    }

    private void openBank() {
        List<RSObject> banks = script.getObjectManager().getObjects(obj -> {
            if (obj.getName() == null || obj.getActions() == null) return false;
            if (Arrays.stream(Constants.BANK_NAMES).noneMatch(n -> n.equalsIgnoreCase(obj.getName()))) return false;
            return Arrays.stream(obj.getActions()).anyMatch(a ->
                Arrays.stream(Constants.BANK_ACTIONS).anyMatch(ba -> ba.equalsIgnoreCase((String) a))
            ) && obj.canReach();
        });
        if (banks.isEmpty()) return;

        RSObject bank = (RSObject) script.getUtils().getClosest(banks);
        if (!bank.interact(Constants.BANK_ACTIONS)) return;

        double dist = bank.distance(script.getWorldPosition());
        script.pollFramesHuman(() -> script.getWidgetManager().getBank().isVisible(), (int)(dist * 1200 + 600), true);
    }

    private void showOutOfMaterialsAlert() {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Script Complete");
            alert.setHeaderText("Out of Materials");
            alert.setContentText("Total dyes made: " + DyeMaker.dyesMade);
            alert.showAndWait();
        });
        script.stop();
    }
}
