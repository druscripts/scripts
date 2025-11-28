package com.druscripts.dyemaker;

import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;

public final class Constants {

    private Constants() {}

    public static final int DRAYNOR_REGION = 12338;

    // Aggie's all walkable area in shop
    public static final RectangleArea[] AGGIE_SHOP_AREAS = new RectangleArea[] {
        new RectangleArea(3083, 3256, 1, 2, 0),
        new RectangleArea(3085, 3256, 2, 2, 0),
        new RectangleArea(3084, 3257, 1, 5, 0),
        new RectangleArea(3087, 3257, 2, 1, 0),
        new RectangleArea(3086, 3258, 2, 3, 0),
        new RectangleArea(3083, 3259, 1, 1, 0),
        new RectangleArea(3085, 3259, 1, 2, 0),
        new RectangleArea(3088, 3259, 1, 2, 0),
        new RectangleArea(3088, 3258, 1, 1, 0)  // Door tile
    };

    public static final WorldPosition AGGIE_SHOP_OUTSIDE = new WorldPosition(3089, 3258, 0);
    public static final WorldPosition AGGIE_DOOR = new WorldPosition(3088, 3258, 0);

    public static final RectangleArea DRAYNOR_BANK_AREA = new RectangleArea(3092, 3242, 2, 3, 0);

    public static final int COINS_ID = 995;
    public static final int COINS_PER_DYE = 5;

    public static final String[] BANK_NAMES = {"Bank booth"};
    public static final String[] BANK_ACTIONS = {"bank"};

    public static final String STAT_DYE_MADE = "dye_made";
    public static final String STAT_RUN_COMPLETED = "run_completed";
}
