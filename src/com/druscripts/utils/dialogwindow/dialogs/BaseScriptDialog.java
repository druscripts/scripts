package com.druscripts.utils.dialogwindow.dialogs;

import com.druscripts.utils.dialogwindow.CanvasUtils;
import com.druscripts.utils.dialogwindow.Theme;
import com.druscripts.utils.dialogwindow.components.Hyperlink;
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

    // Layout constants - use Theme for consistency
    protected static final double PANEL_PADDING = Theme.PADDING_STANDARD;
    protected static final double PANEL_SPACING = Theme.PADDING_STANDARD;
    protected static final double PANEL_CORNER_RADIUS = Theme.PANEL_RADIUS;

    // Left column layout (fixed)
    protected static final double LEFT_COLUMN_X = PANEL_PADDING;
    protected static final double LEFT_COLUMN_Y = PANEL_PADDING;
    protected static final double LEFT_COLUMN_WIDTH = 280;

    // Header panel
    protected static final double HEADER_PANEL_HEIGHT = 80;
    protected static final double HEADER_PADDING_TOP = 25;
    protected static final double HEADER_TITLE_Y = HEADER_PADDING_TOP + 10;
    protected static final double HEADER_AUTHOR_Y = HEADER_TITLE_Y + 25;

    // Info panel
    protected static final double INFO_PANEL_HEIGHT = 250;
    protected static final double INFO_PANEL_Y = LEFT_COLUMN_Y + HEADER_PANEL_HEIGHT + PANEL_SPACING;

    // Button constants
    protected static final double BUTTON_WIDTH = Theme.BUTTON_WIDTH;
    protected static final double BUTTON_HEIGHT = Theme.BUTTON_HEIGHT;
    protected static final double BUTTON_CORNER_RADIUS = Theme.BUTTON_RADIUS;
    protected static final double BUTTON_Y_OFFSET = INFO_PANEL_HEIGHT - BUTTON_HEIGHT - PANEL_PADDING;

    // Right column layout
    protected static final double RIGHT_COLUMN_X = LEFT_COLUMN_X + LEFT_COLUMN_WIDTH + PANEL_SPACING;
    protected static final double RIGHT_COLUMN_Y = PANEL_PADDING;

    protected final Script script;
    protected final String scriptName;
    protected final String scriptVersion;
    protected final String websiteLinkText;

    // Hyperlink component for website
    private Hyperlink websiteLink;

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
     * @param name The script name from @ScriptDefinition (e.g., "DyeMaker.druscripts.com")
     * @param version The script version to display
     * @param rightColumnWidth Width of the right configuration column
     * @param rightColumnHeight Height of the right configuration column
     */
    protected BaseScriptDialog(Script script, String name, String version, double rightColumnWidth, double rightColumnHeight) {
        this.script = script;
        this.scriptName = name.split("\\.")[0];  // Extract display name before first dot
        this.scriptVersion = "v" + version;
        String websiteUrl = name.toLowerCase();
        this.websiteLinkText = "<a href=\"https://" + websiteUrl + "\">" + websiteUrl + "</a>";
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
        root.setStyle("-fx-background-color: " + Theme.BACKGROUND_DARK + ";");

        Scene scene = new Scene(root, canvasWidth, canvasHeight);
        scene.setFill(Color.web(Theme.BACKGROUND_DARK));
        return scene;
    }

    private void render(GraphicsContext gc) {
        // Clear canvas
        gc.setFill(Color.web(Theme.BACKGROUND_DARK));
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

        // Title
        gc.setFill(Color.web(Theme.BRAND_PRIMARY));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.fillText(scriptName, x + PANEL_PADDING, y + HEADER_TITLE_Y);
        double titleWidth = getTextWidth(gc, scriptName);

        // Version (same line, after title)
        gc.setFill(Color.web(Theme.BRAND_SUBTLE));
        gc.setFont(Font.font("Arial", 12));
        gc.fillText(scriptVersion, x + PANEL_PADDING + titleWidth + 8, y + HEADER_TITLE_Y);

        // Website link
        gc.setFont(Font.font("Arial", 10));
        websiteLink = new Hyperlink(
            x + PANEL_PADDING, y + HEADER_AUTHOR_Y,
            websiteLinkText,
            LEFT_COLUMN_WIDTH - PANEL_PADDING * 2,
            Theme.BRAND_TERTIARY,
            mouseX, mouseY
        );
        websiteLink.render(gc);

        // Info panel
        drawPanel(gc, x, INFO_PANEL_Y, LEFT_COLUMN_WIDTH, INFO_PANEL_HEIGHT);

        gc.setFill(Color.web(Theme.TEXT_MUTED));
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
        gc.setFill(Color.web(Theme.BRAND_PRIMARY));
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
        if (websiteLink != null && websiteLink.isClicked(clickX, clickY)) {
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

    // === Utility methods for subclasses (delegate to CanvasUtils) ===

    protected void drawPanel(GraphicsContext gc, double x, double y, double w, double h) {
        CanvasUtils.drawPanel(gc, x, y, w, h, PANEL_CORNER_RADIUS);
    }

    protected void drawButton(GraphicsContext gc, double x, double y, double w, double h, String text, boolean hover) {
        CanvasUtils.drawButton(gc, x, y, w, h, text, hover, Theme.BUTTON_PRIMARY);
    }

    protected boolean isHovering(double x, double y, double w, double h) {
        return CanvasUtils.isInRect(mouseX, mouseY, x, y, w, h);
    }

    protected boolean isInRect(double px, double py, double x, double y, double w, double h) {
        return CanvasUtils.isInRect(px, py, x, y, w, h);
    }

    protected void wrapText(GraphicsContext gc, String text, double x, double y, double maxWidth) {
        CanvasUtils.drawWrappedText(gc, text, x, y, maxWidth, 16);
    }

    protected double getTextWidth(GraphicsContext gc, String text) {
        return CanvasUtils.getTextWidth(gc, text);
    }

    /**
     * Check if the dialog was dismissed by clicking Start.
     */
    public boolean isDismissed() {
        return dismissed;
    }
}
