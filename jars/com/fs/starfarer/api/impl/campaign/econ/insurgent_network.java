package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

import com.fs.starfarer.api.impl.campaign.econ.sol_remove_replace;
import com.fs.starfarer.api.impl.campaign.econ.SolIntelHelper;

public class insurgent_network extends BaseHazardCondition {

    // Memory Keys
    private static final String KEY_STAGE = "$insurgent_stage";
    private static final String KEY_DAYS_LEFT = "$insurgent_days_left";
    private static final String KEY_ACTIVE = "$insurgent_exploration_active";

    @Override
    public void apply(String id) {
        super.apply(id);

        int size = market.getSize();
        boolean isActive = market.getMemoryWithoutUpdate().getBoolean(KEY_ACTIVE);

        if (!isActive && size >= 4) {
            startExploration(market);
            isActive = true;
        }

        int stage = 0;
        if (isActive) {
            stage = market.getMemoryWithoutUpdate().getInt(KEY_STAGE);
        }

        // --- Stability Logic ---
        int stabilityMod = (stage >= 2) ? -1 : -2;

        if (stabilityMod != 0) {
            market.getStability().modifyFlat(id, stabilityMod, condition.getName());
        }

        // --- Ground Defense Logic ---
        float defenseMult = 1.1f;
        if (stage == 2) defenseMult = 1.25f;
        else if (stage == 3) defenseMult = 1.50f;
        else if (stage >= 4) defenseMult = 1.75f;

        market.getStats().getDynamic().getMod(com.fs.starfarer.api.impl.campaign.ids.Stats.GROUND_DEFENSES_MOD)
                .modifyMult(id, defenseMult, condition.getName());

        // --- Production Logic ---
        Industry heavyInd = sol_industry_compat.getHeavyIndustry(market);
        boolean hasHeavyInd = (heavyInd != null);
        
        Industry pop = market.getIndustry(Industries.POPULATION);
        if (pop != null) {
            int popBonus = 1; 
            if (!hasHeavyInd) {
                if (stage >= 2) popBonus += 1;
            }
            pop.getSupply(Commodities.HAND_WEAPONS).getQuantity().modifyFlat(id, popBonus, condition.getName());
        }

        // Heavy Industry
        if (hasHeavyInd) {
            int weaponBonus = 0;
            int supplyBonus = 0;

            if (stage == 2) weaponBonus = 1;
            else if (stage >= 3) weaponBonus = 2;

            if (stage >= 4) supplyBonus = 1;

            if (weaponBonus > 0) {
                heavyInd.getSupply(Commodities.HAND_WEAPONS).getQuantity().modifyFlat(id, weaponBonus, condition.getName());
            }
            if (supplyBonus > 0) {
                heavyInd.getSupply(Commodities.SUPPLIES).getQuantity().modifyFlat(id, supplyBonus, condition.getName());
            }
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        market.getStability().unmodifyFlat(id);
        market.getStats().getDynamic().getMod(com.fs.starfarer.api.impl.campaign.ids.Stats.GROUND_DEFENSES_MOD).unmodifyMult(id);

        cleanupIndustry(market.getIndustry(Industries.POPULATION), id);
        cleanupIndustry(sol_industry_compat.getHeavyIndustry(market), id);
    }
    
    private void cleanupIndustry(Industry ind, String id) {
        if (ind != null) {
            ind.getSupply(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id);
            ind.getSupply(Commodities.SUPPLIES).getQuantity().unmodifyFlat(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        boolean isActive = market.getMemoryWithoutUpdate().getBoolean(KEY_ACTIVE);
        int stage = isActive ? market.getMemoryWithoutUpdate().getInt(KEY_STAGE) : 0;
        Color h = Misc.getHighlightColor();

        // --- Fluff Text ---
        if (!isActive) {
            // Stage 0
            tooltip.addPara("The colony struggles as unsavory elements can effectively disappear in the tunnel networks.", pad);
            tooltip.addPara("Any recently settled colony would be too small to glean its secrets, but given %s it is feasible.", 
                pad, h, "time, and enough size and infrastructure");
        } else {
            // Active Exploration Size 4+, Stage 1+
            if (stage == 1) {
                tooltip.addPara("Exploration of the tunnel systems has begun, but given the convoluted nature of the tunnels %s how long it will last.", 
                    pad, h, "we do not know");
            } else if (stage == 2) {
                tooltip.addPara("Several workshops have been discovered that were used by the populace.", pad);
            } else if (stage == 3) {
                tooltip.addPara("Deep arms workshops have been found in the tunnels and with effort can aid in the production of weapons.", pad);
            } else if (stage >= 4) {
                 tooltip.addPara("Hidden machinery for producing spare parts has been secured within the network, bolstering supply production.", pad);
            }
        }

        // Stats Tooltips
        
        // Stability
        int stabilityMod = (stage >= 2) ? -1 : -2;
        tooltip.addPara("%s stability", pad, h, "" + stabilityMod);

        // Ground Defense
        float defenseMult = 1.1f;
        if (stage == 2) defenseMult = 1.25f;
        else if (stage == 3) defenseMult = 1.50f;
        else if (stage >= 4) defenseMult = 1.75f;
        tooltip.addPara("%s ground defense strength", pad, h, "x" + String.format("%.2f", defenseMult));

        // Production Bonuses
        if (stage < 2) {
            tooltip.addPara("%s heavy weapons production (Population & Infrastructure)", pad, h, "+1");
            if (stage == 1) {
                tooltip.addPara("Exploration in progress...", pad, Misc.getGrayColor());
            }
        } else {
            // Header (With Heavy Industry)
            tooltip.addSectionHeading("With Heavy Industry", Alignment.MID, pad);
            
            // Base Pop Bonus
            tooltip.addPara("%s heavy weapons production (Population & Infrastructure)", pad, h, "+1");
            
            // HI Specific Bonuses
            int weaponBonus = (stage == 2) ? 1 : (stage >= 3 ? 2 : 0);
            if (weaponBonus > 0) {
                tooltip.addPara("%s heavy weapons production (Heavy Industry)", pad, h, "+" + weaponBonus);
            }
            if (stage >= 4) {
                 tooltip.addPara("%s supplies production (Heavy Industry)", pad, h, "+1");
            }

            // --- Header (Without Heavy Industry) ---
            tooltip.addSectionHeading("Without Heavy Industry", Alignment.MID, pad);
            
            // Boosted Pop Bonus
            tooltip.addPara("%s heavy weapons production (Population & Infrastructure)", pad, h, "+2");
        }
    }

    // Script Logic 

    private static String getChainId(MarketAPI market) {
        return "insurgent_" + market.getId();
    }

    private void startExploration(MarketAPI market) {
        market.getMemoryWithoutUpdate().set(KEY_ACTIVE, true);
        market.getMemoryWithoutUpdate().set(KEY_STAGE, 1); 
        
        float days = 100f + (float)(Math.random() * 100f);
        market.getMemoryWithoutUpdate().set(KEY_DAYS_LEFT, days);

        // Chain Intel: Create & first update
        String marketName = market.getName();
        SolIntelHelper.SolChainIntel chain = SolIntelHelper.createChain(
                getChainId(market),
                "Subsurface Mapping",
                "graphics/icons/markets/sol_insurgent_network2.png")
            .market(market)
            .imagePath("graphics/illustrations/salvor_explore_hull.jpg");

        chain.addUpdate("Exploration Begun")
            .subtitle("Exploration begun on " + marketName)
            .description(
                "Survey teams have entered the tunnel networks beneath the surface of %s. "
                + "Thousands of kilometers of ancient insurgent passages wind through the crust, "
                + "all remnants of some insurgency long lost to entropy. "
                + "The tunnels are structurally sound but labyrinthine, and initial progress is slow. "
                + "Crude traps, preserved in vacuum, make the exercise hazardous, "
                + "and the intentionally convoluted layout makes any estimate of completion a mystery.",
                marketName)
            .summary("Mapping operations are underway.")
            .bulletNeg("Unknown completion time", "Unknown")
            .bulletNeg("High workplace injury rate", "High")
            .push();

        Global.getSector().addScript(new InsurgentExplorationScript(market));
    }

    private static class InsurgentExplorationScript implements EveryFrameScript {
        private final MarketAPI market;
        private boolean done = false;

        public InsurgentExplorationScript(MarketAPI market) {
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
            if (!market.hasCondition("sol_insurgent_network")) {
                done = true;
                return;
            }

            float days = Global.getSector().getClock().convertToDays(amount);
            float daysLeft = market.getMemoryWithoutUpdate().getFloat(KEY_DAYS_LEFT);
            daysLeft -= days;
            
            if (daysLeft <= 0) {
                int currentStage = market.getMemoryWithoutUpdate().getInt(KEY_STAGE);
                int nextStage = currentStage + 1;
                
                float nextDuration = 100f + (float)(Math.random() * 100f);
                market.getMemoryWithoutUpdate().set(KEY_DAYS_LEFT, nextDuration);
                market.getMemoryWithoutUpdate().set(KEY_STAGE, nextStage);

                String marketName = market.getName();
                SolIntelHelper.SolChainIntel chain = SolIntelHelper.getChain(getChainId(market));

                if (nextStage == 2 && chain != null) {
                    // Workshops Found
                    chain.addUpdate("Workshops Discovered")
                        .subtitle("Workshops found beneath " + marketName)
                        .description(
                            "Survey teams, between sealing off sections and installing sensors to monitor movement, "
                            + "have breached a sealed section of the tunnel network beneath %s "
                            + "and discovered a cluster of pre-Collapse workshops. The chambers were remarkably well-preserved, "
                            + "their vacuum-sealed bulkheads having kept the atmosphere stable for centuries, "
                            + "until some enterprising colonists took up residence, selling their production on the open market "
                            + "and damaging much of the equipment in the process. "
                            + "Trained professionals are now working to bring production online, "
                            + "and with a weapons manufacturing industry the machinery can be easily integrated.",
                            marketName)
                        .summary("Workshop access secured.")
                        .bulletPos("Stability improved", "improved")
                        .bulletPos("Ground defenses strengthened", "strengthened")
                        .bulletPos("Heavy weapons production unlocked", "unlocked")
                        .push();

                } else if (nextStage == 3 && chain != null) {
                    // Deep Arms Caches
                    chain.addUpdate("Deep Arms Caches Located")
                        .subtitle("Arms caches secured on " + marketName)
                        .description(
                            "Deeper penetration into the tunnel network on %s has revealed a second tier of facilities "
                            + "some distance below the initial workshops. Unlike the workshops the machinery here is too "
                            + "specialized to be useful without integration into a colony's heavy industrial base.",
                            marketName)
                        .summary("Deep production facilities secured.")
                        .bulletPos("Ground defenses increased", "increased")
                        .bulletPos("Heavy weapons production increased", "increased")
                        .push();

                } else if (nextStage == 4 && chain != null) {
                    // Hidden Machinery
                    chain.addUpdate("Hidden Machinery Recovered")
                        .subtitle("Subsurface industry found on " + marketName)
                        .description(
                            "Behind a collapsed section of tunnel on %s, surveyors have found "
                            + "what can only be described as a buried factory complex. "
                            + "Automated assembly lines, fed by ore conveyors drilled directly into mineral veins, "
                            + "once produced spare parts and field supplies for an army that no longer exists. "
                            + "Compared to the rugged weapons workshops, much of the machinery is cold-welded or beyond repair, "
                            + "and what is salvageable will require heavy industry of our own to bring online.",
                            marketName)
                        .summary("Subsurface industry found, awaiting restoration.")
                        .bulletPos("Ground defenses increased", "increased")
                        .bulletHL("Supplies production available with heavy industry", "heavy industry")
                        .push();

                } else if (nextStage == 5) {
                    // Fully Mapped
                    if (chain != null) {
                        chain.setName("Subsurface Mapping - Complete");
                        chain.addUpdate("Network Mapped")
                            .subtitle("Tunnel network secured on " + marketName)
                            .description(
                                "While its full extent has not, and might never, be mapped, near all settlements on %s "
                                + "the tunnel networks have been surveyed and catalogued, "
                                + "with the frontier of what has and has not been mapped delineated by surveillance, "
                                + "checkpoints and barriers that cannot be passed undetected. "
                                + "Elsewhere passages have been fitted with rail links and atmosphere has been restored "
                                + "as the years of slow, dangerous work are now simply part of the colony's infrastructure, "
                                + "unremarkable except to those who remember what it cost.",
                                marketName)
                            .summary("Mapping complete. All facilities operational.")
                            .bulletPos("Stability penalty removed", "removed")
                            .bulletPos("Colony upkeep reduced", "reduced")
                            .bulletPos("Ground defenses maximized", "maximized")
                            .bulletPos("Full production online", "online")
                            .push();
                    }
                    
                    sol_remove_replace.execute(market, "sol_insurgent_network", "sol_insurgent_network_mapped");
                    
                    market.getMemoryWithoutUpdate().unset(KEY_ACTIVE);
                    market.getMemoryWithoutUpdate().unset(KEY_STAGE);
                    market.getMemoryWithoutUpdate().unset(KEY_DAYS_LEFT);
                    
                    done = true;
                }
            } else {
                market.getMemoryWithoutUpdate().set(KEY_DAYS_LEFT, daysLeft);
            }
        }
    }
}