package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

import com.fs.starfarer.api.impl.campaign.econ.SolIntelHelper;

public class sol_dust_storms extends BaseHazardCondition {

    private static final String CONDITION_ID = "sol_dust_storms";
    
    private static final String KEY_REMOVAL_ACTIVE = "$dust_removal_active";
    private static final String KEY_DAYS_REMAINING = "$dust_days_remaining";

    public static final float HAZARD_BASE = 0.20f;
    public static final float HAZARD_THIN = 0.15f;
    public static final float HAZARD_STD = 0.35f;

    @Override
    public void apply(String id) {
        if (market.hasCondition("habitable")) {
            startRemovalScript(market, 467f, "habitable");
        } else if (market.hasCondition("no_atmosphere")) {
            startRemovalScript(market, 1f, "no_atmosphere");
        }

        float hazard = HAZARD_STD;
        if (market.hasCondition("thin_atmosphere")) {
            hazard = HAZARD_THIN;
        }

        market.getHazard().modifyFlat(id, hazard, condition.getName());
    }

    @Override
    public void unapply(String id) {
        market.getHazard().unmodifyFlat(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();

        float hazard = HAZARD_STD;

        String adviceSuffix = " Your officers believe that should the planet %s, vegetation will impede and eventually erase the dust storms.";

        if (market.hasCondition("thin_atmosphere")) {
            hazard = HAZARD_THIN;
            
            if (!market.hasCondition("habitable")) {
                tooltip.addPara("The %s mitigates the severity of the dust storms." + adviceSuffix, 
                    pad, h, "absence of a thick atmosphere", "become habitable");
            } else {
                tooltip.addPara("The %s mitigates the severity of the dust storms.", 
                    pad, h, "absence of a thick atmosphere");
            }
        } else {
            if (!market.hasCondition("habitable")) {
                tooltip.addPara("Without plant life the dust storms ravage unchecked." + adviceSuffix, 
                    pad, h, "become habitable");
            } else {
                tooltip.addPara("Without plant life the dust storms ravage unchecked.", pad);
            }
        }

        tooltip.addPara("%s hazard rating", pad, h, "+" + (int)(hazard * 100f) + "%");
        
        if (market.getMemoryWithoutUpdate().getBoolean(KEY_REMOVAL_ACTIVE)) {
            float daysLeft = market.getMemoryWithoutUpdate().getFloat(KEY_DAYS_REMAINING);
            if (daysLeft > 0) {
                tooltip.addPara("The storms are slowly abating due to environmental changes.", pad);
                tooltip.addPara("Estimated time until clearing: %s days.", pad, h, "" + (int)daysLeft);
            }
        }
    }

    // --- Chain Intel ID ---

    private static String getChainId(MarketAPI market) {
        return "dust_storms_" + market.getId();
    }

    private void startRemovalScript(MarketAPI market, float days, String triggerCondition) {
        if (market.getMemoryWithoutUpdate().getBoolean(KEY_REMOVAL_ACTIVE)) return;

        market.getMemoryWithoutUpdate().set(KEY_REMOVAL_ACTIVE, true);
        market.getMemoryWithoutUpdate().set(KEY_DAYS_REMAINING, days);

        String marketName = market.getName();

        // habitable path
        if ("habitable".equals(triggerCondition)) {
            SolIntelHelper.SolChainIntel chain = SolIntelHelper.createChain(
                    getChainId(market),
                    "Dust Storm Abatement",
                    "graphics/icons/markets/dust_storm.png")
                .market(market)
                .imagePath("graphics/illustrations/mairaath.jpg");

            chain.addUpdate("Vegetation Taking Root")
                .subtitle("Dust storms abating on " + marketName)
                .description(
                    "Vegetation is beginning to take hold across the surface of %s. "
                    + "Root systems are binding the loose topsoil that feeds the dust storms, "
                    + "and the expanding ground cover is breaking up the wind patterns "
                    + "that once carried abrasive grit across barren continents. ",
                    marketName)
                .summary("Environmental changes reducing dust storm severity.")
                .bulletPos("Vegetation stabilising surface", "stabilising")
                .bulletHL("Storms clearing over time", "over time")
                .push();
        }

        Global.getSector().addScript(new ScheduledRemovalScript(market, days, triggerCondition));
    }

    private static class ScheduledRemovalScript implements EveryFrameScript {
        private final MarketAPI market;
        private final String triggerCondition;
        private float daysRemaining;
        private boolean done = false;

        public ScheduledRemovalScript(MarketAPI market, float days, String triggerCondition) {
            this.market = market;
            this.daysRemaining = days;
            this.triggerCondition = triggerCondition;
        }

        @Override
        public boolean isDone() { return done; }

        @Override
        public boolean runWhilePaused() { return false; }

        @Override
        public void advance(float amount) {
            if (done) return;

            if (!market.hasCondition(CONDITION_ID) || !market.hasCondition(triggerCondition)) {
                cleanup();
                return;
            }

            float days = Global.getSector().getClock().convertToDays(amount);
            daysRemaining -= days;
            market.getMemoryWithoutUpdate().set(KEY_DAYS_REMAINING, daysRemaining);

            if (daysRemaining <= 0) {
                String marketName = market.getName();

                if ("habitable".equals(triggerCondition)) {
                    // end of the habitable path
                    SolIntelHelper.SolChainIntel chain = SolIntelHelper.getChain(getChainId(market));
                    if (chain != null) {
                        chain.setName("Dust Storm Abatement - Complete");
                        chain.addUpdate("Storms Ended")
                            .subtitle("Dust storms ended on " + marketName)
                            .description(
                                "The global dust bowl on %s is over. "
                                + "What was once a world of choking, static-charged storms "
                                + "is now held together by a thickening mat of vegetation. "
                                + "The filtration teams have been reassigned, "
                                + "exposed machinery no longer needs to be hardened against abrasion, "
                                + "and for the first time the horizon is reliably visible.",
                                marketName)
                            .summary("Dust storms eliminated.")
                            .bulletPos("Hazard removed", "removed")
                            .bulletPos("Dust storms ended", "ended")
                            .push();
                    }
                } else {
                    // atmosphere loss 
                    SolIntelHelper.create("Dust Storms Ended", "graphics/icons/markets/dust_storm.png")
                        .market(market)
                        .subtitle("Dust storms ended on " + marketName)
                        .imagePath("graphics/illustrations/desert_landscape.jpg")
                        .description(
                            "Without atmosphere, the dust on %s has nothing to carry it. "
                            + "The storms are gone.",
                            marketName)
                        .summary("Dust storms eliminated.")
                        .bulletPos("Hazard removed", "removed")
                        .send();
                }

                sol_remove_replace.execute(market, CONDITION_ID, null);
                cleanup();
            }
        }

        private void cleanup() {
            market.getMemoryWithoutUpdate().unset(KEY_REMOVAL_ACTIVE);
            market.getMemoryWithoutUpdate().unset(KEY_DAYS_REMAINING);
            done = true;
        }
    }
}