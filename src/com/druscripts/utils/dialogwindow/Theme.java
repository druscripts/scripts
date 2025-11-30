package com.druscripts.utils.dialogwindow;

/**
 * Common UI theme constants for DruScripts dialogs including colors, dimensions, and spacing.
 * Use these constants to maintain consistent branding and layout across all scripts.
 */
public class Theme {

    // ==================== Brand Colors ====================

    /** Primary brand color - DruScripts blue */
    public static final String BRAND_PRIMARY = "#4a9eff";

    /** Secondary text color for subtle elements */
    public static final String BRAND_SUBTLE = "#888888";

    /** Tertiary text color for very subtle elements */
    public static final String BRAND_TERTIARY = "#666666";

    // ==================== Background Colors ====================

    /** Main dark background color */
    public static final String BACKGROUND_DARK = "#1e1e1e";

    /** Panel background color (lighter than main background) */
    public static final String PANEL_BACKGROUND = "#2b2b2b";

    /** Row/item background color (lighter than panel) */
    public static final String ROW_BACKGROUND = "#353535";

    // ==================== Text Colors ====================

    /** Primary text color (white) */
    public static final String TEXT_PRIMARY = "#ffffff";

    /** Secondary text color (light gray) */
    public static final String TEXT_SECONDARY = "#e0e0e0";

    /** Tertiary text color (medium gray) */
    public static final String TEXT_TERTIARY = "#cccccc";

    /** Muted text color (dark gray) */
    public static final String TEXT_MUTED = "#aaaaaa";

    // ==================== State Colors ====================

    /** Error/Not Started state - Light red */
    public static final String STATE_ERROR = "#ff6b6b";

    /** Warning/In Progress state - Light yellow */
    public static final String STATE_WARNING = "#ffd93d";

    /** Success/Completed state - Light green */
    public static final String STATE_SUCCESS = "#6bcf7f";

    // ==================== Button Colors ====================

    /** Primary action button - Green */
    public static final String BUTTON_PRIMARY = "#27AE60";

    /** Secondary action button - Dark gray */
    public static final String BUTTON_SECONDARY = "#3a3a3a";

    // ==================== Typography Sizes ====================

    /** Script title font size */
    public static final int FONT_SIZE_TITLE = 24;

    /** Version label font size */
    public static final int FONT_SIZE_VERSION = 12;

    /** Website label font size */
    public static final int FONT_SIZE_WEBSITE = 11;

    /** Header text font size */
    public static final int FONT_SIZE_HEADER = 16;

    /** Subheader text font size */
    public static final int FONT_SIZE_SUBHEADER = 14;

    /** Body text font size */
    public static final int FONT_SIZE_BODY = 12;

    /** Small text font size */
    public static final int FONT_SIZE_SMALL = 11;

    // ==================== Padding & Spacing ====================

    /** Standard padding for panel interiors */
    public static final int PADDING_STANDARD = 20;

    /** Small padding */
    public static final int PADDING_SMALL = 15;

    /** Large padding */
    public static final int PADDING_LARGE = 25;

    /** Standard spacing between elements */
    public static final int SPACING_STANDARD = 15;

    /** Small spacing between elements */
    public static final int SPACING_SMALL = 10;

    /** Tiny spacing between elements */
    public static final int SPACING_TINY = 5;

    // ==================== Border Radius ====================

    /** Panel corner radius */
    public static final int PANEL_RADIUS = 12;

    /** Button corner radius */
    public static final int BUTTON_RADIUS = 5;

    /** Small corner radius for rows/items */
    public static final int ROW_RADIUS = 3;

    // ==================== Common Dimensions ====================

    /** Standard button height */
    public static final int BUTTON_HEIGHT = 40;

    /** Standard button width for primary actions */
    public static final int BUTTON_WIDTH = 240;

    /** Quest row height (reduced to fit 22 quests) */
    public static final int QUEST_ROW_HEIGHT = 24;

    /** State button diameter */
    public static final int STATE_BUTTON_SIZE = 14;

    // Private constructor to prevent instantiation
    private Theme() {
        throw new AssertionError("Theme class should not be instantiated");
    }
}
