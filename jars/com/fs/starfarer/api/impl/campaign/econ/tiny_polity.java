package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import com.fs.starfarer.api.impl.campaign.econ.SolIntelHelper;

public class tiny_polity extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        super.apply(id);

        int size = market.getSize();

        if (size > 4) {
            Global.getSector().addScript(new StripPolityScript(market));
            return;
        }

        Industry mining = sol_industry_compat.getMining(market);
        
        if (mining != null) {
            if (size > 3) {
                if (mining.getSupply(Commodities.ORE) != null) 
                    mining.getSupply(Commodities.ORE).getQuantity().modifyFlat(id, -1f, condition.getName());
                
                if (mining.getSupply(Commodities.RARE_ORE) != null) 
                    mining.getSupply(Commodities.RARE_ORE).getQuantity().modifyFlat(id, -1f, condition.getName());
                
                if (mining.getSupply(Commodities.VOLATILES) != null) 
                    mining.getSupply(Commodities.VOLATILES).getQuantity().modifyFlat(id, -1f, condition.getName());
            } else {
                unapplyPenalties(id);
            }
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        unapplyPenalties(id);
    }

    private void unapplyPenalties(String id) {
        Industry mining = sol_industry_compat.getMining(market);
        if (mining != null) {
            if (mining.getSupply(Commodities.ORE) != null) 
                mining.getSupply(Commodities.ORE).getQuantity().unmodifyFlat(id);
            
            if (mining.getSupply(Commodities.RARE_ORE) != null) 
                mining.getSupply(Commodities.RARE_ORE).getQuantity().unmodifyFlat(id);
            
            if (mining.getSupply(Commodities.VOLATILES) != null) 
                mining.getSupply(Commodities.VOLATILES).getQuantity().unmodifyFlat(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        int size = market.getSize();
        
        if (size > 3) {
            tooltip.addPara("%s Ore production (Mining)", pad, Misc.getHighlightColor(), "-1");
            tooltip.addPara("%s Rare Ore production (Mining)", pad, Misc.getHighlightColor(), "-1");
            tooltip.addPara("%s Volatiles production (Mining)", pad, Misc.getHighlightColor(), "-1");
        }
    }

    private static class StripPolityScript implements EveryFrameScript {
        private final MarketAPI market;
        private boolean done = false;

        public StripPolityScript(MarketAPI market) {
            this.market = market;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public boolean runWhilePaused() {
            return false;
        }

        @Override
        public void advance(float amount) {
            if (done) return;

            String marketName = market.getName();

            SolIntelHelper.create("Resources Exhausted", "graphics/icons/markets/tiny_polity.png")
                .market(market)
                .subtitle("Resources exhausted on " + marketName)
                .description(
                    "The growth of the colony on %s has outstripped what the rock can provide. "
                    + "Economically viable deposits of ore, rare ore, and volatiles have been exhausted. "
                    + "What remains is more station than asteroid.",
                    marketName)
                .summary("Natural resources depleted.")
                .bulletNeg("Ore deposits exhausted", "exhausted")
                .bulletNeg("Rare ore deposits exhausted", "exhausted")
                .bulletNeg("Volatile deposits exhausted", "exhausted")
                .send();

            market.removeCondition(Conditions.ORE_SPARSE);
            market.removeCondition(Conditions.ORE_MODERATE);
            market.removeCondition(Conditions.ORE_ABUNDANT);
            market.removeCondition(Conditions.ORE_RICH);
            market.removeCondition(Conditions.ORE_ULTRARICH);

            market.removeCondition(Conditions.RARE_ORE_SPARSE);
            market.removeCondition(Conditions.RARE_ORE_MODERATE);
            market.removeCondition(Conditions.RARE_ORE_ABUNDANT);
            market.removeCondition(Conditions.RARE_ORE_RICH);
            market.removeCondition(Conditions.RARE_ORE_ULTRARICH);

            market.removeCondition(Conditions.VOLATILES_TRACE);
            market.removeCondition(Conditions.VOLATILES_DIFFUSE);
            market.removeCondition(Conditions.VOLATILES_ABUNDANT);
            market.removeCondition(Conditions.VOLATILES_PLENTIFUL);

            sol_remove_replace.execute(market, "sol_tiny_polity", "sol_tiny_stripped");

            done = true;
        }
    }
}