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
// =========================== NEPTUNE SYSTEM ===============================
// =========================================================================

// ## URANUS (The Primary)--------------------------------------------------
// Uranus | Semi Major Axis: 19.19 AU | Diameter: ~50,724 km | Period: 30,660 days
float dist_NeptuneRaw = 30.07f;
PlanetAPI Neptune = system.addPlanet("Neptune", star, "Neptune", "ice_giant", 0f, calc.getSize(49244f), 10000f, 100000f);
float angleNeptune = Neptune.getCircularOrbitAngle();
float sz_Neptune = Neptune.getRadius();
float dist_Neptune = Neptune.getCircularOrbitRadius();
float p_Neptune = Neptune.getCircularOrbitPeriod(); 

Neptune.getSpec().setTexture("graphics/planets/neptune_tx.jpg");
Neptune.getSpec().setAtmosphereThickness(0.3f);
Neptune.getSpec().setAtmosphereColor(new Color(180, 220, 250, 140));
Neptune.getSpec().setCloudTexture("graphics/planets/clouds_banded01.png");
Neptune.getSpec().setCloudColor(new Color(255, 255, 255, 100));
Neptune.getSpec().setCloudRotation(calc.getRot(.75f));
Neptune.getSpec().setIconColor(new Color(80, 100, 220, 255));
Neptune.getSpec().setTilt(28.3f);
Neptune.getSpec().setPitch(90f);
Neptune.getSpec().setRotation(calc.getRot(.666f));
Neptune.applySpecChanges();

calc.addConditions(Neptune.getMarket(), new String[] {
    "very_cold",
    "dark",
    "high_gravity",
    "dense_atmosphere",
    "volatiles_diffuse",
    "extreme_weather"
});

// NEPTUNE MAGNETOSPHERE
// Cutoff at Galle Ring inner edge (0.00025f) minus 60 units gap
float nepMagLimit = calc.getDistNeptune(0.00025f) - 60f;
SectorEntityToken neptuneInnerField = system.addTerrain(Terrain.MAGNETIC_FIELD, new MagneticFieldTerrainPlugin.MagneticFieldParams(nepMagLimit, nepMagLimit / 2f, Neptune, sz_Neptune * 0.9f, sz_Neptune * 1.5f, new Color(150, 200, 230, 5), .2f));
neptuneInnerField.setCircularOrbit(Neptune, 0, 0, 100);
neptuneInnerField.setName("Neptune's Magnetosphere");

// =========================================================================
// NEPTUNE RINGS
// =========================================================================

// Galle Ring — Real: 40,900–42,900 km → 0.000273–0.000287 AU
calc.smartRingTex(system, Neptune, "sol_rings", "rings_alpha1", 256, 0, calc.getDistNeptune(0.000273f), calc.getDistNeptune(0.000287f), 30);
calc.smartRingTerrain(system, Neptune, "Galle Ring", calc.getDistNeptune(0.000273f), calc.getDistNeptune(0.000287f), 0.20f);

// Le Verrier Ring — Real: ~53,200 km → 0.000356 AU
calc.smartRingTex(system, Neptune, "misc", "rings_dust0", 256, 1, calc.getDistNeptune(0.000354f), calc.getDistNeptune(0.000358f), 30);

// Lassell Ring — Real: 53,200–57,200 km → 0.000356–0.000382 AU
calc.smartRingTex(system, Neptune, "sol_rings", "rings_alpha1", 256, 0, calc.getDistNeptune(0.000358f), calc.getDistNeptune(0.000382f), 30);
calc.smartRingTex(system, Neptune, "sol_rings", "rings_alpha0", 256, 1, calc.getDistNeptune(0.000362f), calc.getDistNeptune(0.000378f), 30);

// Arago Ring — Real: ~57,200 km → 0.000382 AU
calc.smartRingTex(system, Neptune, "misc", "rings_dust0", 256, 1, calc.getDistNeptune(0.000380f), calc.getDistNeptune(0.000384f), 30);

// Le Verrier–Arago terrain
calc.smartRingTerrain(system, Neptune, "Le Verrier–Arago Rings", calc.getDistNeptune(0.000354f), calc.getDistNeptune(0.000384f), 0.28f);

// Adams Ring — Real: ~62,930 km → 0.000421 AU
calc.smartRingTex(system, Neptune, "misc", "rings_special0", 256, 1, calc.getDistNeptune(0.000420f), calc.getDistNeptune(0.000422f), 30);
calc.smartRingTerrain(system, Neptune, "Adams Ring", calc.getDistNeptune(0.000420f), calc.getDistNeptune(0.000422f), 0.38f);

