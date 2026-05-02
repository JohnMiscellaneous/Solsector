package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonySizeChangeListener;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sol_oort_strikes extends BaseHazardCondition implements EconomyTickListener, ColonySizeChangeListener {

    public static final String ID = "sol_oort_strikes";
    public static final String KEY_MARGIN = "$sol_oort_safety_margin";
    public static final float MARGIN_TARGET = 2400f;
    
    public static final float STRIKE_CHANCE = 1f / 600f; 

    @Override
    public void apply(String id) {
        super.apply(id);
        
        Global.getSector().getListenerManager().removeListener(this);
        Global.getSector().getListenerManager().addListener(this);

        float margin = getMargin();
        if (margin <= 0) {
            market.getHazard().modifyFlat(id, 0.75f, condition.getName());
        } else {
            market.getHazard().unmodifyFlat(id);
        }

        Industry patrolHQ = market.getIndustry(Industries.PATROLHQ);
        if (patrolHQ != null) {
            patrolHQ.getUpkeep().modifyMult(id, 1.5f, condition.getName());
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        
        market.getHazard().unmodifyFlat(id);
        
        Industry patrolHQ = market.getIndustry(Industries.PATROLHQ);
        if (patrolHQ != null) {
            patrolHQ.getUpkeep().unmodifyMult(id);
        }

        Global.getSector().getListenerManager().removeListener(this);
    }

    @Override
    public void reportColonySizeChanged(MarketAPI market, int oldSize) {
        if (market != this.market) return;
        
        if (!market.hasCondition(ID)) {
            Global.getSector().getListenerManager().removeListener(this);
            return;
        }
    }

    @Override
    public void reportEconomyTick(int iterIndex) {
        if (!market.hasCondition(ID)) {
            Global.getSector().getListenerManager().removeListener(this);
            return;
        }
        
        runTickLogic();
    }

    @Override
    public void reportEconomyMonthEnd() {}

    protected void runTickLogic() {
        if (market.getSize() < 2) return;

        float margin = getMargin();
        boolean hasPatrols = sol_industry_compat.getMilitary(market) != null;

        if (hasPatrols) {
            margin += 3f;
            setMargin(margin);

            if (margin >= MARGIN_TARGET) {
                triggerClearCondition();
            }
        } else {
            if (margin > 0) {
                margin -= 3f;
                setMargin(margin);
            } else {
                if ((float) Math.random() < STRIKE_CHANCE) {
                    triggerStrike();
                }
            }
        }
    }

    protected float getMargin() {
        if (!market.getMemoryWithoutUpdate().contains(KEY_MARGIN)) return 0f;
        return market.getMemoryWithoutUpdate().getFloat(KEY_MARGIN);
    }

    protected void setMargin(float margin) {
        market.getMemoryWithoutUpdate().set(KEY_MARGIN, margin);
    }

    protected void triggerClearCondition() {
        if (market.isPlayerOwned()) {
            Global.getSector().getIntelManager().addIntel(new OortClearedIntel(market));
        }
        
        sol_remove_replace.execute(market, ID, null);
        
        market.getMemoryWithoutUpdate().unset(KEY_MARGIN);
        
        Global.getSector().getListenerManager().removeListener(this);
    }

    protected void triggerStrike() {
        int currentSize = market.getSize();
        // boolean sizeReduced = false;
        
        // at size 2 do not reduce colony size
        //if (currentSize > 2) {
        //     int newSize = currentSize - 1;
             
        // Swap population conditions manually to match new size
        //     market.removeCondition("population_" + currentSize);
        //    market.addCondition("population_" + newSize);
             
        //    market.setSize(newSize);
        //     sizeReduced = true;
        //}

        for (Industry ind : market.getIndustries()) {
            if (ind.canBeDisrupted()) {
                ind.setDisrupted(ind.getDisruptedDays() + 120f);
            }
        }

        if (market.isPlayerOwned() || market.getFaction().isPlayerFaction()) {
            Global.getSector().getIntelManager().addIntel(new OortStrikeIntel(market, false));
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        Color h = Misc.getHighlightColor();
        float pad = 10f;

        boolean hasPatrols = sol_industry_compat.getMilitary(market) != null;
        float margin = getMargin();

        tooltip.addSpacer(pad);

        if (hasPatrols) {
            tooltip.addPara("In short order, %s have cleaned up the neighborhood, diverting or blasting comets in the light, gravitational surveys have begun to build up the lead time to a safe margin.", 
                pad, h, "patrols");
            tooltip.addPara("Our safety margin is %s days.", pad, h, "" + (int)margin);
        } else {
            if (margin > 0) {
                tooltip.addPara("Without active %s the safety margin creeps down as the statistical boundary in which comets could be creeps ever towards %s.",
                    pad, h, "patrols", market.getName());
                tooltip.addPara("We estimate %s days until the safety margin closes.", pad, h, "" + (int)margin);
            } else {
                tooltip.addPara("Without %s to intercept comets the risk of another strike remains constant.", pad, h, "patrols");
                tooltip.addPara("Though the hazard from this comes in spikes and plateaus, automated financial systems spread the risk born from this out.", pad);
                tooltip.addPara("+%s hazard.", pad, h, "75%");
                tooltip.addPara("%s upkeep (Patrol HQ).", pad, h, "1.5x");
            }
        }
    }

    public static class OortClearedIntel extends BaseIntelPlugin {
        private MarketAPI market;
        public OortClearedIntel(MarketAPI market) { this.market = market; }

        @Override
        public String getName() {
            return "Oort Cloud Cleared";
        }

        @Override
        public String getIcon() {
            return "graphics/icons/campaign/slipstream_detection.png";
        }

        @Override
        public Set<String> getIntelTags(SectorMapAPI map) {
            Set<String> tags = super.getIntelTags(map);
            tags.add("Colony events");
            return tags;
        }

        @Override
        public SectorEntityToken getMapLocation(SectorMapAPI map) {
            return market.getPrimaryEntity();
        }

        @Override
        public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
            Color c = Misc.getBasePlayerColor();
            info.addPara(getName(), c, 0f);
            info.addPara("Safety guaranteed for " + market.getName(), 0f, Misc.getGrayColor(), Misc.getHighlightColor(), market.getName());
        }

        @Override
        public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
            Color h = Misc.getHighlightColor();
            Color p = Misc.getPositiveHighlightColor();
            float pad = 10f;
            
            info.addImage("illustrations/free_orbit.jpg", width, pad);
            
            info.addPara("Patrols have built up sufficient safety. While comets still emerge from the darkness on a trajectory towards %s, it is a simple task of monitoring and removal, now delegated to the most unfortunate crews.",
                pad, h, market.getName());
            
            info.setBulletedListMode(BaseIntelPlugin.BULLET);
            info.addPara("%s safe", pad, p, market.getName());
            info.setBulletedListMode(null);
        }
    }

    public static class OortStrikeIntel extends BaseIntelPlugin {
        private MarketAPI market;
        private boolean sizeReduced;

        public OortStrikeIntel(MarketAPI market, boolean sizeReduced) {
            this.market = market;
            this.sizeReduced = sizeReduced;
        }

        @Override
        public String getName() {
            return "Oort Strike";
        }

        @Override
        public String getIcon() {
            return "graphics/icons/markets/sol_oort_strike.png";
        }

        @Override
        public Set<String> getIntelTags(SectorMapAPI map) {
            Set<String> tags = super.getIntelTags(map);
            tags.add("Colony events");
            return tags;
        }

        @Override
        public SectorEntityToken getMapLocation(SectorMapAPI map) {
            return market.getPrimaryEntity();
        }

        @Override
        public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
            Color c = Misc.getBasePlayerColor();
            info.addPara(getName(), c, 0f);
            info.addPara("Catastrophic impact on " + market.getName(), 0f, Misc.getGrayColor(), Misc.getHighlightColor(), market.getName());
        }

        @Override
        public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
            Color h = Misc.getHighlightColor();
            Color n = Misc.getNegativeHighlightColor();
            float pad = 10f;
            
            info.addImage("graphics/illustrations/facility_explosion.jpg", width, pad);
            
            info.addPara("A comet has struck %s. Originating from the deep Oort cloud, the dark cometary fragment was detected in time to intercept - if there were any ships nearby. The resulting explosion has caused massive casualties and widespread infrastructure damage.",
                pad, h, market.getName());
            
            info.addPara("The planet has been devastated.", pad);
            
            bullet(info);
            // if (sizeReduced) {
            //    info.addPara("Population decimated", 0f, n, "decimated");
            // }
            info.addPara("Industries disrupted", 0f, n, "disrupted");
            unindent(info);
        }
    }
}