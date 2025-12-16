package com.druscripts.enchanter.tasks;

import com.druscripts.enchanter.Enchanter;
import com.druscripts.enchanter.data.Stage;
import com.druscripts.utils.script.Task;
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
    private static final int ENCHANT_TIME_MS = 4200;
    private static final int BUFFER_TIME_MS = 2500;

    public EnchantTask(Enchanter script) {
        super(script);
        this.enchanter = script;
    }

    @Override
    public boolean activate() {
        return !getItems().isEmpty();
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
     * Normal mode: Select spell, click first item, let game auto-enchant the rest.
     */
    private void executeNormal() {
        Spellbook spellbook = enchanter.getWidgetManager().getSpellbook();
        int spriteId = enchanter.enchantLevel.getSpriteId();

        List<ItemSearchResult> items = getItems();
        if (items.isEmpty()) {
            return;
        }

        if (!selectEnchantSpell(spriteId)) {
            enchanter.log(getClass(), "Failed to select enchant spell");
            return;
        }

        enchanter.pollFramesUntil(() -> !spellbook.isOpen(), 2000, true);

        if (!items.get(0).interact()) {
            enchanter.log(getClass(), "Failed to click item");
            return;
        }

        int itemCount = items.size();
        int waitTime = itemCount * ENCHANT_TIME_MS + BUFFER_TIME_MS;
        enchanter.pollFramesHuman(() -> getItems().isEmpty(), waitTime, true);

        int remaining = getItems().size();
        int enchanted = itemCount - remaining;
        if (enchanted > 0) {
            enchanter.increaseItemsEnchanted(enchanted);
        }
    }

    /**
     * Hyper Efficient mode: Rapidly cycle spell→item→spell→item.
     * Game auto-returns to spellbook with submenu open after each item click.
     */
    private void executeHyperEfficient() {
        Spellbook spellbook = enchanter.getWidgetManager().getSpellbook();
        int spriteId = enchanter.enchantLevel.getSpriteId();

        List<ItemSearchResult> items = getItems();
        if (items.isEmpty()) {
            return;
        }

        // Sort by slot so we click top-left to bottom-right
        items.sort(Comparator.comparingInt(ItemSearchResult::getSlot));

        if (!selectEnchantSpell(spriteId)) {
            enchanter.log(getClass(), "Failed to select enchant spell");
            return;
        }

        int enchanted = 0;

        for (int i = 0; i < items.size(); i++) {
            enchanter.pollFramesUntil(() -> !spellbook.isOpen(), 2000, true);

            if (!items.get(i).interact()) {
                continue;
            }
            enchanted++;

            if (i < items.size() - 1) {
                enchanter.pollFramesUntil(() -> spellbook.isOpen(), 2000, true);

                ImageSearchResult spell = findSprite(spriteId);
                if (spell == null || !enchanter.getFinger().tap(spell.getBounds())) {
                    enchanter.log(getClass(), "Lost submenu");
                    break;
                }
            }
        }

        if (enchanted > 0) {
            enchanter.pollFramesUntil(() -> getItems().isEmpty(), 1500, true);
            enchanter.increaseItemsEnchanted(enchanted);
        }
    }

    /**
     * Opens spellbook/submenu if needed, clicks the enchant spell.
     */
    private boolean selectEnchantSpell(int spriteId) {
        Spellbook spellbook = enchanter.getWidgetManager().getSpellbook();

        // Ensure spellbook tab is open first
        if (!spellbook.isOpen()) {
            spellbook.open();
            enchanter.pollFramesUntil(spellbook::isOpen, 2000, true);
        }

        // Check if submenu is already open (sprite visible)
        ImageSearchResult sprite = findSprite(spriteId);

        if (sprite == null) {
            // Submenu not open, click Jewellery Enchantments to open it
            try {
                if (!spellbook.selectSpell(StandardSpellbook.JEWELLERY_ENCHANTMENTS, Spellbook.ResultType.SPRITE_CHANGE)) {
                    return false;
                }
                enchanter.pollFramesUntil(() -> findSprite(spriteId) != null, 2000, true);
                sprite = findSprite(spriteId);
                if (sprite == null) {
                    return false;
                }
            } catch (SpellNotFoundException | InvalidSpellbookTypeException e) {
                enchanter.log(getClass(), "Exception: " + e.getMessage());
                return false;
            }
        }

        return enchanter.getFinger().tap(sprite.getBounds());
    }

    private List<ItemSearchResult> getItems() {
        try {
            int itemId = enchanter.enchantableItem.getUnenchantedId();
            ItemGroupResult inventory = enchanter.getWidgetManager().getInventory().search(Set.of(itemId));
            if (inventory == null || !inventory.contains(itemId)) {
                return new ArrayList<>();
            }
            List<ItemSearchResult> items = inventory.getAllOfItem(itemId);
            return items != null ? new ArrayList<>(items) : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private ImageSearchResult findSprite(int spriteId) {
        try {
            return enchanter.getImageAnalyzer().findLocation(
                new SearchableImage(spriteId, enchanter, ToleranceComparator.ZERO_TOLERANCE, ColorModel.RGB)
            );
        } catch (Exception e) {
            return null;
        }
    }
}
