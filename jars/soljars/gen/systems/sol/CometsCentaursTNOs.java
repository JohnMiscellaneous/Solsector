package soljars.gen.systems.sol;

import com.fs.starfarer.api.campaign.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.RingSystemTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantOfficerGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantStationFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator.RemnantSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantAssignmentAI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.List;
import java.util.ArrayList;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.EveryFrameScript;
import org.json.JSONObject;
import java.util.Random;

import soljars.gen.utils.RemnantNexusFactory;
import soljars.gen.systems.sol.SolEconomies;
import soljars.gen.utils.SolHyperspaceGen;
import soljars.gen.utils.RemnantPatrolFactory;
import soljars.gen.utils.AstroCalc;
import soljars.econ.utils.DistanceConditionManager;
import soljars.gen.utils.AstroCalc.CompoundOrbit;
import soljars.gen.utils.AstroCalc.KeplerComponent;
import soljars.gen.utils.CompoundOrbitTool;
import soljars.gen.utils.AstroCalc.SolAsteroidFactory;

// =========================================================================
// ========================= Initialisation ================================
// =========================================================================
public class CometsCentaursTNOs {
    public void spawn(StarSystemAPI system, SectorEntityToken star, float zeroDegGlobal) {

        AstroCalc calc = new AstroCalc();

        SectorEntityToken Saturn = system.getEntityById("Saturn");
        float dist_SaturnRaw = 9.5826f;
        float angleSaturn = Saturn.getCircularOrbitAngle();
        float sz_Saturn = Saturn.getRadius();
        float dist_Saturn = Saturn.getCircularOrbitRadius();
        float p_Saturn = Saturn.getCircularOrbitPeriod();

        SectorEntityToken Uranus = system.getEntityById("Uranus");
        float dist_UranusRaw = 19.1913f;
        float angleUranus = Uranus.getCircularOrbitAngle();
        float p_Uranus = Uranus.getCircularOrbitPeriod();
        Float dist_Uranus = Uranus.getCircularOrbitRadius();
        Float sz_Uranus = Uranus.getRadius();

        SectorEntityToken Neptune = system.getEntityById("Neptune");
        float dist_NeptuneRaw = 30.07f;
        float angleNeptune = Neptune.getCircularOrbitAngle();
        float sz_Neptune = Neptune.getRadius();
        float dist_Neptune = Neptune.getCircularOrbitRadius();
        float p_Neptune = Neptune.getCircularOrbitPeriod(); 



        boolean luddicClaim = false; 
        try {
            JSONObject settings = Global.getSettings().loadJSON("data/config/sol_settings.json");
            luddicClaim = settings.optBoolean("Luddic_Church_Claim_On_Sol", false);
        } catch (Exception e) {}
        if (luddicClaim) {
            system.getMemoryWithoutUpdate().set(MemFlags.CLAIMING_FACTION, Factions.LUDDIC_CHURCH);
        }

        boolean isSettled = true;
        try {
            isSettled = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Generate_Settled_Planets", true);
        } catch (Exception e) {}

        int remnantHorde = 1;
        try {
            remnantHorde = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("remnant_difficulty", 1);
        } catch (Exception e) {}

        int deepSpaceProbes = 1;
        try {
            deepSpaceProbes = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Generate_Probes", 1);
        } catch (Exception e) {}

        boolean mercuryCold = true;
        try {
            mercuryCold = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Mercury_And_Venus_Have_Poor_Light", true);
        } catch (Exception e) {}
        // Uranus and Neptune spawn in with normal gravity, this, after hyperspace initialisation removes high_gravity because they are too low densisty to have higher gravity than earth
        // Thier gravity curve is much longer than earth tho :\
        boolean Uranus_And_Neptune_Have_Normal_Gravity = true;
        try {
            Uranus_And_Neptune_Have_Normal_Gravity = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Uranus_And_Neptune_Have_Normal_Gravity", true);
        } catch (Exception e) {}

        boolean generateElevators = true;
        try {
            generateElevators = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Generate_Space_Elevators", true);
        } catch (Exception e) {}



        // Object Generation Settings
        int DetailSetting = 0;
        try {
            DetailSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Inner_Sol_Detail", 0);
        } catch (Exception e) {}
        boolean innerSolShortlist = true;
        if(DetailSetting >= 1){
            innerSolShortlist = false;  
        }

        DetailSetting = 0;
        try {
            DetailSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Visited_Asteroids_Detail", 0);
        } catch (Exception e) {}
        boolean visitedAsteroidsShortlist = true;
        if(DetailSetting >= 1){
            visitedAsteroidsShortlist = false;  
        }

        DetailSetting = 0;
        try {
            DetailSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Asteroid_Belt_Detail", 0);
        } catch (Exception e) {}
        boolean asteroidBeltShortlist = true;
        if(DetailSetting >= 1){
            asteroidBeltShortlist = false;  
        }

        DetailSetting = 1;
        try {
            DetailSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Jupiter_Trojans_Detail", 1);
        } catch (Exception e) {}
        boolean jupiterTrojansShortlist = true;
        if(DetailSetting >= 1){
            asteroidBeltShortlist = false;  
        }

        int jupiterDetailSetting = 0;
        try {
            jupiterDetailSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Jupiter_Detail", 0);
        } catch (Exception e) {}
        boolean jupiterAll = (jupiterDetailSetting >= 2)? true : false;
        boolean jupiterShortlist = (jupiterDetailSetting >= 1)? false : true;

        int saturnDetailSetting = 0;
        try {
            saturnDetailSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Saturn_Detail", 0);
        } catch (Exception e) {}
        boolean saturnAll = (saturnDetailSetting >= 2)? true : false;
        boolean saturnShortlist = (saturnDetailSetting >= 1)? false : true;

        DetailSetting = 0;
        try {
            DetailSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Uranus_Detail", 0);
        } catch (Exception e) {}
        boolean uranusShortlist = true;
        if(DetailSetting >= 1){
            uranusShortlist = false;  
        }

        DetailSetting = 0;
        try {
            DetailSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Neptune_Detail", 0);
        } catch (Exception e) {}
        boolean neptuneShortlist = true;
        if(DetailSetting >= 1){
            neptuneShortlist = false;  
        }

        DetailSetting = 0;
        try {
            DetailSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Neptune_Trojans_Detail", 1);
        } catch (Exception e) {}
        boolean neptuneTrojansShortlist = true;
        if(DetailSetting >= 1){
            neptuneTrojansShortlist = false;  
        }

        DetailSetting = 0;
        try {
            DetailSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Centaur_Detail", 0);
        } catch (Exception e) {}
        boolean centaurShortlist = true;
        if(DetailSetting >= 1){
            centaurShortlist = false;  
        }

        DetailSetting = 1;
        try {
            DetailSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Kuiper_Detail", 1);
        } catch (Exception e) {}
        boolean transNeptuneShortlist = true;
        if(DetailSetting >= 1){
            transNeptuneShortlist = false;  
        }

        DetailSetting = 1;
        try {
            DetailSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Scattered_Disk_Detail", 1);
        } catch (Exception e) {}
        boolean scatteredDiskShortlist = true;
        if(DetailSetting >= 1){
            scatteredDiskShortlist = false;  
        }


        // Shit from the respectible end of science fiction
        // no inexplicable 2km moons even closer to Jupiter than Thebe, and no ninth planets, leda, 1999 ZX30, and Burns-Caulfield
        boolean fictionalTNOs = true;
        try {
            fictionalTNOs = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Fictional_Trans_Neptunian_Objects", true);
        } catch (Exception e) {}

        // Other Object generation settings
        // Pins pallas -> Ceres, Clete -> Neptune for the intel screen
        boolean falseMoons = true;
        try {
            falseMoons = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("False_Moons", true);
        } catch (Exception e) {}

        // Disables unnamed bodies showing up on map
        int showNamesSetting = 0;
        try {
            showNamesSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Show_Names", 0);
        } catch (Exception e) {}

        boolean showMinorNames;
        boolean showProvisionalNames;
        boolean showCustomNames;
        String showNameMinor;
        String showNameProv;
        String showNameCustom;

        if (showNamesSetting == 3) {
            showMinorNames = true;
            showProvisionalNames = true;
            showCustomNames = true;
            showNameMinor = "w_name";
            showNameProv = "w_name";
            showNameCustom = "w_name";
        } else if (showNamesSetting == 2) {
            showMinorNames = true;
            showProvisionalNames = false;
            showCustomNames = true;
            showNameMinor = "w_name";
            showNameProv = "no_name";
            showNameCustom = "w_name";
        } else if (showNamesSetting == 0) {
            showMinorNames = false;
            showProvisionalNames = false;
            showCustomNames = false;
            showNameMinor = "no_name";
            showNameProv = "no_name";
            showNameCustom = "no_name";
        } else { // default: 1
            showMinorNames = false;
            showProvisionalNames = false;
            showCustomNames = true;
            showNameMinor = "no_name";
            showNameProv = "no_name";
            showNameCustom = "w_name";
        }

        // Single chord moons, indicated bodies, etc, whatevers too controvertial and not cool enough to send it anyways
        // Extreme is UNOBSERVED, and second order explanations, eris may have an inner moonlet that pumps Dysnomias eccentricity, but dysnomia might have just been decked by a tno recently or been a captured binary at a truly extreme distance  (Dysnomias pros captured so not that big a leap)
        int speculativeBodiesSetting = 0;
        try {
            speculativeBodiesSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Speculative_Bodies", 0);
        } catch (Exception e) {}

        boolean speculativeBodies;
        boolean speculativeBodiesExtreme;

        if (speculativeBodiesSetting == 2) {
            speculativeBodies = true;
            speculativeBodiesExtreme = true;
        } else if (speculativeBodiesSetting == 1) {
            speculativeBodies = true;
            speculativeBodiesExtreme = false;
        } else { // default: 0
            speculativeBodies = false;
            speculativeBodiesExtreme = false;
        }


        int speculativeBodiesBigSetting = 0;
        try {
            speculativeBodiesBigSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Speculative_Bodies_Big", 0);
        } catch (Exception e) {}

        boolean planetNine;
        boolean planetTen;
        boolean planetEleven;

        if (speculativeBodiesBigSetting == 3) {
            planetNine = true;
            planetTen = true;
            planetEleven = true;
        } else if (speculativeBodiesBigSetting == 2) {
            planetNine = true;
            planetTen = true;
            planetEleven = false;
        } else if (speculativeBodiesBigSetting == 1) {
            planetNine = true;
            planetTen = false;
            planetEleven = false;
        } else { // default: 0
            planetNine = false;
            planetTen = false;
            planetEleven = false;
        }

        boolean occultOrbitBeta = true;
        try {
            occultOrbitBeta = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Occult_Orbit_Beta", true);
        } catch (Exception e) {}
        if(!occultOrbitBeta){falseMoons = false;}

        int genericAsteroids = 0;
        try {
            genericAsteroids = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Generic_Asteroids", 0);
        } catch (Exception e) {}

        int gen_Hungarians  = Math.round(genericAsteroids * 0.05f);
        int gen_InnerBelt   = Math.round(genericAsteroids * 0.24f);
        int gen_CoreBelt    = Math.round(genericAsteroids * 0.38f);
        int gen_OuterBelt   = Math.round(genericAsteroids * 0.26f);
        int gen_Cybeles     = Math.round(genericAsteroids * 0.07f);

        float rotMult = 4f;
        try {
            rotMult = (float) Global.getSettings().loadJSON("data/config/sol_settings.json").optDouble("rotMult", 4f);
        } catch (Exception e) {}
        float progradeMult = -1f;
        try {
            progradeMult = (float) Global.getSettings().loadJSON("data/config/sol_settings.json").optDouble("progradeMult", -1f);
        } catch (Exception e) {}

        int stablePointDetail = 0;
        try {
            stablePointDetail = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Stable_Points_Detail", 0);
        } catch (Exception e) {}

        // =========================================================================
        // ========================= ACTIVE COMETS =================================
        // =========================================================================
        // --- Short Period Comets ---

        // 67P Churyumov-Gerasimenko
        SectorEntityToken Churymov = calc.spawnSPSObject(system, star, "Churyumov-Gerasimenko", "Churyumov-Gerasimenko", "custom_entity", "Churymov" + showNameCustom, 1f, 3.4622f, 0.6409f, 50.136f, 12.798f, 2015.616f, zeroDegGlobal, -.529f, 1f);

        // 1P/Halley | Retrograde
        SectorEntityToken Halley = calc.spawnSPSObject6(system, star, "Halley", "Halley", "custom_entity", "Halley" + showNameCustom, 11f, 17.9286f, 0.9679f, 59.099f, 112.241f, 1986.11f, zeroDegGlobal, 2.2f, 1f, null, null, null, star, "Sol", true);

        // 109P/Swift-Tuttle | Retrograde
        SectorEntityToken barycenterSwift = calc.spawnSPSObject6(system, star, "Swift-Tuttle", "Swift-Tuttle", "custom_entity", "Swift" + showNameMinor, 1f, 26.0921f, 0.9632f, 139.381f, 152.982f, 1992.945f, zeroDegGlobal, null, 1f, null, null, null, star, "Sol", true);

        // 2P/Encke
        SectorEntityToken Encke = calc.spawnSPSObject(system, star, "Encke", "Encke", "asteroid", showNameMinor, 4.8f, 2.2197f, 0.8477f, 334.194f, 187.134f, 2023.81f, zeroDegGlobal, 0.458f, 1f);

        // 9P/Tempel 1
        SectorEntityToken Tempel1 = calc.spawnSPSObject(system, star, "Tempel", "Tempel", "custom_entity", "Tempel" + showNameCustom, 6.0f, 3.1461f, 0.5097f, 68.754f, 179.197f, 2016.59f, zeroDegGlobal, 1.696f, 1f);

        // 81P/Wild 2
        SectorEntityToken Wild = calc.spawnSPSObject(system, star, "Wild", "Wild", "custom_entity", "Wild" + showNameCustom, 4.0f, 3.4497f, 0.5374f, 136.110f, 41.725f, 2022.96f, zeroDegGlobal, 0.562f, 1f);

        // 19P/Borrelly
        SectorEntityToken barycenterBorrelly = calc.spawnSPSObject(system, star, "Borrelly", "Borrelly", "custom_entity", "Borelly" + showNameCustom, 1f, 3.6070f, 0.6379f, 74.301f, 351.862f, 2022.088f, zeroDegGlobal, 1.04f, 1f);

        // 103P/Hartley 2
        SectorEntityToken barycenterHartley = calc.spawnSPSObject(system, star, "Hartley", "Hartley", "custom_entity", "Hartley" + showNameCustom, 1f, 3.4757f, 0.6936f, 219.742f, 181.322f, 2017.301f, zeroDegGlobal, 0.68f, 1f);

        // =========================================================================
        // ======================= LONG PERIOD COMETS ==============================
        // =========================================================================

        // cut cuz tiny and violates eccentricity immersion, bad :/

        // =========================================================================
        // =================== CENTAURS & GIANT CROSSERS ===========================
        // =========================================================================

        // --- The Ringed Centaurs ---

        // Chariklo
        PlanetAPI Chariklo = (PlanetAPI) calc.spawnSPSObject7(system, star, "Chariklo", "Chariklo", "rocky_ice", null, 250f, 15.7400f, 0.1702f, 300.475f, 241.224f, 2003.87f, zeroDegGlobal, null, 1f, null, null, null, star, "Sol", false, false);

        // Chariklo Rings
        // C1R "Oiapoque": 391 km → 3.3x radius, tripled to 9.9x
        // C2R "Chuí": 405 km → 3.4x radius, tripled to 10.2x
        float sz_Chariklo = Chariklo.getRadius();
        float r_C1R = sz_Chariklo * 9.9f;
        float r_C2R = sz_Chariklo * 10.2f;

        system.addRingBand(Chariklo, "sol_rings", "rings_alpha2", 256, 3, Color.RED, 8f, r_C1R, (progradeMult * 10));
        system.addRingBand(Chariklo, "sol_rings", "rings_alpha1", 256, 2, Color.RED, 5f, r_C2R, (progradeMult * 13));
        system.addTerrain(Terrain.RING, new RingSystemTerrainPlugin.RingParams(15f, (r_C1R + r_C2R) / 2f, Chariklo, "Oiapoque e Chui rings")).setCircularOrbit(Chariklo, 0, 0, (progradeMult * 60));

        Chariklo.getSpec().setTexture("graphics/planets/chariklo_tx.jpg"); 
        Chariklo.getSpec().setAtmosphereThickness(0f); 
        Chariklo.getSpec().setAtmosphereThicknessMin(0f); 
        Chariklo.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
        Chariklo.getSpec().setIconColor(new Color(150, 100, 80, 255)); 
        Chariklo.getSpec().setTilt(0f); 
        Chariklo.getSpec().setPitch(30f); 
        Chariklo.getSpec().setRotation(calc.getRot(.292f)); 
        Chariklo.applySpecChanges();

        calc.addConditions(Chariklo.getMarket(), new String[] {
            "very_cold",
            "low_gravity",
            "no_atmosphere",
            "volatiles_diffuse",
            "ore_sparse",
            "poor_light",
            "ruins_widespread",
            "sol_dist_distant"
        });
        if(speculativeBodies){
        calc.spawnMoon(system, Chariklo, "Chariklo Beta", calc.getSize(5f), r_C2R + 10f, calc.getTime(12f), 180f, showProvisionalNames);
        }

        Chariklo.setSkipForJumpPointAutoGen(true);

        Chariklo.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 13.077f);
        Chariklo.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 18.420f);

        // Chiron
        PlanetAPI Chiron = (PlanetAPI) calc.spawnSPSObject7(system, falseMoons ? Chariklo : star, "Chiron", "Chiron", "cryovolcanic", null, 220f, 13.6922f, 0.3790f, 209.298f, 339.254f, 2046.60f, zeroDegGlobal, null, 1f, null, null, null, star, "Sol", false, falseMoons);

        // Chiron Rings (uncertain — likely cryovolcanic debris)
        // Real: ~324 km → 3.0x radius, tripled to 9.0x
        float sz_Chiron = Chiron.getRadius();
        float r_ChironRing = sz_Chiron * 9.0f;

        system.addRingBand(Chiron, "sol_rings", "rings_alpha0", 256, 1, Color.RED, 15f, r_ChironRing, (progradeMult * 10));
        system.addRingBand(Chiron, "sol_rings", "rings_alpha1", 256, 2, Color.RED, 5f, r_ChironRing + 8f, (progradeMult * 15));
        system.addTerrain(Terrain.RING, new RingSystemTerrainPlugin.RingParams(20f, r_ChironRing, Chiron, "Chiron Ring")).setCircularOrbit(Chiron, 0, 0, (progradeMult * 60));

        Chiron.getSpec().setTexture("graphics/planets/chiron_tx.jpg"); 
        Chiron.getSpec().setAtmosphereThickness(0f); 
        Chiron.getSpec().setAtmosphereThicknessMin(0f); 
        Chiron.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
        Chiron.getSpec().setIconColor(new Color(160, 140, 130, 255)); 
        Chiron.getSpec().setTilt(0f); 
        Chiron.getSpec().setPitch(40f); 
        Chiron.getSpec().setRotation(calc.getRot(.247f)); 
        Chiron.getSpec().setCloudColor(new Color(0, 0, 0, 0));
        Chiron.applySpecChanges();

        calc.addConditions(Chiron.getMarket(), new String[] {
            "very_cold",
            "low_gravity",
            "no_atmosphere",
            "volatiles_plentiful",
            "ore_moderate",
            "rare_ore_sparse",
            "sol_fast_rotator",
            "sol_dist_distant",
            "poor_light",
            "sol_comet_active"
        });

        Chiron.setSkipForJumpPointAutoGen(true);

        // "Chiron II" — ring-shepherd inferred from ring gap structure
        // Same basis as chariklo II
        if(speculativeBodies){
        calc.spawnMoon(system, Chiron, "Chiron II", calc.getSize(5f), r_ChironRing + 10f, calc.getTime(10f), 45f, showProvisionalNames);
        }

        DistanceConditionManager.track(Chiron.getMarket());
        Chiron.getMarket().getMemoryWithoutUpdate().set("$sol_comet_distance", 12.5f);
        Chiron.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 8.533f);
        Chiron.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 18.87f);

        // 60558 Echeclus
        // mysterious cryovolcanism
        SectorEntityToken Echeclus = calc.spawnSPSObject(system, star, "Echeclus", "Echeclus", "asteroid", showNameMinor, 60f, 10.7588f, 0.4545f, 173.263f, 163.495f, 2015.29f, zeroDegGlobal, 1.117f, 1f);
        Echeclus.setCustomDescriptionId("sol_echeclus");

        SectorEntityToken echeclusProbe = DerelictThemeGenerator.addSalvageEntity(system, Entities.DERELICT_SURVEY_PROBE, Factions.DERELICT); 
        echeclusProbe.setCircularOrbitPointingDown(Echeclus, 90, 40f, calc.getTime(5f)); 

        // --- Standard Centaurs ---

        // Bienor
        // According to Rizos et al. 2024 (A&A 689, A82) Beinor's lightcurve has a serious drug problem.
        // Binary explains the irregular hours where the lightcurve shines brighter
        // talked about ~16% mass, companion, assuming equal density thats 113 km

        if(speculativeBodies){
            SectorEntityToken bienorBarycenter = calc.spawnSPSObject(system, star, "bienor_barycenter", "Bienor Barycenter", "custom_entity", "empty", 1f, 16.6006f, 0.2054f, 337.804f, 152.413f, 2027.99f, zeroDegGlobal, null, 1f);

            float sz_BienorA = calc.getSize(198f);
            float sz_BienorB = calc.getSize(113f);
            float[] bienorOffsets = calc.getBinaryOffsetsReal(198f, 113f, 5f);

            SectorEntityToken Bienor  = calc.spawnMoon(system, bienorBarycenter, "Bienor",   sz_BienorA, bienorOffsets[0], calc.getTime(0.381f), 0f,   showMinorNames);
            SectorEntityToken BienorB = calc.spawnMoon(system, bienorBarycenter, "Bienor Beta", sz_BienorB, bienorOffsets[1], calc.getTime(0.381f), 180f, showProvisionalNames);
        } else {
            SectorEntityToken Bienor = calc.spawnSPSObject(system, star, "Bienor", "Bienor", "asteroid", showNameMinor, 198f, 16.6006f, 0.2054f, 337.804f, 152.413f, 2027.99f, zeroDegGlobal, 0.381f, 1f);
        }

        // Pholus
        SectorEntityToken Pholus = calc.spawnSPSObject(system, star, "Pholus", "Pholus", "asteroid", showNameMinor, 90f, 20.2834f, 0.5747f, 119.290f, 354.730f, 1991.77f, zeroDegGlobal, 0.416f, 1f);

        // 65489 Ceto-Phorcys
        float sz_Ceto = calc.getSize(223f); 
        float sz_Phorcys = calc.getSize(171f); 
        float[] cetoOffsets = calc.getBinaryOffsetsReal(223f, 171f, 12f);
        float p_CetoPhorcys = calc.getTime(9.55f);
        float[][] cetoExtras = new float[][]{
            { cetoOffsets[0], 0f,  0f, p_CetoPhorcys,  0f, +1f } 
        };
        SectorEntityToken Ceto = calc.spawnSPSObject5(system, star, "Ceto", "Ceto", "asteroid", showNameMinor, 223f, 100.4780f, 0.8238f, 171.954f, 319.464f, 1989.59f, zeroDegGlobal, null, 1f, null, null, cetoExtras, star);

        SectorEntityToken Phorcys = calc.spawnMoon(system, Ceto, "Phorcys", sz_Phorcys, cetoOffsets[0]+cetoOffsets[1], p_CetoPhorcys, 180f, showMinorNames);
        Ceto.setCustomDescriptionId("sol_ceto");
        Phorcys.setCustomDescriptionId("sol_phorcys");

        SectorEntityToken cetoProbe = DerelictThemeGenerator.addSalvageEntity(system, Entities.DERELICT_SURVEY_PROBE, Factions.DERELICT); 
        cetoProbe.setCircularOrbitPointingDown(Ceto, 90, 40f, calc.getTime(5f));

        // 5335 Damocles
        SectorEntityToken Damocles = calc.spawnSPSObject(system, star, "Damocles", "Damocles", "asteroid", showNameMinor, 10f, 11.8671f, 0.8666f, 314.211f, 191.108f, 2031.62f, zeroDegGlobal, null, 1f);

        // 42355 Typhon-Echidna
        SectorEntityToken typhonBarycenter = calc.spawnSPSObject(system, star, "typhon_barycenter", "Typhon Barycenter", "custom_entity", "empty", 1f, 37.7071f, 0.5367f, 351.859f, 158.746f, 2006.33f, zeroDegGlobal, null, 1f);
        float sz_Typhon = calc.getSize(162f);
        float sz_Echidna = calc.getSize(89f);
        float typhonPeriodRaw = 18.9f / calc.distDToPeriodD(2f);
        SectorEntityToken[] typhonBinary = calc.spawnEllipticalBinary(system, typhonBarycenter,
            "typhon", "Typhon", sz_Typhon, "asteroid", showNameMinor,
            "echidna", "Echidna", sz_Echidna, "asteroid", showNameMinor,
            sz_Typhon * 19.5f / 2f, 0.537f, calc.getTime(typhonPeriodRaw), 0f);

        SectorEntityToken Typhon = typhonBinary[0];
        SectorEntityToken Echidna = typhonBinary[1];

        SectorEntityToken Xewioso = calc.spawnSPSObject(system, star, "Xewioso", "Xewioso", "asteroid", showNameMinor, 565f, 37.6872f, 0.2435f, 46.729f, 248.379f, 1926.52f, zeroDegGlobal, null, 1f);

        if(!centaurShortlist){
        // 944 Hidalgo
        SectorEntityToken Hidalgo = calc.spawnSPSObject(system, star, "Hidalgo", "Hidalgo", "asteroid", showNameMinor, 38f, 5.7283f, 0.6622f, 21.363f, 56.598f, 2032.54f, zeroDegGlobal, 0.421f, 1f);
        SectorEntityToken hidalgoProbe = DerelictThemeGenerator.addSalvageEntity(system, Entities.DERELICT_SURVEY_PROBE, Factions.DERELICT); hidalgoProbe.setId("hidalgo_probe");
        hidalgoProbe.setName("Guadalupe"); 
        hidalgoProbe.setCircularOrbitPointingDown(Hidalgo, 90, 40f, calc.getTime(5f));
        // 20461 Dioretsa
        SectorEntityToken Dioretsa = calc.spawnSPSObject(system, star, "Dioretsa", "Dioretsa", "asteroid", showNameMinor, 14f, 23.7704f, 0.9004f, 297.119f, 102.244f, 1999.84f, zeroDegGlobal, null, 1f);
        // 8405 Asbolus
        SectorEntityToken Asbolus = calc.spawnSPSObject(system, star, "Asbolus", "Asbolus", "asteroid", showNameMinor, 80f, 18.0636f, 0.6173f, 6.021f, 290.584f, 2002.51f, zeroDegGlobal, 0.373f, 1f);
        // 31824 Elatus
        SectorEntityToken Elatus = calc.spawnSPSObject(system, star, "Elatus", "Elatus", "asteroid", showNameMinor, 57f, 11.7607f, 0.3838f, 87.021f, 281.981f, 2038.94f, zeroDegGlobal, 1.117f, 1f);
        // 32532 Thereus
        SectorEntityToken Thereus = calc.spawnSPSObject(system, star, "Thereus", "Thereus", "asteroid", showNameMinor, 62f, 10.6384f, 0.1976f, 205.176f, 86.494f, 2033.80f, zeroDegGlobal, 0.3475f, 1f);
        // 7066 Nessus
        SectorEntityToken Nessus = calc.spawnSPSObject(system, star, "Nessus", "Nessus", "asteroid", showNameMinor, 60f, 24.5166f, 0.5177f, 31.292f, 170.372f, 1991.99f, zeroDegGlobal, null, 1f);
        // 10370 Hylonome
        SectorEntityToken Hylonome = calc.spawnSPSObject(system, star, "Hylonome", "Hylonome", "asteroid", showNameMinor, 70f, 24.9448f, 0.2461f, 178.212f, 5.337f, 1995.18f, zeroDegGlobal, null, 1f);
        // 49036 Pelion
        SectorEntityToken Pelion = calc.spawnSPSObject(system, star, "Pelion", "Pelion", "asteroid", showNameMinor, 37f, 20.0509f, 0.1321f, 126.887f, 154.010f, 1992.73f, zeroDegGlobal, null, 1f);
        // 55576 Amycus
        SectorEntityToken Amycus = calc.spawnSPSObject(system, star, "Amycus", "Amycus", "asteroid", showNameMinor, 76f, 24.9379f, 0.3925f, 315.505f, 238.682f, 2003.01f, zeroDegGlobal, 0.407f, 1f);
        // 1999 TD10
        SectorEntityToken TD10 = calc.spawnSPSObject(system, star, "TD10", "1999 TD10", "asteroid", showNameProv, 104f, 98.4667f, 0.8743f, 184.608f, 173.033f, 1999.83f, zeroDegGlobal, 0.6417f, 1f);
        // 2002 GZ32
        SectorEntityToken GZ32 = calc.spawnSPSObject(system, star, "GZ32", "2002 GZ32", "asteroid", showNameProv, 220f, 22.9872f, 0.2170f, 107.296f, 155.217f, 2019.58f, zeroDegGlobal, 0.242f, 1f);
        // 1998 BU48
        SectorEntityToken BU48 = calc.spawnSPSObject(system, star, "BU48", "1998 BU48", "asteroid", showNameProv, 180f, 33.3244f, 0.3851f, 132.671f, 282.708f, 1977.87f, zeroDegGlobal, 0.525f, 1f);
        // 1999 OX3
        SectorEntityToken OX3 = calc.spawnSPSObject(system, star, "OX3", "1999 OX3", "asteroid", showNameProv, 160f, 32.6565f, 0.4609f, 259.169f, 144.563f, 2024.14f, zeroDegGlobal, 0.386f, 1f);
        // 2003 UY292
        SectorEntityToken UY292 = calc.spawnSPSObject(system, star, "UY292", "2003 UY292", "asteroid", showNameProv, 210f, 21.8641f, 0.2722f, 28.284f, 23.882f, 2003.81f, zeroDegGlobal, null, 1f);
        }

        // =========================================================================
        // ==================== PLUTINOS (3:2) ===================================== 
        // =========================================================================

        // --- The Pluto System ---
        float dist_PlutoRaw = 39.5886f;
        float _pluRad = calc.getSize(2376f);
        float _pluMoonExt = _pluRad * 5.0f;
        float _pluMoonInt = _pluMoonExt * 0.5f;
        float p_PlutoCharon = -calc.getTime(6.387f);
        float rot_PlutoCharon = 360f / (p_PlutoCharon * 10f);
        float[] pluOffsets = calc.getBinaryOffsetsReal(2376f, 1212f, 9f);

        // Pluto
        PlanetAPI Pluto = (PlanetAPI) calc.spawnSPSObject3(system, star, "Pluto", "Pluto", "cryovolcanic", null, 2376f, 39.5886f, 0.2518f, 110.292f, 113.709f, 1989.78f, zeroDegGlobal, null, 1f, null, null, true, pluOffsets[0], 0f, p_PlutoCharon);
        float p_Pluto = ((CompoundOrbit) Pluto.getOrbit()).getCircularOrbitPeriod();

        float anglePluto = Pluto.getCircularOrbitAngle(); // will be garbage if occultOrbitBeta is on, hardcode if needed
        Pluto.getSpec().setRotation(-rot_PlutoCharon);
        Pluto.applySpecChanges();

        Pluto.getSpec().setTexture("graphics/planets/pluto_tx.jpg"); 
        Pluto.getSpec().setAtmosphereThickness(0.08f); 
        Pluto.getSpec().setAtmosphereThicknessMin(2f); 
        Pluto.getSpec().setAtmosphereColor(new Color(150, 180, 255, 55)); 
        Pluto.getSpec().setIconColor(new Color(230, 210, 190, 255)); 
        Pluto.getSpec().setTilt(122.5f); 
        Pluto.getSpec().setPitch(90f);
        Pluto.getSpec().setCloudTexture("graphics/planets/clouds_mars.png");  
        Pluto.getSpec().setRotation(-rot_PlutoCharon); 
        Pluto.applySpecChanges();

        calc.addConditions(Pluto.getMarket(), new String[] {
            "very_cold",
            "dark",
            "thin_atmosphere",
            "volatiles_plentiful",
            "ore_moderate",
            "rare_ore_sparse",
            "ruins_extensive",
            "sol_inter_binary_elevator",
            "sol_degenerate",
            "sol_subsurface_ocean",
            "sol_frozen_atmosphere_polar",
            "sol_automated_habitats"
        });

        Pluto.setSkipForJumpPointAutoGen(true);

        // Charon
        float sz_Charon = calc.getSize(1212f);
        float dist_Charon_Local = pluOffsets[0] + pluOffsets[1];
        PlanetAPI Charon = system.addPlanet("Charon", Pluto, "Charon", "frozen", 180f, sz_Charon, dist_Charon_Local, p_PlutoCharon);
        Charon.getSpec().setRotation(-rot_PlutoCharon);
        Charon.applySpecChanges();
        Charon.setCircularOrbit(Pluto, 180, dist_Charon_Local, p_PlutoCharon);

        Charon.getSpec().setTexture("graphics/planets/charon_tx.jpg"); 
        Charon.getSpec().setAtmosphereThickness(0f); 
        Charon.getSpec().setAtmosphereThicknessMin(0f); 
        Charon.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Charon.getSpec().setIconColor(new Color(200, 190, 180, 255)); 
        Charon.getSpec().setTilt(122.5f); 
        Charon.getSpec().setPitch(90f); 
        Charon.getSpec().setRotation(-rot_PlutoCharon); 
        Charon.applySpecChanges(); 

        calc.addConditions(Charon.getMarket(), new String[] {
            "very_cold",
            "dark",
            "low_gravity",
            "no_atmosphere",
            "volatiles_diffuse",
            "ore_moderate",
            "ruins_widespread",
            "sol_inter_binary_elevator",
            "sol_degenerate",
            "sol_automated_habitats"
        });

        Charon.setSkipForJumpPointAutoGen(true);

        SectorEntityToken plutoBarycenter;
        if (generateElevators) {
            SectorEntityToken elevatorpc = calc.spawnTransBinaryElevator(system, Pluto, "ElevatorPC", "Pluto-Charon Elevator", pluOffsets[0] + pluOffsets[1], 180f, p_PlutoCharon);
            plutoBarycenter = system.addCustomEntity("PCcenter", "Pluto-Charon Elevator", "CenterpointStation", "neutral"); 
            plutoBarycenter.setCircularOrbitPointingDown(Pluto, 180f, pluOffsets[0], p_PlutoCharon);
        } else {
            plutoBarycenter = system.addCustomEntity("plutoBarycenter", "plutoBarycenter", "empty", "neutral"); 
            plutoBarycenter.setCircularOrbitPointingDown(Pluto, 180f, pluOffsets[0], p_PlutoCharon);
        }

        // Minor Moons (Styx, Nix, Kerberos, Hydra) ALL CHAOTIC ROTATORS
        float spinStyx = calc.getRot(calc.getRandomRotationPeriod(16f));
        SectorEntityToken Styx = system.addCustomEntity("Styx", "Styx", "Styx" + showNameCustom, "neutral"); 
        Styx.setCircularOrbitWithSpin(plutoBarycenter, 123f, dist_Charon_Local + sz_Charon + (_pluMoonExt * (float) Math.log(_pluMoonInt * 0.007f + 1)), p_PlutoCharon * 3f, spinStyx, spinStyx);

        float spinNix = calc.getRot(calc.getRandomRotationPeriod(50f));
        SectorEntityToken Nix = system.addCustomEntity("Nix", "Nix", "Nix" + showNameCustom, "neutral"); 
        Nix.setCircularOrbitWithSpin(plutoBarycenter, 89f, dist_Charon_Local + sz_Charon + (_pluMoonExt * (float) Math.log(_pluMoonInt * 0.010f + 1)), p_PlutoCharon * 4f, spinNix, spinNix);

        float spinKerberos = calc.getRot(calc.getRandomRotationPeriod(19f));
        SectorEntityToken Kerberos = system.addCustomEntity("Kerberos", "Kerberos", "Kerberos" + showNameCustom, "neutral"); 
        Kerberos.setCircularOrbitWithSpin(plutoBarycenter, 123f, dist_Charon_Local + sz_Charon + (_pluMoonExt * (float) Math.log(_pluMoonInt * 0.014f + 1)), p_PlutoCharon * 5f, spinKerberos, spinKerberos);

        float spinHydra = calc.getRot(calc.getRandomRotationPeriod(51f));
        SectorEntityToken Hydra = system.addCustomEntity("Hydra", "Hydra", "Hydra" + showNameCustom, "neutral"); 
        Hydra.setCircularOrbitWithSpin(plutoBarycenter, 306f, dist_Charon_Local + sz_Charon + (_pluMoonExt * (float) Math.log(_pluMoonInt * 0.020f + 1)), p_PlutoCharon * 6f, spinHydra, spinHydra);

        JumpPointAPI HydraJump = Global.getFactory().createJumpPoint("jp_hydra", "Pluto Jump Point");
        HydraJump.setStandardWormholeToHyperspaceVisual();
        HydraJump.setCircularOrbit(Hydra, Hydra.getCircularOrbitAngle(), 100, p_PlutoCharon * 6f); 
        system.addEntity(HydraJump);

        float map_1AU_at_Pluto = calc.getDist(40, star) - calc.getDist(39f, star); 

        DistanceConditionManager.track(Pluto.getMarket());
        DistanceConditionManager.track(Charon.getMarket());
        Pluto.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 29.5f);
        Pluto.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 49.3f);
        Charon.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 29.5f);
        Charon.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 49.3f);

        Pluto.getMarket().getMemoryWithoutUpdate().set("$sol_polar_atmosphere_level", 2);

        // Arawn | 142 km | quasi-satellite of Pluto
        // ignore the lies that say arawn is no quasi moon
        SectorEntityToken Arawn = calc.spawnSPSObject2(system, star, "Arawn", "Arawn", "custom_entity", "Arawn" + showNameMinor, 142f, 39.2077f, 0.1141f, 144.743f, 101.223f, 1995.58f, zeroDegGlobal, null, 1f, p_Pluto, dist_PlutoRaw);
        Arawn.setCustomDescriptionId("sol_arawn");

        float p_OrcusVanth = -calc.getTime(9.5f);
        float rot_OrcusVanth = (360f / (p_OrcusVanth * 10f));  

        float sz_Orcus = calc.getSize(910f);
        float sz_Vanth = calc.getSize(442f);
        float[] orcOffsets = calc.getBinaryOffsetsReal(910f, 442f, 12f);

        // Orcus | THE ANTIPLUTO
        PlanetAPI Orcus = (PlanetAPI) calc.spawnSPSObject3(system, star, "Orcus", "Orcus", "cryovolcanic", null, 910f, 39.3358f, 0.2217f, 268.385f, 73.722f, 2143.69f, zeroDegGlobal, null, 1f, null, null, true, orcOffsets[0], 0f, p_OrcusVanth);

        Orcus.getSpec().setTexture("graphics/planets/orcus_tx.jpg"); 
        Orcus.getSpec().setAtmosphereThickness(0f); 
        Orcus.getSpec().setAtmosphereThicknessMin(0f); 
        Orcus.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Orcus.getSpec().setIconColor(new Color(160, 160, 170, 255)); 
        Orcus.getSpec().setTilt(0f); 
        Orcus.getSpec().setPitch(90f); 
        Orcus.getSpec().setCloudColor(new Color(255, 240, 200, 0)); 
        Orcus.getSpec().setRotation(-rot_OrcusVanth); 
        Orcus.applySpecChanges(); 

        calc.addConditions(Orcus.getMarket(), new String[] {
            "very_cold",
            "dark",
            "no_atmosphere",
            "volatiles_plentiful",
            "ore_moderate",
            "ruins_widespread",
            "sol_inter_binary_elevator",
            "sol_insurgent_network",
            "sol_automated_habitats",
            "sol_ai_freedom_fighters"
        });

        Orcus.setSkipForJumpPointAutoGen(true);

        // Vanth 
        PlanetAPI Vanth = system.addPlanet("Vanth", Orcus, "Vanth", "rocky_ice", 180, sz_Vanth, orcOffsets[0] + orcOffsets[1], p_OrcusVanth);

        Vanth.getSpec().setTexture("graphics/planets/vanth_tx.jpg"); 
        Vanth.getSpec().setAtmosphereThickness(0f); 
        Vanth.getSpec().setAtmosphereThicknessMin(0f); 
        Vanth.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Vanth.getSpec().setIconColor(new Color(140, 130, 130, 255)); 
        Vanth.getSpec().setTilt(0f); 
        Vanth.getSpec().setPitch(90f); 
        Vanth.getSpec().setRotation(-rot_OrcusVanth); 
        Vanth.applySpecChanges(); 

        calc.addConditions(Vanth.getMarket(), new String[] {
            "very_cold",
            "dark",
            "low_gravity",
            "no_atmosphere",
            "volatiles_trace",
            "ore_moderate",
            "ruins_scattered",
            "sol_inter_binary_elevator"
        });

        Vanth.setSkipForJumpPointAutoGen(true);

        if (generateElevators) {
            SectorEntityToken elevatorOV = calc.spawnTransBinaryElevator(system, Orcus, "ElevatorOV", "Orcus-Vanth Elevator", orcOffsets[0] + orcOffsets[1], 180f, p_OrcusVanth);
            SectorEntityToken orcusBarycenter = system.addCustomEntity("OVcenter", "Orcus-Vanth Elevator", "CenterpointStation", "neutral"); 
            orcusBarycenter.setCircularOrbitPointingDown(Orcus, 180f, orcOffsets[0], p_OrcusVanth);
        }

        DistanceConditionManager.track(Orcus.getMarket());
        DistanceConditionManager.track(Vanth.getMarket());
        Orcus.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 30.281f);
        Orcus.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 48.067f);
        Vanth.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 30.281f);
        Vanth.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 48.067f);

        // Ixion
        PlanetAPI Ixion = (PlanetAPI) calc.spawnSPSObject7(system, falseMoons ? Pluto : star, "Ixion", "Ixion", "frozen", null, 617f, 39.3505f, 0.2442f, 71.0929f, 300.658f, 2071.01f, zeroDegGlobal, null, 1f, null, null, null, star, "Sol", false, falseMoons);

        Ixion.getSpec().setTexture("graphics/planets/ixion_tx.jpg");
        Ixion.getSpec().setAtmosphereThickness(0f);
        Ixion.getSpec().setAtmosphereThicknessMin(0f);
        Ixion.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
        Ixion.getSpec().setIconColor(new Color(200, 100, 80, 255));
        Ixion.getSpec().setTilt(19.6f);
        Ixion.getSpec().setPitch(30f);
        Ixion.getSpec().setRotation(calc.getRot(.517f));
        Ixion.applySpecChanges();

        calc.addConditions(Ixion.getMarket(), new String[] {
            "very_cold",
            "dark",
            "low_gravity",
            "no_atmosphere",
            "volatiles_diffuse",
            "ruins_scattered"
        });

        Ixion.setSkipForJumpPointAutoGen(true);

        DistanceConditionManager.track(Ixion.getMarket());
        Ixion.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 29.730f);
        Ixion.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 49.019f);

        // Huya System (38628
        float p_HuyaBinary = calc.getTime(3.2f);

        SectorEntityToken huyaAnchor = calc.spawnSPSObject(system, star, "huya_barycenter", "Huya Barycenter", "custom_entity", "empty", 1f, 39.2133f, 0.2729f, 169.312f, 67.505f, 2014.96f, zeroDegGlobal, null, 1f);
        float sz_Huya = calc.getSize(406f);
        float sz_HuyaMoon = calc.getSize(200f);
        float[] huyaOffsets = calc.getBinaryOffsetsReal(406f, 200f, 8.0f);

        // --- Huya (Primary) ---
        calc.spawnMoon(system, huyaAnchor, "Huya", sz_Huya, huyaOffsets[0], p_HuyaBinary, 0, showMinorNames);

        // --- Huya II (Moon) ---
        calc.spawnMoon(system, huyaAnchor, "Huya II", sz_HuyaMoon, huyaOffsets[1], p_HuyaBinary, 180f, showProvisionalNames);

        // --- Complex Plutino Binaries ---
        // === Spawn phase: both branches must produce Lempo, Hiisi, Paha ===
        PlanetAPI Lempo;
        PlanetAPI Hiisi;
        SectorEntityToken Paha;
        float funMult = 3.14159f;

        if (occultOrbitBeta) {
            // this is three body and its a problem

            float sz_Lempo = calc.getSize(272f);
            float sz_Hiisi = calc.getSize(251f);
            float sz_Paha  = calc.getSize(132f);

            
            float p_Inner = calc.getTime(1.9f) / funMult;
            float p_Outer = calc.getTime(50f)  / funMult;

            double mL   = Math.pow(sz_Lempo, 3);
            double mH   = Math.pow(sz_Hiisi, 3);
            double mP   = Math.pow(sz_Paha,  3);
            double mLH  = mL + mH;
            double mAll = mLH + mP;

            float outer_sma = 292f;
            float outer_e   = 0.29f;
            float outer_lp  = 180f;
            float a_IB      = outer_sma * (float)(mP / mAll);

            float inner_sma = 80f;
            float inner_e   = 0.15f;
            float inner_lp  = 0f;
            float a_L       = inner_sma * (float)(mH / mLH);

            // Lempo -> barycenter -> barycenter -> Sol
            float[][] lempoExtras = new float[][] {
                { a_IB, outer_e, outer_lp, p_Outer, 0f, +1f },
                { a_L,  inner_e, inner_lp, p_Inner, 0f, +1f }
            };
            Lempo = (PlanetAPI) calc.spawnSPSObject7(system, star, "Lempo", "Lempo", "rocky_ice", null, 272f, 39.7192f, 0.2298f, 97.167f, 295.825f, 2015.73f, zeroDegGlobal, null, 1f, null, null, lempoExtras, star, "Sol", false, false);         
            // Hiisi -> barycenter -> lempo
            Hiisi = (PlanetAPI) system.addPlanet("Hiisi", Lempo, "Hiisi", "rocky_ice",
                180f, sz_Hiisi, inner_sma, p_Inner);
            CompoundOrbitTool.attach(Hiisi, Lempo, new KeplerComponent[] {
                CompoundOrbitTool.kc(inner_sma, inner_e, inner_lp, p_Inner, 0f, -1f)
            });

            // Paha -> barycenter -> barycenter -> lempo
            float pahaSpin = 100f / sz_Paha;
            Paha = SolAsteroidFactory.createAsteroid(system, sz_Paha, "Paha", "Paha", showMinorNames);
            Paha.setCircularOrbit(Lempo, 0f, outer_sma, p_Outer);
            CompoundOrbitTool.attachWithSpin(Paha, Lempo, new KeplerComponent[] {
                CompoundOrbitTool.kc(outer_sma, outer_e, outer_lp, p_Outer, 0f, -1f),
                CompoundOrbitTool.kc(a_L,       inner_e, inner_lp, p_Inner, 0f, -1f)
            }, pahaSpin, pahaSpin + 10f);

        } else {
                
            float sz_Lempo = calc.getSize(272f);
            float sz_Hiisi = calc.getSize(251f);
            float sz_Paha  = calc.getSize(132f);
            float p_LempoHiisi = calc.getTime(1.9f) / funMult;
            float p_LempoPaha  = calc.getTime(50f)  / funMult;

            // Barycenter-based fallback (existing code)
            SectorEntityToken lempoBarycenter = calc.spawnSPSObject(system, star,
                "lempo_barycenter", "Lempo Barycenter", "custom_entity", "empty",
                1f, 39.7192f, 0.2298f, 97.167f, 295.825f, 2015.73f, zeroDegGlobal, null, 1f);

            SectorEntityToken[] lempoOuter = calc.spawnEllipticalBinary(system, lempoBarycenter,
                "lempo_hiisi_barycenter", null, sz_Lempo, "custom_entity", "empty",
                "Paha", "Paha", sz_Paha, "asteroid", null,
                292f, 0.29f, p_LempoPaha, 180f);

            SectorEntityToken lempoInnerBarycenter = lempoOuter[0];
            Paha = lempoOuter[1];

            SectorEntityToken[] lempoInner = calc.spawnEllipticalBinary(system, lempoInnerBarycenter,
                "Lempo", "Lempo", sz_Lempo, "planet", "rocky_ice",
                "Hiisi", "Hiisi", sz_Hiisi, "planet", "rocky_ice",
                80f, 0.15f, p_LempoHiisi, 0f);

            Lempo = (PlanetAPI) lempoInner[0];
            Hiisi = (PlanetAPI) lempoInner[1];
        }

        float rot_LempoHiisi = (-360f / (calc.getTime(1.9f / funMult) * 10f));

        // === Shared post-spawn: descriptions, visuals, conditions ===
        Lempo.setCustomDescriptionId("sol_lempo");
        Hiisi.setCustomDescriptionId("sol_hiisi");
        Paha.setCustomDescriptionId("sol_paha");

        // --- Lempo Visuals ---
        Lempo.getSpec().setTexture("graphics/planets/lempo_tx.jpg");
        Lempo.getSpec().setAtmosphereThickness(0f);
        Lempo.getSpec().setAtmosphereThicknessMin(0f);
        Lempo.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
        Lempo.getSpec().setIconColor(new Color(200, 80, 60, 255));
        Lempo.getSpec().setTilt(8.4f);
        Lempo.getSpec().setPitch(90f);
        Lempo.getSpec().setRotation(-rot_LempoHiisi);
        Lempo.applySpecChanges();

        calc.addConditions(Lempo.getMarket(), new String[] {
            "very_cold", 
            "dark", 
            "low_gravity", 
            "no_atmosphere",
            "volatiles_plentiful", 
            "ore_sparse", 
            "sol_chaotic_rotator",
            "ruins_widespread"
        });

        Lempo.setSkipForJumpPointAutoGen(true);

        // --- Hiisi Visuals ---
        Hiisi.getSpec().setTexture("graphics/planets/hiisi_tx.jpg");
        Hiisi.getSpec().setAtmosphereThickness(0f);
        Hiisi.getSpec().setAtmosphereThicknessMin(0f);
        Hiisi.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
        Hiisi.getSpec().setIconColor(new Color(180, 70, 50, 255));
        Hiisi.getSpec().setTilt(8.4f);
        Hiisi.getSpec().setPitch(90f);
        Hiisi.getSpec().setRotation(-rot_LempoHiisi);
        Hiisi.applySpecChanges();

        calc.addConditions(Hiisi.getMarket(), new String[] {
            "very_cold", 
            "dark", 
            "low_gravity",
            "no_atmosphere",
            "volatiles_plentiful", 
            "ore_sparse",
            "sol_chaotic_rotator",
            "ruins_scattered"
        });

        Hiisi.setSkipForJumpPointAutoGen(true);

        DistanceConditionManager.track(Lempo.getMarket());
        Lempo.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 30.542f);
        Lempo.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 48.397f);

        DistanceConditionManager.track(Hiisi.getMarket());
        Hiisi.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 30.542f);
        Hiisi.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 48.397f);

        DistanceConditionManager.track(Paha.getMarket());
        Paha.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 30.542f);
        Paha.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 48.397f);

        // --- Other Plutinos ---

        // Achlys
        float sz_Achlys = calc.getSize(772f);
        PlanetAPI Achlys = (PlanetAPI) calc.spawnSPSObject7(system, falseMoons ? Pluto : star, "Achlys", "Achlys", "frozen", null, 772f, 39.6275f, 0.1748f, 251.873f, 14.402f, 2107.10f, zeroDegGlobal, 0.283f, 1f, null, null, null, star, "Sol", false, falseMoons);

        Achlys.getSpec().setTexture("graphics/planets/achlys_tx.jpg"); 
        Achlys.getSpec().setAtmosphereThickness(0f); 
        Achlys.getSpec().setAtmosphereThicknessMin(0f); 
        Achlys.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Achlys.getSpec().setIconColor(new Color(150, 140, 130, 255)); 
        Achlys.getSpec().setTilt(15f); 
        Achlys.getSpec().setPitch(50.5f); 
        Achlys.getSpec().setRotation(calc.getRot(0.283f)); 
        Achlys.applySpecChanges();

        calc.addConditions(Achlys.getMarket(), new String[] {
            "very_cold",
            "dark",
            "low_gravity",
            "no_atmosphere",
            "volatiles_diffuse",
            "ore_moderate",
            "ruins_widespread",
            "sol_fast_rotator"
        });

        Achlys.setSkipForJumpPointAutoGen(true);

        calc.spawnMoon(system, Achlys, "Achlys II", calc.getSize(72f), sz_Achlys * 4f, calc.getTime(5f), 100f, showProvisionalNames);

        DistanceConditionManager.track(Achlys.getMarket());
        Achlys.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 32.403f);
        Achlys.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 46.536f);

        //2002 XV93 | tiny TNO with atmosphere
        SectorEntityToken XV93 = calc.spawnSPSObject(system, star, "2002 XV93", "2002 XV93", "asteroid", showNameProv, 549f, 39.5762f, 0.1261f, 19.0026f, 161.7632f, 2068.73f, zeroDegGlobal, null, 1f);

        if(!transNeptuneShortlist){
            // 2003 UZ413
            SectorEntityToken UZ413 = calc.spawnSPSObject(system, star, "UZ413", "2003 UZ413", "asteroid", showNameProv, 600f, 39.4274f, 0.2182f, 136.129f, 146.244f, 1941.99f, zeroDegGlobal, 0.172f, 1f);
            SectorEntityToken uz413Ship = DerelictThemeGenerator.addSalvageEntity(system, Entities.DERELICT_SURVEY_SHIP, Factions.DERELICT);
            uz413Ship.setCircularOrbitPointingDown(UZ413, 90, 40f, calc.getTime(5f));
            // 2010 JO179
            SectorEntityToken JO179 = calc.spawnSPSObject(system, star, "JO179", "2010 JO179", "asteroid", showNameProv, 700f, 77.9119f, 0.4980f, 147.079f, 9.324f, 1951.56f, zeroDegGlobal, 1.275f, 1f);
            SectorEntityToken jo179Ship = DerelictThemeGenerator.addSalvageEntity(system, Entities.DERELICT_SURVEY_SHIP, Factions.DERELICT);
            jo179Ship.setCircularOrbitPointingDown(JO179, 90, 40f, calc.getTime(5f));
            // 2003 VS2
            SectorEntityToken VS2 = calc.spawnSPSObject(system, star, "VS2", "2003 VS2", "asteroid", showNameProv, 590f, 39.7148f, 0.0816f, 302.781f, 115.153f, 2005.88f, zeroDegGlobal, 0.309f, 1f);
            // 2003 UR292
            SectorEntityToken UR292 = calc.spawnSPSObject(system, star, "UR292", "2003 UR292", "asteroid", showNameProv, 300f, 32.5814f, 0.1772f, 146.467f, 249.666f, 2012.99f, zeroDegGlobal, null, 1f);
            // 1996 TP66
            SectorEntityToken TP66 = calc.spawnSPSObject(system, star, "TP66", "1996 TP66", "asteroid", showNameProv, 160f, 39.7369f, 0.3345f, 316.824f, 76.174f, 2000.65f, zeroDegGlobal, 0.260f, 1f);
            // 2004 UX10
            SectorEntityToken UX10 = calc.spawnSPSObject(system, star, "UX10", "2004 UX10", "asteroid", showNameProv, 400f, 39.2262f, 0.0384f, 148.016f, 160.004f, 1956.68f, zeroDegGlobal, 0.316f, 1f);
            // 2002 VR128
            SectorEntityToken VR128 = calc.spawnSPSObject(system, star, "VR128", "2002 VR128", "asteroid", showNameProv, 448f, 39.6550f, 0.2618f, 23.0473f, 289.5801f, 1966.18f, zeroDegGlobal, null, 1f);
            // 2014 WQ509
            SectorEntityToken WQ509 = calc.spawnSPSObject(system, star, "WQ509", "2014 WQ509", "asteroid", showNameProv, 357f, 39.5545f, 0.1369f, 40.5725f, 207.1243f, 2109.69f, zeroDegGlobal, null, 1f);
            // 2003 QX111
            SectorEntityToken QX111 = calc.spawnSPSObject(system, star, "QX111", "2003 QX111", "asteroid", showNameProv, 434f, 39.4291f, 0.1296f, 157.5297f, 101.5083f, 1947.24f, zeroDegGlobal, null, 1f);
            // 2001 QF298
            SectorEntityToken QF298 = calc.spawnSPSObject(system, star, "QF298", "2001 QF298", "asteroid", showNameProv, 408f, 39.4830f, 0.1078f, 164.2084f, 41.1964f, 1909.45f, zeroDegGlobal, null, 1f);
            // 2001 XD255
            SectorEntityToken XD255 = calc.spawnSPSObject(system, star, "XD255", "2001 XD255", "asteroid", showNameProv, 346f, 39.5585f, 0.1097f, 106.6003f, 108.0538f, 2068.92f, zeroDegGlobal, null, 1f);
            // 2014 JR80
            SectorEntityToken JR80 = calc.spawnSPSObject(system, star, "JR80", "2014 JR80", "asteroid", showNameProv, 495f, 39.2495f, 0.0947f, 119.1789f, 300.4190f, 2096.34f, zeroDegGlobal, null, 1f);
            // 2007 JH43
            SectorEntityToken JH43 = calc.spawnSPSObject(system, star, "JH43", "2007 JH43", "asteroid", showNameProv, 531f, 39.2717f, 0.0325f, 64.6244f, 2.6784f, 2140.15f, zeroDegGlobal, null, 1f);
        }

        // =========================================================================
        // ========================= TWOTINOS (1:2) ================================
        // =========================================================================

        if(!transNeptuneShortlist){
            // 1998 SM165
            SectorEntityToken SM165 = calc.spawnSPSObject(system, star, "SM165", "1998 SM165", "asteroid", showNameProv, 280f, 47.9076f, 0.3699f, 183.115f, 131.842f, 1975.02f, zeroDegGlobal, 0.350f, 1f);
            float SM165pdiv = calc.distDToPeriodD(6f);
            calc.spawnWithEllipticalOrbit(system, SM165, "SM165b", "1998 SM165 B", "asteroid", showNameProv, calc.getSize(81f), calc.getSize(268f) * 85f / 6f, 0.4727f, 161.99f, calc.getTime(130.158f) / SM165pdiv, 280.5f, null);
            // 1996 TR66
            SectorEntityToken TR66 = calc.spawnSPSObject(system, star, "TR66", "1996 TR66", "asteroid", showNameProv, 200f, 47.9807f, 0.3957f, 343.113f, 309.938f, 1967.08f, zeroDegGlobal, 0.260f, 1f);
            // 2000 JG81
            SectorEntityToken JG81 = calc.spawnSPSObject(system, star, "JG81", "2000 JG81", "asteroid", showNameProv, 100f, 47.1789f, 0.2774f, 46.032f, 168.874f, 1996.22f, zeroDegGlobal, 0.233f, 1f);
            // 2007 JJ43
            SectorEntityToken JJ43 = calc.spawnSPSObject(system, star, "JJ43", "2007 JJ43", "asteroid", showNameProv, 610f, 47.7128f, 0.1549f, 272.503f, 8.377f, 2037.45f, zeroDegGlobal, 0.252f, 1f);
            // 2013 FZ27
            SectorEntityToken FZ27 = calc.spawnSPSObject(system, star, "FZ27", "2013 FZ27", "asteroid", showNameProv, 531f, 48.1007f, 0.2140f, 284.8821f, 342.0634f, 2091.34f, zeroDegGlobal, null, 1f);
            // 2002 WC19
            SectorEntityToken WC19 = calc.spawnSPSObject(system, star, "WC19", "2002 WC19", "asteroid", showNameProv, 338f, 48.2821f, 0.2662f, 109.7408f, 43.2479f, 2056.16f, zeroDegGlobal, null, 1f);
        }

        // =========================================================================
        // =================== HAUMEA FAMILY (COLLISIONAL) =========================
        // =========================================================================

        // Haumea
        PlanetAPI Haumea = (PlanetAPI) calc.spawnSPSObject(system, star, "Haumea", "Haumea", "frozen", null, 1632f, 43.0055f, 0.1958f, 121.797f, 240.888f, 2133.74f, zeroDegGlobal, null, 1f);
        Haumea.getSpec().setTexture("graphics/planets/haumea_tx.jpg"); 
        Haumea.getSpec().setAtmosphereThickness(0f); 
        Haumea.getSpec().setAtmosphereThicknessMin(0f); 
        Haumea.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Haumea.getSpec().setIconColor(new Color(240, 240, 255, 255)); 
        Haumea.getSpec().setTilt(28.2f); 
        Haumea.getSpec().setPitch(12f); 
        Haumea.getSpec().setRotation(calc.getRot(.163f)); 
        Haumea.applySpecChanges();

        calc.addConditions(Haumea.getMarket(), new String[] {
            "very_cold",
            "dark",
            "no_atmosphere",
            "volatiles_plentiful",
            "ore_moderate",
            "ruins_extensive",
            "sol_orbital_ring",
            "sol_fast_rotator",
            "sol_automated_habitats"
        });

        Haumea.setSkipForJumpPointAutoGen(true);

        // Haumea Rings
        // Real: ~2,287 km radius, ~70 km wide, ratio ~2.8x
        // Doubled to ~5.6x to avoid low-poly triangle rendering at small radii
        float sz_Haumea = Haumea.getRadius();
        float r_HaumeaRing = sz_Haumea * 5.6f;

        system.addRingBand(Haumea, "sol_rings", "rings_alpha1", 256, 3, new Color(200, 100, 100, 150), 20f, r_HaumeaRing, (progradeMult * 4));
        system.addRingBand(Haumea, "sol_rings", "rings_alpha1", 256, 2, new Color(200, 100, 100, 150), 10f, r_HaumeaRing + 10f, (progradeMult * 5));
        system.addTerrain(Terrain.RING, new RingSystemTerrainPlugin.RingParams(20f, r_HaumeaRing, Haumea, "Haumea Ring")).setCircularOrbit(Haumea, 0, 0, (progradeMult * 60));

        if(generateElevators){
            SectorEntityToken ringHaumea = system.addCustomEntity("ring1", "Haumea ring", "ring1", "neutral"); 
            ringHaumea.setCircularOrbitPointingDown(Haumea, 0f, 1, calc.getOrbRot(0.163f));
        }

        float dist_Namaka = sz_Haumea * 31.4f * 0.3f; 

        SectorEntityToken Namaka = calc.spawnWithEllipticalOrbit(system, Haumea, "namaka", "Namaka", "asteroid", showNameMinor, calc.getSize(170f), dist_Namaka, 0.25f, 126f, calc.getTime(18.2f), 306f, null);

        Namaka.setCustomDescriptionId("sol_namaka");

        // Hi'iaka
        PlanetAPI Hiiaka = system.addPlanet("Hiiaka", Haumea, "Hiiaka", "barren", 90, calc.getSize(310f), dist_Namaka + sz_Haumea * 5f, calc.getTime(49.5f));

        Hiiaka.getSpec().setTexture("graphics/planets/hiiaka_tx.jpg"); 
        Hiiaka.getSpec().setAtmosphereThickness(0f); 
        Hiiaka.getSpec().setAtmosphereThicknessMin(0f); 
        Hiiaka.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Hiiaka.getSpec().setIconColor(new Color(230, 230, 255, 255)); 
        Hiiaka.getSpec().setTilt(28.2f); 
        Hiiaka.getSpec().setPitch(90f); 
        Hiiaka.getSpec().setRotation(calc.getRot(.408f)); 
        Hiiaka.applySpecChanges();

        calc.addConditions(Hiiaka.getMarket(), new String[] {
            "very_cold",
            "low_gravity",
            "no_atmosphere",
            "dark",
            "ruins_scattered",
            "sol_porus"
        });

        Hiiaka.setSkipForJumpPointAutoGen(true);

        Hiiaka.setCustomDescriptionId("sol_hiiaka");

        DistanceConditionManager.track(Haumea.getMarket());
        Haumea.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 34.647f);
        Haumea.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 51.585f);

        DistanceConditionManager.track(Hiiaka.getMarket());
        Hiiaka.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 34.647f);
        Hiiaka.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 51.585f);

        // --- Haumea Family Asteroids (Orbiting Sun) ---
        // these are all fragments of when haumea was decked by some protoplanet ~4 billion years ago, I have no clue how scientists can tell, but pretty cool eh?
        // actually its shared long run averages of sma, eccentricity, and inclination combined with albedo and color
        // also it's very theoretical 

        if(!transNeptuneShortlist){
            // 1996 TO66
            SectorEntityToken TO66 = calc.spawnSPSObject(system, star, "TO66", "1996 TO66", "asteroid", showNameProv, 300f, 43.4614f, 0.1142f, 355.236f, 240.432f, 1907.73f, zeroDegGlobal, 0.260f, 1f);
            // 2002 TX300
            SectorEntityToken TX300 = calc.spawnSPSObject(system, star, "TX300", "2002 TX300", "asteroid", showNameProv, 286f, 43.4774f, 0.1213f, 324.675f, 342.670f, 1960.09f, zeroDegGlobal, 0.335f, 1f);
            // 2005 RR43
            SectorEntityToken RR43 = calc.spawnSPSObject(system, star, "RR43", "2005 RR43", "asteroid", showNameProv, 250f, 43.5190f, 0.1409f, 85.868f, 281.787f, 1983.02f, zeroDegGlobal, 0.328f, 1f);
            // 2003 OP32
            SectorEntityToken OP32 = calc.spawnSPSObject(system, star, "OP32", "2003 OP32", "asteroid", showNameProv, 230f, 43.1750f, 0.1034f, 183.013f, 68.761f, 1960.48f, zeroDegGlobal, 0.405f, 1f);
        }

        // =========================================================================
        // =================== HOT CLASSICAL CUBEWANOS =============================
        // =========================================================================

        // Makemake
        PlanetAPI Makemake = (PlanetAPI) calc.spawnSPSObject(system, star, "Makemake", "Makemake", "cryovolcanic", null, 1430f, 45.5107f, 0.1604f, 79.269f, 297.075f, 1881.48f, zeroDegGlobal, null, 1f);
        float sz_Makemake = calc.getSize(1430f);

        Makemake.getSpec().setTexture("graphics/planets/makemake_tx.jpg"); 
        Makemake.getSpec().setAtmosphereThickness(0f); 
        Makemake.getSpec().setAtmosphereThicknessMin(0f); 
        Makemake.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Makemake.getSpec().setIconColor(new Color(180, 100, 80, 255)); 
        Makemake.getSpec().setTilt(29f); 
        Makemake.getSpec().setPitch(35f); 
        Makemake.getSpec().setRotation(calc.getRot(.321f)); 
        Makemake.getSpec().setCloudColor(new Color(0, 0, 0, 0));
        Makemake.applySpecChanges();

        calc.addConditions(Makemake.getMarket(), new String[] {
            "very_cold",
            "thin_atmosphere",
            "volatiles_plentiful",
            "ore_moderate",
            "ruins_widespread",
            "sol_subsurface_ocean",
            "sol_degenerate",
            "sol_automated_habitats",
            "dark"
        });

        Makemake.setSkipForJumpPointAutoGen(true);

        Makemake.getMarket().getMemoryWithoutUpdate().set("$sol_polar_atmosphere_level", 2);

        float _makMoonExt = sz_Makemake * 8.0f; float _makMoonInt = _makMoonExt * 0.5f; float sz_Mk2 = calc.getSize(175f); float dist_Mk2 = sz_Makemake + (_makMoonExt * (float) Math.log(_makMoonInt * 0.015f + 1));

        SectorEntityToken MK2 = calc.spawnMoon(system, Makemake, "MK 2", sz_Mk2, dist_Mk2, calc.getTime(12f), 306f, showMinorNames);
        MK2.setCustomDescriptionId("sol_mk2");

        SectorEntityToken mk2Probe = DerelictThemeGenerator.addSalvageEntity(system, Entities.DERELICT_SURVEY_PROBE, Factions.DERELICT);
        mk2Probe.setCircularOrbitPointingDown(MK2, 90, 40f, calc.getTime(5f));

        // Makemake Dust Ring (Kiss et al. 2025)
        if(speculativeBodies){
            float r_MakemakeRing = sz_Makemake * 3.5f;
            system.addRingBand(Makemake, "sol_rings", "rings_alpha0", 256, 0, new Color(80, 70, 60, 90), 12f, r_MakemakeRing, calc.getTime(8f));
            system.addTerrain(Terrain.RING, new RingSystemTerrainPlugin.RingParams(12f, r_MakemakeRing, Makemake, "Makemake Dust Ring")).setCircularOrbit(Makemake, 0, 0, -100f);
        }

        DistanceConditionManager.track(Makemake.getMarket());
        Makemake.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 38.201f);
        Makemake.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 52.796f);

        // --- Quaoar System  ---
        PlanetAPI Quaoar = (PlanetAPI) calc.spawnSPSObject(system, star, "Quaoar", "Quaoar", "frozen", null, 1110f, 43.1477f, 0.0358f, 188.963f, 163.923f, 2079.83f, zeroDegGlobal, null, 1f);

        // Quaoar Rings
        float sz_Quaoar = Quaoar.getRadius();

        float r_Q1R = sz_Quaoar * 4.5f;
        float r_Q2R = sz_Quaoar * 7.4f;

        // Q1R (inner, denser)
        system.addRingBand(Quaoar, "sol_rings", "rings_alpha2", 256, 3, new Color(200, 100, 100, 150), 10f, r_Q1R, calc.getTime(10f));
        system.addTerrain(Terrain.RING, new RingSystemTerrainPlugin.RingParams(10f, r_Q1R, Quaoar, "Quaoar Ring")).setCircularOrbit(Quaoar, 0, 0, (progradeMult * 60));

        // Q2R (outer, diffuse OR MOON?!?!?!?!??!?!?!!?!!"???!?!?!?!!?!!?!?!!!?!?!?!?!?!?!?!)
        system.addRingBand(Quaoar, "sol_rings", "rings_alpha0", 256, 1, new Color(200, 100, 100, 120), 15f, r_Q2R, calc.getTime(10f));
        if(!speculativeBodies){
            system.addRingBand(Quaoar, "sol_rings", "rings_alpha1", 256, 2, new Color(200, 100, 100, 100), 5f, r_Q2R, calc.getTime(10f));
            system.addTerrain(Terrain.RING, new RingSystemTerrainPlugin.RingParams(15f, r_Q2R, Quaoar, "Quaoar III Remnants")).setCircularOrbit(Quaoar, 0, 0, (progradeMult * 60));
        }

        Quaoar.getSpec().setTexture("graphics/planets/quaoar_tx.jpg"); 
        Quaoar.getSpec().setAtmosphereThickness(0f); 
        Quaoar.getSpec().setAtmosphereThicknessMin(0f); 
        Quaoar.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Quaoar.getSpec().setIconColor(new Color(160, 140, 130, 255)); 
        Quaoar.getSpec().setTilt(8f); Quaoar.getSpec().setPitch(77f); 
        Quaoar.getSpec().setRotation(calc.getRot(.7375f)); 
        Quaoar.applySpecChanges();

        calc.addConditions(Quaoar.getMarket(), new String[] {
            "very_cold",
            "volatiles_trace",
            "ore_rich",
            "rare_ore_sparse",
            "ruins_scattered",
            "dark",
            "thin_atmosphere"
        });

        Quaoar.setSkipForJumpPointAutoGen(true);

        // Weywot
        SectorEntityToken Weywot = calc.spawnMoon(system, Quaoar, "Weywot", calc.getSize(80f), sz_Quaoar * 12f, calc.getTime(12.4f), 180f, showMinorNames);
        Weywot.setCustomDescriptionId("sol_weywot");

        if(speculativeBodies){
            // S/2025 (50000) 1 | 2:7 Mean-Motion Resonance with Weywot UNCONFIRMED
            calc.spawnMoon(system, Quaoar, "Quaoar III", calc.getSize(20f), sz_Quaoar * 5.21f, calc.getTime(12.4f * (2f / 7f)), 0f, showProvisionalNames);
        }

        DistanceConditionManager.track(Quaoar.getMarket());
        Quaoar.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 41.900f);
        Quaoar.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 45.488f);

        // Varda
        float p_VardaIlmare = calc.getTime(5.75f);
        float sz_Varda = calc.getSize(740f); 
        float sz_Ilmare = calc.getSize(360f); 
        float[] vardaOffsets = calc.getBinaryOffsetsReal(740f, 360f, 6.0f);

        PlanetAPI Varda = (PlanetAPI) calc.spawnSPSObject3(system, star, "Varda", "Varda", "frozen", null, 740f, 45.5381f, 0.1430f, 184.121f, 184.974f, 2097.11f, zeroDegGlobal, 0.246f, 1f, null, null, true, vardaOffsets[0], 0f, p_VardaIlmare);

        Varda.getSpec().setTexture("graphics/planets/varda_tx.jpg"); 
        Varda.getSpec().setAtmosphereThickness(0f); 
        Varda.getSpec().setAtmosphereThicknessMin(0f); 
        Varda.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Varda.getSpec().setIconColor(new Color(180, 90, 70, 255)); 
        Varda.getSpec().setTilt(21.5f); 
        Varda.getSpec().setPitch(90f); 
        Varda.getSpec().setRotation(calc.getRot(.246f)); 
        Varda.applySpecChanges();

        calc.addConditions(Varda.getMarket(), new String[] {
            "very_cold",
            "low_gravity",
            "no_atmosphere",
            "volatiles_trace",
            "ore_moderate",
            "ruins_scattered",
            "dark",
            "sol_fast_rotator"
        });

        Varda.setSkipForJumpPointAutoGen(true);

        // Ilmare — orbits Varda
        PlanetAPI Ilmare = system.addPlanet("Ilmare", Varda, "Ilmare", "rocky_ice", 180, sz_Ilmare, vardaOffsets[0] + vardaOffsets[1], p_VardaIlmare);

        Ilmare.getSpec().setTexture("graphics/planets/ilmare_tx.jpg"); 
        Ilmare.getSpec().setAtmosphereThickness(0f); 
        Ilmare.getSpec().setAtmosphereThicknessMin(0f); 
        Ilmare.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Ilmare.getSpec().setIconColor(new Color(160, 80, 60, 255)); 
        Ilmare.getSpec().setTilt(21.5f); 
        Ilmare.getSpec().setPitch(90f);
        Ilmare.getSpec().setRotation(-360f / (p_VardaIlmare * 10f));
        Ilmare.applySpecChanges(); 

        calc.addConditions(Ilmare.getMarket(), new String[] {
            "very_cold",
            "low_gravity",
            "no_atmosphere",
            "volatiles_trace",
            "dark",
            "sol_porus",
            "sol_tidal_lock",
            "ruins_scattered"
        });

        Ilmare.setSkipForJumpPointAutoGen(true);

        DistanceConditionManager.track(Varda.getMarket());
        Varda.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 39.510f);
        Varda.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 52.711f);

        DistanceConditionManager.track(Ilmare.getMarket());
        Ilmare.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 39.510f);
        Ilmare.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 52.711f);

        // Varuna
        PlanetAPI Varuna = (PlanetAPI) calc.spawnSPSObject7(system, falseMoons ? Varda : star, "Varuna", "Varuna", "rocky_ice", null, 670f, 43.1782f, 0.0525f, 97.210f, 273.221f, 1935.23f, zeroDegGlobal, null, 1f, null, null, null, star, "Sol", false, falseMoons);

        Varuna.getSpec().setTexture("graphics/planets/varuna_tx.jpg"); 
        Varuna.getSpec().setAtmosphereThickness(0f); 
        Varuna.getSpec().setAtmosphereThicknessMin(0f); 
        Varuna.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Varuna.getSpec().setIconColor(new Color(190, 90, 70, 255)); 
        Varuna.getSpec().setTilt(17.2f); 
        Varuna.getSpec().setPitch(62f); 
        Varuna.getSpec().setRotation(calc.getRot(.132f)); 
        Varuna.applySpecChanges();

        calc.addConditions(Varuna.getMarket(), new String[] {
            "very_cold",
            "low_gravity",
            "no_atmosphere",
            "volatiles_trace",
            "ore_moderate",
            "ruins_scattered",
            "dark",
            "sol_fast_rotator",
            "sol_ancient_drug_lab"
        });

        Varuna.setSkipForJumpPointAutoGen(true);

        if(speculativeBodies){
        // S/2019 (20000) 1 | Photometric indication only (Fernández-Valenzuela et al. 2019)
        calc.spawnMoon(system, Varuna, "Varuna II", calc.getSize(40f), calc.getSize(670f) * 3f, calc.getTime(0.499f), 135f, showProvisionalNames);
        }

        DistanceConditionManager.track(Varuna.getMarket());
        Varuna.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 40.827f);
        Varuna.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 45.448f);

        // --- Salacia System ---
        float p_SalaciaBinary = calc.getTime(5.5f); 
        float rot_SalaciaBinary = 360f / (p_SalaciaBinary * 10f); 

        float sz_Salacia = calc.getSize(850f); 
        float sz_Actea = calc.getSize(290f); 
        float[] salaciaOffsets = calc.getBinaryOffsetsReal(850f, 290f, 8.0f);
        // Some schizo shit about if Actea dual peaked, then mabye possible mabye it is somehow a binary despite the super strong resonance that would pump that E 

        float[][] salaciaExtras = new float[][]{
            { salaciaOffsets[0], 0f, 0f, p_SalaciaBinary, 0f, +1f }  // binary wobble
        };
        PlanetAPI Salacia = (PlanetAPI) calc.spawnSPSObject7(system, falseMoons ? Varda : star, "Salacia", "Salacia", "frozen2", null, 850f, 42.1147f, 0.1034f, 280.263f, 309.478f, 1924.56f, zeroDegGlobal, null, 1f, null, null, salaciaExtras, star, "Sol", false, falseMoons);

        Salacia.getSpec().setTexture("graphics/planets/salacia_tx.jpg"); 
        Salacia.getSpec().setAtmosphereThickness(0f); 
        Salacia.getSpec().setAtmosphereThicknessMin(0f); 
        Salacia.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
        Salacia.getSpec().setIconColor(new Color(100, 90, 80, 255)); 
        Salacia.getSpec().setTilt(23.9f); 
        Salacia.getSpec().setPitch(60f); 
        Salacia.getSpec().setRotation(-rot_SalaciaBinary); 
        Salacia.applySpecChanges();

        calc.addConditions(Salacia.getMarket(), new String[] {
            "very_cold",
            "dark",
            "low_gravity",
            "no_atmosphere",
            "volatiles_trace",
            "ore_moderate",
            "ruins_widespread",
            "sol_inter_binary_elevator"
        });

        Salacia.setSkipForJumpPointAutoGen(true);

        if(generateElevators){
        SectorEntityToken ElevatorSA = calc.spawnTransBinaryElevator(system, Salacia, "ElevatorSA", "Salacia-Actea Elevator", salaciaOffsets[0] + salaciaOffsets[1], 180f, p_SalaciaBinary);
        }

        // Actea — orbits Salacia
        calc.spawnMoon(system, Salacia, "Actea", sz_Actea, salaciaOffsets[0] + salaciaOffsets[1], p_SalaciaBinary, 180f, showMinorNames);

        DistanceConditionManager.track(Salacia.getMarket());
        Salacia.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 37.697f);
        Salacia.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 46.670f);

        // --- Other Hot Classical Planets ---

        // Aya (55565 2002 AW197)
        PlanetAPI Aya = (PlanetAPI) calc.spawnSPSObject7(system, falseMoons ? Makemake : star, "Aya", "Aya", "frozen", null, 770f, 47.2981f, 0.1277f, 297.374f, 294.587f, 2077.21f, zeroDegGlobal, 0.244f, 1f, null, null, null, star, "Sol", false, falseMoons);

        Aya.getSpec().setTexture("graphics/planets/aya_tx.jpg"); 
        Aya.getSpec().setAtmosphereThickness(0f); 
        Aya.getSpec().setAtmosphereThicknessMin(0f); 
        Aya.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Aya.getSpec().setIconColor(new Color(170, 150, 140, 255)); 
        Aya.getSpec().setTilt(15f); 
        Aya.getSpec().setPitch(50.6f);
        Aya.getSpec().setRotation(calc.getRot(0.244f)); 
        Aya.applySpecChanges();

        calc.addConditions(Aya.getMarket(), new String[] {
            "very_cold",
            "dark",
            "low_gravity",
            "no_atmosphere",
            "volatiles_trace",
            "ore_moderate",
            "sol_ancient_drug_lab",
            "sol_fast_rotator"
        });

        Aya.setSkipForJumpPointAutoGen(true);

        if(!isSettled){
        Aya.getMarket().addCondition("ruins_scattered");
        }

        DistanceConditionManager.track(Aya.getMarket());
        Aya.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 41.112f);
        Aya.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 47.196f);

        // Máni
        PlanetAPI Mani = (PlanetAPI) calc.spawnSPSObject7(system, falseMoons ? Quaoar : star, "Mani", "Mani", "frozen", null, 800f, 41.5953f, 0.1487f, 216.186f, 215.216f, 2122.86f, zeroDegGlobal, null, 1f, null, null, null, star, "Sol", false, falseMoons);

        Mani.getSpec().setTexture("graphics/planets/mani_tx.jpg"); 
        Mani.getSpec().setPlanetColor(new Color(230, 220, 210, 255)); 
        Mani.getSpec().setAtmosphereThickness(0f); 
        Mani.getSpec().setAtmosphereThicknessMin(0f); 
        Mani.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Mani.getSpec().setIconColor(new Color(160, 150, 140, 255)); 
        Mani.getSpec().setTilt(25f); 
        Mani.getSpec().setPitch(47.3f);
        Mani.getSpec().setRotation(calc.getRot(calc.getRandomRotationPeriod(800f))); 
        Mani.applySpecChanges();

        calc.addConditions(Mani.getMarket(), new String[] {
            "very_cold",
            "low_gravity",
            "no_atmosphere",
            "ore_rich",
            "ruins_scattered",
            "dark",
            "tectonic_activity"
        });

        Mani.setSkipForJumpPointAutoGen(true);

        // "Mani II" — f*ckin 1 in 100 chance, a chord was too long, now this is here
        if(speculativeBodiesExtreme){
            calc.spawnMoon(system, Mani, "Mani II", calc.getSize(40f), calc.getSize(800f) * 6f, calc.getTime(8f), 135f, showProvisionalNames);
        }

        DistanceConditionManager.track(Mani.getMarket());
        Mani.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 35.677f);
        Mani.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 47.801f);

        // Officially this is named after the Greek primeval state of existence, and not the chaos gods. Do you buy that? This, for sure, made it past IAU because of an embedded Slanesh cult.
        // Chaos
        SectorEntityToken ChaosStation = calc.spawnSPSObject7(system, falseMoons ? Makemake : star, "Chaos", "Chaos", "custom_entity", "Chaos", 1f, 46.1089f, 0.1105f, 49.910f, 56.606f, 2034.25f, zeroDegGlobal, 1.0f, 1f, null, null, null, star, "Sol", false, falseMoons);
        if (!isSettled) {
            ChaosStation.setInteractionImage("illustrations", "abandoned_station2");
            Misc.setAbandonedStationMarket("marketChaosPirate", ChaosStation);

            ChaosStation.getMarket().setName("Chaos");
            ChaosStation.getMarket().addCondition("abandoned_station");
            ChaosStation.getMarket().addCondition("very_cold");
            ChaosStation.getMarket().addCondition("dark");
            ChaosStation.getMarket().addCondition("low_gravity");
            ChaosStation.getMarket().addCondition("no_atmosphere");
            ChaosStation.getMarket().addCondition("ore_moderate");
            ChaosStation.getMarket().addCondition("rare_ore_sparse");
            ChaosStation.getMarket().addCondition("volatiles_plentiful");
            ChaosStation.getMarket().addCondition("sol_contact_binary");
            ChaosStation.getMarket().addCondition("sol_dist_abyssal");

            for (MarketConditionAPI condition : ChaosStation.getMarket().getConditions()) {
                condition.setSurveyed(true);
            }
        }

        ChaosStation.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 35.677f);
        ChaosStation.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 47.801f);

        // Goibniu
        SectorEntityToken Goibniu = calc.spawnSPSObject(system, star, "Goibniu", "Goibniu", "asteroid", showNameMinor, 730f, 41.8086f, 0.0760f, 250.576f, 289.667f, 1987.36f, zeroDegGlobal, 0.450f, 1f);
        SectorEntityToken goibnuStation = DerelictThemeGenerator.addSalvageEntity(system, Entities.DERELICT_SURVEY_SHIP, Factions.DERELICT); 
        goibnuStation.setCircularOrbitPointingDown(Goibniu, 90, 200f, calc.getTime(20f));

        // Ritona
        SectorEntityToken Ritona = calc.spawnSPSObject(system, star, "Ritona", "Ritona", "asteroid", showNameMinor, 640f, 41.5534f, 0.0239f, 187.003f, 178.794f, 2034.86f, zeroDegGlobal, 0.290f, 1f);

        // Uni-Tinia (2002 UX25) | as/rp=14.3 | es=0.17 | Ps=8.309 d
        SectorEntityToken uniBarycenter = calc.spawnSPSObject(system, star, "uni_barycenter", "Uni Barycenter", "custom_entity", "empty", 1f, 42.9742f, 0.1464f, 204.594f, 275.638f, 2065.11f, zeroDegGlobal, 0.367f, 1f);

        float sz_Uni = calc.getSize(640f);
        float sz_Tinia = calc.getSize(260f);
        float p_uniTinia = calc.getTime(8.309f);

        SectorEntityToken[] uniBinary = calc.spawnEllipticalBinary(system, uniBarycenter, "Uni", "Uni", sz_Uni, "asteroid", showNameMinor, "Tinia", "Tinia", sz_Tinia, "asteroid", showNameMinor, sz_Uni * 14.3f, 0.17f, p_uniTinia, 0f);

        if(!transNeptuneShortlist){
            // --- Hot Classical Asteroids ---
            // 2003 BH91
            SectorEntityToken BH91 = calc.spawnSPSObject(system, star, "BH91", "2003 BH91", "asteroid", showNameProv, 210f, 43.9674f, 0.0321f, 80.523f, 131.825f, 2003.09f, zeroDegGlobal, null, 1f);
            // 2002 KW14 (55636)
            SectorEntityToken KW14 = calc.spawnSPSObject(system, star, "KW14", "2002 KW14", "asteroid", showNameProv, 470f, 46.4742f, 0.2013f, 59.897f, 121.592f, 1972.27f, zeroDegGlobal, null, 1f);
            // 2005 UQ513 (202421)
            SectorEntityToken UQ513 = calc.spawnSPSObject(system, star, "UQ513", "2005 UQ513", "asteroid", showNameProv, 500f, 43.5299f, 0.1452f, 307.893f, 219.572f, 2122.61f, zeroDegGlobal, 0.487f, 1f);
            // 2017 OF201
            SectorEntityToken OF201 = calc.spawnSPSObject(system, star, "OF201", "2017 OF201", "asteroid", showNameProv, 360f, 844.9715f, 0.9465f, 328.721f, 338.004f, 1930.79f, zeroDegGlobal, null, 1f);
            // 2010 KZ39
            SectorEntityToken KZ39 = calc.spawnSPSObject(system, star, "KZ39", "2010 KZ39", "asteroid", showNameProv, 600f, 44.9889f, 0.0549f, 53.249f, 323.597f, 2112.45f, zeroDegGlobal, null, 1f);
            // 2002 GJ32
            SectorEntityToken GJ32 = calc.spawnSPSObject(system, star, "GJ32", "2002 GJ32", "asteroid", showNameProv, 416f, 44.0689f, 0.1111f, 244.8386f, 252.1317f, 1947.21f, zeroDegGlobal, null, 1f);
            // 2004 NT33
            SectorEntityToken NT33 = calc.spawnSPSObject(system, star, "NT33", "2004 NT33", "asteroid", showNameProv, 423f, 43.4153f, 0.1485f, 241.1462f, 38.1762f, 1984.96f, zeroDegGlobal, 0.328f, 1f);
            // 2004 PT107
            SectorEntityToken PT107 = calc.spawnSPSObject(system, star, "PT107", "2004 PT107", "asteroid", showNameProv, 400f, 40.5842f, 0.0583f, 320.9851f, 22.1436f, 2022.75f, zeroDegGlobal, null, 1f);
            // 2007 XV50
            SectorEntityToken XV50 = calc.spawnSPSObject(system, star, "XV50", "2007 XV50", "asteroid", showNameProv, 563f, 46.2678f, 0.0638f, 53.5152f, 279.4061f, 1933.18f, zeroDegGlobal, null, 1f);
            // 2011 JF31
            SectorEntityToken JF31 = calc.spawnSPSObject(system, star, "JF31", "2011 JF31", "asteroid", showNameProv, 402f, 41.3112f, 0.1355f, 236.8182f, 250.6074f, 1950.38f, zeroDegGlobal, null, 1f);
            // 2013 XC26
            SectorEntityToken XC26 = calc.spawnSPSObject(system, star, "XC26", "2013 XC26", "asteroid", showNameProv, 604f, 42.6282f, 0.2302f, 43.2129f, 145.9203f, 2054.21f, zeroDegGlobal, null, 1f);
            // 2014 UM33
            SectorEntityToken UM33 = calc.spawnSPSObject(system, star, "UM33", "2014 UM33", "asteroid", showNameProv, 491f, 43.2957f, 0.1565f, 236.6316f, 266.0587f, 2085.84f, zeroDegGlobal, null, 1f);
            // 2014 YZ49
            SectorEntityToken YZ49 = calc.spawnSPSObject(system, star, "YZ49", "2014 YZ49", "asteroid", showNameProv, 416f, 40.1950f, 0.0180f, 130.8685f, 38.4111f, 2010.20f, zeroDegGlobal, null, 1f);
            // 2015 FG345
            SectorEntityToken FG345 = calc.spawnSPSObject(system, star, "FG345", "2015 FG345", "asteroid", showNameProv, 404f, 42.0322f, 0.1244f, 58.5912f, 224.3335f, 2066.88f, zeroDegGlobal, null, 1f);
            // 2003 UY413
            SectorEntityToken UY413 = calc.spawnSPSObject(system, star, "UY413", "2003 UY413", "asteroid", showNameProv, 399f, 47.5081f, 0.2094f, 341.3171f, 346.4055f, 1978.02f, zeroDegGlobal, null, 1f);
            // 2010 VK201
            SectorEntityToken VK201 = calc.spawnSPSObject(system, star, "VK201", "2010 VK201", "asteroid", showNameProv, 521f, 43.4542f, 0.1061f, 156.5173f, 87.9525f, 2168.97f, zeroDegGlobal, null, 1f);
            // 2014 BV64
            SectorEntityToken BV64 = calc.spawnSPSObject(system, star, "BV64", "2014 BV64", "asteroid", showNameProv, 543f, 45.8579f, 0.1415f, 279.0342f, 346.6794f, 2101.77f, zeroDegGlobal, null, 1f);
            // 2014 FT71
            SectorEntityToken FT71 = calc.spawnSPSObject(system, star, "FT71", "2014 FT71", "asteroid", showNameProv, 500f, 43.4290f, 0.1421f, 346.3823f, 352.7454f, 2119.07f, zeroDegGlobal, null, 1f);
            // 2014 QA442
            SectorEntityToken QA442 = calc.spawnSPSObject(system, star, "QA442", "2014 QA442", "asteroid", showNameProv, 439f, 43.3486f, 0.1868f, 109.5213f, 282.2553f, 2030.50f, zeroDegGlobal, null, 1f);
            // 2014 SH349
            SectorEntityToken SH349 = calc.spawnSPSObject(system, star, "SH349", "2014 SH349", "asteroid", showNameProv, 414f, 43.4573f, 0.1483f, 202.5470f, 98.8287f, 1941.89f, zeroDegGlobal, null, 1f);
            // 2014 VU37
            SectorEntityToken VU37 = calc.spawnSPSObject(system, star, "VU37", "2014 VU37", "asteroid", showNameProv, 491f, 40.9675f, 0.0402f, 248.1382f, 332.8928f, 2127.85f, zeroDegGlobal, null, 1f);
            // 2014 WH509
            SectorEntityToken WH509 = calc.spawnSPSObject(system, star, "WH509", "2014 WH509", "asteroid", showNameProv, 456f, 44.4033f, 0.1804f, 76.2787f, 323.5922f, 1964.46f, zeroDegGlobal, null, 1f);
            // 2014 XS40
            SectorEntityToken XS40 = calc.spawnSPSObject(system, star, "XS40", "2014 XS40", "asteroid", showNameProv, 419f, 42.7150f, 0.1715f, 186.5624f, 263.0466f, 1994.38f, zeroDegGlobal, null, 1f);
            // 2013 HV156
            SectorEntityToken HV156 = calc.spawnSPSObject(system, star, "HV156", "2013 HV156", "asteroid", showNameProv, 421f, 47.1372f, 0.1762f, 168.6144f, 185.4471f, 2094.14f, zeroDegGlobal, null, 1f);
            // 2014 HZ199
            SectorEntityToken HZ199 = calc.spawnSPSObject(system, star, "HZ199", "2014 HZ199", "asteroid", showNameProv, 482f, 43.0193f, 0.1572f, 57.0621f, 82.8915f, 1966.99f, zeroDegGlobal, null, 1f);
            // 2014 BZ57
            SectorEntityToken BZ57 = calc.spawnSPSObject(system, star, "BZ57", "2014 BZ57", "asteroid", showNameProv, 488f, 42.9470f, 0.1417f, 274.3059f, 263.3337f, 2035.21f, zeroDegGlobal, null, 1f);
            // 2014 CO23
            SectorEntityToken CO23 = calc.spawnSPSObject(system, star, "CO23", "2014 CO23", "asteroid", showNameProv, 412f, 43.9112f, 0.1776f, 20.3394f, 50.1064f, 1946.16f, zeroDegGlobal, null, 1f);
            // 2015 BZ518
            SectorEntityToken BZ518 = calc.spawnSPSObject(system, star, "BZ518", "2015 BZ518", "asteroid", showNameProv, 538f, 46.7247f, 0.1844f, 44.0142f, 300.4225f, 2100.87f, zeroDegGlobal, null, 1f);
            // 2014 FY71
            SectorEntityToken FY71 = calc.spawnSPSObject(system, star, "FY71", "2014 FY71", "asteroid", showNameProv, 406f, 43.4544f, 0.2090f, 241.7554f, 242.4471f, 1978.32f, zeroDegGlobal, null, 1f);
            // 2014 LO28
            SectorEntityToken LO28 = calc.spawnSPSObject(system, star, "LO28", "2014 LO28", "asteroid", showNameProv, 427f, 42.9690f, 0.1206f, 287.3285f, 107.8620f, 2059.53f, zeroDegGlobal, null, 1f);
            // 2015 AJ281
            SectorEntityToken AJ281 = calc.spawnSPSObject(system, star, "AJ281", "2015 AJ281", "asteroid", showNameProv, 507f, 43.2922f, 0.1261f, 255.8150f, 6.9018f, 2079.25f, zeroDegGlobal, null, 1f);
            // 2012 VR128
            SectorEntityToken VR128 = calc.spawnSPSObject(system, star, "VR128", "2012 VR128", "asteroid", showNameProv, 556f, 41.5629f, 0.1437f, 283.7968f, 273.7946f, 2135.22f, zeroDegGlobal, null, 1f);
            // 2003 QW90
            SectorEntityToken QW90 = calc.spawnSPSObject(system, star, "QW90", "2003 QW90", "asteroid", showNameProv, 401f, 43.9716f, 0.0797f, 17.7689f, 84.7164f, 2080.39f, zeroDegGlobal, null, 1f);
            // 2014 DN143
            SectorEntityToken DN143 = calc.spawnSPSObject(system, star, "DN143", "2014 DN143", "asteroid", showNameProv, 419f, 47.1127f, 0.0653f, 303.4731f, 319.1308f, 2088.53f, zeroDegGlobal, null, 1f);
            // 2014 QW441
            SectorEntityToken QW441 = calc.spawnSPSObject(system, star, "QW441", "2014 QW441", "asteroid", showNameProv, 449f, 44.4456f, 0.1056f, 162.7558f, 205.5166f, 2021.64f, zeroDegGlobal, null, 1f);
            // 2014 AM55
            SectorEntityToken AM55 = calc.spawnSPSObject(system, star, "AM55", "2014 AM55", "asteroid", showNameProv, 435f, 47.3008f, 0.1367f, 131.7959f, 110.6217f, 2099.79f, zeroDegGlobal, null, 1f);
            // 2014 WP509
            SectorEntityToken WP509 = calc.spawnSPSObject(system, star, "WP509", "2014 WP509", "asteroid", showNameProv, 612f, 45.2884f, 0.0931f, 66.6591f, 340.4374f, 1979.43f, zeroDegGlobal, null, 1f);
        } // Consider: balancing the scales, the scattered disk may be cooler but cubewanos are the bread and butter

        // =========================================================================
        // COLD CLASSICAL CUBEWANOS 
        // =========================================================================

        // Arrokoth
        SectorEntityToken arrokothBarycenter = calc.spawnSPSObject(system, star, "arrokoth", "Arrokoth", "custom_entity", "Arrokoth" + showNameCustom, 1f, 44.0615f, 0.0363f, 159.062f, 189.384f, 2066.64f, zeroDegGlobal, 0.664f, 1f);

        // Sila-Nunam (79360)
        float sz_Sila = calc.getSize(249f);
        float sz_Nunam = calc.getSize(236f);
        float[] silaOffsets = calc.getBinaryOffsetsReal(249f, 236f, 11.0f);        
        float p_SilaNunam = calc.getTime(12.8f);

        float[][] silaExtras = new float[][]{
            { silaOffsets[0], 0f,  0f, p_SilaNunam,  0f, +1f } 
        };

        SectorEntityToken Sila = calc.spawnSPSObject5(system, star, "Sila", "Sila", "asteroid", showNameMinor, 249f, 44.0366f, 0.0141f, 304.256f, 214.867f, 2032.81f, zeroDegGlobal, null, 1f, null, null, silaExtras, star);

        calc.spawnMoon(system, Sila, "Nunam", sz_Nunam, silaOffsets[1]+silaOffsets[0], p_SilaNunam, 180f, showMinorNames);

        SectorEntityToken silaHabitat = DerelictThemeGenerator.addSalvageEntity(system, Entities.ORBITAL_HABITAT_REMNANT, Factions.DERELICT); silaHabitat.setId("sila_habitat"); 
        silaHabitat.setCircularOrbitPointingDown(Sila, 0, 1, p_SilaNunam);

        // Borasisi-Pabu (66652)

        SectorEntityToken borasisiBarycenter = calc.spawnSPSObject(system, star, "borasisi_barycenter", "Borasisi-Pabu Barycenter", "custom_entity", "empty", 1f, 43.7912f, 0.0849f, 84.646f, 198.962f, 1970.54f, zeroDegGlobal, null, 1f);
        float sz_Borasisi = calc.getSize(126f); float sz_Pabu = calc.getSize(103f);

        float borasisiDistDiv = 2f;
        float periodborasisiRaw = 46.2f / calc.distDToPeriodD(borasisiDistDiv); 

        SectorEntityToken[] borasisiBinary = calc.spawnEllipticalBinary(system, borasisiBarycenter,"borasisi", "Borasisi", sz_Borasisi, "asteroid", showNameMinor,"pabu", "Pabu", sz_Pabu, "asteroid", showNameMinor, sz_Borasisi * 27f / borasisiDistDiv, 0.47f, calc.getTime(periodborasisiRaw), 0f);

        SectorEntityToken Borasisi = borasisiBinary[0];
        SectorEntityToken Pabu = borasisiBinary[1];

        // Altjira (148780)
        // (Nelsen et al. 2025, Planetary Science Journal)

        SectorEntityToken AltjiraBarycenter = calc.spawnSPSObject(system, star, "altjira_barycenter", "Altjira System Barycenter", "custom_entity", "empty", 1f, 44.5412f, 0.0568f, 1.885f, 303.852f, 1918.49f, zeroDegGlobal, null, 1f);

        float sz_AltjiraI   = calc.getSize(160f);
        float sz_AltjiraII  = calc.getSize(140f);
        float sz_AltjiraIII = calc.getSize(130f); 

        float altjiraOuterPeriod = calc.getTime(140f);  
        float altjiraInnerPeriod = calc.getTime(8f);    // fuckifanyoneknows?

        if(speculativeBodies){
            float sz_AltjiraInnerPair = calc.getSize(160f); // proxy for combined inner mass in binary offset calc
            SectorEntityToken[] altjiraOuter = calc.spawnEllipticalBinary(system, AltjiraBarycenter,
                "altjira_inner_barycenter", null, sz_AltjiraInnerPair, "custom_entity", "empty",
                "altjira_b", "Altjira II", sz_AltjiraII, "asteroid", showNameProv,
                sz_AltjiraI * 10f, 0.34f, altjiraOuterPeriod, 0f);

            SectorEntityToken altjiraInnerBarycenter = altjiraOuter[0];
            SectorEntityToken AltjiraII = altjiraOuter[1];

            calc.spawnMoon(system, altjiraInnerBarycenter, "Altjira",   sz_AltjiraI,   sz_AltjiraI * 1.5f, altjiraInnerPeriod, 0f, showMinorNames);
            calc.spawnMoon(system, altjiraInnerBarycenter, "Altjira III", sz_AltjiraIII, sz_AltjiraI * 1.5f, altjiraInnerPeriod, 180f, showProvisionalNames);
        } else {
            SectorEntityToken[] altjiraBinary = calc.spawnEllipticalBinary(system, AltjiraBarycenter,
                "altjira_a", "Altjira",  sz_AltjiraI,  "asteroid", showNameMinor,
                "altjira_b", "Altjira II", sz_AltjiraII, "asteroid", showNameProv,
                sz_AltjiraI * 10f, 0.34f, altjiraOuterPeriod, 0f);
        }

        // Albion 1996 QB1
        SectorEntityToken Albion = calc.spawnSPSObject(system, star, "Albion", "Albion", "asteroid", showNameMinor, 160f, 44.1992f, 0.0725f, 359.473f, 6.894f, 1997.27f, zeroDegGlobal, 12.188f, 1f);
        Albion.setCustomDescriptionId("sol_albion");


        if(!transNeptuneShortlist){
            // Thirouin, Noll, Grundy, Sheppard, Escarzaga, Donnelly (2025, PSJ):
            // Four ish bodies
            // Logos binary
            // Zoe contact binary
            float logosDistDiv = 6f;
            SectorEntityToken LogosBarycenter = calc.spawnSPSObject(system, star,
                "logos_barycenter", "Logos System Barycenter", "custom_entity", "empty",
                1f, 39.68f, 0.12275f, 132.51f, 336.06f, 1965.88f, zeroDegGlobal, null, 1f);

            float sz_LogosAa = calc.getSize(80f);
            float sz_LogosAb = calc.getSize(67f); 
            float sz_LogosCombined = calc.getSize(77f);
            float sz_Zoe   = calc.getSize(66f);

            float periodLogosRaw = 309.87f / calc.distDToPeriodD(logosDistDiv);
            float periodLogosInner = calc.getTime(0.5f);

            if (speculativeBodies) {
                SectorEntityToken[] logosOuter = calc.spawnEllipticalBinary(system, LogosBarycenter,
                    "logos_inner_barycenter", null, sz_LogosCombined, "custom_entity", "empty",
                    "zoe", "Zoe", sz_Zoe, "custom_entity", "Zoe" + showNameMinor,
                    sz_LogosCombined * 120f / logosDistDiv, 0.5463f, calc.getTime(periodLogosRaw), 0f);

                SectorEntityToken logosInnerBarycenter = logosOuter[0];
                SectorEntityToken Zoe = logosOuter[1];

                calc.spawnMoon(system, logosInnerBarycenter, "Logos",    sz_LogosAa, sz_LogosAa * 1.5f, periodLogosInner, 0f,   showMinorNames);
                calc.spawnMoon(system, logosInnerBarycenter, "Logos Beta", sz_LogosAb, sz_LogosAa * 1.5f, periodLogosInner, 180f, showProvisionalNames);
            } else {
                SectorEntityToken[] logosBinary = calc.spawnEllipticalBinary(system, LogosBarycenter,
                    "logos", "Logos", sz_LogosCombined, "asteroid",      showNameMinor,
                    "zoe",   "Zoe",   sz_Zoe,           "custom_entity", "Zoe" + showNameMinor,
                    sz_LogosCombined * 120f / logosDistDiv, 0.5463f, calc.getTime(periodLogosRaw), 0f);

                SectorEntityToken Logos = logosBinary[0];
                SectorEntityToken Zoe   = logosBinary[1];
            }
        }

        // =========================================================================
        // ==================== RESONANT SDOs ======================================
        // =========================================================================

        if(!transNeptuneShortlist){
            // 2002 TC302
            SectorEntityToken TC302 = calc.spawnSPSObject(system, star, "TC302", "2002 TC302", "asteroid", showNameProv, 584f, 55.8370f, 0.2995f, 23.825f, 86.070f, 2058.66f, zeroDegGlobal, 2.34f, 1f);
            // 2015 RR245 <- Likely dwarf planet (mad disrespect)
            SectorEntityToken RR245 = calc.spawnSPSObject(system, star, "RR245", "2015 RR245", "asteroid", showNameProv, 670f, 82.6916f, 0.5896f, 211.808f, 260.425f, 2092.47f, zeroDegGlobal, 0.348f, 1f);
            if(speculativeBodies){
                // 2015 RR245 II | all orbital params unknown — estimated
                calc.spawnMoon(system, RR245, "2015 RR245 II", calc.getSize(334f), calc.getSize(608f) * 8f, calc.getTime(30f), 180f, showProvisionalNames);
            }
            // 2001 UR163
            SectorEntityToken UR163 = calc.spawnSPSObject(system, star, "UR163", "2001 UR163", "asteroid", showNameProv, 530f, 51.7665f, 0.2803f, 302.396f, 343.466f, 1937.11f, zeroDegGlobal, null, 1f);
            // 2014 SR349
            SectorEntityToken SR349 = calc.spawnSPSObject(system, star, "SR349", "2014 SR349", "asteroid", showNameProv, 500f, 307.7661f, 0.8460f, 34.948f, 341.615f, 2053.71f, zeroDegGlobal, null, 1f);
            // I forgor why these got derelict survey ships :\ It's not size cuz rr245 and tz302 are left hurting if that was the case.
            // 2010 RF4
            SectorEntityToken RF43 = calc.spawnSPSObject(system, star, "RF43", "2010 RF43", "asteroid", showNameProv, 580f, 49.3015f, 0.2444f, 25.376f, 191.432f, 1924.11f, zeroDegGlobal, null, 1f);
            SectorEntityToken rf43Ship = DerelictThemeGenerator.addSalvageEntity(system, Entities.DERELICT_SURVEY_SHIP, Factions.DERELICT);
            rf43Ship.setCircularOrbitPointingDown(RF43, 90, 40f, calc.getTime(5f));
            // 2014 EZ51
            SectorEntityToken EZ51 = calc.spawnSPSObject(system, star, "EZ51", "2014 EZ51", "asteroid", showNameProv, 615f, 51.8674f, 0.2282f, 27.605f, 332.634f, 2118.35f, zeroDegGlobal, 0.133f, 1f);
            SectorEntityToken ez51Ship = DerelictThemeGenerator.addSalvageEntity(system, Entities.DERELICT_SURVEY_SHIP, Factions.DERELICT);
            ez51Ship.setCircularOrbitPointingDown(EZ51, 90, 40f, calc.getTime(5f));
            // 2020 VN40 | highest resonance ratio in Sol (1:10)
            SectorEntityToken VN40 = calc.spawnSPSObject(system, star, "VN40", "2020 VN40", "asteroid", showNameProv, 90f, 144.4991f, 0.7351f, 197.278f, 262.721f, 2045.90f, zeroDegGlobal, null, 1f);
        }

        // =========================================================================
        // ====================== SCATTERED DISK (SDOs) ============================
        // =========================================================================

        // Eris & Dysnomia Sizes
        float sz_Eris = calc.getSize(2326f); 
        float sz_Dysnomia = calc.getSize(700f);

        float p_ErisDysnomiaRaw = 15.785f / calc.distDToPeriodD(2f);
        float p_ErisDysnomia = calc.getTime(15.7f);
        float[] erisOffsets = calc.getBinaryOffsetsReal(2326f, 700f, 16f / 2f);

        // Eris
        // Allegedly dysonomia is super low density, as it apparently does not make Eris wobble around a barycenter, which I'm ignoring.
        PlanetAPI Eris = (PlanetAPI) calc.spawnSPSObject3(system, star, "sol_Eris", "Eris", "frozen", null, 2326f, 67.9964f, 0.4370f, 36.027f, 150.732f, 2257.27f, zeroDegGlobal, null, 1f, null, null, true, erisOffsets[0], 0f, p_ErisDysnomia);

        // Eris Exception: Retaining atmosphere 
        Eris.getSpec().setTexture("graphics/planets/eris_tx.jpg"); 
        Eris.getSpec().setAtmosphereThickness(0.05f); 
        Eris.getSpec().setAtmosphereThicknessMin(10f); 
        Eris.getSpec().setAtmosphereColor(new Color(200, 220, 255, 50)); 
        Eris.getSpec().setIconColor(new Color(240, 240, 255, 255)); 
        Eris.getSpec().setTilt(44f); 
        Eris.getSpec().setPitch(90f); 
        Eris.getSpec().setRotation(-(360/(p_ErisDysnomia *10)));
        Eris.applySpecChanges();

        calc.addConditions(Eris.getMarket(), new String[] {
            "very_cold",
            "ore_abundant",
            "rare_ore_abundant",
            "ruins_widespread",
            "volatiles_plentiful",
            "dark",
            "sol_insurgent_network",
            "sol_inter_binary_elevator",
            "sol_degenerate",
            "sol_subsurface_ocean",
            "sol_automated_habitats"
        });

        Eris.setSkipForJumpPointAutoGen(true);

        // Dysnomia — orbits Eris directly
        // Giant NEA - low albedo, rubble pile
        PlanetAPI Dysnomia = system.addPlanet("Dysnomia", Eris, "Dysnomia", "rocky_ice", 180, sz_Dysnomia, erisOffsets[0] + erisOffsets[1], p_ErisDysnomia);

        Dysnomia.getSpec().setTexture("graphics/planets/dysnomia_tx.jpg");
        Dysnomia.getSpec().setAtmosphereThickness(0f); 
        Dysnomia.getSpec().setAtmosphereThicknessMin(0f); 
        Dysnomia.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Dysnomia.getSpec().setIconColor(new Color(200, 200, 210, 255)); 
        Dysnomia.getSpec().setTilt(44f); 
        Dysnomia.getSpec().setPitch(90f); 
        Dysnomia.getSpec().setRotation(-(360/(p_ErisDysnomia *10))); 
        Dysnomia.applySpecChanges();

        calc.addConditions(Dysnomia.getMarket(), new String[] {
            "very_cold",
            "low_gravity",
            "no_atmosphere",
            "ore_sparse",
            "rare_ore_sparse",
            "volatiles_diffuse",
            "dark",
            "ruins_scattered",
            "sol_inter_binary_elevator",
            "sol_degenerate"
        });

        Dysnomia.setSkipForJumpPointAutoGen(true);

        // Oops, ED stands for Erectile Dysfunction not Eris-Dysnomia.
        if(generateElevators){
            SectorEntityToken elevatorED = calc.spawnTransBinaryElevator(system, Eris, "ElevatorED", "Eris-Dysnomia Elevator", erisOffsets[0] + erisOffsets[1], 180f, p_ErisDysnomia);
        }

        // "Eris III" — hypothetical inner resonant companion
        // Eris-Dysonmia has some eccentricity, a close in strong resonance companion could pump that E, have to be really close to be unobservable.
        // This is some BS. 
        // Luna has an E of .05 but there isnt an Earth III
        if(speculativeBodiesExtreme){
            calc.spawnMoon(system, Eris, "Eris III", calc.getSize(15f), sz_Eris * 2.2f, p_ErisDysnomia * (2f/13f), 0f, showProvisionalNames);
        }

        DistanceConditionManager.track(Eris.getMarket());
        Eris.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 38.1f);
        Eris.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 97.49f);
        DistanceConditionManager.track(Dysnomia.getMarket());
        Dysnomia.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 38.1f);
        Dysnomia.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 97.49f);

        // Gonggong
        PlanetAPI Gonggong = (PlanetAPI) calc.spawnSPSObject7(system, falseMoons ? Eris : star, "Gonggong", "Gonggong", "frozen", null, 1230f, 66.8937f, 0.5032f, 336.840f, 206.642f, 1856.59f, zeroDegGlobal, null, 1f, null, null, null, star, "Sol", false, falseMoons);

        Gonggong.getSpec().setTexture("graphics/planets/gonggong_tx.jpg");
        Gonggong.getSpec().setAtmosphereThickness(0f);
        Gonggong.getSpec().setAtmosphereThicknessMin(0f);
        Gonggong.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
        Gonggong.getSpec().setIconColor(new Color(180, 60, 40, 255));
        Gonggong.getSpec().setTilt(30.7f);
        Gonggong.getSpec().setPitch(40f);
        Gonggong.getSpec().setRotation(calc.getRot(1.875f));
        Gonggong.applySpecChanges();

        calc.addConditions(Gonggong.getMarket(), new String[] {
            "very_cold",
            "dark",
            "low_gravity",
            "thin_atmosphere",
            "volatiles_diffuse",
            "ore_moderate",
            "rare_ore_sparse",
            "ruins_widespread",
            "sol_insurgent_network",
            "sol_unexploded_ordnance"
        });

        Gonggong.setSkipForJumpPointAutoGen(true);

        SectorEntityToken Xiangliu = calc.spawnWithEllipticalOrbit(system, Gonggong, "xiangliu", "Xiangliu", "asteroid", showNameMinor, calc.getSize(100f), calc.getSize(1230f) * 10.0f, 0.29f, 0f, calc.getTime(25f), 180f, calc.getTime(25f) / rotMult);
        Xiangliu.setCustomDescriptionId("sol_xiangliu");

        DistanceConditionManager.track(Gonggong.getMarket());
        Gonggong.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 33.235f);
        Gonggong.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 100.555f);

        // Allowing absolute value, possessive, and exclamation symbols to make it into a name is dumb, gonggong doesn't get to use chinese symbols, and greek bodies don't get greek symbols, so why wasn't Gǃkún||'hòmdímà more latinized?
        // Gkunhomdima (229762) 
        // LOL THE NAME WAS SO CRINGE JAVA REFUSED TO COMPILE 
        float sz_Gkun = calc.getSize(610f); float sz_GoeHu = calc.getSize(100f); 

        PlanetAPI Gkun = (PlanetAPI) calc.spawnSPSObject7(system, falseMoons ? Eris : star, "Gkunhomdima", "G'kun||'homdima", "frozen", null, 610f, 74.5870f, 0.4961f, 131.240f, 345.940f, 2046.02f, zeroDegGlobal, 0.458f, 1f, null, null, null, star, "Sol", false, falseMoons);

        Gkun.getSpec().setTexture("graphics/planets/gkun_tx.jpg"); 
        Gkun.getSpec().setAtmosphereThickness(0f); 
        Gkun.getSpec().setAtmosphereThicknessMin(0f); 
        Gkun.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Gkun.getSpec().setIconColor(new Color(180, 80, 60, 255)); 
        Gkun.getSpec().setTilt(20f); Gkun.getSpec().setPitch(90f); 
        Gkun.getSpec().setRotation(calc.getRot(.458f));
        Gkun.applySpecChanges(); 

        calc.addConditions(Gkun.getMarket(), new String[] {
            "very_cold",
            "dark",
            "low_gravity",
            "no_atmosphere",
            "volatiles_trace",
            "ore_sparse",
            "ruins_widespread",
            "sol_unpronounceable",
            "sol_automated_habitats"
        });

        Gkun.setSkipForJumpPointAutoGen(true);

        // Goehu
        SectorEntityToken Goehu = calc.spawnMoon(system, Gkun, "Goehu", sz_GoeHu, sz_Gkun * 4f, calc.getTime(11f), 180f, showMinorNames);

        // "Gǃkún||'hòmdímà III" — different paper, same thin existence basis as Eris III
        if(speculativeBodiesExtreme){
            calc.spawnMoon(system, Gkun, "Gǃkún||'hòmdímà III", calc.getSize(10f), sz_Gkun * 1.8f, calc.getTime(2f), 180f, showProvisionalNames);
        }

        DistanceConditionManager.track(Gkun.getMarket());
        Gkun.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 37.600f);
        Gkun.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 111.57f);

        // Chiminigagua (2002 MS4)
        float sz_fy27 = calc.getSize(742f); 
        float sz_fy27_moon = calc.getSize(190f); 

        PlanetAPI Chiminigagua = (PlanetAPI) calc.spawnSPSObject7(system, falseMoons ? Eris : star, "Chiminigagua", "Chiminigagua", "frozen", null, 742f, 58.8402f, 0.3918f, 187.031f, 139.457f, 2203.84f, zeroDegGlobal, 0.666f, 1f, null, null, null, star, "Sol", false, falseMoons);

        Chiminigagua.getSpec().setTexture("graphics/planets/chiminigagua_tx.jpg"); 
        Chiminigagua.getSpec().setPlanetColor(new Color(255, 240, 230, 255)); 
        Chiminigagua.getSpec().setAtmosphereThickness(0f); 
        Chiminigagua.getSpec().setAtmosphereThicknessMin(0f); 
        Chiminigagua.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Chiminigagua.getSpec().setIconColor(new Color(190, 90, 70, 255)); 
        Chiminigagua.getSpec().setTilt(19f); 
        Chiminigagua.getSpec().setPitch(30f); 
        Chiminigagua.getSpec().setRotation(calc.getRot(calc.getRandomRotationPeriod(742f))); 
        Chiminigagua.applySpecChanges();

        calc.addConditions(Chiminigagua.getMarket(), new String[] {
            "very_cold",
            "dark",
            "low_gravity",
            "no_atmosphere",
            "volatiles_plentiful",
            "ore_sparse",
            "ruins_scattered"
        });

        Chiminigagua.setSkipForJumpPointAutoGen(true);

        // Minigagua
        calc.spawnMoon(system, Chiminigagua, "Minigagua", sz_fy27_moon, sz_fy27 * 4f, calc.getTime(15f), 180f, showMinorNames);

        DistanceConditionManager.track(Chiminigagua.getMarket());
        Chiminigagua.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 35.572f);
        Chiminigagua.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 82.000f);

        // Dziewanna
        SectorEntityToken Dziewanna = calc.spawnSPSObject(system, star, "Dziewanna", "Dziewanna", "asteroid", showNameMinor, 470f, 68.7806f, 0.5275f, 346.157f, 284.791f, 2038.94f, zeroDegGlobal, 0.295f, 1f);
        SectorEntityToken dziewannaProbe = DerelictThemeGenerator.addSalvageEntity(system, Entities.DERELICT_SURVEY_PROBE, Factions.DERELICT); 

        dziewannaProbe.setCircularOrbitPointingDown(Dziewanna, 90, 40f, calc.getTime(5f));

        SectorEntityToken Rumina = calc.spawnSPSObject(system, star, "Rumina", "Rumina", "asteroid", showNameMinor, 644f, 92.2746f, 0.6190f, 84.630f, 318.731f, 2005.06f, zeroDegGlobal, null, 1f);

        if(!scatteredDiskShortlist){
            // 2003 UY117
            SectorEntityToken UY117 = calc.spawnSPSObject(system, star, "UY117", "2003 UY117", "asteroid", showNameProv, 170f, 56.1198f, 0.4202f, 265.250f, 113.035f, 2005.03f, zeroDegGlobal, 0.516f, 1f);
            // 1996 TL66
            SectorEntityToken TL66 = calc.spawnSPSObject(system, star, "TL66", "1996 TL66", "asteroid", showNameProv, 400f, 84.8933f, 0.5866f, 217.702f, 185.141f, 2001.68f, zeroDegGlobal, 0.500f, 1f);
            // 2021 DR15
            SectorEntityToken DR15 = calc.spawnSPSObject(system, star, "DR15", "2021 DR15", "asteroid", showNameProv, 700f, 67.6937f, 0.4305f, 334.151f, 22.780f, 1832.72f, zeroDegGlobal, null, 1f);
            // 2014 FC72
            SectorEntityToken FC72 = calc.spawnSPSObject(system, star, "FC72", "2014 FC72", "asteroid", showNameProv, 500f, 75.2044f, 0.3130f, 177.996f, 32.068f, 2023.48f, zeroDegGlobal, null, 1f);
            // 2010 RE64
            SectorEntityToken RE64 = calc.spawnSPSObject(system, star, "RE64", "2010 RE64", "asteroid", showNameProv, 430f, 66.2026f, 0.4511f, 67.586f, 20.177f, 2076.62f, zeroDegGlobal, null, 1f);
            // 2005 QU182
            SectorEntityToken QU182 = calc.spawnSPSObject(system, star, "QU182", "2005 QU182", "asteroid", showNameProv, 700f, 112.1695f, 0.6696f, 78.536f, 224.256f, 1971.57f, zeroDegGlobal, null, 1f);
            // 2021 LL37 <- likely largest solar system object w/o wikipedia page [disrespect = mad]
            SectorEntityToken LL37 = calc.spawnSPSObject(system, star, "LL37", "2021 LL37", "asteroid", showNameProv, 500f, 55.6420f, 0.3580f, 345.027f, 50.025f, 2193.34f, zeroDegGlobal, null, 1f);
        }

        // =========================================================================
        // EXTREME SCATTERED, EXTREME DETATCHED, SEDNOIDS, ETNOS, EXTREME DAMOCLOIDS
        // =========================================================================

        // Sedna
        PlanetAPI Sedna = (PlanetAPI) calc.spawnSPSObject(system, star, "Sedna", "Sedna", "frozen",  null, 1000f, 506f, 0.8496f, 144.478f, 311.009f, 2075.73f,  zeroDegGlobal, null, 1f);
        Sedna.getSpec().setTexture("graphics/planets/sedna_tx.jpg"); 
        Sedna.getSpec().setAtmosphereThickness(0f); 
        Sedna.getSpec().setAtmosphereThicknessMin(0f); 
        Sedna.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Sedna.getSpec().setIconColor(new Color(200, 80, 60, 255)); 
        Sedna.getSpec().setTilt(11f); 
        Sedna.getSpec().setPitch(30f); 
        Sedna.getSpec().setRotation(calc.getRot(.416f)); 
        Sedna.applySpecChanges();

        calc.addConditions(Sedna.getMarket(), new String[] {
            "very_cold",
            "dark",
            "volatiles_trace",
            "ore_abundant",
            "rare_ore_sparse",
            "ruins_scattered",
            "sol_insurgent_network"
        });
        Sedna.setCustomDescriptionId("sol_sedna");

        Sedna.setSkipForJumpPointAutoGen(true);

        DistanceConditionManager.track(Sedna.getMarket());
        Sedna.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 76.19f);
        Sedna.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 937f);

        // The Goblin | AKA lekuaksaorhuasifhbsjhvg4ryt | AKA Leleakuhonua (a and u are held constant) | AKA 541132 | AKA 2015 TG387
        PlanetAPI Goblin = (PlanetAPI) calc.spawnSPSObject(system, star, "Goblin", "The Goblin", "frozen", null, 220f, 1389.3517f, 0.94572f, 301.134f, 118.130f, 2078.58f, zeroDegGlobal, null, 1f);

        Goblin.getSpec().setTexture("graphics/planets/goblin_tx.jpg"); 
        Goblin.getSpec().setPlanetColor(new Color(255, 220, 200, 255));
        Goblin.getSpec().setAtmosphereThickness(0f); 
        Goblin.getSpec().setAtmosphereThicknessMin(0f); 
        Goblin.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Goblin.getSpec().setIconColor(new Color(180, 100, 80, 255)); 
        Goblin.getSpec().setTilt(0f); 
        Goblin.getSpec().setPitch(45f); 
        Goblin.getSpec().setRotation(calc.getRot(calc.getRandomRotationPeriod(220f))); 
        Goblin.applySpecChanges();

        calc.addConditions(Goblin.getMarket(), new String[] {
            "dark",
            "low_gravity",
            "organics_abundant",
            "inimical_biosphere",
            "sol_goblin_world",
        });

        Goblin.setSkipForJumpPointAutoGen(true);

        Goblin.getMarket().getMemoryWithoutUpdate().set("$sol_no_freeze", true);

        DistanceConditionManager.track(Goblin.getMarket());
        Goblin.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 64.7f);
        Goblin.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 2714f);

        // The IAU should lock in this name. To me at least, Biden is a diety.
        // Biden (2012 VP113)
        // Ironic humor trigger warning. You gotta be careful because in this polarized society there probably is someone who believes biden is one of the eponymous "old ones" from lovecraft's work.
        // 1000km cuz I feelz like it. you can't spell Assumed albedo without ASS
        PlanetAPI Biden = (PlanetAPI) calc.spawnSPSObject(system, star, "Biden", "Biden", "frozen", null, 1000f, 269.7334f, 0.7011f, 90.902f, 294.290f, 1979.89f, zeroDegGlobal, null, 1f);
        Biden.getSpec().setTexture("graphics/planets/biden_tx.jpg"); 
        Biden.getSpec().setAtmosphereThickness(0f); 
        Biden.getSpec().setAtmosphereThicknessMin(0f); 
        Biden.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Biden.getSpec().setIconColor(new Color(200, 100, 120, 255)); 
        Biden.getSpec().setTilt(24f); 
        Biden.getSpec().setPitch(45f); 
        Biden.getSpec().setRotation(calc.getRot(calc.getRandomRotationPeriod(1000f))); 
        Biden.applySpecChanges();

        calc.addConditions(Biden.getMarket(), new String[] {
            "low_gravity",
            "volatiles_diffuse",
            "ruins_scattered",
            "rare_ore_sparse",
            "ore_sparse",
            "very_cold",
            "dark",
            "sol_pre_domain_sapience", // This reflects my unironic political beliefs.\
            "thin_atmosphere"
        });

        Biden.setSkipForJumpPointAutoGen(true);

        Biden.setCustomDescriptionId("sol_biden");

        SectorEntityToken BidenBeacon = system.addCustomEntity(null, null, "warning_beacon", "neutral");
        BidenBeacon.setCircularOrbitPointingDown(Biden, 0, 200, 10);
        BidenBeacon.setCustomDescriptionId("sol_biden_beacon");

        DistanceConditionManager.track(Biden.getMarket());
        Biden.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 80.52f);
        Biden.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 444.1f);

        // Farout (2018 VG18)
        PlanetAPI Farout = (PlanetAPI) calc.spawnSPSObject(system, star, "Farout", "Farout", "frozen", null, 500f, 81.9689f, 0.5237f, 245.260f, 16.976f, 1694.35f, zeroDegGlobal, null, 1f);

        Farout.getSpec().setTexture("graphics/planets/farout_tx.jpg"); 
        Farout.getSpec().setAtmosphereThickness(0f); 
        Farout.getSpec().setAtmosphereThicknessMin(0f); 
        Farout.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Farout.getSpec().setIconColor(new Color(190, 100, 110, 255)); 
        Farout.getSpec().setTilt(0f); 
        Farout.getSpec().setPitch(30f); 
        Farout.getSpec().setRotation(calc.getRot(calc.getRandomRotationPeriod(500f))); 
        Farout.applySpecChanges();

        calc.addConditions(Farout.getMarket(), new String[] {
            "very_cold",
            "dark",
            "low_gravity",
            "no_atmosphere",
            "volatiles_trace",
            "ore_sparse",
            "ruins_scattered",
            "sol_organ_complex",
            "sol_degenerate",
            "sol_automated_habitats"
        });

        Farout.setSkipForJumpPointAutoGen(true);

        SectorEntityToken faroutStation = DerelictThemeGenerator.addSalvageEntity(system, Entities.STATION_RESEARCH_REMNANT, Factions.DERELICT);
        faroutStation.setCircularOrbitPointingDown(Farout, 90, 50f, calc.getTime(30f));

        DistanceConditionManager.track(Farout.getMarket());
        Farout.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 38.358f);
        Farout.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 124.897f);

        // Farfarout (2018 AG37)
        PlanetAPI Farfarout = (PlanetAPI) calc.spawnSPSObject(system, star, "Farfarout", "Farfarout", "rocky_ice", null, 400f, 80.1675f, 0.6554f, 68.357f, 231.855f, 2364.23f, zeroDegGlobal, null, 1f);

        Farfarout.getSpec().setTexture("graphics/planets/farfarout_tx.jpg"); 
        Farfarout.getSpec().setAtmosphereThickness(0f); 
        Farfarout.getSpec().setAtmosphereThicknessMin(0f); 
        Farfarout.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Farfarout.getSpec().setIconColor(new Color(180, 160, 160, 255)); 
        Farfarout.getSpec().setTilt(0f); 
        Farfarout.getSpec().setPitch(30f); 
        Farfarout.getSpec().setRotation(calc.getRot(calc.getRandomRotationPeriod(400f))); 
        Farfarout.applySpecChanges();

        calc.addConditions(Farfarout.getMarket(), new String[] {
            "very_cold",
            "dark",
            "low_gravity",
            "no_atmosphere",
            "volatiles_trace",
            "ore_sparse",
        });

        Farfarout.setSkipForJumpPointAutoGen(true);

        if(!isSettled){
        Farfarout.getMarket().addCondition("ruins_scattered");
        }

        DistanceConditionManager.track(Farfarout.getMarket());
        Farfarout.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 27.63f);
        Farfarout.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 132.7f);

        // DeeDee (2014 UZ224)
        SectorEntityToken DeeDee = calc.spawnSPSObject(system, star, "DeeDee", "DeeDee", "asteroid", showNameMinor, 635f, 109.8853f, 0.6486f, 131.183f, 28.607f, 2142.25f, zeroDegGlobal, null, 1f);
        DeeDee.setCustomDescriptionId("sol_deedee");

        // Buffy (2004 XR190)
        SectorEntityToken Buffy = calc.spawnSPSObject(system, star, "Buffy", "Buffy", "asteroid", showNameMinor, 500f, 57.8400f, 0.1086f, 252.317f, 280.494f, 2114.30f, zeroDegGlobal, null, 1f);

        SectorEntityToken buffyStation = DerelictThemeGenerator.addSalvageEntity(system, Entities.STATION_RESEARCH, Factions.DERELICT); 
        buffyStation.setCircularOrbitPointingDown(Buffy, 90, 50f, calc.getTime(30f));

        // Alicanto
        SectorEntityToken Alicanto = calc.spawnSPSObject(system, star, "Alicanto", "Alicanto", "asteroid", showNameMinor, 300f, 346.7752f, 0.8634f, 66.040f, 327.268f, 2009.85f, zeroDegGlobal, null, 1f);
        Alicanto.setCustomDescriptionId("sol_alicanto");

        // Cashew (2015 BP519)
        SectorEntityToken Cashew = calc.spawnSPSObject(system, star, "Cashew", "Cashew", "custom_entity", "Caju" + showNameMinor, 543f, 491.3315f, 0.9281f, 135.298f, 347.682f, 2058.63f, zeroDegGlobal, 2f, 1f);
        Cashew.setCustomDescriptionId("sol_cashew");

        // Ammonite (2023 KQ14)
        calc.spawnSPSObject(system, star, "Ammonite", "Ammonite", "asteroid", showNameMinor, 380f, 246.0777f, 0.7322f, 72.073f, 198.830f, 2063.660f, zeroDegGlobal, null, 1f);

        if(!scatteredDiskShortlist){
            float safeAdjustment = .01f;
            // 2021 RR205 
            SectorEntityToken RR205 = calc.spawnSPSObject(system, star, "RR205", "2021 RR205", "asteroid", showNameProv, 200f, 980.3652f, 0.9433f, 108.455f, 208.709f, 1991.84f, zeroDegGlobal, null, 1f);
            // 2014 FE72
            SectorEntityToken FE72 = calc.spawnSPSObject(system, star, "FE72", "2014 FE72", "asteroid", showNameProv, 250f, 2506.4550f, 0.9856f -.01f, 336.768f, 133.900f, 1965.90f, zeroDegGlobal, null, 1f);
            // 2010 GB174
            SectorEntityToken GB174 = calc.spawnSPSObject(system, star, "GB174", "2010 GB174", "asteroid", showNameProv, 220f, 356.7212f, 0.8642f, 130.590f, 347.268f, 1951.83f, zeroDegGlobal, null, 1f);
            // 2019 EU5 | 200 km 
            calc.spawnSPSObject(system, star, "2019 EU5", "2019 EU5", "asteroid", showNameProv, 200f, 1281.294f, 0.9633f, 109.140f, 108.911f, 2101.25f, zeroDegGlobal, null, 1f);
            // 2012 DR30 | <- consider: Long period comet?
            calc.spawnSPSObject(system, star, "2012 DR30", "2012 DR30", "asteroid", showNameProv, 180f, 944.778f, 0.9846f, 341.393f, 195.135f, 2011.19f, zeroDegGlobal, null, 1f);
            // 2015 KG163 | 150 km
            calc.spawnSPSObject(system, star, "2015 KG163", "2015 KG163", "asteroid", showNameProv, 150f, 628.601f, 0.9356f, 219.152f, 31.972f, 2022.43f, zeroDegGlobal, null, 1f);
            // 2015 RX245 | 250 km
            calc.spawnSPSObject(system, star, "2015 RX245", "2015 RX245", "asteroid", showNameProv, 250f, 453.893f, 0.8997f, 8.608f, 65.057f, 2065.78f, zeroDegGlobal, null, 1f);
            // 2013 RF98 | 120 km
            calc.spawnSPSObject(system, star, "2013 RF98", "2013 RF98", "asteroid", showNameProv, 120f, 382.658f, 0.9055f, 67.657f, 312.289f, 2010.07f, zeroDegGlobal, null, 1f);
            // 2010 VZ98
            SectorEntityToken VZ98 = calc.spawnSPSObject(system, star, "VZ98", "2010 VZ98", "asteroid", showNameProv, 500f, 159.8368f, 0.7851f, 117.442f, 313.736f, 2027.86f, zeroDegGlobal, null, 1f);
            calc.spawnSPSObject(system, star, "2013 BL76", "2013 BL76", "asteroid", showNameProv, 40f, 1261.9378f, 0.9933f - safeAdjustment, 180.183f, 166.318f, 2012.819f, zeroDegGlobal, null, 1f);
            // 2006 SQ372
            calc.spawnSPSObject(system, star, "2006 SQ372", "2006 SQ372", "asteroid", showNameProv, 140f, 839.2765f, 0.9711f, 197.375f, 122.647f, 2006.640f, zeroDegGlobal, null, 1f);
            // 2013 SY99
            calc.spawnSPSObject(system, star, "2013 SY99", "2013 SY99", "asteroid", showNameProv, 250f, 839.9317f, 0.9406f, 29.519f, 32.206f, 2054.973f, zeroDegGlobal, null, 1f);
            // 2021 DK18
            calc.spawnSPSObject(system, star, "2021 DK18", "2021 DK18", "asteroid", showNameProv, 170f, 836.2080f, 0.9467f, 322.273f, 234.370f, 2078.633f, zeroDegGlobal, null, 1f);
            // 2000 OO67
            calc.spawnSPSObject(system, star, "2000 OO67", "2000 OO67", "asteroid", showNameProv, 64f, 617.9451f, 0.9663f, 142.383f, 212.721f, 2005.351f, zeroDegGlobal, null, 1f);
            // 2007 TG422
            calc.spawnSPSObject(system, star, "2007 TG422", "2007 TG422", "asteroid", showNameProv, 222f, 567.7505f, 0.9373f, 112.959f, 285.923f, 2005.929f, zeroDegGlobal, null, 1f);
            // 2013 RA109 
            calc.spawnSPSObject(system, star, "2013 RA109", "2013 RA109", "asteroid", showNameProv, 200f, 491.3236f, 0.9062f, 104.901f, 263.143f, 2007.659f, zeroDegGlobal, null, 1f);
        }

        // =========================================================================
        // =================== FICTIONAL / EASTER EGGS =============================
        // =========================================================================

        if(fictionalTNOs){
            // Burns-Caulfield (Blindsight) | Fictional TNO | a=45.0, e=0.20
            SectorEntityToken BurnsCaulfield = calc.spawnSPSObject(system, star, "BurnsCaulfield", "Burns-Caulfield", "asteroid", showNameMinor, 130f, 45.00f, 0.200f, 14.50f, 160.0f, 2082.5f, zeroDegGlobal, 0.55f, 1f);
            BurnsCaulfield.setCustomDescriptionId("sol_burns");

            // Survey Probe
            // SectorEntityToken bcProbe = system.addCustomEntity("burns_probe", "Second wave survey", "Sol_probe", "neutral");
            // bcProbe.setCircularOrbitPointingDown(BurnsCaulfield, 90, 80f, calc.getTime(5f));
            // bcProbe.setDiscoverable(true);

            // 1999 ZX30 (Camelot 30K) | Fictional TNO | a=35.0, e=0.05
            SectorEntityToken ZX30 = calc.spawnSPSObject(system, star, "ZX30", "1999 ZX30", "asteroid", showNameProv, 70f, 80.00f, 0.050f, 2.10f, 120.0f, 2050.0f, zeroDegGlobal, 0.42f, 1f);
            ZX30.setCustomDescriptionId("sol_zx30");
        }

        // =========================================================================
        // =================== Speculative =========================================
        // =========================================================================

        if(planetNine){
            // Sol IX (Planet Nine) — Batygin & Brown 2016, refined Siraj/Chyba/Tremaine 2025
            // Michael Brown would never lie to me
            PlanetAPI SolIX = (PlanetAPI) calc.spawnSPSObject(system, star, "SolIX", "Sol IX", "ice_giant", null, 22000f, 290f, 0.29f, 100f, 150f, 2200f, zeroDegGlobal, null, 1f);

            SolIX.getSpec().setTexture("graphics/planets/planetix_tx.jpg");
            SolIX.getSpec().setPlanetColor(new Color(180, 200, 230, 255));
            SolIX.getSpec().setAtmosphereThickness(0.25f);
            SolIX.getSpec().setAtmosphereColor(new Color(140, 180, 230, 130));
            SolIX.getSpec().setCloudTexture("graphics/planets/clouds_banded01.png");
            SolIX.getSpec().setCloudColor(new Color(200, 220, 240, 90));
            SolIX.getSpec().setCloudRotation(calc.getRot(1.2f));
            SolIX.getSpec().setIconColor(new Color(100, 140, 220, 255));
            SolIX.getSpec().setTilt(20f);
            SolIX.getSpec().setPitch(70f);
            SolIX.getSpec().setRotation(calc.getRot(calc.getRandomRotationPeriod(22000f)));
            SolIX.applySpecChanges();

            calc.addConditions(SolIX.getMarket(), new String[] {
                "very_cold",
                "cold",
                "dark",
                "high_gravity",
                "dense_atmosphere",
                "volatiles_plentiful"
            });

            SolIX.setSkipForJumpPointAutoGen(true);

            SolIX.getMarket().getMemoryWithoutUpdate().set("$sol_no_freeze", true);

            DistanceConditionManager.track(SolIX.getMarket());
            SolIX.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 200f);
            SolIX.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 370f);

        }

        if(planetTen){
            // Sol X (Planet Y / updated Planet Ten) — Siraj, Chyba & Tremaine 2025, MNRAS Letters
            // Mercury mass TNO 80-200 AU
            // I wouldn't do something as pedestrian as only include a planet nine
            PlanetAPI SolX = (PlanetAPI) calc.spawnSPSObject(system, star, "SolX", "Sol X", "frozen", null, 2900f, 130f, 0.077f, 75f, 210f, 2090f, zeroDegGlobal, null, 1f);
            float sz_x = SolX.getRadius();

            SolX.getSpec().setTexture("graphics/planets/planetx_tx.jpg");
            SolX.getSpec().setAtmosphereThickness(.2f);
            SolX.getSpec().setAtmosphereThicknessMin(10f);
            SolX.getSpec().setTilt(15f);
            SolX.getSpec().setPitch(55f);
            SolX.getSpec().setRotation(calc.getRot(calc.getRandomRotationPeriod(2900f)));
            SolX.applySpecChanges();

            calc.addConditions(SolX.getMarket(), new String[] {
                "very_cold",
                "cold",
                "dark",
                "ore_sparse",
                "rare_ore_sparse",
                "volatiles_diffuse",
                "ruins_scattered",
                "sol_space_elevator"
            });

            SolX.setSkipForJumpPointAutoGen(true);

            if(generateElevators){
                SectorEntityToken xelevator = system.addCustomEntity("titan_elevator", "Titan elevator", "elevator3", "neutral");
                xelevator.setCircularOrbitPointingDown(SolX, 0, sz_x + 140, calc.getOrbRot(.8f));
            }

            DistanceConditionManager.track(SolX.getMarket());
            SolX.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 120f);
            SolX.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 140f);
        }

        float sz_SolXI;
        if(planetEleven){
            // Sol XI (Oort Cloud Planet) — Raymond, Izidoro & Kaib 2023, MNRAS Letters
            // Sims showed there is ~7% chance a giant is chilling deep into the oort.
            PlanetAPI SolXI = (PlanetAPI) calc.spawnSPSObject(system, star, "SolXI", "Sol XI", "ice_giant", null, 95000f, 5000f, 0.72f, 215f, 87f, 1450f, zeroDegGlobal, null, 1f);

            SolXI.getSpec().setPlanetColor(new Color(255, 25, 25, 255));
            SolXI.getSpec().setAtmosphereThickness(0.35f);
            SolXI.getSpec().setAtmosphereColor(new Color(255, 100, 100, 255));
            SolXI.getSpec().setCloudTexture("graphics/planets/clouds_banded01.png");
            SolXI.getSpec().setCloudColor(new Color(255, 100, 100, 0));
            SolXI.getSpec().setCloudRotation(calc.getRot(0.9f));
            SolXI.getSpec().setIconColor(new Color(150, 120, 190, 255));
            SolXI.getSpec().setTilt(64f);
            SolXI.getSpec().setPitch(110f);
            SolXI.getSpec().setRotation(calc.getRot(calc.getRandomRotationPeriod(95000f)));
            SolXI.applySpecChanges();

            calc.addConditions(SolXI.getMarket(), new String[] {
                "very_cold",
                "cold",
                "dark",
                "high_gravity",
                "dense_atmosphere",
                "volatiles_plentiful",
                "sol_goblin_world",
                "sol_degenerate",
                "sol_ai_terminators",
                "sol_ai_freedom_fighters",
                "inimical_biosphere"
            });

            SolXI.setSkipForJumpPointAutoGen(true);

            sz_SolXI = SolXI.getRadius();
            SectorEntityToken SolXIStation = DerelictThemeGenerator.addSalvageEntity(system, Entities.STATION_RESEARCH_REMNANT, Factions.DERELICT);
            SolXIStation.setCircularOrbitPointingDown(SolXI, 90, sz_SolXI + 100f, calc.getTime(5f));
            SolXIStation.setName("Isolation");

            SolXI.getMarket().getMemoryWithoutUpdate().set("$sol_no_freeze", true);

            DistanceConditionManager.track(SolXI.getMarket());
            SolXI.getMemoryWithoutUpdate().set("$sol_orbit_min_au", 1400f);
            SolXI.getMemoryWithoutUpdate().set("$sol_orbit_max_au", 8600f);

        }

        SectorEntityToken SolIX = system.getEntityById("SolIX");
        SectorEntityToken SolX = system.getEntityById("SolX");
        SectorEntityToken SolXI = system.getEntityById("SolXI");

        // =========================================================================
        // Heliopause 
        // =========================================================================
        // Boundary of the Heliosphere roughly 120 AU
        float heliopauseInner = calc.getDist(120f, star);
        float heliopauseOuter = calc.getDist(132.5f, star);

        SectorEntityToken solHeliopause = system.addTerrain(Terrain.MAGNETIC_FIELD, 
        new MagneticFieldTerrainPlugin.MagneticFieldParams( heliopauseOuter - heliopauseInner, heliopauseInner + ((heliopauseOuter - heliopauseInner) / 2f), star,heliopauseInner, heliopauseOuter, new Color(30, 20, 100, 30), 0.01f));
        solHeliopause.setCircularOrbit(star, 0, 0, -1000);
        ((CampaignTerrainAPI) solHeliopause).getPlugin().setTerrainName("Heliopause");

        if (Global.getSettings().getModManager().isModEnabled("IndEvo")) { 

            SectorEntityToken watchtowerDysnomia= system.addCustomEntity(null, null, "IndEvo_Watchtower", "remnant");
            watchtowerDysnomia.setCircularOrbitPointingDown(Eris, 0, erisOffsets[1] - erisOffsets[0], p_ErisDysnomia);
            watchtowerDysnomia.setDiscoverable(true); 
            watchtowerDysnomia.setSensorProfile(1000f);

            SectorEntityToken watchtowerNix= system.addCustomEntity(null, null, "IndEvo_Watchtower", "remnant");
            watchtowerNix.setCircularOrbitPointingDown(Nix, 50, 50, calc.getTime(5f));
            watchtowerNix.setDiscoverable(true); 
            watchtowerNix.setSensorProfile(1000f);

            SectorEntityToken watchtowerNamaka= system.addCustomEntity(null, null, "IndEvo_Watchtower", "remnant");
            watchtowerNamaka.setCircularOrbitPointingDown(Namaka, 50, 50, calc.getTime(5f));
            watchtowerNamaka.setDiscoverable(true); 
            watchtowerNamaka.setSensorProfile(1000f);

            SectorEntityToken watchtowerFarfarout= system.addCustomEntity(null, null, "IndEvo_Watchtower", "remnant");
            watchtowerFarfarout.setCircularOrbitPointingDown(Farfarout, 200, 200, calc.getTime(20f));
            watchtowerFarfarout.setDiscoverable(true); 
            watchtowerFarfarout.setSensorProfile(1000f);

            SectorEntityToken watchtowerDeeDee= system.addCustomEntity(null, null, "IndEvo_Watchtower", "remnant");
            watchtowerDeeDee.setCircularOrbitPointingDown(DeeDee, 200, 200, calc.getTime(20f));
            watchtowerDeeDee.setDiscoverable(true); 
            watchtowerDeeDee.setSensorProfile(1000f);
            
            SectorEntityToken tnoWMDs = system.addCustomEntity(null, null, "IndEvo_arsenalStation", "neutral");
            tnoWMDs.setCircularOrbitPointingDown(Chiminigagua, 50, 100, calc.getTime(10f));
            tnoWMDs.setDiscoverable(true); 
            tnoWMDs.setSensorProfile(2000f);

            Pluto.getMarket().addCondition("IndEvo_RuinsCondition");
            Eris.getMarket().addCondition("IndEvo_RuinsCondition");
            Haumea.getMarket().addCondition("IndEvo_RuinsCondition");
        }

    }
}