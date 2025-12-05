package com.druscripts.piemaker.tasks;

import com.druscripts.piemaker.PieMaker;
import com.druscripts.piemaker.data.Constants;
import com.druscripts.piemaker.data.Stage;
import com.druscripts.piemaker.ui.PieMakerUI;
import com.druscripts.utils.script.Task;
import com.druscripts.utils.dialogwindow.dialogs.ErrorDialog;
import com.osmb.api.location.position.types.WorldPosition;
import javafx.scene.Scene;

public class SetupTask extends Task {

    private final PieMaker pieMaker;
    private boolean setupComplete = false;

    public SetupTask(PieMaker script) {
        super(script);
        this.pieMaker = script;
    }

    @Override
    public boolean activate() {
        return !setupComplete;
    }

    @Override
    public void execute() {
        pieMaker.task = "Setting up...";
        pieMaker.stage = Stage.SETUP;

        pieMaker.log(getClass(), "Starting setup...");

        WorldPosition playerPos = pieMaker.getWorldPosition();
        if (playerPos == null) {
            Scene errorScene = ErrorDialog.createErrorScene(
                "Position Error",
                "Could not detect your location. Please try restarting the script.",
                pieMaker::stop
            );
            pieMaker.getStageController().show(errorScene, "Error", false);
            return;
        }

        int playerRegion = playerPos.getRegionID();
        boolean isLumbridge = playerRegion == Constants.LUMBRIDGE_REGION;
        boolean isGE = playerRegion == Constants.GRAND_EXCHANGE_REGION;

        if (!isLumbridge && !isGE) {
            Scene errorScene = ErrorDialog.createErrorScene(
                "Invalid Location",
                "You must start at either Lumbridge Castle or Grand Exchange bank.\n\nCurrent region: " + playerRegion,
                () -> pieMaker.stop()
            );
            pieMaker.getStageController().show(errorScene, "Error", false);
            return;
        }

        pieMaker.detectedRegion = playerRegion;
        pieMaker.log(getClass(), "Location: " + (isLumbridge ? "Lumbridge" : "Grand Exchange"));

        pieMaker.log(getClass(), "Showing configuration UI...");
        PieMakerUI ui = new PieMakerUI(pieMaker, isLumbridge);
        Scene scene = ui.buildScene();
        pieMaker.getStageController().show(scene, "PieMaker Configuration", false);

        if (!ui.wasStarted()) {
            pieMaker.log(getClass(), "User closed configuration window. Stopping script.");
            pieMaker.stop();
            setupComplete = true;
            return;
        }

        pieMaker.allInOne = ui.isAllInOne();
        pieMaker.stage = ui.getStage();
        pieMaker.waterSourceId = ui.getWaterSourceId();
        pieMaker.pieType = ui.getPieType();

        pieMaker.log(getClass(), "Mode: " + (pieMaker.allInOne ? "All-in-one" : "Step-by-step"));
        pieMaker.log(getClass(), "Stage: " + pieMaker.stage);
        pieMaker.log(getClass(), "Setup complete!");

        pieMaker.initializeProductionTasks();
        setupComplete = true;
    }
}
