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
    DetailSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Hilda_Detail", 1);
} catch (Exception e) {}
boolean hildaShortlist = true;
if(DetailSetting >= 1){
    hildaShortlist = false;  
}

DetailSetting = 1;
try {
    DetailSetting = Global.getSettings().loadJSON("data/config/sol_settings.json").optInt("Jupiter_Trojans_Detail", 1);
} catch (Exception e) {}
boolean jupiterTrojansShortlist = true;
if(DetailSetting >= 1){
    jupiterTrojansShortlist = false;  
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

// Phaethon
SectorEntityToken Phaethon = calc.spawnSPSObject(system, star, "Phaethon", "Phaethon", "asteroid", showNameMinor, 6f, 1.2714f, 0.8898f, 265.220f, 322.180f, 2020.96f, zeroDegGlobal, 0.150f, 1f);
Phaethon.setCustomDescriptionId("sol_phaethon");

// Attach Jump Point
JumpPointAPI jpPhaethon = Global.getFactory().createJumpPoint("jp_phaethon", "Phaethon Jump Point");
jpPhaethon.setStandardWormholeToHyperspaceVisual();
jpPhaethon.setCircularOrbit(Phaethon, 20, 35, 10);
system.addEntity(jpPhaethon);

float dist_VenusRaw = 0.7233f;
PlanetAPI Venus = (PlanetAPI) calc.spawnSPSObject(system, star, "Venus", "Venus", "toxic", null, 12104f, dist_VenusRaw, 0.0068f, 76.680f, 54.884f, 2025.14f, zeroDegGlobal, null, 1f);
Venus.setSkipForJumpPointAutoGen(true);

system.autogenerateHyperspaceJumpPoints(true, false, false);

Global.getSector().addScript(new SolInnit(system, star));
}
}

