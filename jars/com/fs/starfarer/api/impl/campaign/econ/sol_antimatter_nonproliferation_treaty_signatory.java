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
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sol_antimatter_nonproliferation_treaty_signatory extends BaseHazardCondition implements EconomyTickListener {

    public static final String ID = "sol_antimatter_nonproliferation_treaty_signatory";

    private static final String KEY_SETUP = "$sol_anpt_setup_done";
    private static final String KEY_TIER = "$sol_anpt_tier"; 
    private static final String KEY_DAYS = "$sol_anpt_days";
    private static final String KEY_LAST_DAY = "$sol_anpt_last_day";
    private static final String KEY_PENDING = "$sol_anpt_placeholder_pending";

    // Tiers
    private static final int TIER_SECRET_BREACH = 0; // Option 0
    private static final int TIER_MINIMAL = 1;       // Option 1
    private static final int TIER_MODERATE = 2;      // Option 2
    private static final int TIER_MAXIMUM = 3;       // Option 3

    public void apply(String id) {
        super.apply(id);
        
        // Prevent duplicate listeners
        Global.getSector().getListenerManager().removeListener(this);
        // Register Listener
        Global.getSector().getListenerManager().addListener(this);

        MemoryAPI mem = market.getMemoryWithoutUpdate();
        
        if (mem.getBoolean(KEY_PENDING) || !mem.getBoolean(KEY_SETUP)) return;

        int tier = TIER_MODERATE; 
        if (mem.contains(KEY_TIER)) tier = mem.getInt(KEY_TIER);
        
        String desc = condition.getName();

        Industry popInd = market.getIndustry(Industries.POPULATION);
        if (popInd != null) {
            float upkeepFlat = 0f;
            
            if (tier == TIER_MINIMAL) upkeepFlat = 15000f;
            else if (tier == TIER_MODERATE) upkeepFlat = 30000f;
            else if (tier == TIER_MAXIMUM) upkeepFlat = 60000f;

            if (market.hasCondition("sol_civilised_subpop")) {
                upkeepFlat /= 3f;
            }

            if (upkeepFlat > 0f) {
                popInd.getUpkeep().modifyFlat(id, upkeepFlat, desc);
            }
        }

        if (tier != TIER_SECRET_BREACH) {
            Industry fuelInd = sol_industry_compat.getFuelProduction(market);
            if (fuelInd != null) {
                fuelInd.getSupply(Commodities.FUEL).getQuantity().modifyMult(id, 0f, "Treaty Ban");
            }
        }
    }

    public void unapply(String id) {
        super.unapply(id);
        
        // Remove Listener
        Global.getSector().getListenerManager().removeListener(this);

        Industry popInd = market.getIndustry(Industries.POPULATION);
        if (popInd != null) {
            popInd.getUpkeep().unmodify(id);
        }
        
        Industry fuelInd = sol_industry_compat.getFuelProduction(market);
        if (fuelInd != null) {
            fuelInd.getSupply(Commodities.FUEL).getQuantity().unmodify(id);
        }
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        if (Global.getSector().isPaused()) return;
        if (Global.getSector().getCampaignUI().isShowingDialog()) return;

        MemoryAPI mem = market.getMemoryWithoutUpdate();

        if (mem.getBoolean(KEY_PENDING)) {
            if (!market.isPlanetConditionMarketOnly()) {
                if (market.getSize() >= 3) {
                    mem.unset(KEY_PENDING);
                } else {
                    return;
                }
            } else {
                return;
            }
        }

        if (!mem.getBoolean(KEY_SETUP)) {
            
            if (mem.contains(KEY_TIER)) {
                mem.set(KEY_SETUP, true);
                return;
            }

            if (market.isPlayerOwned()) {
                Global.getSector().getCampaignUI().showInteractionDialog(
                    new ResourceAllocationDialog(market, ID), 
                    null
                );
            } else {
                mem.set(KEY_TIER, TIER_MODERATE);
                mem.set(KEY_SETUP, true);
            }
        }
    }

    @Override
    public void reportEconomyTick(int iterIndex) {
        if (!market.hasCondition(ID)) {
            Global.getSector().getListenerManager().removeListener(this);
            return;
        }

        MemoryAPI mem = market.getMemoryWithoutUpdate();
        
        // Ensure setup is done before running logic
        if (!mem.getBoolean(KEY_SETUP)) return;

        // Timer Logic
        float elapsed = 0f;
        if (mem.contains(KEY_DAYS)) elapsed = mem.getFloat(KEY_DAYS);
        elapsed += 1f;
        mem.set(KEY_DAYS, elapsed);

        int tier = mem.getInt(KEY_TIER);

        // Outcomes
        
        // Outcome A: Secret Breach (Apocalypse)
        if (tier == TIER_SECRET_BREACH) {
            if (elapsed >= 150f) { // ~15 Ticks
                sol_apocalypse.execute(market, ID);
            }
        }
        
        // Outcome B: Minimal Compliance (Failure -> Arms Race)
        else if (tier == TIER_MINIMAL) {
            if (elapsed >= 150f) { // ~20 Ticks
                if (market.isPlayerOwned()) {
                    sendArmsRaceIntel(market);
                }
                sol_remove_replace.execute(market, ID, "sol_antimatter_nonproliferation_treaty_breacher");
                // Explicitly remove listener immediately
                Global.getSector().getListenerManager().removeListener(this);
            }
        } 
        
        // Outcome C: Moderate/Max Compliance (Success -> Peace)
        else if (tier == TIER_MODERATE || tier == TIER_MAXIMUM) {
            if (elapsed >= 300f) { // ~30 Ticks
                if (market.isPlayerOwned()) {
                    sendTreatySuccessIntel(market);
                }
                
                // Only Moderate sets forbidden. Maximum allows unification.
                if (tier == TIER_MODERATE) {
                    mem.set("$unification_forbidden", true);
                }
                
                sol_remove_replace.execute(market, ID, null);
                Global.getSector().getListenerManager().removeListener(this);
            }
        }
    }

    @Override
    public void reportEconomyMonthEnd() {
        // Not used
    }

    // INTEL HELPERS 

    private void sendArmsRaceIntel(final MarketAPI market) {
        final String marketName = market.getName();
        final String text = "International observers exposed severe discrepancies in the antimatter stockpiles of several minor signatories. Instead of condemnation, this revelation triggered a panic; major powers have cited these hidden arsenals as proof that disarmament is suicide. The treaty has collapsed, replaced by a frantic scramble for deterrence.";

        Global.getSector().getIntelManager().addIntel(new BaseIntelPlugin() {
            @Override public String getName() { return "Treaty Collapsed"; }
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
                info.addPara("Antimatter arms race on " + marketName, 0f, Misc.getGrayColor(), Misc.getHighlightColor(), marketName);
            }
            @Override public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
                Color h = Misc.getHighlightColor();
                float pad = 10f;
                String imagePath = "graphics/icons/industry/advanced_fuel_production.png";
                info.addImage(imagePath, width, pad);
                info.addPara(text, pad, h, marketName);
                info.addPara("Diplomacy failed.", pad);
                bullet(info);
                info.addPara("Treaty dissolved", 0f, Misc.getNegativeHighlightColor(), "dissolved");
                info.addPara("Antimatter production resumed", 0f, Misc.getNegativeHighlightColor(), "resumed");
                unindent(info);
            }
        }, false);
    }

    private void sendTreatySuccessIntel(final MarketAPI market) {
        final String marketName = market.getName();
        final String text = "The critical window of danger has passed. Through rigorous enforcement and mutual surveillance, the signatory nations have successfully navigated the antimatter crisis. While tensions remain, the immediate threat of planetary annihilation has receded, though the scars of the panic will linger in diplomatic relations for a generation.";

        Global.getSector().getIntelManager().addIntel(new BaseIntelPlugin() {
            @Override public String getName() { return "Antimatter Crisis Averted"; }
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
                info.addPara("Crisis weathered on " + marketName, 0f, Misc.getGrayColor(), Misc.getHighlightColor(), marketName);
            }
            @Override public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
                Color h = Misc.getHighlightColor();
                float pad = 10f;
                String imagePath = Global.getSettings().getSpriteName("illustrations", "corporate_lobby");
                info.addImage(imagePath, width, pad);
                info.addPara(text, pad, h, marketName);
                info.addPara("Stability restored.", pad);
                bullet(info);
                info.addPara("Treaty successfully upheld", 0f, Misc.getPositiveHighlightColor(), "upheld");
                unindent(info);
            }
        }, false);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        
        MemoryAPI mem = market.getMemoryWithoutUpdate();
        
        // Placeholder Tooltip
        if (mem.getBoolean(KEY_PENDING)) {
            tooltip.addPara("The factions of this world have an Antimatter Non-Proliferation Treaty, and, should we colonize it, we will be asked to sign it, and it makes sense to do so - after all, what we actually do is up to us.", 10f, Misc.getHighlightColor());
            return;
        }
        
        if (!mem.getBoolean(KEY_SETUP)) {
             tooltip.addPara("Awaiting resource allocation decision...", 10f, Misc.getHighlightColor());
             return;
        }

        int tier = mem.getInt(KEY_TIER);
        float pad = 10f;
        Color h = Misc.getHighlightColor();

        // General Flavor
        tooltip.addPara("The factions of this market have ratified an Antimatter Non-Proliferation Treaty, agreeing to halt all antimatter fuel production to prevent mutually assured planetary annihilation.", pad);

        // Specific Tier Flavor (3rd Person Condition Style)
        if (tier == TIER_MINIMAL) {
             tooltip.addPara("Compliance is technically being met, though inspectors report frequent 'scheduling conflicts' and limited access.", pad);
        } else if (tier == TIER_MODERATE) {
             tooltip.addPara("A standard inspection regime is in place. Enforcers have free access to industrial sectors.", pad);
        } else if (tier == TIER_MAXIMUM) {
             tooltip.addPara("Sovereignty is a polite fiction here. International task forces operate with impunity, authorized to bypass local laws, seize encrypted data, and conduct no-knock raids at the slightest hint of an energy anomaly. Safety is absolute, and so is the surveillance.", pad);
        } else if (tier == TIER_SECRET_BREACH) {
            tooltip.addPara("To the outside world, we are compliant. Behind closed doors, the accelerators continue to run.", pad);
        }

        tooltip.addSpacer(10f);

        // Stats (Display full value, backend handles division)
        if (tier == TIER_MINIMAL) {
            tooltip.addPara("+%s Population upkeep (Minimal Compliance)", pad, h, "15,000\u00A2");
        } else if (tier == TIER_MODERATE) {
            tooltip.addPara("+%s Population upkeep (Strict Compliance)", pad, h, "30,000\u00A2");
        } else if (tier == TIER_MAXIMUM) {
            tooltip.addPara("+%s Population upkeep (Absolute Compliance)", pad, h, "60,000\u00A2");
        } else if (tier == TIER_SECRET_BREACH) {
            tooltip.addPara("Treaty signed (Deceptive).", pad, h);
        }

        if (tier != TIER_SECRET_BREACH) {
            tooltip.addPara("%s Fuel production", pad, h, "0x");
        }
    }

    public static class ResourceAllocationDialog implements InteractionDialogPlugin {
        private InteractionDialogAPI dialog;
        private MarketAPI market;
        private String conditionId;

        public ResourceAllocationDialog(MarketAPI market, String conditionId) {
            this.market = market;
            this.conditionId = conditionId;
        }

        public void init(InteractionDialogAPI dialog) {
            this.dialog = dialog;
            TextPanelAPI text = dialog.getTextPanel();
            OptionPanelAPI options = dialog.getOptionPanel();

            text.addPara("The governor of " + market.getName() + " asks how many resources should be dedicated to the joint inspection and enforcement efforts required by the newly signed treaty.");

            options.addOption("None. We will sign the treaty but continue production in secret.", "opt_none");
            options.addOption("The minimal amount required by the treaty. (15,000\u00A2 Upkeep, 0x Fuel Production)", "opt_min");
            options.addOption("A significant amount of resources to ensure compliance. (30,000\u00A2 Upkeep, 0x Fuel Production)", "opt_mod");
            options.addOption("The maximum possible resources. We must be above both reproach and boundaries. (60,000\u00A2 Upkeep, 0x Fuel Production)", "opt_max");
        }

        public void optionSelected(String optionText, Object optionData) {
            String option = (String) optionData;
            MemoryAPI mem = market.getMemoryWithoutUpdate();

            // OUTCOMES
            if ("opt_none".equals(option)) {
                mem.set(KEY_TIER, TIER_SECRET_BREACH);
                dialog.getTextPanel().addPara("We have publicly signed the document, but our factories run in secret. An arms race has been avoided in name only.");
            }
            else if ("opt_min".equals(option)) {
                mem.set(KEY_TIER, TIER_MINIMAL);
                dialog.getTextPanel().addPara("We have agreed to the treaty's terms, though our legal teams have ensured the inspections will be strictly limited to the agreed-upon schedules. Compliance is technical, not spiritual.");
            } 
            else if ("opt_mod".equals(option)) {
                mem.set(KEY_TIER, TIER_MODERATE);
                dialog.getTextPanel().addPara("A robust inspection regime has been established. We have granted access to our facilities to ensure mutual trust, striking a difficult balance between security and transparency.");
            }
            else if ("opt_max".equals(option)) {
                mem.set(KEY_TIER, TIER_MAXIMUM);
                dialog.getTextPanel().addPara("We have thrown open the doors - specifically, everyone else's. Our funding supports elite teams of international inspectors authorized to seize hard drives, bypass locks, and drag facility managers from their beds in the dead of night. There will be no secrets, because there is no place left to hide them.");
            }

            mem.set(KEY_SETUP, true);
            market.reapplyCondition(conditionId);

            dialog.getOptionPanel().clearOptions();
            dialog.getOptionPanel().addOption("Leave", "leave");
            
            if ("leave".equals(option)) {
                dialog.dismiss();
            }
        }

        public void optionMousedOver(String optionText, Object optionData) {}
        public void advance(float amount) {}
        public void backFromEngagement(EngagementResultAPI battleResult) {}
        public Object getContext() { return null; }
        public Map<String, MemoryAPI> getMemoryMap() { return null; }
    }
}