package soljars.gen.systems.sol;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import org.json.JSONObject;

import java.util.Random;

import soljars.gen.utils.AstroCalc;
import soljars.compat.industrialevolution.ArtillerySpawnTool;

public class SolMisc {

    public void generate(StarSystemAPI system) {

        AstroCalc calc = new AstroCalc();
        SectorEntityToken star = system.getStar();
        float zeroDegGlobal = 146f;

        JSONObject cfg;
        try {
            cfg = Global.getSettings().loadJSON("data/config/sol_settings.json");
        } catch (Exception e) {
            cfg = new JSONObject();
        }

        boolean luddicSystem = cfg.optBoolean("Luddic_Church_Claim_On_Sol", false);
        boolean isSettled       = cfg.optBoolean("Generate_Settled_Planets", true);
        boolean instantMarkets       = cfg.optBoolean("Settled_Planets_Spawn_In_Instantly", true);

        int remnantHorde        = cfg.optInt("remnant_difficulty", 1);
        int remnantSizeModifier = 0;
        if(remnantHorde == 1){ remnantSizeModifier = -10;}
        if(remnantHorde == 2){ remnantSizeModifier = 0;}
        if(remnantHorde == 3){ remnantSizeModifier = 10;}

        int deepSpaceProbes     = cfg.optInt("Generate_Probes", 1);
        boolean mercuryCold     = cfg.optBoolean("Mercury_And_Venus_Have_Poor_Light", true);
        // Uranus and Neptune spawn with normal gravity; hyperspace init strips high_gravity (too low density), but their gravity curve is much longer than Earth's
        boolean Uranus_And_Neptune_Have_Normal_Gravity = cfg.optBoolean("Uranus_And_Neptune_Have_Normal_Gravity", true);
        boolean generateElevators = cfg.optBoolean("Generate_Space_Elevators", true);
        boolean transNeptuneMemes = cfg.optBoolean("Trans_Neptunian_Memes", true);

        // Object Generation Settings
        int innerSolDetail               = cfg.optInt("Inner_Sol_Detail", 1);
        int visitedDetail                = cfg.optInt("Visited_Asteroids_Detail", 1);
        int asteroidBeltDetail           = cfg.optInt("Asteroid_Belt_Detail", 1);
        int hildaDetail                  = cfg.optInt("Hilda_Detail", 1);
        int jupiterTrojansDetail         = cfg.optInt("Jupiter_Trojans_Detail", 1);
        int jupiterDetail                = cfg.optInt("Jupiter_Detail", 1);
        int saturnDetail                 = cfg.optInt("Saturn_Detail", 1);
        int uranusDetail                 = cfg.optInt("Uranus_Detail", 1);
        int neptuneDetail                = cfg.optInt("Neptune_Detail", 1);
        int neptuneTrojansDetail         = cfg.optInt("Neptune_Trojans_Detail", 1);
        int centaurDetail                = cfg.optInt("Centaur_Detail", 1);
        int transNeptuneDetail           = cfg.optInt("Kuiper_Detail", 1);
        int scatteredDiskDetail          = cfg.optInt("Scattered_Disk_Detail", 1);
        int cometDetail                  = cfg.optInt("Comet_Detail", 1);

        boolean allowNonVisited          = cfg.optBoolean("Allow_Non_Visited", true);

        // Respectable end of science fiction: no 2km moons inside Thebe, no ninth planets, Leda, 1999 ZX30, Burns-Caulfield
        boolean fictionalTNOs = cfg.optBoolean("Fictional_Trans_Neptunian_Objects", true);
        // Pins Pallas -> Ceres, Clete -> Neptune for the intel screen
        boolean falseMoons    = cfg.optBoolean("False_Moons", true);

        // Disables unnamed bodies showing up on map
        int showNamesSetting = cfg.optInt("Show_Names", 0);
        boolean showMinorNames      = showNamesSetting >= 2;
        boolean showProvisionalNames = showNamesSetting == 3;
        boolean showCustomNames     = showNamesSetting != 0;
        String showNameMinor  = showMinorNames      ? "w_name" : "no_name";
        String showNameProv   = showProvisionalNames ? "w_name" : "no_name";
        String showNameCustom = showCustomNames     ? "w_name" : "no_name";

        // Single-chord moons, indicated bodies — too controversial or not cool enough to send anyway.
        // Extreme is UNOBSERVED / second-order: Eris may have an inner moonlet pumping Dysnomia's eccentricity, or Dysnomia was recently decked by a TNO or captured as an extreme-distance binary
        int speculativeBodiesSetting = cfg.optInt("Speculative_Bodies", 0);
        boolean speculativeBodies        = speculativeBodiesSetting >= 1;
        boolean speculativeBodiesExtreme = speculativeBodiesSetting >= 2;

        int speculativeBodiesBigSetting = cfg.optInt("Speculative_Bodies_Big", 0);
        boolean planetNine   = speculativeBodiesBigSetting >= 1;
        boolean planetTen    = speculativeBodiesBigSetting >= 2;
        boolean planetEleven = speculativeBodiesBigSetting >= 3;

        boolean occultOrbitBeta = cfg.optBoolean("Occult_Orbit_Beta", true);
        if (!occultOrbitBeta) { falseMoons = false; }

        boolean occultTerrainBeta = cfg.optBoolean("Occult_Terrain_Beta", true);

        int genericAsteroids = cfg.optInt("Generic_Asteroids", 0);
        int gen_Hungarians = Math.round(genericAsteroids * 0.05f);
        int gen_InnerBelt  = Math.round(genericAsteroids * 0.24f);
        int gen_CoreBelt   = Math.round(genericAsteroids * 0.38f);
        int gen_OuterBelt  = Math.round(genericAsteroids * 0.26f);
        int gen_Cybeles    = Math.round(genericAsteroids * 0.07f);

        float rotMult       = (float) cfg.optDouble("rotMult", 4f);
        float progradeMult  = (float) cfg.optDouble("progradeMult", -1f);
        int stablePointDetail = cfg.optInt("Stable_Points_Detail", 0);
        int numberArtilleryStations = cfg.optInt("Artillery_Stations", 3);

        // =========================================================================
        // Gravity fixups + eccentric SPS orbits (core Sol, mod-independent)
        // =========================================================================

        // They are small, and low density
        if (Uranus_And_Neptune_Have_Normal_Gravity) {
            removeCond(system, "Uranus",  "high_gravity");
            removeCond(system, "Neptune", "high_gravity");
            removeCond(system, "SolIX", "high_gravity");
        }

        if (allowNonVisited) {
            float[] erisOffsets = calc.getBinaryOffsetsReal(2326f, 700f, 16f / 2f);
            float[] orcOffsets  = calc.getBinaryOffsetsReal(910f, 442f, 12f);
            float p_OrcusVanth   = -calc.getTime(9.5f);
            float p_ErisDysnomia = calc.getTime(15.7f);

            SectorEntityToken Haumea   = system.getEntityById("Haumea");
            SectorEntityToken Sedna    = system.getEntityById("Sedna");
            SectorEntityToken Quaoar   = system.getEntityById("Quaoar");
            SectorEntityToken Makemake = system.getEntityById("Makemake");
            SectorEntityToken Orcus    = system.getEntityById("Orcus");
            SectorEntityToken Gonggong = system.getEntityById("Gonggong");
            SectorEntityToken Eris     = system.getEntityById("Eris");

            // these begin on circular obits because the game when generating nacsent grav wells relies on a circular orbit.
            if (Haumea != null)   calc.applySPSOrbit(Haumea, star, 43.0055f, 0.1958f, 121.797f, 240.888f, 2133.74f, zeroDegGlobal, null, 1f, null, null, null, star, "Sol", false, false);
            if (Sedna != null)    calc.applySPSOrbit(Sedna, star, 506f, 0.8496f, 144.478f, 311.009f, 2075.73f, zeroDegGlobal, null, 1f, null, null, null, star, "Sol", false, false);
            if (Quaoar != null)   calc.applySPSOrbit(Quaoar, star, 43.1477f, 0.0358f, 188.963f, 163.923f, 2079.83f, zeroDegGlobal, null, 1f, null, null, null, star, "Sol", false, false);
            if (Makemake != null) calc.applySPSOrbit(Makemake, star, 45.5107f, 0.1604f, 79.269f, 297.075f, 1881.48f, zeroDegGlobal, null, 1f, null, null, null, star, "Sol", false, false);
            if (Orcus != null)    calc.applySPSOrbit(Orcus, star, 39.3358f, 0.2217f, 268.385f, 73.722f, 2143.69f, zeroDegGlobal, null, 1f, null, null, new float[][]{{orcOffsets[0], 0f, 0f, p_OrcusVanth, 0f, 1f}}, star, "Sol", false, false);
            if (Gonggong != null) calc.applySPSOrbit(Gonggong, star, 66.8937f, 0.5032f, 336.840f, 206.642f, 1856.59f, zeroDegGlobal, null, 1f, null, null, null, star, "Sol", false, false);
            if (Eris != null)     calc.applySPSOrbit(Eris, star, 67.9964f, 0.4370f, 36.027f, 150.732f, 2257.27f, zeroDegGlobal, null, 1f, null, null, new float[][]{{erisOffsets[0], 0f, 0f, p_ErisDysnomia, 0f, 1f}}, star, "Sol", false, false);
        }

        // =========================================================================
        // Industrial evolution
        // =========================================================================
        if (Global.getSettings().getModManager().isModEnabled("IndEvo")) {

            SectorEntityToken VulcanEnergy = system.getEntityById("VulcanEnergy");
            if (VulcanEnergy != null) {
                SectorEntityToken SunYards = system.addCustomEntity(null, null, "IndEvo_GachaStation", "neutral");
                SunYards.setCircularOrbitPointingDown(system.getStar(), 180, VulcanEnergy.getCircularOrbitRadius(), VulcanEnergy.getCircularOrbitPeriod());
                SunYards.setDiscoverable(true);
                SunYards.setSensorProfile(4000f);
            } else {
                Global.getLogger(SolMisc.class).warn("SunYards skipped: no VulcanEnergy");
            }

            if (innerSolDetail >= 1 && allowNonVisited) {
                spawnWatchtower(system, system.getEntityById("Zoozve"), 0f, 50f, calc.getTime(10f));
            } else {
                SectorEntityToken Venus = system.getEntityById("Venus");
                if (Venus != null) {
                    spawnWatchtower(system, Venus, Venus.getCircularOrbitAngle(), Venus.getRadius() * 1.5f, Venus.getCircularOrbitPeriod());
                }
            }

            if (jupiterDetail >= 1 && allowNonVisited) {
                spawnWatchtower(system, system.getEntityById("Elara"), 0f, 100f, calc.getTime(10f));
            } else {
                SectorEntityToken Callisto = system.getEntityById("Callisto");
                if (Callisto != null) {
                    spawnWatchtower(system, Callisto, Callisto.getCircularOrbitAngle(), Callisto.getRadius() * 1.5f, Callisto.getCircularOrbitPeriod());
                }
            }

            spawnWatchtower(system, system.getEntityById("Phoebe"), 180f, 100f, calc.getTime(10f));
            spawnWatchtower(system, system.getEntityById("Nereid"), 180f, 100f, calc.getTime(.48f) * rotMult);

            if (neptuneTrojansDetail >= 1 && allowNonVisited) {
                spawnStation(system, "IndEvo_abandonedPetCenter", "WG157", 50f, 50f, calc.getTime(5f), 2000f);
            }

            addCond(system, "mercury",  "IndEvo_RuinsCondition");
            addCond(system, "Luna",     "IndEvo_RuinsCondition");
            addCond(system, "Mars",     "IndEvo_RuinsCondition");
            addCond(system, "Ceres",    "IndEvo_RuinsCondition");
            addCond(system, "Callisto", "IndEvo_RuinsCondition");
            addCond(system, "Titan",    "IndEvo_RuinsCondition");
            addCond(system, "Iapetus",  "IndEvo_RuinsCondition");
            addCond(system, "Vesta",    "IndEvo_RuinsCondition");
            addCond(system, "Oberon",   "IndEvo_RuinsCondition");
            addCond(system, "Rhea",     "IndEvo_RuinsCondition");
            addCond(system, "Triton",   "IndEvo_RuinsCondition");

            spawnWatchtower(system, system.getEntityById("Nix"), 50f, 50f, calc.getTime(5f));

            if (allowNonVisited) {
                SectorEntityToken Eris = system.getEntityById("Eris");
                if (Eris != null) {
                    float p_ErisDysnomia = calc.getTime(15.7f);
                    float[] erisOffsets = calc.getBinaryOffsetsReal(2326f, 700f, 16f / 2f);
                    spawnWatchtower(system, Eris, 0f, erisOffsets[1] - erisOffsets[0], p_ErisDysnomia);
                }

                spawnWatchtower(system, system.getEntityById("Namaka"), 50f, 50f, calc.getTime(5f));
                spawnWatchtower(system, system.getEntityById("Farfarout"), 200f, 200f, calc.getTime(20f));
                spawnWatchtower(system, system.getEntityById("DeeDee"), 200f, 200f, calc.getTime(20f));

                spawnStation(system, "IndEvo_arsenalStation", "Chiminigagua", 50f, 100f, calc.getTime(10f), 2000f);

                addCond(system, "Eris",   "IndEvo_RuinsCondition");
                addCond(system, "Haumea", "IndEvo_RuinsCondition");
            }

            addCond(system, "Pluto", "IndEvo_RuinsCondition");

            if (numberArtilleryStations >= 1) {
                spawnArtillery(system, "mercury", "remnant", "missile");
            } else {
                addCond(system, "mercury", "IndEvo_ArtilleryStationCondition");
            }
            if (numberArtilleryStations >= 2) {
                if (remnantHorde <= 2) {
                    spawnArtillery(system, "Callisto", "pirates", "railgun");
                } else {
                    spawnArtillery(system, "Mars", "remnant", "railgun");
                }
            }
            if (numberArtilleryStations >= 3) {
                spawnArtillery(system, "Luna", "remnant", "mortar");
            }
            if(visitedDetail >= 1 || innerSolDetail >= 1){
                spawnWatchtower(system, system.getEntityById("Kamooalewa"), 0f, 50f, calc.getTime(10f));
            } else{
                SectorEntityToken Luna = system.getEntityById("Luna");
                if (Luna != null) {
                    spawnWatchtower(system, Luna, Luna.getCircularOrbitAngle(), Luna.getRadius() * 1.5f, Luna.getCircularOrbitPeriod());
                }
            }
        }

        // =========================================================================
        // Tasc difficulty
        // =========================================================================
        if (Global.getSettings().getModManager().isModEnabled("Terraforming & Station Construction")) {
            addCond(system, "mercury", "pollution");
            addCond(system, "Earth",   "pollution");
            addCond(system, "Mars",    "pollution");
            addCond(system, "Phobos",  "pollution");
            addCond(system, "Ceres",   "pollution");
            addCond(system, "Vesta",   "pollution");
        }

        // =========================================================================
        // Aotd compat
        // =========================================================================
        if (Global.getSettings().getModManager().isModEnabled("aotd_vok")) {
            addCond(system, "Phoebe", "pre_collapse_facility");
            addCond(system, "Farout",       "pre_collapse_facility");
            addCond(system, "Chiminigagua", "pre_collapse_facility");
            addCond(system, "Agamemnon",    "pre_collapse_facility");
            addCond(system, "Chariklo",       "pre_collapse_facility");
            addCond(system, "SolX", "pre_collapse_facility");
        }

        // =========================================================================
        // Unkown Skies compat
        // =========================================================================
        if (Global.getSettings().getModManager().isModEnabled("US")) {
            addCond(system, "Callisto", "US_base");
            addCond(system, "Earth",    "US_religious");
            addCond(system, "Uranus",   "US_floating");
            addCond(system, "Chiminigagua", "US_cryosanctum");
        }

        // =========================================================================
        // Random Assortment of Things compat
        // =========================================================================
        if (Global.getSettings().getModManager().isModEnabled("assortment_of_things")) {
            addCond(system, "mercury", "rat_ancient_military_hub");
            addCond(system, "mercury", "rat_rampant_military_core");
            addCond(system, "Uranus",  "rat_ancient_fuel_hub");
            addCond(system, "Titania", "rat_warscape");
            addCond(system, "Biden", "rat_rampant_military_core");
            
            addCond(system, "Mars", "rat_ancient_megacities");
        } // Rat stuff spawns on its own so Rat might be kinda dangerous

        // =============================================================
        // PROCEDURAL DERELICT GENERATION
        // =============================================================
        BaseThemeGenerator.StarSystemData solSystemData = BaseThemeGenerator.computeSystemData(system);
        MiscellaneousThemeGenerator miscThemeGen = new MiscellaneousThemeGenerator();
        WeightedRandomPicker<String> factionPicker = SalvageSpecialAssigner.getNearbyFactions(new Random(), system.getCenter(), 15f, 5f, 5f);
        miscThemeGen.addDerelictShips(solSystemData, 1f, 20, 30, factionPicker);

        // In unverse all of these but mercury can be chalked up to being coincidental, especially useless asf Hyperion
        // ## PHAETHON
        // I thought it was a perfect match, cause I can't read an H. It's still close enough tho.
        spawnWreck(system, system.getEntityById("Phaethon"), "phaeton_Standard", 90f, 50f, calc.getTime(10f));

        // ## HYPERION
        spawnWreck(system, system.getEntityById("Hyperion"), "hyperion_Strike", 120f, 50f, calc.getTime(10f));

        // ## ATLAS
        spawnWreck(system, system.getEntityById("Atlas"), "atlas_Standard", 210f, 20f, calc.getTime(2f));

        // ## PROMETHEUS
        spawnWreck(system, system.getEntityById("Prometheus"), "prometheus_Super", 300f, 20f, calc.getTime(2f));

        // ## MERCURY
        // Wouldnt want to be op by giving a better mercury than battered would we?
        spawnWreck(system, system.getEntityById("mercury"), "mercury_Standard", 180f, 460f, calc.getTime(10f));
    }

