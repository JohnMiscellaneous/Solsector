RunCode import com.fs.starfarer.api.campaign.*;

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

import data.scripts.world.systems.SolDiscoveryListener;
import data.scripts.world.systems.RemnantNexusFactory;
import data.scripts.world.systems.SolEconomies;
import data.scripts.world.systems.SolHyperspaceGen;
import data.scripts.world.systems.RemnantPatrolFactory;
import data.scripts.world.systems.GiantMoonsTotal;
import data.scripts.world.systems.AstroCalc;

StarSystemAPI system = (StarSystemAPI) Global.getSector().getPlayerFleet().getContainingLocation();
SectorEntityToken player = Global.getSector().getPlayerFleet();
SectorEntityToken star = system.getStar();

class Cleanup {
    void cleanupSystem(StarSystemAPI system, SectorEntityToken keepStar, SectorEntityToken keepPlayer) {
        List<SectorEntityToken> entities = new ArrayList<SectorEntityToken>(system.getAllEntities());
        for (SectorEntityToken entity : entities) {
            if (!entity.equals(keepStar) && !entity.equals(keepPlayer)) {
                if (entity.getMarket() != null) {
                    Global.getSector().getEconomy().removeMarket(entity.getMarket());
                }
                system.removeEntity(entity);
            }
        }
    }
}

// Instantiate the Factory (Empty constructor now)
RemnantNexusFactory nexusFactory = new RemnantNexusFactory();

// Instantiate the Factory
RemnantPatrolFactory patrolFactory = new RemnantPatrolFactory();

// 3. INITIALIZE & CLEANUP
AstroCalc calc = new AstroCalc();
new Cleanup().cleanupSystem(system, star, player);

// =========================================================================
// ============================ LET THERE BE LIGHT =========================
// =========================================================================
// Suun
float zeroDegGlobal = 210f;

// I didn’t bother changing the texture, the yellow_star texture is basically a slightly altered version of a popular sun true color.

// =========================================================================
// ========================  The sun and the stars =========================
// =========================================================================

system.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg"); 

float solMapGridSize = 150000f; 
system.setMapGridWidthOverride(solMapGridSize);
system.setMapGridHeightOverride(solMapGridSize);

// Tags stolen from suitable star systems
system.addTag(Tags.THEME_INTERESTING);
system.addTag(Tags.THEME_UNSAFE);
system.addTag(Tags.THEME_REMNANT);
system.addTag(Tags.THEME_REMNANT_MAIN);
system.addTag(Tags.THEME_REMNANT_RESURGENT);

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

