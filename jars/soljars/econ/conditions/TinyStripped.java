package soljars.econ.conditions;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;

public class TinyStripped extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        super.apply(id);

        if (market.getSize() > 5) {
            market.getHazard().modifyFlat(id, 0.10f, condition.getName());
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

        if (market.getSize() > 5) {
            tooltip.addPara("%s hazard rating", 10f, Misc.getHighlightColor(), "+10%");
        }
    }
}