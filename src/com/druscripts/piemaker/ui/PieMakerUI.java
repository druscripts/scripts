package com.druscripts.piemaker.ui;

import com.druscripts.piemaker.PieMaker;
import com.druscripts.piemaker.data.Constants;
import com.druscripts.piemaker.data.PieType;
import com.druscripts.piemaker.data.Stage;
import com.druscripts.utils.dialogwindow.Theme;
import com.druscripts.utils.dialogwindow.components.RadioButton;
import com.druscripts.utils.dialogwindow.dialogs.BaseScriptDialog;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.prefs.Preferences;

public class PieMakerUI extends BaseScriptDialog {

    private static final double RIGHT_COL_WIDTH = 380;
    private static final double RIGHT_COL_HEIGHT = 450;
    private static final String DESCRIPTION = "All-in-one Lumbridge pie making script. " +
            "Makes pies from flour to cooked pies. Start at Lumbridge Castle or Grand Exchange bank.";

    private final Preferences prefs = Preferences.userNodeForPackage(PieMakerUI.class);
    private final boolean canCook;

    private boolean allInOne = true;
    private Stage stage = Stage.MAKE_DOUGH;
    private int waterSourceId = Constants.JUG_OF_WATER;
    private PieType pieType = PieType.REDBERRY;

    public PieMakerUI(PieMaker script, boolean canCook) {
        super(script, script.getTitle(), script.getVersion(), RIGHT_COL_WIDTH, RIGHT_COL_HEIGHT);
        this.canCook = canCook;
        loadPreferences();
    }

    @Override
    protected String getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected void renderRightColumnContent(GraphicsContext gc, double x, double y, double width) {
        double currentY = y;

        // Mode
        gc.setFill(Color.web(Theme.TEXT_PRIMARY));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText("Mode:", x, currentY);
        currentY += 25;

        if (RadioButton.render(gc, x, currentY, "All-in-one", allInOne, mouseX, mouseY, clickX, clickY)) {
            allInOne = true;
            clickX = -1; clickY = -1;
        }
        if (RadioButton.render(gc, x + 180, currentY, "Step-by-step", !allInOne, mouseX, mouseY, clickX, clickY)) {
            allInOne = false;
            clickX = -1; clickY = -1;
        }
        currentY += 40;

        gc.setFill(Color.web(Theme.TEXT_MUTED));
        gc.setFont(Font.font("Arial", 11));
        gc.fillText(allInOne ? "Full pipeline from ingredients to cooked pies." : "Performs only the selected step.", x, currentY);
        currentY += 30;

        // Stage
        gc.setFill(Color.web(Theme.TEXT_PRIMARY));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText(allInOne ? "Start From:" : "Make:", x, currentY);
        currentY += 25;

        String[] stageNames;
        Stage[] stageValues;

        if (allInOne) {
            if (canCook) {
                stageNames = new String[] {"Flour", "Pastry Dough", "Pie Shell", "Uncooked Pie"};
                stageValues = new Stage[] {Stage.MAKE_DOUGH, Stage.MAKE_SHELL, Stage.MAKE_UNCOOKED, Stage.COOK};
            } else {
                stageNames = new String[] {"Flour", "Pastry Dough", "Pie Shell"};
                stageValues = new Stage[] {Stage.MAKE_DOUGH, Stage.MAKE_SHELL, Stage.MAKE_UNCOOKED};
            }
        } else {
            if (canCook) {
                stageNames = new String[] {"Pastry Dough", "Pie Shell", "Uncooked Pie", "Cooked Pie"};
                stageValues = new Stage[] {Stage.MAKE_DOUGH, Stage.MAKE_SHELL, Stage.MAKE_UNCOOKED, Stage.COOK};
            } else {
                stageNames = new String[] {"Pastry Dough", "Pie Shell", "Uncooked Pie"};
                stageValues = new Stage[] {Stage.MAKE_DOUGH, Stage.MAKE_SHELL, Stage.MAKE_UNCOOKED};
            }
        }

        for (int i = 0; i < stageNames.length; i++) {
            int col = i % 2;
            int row = i / 2;
            if (RadioButton.render(gc, x + (col * 180), currentY + (row * 30), stageNames[i],
                    stage == stageValues[i], mouseX, mouseY, clickX, clickY)) {
                stage = stageValues[i];
                clickX = -1; clickY = -1;
            }
        }
        currentY += ((stageNames.length + 1) / 2) * 30 + 10;

        // Water source (only for dough stage when starting from flour)
        if (stage == Stage.MAKE_DOUGH) {
            gc.setFill(Color.web(Theme.TEXT_PRIMARY));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            gc.fillText("Water Source:", x, currentY);
            currentY += 25;

            if (RadioButton.render(gc, x, currentY, "Jug", waterSourceId == Constants.JUG_OF_WATER,
                    mouseX, mouseY, clickX, clickY)) {
                waterSourceId = Constants.JUG_OF_WATER;
                clickX = -1; clickY = -1;
            }
            if (RadioButton.render(gc, x + 120, currentY, "Bucket", waterSourceId == Constants.BUCKET_OF_WATER,
                    mouseX, mouseY, clickX, clickY)) {
                waterSourceId = Constants.BUCKET_OF_WATER;
                clickX = -1; clickY = -1;
            }
            if (RadioButton.render(gc, x + 240, currentY, "Bowl", waterSourceId == Constants.BOWL_OF_WATER,
                    mouseX, mouseY, clickX, clickY)) {
                waterSourceId = Constants.BOWL_OF_WATER;
                clickX = -1; clickY = -1;
            }
            currentY += 40;
        }

        // Pie type (when needed)
        if (allInOne || stage == Stage.MAKE_UNCOOKED || stage == Stage.COOK) {
            gc.setFill(Color.web(Theme.TEXT_PRIMARY));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            gc.fillText("Pie Type:", x, currentY);
            currentY += 25;

            PieType[] types = PieType.values();
            for (int i = 0; i < types.length; i++) {
                int col = i % 2;
                int row = i / 2;
                if (RadioButton.render(gc, x + (col * 180), currentY + (row * 30), types[i].getDisplayName(),
                        pieType == types[i], mouseX, mouseY, clickX, clickY)) {
                    pieType = types[i];
                    clickX = -1; clickY = -1;
                }
            }
        }
    }

    @Override
    protected void onStart() {
        prefs.putBoolean("piemaker_allInOne", allInOne);
        prefs.put("piemaker_stage", stage.name());
        prefs.putInt("piemaker_waterSourceId", waterSourceId);
        prefs.put("piemaker_pieType", pieType.name());
    }

    @Override
    protected void loadPreferences() {
        allInOne = prefs.getBoolean("piemaker_allInOne", true);
        try {
            stage = Stage.valueOf(prefs.get("piemaker_stage", Stage.MAKE_DOUGH.name()));
        } catch (Exception e) {
            stage = Stage.MAKE_DOUGH;
        }
        waterSourceId = prefs.getInt("piemaker_waterSourceId", Constants.JUG_OF_WATER);
        try {
            pieType = PieType.valueOf(prefs.get("piemaker_pieType", PieType.REDBERRY.name()));
        } catch (Exception e) {
            pieType = PieType.REDBERRY;
        }
    }

    public boolean isAllInOne() { return wasStarted() ? allInOne : true; }
    public Stage getStage() { return wasStarted() ? stage : Stage.MAKE_DOUGH; }
    public int getWaterSourceId() { return wasStarted() ? waterSourceId : Constants.JUG_OF_WATER; }
    public PieType getPieType() { return wasStarted() ? pieType : null; }
}
