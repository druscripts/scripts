package com.druscripts.utils.dialogwindow.dialogs;

import com.druscripts.utils.dialogwindow.Theme;
import com.druscripts.utils.dialogwindow.components.Hyperlink;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Reusable error dialog component with consistent styling.
 * Uses Canvas-based immediate mode rendering to match other script UIs.
 * Supports clickable hyperlinks in message text using HTML anchor tags.
 */
public class ErrorDialog {

    private static final double CANVAS_WIDTH = 500;
    private static final double CANVAS_HEIGHT = 300;
    private static final double PANEL_PADDING = Theme.PADDING_STANDARD;
    private static final double PANEL_CORNER_RADIUS = Theme.PANEL_RADIUS;
    private static final double BUTTON_WIDTH = 120;
    private static final double BUTTON_HEIGHT = 40;
    private static final double BUTTON_CORNER_RADIUS = Theme.BUTTON_RADIUS;

    /**
     * Creates a standardized error dialog scene using Canvas.
     *
     * @param title The error title (e.g., "Invalid Location")
     * @param message The error message to display (supports HTML anchor tags for links)
     * @param onClose Callback to execute when user clicks OK (typically script.stop())
     * @return Scene ready to be shown via StageController
     */
    public static Scene createErrorScene(String title, String message, Runnable onClose) {
        return createErrorScene(null, title, message, onClose);
    }

    /**
     * Creates a standardized error dialog scene using Canvas with script header.
     *
     * @param scriptName The script name to display in header (e.g., "Rogues Den")
     * @param title The error title (e.g., "Invalid Location")
     * @param message The error message to display (supports HTML anchor tags for links)
     * @param onClose Callback to execute when user clicks OK (typically script.stop())
     * @return Scene ready to be shown via StageController
     */
    public static Scene createErrorScene(String scriptName, String title, String message, Runnable onClose) {
        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Mouse state
        final double[] mouseX = {0};
        final double[] mouseY = {0};
        final boolean[] dismissed = {false};

        // Hyperlink components (recreated each frame with current mouse position)
        final Hyperlink[] websiteLink = {null};
        final Hyperlink[] messageLink = {null};

        // Mouse tracking
        canvas.setOnMouseMoved(e -> {
            mouseX[0] = e.getX();
            mouseY[0] = e.getY();
        });

        // Click handler
        canvas.setOnMouseClicked(e -> {
            double clickX = e.getX();
            double clickY = e.getY();

            // Check hyperlinks
            if (websiteLink[0] != null && websiteLink[0].isClicked(clickX, clickY)) {
                return;
            }
            if (messageLink[0] != null && messageLink[0].isClicked(clickX, clickY)) {
                return;
            }

            // Calculate button position
            double buttonX = (CANVAS_WIDTH - BUTTON_WIDTH) / 2;
            double buttonY = CANVAS_HEIGHT - PANEL_PADDING - BUTTON_HEIGHT - 20;

            if (clickX >= buttonX && clickX <= buttonX + BUTTON_WIDTH &&
                clickY >= buttonY && clickY <= buttonY + BUTTON_HEIGHT) {
                dismissed[0] = true;
                javafx.application.Platform.runLater(() -> {
                    javafx.stage.Window window = canvas.getScene().getWindow();
                    if (window != null) {
                        window.hide();
                    }
                    if (onClose != null) {
                        onClose.run();
                    }
                });
            }
        });

        // Animation loop
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (dismissed[0]) {
                    stop();
                    return;
                }
                render(gc, scriptName, title, message, mouseX[0], mouseY[0], websiteLink, messageLink);
            }
        };
        timer.start();

        StackPane root = new StackPane(canvas);
        root.setStyle("-fx-background-color: " + Theme.BACKGROUND_DARK + ";");

        Scene scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT);
        scene.setFill(Color.web(Theme.BACKGROUND_DARK));
        return scene;
    }

    private static void render(GraphicsContext gc, String scriptName, String title, String message,
                               double mouseX, double mouseY, Hyperlink[] websiteLink, Hyperlink[] messageLink) {
        // Clear canvas
        gc.setFill(Color.web(Theme.BACKGROUND_DARK));
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Draw panel
        double panelX = PANEL_PADDING;
        double panelY = PANEL_PADDING;
        double panelWidth = CANVAS_WIDTH - (PANEL_PADDING * 2);
        double panelHeight = CANVAS_HEIGHT - (PANEL_PADDING * 2);

        gc.setFill(Color.web(Theme.PANEL_BACKGROUND));
        gc.fillRoundRect(panelX, panelY, panelWidth, panelHeight, PANEL_CORNER_RADIUS, PANEL_CORNER_RADIUS);

        double currentY = panelY + 30;

        // Draw script header if provided
        if (scriptName != null && !scriptName.isEmpty()) {
            // Script name
            gc.setFill(Color.web(Theme.BRAND_PRIMARY));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            double scriptNameWidth = getTextWidth(gc, scriptName);
            gc.fillText(scriptName, (CANVAS_WIDTH - scriptNameWidth) / 2, currentY);
            currentY += 22;

            // Website link
            gc.setFont(Font.font("Arial", 12));
            String websiteLinkText = "<a href=\"https://druscripts.com\">druscripts.com</a>";
            double websiteWidth = getTextWidth(gc, "druscripts.com");
            double websiteX = (CANVAS_WIDTH - websiteWidth) / 2;

            websiteLink[0] = new Hyperlink(websiteX, currentY, websiteLinkText, websiteWidth + 20, Theme.BRAND_PRIMARY, mouseX, mouseY);
            websiteLink[0].render(gc);

            currentY += 40;
        } else {
            websiteLink[0] = null;
        }

        // Draw title with warning icon (centered)
        gc.setFill(Color.web(Theme.STATE_ERROR));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        String titleText = "⚠ " + title;
        double titleWidth = getTextWidth(gc, titleText);
        gc.fillText(titleText, (CANVAS_WIDTH - titleWidth) / 2, currentY);
        currentY += 30;

        // Draw message with hyperlink support
        gc.setFont(Font.font("Arial", Theme.FONT_SIZE_BODY));
        messageLink[0] = new Hyperlink(panelX + 30, currentY, message, panelWidth - 60, Theme.TEXT_PRIMARY, mouseX, mouseY);
        messageLink[0].render(gc);

        // Draw OK button
        double buttonX = (CANVAS_WIDTH - BUTTON_WIDTH) / 2;
        double buttonY = CANVAS_HEIGHT - PANEL_PADDING - BUTTON_HEIGHT - 20;
        boolean hover = mouseX >= buttonX && mouseX <= buttonX + BUTTON_WIDTH &&
                       mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT;

        Color buttonColor = Color.web(Theme.BUTTON_SECONDARY);
        gc.setFill(hover ? buttonColor.brighter() : buttonColor);
        gc.fillRoundRect(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, Theme.FONT_SIZE_BODY));
        String buttonText = "OK";
        double buttonTextWidth = getTextWidth(gc, buttonText);
        gc.fillText(buttonText, buttonX + (BUTTON_WIDTH - buttonTextWidth) / 2, buttonY + BUTTON_HEIGHT / 2 + 5);
    }

    private static double getTextWidth(GraphicsContext gc, String text) {
        javafx.scene.text.Text textNode = new javafx.scene.text.Text(text);
        textNode.setFont(gc.getFont());
        return textNode.getLayoutBounds().getWidth();
    }
}
