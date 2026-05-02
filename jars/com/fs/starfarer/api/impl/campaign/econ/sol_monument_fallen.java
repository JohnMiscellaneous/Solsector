package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

public class sol_monument_fallen extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        super.apply(id);

        int size = market.getSize();
        
        Industry milInd = sol_industry_compat.getMilitary(market);
        boolean hasMilitaryBase = (milInd != null && !milInd.getId().equals(Industries.PATROLHQ));
        
        Industry pop = market.getIndustry(Industries.POPULATION);

        if (size < 4) {
            if (hasMilitaryBase) {
                milInd.getSupply(Commodities.MARINES).getQuantity().modifyFlat(id, 1, "Fallen monument");
            }
        } else {
            if (hasMilitaryBase) {
                milInd.getSupply(Commodities.MARINES).getQuantity().modifyFlat(id, 2, "Fallen monument");
            } else {
                if (pop != null) {
                    pop.getSupply(Commodities.MARINES).getQuantity().modifyFlat(id, 1, "Fallen monument");
                }
            }
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        Industry milInd = sol_industry_compat.getMilitary(market);
        if (milInd != null) {
            milInd.getSupply(Commodities.MARINES).getQuantity().unmodifyFlat(id);
        }

        Industry pop = market.getIndustry(Industries.POPULATION);
        if (pop != null) {
            pop.getSupply(Commodities.MARINES).getQuantity().unmodifyFlat(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();
        int size = market.getSize();
        
        tooltip.addPara("The cleared ruins now serve as a monument to the past conflict, fostering a martial culture and inspiring recruits.", pad);

        if (size < 4) {
            tooltip.addPara("%s marines production (Military Base)", pad, h, "+1");
        } else {
            
            tooltip.addSectionHeading("With Military Infrastructure", Alignment.MID, pad);
            tooltip.addPara("%s marines production (Military Base)", pad, h, "+2");

            tooltip.addSectionHeading("Without Military Infrastructure", Alignment.MID, pad);
            tooltip.addPara("%s marines production (Population & Infrastructure)", pad, h, "+1");
        }
    }
}