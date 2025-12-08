package com.druscripts.utils.production;

import com.druscripts.utils.script.Task;
import com.druscripts.utils.widget.InventoryUtils;
import com.druscripts.utils.widget.exception.CannotOpenWidgetException;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.script.Script;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Generic task for combining two items to create a result item.
 * Reusable across different scripts.
 */
public class CombineItemsTask extends Task {

    private final int primaryItemId;
    private final int secondaryItemId;
    private final int resultItemId;
    private final String taskName;
    private final Consumer<Integer> onItemsCrafted;
    private final boolean useMenuAction;

    public CombineItemsTask(Script script, int primaryItemId, int secondaryItemId, int resultItemId, String taskName) {
        this(script, primaryItemId, secondaryItemId, resultItemId, taskName, null, false);
    }

    public CombineItemsTask(Script script, int primaryItemId, int secondaryItemId, int resultItemId, String taskName, Consumer<Integer> onItemsCrafted) {
        this(script, primaryItemId, secondaryItemId, resultItemId, taskName, onItemsCrafted, false);
    }

    public CombineItemsTask(Script script, int primaryItemId, int secondaryItemId, int resultItemId, String taskName, Consumer<Integer> onItemsCrafted, boolean useMenuAction) {
        super(script);
        this.primaryItemId = primaryItemId;
        this.secondaryItemId = secondaryItemId;
        this.resultItemId = resultItemId;
        this.taskName = taskName;
        this.onItemsCrafted = onItemsCrafted;
        this.useMenuAction = useMenuAction;
    }

    @Override
    public boolean activate() {
        try {
            return InventoryUtils.hasAllItems(script, primaryItemId, secondaryItemId);
        } catch (CannotOpenWidgetException e) {
            return false;
        }
    }

    @Override
    public void execute() {
        log("Starting");

        if (script.getWidgetManager().getBank().isVisible()) {
            script.getWidgetManager().getBank().close();
            return;
        }

        if (!script.getWidgetManager().getInventory().unSelectItemIfSelected()) {
            return;
        }

        ItemGroupResult inventory = script.getWidgetManager().getInventory().search(
            Set.of(primaryItemId, secondaryItemId)
        );

        if (inventory == null) {
            log("Inventory not visible");
            return;
        }

        log("Combining items");

        ItemSearchResult primaryItem = inventory.getItem(new int[]{primaryItemId});
        if (primaryItem == null) {
            log("Could not find primary item in inventory");
            return;
        }

        boolean selected;
        if (useMenuAction) {
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
            selected = primaryItem.interact();
        }

        if (!selected) {
            log("Failed to select primary item");
            return;
        }

        ItemSearchResult secondaryItem = inventory.getItem(new int[]{secondaryItemId});
        if (secondaryItem == null) {
            log("Could not find secondary item in inventory");
            return;
        }

        if (!secondaryItem.interact()) {
            log("Failed to use primary on secondary item");
            return;
        }

        log("Waiting for dialogue...");
        boolean dialogueAppeared = script.pollFramesHuman(() -> {
            DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
            return type == DialogueType.ITEM_OPTION;
        }, 3000, true);

        if (!dialogueAppeared) {
            log("Dialogue did not appear in time");
            return;
        }

        script.pollFramesHuman(() -> false, 600, false);

        log("Selecting result item in dialogue...");

        boolean itemSelected = script.getWidgetManager().getDialogue().selectItem(resultItemId);

        if (!itemSelected) {
            log("First selection attempt failed, trying array method...");
            itemSelected = script.getWidgetManager().getDialogue().selectItem(new int[]{resultItemId});
        }

        if (!itemSelected) {
            log("Failed to select result item with ID: " + resultItemId);
            return;
        }

        log("Successfully selected item, confirming make all...");

        script.pollFramesHuman(() -> false, 1200, false);

        waitUntilCraftingComplete();

        try {
            int made = InventoryUtils.getItemCount(script, resultItemId);
            if (made > 0 && onItemsCrafted != null) {
                onItemsCrafted.accept(made);
            }
            log("Made " + made + " items");
        } catch (CannotOpenWidgetException e) {
            log("Could not count crafted items");
        }
    }

    private void log(String message) {
        script.log(taskName, message);
    }

    private void waitUntilCraftingComplete() {
        log("Crafting...");

        script.pollFramesHuman(() -> {
            try {
                return !InventoryUtils.hasAllItems(script, primaryItemId, secondaryItemId);
            } catch (CannotOpenWidgetException e) {
                return false;
            }
        }, 60000, true);
    }
}
