package com.druscripts.utils.dialogwindow;

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
 * Base class for script configuration dialogs.
 * Provides a standard two-column layout:
 * - Left column: Header (title, version, website link) + Info panel with Start button
 * - Right column: Configuration options (implemented by subclass)
 */
public abstract class BaseScriptDialog {

    // Layout constants - use DialogConstants for consistency
    protected static final double PANEL_PADDING = DialogConstants.PADDING_STANDARD;
    protected static final double PANEL_SPACING = DialogConstants.PADDING_STANDARD;
    protected static final double PANEL_CORNER_RADIUS = DialogConstants.PANEL_RADIUS;

    // Left column layout (fixed)
    protected static final double LEFT_COLUMN_X = PANEL_PADDING;
    protected static final double LEFT_COLUMN_Y = PANEL_PADDING;
    protected static final double LEFT_COLUMN_WIDTH = 280;

    // Header panel
    protected static final double HEADER_PANEL_HEIGHT = 100;
    protected static final double HEADER_PADDING_TOP = 25;
    protected static final double HEADER_TITLE_Y = HEADER_PADDING_TOP + 10;
    protected static final double HEADER_VERSION_Y = HEADER_TITLE_Y + 25;
    protected static final double HEADER_AUTHOR_Y = HEADER_VERSION_Y + 20;

    // Info panel
    protected static final double INFO_PANEL_HEIGHT = 250;
    protected static final double INFO_PANEL_Y = LEFT_COLUMN_Y + HEADER_PANEL_HEIGHT + PANEL_SPACING;

    // Button constants
    protected static final double BUTTON_WIDTH = DialogConstants.BUTTON_WIDTH;
    protected static final double BUTTON_HEIGHT = DialogConstants.BUTTON_HEIGHT;
    protected static final double BUTTON_CORNER_RADIUS = DialogConstants.BUTTON_RADIUS;
    protected static final double BUTTON_Y_OFFSET = INFO_PANEL_HEIGHT - BUTTON_HEIGHT - PANEL_PADDING;

    // Right column layout
    protected static final double RIGHT_COLUMN_X = LEFT_COLUMN_X + LEFT_COLUMN_WIDTH + PANEL_SPACING;
    protected static final double RIGHT_COLUMN_Y = PANEL_PADDING;

    // Website link
    protected static final String WEBSITE_URL = "https://druscripts.com";
    protected static final String WEBSITE_DISPLAY = "druscripts.com";

    protected final Script script;

    // Mouse state
    protected double mouseX = 0;
    protected double mouseY = 0;

    // UI dismissed flag
    protected volatile boolean dismissed = false;

    // Canvas dimensions (set by subclass)
    protected final double canvasWidth;
    protected final double canvasHeight;
    protected final double rightColumnWidth;
    protected final double rightColumnHeight;

    /**
     * Create a new script dialog.
     *
     * @param script The script instance
     * @param rightColumnWidth Width of the right configuration column
     * @param rightColumnHeight Height of the right configuration column
     */
    protected BaseScriptDialog(Script script, double rightColumnWidth, double rightColumnHeight) {
        this.script = script;
        this.rightColumnWidth = rightColumnWidth;
        this.rightColumnHeight = rightColumnHeight;

        // Calculate canvas dimensions
        this.canvasWidth = RIGHT_COLUMN_X + rightColumnWidth + PANEL_PADDING;
        this.canvasHeight = Math.max(
            INFO_PANEL_Y + INFO_PANEL_HEIGHT + PANEL_PADDING,
            RIGHT_COLUMN_Y + rightColumnHeight + PANEL_PADDING
        );
    }

    /**
     * Get the script title to display in the header.
     */
    protected abstract String getScriptTitle();

    /**
     * Get the script version to display in the header.
     */
    protected abstract String getScriptVersion();

    /**
     * Get the description text to display in the info panel.
     */
    protected abstract String getDescription();

    /**
     * Render the right column configuration content.
     * @param gc The graphics context
     * @param x The x position of the right column content area (inside padding)
     * @param y The y position of the right column content area (inside padding)
     * @param width The available width for content
     */
    protected abstract void renderRightColumnContent(GraphicsContext gc, double x, double y, double width);

    /**
     * Handle clicks in the right column.
     * @param clickX The x position of the click
     * @param clickY The y position of the click
     * @return true if the click was handled
     */
    protected abstract boolean handleRightColumnClick(double clickX, double clickY);

    /**
     * Called when the Start button is clicked, before dismissing the dialog.
     * Subclasses should save preferences here.
     */
    protected abstract void onStart();

    /**
     * Load preferences from storage.
     * Called during construction.
     */
    protected abstract void loadPreferences();

