package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class dist_erebal extends BaseHazardCondition {

    public static final float UPKEEP_MULT = 2.0f;

    @Override
    public void apply(String id) {
        super.apply(id);

        // 1. Get Industries via Compat
        Industry milInd = sol_industry_compat.getMilitary(market);
        Industry waystation = sol_industry_compat.getWaystation(market);

        boolean hasMilitary = (milInd != null && milInd.isFunctional());
        boolean hasWaystation = (waystation != null && waystation.isFunctional());

        int stabilityMod = -3;
        float accessibilityMod = -0.60f;
        float hazardMod = 0.75f;

        if (hasMilitary) {
            stabilityMod += 3;
            accessibilityMod += 0.30f;
            hazardMod -= 0.40f; // Military handles 40%
            
            milInd.getUpkeep().modifyMult(id, UPKEEP_MULT, condition.getName());
        }

        if (hasWaystation) {
            accessibilityMod += 0.30f;
            hazardMod -= 0.35f; // Waystation handles 35% (Total 75%)
            
            waystation.getUpkeep().modifyMult(id, UPKEEP_MULT, condition.getName());
        }

        if (stabilityMod != 0) {
            market.getStability().modifyFlat(id, stabilityMod, condition.getName());
        }
        
        if (Math.abs(accessibilityMod) > 0.001f) {
            market.getAccessibilityMod().modifyFlat(id, accessibilityMod, condition.getName());
        }
        
        if (Math.abs(hazardMod) > 0.001f) {
            market.getHazard().modifyFlat(id, hazardMod, condition.getName());
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        market.getStability().unmodifyFlat(id);
        market.getAccessibilityMod().unmodifyFlat(id);
        market.getHazard().unmodifyFlat(id);

        Industry milInd = sol_industry_compat.getMilitary(market);
        if (milInd != null) {
            milInd.getUpkeep().unmodifyMult(id);
        }

        Industry waystation = sol_industry_compat.getWaystation(market);
        if (waystation != null) {
            waystation.getUpkeep().unmodifyMult(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        
        Industry milInd = sol_industry_compat.getMilitary(market);
        Industry waystation = sol_industry_compat.getWaystation(market);

        boolean hasMilitary = (milInd != null && milInd.isFunctional());
        boolean hasWaystation = (waystation != null && waystation.isFunctional());
        
        boolean isColonized = !market.isPlanetConditionMarketOnly();

        if (!isColonized) {
            tooltip.addPara("The negative effects of this isolation can be counteracted by a %s.", 
                pad, Misc.getHighlightColor(), "Patrol HQ and a Waystation");

            tooltip.addSectionHeading("Erebal Effects", Alignment.MID, pad);
            
            tooltip.addPara("%s stability", pad, Misc.getHighlightColor(), "-3");
            tooltip.addPara("%s accessibility", pad, Misc.getHighlightColor(), "-60%");
            tooltip.addPara("%s hazard rating", pad, Misc.getHighlightColor(), "+75%");

            tooltip.addPara("2x upkeep for Patrol HQ and Waystation.", pad, Misc.getHighlightColor(), "2x");
        } else {
            // Contextual text based on what is built
            if (hasMilitary && hasWaystation) {
                tooltip.addPara("In spite of the suffocating depth and darkness, merchants deliver to %s with some unease, but in a relatively standard manner.", 
                    pad, Misc.getTextColor(), market.getName());
            } else if (!hasMilitary && !hasWaystation) {
                tooltip.addPara("The docks are crammed with the dying, be it food, prosthetics, or medicines the logistical strain is too much. Several ships in a row have failed to arrive and nobody bothers speculating. The military governor of the installation begs for patrols and a waystation.", 
                    pad, Misc.getNegativeHighlightColor(), "begs for patrols and a waystation");
            } else if (hasMilitary && !hasWaystation) {
                tooltip.addPara("The docks are crammed with the dying. Food, prosthetics, and medicines are all in critical shortage. The military governor begs for a waystation.", 
                    pad, Misc.getHighlightColor(), "begs for a waystation");
            } else if (!hasMilitary && hasWaystation) {
                tooltip.addPara("Supplies are stockpiled, but several ships in a row have failed to arrive. Nobody bothers speculating on their fate. The military governor begs for patrols.", 
                    pad, Misc.getHighlightColor(), "begs for patrols");
            }

            int stabilityMod = -3;
            float accessibilityMod = -60f; 
            float hazardMod = 75f;       

            if (hasMilitary) {
                stabilityMod += 3;
                accessibilityMod += 30f;
                hazardMod -= 40f;
            }
            if (hasWaystation) {
                accessibilityMod += 30f;
                hazardMod -= 35f;
            }
            
            if (stabilityMod != 0 || Math.abs(accessibilityMod) > 0.1f || Math.abs(hazardMod) > 0.1f || hasMilitary || hasWaystation) {
                tooltip.addSectionHeading("Erebal Effects", Alignment.MID, pad);
            }

            if (stabilityMod != 0) {
                tooltip.addPara("%s stability", pad, Misc.getHighlightColor(), "" + stabilityMod);
            }

            if (Math.abs(accessibilityMod) > 0.1f) {
                tooltip.addPara("%s accessibility", pad, Misc.getHighlightColor(), "" + (int)accessibilityMod + "%");
            }

            if (Math.abs(hazardMod) > 0.1f) {
                tooltip.addPara("%s hazard rating", pad, Misc.getHighlightColor(), "+" + (int)hazardMod + "%");
            }

            String milName = (milInd != null) ? milInd.getCurrentName() : "Patrol HQ";
            tooltip.addPara("2x upkeep for " + milName + " and Waystation.", pad, Misc.getHighlightColor(), "2x");
        }
    }
}