boolean remnantHorde = true;
try {
    remnantHorde = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Remnant_Horde", true);
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
boolean innerSolShortlist = true;
try {
    innerSolShortlist = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Inner_Sol_Shortlist", true);
} catch (Exception e) {}
boolean visitedAsteroidsShortlist = true;
try {
    visitedAsteroidsShortlist = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Visited_Asteroids_Shortlist", true);
} catch (Exception e) {}
boolean asteroidBeltShortlist = true;
try {
    asteroidBeltShortlist = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Asteroid_Belt_Shortlist", true);
} catch (Exception e) {}
boolean jupiterTrojansShortlist = true;
try {
    jupiterTrojansShortlist = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Jupiter_Trojans_Shortlist", true);
} catch (Exception e) {}

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

boolean uranusShortlist = true;
try {
    uranusShortlist = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Uranus_Shortlist", true);
} catch (Exception e) {}
boolean neptuneShortlist = true;
try {
    neptuneShortlist = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Neptune_Shortlist", true);
} catch (Exception e) {}
boolean neptuneTrojansShortlist = true;
try {
    neptuneTrojansShortlist = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Neptune_Trojans_Shortlist", true);
} catch (Exception e) {}
boolean centaurShortlist = true;
try {
    centaurShortlist = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Trans_Neptune_Shortlist", true);
} catch (Exception e) {}
boolean transNeptuneShortlist = true;
try {
    transNeptuneShortlist = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Trans_Neptune_Shortlist", true);
} catch (Exception e) {}

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
int showNamesSetting = 1;
try {
    showNamesSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Show_Names", 1);
} catch (Exception e) {}

boolean showMinorNames;
boolean showProvisionalNames;
String showNameMinor;
String showNameProv;

if (showNamesSetting == 2) {
    showMinorNames = true;
    showProvisionalNames = true;
    showNameMinor = null;
    showNameProv = null;
} else if (showNamesSetting == 0) {
    showMinorNames = false;
    showProvisionalNames = false;
    showNameMinor = "no_name";
    showNameProv = "no_name";
} else { // default: 1
    showMinorNames = true;
    showProvisionalNames = false;
    showNameMinor = null;
    showNameProv = "no_name";
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

if (speculativeBodiesBigSetting == 2) {
    planetNine = true;
    planetTen = true;
} else if (speculativeBodiesBigSetting == 1) {
    planetNine = true;
    planetTen = false;
} else { // default: 0
    planetNine = false;
    planetTen = false;
}

boolean occultOrbitBeta = true;
try {
    occultOrbitBeta = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Occult_Orit_Beta", true);
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

// =========================================================================
// =========================== JUPITER SYSTEM ==============================
// =========================================================================

// ## JUPITER (The Primary)
// Jupiter
float dist_JupiterRaw = 5.2038f;
PlanetAPI Jupiter = system.addPlanet("Jupiter", star, "jupiter", "gas_giant", 0f, calc.getSize(120536f), 10000f, 100000f);
float angleJupiter = Jupiter.getCircularOrbitAngle();
float sz_Jupiter = Jupiter.getRadius();
float dist_Jupiter = Jupiter.getCircularOrbitRadius();
float p_Jupiter = Jupiter.getCircularOrbitPeriod();

Jupiter.getSpec().setTexture("graphics/planets/jupiter_tx.jpg"); 
Jupiter.getSpec().setAtmosphereThickness(1f); 
Jupiter.getSpec().setAtmosphereThicknessMin(20f); 
Jupiter.getSpec().setAtmosphereColor(new Color(180, 170, 150, 255)); 
Jupiter.getSpec().setIconColor(new Color(200, 160, 120, 255)); 
Jupiter.getSpec().setTilt(4.1f); 
Jupiter.getSpec().setPitch(93f); 
Jupiter.getSpec().setRotation(calc.getRot(.416f)); 
Jupiter.getSpec().setCloudTexture("graphics/planets/clouds_banded01.png"); 
Jupiter.getSpec().setCloudColor(new Color(255, 250, 240, 80)); 
Jupiter.getSpec().setCloudRotation(20.0f); 
Jupiter.applySpecChanges(); 

calc.addConditions(Jupiter.getMarket(), new String[] {
    "sol_irradiated_extreme",
    "high_gravity",
    "dense_atmosphere",
    "volatiles_plentiful",
    "meteor_impacts", // Wat u get 4 klling shoemaker Levy
    "cold",
    "extreme_weather"
});

// ## JUPITER RINGS & MAGNETIC FIELDS --------------------------------------

float distJupRingInner = calc.getDistJupiter(0.00082f);
float distJupRingOuter = calc.getDistJupiter(0.00086f); 
float distJupEuropa    = calc.getDistJupiter(0.0045f);
float jupGapClearance  = 60f;

// Halo Ring (inner extension, diffuses inward from main ring)
calc.smartRingTex(system, Jupiter, "sol_rings", "rings_alpha0", 128, 0, calc.getDistJupiter(0.000565f), calc.getDistJupiter(0.000819f), calc.getTimeGiant(32f));

// Main Ring
calc.smartRingTex(system, Jupiter, "misc", "rings_dust0", 256, 1, distJupRingInner, distJupRingOuter, calc.getTime(32f));
calc.smartRingTerrain(system, Jupiter, "Main Ring", distJupRingInner, distJupRingOuter, calc.getTime(32f));

// Amalthea Gossamer Ring (diffuses outward from main ring)
calc.smartRingTex(system, Jupiter, "sol_rings", "rings_alpha2", 128, 1, calc.getDistJupiter(0.000840f), calc.getDistJupiter(0.001217f), calc.getTimeGiant(32f));

// Thebe Gossamer Ring (faintest, diffuses outward from main ring)
calc.smartRingTex(system, Jupiter, "sol_rings", "rings_alpha0", 128, 1, calc.getDistJupiter(0.000840f), calc.getDistJupiter(0.001511f), calc.getTimeGiant(32f));

// Inner Magnetic Field
float magInnerLimit = distJupRingOuter - jupGapClearance;
float magInnerWidth = magInnerLimit;
float magInnerMid   = magInnerLimit / 2f;

SectorEntityToken magFieldInner = system.addTerrain(Terrain.MAGNETIC_FIELD,
new MagneticFieldTerrainPlugin.MagneticFieldParams(magInnerWidth, magInnerMid, Jupiter, sz_Jupiter * 0.9f, sz_Jupiter * 2f, new Color(40, 10, 60, 80), 1f));
magFieldInner.setCircularOrbit(Jupiter, 0, 0, calc.getTimeGiant(3.55f));

// Outer Magnetic Field (invisible terrain effect only)
float magOuterStart = distJupRingOuter + jupGapClearance;
float magOuterWidth = distJupEuropa - magOuterStart;
float magOuterMid   = magOuterStart + (magOuterWidth / 2f);

SectorEntityToken magFieldOuter = system.addTerrain(Terrain.MAGNETIC_FIELD,
new MagneticFieldTerrainPlugin.MagneticFieldParams(magOuterWidth, magOuterMid, Jupiter, magOuterStart, distJupEuropa, new Color(0, 0, 0, 0), 0f));
magFieldOuter.setCircularOrbit(Jupiter, 0, 0, calc.getTimeGiant(3.55f));

// =========================================================================
// ========================= JUPITER INNER MOONS ===========================
// =========================================================================

// Metis
float p_Io = calc.getTimeGiant(1.77f); 
SectorEntityToken Metis = system.addCustomEntity("Metis", "Metis", "Metis", "neutral"); 
Metis.setCircularOrbitPointingDown(Jupiter, 153f, calc.getDistJupiter(0.00085f), p_Io / 6f);

// Metis
SectorEntityToken Adrastea = system.addCustomEntity("Adrastea", "Adrastea", "Adrastea", "neutral"); 
Adrastea.setCircularOrbitPointingDown(Jupiter, 237f, calc.getDistJupiter(0.00086f), calc.getTimeGiant(0.298f));

// Amalthea
float p_Amalthea = calc.getTimeGiant(0.498f);
float sz_Amalthea = calc.getSize(167f);
float dist_Amalthea = calc.getDistJupiter(0.0012f);
PlanetAPI Amalthea = system.addPlanet("Amalthea", Jupiter, "Amalthea", "irradiated", 264, sz_Amalthea, dist_Amalthea, p_Amalthea);

Amalthea.getSpec().setTexture("graphics/planets/amalthea_tx.jpg");
Amalthea.getSpec().setPlanetColor(new Color(200, 100, 100, 255));
Amalthea.getSpec().setAtmosphereThickness(0f);
Amalthea.getSpec().setAtmosphereThicknessMin(10f);
Amalthea.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
Amalthea.getSpec().setIconColor(new Color(160, 80, 80, 255));
Amalthea.getSpec().setTilt(3.1f);
Amalthea.getSpec().setPitch(90f);
Amalthea.getSpec().setRotation(-360f / (p_Amalthea * 10f));
Amalthea.getSpec().setCloudColor(new Color(0, 0, 0, 0)); 
Amalthea.applySpecChanges();

calc.addConditions(Amalthea.getMarket(), new String[] {
    "cold",
    "ruins_scattered",
    "ore_moderate",
    "volatiles_trace",
    "no_atmosphere",
    "low_gravity",
    "sol_fast_moon",
    "sol_antimatter_infrastructure",
    "sol_irradiated_extreme",
    "sol_meteoroids"
});
// Thebe
SectorEntityToken Thebe = system.addCustomEntity("Thebe", "Thebe", "Thebe", "neutral"); 
Thebe.setCircularOrbitPointingDown(Jupiter, 125f, calc.getDistJupiter(0.0015f), calc.getTimeGiant(0.675f));

// ## GALILEAN MOONS--------------------------------------------------------
// Io
float sz_Io = calc.getSize(3643f);
float dist_Io = calc.getDistJupiter(0.0028f);
PlanetAPI Io = system.addPlanet("Io", Jupiter, "Io", "lava_minor", 97, sz_Io, dist_Io, p_Io);

// Texture: 
Io.getSpec().setTexture("graphics/planets/venuslike.jpg"); 
Io.getSpec().setIconColor(new Color(255, 80, 40, 255)); 
Io.applySpecChanges();

Io.getSpec().setTexture("graphics/planets/io_tx.jpg"); 
Io.getSpec().setAtmosphereThickness(0f); 
Io.getSpec().setAtmosphereThicknessMin(0f); 
Io.getSpec().setIconColor(new Color(220, 200, 100, 255)); 
Io.getSpec().setTilt(3.1f); 
Io.getSpec().setPitch(90f); 
Io.getSpec().setRotation(-360f / (p_Io * 10f)); 
Io.applySpecChanges();

calc.addConditions(Io.getMarket(), new String[] {
    "ruins_scattered",
    "extreme_tectonic_activity",
    "low_gravity",
    "ore_ultrarich",
    "rare_ore_ultrarich",
    "volatiles_trace",
    "irradiated",
    "no_atmosphere"
});
SectorEntityToken ioField = system.addTerrain(Terrain.MAGNETIC_FIELD, new MagneticFieldTerrainPlugin.MagneticFieldParams(sz_Io * .6f, sz_Io * 1.2f, Io, sz_Io *.9f, sz_Io *1.5f, new Color(30, 4, 44, 23), .01f)); 
ioField.setCircularOrbit(Io, 0, 0, 100);


// Europa
float sz_Europa = calc.getSize(3121f);
float dist_Europa = calc.getDistJupiter(0.0045f);
float p_Europa = p_Io * 2f;
PlanetAPI Europa = system.addPlanet("Europa", Jupiter, "Europa", "cryovolcanic", 292, sz_Europa, dist_Europa, p_Europa);

Europa.getSpec().setTexture("graphics/planets/europa_tx.jpg"); 
Europa.getSpec().setAtmosphereThickness(0f); 
Europa.getSpec().setAtmosphereThicknessMin(10f); 
Europa.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
Europa.getSpec().setIconColor(new Color(200, 200, 220, 255)); 
Europa.getSpec().setTilt(3.1f); 
Europa.getSpec().setPitch(90f); 
Europa.getSpec().setRotation(-360f / (p_Europa * 10f)); 
Europa.applySpecChanges();

calc.addConditions(Europa.getMarket(), new String[] {
    "irradiated",
    "tectonic_activity",
    "volatiles_plentiful",
    "ruins_widespread",
    "ore_moderate",
    "rare_ore_sparse",
    "no_atmosphere",
    "low_gravity"
});
// Ganymede
float p_Ganymede = p_Europa * 2f; 
float sz_Ganymede = calc.getSize(5268f);
float dist_Ganymede = calc.getDistJupiter(0.0072f);
float angleGanymede = 197f;
PlanetAPI Ganymede = system.addPlanet("Ganymede", Jupiter, "Ganymede", "water", angleGanymede , sz_Ganymede, dist_Ganymede, p_Ganymede );

Ganymede.getSpec().setTexture("graphics/planets/ganymede_tx_2.jpg"); 
Ganymede.getSpec().setTilt(3.1f); 
Ganymede.getSpec().setPitch(90f); 
Ganymede.getSpec().setRotation(-360f / (p_Ganymede * 10f)); Ganymede.applySpecChanges();

calc.addConditions(Ganymede.getMarket(), new String[] {
    "low_gravity",
    "volatiles_trace",
    "organics_trace",
    "ore_moderate",
    "rare_ore_sparse",
    "ruins_scattered",
    "habitable",
    "water_surface",
    "decivilized",
    "extreme_weather",
    "sol_pond_scum"
});
// Ganymede's magnetosphere
float ganyFieldLimit = sz_Ganymede * 2.5f;
SectorEntityToken ganymedeField = system.addTerrain(Terrain.MAGNETIC_FIELD, new MagneticFieldTerrainPlugin.MagneticFieldParams(ganyFieldLimit, ganyFieldLimit / 2f, Ganymede, sz_Ganymede * 0.9f, sz_Ganymede * 1.7f, new Color(180, 200, 255, 10), .3f));
ganymedeField.setCircularOrbit(Ganymede, 0, 0, p_Ganymede);

// Cuz melting Ice be energetic
system.addRingBand(Ganymede, "sol_rings", "rings_alpha1", 256, 1, Color.RED, 40, 100, 30);

// Ganymede L4 Fusion Lamp 
SectorEntityToken ganymedeL4Lamp = system.addCustomEntity(null, "Ganymede L4 Lamp", "fusion_lamp", "neutral");
ganymedeL4Lamp.setCircularOrbitPointingDown(Jupiter, angleGanymede + 60f, dist_Ganymede, p_Ganymede);

// Ganymede L5 Fusion Lamp (Trailing)
SectorEntityToken ganymedeL5Lamp = system.addCustomEntity(null, "Ganymede L5 Lamp", "fusion_lamp", "neutral");
ganymedeL5Lamp.setCircularOrbitPointingDown(Jupiter, angleGanymede - 60f, dist_Ganymede, p_Ganymede);

// Callisto | Diameter: ~4821 km
float sz_Callisto = calc.getSize(4821f);
float p_Callisto =  p_Ganymede * (7f/3f); // this isnt statistically significant, but whatever
float dist_Callisto = calc.getDistJupiter(0.0126f);
PlanetAPI Callisto = system.addPlanet("Callisto", Jupiter, "Callisto", "frozen", 161, sz_Callisto, dist_Callisto, p_Callisto);

Callisto.getSpec().setTexture("graphics/planets/callisto_tx.jpg"); 
Callisto.getSpec().setAtmosphereThickness(0f); 
Callisto.getSpec().setAtmosphereThicknessMin(10f); 
Callisto.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
Callisto.getSpec().setIconColor(new Color(140, 130, 120, 255)); 
Callisto.getSpec().setTilt(3.1f); Callisto.getSpec().setPitch(90f); 
Callisto.getSpec().setRotation(-360f / (p_Callisto * 10f)); 
Callisto.applySpecChanges();

calc.addConditions(Callisto.getMarket(), new String[] {
    "cold",
    "low_gravity",
    "no_atmosphere",
    "volatiles_plentiful",
    "ore_sparse",
    "rare_ore_sparse",
    "ruins_extensive",
    "sol_degenerate"
});
JumpPointAPI lightspeedJump = Global.getFactory().createJumpPoint("lightspeed_jump", "Jovian Jump Point");
lightspeedJump.setCircularOrbitPointingDown(Jupiter, angleJupiter, calc.getDistJupiter(0.35f), p_Jupiter);
lightspeedJump.setStandardWormholeToHyperspaceVisual();
system.addEntity(lightspeedJump);

// =========================================================================
// ===================== JUPITER IRREGULAR MOONS ===========================
// =========================================================================

if(fictionalTNOs){
    // Leda
    SectorEntityToken Leda = calc.spawnIrregularBody2(system, Jupiter, "Leda", "Leda", "moon", showNameMinor, 21.5f, 0.074501f, 0.162f, 232.6f, 271.3f, 1999.604f, zeroDegGlobal, null, 0.0009546f, "Jupiter", false);
    Leda.setCustomDescriptionId("sol_leda");
}

// Himalia Ring (diffuse dust torus around Himalia group)
calc.smartRingTex(system, Jupiter, "sol_rings", "rings_alpha0", 256, 0, calc.getDistJupiter(0.050f), calc.getDistJupiter(0.087f), calc.getTime(250f));

// Himalia
PlanetAPI Himalia = (PlanetAPI) calc.spawnIrregularBody2(system, Jupiter, "Himalia", "Himalia", "barren-bombarded", null, 140f, 0.076465f, 0.16f, 78.3f, 321.1f, 1999.878f, zeroDegGlobal, null, 0.0009546f, "Jupiter", false);

Himalia.getSpec().setTexture("graphics/planets/himalia_tx.jpg");
Himalia.getSpec().setAtmosphereThickness(0f);
Himalia.getSpec().setAtmosphereThicknessMin(10f);
Himalia.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
Himalia.getSpec().setIconColor(new Color(120, 120, 130, 255));
Himalia.getSpec().setTilt(3.1f);
Himalia.getSpec().setPitch(60f);
Himalia.getSpec().setRotation(calc.getRot(.322f));
Himalia.applySpecChanges();

calc.addConditions(Himalia.getMarket(), new String[] {
    "cold",
    "no_atmosphere",
    "low_gravity",
    "ore_sparse",
    "volatiles_diffuse",
    "ruins_scattered",
    "poor_light",
    "sol_meteoroids"
});

// Elara
SectorEntityToken Elara = calc.spawnIrregularBody2(system, Jupiter, "Elara", "Elara", "moon", showNameMinor, 79.9f, 0.078281f, 0.212f, 346.9f, 129.9f, 1999.778f, zeroDegGlobal, null, 0.0009546f, "Jupiter", false);

if(!jupiterShortlist){
    // Lysithea
    SectorEntityToken Lysithea = calc.spawnIrregularBody2(system, Jupiter, "Lysithea", "Lysithea", "moon", showNameMinor, 42.2f, 0.078204f, 0.117f, 328.5f, 48.3f, 1999.982f, zeroDegGlobal, null, 0.0009546f, "Jupiter", false);
    // Themisto
    SectorEntityToken Themisto = calc.spawnIrregularBody2(system, Jupiter, "Themisto", "Themisto", "moon", showNameMinor, 9.0f, 0.049446f, 0.257f, 289.1f, 236.3f, 1999.8f, zeroDegGlobal, null, 0.0009546f, "Jupiter", false);
    // Carpo
    SectorEntityToken Carpo = calc.spawnIrregularBody2(system, Jupiter, "Carpo", "Carpo", "moon", showNameMinor, 3.0f, 0.113902f, 0.415f, 336.6f, 90.5f, 1999.82f, zeroDegGlobal, null, 0.0009546f, "Jupiter", false);
}

// ## RETROGRADE GROUP (Pasiphae/Ananke/Carme) ---------------------------

// Carme
SectorEntityToken Carme = calc.spawnIrregularBody2(system, Jupiter, "Carme", "Carme", "moon", showNameMinor, 46.7f, 0.154676f, 0.261f, 259.5f, 6.5f, 2000.632f, zeroDegGlobal, null, 0.0009546f, "Jupiter", true);

// Pasiphae
SectorEntityToken Pasiphae = calc.spawnIrregularBody2(system, Jupiter, "Pasiphae", "Pasiphae", "moon", showNameMinor, 57.8f, 0.156842f, 0.412f, 279.3f, 172.8f, 2001.763f, zeroDegGlobal, null, 0.0009546f, "Jupiter", true);

if(!jupiterShortlist){
    // Ananke
    SectorEntityToken Ananke = calc.spawnIrregularBody2(system, Jupiter, "Ananke", "Ananke", "moon", showNameMinor, 29.1f, 0.140574f, 0.238f, 271.7f, 78.8f, 2000.065f, zeroDegGlobal, null, 0.0009546f, "Jupiter", true);
    // Sinope
    SectorEntityToken Sinope = calc.spawnIrregularBody2(system, Jupiter, "Sinope", "Sinope", "moon", showNameMinor, 35.0f, 0.158286f, 0.262f, 157.4f, 354.3f, 2001.744f, zeroDegGlobal, null, 0.0009546f, "Jupiter", true);
}

if(jupiterAll){
    new GiantMoonsTotal().spawn(system, Jupiter, "jupiter", angleJupiter);
}
