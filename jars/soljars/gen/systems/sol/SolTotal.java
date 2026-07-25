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
import soljars.gen.systems.sol.SolDeferredSetupScript;
import soljars.gen.utils.SolHyperspaceGen;
import soljars.gen.utils.RemnantPatrolFactory;
import soljars.gen.utils.AstroCalc;
import soljars.gen.systems.sol.GiantMoonsTotal;
import soljars.gen.systems.sol.CometsCentaursTNOs;
import soljars.gen.systems.sol.SolInnit;

import soljars.compat.widehorizons.LocationXY;
import soljars.compat.industrialevolution.ArtillerySpawnTool;


import com.fs.starfarer.api.campaign.CampaignTerrainPlugin;

public class SolTotal {

    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem("Sol");
        LocationAPI hyper = Global.getSector().getHyperspace();

        // Instantiate the Factory (Empty constructor now)
        RemnantNexusFactory nexusFactory = new RemnantNexusFactory();

        // Instantiate the Factory
        RemnantPatrolFactory patrolFactory = new RemnantPatrolFactory();

        // 3. INITIALIZE & CLEANUP
        AstroCalc calc = new AstroCalc();


        // =========================================================================
        // ============================ LET THERE BE LIGHT =========================
        // =========================================================================
        // Suun
        float zeroDegGlobal = 146f;

        float solRad = calc.getSize(1392700f);

        SectorEntityToken star = system.initStar("Sol", "star_yellow", solRad,  600f,  10f, 0.5f, 3f); 
        system.setLightColor(new Color(255, 245, 230)); 

        system.addTag(Tags.THEME_INTERESTING);
        system.addTag(Tags.THEME_UNSAFE);
        system.addTag(Tags.THEME_REMNANT);
        system.addTag(Tags.THEME_REMNANT_MAIN);
        system.addTag(Tags.THEME_REMNANT_RESURGENT);

        // =========================================================================
        // ========  On the Second Day, God Created the X-Y plane ==================
        // =========================================================================

        float xInput = 75000f;
        float yInput = 42000f;
        boolean scaleWithMapSize = true;
        boolean randomPosition = false;

        try {
            JSONObject solSettings = Global.getSettings().loadJSON("data/config/sol_settings.json");
            xInput           = (float) solSettings.optDouble ("X-coord",            75000d);
            yInput           = (float) solSettings.optDouble ("Y-coord",            42000d);
            scaleWithMapSize =         solSettings.optBoolean("Scale_With_Map_Size", true);
            randomPosition   =         solSettings.optBoolean("Random_Location",    false);
        } catch (Exception e) {}

        float vanillaX = randomPosition ? (float) (Math.random() * 164000d) : xInput;
        float vanillaY = randomPosition ? (float) (Math.random() * 104000d) : yInput;

        float finalX = vanillaX;
        float finalY = vanillaY;

        if (scaleWithMapSize) {
            float[] whCoords = LocationXY
                .getScaledCoords(vanillaX, vanillaY);
            if (whCoords != null) {
                finalX = whCoords[0];
                finalY = whCoords[1];
            } else {
                float sectorWidth  = 164000f;
                float sectorHeight = 104000f;
                try {
                    JSONObject gameSettings = Global.getSettings().loadJSON("data/config/settings.json");
                    sectorWidth  = (float) gameSettings.optDouble("sectorWidth",  164000d);
                    sectorHeight = (float) gameSettings.optDouble("sectorHeight", 104000d);
                } catch (Exception e) {}
                finalX = (vanillaX / 164000f) * sectorWidth;
                finalY = (vanillaY / 104000f) * sectorHeight;
            }
        }

        system.getLocation().set(finalX, finalY);

        // I didn’t bother changing the texture, the yellow_star texture is basically a slightly altered version of a popular sun true color.
        // Tags stolen from suitable star systems

        // =========================================================================
        // ========================  The sun and the stars =========================
        // =========================================================================

        system.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg"); 

        float solMapGridSize = 120000f; 
        system.setMapGridWidthOverride(solMapGridSize);
        system.setMapGridHeightOverride(solMapGridSize);

        JSONObject cfg;
        try {
            cfg = Global.getSettings().loadJSON("data/config/sol_settings.json");
        } catch (Exception e) {
            cfg = new JSONObject();
        }

        if (cfg.optBoolean("Luddic_Church_Claim_On_Sol", false)) {
            system.getMemoryWithoutUpdate().set(MemFlags.CLAIMING_FACTION, Factions.LUDDIC_CHURCH);
        }
        boolean isSettled       = cfg.optBoolean("Generate_Settled_Planets", true);
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

        if(allowNonVisited){
            // Phaethon
            SectorEntityToken Phaethon = calc.spawnSPSObject(system, star, "Phaethon", "Phaethon", "asteroid", showNameMinor, 6f, 1.2714f, 0.8898f, 265.220f, 322.180f, 2020.96f, zeroDegGlobal, 0.150f, 1f);
            Phaethon.setCustomDescriptionId("sol_phaethon");

            // Attach Jump Point
            JumpPointAPI jpPhaethon = Global.getFactory().createJumpPoint("jp_phaethon", "Phaethon Jump Point");
            jpPhaethon.setStandardWormholeToHyperspaceVisual();
            jpPhaethon.setCircularOrbit(Phaethon, 20, 35, 10);
            system.addEntity(jpPhaethon);
        } else {
            JumpPointAPI jpPhaethon = Global.getFactory().createJumpPoint("jp_phaethon", "Sedna Jump Point");
            jpPhaethon.setStandardWormholeToHyperspaceVisual();
            system.addEntity(jpPhaethon);
            calc.applySPSOrbit(jpPhaethon, star, 506f, 0.8496f, 144.478f, 311.009f, 2075.73f, zeroDegGlobal, null, 1f, null, null, null, star, "Sol", false, false);
        }

        system.autogenerateHyperspaceJumpPoints(true, false, false);

        Global.getSector().addScript(new SolInnit(system, star));
    }
}
