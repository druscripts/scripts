package com.druscripts.piemaker.tasks.processing;

import com.druscripts.piemaker.PieMaker;
import com.druscripts.piemaker.data.Constants;
import com.druscripts.piemaker.data.Stage;
import com.druscripts.utils.script.Task;
import com.druscripts.utils.production.CombineItemsTask;

public class MakePieShellsTask extends Task {

    private final PieMaker pieMaker;
    private final CombineItemsTask combineTask;

    public MakePieShellsTask(PieMaker pieMaker) {
        super(pieMaker);
        this.pieMaker = pieMaker;
        this.combineTask = new CombineItemsTask(
            pieMaker, Constants.PASTRY_DOUGH, Constants.PIE_DISH, Constants.PIE_SHELL,
            "MakePieShells", pieMaker::increasePieShellsMade
        );
    }

    @Override
    public boolean activate() {
        if (pieMaker.stage != Stage.MAKE_SHELL) return false;
        return combineTask.activate();
    }

    @Override
    public void execute() {
        pieMaker.task = "Making pie shells";
        combineTask.execute();
    }
}
