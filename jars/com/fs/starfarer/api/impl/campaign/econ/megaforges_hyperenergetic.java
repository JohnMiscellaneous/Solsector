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
// 70/100, ok code. Shame about the comments (-30 points)
public class megaforges_hyperenergetic extends BaseHazardCondition {

    private static final String KEY_ACTIVE = "$hyperforge_active";
    private static final String KEY_STAGE = "$hyperforge_stage"; 
    private static final String KEY_DAYS_LEFT = "$hyperforge_days_left";

    @Override
    public void apply(String id) {
        super.apply(id);
        
        String desc = condition.getName();

        int size = market.getSize();
        boolean isActive = market.getMemoryWithoutUpdate().getBoolean(KEY_ACTIVE);
        int stage = isActive ? market.getMemoryWithoutUpdate().getInt(KEY_STAGE) : 0;

        
        if (stage == 0 && size >= 3) {
            startScript(market, 1);
            stage = 1; 
        }
        
        if (stage == 2 && size >= 4) {
            startScript(market, 3);
            stage = 3;
        }

        if (stage == 4 && size >= 5) {
            startScript(market, 5);
            stage = 5;
        }

        if (stage == 6 && size >= 6) {
            startScript(market, 7);
            stage = 7;
        }


        float hazardMod = 0.75f;
        
        if (stage == 2 || stage == 3) {
            hazardMod = 0.625f;
        }
        else if (stage == 4 || stage == 5) {
            hazardMod = 0.50f;
        }
        else if (stage >= 6) {
            hazardMod = 0.35f;
        }

        market.getHazard().modifyFlat(id, hazardMod, desc);

        Industry refining = getRefining(market);
        if (refining != null) {
            int metalsAmt = 4;
            int rareAmt = 2;
            int metalsDemand = 1;

            refining.getSupply(Commodities.METALS).getQuantity().modifyFlat(id, metalsAmt, desc);
            refining.getSupply(Commodities.RARE_METALS).getQuantity().modifyFlat(id, rareAmt, desc);

            refining.getDemand(Commodities.ORE).getQuantity().modifyFlat(id, metalsAmt, desc);
            refining.getDemand(Commodities.RARE_ORE).getQuantity().modifyFlat(id, rareAmt, desc);
            refining.getDemand(Commodities.METALS).getQuantity().modifyFlat(id, metalsDemand, desc);

            refining.getUpkeep().modifyMult(id, 3f, desc);
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
            refining.getDemand(Commodities.METALS).getQuantity().unmodifyFlat(id);
            
            refining.getUpkeep().unmodifyMult(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();
        
        int stage = market.getMemoryWithoutUpdate().getInt(KEY_STAGE);
        
        String flavorText = "";
        
        if (stage <= 1) {
            flavorText = "Attempts at carving a livable space face intense difficulties, and the engineers wear grim faces as they try to conduct a short damage assessment.";
            if (stage == 1) {
                flavorText += " Preliminary assessment has begun.";
            }
        } else if (stage == 2 || stage == 3) {
            flavorText = "Preliminary assessments are complete. Engineers are identifying key sectors for heavy metal abatement.";
            if (stage == 3) {
                flavorText += " Abatement programs have begun.";
            }
        } else if (stage == 4 || stage == 5) {
            flavorText = "Heavy metal abatement programs are underway as the most critical failures begin repairs.";
             if (stage == 5) {
                flavorText += " Work on radiators and insulation has begun.";
            }
        } else if (stage >= 6) {
            flavorText = "Sections where it is safe to not wear a suit have been established; work on radiators and insulation is ongoing, and beyond-repair sections are bypassed and blocked off.";
            if (stage == 7) {
                flavorText += " Final restoration efforts have begun.";
            }
        }

        if (!flavorText.isEmpty()) {
            tooltip.addPara(flavorText, pad);
        }
        
        if (stage <= 1) {
             tooltip.addPara("With %s, limited repairs are possible.", pad, h, "size and time");
        }
        
        if (market.getMemoryWithoutUpdate().contains(KEY_DAYS_LEFT)) {
            float days = market.getMemoryWithoutUpdate().getFloat(KEY_DAYS_LEFT);
            if (days > 0) {
                tooltip.addPara("Estimated time until current phase completion: %s days.", pad, h, "" + (int)days);
            }
        }

        float hazardMod = 0.75f;
        if (stage == 2 || stage == 3) hazardMod = 0.625f;
        else if (stage == 4 || stage == 5) hazardMod = 0.50f;
        else if (stage >= 6) hazardMod = 0.35f;

        tooltip.addPara("%s hazard rating", pad, h, "+" + (int)(hazardMod * 1000) / 10f + "%"); 

        int metalsAmt = 4;
        int rareAmt = 2;
        int metalsDemand = 1;

        tooltip.addPara("%s metals production (Refining)", pad, h, "+" + metalsAmt);
        tooltip.addPara("%s transplutonics production (Refining)", pad, h, "+" + rareAmt);
        
        tooltip.addPara("%s ore demand (Refining)", pad, h, "+" + metalsAmt);
        tooltip.addPara("%s transplutonic ore demand (Refining)", pad, h, "+" + rareAmt);
        tooltip.addPara("%s metals demand (Refining)", pad, h, "+" + metalsDemand);

        tooltip.addPara("3x upkeep (Refining)", pad, h, "3x");
    }

    private Industry getRefining(MarketAPI market) {
        if (market.hasIndustry(Industries.REFINING)) return market.getIndustry(Industries.REFINING);
        return null;
    }


    private static String getChainId(MarketAPI market) {
        return "hyperforge_" + market.getId();
    }

    private void startScript(MarketAPI market, int targetStage) {
        market.getMemoryWithoutUpdate().set(KEY_ACTIVE, true);
        
        float days = 200f + (float)(Math.random() * 100f);
        market.getMemoryWithoutUpdate().set(KEY_DAYS_LEFT, days);
        
        market.getMemoryWithoutUpdate().set(KEY_STAGE, targetStage);

        String marketName = market.getName();

        if (targetStage == 1) {
            SolIntelHelper.SolChainIntel chain = SolIntelHelper.createChain(
                    getChainId(market),
                    "Hyperforge Restoration",
                    "graphics/icons/markets/megaforges_hyperenergetic.png")
                .market(market)
                .imagePath("graphics/illustrations/sol_coronal_tap.jpg");

            chain.addUpdate("Damage Assessment")
                .subtitle("Assessment underway on " + marketName)
                .description(
                    "Engineers on %s have begun a preliminary survey of the station's condition. "
                    + "The superstructure is warped by centuries of thermal stress, "
                    + "hastily built auxiliary forges scar the hull, "
                    + "and a scattered ring of burnt-off insulation ceramic drifts in the vicinity. "
                    + "In much of the station, the temperature behind what remains of the heat shielding is scorching, "
                    + "and the halls are saturated with heavy metal particulates from ages of wear. "
                    + "The core appears functional, but everything around it is trying to kill the surveyors.",
                    marketName)
                .summary("Preliminary assessment underway.")
                .bulletNeg("Extreme hazard rating", "Extreme")
                .bulletHL("Station core functional", "functional")
                .push();

        } else if (targetStage == 3) {
            SolIntelHelper.SolChainIntel chain = SolIntelHelper.getChain(getChainId(market));
            if (chain != null) {
                chain.addUpdate("Abatement Begun")
                    .subtitle("Heavy metal abatement on " + marketName)
                    .description(
                        "With the assessment complete, abatement teams on %s have begun the work "
                        + "of stripping contaminated surfaces and sealing off the worst sections. "
                        + "The heavy metal dust is the immediate priority - "
                        + "in places the particulate concentration is dense enough to be visible, "
                        + "settling on every surface in a fine grey film that eats through standard filters, then lungs, in hours. "
                        + "Progress is slow. The station was not designed to be cleaned, it was designed to smelt.",
                        marketName)
                    .summary("Abatement programs underway.")
                    .bulletPos("Hazard reduced", "reduced")
                    .push();
            }

        } else if (targetStage == 5) {
            SolIntelHelper.SolChainIntel chain = SolIntelHelper.getChain(getChainId(market));
            if (chain != null) {
                chain.addUpdate("Thermal Restoration")
                    .subtitle("Coolant system repairs on " + marketName)
                    .description(
                        "The abatement is done, or as done as it will ever be. "
                        + "Attention on %s has shifted to the thermal management systems. "
                        + "The station's radiators and coolant loops are functional in the loosest sense - "
                        + "they run, but centuries of thermal cycling have left them riddled with microfractures, "
                        + "failed valves, and corroded junctions that leak coolant faster than it can be replaced. "
                        + "The repair work is unglamorous: tracing lines, cutting out failed sections, "
                        + "welding in replacements, and pressure-testing the result "
                        + "before moving on to the next leak.",
                        marketName)
                    .summary("Thermal systems under repair.")
                    .bulletPos("Hazard reduced", "reduced")
                    .bulletPos("Habitable sections established", "established")
                    .push();
            }

        } else if (targetStage == 7) {
            SolIntelHelper.SolChainIntel chain = SolIntelHelper.getChain(getChainId(market));
            if (chain != null) {
                chain.addUpdate("Final Restoration")
                    .subtitle("Final repairs on " + marketName)
                    .description(
                        "The last phase of restoration on %s is underway. "
                        + "The habitable core is stable, the worst contamination has been contained, "
                        + "and the coolant systems are holding. What remains is the work of integration - "
                        + "reconnecting isolated forge sections to the main circulatory network, "
                        + "bypassing junctions that are beyond repair, "
                        + "and bringing the station's throughput up to whatever fraction "
                        + "of its original capacity the damaged infrastructure will bear.",
                        marketName)
                    .summary("Final restoration phase.")
                    .bulletPos("Hazard reduced", "reduced")
                    .bulletHL("Full restoration pending", "pending")
                    .push();
            }
        }

        Global.getSector().addScript(new HyperforgeCleanupScript(market));
    }

    private static class HyperforgeCleanupScript implements EveryFrameScript {
        private final MarketAPI market;
        private boolean done = false;

        public HyperforgeCleanupScript(MarketAPI market) {
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
            if (!market.hasCondition("sol_megaforges_hyperenergetic")) {
                done = true;
                return;
            }

            if (market.getMemoryWithoutUpdate().contains(KEY_DAYS_LEFT)) {
                float days = Global.getSector().getClock().convertToDays(amount);
                float daysLeft = market.getMemoryWithoutUpdate().getFloat(KEY_DAYS_LEFT);
                daysLeft -= days;

                if (daysLeft <= 0) {
                    int currentStage = market.getMemoryWithoutUpdate().getInt(KEY_STAGE);
                    int nextStage = currentStage + 1; 
                    
                    market.getMemoryWithoutUpdate().set(KEY_STAGE, nextStage);
                    market.getMemoryWithoutUpdate().unset(KEY_DAYS_LEFT);

                    String marketName = market.getName();
                    SolIntelHelper.SolChainIntel chain = SolIntelHelper.getChain(getChainId(market));

                    if (nextStage == 2 && chain != null) {
                        chain.addUpdate("Assessment Complete")
                            .subtitle("Assessment complete on " + marketName)
                            .description(
                                "The preliminary survey of %s is finished. "
                                + "The findings are about what was expected: "
                                + "the core circulatory system is intact and the primary forge chambers are operational, "
                                + "but the surrounding infrastructure is in severe disrepair. "
                                + "Heavy metal contamination is pervasive, thermal shielding has degraded across most sections, "
                                + "and several auxiliary forge clusters have fused to the superstructure "
                                + "in ways that will require cutting apart rather than repairing. "
                                + "The engineering team has drawn up a phased plan. It will take years.",
                                marketName)
                            .summary("Assessment complete. Restoration plan drafted.")
                            .bulletPos("Hazard reduced", "reduced")
                            .bulletHL("Awaiting further colony growth", "further colony growth")
                            .push();

                    } else if (nextStage == 4 && chain != null) {
                        chain.addUpdate("Abatement Complete")
                            .subtitle("Contamination contained on " + marketName)
                            .description(
                                "The heavy metal abatement program on %s has concluded. "
                                + "The worst-contaminated sections have been stripped, sealed, or vented to vacuum. "
                                + "Particulate levels in the working areas have dropped enough "
                                + "that standard filtration equipment can keep up, "
                                + "though long-term exposure remains a concern for permanent residents. "
                                + "The station smells better, which the crew insist is the most important metric.",
                                marketName)
                            .summary("Contamination contained. Conditions improving.")
                            .bulletPos("Hazard reduced", "reduced")
                            .bulletHL("Thermal repairs require further colony growth", "further colony growth")
                            .push();

                    } else if (nextStage == 6 && chain != null) {
                        chain.addUpdate("Thermal Systems Restored")
                            .subtitle("Radiators operational on " + marketName)
                            .description(
                                "The replacement radiator arrays on %s are operational. "
                                + "Interior temperatures in the habitable core have dropped "
                                + "to a range that no longer requires active cooling on personnel suits. "
                                + "A contiguous safe zone now exists around the central forge complex - "
                                + "still uncomfortably warm, still loud, still smelling faintly of hot metal, "
                                + "but recognisably a place where people can live and work "
                                + "without it being a calculated risk.",
                                marketName)
                            .summary("Habitable zone established. Thermal load managed.")
                            .bulletPos("Hazard reduced", "reduced")
                            .bulletPos("Safe habitation zone established", "established")
                            .bulletHL("Final restoration requires further colony growth", "further colony growth")
                            .push();

                    } else if (nextStage == 8) {
                        if (chain != null) {
                            chain.setName("Hyperforge Restoration - Complete");
                            chain.addUpdate("Forges Restored")
                                .subtitle("Hyperforges restored on " + marketName)
                                .description(
                                    "The colossal circulatory network of %s now pulses "
                                    + "with the controlled flow of superheated metal vapor. "
                                    + "Containment fields channel the facility's immense heat "
                                    + "into productive output rather than waste. "
                                    + "The hull still bears the scars of centuries of neglect, "
                                    + "and there are uncharted corners that remain hazardous in a hundred ways, "
                                    + "but the core functions with a terrifying efficiency, "
                                    + "swallowing raw ore and producing refined alloys "
                                    + "at a scale beyond any facility in the Persean Sector.",
                                    marketName)
                                .summary("Restoration complete. Output dramatically increased.")
                                .bulletPos("Hazard significantly reduced", "significantly reduced")
                                .bulletPos("Metals production greatly increased", "greatly increased")
                                .bulletPos("Transplutonics production greatly increased", "greatly increased")
                                .push();
                        }
                        
                        sol_remove_replace.execute(market, "sol_megaforges_hyperenergetic", "sol_megaforges_hyperenergetic_complete");
                        
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