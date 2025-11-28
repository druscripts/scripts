package com.druscripts.dyemaker.tasks;

import com.druscripts.utils.FreeScript;
import com.druscripts.utils.Task;
import com.druscripts.dyemaker.Constants;
import com.druscripts.dyemaker.DyeMaker;
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

    public MakeDyeTask(FreeScript script) {
        super(script);
    }

    @Override
    public boolean activate() {
        DyeMaker dm = (DyeMaker) script;
        WorldPosition pos = script.getWorldPosition();
        return pos != null && Constants.isInAggieShop(pos) && dm.hasMaterials();
    }

    @Override
    public boolean execute() {
        DyeMaker.task = "Making dye";
        Constants.DyeType dyeType = DyeMaker.selectedDyeType;

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(
            Set.of(dyeType.getIngredientId(), dyeType.getDyeId())
        );
        if (inv == null) return false;

        int dyesBefore = inv.contains(dyeType.getDyeId()) ? inv.getAmount(new int[]{dyeType.getDyeId()}) : 0;

        if (!selectIngredient(inv, dyeType)) return false;
        if (!clickOnAggie()) return false;

        boolean dialogueAppeared = script.pollFramesHuman(() ->
            script.getWidgetManager().getDialogue().getDialogueType() == DialogueType.ITEM_OPTION, 3000, true);
        if (!dialogueAppeared) return false;

        script.pollFramesHuman(() -> false, 600, false);

        boolean selected = script.getWidgetManager().getDialogue().selectItem(dyeType.getDyeId());
        if (!selected) {
            selected = script.getWidgetManager().getDialogue().selectItem(new int[]{dyeType.getDyeId()});
        }
        if (!selected) return false;

        script.pollFramesHuman(() -> false, 1200, false);

        // Wait for all ingredients to be used
        script.pollFramesHuman(() -> {
            ItemGroupResult currentInv = script.getWidgetManager().getInventory().search(Set.of(dyeType.getIngredientId()));
            return currentInv == null || !currentInv.contains(dyeType.getIngredientId());
        }, 60000, true);

        // Count results
        ItemGroupResult afterInv = script.getWidgetManager().getInventory().search(Set.of(dyeType.getDyeId()));
        if (afterInv != null && afterInv.contains(dyeType.getDyeId())) {
            int made = afterInv.getAmount(new int[]{dyeType.getDyeId()}) - dyesBefore;
            if (made > 0) {
                DyeMaker.dyesMade += made;
                script.sendStat(Constants.STAT_DYE_MADE, made);
            }
        }

        return false;
    }

    private boolean selectIngredient(ItemGroupResult inv, Constants.DyeType dyeType) {
        if (inv.getSelectedSlot() != null) {
            script.getWidgetManager().getInventory().unSelectItemIfSelected();
            script.pollFramesHuman(() -> {
                ItemGroupResult check = script.getWidgetManager().getInventory().search(Set.of(dyeType.getIngredientId()));
                return check != null && check.getSelectedSlot() == null;
            }, 1000, true);
        }

        ItemSearchResult item = inv.getItem(new int[]{dyeType.getIngredientId()});
        if (item == null) return false;

        Rectangle bounds = item.getBounds();
        if (bounds == null) return false;

        boolean clicked = script.getFinger().tap(bounds, entries -> {
            if (entries == null || entries.isEmpty()) return null;
            for (MenuEntry entry : entries) {
                if ("Use".equalsIgnoreCase(entry.getAction())) return entry;
            }
            return null;
        });
        if (!clicked) return false;

        return script.pollFramesHuman(() -> {
            ItemGroupResult check = script.getWidgetManager().getInventory().search(Set.of(dyeType.getIngredientId()));
            return check != null && check.getSelectedSlot() != null;
        }, 2000, true);
    }

    private boolean clickOnAggie() {
        UIResultList<WorldPosition> npcPositions = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcPositions == null || npcPositions.isEmpty()) return false;

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || !Constants.isInAggieShop(npcPos)) continue;

            RSTile tile = script.getSceneManager().getTile(npcPos);
            if (tile == null || !tile.isOnGameScreen()) continue;

            Polygon tileCube = tile.getTileCube(150);
            if (tileCube == null) continue;

            boolean success = script.getFinger().tap(tileCube, entries -> {
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
