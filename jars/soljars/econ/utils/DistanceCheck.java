package soljars.econ.utils;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import soljars.gen.utils.AstroCalc;
import org.lwjgl.util.vector.Vector2f;

public class DistanceCheck {

    public static float getMarketAU(MarketAPI market) {
        if (market == null) return 0f;
        SectorEntityToken primary = market.getPrimaryEntity();
        if (primary == null) return 0f;

        Vector2f loc = primary.getLocation();
        if (loc == null) return 0f;

        float dist = (float) Math.sqrt(loc.x * loc.x + loc.y * loc.y);

        AstroCalc calc = new AstroCalc();
        return calc.getAU(dist);
    }
}