package soljars.compat.widehorizons;

import com.fs.starfarer.api.Global;

import org.lwjgl.util.vector.Vector2f;
import org.widehorizons.api.WideHorizonsAPI;

public class LocationXY {

    public static float[] getScaledCoords(float vanillaX, float vanillaY) {
        if (!(Global.getSettings().getModManager().isModEnabled("WideHorizons") || Global.getSettings().getModManager().isModEnabled("WideHorizonsBasic")) ) {
            return null;
        }
        Vector2f pos = WideHorizonsAPI.getScaledPosition(vanillaX, vanillaY);
        if (pos == null) return null;

        Global.getLogger(LocationXY.class).info(
            "LocationXY: WH scaled (" + vanillaX + ", " + vanillaY + ") -> ("
                + pos.x + ", " + pos.y + ")");
        return new float[] { pos.x, pos.y };
    }
}