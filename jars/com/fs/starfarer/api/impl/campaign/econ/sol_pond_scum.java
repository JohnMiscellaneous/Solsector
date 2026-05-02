package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import com.fs.starfarer.api.impl.campaign.econ.SolIntelHelper;

public class sol_pond_scum extends BaseHazardCondition {

    private static final String KEY_SEEDING_START = "$sol_pond_scum_seeding_start";
    private static final String KEY_MSG_SENT = "$sol_pond_scum_msg_sent";
    private static final String KEY_LAST_STAGE = "$sol_pond_scum_last_stage";

    public static final float HAZARD_BASE = 0.05f;
    public static final float HAZARD_STAGE_1_ADD = 0.05f;
    public static final float HAZARD_STAGE_2_ADD = 0.10f; 
    
    public static final int FOOD_PENALTY_BASE = -3;
    
    public static final float DURATION_STAGE_1 = 365f;
    public static final float DURATION_STAGE_2 = 365f;
    public static final float DURATION_STAGE_3 = 365f;
    public static final float TOTAL_DURATION = DURATION_STAGE_1 + DURATION_STAGE_2 + DURATION_STAGE_3;

    private static String getChainId(String marketId) {
        return "pond_scum_" + marketId;
    }


    private static String safeGetTypeId(PlanetAPI planet) {
        if (planet == null) return null;
        try {
            return planet.getTypeId();
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Override
    public void apply(String id) {
        super.apply(id);
        
        String desc = condition.getName();
        
    
        PlanetAPI planet = market.getPlanetEntity();
        String typeId = safeGetTypeId(planet);
        if (planet != null && typeId != null && !"water".equals(typeId)) {
            String marketName = market.getName();

            SolIntelHelper.create("Biosphere Reshaped", "graphics/icons/markets/sol_pond_scum.png")
                .market(market)
                .subtitle("Biosphere reshaped on " + marketName)
                .description(
                    "A concerted terraforming effort has reshaped %s and its biosphere. "
                    + "The kilometers of primordial sludge are now a thing of the past.",
                    marketName)
                .summary("Pond scum condition removed.")
                .bulletPos("Biosphere restructured", "restructured")
                .send();

            if (!market.hasCondition("habitable")) {
                market.addCondition("habitable");
            }

            sol_remove_replace.execute(market, "sol_pond_scum", null);
            cleanupMemory();
            return;
        }
        
        if (market.getSize() >= 4 && !market.getMemoryWithoutUpdate().contains(KEY_SEEDING_START)) {
            market.getMemoryWithoutUpdate().set(KEY_SEEDING_START, Global.getSector().getClock().getTimestamp());
        }

        if (market.getMemoryWithoutUpdate().contains(KEY_SEEDING_START) && 
            !market.getMemoryWithoutUpdate().contains(KEY_MSG_SENT)) {
            
            String marketName = market.getName();

            SolIntelHelper.SolChainIntel chain = SolIntelHelper.createChain(
                    getChainId(market.getId()),
                    "Biosphere Seeding",
                    "graphics/icons/markets/sol_pond_scum.png")
                .market(market)
                .imagePath("graphics/illustrations/volturn_background.jpg");

            chain.addUpdate("Seeding Begun")
                .subtitle("Biosphere seeding on " + marketName)
                .description(
                    "With the colony on %s now large enough to support the effort, "
                    + "infrastructure teams have begun introducing complex organisms into the world ocean. "
                    + "The existing microbe mat is vast and entrenched - "
                    + "a monoculture of Domain-era extremophiles that has had the ocean to itself for centuries. "
                    + "Displacing it will require sustained intervention. "
                    + "In the short term, the disruption to the existing ecosystem "
                    + "will reduce aquaculture yields as the introduced species compete for resources.",
                    marketName)
                .summary("Complex life seeding underway.")
                .push();

            market.getMemoryWithoutUpdate().set(KEY_MSG_SENT, true);
            market.getMemoryWithoutUpdate().set(KEY_LAST_STAGE, 0);
        }

        // Modifiers and stage transitions
        if (market.getMemoryWithoutUpdate().contains(KEY_SEEDING_START)) {
            long startTimestamp = market.getMemoryWithoutUpdate().getLong(KEY_SEEDING_START);
            float daysElapsed = Global.getSector().getClock().getElapsedDaysSince(startTimestamp);
            
            int currentStage;
            if (daysElapsed < DURATION_STAGE_1) {
                currentStage = 1;
            } else if (daysElapsed < (DURATION_STAGE_1 + DURATION_STAGE_2)) {
                currentStage = 2;
            } else if (daysElapsed < TOTAL_DURATION) {
                currentStage = 3;
            } else {
                currentStage = 4;
            }

            // Chain intel updates on stage transitions
            int lastStage = market.getMemoryWithoutUpdate().getInt(KEY_LAST_STAGE);
            if (currentStage != lastStage) {
                String marketName = market.getName();
                SolIntelHelper.SolChainIntel chain = SolIntelHelper.getChain(getChainId(market.getId()));

                if (currentStage == 2 && chain != null) {
                    chain.addUpdate("Organisms Establishing")
                        .subtitle("Ecosystem developing on " + marketName)
                        .description(
                            "The introduced organisms on %s are establishing themselves. "
                            + "Patches of colour are appearing in the ocean "
                            + "where algal blooms are being displaced by more complex photosynthetic life, "
                            + "Aquaculture crews report the new species are beginning to stabilise "
                            + "local nutrient cycles, though the disruption to yields continues.",
                            marketName)
                        .summary("Ecosystem developing. Food penalty easing.")
                        .bulletPos("Aquaculture penalty reduced", "reduced")
                        .bulletHL("Biosphere restructuring ongoing", "ongoing")
                        .push();

                } else if (currentStage == 3 && chain != null) {
                    chain.addUpdate("Biosphere Stabilising")
                        .subtitle("Biosphere stabilising on " + marketName)
                        .description(
                            "The biosphere on %s is approaching equilibrium. "
                            + "The primordial microbe mat has been broken up across most of the ocean surface, "
                            + "replaced by a functioning food chain "
                            + "that regulates itself without constant intervention. "
                            + "Decomposition has made the atmosphere measurably more oxygenated. "
                            + "Aquaculture yields are recovering.",
                            marketName)
                        .summary("Biosphere nearing stability. Food penalty minimal.")
                        .bulletPos("Aquaculture penalty further reduced", "further reduced")
                        .bulletPos("Atmosphere improving", "improving")
                        .push();

                } else if (currentStage == 4) {
                    // Completion
                    if (chain != null) {
                        chain.setName("Biosphere Seeding - Complete");
                        chain.addUpdate("Biosphere Established")
                            .subtitle("Biosphere established on " + marketName)
                            .description(
                                "The seeding of %s is complete. "
                                + "A self-sustaining biosphere of complex multicellular life "
                                + "now inhabits the world ocean. "
                                + "The choking mat of primordial sludge that once defined the planet "
                                + "has been subsumed into a functioning ecology created by, and for, humans. ",
                                marketName)
                            .summary("Biosphere established.")
                            .bulletPos("Hazard penalty removed", "removed")
                            .bulletPos("Aquaculture penalty removed", "removed")
                            .bulletPos("Planet now habitable", "habitable")
                            .push();
                    }

                    if (!market.hasCondition("habitable")) {
                        market.addCondition("habitable");
                    }

                    // Set volturn texture if water planet
                    if (planet != null && "water".equals(safeGetTypeId(planet))) {
                        planet.getSpec().setTexture("graphics/planets/volturn.jpg");
                        planet.getSpec().setPlanetColor(new Color(255, 255, 255, 255));
                        planet.applySpecChanges();
                    }

                    sol_remove_replace.execute(market, "sol_pond_scum", null);
                    cleanupMemory();
                    return;
                }

                market.getMemoryWithoutUpdate().set(KEY_LAST_STAGE, currentStage);
            }

            // Apply modifiers based on stage
            float hazardMod = HAZARD_BASE;
            int foodMod = FOOD_PENALTY_BASE;

            if (currentStage == 1) {
                hazardMod += HAZARD_STAGE_1_ADD;
                foodMod = FOOD_PENALTY_BASE;
            } else if (currentStage == 2) {
                hazardMod += HAZARD_STAGE_2_ADD;
                foodMod = FOOD_PENALTY_BASE + 1;
            } else if (currentStage == 3) {
                hazardMod += HAZARD_STAGE_2_ADD;
                foodMod = FOOD_PENALTY_BASE + 2;
            }

            market.getHazard().modifyFlat(id, hazardMod, desc);
            
            Industry aquaculture = sol_industry_compat.getAquaculture(market);
            if (aquaculture != null) {
                aquaculture.getSupply(Commodities.FOOD).getQuantity().modifyFlat(id, foodMod, desc);
            }

        } else {
            // Dormant Phase (Size < 4)
            market.getHazard().modifyFlat(id, HAZARD_BASE, desc);
            
            Industry aquaculture = sol_industry_compat.getAquaculture(market);
            if (aquaculture != null) {
                aquaculture.getSupply(Commodities.FOOD).getQuantity().modifyFlat(id, FOOD_PENALTY_BASE, desc);
            }
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        
        market.getHazard().unmodifyFlat(id);
        
        Industry aquaculture = sol_industry_compat.getAquaculture(market);
        if (aquaculture != null) {
            aquaculture.getSupply(Commodities.FOOD).getQuantity().unmodifyFlat(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();

        boolean isSeeding = market.getMemoryWithoutUpdate().contains(KEY_SEEDING_START);
        
        if (isSeeding) {
            long startTimestamp = market.getMemoryWithoutUpdate().getLong(KEY_SEEDING_START);
            float daysElapsed = Global.getSector().getClock().getElapsedDaysSince(startTimestamp);
            float daysRemaining = Math.max(0, TOTAL_DURATION - daysElapsed);
            
            tooltip.addPara("The local infrastructure has begun the seeding of complex life. The biosphere is currently undergoing rapid, volatile restructuring to accommodate the new ecosystem.", pad);
            
            float currentHazard = HAZARD_BASE;
            int currentFoodMod = FOOD_PENALTY_BASE;

            if (daysElapsed < DURATION_STAGE_1) {
                currentHazard += HAZARD_STAGE_1_ADD;
                currentFoodMod = FOOD_PENALTY_BASE;
            } else if (daysElapsed < (DURATION_STAGE_1 + DURATION_STAGE_2)) {
                currentHazard += HAZARD_STAGE_2_ADD;
                currentFoodMod = FOOD_PENALTY_BASE + 1;
            } else {
                currentHazard += HAZARD_STAGE_2_ADD;
                currentFoodMod = FOOD_PENALTY_BASE + 2;
            }

            tooltip.addPara("%s hazard rating", pad, h, "+" + (int)(currentHazard * 100) + "%");
            
            if (daysRemaining > 0) {
                 tooltip.addPara("Estimated time until ecological stabilization: %s days.", 
                    pad, h, "" + (int)daysRemaining);
            }
            
            String sign = currentFoodMod > 0 ? "+" : "";
            tooltip.addPara("%s food production (Aquaculture)", pad, h, sign + currentFoodMod);

        } else {
            tooltip.addPara("Thick layers of primordial slime choke the water surface. The biosphere lacks complex multicellular life to regulate the bloom.", pad);
            
            if (market.isPlanetConditionMarketOnly()) {
                 tooltip.addPara("Your officers estimate that a %s colony, if established, could begin a seeding of complex life.", 
                    pad, h, "size 4");
            } else {
                 tooltip.addPara("Your officers estimate that a %s colony could begin seeding complex life.", 
                    pad, h, "size 4");
            } 
            
            tooltip.addPara("%s hazard rating", pad, h, "+" + (int)(HAZARD_BASE * 100) + "%");
            tooltip.addPara("%s food production (Aquaculture)", pad, h, "" + FOOD_PENALTY_BASE);
        }
    }

    private void cleanupMemory() {
        market.getMemoryWithoutUpdate().unset(KEY_SEEDING_START);
        market.getMemoryWithoutUpdate().unset(KEY_MSG_SENT);
        market.getMemoryWithoutUpdate().unset(KEY_LAST_STAGE);
    }
}