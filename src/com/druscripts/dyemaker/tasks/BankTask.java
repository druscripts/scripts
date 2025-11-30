package com.druscripts.dyemaker.tasks;

import com.druscripts.utils.FreeScript;
import com.druscripts.utils.Task;
import com.druscripts.dyemaker.Constants;
import com.druscripts.dyemaker.DyeMaker;
import com.druscripts.dyemaker.DyeType;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.scene.RSObject;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BankTask extends Task {

    private final DyeMaker dm;

    public BankTask(FreeScript script) {
        super(script);
        dm = (DyeMaker) script;
    }

    @Override
    public boolean activate() {
        return dm.isInBankArea(script.getWorldPosition()) && (dm.hasDyes() || !dm.hasMaterials());
    }

    @Override
    public boolean execute() {
        DyeType dyeType = dm.selectedDyeType;

        if (!script.getWidgetManager().getBank().isVisible()) {
            dm.task = "Opening bank";
            openBank();
            return false;
        }

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Collections.emptySet());
        if (inv != null && inv.getFreeSlots() < script.getWidgetManager().getInventory().getGroupSize()) {
            dm.task = "Depositing";
            script.getWidgetManager().getBank().depositAll(Collections.emptySet());
            script.pollFramesHuman(() -> {
                ItemGroupResult check = script.getWidgetManager().getInventory().search(Collections.emptySet());
                return check == null || check.getFreeSlots() == script.getWidgetManager().getInventory().getGroupSize();
            }, 3000, true);
        }

        // Query bank materials once
        int coinsInBank = getBankAmount(Constants.COINS_ID);
        int ingredientsInBank = getBankAmount(dyeType.getIngredientId());

        // Check if we're truly out of materials
        if (coinsInBank < Constants.COINS_PER_DYE || ingredientsInBank < dyeType.getIngredientCount()) {
            dm.task = "Out of materials";
            showOutOfMaterialsAlert();
            return false;
        }

        // Try to withdraw - if it fails, we'll retry next poll
        if (!withdrawMaterials(dyeType, coinsInBank, ingredientsInBank)) {
            return false;
        }

        script.getWidgetManager().getBank().close();
        return false;
    }

    private int getBankAmount(int itemId) {
        ItemGroupResult result = script.getWidgetManager().getBank().search(Set.of(itemId));
        if (result == null || !result.contains(itemId)) return 0;
        return result.getAmount(new int[]{itemId});
    }

    /**
     * Attempt to withdraw materials from bank.
     * Returns true if successful, false if failed (will retry next poll).
     */
    private boolean withdrawMaterials(DyeType dyeType, int coinsInBank, int ingredientsInBank) {
        dm.task = "Withdrawing";

        // For stackable ingredients, full inventory available. For non-stackable,
        // one slot is reserved for coins so we subtract 1.
        int maxBatches = dyeType.isStackable()
            ? Constants.MAX_INVENTORY_SIZE
            : (Constants.MAX_INVENTORY_SIZE - 1) / dyeType.getIngredientCount();
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
        List<RSObject> banks = script.getObjectManager().getObjects(obj ->
            Constants.BANK_NAME.equalsIgnoreCase(obj.getName()) && obj.canReach()
        );
        if (banks.isEmpty()) return;

        RSObject bank = (RSObject) script.getUtils().getClosest(banks);
        if (!bank.interact(Constants.BANK_ACTION)) return;

        double dist = bank.distance(script.getWorldPosition());
        script.pollFramesHuman(() -> script.getWidgetManager().getBank().isVisible(), (int)(dist * 1200 + 600), true);
    }

    private void showOutOfMaterialsAlert() {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Script Complete");
            alert.setHeaderText("Out of Materials");
            alert.setContentText("Total dyes made: " + dm.dyesMade);
            alert.showAndWait();
        });
        script.stop();
    }
}
