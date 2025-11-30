package com.druscripts.dyemaker;

public enum DyeType {
    RED(1763, 1951, 3, "Redberries", false),
    YELLOW(1765, 1957, 2, "Onions", false),
    BLUE(1767, 1793, 2, "Woad leaves", true);

    private final int dyeId;
    private final int ingredientId;
    private final int ingredientCount;
    private final String ingredientName;
    private final boolean stackable;

    DyeType(int dyeId, int ingredientId, int ingredientCount, String ingredientName, boolean stackable) {
        this.dyeId = dyeId;
        this.ingredientId = ingredientId;
        this.ingredientCount = ingredientCount;
        this.ingredientName = ingredientName;
        this.stackable = stackable;
    }

    public int getDyeId() {
        return dyeId;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public int getIngredientCount() {
        return ingredientCount;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public boolean isStackable() {
        return stackable;
    }

    public String getDisplayName() {
        return name().charAt(0) + name().substring(1).toLowerCase() + " Dye";
    }

    public String getStatName() {
        switch (this) {
            case RED: return Constants.STAT_RED_DYE_MADE;
            case YELLOW: return Constants.STAT_YELLOW_DYE_MADE;
            case BLUE: return Constants.STAT_BLUE_DYE_MADE;
            default: return "dye_made";
        }
    }

    public String getLapTimeStatName() {
        switch (this) {
            case RED: return Constants.STAT_RED_LAP_TIME;
            case YELLOW: return Constants.STAT_YELLOW_LAP_TIME;
            case BLUE: return Constants.STAT_BLUE_LAP_TIME;
            default: return "lap_time";
        }
    }
}
