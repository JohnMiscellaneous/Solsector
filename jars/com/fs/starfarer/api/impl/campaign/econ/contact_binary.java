package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class contact_binary extends BaseHazardCondition {

    public static float ACCESS_PENALTY = 5f;
    public static int ORE_BONUS = 1;
    public static int RARE_ORE_BONUS = 1;
    public static int VOLATILES_BONUS = 1;

    @Override
    public void apply(String id) {
        super.apply(id);

        market.getAccessibilityMod().modifyFlat(id, -ACCESS_PENALTY / 100f, condition.getName());

        Industry mining = sol_industry_compat.getMining(market);
        
        if (mining != null) {
            mining.supply(id + "_0", Commodities.ORE, ORE_BONUS, condition.getName());
            mining.supply(id + "_1", Commodities.RARE_ORE, RARE_ORE_BONUS, condition.getName());
            mining.supply(id + "_2", Commodities.VOLATILES, VOLATILES_BONUS, condition.getName());
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        
        market.getAccessibilityMod().unmodifyFlat(id);

        Industry mining = sol_industry_compat.getMining(market);
        
        if (mining != null) {
            mining.getSupply(Commodities.ORE).getQuantity().unmodifyFlat(id + "_0");
            mining.getSupply(Commodities.RARE_ORE).getQuantity().unmodifyFlat(id + "_1");
            mining.getSupply(Commodities.VOLATILES).getQuantity().unmodifyFlat(id + "_2");
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara("%s accessibility", 
                10f, Misc.getHighlightColor(), 
                "-" + (int)ACCESS_PENALTY + "%");

        tooltip.addPara("%s ore production (Mining)", 
                10f, Misc.getHighlightColor(), 
                "+" + ORE_BONUS);
        
        tooltip.addPara("%s rare ore production (Mining)", 
                10f, Misc.getHighlightColor(), 
                "+" + RARE_ORE_BONUS);

        tooltip.addPara("%s volatiles production (Mining)", 
                10f, Misc.getHighlightColor(), 
                "+" + VOLATILES_BONUS);
    }
}