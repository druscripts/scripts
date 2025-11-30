package com.druscripts.utils.dialogwindow.components;

import com.druscripts.utils.dialogwindow.Theme;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reusable hyperlink component for immediate mode Canvas UIs.
 * Renders text with clickable hyperlinks using HTML-style anchor tags.
 *
 * Usage:
 * - Syntax: <a href="url">text</a>, <a href='url'>text</a>, or <a>url</a>
 * - Example: "Visit the <a href=\"https://example.com\">website</a> for info"
 */
public class Hyperlink {

    private static final Pattern LINK_PATTERN = Pattern.compile("<a(?:\\s+href=[\"']([^\"']+)[\"'])?>([^<]+)</a>");
    private static final String LINK_COLOR = Theme.BRAND_PRIMARY;
    private static final String HOVER_COLOR = "#6bb8ff";
    private static final double LINE_HEIGHT = 20;

    private final double x;
    private final double y;
    private final String text;
    private final double maxWidth;
    private final String normalColor;
    private final double mouseX;
    private final double mouseY;

    private final List<HitRegion> hitRegions = new ArrayList<>();

    /**
     * Internal data for clickable regions.
     */
    private static class HitRegion {
        final double x, y, width, height;
        final String url;

        HitRegion(double x, double y, double width, double height, String url) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.url = url;
        }

        boolean contains(double px, double py) {
            return px >= x && px <= x + width && py >= y - height && py <= y;
        }
    }

    /**
     * Parsed segment of text (either plain text or a link).
     */
    private static class ParsedSegment {
        final String text;
        final String url;  // null if plain text

        ParsedSegment(String text, String url) {
            this.text = text;
            this.url = url;
        }

        boolean isLink() {
            return url != null;
        }
    }

    /**
     * Create a hyperlink component.
     *
     * @param x X position (left edge)
     * @param y Y position (baseline of first line)
     * @param text Text with optional HTML anchor tags
     * @param maxWidth Maximum width before wrapping
     * @param normalColor Color for non-link text
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     */
    public Hyperlink(double x, double y, String text, double maxWidth, String normalColor, double mouseX, double mouseY) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.maxWidth = maxWidth;
        this.normalColor = normalColor;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    /**
     * Render the hyperlink text.
     */
    public void render(GraphicsContext gc) {
        hitRegions.clear();

        String[] lines = text.split("\\n");
        double currentY = y;

        for (String line : lines) {
            if (line.isEmpty()) {
                currentY += LINE_HEIGHT;
                continue;
            }

            List<ParsedSegment> segments = parseText(line);
            double currentX = x;

            for (ParsedSegment segment : segments) {
                String segmentText = segment.text;

                boolean startsWithSpace = segmentText.startsWith(" ");
                if (startsWithSpace && currentX > x) {
                    currentX += getTextWidth(gc, " ");
                    segmentText = segmentText.substring(1);
                }

                boolean endsWithSpace = segmentText.endsWith(" ");
                String[] words = segmentText.split(" ");

                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    if (word.isEmpty()) continue;

                    boolean isLastWord = (i == words.length - 1);
                    String wordWithSpace = (isLastWord && !endsWithSpace) ? word : word + " ";
                    double wordWidth = getTextWidth(gc, wordWithSpace);

                    // Wrap if needed
                    if (currentX + wordWidth > x + maxWidth && currentX > x) {
                        currentY += LINE_HEIGHT;
                        currentX = x;
                    }

                    // Check hover state
                    boolean isHovered = segment.isLink() &&
                        mouseX >= currentX && mouseX <= currentX + wordWidth &&
                        mouseY >= currentY - LINE_HEIGHT && mouseY <= currentY;

                    // Set color
                    if (segment.isLink()) {
                        gc.setFill(Color.web(isHovered ? HOVER_COLOR : LINK_COLOR));
                    } else {
                        gc.setFill(Color.web(normalColor));
                    }

                    // Render word
                    gc.fillText(wordWithSpace, currentX, currentY);

                    // Underline for links
                    if (segment.isLink()) {
                        gc.setStroke(Color.web(isHovered ? HOVER_COLOR : LINK_COLOR));
                        gc.strokeLine(currentX, currentY + 2, currentX + wordWidth, currentY + 2);

                        // Store hit region
                        hitRegions.add(new HitRegion(currentX, currentY, wordWidth, LINE_HEIGHT, segment.url));
                    }

                    currentX += wordWidth;
                }
            }

            currentY += LINE_HEIGHT;
        }
    }

    /**
     * Check if a click hits a link and open it.
     *
     * @param clickX Click X position
     * @param clickY Click Y position
     * @return true if a link was clicked
     */
    public boolean isClicked(double clickX, double clickY) {
        for (HitRegion region : hitRegions) {
            if (region.contains(clickX, clickY)) {
                openUrl(region.url);
                return true;
            }
        }
        return false;
    }

    /**
     * Parse text containing anchor tags into segments.
     */
    private List<ParsedSegment> parseText(String text) {
        List<ParsedSegment> segments = new ArrayList<>();
        Matcher matcher = LINK_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                segments.add(new ParsedSegment(text.substring(lastEnd, matcher.start()), null));
            }

            String href = matcher.group(1);
            String linkText = matcher.group(2);
            String url = (href != null && !href.isEmpty()) ? href : linkText;
            segments.add(new ParsedSegment(linkText, url));

            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            segments.add(new ParsedSegment(text.substring(lastEnd), null));
        }

        if (segments.isEmpty()) {
            segments.add(new ParsedSegment(text, null));
        }

        return segments;
    }

    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to open URL: " + url);
        }
    }

    private double getTextWidth(GraphicsContext gc, String text) {
        javafx.scene.text.Text textNode = new javafx.scene.text.Text(text);
        textNode.setFont(gc.getFont());
        return textNode.getLayoutBounds().getWidth();
    }
}
