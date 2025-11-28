package com.druscripts.dyemaker;

import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;

public final class Constants {

    private Constants() {}

    // Draynor Bank
    public static final RectangleArea DRAYNOR_BANK_AREA = new RectangleArea(3092, 3242, 2, 3, 0);
    public static final WorldPosition DRAYNOR_BANK_CENTER = new WorldPosition(3093, 3243, 0);

    // Aggie's shop (multiple rectangles for irregular shape)
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

    public static boolean isInAggieShop(WorldPosition pos) {
        if (pos == null) return false;
        int px = pos.getX();
        int py = pos.getY();
        int pz = pos.getPlane();

        for (RectangleArea area : AGGIE_SHOP_AREAS) {
            int ax = area.getX();
            int ay = area.getY();
            int width = area.getWidth();
            int height = area.getHeight();
            int plane = area.getPlane();

            if (pz == plane && px >= ax && px < ax + width && py >= ay && py < ay + height) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInBankArea(WorldPosition pos) {
        if (pos == null) return false;
        int px = pos.getX();
        int py = pos.getY();
        int pz = pos.getPlane();
        int ax = DRAYNOR_BANK_AREA.getX();
        int ay = DRAYNOR_BANK_AREA.getY();
        int width = DRAYNOR_BANK_AREA.getWidth();
        int height = DRAYNOR_BANK_AREA.getHeight();
        int plane = DRAYNOR_BANK_AREA.getPlane();
        return pz == plane && px >= ax && px < ax + width && py >= ay && py < ay + height;
    }

    // Door positions
    public static final WorldPosition AGGIE_SHOP_OUTSIDE = new WorldPosition(3089, 3258, 0);
    public static final WorldPosition AGGIE_DOOR = new WorldPosition(3088, 3258, 0);

    public static final int DRAYNOR_REGION = 12338;
    public static final int COINS_ID = 995;
    public static final int COINS_PER_DYE = 5;

    public static final String[] BANK_NAMES = {"Bank", "Bank booth", "Bank chest"};
    public static final String[] BANK_ACTIONS = {"bank", "open", "use"};

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
    }

    public static final String STAT_DYE_MADE = "dye_made";
    public static final String STAT_RUN_COMPLETED = "run_completed";
}
