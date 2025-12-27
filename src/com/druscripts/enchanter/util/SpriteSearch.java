package com.druscripts.enchanter.util;

import com.osmb.api.script.Script;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.ToleranceComparator;
import com.osmb.api.visual.image.ImageSearchResult;
import com.osmb.api.visual.image.SearchableImage;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Utility class for sprite-based searching in inventory and bank.
 * Used for items with missing IDs in the OSMB API.
 */
public class SpriteSearch {

    private SpriteSearch() {}

    // ==================== INVENTORY METHODS ====================

    /**
     * Searches the inventory for an item by its sprite image.
     */
    public static boolean hasItemInInventory(Script script, String spriteName) {
        BufferedImage sprite = SpriteLoader.loadSprite(spriteName);
        if (sprite == null) {
            script.log(SpriteSearch.class, "Sprite not loaded: " + spriteName);
            return false;
        }

        if (!script.getWidgetManager().getInventory().isOpen()) {
            script.getWidgetManager().getInventory().open();
            script.pollFramesHuman(() -> script.getWidgetManager().getInventory().isOpen(), 1000);
        }

        return findSprite(script, sprite) != null;
    }

    /**
     * Counts items matching the sprite in inventory.
     */
    public static int countInInventory(Script script, String spriteName) {
        BufferedImage sprite = SpriteLoader.loadSprite(spriteName);
        if (sprite == null) {
            return 0;
        }

        if (!script.getWidgetManager().getInventory().isOpen()) {
            script.getWidgetManager().getInventory().open();
            script.pollFramesHuman(() -> script.getWidgetManager().getInventory().isOpen(), 1000);
        }

        return findAllSprites(script, sprite).size();
    }

    // ==================== BANK METHODS ====================

    /**
     * Searches the bank for an item by its sprite image.
     * Bank must already be open.
     */
    public static boolean hasItemInBank(Script script, String spriteName) {
        BufferedImage sprite = SpriteLoader.loadSprite(spriteName);
        if (sprite == null) {
            script.log(SpriteSearch.class, "Sprite not loaded: " + spriteName);
            return false;
        }

        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(SpriteSearch.class, "Bank not open for sprite search");
            return false;
        }

        Rectangle bankBounds = script.getWidgetManager().getBank().getBounds();
        if (bankBounds == null) {
            return findSprite(script, sprite) != null;
        }

        return findSpriteInBounds(script, sprite, bankBounds) != null;
    }

    /**
     * Counts items matching the sprite in bank.
     * Bank must already be open.
     * Note: Returns match count, not stack quantity.
     */
    public static int countInBank(Script script, String spriteName) {
        BufferedImage sprite = SpriteLoader.loadSprite(spriteName);
        if (sprite == null) {
            return 0;
        }

        if (!script.getWidgetManager().getBank().isVisible()) {
            return 0;
        }

        Rectangle bankBounds = script.getWidgetManager().getBank().getBounds();
        if (bankBounds == null) {
            return findAllSprites(script, sprite).size();
        }

        return findAllSpritesInBounds(script, sprite, bankBounds).size();
    }

    // ==================== CORE SEARCH METHODS ====================

    private static ImageSearchResult findSprite(Script script, BufferedImage sprite) {
        try {
            SearchableImage searchable = new SearchableImage(
                sprite,
                ToleranceComparator.ZERO_TOLERANCE,
                ColorModel.RGB
            );
            return script.getImageAnalyzer().findLocation(searchable);
        } catch (Exception e) {
            return null;
        }
    }

    private static List<ImageSearchResult> findAllSprites(Script script, BufferedImage sprite) {
        try {
            SearchableImage searchable = new SearchableImage(
                sprite,
                ToleranceComparator.ZERO_TOLERANCE,
                ColorModel.RGB
            );
            List<ImageSearchResult> results = script.getImageAnalyzer().findLocations(searchable);
            return results != null ? results : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    private static ImageSearchResult findSpriteInBounds(Script script, BufferedImage sprite, Rectangle bounds) {
        try {
            SearchableImage searchable = new SearchableImage(
                sprite,
                ToleranceComparator.ZERO_TOLERANCE,
                ColorModel.RGB
            );
            return script.getImageAnalyzer().findLocation(bounds, searchable);
        } catch (Exception e) {
            return null;
        }
    }

    private static List<ImageSearchResult> findAllSpritesInBounds(Script script, BufferedImage sprite, Rectangle bounds) {
        try {
            SearchableImage searchable = new SearchableImage(
                sprite,
                ToleranceComparator.ZERO_TOLERANCE,
                ColorModel.RGB
            );
            List<ImageSearchResult> results = script.getImageAnalyzer().findLocations(bounds, searchable);
            return results != null ? results : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
}
