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
// =========================== Uranus SYSTEM ===============================
// =========================================================================

// ## URANUS (The Primary)--------------------------------------------------
// Uranus | Semi Major Axis: 19.19 AU | Diameter: ~50,724 km | Period: 30,660 days
float dist_UranusRaw = 19.1913f;
PlanetAPI Uranus = system.addPlanet("Uranus", star, "Uranus", "ice_giant", 0f, calc.getSize(51118f), 10000f, 100000f);
float angleUranus = Uranus.getCircularOrbitAngle();
float p_Uranus = Uranus.getCircularOrbitPeriod();
Float dist_Uranus = Uranus.getCircularOrbitRadius();
Float sz_Uranus = Uranus.getRadius();

Uranus.getSpec().setTexture("graphics/planets/uranus_tx.jpg"); 
Uranus.getSpec().setAtmosphereThickness(0.3f); 
Uranus.getSpec().setAtmosphereColor(new Color(180, 220, 250, 140)); 
Uranus.getSpec().setCloudTexture("graphics/planets/clouds_banded01.png"); 
Uranus.getSpec().setCloudColor(new Color(255, 255, 255, 80)); 
Uranus.getSpec().setCloudRotation(calc.getRot(1.218f)); 
Uranus.getSpec().setIconColor(new Color(150, 200, 230, 255)); 
Uranus.getSpec().setTilt(97.8f); 
Uranus.getSpec().setPitch(0f); 
Uranus.getSpec().setRotation(calc.getRot(.718f)); 
Uranus.applySpecChanges(); 

calc.addConditions(Uranus.getMarket(), new String[] {
    "dense_atmosphere",
    "high_gravity",
    "very_cold",
    "volatiles_abundant",
    "dark",
    "irradiated",
    "ruins_scattered"
});
// Magnetosphere
float uraMagLimit = calc.getDistUranus(0.000247f) - 60f;
SectorEntityToken uranusInnerField = system.addTerrain(Terrain.MAGNETIC_FIELD, new MagneticFieldTerrainPlugin.MagneticFieldParams(uraMagLimit, uraMagLimit / 2f, Uranus, sz_Uranus * .9f, sz_Uranus * 1.5f, new Color(150, 200, 230, 5), .3f));
uranusInnerField.setCircularOrbit(Uranus, 0, 0, 100);
uranusInnerField.setName("Uranus' Magnetosphere");

// ## URANUS RINGS----------------------------------------------------------

// Zeta Ring (1986U2R) - broad, faint inner dusty ring
// Real (extended): 26,840–39,500 km → 0.000179–0.000264 AU
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha0", 256, 0, calc.getDistUranus(0.000179f), calc.getDistUranus(0.000247f), 30);
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha1", 256, 0, calc.getDistUranus(0.000247f), calc.getDistUranus(0.000264f), 30);

// Inner Narrow Rings (6, 5, 4, Alpha, Beta, Eta, Gamma, Delta)
// Real: 41,837–48,300 km → 0.000280–0.000323 AU
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000278f), calc.getDistUranus(0.000282f), 30); // 6
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000281f), calc.getDistUranus(0.000284f), 30); // 5
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000283f), calc.getDistUranus(0.000287f), 30); // 4
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000297f), calc.getDistUranus(0.000301f), 30); // Alpha
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000303f), calc.getDistUranus(0.000307f), 30); // Beta
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000313f), calc.getDistUranus(0.000317f), 30); // Eta
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000316f), calc.getDistUranus(0.000320f), 30); // Gamma
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000321f), calc.getDistUranus(0.000325f), 30); // Delta

// Lambda Ring — Real: ~50,024 km → 0.000334 AU
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000332f), calc.getDistUranus(0.000336f), 30);

// Terrain: Dense Zeta through Lambda
calc.smartRingTerrain(system, Uranus, "Inner Ring System", calc.getDistUranus(0.000247f), calc.getDistUranus(0.000336f), 0.25f);

