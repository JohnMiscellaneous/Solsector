package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

public class goblin_subpop extends BaseHazardCondition {

    private static class GoblinData {
        String flavorText = "";
        String highlightString = null;
        int stability = 0;
        float hazard = 0f;
        float upkeepMult = 1f;
        int foodBonus = 0;
    }

    private GoblinData getData(int size, boolean hasDefenses, boolean isFreePort) {
        GoblinData data = new GoblinData();
        
        // --- Optimized 16-Case Logic ---
        // Checks are nested: Size -> FreePort -> Defenses
        // All stats are hardcoded constants. Size 6+ is treated exactly as Size 6.
        // Consider: not doing this again
        if (size <= 3) {
            if (isFreePort) {
                if (hasDefenses) {
                    data.flavorText = "Mechanical units have secured a perimeter as scouting parties and orbital satellites have begun a census. They are billions and they are tasty.";
                    data.stability = -4;
                    data.hazard = 0.40f;
                    data.foodBonus = 4;
                    data.upkeepMult = 3.0f;
                } else {
                    data.flavorText = "Hunting parties venture out to capture specimens, but without ground defenses, more often they don't come back.";
                    data.highlightString = "ground defenses";
                    data.stability = -8;
                    data.hazard = 0.60f;
                    data.foodBonus = 3;
                    data.upkeepMult = 3.0f;
                }
            } else { // Standard
                if (hasDefenses) {
                    data.flavorText = "The settlement is secure, though the perimeter is constantly tested by raids. Exosuits stand guard at every street corner.";
                    data.stability = -1;
                    data.hazard = 0.10f;
                    data.foodBonus = 0;
                    data.upkeepMult = 1.5f;
                } else {
                    data.flavorText = "The colony is effectively under siege. Establishing ground defenses is essential to secure the perimeter against the goblin tribes.";
                    data.highlightString = "ground defenses";
                    data.stability = -6;
                    data.hazard = 0.50f;
                    data.foodBonus = 0;
                    data.upkeepMult = 1.5f;
                }
            }
        } 
        else if (size == 4) {
            if (isFreePort) {
                if (hasDefenses) {
                    data.flavorText = "Industrial harvesting teams sweep surface of what was once leleakuhonua, with rifles in hand at dilligently harvesting from low flying gunships. The local economy booms on the export of exotic proteins.";
                    data.stability = -3;
                    data.hazard = 0.30f;
                    data.foodBonus = 5;
                    data.upkeepMult = 3.0f;
                } else {
                    data.flavorText = "The meat markets are lucrative, but the trade is chaotic. The lack of ground defenses means warehouses are frequently raided by the very 'stock' they intend to sell.";
                    data.highlightString = "ground defenses";
                    data.stability = -7;
                    data.hazard = 0.55f;
                    data.foodBonus = 3;
                    data.upkeepMult = 3.0f;
                }
            } else { // Standard
                if (hasDefenses) {
                    data.flavorText = "Military patrols keep the main thoroughfares clear. The goblin population has been forced into the deep sewers, posing little threat to daily operations.";
                    data.stability = 0;
                    data.hazard = 0.05f;
                    data.foodBonus = 0;
                    data.upkeepMult = 1.5f;
                } else {
                    data.flavorText = "The population has grown, but so has the goblin presence. The lack of ground defenses leaves entire districts vulnerable to the horde.";
                    data.highlightString = "ground defenses";
                    data.stability = -5;
                    data.hazard = 0.45f;
                    data.foodBonus = 0;
                    data.upkeepMult = 1.5f;
                }
            }
        } 
        else if (size == 5) {
            if (isFreePort) {
                if (hasDefenses) {
                    data.flavorText = "The city manages a massive, industrialized cull. The goblin population is strictly regulated, harvested in cycles to maximize export yields.";
                    data.stability = -2;
                    data.hazard = 0.20f;
                    data.foodBonus = 6;
                    data.upkeepMult = 3.0f;
                } else {
                    data.flavorText = "Demand for goblin delicacies is high, but the supply chain is anarchic. Ground defenses are required to regulate the violent, chaotic ecosystem.";
                    data.highlightString = "ground defenses";
                    data.stability = -6;
                    data.hazard = 0.50f;
                    data.foodBonus = 3;
                    data.upkeepMult = 3.0f;
                }
            } else { // Standard
                if (hasDefenses) {
                    data.flavorText = "The metropolis is a fortress of order. The goblin tribes have been crushed or driven so deep underground that they are mere myths to the average citizen.";
                    data.stability = 0;
                    data.hazard = 0f;
                    data.foodBonus = 0;
                    data.upkeepMult = 1.5f;
                } else {
                    data.flavorText = "The city's sheer size keeps the core safe, but the outskirts are a warzone. Without organized ground defenses, the tribes control the periphery.";
                    data.highlightString = "ground defenses";
                    data.stability = -4;
                    data.hazard = 0.40f;
                    data.foodBonus = 0;
                    data.upkeepMult = 1.5f;
                }
            }
        } 
        else { // Size >= 6 (Hardcoded Size 6 Stats)
            if (isFreePort) {
                if (hasDefenses) {
                    data.flavorText = "A macabre masterpiece of logistics. The goblin population is farmed on a planetary scale, fueling a massive export industry of dubious morality.";
                    data.stability = -1;
                    data.hazard = 0.10f;
                    data.foodBonus = 7;
                    data.upkeepMult = 3.0f;
                } else {
                    data.flavorText = "The city consumes everything. The goblin population is integrated into the food chain, though the lack of ground defenses leads to occasional, bloody accidents.";
                    data.highlightString = "ground defenses";
                    data.stability = -5;
                    data.hazard = 0.45f;
                    data.foodBonus = 3;
                    data.upkeepMult = 3.0f;
                }
            } else { // Standard
                if (hasDefenses) {
                    data.flavorText = "A golden age of security. The goblin threat has been entirely neutralized by overwhelming military force, reduced to a footnote in the colony's reports.";
                    data.stability = 0;
                    data.hazard = 0f;
                    data.foodBonus = 0;
                    data.upkeepMult = 1.5f;
                } else {
                    data.flavorText = "The sprawl of the colony intimidates the tribes, though the absence of ground defenses allows feral pockets to persist in the shadows.";
                    data.highlightString = "ground defenses";
                    data.stability = -3;
                    data.hazard = 0.35f;
                    data.foodBonus = 0;
                    data.upkeepMult = 1.5f;
                }
            }
        }

        return data;
    }

