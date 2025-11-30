package com.druscripts.dyemaker.tasks;

import com.druscripts.utils.script.FreeScript;
import com.druscripts.utils.tasks.Task;
import com.druscripts.dyemaker.DyeMaker;
import com.druscripts.dyemaker.ui.DyeMakerUI;
import javafx.scene.Scene;

public class SetupTask extends Task {

    private final DyeMaker dm;
    private boolean setupComplete = false;

    public SetupTask(FreeScript script) {
        super(script);
        dm = (DyeMaker) script;
    }

    @Override
    public boolean activate() {
        return !setupComplete;
    }

    @Override
    public void execute() {
        dm.task = "Setup";

        DyeMakerUI ui = new DyeMakerUI(script);
        Scene scene = ui.buildScene();
        script.getStageController().show(scene, "DyeMaker Configuration", false);

        dm.selectedDyeType = ui.getSelectedDyeType();

        if (dm.selectedDyeType == null) {
            script.stop();
            setupComplete = true;
            return;
        }

        script.log(getClass(), "Selected: " + dm.selectedDyeType.getDisplayName());
        dm.task = "Starting...";
        setupComplete = true;
    }
}