// Epsilon Ring — Real: ~51,149 km → 0.000342 AU
calc.smartRingTex(system, Uranus, "misc", "rings_dust0", 256, 1, calc.getDistUranus(0.000336f), calc.getDistUranus(0.000341f), 30);
calc.smartRingTex(system, Uranus, "misc", "rings_special0", 256, 1, calc.getDistUranus(0.000340f), calc.getDistUranus(0.000345f), 30);
calc.smartRingTerrain(system, Uranus, "Epsilon Ring", calc.getDistUranus(0.000336f), calc.getDistUranus(0.000345f), 0.30f);

// Nu Ring — Real: 66,100–69,900 km → 0.000442–0.000467 AU
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha0", 256, 0, calc.getDistUranus(0.000442f), calc.getDistUranus(0.000467f), 30);
calc.smartRingTerrain(system, Uranus, "Nu Ring", calc.getDistUranus(0.000442f), calc.getDistUranus(0.000467f), 0.35f);

// Mu Ring — Real: 86,000–103,000 km → 0.000575–0.000689 AU
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha1", 256, 0, calc.getDistUranus(0.000575f), calc.getDistUranus(0.000689f), 30);
calc.smartRingTerrain(system, Uranus, "Mu Ring", calc.getDistUranus(0.000575f), calc.getDistUranus(0.000689f), 0.40f);

// =========================================================================
// ========================= URANUS INNER MOONS ============================
// =========================================================================

// Cordelia (inner Epsilon shepherd)
// Real: 49,771 km → 0.000333 AU | Period: 0.335 days
SectorEntityToken Cordelia = system.addCustomEntity("Cordelia", "Cordelia", "Cordelia", "neutral"); 
Cordelia.setCircularOrbitPointingDown(Uranus, 45f, calc.getDistUranus(0.000333f), calc.getTimeGiant(0.335f));

// Ophelia (outer Epsilon shepherd)
// Real: 53,764 km → 0.000359 AU | Period: 0.376 days
SectorEntityToken Ophelia = system.addCustomEntity("Ophelia", "Ophelia", "Ophelia", "neutral"); 
Ophelia.setCircularOrbitPointingDown(Uranus, 120f, calc.getDistUranus(0.000359f), calc.getTimeGiant(0.376f));

if(!uranusShortlist){
    // S/2025 U 1 (Recent Discovery)
    // Real: ~55,500 km → 0.000371 AU | Period: ~0.39 days
    SectorEntityToken S2025U1 = calc.spawnMoon(system, Uranus, "S/2025 U 1", calc.getSize(9f), calc.getDistUranus(0.000371f), calc.getTimeGiant(0.39f), 200f, showProvisionalNames);
}

// Bianca
// Real: 59,165 km → 0.000395 AU | Period: 0.435 days
SectorEntityToken Bianca = system.addCustomEntity("Bianca", "Bianca", "Bianca", "neutral"); 
Bianca.setCircularOrbitPointingDown(Uranus, 315f, calc.getDistUranus(0.000395f), calc.getTimeGiant(0.435f));

// Cressida
// Real: 61,767 km → 0.000413 AU | Period: 0.464 days
SectorEntityToken Cressida = system.addCustomEntity("Cressida", "Cressida", "Cressida", "neutral"); 
Cressida.setCircularOrbitPointingDown(Uranus, 180f, calc.getDistUranus(0.000413f), calc.getTimeGiant(0.464f));

// Desdemona
// Real: 62,659 km → 0.000419 AU | Period: 0.474 days
SectorEntityToken Desdemona = system.addCustomEntity("Desdemona", "Desdemona", "Desdemona", "neutral"); 
Desdemona.setCircularOrbitPointingDown(Uranus, 270f, calc.getDistUranus(0.000419f), calc.getTimeGiant(0.474f));

// Juliet
// Real: 64,358 km → 0.000430 AU | Period: 0.493 days
SectorEntityToken Juliet = system.addCustomEntity("Juliet", "Juliet", "Juliet", "neutral"); 
Juliet.setCircularOrbitPointingDown(Uranus, 90f, calc.getDistUranus(0.000430f), calc.getTimeGiant(0.493f));

// Portia
// Real: 66,097 km → 0.000442 AU | Period: 0.513 days
SectorEntityToken Portia = system.addCustomEntity("Portia", "Portia", "Portia", "neutral"); 
Portia.setCircularOrbitPointingDown(Uranus, 45f, calc.getDistUranus(0.000442f), calc.getTimeGiant(0.513f));

