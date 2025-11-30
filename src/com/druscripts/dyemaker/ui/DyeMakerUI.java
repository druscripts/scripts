package com.druscripts.dyemaker.ui;

import com.druscripts.dyemaker.data.DyeType;
import com.druscripts.utils.dialogwindow.Theme;
import com.druscripts.utils.dialogwindow.components.RadioButton;
import com.druscripts.utils.dialogwindow.dialogs.BaseScriptDialog;
import com.osmb.api.script.Script;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * DyeMaker configuration dialog using the standard BaseScriptDialog layout.
 */
public class DyeMakerUI extends BaseScriptDialog {

    private static final double RIGHT_COL_WIDTH = 280;
    private static final double RIGHT_COL_HEIGHT = 368;

    private DyeType selectedDyeType = DyeType.RED;

    public DyeMakerUI(Script script) {
        super(script, RIGHT_COL_WIDTH, RIGHT_COL_HEIGHT);
        loadPreferences();
    }

    @Override
    protected String getScriptTitle() {
        return "DyeMaker";
    }

    @Override
    protected String getScriptVersion() {
        return "v1.0";
    }

    @Override
    protected String getDescription() {
        return "Makes dyes at Aggie's shop in Draynor Village. " +
               "Start near Draynor bank or Aggie's house with coins and ingredients in your bank.";
    }

    @Override
    protected void renderRightColumnContent(GraphicsContext gc, double x, double y, double width) {
        // Dye Type Selection label
        gc.setFill(Color.web(Theme.TEXT_PRIMARY));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText("Select Dye Type:", x, y);

        double contentY = y + 30;

        // Render radio buttons for each dye type
        for (DyeType dye : DyeType.values()) {
            new RadioButton(
                x,
                contentY,
                dye.getDisplayName(),
                selectedDyeType == dye,
                mouseX,
                mouseY
            ).render(gc);
            contentY += 35;
        }

        contentY += 15;

        // Selected dye info
        gc.setFill(Color.web(Theme.TEXT_MUTED));
        gc.setFont(Font.font("Arial", 11));
        String info = "Requires: " + selectedDyeType.getIngredientCount() + "x " +
                      selectedDyeType.getIngredientName() + " + 5 coins per dye";
        gc.fillText(info, x, contentY);
    }

    @Override
    protected boolean handleRightColumnClick(double clickX, double clickY) {
        double x = RIGHT_COLUMN_X + PANEL_PADDING;
        double y = RIGHT_COLUMN_Y + 60 + 30;  // After "Configuration" header and label

        for (DyeType dye : DyeType.values()) {
            RadioButton radio = new RadioButton(
                x,
                y,
                dye.getDisplayName(),
                selectedDyeType == dye,
                mouseX,
                mouseY
            );
            if (radio.isClicked(clickX, clickY)) {
                selectedDyeType = dye;
                return true;
            }
            y += 35;
        }
        return false;
    }

    @Override
    protected void onStart() {
        // No preferences to save for now
    }

    @Override
    protected void loadPreferences() {
        // No preferences to load for now
    }

    /**
     * Get the selected dye type. Returns null if user closed without confirming.
     */
    public DyeType getSelectedDyeType() {
        return isDismissed() ? selectedDyeType : null;
    }
}
