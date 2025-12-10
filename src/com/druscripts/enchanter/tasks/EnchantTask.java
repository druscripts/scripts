package com.druscripts.enchanter.tasks;

import com.druscripts.enchanter.Enchanter;
import com.druscripts.enchanter.data.Constants;
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

import java.util.List;
import java.util.Set;

public class EnchantTask extends Task {

    private final Enchanter enchanter;

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

        int unenchantedId = enchanter.enchantableItem.getUnenchantedId();

        int countBefore = getInventoryCount(unenchantedId);
        if (countBefore == 0) {
            enchanter.log(getClass(), "No items to enchant");
            return;
        }

        String spellName = "Lvl-" + enchanter.enchantLevel.getLevel() + " Enchant";

        ItemGroupResult inventory = enchanter.getWidgetManager().getInventory().search(Set.of(unenchantedId));
        if (inventory == null || !inventory.contains(unenchantedId)) {
            enchanter.log(getClass(), "Could not find item in inventory");
            return;
        }

        // Get the first item (earliest inventory slot)
        List<ItemSearchResult> items = inventory.getAllOfItem(unenchantedId);
        if (items == null || items.isEmpty()) {
            enchanter.log(getClass(), "Could not get item reference");
            return;
        }

        // Sort by slot and get the first one
        ItemSearchResult item = items.stream()
            .min((a, b) -> Integer.compare(a.getSlot(), b.getSlot()))
            .orElse(null);

        if (item == null) {
            enchanter.log(getClass(), "Could not get first item");
            return;
        }

        boolean spellCast = castEnchantSpell(spellName, item, countBefore);
        if (!spellCast) {
            enchanter.log(getClass(), "Failed to cast spell");
            return;
        }

        int countAfter = getInventoryCount(unenchantedId);
        int enchanted = countBefore - countAfter;

        if (enchanted > 0) {
            enchanter.increaseItemsEnchanted(enchanted);
        }
    }

    private int getInventoryCount(int itemId) {
        try {
            return InventoryUtils.getItemCount(enchanter, itemId);
        } catch (CannotOpenWidgetException e) {
            return 0;
        }
    }

    private boolean castEnchantSpell(String spellName, ItemSearchResult item, int itemCount) {
        Spellbook spellbook = enchanter.getWidgetManager().getSpellbook();
        int targetSpriteId = enchanter.enchantLevel.getSpriteId();
        int unenchantedId = enchanter.enchantableItem.getUnenchantedId();

        // Ensure spellbook is open
        if (!spellbook.isOpen()) {
            spellbook.open();
            enchanter.pollFramesUntil(() -> spellbook.isOpen(), 2000, true);
        }

        // Check if the enchant spell is already visible (submenu open)
        ImageSearchResult enchantSprite = findSprite(targetSpriteId);

        if (enchantSprite == null) {
            // Submenu not open, need to open it first
            try {
                boolean openResult = spellbook.selectSpell(StandardSpellbook.JEWELLERY_ENCHANTMENTS, Spellbook.ResultType.SPRITE_CHANGE);
                if (!openResult) {
                    enchanter.log(getClass(), "Failed to open jewellery enchantments submenu");
                    return false;
                }

                // Wait for submenu to appear (instant poll, no human delay)
                enchanter.pollFramesUntil(() -> findSprite(targetSpriteId) != null, 2000, true);

                // Search for the enchant spell again
                enchantSprite = findSprite(targetSpriteId);
                if (enchantSprite == null) {
                    enchanter.log(getClass(), "Could not find enchant sprite after opening submenu");
                    return false;
                }
            } catch (SpellNotFoundException | InvalidSpellbookTypeException e) {
                enchanter.log(getClass(), "Exception opening submenu: " + e.getMessage());
                return false;
            }
        }

        // Click the enchant spell
        if (!enchanter.getFinger().tap(enchantSprite.getBounds())) {
            enchanter.log(getClass(), "Failed to click enchant spell");
            return false;
        }

        // Wait for spell to be selected (tab changes to inventory) - instant poll
        enchanter.pollFramesUntil(() -> !spellbook.isOpen(), 2000, true);

        // Click on the item to cast the spell
        if (!item.interact()) {
            enchanter.log(getClass(), "Failed to interact with item");
            return false;
        }

        // If this is the last item, wait for enchant to complete with human timing
        // Otherwise, just wait quickly for the item to be enchanted before continuing
        if (itemCount == 1) {
            // Last item - wait with human timing for it to be enchanted
            enchanter.pollFramesHuman(() -> getInventoryCount(unenchantedId) == 0, 2000, true);
        } else {
            // Multiple items - quick poll, we'll immediately go back to spellbook
            enchanter.pollFramesUntil(() -> getInventoryCount(unenchantedId) < itemCount, 2000, true);
        }

        return true;
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
            enchanter.log(getClass(), "Error finding sprite " + spriteId + ": " + e.getMessage());
            return null;
        }
    }
}
