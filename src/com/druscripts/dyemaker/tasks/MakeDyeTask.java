package com.druscripts.dyemaker.tasks;

import com.druscripts.utils.FreeScript;
import com.druscripts.utils.Task;
import com.druscripts.dyemaker.Constants;
import com.druscripts.dyemaker.DyeMaker;
import com.druscripts.dyemaker.DyeType;
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
        return pos != null && dm.isInAggieShop(pos) && dm.hasMaterials();
    }

    @Override
    public boolean execute() {
        DyeMaker dm = (DyeMaker) script;
        DyeMaker.task = "Making dye";
        DyeType dyeType = DyeMaker.selectedDyeType;

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

        boolean selected = script.getWidgetManager().getDialogue().selectItem(dyeType.getDyeId());
        if (!selected) return false;

        ItemGroupResult[] lastInv = {null};
        boolean success = script.pollFramesHuman(() -> {
            lastInv[0] = script.getWidgetManager().getInventory().search(Set.of(dyeType.getIngredientId(), dyeType.getDyeId()));
            return lastInv[0] == null || !lastInv[0].contains(dyeType.getIngredientId());
        }, 3000);

        if (success) {
            int made = lastInv[0].getAmount(new int[]{dyeType.getDyeId()}) - dyesBefore;
            if (made > 0) {
                DyeMaker.dyesMade += made;
                dm.sendStat(Constants.STAT_DYE_MADE, made);
            }
        }

        return false;
    }

    private boolean selectIngredient(ItemGroupResult inv, DyeType dyeType) {
        ItemSearchResult item = inv.getItem(new int[]{dyeType.getIngredientId()});
        if (item == null) return false;

        // Early exit if ingredient is already selected
        Integer selectedSlot = inv.getSelectedSlot();
        if (selectedSlot != null && selectedSlot == item.getSlot()) {
            return true;
        }

        // Unselect if something else is selected
        if (selectedSlot != null) {
            script.getWidgetManager().getInventory().unSelectItemIfSelected();
            script.pollFramesHuman(() -> {
                ItemGroupResult check = script.getWidgetManager().getInventory().search(Set.of(dyeType.getIngredientId()));
                return check != null && check.getSelectedSlot() == null;
            }, 1000, true);
        }

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
        DyeMaker dm = (DyeMaker) script;
        UIResultList<WorldPosition> npcPositions = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcPositions == null || npcPositions.isEmpty()) return false;

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || !dm.isInAggieShop(npcPos)) continue;

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