// =========================================================================
// ========================= NEPTUNE INNER MOONS ===========================
// =========================================================================

// Naiad
float p_Naiad = calc.getTimeGiant(0.294f);
calc.spawnMoon(system, Neptune, "Naiad", calc.getSize(66f), calc.getDistNeptune(0.00032f), p_Naiad, 245f, showMinorNames);

// Thalassa (nice ressy)
calc.spawnMoon(system, Neptune, "Thalassa", calc.getSize(82f), calc.getDistNeptune(0.00033f), p_Naiad * (73f/69f), 15f, showMinorNames);

if(!neptuneShortlist){
    // Hippocamp
    SectorEntityToken Hippocamp = calc.spawnMoon(system, Neptune, "Hippocamp", calc.getSize(18f), calc.getDistNeptune(0.00070f), calc.getTimeGiant(0.940f), 90f, showMinorNames);
}

// Despina
calc.spawnMoon(system, Neptune, "Despina", calc.getSize(150f), calc.getDistNeptune(0.00035f), calc.getTimeGiant(0.335f), 53f, showMinorNames);

// Galatea 
calc.spawnMoon(system, Neptune, "Galatea", calc.getSize(174f), calc.getDistNeptune(0.00041f), calc.getTimeGiant(0.428f), 8f, showMinorNames);

// Larissa
PlanetAPI Larissa = system.addPlanet("Larissa", Neptune, "Larissa", "rocky_ice", 320, 
calc.getSize(194f), calc.getDistNeptune(0.00049f), calc.getTimeGiant(0.555f));

Larissa.getSpec().setTexture("graphics/planets/larissa_tx.jpg");
Larissa.getSpec().setAtmosphereThickness(0f);
Larissa.getSpec().setAtmosphereColor(new Color(0,0,0,0));
Larissa.getSpec().setIconColor(new Color(150, 150, 160, 255));
Larissa.getSpec().setTilt(28.3f);
Larissa.getSpec().setPitch(90f);
Larissa.getSpec().setRotation(-(360f / (calc.getTimeGiant(0.555f) * 10f)));
Larissa.applySpecChanges();

calc.addConditions(Larissa.getMarket(), new String[] {
    "no_atmosphere",
    "very_cold",
    "low_gravity",
    "volatiles_trace",
    "dark",
    "sol_space_elevator_nearby",
    "ruins_scattered"
});

// Proteus
PlanetAPI Proteus = system.addPlanet("Proteus", Neptune, "Proteus", "rocky_ice", 318, 
calc.getSize(420f), calc.getDistNeptune(0.00078f), calc.getTimeGiant(1.122f));

Proteus.getSpec().setTexture("graphics/planets/proteus_tx.jpg");
Proteus.getSpec().setAtmosphereThickness(0f);
Proteus.getSpec().setAtmosphereColor(new Color(0,0,0,0));
Proteus.getSpec().setIconColor(new Color(140, 140, 150, 255));
Proteus.getSpec().setTilt(28.3f);
Proteus.getSpec().setPitch(90f);
Proteus.getSpec().setRotation(-(360f / (calc.getTimeGiant(1.122f) * 10f)));
Proteus.applySpecChanges();

calc.addConditions(Proteus.getMarket(), new String[] {
    "no_atmosphere",
    "very_cold",
    "low_gravity",
    "volatiles_trace",
    "dark",
    "sol_space_elevator_nearby",
    "ruins_widespread"
});

// Triton
float sz_Triton = calc.getSize(2706f);
float dist_Triton = calc.getDistNeptune(0.00237f);
float p_Triton = -calc.getTimeGiant(5.877f); // Retrograde

PlanetAPI Triton = system.addPlanet("Triton", Neptune, "Triton", "cryovolcanic", 128, sz_Triton, dist_Triton, p_Triton);

Triton.getSpec().setTexture("graphics/planets/triton_tx.jpg");
Triton.getSpec().setAtmosphereThickness(0.05f);
Triton.getSpec().setAtmosphereColor(new Color(255, 230, 230, 40));
Triton.getSpec().setCloudTexture("graphics/planets/clouds_white.png");
Triton.getSpec().setCloudColor(new Color(255, 255, 255, 20));
Triton.getSpec().setCloudRotation(-0.5f);
Triton.getSpec().setIconColor(new Color(137, 184, 218, 255));
Triton.getSpec().setTilt(28.3f);
Triton.getSpec().setPitch(90f);
Triton.getSpec().setRotation((360f / (calc.getTimeGiant(5.877f) * 10f)));
Triton.applySpecChanges();

