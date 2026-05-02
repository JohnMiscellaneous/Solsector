package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sol_subsurface_aquaponics extends ResourceDepositsCondition implements EconomyTickListener {

    public static final String ID = "sol_subsurface_aquaponics";

    private static final Set<String> INVALID_PLANETS = new HashSet<String>();
    static {
        INVALID_PLANETS.add("water");
        INVALID_PLANETS.add("terran");
        INVALID_PLANETS.add("tundra");
        INVALID_PLANETS.add("desert");
        INVALID_PLANETS.add("terran_eccentric");
    }

    @Override
    public void apply(String id) {

        Global.getSector().getListenerManager().removeListener(this);
        Global.getSector().getListenerManager().addListener(this);

        Industry industry = market.getIndustry(Industries.AQUACULTURE);
        
        if (industry == null) {
            unapply(id);
            return;
        }

        String desc = condition.getName();

        if (industry.isFunctional()) {

            industry.getUpkeep().modifyMult(id, 1.5f, desc);
            industry.getDemand(Commodities.VOLATILES).getQuantity().modifyFlat(id, 1, desc);
            
            industry.getSupply(Commodities.FOOD).getQuantity().modifyFlat(id, -3, desc);
        } else {
            unapply(id);
        }
    }

    @Override
    public void unapply(String id) {
        Global.getSector().getListenerManager().removeListener(this);

        Industry industry = market.getIndustry(Industries.AQUACULTURE);
        if (industry == null) return;

        industry.getUpkeep().unmodifyMult(id);
        industry.getDemand(Commodities.VOLATILES).getQuantity().unmodifyFlat(id);
        industry.getSupply(Commodities.FOOD).getQuantity().unmodifyFlat(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();

        tooltip.addPara("Production of food: %s", pad, h, "-3");
        tooltip.addPara("Increases aquaculture upkeep by %s", pad, h, "1.5x");
        tooltip.addPara("Increases aquaculture volatiles demand by %s", pad, h, "1");
    }

    @Override
    public void reportEconomyTick(int iterIndex) {

        if (!market.hasCondition(ID)) {
            Global.getSector().getListenerManager().removeListener(this);
            return;
        }
        
        if (!market.hasIndustry(Industries.AQUACULTURE)) {
            unapply(ID);
            return;
        }

        if (market.hasCondition("habitable")) {
            PlanetAPI planet = market.getPlanetEntity();
            
            if (planet != null && INVALID_PLANETS.contains(planet.getTypeId())) {
                sol_remove_replace.execute(market, ID, null);
                Global.getSector().getListenerManager().removeListener(this);
            }
        }
    }

    @Override
    public void reportEconomyMonthEnd() {
        // Not used
    }
}