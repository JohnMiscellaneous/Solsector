package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

public class sol_megaforges_complete extends BaseMarketConditionPlugin {

    @Override
    public void apply(String id) {
        Industry refining = getRefining(market);
        if (refining != null) {
            refining.getSupply(Commodities.METALS).getQuantity().modifyFlat(id, 3, "Megaforges");
            refining.getSupply(Commodities.RARE_METALS).getQuantity().modifyFlat(id, 2, "Megaforges");
            
            refining.getDemand(Commodities.ORE).getQuantity().modifyFlat(id, 3, "Megaforges");
            refining.getDemand(Commodities.RARE_ORE).getQuantity().modifyFlat(id, 2, "Megaforges");
        }
    }

    @Override
    public void unapply(String id) {
        Industry refining = getRefining(market);
        if (refining != null) {
            refining.getSupply(Commodities.METALS).getQuantity().unmodifyFlat(id);
            refining.getSupply(Commodities.RARE_METALS).getQuantity().unmodifyFlat(id);
            refining.getDemand(Commodities.ORE).getQuantity().unmodifyFlat(id);
            refining.getDemand(Commodities.RARE_ORE).getQuantity().unmodifyFlat(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        
        float pad = 10f;
        Color h = Misc.getHighlightColor();

        tooltip.addPara("%s metals production (Refining)", pad, h, "+3");
        tooltip.addPara("%s transplutonics production (Refining)", pad, h, "+2");
        
        tooltip.addPara("%s ore demand (Refining)", pad, h, "+3");
        tooltip.addPara("%s transplutonic ore demand (Refining)", pad, h, "+2");
    }

    private Industry getRefining(MarketAPI market) {
        if (market.hasIndustry(Industries.REFINING)) return market.getIndustry(Industries.REFINING);
        return null;
    }
}