package com.druscripts.piemaker.tasks;

import com.osmb.api.input.MenuEntry;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
import com.osmb.api.walker.WalkConfig;
import com.druscripts.piemaker.PieMaker;
import com.druscripts.piemaker.data.Constants;
import com.druscripts.piemaker.data.Stage;
import com.druscripts.utils.location.AreaUtils;
import com.druscripts.utils.widget.InventoryUtils;
import com.druscripts.utils.widget.exception.CannotOpenWidgetException;
import com.druscripts.utils.script.Task;

import java.util.*;

public class CookPiesTask extends Task {

    private final PieMaker pieMaker;
    private final Map<Integer, Integer> lastCounts = new HashMap<>();

    public CookPiesTask(PieMaker script) {
        super(script);
        this.pieMaker = script;
    }

    @Override
    public boolean activate() {
        if (!pieMaker.allInOne && pieMaker.stage != Stage.COOK) {
            return false;
        }

        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null || myPos.getPlane() != 0) return false;

        try {
            return InventoryUtils.hasItem(script, pieMaker.pieType.getUncookedId());
        } catch (CannotOpenWidgetException e) {
            script.log(getClass(), e.getMessage());
            return false;
        }
    }

    @Override
    public void execute() {
        pieMaker.task = "Cooking pies";
        pieMaker.stage = Stage.COOK;
        script.log(getClass(), "Cooking pies...");

        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return;

        if (!AreaUtils.isInArea(myPos, Constants.RANGE_AREA)) {
            script.log(getClass(), "Walking to range");
            WalkConfig.Builder cfg = new WalkConfig.Builder();
            cfg.enableRun(true);
            script.getWalker().walkTo(Constants.RANGE_AREA.getRandomPosition(), cfg.build());
            script.pollFramesHuman(() -> {
                WorldPosition p = script.getWorldPosition();
                return p != null && AreaUtils.isInArea(p, Constants.RANGE_AREA);
            }, 10000, true);
            return;
        }

        RSObject range = findRange();
        if (range == null) {
            script.log(getClass(), "Could not find range");
            return;
        }

        int uncookedId = pieMaker.pieType.getUncookedId();
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(uncookedId));
        if (inv == null || !inv.contains(uncookedId)) {
            script.log(getClass(), "No uncooked pies");
            return;
        }

        boolean success = script.random(0, 2) == 0 ? cookOnRange(range) : useOnRange(range, inv, uncookedId);
        if (!success) return;

        script.pollFramesHuman(() ->
            script.getWidgetManager().getDialogue().getDialogueType() == DialogueType.ITEM_OPTION, 6000, true);

        if (script.getWidgetManager().getDialogue().getDialogueType() == DialogueType.ITEM_OPTION) {
            script.log(getClass(), "Selecting pie in dialogue");
            int cookedId = pieMaker.pieType.getCookedId();
            if (!script.getWidgetManager().getDialogue().selectItem(uncookedId) &&
                !script.getWidgetManager().getDialogue().selectItem(cookedId)) {
                script.log(getClass(), "Failed to select pie");
                return;
            }
            waitForCooking();
        }
    }

    private boolean cookOnRange(RSObject range) {
        script.log(getClass(), "Cook on range");
        script.getWidgetManager().getInventory().unSelectItemIfSelected();
        script.pollFramesHuman(() -> false, script.random(200, 400), true);
        return range.interact("cook");
    }

    private boolean useOnRange(RSObject range, ItemGroupResult inv, int uncookedId) {
        script.log(getClass(), "Use item on range");
        if (!inv.getItem(uncookedId).interact()) return false;
        script.pollFramesHuman(() -> false, script.random(300, 600), true);
        return range.interact(entries -> {
            for (MenuEntry e : entries) {
                if (e.getAction() != null && e.getAction().toLowerCase().startsWith("use")) return e;
            }
            return null;
        });
    }

    private void waitForCooking() {
        script.log(getClass(), "Waiting for cooking...");

        int uncookedId = pieMaker.pieType.getUncookedId();
        int cookedId = pieMaker.pieType.getCookedId();

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(uncookedId, cookedId));
        if (inv != null) {
            lastCounts.put(uncookedId, inv.getAmount(uncookedId));
            lastCounts.put(cookedId, inv.getAmount(cookedId));
        }

        script.pollFramesHuman(() -> {
            if (script.getWidgetManager().getDialogue().getDialogueType() == DialogueType.TAP_HERE_TO_CONTINUE) {
                return true;
            }
            updateStats();
            ItemGroupResult cur = script.getWidgetManager().getInventory().search(Set.of(uncookedId));
            return cur == null || !cur.contains(uncookedId);
        }, 120000, true);

        script.log(getClass(), "Cooking complete");
    }

    private void updateStats() {
        int cookedId = pieMaker.pieType.getCookedId();
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(cookedId));
        if (inv == null) return;

        int current = inv.getAmount(cookedId);
        int last = lastCounts.getOrDefault(cookedId, 0);
        int newCooked = current - last;

        if (newCooked > 0) {
            pieMaker.increaseCookedPiesMade(newCooked);
        }
        lastCounts.put(cookedId, current);
    }

    private RSObject findRange() {
        List<RSObject> ranges = script.getObjectManager().getObjects(obj -> {
            if (obj.getActions() == null) return false;
            return Arrays.stream(obj.getActions()).anyMatch(a -> a != null && a.equalsIgnoreCase("Cook")) && obj.canReach();
        });
        return ranges.isEmpty() ? null : (RSObject) script.getUtils().getClosest(ranges);
    }
}
