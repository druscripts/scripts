package com.druscripts.piemaker.tasks.processing;

import com.druscripts.piemaker.PieMaker;
import com.druscripts.piemaker.data.Constants;
import com.druscripts.utils.script.Task;
import com.druscripts.utils.production.CombineItemsTask;
import com.druscripts.utils.production.CombineItemsConfig;

public class MakePastryDoughTask extends Task {

    private final CombineItemsTask combineTask;

    public MakePastryDoughTask(PieMaker pieMaker, int waterSourceId) {
        super(pieMaker);

        CombineItemsConfig config = new CombineItemsConfig(
            waterSourceId, Constants.FLOUR, Constants.PASTRY_DOUGH,
            "MakePastryDough", pieMaker::increaseItemsMade
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
