package com.druscripts.dyemaker.tasks;

import com.druscripts.utils.script.FreeScript;
import com.druscripts.utils.tasks.FreeTask;
import com.druscripts.dyemaker.data.Constants;
import com.druscripts.dyemaker.DoorHelper;
import com.druscripts.dyemaker.DyeMaker;
import com.osmb.api.location.position.types.WorldPosition;

public class WalkToBankTask extends FreeTask {

    private final DyeMaker dm;
    private final DoorHelper doorHelper;

    public WalkToBankTask(FreeScript script) {
        super(script);
        dm = (DyeMaker) script;
        this.doorHelper = new DoorHelper(script);
    }

    @Override
    public boolean activate() {
        if (dm.selectedDyeType == null) return false;
        WorldPosition pos = script.getWorldPosition();
        if (pos == null || dm.isInBankArea(pos)) return false;
        return dm.hasDyes() || !dm.hasMaterials();
    }

    @Override
    public boolean execute() {
        dm.task = "Walking to bank";

        script.getWidgetManager().getInventory().unSelectItemIfSelected();

        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;

        if (dm.isInAggieShop(pos)) {
            if (!dm.isAtPosition(pos, Constants.AGGIE_DOOR)) {
                if (!dm.walkToTile(Constants.AGGIE_DOOR)) return false;
            }
            if (!doorHelper.openDoor()) return false;
            if (!dm.walkToTile(Constants.AGGIE_SHOP_OUTSIDE)) return false;
        }

        script.getWalker().walkTo(dm.getRandomBankTile(), dm.walkConfig);
        script.pollFramesHuman(() -> dm.isInBankArea(script.getWorldPosition()), 15000, true);
        return false;
    }
}
