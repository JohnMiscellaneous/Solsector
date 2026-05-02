package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonySizeChangeListener;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sol_civilised_world extends BaseMarketConditionPlugin implements ColonySizeChangeListener {

    public static final String ID = "sol_civilised_world";

    public void apply(String id) {
        super.apply(id);
        
        // Prevent duplicate listeners
        Global.getSector().getListenerManager().removeListener(this);
        // Register Listener
        Global.getSector().getListenerManager().addListener(this);
    }

    public void unapply(String id) {
        super.unapply(id);
        // Remove Listener
        Global.getSector().getListenerManager().removeListener(this);
    }

    @Override
    public void reportColonySizeChanged(MarketAPI market, int prevSize) {
        if (market != this.market) return;

        if (!market.hasCondition(ID)) {
            Global.getSector().getListenerManager().removeListener(this);
            return;
        }

        if (!market.isPlanetConditionMarketOnly()) {
            if (market.getSize() >= 3) {
                sol_remove_replace.execute(market, ID, "sol_civilised_subpop");
                
                Global.getSector().getListenerManager().removeListener(this);
            }
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        
        float pad = 10f;
        Color h = Misc.getHighlightColor();
        Color negative = Misc.getNegativeHighlightColor();
        String mName = market.getName();

        tooltip.addPara("Civilization on " + mName + " still exists with a stable network of ruling polities laying claim to the entire planet. However, without spaceflight, the countries of " + mName + " cannot project into the system or sector.", pad);
        
        tooltip.addPara("Colonizing " + mName + " is possible but only through a network of tedious (and potentially lucrative) agreements between powers who, if provoked, could damage much of the planet's infrastructure.", pad);

        tooltip.addSpacer(10f);
        String assessment = "Your officers believe that with %s, the planet's existing power structures could be subsumed and brought under your administration.";
        tooltip.addPara(assessment, pad, h, "sufficient time and resources");

        boolean hasWar = market.hasCondition("sol_world_war");
        boolean hasSignatory = market.hasCondition("sol_antimatter_nonproliferation_treaty_signatory");
        boolean hasBreacher = market.hasCondition("sol_antimatter_nonproliferation_treaty_breacher");

        if (hasWar || hasSignatory || hasBreacher) {
            String blocker = "The %s must first abate, however.";
            tooltip.addPara(blocker, pad, h, "ongoing geopolitical tensions");
        }

        tooltip.addSpacer(10f);
        tooltip.addPara("%s Population growth", pad, h, "+20");
        tooltip.addPara("%s Upkeep (Diplomatic Burden)", pad, h, "3x");
        tooltip.addPara("%s Population income", pad, h, "2x");
        tooltip.addPara("%s Maximum industries", pad, h, "-1");
    }
}