    private static final String MEM_KEY_SWAP_QUEUED = "$goblin_subpop_swap_queued";

    @Override
    public void apply(String id) {
        // Uncolonized Check
        if (market.isPlanetConditionMarketOnly()) {
            if (!market.getMemoryWithoutUpdate().getBoolean(MEM_KEY_SWAP_QUEUED)) {
                market.getMemoryWithoutUpdate().set(MEM_KEY_SWAP_QUEUED, true);
                sol_remove_replace.execute(market, "sol_goblin_subpop", "sol_goblin_world");
            }
            return;
        }

        super.apply(id);
        String desc = condition.getName();
        
        // Gather Context
        Industry defenseInd = sol_industry_compat.getDefense(market);
        boolean hasDefenses = (defenseInd != null && defenseInd.isFunctional());
        
        // Get Data (Flavor + Constants)
        GoblinData data = getData(market.getSize(), hasDefenses, market.isFreePort());

        // Apply Modifiers
        if (data.stability != 0) {
            market.getStability().modifyFlat(id, data.stability, desc);
        }

        if (data.hazard > 0.001f) {
            market.getHazard().modifyFlat(id, data.hazard, desc);
        }

        Industry target = hasDefenses ? defenseInd : market.getIndustry(Industries.POPULATION);
        
        if (target != null) {
            target.getDemand(Commodities.HAND_WEAPONS).getQuantity().modifyFlat(id, 2, desc);
            target.getDemand(Commodities.MARINES).getQuantity().modifyFlat(id, 2, desc);
            target.getDemand(Commodities.CREW).getQuantity().modifyFlat(id, 1, desc);

            if (hasDefenses && defenseInd != null) {
                defenseInd.getUpkeep().modifyMult(id, data.upkeepMult, desc);
            }

            if (data.foodBonus > 0) {
                target.supply(id + "_food", Commodities.FOOD, data.foodBonus, desc);
            }
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getMemoryWithoutUpdate().unset(MEM_KEY_SWAP_QUEUED);
        market.getStability().unmodifyFlat(id);
        market.getHazard().unmodifyFlat(id);

        Industry pop = market.getIndustry(Industries.POPULATION);
        if (pop != null) {
            pop.getDemand(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id);
            pop.getDemand(Commodities.MARINES).getQuantity().unmodifyFlat(id);
            pop.getDemand(Commodities.CREW).getQuantity().unmodifyFlat(id);
            pop.getSupply(Commodities.FOOD).getQuantity().unmodifyFlat(id + "_food");
        }
        
        Industry defenseInd = sol_industry_compat.getDefense(market);
        if (defenseInd != null) {
            defenseInd.getUpkeep().unmodifyMult(id);
            defenseInd.getSupply(Commodities.FOOD).getQuantity().unmodifyFlat(id + "_food");
            defenseInd.getDemand(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id);
            defenseInd.getDemand(Commodities.MARINES).getQuantity().unmodifyFlat(id);
            defenseInd.getDemand(Commodities.CREW).getQuantity().unmodifyFlat(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        Industry defenseInd = sol_industry_compat.getDefense(market);
        boolean hasDefenses = (defenseInd != null && defenseInd.isFunctional());
        boolean isFreePort = market.isFreePort();
        int size = market.getSize();
        
        // Helper colors
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 10f;

        // Flavor Text (Always for current state)
        GoblinData activeData = getData(size, hasDefenses, isFreePort);
        if (!activeData.flavorText.isEmpty()) {
            if (activeData.highlightString != null) {
                tooltip.addPara(activeData.flavorText, pad, h, activeData.highlightString);
            } else {
                tooltip.addPara(activeData.flavorText, pad);
            }
        }

        // FREE PORT MODE
        tooltip.addSectionHeading("Free Port Mode", Alignment.MID, pad);
        
        Color fpValColor = isFreePort ? h : g;
        GoblinData fpData = getData(size, hasDefenses, true);

        tooltip.setBulletedListMode(" - ");
        
        if (fpData.stability != 0) {
            tooltip.addPara("Stability: %s", 0f, tc, fpValColor, "" + fpData.stability);
        }
        if (fpData.hazard > 0.001f) {
            tooltip.addPara("Hazard rating: %s", 0f, tc, fpValColor, "+" + (int)(fpData.hazard * 100) + "%");
        }
        if (fpData.foodBonus > 0) {
            tooltip.addPara("Food production: %s", 0f, tc, fpValColor, "+" + fpData.foodBonus);
        }
        if (fpData.upkeepMult > 1f) {
            tooltip.addPara("Upkeep (Ground Defenses): %s", 0f, tc, fpValColor, fpData.upkeepMult + "x");
        }
        
        // Fixed Demands (Same for both, but listed for completeness in tooltip)
        String demandTarget = hasDefenses ? "Ground Defenses" : "Population";
        tooltip.addPara("Heavy weapons demand (%s): %s", 0f, tc, fpValColor, demandTarget, "+2");
        tooltip.addPara("Marines demand (%s): %s", 0f, tc, fpValColor, demandTarget, "+2");
        tooltip.addPara("Crew demand (%s): %s", 0f, tc, fpValColor, demandTarget, "+1");

        tooltip.setBulletedListMode(null);

        // STANDARD MODE
        tooltip.addSectionHeading("Standard Mode", Alignment.MID, pad);
        
        Color stdValColor = !isFreePort ? h : g;
        GoblinData stdData = getData(size, hasDefenses, false);

        tooltip.setBulletedListMode(" - ");
        
        if (stdData.stability != 0) {
            tooltip.addPara("Stability: %s", 0f, tc, stdValColor, "" + stdData.stability);
        }
        if (stdData.hazard > 0.001f) {
            tooltip.addPara("Hazard rating: %s", 0f, tc, stdValColor, "+" + (int)(stdData.hazard * 100) + "%");
        }
        if (stdData.foodBonus > 0) {
            tooltip.addPara("Food production: %s", 0f, tc, stdValColor, "+" + stdData.foodBonus);
        }
        if (stdData.upkeepMult > 1f) {
            tooltip.addPara("Upkeep (Ground Defenses): %s", 0f, tc, stdValColor, stdData.upkeepMult + "x");
        }
        
        // Fixed Demands
        tooltip.addPara("Heavy weapons demand (%s): %s", 0f, tc, stdValColor, demandTarget, "+2");
        tooltip.addPara("Marines demand (%s): %s", 0f, tc, stdValColor, demandTarget, "+2");
        tooltip.addPara("Crew demand (%s): %s", 0f, tc, stdValColor, demandTarget, "+1");
        
        if (stdData.stability == 0 && stdData.hazard <= 0.001f && stdData.foodBonus == 0 && stdData.upkeepMult <= 1f) {
             tooltip.addPara("No specific bonuses or penalties beyond demands.", 0f, tc, stdValColor);
        }

        tooltip.setBulletedListMode(null);
    }
}