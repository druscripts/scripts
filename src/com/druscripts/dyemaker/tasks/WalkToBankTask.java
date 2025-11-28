package com.druscripts.dyemaker.tasks;

import com.druscripts.utils.FreeScript;
import com.druscripts.utils.Task;
import com.druscripts.dyemaker.Constants;
import com.druscripts.dyemaker.DoorHelper;
import com.druscripts.dyemaker.DyeMaker;
import com.osmb.api.location.position.types.WorldPosition;

public class WalkToBankTask extends Task {

    private final DoorHelper doorHelper;

    public WalkToBankTask(FreeScript script) {
        super(script);
        this.doorHelper = new DoorHelper(script);
    }

    @Override
    public boolean activate() {
        DyeMaker dm = (DyeMaker) script;
        if (DyeMaker.selectedDyeType == null) return false;
        WorldPosition pos = script.getWorldPosition();
        if (pos == null || Constants.isInBankArea(pos)) return false;
        return dm.hasDyes() || !dm.hasMaterials();
    }

    @Override
    public boolean execute() {
        DyeMaker dm = (DyeMaker) script;
        DyeMaker.task = "Walking to bank";

        script.getWidgetManager().getInventory().unSelectItemIfSelected();

        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;

        if (Constants.isInAggieShop(pos)) {
            if (!dm.isAtPosition(pos, Constants.AGGIE_DOOR)) {
                if (!dm.walkToTile(Constants.AGGIE_DOOR)) return false;
            }
            if (!doorHelper.openDoor()) return false;
            if (!dm.walkToTile(Constants.AGGIE_SHOP_OUTSIDE)) return false;
        }

        script.getWalker().walkTo(Constants.DRAYNOR_BANK_CENTER, dm.walkConfig);
        script.pollFramesHuman(() -> Constants.isInBankArea(script.getWorldPosition()), 15000, true);
        return false;
    }
}
