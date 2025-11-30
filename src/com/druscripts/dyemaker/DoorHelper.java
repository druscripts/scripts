package com.druscripts.dyemaker;

import com.druscripts.dyemaker.data.Constants;
import com.osmb.api.input.MenuEntry;
import com.osmb.api.scene.RSObject;
import com.osmb.api.scene.RSTile;
import com.osmb.api.shape.Polygon;

import java.util.List;

public class DoorHelper {

    private final DyeMaker dm;

    public DoorHelper(DyeMaker script) {
        this.dm = script;
    }

    public boolean openDoor() {
        RSTile outsideTile = dm.getSceneManager().getTile(Constants.AGGIE_SHOP_OUTSIDE);
        if (outsideTile != null) {
            Polygon tilePoly = outsideTile.getTileCube(0);
            if (tilePoly != null) {
                Polygon scaledPoly = tilePoly.getResized(0.8);
                if (scaledPoly != null && tryDoorInteraction(scaledPoly)) {
                    return true;
                }
            }
        }

        RSObject door = findNearestDoor();
        if (door != null) {
            Polygon doorPoly = door.getConvexHull();
            if (doorPoly != null && tryDoorInteraction(doorPoly)) {
                return true;
            }
        }

        return false;
    }

    private boolean tryDoorInteraction(Polygon poly) {
        final String[] foundAction = {null};

        dm.getFinger().tap(poly, entries -> {
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
            dm.pollFramesHuman(() -> false, 800, false);
            return true;
        }

        return false;
    }

    private RSObject findNearestDoor() {
        List<RSObject> doors = dm.getObjectManager().getObjects(obj ->
            obj.getName() != null && obj.getName().equalsIgnoreCase("Door")
        );
        return doors.isEmpty() ? null : (RSObject) dm.getUtils().getClosest(doors);
    }
}
