package com.druscripts.dyemaker.tasks;

import com.druscripts.utils.script.FreeScript;
import com.druscripts.utils.script.Task;
import com.druscripts.utils.dialogwindow.dialogs.ErrorDialog;
import com.druscripts.dyemaker.data.Constants;
import com.druscripts.dyemaker.DyeMaker;
import com.druscripts.dyemaker.data.DyeType;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.scene.RSObject;
import javafx.scene.Scene;

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
    public void execute() {
        if (!script.getWidgetManager().getBank().isVisible()) {
            dm.task = "Opening bank";
            openBank();
            return;
        }

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Collections.emptySet());
        if (inv != null && inv.getFreeSlots() < Constants.MAX_INVENTORY_SIZE) {
            dm.task = "Depositing";
            script.getWidgetManager().getBank().depositAll(Collections.emptySet());
            script.pollFramesHuman(() -> {
                // must re-query to get updated inventory status
                ItemGroupResult check = script.getWidgetManager().getInventory().search(Collections.emptySet());
                return check == null || check.getFreeSlots() == Constants.MAX_INVENTORY_SIZE;
            }, 3000, true);
        }

        DyeType dyeType = dm.selectedDyeType;

        int coinsInBank = getBankAmount(Constants.COINS_ID);
        int ingredientsInBank = getBankAmount(dyeType.getIngredientId());

        if (coinsInBank < Constants.COINS_PER_DYE || ingredientsInBank < dyeType.getIngredientCount()) {
            dm.task = "Out of materials";
            showOutOfMaterialsAlertAndStopScript();
            return;
        }

        dm.task = "Withdrawing";
        if (!withdrawMaterials(dyeType, coinsInBank, ingredientsInBank)) {
            return;
        }

        script.getWidgetManager().getBank().close();
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

    private void showOutOfMaterialsAlertAndStopScript() {
        String message = "Total dyes made: " + dm.dyesMade;
        Scene errorScene = ErrorDialog.createErrorScene(
            "DyeMaker",
            "Out of Materials",
            message,
            script::stop
        );
        script.getStageController().show(errorScene, "Script Complete", false);
    }
}
