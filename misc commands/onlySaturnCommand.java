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
// =========================== SATURN SYSTEM ===============================
// =========================================================================

// ## JUPITER (The Primary)
// Jupiter
float dist_SaturnRaw = 9.5826f;
PlanetAPI Saturn = system.addPlanet("Saturn", star, "Saturn", "gas_giant", 0f, calc.getSize(120536f), 10000f, 100000f);
float angleSaturn = Saturn.getCircularOrbitAngle();
float sz_Saturn = Saturn.getRadius();
float dist_Saturn = Saturn.getCircularOrbitRadius();
float p_Saturn = Saturn.getCircularOrbitPeriod();

Saturn.getSpec().setTexture("graphics/planets/saturn_tx.jpg"); 
Saturn.getSpec().setAtmosphereThickness(0.3f); 
Saturn.getSpec().setAtmosphereThicknessMin(20f); 
Saturn.getSpec().setAtmosphereColor(new Color(210, 190, 140, 100)); 
Saturn.getSpec().setIconColor(new Color(230, 200, 120, 255)); 
Saturn.getSpec().setTilt(30f); Saturn.getSpec().setPitch(63); 
Saturn.getSpec().setRotation(20.0f);
Saturn.getSpec().setCloudTexture("graphics/planets/clouds_banded01.png"); 
Saturn.getSpec().setCloudColor(new Color(255, 255, 230, 60)); 
Saturn.getSpec().setCloudRotation(calc.getRot(.445f)); 
Saturn.applySpecChanges();

calc.addConditions(Saturn.getMarket(), new String[] {
    "irradiated",
    "high_gravity",
    "dense_atmosphere",
    "poor_light",
    "volatiles_plentiful",
    "cold",
    "extreme_weather"
});
// =========================================================================
// SATURN RINGS
// =========================================================================

// Calibration: Encke gap px 7515 = 133,589 km, Keeler gap px 7860 = 136,505 km
// → 8.452 km/px, px 0 = 70,070 km, px 8389 = 140,975 km
float satRingGameStart = calc.getDistSaturn(0.000168f);
float satRingGameEnd   = calc.getDistSaturn(0.000942f);
float w_satRings = satRingGameEnd - satRingGameStart;
float r_satRings = (satRingGameStart + satRingGameEnd) / 2f;

calc.smartRingTex(system, Saturn, "sol_rings", "saturn_rings0", 8389, 0, satRingGameStart, satRingGameEnd, 30);
calc.setSatRingScale(satRingGameStart, w_satRings);

// SATURN MAGNETOSPHERE
float satMagOuter = calc.getSatRingPos(66900f) - 60f;
SectorEntityToken saturnField = system.addTerrain(Terrain.MAGNETIC_FIELD, new MagneticFieldTerrainPlugin.MagneticFieldParams(
    satMagOuter, satMagOuter / 2f, Saturn, sz_Saturn * 0.9f, sz_Saturn * 1.7f, new Color(230, 200, 120, 10), .5f));
saturnField.setCircularOrbit(Saturn, 0, 0, 100);
saturnField.setName("Saturn's Magnetosphere");

// Ring System terrain (D Ring → F Ring)
calc.smartRingTerrain(system, Saturn, "Saturn Ring System", calc.getSatRingPos(66900f), calc.getSatRingPos(140180f), 0.40f);

// F Ring visual boost
calc.smartRingTex(system, Saturn, "sol_rings", "rings_alpha3", 256, 3, calc.getSatRingPos(140180f) - 3.5f, calc.getSatRingPos(140180f) + 3.5f, 30);

// =========================================================================
// INNER MOONS
// =========================================================================

if(!saturnShortlist){
    // S/2009 S 1
    calc.spawnMoon(system, Saturn, "S/2009 S 1", calc.getSize(0.3f), calc.getSatRingPos(117000f), calc.getTimeGiant(0.47f), 10f, showProvisionalNames);
}

// Pan
SectorEntityToken Pan = system.addCustomEntity("Pan", "Pan", "Pan", "neutral"); 
Pan.setCircularOrbitPointingDown(Saturn, 45f, calc.getSatRingPos(133584f), calc.getTimeGiant(0.575f));

