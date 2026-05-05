package data.scripts.world.systems;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;

public class SolDeferredSetupScript implements EveryFrameScript {

    private static final float CHECK_INTERVAL = 1.0f;
    private float elapsed = 0f;

    private static final String TARGET_SYSTEM_ID = "sol";

    private static final String ID_VULCAN   = "vulcanenergy";
    private static final String ID_VANERA   = "vanera_station";
    private static final String ID_ISS      = "iss_station";
    private static final String ID_TIANGONG = "tiangong_station";
    private static final String ID_PELEUS   = "patmen";

    private static final String ID_EROS     = "eros";
    private static final String ID_PSYCHE   = "psyche";
    private static final String ID_PUCK     = "puck";
    private static final String ID_POLYSO   = "polyso_station";
    private static final String ID_CHAOS    = "chaos";

    private static final String MEM_VULCAN    = "$sol_setup_vulcan_done";
    private static final String MEM_VANERA    = "$sol_setup_vanera_done";
    private static final String MEM_ISS       = "$sol_setup_iss_done";
    private static final String MEM_TIANGONG  = "$sol_setup_tiangong_done";
    private static final String MEM_PELEUS    = "$sol_setup_peleus_done";

    private static final String MEM_EROS      = "$sol_setup_eros_done";
    private static final String MEM_PSYCHE    = "$sol_setup_psyche_done";
    private static final String MEM_PUCK      = "$sol_setup_puck_done";
    private static final String MEM_POLYSO    = "$sol_setup_polyso_done";
    private static final String MEM_CHAOS     = "$sol_setup_chaos_done";

    private static final String MEM_DISCOVERY = "$sol_discovery_done";

    // latched at world creation by SolTotal so mid-save JSON edits don't desync
    private static final String MEM_LATCHED_SETTLED       = "$sol_latched_settled";
    private static final String MEM_INSTANT_INIT_NOTIFIED = "$sol_instant_init_notified";

    private boolean isSettled    = true;
    private boolean mercuryCold  = true;
    private boolean settingsLoaded = false;

    private boolean finished = false;

    // =====================================================================
    // EveryFrameScript interface
    // =====================================================================

    @Override
    public boolean isDone() {
        return finished;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        elapsed += amount;
        if (elapsed < CHECK_INTERVAL) return;
        elapsed = 0f;

        MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();

        // gra settings
        if (!settingsLoaded) {
            // prefer latched, JSON fallback for legacy saves
            if (mem.contains(MEM_LATCHED_SETTLED)) {
                isSettled = mem.getBoolean(MEM_LATCHED_SETTLED);
            } else {
                try {
                    isSettled = Global.getSettings().loadJSON("data/config/sol_settings.json")
                            .optBoolean("Generate_Settled_Planets", true);
                } catch (Exception e) {
                    isSettled = true;
                }
            }
            try {
                mercuryCold = Global.getSettings().loadJSON("data/config/sol_settings.json")
                        .optBoolean("Mercury_And_Venus_Have_Poor_Light", true); // important for eros
            } catch (Exception e) {
                mercuryCold = true;
            }
            settingsLoaded = true;
        }

        // re-read live so mid-save flips work; SolTotal handles the new-game case
        boolean instantMarkets = false;
        try {
            instantMarkets = Global.getSettings().loadJSON("data/config/sol_settings.json")
                    .optBoolean("Settled_Planets_Spawn_In_Instantly", false);
        } catch (Exception e) {}

        // mid-save flip path; MEM_DISCOVERY dedupes against SolTotal
        if (instantMarkets && isSettled) {
            StarSystemAPI sol = Global.getSector().getStarSystem(TARGET_SYSTEM_ID);
            if (sol != null) {
                if (!mem.getBoolean(MEM_DISCOVERY)) {
                    new SolEconomies().generate(sol);
                    mem.set(MEM_DISCOVERY, true);
                }
                if (!mem.getBoolean(MEM_INSTANT_INIT_NOTIFIED)) {
                    try {
                        Global.getSector().getCampaignUI()
                                .addMessage("Markets for Sol initialized");
                    } catch (Exception ignore) {}
                    mem.set(MEM_INSTANT_INIT_NOTIFIED, true);
                }
            }
        }

        boolean discoveryDone = mem.getBoolean(MEM_DISCOVERY);
        if (!discoveryDone) {
            if (!isSettled) {
                mem.set(MEM_DISCOVERY, true);
                discoveryDone = true;
            } else {
                discoveryDone = checkDiscovery(mem); // checks if Sol gate discovered for the detonation of soleconomies
            }
        }

        boolean doneVulcan   = mem.getBoolean(MEM_VULCAN);
        boolean doneVanera   = mem.getBoolean(MEM_VANERA);
        boolean doneIss      = mem.getBoolean(MEM_ISS);
        boolean doneTiangong = mem.getBoolean(MEM_TIANGONG);
        boolean donePeleus   = mem.getBoolean(MEM_PELEUS);

        boolean doneEros, donePsyche, donePuck, donePolyso, doneChaos;
        if (isSettled) {
            if (!mem.getBoolean(MEM_EROS))   mem.set(MEM_EROS, true);
            if (!mem.getBoolean(MEM_PSYCHE)) mem.set(MEM_PSYCHE, true);
            if (!mem.getBoolean(MEM_PUCK))   mem.set(MEM_PUCK, true);
            if (!mem.getBoolean(MEM_POLYSO)) mem.set(MEM_POLYSO, true);
            if (!mem.getBoolean(MEM_CHAOS))  mem.set(MEM_CHAOS, true);
            doneEros = donePsyche = donePuck = donePolyso = doneChaos = true;
        } else {
            doneEros   = mem.getBoolean(MEM_EROS);
            donePsyche = mem.getBoolean(MEM_PSYCHE);
            donePuck   = mem.getBoolean(MEM_PUCK);
            donePolyso = mem.getBoolean(MEM_POLYSO);
            doneChaos  = mem.getBoolean(MEM_CHAOS);
        }

        // Should suicide?
        boolean allStationsDone = doneVulcan && doneVanera && doneIss
                && doneTiangong && donePeleus
                && doneEros && donePsyche && donePuck && donePolyso && doneChaos;

        if (!allStationsDone) {
            allStationsDone = checkStations(mem,
                    doneVulcan, doneVanera, doneIss, doneTiangong, donePeleus,
                    doneEros, donePsyche, donePuck, donePolyso, doneChaos);
        }

        if (discoveryDone && allStationsDone) {
            finished = true;
        }
    }

