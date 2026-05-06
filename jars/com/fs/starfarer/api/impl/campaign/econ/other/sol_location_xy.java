package com.fs.starfarer.api.impl.campaign.econ.other;

import com.fs.starfarer.api.Global;

import org.lwjgl.util.vector.Vector2f;
import org.widehorizons.api.WideHorizonsAPI;

public class sol_location_xy {

    public static float[] getScaledCoords(float vanillaX, float vanillaY) {
        if (!Global.getSettings().getModManager().isModEnabled("WideHorizons")) {
            return null;
        }
        Vector2f pos = WideHorizonsAPI.getScaledPosition(vanillaX, vanillaY);
        if (pos == null) return null;

        Global.getLogger(sol_location_xy.class).info(
            "sol_location_xy: WH scaled (" + vanillaX + ", " + vanillaY + ") -> ("
                + pos.x + ", " + pos.y + ")");
        return new float[] { pos.x, pos.y };
    }
}