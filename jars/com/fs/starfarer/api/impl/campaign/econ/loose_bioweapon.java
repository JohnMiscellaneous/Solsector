package com.fs.starfarer.api.impl.campaign.econ;
 
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
 
import com.fs.starfarer.api.impl.campaign.econ.sol_remove_replace;
import com.fs.starfarer.api.impl.campaign.econ.SolIntelHelper;
 
public class loose_bioweapon extends BaseHazardCondition {
 
    private static final String TASC_MOD_ID = "boggled_tasc"; 
    private static boolean TASC_ENABLED = false;
    static {
        if (Global.getSettings() != null && Global.getSettings().getModManager() != null) {
            TASC_ENABLED = Global.getSettings().getModManager().isModEnabled(TASC_MOD_ID);
        }
    }
 
    // Memory Keys
    private static final String MEM_KEY_ACTIVE = "$bioweapon_research_active";
    private static final String MEM_KEY_STAGE = "$bioweapon_stage"; 
    private static final String MEM_KEY_DAYS_LEFT = "$bioweapon_days_left";
    private static final String MEM_KEY_LAST_AIRLESS = "$bioweapon_last_airless";
 
    // Stats
    private static final float BASE_HAZARD = 0.25f;
    private static final float BASE_ACCESS = -0.30f;
    private static final int BASE_GROWTH = -4;
 
    private static final float ATMO_HAZARD = 1.00f;
    private static final float ATMO_ACCESS = -0.60f;
    private static final int ATMO_GROWTH = -12;
 
