package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class antimatter_infrastructure extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        super.apply(id);
        
        String desc = condition.getName();

        Industry fuelProd = sol_industry_compat.getFuelProduction(market);
        
        if (fuelProd != null) {
            int size = market.getSize();
            
            // Clear previous mods first
            unapply(id);
            
            // Logic definition
            int fuelBonus;
            float upkeepMult;
            int volatilesDemandMod = -1; 
            
            if (size >= 5) {
                fuelBonus = 3;
                upkeepMult = 0.75f;
            } else {
                fuelBonus = 2;
                upkeepMult = 0.5f;
            }

            fuelProd.getSupply(Commodities.FUEL).getQuantity().modifyFlat(id, fuelBonus, desc);
            
            // Demand: MUST use suffix to stick (-1 volatiles)
            fuelProd.getDemand(Commodities.VOLATILES).getQuantity().modifyFlat(id + "_volatiles", volatilesDemandMod, desc);
            
            fuelProd.getUpkeep().modifyMult(id, upkeepMult, desc);
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        
        Industry fuelProd = sol_industry_compat.getFuelProduction(market);
        
        if (fuelProd != null) {
            fuelProd.getSupply(Commodities.FUEL).getQuantity().unmodifyFlat(id);
            // Unapply with suffix
            fuelProd.getDemand(Commodities.VOLATILES).getQuantity().unmodifyFlat(id + "_volatiles");
            fuelProd.getUpkeep().unmodifyMult(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();
        
        int size = market.getSize();
        
        int fuelBonus = (size >= 5) ? 3 : 2;
        String upkeepStr = (size >= 5) ? "0.75x" : "0.5x";
        
        tooltip.addPara("%s Fuel production (Fuel Production)", pad, h, "+" + fuelBonus);
        tooltip.addPara("%s Volatiles demand (Fuel Production)", pad, h, "-1");
        tooltip.addPara("%s Fuel Production upkeep", pad, h, upkeepStr);
    }
}