package com.druscripts.utils.production;

import java.util.function.Consumer;

/**
 * Configuration for combining two items to create a result item.
 * Used by CombineItemsTask to make it reusable across different scripts.
 */
public class CombineItemsConfig {
    private final int primaryItemId;
    private final int secondaryItemId;
    private final int resultItemId;
    private final String taskName;
    private final Consumer<Integer> onItemsCrafted;
    private final boolean useMenuAction;

    public CombineItemsConfig(int primaryItemId, int secondaryItemId, int resultItemId, String taskName) {
        this(primaryItemId, secondaryItemId, resultItemId, taskName, null, false);
    }

    public CombineItemsConfig(int primaryItemId, int secondaryItemId, int resultItemId, String taskName, Consumer<Integer> onItemsCrafted) {
        this(primaryItemId, secondaryItemId, resultItemId, taskName, onItemsCrafted, false);
    }

    public CombineItemsConfig(int primaryItemId, int secondaryItemId, int resultItemId, String taskName, Consumer<Integer> onItemsCrafted, boolean useMenuAction) {
        this.primaryItemId = primaryItemId;
        this.secondaryItemId = secondaryItemId;
        this.resultItemId = resultItemId;
        this.taskName = taskName;
        this.onItemsCrafted = onItemsCrafted;
        this.useMenuAction = useMenuAction;
    }

    public int getPrimaryItemId() { return primaryItemId; }
    public int getSecondaryItemId() { return secondaryItemId; }
    public int getResultItemId() { return resultItemId; }
    public String getTaskName() { return taskName; }
    public boolean useMenuAction() { return useMenuAction; }

    public void notifyItemsCrafted(int count) {
        if (onItemsCrafted != null) {
            onItemsCrafted.accept(count);
        }
    }
}
