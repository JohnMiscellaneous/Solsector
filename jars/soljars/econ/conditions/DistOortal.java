package soljars.econ.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import soljars.econ.utils.IndustryCompat;
import soljars.econ.utils.OrbitRulerHelper;

public class DistOortal extends BaseHazardCondition {

    public static final String ID = "sol_dist_oortal";
    public static final float UPKEEP_MULT = 5f;
    public static final int LARGE_SIZE_THRESHOLD = 4;

    @Override
    public void apply(String id) {
        super.apply(id);
        String desc = condition.getName();

        Industry milInd = IndustryCompat.getMilitary(market);
        Industry waystation = IndustryCompat.getWaystation(market);

        boolean hasMilitary = (milInd != null && milInd.isFunctional());
        boolean hasWaystation = (waystation != null && waystation.isFunctional());
        boolean isLarge = market.getSize() >= LARGE_SIZE_THRESHOLD;

        // Base penalties — always applied.
        int stabilityMod = -5;
        float accessibilityMod = -1.50f;
        float hazardMod = 1.50f;

        if (hasWaystation) {
            stabilityMod += 2;
            accessibilityMod += 0.70f;
            hazardMod -= 0.40f;
            waystation.getUpkeep().modifyMult(id, UPKEEP_MULT, desc);
        }
        if (hasMilitary) {
            stabilityMod += 2;
            accessibilityMod += 0.40f;
            hazardMod -= 0.70f;
            milInd.getUpkeep().modifyMult(id, UPKEEP_MULT, desc);
        }
        if (isLarge) {
            stabilityMod += 1;
            accessibilityMod += 0.20f;
            hazardMod -= 0.40f;
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
        boolean isLarge = market.getSize() >= LARGE_SIZE_THRESHOLD;
        boolean isColonized = !market.isPlanetConditionMarketOnly();

        // not colonized
        // Player - Ai branch
        // nothing flavor
        // generic text depending on military, waystation and size
        // all 3 text 
        boolean hasAllThree = hasMilitary && hasWaystation && isLarge;
        java.awt.Color flavorColor = hasAllThree
                ? Misc.getHighlightColor()
                : Misc.getNegativeHighlightColor();

 if (!isColonized) {
            tooltip.addPara(
                    "Should a madman settle here, the negative effects of being 'ad ultima thule' "
                    + "can be counteracted by a Patrol HQ, a Waystation, and a population of meaningful size.",
                    pad, flavorColor,
                    "Patrol HQ", "Waystation", "size");

        } else if (market.isPlayerOwned()) {
            // ----- Player branches -----
            if (!hasMilitary && !hasWaystation && !isLarge) {
                tooltip.addPara("The acting warden of %s will not last longer than his allegedly quite "
                        + "appetising predecessor without a %s and a %s.",
                        pad, flavorColor,
                        market.getName(), "Waystation", "Patrol HQ");

            } else if (hasMilitary && !hasWaystation && !isLarge) {
                tooltip.addPara(
                        "Life on " + market.getName() + " is incredibly short, and it is made all the shorter by a lack of a Waystation and size.",
                        pad, flavorColor,
                        market.getName(), "a Waystation", "size");

            } else if (!hasMilitary && hasWaystation && !isLarge) {
                tooltip.addPara(
                        "Life on " + market.getName() + " is incredibly short, and it is made all the shorter by a lack of a Patrol HQ and size.",
                        pad, flavorColor,
                        market.getName(), "a Patrol HQ", "size");

            } else if (!hasMilitary && !hasWaystation && isLarge) {
                tooltip.addPara(
                        "Life on " + market.getName() + " is incredibly short, and it is made all the shorter by a lack of a Patrol HQ and a Waystation.",
                        pad, flavorColor,
                        market.getName(), "a Patrol HQ", "a Waystation");

            } else if (hasMilitary && hasWaystation && !isLarge) {
                tooltip.addPara(                        
                        "Life on " + market.getName() + " is incredibly short, and it is made all the shorter by a lack of size.",
                        pad, flavorColor,
                        market.getName(), "size");

            } else if (hasMilitary && !hasWaystation && isLarge) {
                tooltip.addPara(
                        "Life on " + market.getName() + " is incredibly short, and it is made all the shorter by a lack of a Waystation.",
                        pad, flavorColor,
                        market.getName(), "a Waystation");

            } else if (!hasMilitary && hasWaystation && isLarge) {
                tooltip.addPara(
                        "Life on " + market.getName() + " is incredibly short, and it is made all the shorter by a lack of a Patrol HQ.",
                        pad, flavorColor,
                        market.getName(), "a Patrol HQ");

            } else { // hasAllThree
                tooltip.addPara("Against all reason, %s endures. The infrastructure holds, the convoys "
                        + "arrive, and the void merely watches.",
                        pad, flavorColor, market.getName());
            }

        } else {
            if (!hasMilitary && !hasWaystation && !isLarge) {
                tooltip.addPara("Conditions on %s are utterly devastated by a lack of a %s, a %s, and %s.",
                        pad, flavorColor,
                        market.getName(), "Patrol HQ", "Waystation", "size");

            } else if (hasMilitary && !hasWaystation && !isLarge) {
                tooltip.addPara(
                        "Life on " + market.getName() + " is incredibly short, and it is made all the shorter by a lack of a Waystation and size.",
                        pad, flavorColor,
                        market.getName(), "a Waystation", "size");

            } else if (!hasMilitary && hasWaystation && !isLarge) {
                tooltip.addPara(
                        "Life on " + market.getName() + " is incredibly short, and it is made all the shorter by a lack of a Patrol HQ and size.",
                        pad, flavorColor,
                        market.getName(), "a Patrol HQ", "size");

            } else if (!hasMilitary && !hasWaystation && isLarge) {
                tooltip.addPara(
                        "Life on " + market.getName() + " is incredibly short, and it is made all the shorter by a lack of a Patrol HQ and a Waystation.",
                        pad, flavorColor,
                        market.getName(), "a Patrol HQ", "a Waystation");

            } else if (hasMilitary && hasWaystation && !isLarge) {
                tooltip.addPara(
                        "Life on " + market.getName() + " is incredibly short, and it is made all the shorter by a lack of size.",
                        pad, flavorColor,
                        market.getName(), "size");

            } else if (hasMilitary && !hasWaystation && isLarge) {
                tooltip.addPara(
                        "Life on " + market.getName() + " is incredibly short, and it is made all the shorter by a lack of a Waystation.",
                        pad, flavorColor,
                        market.getName(), "a Waystation");

            } else if (!hasMilitary && hasWaystation && isLarge) {
                tooltip.addPara(
                        "Life on " + market.getName() + " is incredibly short, and it is made all the shorter by a lack of a Patrol HQ.",
                        pad, flavorColor,
                        market.getName(), "a Patrol HQ");

            } else { // hasAllThree
                tooltip.addPara("The negative effect of isolation is %s by local infrastructure.",
                        pad, flavorColor, "partially mitigated");
            }
        }

        // ---------- Effects ----------
        if (isColonized) { // colonized calc
            int stabilityMod = -5;
            float accessibilityMod = -150f;
            float hazardMod = 150f;

            if (hasWaystation) {
                stabilityMod += 2;
                accessibilityMod += 70f;
                hazardMod -= 40f;
            }
            if (hasMilitary) {
                stabilityMod += 2;
                accessibilityMod += 40f;
                hazardMod -= 70f;
            }
            if (isLarge) {
                stabilityMod += 1;
                accessibilityMod += 20f;
                hazardMod -= 40f;
            }

            tooltip.addSectionHeading("Oortal Effects", Alignment.MID, pad);

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
                tooltip.addPara("5x upkeep for " + milName + " and Waystation.",
                        pad, flavorColor, "5x");
            }
        } else {
            tooltip.addSectionHeading("Oortal Effects", Alignment.MID, pad);
            tooltip.addPara("%s stability", pad, flavorColor, "-5");
            tooltip.addPara("%s accessibility", pad, flavorColor, "-150%");
            tooltip.addPara("%s hazard rating", pad, flavorColor, "+150%");
            tooltip.addPara("5x upkeep for Patrol HQ and Waystation.",
                    pad, flavorColor, "5x");
        } // uncolonized nums

        OrbitRulerHelper.render(tooltip, market, 10f);
    }
}