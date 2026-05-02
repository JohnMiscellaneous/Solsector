package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class dist_tartarean extends BaseHazardCondition {

    public static final float UPKEEP_MULT = 3.0f;

    @Override
    public void apply(String id) {
        super.apply(id);

        // 1. Get Industries via Compat
        Industry milInd = sol_industry_compat.getMilitary(market);
        Industry waystation = sol_industry_compat.getWaystation(market);
        Industry defInd = sol_industry_compat.getDefense(market);

        boolean hasMilitary = (milInd != null && milInd.isFunctional());
        boolean hasWaystation = (waystation != null && waystation.isFunctional());
        boolean hasDefenses = (defInd != null && defInd.isFunctional());

        // Base Modifiers
        int stabilityMod = -5;
        float accessibilityMod = -1.00f; // -100%
        float hazardMod = 1.00f;        // +100%

        if (hasMilitary) {
            stabilityMod += 3;
            accessibilityMod += 0.45f;
            hazardMod -= 0.50f;
            
            milInd.getUpkeep().modifyMult(id, UPKEEP_MULT, condition.getName());
        }

        if (hasWaystation) {
            accessibilityMod += 0.45f;
            hazardMod -= 0.50f;
            
            waystation.getUpkeep().modifyMult(id, UPKEEP_MULT, condition.getName());
        }
        
        // Defense industry mitigates stability
        if (hasDefenses) {
            stabilityMod += 2;
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
        Industry defInd = sol_industry_compat.getDefense(market);

        boolean hasMilitary = (milInd != null && milInd.isFunctional());
        boolean hasWaystation = (waystation != null && waystation.isFunctional());
        boolean hasDefenses = (defInd != null && defInd.isFunctional());
        
        boolean isColonized = !market.isPlanetConditionMarketOnly();

        if (!isColonized) {
            tooltip.addPara("The negative effects of this isolation can be counteracted by a %s.", 
                pad, Misc.getHighlightColor(), "Patrol HQ, Ground Defenses, and a Waystation");

            tooltip.addSectionHeading("Tartarean Effects", Alignment.MID, pad);
            
            tooltip.addPara("%s stability", pad, Misc.getHighlightColor(), "-5");
            tooltip.addPara("%s accessibility", pad, Misc.getHighlightColor(), "-100%");
            tooltip.addPara("%s hazard rating", pad, Misc.getHighlightColor(), "+100%");

            tooltip.addPara("3x upkeep for Patrol HQ and Waystation.", pad, Misc.getHighlightColor(), "3x");
        } else {
            // Contextual text
            if (hasMilitary && hasWaystation && hasDefenses) {
                tooltip.addPara("By some miracle, %s receives intermittent deliveries by skittish captains with wary eyed crews.", 
                    pad, Misc.getTextColor(), market.getName());
            } else if (!hasMilitary && !hasWaystation && !hasDefenses) {
                tooltip.addPara("The docks are completely empty, bar a locker filled with bones... Bones with bite marks. The Warden, holding a rifle behind at a fallback line, when asked, joked about needing patrols, ground defenses, and a waystation.", 
                    pad, Misc.getNegativeHighlightColor(), "joked about needing patrols, ground defenses, and a waystation");
            } else if (!hasMilitary && !hasWaystation && hasDefenses) {
                tooltip.addPara("The ground batteries track targets in the dark, but the docks are silent. A locker was found filled with bones... Bones with bite marks. The Warden, when asked, joked about needing patrols and a waystation.", 
                    pad, Misc.getNegativeHighlightColor(), "joked about needing patrols and a waystation");
            } else if (hasMilitary && hasWaystation && !hasDefenses) {
                tooltip.addPara("An unknown assailant attacked %s recently and evidence of the battle is everywhere. The Warden, when asked, joked about needing ground defenses.", 
                    pad, Misc.getNegativeHighlightColor(), market.getName(), "joked about needing ground defenses");
            } else if (hasMilitary && !hasWaystation && !hasDefenses) {
                tooltip.addPara("Patrol craft drift in the black, conserving fuel, while something prowls the perimeter. The Warden reports that without a Waystation and Ground Defenses, the colony is a sitting duck.", 
                    pad, Misc.getHighlightColor(), "without a waystation and ground defenses");
            } else if (!hasMilitary && hasWaystation && !hasDefenses) {
                tooltip.addPara("The Waystation offers fuel, but without Patrols or Ground Defenses, the 'screaming on the comms' has frightened off every independent captain.", 
                    pad, Misc.getHighlightColor(), "without Patrols or ground defenses");
            } else if (hasMilitary && !hasWaystation) {
                tooltip.addPara("Patrol craft drift in the black, conserving fuel. The Warden reports that without a Waystation to refuel incoming convoys, the trade lanes are effectively dead.", 
                    pad, Misc.getHighlightColor(), "without a waystation");
            } else if (!hasMilitary && hasWaystation) {
                tooltip.addPara("The Waystation offers respite, but without Patrols to scour the darkness, the disappearances have frightened off every independent captain.", 
                    pad, Misc.getHighlightColor(), "without patrols");
            } else {
                tooltip.addPara("It has been a very long time since a merchant dared to deliver to %s, and even longer still since a merchant has arrived.", 
                    pad, Misc.getHighlightColor(), market.getName());
            }

            int stabilityMod = -5;
            float accessibilityMod = -100f; 
            float hazardMod = 100f;       

            if (hasMilitary) {
                stabilityMod += 3;
                accessibilityMod += 45f;
                hazardMod -= 50f;
            }
            if (hasWaystation) {
                accessibilityMod += 45f;
                hazardMod -= 50f;
            }
            if (hasDefenses) {
                stabilityMod += 2;
            }
            
            if (stabilityMod != 0 || Math.abs(accessibilityMod) > 0.1f || Math.abs(hazardMod) > 0.1f || hasMilitary || hasWaystation) {
                tooltip.addSectionHeading("Tartarean Effects", Alignment.MID, pad);
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
            tooltip.addPara("3x upkeep for " + milName + " and Waystation.", pad, Misc.getHighlightColor(), "3x");
        }
    }
}