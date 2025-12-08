package com.druscripts.utils.widget.exception;

/**
 * Exception thrown when a widget (inventory, bank, etc.) cannot be opened.
 */
public class CannotOpenWidgetException extends RuntimeException {

    public CannotOpenWidgetException(String widgetName) {
        super("Failed to open widget: " + widgetName);
    }
}