// Rosalind
// Real: 69,927 km → 0.000467 AU | Period: 0.558 days
SectorEntityToken Rosalind = system.addCustomEntity("Rosalind", "Rosalind", "Rosalind", "neutral"); 
Rosalind.setCircularOrbitPointingDown(Uranus, 225f, calc.getDistUranus(0.000467f), calc.getTimeGiant(0.558f));

if(!uranusShortlist){
    // Cupid
    // Real: 74,800 km → 0.000500 AU | Period: 0.613 days
    calc.spawnMoon(system, Uranus, "Cupid", calc.getSize(18f), calc.getDistUranus(0.000500f), calc.getTimeGiant(0.613f), 15f, showMinorNames);
}

// Belinda
// Real: 75,255 km → 0.000503 AU | Period: 0.624 days
SectorEntityToken Belinda = system.addCustomEntity("Belinda", "Belinda", "Belinda", "neutral"); 
Belinda.setCircularOrbitPointingDown(Uranus, 135f, calc.getDistUranus(0.000503f), calc.getTimeGiant(0.624f));

if(!uranusShortlist){
    // Perdita
    // Real: 76,417 km → 0.000511 AU | Period: 0.638 days
    calc.spawnMoon(system, Uranus, "Perdita", calc.getSize(30f), calc.getDistUranus(0.000511f), calc.getTimeGiant(0.638f), 300f, showMinorNames);
}

// Puck
// Real: 86,004 km → 0.000575 AU | Period: 0.762 days
SectorEntityToken Puck = system.addCustomEntity("Puck", "Puck", "Puck", "neutral");
Puck.setCircularOrbitPointingDown(Uranus, 322, calc.getDistUranus(0.000575f), calc.getTimeGiant(0.762f));

if (!isSettled) {
    Puck.setInteractionImage("illustrations", "abandoned_station2");
    Misc.setAbandonedStationMarket("marketPuck", Puck);

    Puck.getMarket().setName("Puck");
    Puck.getMarket().addCondition("abandoned_station");
    Puck.getMarket().addCondition("very_cold");
    Puck.getMarket().addCondition("low_gravity");
    Puck.getMarket().addCondition("ore_sparse");
    Puck.getMarket().addCondition("volatiles_trace");
    Puck.getMarket().addCondition("dark");
    Puck.getMarket().addCondition("irradiated");
    Puck.getMarket().addCondition("sol_meteoroids");

    for (MarketConditionAPI condition : Puck.getMarket().getConditions()) {
        condition. setSurveyed(true);
    }

    Puck.setDiscoverable(true);
    Puck.setSensorProfile(4000f);
}

// Mab
// Real: 97,736 km → 0.000653 AU | Period: 0.923 days
SectorEntityToken Mab = system.addCustomEntity("Mab", "Mab", "Mab", "neutral"); 
Mab.setCircularOrbitPointingDown(Uranus, 60f, calc.getDistUranus(0.000653f), calc.getTimeGiant(0.923f));

// ## MAJOR MOONS-----------------------------------------------------------

// Miranda
PlanetAPI Miranda = system.addPlanet("Miranda", Uranus, "Miranda", "rocky_unstable", 172, 
calc.getSize(471f), calc.getDistUranus(0.000865f), calc.getTimeGiant(1.413f));

Miranda.getSpec().setTexture("graphics/planets/miranda_tx.jpg");
Miranda.getSpec().setAtmosphereThickness(0f);
Miranda.getSpec().setAtmosphereColor(new Color(0,0,0,0));
Miranda.getSpec().setIconColor(new Color(200, 200, 210, 255));
Miranda.getSpec().setTilt(97.8f);
Miranda.getSpec().setPitch(0f);
Miranda.getSpec().setRotation(-(360f / (calc.getTimeGiant(1.413f) * 10f)));
Miranda.applySpecChanges();

calc.addConditions(Miranda.getMarket(), new String[] {
    "no_atmosphere",
    "very_cold",
    "low_gravity",
    "tectonic_activity",
    "ore_moderate",
    "volatiles_trace",
    "dark",
    "ruins_scattered",
    "sol_loose_bioweapon",
    "sol_ai_terminators",
    "sol_insurgent_network_desperate"
});

