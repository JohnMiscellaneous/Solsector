package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
// TODO check to make sure this isnt called MCRD hell still
public class penal_world extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        super.apply(id);

        // Use sol_industry_compat to find any valid military structure
        Industry milInd = sol_industry_compat.getMilitary(market);
        
        if (milInd != null) {
            int size = market.getSize();

            int growthAmt = 0;
            if (size <= 3) growthAmt = 4;
            else if (size == 4) growthAmt = 0;
            else if (size >= 5) growthAmt = -2;

            if (growthAmt != 0) {
                market.getIncoming().getWeight().modifyFlat(id, growthAmt, "Penal conscription");
            }

            market.getAccessibilityMod().modifyFlat(id, -0.30f, "Strict approach protocols");

            milInd.getSupply(Commodities.MARINES).getQuantity().modifyFlat(id, 3, "Sauderkar");
            milInd.getDemand(Commodities.CREW).getQuantity().modifyFlat(id, 6, "Volunteers");

            milInd.getUpkeep().modifyMult(id, 0.5f, "Penal labor");

        } else {
            unapply(id);
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        market.getIncoming().getWeight().unmodifyFlat(id);
        market.getAccessibilityMod().unmodifyFlat(id);

        Industry milInd = sol_industry_compat.getMilitary(market);
        if (milInd != null) {
            milInd.getSupply(Commodities.MARINES).getQuantity().unmodifyFlat(id);
            milInd.getDemand(Commodities.CREW).getQuantity().unmodifyFlat(id);
            milInd.getUpkeep().unmodifyMult(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        
        float pad = 10f;
        Color h = Misc.getHighlightColor();
        // Section w/milbase
        tooltip.addSectionHeading("With military base", Alignment.MID, pad);

        int size = market.getSize();
        int growthAmt = 0;
        if (size <= 3) growthAmt = 4;
        else if (size == 4) growthAmt = 0;
        else if (size >= 5) growthAmt = -2;

        if (growthAmt != 0) {
            String prefix = growthAmt > 0 ? "+" : "";
            tooltip.addPara("Population growth: %s", pad, h, prefix + growthAmt);
        }

        tooltip.addPara("Accessibility: %s", pad, h, "-30%");

        tooltip.addPara("Marines production: %s", pad, h, "+3");
        tooltip.addPara("Crew demand: %s", pad, h, "+6");

        tooltip.addPara("Industry upkeep: %s", pad, h, "0.5x");


        // Section w/o milbase
        tooltip.addSectionHeading("Without military base", Alignment.MID, pad);

        tooltip.addPara("No effects", pad);
    }
}