package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import com.fs.starfarer.api.impl.campaign.econ.SolIntelHelper;

public class sol_ai_terminators extends BaseHazardCondition {

    private static final String KEY_CAMPAIGN_ACTIVE = "$sol_hk_campaign_active";
    private static final String KEY_DAYS_LEFT = "$sol_hk_campaign_days";

    @Override
    public void apply(String id) {
        super.apply(id);
        
        String desc = condition.getName();
        int size = market.getSize();
        Industry defense = sol_industry_compat.getDefense(market);
        boolean hasDefense = (defense != null);
        boolean campaignActive = market.getMemoryWithoutUpdate().getBoolean(KEY_CAMPAIGN_ACTIVE);

        // Extermination campaign
        if (size >= 6 && hasDefense && !campaignActive) {
            startCampaign(market);
            campaignActive = true;
        }

        // Modifiers Logic
        float hazard = 0f;
        int stability = 0;
        int growth = 0;

        if (size < 4) {
            if (hasDefense) {
                hazard = 0.25f;
                growth = -2;
            } else {
                hazard = 0.50f;
                stability = -1;
                growth = -5;
            }
        }
        else if (size == 4) {
            if (hasDefense) {
                hazard = 0.15f;
                growth = -1;
            } else {
                hazard = 0.60f;
                stability = -2;
                growth = -10;
            }
        }
        else if (size == 5) {
            if (hasDefense) {
                hazard = 0.10f;
            } else {
                hazard = 0.75f;
                stability = -3;
                growth = -15;
            }
        }
        else if (size >= 6) {
            if (hasDefense && campaignActive) {
                hazard = 0.05f;

                defense.getDemand(Commodities.HAND_WEAPONS).getQuantity().modifyFlat(id, 5, "Extermination campaign");
                defense.getDemand(Commodities.MARINES).getQuantity().modifyFlat(id, 2, "Extermination campaign");
            } else if (hasDefense) {
                hazard = 0.05f; 
            } else {
                hazard = 1.00f;
                stability = -5;
                growth = -20;
            }
        }

        // Apply Modifiers
        if (hazard > 0) {
            market.getHazard().modifyFlat(id, hazard, desc);
        }
        if (stability != 0) {
            market.getStability().modifyFlat(id, stability, desc);
        }
        if (growth != 0) {
            market.getIncoming().getWeight().modifyFlat(id, growth, desc);
        }
        
        // Apply 4x Upkeep to Defenses
        if (hasDefense) {
            defense.getUpkeep().modifyMult(id, 4f, desc);
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getHazard().unmodifyFlat(id);
        market.getStability().unmodifyFlat(id);
        market.getIncoming().getWeight().unmodifyFlat(id);

        Industry defense = sol_industry_compat.getDefense(market);
        if (defense != null) {
            defense.getDemand(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id);
            defense.getDemand(Commodities.MARINES).getQuantity().unmodifyFlat(id);
            defense.getUpkeep().unmodifyMult(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();

        int size = market.getSize();
        Industry defense = sol_industry_compat.getDefense(market);
        boolean hasDefense = (defense != null);
        boolean campaignActive = market.getMemoryWithoutUpdate().getBoolean(KEY_CAMPAIGN_ACTIVE);

        // Description Text 
        if (market.isPlanetConditionMarketOnly()) {
            tooltip.addPara("Autonomous Hunter-Killer drones roam the surface. Your officers estimate a colony with %s could push them back.", pad, h, "defenses");
        } else {
            if (!hasDefense) {
                if (size < 4) {
                    tooltip.addPara("Without %s, the fledgling settlement is subjected to constant, probing attacks by machine kill-teams.", pad, h, "defenses");
                } else if (size == 4) {
                    tooltip.addPara("The colony has expanded, though the lack of %s leaves the outer districts exposed to devastating drone raids.", pad, h, "defenses");
                } else if (size == 5) {
                    tooltip.addPara("Mass casualties are a daily reality; sustaining a population of this size without %s is becoming untenable.", pad, h, "defenses");
                } else if (size >= 6) {
                    tooltip.addPara("The colony's existence at this scale without %s defies all tactical logic; the hunter-killers should have overrun it long ago.", pad, h, "defenses");
                }
            } else {
                if (size < 4) {
                    tooltip.addPara("Defensive perimeters are holding, though the drones test them constantly.", pad);
                } else if (size == 4) {
                    tooltip.addPara("Patrols have established safe zones, pushing the machines into the wastes.", pad);
                } else if (size == 5) {
                    tooltip.addPara("The drones are isolated in remote sectors, posing a minimal threat to operations.", pad);
                } else if (size >= 6 && campaignActive) {
                    tooltip.addPara("An extermination campaign has begun against the Hunter-killer drones and their control nodes.", pad, h, "");
                    if (market.getMemoryWithoutUpdate().contains(KEY_DAYS_LEFT)) {
                        float days = market.getMemoryWithoutUpdate().getFloat(KEY_DAYS_LEFT);
                        tooltip.addPara("Estimated time to completion: %s days.", pad, h, "" + (int)days);
                    }
                }
            }
        }

        // Stats Display
        float hazard = 0f;
        int stability = 0;
        int growth = 0;

        if (size < 4) {
            if (hasDefense) { hazard = 0.25f; growth = -2; }
            else { hazard = 0.50f; stability = -1; growth = -5; }
        } else if (size == 4) {
            if (hasDefense) { hazard = 0.15f; growth = -1; }
            else { hazard = 0.60f; stability = -2; growth = -10; }
        } else if (size == 5) {
            if (hasDefense) { hazard = 0.10f; }
            else { hazard = 0.75f; stability = -3; growth = -15; }
        } else if (size >= 6) {
            if (hasDefense && campaignActive) { hazard = 0.05f; }
            else if (hasDefense) { hazard = 0.05f; }
            else { hazard = 1.00f; stability = -5; growth = -20; }
        }

        if (hazard > 0) {
            tooltip.addPara("%s hazard rating", pad, h, "+" + (int)(hazard * 100f) + "%");
        }
        if (stability != 0) {
            tooltip.addPara("%s stability", pad, h, "" + stability);
        }
        if (growth != 0) {
            String growthStr = growth > 0 ? "+" + growth : "" + growth;
            tooltip.addPara("%s population growth", pad, h, growthStr);
        }
        
        if (hasDefense) {
            tooltip.addPara("%s upkeep (Defenses)", pad, h, "4x");
        }

        if (size >= 6 && hasDefense && campaignActive) {
            tooltip.addPara("%s Heavy Armaments demand (Defenses)", pad, h, "+5");
            tooltip.addPara("%s Marines demand (Defenses)", pad, h, "+2");
        }
    }

    private void startCampaign(MarketAPI market) {
        market.getMemoryWithoutUpdate().set(KEY_CAMPAIGN_ACTIVE, true);
        
        // Random 100-300 days
        float days = 100f + (float)(Math.random() * 200f);
        market.getMemoryWithoutUpdate().set(KEY_DAYS_LEFT, days);

        String marketName = market.getName();

        // Intel: chain start
        SolIntelHelper.SolChainIntel chain = SolIntelHelper.createChain(
                "hk_campaign_" + market.getId(),
                "Extermination Campaign",
                "graphics/icons/markets/ai_terminators.png")
            .market(market)
            .imagePath("graphics/illustrations/raid_destroy.jpg");

        chain.addUpdate("Campaign Launched")
            .subtitle("Extermination campaign on " + marketName)
            .description(
                "Garrison command on %s has committed to a full extermination campaign "
                + "against the Hunter-Killer drone population. "
                + "Kill-teams are being deployed to locate and destroy the control nodes "
                + "that coordinate drone behaviour and the fabrication sites "
                + "where new units are assembled from scavenged wreckage. "
                + "The drafted campaign demands an immense toll in time, material, and men.",
                marketName)
            .summary("Extermination operations underway.")
            .bulletNeg("Heavy demand for weapons and marines", "Heavy")
            .push();

        Global.getSector().addScript(new TerminatorCampaignScript(market));
    }

    private static class TerminatorCampaignScript implements EveryFrameScript {
        private final MarketAPI market;
        private boolean done = false;

        public TerminatorCampaignScript(MarketAPI market) {
            this.market = market;
        }

        @Override
        public boolean isDone() { return done; }

        @Override
        public boolean runWhilePaused() { return false; }

        @Override
        public void advance(float amount) {
            if (done) return;
            if (!market.hasCondition("sol_ai_terminators")) {
                done = true;
                return;
            }

            if (market.getMemoryWithoutUpdate().contains(KEY_DAYS_LEFT)) {
                float days = Global.getSector().getClock().convertToDays(amount);
                float daysLeft = market.getMemoryWithoutUpdate().getFloat(KEY_DAYS_LEFT);
                daysLeft -= days;

                if (daysLeft <= 0) {
                    market.getMemoryWithoutUpdate().unset(KEY_DAYS_LEFT);
                    market.getMemoryWithoutUpdate().unset(KEY_CAMPAIGN_ACTIVE);

                    String marketName = market.getName();

                    // Intel: chain completion
                    SolIntelHelper.SolChainIntel chain = SolIntelHelper.getChain("hk_campaign_" + market.getId());
                    if (chain != null) {
                        chain.setName("Extermination Campaign - Complete");
                        chain.addUpdate("Campaign Concluded")
                            .subtitle("Hunter-Killers exterminated on " + marketName)
                            .description(
                                "The extermination campaign on %s has concluded. "
                                + "Every identified control node has been destroyed "
                                + "and the fabrication sites reduced to slag. "
                                + "Without the ability to coordinate or replicate, "
                                + "the remaining drones have degraded rapidly - "
                                + "most were found inert within weeks of losing their network, "
                                + "their extermination directives looping uselessly "
                                + "in logic cores no longer connected to anything. ",
                                marketName)
                            .summary("Hunter-Killer threat eliminated.")
                            .bulletPos("Hazard penalty removed", "removed")
                            .bulletPos("Defense upkeep normalised", "normalised")
                            .bulletPos("Campaign demands ended", "ended")
                            .bulletPos("Condition removed", "removed")
                            .push();
                    }

                    sol_remove_replace.execute(market, "sol_ai_terminators", null);
                    
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