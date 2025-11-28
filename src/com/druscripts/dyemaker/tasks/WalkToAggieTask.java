package com.druscripts.dyemaker.tasks;

import com.druscripts.utils.FreeScript;
import com.druscripts.utils.Task;
import com.druscripts.dyemaker.Constants;
import com.druscripts.dyemaker.DoorHelper;
import com.druscripts.dyemaker.DyeMaker;
import com.osmb.api.location.position.types.WorldPosition;

public class WalkToAggieTask extends Task {

    private final DoorHelper doorHelper;

    public WalkToAggieTask(FreeScript script) {
        super(script);
        this.doorHelper = new DoorHelper(script);
    }

    @Override
    public boolean activate() {
        DyeMaker dm = (DyeMaker) script;
        if (!dm.hasMaterials()) return false;
        WorldPosition pos = script.getWorldPosition();
        return pos != null && !Constants.isInAggieShop(pos);
    }

    @Override
    public boolean execute() {
        DyeMaker dm = (DyeMaker) script;
        DyeMaker.task = "Walking to Aggie";

        if (script.getWidgetManager().getBank().isVisible()) {
            script.getWidgetManager().getBank().close();
            script.pollFramesHuman(() -> !script.getWidgetManager().getBank().isVisible(), 2000, true);
        }

        script.getWidgetManager().getInventory().unSelectItemIfSelected();

        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;

        if (!dm.isAtPosition(pos, Constants.AGGIE_SHOP_OUTSIDE)) {
            script.getWalker().walkTo(Constants.AGGIE_SHOP_OUTSIDE, dm.walkConfig);
            script.pollFramesHuman(() -> dm.isAtPosition(script.getWorldPosition(), Constants.AGGIE_SHOP_OUTSIDE), 15000, true);
        }

        if (!doorHelper.openDoor()) return false;

        dm.walkToTile(Constants.AGGIE_DOOR);
        return false;
    }
}
