package com.druscripts.piemaker.tasks.processing;

import com.druscripts.piemaker.PieMaker;
import com.druscripts.piemaker.data.Constants;
import com.druscripts.piemaker.data.Stage;
import com.druscripts.utils.script.Task;
import com.druscripts.utils.production.CombineItemsTask;

public class MakePastryDoughTask extends Task {

    private final PieMaker pieMaker;
    private final CombineItemsTask combineTask;

    public MakePastryDoughTask(PieMaker pieMaker, int waterSourceId) {
        super(pieMaker);
        this.pieMaker = pieMaker;
        this.combineTask = new CombineItemsTask(
            pieMaker, waterSourceId, Constants.FLOUR, Constants.PASTRY_DOUGH,
            "MakePastryDough", pieMaker::increaseItemsMade
        );
    }

    @Override
    public boolean activate() {
        if (pieMaker.stage != Stage.MAKE_DOUGH) return false;
        return combineTask.activate();
    }

    @Override
    public void execute() {
        pieMaker.task = "Making pastry dough";
        combineTask.execute();
    }
}
