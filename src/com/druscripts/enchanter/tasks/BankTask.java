package com.druscripts.enchanter.tasks;

import com.druscripts.enchanter.Enchanter;
import com.druscripts.enchanter.data.Constants;
import com.druscripts.enchanter.data.EnchantLevel;
import com.druscripts.enchanter.data.Stage;
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

    private final Enchanter enchanter;

    public BankTask(Enchanter script) {
        super(script);
        this.enchanter = script;
    }

    @Override
    public boolean activate() {
        try {
            boolean hasEnchanted = InventoryUtils.hasAnyItem(enchanter, enchanter.enchantableItem.getEnchantedId());
            boolean hasUnenchanted = InventoryUtils.hasItem(enchanter, enchanter.enchantableItem.getUnenchantedId());

            boolean hasAllRunes = true;
            EnchantLevel.RuneRequirement[] runes = enchanter.enchantLevel.getRunes();
            for (EnchantLevel.RuneRequirement rune : runes) {
                int count = InventoryUtils.getItemCount(enchanter, rune.getRuneId());
                if (count < rune.getAmount()) {
                    hasAllRunes = false;
                    break;
                }
            }

            return hasEnchanted || !hasUnenchanted || !hasAllRunes;
        } catch (CannotOpenWidgetException e) {
            enchanter.log(getClass(), e.getMessage());
            return false;
        }
    }

    @Override
    public void execute() {
        enchanter.task = "Banking";
        enchanter.stage = Stage.BANKING;

        if (!enchanter.getWidgetManager().getBank().isVisible()) {
            enchanter.log(getClass(), "Opening bank...");
            openBank();
            return;
        }

        try {
            if (!InventoryUtils.isEmpty(enchanter)) {
                enchanter.task = "Depositing";
                if (!enchanter.getWidgetManager().getBank().depositAll(Collections.emptySet())) {
                    enchanter.log(getClass(), "Deposit failed");
                    return;
                }
            }
        } catch (CannotOpenWidgetException e) {
            enchanter.log(getClass(), e.getMessage());
            return;
        }

        updateBankCounts();

        if (!canEnchant()) {
            showOutOfMaterialsAlert();
            return;
        }

        if (!withdrawItems()) {
            enchanter.log(getClass(), "Withdrawal failed, closing bank...");
            enchanter.getWidgetManager().getBank().close();
            return;
        }

        enchanter.completeLap();
        enchanter.log(getClass(), "Closing bank...");
        enchanter.getWidgetManager().getBank().close();
    }

    private void openBank() {
        List<RSObject> banks = enchanter.getObjectManager().getObjects(BANK_QUERY);
        if (banks.isEmpty()) {
            enchanter.log(getClass(), "No bank found.");
            return;
        }

        RSObject bank = (RSObject) enchanter.getUtils().getClosest(banks);
        if (!bank.interact(Constants.BANK_ACTIONS)) {
            enchanter.log(getClass(), "Failed to interact with bank.");
            return;
        }

        double dist = bank.distance(enchanter.getWorldPosition());
        enchanter.pollFramesHuman(() -> enchanter.getWidgetManager().getBank().isVisible(), (int)(dist * 1000 + 500), true);
    }

    private void updateBankCounts() {
        enchanter.bankUnenchanted = getBankAmount(enchanter.enchantableItem.getUnenchantedId());
        enchanter.bankEnchanted = getBankAmount(enchanter.enchantableItem.getEnchantedId());
    }

    private int getBankAmount(int itemId) {
        ItemGroupResult bank = enchanter.getWidgetManager().getBank().search(Set.of(itemId));
        if (bank == null || !bank.contains(itemId)) return 0;
        return bank.getAmount(new int[]{itemId});
    }

    private boolean canEnchant() {
        if (enchanter.bankUnenchanted <= 0) {
            return false;
        }

        EnchantLevel.RuneRequirement[] runes = enchanter.enchantLevel.getRunes();
        for (EnchantLevel.RuneRequirement rune : runes) {
            int bankAmount = getBankAmount(rune.getRuneId());
            int invAmount = getInventoryAmount(rune.getRuneId());
            if (bankAmount + invAmount < rune.getAmount()) {
                enchanter.log(getClass(), "Not enough " + rune.getRuneName() + " runes");
                return false;
            }
        }
        return true;
    }

    private int getInventoryAmount(int itemId) {
        try {
            return InventoryUtils.getItemCount(enchanter, itemId);
        } catch (CannotOpenWidgetException e) {
            return 0;
        }
    }

    private void showOutOfMaterialsAlert() {
        enchanter.log(getClass(), "Out of materials!");
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Script Complete");
            alert.setHeaderText("Out of Materials");
            alert.setContentText("Processed all available materials.\n\n" +
                "Items Enchanted: " + enchanter.itemsEnchanted);
            alert.showAndWait();
        });
        enchanter.stop();
    }

    private int calculateBatchSize() {
        int batchSize = Math.min(enchanter.maxBatchSize, enchanter.bankUnenchanted);

        EnchantLevel.RuneRequirement[] runes = enchanter.enchantLevel.getRunes();
        for (EnchantLevel.RuneRequirement rune : runes) {
            int runesAvailable = getBankAmount(rune.getRuneId());
            int enchantsFromRune = runesAvailable / rune.getAmount();
            batchSize = Math.min(batchSize, enchantsFromRune);
        }

        return batchSize;
    }

    private boolean withdrawItems() {
        int batchSize = calculateBatchSize();
        if (batchSize <= 0) {
            enchanter.log(getClass(), "Cannot calculate valid batch size");
            return false;
        }

        enchanter.log(getClass(), "Batch size: " + batchSize);

        if (!withdrawRunes(batchSize)) {
            return false;
        }

        if (!enchanter.getWidgetManager().getBank().withdraw(enchanter.enchantableItem.getUnenchantedId(), batchSize)) {
            enchanter.log(getClass(), "Failed to withdraw unenchanted items");
            return false;
        }

        return true;
    }

    private boolean withdrawRunes(int batchSize) {
        EnchantLevel.RuneRequirement[] runes = enchanter.enchantLevel.getRunes();

        for (EnchantLevel.RuneRequirement rune : runes) {
            int needed = rune.getAmount() * batchSize;

            if (!enchanter.getWidgetManager().getBank().withdraw(rune.getRuneId(), needed)) {
                enchanter.log(getClass(), "Failed to withdraw " + rune.getRuneName() + " runes");
                return false;
            }
        }
        return true;
    }
}
