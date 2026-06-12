package soljars.econ.conditions;

import java.awt.Color;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import soljars.econ.industries.utils.RemoveReplaceIndustry;
import soljars.econ.utils.RemoveReplace;

// Now with 100% fresh logic
public class SubsurfaceOcean extends BaseHazardCondition {

    public static final String ID = "sol_subsurface_ocean";

    // If not this, freak out
    private static final String[] VALID_PLANETS = {
            "frozen",
            "cryovolcanic",
            "rocky_ice"
    };

    // if this and has subsurface aquaponics, add farmland
    private static final String[] FARMLAND_CONDITIONS = {
            "farmland_poor",
            "farmland_adequate",
            "farmland_bountiful",
            "farmland_rich"
    };

    // transient: not serialized, defaults to false every load
    private transient boolean checkPending = false;

    @Override
    public void apply(String id) {
        super.apply(id);

        if (checkPending) return;
        if (Global.getSector() == null) return;

        checkPending = true;
        Global.getSector().addTransientScript(new TerraformCheck(this, market));
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
    }

    // Runs once, post-load, on the campaign thread - planet is safe to read here.
    private static class TerraformCheck implements EveryFrameScript {
        private final SubsurfaceOcean condition;
        private final MarketAPI market;
        private boolean done = false;

        TerraformCheck(SubsurfaceOcean condition, MarketAPI market) {
            this.condition = condition;
            this.market = market;
        }

        @Override public boolean isDone() { return done; }
        @Override public boolean runWhilePaused() { return true; }

        @Override
        public void advance(float amount) {
            done = true;
            condition.checkPending = false;

            PlanetAPI planet = market.getPlanetEntity();
            if (planet == null) return;

            // Still on an icy world - condition stays.
            String type = planet.getTypeId();
            for (String t : VALID_PLANETS) {
                if (t.equals(type)) return;
            }

            // Terraformed off an icy world - pick replacement, swap, drop condition.
            String replacement = null;
            if (market.hasCondition("water_surface")) {
                replacement = Industries.AQUACULTURE;
            } else {
                for (String f : FARMLAND_CONDITIONS) {
                    if (market.hasCondition(f)) {
                        replacement = Industries.FARMING;
                        break;
                    }
                }
            }

            if (replacement != null && market.hasIndustry(ID)) {
                RemoveReplaceIndustry.execute(market, ID, replacement);
            }
            RemoveReplace.execute(market, ID, null);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();

        tooltip.addPara("Enables construction of %s.", pad, h, "Subsurface Aquaponics");
        tooltip.addPara("%s food production (Subsurface Aquaponics).", pad, h, "-2");
    }
}