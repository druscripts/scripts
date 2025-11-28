package com.druscripts.dyemaker.ui;

import com.druscripts.dyemaker.Constants;
import com.druscripts.utils.dialogwindow.CanvasUtils;
import com.druscripts.utils.dialogwindow.DialogConstants;
import com.druscripts.utils.dialogwindow.components.CanvasRadioButton;
import com.osmb.api.script.Script;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Immediate mode Canvas-based UI for DyeMaker configuration.
 * Matches the styling of other DruScripts dialogs.
 */
public class DyeMakerUI {

    // Canvas dimensions
    private static final double CANVAS_WIDTH = 400;
    private static final double CANVAS_HEIGHT = 350;

    // Layout constants
    private static final double PANEL_PADDING = DialogConstants.PADDING_STANDARD;

    // Button constants
    private static final double BUTTON_WIDTH = 200;
    private static final double BUTTON_HEIGHT = DialogConstants.BUTTON_HEIGHT;

    private final Script script;

    // State
    private Constants.DyeType selectedDyeType = Constants.DyeType.RED;

    // Mouse state
    private double mouseX = 0;
    private double mouseY = 0;

    // UI dismissed flag
    private volatile boolean dismissed = false;

    public DyeMakerUI(Script script) {
        this.script = script;
    }

    public Scene buildScene() {
        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Mouse tracking
        canvas.setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });

        canvas.setOnMouseClicked(this::handleClick);

        // Animation loop - redraw everything each frame (immediate mode)
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (dismissed) {
                    stop();
                    return;
                }
                render(gc);
            }
        };
        timer.start();

        StackPane root = new StackPane(canvas);
        root.setStyle("-fx-background-color: " + DialogConstants.BACKGROUND_DARK + ";");

        Scene scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT);
        scene.setFill(Color.web(DialogConstants.BACKGROUND_DARK));
        return scene;
    }

    private void render(GraphicsContext gc) {
        CanvasUtils.clearCanvas(gc, CANVAS_WIDTH, CANVAS_HEIGHT);

        double x = PANEL_PADDING;
        double y = PANEL_PADDING;
        double panelWidth = CANVAS_WIDTH - (PANEL_PADDING * 2);
        double panelHeight = CANVAS_HEIGHT - (PANEL_PADDING * 2);

        CanvasUtils.drawPanel(gc, x, y, panelWidth, panelHeight);

        // Header
        double contentY = y + 30;

        gc.setFill(Color.web(DialogConstants.BRAND_PRIMARY));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        CanvasUtils.drawCenteredText(gc, "DyeMaker", CANVAS_WIDTH / 2, contentY);

        contentY += 18;
        gc.setFill(Color.web(DialogConstants.BRAND_SUBTLE));
        gc.setFont(Font.font("Arial", 11));
        CanvasUtils.drawCenteredText(gc, "druscripts.com", CANVAS_WIDTH / 2, contentY);

        contentY += 40;

        // Dye Type Selection
        gc.setFill(Color.web(DialogConstants.TEXT_PRIMARY));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText("Select Dye Type:", x + PANEL_PADDING, contentY);

        contentY += 30;

        // Render radio buttons for each dye type
        for (Constants.DyeType dye : Constants.DyeType.values()) {
            new CanvasRadioButton(
                x + PANEL_PADDING,
                contentY,
                dye.getDisplayName(),
                selectedDyeType == dye,
                mouseX,
                mouseY
            ).render(gc);
            contentY += 35;
        }

        contentY += 10;

        // Selected dye info
        gc.setFill(Color.web(DialogConstants.TEXT_MUTED));
        gc.setFont(Font.font("Arial", 11));
        String info = "Requires: " + selectedDyeType.getIngredientCount() + "x " +
                      selectedDyeType.getIngredientName() + " + 5 coins per dye";
        gc.fillText(info, x + PANEL_PADDING, contentY);

        // Start button
        double buttonX = (CANVAS_WIDTH - BUTTON_WIDTH) / 2;
        double buttonY = y + panelHeight - PANEL_PADDING - BUTTON_HEIGHT;
        boolean startHover = CanvasUtils.isInRect(mouseX, mouseY, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        CanvasUtils.drawPrimaryButton(gc, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, "Start Script", startHover);
    }

    private void handleClick(MouseEvent e) {
        double clickX = e.getX();
        double clickY = e.getY();

        double x = PANEL_PADDING;
        double y = PANEL_PADDING;
        double panelWidth = CANVAS_WIDTH - (PANEL_PADDING * 2);
        double panelHeight = CANVAS_HEIGHT - (PANEL_PADDING * 2);

        // Start button
        double buttonX = (CANVAS_WIDTH - BUTTON_WIDTH) / 2;
        double buttonY = y + panelHeight - PANEL_PADDING - BUTTON_HEIGHT;
        if (CanvasUtils.isInRect(clickX, clickY, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            dismissed = true;
            javafx.application.Platform.runLater(() -> {
                javafx.stage.Window window = ((Canvas) e.getSource()).getScene().getWindow();
                if (window != null) {
                    window.hide();
                }
            });
            return;
        }

        // Dye type radio buttons - must match render positions
        double contentY = y + 30 + 18 + 40 + 30;  // After header and label

        for (Constants.DyeType dye : Constants.DyeType.values()) {
            CanvasRadioButton radio = new CanvasRadioButton(
                x + PANEL_PADDING,
                contentY,
                dye.getDisplayName(),
                selectedDyeType == dye,
                mouseX,
                mouseY
            );
            if (radio.isClicked(clickX, clickY)) {
                selectedDyeType = dye;
                return;
            }
            contentY += 35;
        }
    }

    /**
     * Get the selected dye type. Returns null if user closed without confirming.
     */
    public Constants.DyeType getSelectedDyeType() {
        return dismissed ? selectedDyeType : null;
    }
}
