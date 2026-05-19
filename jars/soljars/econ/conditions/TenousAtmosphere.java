package soljars.econ.conditions;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;

import soljars.econ.utils.DistanceCheck;
import soljars.econ.utils.DistanceConditionManager;
import soljars.econ.utils.OrbitRulerHelper;

import java.awt.Color;
// Ill be back when I innevitably start handling tempurature
/*
 * Display condition: the body's atmosphere is currently sublimed but is not a
 * stable steady-state - either seasonal eccentricity will carry it back into
 * frost, or the warming source could be removed. Pure display; all transitions
 * are driven by DistanceConditionManager.
 */
public class TenousAtmosphere extends BaseHazardCondition {

    public static final String ID = "sol_tenous_atmosphere";

    @Override
    public void apply(String id) {
        super.apply(id);
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

        boolean seasonal = OrbitRulerHelper.hasSeasonalCrossing(market);
        boolean lampHeld = DistanceConditionManager.hasFusionLamp(market);
        int atmLevel = market.getMemoryWithoutUpdate().getInt(
                DistanceConditionManager.MEM_ATM_LEVEL);
        int observed = DistanceConditionManager.observedLevel(market);
        boolean partial = observed < atmLevel;

        if (partial) {
            // Body is mid-transition - also wearing FrozenAtmosphere.
            float au = DistanceCheck.getMarketAU(market);
            boolean freezingOut = au > DistanceConditionManager.SUBLIMATION_AU && !lampHeld;
            if (freezingOut) {
                tooltip.addPara(
                        "The thin envelope above the surface is itself unstable - month "
                        + "by month, more of it settles onto the ice below.",
                        pad);
            } else {
                tooltip.addPara(
                        "What atmosphere there is continues to thicken from the polar ice "
                        + "month by month, climbing toward equilibrium.",
                        pad);
            }
        }

        if (seasonal) { // this only shows for those that actually get close enough (or far enough)
            if (lampHeld) {
                tooltip.addPara(
                        "Eccentricity would normally carry this world back past the %s AU "
                        + "sublimation threshold and refreeze the atmosphere, but the "
                        + "%s holds it sublimed till the volatiles run dry.",
                        pad, h, "40", "fusion lamp");
            } else {
                tooltip.addPara(
                        market.getName() + "'s atmosphere is not forever - as the orbit carries "
                        + "this world back past the %s nitrogen frost line, it will "
                        + "recondense onto the surface if not interrupted by a %s.",
                        pad, h, "40 AU", "fusion lamp");
            }
            OrbitRulerHelper.renderSeasonalRuler(tooltip, market, pad);
        } else { // KEEP YOUR FUSION LAMPS ON
            tooltip.addPara(
                    market.getName() + "'s atmosphere is not forever - should the body lose its "
                    + "%s, it will recondense oncemore onto the surface.",
                    pad, h, "fusion lamp");
        }
    }
}