if(!saturnShortlist){
    // Daphnis
    calc.spawnMoon(system, Saturn, "Daphnis", calc.getSize(8f), calc.getSatRingPos(136505f), calc.getTimeGiant(0.594f), 90f, showMinorNames);
}

// Atlas
SectorEntityToken Atlas = system.addCustomEntity("Atlas", "Atlas", "Atlas", "neutral"); 
Atlas.setCircularOrbitPointingDown(Saturn, 135f, calc.getSatRingPos(137670f), calc.getTimeGiant(0.601f));

// In a fair and just world Janus with 10x the mass of prometheus and the cool co-moon situation with epimetheus would get to be the planet and prometheus would just be another asteroid, but life isn't fair and prometheus got a good name, while Janus just sounds like some guy.

// Prometheus
float p_Prometheus = calc.getTimeGiant(0.612f);

PlanetAPI Prometheus = system.addPlanet("Prometheus", Saturn, "Prometheus", "barren", 180f, calc.getSize(86f), calc.getSatRingPos(139380f), p_Prometheus);

Prometheus.getSpec().setTexture("graphics/planets/prometheus_tx.jpg");
Prometheus.getSpec().setAtmosphereThickness(0f);
Prometheus.getSpec().setAtmosphereThicknessMin(10f);
Prometheus.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
Prometheus.getSpec().setIconColor(new Color(150, 150, 160, 255));
Prometheus.getSpec().setTilt(26.7f);
Prometheus.getSpec().setPitch(90f);
Prometheus.getSpec().setRotation(-360f / (p_Prometheus * 10f));
Prometheus.applySpecChanges();

calc.addConditions(Prometheus.getMarket(), new String[] {
    "very_cold",
    "no_atmosphere",
    "low_gravity",
    "ore_sparse",
    "volatiles_abundant",
    "poor_light",
    "irradiated",
    "sol_antimatter_infrastructure",
    "sol_meteoroids"
});
// END OF SHEPHERD MOONS ===============================================

// Pandora
SectorEntityToken Pandora = system.addCustomEntity("Pandora", "Pandora", "Pandora", "neutral"); 
Pandora.setCircularOrbitPointingDown(Saturn, 225f, calc.getDistSaturn(0.000947f), calc.getTimeGiant(0.628f));

// Some Day I will figure out horseshoe orbits with circles, then I will come for janus and epimethius >:|
float p_JanusEpimetheus = calc.getTimeGiant(0.694f);
float dist_JanusEpimetheus = calc.getDistSaturn(0.001013f);

// Epimetheus
SectorEntityToken Epimetheus = system.addCustomEntity("Epimetheus", "Epimetheus", "Epimetheus", "neutral"); 
Epimetheus.setCircularOrbitPointingDown(Saturn, 270f, dist_JanusEpimetheus, p_JanusEpimetheus);

// Janus
SectorEntityToken Janus = system.addCustomEntity("Janus", "Janus", "Janus", "neutral"); 
Janus.setCircularOrbitPointingDown(Saturn, 280f, dist_JanusEpimetheus, p_JanusEpimetheus);

if(!saturnShortlist){
    // Aegaeon
    calc.spawnMoon(system, Saturn, "Aegaeon", calc.getSize(0.7f), calc.getDistSaturn(0.001120f), calc.getTimeGiant(0.808f), 315f, showMinorNames);
}

// ADDITIONAL RINGS ========================================================
// G Ring — Real: 166,000–175,000 km
calc.smartRingTex(system, Saturn, "sol_rings", "rings_alpha0", 256, 0, calc.getDistSaturn(0.001109f), calc.getDistSaturn(0.001170f), 30);

// E Ring — Real: 180,000–480,000 km
calc.smartRingTex(system, Saturn, "sol_rings", "rings_alpha0", 256, 1, calc.getDistSaturn(0.001203f), calc.getDistSaturn(0.003209f), 30);

// =========================================================================
// MAJOR MOONS
// =========================================================================

// Mimas
float p_Mimas = calc.getTimeGiant(0.942f);
PlanetAPI Mimas = (PlanetAPI) calc.spawnIrregularBody3(system, Saturn, "Mimas", "Mimas", "barren-bombarded", null, 396f, 0.001243f, 0.02f, 275.3f, 160.4f, 2000.0f, zeroDegGlobal, null, 0.0002857f, p_Mimas, "Saturn", false);

