package soljars.econ.industries;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;

import soljars.econ.conditions.SubsurfaceOcean;

// Shitty aquaponics
// The removal script is handled by the condition.
public class SubsurfaceAquaponicsIndustry extends BaseIndustry implements MarketImmigrationModifier {

    public static final String INDUSTRY_ID = "sol_subsurface_ocean";

    // food prod "bonus"
    public static final int FOOD_PENALTY = 2;

    // organics bonus
    // Cause of how this works this means it kicks in at size 5, and alpha core + improvements can work from there
    public static final int ORGANICS_PENALTY = 4;

    // added volatiles demand
    public static final int VOLATILES_DEMAND = 1;

    @Override
    public void apply() {
        super.apply(true);

        int size = market.getSize();

        // Demand
        demand(Commodities.HEAVY_MACHINERY, size);
        demand(Commodities.VOLATILES, Math.max(1, size - 2) + VOLATILES_DEMAND);

        // Production - never negative
        int foodOut = Math.max(0, size - FOOD_PENALTY);
        int organicsOut = Math.max(0, size - ORGANICS_PENALTY);
        supply(Commodities.FOOD, foodOut);
        supply(Commodities.ORGANICS, organicsOut);

        // Heavy machinery deficits reduce both outputs, like vanilla farming
        Pair<String, Integer> machDeficit = getMaxDeficit(Commodities.HEAVY_MACHINERY);
        applyDeficitToProduction(1, machDeficit, Commodities.FOOD, Commodities.ORGANICS);

        // Volatiles deficits also reduce production
        Pair<String, Integer> volDeficit = getMaxDeficit(Commodities.VOLATILES);
        applyDeficitToProduction(2, volDeficit, Commodities.FOOD, Commodities.ORGANICS);

        if (!isFunctional()) {
            supply.clear();
        }
    }

    @Override
    public void unapply() {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild() {
        if (!super.isAvailableToBuild()) return false;
        return market.hasCondition(SubsurfaceOcean.ID);
    }

    @Override
    public boolean showWhenUnavailable() {
        return market.hasCondition(SubsurfaceOcean.ID);
    }

    @Override
    public String getUnavailableReason() {
        if (!market.hasCondition(SubsurfaceOcean.ID)) {
            return "Requires a subsurface ocean";
        }
        return super.getUnavailableReason();
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.add(Factions.LUDDIC_CHURCH, 5f);
    }

    @Override
    protected boolean canImproveToIncreaseProduction() {
        return true;
    }

    @Override
    public RaidDangerLevel adjustCommodityDangerLevel(String commodityId, RaidDangerLevel level) {
        return level.prev();
    }

    @Override
    public RaidDangerLevel adjustItemDangerLevel(String itemId, String data, RaidDangerLevel level) {
        return level.prev();
    }

    @Override
    public void createTooltip(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltip(mode, tooltip, expanded);
    }
}