    @Override
    public void apply(String id) {
        super.apply(id);
 
        int size = market.getSize();
        boolean isActive = market.getMemoryWithoutUpdate().getBoolean(MEM_KEY_ACTIVE);
 
        if (!isActive && size >= 4) {
            startResearch(market);
            isActive = true;
        }
 
        int stage = 0;
        if (isActive) {
            stage = market.getMemoryWithoutUpdate().getInt(MEM_KEY_STAGE);
        }
 
        boolean highDangerMode = false;
        if (TASC_ENABLED) {
            boolean hasAtmosphere = !market.hasCondition(Conditions.NO_ATMOSPHERE);
            if (hasAtmosphere) {
                highDangerMode = true;
            }
            handleAtmosphereNotification(hasAtmosphere, market.getMemoryWithoutUpdate());
        }
 
        float multiplier = 1.0f;
        if (stage == 1) multiplier = 0.5f;
        else if (stage >= 2) multiplier = 0.0f;
 
        float hazard = highDangerMode ? ATMO_HAZARD : BASE_HAZARD;
        float access = highDangerMode ? ATMO_ACCESS : BASE_ACCESS;
        int growth = highDangerMode ? ATMO_GROWTH : BASE_GROWTH;
 
        if (multiplier > 0) {
            market.getHazard().modifyFlat(id, hazard * multiplier, "Loose bioweapon");
            market.getAccessibilityMod().modifyFlat(id, access * multiplier, "Quarantine protocols");
            market.getIncoming().getWeight().modifyFlat(id, growth * multiplier, "Bioweapon fears");
        }
    }
 
    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getHazard().unmodifyFlat(id);
        market.getAccessibilityMod().unmodifyFlat(id);
        market.getIncoming().getWeight().unmodifyFlat(id);
    }
 
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
 
        float pad = 10f;
        int size = market.getSize();
        boolean isActive = market.getMemoryWithoutUpdate().getBoolean(MEM_KEY_ACTIVE);
        int stage = isActive ? market.getMemoryWithoutUpdate().getInt(MEM_KEY_STAGE) : 0;
 
        // --- Status Text ---
        if (size < 4) {
             tooltip.addPara("The colony lacks the infrastructure to research the pathogen. " +
                    "Standard quarantine procedures are in effect, severely hampering operations.",
                    pad, Misc.getHighlightColor(), "lacks the infrastructure");
        } else {
            if (stage == 2) {
                 tooltip.addPara("A successful compulsory vaccination program has made the disease irrelevant. " +
                    "Final preparations for a complete cure are underway.", 
                    pad, Misc.getHighlightColor(), "disease irrelevant");
            } else if (stage == 1) {
                tooltip.addPara("Reliable medications have been developed, reducing mortality and infectivity. " +
                    "Research into a permanent vaccine is ongoing.", 
                    pad, Misc.getHighlightColor(), "Reliable medications");
            } else {
                tooltip.addPara("Pathogen research has begun. No effective treatments have been identified yet.", 
                    pad, Misc.getHighlightColor(), "research has begun");
            }
        }
        
        // --- Atmosphere Text ---
        boolean highDangerMode = false;
        if (TASC_ENABLED && !market.hasCondition(Conditions.NO_ATMOSPHERE)) {
            highDangerMode = true;
        }
 
        if (TASC_ENABLED) {
            if (highDangerMode) {
                  tooltip.addPara("The atmosphere allows the pathogen to spread freely.", pad, Misc.getNegativeHighlightColor());
            } else {
                  tooltip.addPara("The lack of atmosphere limits the spread of the pathogen.", pad, Misc.getTextColor());
            }
        }
 
        // --- Stats ---
        float hazard = highDangerMode ? ATMO_HAZARD : BASE_HAZARD;
        float access = highDangerMode ? ATMO_ACCESS : BASE_ACCESS;
        int growth = highDangerMode ? ATMO_GROWTH : BASE_GROWTH;
        
        float multiplier = 1.0f;
        if (isActive) {
             if (stage == 1) multiplier = 0.5f;
             else if (stage >= 2) multiplier = 0.0f;
        }
 
        if (multiplier > 0) {
            tooltip.addPara("%s hazard rating", pad, Misc.getHighlightColor(), "+" + (int)(hazard * multiplier * 100f) + "%");
            tooltip.addPara("%s accessibility", pad, Misc.getHighlightColor(), (int)(access * multiplier * 100f) + "%"); 
            tooltip.addPara("%s population growth", pad, Misc.getHighlightColor(), "" + (int)(growth * multiplier));
        }
    }
 
    private void handleAtmosphereNotification(boolean hasAtmosphere, MemoryAPI memory) {
        boolean currentIsAirless = !hasAtmosphere;
 
        if (memory.contains(MEM_KEY_LAST_AIRLESS)) {
            boolean lastIsAirless = memory.getBoolean(MEM_KEY_LAST_AIRLESS);
            
            if (lastIsAirless && !currentIsAirless) {
                Global.getSector().getCampaignUI().addMessage(
                    "Atmosphere generation on " + market.getName() + " has allowed the bioweapon to spread airborne! Danger level increased!",
                    Misc.getNegativeHighlightColor());
            } 
            else if (!lastIsAirless && currentIsAirless) {
                Global.getSector().getCampaignUI().addMessage(
                    "Loss of atmosphere on " + market.getName() + " has restricted bioweapon vectors. Danger level reduced.",
                    Misc.getPositiveHighlightColor());
            }
        }
        memory.set(MEM_KEY_LAST_AIRLESS, currentIsAirless);
    }
 
    // --- Chain Intel ID ---
 
    private static String getChainId(MarketAPI market) {
        return "bioweapon_" + market.getId();
    }
 
    // --- Script Logic ---
 
    private void startResearch(MarketAPI market) {
        market.getMemoryWithoutUpdate().set(MEM_KEY_ACTIVE, true);
        market.getMemoryWithoutUpdate().set(MEM_KEY_STAGE, 0);
        
        // Random 200-400 days for first stage
        float days = 200f + (float)(Math.random() * 900f);
        market.getMemoryWithoutUpdate().set(MEM_KEY_DAYS_LEFT, days);
 
        // --- Chain Intel: Create & first update ---
        String marketName = market.getName();
        SolIntelHelper.SolChainIntel chain = SolIntelHelper.createChain(
                getChainId(market),
                "Pathogen Research",
                "graphics/icons/markets/xenoplague.png")
            .market(market)
            .imagePath("graphics/illustrations/survey.jpg");
 
        chain.addUpdate("Research Initiated")
            .subtitle("Pathogen research on " + marketName)
            .description(
                "With sufficient population and infrastructure now established on %s, "
                + "a dedicated research effort has been authorized to study the extremophilic bioweapon "
                + "contaminating the regolith. Initial samples are being collected under full bio-containment. "
                + "The pathogen is unlike anything in the Sector's medical databases - "
                + "an archaean-viral chimera engineered to remain dormant in frozen regolith "
                + "and reactivate upon contact with warmth. "
                + "Researchers warn that understanding the organism will take considerable time, "
                + "and that any meaningful treatment is cycles away at best.",
                marketName)
            .summary("Research is underway. No treatments available.")
            .push();
 
        Global.getSector().addScript(new BioweaponResearchScript(market));
    }
 
    private static class BioweaponResearchScript implements EveryFrameScript {
        private final MarketAPI market;
        private boolean done = false;
 
        public BioweaponResearchScript(MarketAPI market) {
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
            if (!market.hasCondition("sol_loose_bioweapon")) {
                done = true;
                return;
            }
 
            float days = Global.getSector().getClock().convertToDays(amount);
            float daysLeft = market.getMemoryWithoutUpdate().getFloat(MEM_KEY_DAYS_LEFT);
            daysLeft -= days;
 
            if (daysLeft <= 0) {
                // Advance Stage
                int currentStage = market.getMemoryWithoutUpdate().getInt(MEM_KEY_STAGE);
                int nextStage = currentStage + 1;
                
                // Set next duration (200-400 days)
                float nextDuration = 200f + (float)(Math.random() * 200f);
                market.getMemoryWithoutUpdate().set(MEM_KEY_DAYS_LEFT, nextDuration);
                market.getMemoryWithoutUpdate().set(MEM_KEY_STAGE, nextStage);
 
                String marketName = market.getName();
                SolIntelHelper.SolChainIntel chain = SolIntelHelper.getChain(getChainId(market));
 
                if (nextStage == 1 && chain != null) {
                    // --- Medications Developed ---
                    chain.addUpdate("Medications Developed")
                        .subtitle("Treatments available on " + marketName)
                        .description(
                            "After extensive trial and a mortality rate among lab personnel that would be unacceptable "
                            + "anywhere but here, researchers on %s have identified a combination of broad-spectrum antivirals "
                            + "and targeted archaean inhibitors that reliably suppress the pathogen in infected individuals. "
                            + "The medication is not a cure - it must be taken continuously, "
                            + "and its efficacy drops sharply if doses are missed - "
                            + "but it has cut the death rate in half "
                            + "and allowed a partial relaxation of quarantine protocols. "
                            + "Workers no longer treat every surface crack as a death sentence, "
                            + "though the medication's side effects ensure that nobody forgets what they're standing on.",
                            marketName)
                        .summary("Effective medication deployed. Penalties halved.")
                        .bulletPos("Hazard rating reduced", "reduced")
                        .bulletPos("Accessibility improved", "improved")
                        .bulletPos("Population growth penalty reduced", "reduced")
                        .bulletHL("Research continues toward a vaccine", "vaccine")
                        .push();
 
                } else if (nextStage == 2 && chain != null) {
                    // --- Vaccine Deployed ---
                    chain.addUpdate("Vaccine Deployed")
                        .subtitle("Vaccination program on " + marketName)
                        .description(
                            "A viable vaccine has been synthesized on %s. "
                            + "Unlike the stopgap medications, the vaccine provokes a lasting immune response "
                            + "that renders the pathogen unable to establish infection even upon direct exposure. "
                            + "A compulsory vaccination program has been rolled out colony-wide, "
                            + "and within weeks the disease has become functionally irrelevant to daily operations. "
                            + "Quarantine protocols have been stood down.",
                            marketName)
                        .summary("Colony vaccinated. All penalties removed.")
                        .bulletPos("Hazard rating penalty removed", "removed")
                        .bulletPos("Accessibility restored", "restored")
                        .bulletPos("Population growth restored", "restored")
                        .bulletHL("Continued research ongoing", "ongoing")
                        .push();
 
                } else if (nextStage == 3) {
                    // --- Permanent Cure ---
                    if (chain != null) {
                        chain.setName("Pathogen Research - Complete");
                        chain.addUpdate("Pathogen Eradicated")
                            .subtitle("Bioweapon eradicated on " + marketName)
                            .description(
                                "It has been some time since anyone on %s died of the pathogen. "
                                + "A subviral packet, engineered to hijack the bioweapon's own viral delivery mechanism, "
                                + "has proven effective at disrupting the archaean-viral symbiosis in reactivated regolith. "
                                + "Between the vaccine and the countermeasure, the threat has been quietly downgraded "
                                + "from existential to historical, and with it, the externally imposed quarantines for %s have ended. ",
                                marketName, marketName)
                            .summary("Pathogen eradicated. Condition removed.")
                            .bulletPos("Bioweapon neutralized", "neutralized")
                            .bulletPos("All restrictions lifted", "lifted")
                            .bulletPos("Quarantine rescinded", "rescinded")
                            .push();
                    }
                    
                    sol_remove_replace.execute(market, "sol_loose_bioweapon", null);
                    
                    // Cleanup memory
                    market.getMemoryWithoutUpdate().unset(MEM_KEY_ACTIVE);
                    market.getMemoryWithoutUpdate().unset(MEM_KEY_STAGE);
                    market.getMemoryWithoutUpdate().unset(MEM_KEY_DAYS_LEFT);
                    market.getMemoryWithoutUpdate().unset(MEM_KEY_LAST_AIRLESS);
                    
                    done = true;
                }
            } else {
                market.getMemoryWithoutUpdate().set(MEM_KEY_DAYS_LEFT, daysLeft);
            }
        }
    }
}