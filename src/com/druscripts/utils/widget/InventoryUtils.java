package com.druscripts.utils.widget;

import com.osmb.api.script.Script;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.ui.tabs.Tab;
import com.druscripts.utils.widget.exception.CannotOpenWidgetException;

import java.util.Collections;
import java.util.Set;

/**
 * Utility class for common inventory operations.
 */
public class InventoryUtils {

    /**
     * Ensures the inventory tab is open before searching.
     * @param script The script instance
     * @throws CannotOpenWidgetException if inventory cannot be opened
     */
    private static void ensureInventoryTabOpen(Script script) {
        if (script.getWidgetManager().getInventory().isOpen()) return;

        script.getWidgetManager().getInventory().open();
        if (!script.pollFramesHuman(() -> script.getWidgetManager().getInventory().isOpen(), 1000)) {
            throw new CannotOpenWidgetException("Inventory");
        }
    }

    /**
     * Checks if the inventory is empty.
     *
     * @param script The script instance
     * @return true if the inventory is empty (0 items), false otherwise
     */
    public static boolean isEmpty(Script script) {
        ensureInventoryTabOpen(script);

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Collections.emptySet());

        boolean empty = inv.getFreeSlots() == script.getWidgetManager().getInventory().getGroupSize();

        script.log(InventoryUtils.class, empty ? "Inv Empty" : "Inv Not Empty");
        return empty;
    }

    /**
     * Gets the number of free slots in the inventory.
     *
     * @param script The script instance
     * @return The number of free slots
     * @throws CannotOpenWidgetException if inventory cannot be opened
     */
    public static int getFreeSlots(Script script) {
        ensureInventoryTabOpen(script);

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Collections.emptySet());

        return inv.getFreeSlots();
    }

    /**
     * Gets the total number of inventory slots.
     *
     * @param script The script instance
     * @return The total number of inventory slots (typically 28 for OSRS)
     */
    public static int getTotalSlots(Script script) {
        return script.getWidgetManager().getInventory().getGroupSize();
    }

    /**
     * Checks if the inventory is full.
     *
     * @param script The script instance
     * @return true if the inventory is full (0 free slots), false otherwise
     * @throws CannotOpenWidgetException if inventory cannot be opened
     */
    public static boolean isFull(Script script) {
        ensureInventoryTabOpen(script);
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Collections.emptySet());
        return inv.isFull();
    }

    /**
     * Checks if the inventory contains a specific item.
     *
     * @param script The script instance
     * @param itemId The item ID to check for
     * @return true if the item is in the inventory, false otherwise
     * @throws CannotOpenWidgetException if inventory cannot be opened
     */
    public static boolean hasItem(Script script, int itemId) {
        ensureInventoryTabOpen(script);
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(itemId));
        return inv != null && inv.contains(itemId);
    }

    /**
     * Gets the count of a specific item in the inventory.
     *
     * @param script The script instance
     * @param itemId The item ID to count
     * @return The number of that item in the inventory
     * @throws CannotOpenWidgetException if inventory cannot be opened
     */
    public static int getItemCount(Script script, int itemId) {
        ensureInventoryTabOpen(script);
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(itemId));
        if (inv == null || !inv.contains(itemId)) return 0;
        return inv.getAmount(itemId);
    }
}