// Ariel
PlanetAPI Ariel = system.addPlanet("Ariel", Uranus, "Ariel", "frozen", 89, 
calc.getSize(1158f), calc.getDistUranus(0.001276f), calc.getTimeGiant(2.520f));

Ariel.getSpec().setTexture("graphics/planets/ariel_tx.jpg");
Ariel.getSpec().setAtmosphereThickness(0f);
Ariel.getSpec().setAtmosphereColor(new Color(0,0,0,0));
Ariel.getSpec().setIconColor(new Color(220, 220, 230, 255));
Ariel.getSpec().setTilt(97.8f);
Ariel.getSpec().setPitch(0f);
Ariel.getSpec().setRotation(-(360f / (calc.getTimeGiant(2.520f) * 10f)));
Ariel.applySpecChanges();

calc.addConditions(Ariel.getMarket(), new String[] {
    "no_atmosphere",
    "very_cold",
    "low_gravity",
    "ore_sparse",
    "volatiles_diffuse",
    "dark",
    "ruins_widespread",
    "sol_unexploded_ordnance",
    "sol_loose_bioweapon",
    "sol_ai_terminators"
});
// Umbriel
PlanetAPI Umbriel = system.addPlanet("Umbriel", Uranus, "Umbriel", "frozen", 345, 
calc.getSize(1169f), calc.getDistUranus(0.001778f), calc.getTimeGiant(4.144f));

Umbriel.getSpec().setTexture("graphics/planets/umbriel_tx.jpg");
Umbriel.getSpec().setAtmosphereThickness(0f);
Umbriel.getSpec().setAtmosphereColor(new Color(0,0,0,0));
Umbriel.getSpec().setIconColor(new Color(120, 120, 130, 255));
Umbriel.getSpec().setTilt(97.8f);
Umbriel.getSpec().setPitch(0f);
Umbriel.getSpec().setRotation(-(360f / (calc.getTimeGiant(4.144f) * 10f)));
Umbriel.applySpecChanges();

calc.addConditions(Umbriel.getMarket(), new String[] {
    "no_atmosphere",
    "very_cold",
    "low_gravity",
    "ore_rich",
    "volatiles_trace",
    "dark",
    "sol_loose_bioweapon",
    "sol_insurgent_network_desperate",
    "ruins_widespread"
});

// Titania
PlanetAPI Titania = system.addPlanet("Titania", Uranus, "Titania", "frozen", 233, 
calc.getSize(1577f), calc.getDistUranus(0.002914f), calc.getTimeGiant(8.706f));

Titania.getSpec().setTexture("graphics/planets/titania_tx.jpg");
Titania.getSpec().setAtmosphereThickness(0f);
Titania.getSpec().setAtmosphereColor(new Color(0,0,0,0));
Titania.getSpec().setIconColor(new Color(180, 180, 170, 255));
Titania.getSpec().setTilt(97.8f);
Titania.getSpec().setPitch(0f);
Titania.getSpec().setRotation(-(360f / (calc.getTimeGiant(8.706f) * 10f)));
Titania.applySpecChanges();

calc.addConditions(Titania.getMarket(), new String[] {
    "very_cold",
    "no_atmosphere",
    "ore_sparse",
    "volatiles_plentiful",
    "ruins_extensive",
    "sol_loose_bioweapon",
    "dark",
    "sol_unexploded_ordnance",
    "sol_lvmh_hq",
    "sol_insurgent_network_desperate"
});

// Oberon
float dist_Oberon = calc.getDistUranus(0.003901f); 
PlanetAPI Oberon = system.addPlanet("Oberon", Uranus, "Oberon", "frozen", 23, 
calc.getSize(1523f), dist_Oberon, calc.getTimeGiant(13.463f));

Oberon.getSpec().setTexture("graphics/planets/oberon_tx.jpg");
Oberon.getSpec().setAtmosphereThickness(0f);
Oberon.getSpec().setAtmosphereColor(new Color(0,0,0,0));
Oberon.getSpec().setIconColor(new Color(160, 140, 140, 255));
Oberon.getSpec().setTilt(97.8f);
Oberon.getSpec().setPitch(0f);
Oberon.getSpec().setRotation(-(360f / (calc.getTimeGiant(13.463f) * 10f)));
Oberon.applySpecChanges();

