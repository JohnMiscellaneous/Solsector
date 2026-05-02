package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sol_ai_security_systems_complete extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        super.apply(id);
        String desc = condition.getName();

        // Fixed Effects
        // +2 Stab, 1.50 Def, -5% Haz
        
        market.getStability().modifyFlat(id, 2, desc);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, 1.50f, desc);
        market.getHazard().modifyFlat(id, -0.05f, desc);
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getStability().unmodifyFlat(id);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(id);
        market.getHazard().unmodifyFlat(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();

        tooltip.addPara("The ancient security architecture has been fully subverted; the colony wields the planetary defense grid with absolute authority.", pad);

        tooltip.addPara("%s stability", pad, h, "+2");
        tooltip.addPara("%s ground defense strength", pad, h, "1.5x");
        tooltip.addPara("%s hazard rating", pad, h, "-5%");
    }
}