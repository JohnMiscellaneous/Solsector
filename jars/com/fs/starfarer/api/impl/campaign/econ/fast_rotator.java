package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class fast_rotator extends BaseHazardCondition {

    public static final float ACCESS_BONUS = 0.1f;

    @Override
    public void apply(String id) {
        // Basehazard does the hazard
        // wow... almost a normal condition
        super.apply(id);

        market.getAccessibilityMod().modifyFlat(id, ACCESS_BONUS, "Low orbital velocity");
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        market.getAccessibilityMod().unmodifyFlat(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara("%s accessibility", 
            10f, 
            Misc.getHighlightColor(), 
            "+" + (int)(ACCESS_BONUS * 100f) + "%"
        );
    }
}