package soljars.econ.conditions;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;

import soljars.econ.utils.DistanceCheck;
import soljars.econ.utils.DistanceConditionManager;
import soljars.econ.utils.OrbitRulerHelper;

import java.awt.Color;

/**
 * Display condition: the body's atmosphere is currently frozen onto its surface
 * as nitrogen / CO2 / methane ice. Pure display - all state transitions are
 * driven by DistanceConditionManager.
 */
public class FrozenAtmosphere extends BaseHazardCondition {

    public static final String ID = "sol_frozen_atmosphere";

    @Override
    public void apply(String id) {
        super.apply(id);
        // No mechanical effects of our own; hazard, accessibility, etc. come from
        // the distance bands (tartarean/erebal/etc) this world already sits in.
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();
        int level = market.getMemoryWithoutUpdate().getInt(
                DistanceConditionManager.MEM_ATM_LEVEL);
        int observed = DistanceConditionManager.observedLevel(market);

        String thickness = level == 3 ? "dense"
                : level == 2 ? "regular"
                : "thin";

        if (observed == 0) {
            tooltip.addPara("%s", pad, h, "Cannot hold atmosphere");
            tooltip.addPara(
                    "The components of a %s atmosphere lie spread across the surface, "
                    + "awaiting sublimation under the light of a %s or %s.",
                    pad, h, thickness, "star", "fusion lamp");
        } else {
            float au = DistanceCheck.getMarketAU(market);
            boolean lampWarm = DistanceConditionManager.hasFusionLamp(market);
            boolean freezingOut = au > DistanceConditionManager.SUBLIMATION_AU && !lampWarm;
            if (freezingOut) {
                tooltip.addPara(
                        "Most of the body's %s atmosphere has condensed onto the surface; "
                        + "the remainder is freezing out month by month.",
                        pad, h, thickness);
                        
            } else {
                tooltip.addPara(
                        "The remainder of the body's %s atmosphere is still locked in surface "
                        + "ice, sublimating month by month into the thickening envelope above.",
                        pad, h, thickness);
            }
        }

        boolean seasonal = OrbitRulerHelper.hasSeasonalCrossing(market);
        if (seasonal) {
            float au = DistanceCheck.getMarketAU(market);
            String side = au > DistanceConditionManager.SUBLIMATION_AU ? "Aphelic winter" : "Perihelic summer";
            tooltip.addPara(
                    "Eccentricity carries this world across the %s nitrogen frost line; "
                    + "it is currently in its long %s.",
                    pad, h, "40", side);
            OrbitRulerHelper.renderSeasonalRuler(tooltip, market, pad);
        }
    }
}