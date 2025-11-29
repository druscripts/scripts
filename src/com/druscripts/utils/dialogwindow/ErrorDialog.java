package com.druscripts.utils.dialogwindow;

import javafx.animation.AnimationTimer;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable error dialog component with consistent styling.
 * Uses Canvas-based immediate mode rendering to match other script UIs.
 * Supports clickable hyperlinks in message text using HTML anchor tags.
 * Both single and double quotes are supported: <a href="url">text</a> or <a href='url'>text</a>
 */
public class ErrorDialog {

    private static final double CANVAS_WIDTH = 500;
    private static final double CANVAS_HEIGHT = 300;
    private static final double PANEL_PADDING = DialogConstants.PADDING_STANDARD;
    private static final double PANEL_CORNER_RADIUS = DialogConstants.PANEL_RADIUS;
    private static final double BUTTON_WIDTH = 120;
    private static final double BUTTON_HEIGHT = 40;
    private static final double BUTTON_CORNER_RADIUS = DialogConstants.BUTTON_RADIUS;

    /**
     * Creates a standardized error dialog scene using Canvas.
     *
     * @param title The error title (e.g., "Invalid Location")
     * @param message The error message to display (supports <a href='url'>text</a> or <a>url</a> for links)
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
     * @param message The error message to display (supports <a href='url'>text</a> or <a>url</a> for links)
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
        final List<TextSegment> textSegments = new ArrayList<>();

        // Mouse tracking
        canvas.setOnMouseMoved(e -> {
            mouseX[0] = e.getX();
            mouseY[0] = e.getY();
        });

        // Click handler (handles both button and hyperlinks)
        canvas.setOnMouseClicked(e -> {
            double clickX = e.getX();
            double clickY = e.getY();

            // Check if a hyperlink was clicked
            if (HyperlinkRenderer.handleClick(textSegments, clickX, clickY)) {
                return; // Link was clicked, don't check button
            }

            // Calculate button position (same as in render)
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
                render(gc, scriptName, title, message, mouseX[0], mouseY[0], textSegments);
                updateCursor(canvas, textSegments, mouseX[0], mouseY[0]);
            }
        };
        timer.start();

        StackPane root = new StackPane(canvas);
        root.setStyle("-fx-background-color: " + DialogConstants.BACKGROUND_DARK + ";");

        Scene scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT);
        scene.setFill(Color.web(DialogConstants.BACKGROUND_DARK));
        return scene;
    }

    private static void render(GraphicsContext gc, String scriptName, String title, String message, double mouseX, double mouseY, List<TextSegment> textSegments) {
        // Clear canvas
        gc.setFill(Color.web(DialogConstants.BACKGROUND_DARK));
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Draw panel
        double panelX = PANEL_PADDING;
        double panelY = PANEL_PADDING;
        double panelWidth = CANVAS_WIDTH - (PANEL_PADDING * 2);
        double panelHeight = CANVAS_HEIGHT - (PANEL_PADDING * 2);

        gc.setFill(Color.web(DialogConstants.PANEL_BACKGROUND));
        gc.fillRoundRect(panelX, panelY, panelWidth, panelHeight, PANEL_CORNER_RADIUS, PANEL_CORNER_RADIUS);

        double currentY = panelY + 30;

        // Draw script header if provided and get website segment
        HeaderResult headerResult = renderScriptHeader(gc, scriptName, currentY, mouseX, mouseY);
        TextSegment websiteSegment = headerResult.websiteSegment;
        currentY = headerResult.nextY;

        // Draw title with warning icon (centered, bigger)
        gc.setFill(Color.web(DialogConstants.STATE_ERROR));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        String titleText = "⚠ " + title;
        double titleWidth = getTextWidth(gc, titleText);
        gc.fillText(titleText, (CANVAS_WIDTH - titleWidth) / 2, currentY);
        currentY += 30;

        // Draw message (word-wrapped with hyperlink support)
        gc.setFill(Color.web(DialogConstants.TEXT_PRIMARY));
        gc.setFont(Font.font("Arial", DialogConstants.FONT_SIZE_BODY));
        textSegments.clear();
        textSegments.addAll(HyperlinkRenderer.renderTextWithLinks(
            gc, message, panelX + 30, currentY, panelWidth - 60,
            DialogConstants.TEXT_PRIMARY, mouseX, mouseY
        ));

        // Add website segment after message links (so it doesn't get cleared)
        if (websiteSegment != null) {
            textSegments.add(websiteSegment);
        }

        // Draw OK button
        double buttonX = (CANVAS_WIDTH - BUTTON_WIDTH) / 2;
        double buttonY = CANVAS_HEIGHT - PANEL_PADDING - BUTTON_HEIGHT - 20;
        boolean hover = mouseX >= buttonX && mouseX <= buttonX + BUTTON_WIDTH &&
                       mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT;

        Color buttonColor = Color.web(DialogConstants.BUTTON_SECONDARY);
        gc.setFill(hover ? buttonColor.brighter() : buttonColor);
        gc.fillRoundRect(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, DialogConstants.FONT_SIZE_BODY));
        String buttonText = "OK";
        double buttonTextWidth = getTextWidth(gc, buttonText);
        gc.fillText(buttonText, buttonX + (BUTTON_WIDTH - buttonTextWidth) / 2, buttonY + BUTTON_HEIGHT / 2 + 5);

        // Render tooltips for all segments (including website link)
        renderTooltipsForSegments(gc, textSegments, mouseX, mouseY);
    }

    /**
     * Renders tooltips for text segments (extracted to avoid duplication with HyperlinkRenderer).
     */
    private static void renderTooltipsForSegments(GraphicsContext gc, List<TextSegment> segments, double mouseX, double mouseY) {
        for (TextSegment segment : segments) {
            if (segment.isLink() && segment.showTooltip() && segment.contains(mouseX, mouseY)) {
                String url = segment.getUrl();

                // Measure tooltip
                gc.setFont(Font.font("Arial", 11));
                javafx.scene.text.Text textNode = new javafx.scene.text.Text(url);
                textNode.setFont(gc.getFont());
                double tooltipTextWidth = textNode.getLayoutBounds().getWidth();
                double tooltipPadding = 8;
                double tooltipWidth = tooltipTextWidth + tooltipPadding * 2;
                double tooltipHeight = 24;

                // Position tooltip below and slightly right of cursor
                double tooltipX = mouseX + 10;
                double tooltipY = mouseY + 15;

                // Draw tooltip background
                gc.setFill(Color.web("#2b2b2b"));
                gc.fillRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 4, 4);

                // Draw tooltip border
                gc.setStroke(Color.web(DialogConstants.BRAND_PRIMARY));
                gc.setLineWidth(1);
                gc.strokeRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 4, 4);

                // Draw tooltip text
                gc.setFill(Color.web(DialogConstants.TEXT_PRIMARY));
                gc.fillText(url, tooltipX + tooltipPadding, tooltipY + tooltipHeight / 2 + 4);

                break; // Only show one tooltip at a time
            }
        }
    }

    /**
     * Renders the script header (script name + druscripts.com link).
     *
     * @param gc GraphicsContext to render on
     * @param scriptName The script name to display (can be null)
     * @param startY Starting Y position
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     * @return HeaderResult containing the website segment and next Y position
     */
    private static HeaderResult renderScriptHeader(GraphicsContext gc, String scriptName, double startY, double mouseX, double mouseY) {
        double currentY = startY;
        TextSegment websiteSegment = null;

        if (scriptName != null && !scriptName.isEmpty()) {
            // Script name
            gc.setFill(Color.web(DialogConstants.BRAND_PRIMARY));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            double scriptNameWidth = getTextWidth(gc, scriptName);
            gc.fillText(scriptName, (CANVAS_WIDTH - scriptNameWidth) / 2, currentY);
            currentY += 22;

            // druscripts.com as hyperlink
            gc.setFont(Font.font("Arial", 12));
            String website = "druscripts.com";
            double websiteWidth = getTextWidth(gc, website);
            double websiteX = (CANVAS_WIDTH - websiteWidth) / 2;

            // Check if hovering over website
            boolean websiteHover = mouseX >= websiteX && mouseX <= websiteX + websiteWidth &&
                                  mouseY >= currentY - 12 && mouseY <= currentY;

            gc.setFill(Color.web(websiteHover ? "#6bb8ff" : DialogConstants.BRAND_PRIMARY));
            gc.fillText(website, websiteX, currentY);

            // Underline
            gc.setStroke(Color.web(websiteHover ? "#6bb8ff" : DialogConstants.BRAND_PRIMARY));
            gc.strokeLine(websiteX, currentY + 2, websiteX + websiteWidth, currentY + 2);

            // Create clickable segment with tooltip (to be added to segments list later)
            websiteSegment = new TextSegment(website, "https://druscripts.com", true);
            websiteSegment.setBounds(websiteX, currentY, websiteWidth, 12);

            currentY += 40;
        }

        return new HeaderResult(websiteSegment, currentY);
    }

    /**
     * Simple container for header rendering results.
     */
    private static class HeaderResult {
        final TextSegment websiteSegment;
        final double nextY;

        HeaderResult(TextSegment websiteSegment, double nextY) {
            this.websiteSegment = websiteSegment;
            this.nextY = nextY;
        }
    }

    /**
     * Updates the cursor based on whether it's hovering over a link.
     *
     * @param canvas Canvas to update cursor on
     * @param segments List of text segments to check
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     */
    private static void updateCursor(Canvas canvas, List<TextSegment> segments, double mouseX, double mouseY) {
        boolean overLink = false;

        // Check if hovering over any link
        for (TextSegment segment : segments) {
            if (segment.isLink() && segment.contains(mouseX, mouseY)) {
                overLink = true;
                break;
            }
        }

        // Update cursor
        canvas.setCursor(overLink ? Cursor.HAND : Cursor.DEFAULT);
    }

    private static double getTextWidth(GraphicsContext gc, String text) {
        javafx.scene.text.Text textNode = new javafx.scene.text.Text(text);
        textNode.setFont(gc.getFont());
        return textNode.getLayoutBounds().getWidth();
    }
}
