package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class sol_industry_compat {
    
    // Static check for AOTD existence
    public static final boolean HAS_AOTD = Global.getSettings().getModManager().isModEnabled("aotd_vok");

    // --- AOTD / Modded Industry IDs ---
    
    // Farming
    public static final String AOTD_MONOCULTURE_PLOTS = "aotd_monoculture_plots";
    public static final String AOTD_ARTISANAL_FARMING = "aotd_artisanal_farming";
    public static final String AOTD_SUBSIDISED_FARMING = "aotd_subsidised_farming";
    
    // Aquaculture
    public static final String AOTD_FISHING_HARBOUR = "aotd_fishing_harbour";

    // --- Tags (Matching vanilla tags from industries.csv) ---
    public static final String TAG_LIGHT_INDUSTRY = "lightindustry";
    public static final String TAG_HEAVY_INDUSTRY = "heavyindustry";
    public static final String TAG_MINING = "mining";
    public static final String TAG_REFINING = "refining";
    public static final String TAG_FUEL_PRODUCTION = "fuelprod";
    public static final String TAG_WAYSTATION = "waystation";
    public static final String TAG_GROUND_DEFENSES = "grounddefenses";

    public static Industry getIndustryWithTag(MarketAPI market, String tag) {
        for (Industry ind : market.getIndustries()) {
            if (ind.getSpec().hasTag(tag) && ind.isFunctional()) {
                return ind;
            }
        }
        return null;
    }
    
    public static boolean hasIndustryWithTag(MarketAPI market, String tag) {
        return getIndustryWithTag(market, tag) != null;
    }

    // LIGHT INDUSTRY (Tag Based)
    public static Industry getLightIndustry(MarketAPI market) {
        return getIndustryWithTag(market, TAG_LIGHT_INDUSTRY);
    }
    public static boolean hasLightIndustry(MarketAPI market) { return getLightIndustry(market) != null; }

    // HEAVY INDUSTRY (Tag Based)
    public static Industry getHeavyIndustry(MarketAPI market) {
        return getIndustryWithTag(market, TAG_HEAVY_INDUSTRY);
    }
    public static boolean hasHeavyIndustry(MarketAPI market) { return getHeavyIndustry(market) != null; }

    // MINING (Tag Based)
    public static Industry getMining(MarketAPI market) {
        return getIndustryWithTag(market, TAG_MINING);
    }
    public static boolean hasMining(MarketAPI market) { return getMining(market) != null; }

    // REFINING (Tag Based)
    public static Industry getRefining(MarketAPI market) {
        return getIndustryWithTag(market, TAG_REFINING);
    }
    public static boolean hasRefining(MarketAPI market) { return getRefining(market) != null; }

    // FUEL PRODUCTION (Tag Based)
    public static Industry getFuelProduction(MarketAPI market) {
        return getIndustryWithTag(market, TAG_FUEL_PRODUCTION);
    }
    public static boolean hasFuelProduction(MarketAPI market) { return getFuelProduction(market) != null; }

    // WAYSTATION (Tag Based)
    public static Industry getWaystation(MarketAPI market) {
        return getIndustryWithTag(market, TAG_WAYSTATION);
    }
    public static boolean hasWaystation(MarketAPI market) { return getWaystation(market) != null; }

    // DEFENSES (Tag Based)
    public static Industry getDefense(MarketAPI market) {
        return getIndustryWithTag(market, TAG_GROUND_DEFENSES);
    }

    // FARMING (Explicit Checks)
    public static Industry getFarming(MarketAPI market) {
        Industry ind = market.getIndustry(Industries.FARMING);
        if (ind != null && ind.isFunctional()) return ind;

        if (HAS_AOTD) {
            ind = market.getIndustry(AOTD_MONOCULTURE_PLOTS);
            if (ind != null && ind.isFunctional()) return ind;
            ind = market.getIndustry(AOTD_ARTISANAL_FARMING);
            if (ind != null && ind.isFunctional()) return ind;
            ind = market.getIndustry(AOTD_SUBSIDISED_FARMING);
            if (ind != null && ind.isFunctional()) return ind;
        }
        return null;
    }
    public static boolean hasFarming(MarketAPI market) { return getFarming(market) != null; }

    // AQUACULTURE (Explicit Checks)
    public static Industry getAquaculture(MarketAPI market) {
        Industry ind = market.getIndustry(Industries.AQUACULTURE);
        if (ind != null && ind.isFunctional()) return ind;

        if (HAS_AOTD) {
            ind = market.getIndustry(AOTD_FISHING_HARBOUR);
            if (ind != null && ind.isFunctional()) return ind;
        }
        return null;
    }
    public static boolean hasAquaculture(MarketAPI market) { return getAquaculture(market) != null; }

    // MILITARY STRUCTURES (Explicit Checks)
    public static Industry getMilitary(MarketAPI market) {

        Industry ind = market.getIndustry(Industries.HIGHCOMMAND);
        if (ind != null && ind.isFunctional()) return ind;
        
        ind = market.getIndustry(Industries.MILITARYBASE);
        if (ind != null && ind.isFunctional()) return ind;

        ind = market.getIndustry(Industries.PATROLHQ);
        if (ind != null && ind.isFunctional()) return ind;
        
        return null;
    }
}