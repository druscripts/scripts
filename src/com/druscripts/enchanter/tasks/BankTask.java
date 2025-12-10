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
import java.util.HashSet;
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
        return needsToBank();
    }

    private boolean needsToBank() {
        try {
            int unenchantedId = enchanter.enchantableItem.getUnenchantedId();
            int enchantedId = enchanter.enchantableItem.getEnchantedId();

            boolean hasEnchanted = InventoryUtils.hasAnyItem(enchanter, enchantedId);
            boolean hasUnenchanted = InventoryUtils.hasItem(enchanter, unenchantedId);

            // Check if we have all required runes in inventory
            boolean hasAllRunes = hasRequiredRunes();

            return hasEnchanted || !hasUnenchanted || !hasAllRunes;
        } catch (CannotOpenWidgetException e) {
            enchanter.log(getClass(), e.getMessage());
            return false;
        }
    }

    private boolean hasRequiredRunes() {
        try {
            EnchantLevel.RuneRequirement[] runes = enchanter.enchantLevel.getRunes();
            for (EnchantLevel.RuneRequirement rune : runes) {
                int count = InventoryUtils.getItemCount(enchanter, rune.getRuneId());
                if (count < rune.getAmount()) {
                    return false;
                }
            }
            return true;
        } catch (CannotOpenWidgetException e) {
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
                Set<Integer> keepItems = getRuneIdsToKeep();
                if (!enchanter.getWidgetManager().getBank().depositAll(keepItems)) {
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

    private Set<Integer> getRuneIdsToKeep() {
        Set<Integer> runeIds = new HashSet<>();
        EnchantLevel.RuneRequirement[] runes = enchanter.enchantLevel.getRunes();
        for (EnchantLevel.RuneRequirement rune : runes) {
            runeIds.add(rune.getRuneId());
        }
        return runeIds;
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
        // Check runes - need enough for at least one enchant
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

    private boolean withdrawItems() {
        // First, withdraw runes if we don't have enough for a full inventory
        if (!withdrawRunes()) {
            return false;
        }

        int unenchantedId = enchanter.enchantableItem.getUnenchantedId();

        // Withdraw unenchanted items (fill inventory)
        ItemGroupResult bank = enchanter.getWidgetManager().getBank().search(Set.of(unenchantedId));
        if (bank == null || !bank.contains(unenchantedId)) {
            enchanter.log(getClass(), "No unenchanted items in bank");
            return false;
        }

        // Withdraw all available (up to inventory space)
        if (!enchanter.getWidgetManager().getBank().withdraw(unenchantedId, Integer.MAX_VALUE)) {
            enchanter.log(getClass(), "Failed to withdraw unenchanted items");
            return false;
        }

        return true;
    }

    private boolean withdrawRunes() {
        EnchantLevel.RuneRequirement[] runes = enchanter.enchantLevel.getRunes();

        // Calculate how many items we can enchant (roughly 27 slots for items, rest for runes)
        // For simplicity, withdraw enough runes for ~27 enchants
        int enchantsPerTrip = 27;

        for (EnchantLevel.RuneRequirement rune : runes) {
            int needed = rune.getAmount() * enchantsPerTrip;
            int have = getInventoryAmount(rune.getRuneId());

            if (have < needed) {
                int toWithdraw = needed - have;
                int bankHas = getBankAmount(rune.getRuneId());

                if (bankHas > 0) {
                    int withdrawAmount = Math.min(toWithdraw, bankHas);
                    if (!enchanter.getWidgetManager().getBank().withdraw(rune.getRuneId(), withdrawAmount)) {
                        enchanter.log(getClass(), "Failed to withdraw " + rune.getRuneName() + " runes");
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