    // =====================================================================
    // DISCOVERY LOGIC
    // =====================================================================
    // when player jumps in they discover the gate
    private boolean checkDiscovery(MemoryAPI mem) {
        StarSystemAPI system = Global.getSector().getStarSystem(TARGET_SYSTEM_ID);
        if (system == null) return false;

        SectorEntityToken lunaL3 = system.getEntityById("luna_l3");
        if (lunaL3 == null) return false;

        if (lunaL3.isDiscoverable()) {
            return false;
        }

        new SolEconomies().generate(system);
        mem.set(MEM_DISCOVERY, true);
        return true;
    }

    // =====================================================================
    // STATION SETUP LOGIC
    // =====================================================================
    // CAUSE TASC DELETES EVERY CONDITION ON STATION SETTLEMENT
    // MIGHT DO A SIMILAR SCRIPT FOR AOTD AND RENAMING PLANETS

    private boolean checkStations(MemoryAPI mem,
                                  boolean doneVulcan, boolean doneVanera,
                                  boolean doneIss, boolean doneTiangong,
                                  boolean donePeleus,
                                  boolean doneEros, boolean donePsyche,
                                  boolean donePuck, boolean donePolyso,
                                  boolean doneChaos) {

        StarSystemAPI system = Global.getSector().getStarSystem(TARGET_SYSTEM_ID);
        if (system == null) return false;

        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.getStarSystem() == null) continue;
            if (!market.getStarSystem().getId().equalsIgnoreCase(TARGET_SYSTEM_ID)) continue;
            if (market.getSize() < 3) continue;

            String mId = market.getId() != null ? market.getId().toLowerCase() : "";
            String eId = (market.getPrimaryEntity() != null)
                    ? market.getPrimaryEntity().getId().toLowerCase() : "";

            // ---------------- ALWAYS-ABANDONED STATIONS ----------------

            // Vulcan
            if (!doneVulcan && matches(mId, eId, ID_VULCAN, "vulcan")) {
                safeAdd(market, "very_hot");
                safeAdd(market, "sol_megaforges_hyperenergetic");
                safeAdd(market, "sol_irradiated_extreme");
                markConditionsSurveyed(market);
                mem.set(MEM_VULCAN, true);
                doneVulcan = true;
            }

            // Vanera
            if (!doneVanera && matches(mId, eId, ID_VANERA, "vanera")) {
                safeAdd(market, "decivilized");
                safeAdd(market, "very_hot");
                safeAdd(market, "sol_megaforges");
                markConditionsSurveyed(market);
                mem.set(MEM_VANERA, true);
                doneVanera = true;
            }

            // ISS / Apollo
            if (!doneIss && matches(mId, eId, ID_ISS, "iss")) {
                safeAdd(market, "decivilized");
                safeAdd(market, "sol_megaforges");
                markConditionsSurveyed(market);
                mem.set(MEM_ISS, true);
                doneIss = true;
            }

