package com.druscripts.dyemaker.ui;

import com.druscripts.dyemaker.DyeMaker;
import com.druscripts.dyemaker.data.DyeType;
import com.druscripts.utils.dialogwindow.Theme;
import com.druscripts.utils.dialogwindow.components.RadioButton;
import com.druscripts.utils.dialogwindow.dialogs.BaseScriptDialog;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.prefs.Preferences;

/**
 * DyeMaker configuration dialog using the standard BaseScriptDialog layout.
 */
public class DyeMakerUI extends BaseScriptDialog {

    private static final double RIGHT_COL_WIDTH = 280;
    private static final double RIGHT_COL_HEIGHT = 368;
    private static final String DESCRIPTION = "Makes dyes at Aggie's shop in Draynor Village. " +
            "Start near Draynor bank or Aggie's house with coins and ingredients in your bank.";

    private final Preferences prefs = Preferences.userNodeForPackage(DyeMakerUI.class);

    private DyeType selectedDyeType = DyeType.RED;

    public DyeMakerUI(DyeMaker script) {
        super(script, script.getTitle(), script.getVersion(), RIGHT_COL_WIDTH, RIGHT_COL_HEIGHT);
        loadPreferences();
    }

    @Override
    protected String getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected void renderRightColumnContent(GraphicsContext gc, double x, double y, double width) {
        gc.setFill(Color.web(Theme.TEXT_PRIMARY));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText("Select Dye Type:", x, y);

        double contentY = y + 30;

        for (DyeType dye : DyeType.values()) {
            if (RadioButton.render(gc, x, contentY, dye.getDisplayName(),
                    selectedDyeType == dye, mouseX, mouseY, clickX, clickY)) {
                selectedDyeType = dye;
                clickX = -1;  // Consume click
                clickY = -1;
            }
            contentY += 35;
        }

        contentY += 15;

        gc.setFill(Color.web(Theme.TEXT_MUTED));
        gc.setFont(Font.font("Arial", 11));
        String info = "Requires: " + selectedDyeType.getIngredientCount() + "x " +
                      selectedDyeType.getIngredientName() + " + 5 coins per dye";
        gc.fillText(info, x, contentY);
    }

    @Override
    protected void onStart() {
        prefs.put("dyemaker_dyeType", selectedDyeType.name());
    }

    @Override
    protected void loadPreferences() {
        try {
            selectedDyeType = DyeType.valueOf(prefs.get("dyemaker_dyeType", DyeType.RED.name()));
        } catch (Exception e) {
            selectedDyeType = DyeType.RED;
        }
    }

    public DyeType getSelectedDyeType() {
        return wasStarted() ? selectedDyeType : null;
    }
}
