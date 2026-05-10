package soljars.econ.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import soljars.econ.utils.IndustryCompat;
import soljars.econ.utils.OrbitRulerHelper;

public class DistTartarean extends BaseHazardCondition {

    public static final String ID = "sol_dist_tartarean";
    public static final float UPKEEP_MULT = 3.0f;

    @Override
    public void apply(String id) {
        super.apply(id);
        String desc = condition.getName();

        Industry milInd = IndustryCompat.getMilitary(market);
        Industry waystation = IndustryCompat.getWaystation(market);

        boolean hasMilitary = (milInd != null && milInd.isFunctional());
        boolean hasWaystation = (waystation != null && waystation.isFunctional());

        int stabilityMod = -4;
        float accessibilityMod = -1.00f; 
        float hazardMod = 1.00f;         

        if (hasMilitary) {
            stabilityMod += 3;
            accessibilityMod += 0.45f;
            hazardMod -= 0.50f;
            milInd.getUpkeep().modifyMult(id, UPKEEP_MULT, desc);
        }
        if (hasWaystation) {
            stabilityMod += 1;
            accessibilityMod += 0.45f;
            hazardMod -= 0.50f;
            waystation.getUpkeep().modifyMult(id, UPKEEP_MULT, desc);
        }

        if (stabilityMod != 0) {
            market.getStability().modifyFlat(id, stabilityMod, desc);
        }
        if (Math.abs(accessibilityMod) > 0.001f) {
            market.getAccessibilityMod().modifyFlat(id, accessibilityMod, desc);
        }
        if (Math.abs(hazardMod) > 0.001f) {
            market.getHazard().modifyFlat(id, hazardMod, desc);
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        market.getStability().unmodifyFlat(id);
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
        boolean hasNeither = !hasMilitary && !hasWaystation;
        boolean isColonized = !market.isPlanetConditionMarketOnly();

        java.awt.Color flavorColor = hasNeither
                ? Misc.getNegativeHighlightColor()
                : Misc.getHighlightColor();

        // ---------- Flavor ----------
        if (!isColonized) {
            tooltip.addPara("The negative effects of this isolation can be counteracted by a %s and a %s.",
                    pad, flavorColor, "Patrol HQ", "Waystation");
        } else if (market.isPlayerOwned()) {
            if (hasMilitary && hasWaystation) {
                tooltip.addPara("By some miracle, %s receives intermittent deliveries by skittish "
                        + "captains with wary-eyed crews.",
                        pad, flavorColor, market.getName());
            } else if (hasMilitary) {
                tooltip.addPara("Patrol craft drift in the black, conserving fuel. The Warden reports "
                        + "that %s, the trade lanes are effectively dead.",
                        pad, flavorColor, "without a Waystation to refuel incoming convoys");
            } else if (hasWaystation) {
                tooltip.addPara("The Waystation offers respite, but %s, the disappearances have "
                        + "frightened off every independent captain.",
                        pad, flavorColor, "without Patrols to scour the darkness");
            } else {
                tooltip.addPara("It has been a very long time since a merchant dared to deliver to %s, "
                        + "and even longer still since a merchant has arrived.",
                        pad, flavorColor, market.getName());
            }
        } else {
            // Non-player: TBD pending flavor decisions
            if (hasMilitary && hasWaystation) {
                tooltip.addPara("The negative effect of distance is %s by local infrastructure.",
                        pad, flavorColor, "mitigated");
            } else if (hasMilitary) {
                tooltip.addPara("Conditions on %s are utterly devastated by a lack of a %s.",
                        pad, flavorColor, market.getName(), "Waystation");
            } else if (hasWaystation) {
                tooltip.addPara("Conditions on %s are utterly devastated by a lack of a %s.",
                        pad, flavorColor, market.getName(), "Patrol HQ");
            } else {
                tooltip.addPara("Conditions on %s are utterly devastated by a lack of a %s and a %s.",
                        pad, flavorColor, market.getName(), "Patrol HQ", "Waystation");
            }
        }

        // ---------- Effects ----------
        if (isColonized) {
            int stabilityMod = -4;
            float accessibilityMod = -100f;
            float hazardMod = 100f;

            if (hasMilitary) {
                stabilityMod += 3;
                accessibilityMod += 45f;
                hazardMod -= 50f;
            }
            if (hasWaystation) {
                stabilityMod += 1;
                accessibilityMod += 45f;
                hazardMod -= 50f;
            }

            tooltip.addSectionHeading("Tartarean Effects", Alignment.MID, pad);

            if (stabilityMod != 0) {
                tooltip.addPara("%s stability", pad, flavorColor,
                        (stabilityMod > 0 ? "+" : "") + stabilityMod);
            }
            if (Math.abs(accessibilityMod) > 0.1f) {
                tooltip.addPara("%s accessibility", pad, flavorColor,
                        (accessibilityMod > 0 ? "+" : "") + (int) accessibilityMod + "%");
            }
            if (Math.abs(hazardMod) > 0.1f) {
                tooltip.addPara("%s hazard rating", pad, flavorColor,
                        (hazardMod > 0 ? "+" : "") + (int) hazardMod + "%");
            }

            if (hasMilitary || hasWaystation) {
                String milName = (milInd != null) ? milInd.getCurrentName() : "Patrol HQ";
                tooltip.addPara("3x upkeep for " + milName + " and Waystation.",
                        pad, flavorColor, "3x");
            }
        } else {
            tooltip.addSectionHeading("Tartarean Effects", Alignment.MID, pad);
            tooltip.addPara("%s stability", pad, flavorColor, "-4");
            tooltip.addPara("%s accessibility", pad, flavorColor, "-100%");
            tooltip.addPara("%s hazard rating", pad, flavorColor, "+100%");
            tooltip.addPara("3x upkeep for Patrol HQ and Waystation.",
                    pad, flavorColor, "3x");
        }

        OrbitRulerHelper.render(tooltip, market, 10f);
    }
}