            // Tiangong
            if (!doneTiangong && matches(mId, eId, ID_TIANGONG, "tiangong")) {
                safeAdd(market, "sol_no_atmosphere_bodgejob");
                markConditionsSurveyed(market);
                mem.set(MEM_TIANGONG, true);
                doneTiangong = true;
            }

            // Peleus
            if (!donePeleus && (matches(mId, eId, ID_PELEUS, "peleus")
                    || mId.contains("peleus") || eId.contains("peleus"))) {
                safeAdd(market, "sol_meteoroids");
                safeAdd(market, "volatiles_diffuse");
                safeAdd(market, "ore_moderate");
                safeAdd(market, "rare_ore_sparse");
                safeAdd(market, "cold");
                safeAdd(market, "poor_light");
                safeAdd(market, "ruins_widespread");
                safeAdd(market, "sol_no_atmosphere_bodgejob");
                markConditionsSurveyed(market);
                mem.set(MEM_PELEUS, true);
                donePeleus = true;
            }

            // ------------- CONDITIONALLY-ABANDONED BODIES --------------
            if (!isSettled) {

                // Eros
                if (!doneEros && matches(mId, eId, ID_EROS, "eros")) {
                    safeAdd(market, "low_gravity");
                    safeAdd(market, "sol_tiny_stripped");
                    safeAdd(market, "volatiles_trace");
                    safeAdd(market, "hot");
                    if (mercuryCold) safeAdd(market, "cold");
                    markConditionsSurveyed(market);
                    mem.set(MEM_EROS, true);
                    doneEros = true;
                }

                // Psyche
                if (!donePsyche && matches(mId, eId, ID_PSYCHE, "psyche")) {
                    safeAdd(market, "low_gravity");
                    safeAdd(market, "cold");
                    safeAdd(market, "ore_ultrarich");
                    safeAdd(market, "rare_ore_ultrarich");
                    safeAdd(market, "sol_meteoroids");
                    safeAdd(market, "sol_orbital_ring");
                    safeAdd(market, "sol_fast_rotator");
                    safeAdd(market, "sol_no_atmosphere_bodgejob");
                    markConditionsSurveyed(market);
                    mem.set(MEM_PSYCHE, true);
                    donePsyche = true;
                }

                // Puck
                if (!donePuck && matches(mId, eId, ID_PUCK, "puck")) {
                    safeAdd(market, "very_cold");
                    safeAdd(market, "low_gravity");
                    safeAdd(market, "ore_sparse");
                    safeAdd(market, "volatiles_trace");
                    safeAdd(market, "dark");
                    safeAdd(market, "irradiated");
                    safeAdd(market, "sol_meteoroids");
                    markConditionsSurveyed(market);
                    mem.set(MEM_PUCK, true);
                    donePuck = true;
                }

                // Polyso
                if (!donePolyso && matches(mId, eId, ID_POLYSO, "polyso")) {
                    safeAdd(market, "very_cold");
                    safeAdd(market, "dark");
                    safeAdd(market, "sol_dist_abyssal");
                    markConditionsSurveyed(market);
                    mem.set(MEM_POLYSO, true);
                    donePolyso = true;
                }

                // Chaos
                if (!doneChaos && matches(mId, eId, ID_CHAOS, "chaos")) {
                    safeAdd(market, "very_cold");
                    safeAdd(market, "dark");
                    safeAdd(market, "low_gravity");
                    safeAdd(market, "no_atmosphere");
                    safeAdd(market, "ore_moderate");
                    safeAdd(market, "rare_ore_sparse");
                    safeAdd(market, "volatiles_plentiful");
                    safeAdd(market, "sol_contact_binary");
                    safeAdd(market, "sol_dist_abyssal");
                    markConditionsSurveyed(market);
                    mem.set(MEM_CHAOS, true);
                    doneChaos = true;
                }
            }
        }

        return doneVulcan && doneVanera && doneIss
                && doneTiangong && donePeleus
                && doneEros && donePsyche && donePuck && donePolyso && doneChaos;
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private boolean matches(String marketId, String entityId, String primaryTarget, String altTarget) {
        return marketId.equals(primaryTarget) || entityId.equals(primaryTarget)
                || marketId.contains(altTarget)  || entityId.contains(altTarget);
    }

    private void safeAdd(MarketAPI market, String conditionId) {
        if (!market.hasCondition(conditionId)) {
            market.addCondition(conditionId);
        }
    }

    private void markConditionsSurveyed(MarketAPI market) {
        for (MarketConditionAPI condition : market.getConditions()) {
            condition.setSurveyed(true);
        }
    }
}