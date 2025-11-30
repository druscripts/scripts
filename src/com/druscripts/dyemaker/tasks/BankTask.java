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
        return dm.isInBankArea(dm.getWorldPosition()) && (dm.hasDyes() || !dm.hasMaterials());
    }

    @Override
    public void execute() {
        if (!dm.getWidgetManager().getBank().isVisible()) {
            dm.task = "Opening bank";
            openBank();
            return;
        }

        ItemGroupResult inv = dm.getWidgetManager().getInventory().search(Collections.emptySet());
        if (inv == null) return;
        // NOTE: Easier to deposit everything even if partial withdrawl was successful last poll
        if (inv.getFreeSlots() < Constants.MAX_INVENTORY_SIZE) {
            dm.task = "Depositing";
            dm.getWidgetManager().getBank().depositAll(Collections.emptySet());

            // sanity check
            boolean depositSuccess = dm.pollFramesHuman(() -> {
                ItemGroupResult check = dm.getWidgetManager().getInventory().search(Collections.emptySet());
                return check == null || check.getFreeSlots() == Constants.MAX_INVENTORY_SIZE;
            }, 3000, true);

            if (!depositSuccess) return;
        }

        int coinsInBank = getBankAmount(Constants.COINS_ID);

        DyeType dyeType = dm.selectedDyeType;
        int ingredientsInBank = getBankAmount(dyeType.getIngredientId());

        if (coinsInBank < Constants.COINS_PER_DYE ||
            ingredientsInBank < dyeType.getIngredientCount())
        {
            dm.task = "Out of materials";
            showOutOfMaterialsAlertAndStopScript();
            return;
        }

        dm.task = "Withdrawing";
        if (!withdrawMaterials(dyeType, coinsInBank, ingredientsInBank)) {
            return;
        }

        dm.getWidgetManager().getBank().close();
    }

    private int getBankAmount(int itemId) {
        ItemGroupResult result = dm.getWidgetManager().getBank().search(Set.of(itemId));
        if (result == null || !result.contains(itemId)) return 0;
        return result.getAmount(itemId);
    }

    private boolean withdrawMaterials(DyeType dyeType, int coinsInBank, int ingredientsInBank) {
        int totalBatches = Math.min(
            coinsInBank / Constants.COINS_PER_DYE,
            ingredientsInBank / dyeType.getIngredientCount());

        int batchesThisRun = Math.min(totalBatches, dyeType.getBatchesPerRun());

        boolean result = withdrawItem(Constants.COINS_ID, batchesThisRun * Constants.COINS_PER_DYE);
        if (result == false) return false;

        result = withdrawItem(dyeType.getIngredientId(), batchesThisRun * dyeType.getIngredientCount());
        return result;
    }

    private boolean withdrawItem(int id, int amount) {
        // search should auto switch to inv. null means inv not visible
        ItemGroupResult inv = dm.getWidgetManager().getInventory().search(Set.of(id));
        if (inv == null) return false;
        // will always contain correct amount due to deposit at start of task
        if (inv.contains(id)) return true;

        if (!dm.getWidgetManager().getBank().withdraw(id, amount)) {
            return false;
        }

        // sanity check
        boolean withdrawn = dm.pollFramesHuman(() -> {
            ItemGroupResult check = dm.getWidgetManager().getInventory().search(Set.of(id));
            return check != null && check.contains(id);
        }, 3000, true);

        return withdrawn;
    }

    private void openBank() {
        List<RSObject> banks = dm.getObjectManager().getObjects(obj ->
            Constants.BANK_NAME.equalsIgnoreCase(obj.getName()) && obj.canReach()
        );
        if (banks.isEmpty()) return;

        RSObject bank = (RSObject) dm.getUtils().getClosest(banks);
        if (!bank.interact(Constants.BANK_ACTION)) return;

        double dist = bank.distance(dm.getWorldPosition());
        dm.pollFramesHuman(() -> dm.getWidgetManager().getBank().isVisible(), (int)(dist * 1200 + 600), true);
    }

    private void showOutOfMaterialsAlertAndStopScript() {
        Scene errorScene = ErrorDialog.createErrorScene(
            "DyeMaker",
            "Out of Materials",
            "Total dyes made: " + dm.dyesMade,
            dm::stop
        );
        dm.getStageController().show(errorScene, "Script Complete", false);
    }
}
