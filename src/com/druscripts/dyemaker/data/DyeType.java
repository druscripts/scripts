package com.druscripts.dyemaker.data;
import com.druscripts.dyemaker.data.Constants;

public enum DyeType {
    RED(1763, 1951, 3, "Redberries", false, "red_dye_made", "red_lap_time"),
    YELLOW(1765, 1957, 2, "Onions", false, "yellow_dye_made", "yellow_lap_time"),
    BLUE(1767, 1793, 2, "Woad leaves", true, "blue_dye_made", "blue_lap_time");

    private final int dyeId;
    private final int ingredientId;
    private final int ingredientCount;
    private final String ingredientName;
    private final int batchesPerRun;
    private final boolean stackable;
    private final String statName;
    private final String lapTimeStatName;

    DyeType(
        int dyeId,
        int ingredientId,
        int ingredientCount,
        String ingredientName,
        boolean stackable,
        String statName,
        String lapTimeStatName)
    {
        this.dyeId = dyeId;
        this.ingredientId = ingredientId;
        this.ingredientCount = ingredientCount;
        this.ingredientName = ingredientName;
        this.stackable = stackable;
        this.statName = statName;
        this.lapTimeStatName = lapTimeStatName;

        // For stackable ingredients, full inventory available. For non-stackable,
        // one slot is reserved for coins so we subtract 1.
        this.batchesPerRun = stackable
            ? Constants.MAX_INVENTORY_SIZE
            : (Constants.MAX_INVENTORY_SIZE - 1) / ingredientCount;
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

    public int getBatchesPerRun() {
        return batchesPerRun;
    }

    public boolean isStackable() {
        return stackable;
    }

    public String getDisplayName() {
        return name().charAt(0) + name().substring(1).toLowerCase() + " Dye";
    }

    public String getStatName() {
        return statName;
    }

    public String getLapTimeStatName() {
        return lapTimeStatName;
    }
}
