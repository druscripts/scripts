package com.druscripts.piemaker.data;

import com.osmb.api.location.area.impl.RectangleArea;

public final class Constants {

    private Constants() {}

    // === Regions ===
    public static final int LUMBRIDGE_REGION = 12850;
    public static final int GRAND_EXCHANGE_REGION = 12598;

    // === Lumbridge Areas ===
    public static final RectangleArea BANK_AREA = new RectangleArea(3207, 3217, 2, 2, 2);
    public static final RectangleArea STAIRS_AREA_FLOOR_2 = new RectangleArea(3205, 3209, 2, 2, 2);
    public static final RectangleArea RANGE_AREA = new RectangleArea(3207, 3212, 4, 4, 0);

    // === Item IDs ===
    public static final int FLOUR = 1933;
    public static final int PIE_DISH = 2313;
    public static final int PASTRY_DOUGH = 1953;
    public static final int PIE_SHELL = 2315;

    public static final int BUCKET_OF_WATER = 1929;
    public static final int BOWL_OF_WATER = 1921;
    public static final int JUG_OF_WATER = 1937;

    public static final int REDBERRIES = 1951;
    public static final int COOKING_APPLE = 1955;
    public static final int COOKED_MEAT = 2142;
    public static final int COOKED_CHICKEN = 2140;

    public static final int UNCOOKED_BERRY_PIE = 2321;
    public static final int UNCOOKED_APPLE_PIE = 2317;
    public static final int UNCOOKED_MEAT_PIE = 2319;

    public static final int REDBERRY_PIE = 2325;
    public static final int APPLE_PIE = 2323;
    public static final int MEAT_PIE = 2327;

    // === Inventory Counts ===
    public static final int FLOUR_COUNT = 9;
    public static final int WATER_COUNT = 9;
    public static final int DOUGH_COUNT = 14;
    public static final int DISH_COUNT = 14;
    public static final int SHELL_COUNT = 14;
    public static final int INGREDIENT_COUNT = 14;
    public static final int UNCOOKED_COUNT = 28;

    // === Bank Detection ===
    public static final String[] BANK_NAMES = {
        "Bank", "Chest", "Bank booth", "Bank chest",
        "Grand Exchange booth", "Bank counter", "Bank table"
    };

    public static final String[] BANK_ACTIONS = {
        "bank", "open", "use", "bank banker"
    };
}
