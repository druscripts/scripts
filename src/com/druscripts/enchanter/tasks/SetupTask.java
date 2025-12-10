package com.druscripts.enchanter.tasks;

import com.druscripts.enchanter.Enchanter;
import com.druscripts.enchanter.data.Stage;
import com.druscripts.enchanter.ui.EnchanterUI;
import com.druscripts.utils.script.Task;

import javafx.scene.Scene;

public class SetupTask extends Task {

    private final Enchanter enchanter;
    private boolean setupComplete = false;

    public SetupTask(Enchanter script) {
        super(script);
        this.enchanter = script;
    }

    @Override
    public boolean activate() {
        return !setupComplete;
    }

    @Override
    public void execute() {
        enchanter.task = "Setting up...";
        enchanter.stage = Stage.SETUP;

        enchanter.log(getClass(), "Starting setup...");
        enchanter.log(getClass(), "Showing configuration UI...");

        EnchanterUI ui = new EnchanterUI(enchanter);
        Scene scene = ui.buildScene();
        enchanter.getStageController().show(scene, "Enchanter Configuration", false);

        if (!ui.wasStarted()) {
            enchanter.log(getClass(), "User closed configuration window. Stopping script.");
            enchanter.stop();
            setupComplete = true;
            return;
        }

        enchanter.enchantLevel = ui.getSelectedLevel();
        enchanter.enchantableItem = ui.getSelectedItem();

        enchanter.log(getClass(), "Level: " + enchanter.enchantLevel.getDisplayName());
        enchanter.log(getClass(), "Item: " + enchanter.enchantableItem.getUnenchantedName());
        enchanter.log(getClass(), "Setup complete!");

        enchanter.initializeTasks();
        setupComplete = true;
    }
}
