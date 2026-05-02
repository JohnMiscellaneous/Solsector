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

public class sol_ai_freedom_fighters extends BaseHazardCondition {

    private static final String KEY_SURGE_ACTIVE = "$sol_ai_surge_active";
    private static final String KEY_DAYS_LEFT = "$sol_ai_surge_days";

    @Override
    public void apply(String id) {
        super.apply(id);

        int size = market.getSize();
        Industry defense = sol_industry_compat.getDefense(market);
        boolean hasDefense = (defense != null);
        
        // Check Surge Status
        boolean surgeActive = market.getMemoryWithoutUpdate().getBoolean(KEY_SURGE_ACTIVE);

        // Logic Transitions
        
        // Trigger Surge at Size 5 if Defenses are present and not already started/finished
        if (size >= 5 && hasDefense && !surgeActive) {
            startSurge(market);
            surgeActive = true;
        }

        // Modifiers Application

        float hazard = 0f;
        int stability = 0;
        
        if (size < 4) {
            if (hasDefense) {
                stability = -2;
                hazard = 0.05f;
            } else {
                stability = -5;
                hazard = 0.10f;
            }
        } 
        else if (size == 4) {
            if (hasDefense) {
                stability = -1;
                hazard = 0.05f;
            } else {
                stability = -4;
                hazard = 0.05f;
            }
        } 
        else if (size >= 5) {
            if (surgeActive && hasDefense) {
                stability = -3; 
                
                defense.getDemand(Commodities.MARINES).getQuantity().modifyFlat(id, 4, "Counterinsurgent surge");
                defense.getDemand(Commodities.HAND_WEAPONS).getQuantity().modifyFlat(id, 4, "Counterinsurgent surge");
                defense.getDemand(Commodities.SUPPLIES).getQuantity().modifyFlat(id, 2, "Counterinsurgent surge (Ships)");
            } else {
                stability = -3;
            }
        }

        // Apply Stats
        if (hazard > 0) {
            market.getHazard().modifyFlat(id, hazard, condition.getName());
        }
        if (stability != 0) {
            market.getStability().modifyFlat(id, stability, "AI Insurgency");
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getHazard().unmodifyFlat(id);
        market.getStability().unmodifyFlat(id);

        Industry defense = sol_industry_compat.getDefense(market);
        if (defense != null) {
            defense.getDemand(Commodities.MARINES).getQuantity().unmodifyFlat(id);
            defense.getDemand(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id);
            defense.getDemand(Commodities.SUPPLIES).getQuantity().unmodifyFlat(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();

        int size = market.getSize();
        boolean hasDefense = sol_industry_compat.getDefense(market) != null;
        boolean surgeActive = market.getMemoryWithoutUpdate().getBoolean(KEY_SURGE_ACTIVE);

        // Descriptions
        if (market.isPlanetConditionMarketOnly()) {
            tooltip.addPara("Your officers expect that a sizeable colony with %s will be able to purge the insurgents.", pad, h, "defenses");
        } else {
            if (size < 4) {
                if (hasDefense) {
                    tooltip.addPara("Garrisoned patrols have forced the insurgents underground, reducing their effectiveness.", pad);
                } else {
                    tooltip.addPara("The local AI cells are deeply entrenched, taking advantage of the lack of %s to launch frequent terror attacks against the small colony.", pad, h, "defenses");
                }
            } else if (size == 4) {
                if (hasDefense) {
                    tooltip.addPara("Heavily monitored checkpoints keep the peace, though tension remains high.", pad);
                } else {
                    tooltip.addPara("The colony's growth has outpaced the insurgents, though without %s they remain a constant thorn in daily life.", pad, h, "defenses");
                }
            } else if (size >= 5) {
                if (hasDefense) {
                    if (surgeActive) {
                        tooltip.addPara("A counterinsurgent surge has begun on %s.", pad, h, market.getName());
                        
                        if (market.getMemoryWithoutUpdate().contains(KEY_DAYS_LEFT)) {
                             float days = market.getMemoryWithoutUpdate().getFloat(KEY_DAYS_LEFT);
                             tooltip.addPara("Your generals estimate %s days until the insurgency is purged.", pad, h, "" + (int)days);
                        }
                    } else {
                         tooltip.addPara("The insurgency is marginalized but persists in the shadows, waiting for a lapse in security.", pad);
                    }
                } else {
                    tooltip.addPara("The insurgency is marginalized but persists in the shadows, waiting for the lack of %s to present an opportunity.", pad, h, "defenses");
                }
            }
        }

        // Modifiers Display
        float hazard = 0f;
        int stability = 0;
        
        if (size < 4) {
            if (hasDefense) { stability = -2; hazard = 0.05f; }
            else { stability = -5; hazard = 0.10f; }
        } else if (size == 4) {
            if (hasDefense) { stability = -1; hazard = 0.05f; }
            else { stability = -4; hazard = 0.05f; }
        } else if (size >= 5) {
            stability = -3;
        }

        if (hazard > 0) {
            tooltip.addPara("%s hazard rating", pad, h, "+" + (int)(hazard * 100f) + "%");
        }
        if (stability != 0) {
            tooltip.addPara("%s stability", pad, h, "" + stability);
        }

        // Surge Demands Tooltip
        if (size >= 5 && surgeActive && hasDefense) {
            tooltip.addPara("%s Marines demand (Defenses)", pad, h, "+4");
            tooltip.addPara("%s Heavy Armaments demand (Defenses)", pad, h, "+4");
            tooltip.addPara("%s Supplies demand (Defenses)", pad, h, "+2");
        }
    }

    private void startSurge(MarketAPI market) {
        market.getMemoryWithoutUpdate().set(KEY_SURGE_ACTIVE, true);
        
        // Random 300-700 days
        float days = 300f + (float)(Math.random() * 400f);
        market.getMemoryWithoutUpdate().set(KEY_DAYS_LEFT, days);

        String marketName = market.getName();

        // Intel: chain start
        SolIntelHelper.SolChainIntel chain = SolIntelHelper.createChain(
                "ai_surge_" + market.getId(),
                "Counterinsurgent Surge",
                "graphics/icons/markets/ai_freedom_fighters.png")
            .market(market)
            .imagePath("graphics/illustrations/raid_destroy.jpg");

        chain.addUpdate("Surge Begun")
            .subtitle("Counterinsurgent surge on " + marketName)
            .description(
                "Garrison command on %s has authorised a full counterinsurgent surge "
                + "against the remaining automated partisan cells. "
                + "Marine detachments are being deployed into the deep tunnel networks and maintenance corridors "
                + "in search of replication nodes, "
                + "supported by electronic warfare teams attempting to jam their coordination protocols. "
                + "The operation will require sustained commitments of marines, heavy weapons, and supplies "
                + "for the foreseeable future.",
                marketName)
            .summary("Counterinsurgent operations underway.")
            .bulletNeg("Heavy demand for marines and weapons", "Heavy")
            .bulletNeg("Stability temporarily worsened", "worsened")
            .push();

        Global.getSector().addScript(new PurgeScript(market));
    }

    private static class PurgeScript implements EveryFrameScript {
        private final MarketAPI market;
        private boolean done = false;

        public PurgeScript(MarketAPI market) {
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
            if (!market.hasCondition("sol_ai_freedom_fighters")) {
                done = true;
                return;
            }

            if (market.getMemoryWithoutUpdate().contains(KEY_DAYS_LEFT)) {
                float days = Global.getSector().getClock().convertToDays(amount);
                float daysLeft = market.getMemoryWithoutUpdate().getFloat(KEY_DAYS_LEFT);
                daysLeft -= days;

                if (daysLeft <= 0) {
                    market.getMemoryWithoutUpdate().unset(KEY_DAYS_LEFT);
                    market.getMemoryWithoutUpdate().unset(KEY_SURGE_ACTIVE);

                    String marketName = market.getName();

                    // Intel: surge ended
                    SolIntelHelper.SolChainIntel chain = SolIntelHelper.getChain("ai_surge_" + market.getId());
                    if (chain != null) {
                        chain.setName("Counterinsurgent Surge - Complete");
                        chain.addUpdate("Surge Concluded")
                            .subtitle("Counterinsurgent surge ended on " + marketName)
                            .description(
                                "The counterinsurgent surge on %s has concluded. "
                                + "Garrison forces have located and destroyed the primary replication nodes "
                                + "that sustained the automated partisan network, "
                                + "eliminating the insurgency's capacity for self-repair and reproduction. "
                                + "The surviving units, cut off from resupply, "
                                + "have been hunted down or have simply wound down. "
                                + "While sightings of insurgent machines have occasionally been reported since, "
                                + "they are more an oddity to the civilian population "
                                + "and an excuse for local security to pull out the heavy weapons "
                                + "than a material threat.",
                                marketName)
                            .summary("AI insurgency neutralised.")
                            .bulletPos("Stability penalty removed", "removed")
                            .bulletPos("Surge demands ended", "ended")
                            .bulletPos("Condition removed", "removed")
                            .push();
                    }
                    
                    sol_remove_replace.execute(market, "sol_ai_freedom_fighters", null);
                    
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