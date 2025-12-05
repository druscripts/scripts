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
    private final String taskDescription;
    private final String combiningDescription;
    private final String craftingDescription;
    private final String logClassName;
    private final Consumer<Integer> onItemsCrafted;
    private final boolean useMenuAction;

    private CombineItemsConfig(Builder builder) {
        this.primaryItemId = builder.primaryItemId;
        this.secondaryItemId = builder.secondaryItemId;
        this.resultItemId = builder.resultItemId;
        this.taskDescription = builder.taskDescription;
        this.combiningDescription = builder.combiningDescription;
        this.craftingDescription = builder.craftingDescription;
        this.logClassName = builder.logClassName;
        this.onItemsCrafted = builder.onItemsCrafted;
        this.useMenuAction = builder.useMenuAction;
    }

    public int getPrimaryItemId() { return primaryItemId; }
    public int getSecondaryItemId() { return secondaryItemId; }
    public int getResultItemId() { return resultItemId; }
    public String getTaskDescription() { return taskDescription; }
    public String getCombiningDescription() { return combiningDescription; }
    public String getCraftingDescription() { return craftingDescription; }
    public String getLogClassName() { return logClassName; }
    public boolean useMenuAction() { return useMenuAction; }

    public void notifyItemsCrafted(int count) {
        if (onItemsCrafted != null) {
            onItemsCrafted.accept(count);
        }
    }

    public static class Builder {
        private int primaryItemId;
        private int secondaryItemId;
        private int resultItemId;
        private String taskDescription = "Crafting items";
        private String combiningDescription = "Combining items";
        private String craftingDescription = "Crafting";
        private String logClassName = "CombineItemsTask";
        private Consumer<Integer> onItemsCrafted;
        private boolean useMenuAction = false;

        public Builder primaryItem(int itemId) {
            this.primaryItemId = itemId;
            return this;
        }

        public Builder secondaryItem(int itemId) {
            this.secondaryItemId = itemId;
            return this;
        }

        public Builder resultItem(int itemId) {
            this.resultItemId = itemId;
            return this;
        }

        public Builder taskDescription(String description) {
            this.taskDescription = description;
            return this;
        }

        public Builder combiningDescription(String description) {
            this.combiningDescription = description;
            return this;
        }

        public Builder craftingDescription(String description) {
            this.craftingDescription = description;
            return this;
        }

        public Builder logClassName(String className) {
            this.logClassName = className;
            return this;
        }

        public Builder onItemsCrafted(Consumer<Integer> callback) {
            this.onItemsCrafted = callback;
            return this;
        }

        public Builder useMenuAction(boolean useMenu) {
            this.useMenuAction = useMenu;
            return this;
        }

        public CombineItemsConfig build() {
            return new CombineItemsConfig(this);
        }
    }
}
