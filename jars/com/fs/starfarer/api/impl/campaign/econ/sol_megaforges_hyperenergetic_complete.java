package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

public class sol_megaforges_hyperenergetic_complete extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        super.apply(id);

        market.getHazard().modifyFlat(id, 0.25f, condition.getName());

        Industry refining = getRefining(market);
        if (refining != null) {
            int metalsAmt = 7;
            int rareAmt = 5;
            int metalsDemand = 4;

            refining.getSupply(Commodities.METALS).getQuantity().modifyFlat(id, metalsAmt, "Hyperenergetic forges");
            refining.getSupply(Commodities.RARE_METALS).getQuantity().modifyFlat(id, rareAmt, "Hyperenergetic forges");

            refining.getDemand(Commodities.ORE).getQuantity().modifyFlat(id, metalsAmt, "Hyperenergetic throughput");
            refining.getDemand(Commodities.RARE_ORE).getQuantity().modifyFlat(id, rareAmt, "Hyperenergetic throughput");
            refining.getDemand(Commodities.METALS).getQuantity().modifyFlat(id, metalsDemand, "Structure reinforcement");

            refining.getUpkeep().modifyMult(id, 3f, "Hyperenergetic operation");
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        market.getHazard().unmodifyFlat(id);

        Industry refining = getRefining(market);
        if (refining != null) {
            refining.getSupply(Commodities.METALS).getQuantity().unmodifyFlat(id);
            refining.getSupply(Commodities.RARE_METALS).getQuantity().unmodifyFlat(id);
            
            refining.getDemand(Commodities.ORE).getQuantity().unmodifyFlat(id);
            refining.getDemand(Commodities.RARE_ORE).getQuantity().unmodifyFlat(id);
            refining.getDemand(Commodities.METALS).getQuantity().unmodifyFlat(id);
            
            refining.getUpkeep().unmodifyMult(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();

        tooltip.addPara("A large contiguous safe zone exists in the center of the station. Beyond that there are still uncharted corners and hallways hazardous in a hundred ways. The output of the station has reached a fraction of its former capacity.", pad);

        tooltip.addPara("%s hazard rating", pad, h, "+25%");

        int metalsAmt = 7;
        int rareAmt = 5;
        int metalsDemand = 4;

        tooltip.addPara("%s metals production (Refining)", pad, h, "+" + metalsAmt);
        tooltip.addPara("%s transplutonics production (Refining)", pad, h, "+" + rareAmt);
        
        tooltip.addPara("%s ore demand (Refining)", pad, h, "+" + metalsAmt);
        tooltip.addPara("%s transplutonic ore demand (Refining)", pad, h, "+" + rareAmt);
        tooltip.addPara("%s metals demand (Refining)", pad, h, "+" + metalsDemand);

        tooltip.addPara("3x upkeep (Refining)", pad, h, "3x");
    }

    private Industry getRefining(MarketAPI market) {
        if (market.hasIndustry(Industries.REFINING)) return market.getIndustry(Industries.REFINING);
        return null;
    }
}