calc.addConditions(Triton.getMarket(), new String[] {
    "very_cold",
    "dark",
    "thin_atmosphere",
    "low_gravity",
    "tectonic_activity",
    "volatiles_plentiful",
    "ore_moderate",
    "rare_ore_moderate",
    "ruins_vast",
    "sol_retrograde_moon",
    "sol_space_elevator_nearby",
    "sol_automated_habitats",
    "sol_degenerate"
});
Triton.setCustomDescriptionId("sol_triton");

// =========================================================================
// ===================== NEPTUNE IRREGULAR MOONS ===========================
// =========================================================================

// Nereid
PlanetAPI Nereid = (PlanetAPI) calc.spawnIrregularBody2(system, Neptune, "Nereid", "Nereid", "rocky_ice", null, 357f, 0.036859f, 0.751f, 318.5f, 296.8f, 2019.123f, zeroDegGlobal, null, 0.00005151f, "Neptune", false);

Nereid.getSpec().setTexture("graphics/planets/nereid_tx.jpg");
Nereid.getSpec().setAtmosphereThickness(0f);
Nereid.getSpec().setAtmosphereColor(new Color(0,0,0,0));
Nereid.getSpec().setIconColor(new Color(160, 160, 170, 255));
Nereid.getSpec().setTilt(28.3f);
Nereid.getSpec().setPitch(60f);
Nereid.getSpec().setRotation(calc.getRot(.483f));
Nereid.applySpecChanges();

calc.addConditions(Nereid.getMarket(), new String[] {
    "no_atmosphere",
    "very_cold",
    "low_gravity",
    "ore_sparse",
    "volatiles_diffuse",
    "ruins_extensive",
    "dark",
    "sol_space_elevator"
});

if(generateElevators){
    SectorEntityToken elevatorNereid = system.addCustomEntity("elevator1", "Nereid Elevator", "elevator1", "neutral"); 
    elevatorNereid.setCircularOrbitPointingDown(Nereid, 0f, calc.getSize(340f)+33, calc.getOrbRot(0.48f));
}

// Halimede
SectorEntityToken Halimede = calc.spawnIrregularBody2(system, Neptune, "Halimede", "Halimede", "moon", showNameMinor, 61f, 0.110902f, 0.521f, 135.8f, 157.9f, 2017.913f, zeroDegGlobal, null, 0.00005151f, "Neptune", true);

if(!neptuneShortlist){
    // Sao
    SectorEntityToken Sao = calc.spawnIrregularBody2(system, Neptune, "Sao", "Sao", "moon", showNameMinor, 40f, 0.148664f, 0.296f, 178.5f, 93.5f, 2019.666f, zeroDegGlobal, null, 0.00005151f, "Neptune", false);
    // S/2002 N 5
    SectorEntityToken S2002N5 = calc.spawnIrregularBody2(system, Neptune, "S/2002 N 5", "S/2002 N 5", "moon", showNameProv, 38f, 0.156518f, 0.433f, 303.2f, 59.1f, 2011.376f, zeroDegGlobal, null, 0.00005151f, "Neptune", false);
    // Laomedeia
    SectorEntityToken Laomedeia = calc.spawnIrregularBody2(system, Neptune, "Laomedeia", "Laomedeia", "moon", showNameMinor, 40f, 0.157094f, 0.419f, 248.1f, 146.2f, 2019.5f, zeroDegGlobal, null, 0.00005151f, "Neptune", false);
    // Psamathe
    SectorEntityToken Psamathe = calc.spawnIrregularBody2(system, Neptune, "Psamathe", "Psamathe", "moon", showNameMinor, 38f, 0.318495f, 0.413f, 183.2f, 148.2f, 2027.591f, zeroDegGlobal, null, 0.00005151f, "Neptune", true);
}

// Neso (furthest moon, longest orbit) ignore the provisional one
SectorEntityToken Neso = calc.spawnIrregularBody2(system, Neptune, "Neso", "Neso", "moon", showNameMinor, 60f, 0.333553f, 0.455f, 13.8f, 89.8f, 2018.513f, zeroDegGlobal, null, 0.00005151f, "Neptune", true);
Neso.setCustomDescriptionId("sol_neso");

if(!neptuneShortlist){
    // S/2021 N 1
    SectorEntityToken S2021N1 = calc.spawnIrregularBody2(system, Neptune, "S/2021 N 1", "S/2021 N 1", "moon", showNameProv, 25f, 0.338919f, 0.503f, 237.1f, 90.4f, 2012.892f, zeroDegGlobal, null, 0.00005151f, "Neptune", true);
}
