package com.druscripts.piemaker.tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.walker.WalkConfig;
import com.druscripts.piemaker.PieMaker;
import com.druscripts.piemaker.data.Constants;
import com.druscripts.piemaker.data.Stage;
import com.druscripts.utils.location.AreaUtils;
import com.druscripts.utils.widget.InventoryUtils;
import com.druscripts.utils.widget.exception.CannotOpenWidgetException;
import com.druscripts.utils.script.Task;

import java.util.Arrays;
import java.util.List;

public class TravelToRangeTask extends Task {

    private final PieMaker pieMaker;

    public TravelToRangeTask(PieMaker script) {
        super(script);
        this.pieMaker = script;
    }

    @Override
    public boolean activate() {
        if (pieMaker.stage != Stage.COOK) {
            return false;
        }

        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;

        boolean notInKitchen = !AreaUtils.isInArea(myPos, Constants.RANGE_AREA);
        boolean hasUncookedPies;

        try {
            hasUncookedPies = InventoryUtils.hasItem(script, pieMaker.pieType.getUncookedId());
        } catch (CannotOpenWidgetException e) {
            script.log(getClass(), e.getMessage());
            return false;
        }

        return notInKitchen && hasUncookedPies;
    }

    @Override
    public void execute() {
        pieMaker.task = "Travel to range";
        script.log(getClass(), "Traveling to range...");

        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return;

        if (AreaUtils.isInArea(myPos, Constants.RANGE_AREA)) {
            script.log(getClass(), "Already at range");
            return;
        }

        if (myPos.getPlane() == 2) {
            if (!AreaUtils.isInArea(myPos, Constants.STAIRS_AREA_FLOOR_2)) {
                script.log(getClass(), "Walking to stairs");
                WalkConfig.Builder cfg = new WalkConfig.Builder();
                cfg.enableRun(true);
                script.getWalker().walkTo(Constants.STAIRS_AREA_FLOOR_2.getRandomPosition(), cfg.build());
                script.pollFramesHuman(() -> {
                    WorldPosition p = script.getWorldPosition();
                    return p != null && AreaUtils.isInArea(p, Constants.STAIRS_AREA_FLOOR_2);
                }, 6000, true);
                return;
            }

            RSObject stairs = findStairs("Climb-down");
            if (stairs == null) {
                script.log(getClass(), "No staircase found");
                return;
            }

            // Try bottom-floor option
            String[] actions = stairs.getActions();
            if (actions != null) {
                for (String a : actions) {
                    if (a != null && a.equalsIgnoreCase("Bottom-floor")) {
                        if (stairs.interact(a)) {
                            script.pollFramesHuman(() -> {
                                WorldPosition p = script.getWorldPosition();
                                return p != null && p.getPlane() == 0;
                            }, 6000, true);
                            return;
                        }
                        break;
                    }
                }
            }

            stairs.interact("Climb-down");
            script.pollFramesHuman(() -> {
                WorldPosition p = script.getWorldPosition();
                return p != null && p.getPlane() < 2;
            }, 6000, true);
            return;
        }

        if (myPos.getPlane() == 1) {
            RSObject stairs = findStairs("Climb-down");
            if (stairs != null) {
                stairs.interact("Climb-down");
                script.pollFramesHuman(() -> {
                    WorldPosition p = script.getWorldPosition();
                    return p != null && p.getPlane() == 0;
                }, 6000, true);
            }
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
