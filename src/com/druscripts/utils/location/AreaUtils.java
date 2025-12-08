package com.druscripts.utils.location;

import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;

public class AreaUtils {

    public static boolean isInArea(WorldPosition pos, RectangleArea area) {
        if (pos == null || area == null) return false;
        int px = pos.getX();
        int py = pos.getY();
        int pz = pos.getPlane();
        int ax = area.getX();
        int ay = area.getY();
        int width = area.getWidth();
        int height = area.getHeight();
        int plane = area.getPlane();
        return pz == plane && px >= ax && px < ax + width && py >= ay && py < ay + height;
    }

    public static boolean isInAnyArea(WorldPosition pos, RectangleArea... areas) {
        if (pos == null || areas == null) return false;
        for (RectangleArea area : areas) {
            if (isInArea(pos, area)) return true;
        }
        return false;
    }
}
