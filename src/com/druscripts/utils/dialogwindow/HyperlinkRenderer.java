package com.druscripts.utils.dialogwindow;

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
 * Utility for rendering text with clickable hyperlinks in Canvas-based UIs.
 *
 * Usage:
 * - Syntax: <a href="url">text</a>, <a href='url'>text</a>, or <a>url</a>
 * - Optional tooltip: <a href="url" tooltip>text</a> shows URL on hover
 * - Example: "Visit the <a href=\"https://oldschool.runescape.wiki\">wiki</a> for info"
 * - Example: "Visit the <a href='https://oldschool.runescape.wiki' tooltip>wiki</a> for info"
 * - Example: "Visit <a>https://oldschool.runescape.wiki</a> for info"
 *
 * Features:
 * - Parses HTML-style anchor tags (supports both single and double quotes)
 * - Renders links in different color with underline
 * - Tracks hover state
 * - Opens links in default browser on click
 * - Optional tooltips showing URL on hover
 */
public class HyperlinkRenderer {

    // Matches <a href="url" [tooltip]>text</a>, <a href='url' [tooltip]>text</a>, or <a>url</a>
    private static final Pattern LINK_PATTERN = Pattern.compile("<a(?:\\s+href=[\"']([^\"']+)[\"'])?(?:\\s+(tooltip))?>([^<]+)</a>");
    private static final String LINK_COLOR = DialogConstants.BRAND_PRIMARY;
    private static final String HOVER_COLOR = "#6bb8ff"; // Brighter blue on hover

    private final List<TextSegment> segments = new ArrayList<>();
    private TextSegment hoveredSegment = null;

    /**
     * Parses text containing anchor tags and returns a list of text segments.
     *
     * @param text Text with optional links (supports both single and double quotes)
     * @return List of TextSegment objects (plain text and links)
     */
    public static List<TextSegment> parseText(String text) {
        List<TextSegment> segments = new ArrayList<>();
        Matcher matcher = LINK_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            // Add plain text before the link
            if (matcher.start() > lastEnd) {
                String plainText = text.substring(lastEnd, matcher.start());
                segments.add(new TextSegment(plainText));
            }

            // Add the link
            String href = matcher.group(1); // Can be null if no href attribute
            String tooltipAttr = matcher.group(2); // "tooltip" or null
            String linkText = matcher.group(3);
            String url = (href != null && !href.isEmpty()) ? href : linkText;
            boolean showTooltip = (tooltipAttr != null);
            segments.add(new TextSegment(linkText, url, showTooltip));

            lastEnd = matcher.end();
        }

        // Add remaining plain text
        if (lastEnd < text.length()) {
            String plainText = text.substring(lastEnd);
            segments.add(new TextSegment(plainText));
        }

        // If no links found, return single segment
        if (segments.isEmpty()) {
            segments.add(new TextSegment(text));
        }

