package com.druscripts.utils.dialogwindow;

/**
 * Represents a segment of text that can be either plain text or a clickable hyperlink.
 */
public class TextSegment {
    private final String text;
    private final String url;
    private final boolean isLink;
    private final boolean showTooltip;

    // Bounding box (set during rendering)
    private double x;
    private double y;
    private double width;
    private double height;

    public TextSegment(String text) {
        this.text = text;
        this.url = null;
        this.isLink = false;
        this.showTooltip = false;
    }

    public TextSegment(String text, String url) {
        this(text, url, false);
    }

    public TextSegment(String text, String url, boolean showTooltip) {
        this.text = text;
        this.url = url;
        this.isLink = true;
        this.showTooltip = showTooltip;
    }

    public String getText() {
        return text;
    }

    public String getUrl() {
        return url;
    }

    public boolean isLink() {
        return isLink;
    }

    public boolean showTooltip() {
        return showTooltip;
    }

    public void setBounds(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width &&
               mouseY >= y - height && mouseY <= y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
