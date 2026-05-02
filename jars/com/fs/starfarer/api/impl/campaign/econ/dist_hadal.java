package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class dist_hadal extends BaseHazardCondition {

    public static final float UPKEEP_MULT = 1.5f;

    @Override
    public void apply(String id) {
        super.apply(id);

        // 1. Get Industries via Compat
        // getMilitary returns PatrolHQ, Military Base, High Command, or AOTD equivalents
        Industry milInd = sol_industry_compat.getMilitary(market);
        Industry waystation = sol_industry_compat.getWaystation(market);

        boolean hasMilitary = (milInd != null && milInd.isFunctional());
        boolean hasWaystation = (waystation != null && waystation.isFunctional());

        int stabilityMod = -2;
        float accessibilityMod = -0.40f;
        float hazardMod = 0.50f;

        if (hasMilitary) {
            stabilityMod += 2;
            accessibilityMod += 0.20f;
            hazardMod -= 0.25f;
            
            milInd.getUpkeep().modifyMult(id, UPKEEP_MULT, condition.getName());
        }

        if (hasWaystation) {
            accessibilityMod += 0.20f;
            hazardMod -= 0.25f;
            
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

            tooltip.addSectionHeading("Hadal Effects", Alignment.MID, pad);
            
            tooltip.addPara("%s stability", pad, Misc.getHighlightColor(), "-2");
            tooltip.addPara("%s accessibility", pad, Misc.getHighlightColor(), "-40%");
            tooltip.addPara("%s hazard rating", pad, Misc.getHighlightColor(), "+50%");

            tooltip.addPara("1.5x upkeep for Patrol HQ and Waystation.", pad, Misc.getHighlightColor(), "1.5x");
        } else {

            if (hasMilitary && hasWaystation) {
                tooltip.addPara("Supported by comprehensive infrastructure, merchants brave the permanent night to deliver to %s, though crews remain unsettled by the isolation.", 
                    pad, Misc.getTextColor(), market.getName());
            } else if (!hasMilitary && !hasWaystation) {
                tooltip.addPara("The docks are crowded with people stuck lacking the supplies for the return journey, and a merchant ship destined to arrive a month ago has yet to appear. Rumors blame pirates. The governor begs for patrols and a waystation.", 
                    pad, Misc.getNegativeHighlightColor(), "begs for patrols and a waystation");
            } else if (hasMilitary && !hasWaystation) {
                tooltip.addPara("Patrols keep the immediate space clear, but the docks are crowded with people stuck lacking the supplies for the return journey. The governor begs for a waystation.", 
                    pad, Misc.getHighlightColor(), "begs for a waystation");
            } else if (!hasMilitary && hasWaystation) {
                tooltip.addPara("Fuel is available, but a merchant ship destined to arrive a month ago has yet to appear. Rumors blame pirates. The governor begs for patrols.", 
                    pad, Misc.getHighlightColor(), "begs for patrols");
            }

            int stabilityMod = -2;
            float accessibilityMod = -40f; 
            float hazardMod = 50f;       

            if (hasMilitary) {
                stabilityMod += 2;
                accessibilityMod += 20f;
                hazardMod -= 25f;
            }
            if (hasWaystation) {
                accessibilityMod += 20f;
                hazardMod -= 25f;
            }
            
            if (stabilityMod != 0 || Math.abs(accessibilityMod) > 0.1f || Math.abs(hazardMod) > 0.1f || hasMilitary || hasWaystation) {
                tooltip.addSectionHeading("Hadal Effects", Alignment.MID, pad);
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
            tooltip.addPara("1.5x upkeep for " + milName + " and Waystation.", pad, Misc.getHighlightColor(), "1.5x");
        }
    }
}