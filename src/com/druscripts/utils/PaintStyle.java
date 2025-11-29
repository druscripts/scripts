package com.druscripts.utils;

import com.osmb.api.visual.drawing.Canvas;

import java.awt.Color;
import java.awt.Font;

/**
 * Common UI styling constants for script paint overlays.
 * Provides java.awt.Color versions matching DialogConstants for consistent branding.
 */
public class PaintStyle {

    // ==================== Background Colors ====================

    /** Main dark background color */
    public static final Color BACKGROUND_COLOR_DARK = Color.decode("#1e1e1e");

    /** Panel background color (lighter than main background) */
    public static final Color BACKGROUND_COLOR_PANEL = Color.decode("#2b2b2b");

    /** Row/item background color (lighter than panel) */
    public static final Color BACKGROUND_COLOR_ROW = Color.decode("#353535");

    // ==================== Text Colors ====================

    /** Title text color (white) */
    public static final Color TEXT_COLOR_TITLE = Color.decode("#ffffff");

    /** Body text color (light gray) */
    public static final Color TEXT_COLOR_BODY = Color.decode("#e0e0e0");

    /** Muted text color (dark gray) */
    public static final Color TEXT_COLOR_MUTED = Color.decode("#aaaaaa");

    /** Brand/highlight text color - DruScripts blue */
    public static final Color TEXT_COLOR_BRAND = Color.decode("#4a9eff");

    /** Task/in-progress text color - Yellow */
    public static final Color TEXT_COLOR_TASK = Color.decode("#ffd93d");

    /** Success/completed text color - Green */
    public static final Color TEXT_COLOR_SUCCESS = Color.decode("#6bcf7f");

    /** Error text color - Red */
    public static final Color TEXT_COLOR_ERROR = Color.decode("#ff6b6b");

    // ==================== Button Colors ====================

    /** Primary action button - Green */
    public static final Color BUTTON_COLOR_PRIMARY = Color.decode("#27AE60");

    /** Secondary action button - Dark gray */
    public static final Color BUTTON_COLOR_SECONDARY = Color.decode("#3a3a3a");

    // ==================== Fonts ====================

    /** Title font - Bold */
    public static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 14);

    /** Body font - Regular */
    public static final Font FONT_BODY = new Font("Arial", Font.PLAIN, 12);

    /** Small font - Regular */
    public static final Font FONT_SMALL = new Font("Arial", Font.PLAIN, 11);

    // ==================== Layout Constants ====================

    /** Default X position for paint panel */
    public static final int START_X = 5;

    /** Default Y position for paint panel */
    public static final int START_Y = 35;

    /** Panel corner radius */
    public static final int PANEL_RADIUS = 8;

    /** Standard padding */
    public static final int PADDING = 10;

    /** Line height for text rows */
    public static final int LINE_HEIGHT = 18;

    /** Title line height (larger than body) */
    public static final int TITLE_HEIGHT = 22;

    /** Panel opacity (0.0 - 1.0) */
    public static final double PANEL_OPACITY = 0.90;

    // ==================== Helper Methods ====================

    /**
     * Calculates the required panel height for a given number of body lines.
     * Accounts for padding, title height, and line spacing.
     *
     * @param lines number of body text lines (excluding title)
     * @return total height needed for the panel
     */
    public static int calculateHeightGivenLines(int lines) {
        return PADDING * 2 + TITLE_HEIGHT + (lines * LINE_HEIGHT);
    }

    /**
     * Draws a panel background at the default position.
     *
     * @param c the canvas to draw on
     * @param width panel width
     * @param lines number of body lines (used to calculate height)
     */
    public static void drawBackground(Canvas c, int width, int lines) {
        c.fillRect(START_X, START_Y, width, calculateHeightGivenLines(lines),
                   BACKGROUND_COLOR_PANEL.getRGB(), PANEL_OPACITY);
    }

    /**
     * Draws the title and returns the Y position for the first body line.
     *
     * @param c the canvas to draw on
     * @param title the title text
     * @return Y position for the next line
     */
    public static int drawTitle(Canvas c, String title) {
        int textX = START_X + PADDING;
        int textY = START_Y + PADDING + 14;
        c.drawText(title, textX, textY, TEXT_COLOR_TITLE.getRGB(), FONT_TITLE);
        return textY + TITLE_HEIGHT;
    }

    /**
     * Draws a line of text and returns the Y position for the next line.
     *
     * @param c the canvas to draw on
     * @param text the text to draw
     * @param y current Y position
     * @param color text color
     * @return Y position for the next line
     */
    public static int drawLine(Canvas c, String text, int y, Color color) {
        int textX = START_X + PADDING;
        c.drawText(text, textX, y, color.getRGB(), FONT_BODY);
        return y + LINE_HEIGHT;
    }

    // Private constructor to prevent instantiation
    private PaintStyle() {
        throw new AssertionError("PaintStyle class should not be instantiated");
    }
}
