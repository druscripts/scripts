package com.druscripts.dyemaker.tasks;

import com.druscripts.utils.script.FreeScript;
import com.druscripts.utils.script.Task;
import com.druscripts.dyemaker.data.Constants;
import com.druscripts.dyemaker.DoorHelper;
import com.druscripts.dyemaker.DyeMaker;
import com.osmb.api.location.position.types.WorldPosition;

public class WalkToAggieTask extends Task {

    private final DyeMaker dm;
    private final DoorHelper doorHelper;

    public WalkToAggieTask(FreeScript script) {
        super(script);
        dm = (DyeMaker) script;
        this.doorHelper = new DoorHelper(dm);
    }

    @Override
    public boolean activate() {
        if (!dm.hasMaterials()) return false;

        WorldPosition pos = dm.getWorldPosition();
        return pos != null && !dm.isInAggieShop(pos);
    }

    @Override
    public void execute() {
        dm.task = "Walking to Aggie";

        if (dm.getWidgetManager().getBank().isVisible()) {
            dm.getWidgetManager().getBank().close();
            dm.pollFramesHuman(() -> !dm.getWidgetManager().getBank().isVisible(), 2000);
        }

        dm.getWidgetManager().getInventory().unSelectItemIfSelected();

        WorldPosition pos = dm.getWorldPosition();
        if (pos == null) return;

        if (!dm.isAtPosition(pos, Constants.AGGIE_SHOP_OUTSIDE)) {
            dm.getWalker().walkTo(Constants.AGGIE_SHOP_OUTSIDE, dm.walkConfig);
            dm.pollFramesHuman(() -> dm.isAtPosition(dm.getWorldPosition(), Constants.AGGIE_SHOP_OUTSIDE), 15000, true);
        }

        if (!doorHelper.openDoor()) return;

        dm.walkToTile(Constants.AGGIE_DOOR);
    }
}
