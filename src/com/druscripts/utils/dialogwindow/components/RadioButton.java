package com.druscripts.utils.dialogwindow.components;

import com.druscripts.utils.dialogwindow.Theme;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Reusable radio button component for immediate mode Canvas UIs.
 * Renders a radio button with text label.
 */
public class RadioButton {

    private static final double RADIO_SIZE = 10;  // Smaller than 12px font
    private static final double TEXT_OFFSET = RADIO_SIZE + 6;  // Space between radio and text
    private static final double WIDTH = 160;  // Total clickable width

    private final double x;
    private final double y;
    private final String text;
    private final boolean selected;
    private final boolean hover;

    /**
     * Create a radio button.
     *
     * @param x X position (left edge)
     * @param y Y position (baseline of text)
     * @param text Label text
     * @param selected Whether this radio is selected
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     */
    public RadioButton(double x, double y, String text, boolean selected, double mouseX, double mouseY) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.selected = selected;
        this.hover = isHovering(mouseX, mouseY);
    }

    /**
     * Render the radio button.
     */
    public void render(GraphicsContext gc) {
        // Calculate circle center - vertically centered with text
        double circleCenterY = y - 4;  // Move up from baseline to center with lowercase letters
        double circleTopY = circleCenterY - RADIO_SIZE / 2;

        // Outer circle
        gc.setStroke(selected ? Color.web(Theme.BRAND_PRIMARY) : Color.web(Theme.TEXT_SECONDARY));
        gc.setLineWidth(1.5);
        gc.strokeOval(x, circleTopY, RADIO_SIZE, RADIO_SIZE);

        // Inner circle (if selected)
        if (selected) {
            gc.setFill(Color.web(Theme.BRAND_PRIMARY));
            double innerSize = RADIO_SIZE - 4;
            gc.fillOval(x + 2, circleTopY + 2, innerSize, innerSize);
        }

        // Label
        gc.setFill(hover ? Color.web(Theme.TEXT_PRIMARY) : Color.web(Theme.TEXT_SECONDARY));
        gc.setFont(Font.font("Arial", 12));
        gc.fillText(text, x + TEXT_OFFSET, y);
    }

    /**
     * Check if the mouse is over this radio button.
     */
    private boolean isHovering(double mouseX, double mouseY) {
        // Hitbox: from top of circle to bottom of text baseline
        double hitboxY = y - RADIO_SIZE;
        double hitboxHeight = RADIO_SIZE + 4;  // Circle + text descent
        return mouseX >= x && mouseX <= x + WIDTH &&
               mouseY >= hitboxY && mouseY <= hitboxY + hitboxHeight;
    }

    /**
     * Check if a click at the given coordinates hits this radio button.
     */
    public boolean isClicked(double clickX, double clickY) {
        double hitboxY = y - RADIO_SIZE;
        double hitboxHeight = RADIO_SIZE + 4;
        return clickX >= x && clickX <= x + WIDTH &&
               clickY >= hitboxY && clickY <= hitboxY + hitboxHeight;
    }

    /**
     * Get the width of this radio button for layout purposes.
     */
    public static double getWidth() {
        return WIDTH;
    }

    /**
     * Get the height of this radio button for layout purposes.
     */
    public static double getHeight() {
        return RADIO_SIZE + 4;
    }
}
