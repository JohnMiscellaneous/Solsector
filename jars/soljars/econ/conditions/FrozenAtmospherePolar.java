package soljars.econ.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import soljars.econ.utils.RemoveReplace;

import java.awt.Color;

/**
 * Mars has C02 ice on its caps
 * Triton has nitrogen that fucking dips from the south when the heat turns up // consider
 * Pluto's got 90deg on the axial and when periwinter becomes aposummer its 50AU away and hopelessly cold
 * Eris could have similar shit
 * this is for stuff that gets dislodged and fixed, with triton added cause fuck it.
 */
public class FrozenAtmospherePolar extends BaseHazardCondition implements EconomyTickListener {

    public static final String ID = "sol_frozen_atmosphere_polar";

    public static final String MEM_POLAR_LEVEL = "$sol_polar_atmosphere_level";

    public static final String THIN_ATM    = "thin_atmosphere";
    public static final String DENSE_ATM   = "dense_atmosphere";
    public static final String NO_ATM      = "no_atmosphere";
    public static final String SOLAR_ARRAY = "solar_array";

    // DCM coordination - these must match DistanceConditionManager.
    private static final String MEM_ATM_LEVEL = "$sol_atmosphere_level";
    private static final String[] DEEP_SPACE_BANDS = {
            "sol_dist_abyssal", "sol_dist_hadal", "sol_dist_erebal",
            "sol_dist_tartarean", "sol_dist_oortal"
    };

    @Override
    public void apply(String id) {
        super.apply(id);

        Global.getSector().getListenerManager().removeListener(this);
        Global.getSector().getListenerManager().addListener(this);
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        Global.getSector().getListenerManager().removeListener(this);
    }

    @Override
    public void reportEconomyTick(int iterIndex) {
        // Not used
    }

    // step atmosphere by +1 toward target each month if lamp/array present.
    // For DCM-managed bodies, hand off to DCM by bumping its target level.
    @Override
    public void reportEconomyMonthEnd() {
        if (!market.hasCondition(ID)) {
            Global.getSector().getListenerManager().removeListener(this);
            return;
        }

        if (!hasLampOrArray(market)) return;

        int polarLevel = clamp(market.getMemoryWithoutUpdate().getInt(MEM_POLAR_LEVEL));
        int currentLevel = detectCurrentLevel();

        // already at or above target - no process to announce, just clean up
        if (currentLevel >= polarLevel) {
            RemoveReplace.execute(market, ID, null);
            return;
        }

        // DCM-managed: hand off by raising DCM's target. DCM owns the stepping
        // from now on. Polar's job is done.
        if (market.getMemoryWithoutUpdate().contains(MEM_ATM_LEVEL)) {
            int atmLevel = market.getMemoryWithoutUpdate().getInt(MEM_ATM_LEVEL);
            if (polarLevel > atmLevel) {
                market.getMemoryWithoutUpdate().set(MEM_ATM_LEVEL, polarLevel);
            }
            beginSublimation();
            RemoveReplace.execute(market, ID, null);
            return;
        }

        // Not DCM-managed: step here.
        int nextLevel = currentLevel + 1;
        applyAtmosphereLevel(nextLevel);

        if (nextLevel == polarLevel) {
            completeSublimation(polarLevel);
        }
    }

    private void beginSublimation() {
        if (market.isPlayerOwned()) {
            Global.getSector().getCampaignUI().addMessage(
                "Sublimation of polar ice on " + market.getName() + " has begun.",
                Misc.getPositiveHighlightColor());
        }
    }

    private void completeSublimation(int finalLevel) {
        if (market.isPlayerOwned()) {
            String thickness = finalLevel == 1 ? "thin"
                    : finalLevel == 2 ? "regular"
                    : "dense";
            Global.getSector().getCampaignUI().addMessage(
                "Sublimation of the " + thickness + " atmosphere trapped on the poles of "
                + market.getName() + " has completed.",
                Misc.getPositiveHighlightColor());
        }
        RemoveReplace.execute(market, ID, null);
    }

    // Check for a viable warmth source. Solar arrays are sufficient in the
    // inner system; in deep-space bands (abyssal and beyond) only a fusion
    // lamp provides enough warmth to drive sublimation.
    private static boolean hasLampOrArray(MarketAPI market) {
        if (market == null) return false;

        boolean hasLamp = hasFusionLamp(market);
        if (hasLamp) return true;

        // No lamp - only acceptable if solar array AND not in deep space.
        if (!market.hasCondition(SOLAR_ARRAY)) return false;
        for (String band : DEEP_SPACE_BANDS) {
            if (market.hasCondition(band)) return false;
        }
        return true;
    }

    private static boolean hasFusionLamp(MarketAPI market) {
        Industry pop = market.getIndustry(Industries.POPULATION);
        if (pop == null) return false;
        SpecialItemData special = pop.getSpecialItem();
        if (special == null) return false;
        return Items.ORBITAL_FUSION_LAMP.equals(special.getId());
    }

    // read atmosphere level
    private int detectCurrentLevel() {
        if (market.hasCondition(NO_ATM))    return 0;
        if (market.hasCondition(DENSE_ATM)) return 3;
        if (market.hasCondition(THIN_ATM))  return 1;
        return 2; // lack of condition = normal atmo
    }

    private void applyAtmosphereLevel(int level) {
        if (market.hasCondition(NO_ATM))    market.removeCondition(NO_ATM);
        if (market.hasCondition(THIN_ATM))  market.removeCondition(THIN_ATM);
        if (market.hasCondition(DENSE_ATM)) market.removeCondition(DENSE_ATM);

        if (level == 1) {
            market.addCondition(THIN_ATM);
        } else if (level == 3) {
            market.addCondition(DENSE_ATM);
        }
        // level == 2 (normal) is the absence of thin/dense - nothing to add.
    }

    private static int clamp(int level) {
        if (level < 1) return 1;
        if (level > 3) return 3;
        return level;
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();

        int polarLevel = clamp(market.getMemoryWithoutUpdate().getInt(MEM_POLAR_LEVEL));

        String thickness = polarLevel == 1 ? "thin"
                : polarLevel == 2 ? "regular"
                : "dense";

        tooltip.addPara(
                "Through accretion to polar latitudes, nesting into a high albedo region, "
                + "or settling on the perihelion winter pole, " + market.getName() + " has "
                + "the components of an atmosphere - albeit a frozen one awaiting the hand "
                + "of man to sublimate it. A %s would sublimate this into a %s atmosphere.",
                pad, h, "fusion lamp or solar array", thickness);
    }
}