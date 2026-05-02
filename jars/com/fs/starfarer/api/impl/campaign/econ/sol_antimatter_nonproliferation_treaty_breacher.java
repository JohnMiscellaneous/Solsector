package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sol_antimatter_nonproliferation_treaty_breacher extends BaseHazardCondition implements EconomyTickListener {

    public static final String ID = "sol_antimatter_nonproliferation_treaty_breacher";
    public static final String KEY_DAYS = "$sol_anptb_days";
    private static final float DAYS_TO_DOOM = 300f; 

    public void apply(String id) {
        super.apply(id);
        
        Global.getSector().getListenerManager().removeListener(this);
        Global.getSector().getListenerManager().addListener(this);
        
        // FUEL INCOME BONUS
        // +2000c per unit of fuel produced
        // This is a bonus to the player 
        
        Industry fuelInd = sol_industry_compat.getFuelProduction(market);
        if (fuelInd != null) {
            fuelInd.getSupply(Commodities.FUEL).getQuantity().unmodify(id);

            int fuelQty = fuelInd.getSupply(Commodities.FUEL).getQuantity().getModifiedInt();
            if (fuelQty > 0) {
                float bonus = fuelQty * 2000f;
                fuelInd.getIncome().modifyFlat(id, bonus, condition.getName());
            }
        }
    }

    public void unapply(String id) {
        super.unapply(id);
        
        // Remove Listener
        Global.getSector().getListenerManager().removeListener(this);
        
        Industry fuelInd = sol_industry_compat.getFuelProduction(market);
        if (fuelInd != null) {
            fuelInd.getSupply(Commodities.FUEL).getQuantity().unmodify(id);
            fuelInd.getIncome().unmodify(id);
        }
    }

    @Override
    public void reportEconomyTick(int iterIndex) {
        if (!market.hasCondition(ID)) {
            Global.getSector().getListenerManager().removeListener(this);
            return;
        }

        MemoryAPI mem = market.getMemoryWithoutUpdate();
        
        float counter = 0f;
        if (mem.contains(KEY_DAYS)) counter = mem.getFloat(KEY_DAYS);
        
        // Increment Counter (1 tick = 1 day roughly in this context, or simply counting ticks until doom)
        // Since this listener runs daily, +1f is one day.
        counter += 1f;
        mem.set(KEY_DAYS, counter);
        
        // Trigger Apocalypse
        if (counter >= DAYS_TO_DOOM) {
            sol_apocalypse.execute(market, ID);
        }
    }

    @Override
    public void reportEconomyMonthEnd() {
        // Not used
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        
        float pad = 10f;
        Color h = Misc.getHighlightColor();

        // Flavor Text
        tooltip.addPara("The treaty is dead. Factions are openly stockpiling strategic weapons, and antimatter production lines are running hot to meet the demand of the inevitable conflict.", pad);
        
        tooltip.addSpacer(10f);

        // Stats
        tooltip.addPara("+%s export income per unit (Fuel)", pad, h, "2,000\u00A2");
    }
}