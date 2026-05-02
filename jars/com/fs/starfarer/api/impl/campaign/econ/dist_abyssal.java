package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

// TODO distance updating 

public class dist_abyssal extends BaseHazardCondition {

    public static final float UPKEEP_MULT = 1.25f;

    @Override
    public void apply(String id) {
        super.apply(id);
        String desc = condition.getName();

        Industry milInd = sol_industry_compat.getMilitary(market);
        Industry waystation = sol_industry_compat.getWaystation(market);

        boolean hasMilitary = (milInd != null && milInd.isFunctional());
        boolean hasWaystation = (waystation != null && waystation.isFunctional());

        int stabilityMod = -1;
        float accessibilityMod = -0.20f;
        float hazardMod = 0.25f;

        if (hasMilitary) {
            stabilityMod += 1;
            accessibilityMod += 0.10f;
            hazardMod -= 0.15f;
            
            milInd.getUpkeep().modifyMult(id, UPKEEP_MULT, desc);
        }

        if (hasWaystation) {
            accessibilityMod += 0.10f;
            hazardMod -= 0.10f;
            
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

            tooltip.addSectionHeading("Abyssal Effects", Alignment.MID, pad);
            
            tooltip.addPara("%s stability", pad, Misc.getHighlightColor(), "-1");
            tooltip.addPara("%s accessibility", pad, Misc.getHighlightColor(), "-20%");
            tooltip.addPara("%s hazard rating", pad, Misc.getHighlightColor(), "+25%");

            tooltip.addPara("1.25x upkeep for Patrol HQ and Waystation.", pad, Misc.getHighlightColor(), "1.25x");
        } else {
            if (hasMilitary && hasWaystation) {
                tooltip.addPara("Despite the depth of the region, merchants treat deliveries to %s like anywhere else and don't take excess precautions.", 
                    pad, Misc.getTextColor(), market.getName());
            } else if (!hasMilitary && !hasWaystation) {
                tooltip.addPara("Merchants complain of unprotected convoys and logistical difficulties. The local administrator requests patrols and a waystation.", 
                    pad, Misc.getNegativeHighlightColor(), "requests patrols and a waystation");
            } else if (hasMilitary && !hasWaystation) {
                tooltip.addPara("Merchants appreciate the convoy protection, but logistical difficulties remain. The local administrator requests a waystation to bridge the gap.", 
                    pad, Misc.getHighlightColor(), "requests a waystation");
            } else if (!hasMilitary && hasWaystation) {
                tooltip.addPara("Logistics are handled well, but merchants complain of unprotected convoys. The local administrator requests patrols to secure the lanes.", 
                    pad, Misc.getHighlightColor(), "requests patrols");
            }

            int stabilityMod = -1;
            float accessibilityMod = -20f; 
            float hazardMod = 25f;       

            if (hasMilitary) {
                stabilityMod += 1;
                accessibilityMod += 10f;
                hazardMod -= 15f;
            }
            if (hasWaystation) {
                accessibilityMod += 10f;
                hazardMod -= 10f;
            }
            
            if (stabilityMod != 0 || Math.abs(accessibilityMod) > 0.1f || Math.abs(hazardMod) > 0.1f || hasMilitary || hasWaystation) {
                tooltip.addSectionHeading("Abyssal Effects", Alignment.MID, pad);
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
            tooltip.addPara("1.25x upkeep for " + milName + " and Waystation.", pad, Misc.getHighlightColor(), "1.25x");
        }
    }
}