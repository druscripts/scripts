package com.druscripts.piemaker.tasks.processing;

import com.druscripts.piemaker.PieMaker;
import com.druscripts.piemaker.data.Constants;
import com.druscripts.piemaker.data.Stage;
import com.druscripts.utils.production.CombineItemsTask;
import com.druscripts.utils.script.Task;

public class MakeUncookedPieTask extends Task {

    private final PieMaker pieMaker;
    private final CombineItemsTask combineTask;

    public MakeUncookedPieTask(PieMaker script) {
        super(script);
        this.pieMaker = script;
        this.combineTask = new CombineItemsTask(
            script, pieMaker.pieType.getIngredientId(), Constants.PIE_SHELL, pieMaker.pieType.getUncookedId(),
            "MakeUncookedPie", pieMaker::increaseItemsMade
        );
    }

    @Override
    public boolean activate() {
        if (pieMaker.stage != Stage.MAKE_UNCOOKED) return false;
        return combineTask.activate();
    }

    @Override
    public void execute() {
        pieMaker.task = "Adding ingredients to pies";
        combineTask.execute();
    }
}
