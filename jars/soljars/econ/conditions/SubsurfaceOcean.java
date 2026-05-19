package soljars.econ.conditions;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.campaign.PlanetAPI;
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
    private static final Set<String> VALID_PLANETS = new HashSet<String>(Arrays.asList(
            "frozen",
            "cryovolcanic",
            "rocky_ice"
    ));

    // if this and has subsurface aquaponics, add farmland
    private static final Set<String> FARMLAND_CONDITIONS = new HashSet<String>(Arrays.asList(
            "farmland_poor",
            "farmland_adequate",
            "farmland_bountiful",
            "farmland_rich"
    ));

    @Override
    public void apply(String id) {
        super.apply(id);

        PlanetAPI planet = market.getPlanetEntity();
        if (planet == null) return;

        // Still on an icy world - condition stays.
        if (VALID_PLANETS.contains(planet.getTypeId())) return;

        // Terraformed off an icy world - pick a replacement industry based on
        // what the market now supports, then drop the condition.
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

    @Override
    public void unapply(String id) {
        super.unapply(id);
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