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

    private DyeMaker dm;

    public BankTask(FreeScript script) {
        super(script);
        dm = (DyeMaker) script;
    }

    @Override
    public boolean activate() {
        boolean inBank = dm.isInBankArea(script.getWorldPosition());
        return inBank && (dm.hasDyes() || !dm.hasMaterials());
    }

    @Override
    public boolean execute() {
        DyeType dyeType = DyeMaker.selectedDyeType;

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

        // Check if we're truly out of materials
        if (!hasSufficientBankMaterials(dyeType)) {
            DyeMaker.task = "Out of materials";
            showOutOfMaterialsAlert();
            return false;
        }

        // Try to withdraw - if it fails, we'll retry next poll
        if (!withdrawMaterials(dyeType)) {
            return false;
        }

        script.getWidgetManager().getBank().close();
        return false;
    }

    /**
     * Check if bank has enough materials for at least one batch.
     */
    private boolean hasSufficientBankMaterials(DyeType dyeType) {
        ItemGroupResult bankCoins = script.getWidgetManager().getBank().search(Set.of(Constants.COINS_ID));
        if (bankCoins == null || !bankCoins.contains(Constants.COINS_ID)) return false;
        int coinsInBank = bankCoins.getAmount(new int[]{Constants.COINS_ID});
        if (coinsInBank < Constants.COINS_PER_DYE) return false;

        ItemGroupResult bankIngredients = script.getWidgetManager().getBank().search(Set.of(dyeType.getIngredientId()));
        if (bankIngredients == null || !bankIngredients.contains(dyeType.getIngredientId())) return false;
        int ingredientsInBank = bankIngredients.getAmount(new int[]{dyeType.getIngredientId()});
        if (ingredientsInBank < dyeType.getIngredientCount()) return false;

        return true;
    }

    /**
     * Attempt to withdraw materials from bank.
     * Returns true if successful, false if failed (will retry next poll).
     */
    private boolean withdrawMaterials(DyeType dyeType) {
        DyeMaker.task = "Withdrawing";

        ItemGroupResult bankCoins = script.getWidgetManager().getBank().search(Set.of(Constants.COINS_ID));
        int coinsInBank = bankCoins.getAmount(new int[]{Constants.COINS_ID});

        ItemGroupResult bankIngredients = script.getWidgetManager().getBank().search(Set.of(dyeType.getIngredientId()));
        int ingredientsInBank = bankIngredients.getAmount(new int[]{dyeType.getIngredientId()});

        int maxBatches = dyeType.isStackable() ? 28 : 27 / dyeType.getIngredientCount();
        int batches = Math.min(coinsInBank / Constants.COINS_PER_DYE, ingredientsInBank / dyeType.getIngredientCount());
        batches = Math.min(batches, maxBatches);

        // Withdraw coins if not already in inventory
        ItemGroupResult invCoins = script.getWidgetManager().getInventory().search(Set.of(Constants.COINS_ID));
        if (invCoins == null || !invCoins.contains(Constants.COINS_ID)) {
            int coins = batches * Constants.COINS_PER_DYE;
            if (!script.getWidgetManager().getBank().withdraw(Constants.COINS_ID, coins)) {
                return false;
            }
            boolean coinsAppeared = script.pollFramesHuman(() -> {
                ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(Constants.COINS_ID));
                return inv != null && inv.contains(Constants.COINS_ID);
            }, 3000, true);
            if (!coinsAppeared) {
                return false;
            }
        }

        // Withdraw ingredients if not already in inventory
        ItemGroupResult invIngredients = script.getWidgetManager().getInventory().search(Set.of(dyeType.getIngredientId()));
        if (invIngredients == null || !invIngredients.contains(dyeType.getIngredientId())) {
            int ingredients = batches * dyeType.getIngredientCount();
            if (!script.getWidgetManager().getBank().withdraw(dyeType.getIngredientId(), ingredients)) {
                return false;
            }
            boolean ingredientsAppeared = script.pollFramesHuman(() -> {
                ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(dyeType.getIngredientId()));
                return inv != null && inv.contains(dyeType.getIngredientId());
            }, 3000, true);
            if (!ingredientsAppeared) {
                return false;
            }
        }

        return true;
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
