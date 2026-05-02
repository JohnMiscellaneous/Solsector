package com.fs.starfarer.api.impl.campaign.econ;
// Consider: made-up corp
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class lvmh_hq extends BaseMarketConditionPlugin {

    public static final int INDUSTRY_BONUS = 1;

    @Override
    public void apply(String id) {
        Industry lightInd = sol_industry_compat.getLightIndustry(market);
        
        if (lightInd != null) {
            lightInd.getSupply(Commodities.LUXURY_GOODS).getQuantity().modifyFlat(id, INDUSTRY_BONUS, "Hermes-LVMH HQ");
        }
    }

    @Override
    public void unapply(String id) {
        Industry lightInd = sol_industry_compat.getLightIndustry(market);
        
        if (lightInd != null) {
            lightInd.getSupply(Commodities.LUXURY_GOODS).getQuantity().unmodifyFlat(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara("%s luxury goods production (Light Industry)", 
            10f, Misc.getHighlightColor(), "+" + INDUSTRY_BONUS);
    }
}