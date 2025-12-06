package com.druscripts.utils.production;

import com.druscripts.utils.script.Task;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.script.Script;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;

import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Generic task for combining two items to create a result item.
 * Reusable across different scripts by passing CombineItemsConfig.
 *
 * Extends Task (not PremiumTask) so it works with both free and premium scripts.
 */
public class CombineItemsTask extends Task {

    private final CombineItemsConfig config;
    private final BooleanSupplier additionalActivationCondition;

    public CombineItemsTask(Script script, CombineItemsConfig config) {
        this(script, config, null);
    }

    public CombineItemsTask(Script script, CombineItemsConfig config, BooleanSupplier additionalActivationCondition) {
        super(script);
        this.config = config;
        this.additionalActivationCondition = additionalActivationCondition;
    }

    @Override
    public boolean activate() {
        // Check additional condition if provided
        if (additionalActivationCondition != null && !additionalActivationCondition.getAsBoolean()) {
            return false;
        }

        // Activate when we have both items in inventory
        ItemGroupResult inventory = script.getWidgetManager().getInventory().search(
            Set.of(config.getPrimaryItemId(), config.getSecondaryItemId())
        );

        if (inventory == null) {
            return false;
        }

        // We need both ingredients to craft
        return inventory.contains(config.getPrimaryItemId())
            && inventory.contains(config.getSecondaryItemId());
    }

    @Override
    public void execute() {
        log("Starting");

        // Close bank if visible
        if (script.getWidgetManager().getBank().isVisible()) {
            script.getWidgetManager().getBank().close();
            return;
        }

        // Unselect any selected item
        if (!script.getWidgetManager().getInventory().unSelectItemIfSelected()) {
            return;
        }

        ItemGroupResult inventory = script.getWidgetManager().getInventory().search(
            Set.of(config.getPrimaryItemId(), config.getSecondaryItemId())
        );

        if (inventory == null) {
            log("Inventory not visible");
            return;
        }

        // Use primary item on secondary item
        log("Combining items");

        ItemSearchResult primaryItem = inventory.getItem(new int[]{config.getPrimaryItemId()});
        if (primaryItem == null) {
            log("Could not find primary item in inventory");
            return;
        }

        // Select the primary item (with menu action if required)
        boolean selected;
        if (config.useMenuAction()) {
            // Use "Use" menu action (e.g., for plain pizza which defaults to "Eat")
            selected = primaryItem.interact(entries -> {
                for (com.osmb.api.input.MenuEntry entry : entries) {
                    String action = entry.getAction();
                    if (action != null && action.equalsIgnoreCase("Use")) {
                        return entry;
                    }
                }
                return null;
            });
        } else {
            // Use default action
            selected = primaryItem.interact();
        }

        if (!selected) {
            log("Failed to select primary item");
            return;
        }

        // Use it on secondary item
        ItemSearchResult secondaryItem = inventory.getItem(new int[]{config.getSecondaryItemId()});
        if (secondaryItem == null) {
            log("Could not find secondary item in inventory");
            return;
        }

        if (!secondaryItem.interact()) {
            log("Failed to use primary on secondary item");
            return;
        }

        // Wait for dialogue to appear
        log("Waiting for dialogue...");
        boolean dialogueAppeared = script.pollFramesHuman(() -> {
            DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
            return type == DialogueType.ITEM_OPTION;
        }, 3000, true);

        if (!dialogueAppeared) {
            log("Dialogue did not appear in time");
            return;
        }

        // Give the dialogue a moment to fully load items
        script.pollFramesHuman(() -> false, 600, false);

        log("Selecting result item in dialogue...");

        // Try to select the result item in the dialogue
        boolean itemSelected = script.getWidgetManager().getDialogue().selectItem(config.getResultItemId());

        if (!itemSelected) {
            log("First selection attempt failed, trying array method...");
            itemSelected = script.getWidgetManager().getDialogue().selectItem(new int[]{config.getResultItemId()});
        }

        if (!itemSelected) {
            log("Failed to select result item with ID: " + config.getResultItemId());
            return;
        }

        log("Successfully selected item, confirming make all...");

        // Wait after selecting to ensure it registers before crafting starts
        script.pollFramesHuman(() -> false, 1200, false);

        // Wait for crafting to complete
        waitUntilCraftingComplete();

        // Count how many we made
        ItemGroupResult afterCraft = script.getWidgetManager().getInventory().search(
            Set.of(config.getResultItemId())
        );

        if (afterCraft != null && afterCraft.contains(config.getResultItemId())) {
            int made = afterCraft.getAmount(new int[]{config.getResultItemId()});
            config.notifyItemsCrafted(made);
            log("Made " + made + " items");
        }
    }

    private void log(String message) {
        script.log(config.getTaskName(), message);
    }

    private void waitUntilCraftingComplete() {
        log("Crafting...");

        // Wait until we have no more of either ingredient (all items have been crafted)
        script.pollFramesHuman(() -> {
            ItemGroupResult inventory = script.getWidgetManager().getInventory().search(
                Set.of(config.getPrimaryItemId(), config.getSecondaryItemId())
            );

            if (inventory == null) {
                return false;
            }

            // Crafting is complete when we have no more of either ingredient
            return !inventory.contains(config.getPrimaryItemId())
                || !inventory.contains(config.getSecondaryItemId());
        }, 60000, true);
    }
}