Mimas.getSpec().setTexture("graphics/planets/mimas_tx.jpg");
Mimas.getSpec().setAtmosphereThickness(0f);
Mimas.getSpec().setAtmosphereThicknessMin(10f);
Mimas.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
Mimas.getSpec().setIconColor(new Color(180, 180, 190, 255));
Mimas.getSpec().setTilt(26.7f);
Mimas.getSpec().setPitch(90f);
Mimas.getSpec().setRotation(-360f / (p_Mimas * 10f));
Mimas.applySpecChanges();

calc.addConditions(Mimas.getMarket(), new String[] {
    "very_cold",
    "low_gravity",
    "no_atmosphere",
    "poor_light",
    "ore_abundant",
    "rare_ore_sparse",
    "volatiles_diffuse",
    "ruins_scattered",
    "irradiated"
});
// Methone's smaller but better photographed than Pallene, it is the most famous of the hydrostatic equilibrium trolls
// Methone
SectorEntityToken Methone = system.addCustomEntity("Methone", "Methone", "Methone", "neutral"); 
Methone.setCircularOrbitPointingDown(Saturn, 60f, calc.getDistSaturn(0.00135f), p_Mimas * (15f/14f));

if(!saturnShortlist){
    // Anthe
    calc.spawnMoon(system, Saturn, "Anthe", calc.getSize(2f), calc.getDistSaturn(0.00138f), p_Mimas * (11f/10f), 100f, showMinorNames);
    // Pallene
    calc.spawnMoon(system, Saturn, "Pallene", calc.getSize(5f), calc.getDistSaturn(0.00142f), p_Mimas * (7f/6f), 140f, showMinorNames);
}

// Enceladus
float p_Enceladus = calc.getTimeGiant(1.37f);
float sz_Enceladus = calc.getSize(504f);
PlanetAPI Enceladus = system.addPlanet("Enceladus", Saturn, "Enceladus", "cryovolcanic", 190, sz_Enceladus, calc.getDistSaturn(0.0016f), p_Enceladus);

Enceladus.getSpec().setTexture("graphics/planets/enceladus_tx.jpg");
Enceladus.getSpec().setAtmosphereThickness(0.05f);
Enceladus.getSpec().setAtmosphereThicknessMin(10f);
Enceladus.getSpec().setAtmosphereColor(new Color(200, 240, 255, 50));
Enceladus.getSpec().setIconColor(new Color(240, 250, 255, 255));
Enceladus.getSpec().setTilt(26.7f);
Enceladus.getSpec().setPitch(90f);
Enceladus.getSpec().setRotation(-360f / (p_Enceladus * 10f));
Enceladus.getSpec().setCloudTexture("graphics/planets/clouds_white.png");
Enceladus.getSpec().setCloudColor(new Color(255, 255, 255, 40));
Enceladus.getSpec().setCloudRotation(-6.0f);
Enceladus.applySpecChanges();

calc.addConditions(Enceladus.getMarket(), new String[] {
    "volatiles_plentiful",
    "low_gravity",
    "no_atmosphere",
    "very_cold",
    "poor_light",
    "tectonic_activity",
    "sol_degenerate",
    "ruins_widespread",
    "pollution"
});
// ## TETHYS SYSTEM (Trojan Setup)------------------------------------------
// Tethys
float angleTethys = 30f;
float dist_Tethys = calc.getDistSaturn(0.0019f);
float p_Tethys = p_Mimas * 2f;

// Scale factor for bean orbits (Approx 10% of orbit radius)
float saturn_AU_Tethys = dist_Tethys * 0.1f;

PlanetAPI Tethys = system.addPlanet("Tethys", Saturn, "Tethys", "frozen", angleTethys, calc.getSize(1062f), dist_Tethys, p_Tethys);

Tethys.getSpec().setTexture("graphics/planets/tethys_tx.jpg");
Tethys.getSpec().setAtmosphereThickness(0f);
Tethys.getSpec().setAtmosphereThicknessMin(10f);
Tethys.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
Tethys.getSpec().setIconColor(new Color(200, 200, 210, 255));
Tethys.getSpec().setTilt(26.7f);
Tethys.getSpec().setPitch(90f);
Tethys.getSpec().setRotation(-360f / (p_Tethys * 10f));
Tethys.applySpecChanges();

