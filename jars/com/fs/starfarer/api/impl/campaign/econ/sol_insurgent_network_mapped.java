package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sol_insurgent_network_mapped extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        super.apply(id);

        String desc = condition.getName();

        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
                .modifyMult(id, 2f, desc);

        Industry heavyInd = sol_industry_compat.getHeavyIndustry(market);
        Industry pop = market.getIndustry(Industries.POPULATION);

        if (heavyInd != null) {
            heavyInd.getSupply(Commodities.HAND_WEAPONS).getQuantity().modifyFlat(id, 2, desc);
            heavyInd.getSupply(Commodities.SUPPLIES).getQuantity().modifyFlat(id, 1, desc);
        } else {
            if (pop != null) {
                pop.getSupply(Commodities.HAND_WEAPONS).getQuantity().modifyFlat(id, 1, desc);
            }
        }

        if (pop != null) {
            pop.getUpkeep().modifyMult(id, 0.75f, desc);
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(id);

        Industry pop = market.getIndustry(Industries.POPULATION);
        if (pop != null) {
            pop.getSupply(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id);
            pop.getUpkeep().unmodifyMult(id);
        }

        Industry heavyInd = sol_industry_compat.getHeavyIndustry(market);
        if (heavyInd != null) {
            heavyInd.getSupply(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id);
            heavyInd.getSupply(Commodities.SUPPLIES).getQuantity().unmodifyFlat(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;

        tooltip.addSectionHeading("General Effects", Alignment.MID, pad);
        
        tooltip.addPara("%s ground defense strength", pad, Misc.getHighlightColor(), "x2");
        
        tooltip.addPara("%s Population & Infrastructure upkeep", pad, Misc.getHighlightColor(), "0.75x");

        tooltip.addSectionHeading("With Heavy Industry", Alignment.MID, pad);
        tooltip.addPara("%s heavy weapons production (Heavy Industry)", pad, Misc.getHighlightColor(), "+2");
        tooltip.addPara("%s supplies production (Heavy Industry)", pad, Misc.getHighlightColor(), "+1");

        tooltip.addSectionHeading("Without Heavy Industry", Alignment.MID, pad);
        tooltip.addPara("%s heavy weapons production (Population & Infrastructure)", pad, Misc.getHighlightColor(), "+1");
    }
}