package com.druscripts.dyemaker;

import com.druscripts.utils.FreeScript;
import com.osmb.api.input.MenuEntry;
import com.osmb.api.scene.RSObject;
import com.osmb.api.scene.RSTile;
import com.osmb.api.shape.Polygon;

import java.util.List;

public class DoorHelper {

    private final FreeScript script;

    public DoorHelper(FreeScript script) {
        this.script = script;
    }

    public boolean openDoor() {
        RSObject door = findNearestDoor();
        if (door != null) {
            Polygon doorPoly = door.getConvexHull();
            if (doorPoly != null && tryDoorInteraction(doorPoly)) {
                return true;
            }
        }

        RSTile outsideTile = script.getSceneManager().getTile(Constants.AGGIE_SHOP_OUTSIDE);
        if (outsideTile != null) {
            Polygon tilePoly = outsideTile.getTileCube(0);
            if (tilePoly != null) {
                Polygon scaledPoly = tilePoly.getResized(0.8);
                if (scaledPoly != null && tryDoorInteraction(scaledPoly)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean tryDoorInteraction(Polygon poly) {
        final String[] foundAction = {null};

        script.getFinger().tap(poly, entries -> {
            if (entries == null || entries.isEmpty()) return null;

            for (MenuEntry entry : entries) {
                String action = entry.getAction();
                if (action == null) continue;

                String lower = action.toLowerCase();
                if (lower.startsWith("open")) {
                    foundAction[0] = "open";
                    return entry;
                } else if (lower.startsWith("close")) {
                    foundAction[0] = "close";
                    return null;
                }
            }
            return null;
        });

        if ("close".equals(foundAction[0])) {
            return true;
        } else if ("open".equals(foundAction[0])) {
            script.pollFramesHuman(() -> false, 800, false);
            return true;
        }

        return false;
    }

    private RSObject findNearestDoor() {
        List<RSObject> doors = script.getObjectManager().getObjects(obj ->
            obj.getName() != null && obj.getName().equalsIgnoreCase("Door")
        );
        return doors.isEmpty() ? null : (RSObject) script.getUtils().getClosest(doors);
    }
}
