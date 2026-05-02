package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

import com.fs.starfarer.api.impl.campaign.econ.sol_remove_replace;
import com.fs.starfarer.api.impl.campaign.econ.SolIntelHelper;

public class megaforges extends BaseHazardCondition {

    // Memory Keys
    private static final String KEY_ACTIVE = "$megaforges_active";
    private static final String KEY_STAGE = "$megaforges_stage"; 
    private static final String KEY_DAYS_LEFT = "$megaforges_days_left";

    @Override
    public void apply(String id) {
        super.apply(id);
        
        // Optimize: Store name once to avoid repeated calls
        String desc = condition.getName();

        int size = market.getSize();
        boolean isActive = market.getMemoryWithoutUpdate().getBoolean(KEY_ACTIVE);
        int stage = isActive ? market.getMemoryWithoutUpdate().getInt(KEY_STAGE) : 0;
        
        if (stage == 0 && size >= 4) {
            startScript(market, 1);
            stage = 1; 
        }
        
        if (stage == 2 && size >= 5) {
            startScript(market, 3);
            stage = 3;
        }
        
        float hazard = 0.25f;
        int metals = 1;
        int trans = 0;
        int oreDem = 1;
        int transOreDem = 0;
        int crewDem = 2;

        if (stage == 2 || stage == 3) {
            hazard = 0.10f;
            metals = 2;
            trans = 1;
            oreDem = 2;
            transOreDem = 1;
            crewDem = 1;
        } 
        
        if (hazard > 0) {
            market.getHazard().modifyFlat(id, hazard, desc);
        }

        Industry refining = getRefining(market);
        if (refining != null) {
            if (metals > 0) {
                refining.getSupply(Commodities.METALS).getQuantity().modifyFlat(id, metals, desc);
            }
            if (trans > 0) {
                refining.getSupply(Commodities.RARE_METALS).getQuantity().modifyFlat(id, trans, desc);
            }
            
            if (oreDem > 0) {
                refining.getDemand(Commodities.ORE).getQuantity().modifyFlat(id, oreDem, desc);
            }
            if (transOreDem > 0) {
                refining.getDemand(Commodities.RARE_ORE).getQuantity().modifyFlat(id, transOreDem, desc);
            }
            
            if (crewDem > 0) {
                refining.getDemand(Commodities.CREW).getQuantity().modifyFlat(id, crewDem, desc);
            }
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getHazard().unmodifyFlat(id);
        
        Industry refining = getRefining(market);
        if (refining != null) {
            refining.getSupply(Commodities.METALS).getQuantity().unmodifyFlat(id);
            refining.getSupply(Commodities.RARE_METALS).getQuantity().unmodifyFlat(id);
            refining.getDemand(Commodities.ORE).getQuantity().unmodifyFlat(id);
            refining.getDemand(Commodities.RARE_ORE).getQuantity().unmodifyFlat(id);
            refining.getDemand(Commodities.CREW).getQuantity().unmodifyFlat(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getHighlightColor();
        
        int stage = market.getMemoryWithoutUpdate().getInt(KEY_STAGE);
        
        // --- Description Text ---
        if (stage <= 1) {
            tooltip.addPara("Much of the world is rendered uninhabitable by toxic slag and twisted metal from ancient smelteries.", pad);
            
            if (stage == 1) {
                tooltip.addPara("Limited cleanup efforts have %s.", pad, h, "begun");
                addDaysRemainingTooltip(tooltip, pad, h);
            } else {
                tooltip.addPara("Your officers estimate a colony with enough %s, can make limited improvements.", pad, h, "size and time");
            }
        } else if (stage == 2 || stage == 3) {
            tooltip.addPara("The most toxic sectors have been cleaned up or sectioned off, though the area remains dangerous.", pad);
            if (stage == 3) {
                 tooltip.addPara("Repair efforts have %s.", pad, h, "begun");
                 addDaysRemainingTooltip(tooltip, pad, h);
            }
        }

        // --- Values Setup ---
        float hazard = 0.25f;
        int metals = 1;
        int trans = 0;
        int oreDem = 1;
        int transOreDem = 0;
        int crewDem = 2;

        if (stage == 2 || stage == 3) {
            hazard = 0.10f;
            metals = 2;
            trans = 1;
            oreDem = 2;
            transOreDem = 1;
            crewDem = 1;
        }

        // --- Stats Tooltip (ALWAYS VISIBLE) ---
        if (hazard > 0) {
            tooltip.addPara("%s hazard rating", pad, bad, "+" + (int)(hazard * 100f) + "%");
        }
        
        tooltip.addPara("%s metals production (Refining)", pad, h, "+" + metals);
        if (trans > 0) {
            tooltip.addPara("%s transplutonics production (Refining)", pad, h, "+" + trans);
        }
        
        tooltip.addPara("%s ore demand (Refining)", pad, h, "+" + oreDem);
        if (transOreDem > 0) {
            tooltip.addPara("%s transplutonic ore demand (Refining)", pad, h, "+" + transOreDem);
        }
        
        if (crewDem > 0) {
             tooltip.addPara("%s crew demand (Refining)", pad, h, "+" + crewDem);
        }
    }

    private void addDaysRemainingTooltip(TooltipMakerAPI tooltip, float pad, Color h) {
        if (market.getMemoryWithoutUpdate().contains(KEY_DAYS_LEFT)) {
            float days = market.getMemoryWithoutUpdate().getFloat(KEY_DAYS_LEFT);
            if (days > 0) {
                tooltip.addPara("Estimated time until completion: %s days.", pad, h, "" + (int)days);
            }
        }
    }

    private Industry getRefining(MarketAPI market) {
        if (market.hasIndustry(Industries.REFINING)) return market.getIndustry(Industries.REFINING);
        return null;
    }

    // --- Chain Intel ID ---

    private static String getChainId(MarketAPI market) {
        return "megaforges_" + market.getId();
    }

    private void startScript(MarketAPI market, int targetStage) {
        market.getMemoryWithoutUpdate().set(KEY_ACTIVE, true);
        
        // Random 200-300 days
        float days = 200f + (float)(Math.random() * 100f);
        market.getMemoryWithoutUpdate().set(KEY_DAYS_LEFT, days);
        
        market.getMemoryWithoutUpdate().set(KEY_STAGE, targetStage);

        String marketName = market.getName();

        if (targetStage == 1) {
            // --- Chain Intel: Create & first update ---
            SolIntelHelper.SolChainIntel chain = SolIntelHelper.createChain(
                    getChainId(market),
                    "Megaforge Restoration",
                    "graphics/icons/markets/sol_megaforges.png")
                .market(market)
                .imagePath("graphics/illustrations/industrial_megafacility.jpg");

            chain.addUpdate("Cleanup Begun")
                .subtitle("Cleanup underway on " + marketName)
                .description(
                    "With the colony on %s now large enough to spare the labour, "
                    + "work crews have been dispatched into the surrounding smeltery complexes "
                    + "to begin containment of the worst hazards. "
                    + "The scale of the task is staggering - toxic slag fields stretch to the horizon, "
                    + "industrial rail lines have buckled under centuries of thermal cycling, "
                    + "and the air in the enclosed sections is thick with heavy metal particulates. "
                    + "For now the effort is limited to sectioning off the most dangerous areas "
                    + "and establishing safe corridors between the colony and the more intact forges.",
                    marketName)
                .summary("Cleanup operations are underway.")
                .bulletHL("Hazardous working conditions", "Hazardous")
                .bulletHL("High crew demand for refining operations", "High")
                .push();

        } else if (targetStage == 3) {
            // --- Chain Intel: Repairs begun ---
            SolIntelHelper.SolChainIntel chain = SolIntelHelper.getChain(getChainId(market));
            if (chain != null) {
                chain.addUpdate("Repairs Begun")
                    .subtitle("Forge repairs underway on " + marketName)
                    .description(
                        "With the toxic sectors on %s now contained, "
                        + "engineers have turned their attention to the forge machinery itself. "
                        + "Most of the original control systems are beyond salvage, "
                        + "but the physical infrastructure - the crucibles, conveyors, and smelting chambers - "
                        + "is built to a standard that has outlasted everything else on this world. "
                        + "The approach is crude by necessity: scavenged automation, "
                        + "manual overrides where software should be, and brute-force workarounds "
                        + "for components that no longer exist. "
                        + "It will never match the original design, but it does not need to.",
                        marketName)
                    .summary("Forge restoration in progress.")
                    .bulletPos("Hazard reduced", "reduced")
                    .bulletPos("Crew demand reduced", "reduced")
                    .bulletHL("Full restoration pending", "pending")
                    .push();
            }
        }

        Global.getSector().addScript(new MegaforgesCleanupScript(market));
    }

    private static class MegaforgesCleanupScript implements EveryFrameScript {
        private final MarketAPI market;
        private boolean done = false;

        public MegaforgesCleanupScript(MarketAPI market) {
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
            if (!market.hasCondition("sol_megaforges")) {
                done = true;
                return;
            }

            if (market.getMemoryWithoutUpdate().contains(KEY_DAYS_LEFT)) {
                float days = Global.getSector().getClock().convertToDays(amount);
                float daysLeft = market.getMemoryWithoutUpdate().getFloat(KEY_DAYS_LEFT);
                daysLeft -= days;

                if (daysLeft <= 0) {
                    // Timer Finished
                    int currentStage = market.getMemoryWithoutUpdate().getInt(KEY_STAGE);
                    int nextStage = currentStage + 1; // 1->2 (Cleanup Done), 3->4 (Repair Done)
                    
                    market.getMemoryWithoutUpdate().set(KEY_STAGE, nextStage);
                    market.getMemoryWithoutUpdate().unset(KEY_DAYS_LEFT);

                    String marketName = market.getName();
                    SolIntelHelper.SolChainIntel chain = SolIntelHelper.getChain(getChainId(market));

                    if (nextStage == 2 && chain != null) {
                        // --- Cleanup Complete ---
                        chain.addUpdate("Cleanup Complete")
                            .subtitle("Toxic sectors contained on " + marketName)
                            .description(
                                "The worst of it is done. The slag fields nearest the colony on %s "
                                + "have been stabilized or walled off, safe corridors have been established "
                                + "to the primary forge complexes, and the particulate levels in the working sections "
                                + "have dropped to something the medical staff will reluctantly call tolerable. "
                                + "The groundwork for restoration has been laid, "
                                + "but bringing the forges themselves back online will require "
                                + "a larger industrial base than the colony can currently support.",
                                marketName)
                            .summary("Hazardous areas contained. Awaiting further growth.")
                            .bulletPos("Hazard reduced", "reduced")
                            .bulletPos("Crew demand reduced", "reduced")
                            .bulletHL("Forge repairs require further colony growth", "further colony growth")
                            .push();

                    } else if (nextStage == 4) {
                        // --- FINAL COMPLETION ---
                        if (chain != null) {
                            chain.setName("Megaforge Restoration - Complete");
                            chain.addUpdate("Forges Operational")
                                .subtitle("Megaforges restored on " + marketName)
                                .description(
                                    "The heavy infrastructure on %s has been forced back to life. "
                                    + "Through a patchwork of scavenged tech and crude overrides, "
                                    + "the industrial rail networks and forges run once more. "
                                    + "It is a shadow of its original design, but a functional one, "
                                    + "processing vast quantities of ore through sheer mechanical scale. "
                                    + "The hazard to personnel has been eliminated, or at least "
                                    + "reduced to the baseline danger of working inside an ancient smeltery "
                                    + "held together with improvised welds and optimism.",
                                    marketName)
                                .summary("Restoration complete. Full production capacity.")
                                .bulletPos("Hazard penalty removed", "removed")
                                .bulletPos("Crew demand removed", "removed")
                                .bulletPos("Metals production increased", "increased")
                                .bulletPos("Transplutonics production increased", "increased")
                                .push();
                        }
                            
                        sol_remove_replace.execute(market, "sol_megaforges", "sol_megaforges_complete");
                        
                        market.getMemoryWithoutUpdate().unset(KEY_ACTIVE);
                        market.getMemoryWithoutUpdate().unset(KEY_STAGE);
                        market.getMemoryWithoutUpdate().unset(KEY_DAYS_LEFT);
                    }
                    
                    done = true;
                } else {
                    market.getMemoryWithoutUpdate().set(KEY_DAYS_LEFT, daysLeft);
                }
            } else {
                done = true;
            }
        }
    }
}