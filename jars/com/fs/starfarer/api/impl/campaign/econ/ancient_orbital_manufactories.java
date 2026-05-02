package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ancient_orbital_manufactories extends BaseHazardCondition {


    @Override
    public void apply(String id) {

        super.apply(id);

        String desc = condition.getName();

        int size = market.getSize();
        
        // retrieve indusrtry
        // consider? depreciating because tag can do this pretty much perfectly
        Industry heavyInd = sol_industry_compat.getHeavyIndustry(market);
        Industry lightInd = sol_industry_compat.getLightIndustry(market);

        // check for industry
        boolean hasOrbitalWorks = (heavyInd != null && !heavyInd.getId().equals(Industries.HEAVYINDUSTRY));
        boolean hasLightIndustry = (lightInd != null);

        // Define Values based on Size
        int supplies = (size >= 5) ? 3 : 2;
        int machinery = (size >= 5) ? 3 : 2;
        int metalsDem = (size >= 5) ? 2 : 1;
        int rareMetalsDem = 1; 
        
        int domesticGoods = (size >= 5) ? 3 : 2;

        unapply(id);

        // Apply Orbital Works Effects
        if (hasOrbitalWorks) {
            heavyInd.getSupply(Commodities.SUPPLIES).getQuantity().modifyFlat(id, supplies, desc);
            heavyInd.getSupply(Commodities.HEAVY_MACHINERY).getQuantity().modifyFlat(id, machinery, desc);
            
            heavyInd.getDemand(Commodities.METALS).getQuantity().modifyFlat(id + "_metals", metalsDem, desc);
            heavyInd.getDemand(Commodities.RARE_METALS).getQuantity().modifyFlat(id + "_rare_metals", rareMetalsDem, desc);
            
            heavyInd.getUpkeep().modifyMult(id, 1.3f, desc);
        }


        if (hasLightIndustry) {
            lightInd.getSupply(Commodities.DOMESTIC_GOODS).getQuantity().modifyFlat(id, domesticGoods, desc);
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        
        Industry heavyInd = sol_industry_compat.getHeavyIndustry(market);
        if (heavyInd != null) {
            heavyInd.getSupply(Commodities.SUPPLIES).getQuantity().unmodifyFlat(id);
            heavyInd.getSupply(Commodities.HEAVY_MACHINERY).getQuantity().unmodifyFlat(id);
            
            heavyInd.getDemand(Commodities.METALS).getQuantity().unmodifyFlat(id + "_metals");
            heavyInd.getDemand(Commodities.RARE_METALS).getQuantity().unmodifyFlat(id + "_rare_metals");
            
            heavyInd.getUpkeep().unmodifyMult(id);
        }

        Industry lightInd = sol_industry_compat.getLightIndustry(market);
        if (lightInd != null) {
            lightInd.getSupply(Commodities.DOMESTIC_GOODS).getQuantity().unmodifyFlat(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();
        
        int size = market.getSize();
        
        int supplies = (size >= 5) ? 3 : 2;
        int machinery = (size >= 5) ? 3 : 2;
        int metalsDem = (size >= 5) ? 2 : 1;
        int rareMetalsDem = 1; 
        int domesticGoods = (size >= 5) ? 3 : 2;

        tooltip.addPara("%s supplies production (Orbital Works)", pad, h, "+" + supplies);
        tooltip.addPara("%s heavy machinery production (Orbital Works)", pad, h, "+" + machinery);
        
        tooltip.addPara("%s metals demand (Orbital Works)", pad, h, "+" + metalsDem);
        tooltip.addPara("%s transplutonics demand (Orbital Works)", pad, h, "+" + rareMetalsDem);
        tooltip.addPara("%s upkeep (Orbital Works)", pad, h, "1.3x");

        tooltip.addPara("%s domestic goods production (Light Industry)", pad, h, "+" + domesticGoods);
    }
}