        return segments;
    }

    /**
     * Renders text with word wrapping and clickable hyperlinks.
     *
     * @param gc GraphicsContext to render on
     * @param text Text with optional markdown-style links
     * @param x Starting X position
     * @param y Starting Y position
     * @param maxWidth Maximum width before wrapping
     * @param normalColor Color for normal text
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     * @return List of all text segments with their bounding boxes
     */
    public static List<TextSegment> renderTextWithLinks(
            GraphicsContext gc,
            String text,
            double x,
            double y,
            double maxWidth,
            String normalColor,
            double mouseX,
            double mouseY) {

        List<TextSegment> allSegments = new ArrayList<>();
        String[] lines = text.split("\\n");
        double lineHeight = 20;
        double currentY = y;

        for (String line : lines) {
            // Handle empty lines
            if (line.isEmpty()) {
                currentY += lineHeight;
                continue;
            }

            List<TextSegment> lineSegments = parseText(line);
            double currentX = x;

            for (TextSegment segment : lineSegments) {
                String segmentText = segment.getText();

                // Handle leading space from segment
                boolean segmentStartsWithSpace = segmentText.startsWith(" ");
                if (segmentStartsWithSpace && currentX > x) {
                    // Add space width to currentX
                    currentX += getTextWidth(gc, " ");
                    segmentText = segmentText.substring(1); // Remove leading space from text to split
                }

                boolean segmentEndsWithSpace = segmentText.endsWith(" ");
                String[] words = segmentText.split(" ");

                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    if (word.isEmpty()) continue; // Skip empty strings from split

                    boolean isLastWord = (i == words.length - 1);

                    // Add space after word if not last, OR if it's the last word but segment ended with space
                    String wordWithSpace = (isLastWord && !segmentEndsWithSpace) ? word : word + " ";
                    double wordWidth = getTextWidth(gc, wordWithSpace);

                    // Check if word fits on current line
                    if (currentX + wordWidth > x + maxWidth && currentX > x) {
                        // Wrap to next line
                        currentY += lineHeight;
                        currentX = x;
                    }

                    // Create segment for this word
                    TextSegment wordSegment = segment.isLink()
                            ? new TextSegment(wordWithSpace, segment.getUrl(), segment.showTooltip())
                            : new TextSegment(wordWithSpace);

                    // Set color based on link status and hover
                    if (wordSegment.isLink()) {
                        boolean isHovered = false;
                        // Check if mouse is over this word (approximate check)
                        if (mouseX >= currentX && mouseX <= currentX + wordWidth &&
                            mouseY >= currentY - lineHeight && mouseY <= currentY) {
                            isHovered = true;
                        }
                        gc.setFill(Color.web(isHovered ? HOVER_COLOR : LINK_COLOR));
                    } else {
                        gc.setFill(Color.web(normalColor));
                    }

                    // Render the word
                    gc.fillText(wordWithSpace, currentX, currentY);

                    // Draw underline for links
                    if (wordSegment.isLink()) {
                        gc.strokeLine(currentX, currentY + 2, currentX + wordWidth, currentY + 2);
                    }

                    // Store bounding box
                    wordSegment.setBounds(currentX, currentY, wordWidth, lineHeight);
                    allSegments.add(wordSegment);

                    currentX += wordWidth;
                }
            }

            currentY += lineHeight;
        }

        // Render tooltip if hovering over a link with tooltip enabled
        renderTooltips(gc, allSegments, mouseX, mouseY);

        return allSegments;
    }

    /**
     * Renders tooltips for hovered links that have tooltip enabled.
     *
     * @param gc GraphicsContext to render on
     * @param segments List of text segments to check
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     */
    private static void renderTooltips(GraphicsContext gc, List<TextSegment> segments, double mouseX, double mouseY) {
        for (TextSegment segment : segments) {
            if (segment.isLink() && segment.showTooltip() && segment.contains(mouseX, mouseY)) {
                String url = segment.getUrl();

                // Measure tooltip
                gc.setFont(Font.font("Arial", 11));
                double tooltipTextWidth = getTextWidth(gc, url);
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
     * Handles click events on text segments. Opens URLs in default browser.
     *
     * @param segments List of text segments with bounding boxes
     * @param clickX Click X position
     * @param clickY Click Y position
     * @return true if a link was clicked and opened
     */
    public static boolean handleClick(List<TextSegment> segments, double clickX, double clickY) {
        for (TextSegment segment : segments) {
            if (segment.isLink() && segment.contains(clickX, clickY)) {
                openUrl(segment.getUrl());
                return true;
            }
        }
        return false;
    }

    /**
     * Opens a URL in the default browser.
     *
     * @param url URL to open
     */
    public static void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to open URL: " + url);
            e.printStackTrace();
        }
    }

    /**
     * Gets the display width of text.
     *
     * @param gc GraphicsContext with current font
     * @param text Text to measure
     * @return Width in pixels
     */
    private static double getTextWidth(GraphicsContext gc, String text) {
        javafx.scene.text.Text textNode = new javafx.scene.text.Text(text);
        textNode.setFont(gc.getFont());
        return textNode.getLayoutBounds().getWidth();
    }
}