if (!isSettled) {
calc.addConditions(Tethys.getMarket(), new String[] {
    "very_cold",
    "low_gravity",
    "no_atmosphere",
    "poor_light",
    "volatiles_abundant",
    "ore_sparse",
    "ruins_scattered"
});} else {
    Tethys.getMarket().addCondition("sol_porus");
}

SectorEntityToken Telesto = system.addCustomEntity("Telesto", "Telesto", "Telesto", "neutral"); 
Telesto.setCircularOrbitPointingDown(Saturn, angleTethys + 60f, dist_Tethys, p_Tethys);

SectorEntityToken Calypso = system.addCustomEntity("Calypso", "Calypso", "Calypso", "neutral"); 
Calypso.setCircularOrbitPointingDown(Saturn, angleTethys - 60f, dist_Tethys, p_Tethys);

// ## DIONE SYSTEM (Trojan Setup)-------------------------------------------
// Dione
float angleDione = 200f;
float dist_Dione = calc.getDistSaturn(0.0025f);
float p_Dione = p_Enceladus * 2f;

float saturn_AU_Dione = dist_Dione * 0.1f;

PlanetAPI Dione = system.addPlanet("Dione", Saturn, "Dione", "frozen", angleDione, calc.getSize(1123f), dist_Dione, p_Dione);

Dione.getSpec().setTexture("graphics/planets/dione_tx.jpg");
Dione.getSpec().setAtmosphereThickness(0f);
Dione.getSpec().setAtmosphereThicknessMin(10f);
Dione.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
Dione.getSpec().setIconColor(new Color(200, 200, 210, 255));
Dione.getSpec().setTilt(26.7f);
Dione.getSpec().setPitch(90f);
Dione.getSpec().setRotation(-360f / (p_Dione * 10f)); 
Dione.applySpecChanges();

if (!isSettled) {
calc.addConditions(Dione.getMarket(), new String[] {
    "very_cold",
    "low_gravity",
    "no_atmosphere",
    "poor_light",
    "volatiles_plentiful",
    "ore_sparse",
    "ruins_scattered",
    "tectonic_activity"
});} else {
    Dione.getMarket().addCondition("sol_porus");
}

// --- L4 Trojan: Helene ---
SectorEntityToken Helene = system.addCustomEntity("Helene", "Helene", "Helene", "neutral"); 
Helene.setCircularOrbitPointingDown(Saturn, angleDione + 60f, dist_Dione, p_Dione);

// --- L5 Trojan: Polydeuces ---
SectorEntityToken Polydeuces = system.addCustomEntity("Polydeuces", "Polydeuces", "Polydeuces", "neutral"); 
Polydeuces.setCircularOrbitPointingDown(Saturn, angleDione - 60f, dist_Dione, p_Dione);

// Rhea
PlanetAPI Rhea = system.addPlanet("Rhea", Saturn, "Rhea", "frozen", 10, 
calc.getSize(1527f), calc.getDistSaturn(0.0035f), calc.getTimeGiant(4.518f));

Rhea.getSpec().setTexture("graphics/planets/rhea_tx.jpg");
Rhea.getSpec().setIconColor(new Color(180,180,190,255));
Rhea.getSpec().setAtmosphereThickness(0f);
Rhea.getSpec().setAtmosphereColor(new Color(0,0,0,0));
Rhea.getSpec().setTilt(26.7f);
Rhea.getSpec().setPitch(90f);
Rhea.getSpec().setRotation(-(360f / (calc.getTimeGiant(4.518f) * 10f)));
Rhea.applySpecChanges();

calc.addConditions(Rhea.getMarket(), new String[] {
    "no_atmosphere",
    "low_gravity",
    "very_cold",
    "ore_moderate",
    "volatiles_diffuse",
    "ruins_extensive",
    "poor_light",
    "sol_ai_terminators",
    "sol_loose_bioweapon",
    "sol_insurgent_network_desperate"
});
// ## TITAN SYSTEM----------------------------------------------------------
// Titan
float sz_Titan = calc.getSize(5150f);
float p_Titan = calc.getTimeGiant(15.945f);
float dist_Titan = calc.getDistSaturn(0.0081f);