    public Scene buildScene() {
        Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Mouse tracking
        canvas.setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });

        canvas.setOnMouseClicked(this::handleClick);

        // Animation loop - redraw everything each frame
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

        Scene scene = new Scene(root, canvasWidth, canvasHeight);
        scene.setFill(Color.web(DialogConstants.BACKGROUND_DARK));
        return scene;
    }

    private void render(GraphicsContext gc) {
        // Clear canvas
        gc.setFill(Color.web(DialogConstants.BACKGROUND_DARK));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // Left column
        renderLeftColumn(gc);

        // Right column
        renderRightColumn(gc);
    }

    private void renderLeftColumn(GraphicsContext gc) {
        double x = LEFT_COLUMN_X;
        double y = LEFT_COLUMN_Y;

        // Header panel
        drawPanel(gc, x, y, LEFT_COLUMN_WIDTH, HEADER_PANEL_HEIGHT);

        gc.setFill(Color.web(DialogConstants.BRAND_PRIMARY));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.fillText(getScriptTitle(), x + PANEL_PADDING, y + HEADER_TITLE_Y);

        gc.setFill(Color.web(DialogConstants.BRAND_SUBTLE));
        gc.setFont(Font.font("Arial", 12));
        gc.fillText(getScriptVersion(), x + PANEL_PADDING, y + HEADER_VERSION_Y);

        // Website link (with hover effect)
        boolean linkHover = isHoveringLink();
        gc.setFill(linkHover ? Color.web(DialogConstants.BRAND_PRIMARY) : Color.web(DialogConstants.BRAND_TERTIARY));
        gc.setFont(Font.font("Arial", 10));
        gc.fillText(WEBSITE_DISPLAY, x + PANEL_PADDING, y + HEADER_AUTHOR_Y);

        // Info panel
        drawPanel(gc, x, INFO_PANEL_Y, LEFT_COLUMN_WIDTH, INFO_PANEL_HEIGHT);

        gc.setFill(Color.web(DialogConstants.TEXT_MUTED));
        gc.setFont(Font.font("Arial", 11));
        wrapText(gc, getDescription(), x + PANEL_PADDING, INFO_PANEL_Y + 30, BUTTON_WIDTH);

        // Start button
        double buttonX = x + PANEL_PADDING;
        double buttonY = INFO_PANEL_Y + BUTTON_Y_OFFSET;
        boolean startHover = isHovering(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        drawButton(gc, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, "Start Script", startHover);
    }

    private void renderRightColumn(GraphicsContext gc) {
        drawPanel(gc, RIGHT_COLUMN_X, RIGHT_COLUMN_Y, rightColumnWidth, rightColumnHeight);

        // Config header
        gc.setFill(Color.web(DialogConstants.BRAND_PRIMARY));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.fillText("Configuration", RIGHT_COLUMN_X + PANEL_PADDING, RIGHT_COLUMN_Y + 30);

        // Delegate content rendering to subclass
        renderRightColumnContent(gc,
            RIGHT_COLUMN_X + PANEL_PADDING,
            RIGHT_COLUMN_Y + 60,
            rightColumnWidth - (PANEL_PADDING * 2));
    }

    private void handleClick(MouseEvent e) {
        double clickX = e.getX();
        double clickY = e.getY();

        // Website link
        if (isHoveringLink()) {
            openWebsite();
            return;
        }

        // Start button
        double buttonX = LEFT_COLUMN_X + PANEL_PADDING;
        double buttonY = INFO_PANEL_Y + BUTTON_Y_OFFSET;
        if (isInRect(clickX, clickY, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            onStart();
            dismissed = true;
            javafx.application.Platform.runLater(() -> {
                javafx.stage.Window window = ((Canvas)e.getSource()).getScene().getWindow();
                if (window != null) {
                    window.hide();
                }
            });
            return;
        }

        // Right column clicks
        handleRightColumnClick(clickX, clickY);
    }

    private boolean isHoveringLink() {
        double linkX = LEFT_COLUMN_X + PANEL_PADDING;
        double linkY = LEFT_COLUMN_Y + HEADER_AUTHOR_Y - 10;
        double linkWidth = 80;
        double linkHeight = 14;
        return mouseX >= linkX && mouseX <= linkX + linkWidth &&
               mouseY >= linkY && mouseY <= linkY + linkHeight;
    }

    private void openWebsite() {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(WEBSITE_URL));
        } catch (Exception ex) {
            // Ignore - can't open browser
        }
    }

    // === Utility methods for subclasses ===

    protected void drawPanel(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(Color.web(DialogConstants.PANEL_BACKGROUND));
        gc.fillRoundRect(x, y, w, h, PANEL_CORNER_RADIUS, PANEL_CORNER_RADIUS);
    }

    protected void drawButton(GraphicsContext gc, double x, double y, double w, double h, String text, boolean hover) {
        Color baseColor = Color.web(DialogConstants.BUTTON_PRIMARY);
        Color bgColor = hover ? baseColor.brighter() : baseColor;
        gc.setFill(bgColor);
        gc.fillRoundRect(x, y, w, h, BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        double textWidth = getTextWidth(gc, text);
        gc.fillText(text, x + (w - textWidth) / 2, y + h / 2 + 5);
    }

    protected boolean isHovering(double x, double y, double w, double h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    protected boolean isInRect(double px, double py, double x, double y, double w, double h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    protected void wrapText(GraphicsContext gc, String text, double x, double y, double maxWidth) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        double lineY = y;

        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            double testWidth = getTextWidth(gc, testLine);

            if (testWidth > maxWidth && line.length() > 0) {
                gc.fillText(line.toString(), x, lineY);
                line = new StringBuilder(word);
                lineY += 16;
            } else {
                line = new StringBuilder(testLine);
            }
        }
        if (line.length() > 0) {
            gc.fillText(line.toString(), x, lineY);
        }
    }

    protected double getTextWidth(GraphicsContext gc, String text) {
        javafx.scene.text.Text tempText = new javafx.scene.text.Text(text);
        tempText.setFont(gc.getFont());
        return tempText.getLayoutBounds().getWidth();
    }

    /**
     * Check if the dialog was dismissed by clicking Start.
     */
    public boolean isDismissed() {
        return dismissed;
    }
}
