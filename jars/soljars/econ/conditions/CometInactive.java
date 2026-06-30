package soljars.econ.conditions;

import java.awt.Color;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;

import soljars.econ.utils.OrbitRulerHelper;

public class CometInactive extends BaseMarketConditionPlugin {

    public static final String ID = "sol_comet_inactive";

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();

        tooltip.addPara(
                "Little is certain on " + market.getName() + ", except that it will circle its star "
                + "and, once more, shine brilliantly as its surface sublimates and is blown away. "
                + "Improving %s and %s, but causing %s.",
                pad, h,
                "mining efficiency", "volatile extraction", "tectonic instability");

        tooltip.addPara(
                "The degradation of " + market.getName() + " can always be sped up and initiated "
                + "with the assistance of a %s.",
                pad, h,
                "fusion lamp");

        OrbitRulerHelper.renderCometRuler(tooltip, market, pad);
    }
}