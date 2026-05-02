package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

// Consider: Depreciation????
// I dont like this one.
public class sol_ai_security_systems extends BaseHazardCondition {

    private static final String KEY_PROJ_1_DONE = "$sol_ai_sec_proj1_done";
    private static final String KEY_DAYS_LEFT = "$sol_ai_sec_days_left";
    private static final String KEY_ACTIVE = "$sol_ai_sec_active";

    @Override
    public void apply(String id) {
        super.apply(id);
        String desc = condition.getName();

        int sizeBase = Math.max(0, market.getSize() - 3);
        boolean hasDefense = sol_industry_compat.getDefense(market) != null;
        
        int rawStage = sizeBase + (hasDefense ? 1 : 0);
        
        boolean p1Done = market.getMemoryWithoutUpdate().getBoolean(KEY_PROJ_1_DONE);
        boolean isActive = market.getMemoryWithoutUpdate().getBoolean(KEY_ACTIVE);

        int bonus = 0;
        int limit = 3;

        if (p1Done) {
            bonus++;
            limit = 4; 
        }
        
        int finalStage = Math.min(rawStage + bonus, limit);

        if (finalStage == 3 && rawStage >= 3 && !p1Done && !isActive) {
            startProject(market, 1);
            isActive = true;
        }

        if (finalStage == 4 && rawStage >= 4 && !isActive) {
            startProject(market, 2);
            isActive = true;
        }

        int stability = -3 + finalStage;
        float defenseMult = 0.25f + (finalStage * 0.25f);
        float hazard = 0.20f - (finalStage * 0.05f);

        if (stability != 0) {
            market.getStability().modifyFlat(id, stability, desc);
        }
        
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, defenseMult, desc);
        
        if (hazard != 0) {
            market.getHazard().modifyFlat(id, hazard, desc);
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getStability().unmodifyFlat(id);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(id);
        market.getHazard().unmodifyFlat(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();
        
        int sizeBase = Math.max(0, market.getSize() - 3);
        boolean hasDefense = sol_industry_compat.getDefense(market) != null;
        int rawStage = sizeBase + (hasDefense ? 1 : 0);
        boolean p1Done = market.getMemoryWithoutUpdate().getBoolean(KEY_PROJ_1_DONE);

        int bonus = p1Done ? 1 : 0;
        int limit = p1Done ? 4 : 3;
        int finalStage = Math.min(rawStage + bonus, limit);

        if (market.isPlanetConditionMarketOnly()) {
            tooltip.addPara("Your officers estimate that with %s and %s, the security systems could be repurposed for use.", 
                pad, h, "sizeable infrastructure", "defenses");
        } else {
            if (finalStage <= 0) {
                tooltip.addPara("The AI security grid is functioning within original parameters, treating the unauthorized colonial population as hostile trespassers.", pad);
            } else if (finalStage == 1) {
                tooltip.addPara("Crude overrides have forced a ceasefire, though the aging friend-foe identification logic frequently flags colonists as combatants.", pad);
            } else if (finalStage == 2) {
                tooltip.addPara("The security lattice is largely compliant, though blind spots are common where the automated logic conflicts with modern transponder codes.", pad);
            } else if (finalStage == 3) {
                tooltip.addPara("The automated grid successfully supplements human patrols, providing reliable static defense and area surveillance.", pad);
            } else if (finalStage == 4) {
                tooltip.addPara("Deep-level reprogramming has harmonized the disparate security nodes, repurposing the grid into a formidable defensive bulwark.", pad);
            } 
        }

        if (!market.isPlanetConditionMarketOnly() && market.getMemoryWithoutUpdate().contains(KEY_DAYS_LEFT)) {
            float days = market.getMemoryWithoutUpdate().getFloat(KEY_DAYS_LEFT);
            tooltip.addPara("Architecture repurposing in progress. Estimated completion: %s days.", pad, h, "" + (int)days);
        }

        int stability = -3 + finalStage;
        float defenseMult = 0.25f + (finalStage * 0.25f);
        float hazard = 0.20f - (finalStage * 0.05f);

        if (stability != 0) {
            tooltip.addPara("%s stability", pad, h, (stability > 0 ? "+" : "") + stability);
        }
        
        tooltip.addPara("%s ground defense strength", pad, h, Misc.getRoundedValueFloat(defenseMult) + "x");
        
        if (hazard != 0) {
            String hazStr = (int)(Math.abs(hazard) * 100f) + "%";
            if (hazard > 0) {
                tooltip.addPara("%s hazard rating", pad, h, "+" + hazStr);
            } else {
                tooltip.addPara("%s hazard rating", pad, h, "-" + hazStr);
            }
        }
    }

    private void startProject(MarketAPI market, int projNum) {
        market.getMemoryWithoutUpdate().set(KEY_ACTIVE, true);
        
        float days = 100f + (float)(Math.random() * 400f);
        market.getMemoryWithoutUpdate().set(KEY_DAYS_LEFT, days);
        
        market.getMemoryWithoutUpdate().set("$sol_sec_proj_target", projNum);

        String msg = "";
        if (projNum == 1) {
            msg = "Protocols to repurpose the ancient security architecture have begun on " + market.getName() + ".";
        } else {
            msg = "Final optimization and subversion of the security grid has begun on " + market.getName() + ".";
        }

        Global.getSector().getCampaignUI().addMessage(msg, Misc.getBasePlayerColor());

        Global.getSector().addScript(new SecurityProjectScript(market));
    }

    private static class SecurityProjectScript implements EveryFrameScript {
        private final MarketAPI market;
        private boolean done = false;

        public SecurityProjectScript(MarketAPI market) {
            this.market = market;
        }

        @Override
        public boolean isDone() { return done; }

        @Override
        public boolean runWhilePaused() { return false; }

        @Override
        public void advance(float amount) {
            if (done) return;
            if (!market.hasCondition("sol_ai_security_systems")) {
                done = true;
                return;
            }

            if (market.getMemoryWithoutUpdate().contains(KEY_DAYS_LEFT)) {
                float days = Global.getSector().getClock().convertToDays(amount);
                float daysLeft = market.getMemoryWithoutUpdate().getFloat(KEY_DAYS_LEFT);
                daysLeft -= days;

                if (daysLeft <= 0) {
                    int projNum = market.getMemoryWithoutUpdate().getInt("$sol_sec_proj_target");
                    
                    if (projNum == 1) {
                        market.getMemoryWithoutUpdate().set(KEY_PROJ_1_DONE, true);
                        Global.getSector().getCampaignUI().addMessage(
                            "The security architecture on " + market.getName() + " has been successfully repurposed.",
                            Misc.getPositiveHighlightColor());
                            
                        market.getMemoryWithoutUpdate().unset(KEY_DAYS_LEFT);
                        market.getMemoryWithoutUpdate().unset(KEY_ACTIVE);
                        market.getMemoryWithoutUpdate().unset("$sol_sec_proj_target");
                        
                    } else if (projNum == 2) {
                        Global.getSector().getCampaignUI().addMessage(
                            "The security grid on " + market.getName() + " is now fully optimized and under colonial control.",
                            Misc.getPositiveHighlightColor());
                        
                        sol_remove_replace.execute(market, "sol_ai_security_systems", "sol_ai_security_systems_complete");

                        market.getMemoryWithoutUpdate().unset(KEY_DAYS_LEFT);
                        market.getMemoryWithoutUpdate().unset(KEY_ACTIVE);
                        market.getMemoryWithoutUpdate().unset("$sol_sec_proj_target");
                        market.getMemoryWithoutUpdate().unset(KEY_PROJ_1_DONE);
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