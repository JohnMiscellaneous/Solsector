package soljars.econ.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import soljars.econ.utils.IndustryCompat;
import soljars.econ.utils.OrbitRulerHelper;

public class DistDistant extends BaseHazardCondition {

    public static final String ID = "sol_dist_distant";
    public static final float UPKEEP_MULT = 1.1f;

    @Override
    public void apply(String id) {
        super.apply(id);
        String desc = condition.getName();

        Industry milInd = IndustryCompat.getMilitary(market);
        Industry waystation = IndustryCompat.getWaystation(market);

        boolean hasMilitary = (milInd != null && milInd.isFunctional());
        boolean hasWaystation = (waystation != null && waystation.isFunctional());

        // Either Patrol HQ or Waystation fully offsets the penalties.
        boolean offset = hasMilitary || hasWaystation;

        if (!offset) {
            market.getAccessibilityMod().modifyFlat(id, -0.05f, desc);
            market.getHazard().modifyFlat(id, 0.05f, desc);
        }

        if (hasMilitary) {
            milInd.getUpkeep().modifyMult(id, UPKEEP_MULT, desc);
        }
        if (hasWaystation) {
            waystation.getUpkeep().modifyMult(id, UPKEEP_MULT, desc);
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        market.getAccessibilityMod().unmodifyFlat(id);
        market.getHazard().unmodifyFlat(id);

        Industry milInd = IndustryCompat.getMilitary(market);
        if (milInd != null) {
            milInd.getUpkeep().unmodifyMult(id);
        }

        Industry waystation = IndustryCompat.getWaystation(market);
        if (waystation != null) {
            waystation.getUpkeep().unmodifyMult(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;

        Industry milInd = IndustryCompat.getMilitary(market);
        Industry waystation = IndustryCompat.getWaystation(market);

        boolean hasMilitary = (milInd != null && milInd.isFunctional());
        boolean hasWaystation = (waystation != null && waystation.isFunctional());
        boolean offset = hasMilitary || hasWaystation;

        boolean isColonized = !market.isPlanetConditionMarketOnly();

        if (!isColonized) {
            tooltip.addPara("The negative effects of this isolation can be counteracted by either a %s or a %s.",
                    pad, Misc.getHighlightColor(), "Patrol HQ", "Waystation");

            tooltip.addSectionHeading("Distant Effects", Alignment.MID, pad);

            tooltip.addPara("%s accessibility", pad, Misc.getHighlightColor(), "-5%");
            tooltip.addPara("%s hazard rating", pad, Misc.getHighlightColor(), "+5%");
            tooltip.addPara("%s upkeep for Patrol HQ and Waystation.", pad, Misc.getHighlightColor(), "1.1x");
        } else if (market.isPlayerOwned()) {
            if (offset) {
                tooltip.addPara("The existence of local facilities %s any harm %s might have received.",
                        pad, Misc.getHighlightColor(), "offsets", market.getName());
            } else {
                tooltip.addPara("The lack of a Patrol HQ or a Waystation combined with its isolation makes supplying %s %s.",
                        pad, Misc.getHighlightColor(), market.getName(), "inconvenient");
            }
        } else {
            if (offset) {
                tooltip.addPara("The negative effect of distance is %s by local infrastructure.",
                        pad, Misc.getHighlightColor(), "mitigated");
            } else {
                tooltip.addPara("Conditions on %s are impaired by a lack of a %s or a %s.",
                        pad, Misc.getHighlightColor(), market.getName(), "Patrol HQ", "Waystation");
            }
        }

        if (isColonized) {
            float accessibilityMod = offset ? 0f : -5f;
            float hazardMod = offset ? 0f : 5f;

            if (Math.abs(accessibilityMod) > 0.1f
                    || Math.abs(hazardMod) > 0.1f
                    || hasMilitary
                    || hasWaystation) {
                tooltip.addSectionHeading("Distant Effects", Alignment.MID, pad);
            }

            if (Math.abs(accessibilityMod) > 0.1f) {
                tooltip.addPara("%s accessibility", pad, Misc.getHighlightColor(),
                        (int) accessibilityMod + "%");
            }
            if (Math.abs(hazardMod) > 0.1f) {
                tooltip.addPara("%s hazard rating", pad, Misc.getHighlightColor(),
                        "+" + (int) hazardMod + "%");
            }

            if (hasMilitary || hasWaystation) {
                String milName = (milInd != null) ? milInd.getCurrentName() : "Patrol HQ";
                tooltip.addPara("1.1x upkeep for " + milName + " and Waystation.",
                        pad, Misc.getHighlightColor(), "1.1x");
            }
        }

        OrbitRulerHelper.render(tooltip, market, 10f);
    }
}