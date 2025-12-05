package com.druscripts.utils.dialogwindow.components;

import com.druscripts.utils.dialogwindow.Theme;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Immediate-mode radio button component for Canvas UIs.
 * Renders a radio button with text label and returns click state.
 */
public class RadioButton {

    private static final double RADIO_SIZE = 10;
    private static final double TEXT_OFFSET = RADIO_SIZE + 6;
    private static final double WIDTH = 160;
    private static final double HEIGHT = RADIO_SIZE + 4;

    /**
     * Render a radio button and check for clicks (immediate mode style).
     *
     * @param gc Graphics context
     * @param x X position (left edge)
     * @param y Y position (baseline of text)
     * @param text Label text
     * @param selected Whether this radio is currently selected
     * @param mouseX Current mouse X for hover effect
     * @param mouseY Current mouse Y for hover effect
     * @param clickX Pending click X (-1 if no click)
     * @param clickY Pending click Y (-1 if no click)
     * @return true if this radio button was clicked
     */
    public static boolean render(GraphicsContext gc, double x, double y, String text,
                                  boolean selected, double mouseX, double mouseY,
                                  double clickX, double clickY) {
        // Check hover
        boolean hover = isInBounds(mouseX, mouseY, x, y);

        // Draw circle
        double circleCenterY = y - 4;
        double circleTopY = circleCenterY - RADIO_SIZE / 2;

        gc.setStroke(selected ? Color.web(Theme.BRAND_PRIMARY) : Color.web(Theme.TEXT_SECONDARY));
        gc.setLineWidth(1.5);
        gc.strokeOval(x, circleTopY, RADIO_SIZE, RADIO_SIZE);

        if (selected) {
            gc.setFill(Color.web(Theme.BRAND_PRIMARY));
            double innerSize = RADIO_SIZE - 4;
            gc.fillOval(x + 2, circleTopY + 2, innerSize, innerSize);
        }

        // Draw label
        gc.setFill(hover ? Color.web(Theme.TEXT_PRIMARY) : Color.web(Theme.TEXT_SECONDARY));
        gc.setFont(Font.font("Arial", 12));
        gc.fillText(text, x + TEXT_OFFSET, y);

        // Check click
        return clickX >= 0 && isInBounds(clickX, clickY, x, y);
    }

    private static boolean isInBounds(double px, double py, double x, double y) {
        double hitboxY = y - RADIO_SIZE;
        return px >= x && px <= x + WIDTH &&
               py >= hitboxY && py <= hitboxY + HEIGHT;
    }

    public static double getWidth() {
        return WIDTH;
    }

    public static double getHeight() {
        return HEIGHT;
    }
}
