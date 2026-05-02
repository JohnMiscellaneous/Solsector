package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class orbital_fleetworks extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        super.apply(id);

        String desc = condition.getName();

        Industry heavyInd = sol_industry_compat.getHeavyIndustry(market);
        
        boolean hasOrbitalWorks = heavyInd != null && !heavyInd.getId().equals(Industries.HEAVYINDUSTRY);

        if (hasOrbitalWorks) {
            int size = market.getSize();
            
            int weaponsMod = (size >= 5) ? 2 : 1;
            int shipsMod = Math.max(1, size - 2); 
            int metalsDem = (size >= 5) ? 3 : 2;
            int rareMetalsDem = (size >= 5) ? 2 : 1;

            heavyInd.getSupply(Commodities.HAND_WEAPONS).getQuantity().modifyFlat(id, weaponsMod, desc);
            heavyInd.getSupply(Commodities.SHIPS).getQuantity().modifyFlat(id, shipsMod, desc);
            
            heavyInd.getDemand(Commodities.METALS).getQuantity().modifyFlat(id, metalsDem, desc);
            heavyInd.getDemand(Commodities.RARE_METALS).getQuantity().modifyFlat(id, rareMetalsDem, desc);
            
            heavyInd.getUpkeep().modifyMult(id, 1.2f, desc);
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        Industry heavyInd = sol_industry_compat.getHeavyIndustry(market);
        cleanupIndustry(heavyInd, id);
    }

    private void cleanupIndustry(Industry ind, String id) {
        if (ind != null) {
            ind.getSupply(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id);
            ind.getSupply(Commodities.SHIPS).getQuantity().unmodifyFlat(id);
            ind.getDemand(Commodities.METALS).getQuantity().unmodifyFlat(id);
            ind.getDemand(Commodities.RARE_METALS).getQuantity().unmodifyFlat(id);
            ind.getUpkeep().unmodifyMult(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        
        float pad = 10f;
        int size = market.getSize();
        
        int weaponsMod = (size >= 5) ? 2 : 1;
        int shipsMod = Math.max(1, size - 2);
        int metalsDem = (size >= 5) ? 3 : 2;
        int rareMetalsDem = (size >= 5) ? 2 : 1;

        tooltip.addPara("%s heavy weapons production (Orbital Works)", pad, Misc.getHighlightColor(), "+" + weaponsMod);
        tooltip.addPara("%s ships production (Orbital Works)", pad, Misc.getHighlightColor(), "+" + shipsMod);
        
        tooltip.addPara("%s metals demand (Orbital Works)", pad, Misc.getHighlightColor(), "+" + metalsDem);
        tooltip.addPara("%s transplutonics demand (Orbital Works)", pad, Misc.getHighlightColor(), "+" + rareMetalsDem);
        
        tooltip.addPara("1.2x upkeep (Orbital Works)", pad, Misc.getHighlightColor(), "1.2x");
    }
}