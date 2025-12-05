package com.druscripts.piemaker.data;

import static com.druscripts.piemaker.data.Constants.*;

public enum PieType {
    REDBERRY("Redberry Pie", REDBERRY_PIE, UNCOOKED_BERRY_PIE, REDBERRIES, "Redberries"),
    APPLE("Apple Pie", APPLE_PIE, UNCOOKED_APPLE_PIE, COOKING_APPLE, "Cooking Apple"),
    MEAT_CHICKEN("Meat Pie (Chicken)", MEAT_PIE, UNCOOKED_MEAT_PIE, COOKED_CHICKEN, "Cooked Chicken"),
    MEAT_BEEF("Meat Pie (Beef)", MEAT_PIE, UNCOOKED_MEAT_PIE, COOKED_MEAT, "Cooked Meat");

    private final String displayName;
    private final int cookedId;
    private final int uncookedId;
    private final int ingredientId;
    private final String ingredientName;

    PieType(String displayName, int cookedId, int uncookedId, int ingredientId, String ingredientName) {
        this.displayName = displayName;
        this.cookedId = cookedId;
        this.uncookedId = uncookedId;
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCookedId() {
        return cookedId;
    }

    public int getUncookedId() {
        return uncookedId;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }
}
