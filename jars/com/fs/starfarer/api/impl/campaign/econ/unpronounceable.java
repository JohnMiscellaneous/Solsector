package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
// Lol
public class unpronounceable extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        super.apply(id);

        int size = market.getSize();
        float hazardMod = 0f;
        
        if (size < 4) {
            hazardMod = 0.20f;
        } else if (size == 4) {
            hazardMod = 0.15f;
        } else if (size == 5) {
            hazardMod = 0.05f;
        }

        if (hazardMod > 0) {
            market.getHazard().modifyFlat(id, hazardMod, "Unpronounceable name");
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

        float pad = 10f;
        int size = market.getSize();
        boolean isColonized = !market.isPlanetConditionMarketOnly();

        if (size < 4) {
            if (isColonized) {
                tooltip.addPara("Colonists regularly spend half a minute trying to correctly pronounce %s. Occasionally this is a mispronunciation in the presence of an automated system which kills them on the spot.", 
                    pad, Misc.getTextColor(), market.getName());
            }
        } else if (size == 4) {
            tooltip.addPara("Colonists have some expertise in pronouncing %s, but automated systems occasionally subject a colonist to torture for missing an inflection.", 
                pad, Misc.getTextColor(), market.getName());
        } else if (size == 5) {
            tooltip.addPara("Most automated name enforcement systems have been deactivated and colonists can usually pronounce %s in a few seconds.", 
                pad, Misc.getTextColor(), market.getName());
        } else {
            tooltip.addPara("A unique dialect has formed around %s's name. Everything else colonists say sounds quite queer, but should they say %s it is perfect.", 
                pad, Misc.getTextColor(), market.getName(), market.getName());
        }

        int hazardPct = 0;
        if (size < 4) hazardPct = 20;
        else if (size == 4) hazardPct = 15;
        else if (size == 5) hazardPct = 5;

        if (hazardPct > 0) {
            tooltip.addPara("%s hazard rating", pad, Misc.getHighlightColor(), "+" + hazardPct + "%");
        } else {
            tooltip.addPara("Hazard penalty negated by language adaptation.", pad, Misc.getHighlightColor());
        }
    }
}