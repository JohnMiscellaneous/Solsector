package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ancient_drug_lab extends BaseHazardCondition implements MarketImmigrationModifier {

    public static final float GROWTH_BONUS = 2f;
    public static final float ACCESS_PENALTY = 0.05f;
    public static final int TRANSPLUTONICS_DEMAND = 1;

    @Override
    public void apply(String id) {
        super.apply(id);
        
        String desc = condition.getName();

        int size = market.getSize();
        boolean isFreePort = market.isFreePort();
        
        Industry lightInd = sol_industry_compat.getLightIndustry(market);
        boolean hasLightInd = (lightInd != null);
        
        boolean hasPatrolHQ = sol_industry_compat.getMilitary(market) != null;

        Industry pop = market.getIndustry(Industries.POPULATION);
        
        unapply(id);

        // Effects
        if (size < 5) {
            if (isFreePort) {
                // Free Port (Population) Drugs +1
                if (pop != null) {
                    pop.supply(id + "_pop", Commodities.DRUGS, 1, desc);
                }

                // Free Port, Stability -2
                market.getStability().modifyFlat(id, -2, desc);

                // Free Port with Light Industry Effects
                if (hasLightInd) {
                    lightInd.supply(id + "_li", Commodities.DRUGS, 2, desc);
                    market.getStability().modifyFlat(id + "_li", -1, desc);
                    lightInd.getDemand(Commodities.RARE_METALS).getQuantity().modifyFlat(id + "_li_dem", TRANSPLUTONICS_DEMAND, desc);
                }
                
                // Free Port: Growth +2
                market.addTransientImmigrationModifier(this);

            } else {
                // Standard Effects
                if (!hasPatrolHQ) {
                    market.getStability().modifyFlat(id, -1, desc);
                } else {
                    market.getAccessibilityMod().modifyFlat(id, -ACCESS_PENALTY, desc);
                }
            }
        } else {
            // Size > 4
            if (isFreePort) {
                // Free Port (Population & inf) Drugs +2
                if (pop != null) {
                    pop.supply(id + "_pop", Commodities.DRUGS, 2, desc);
                }

                // Free Port, Stability -1
                market.getStability().modifyFlat(id, -1, desc);

                // Free Port (Light Industry)
                if (hasLightInd) {
                    // +3 Drugs
                    lightInd.supply(id + "_li", Commodities.DRUGS, 3, desc);
                    // -1 Stability
                    market.getStability().modifyFlat(id + "_li", -1, desc);
                    // +1 Transplutonics Demand
                    lightInd.getDemand(Commodities.RARE_METALS).getQuantity().modifyFlat(id + "_li_dem", TRANSPLUTONICS_DEMAND, desc);
                }
            }
            // Standard, No effects over 4
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        
        market.getStability().unmodifyFlat(id);
        market.getStability().unmodifyFlat(id + "_li");
        market.getAccessibilityMod().unmodifyFlat(id);
        
        Industry pop = market.getIndustry(Industries.POPULATION);
        if (pop != null) {
            pop.getSupply(Commodities.DRUGS).getQuantity().unmodifyFlat(id + "_pop");
        }

        Industry lightInd = sol_industry_compat.getLightIndustry(market);
        if (lightInd != null) {
            lightInd.getSupply(Commodities.DRUGS).getQuantity().unmodifyFlat(id + "_li");
            lightInd.getDemand(Commodities.RARE_METALS).getQuantity().unmodifyFlat(id + "_li_dem");
        }

        market.removeTransientImmigrationModifier(this);
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        if (market.isFreePort() && market.getSize() < 5) {
            incoming.getWeight().modifyFlat(getModId(), GROWTH_BONUS, condition.getName());
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        int size = market.getSize();
        boolean isFreePort = market.isFreePort();

        // Common Colors
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();

        // FREE PORT MODE
        tooltip.addSectionHeading("Free Port Mode", Alignment.MID, pad);
        
        // If Active: Highlight Standard. If Inactive: Highlight Gray. 
        Color fpValColor = isFreePort ? h : g;

        tooltip.setBulletedListMode(" - ");

        if (size < 5) {
            tooltip.addPara("Stability: %s", 0f, tc, fpValColor, "-2");
            tooltip.addPara("Drugs production (Population): %s", 0f, tc, fpValColor, "+1");
            tooltip.addPara("Population growth: %s", 0f, tc, fpValColor, "+" + (int)GROWTH_BONUS);
            
            tooltip.addPara("Drugs production (Light Industry): %s", 0f, tc, fpValColor, "+2");
            tooltip.addPara("Stability (Light Industry): %s", 0f, tc, fpValColor, "-1");
            tooltip.addPara("Transplutonics demand (Light Industry): %s", 0f, tc, fpValColor, "+" + TRANSPLUTONICS_DEMAND);
        } else {
            // Size 5+
            tooltip.addPara("Stability: %s", 0f, tc, fpValColor, "-1");
            tooltip.addPara("Drugs production (Population): %s", 0f, tc, fpValColor, "+2");
            
            tooltip.addPara("Drugs production (Light Industry): %s", 0f, tc, fpValColor, "+3");
            tooltip.addPara("Stability (Light Industry): %s", 0f, tc, fpValColor, "-1");
            tooltip.addPara("Transplutonics demand (Light Industry): %s", 0f, tc, fpValColor, "+" + TRANSPLUTONICS_DEMAND);
        }
        tooltip.setBulletedListMode(null);

        // STANDARD MODE
        tooltip.addSectionHeading("Standard Mode", Alignment.MID, pad);
        
        Color stdValColor = !isFreePort ? h : g;

        tooltip.setBulletedListMode(" - ");

        if (size < 5) {
            tooltip.addPara("Stability (No Patrol HQ): %s", 0f, tc, stdValColor, "-1");
            tooltip.addPara("Accessibility (Patrol HQ): %s", 0f, tc, stdValColor, "-" + (int)(ACCESS_PENALTY * 100f) + "%");
        } else {
            tooltip.addPara("No penalties or bonuses.", 0f, tc, stdValColor, "No penalties");
        }
        
        tooltip.setBulletedListMode(null);
    }
}