package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;

public class sol_remove_replace {

    public static void execute(final MarketAPI market, final String removeId, final String replaceId) {
        if (market == null || removeId == null) return;

        Global.getSector().addScript(new EveryFrameScript() {
            private boolean done = false;

            @Override
            public boolean isDone() {
                return done;
            }

            @Override
            public boolean runWhilePaused() {
                return true;
            }

            @Override
            public void advance(float amount) {
                if (done) return;

                try {
                    if (replaceId != null) {
                        boolean added = false;
                        
                        if (!market.hasCondition(replaceId)) {
                            market.addCondition(replaceId);
                            added = true;
                        }
                        
                        if (market.hasCondition(replaceId)) {
                            MarketConditionAPI mc = market.getSpecificCondition(replaceId);
                            if (mc != null && mc.getPlugin() != null) {
                                mc.getPlugin().apply(replaceId);
                            }

                            if (market.hasCondition(removeId)) {
                                market.removeCondition(removeId);
                            }
                            
                            done = true; 
                        } else {
                             Global.getSector().getCampaignUI().addMessage("Replacing " + removeId + " with " + replaceId + "...");
                        }
                    } 
                    else {
                        if (market.hasCondition(removeId)) {
                            market.removeCondition(removeId);
                        }
                        done = true;
                    }
                } catch (Exception e) {
                    Global.getSector().getCampaignUI().addMessage("Error in sol_remove_replace: " + e.getMessage());
                }
            }
        });
    }
}