PlanetAPI Titan = system.addPlanet("Titan", Saturn, "Titan", "frozen", 120, sz_Titan, dist_Titan, p_Titan);

Titan.getSpec().setTexture("graphics/planets/titan_tx.jpg"); 
Titan.getSpec().setAtmosphereThickness(1.45f); 
Titan.getSpec().setAtmosphereColor(new Color(255, 160, 50, 200)); 
Titan.getSpec().setCloudTexture("graphics/planets/clouds_white.png"); 
Titan.getSpec().setCloudColor(new Color(255, 200, 100, 100)); 
Titan.getSpec().setCloudRotation(calc.getRot(.375f)); 
Titan.getSpec().setIconColor(new Color(220, 150, 50, 255)); 
Titan.getSpec().setTilt(26.7f); 
Titan.getSpec().setPitch(90f); 
Titan.getSpec().setRotation(calc.getRot(.375f)); // Atmosphere is texture and Titan superrotates
Titan.applySpecChanges();

calc.addConditions(Titan.getMarket(), new String[] {
    "very_cold",
    "organics_trace",
    "volatiles_abundant",
    "ore_moderate",
    "rare_ore_moderate",
    "ruins_vast",
    "decivilized",
    "solar_array",
    "sol_ice_storms",
    "sol_space_ladder",
    "habitable"
});
if(generateElevators){
    SectorEntityToken titanelevator = system.addCustomEntity("titan_elevator", "Titan elevator", "elevator3", "neutral");
    titanelevator .setCircularOrbitPointingDown(Titan, 120 - 180, sz_Titan + 140, p_Titan);
}

// ==========================================
// TITAN ORBITAL MIRRORS
// ==========================================
// 5 Mirrors for atmospheric heating/illumination
float mirrorRad = sz_Titan * 1.7f;
float mirrorPeriod = 8f;

// Titan Mirror Alpha
SectorEntityToken TitanMirrorAlpha = system.addCustomEntity(null, "Titan Mirror Alpha", "stellar_mirror", "neutral");
TitanMirrorAlpha.setCircularOrbitPointingDown(Titan, 0f, mirrorRad, mirrorPeriod);
TitanMirrorAlpha.setDiscoverable(true);
TitanMirrorAlpha.setSensorProfile(1000f);

// Titan Mirror Beta
SectorEntityToken TitanMirrorBeta = system.addCustomEntity(null, "Titan Mirror Beta", "stellar_mirror", "neutral");
TitanMirrorBeta.setCircularOrbitPointingDown(Titan, 72f, mirrorRad, mirrorPeriod);
TitanMirrorBeta.setDiscoverable(true);
TitanMirrorBeta.setSensorProfile(1000f);

// Titan Mirror Gamma
SectorEntityToken TitanMirrorGamma = system.addCustomEntity(null, "Titan Mirror Gamma", "stellar_mirror", "neutral");
TitanMirrorGamma.setCircularOrbitPointingDown(Titan, 144f, mirrorRad, mirrorPeriod);
TitanMirrorGamma.setDiscoverable(true);
TitanMirrorGamma.setSensorProfile(1000f);

// Titan Mirror Delta
SectorEntityToken TitanMirrorDelta = system.addCustomEntity(null, "Titan Mirror Delta", "stellar_mirror", "neutral");
TitanMirrorDelta.setCircularOrbitPointingDown(Titan, 216f, mirrorRad, mirrorPeriod);
TitanMirrorDelta.setDiscoverable(true);
TitanMirrorDelta.setSensorProfile(1000f);

// Titan Mirror Epsilon
SectorEntityToken TitanMirrorEpsilon = system.addCustomEntity(null, "Titan Mirror Epsilon", "stellar_mirror", "neutral");
TitanMirrorEpsilon.setCircularOrbitPointingDown(Titan, 288f, mirrorRad, mirrorPeriod);
TitanMirrorEpsilon.setDiscoverable(true);
TitanMirrorEpsilon.setSensorProfile(1000f);

// TODO repair
// SectorEntityToken titanSun = system.addCustomEntity("titan_sun", "The Titanean Sun", "derelict_vambrace", "neutral");
// titanSun.setCircularOrbitPointingDown(Saturn, 120, dist_Titan * 0.9f, p_Titan);

