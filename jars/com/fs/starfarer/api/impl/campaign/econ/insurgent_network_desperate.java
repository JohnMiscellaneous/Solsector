package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.sol_remove_replace;
import com.fs.starfarer.api.impl.campaign.econ.SolIntelHelper;

public class insurgent_network_desperate extends BaseMarketConditionPlugin {

    // Memory keys
    private static final String MEM_KEY_ACTIVE = "$insurgent_desperate_active";
    private static final String MEM_KEY_STAGE = "$insurgent_desperate_stage";
    private static final String MEM_KEY_DAYS_LEFT = "$insurgent_desperate_days_left";

    @Override
    public void apply(String id) {
        if (market.isPlanetConditionMarketOnly()) {
            return;
        }
        if (market.getSize() < 3) {
            return;
        }

        MemoryAPI memory = market.getMemoryWithoutUpdate();
        String desc = condition.getName();

        boolean isActive = memory.getBoolean(MEM_KEY_ACTIVE);
        if (!isActive) {
            startCleanup(market);
        }

        // Stat penalties always apply while condition is present
        market.getHazard().modifyFlat(id, 0.05f, desc);
        market.getStability().modifyFlat(id, -1, desc);
    }

    @Override
    public void unapply(String id) {
        market.getHazard().unmodifyFlat(id);
        market.getStability().unmodifyFlat(id);
    }

    // --- Chain Intel ID ---

    private static String getChainId(MarketAPI market) {
        return "insurgent_desperate_" + market.getId();
    }

    // --- Script Logic ---

    private void startCleanup(MarketAPI market) {
        market.getMemoryWithoutUpdate().set(MEM_KEY_ACTIVE, true);
        market.getMemoryWithoutUpdate().set(MEM_KEY_STAGE, 1);

        // Random 200-400 days for the initial cleanup phase
        float days = 200f + (float)(Math.random() * 200f);
        market.getMemoryWithoutUpdate().set(MEM_KEY_DAYS_LEFT, days);

        // --- Chain Intel: Create & first update ---
        String marketName = market.getName();
        SolIntelHelper.SolChainIntel chain = SolIntelHelper.createChain(
                getChainId(market),
                "Embattlement Cleanup",
                "graphics/icons/markets/insurgent_network_desperate.png")
            .market(market)
            .imagePath("graphics/illustrations/killa_shrine.jpg");

        chain.addUpdate("Cleanup Begun")
            .subtitle("Cleanup begun on " + marketName)
            .description(
                "Heavy machinery has begun the slow work of clearing the embattlements that scar %s. "
                + "Cleanup crews are pulling down trench works, hauling away rubble, "
                + "and identifying remains where they can.",
                marketName)
            .summary("Cleanup operations are underway.")
            .bulletHL("Hazard and stability penalties remain", "remain")
            .push();

        Global.getSector().addScript(new InsurgentDesperateScript(market));
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        Color h = Misc.getHighlightColor();
        float pad = 10f;

        if (market.getSize() >= 3) {
            tooltip.addPara("Clearing operations are %s to remove the scars of war.",
                pad, h, "underway");

            MemoryAPI memory = market.getMemoryWithoutUpdate();
            int stage = memory.getInt(MEM_KEY_STAGE);
            if (stage == 1 && memory.contains(MEM_KEY_DAYS_LEFT)) {
                int daysLeft = (int) memory.getFloat(MEM_KEY_DAYS_LEFT);
                if (daysLeft > 0) {
                    tooltip.addPara("Estimated time until completion: %s days.", pad, h, "" + daysLeft);
                }
            }
        } else {
            tooltip.addPara("Given %s your officers believe the worst of these scars can be easily cleared.", pad, h, "time");
        }
        tooltip.addPara("%s hazard rating", pad, h, "+5%");
        tooltip.addPara("%s stability", pad, h, "-1");
    }

    // =========================================================================
    // Progression script
    // =========================================================================

    private static class InsurgentDesperateScript implements EveryFrameScript {
        private final MarketAPI market;
        private boolean done = false;

        public InsurgentDesperateScript(MarketAPI market) {
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

            int currentStage = market.getMemoryWithoutUpdate().getInt(MEM_KEY_STAGE);

            // During stage 1 the condition must still be present.
            // Past stage 1 the condition has been removed - the script keeps
            // running silently in the background to handle the monument roll.
            if (currentStage <= 1 && !market.hasCondition("sol_insurgent_network_desperate")) {
                done = true;
                return;
            }

            float days = Global.getSector().getClock().convertToDays(amount);
            float daysLeft = market.getMemoryWithoutUpdate().getFloat(MEM_KEY_DAYS_LEFT);
            daysLeft -= days;

            if (daysLeft > 0) {
                market.getMemoryWithoutUpdate().set(MEM_KEY_DAYS_LEFT, daysLeft);
                return;
            }

            // Advance stage
            int nextStage = currentStage + 1;
            market.getMemoryWithoutUpdate().set(MEM_KEY_STAGE, nextStage);

            String marketName = market.getName();
            SolIntelHelper.SolChainIntel chain = SolIntelHelper.getChain(getChainId(market));

            if (nextStage == 2) {
                // Begin the silent monument-decision wait (one standard year).
                market.getMemoryWithoutUpdate().set(MEM_KEY_DAYS_LEFT, 365f);

                if (chain != null) {
                    chain.setName("Embattlement Cleanup - Complete");
                    chain.addUpdate("Cleanup Concluded")
                        .subtitle("Worst remnants cleared on " + marketName)
                        .description(
                            "The most visible reminders of the old fighting on %s "
                            + "no longer dot the horizon of the settlement. "
                            + "Crews continue to work on outlying sectors, but the daily life of the colony "
                            + "is no longer immediately framed by what happened here. "
                            + "Whether anything will be done with the cleared ground is, for now, an open question.",
                            marketName)
                        .summary("Cleanup finished. Penalties removed.")
                        .bulletPos("Desperate Embattlements removed", "removed")
                        .push();
                }

                sol_remove_replace.execute(market, "sol_insurgent_network_desperate", null);

            } else if (nextStage == 3) {
                // Stage 3: silent monument decision. 50/50 roll.
                boolean monument = Math.random() < 0.5;

                if (monument) {
                    if (!market.hasCondition("sol_monument_fallen")) {
                        market.addCondition("sol_monument_fallen");
                    }

                    if (chain != null) {
                        chain.setName("Embattlement Cleanup - Monument Erected");
                        chain.addUpdate("Monument Erected")
                            .subtitle("Monument erected on " + marketName)
                            .description(
                                "A sculpture has been welded together from salvaged battlefield material on %s. "
                                + "Its creator - a member of the cleanup crew, at the time disciplined for poor performance, "
                                + "who spent their shifts documenting final moments rather than hauling debris - "
                                + "has gained some note for the work, "
                                + "which sits at the forefront of an emerging school of grim and grisly memorial art. "
                                + "It now stands in the city center as a monument to the fallen, and an inspiration to go out and make more.",
                                marketName)
                            .bulletPos("Monument To The Fallen established", "established")
                            .push();
                    }
                }
                // On a failed roll: do nothing. No condition added, no intel posted, no notification.

                // Cleanup memory regardless of outcome
                market.getMemoryWithoutUpdate().unset(MEM_KEY_ACTIVE);
                market.getMemoryWithoutUpdate().unset(MEM_KEY_STAGE);
                market.getMemoryWithoutUpdate().unset(MEM_KEY_DAYS_LEFT);

                done = true;
            }
        }
    }
}