package com.druscripts.utils.dialogwindow;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Utility functions for Canvas-based immediate mode UIs.
 * Provides common drawing operations used across setup dialogs.
 */
public class CanvasUtils {

    private CanvasUtils() {} // Prevent instantiation

    /**
     * Draw a rounded panel with the standard background color.
     */
    public static void drawPanel(GraphicsContext gc, double x, double y, double w, double h) {
        drawPanel(gc, x, y, w, h, DialogConstants.PANEL_RADIUS);
    }

    /**
     * Draw a rounded panel with custom corner radius.
     */
    public static void drawPanel(GraphicsContext gc, double x, double y, double w, double h, double radius) {
        gc.setFill(Color.web(DialogConstants.PANEL_BACKGROUND));
        gc.fillRoundRect(x, y, w, h, radius, radius);
    }

    /**
     * Draw a primary action button (green).
     */
    public static void drawPrimaryButton(GraphicsContext gc, double x, double y, double w, double h,
                                         String text, boolean hover) {
        drawButton(gc, x, y, w, h, text, hover, DialogConstants.BUTTON_PRIMARY);
    }

    /**
     * Draw a secondary action button (gray).
     */
    public static void drawSecondaryButton(GraphicsContext gc, double x, double y, double w, double h,
                                           String text, boolean hover) {
        drawButton(gc, x, y, w, h, text, hover, DialogConstants.BUTTON_SECONDARY);
    }

    /**
     * Draw a button with custom color.
     */
    public static void drawButton(GraphicsContext gc, double x, double y, double w, double h,
                                  String text, boolean hover, String colorHex) {
        Color baseColor = Color.web(colorHex);
        Color bgColor = hover ? baseColor.brighter() : baseColor;
        gc.setFill(bgColor);
        gc.fillRoundRect(x, y, w, h, DialogConstants.BUTTON_RADIUS, DialogConstants.BUTTON_RADIUS);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        double textWidth = getTextWidth(gc, text);
        gc.fillText(text, x + (w - textWidth) / 2, y + h / 2 + 5);
    }

    /**
     * Check if a point is within a rectangle.
     */
    public static boolean isInRect(double px, double py, double x, double y, double w, double h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    /**
     * Get the display width of text using the current font.
     */
    public static double getTextWidth(GraphicsContext gc, String text) {
        javafx.scene.text.Text tempText = new javafx.scene.text.Text(text);
        tempText.setFont(gc.getFont());
        return tempText.getLayoutBounds().getWidth();
    }

    /**
     * Draw text centered horizontally within a given width.
     */
    public static void drawCenteredText(GraphicsContext gc, String text, double centerX, double y) {
        double textWidth = getTextWidth(gc, text);
        gc.fillText(text, centerX - textWidth / 2, y);
    }

    /**
     * Draw word-wrapped text within a maximum width.
     */
    public static void drawWrappedText(GraphicsContext gc, String text, double x, double y,
                                       double maxWidth, double lineHeight) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        double lineY = y;

        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            double testWidth = getTextWidth(gc, testLine);

            if (testWidth > maxWidth && line.length() > 0) {
                gc.fillText(line.toString(), x, lineY);
                line = new StringBuilder(word);
                lineY += lineHeight;
            } else {
                line = new StringBuilder(testLine);
            }
        }
        if (line.length() > 0) {
            gc.fillText(line.toString(), x, lineY);
        }
    }

    /**
     * Clear the canvas with the standard dark background.
     */
    public static void clearCanvas(GraphicsContext gc, double width, double height) {
        gc.setFill(Color.web(DialogConstants.BACKGROUND_DARK));
        gc.fillRect(0, 0, width, height);
    }
}
