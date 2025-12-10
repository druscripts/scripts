package com.druscripts.enchanter.tasks;

import com.druscripts.enchanter.Enchanter;
import com.druscripts.enchanter.data.Stage;
import com.druscripts.utils.script.Task;
import com.druscripts.utils.widget.InventoryUtils;
import com.druscripts.utils.widget.exception.CannotOpenWidgetException;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.ui.spellbook.InvalidSpellbookTypeException;
import com.osmb.api.ui.spellbook.SpellNotFoundException;
import com.osmb.api.ui.spellbook.StandardSpellbook;
import com.osmb.api.ui.tabs.Spellbook;
import com.osmb.api.visual.image.ImageSearchResult;
import com.osmb.api.visual.image.SearchableImage;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.ToleranceComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class EnchantTask extends Task {

    private final Enchanter enchanter;

    private static final int ENCHANT_TIME_MS = 4200; // ~7 ticks at 600ms each
    private static final int BUFFER_TIME_MS = 2500; // Extra buffer time

    public EnchantTask(Enchanter script) {
        super(script);
        this.enchanter = script;
    }

    @Override
    public boolean activate() {
        try {
            int unenchantedId = enchanter.enchantableItem.getUnenchantedId();
            return InventoryUtils.hasItem(enchanter, unenchantedId);
        } catch (CannotOpenWidgetException e) {
            enchanter.log(getClass(), e.getMessage());
            return false;
        }
    }

    @Override
    public void execute() {
        enchanter.task = "Enchanting";
        enchanter.stage = Stage.ENCHANTING;

        if (enchanter.hyperEfficientMode) {
            executeHyperEfficient();
        } else {
            executeNormal();
        }
    }

    /**
     * Normal mode: Select spell, click first item, wait for all items to be enchanted.
     * The game will auto-continue enchanting after the first click.
     */
    private void executeNormal() {
        int unenchantedId = enchanter.enchantableItem.getUnenchantedId();
        Spellbook spellbook = enchanter.getWidgetManager().getSpellbook();
        int targetSpriteId = enchanter.enchantLevel.getSpriteId();

        // Ensure spellbook is open
        if (!spellbook.isOpen()) {
            spellbook.open();
            enchanter.pollFramesUntil(() -> spellbook.isOpen(), 2000, true);
        }

        // Open submenu if needed and select the enchant spell
        if (!selectEnchantSpell(targetSpriteId)) {
            enchanter.log(getClass(), "Failed to select enchant spell");
            return;
        }

        // Wait for tab to change to inventory
        enchanter.pollFramesUntil(() -> !spellbook.isOpen(), 2000, true);

        // Count items before clicking
        int itemCount = getInventoryCount(unenchantedId);
        if (itemCount == 0) {
            enchanter.log(getClass(), "No items to enchant");
            return;
        }

        // Get the first item (earliest slot)
        ItemSearchResult firstItem = getFirstItem(unenchantedId);
        if (firstItem == null) {
            enchanter.log(getClass(), "Could not find first item");
            return;
        }

        // Click the first item
        if (!firstItem.interact()) {
            enchanter.log(getClass(), "Failed to click item");
            return;
        }

        // Calculate wait time: item_count * enchant_time + buffer
        int waitTime = itemCount * ENCHANT_TIME_MS + BUFFER_TIME_MS;

        // Wait until inventory is empty or timeout
        enchanter.pollFramesHuman(() -> getInventoryCount(unenchantedId) == 0, waitTime, true);

        // Count enchanted items
        int countAfter = getInventoryCount(unenchantedId);
        int enchanted = itemCount - countAfter;

        if (enchanted > 0) {
            enchanter.increaseItemsEnchanted(enchanted);
        }
    }

    /**
     * Hyper Efficient mode: Cache slots, rapidly click spell→item→spell→item.
     * This beats the auto-enchant timer by manually triggering each enchant.
     */
    private void executeHyperEfficient() {
        int unenchantedId = enchanter.enchantableItem.getUnenchantedId();
        Spellbook spellbook = enchanter.getWidgetManager().getSpellbook();
        int targetSpriteId = enchanter.enchantLevel.getSpriteId();

        // Cache the inventory slots containing unenchanted items
        List<Integer> itemSlots = getItemSlots(unenchantedId);
        if (itemSlots.isEmpty()) {
            enchanter.log(getClass(), "No items to enchant");
            return;
        }

        int itemsEnchanted = 0;

        for (int slot : itemSlots) {
            // Ensure spellbook is open
            if (!spellbook.isOpen()) {
                spellbook.open();
                enchanter.pollFramesUntil(() -> spellbook.isOpen(), 2000, true);
            }

            // Select the enchant spell (submenu should already be open after first iteration)
            if (!selectEnchantSpell(targetSpriteId)) {
                enchanter.log(getClass(), "Failed to select enchant spell");
                break;
            }

            // Wait for tab to change to inventory
            enchanter.pollFramesUntil(() -> !spellbook.isOpen(), 2000, true);

            // Sanity check: verify the slot still contains our item
            ItemSearchResult itemInSlot = getItemInSlot(unenchantedId, slot);
            if (itemInSlot == null) {
                // Item no longer in this slot, skip
                continue;
            }

            // Click the item
            if (!itemInSlot.interact()) {
                enchanter.log(getClass(), "Failed to click item in slot " + slot);
                continue;
            }

            itemsEnchanted++;
        }

        if (itemsEnchanted > 0) {
            // Small wait for the last enchant to complete
            enchanter.pollFramesUntil(() -> getInventoryCount(unenchantedId) == 0, 1500, true);
            enchanter.increaseItemsEnchanted(itemsEnchanted);
        }
    }

    /**
     * Select the enchant spell from the submenu.
     * Opens submenu if not already open.
     */
    private boolean selectEnchantSpell(int targetSpriteId) {
        Spellbook spellbook = enchanter.getWidgetManager().getSpellbook();

        // Check if the enchant spell is already visible (submenu open)
        ImageSearchResult enchantSprite = findSprite(targetSpriteId);

        if (enchantSprite == null) {
            // Submenu not open, need to open it first
            try {
                boolean openResult = spellbook.selectSpell(StandardSpellbook.JEWELLERY_ENCHANTMENTS, Spellbook.ResultType.SPRITE_CHANGE);
                if (!openResult) {
                    return false;
                }

                // Wait for submenu to appear
                enchanter.pollFramesUntil(() -> findSprite(targetSpriteId) != null, 2000, true);

                enchantSprite = findSprite(targetSpriteId);
                if (enchantSprite == null) {
                    return false;
                }
            } catch (SpellNotFoundException | InvalidSpellbookTypeException e) {
                enchanter.log(getClass(), "Exception opening submenu: " + e.getMessage());
                return false;
            }
        }

        // Click the enchant spell
        return enchanter.getFinger().tap(enchantSprite.getBounds());
    }

    /**
     * Get cached list of slots containing unenchanted items, sorted by slot number.
     */
    private List<Integer> getItemSlots(int itemId) {
        List<Integer> slots = new ArrayList<>();
        try {
            ItemGroupResult inventory = enchanter.getWidgetManager().getInventory().search(Set.of(itemId));
            if (inventory == null || !inventory.contains(itemId)) {
                return slots;
            }

            List<ItemSearchResult> items = inventory.getAllOfItem(itemId);
            if (items != null) {
                items.stream()
                    .sorted(Comparator.comparingInt(ItemSearchResult::getSlot))
                    .forEach(item -> slots.add(item.getSlot()));
            }
        } catch (Exception e) {
            enchanter.log(getClass(), "Error getting item slots: " + e.getMessage());
        }
        return slots;
    }

    /**
     * Get the first item (earliest slot) of the given type.
     */
    private ItemSearchResult getFirstItem(int itemId) {
        try {
            ItemGroupResult inventory = enchanter.getWidgetManager().getInventory().search(Set.of(itemId));
            if (inventory == null || !inventory.contains(itemId)) {
                return null;
            }

            List<ItemSearchResult> items = inventory.getAllOfItem(itemId);
            if (items == null || items.isEmpty()) {
                return null;
            }

            return items.stream()
                .min(Comparator.comparingInt(ItemSearchResult::getSlot))
                .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get item in a specific slot (for sanity check in hyper efficient mode).
     */
    private ItemSearchResult getItemInSlot(int itemId, int slot) {
        try {
            ItemGroupResult inventory = enchanter.getWidgetManager().getInventory().search(Set.of(itemId));
            if (inventory == null || !inventory.contains(itemId)) {
                return null;
            }

            List<ItemSearchResult> items = inventory.getAllOfItem(itemId);
            if (items == null) {
                return null;
            }

            return items.stream()
                .filter(item -> item.getSlot() == slot)
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private int getInventoryCount(int itemId) {
        try {
            return InventoryUtils.getItemCount(enchanter, itemId);
        } catch (CannotOpenWidgetException e) {
            return 0;
        }
    }

    private ImageSearchResult findSprite(int spriteId) {
        try {
            SearchableImage spriteImage = new SearchableImage(
                spriteId,
                enchanter,
                ToleranceComparator.ZERO_TOLERANCE,
                ColorModel.RGB
            );
            return enchanter.getImageAnalyzer().findLocation(spriteImage);
        } catch (Exception e) {
            return null;
        }
    }
}
