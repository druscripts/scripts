package com.druscripts.piemaker.tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.walker.WalkConfig;
import com.druscripts.piemaker.PieMaker;
import com.druscripts.piemaker.data.Constants;
import com.druscripts.utils.location.AreaUtils;
import com.druscripts.utils.widget.InventoryUtils;
import com.druscripts.utils.widget.exception.CannotOpenWidgetException;
import com.druscripts.utils.script.Task;

import java.util.Arrays;
import java.util.List;

public class TravelToBankTask extends Task {

    private final PieMaker pieMaker;

    public TravelToBankTask(PieMaker script) {
        super(script);
        this.pieMaker = script;
    }

    @Override
    public boolean activate() {
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;

        if (script.getWidgetManager().getBank().isVisible()) return false;
        if (AreaUtils.isInArea(myPos, Constants.BANK_AREA)) return false;

        try {
            return !InventoryUtils.hasItem(script, pieMaker.pieType.getUncookedId());
        } catch (CannotOpenWidgetException e) {
            script.log(getClass(), e.getMessage());
            return false;
        }
    }

    @Override
    public void execute() {
        pieMaker.task = "Travel to bank";
        // Stage will be set by BankAndManageStageTask after banking
        script.log(getClass(), "Traveling to bank...");

        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return;

        if (AreaUtils.isInArea(myPos, Constants.BANK_AREA)) {
            script.log(getClass(), "Already at bank");
            return;
        }

        if (myPos.getPlane() == 0) {
            RSObject stairs = findStairs("Climb-up");
            if (stairs == null) {
                script.log(getClass(), "No staircase found");
                return;
            }

            // Try top-floor option
            String[] actions = stairs.getActions();
            if (actions != null) {
                for (String a : actions) {
                    if (a != null && a.equalsIgnoreCase("Top-floor")) {
                        if (stairs.interact(a)) {
                            script.pollFramesHuman(() -> {
                                WorldPosition p = script.getWorldPosition();
                                return p != null && p.getPlane() == 2;
                            }, 6000, true);
                            return;
                        }
                        break;
                    }
                }
            }

            stairs.interact("Climb-up");
            script.pollFramesHuman(() -> {
                WorldPosition p = script.getWorldPosition();
                return p != null && p.getPlane() == 1;
            }, 6000, true);
            return;
        }

        if (myPos.getPlane() == 1) {
            RSObject stairs = findStairs("Climb-up");
            if (stairs != null) {
                stairs.interact("Climb-up");
                script.pollFramesHuman(() -> {
                    WorldPosition p = script.getWorldPosition();
                    return p != null && p.getPlane() == 2;
                }, 6000, true);
            }
            return;
        }

        if (myPos.getPlane() == 2 && !AreaUtils.isInArea(myPos, Constants.BANK_AREA)) {
            script.log(getClass(), "Walking to bank area");
            WalkConfig.Builder cfg = new WalkConfig.Builder();
            cfg.enableRun(true);
            script.getWalker().walkTo(Constants.BANK_AREA.getRandomPosition(), cfg.build());
            script.pollFramesHuman(() -> {
                WorldPosition p = script.getWorldPosition();
                return p != null && AreaUtils.isInArea(p, Constants.BANK_AREA);
            }, 6000, true);
        }
    }

    private RSObject findStairs(String action) {
        List<RSObject> stairs = script.getObjectManager().getObjects(obj -> {
            if (obj.getName() == null || obj.getActions() == null) return false;
            return (obj.getName().equalsIgnoreCase("Staircase") || obj.getName().equalsIgnoreCase("Stairs"))
                && Arrays.stream(obj.getActions()).anyMatch(a -> a != null && a.equalsIgnoreCase(action))
                && obj.canReach();
        });
        return stairs.isEmpty() ? null : (RSObject) script.getUtils().getClosest(stairs);
    }
}