    // -----------------------------------------------------------------------------
    // Helpers: all resolve/guard internally so callers stay flat and null-safe
    // -----------------------------------------------------------------------------

    private MarketAPI marketOf(StarSystemAPI system, String id) {
        SectorEntityToken e = system.getEntityById(id);
        return e == null ? null : e.getMarket();
    }

    private void addCond(StarSystemAPI system, String id, String cond) {
        MarketAPI m = marketOf(system, id);
        if (m != null) {
            m.addCondition(cond);
        } else {
            Global.getLogger(SolMisc.class).warn(
                    "addCond skipped: no market for '" + id + "' (cond " + cond + ")");
        }
    }

    private void removeCond(StarSystemAPI system, String id, String cond) {
        MarketAPI m = marketOf(system, id);
        if (m != null) {
            m.removeCondition(cond);
        } else {
            Global.getLogger(SolMisc.class).warn(
                    "removeCond skipped: no market for '" + id + "' (cond " + cond + ")");
        }
    }

    private void spawnArtillery(StarSystemAPI system, String id, String faction, String weapon) {
        MarketAPI m = marketOf(system, id);
        if (m == null) {
            Global.getLogger(SolMisc.class).warn(
                    "spawnArtillery skipped: no market for '" + id + "' (" + faction + "/" + weapon + ")");
            return;
        }
        ArtillerySpawnTool.spawnArtilleryStation(m, faction, weapon);
    }

