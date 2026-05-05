package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sol_world_war extends BaseHazardCondition implements EconomyTickListener {

    public static final String ID = "sol_world_war";
    
    private static final String KEY_STATE = "$sol_ww_state";
    private static final String KEY_TIMER = "$sol_ww_timer";
    private static final String KEY_TOTAL_DAYS = "$sol_ww_total_duration";

    private static final String KEY_ANPT_TIER = "$sol_anpt_tier";
    private static final String KEY_ANPT_SETUP = "$sol_anpt_setup_done";
    private static final String KEY_ANPT_PENDING = "$sol_anpt_placeholder_pending";

    private static final int STATE_WAR = 0;
    private static final int STATE_PROLIFERATION = 1;
    private static final int STATE_CEASEFIRE = 2;

    public void apply(String id) {
        super.apply(id);
        
        Global.getSector().getListenerManager().removeListener(this);

        Global.getSector().getListenerManager().addListener(this);

        MemoryAPI mem = market.getMemoryWithoutUpdate();
        int state = 0;
        if (mem.contains(KEY_STATE)) state = mem.getInt(KEY_STATE);
        
        String desc = condition.getName();

        if (state == STATE_CEASEFIRE) {
            market.getHazard().unmodifyFlat(id);
        } else {
            market.getHazard().modifyFlat(id, 0.25f, desc);
        }

        for (Industry solindustry : market.getIndustries()) {
            solindustry.getIncome().unmodify(id + "_mar_inc");
            solindustry.getIncome().unmodify(id + "_weps_inc");
            solindustry.getIncome().unmodify(id + "_fuel_inc");

            int marinesQty = 0;
            int wepsQty = 0;
            int fuelQty = 0;

            if (solindustry.getSupply(Commodities.MARINES) != null) {
                marinesQty = solindustry.getSupply(Commodities.MARINES).getQuantity().getModifiedInt();
            }
            if (solindustry.getSupply(Commodities.HAND_WEAPONS) != null) {
                wepsQty = solindustry.getSupply(Commodities.HAND_WEAPONS).getQuantity().getModifiedInt();
            }
            if (solindustry.getSupply(Commodities.FUEL) != null) {
                fuelQty = solindustry.getSupply(Commodities.FUEL).getQuantity().getModifiedInt();
            }

            if (state != STATE_CEASEFIRE) {
                if (marinesQty > 0) solindustry.getIncome().modifyFlat(id + "_mar_inc", marinesQty * 1000f, desc);
                if (wepsQty > 0) solindustry.getIncome().modifyFlat(id + "_weps_inc", wepsQty * 3000f, desc);
            }

            if (fuelQty > 0) {
                float rate = 6000f; 
                if (state == STATE_PROLIFERATION) rate = 4000f;
                if (state == STATE_CEASEFIRE) rate = 2000f;
                solindustry.getIncome().modifyFlat(id + "_fuel_inc", fuelQty * rate, desc);
            }
        }
    }

    public void unapply(String id) {
        super.unapply(id);
        
        Global.getSector().getListenerManager().removeListener(this);
        
        market.getHazard().unmodifyFlat(id);
        for (Industry solindustry : market.getIndustries()) {
            solindustry.getIncome().unmodify(id + "_mar_inc");
            solindustry.getIncome().unmodify(id + "_weps_inc");
            solindustry.getIncome().unmodify(id + "_fuel_inc");
        }
    }

    @Override
    public void reportEconomyTick(int iterIndex) {

        if (!market.hasCondition(ID)) {
            Global.getSector().getListenerManager().removeListener(this);
            return;
        }

        MemoryAPI mem = market.getMemoryWithoutUpdate();

        if (market.getSurveyLevel() == SurveyLevel.FULL) {
            float totalDuration = 0f;
            if (mem.contains(KEY_TOTAL_DAYS)) totalDuration = mem.getFloat(KEY_TOTAL_DAYS);
            totalDuration += 1f;
            mem.set(KEY_TOTAL_DAYS, totalDuration);

            if (totalDuration >= 100f) {
                if (market.isPlayerOwned()) {
                    sendPeaceIntel(market);
                }
                sol_remove_replace.execute(market, ID, null);

                Global.getSector().getListenerManager().removeListener(this);
                return;
            }
        }

        int state = mem.getInt(KEY_STATE);
        if (state == STATE_PROLIFERATION || state == STATE_CEASEFIRE) {
            float timer = mem.getFloat(KEY_TIMER) - 1f;
            mem.set(KEY_TIMER, timer);

            if (timer <= 0) {
                if (state == STATE_PROLIFERATION) {

                    mem.set(KEY_STATE, STATE_CEASEFIRE);
                    mem.set(KEY_TIMER, 3f); 
                    
                    if (market.isPlayerOwned()) {
                        sendCeasefireIntel(market);
                    }
                    
                    market.reapplyCondition(ID);
                } else {

                    if (market.isPlayerOwned()) {
                        if (!Global.getSector().getCampaignUI().isShowingDialog()) {
                            Global.getSector().getCampaignUI().showInteractionDialog(new CounterProliferationDialog(market, ID), null);
                        }
                    } else {

                        if (market.isPlanetConditionMarketOnly()) {
                            mem.set(KEY_ANPT_PENDING, true);
                            sol_remove_replace.execute(market, ID, "sol_antimatter_nonproliferation_treaty_signatory");
                            Global.getSector().getListenerManager().removeListener(this);
                            return;
                        }

                        Industry fuelInd = sol_industry_compat.getFuelProduction(market);
                        boolean hasFuel = (fuelInd != null); 

                        String nextCond = "sol_antimatter_nonproliferation_treaty_signatory";
                        if (hasFuel) {
                            nextCond = "sol_antimatter_nonproliferation_treaty_breacher";
                            sendTreatyResultIntel(market, false, -1);
                        } else {
                            mem.set(KEY_ANPT_TIER, 2);
                            mem.set(KEY_ANPT_SETUP, true);
                            sendTreatyResultIntel(market, true, 2);
                        }
                        
                        sol_remove_replace.execute(market, ID, nextCond);
                        Global.getSector().getListenerManager().removeListener(this);
                    }
                    return;
                }
            }
        }

        if (state != STATE_CEASEFIRE && Math.random() < 0.25f) {
            Industry heavy = sol_industry_compat.getHeavyIndustry(market);
            if (heavy != null) heavy.setDisrupted(30f);
        }

        if (state == STATE_WAR) {
            Industry fuel = sol_industry_compat.getFuelProduction(market);
            if (fuel != null && Math.random() < 0.10f) {
                mem.set(KEY_STATE, STATE_PROLIFERATION);
                mem.set(KEY_TIMER, 3f);
                
                if (market.isPlayerOwned()) {
                    sendProliferationIntel(market);
                }
                
                market.reapplyCondition(ID);
            }
        }
    }

    @Override
    public void reportEconomyMonthEnd() {}

    private void sendPeaceIntel(final MarketAPI market) {
        final String marketName = market.getName();
        final String text = "Even through the walls of your outpost on " + marketName + ", the cheers can be heard. The final holdouts of the losing side have unconditionally surrendered. With this comes the start of a new era: an end to the violence, an end to the danger, and an end to the markup you placed on weapons.";

        Global.getSector().getIntelManager().addIntel(new BaseIntelPlugin() {
            @Override public String getName() { return "Global Conflict Ended"; }
            @Override public String getIcon() { return "graphics/icons/intel/peace.png"; }
            @Override public Set<String> getIntelTags(SectorMapAPI map) {
                Set<String> tags = super.getIntelTags(map);
                tags.add("Colony events");
                return tags;
            }
            @Override public SectorEntityToken getMapLocation(SectorMapAPI map) { return market.getPrimaryEntity(); }
            @Override public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
                Color c = Misc.getBasePlayerColor();
                info.addPara(getName(), c, 0f);
                info.addPara("Peace restored on " + marketName, 0f, Misc.getGrayColor(), Misc.getHighlightColor(), marketName);
            }
            @Override public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
                Color h = Misc.getHighlightColor();
                float pad = 10f;
                String imagePath = Global.getSettings().getSpriteName("illustrations", "hegemony_bar");
                info.addImage(imagePath, width, pad);
                info.addPara(text, pad, h, marketName);
                info.addPara("War over.", pad);
                bullet(info);
                info.addPara("Hazard rating normalized", 0f, Misc.getPositiveHighlightColor(), "normalized");
                info.addPara("Export prices normalized", 0f, Misc.getNegativeHighlightColor(), "normalized");
                unindent(info);
            }
        }, false);
    }

    private void sendProliferationIntel(final MarketAPI market) {
        final String marketName = market.getName();
        final String text = "Local factions, having experienced the power of antimatter firsthand, have purchased the loyalty of several high-level managers at our facility. We are already seeing lower bids on our exports; those who possess the knowledge no longer need to buy, while those who don't are being ground into dust.";

        Global.getSector().getIntelManager().addIntel(new BaseIntelPlugin() {
            @Override public String getName() { return "Antimatter Technology Leaked"; }
            @Override public String getIcon() { return "graphics/icons/markets/am_fuel_facility.png"; }
            @Override public Set<String> getIntelTags(SectorMapAPI map) {
                Set<String> tags = super.getIntelTags(map);
                tags.add("Colony events");
                return tags;
            }
            @Override public SectorEntityToken getMapLocation(SectorMapAPI map) { return market.getPrimaryEntity(); }
            @Override public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
                Color c = Misc.getBasePlayerColor();
                info.addPara(getName(), c, 0f);
                info.addPara("Security breach on " + marketName, 0f, Misc.getGrayColor(), Misc.getHighlightColor(), marketName);
            }
            @Override public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
                Color h = Misc.getHighlightColor();
                float pad = 10f;
                String imagePath = "graphics/icons/industry/fuel_production.png";
                info.addImage(imagePath, width, pad);
                info.addPara(text, pad, h, marketName);
                info.addPara("Market conditions updated.", pad);
                bullet(info);
                info.addPara("Fuel income reduced", 0f, Misc.getNegativeHighlightColor(), "reduced");
                info.addPara("Technology proliferation imminent", 0f, Misc.getNegativeHighlightColor(), "imminent");
                unindent(info);
            }
        }, false);
    }

    private void sendCeasefireIntel(final MarketAPI market) {
        final String marketName = market.getName();
        final String text = "Through elimination or espionage all factions have the knowledge and ability to produce antimatter. The playing field has leveled, but at a terrifying altitude. A ceasefire has been reached on " + marketName + ", brought on by the several order of magnitude increase in destructive capability.";

        Global.getSector().getIntelManager().addIntel(new BaseIntelPlugin() {
            @Override public String getName() { return "Ceasefire Negotiated"; }
            @Override public String getIcon() { return market.getFaction().getCrest(); }
            @Override public Set<String> getIntelTags(SectorMapAPI map) {
                Set<String> tags = super.getIntelTags(map);
                tags.add("Colony events");
                return tags;
            }
            @Override public SectorEntityToken getMapLocation(SectorMapAPI map) { return market.getPrimaryEntity(); }
            @Override public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
                Color c = Misc.getBasePlayerColor();
                info.addPara(getName(), c, 0f);
                info.addPara("Hostilities suspended on " + marketName, 0f, Misc.getGrayColor(), Misc.getHighlightColor(), marketName);
            }
            @Override public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
                Color h = Misc.getHighlightColor();
                float pad = 10f;
                String imagePath = Global.getSettings().getSpriteName("illustrations", "corporate_lobby");
                info.addImage(imagePath, width, pad);
                info.addPara(text, pad, h, marketName);
                info.addPara("Global conflict paused.", pad);
                bullet(info);
                info.addPara("Hazard rating normalized", 0f, Misc.getPositiveHighlightColor(), "normalized");
                info.addPara("War economy demand dropped", 0f, Misc.getNegativeHighlightColor(), "dropped");
                unindent(info);
            }
        }, false);
    }

    private static void sendTreatyResultIntel(final MarketAPI market, final boolean signed, final int tier) {
        final String marketName = market.getName();
        final String header = signed ? "Antimatter Treaty Ratified" : "Antimatter Arms Race";
        
        Global.getSector().getIntelManager().addIntel(new BaseIntelPlugin() {
            @Override public String getName() { return header; }
            @Override public String getIcon() { 
                return signed ? "graphics/icons/intel/peace.png" : "graphics/icons/markets/am_fuel_facility.png"; 
            }
            @Override public Set<String> getIntelTags(SectorMapAPI map) {
                Set<String> tags = super.getIntelTags(map);
                tags.add("Colony events");
                return tags;
            }
            @Override public SectorEntityToken getMapLocation(SectorMapAPI map) { return market.getPrimaryEntity(); }
            @Override public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
                Color c = Misc.getBasePlayerColor();
                info.addPara(getName(), c, 0f);
                String sub = signed ? "Treaty signed on " + marketName : "Negotiations failed on " + marketName;
                info.addPara(sub, 0f, Misc.getGrayColor(), Misc.getHighlightColor(), marketName);
            }
            @Override public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
                Color h = Misc.getHighlightColor();
                float pad = 10f;
                
                String imagePath = signed ? Global.getSettings().getSpriteName("illustrations", "corporate_lobby") : "graphics/icons/industry/advanced_fuel_production.png";
                info.addImage(imagePath, width, pad);
                
                if (signed) {
                    info.addPara("The Antimatter Non-Proliferation Treaty has been signed on " + marketName + ".", pad, h, marketName);
                    String commitment = tier == 0 ? "Deceptive (Secret Breach)" : tier == 1 ? "Minimal" : tier == 3 ? "Maximum" : "Significant";
                    info.addPara("Inspection Resources: %s", pad, h, commitment);
                } else {
                    info.addPara("Negotiations regarding the control of antimatter production on " + marketName + " have collapsed.", pad, h, marketName);
                    info.addPara("With the treaty dead, an Antimatter Arms Race has begun. Factions are now openly stockpiling strategic weapons.", pad);
                }
            }
        }, false);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
    float pad = 10f;
    Color h = Misc.getHighlightColor();
    Color neg = Misc.getNegativeHighlightColor();
    MemoryAPI mem = market.getMemoryWithoutUpdate();
    int state = mem.getInt(KEY_STATE);

    if (state != STATE_CEASEFIRE) tooltip.addPara("%s hazard rating", pad, h, "+25%");
    
    String fuelRate = (state == STATE_WAR) ? "6,000\u00A2" : (state == STATE_PROLIFERATION) ? "4,000\u00A2" : "2,000\u00A2";
    if (state != STATE_CEASEFIRE) {
        tooltip.addPara("+%s export income per unit (Marines)", pad, h, "1,000\u00A2");
        tooltip.addPara("+%s export income per unit (Heavy Weapons)", pad, h, "3,000\u00A2");
    }
    tooltip.addPara("+%s export income per unit (Fuel)", pad, h, fuelRate);

    if (state == STATE_PROLIFERATION) {
        tooltip.addPara("Concerningly, several factions have begun copying our antimatter production technique.", pad, h, "copying our antimatter production technique");
    }
    if (state == STATE_CEASEFIRE) {
        tooltip.addPara("A ceasefire has been reached between all parties brought on by their order of magnitude increase in destructive capability.", pad, h, "ceasefire");
    }

    if (state != STATE_CEASEFIRE) {
        tooltip.addPara("Given the population of this world your officers are uneasy with the idea of selling antimatter weapons to its subpopulation.",
            pad, neg,
            "Given the population of this world your officers are uneasy with the idea of selling antimatter weapons to its subpopulation.");

        if (sol_industry_compat.getFuelProduction(market) != null) {
            tooltip.addPara("Several officers have resigned in protest as cities get turned to plasma and the atmosphere thins from airbursts energetic enough to vent the planet.",
                pad, neg,
                "Several officers have resigned in protest as cities get turned to plasma and the atmosphere thins from airbursts energetic enough to vent the planet.");
        }
    }
}

    public static class CounterProliferationDialog implements InteractionDialogPlugin {
        private InteractionDialogAPI dialog;
        private MarketAPI market;
        private String currentConditionId;

        public CounterProliferationDialog(MarketAPI market, String currentConditionId) {
            this.market = market;
            this.currentConditionId = currentConditionId;
        }

        public void init(InteractionDialogAPI dialog) {
            this.dialog = dialog;
            dialog.getVisualPanel().showImagePortion("illustrations", "corporate_lobby", 640, 400, 0, 0, 480, 300);

            TextPanelAPI text = dialog.getTextPanel();
            text.addPara("The governor of " + market.getName() + " has requested input on whether to sign on to the antimatter counterproliferation treaty.");
            text.addPara("The treaty stipulates a complete cessation of antimatter production and significant resources committed to inspections.");

            OptionPanelAPI options = dialog.getOptionPanel();
            options.addOption("Sign the treaty", "sign");
            options.addOption("Refuse to sign", "refuse");
        }

        public void optionSelected(String optionText, Object optionData) {
            String option = (String) optionData;
            if ("sign".equals(option)) {
                showCommitmentOptions();
            } else if ("refuse".equals(option)) {
                dialog.getTextPanel().addPara("You refuse to sign. The treaty is doomed.");
                sol_remove_replace.execute(market, currentConditionId, "sol_antimatter_nonproliferation_treaty_breacher");
                sendTreatyResultIntel(market, false, -1);
                finalizeDialog();
            } else if (option.startsWith("tier_")) {
                finalizeWithTier(Integer.parseInt(option.replace("tier_", "")));
            } else if ("leave".equals(option)) {
                dialog.dismiss();
            }
        }

        private void showCommitmentOptions() {
            dialog.getTextPanel().addPara("The governor asks how many resources should be dedicated to the inspections.");
            OptionPanelAPI options = dialog.getOptionPanel();
            options.clearOptions();
            options.addOption("None. Sign but continue production in secret.", "tier_0");
            options.addOption("The minimal amount required (15,000\u00A2 Upkeep, 0x Fuel Production)", "tier_1");
            options.addOption("A significant amount (30,000\u00A2 Upkeep, 0x Fuel Production)", "tier_2");
            options.addOption("The maximum possible (60,000\u00A2 Upkeep, 0x Fuel Production)", "tier_3");
        }

        private void finalizeWithTier(int tier) {
            MemoryAPI mem = market.getMemoryWithoutUpdate();
            mem.set(KEY_ANPT_TIER, tier);
            mem.set(KEY_ANPT_SETUP, true);
            dialog.getTextPanel().addPara("Resources have been allocated. The treaty is active.");
            sol_remove_replace.execute(market, currentConditionId, "sol_antimatter_nonproliferation_treaty_signatory");
            sendTreatyResultIntel(market, true, tier);
            finalizeDialog();
        }

        private void finalizeDialog() {
            dialog.getOptionPanel().clearOptions();
            dialog.getOptionPanel().addOption("Leave", "leave");
        }

        public void optionMousedOver(String optionText, Object optionData) {}
        public void advance(float amount) {}
        public void backFromEngagement(EngagementResultAPI battleResult) {}
        public Object getContext() { return null; }
        public Map<String, MemoryAPI> getMemoryMap() { return null; }
    }
}