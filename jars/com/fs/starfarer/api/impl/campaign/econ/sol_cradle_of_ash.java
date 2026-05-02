package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sol_cradle_of_ash extends BaseHazardCondition implements MarketImmigrationModifier, EconomyTickListener {

    public static final String ID = "sol_cradle_of_ash";
    private static final String KEY_TOTAL_MONTHS = "$sol_coa_total_months";
    private static final String KEY_LAST_DAY = "$sol_coa_last_day";

    public void apply(String id) {
        super.apply(id);

        Global.getSector().getListenerManager().removeListener(this);
        Global.getSector().getListenerManager().addListener(this);

        // Luddic Path interest
        market.addTransientImmigrationModifier(this);

        
        MemoryAPI mem = market.getMemoryWithoutUpdate();
        int totalMonths = 0;
        if (mem.contains(KEY_TOTAL_MONTHS)) totalMonths = mem.getInt(KEY_TOTAL_MONTHS);
       
        // Base penalty: -20 staility
        // Every 3 months + 1
        // Like it wont deciv before the 60 months end
        int recovery = totalMonths / 3; 

        float penalty = -20f + recovery;
        
        if (penalty > 0) penalty = 0;
        
        String desc = condition.getName();

        if (penalty < 0) {
            market.getStability().modifyFlat(id, penalty, desc);
        } else {
            market.getStability().unmodify(id);
        }
    }

    public void unapply(String id) {
        super.unapply(id);
        
        Global.getSector().getListenerManager().removeListener(this);
        market.removeTransientImmigrationModifier(this);
        market.getStability().unmodify(id);

        if (market.hasCondition("habitable") && market.getPrimaryEntity() != null) {
            if (market.getPrimaryEntity().getId().equalsIgnoreCase("Earth")) {
                restoreEarthVisuals();
            }
        }
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.add(Factions.LUDDIC_PATH, 20f);
    }

    @Override
    public void reportEconomyTick(int iterIndex) {
        if (!market.hasCondition(ID)) {
            Global.getSector().getListenerManager().removeListener(this);
            return;
        }

        MemoryAPI mem = market.getMemoryWithoutUpdate();
        float currentDay = Global.getSector().getClock().getDay();

        if (!mem.contains(KEY_LAST_DAY)) {
            mem.set(KEY_LAST_DAY, currentDay);
            return;
        }

        if (currentDay != mem.getFloat(KEY_LAST_DAY)) {
            mem.set(KEY_LAST_DAY, currentDay);

            if (market.hasCondition("habitable")) {
                sendRemovalMessage();
                market.removeCondition(ID);
                Global.getSector().getListenerManager().removeListener(this);
            }
        }
    }

    @Override
    public void reportEconomyMonthEnd() {
        if (!market.hasCondition(ID)) return;

        MemoryAPI mem = market.getMemoryWithoutUpdate();
        int totalMonths = 0;
        if (mem.contains(KEY_TOTAL_MONTHS)) totalMonths = mem.getInt(KEY_TOTAL_MONTHS);
        
        totalMonths++;
        mem.set(KEY_TOTAL_MONTHS, totalMonths);
        
        market.reapplyCondition(ID);
    }

    private void restoreEarthVisuals() {
        PlanetAPI planet = market.getPlanetEntity();
        if (planet != null) {
            planet.getSpec().setTexture("graphics/planets/earth_tx.jpg");
            planet.getSpec().setPlanetColor(new Color(255, 255, 255, 255));
            planet.getSpec().setAtmosphereThickness(0.5f);
            planet.getSpec().setAtmosphereThicknessMin(20f);
            planet.getSpec().setAtmosphereColor(new Color(100, 150, 255, 200));
            planet.getSpec().setIconColor(new Color(100, 150, 255, 255));
            planet.getSpec().setTilt(23.4f);
            planet.getSpec().setPitch(71.6f);
            planet.getSpec().setCloudTexture("graphics/planets/clouds_terran03.png");
            planet.getSpec().setCloudColor(new Color(255, 255, 255, 50));
            planet.getSpec().setCloudRotation(10.0f);
            
            planet.applySpecChanges();
        }
    }

    private void sendRemovalMessage() {
        final String marketName = market.getName();
        final String text = "The mistakes that marr " + marketName + "'s past have been to some extent undone. The scars of the past are fading, replaced by a renewed hope for the future.";

        final MarketAPI marketRef = market;

        Global.getSector().getIntelManager().addIntel(new BaseIntelPlugin() {
            @Override
            public String getName() {
                return "Planet Recovered";
            }

            @Override
            public String getIcon() {
                return "graphics/icons/markets/farmland_poor.png";
            }

            @Override
            public Set<String> getIntelTags(SectorMapAPI map) {
                Set<String> tags = super.getIntelTags(map);
                tags.add("Colony events");
                return tags;
            }

            @Override
            public SectorEntityToken getMapLocation(SectorMapAPI map) {
                return marketRef.getPrimaryEntity();
            }

            @Override
            public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
                Color c = Misc.getBasePlayerColor();
                info.addPara(getName(), c, 0f);
                info.addPara("Ecological recovery on " + marketName, 0f, Misc.getGrayColor(), Misc.getHighlightColor(), marketName);
            }

            @Override
            public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
                Color h = Misc.getHighlightColor();
                float pad = 10f;
                
                String imagePath = "graphics/illustrations/killa_shrine.jpg";
                info.addImage(imagePath, width, pad);
                
                info.addPara(text, pad, h, marketName);
                
                info.addPara("The ecosystem has stabilized.", pad);
                bullet(info);
                info.addPara("Atmosphere stabilized", 0f, Misc.getPositiveHighlightColor(), "stabilized");
                info.addPara("Habitability restored", 0f, Misc.getPositiveHighlightColor(), "restored");
                unindent(info);
            }
        }, false);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();
        
        MemoryAPI mem = market.getMemoryWithoutUpdate();
        int totalMonths = 0;
        if (mem.contains(KEY_TOTAL_MONTHS)) totalMonths = mem.getInt(KEY_TOTAL_MONTHS);
        
        int recovery = totalMonths / 3;
        int currentPenalty = -20 + recovery;
        if (currentPenalty > 0) currentPenalty = 0;

        tooltip.addPara("The world has become a pilgrimage site for luddic fighters, a testament to humanity's destructive potential.", pad, h);
        
        if (currentPenalty < 0) {
            tooltip.addPara("Stability reduced by %s (Recovers by 1 every 3 months)", pad, h, "" + Math.abs(currentPenalty));
        } else {
             tooltip.addPara("Stability penalty has fully recovered.", pad, h);
        }
    }
}