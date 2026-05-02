package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

public class unexploded_ordnance extends BaseHazardCondition {

    private static final String KEY_ACTIVE = "$ordnance_active";
    private static final String KEY_STAGE = "$ordnance_stage"; 
    private static final String KEY_DAYS_LEFT = "$ordnance_days_left";

    @Override
    public void apply(String id) {
        super.apply(id);
        
        String desc = condition.getName();
        int size = market.getSize();
        boolean isActive = market.getMemoryWithoutUpdate().getBoolean(KEY_ACTIVE);
        
        int stage = isActive || market.getMemoryWithoutUpdate().contains(KEY_STAGE) 
                    ? market.getMemoryWithoutUpdate().getInt(KEY_STAGE) 
                    : 0;
        
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

        float hazard = 0.35f;
        int stability = -2;
        float upkeepMult = 2.5f;

        if (stage == 2 || stage == 3) {
            hazard = 0.25f;
            stability = -1;
            upkeepMult = 2.0f;
        }
        else if (stage == 4 || stage == 5) {
            hazard = 0.15f;
            stability = 0;
            upkeepMult = 1.5f;
        }
        else if (stage >= 6) {
             hazard = 0f;
             stability = 0;
             upkeepMult = 1f;
        }

        if (hazard > 0) {
            market.getHazard().modifyFlat(id, hazard, desc);
        }
        
        if (stability != 0) {
            market.getStability().modifyFlat(id, stability, "Bomb threats");
        }

        Industry pop = market.getIndustry(Industries.POPULATION);
        if (pop != null && upkeepMult > 1f) {
            pop.getUpkeep().modifyMult(id, upkeepMult, desc);
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getHazard().unmodifyFlat(id);
        market.getStability().unmodifyFlat(id);
        
        Industry pop = market.getIndustry(Industries.POPULATION);
        if (pop != null) {
            pop.getUpkeep().unmodifyMult(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();
        
        int stage = market.getMemoryWithoutUpdate().getInt(KEY_STAGE);
        
        if (market.isPlanetConditionMarketOnly()) {
            tooltip.addPara("Your officers estimate a colony with enough %s and %s could fully clear the ordnance.", 
                pad, h, "size", "time");
        } else {
            
            if (stage <= 1) {
                tooltip.addPara("Bomb crews struggle as ground is broken in the colony; inevitably there are slip ups.", pad);
                if (stage == 1) {
                    tooltip.addPara("Basic mitigation protocols have %s.", pad, h, "begun");
                    addDaysRemainingTooltip(tooltip, pad, h);
                }
            } 
            else if (stage == 2 || stage == 3) {
                tooltip.addPara("Basic protocols are in place, though the risk of triggering dormant explosives remains significant.", pad);
                if (stage == 3) {
                    tooltip.addPara("Advanced sweeping operations have %s.", pad, h, "begun");
                    addDaysRemainingTooltip(tooltip, pad, h);
                }
            }
            else if (stage == 4 || stage == 5) {
                tooltip.addPara("Bomb crews sweep the countryside proactively identifying and defusing unexploded ordnance.", pad);
                if (stage == 5) {
                    tooltip.addPara("Final clearance protocols have %s.", pad, h, "begun");
                    addDaysRemainingTooltip(tooltip, pad, h);
                }
            }
        }

        float hazard = 0.35f;
        int stability = -2;
        float upkeepMult = 2.5f;

        if (stage == 2 || stage == 3) {
            hazard = 0.25f;
            stability = -1;
            upkeepMult = 2.0f;
        } else if (stage == 4 || stage == 5) {
            hazard = 0.15f;
            stability = 0;
            upkeepMult = 1.5f;
        }

        if (hazard > 0) {
            tooltip.addPara("%s hazard rating", pad, h, "+" + (int)(hazard * 100f) + "%");
        }
        
        if (stability != 0) {
            tooltip.addPara("%s stability", pad, h, "" + stability);
        }

        if (upkeepMult > 1f) {
            String multStr = "" + upkeepMult;
            if (upkeepMult == (int)upkeepMult) multStr = "" + (int)upkeepMult;
            tooltip.addPara("%s upkeep (Population & Infrastructure)", pad, h, multStr + "x");
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

    private void startScript(MarketAPI market, int targetStage) {
        market.getMemoryWithoutUpdate().set(KEY_ACTIVE, true);
        
        float days = 150f + (float)(Math.random() * 100f);
        market.getMemoryWithoutUpdate().set(KEY_DAYS_LEFT, days);
        
        market.getMemoryWithoutUpdate().set(KEY_STAGE, targetStage);

        String msg = "";
        if (targetStage == 1) {
            msg = "Ordnance disposal teams on " + market.getName() + " are establishing mitigation protocols.";
        } else if (targetStage == 3) {
            msg = "Wide-area ordnance sweeping has begun on " + market.getName() + ".";
        } else if (targetStage == 5) {
            msg = "Final clearance protocols are being enacted on " + market.getName() + ".";
        }
        
        if (!msg.isEmpty()) {
            Global.getSector().getCampaignUI().addMessage(msg, Misc.getBasePlayerColor());
        }

        Global.getSector().addScript(new OrdnanceCleanupScript(market));
    }

    private static class OrdnanceCleanupScript implements EveryFrameScript {
        private final MarketAPI market;
        private boolean done = false;

        public OrdnanceCleanupScript(MarketAPI market) {
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
            if (!market.hasCondition("unexploded_ordnance")) {
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
                    market.getMemoryWithoutUpdate().unset(KEY_ACTIVE);

                    String msg = "";
                    if (nextStage == 2) {
                        msg = "Ordnance disposal teams on " + market.getName() + " have mitigated the worst of the danger.";
                    } else if (nextStage == 4) {
                        msg = "Most of the countryside on " + market.getName() + " has been cleared of explosives.";
                    } else if (nextStage == 6) {
                        msg = "Unexploded ordnance on " + market.getName() + " has been fully cleared.";
                        
                        sol_remove_replace.execute(market, "unexploded_ordnance", null);
                        
                        market.getMemoryWithoutUpdate().unset(KEY_STAGE);
                    }

                    if (!msg.isEmpty()) {
                        Global.getSector().getCampaignUI().addMessage(msg, Misc.getPositiveHighlightColor());
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