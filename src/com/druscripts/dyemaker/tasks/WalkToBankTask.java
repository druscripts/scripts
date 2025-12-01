package com.druscripts.dyemaker.tasks;

import com.druscripts.utils.script.FreeScript;
import com.druscripts.utils.script.Task;
import com.druscripts.dyemaker.data.Constants;
import com.druscripts.dyemaker.DoorHelper;
import com.druscripts.dyemaker.DyeMaker;
import com.osmb.api.location.position.types.WorldPosition;

public class WalkToBankTask extends Task {

    private final DyeMaker dm;
    private final DoorHelper doorHelper;

    public WalkToBankTask(FreeScript script) {
        super(script);
        dm = (DyeMaker) script;
        this.doorHelper = new DoorHelper(dm);
    }

    @Override
    public boolean activate() {
        if (dm.selectedDyeType == null) return false;

        WorldPosition pos = dm.getWorldPosition();
        if (pos == null || dm.isInBankArea(pos)) return false;

        return dm.hasDyes() || !dm.hasMaterials();
    }

    @Override
    public void execute() {
        dm.task = "Walking to bank";

        dm.getWidgetManager().getInventory().unSelectItemIfSelected();

        WorldPosition pos = dm.getWorldPosition();
        if (pos == null) return;

        if (dm.isInAggieShop(pos)) {
            if (!dm.isAtPosition(pos, Constants.AGGIE_DOOR)) {
                if (!dm.walkToTile(Constants.AGGIE_DOOR)) return;
            }
            if (!doorHelper.openDoor()) return;
            if (!dm.walkToTile(Constants.AGGIE_SHOP_OUTSIDE)) return;
        }

        dm.getWalker().walkTo(dm.getRandomBankTile(), dm.walkConfig);
        dm.pollFramesHuman(() -> dm.isInBankArea(dm.getWorldPosition()), 15000, true);
    }
}
