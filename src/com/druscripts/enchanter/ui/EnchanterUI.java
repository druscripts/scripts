package com.druscripts.enchanter.ui;

import com.druscripts.enchanter.Enchanter;
import com.druscripts.enchanter.data.EnchantableItem;
import com.druscripts.enchanter.data.EnchantLevel;
import com.druscripts.utils.dialogwindow.Theme;
import com.druscripts.utils.dialogwindow.components.RadioButton;
import com.druscripts.utils.dialogwindow.dialogs.BaseScriptDialog;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.prefs.Preferences;

public class EnchanterUI extends BaseScriptDialog {

    private static final double RIGHT_COL_WIDTH = 400;
    private static final double RIGHT_COL_HEIGHT = 480;
    private static final String DESCRIPTION = "Enchants jewellery using the standard spellbook. " +
            "Supports all enchantment levels from 1-7. Start at any bank with runes and jewellery.";

    private final Preferences prefs = Preferences.userNodeForPackage(EnchanterUI.class);

    private EnchantLevel selectedLevel = EnchantLevel.LEVEL_1;
    private EnchantableItem selectedItem = EnchantableItem.SAPPHIRE_RING_ITEM;

    public EnchanterUI(Enchanter script) {
        super(script, script.getTitle(), script.getVersion(), RIGHT_COL_WIDTH, RIGHT_COL_HEIGHT);
        loadPreferences();
    }

    @Override
    protected String getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected void renderRightColumnContent(GraphicsContext gc, double x, double y, double width) {
        double currentY = y;

        // Enchant Level Selection
        gc.setFill(Color.web(Theme.TEXT_PRIMARY));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText("Enchant Level:", x, currentY);
        currentY += 25;

        EnchantLevel[] levels = EnchantLevel.values();
        for (int i = 0; i < levels.length; i++) {
            int col = i % 3;
            int row = i / 3;
            double radioX = x + (col * 130);
            double radioY = currentY + (row * 28);

            if (RadioButton.render(gc, radioX, radioY, levels[i].getDisplayName(),
                    selectedLevel == levels[i], mouseX, mouseY, clickX, clickY)) {
                selectedLevel = levels[i];
                // Reset item selection to first item of new level
                EnchantableItem[] items = EnchantableItem.getItemsForLevel(selectedLevel);
                if (items.length > 0) {
                    selectedItem = items[0];
                }
                clickX = -1; clickY = -1;
            }
        }
        currentY += ((levels.length + 2) / 3) * 28 + 15;

        // Equipment Section (Runes)
        gc.setFill(Color.web(Theme.TEXT_PRIMARY));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText("Equipment (Normal Runes):", x, currentY);
        currentY += 22;

        gc.setFill(Color.web(Theme.TEXT_SECONDARY));
        gc.setFont(Font.font("Arial", 12));
        gc.fillText(selectedLevel.getRuneString(), x + 10, currentY);
        currentY += 30;

        // Item Selection Section
        gc.setFill(Color.web(Theme.TEXT_PRIMARY));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText("Items:", x, currentY);
        currentY += 22;

        EnchantableItem[] items = EnchantableItem.getItemsForLevel(selectedLevel);
        gc.setFill(Color.web(Theme.TEXT_MUTED));
        gc.setFont(Font.font("Arial", 11));

        for (int i = 0; i < items.length; i++) {
            double itemY = currentY + (i * 18);
            gc.fillText("• " + items[i].getDisplayString(), x + 10, itemY);
        }
        currentY += items.length * 18 + 15;

        // Selected Item Radio Buttons
        gc.setFill(Color.web(Theme.TEXT_PRIMARY));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText("Select Item to Enchant:", x, currentY);
        currentY += 25;

        for (int i = 0; i < items.length; i++) {
            int col = i % 2;
            int row = i / 2;
            double radioX = x + (col * 190);
            double radioY = currentY + (row * 26);

            if (RadioButton.render(gc, radioX, radioY, items[i].getUnenchantedName(),
                    selectedItem == items[i], mouseX, mouseY, clickX, clickY)) {
                selectedItem = items[i];
                clickX = -1; clickY = -1;
            }
        }
    }

    @Override
    protected void onStart() {
        prefs.put("enchanter_level", selectedLevel.name());
        prefs.put("enchanter_item", selectedItem.name());
    }

    @Override
    protected void loadPreferences() {
        try {
            selectedLevel = EnchantLevel.valueOf(prefs.get("enchanter_level", EnchantLevel.LEVEL_1.name()));
        } catch (Exception e) {
            selectedLevel = EnchantLevel.LEVEL_1;
        }
        try {
            selectedItem = EnchantableItem.valueOf(prefs.get("enchanter_item", EnchantableItem.SAPPHIRE_RING_ITEM.name()));
            // Ensure item matches level
            if (selectedItem.getLevel() != selectedLevel) {
                EnchantableItem[] items = EnchantableItem.getItemsForLevel(selectedLevel);
                selectedItem = items.length > 0 ? items[0] : EnchantableItem.SAPPHIRE_RING_ITEM;
            }
        } catch (Exception e) {
            EnchantableItem[] items = EnchantableItem.getItemsForLevel(selectedLevel);
            selectedItem = items.length > 0 ? items[0] : EnchantableItem.SAPPHIRE_RING_ITEM;
        }
    }

    public EnchantLevel getSelectedLevel() {
        return wasStarted() ? selectedLevel : EnchantLevel.LEVEL_1;
    }

    public EnchantableItem getSelectedItem() {
        return wasStarted() ? selectedItem : EnchantableItem.SAPPHIRE_RING_ITEM;
    }
}
