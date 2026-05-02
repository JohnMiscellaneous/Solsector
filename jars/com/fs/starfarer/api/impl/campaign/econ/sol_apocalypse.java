package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sol_apocalypse {

    public static void execute(final MarketAPI market, String conditionToRemove) {
        
        // Send Intel
        final String marketName = market.getName();
        final String text = "An antimatter containment cell fails on " + marketName + ", within minutes billions are killed in the ensuing failure of systems, communication channels, and judgement.";
        
        Global.getSector().getIntelManager().addIntel(new BaseIntelPlugin() {
            @Override
            public String getName() {
                return "Antimatter Holocaust";
            }

            @Override
            public String getIcon() {
                return "graphics/icons/markets/pollution.png";
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
                info.addPara("Catastrophe on " + marketName, 0f, Misc.getGrayColor(), Misc.getHighlightColor(), marketName);
            }

            @Override
            public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
                Color h = Misc.getHighlightColor();
                float pad = 10f;
                
                String imagePath = Global.getSettings().getSpriteName("illustrations", "facility_explosion");
                info.addImage(imagePath, width, pad);
                
                // Description
                info.addPara(text, pad, h, marketName);
                
                // Summary
                info.addPara("The planet has been devastated.", pad);
                bullet(info);
                info.addPara("Population decimated", 0f, Misc.getNegativeHighlightColor(), "decimated");
                info.addPara("Atmosphere ruined", 0f, Misc.getNegativeHighlightColor(), "ruined");
                info.addPara("Industries disrupted", 0f, Misc.getNegativeHighlightColor(), "disrupted");
                unindent(info);
            }
        }, false);

        // Visual Changes
        PlanetAPI planet = market.getPlanetEntity();
        if (planet != null) {
            planet.getSpec().setTexture("graphics/planets/radiated.jpg");
            planet.getSpec().setPlanetColor(new Color(255, 255, 255, 255));
            
            planet.getSpec().setAtmosphereThickness(0.25f); 
            planet.getSpec().setAtmosphereThicknessMin(10f);
            planet.getSpec().setAtmosphereColor(new Color(110, 100, 90, 160)); 
            
            planet.getSpec().setCloudTexture("graphics/planets/clouds_mars.png");
            planet.getSpec().setCloudColor(new Color(140, 110, 90, 180));
            planet.getSpec().setCloudRotation(15.0f); 
            
            planet.getSpec().setIconColor(new Color(155, 100, 60, 255));
            planet.applySpecChanges();
        }

        // Condition Changes
        market.removeCondition("habitable");
        
        if (!market.hasCondition(Conditions.THIN_ATMOSPHERE)) market.addCondition(Conditions.THIN_ATMOSPHERE);
        if (!market.hasCondition(Conditions.EXTREME_WEATHER)) market.addCondition(Conditions.EXTREME_WEATHER);

        market.removeCondition("sol_civilised_subpop");
        
        if (!market.hasCondition(Conditions.DECIVILIZED_SUBPOP)) {
            market.addCondition(Conditions.DECIVILIZED_SUBPOP);
        }

        if (!market.hasCondition("sol_dust_storms")) market.addCondition("sol_dust_storms");
        if (!market.hasCondition(Conditions.POLLUTION)) market.addCondition(Conditions.POLLUTION);
        if (!market.hasCondition("sol_cradle_of_ash")) market.addCondition("sol_cradle_of_ash");

        market.removeCondition("farmland_poor");
        market.removeCondition("farmland_adequate");
        market.removeCondition("farmland_rich");
        market.removeCondition("farmland_bountiful");

        // Market Size Change
        int currentSize = market.getSize();
        int newSize = Math.max(3, currentSize - 2);
        
        if (newSize != currentSize) {
            market.setSize(newSize);
        }
        
        // Disrupt All Industries
        for (Industry ind : market.getIndustries()) {
            ind.setDisrupted(365f);
        }

        // Remove the triggering condition
        if (conditionToRemove != null) {
            market.removeCondition(conditionToRemove);
        }
        
        market.reapplyConditions();
    }
}