calc.addConditions(Oberon.getMarket(), new String[] {
    "very_cold",
    "ore_sparse",
    "ruins_vast",
    "sol_unexploded_ordnance",
    "dark",
    "no_atmosphere",
    "volatiles_diffuse",
    "sol_degenerate",
    "sol_insurgent_network_desperate",
    "sol_ai_terminators"
});

// =========================================================================
// ===================== URANUS IRREGULAR MOONS ============================
// =========================================================================

// Caliban
SectorEntityToken Caliban = calc.spawnIrregularBody2(system, Uranus, "Caliban", "Caliban", "moon", showNameMinor, 15f, 0.047908f, 0.2f, 241.2f, 349.7f, 2000.771f, zeroDegGlobal, null, 0.00004366f, "Uranus", true);
// Sycorax
SectorEntityToken Sycorax = calc.spawnIrregularBody2(system, Uranus, "Sycorax", "Sycorax", "moon", showNameMinor, 330f, 0.081507f, 0.52f, 332.1f, 25.4f, 2002.612f, zeroDegGlobal, null, 0.00004366f, "Uranus", true);
// SectorEntityToken sycoraxField = system.addTerrain(Terrain.ASTEROID_FIELD, new AsteroidFieldTerrainPlugin.AsteroidFieldParams(50f, 50f, 5, 5, 3f, 7f, "Sycorax remnants"));
// sycoraxField.setOrbit(Sycorax.getOrbit().makeCopy());

if(!uranusShortlist){
    // Margaret
    SectorEntityToken Margaret = calc.spawnIrregularBody2(system, Uranus, "Margaret", "Margaret", "moon", showNameMinor, 20.0f, 0.096425f, 0.642f, 115.9f, 90.7f, 1999.989f, zeroDegGlobal, null, 0.00004366f, "Uranus", false);
    // Prospero
    SectorEntityToken Prospero = calc.spawnIrregularBody2(system, Uranus, "Prospero", "Prospero", "moon", showNameMinor, 50.0f, 0.108431f, 0.441f, 197.6f, 180.4f, 2004.872f, zeroDegGlobal, null, 0.00004366f, "Uranus", true);
    // Setebos
    SectorEntityToken Setebos = calc.spawnIrregularBody2(system, Uranus, "Setebos", "Setebos", "moon", showNameMinor, 47.0f, 0.117113f, 0.579f, 148.0f, 356.1f, 2004.122f, zeroDegGlobal, null, 0.00004366f, "Uranus", true);
    // Ferdinand
    SectorEntityToken Ferdinand = calc.spawnIrregularBody2(system, Uranus, "Ferdinand", "Ferdinand", "moon", showNameMinor, 21.0f, 0.136509f, 0.395f, 172.3f, 166.9f, 2004.747f, zeroDegGlobal, null, 0.00004366f, "Uranus", true);
    // Stephano
    SectorEntityToken Stephano = calc.spawnIrregularBody2(system, Uranus, "Stephano", "Stephano", "moon", showNameMinor, 32.0f, 0.053152f, 0.235f, 164.4f, 17.1f, 2000.995f, zeroDegGlobal, null, 0.00004366f, "Uranus", true);
    // Trinculo
    SectorEntityToken Trinculo = calc.spawnIrregularBody2(system, Uranus, "Trinculo", "Trinculo", "moon", showNameMinor, 18.0f, 0.056836f, 0.22f, 55.6f, 162.2f, 2001.119f, zeroDegGlobal, null, 0.00004366f, "Uranus", true);
    // Francisco
    SectorEntityToken Francisco = calc.spawnIrregularBody2(system, Uranus, "Francisco", "Francisco", "moon", showNameMinor, 22.0f, 0.028581f, 0.144f, 288.4f, 137.6f, 2000.207f, zeroDegGlobal, null, 0.00004366f, "Uranus", true);
    // S/2023 U1
    SectorEntityToken S2023_U1 = calc.spawnIrregularBody2(system, Uranus, "S2023_U1", "S2023_U1", "moon", showNameProv, 8.0f, 0.05332f, 0.25f, 101.8f, 158.7f, 2001.348f, zeroDegGlobal, null, 0.00004366f, "Uranus", true);
}