    private void spawnStation(StarSystemAPI system, String type, String anchorId,
                              float angle, float orbitRadius, float period, float sensorProfile) {
        SectorEntityToken anchor = system.getEntityById(anchorId);
        if (anchor == null) {
            Global.getLogger(SolMisc.class).warn(
                    "spawnStation skipped: no anchor '" + anchorId + "' for " + type);
            return;
        }
        SectorEntityToken e = system.addCustomEntity(null, null, type, "neutral");
        e.setCircularOrbitPointingDown(anchor, angle, orbitRadius, period);
        e.setDiscoverable(true);
        e.setSensorProfile(sensorProfile);
    }

    private void spawnWatchtower(StarSystemAPI system, SectorEntityToken anchor, float angle, float orbitRadius, float period) {
        if (anchor == null) {
            Global.getLogger(SolMisc.class).warn("spawnWatchtower skipped: null anchor");
            return;
        }
        SectorEntityToken wt = system.addCustomEntity(null, null, "IndEvo_Watchtower", "remnant");
        wt.setCircularOrbitPointingDown(anchor, angle, orbitRadius, period);
        wt.setDiscoverable(true);
        wt.setSensorProfile(1000f);
    }

    private void spawnWreck(StarSystemAPI system, SectorEntityToken anchor, String variantId, float angle, float orbitRadius, float period) {
        if (anchor == null) {
            Global.getLogger(SolMisc.class).warn("spawnWreck skipped: null anchor for variant " + variantId);
            return;
        }
        DerelictShipData params = new DerelictShipData(new PerShipData(variantId, ShipCondition.BATTERED), false);
        SectorEntityToken wreck = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        wreck.setCircularOrbit(anchor, angle, orbitRadius, period);
        wreck.setDiscoverable(true);
    }
}