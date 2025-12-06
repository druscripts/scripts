package com.druscripts.piemaker.tasks.processing;

import com.druscripts.piemaker.PieMaker;
import com.druscripts.piemaker.data.Constants;
import com.druscripts.utils.script.Task;
import com.druscripts.utils.production.CombineItemsTask;
import com.druscripts.utils.production.CombineItemsConfig;

public class MakePieShellsTask extends Task {

    private final CombineItemsTask combineTask;

    public MakePieShellsTask(PieMaker pieMaker) {
        super(pieMaker);

        CombineItemsConfig config = new CombineItemsConfig(
            Constants.PASTRY_DOUGH, Constants.PIE_DISH, Constants.PIE_SHELL,
            "MakePieShells", pieMaker::increaseItemsMade
        );

        this.combineTask = new CombineItemsTask(pieMaker, config);
    }

    @Override
    public boolean activate() {
        return combineTask.activate();
    }

    @Override
    public void execute() {
        combineTask.execute();
    }
}
