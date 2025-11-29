package com.druscripts.dyemaker.tasks;

import com.druscripts.utils.FreeScript;
import com.druscripts.utils.Task;
import com.druscripts.dyemaker.DyeMaker;
import com.druscripts.dyemaker.ui.DyeMakerUI;
import javafx.scene.Scene;

public class SetupTask extends Task {

    private boolean setupComplete = false;

    public SetupTask(FreeScript script) {
        super(script);
    }

    @Override
    public boolean activate() {
        return !setupComplete;
    }

    @Override
    public boolean execute() {
        DyeMaker.task = "Setup";

        DyeMakerUI ui = new DyeMakerUI(script);
        Scene scene = ui.buildScene();
        script.getStageController().show(scene, "DyeMaker Configuration", false);

        DyeMaker.selectedDyeType = ui.getSelectedDyeType();

        if (DyeMaker.selectedDyeType == null) {
            script.stop();
            setupComplete = true;
            return false;
        }

        script.log(getClass(), "Selected: " + DyeMaker.selectedDyeType.getDisplayName());
        DyeMaker.task = "Starting...";
        setupComplete = true;
        return true;
    }
}
