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

        CombineItemsConfig config = new CombineItemsConfig.Builder()
            .primaryItem(Constants.PASTRY_DOUGH)
            .secondaryItem(Constants.PIE_DISH)
            .resultItem(Constants.PIE_SHELL)
            .taskDescription("Making pie shells")
            .combiningDescription("Using pastry dough on pie dish")
            .craftingDescription("Crafting pie shells")
            .logClassName("MakePieShellsTask")
            .onItemsCrafted(pieMaker::increaseItemsMade)
            .build();

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
