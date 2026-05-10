package soljars.econ.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import soljars.econ.utils.IndustryCompat;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;

public class Meteoroids extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        super.apply(id);

        Industry defense = IndustryCompat.getDefense(market);

        boolean hasDefenses = (defense != null && !defense.getId().equals(Industries.GROUNDDEFENSES));

        if (!hasDefenses) {
            market.getHazard().modifyFlat(id, 0.25f, "Meteoroid impacts");
        } else {
            market.getHazard().unmodifyFlat(id);
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getHazard().unmodifyFlat(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        Industry defense = IndustryCompat.getDefense(market);
        boolean hasDefenses = (defense != null && !defense.getId().equals(Industries.GROUNDDEFENSES));
        
        float pad = 10f;

        if (!hasDefenses) {
            tooltip.addPara("%s hazard rating", pad, Misc.getHighlightColor(), "+25%");
            tooltip.addPara("Your officers estimate that Heavy Batteries are capable of intercepting most meteoroids.", 
                pad, Misc.getHighlightColor(), "Heavy Batteries");
        } else {
            tooltip.addPara("Heavy Batteries, using precise orbital data intercept meteoroids before they can threaten the colony's infrastructure.", 
                pad, Misc.getHighlightColor(), "intercept");
        }
    }
}