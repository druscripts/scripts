package com.druscripts.utils.dialogwindow.components;

import com.druscripts.utils.dialogwindow.Theme;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Immediate-mode checkbox component for Canvas UIs.
 * Renders a checkbox with text label and returns click state.
 */
public class Checkbox {

    private static final double BOX_SIZE = 12;
    private static final double TEXT_OFFSET = BOX_SIZE + 8;
    private static final double WIDTH = 200;
    private static final double HEIGHT = BOX_SIZE + 4;

    /**
     * Render a checkbox and check for clicks (immediate mode style).
     *
     * @param gc Graphics context
     * @param x X position (left edge)
     * @param y Y position (baseline of text)
     * @param text Label text
     * @param checked Whether this checkbox is currently checked
     * @param mouseX Current mouse X for hover effect
     * @param mouseY Current mouse Y for hover effect
     * @param clickX Pending click X (-1 if no click)
     * @param clickY Pending click Y (-1 if no click)
     * @return true if this checkbox was clicked
     */
    public static boolean render(GraphicsContext gc, double x, double y, String text,
                                  boolean checked, double mouseX, double mouseY,
                                  double clickX, double clickY) {
        // Check hover
        boolean hover = isInBounds(mouseX, mouseY, x, y);

        // Draw box
        double boxCenterY = y - 4;
        double boxTopY = boxCenterY - BOX_SIZE / 2;

        gc.setStroke(checked ? Color.web(Theme.BRAND_PRIMARY) : Color.web(Theme.TEXT_SECONDARY));
        gc.setLineWidth(1.5);
        gc.strokeRect(x, boxTopY, BOX_SIZE, BOX_SIZE);

        if (checked) {
            // Draw checkmark
            gc.setStroke(Color.web(Theme.BRAND_PRIMARY));
            gc.setLineWidth(2);
            double padding = 3;
            gc.strokeLine(x + padding, boxTopY + BOX_SIZE / 2,
                         x + BOX_SIZE / 2, boxTopY + BOX_SIZE - padding);
            gc.strokeLine(x + BOX_SIZE / 2, boxTopY + BOX_SIZE - padding,
                         x + BOX_SIZE - padding, boxTopY + padding);
        }

        // Draw label
        gc.setFill(hover ? Color.web(Theme.TEXT_PRIMARY) : Color.web(Theme.TEXT_SECONDARY));
        gc.setFont(Font.font("Arial", 12));
        gc.fillText(text, x + TEXT_OFFSET, y);

        // Check click
        return clickX >= 0 && isInBounds(clickX, clickY, x, y);
    }

    private static boolean isInBounds(double px, double py, double x, double y) {
        double hitboxY = y - BOX_SIZE;
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
