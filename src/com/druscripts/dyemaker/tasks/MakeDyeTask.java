package com.druscripts.dyemaker.tasks;

import com.druscripts.utils.script.FreeScript;
import com.druscripts.utils.script.Task;
import com.druscripts.dyemaker.DyeMaker;
import com.druscripts.dyemaker.data.DyeType;
import com.osmb.api.input.MenuEntry;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSTile;
import com.osmb.api.shape.Polygon;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
import com.osmb.api.utils.UIResultList;

import java.util.Set;

public class MakeDyeTask extends Task {

    private final DyeMaker dm;

    public MakeDyeTask(FreeScript script) {
        super(script);
        dm = (DyeMaker) script;
    }

    @Override
    public boolean activate() {
        WorldPosition pos = dm.getWorldPosition();
        return pos != null && dm.isInAggieShop(pos) && dm.hasMaterials();
    }

    @Override
    public void execute() {
        dm.task = "Making dye";
        DyeType dyeType = dm.selectedDyeType;

        ItemGroupResult inv = dm.getWidgetManager().getInventory().search(Set.of(dyeType.getIngredientId()));
        if (inv == null) return;
        int batches = inv.getAmount(dyeType.getIngredientId()) / dyeType.getIngredientCount();

        if (!selectIngredient(dyeType)) return;
        if (!clickOnAggie()) return;

        boolean dialogAppeared = dm.pollFramesHuman(() ->
            dm.getWidgetManager().getDialogue().getDialogueType() == DialogueType.ITEM_OPTION, 3000, true);
        if (!dialogAppeared) return;

        boolean selected = dm.getWidgetManager().getDialogue().selectItem(dyeType.getDyeId());
        if (!selected) return;

        boolean success = dm.pollFramesHuman(() -> !dm.hasMaterials(), 3000);
        if (success) {
            dm.dyesMade += batches;

            if (dm.firstRoundComplete) {
                long lapTimeMs = System.currentTimeMillis() - dm.lapStartTime;
                dm.sendStat(dyeType.getStatName(), batches);
                dm.sendStat(dyeType.getLapTimeStatName(), lapTimeMs);
            }

            dm.firstRoundComplete = true;
            dm.lapStartTime = System.currentTimeMillis();
        }
    }

    private boolean selectIngredient(DyeType dyeType) {
        ItemGroupResult inv = dm.getWidgetManager().getInventory().search(Set.of(dyeType.getIngredientId()));
        if (inv == null) return false;

        Integer selectedSlot = inv.getSelectedSlot();
        if (selectedSlot != null) {
            return true;
        }

        ItemSearchResult item = inv.getItem(dyeType.getIngredientId());
        if (item == null) return false;

        Rectangle bounds = item.getBounds();
        if (bounds == null) return false;

        boolean clicked = dm.getFinger().tap(bounds, entries -> {
            if (entries == null || entries.isEmpty()) return null;
            for (MenuEntry entry : entries) {
                if ("Use".equalsIgnoreCase(entry.getAction())) return entry;
            }
            return null;
        });
        if (!clicked) return false;

        return dm.pollFramesHuman(() -> {
            ItemGroupResult check = dm.getWidgetManager().getInventory().search(Set.of(dyeType.getIngredientId()));
            return check != null && check.getSelectedSlot() != null;
        }, 2000, true);
    }

    private boolean clickOnAggie() {
        UIResultList<WorldPosition> npcPositions = dm.getWidgetManager().getMinimap().getNPCPositions();
        if (npcPositions == null || npcPositions.isEmpty()) return false;

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || !dm.isInAggieShop(npcPos)) continue;

            RSTile tile = dm.getSceneManager().getTile(npcPos);
            if (tile == null || !tile.isOnGameScreen()) continue;

            Polygon tileCube = tile.getTileCube(150);
            if (tileCube == null) continue;

            boolean success = dm.getFinger().tap(tileCube, entries -> {
                if (entries == null || entries.isEmpty()) return null;
                for (MenuEntry entry : entries) {
                    String raw = entry.getRawText();
                    if (raw != null && raw.toLowerCase().startsWith("use") && raw.toLowerCase().contains("aggie")) {
                        return entry;
                    }
                }
                return null;
            });

            if (success) return true;
        }
        return false;
    }
}
