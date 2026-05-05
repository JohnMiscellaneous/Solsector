package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
// I WILL KILL FOR JOE BIDEN
// I WOULD DIE FOR JOE BIDEN
// WE ARE JOE BIDEN
// AJISFSIAEBTEIASBFISANWSJFBIQASLAFJKBSALFKSDIPFGBDSDBSL
public class pre_domain_sapience extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        unapplyAll(id);

        int size = market.getSize();

        if (size < 3) return;

        // SIZE 3 
        if (size == 3) {
            market.getStability().modifyFlat(id, -3, "Viral sleepyness");
            market.getHazard().modifyFlat(id, 0.25f, "Bidenomics");
        }
        
        // SIZE 4
        else if (size == 4) {
            market.getStability().modifyFlat(id, -2, "Viral sleepyness");
            market.getHazard().modifyFlat(id, 0.50f, "Gamma core bubble");
        }
        
        // SIZE 5
        else if (size == 5) {
            market.getStability().modifyFlat(id, 1, "The joy of forgetting");
            market.getAccessibilityMod().modifyFlat(id, -0.10f, "Chips act");
            market.getHazard().modifyFlat(id, -0.15f, "Build back better");
        }
        
        // SIZE 6+
        else if (size >= 6) {
            market.getStability().modifyFlat(id, 3, "The joy of forgetting");
            market.getAccessibilityMod().modifyFlat(id, 0.25f, "Beacon on a hill");
            market.getHazard().modifyFlat(id, -0.25f, "Bioweapon vaccination program");

            Industry pop = market.getIndustry("population");
            if (pop != null) {
                pop.getSupply("drugs").getQuantity().modifyFlat(id, 1, "The hunter");
            }

            Industry heavy = sol_industry_compat.getHeavyIndustry(market);
            
            if (heavy != null) {
                heavy.getSupply("hand_weapons").getQuantity().modifyFlat(id, 2, "Reafirmed commitment to NATO");
            }
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        unapplyAll(id);
    }

    private void unapplyAll(String id) {
        market.getStability().unmodifyFlat(id);
        market.getHazard().unmodifyFlat(id);
        market.getAccessibilityMod().unmodifyFlat(id);

        Industry pop = market.getIndustry("population");
        if (pop != null) {
            pop.getSupply("drugs").getQuantity().unmodifyFlat(id);
        }

        Industry heavy = sol_industry_compat.getHeavyIndustry(market);
        if (heavy != null) {
            heavy.getSupply("hand_weapons").getQuantity().unmodifyFlat(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
    super.createTooltipAfterDescription(tooltip, expanded); 

    float pad = 10f;

    if (market.isPlanetConditionMarketOnly() || !market.isPlayerOwned()) {
        tooltip.addPara("Your officers recommend %s.",
            pad, Misc.getPositiveHighlightColor(), "erecting a shrine to his glorius majesty, the Pre Domain Sapience");
        return;
    }

    int size = market.getSize();
    if (size < 3) return;

    if (size == 3) {
        tooltip.addPara("%s stability", pad, Misc.getHighlightColor(), "-3");
        tooltip.addPara("%s hazard rating", pad, Misc.getHighlightColor(), "+25%");
    } else if (size == 4) {
        tooltip.addPara("%s stability", pad, Misc.getHighlightColor(), "-2");
        tooltip.addPara("%s hazard rating", pad, Misc.getHighlightColor(), "+50%");
    } else if (size == 5) {
        tooltip.addPara("%s stability", pad, Misc.getHighlightColor(), "+1");
        tooltip.addPara("%s accessibility", pad, Misc.getHighlightColor(), "-10%");
        tooltip.addPara("%s hazard rating", pad, Misc.getHighlightColor(), "-15%");
    } else if (size >= 6) {
        tooltip.addPara("%s stability", pad, Misc.getHighlightColor(), "+3");
        tooltip.addPara("%s accessibility", pad, Misc.getHighlightColor(), "+25%");
        tooltip.addPara("%s hazard rating", pad, Misc.getHighlightColor(), "-25%");
        tooltip.addPara("%s drugs production (Population)", pad, Misc.getHighlightColor(), "+1");
        tooltip.addPara("%s Heavy Armaments production (Heavy Industry)", pad, Misc.getHighlightColor(), "+2");
    }
}
}