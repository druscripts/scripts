package com.druscripts.enchanter.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for loading item sprites from JAR resources.
 * Used for items with missing IDs in the OSMB API.
 */
public class SpriteLoader {

    private static final String SPRITES_PATH = "/com/druscripts/enchanter/resources/sprites/";
    private static final Map<String, BufferedImage> cache = new HashMap<>();

    private SpriteLoader() {}

    /**
     * Loads a sprite image from the JAR resources.
     *
     * @param spriteName The name of the sprite file (without extension)
     * @return The loaded BufferedImage, or null if not found
     */
    public static BufferedImage loadSprite(String spriteName) {
        if (cache.containsKey(spriteName)) {
            return cache.get(spriteName);
        }

        String path = SPRITES_PATH + spriteName + ".png";
        try (InputStream is = SpriteLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("[SpriteLoader] Sprite not found: " + path);
                return null;
            }
            BufferedImage img = ImageIO.read(is);
            cache.put(spriteName, img);
            return img;
        } catch (IOException e) {
            System.err.println("[SpriteLoader] Failed to load sprite: " + path);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if a sprite exists in the resources.
     *
     * @param spriteName The name of the sprite file (without extension)
     * @return true if the sprite exists
     */
    public static boolean spriteExists(String spriteName) {
        if (cache.containsKey(spriteName)) {
            return true;
        }
        String path = SPRITES_PATH + spriteName + ".png";
        try (InputStream is = SpriteLoader.class.getResourceAsStream(path)) {
            return is != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Clears the sprite cache.
     */
    public static void clearCache() {
        cache.clear();
    }
}