// Hyperion
// Hyperion might not have the most hazard, but it is the most useless
// Fucking useless piece of shit that zapped Cassini.
PlanetAPI Hyperion = (PlanetAPI) calc.spawnIrregularBody3(system, Saturn, "Hyperion", "Hyperion", "barren", null, 266f, 0.009903f, 0.105f, 122.9f, 214.0f, 1999.986f, zeroDegGlobal, null, 0.0002857f,  p_Titan * (4f/3f), "Saturn", false);
 
Hyperion.getSpec().setTexture("graphics/planets/hyperion_tx.jpg");
Hyperion.getSpec().setAtmosphereThickness(0f);
Hyperion.getSpec().setAtmosphereColor(new Color(0,0,0,0));
Hyperion.getSpec().setIconColor(new Color(180, 160, 140, 255));
Hyperion.getSpec().setTilt(26.7f);
Hyperion.getSpec().setPitch(45f);
Hyperion.getSpec().setRotation(calc.getRot(13f));
Hyperion.applySpecChanges();
 
calc.addConditions(Hyperion.getMarket(), new String[] {
    "no_atmosphere",
    "low_gravity",
    "very_cold",
    "poor_light",
    "sol_porus",
    "sol_static_charge"
});
Hyperion.setCustomDescriptionId("sol_hyperion");

// Iapetus
PlanetAPI Iapetus = system.addPlanet("Iapetus", Saturn, "Iapetus", "frozen", 50f, calc.getSize(1469f), calc.getDistSaturn(.0286f), calc.getTimeGiant(79.321f));

Iapetus.getSpec().setTexture("graphics/planets/iapetus_tx.jpg");
Iapetus.getSpec().setAtmosphereThickness(0f);
Iapetus.getSpec().setAtmosphereColor(new Color(0,0,0,0));
Iapetus.getSpec().setIconColor(new Color(150, 150, 150, 255));
Iapetus.getSpec().setTilt(26.7f);
Iapetus.getSpec().setPitch(75f);
Iapetus.getSpec().setRotation(-(360f / (calc.getTime(79.321f) * 10f)));
Iapetus.applySpecChanges();

if (!isSettled) {
calc.addConditions(Iapetus.getMarket(), new String[] {
    "no_atmosphere",
    "low_gravity",
    "very_cold",
    "ore_moderate",
    "volatiles_trace",
    "organics_common",
    "poor_light",
    "ruins_extensive"
});

} else {
    Iapetus.getMarket().addCondition("sol_porus");
}

// =========================================================================
// ===================== SATURN IRREGULAR MOONS ============================
// =========================================================================

// Phoebe Ring — Real: 6,000,000–16,000,000 km (retrograde)
calc.smartRingTex(system, Saturn, "sol_rings", "rings_alpha0", 256, 1, calc.getDistSaturn(0.040f), calc.getDistSaturn(0.107f), -calc.getTime(550f));

// Phoebe
PlanetAPI Phoebe = (PlanetAPI) calc.spawnIrregularBody2(system, Saturn, "Phoebe", "Phoebe", "barren-bombarded", null, 213f, 0.086428f, 0.164f, 308.0f, 240.3f, 2000.806f, zeroDegGlobal, null, 0.0002857f, "Saturn", true);
 
Phoebe.getSpec().setTexture("graphics/planets/phoebe_tx.jpg");
Phoebe.getSpec().setAtmosphereThickness(0f);
Phoebe.getSpec().setAtmosphereColor(new Color(0,0,0,0));
Phoebe.getSpec().setIconColor(new Color(80, 80, 90, 255));
Phoebe.getSpec().setTilt(26.7f);
Phoebe.getSpec().setPitch(60f);
Phoebe.getSpec().setRotation(-calc.getRot(.387f));
Phoebe.applySpecChanges();
 
calc.addConditions(Phoebe.getMarket(), new String[] {
    "very_cold",
    "poor_light",
    "low_gravity",
    "no_atmosphere",
    "ruins_widespread",
    "ore_sparse",
    "volatiles_diffuse",
    "sol_jump_point_nearby",
    "sol_meteoroids"
});
 
JumpPointAPI jp_phoebe = Global.getFactory().createJumpPoint("jp_phoebe", "Phoebe Jump Point");
jp_phoebe.setStandardWormholeToHyperspaceVisual();
jp_phoebe.setCircularOrbit(Phoebe, 0, 100, 10);
system.addEntity(jp_phoebe);

// Inuit Group ------------------------------------------------------------

// Siarnaq
SectorEntityToken Siarnaq = calc.spawnIrregularBody2(system, Saturn, "Siarnaq", "Siarnaq", "moon", showNameMinor, 39.3f, 0.119528f, 0.308f, 62.7f, 68.4f, 1999.566f, zeroDegGlobal, null, 0.0002857f, "Saturn", false);

// Kiviuq
SectorEntityToken Kiviuq = calc.spawnIrregularBody2(system, Saturn, "Kiviuq", "Kiviuq", "custom_entity", "Kiviuq", 17.0f, 0.075585f, 0.275f, 183.2f, 91.8f, 1998.796f, zeroDegGlobal, null, 0.0002857f, "Saturn", false);

if(!saturnShortlist){
    // Ijiraq
    SectorEntityToken Ijiraq = calc.spawnIrregularBody2(system, Saturn, "Ijiraq", "Ijiraq", "moon", showNameMinor, 13.0f, 0.075835f, 0.293f, 83.4f, 68.6f, 1999.477f, zeroDegGlobal, null, 0.0002857f, "Saturn", false);
    // Paaliaq
    SectorEntityToken Paaliaq = calc.spawnIrregularBody2(system, Saturn, "Paaliaq", "Paaliaq", "moon", showNameMinor, 11.7f, 0.100253f, 0.378f, 318.2f, 237.6f, 1998.158f, zeroDegGlobal, null, 0.0002857f, "Saturn", false);
    // Tarvos
    SectorEntityToken Tarvos = calc.spawnIrregularBody2(system, Saturn, "Tarvos", "Tarvos", "moon", showNameMinor, 15.0f, 0.12177f, 0.522f, 64.5f, 282.9f, 1999.332f, zeroDegGlobal, null, 0.0002857f, "Saturn", false);
}


// Norse Group ------------------------------------------------------------

if(!saturnShortlist){
    // Skathi
    SectorEntityToken Skathi = calc.spawnIrregularBody2(system, Saturn, "Skathi", "Skathi", "moon", showNameMinor, 8.0f, 0.104115f, 0.281f, 269.3f, 202.3f, 2001.568f, zeroDegGlobal, null, 0.0002857f, "Saturn", true);
    // Bestla
    SectorEntityToken Bestla = calc.spawnIrregularBody2(system, Saturn, "Bestla", "Bestla", "moon", showNameMinor, 7.0f, 0.13595f, 0.486f, 33.1f, 85.6f, 2002.331f, zeroDegGlobal, null, 0.0002857f, "Saturn", true);
    // Ymir
    SectorEntityToken Ymir = calc.spawnIrregularBody2(system, Saturn, "Ymir", "Ymir", "moon", showNameMinor, 19.0f, 0.153449f, 0.338f, 167.5f, 45.3f, 2002.02f, zeroDegGlobal, null, 0.0002857f, "Saturn", true);
    // Fornjot
    SectorEntityToken Fornjot = calc.spawnIrregularBody2(system, Saturn, "Fornjot", "Fornjot", "moon", showNameMinor, 6.0f, 0.166692f, 0.213f, 53.5f, 321.9f, 2003.011f, zeroDegGlobal, null, 0.0002857f, "Saturn", true);
}

// Gallic Group ------------------------------------------------------------

// Albiorix
SectorEntityToken Albiorix = calc.spawnIrregularBody2(system, Saturn, "Albiorix", "Albiorix", "moon", showNameMinor, 28.6f, 0.109154f, 0.482f, 155.0f, 62.8f, 1999.342f, zeroDegGlobal, null, 0.0002857f, "Saturn", false);

if(!saturnShortlist){
    // Bebhionn
    SectorEntityToken Bebhionn = calc.spawnIrregularBody2(system, Saturn, "Bebhionn", "Bebhionn", "moon", showNameMinor, 6.0f, 0.11382f, 0.459f, 128.4f, 5.1f, 1998.753f, zeroDegGlobal, null, 0.0002857f, "Saturn", false);
}
if(saturnAll){
    new GiantMoonsTotal().spawn(system, Saturn,  "saturn",  angleSaturn);
}