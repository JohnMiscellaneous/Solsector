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

import data.scripts.world.systems.RemnantNexusFactory;
import data.scripts.world.systems.SolEconomies;
import data.scripts.world.systems.SolHyperspaceGen;
import data.scripts.world.systems.RemnantPatrolFactory;
import data.scripts.world.systems.GiantMoonsTotal;
import data.scripts.world.systems.cometsCentaursTNOs;
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

RemnantNexusFactory nexusFactory = new RemnantNexusFactory();
RemnantPatrolFactory patrolFactory = new RemnantPatrolFactory();

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

float solMapGridSize = 115000f; 
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

float rotMult = 4f;
try {
    rotMult = (float) Global.getSettings().loadJSON("data/config/sol_settings.json").optDouble("rotMult", 4f);
} catch (Exception e) {}
float progradeMult = -1f;
try {
    progradeMult = (float) Global.getSettings().loadJSON("data/config/sol_settings.json").optDouble("progradeMult", -1f);
} catch (Exception e) {}

// =========================================================================
// ========================== MERCURY SYSTEM ===============================
// =========================================================================

// ## MERCURY (The Primary)-------------------------------------------------

// Mercury | Semi Major Axis: 0.387 AU | Diameter: ~4,880 km | Period: 88 days | Eccentricity: 0.2056
// Spawn Mercury (Eccentric)
// args: system, primary, id, name, type, angle, a, e, diam, period
PlanetAPI Mercury = (PlanetAPI) calc.spawnSPSObject(system, star, "mercury", "Mercury", "barren_castiron", null, 4880f, 0.3871f, 0.2056f, 48.331f, 29.124f, 2026.06f, zeroDegGlobal, 58.646f, 1f);
float angleMercury = Mercury.getCircularOrbitAngle();
float sz_Mercury = Mercury.getRadius();
float p_Mercury = Mercury.getCircularOrbitPeriod();

Mercury.getSpec().setTexture("graphics/planets/mercury_tx.jpg"); 
Mercury.getSpec().setAtmosphereThickness(0f); 
Mercury.getSpec().setAtmosphereThicknessMin(10f); 
Mercury.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
Mercury.getSpec().setIconColor(new Color(140, 130, 120, 255)); 
Mercury.getSpec().setTilt(7.0f); 
Mercury.getSpec().setPitch(90f); 
Mercury.getSpec().setRotation(calc.getRot(58.6f)); 
Mercury.applySpecChanges();

calc.addConditions(Mercury.getMarket(), new String[] {
    "very_hot",
    "irradiated",
    "ore_ultrarich",
    "ruins_vast",
    "no_atmosphere",
    "sol_degenerate",
    "sol_megaforges",
    "sol_ancient_orbital_manufactories",
    "sol_space_ladder",
    "sol_ai_security_systems",
    "sol_oort_strikes",
    "sol_orbital_fleetworks"
});

if(mercuryCold){
    Mercury.getMarket().addCondition("poor_light");
    Mercury.getMarket().addCondition("very_cold");
}
// Mercury is cold and has poor light, despite being very close because mercury is nearly tidally locked. Nearly being very important. Because of that, days on mercury are extremely long and being on the surface of mercury means an extreme temperature variation which is represented by mercury being cold and having poor light.

// Mercury Mirror
// TODO repair
// SectorEntityToken mercuryShade = system.addCustomEntity("mercury_shade", "Mercury Mirror Alpha", "stellar_mirror", "neutral");
// mercuryShade.setCircularOrbitWithSpin(star, 60f, calc.getDist(.15f, star), calc.getTime(21.2f), 10000f, 10000f);
// mercuryShade.setDiscoverable(true);
// mercuryShade.setSensorProfile(1000f);

// ==========================================
// VULCAN INFRASTRUCTURE
// ==========================================

float p_Vulcan = calc.getTime(30f);

float dist_VulcanShunt = calc.getDist(0.1f, star);

SectorEntityToken VulcanShunt = system.addCustomEntity("VulcanHypershunt", "Vulcan Hypershunt", "coronal_tap", "neutral");
VulcanShunt.setCircularOrbitPointingDown(star, 0, dist_VulcanShunt, p_Vulcan);

VulcanShunt.setDiscoverable(true);
VulcanShunt.setSensorProfile(4000f);

// Vulcan Energy | Orbital Station
float dist_VulcanEnergy = dist_VulcanShunt + 130;
SectorEntityToken VulcanEnergy = system.addCustomEntity("VulcanEnergy", "Vulcan Station", "station_sporeship_derelict", "neutral");
VulcanEnergy.setCircularOrbitPointingDown(star, 0, dist_VulcanEnergy, calc.getTime(30f));

VulcanEnergy.setInteractionImage("illustrations", "abandoned_station2");
Misc.setAbandonedStationMarket("Vulcan_energy_market", VulcanEnergy);

VulcanEnergy.getMarket().setName("Vulcan Station");
VulcanEnergy.getMarket().addCondition("very_hot");
VulcanEnergy.getMarket().addCondition("sol_megaforges_hyperenergetic");
VulcanEnergy.getMarket().addCondition("sol_irradiated_extreme");
VulcanEnergy.getMarket().getMemoryWithoutUpdate().set("$surveyed", true);

for (MarketConditionAPI condition : VulcanEnergy.getMarket().getConditions()) {
    condition.setSurveyed(true);
}
VulcanEnergy.setCustomDescriptionId("sol_vulcan_station");

VulcanEnergy.setDiscoverable(true);
VulcanEnergy.setSensorProfile(4000f);

// =========================================================================
// =========================== VENUS SYSTEM ================================
// =========================================================================
// Venus
float dist_VenusRaw = 0.7233f;
PlanetAPI Venus = (PlanetAPI) calc.spawnSPSObject(system, star, "venus", "Venus", "toxic", null, 12104f, dist_VenusRaw, 0.0068f, 76.680f, 54.884f, 2025.14f, zeroDegGlobal, null, 1f);
float a_Venus = Venus.getCircularOrbitAngle();
float sz_Venus = Venus.getRadius();
float dist_Venus = Venus.getCircularOrbitRadius();
float p_Venus = Venus.getCircularOrbitPeriod();

calc.addConditions(Venus.getMarket(), new String[] {
    "toxic_atmosphere",
    "very_hot",
    "extreme_weather",
    "ruins_widespread",
    "tectonic_activity",
    "ore_rich",
    "organics_trace",
    "dense_atmosphere",
    "sol_penal_world"
});

if(mercuryCold){
    Venus.getMarket().addCondition("poor_light"); //poor light because venus is nearly tidally locked, atmosphere prevents cold
}

Venus.getSpec().setTexture("graphics/planets/venus_tx.jpg"); 
Venus.getSpec().setPlanetColor(new Color(255, 240, 200, 255)); 
Venus.getSpec().setAtmosphereThickness(0.4f); 
Venus.getSpec().setAtmosphereThicknessMin(20f); 
Venus.getSpec().setAtmosphereColor(new Color(220, 200, 150, 200)); 
Venus.getSpec().setCloudTexture("graphics/planets/clouds_banded01.png"); 
Venus.getSpec().setCloudColor(new Color(255, 240, 200, 150)); 
Venus.getSpec().setCloudRotation(-calc.getRot(4f)); 
Venus.getSpec().setIconColor(new Color(249, 220, 162, 255)); 
Venus.getSpec().setTilt(3.4f); 
Venus.getSpec().setPitch(87f); 
Venus.getSpec().setRotation(-calc.getRot(4f)); // The surface rotates MUUUUUUUUUUUUUUUUUUUUUUCH slower, but the texture is of the atmosphere, so 4days is what we do 
Venus.applySpecChanges();

// Vanera 4789 | Orbital Station
SectorEntityToken vanera = system.addCustomEntity("vanera_station", "Vanera 4789", "station_midline1", "neutral");
vanera.setCircularOrbitPointingDown(Venus, 180, sz_Venus * 2, calc.getTime(40f));

vanera.setInteractionImage("illustrations", "abandoned_station2");
Misc.setAbandonedStationMarket("vanera_market", vanera);

vanera.getMarket().setName("Vanera 4789");
vanera.getMarket().addCondition("abandoned_station");
vanera.getMarket().addCondition("decivilized");
vanera.getMarket().addCondition("very_hot");
vanera.getMarket().addCondition("sol_megaforges");

for (MarketConditionAPI condition : vanera.getMarket().getConditions()) {
    condition.setSurveyed(true);
}

vanera.setDiscoverable(true);
vanera.setSensorProfile(4000f);


// =========================================================================
// ==================== VENUS TROJANS & COMPANIONS =========================
// =========================================================================

// Zoozve | Quasi-moon
float map_1AU_at_Venus = (calc.getDist(1.223f, star));
SectorEntityToken Zoozve = calc.spawnSPSObject(system, star, "Zoozve", "Zoozve", "asteroid", showNameMinor, 0.23f, dist_VenusRaw, 0.4101f, 231.460f, 355.406f, 2025.94f, zeroDegGlobal, 0.563f, 1f);
Zoozve.setCustomDescriptionId("sol_zoozve");

// =========================================================================
// ============================ EARTH SYSTEM ===============================
// =========================================================================

// ## EARTH & LUNA (The Primary)--------------------------------------------

// Earth | Diameter: ~12,742 km
float dist_EarthRaw = 1f;
PlanetAPI Earth = (PlanetAPI) calc.spawnSPSObject(system, star, "Earth", "Earth", "terran", null, 12742f, dist_EarthRaw, 0f, 348.739f, 114.208f, 2026.01f, zeroDegGlobal, null, 1f);
float earthAngle = Earth.getCircularOrbitAngle();
float sz_Earth = Earth.getRadius();
float dist_Earth = Earth.getCircularOrbitRadius();
float p_Earth = Earth.getCircularOrbitPeriod();

Earth.getSpec().setTexture("graphics/planets/earth_tx.jpg"); 
Earth.getSpec().setAtmosphereThickness(0.5f); 
Earth.getSpec().setAtmosphereThicknessMin(20f); 
Earth.getSpec().setAtmosphereColor(new Color(100, 150, 255, 200)); 
Earth.getSpec().setIconColor(new Color(100, 150, 255, 255)); 
Earth.getSpec().setTilt(23.4f); 
Earth.getSpec().setPitch(71.6f); 
Earth.getSpec().setRotation(calc.getRot(1f)); 
Earth.getSpec().setCloudTexture("graphics/planets/clouds_terran03.png"); 
Earth.getSpec().setCloudColor(new Color(255, 255, 255, 50));
Earth.getSpec().setCloudRotation(10.0f); 
Earth.applySpecChanges();

MarketAPI marketEarth = Earth.getMarket();
calc.addConditions(marketEarth, new String[] {
    "habitable",
    "ruins_vast",
    "sol_civilised_world",
    "farmland_poor",
    "solar_array",
    "ore_moderate",
    "organics_plentiful",
    "sol_orbital_ring", //TODO damaged ring
    "sol_world_war"
});

// Earth ring:
if(generateElevators){
    Float Jarvis = 0f;
    SectorEntityToken elevatorEarth1 = system.addCustomEntity("EarthRing", "Earth", "ringearth", "neutral"); 
    elevatorEarth1.setCircularOrbitPointingDown(Earth, 0, 1, calc.getOrbRot(1f));
}

// Scaling factors for Earth-Moon system (Local scaling)
float _earthMoonExt = sz_Earth * 1.2f;
float _earthMoonInt = _earthMoonExt * 20f;

// Luna | Semi Major Axis: 384,400 km | Diameter: ~3,474 km | Period: 30 days
float lunaAngle = 33f;
float sz_Luna = calc.getSize(3474f);
float p_Luna = calc.getTime(30f);
float dist_Luna = sz_Earth + (_earthMoonExt * (float) Math.log(_earthMoonInt * 0.00257f + 1));

PlanetAPI Luna = system.addPlanet("Luna", Earth, "Luna", "barren", lunaAngle, sz_Luna, dist_Luna, p_Luna);

Luna.getSpec().setTexture("graphics/planets/luna_tx.jpg"); 
Luna.getSpec().setAtmosphereThickness(0f); 
Luna.getSpec().setAtmosphereThicknessMin(10f); 
Luna.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
Luna.getSpec().setIconColor(new Color(150, 150, 150, 255)); 
Luna.getSpec().setTilt(23.4f); 
Luna.getSpec().setPitch(88f); 
Luna.getSpec().setRotation(-(360f / (p_Luna * 10f))); 
Luna.applySpecChanges();

MarketAPI marketLuna = Luna.getMarket();
marketLuna.addCondition("no_atmosphere");
marketLuna.addCondition("low_gravity");
marketLuna.addCondition("ruins_vast");
marketLuna.addCondition("ore_moderate");
marketLuna.addCondition("rare_ore_sparse");
marketLuna.addCondition("volatiles_trace");
marketLuna.addCondition("decivilized");
marketLuna.addCondition("sol_megaforges");
marketLuna.addCondition("sol_space_ladder");
marketLuna.addCondition("solar_array");

if(generateElevators){
    SectorEntityToken elevatorluna = system.addCustomEntity("luna_ladder", "Luna ladder", "elevator1", "neutral");
    elevatorluna.setCircularOrbitPointingDown(Luna, lunaAngle - 180f, sz_Luna + 33f , p_Luna);
}

Luna.setCustomDescriptionId("sol_luna");

// ## EARTH LOCAL ENVIRONMENT-----------------------------------------------

// Van Allen Belts 
float vanAllenInner = sz_Earth + (_earthMoonExt * (float) Math.log(_earthMoonInt * 0.0003f + 1));
float vanAllenOuter = sz_Earth + (_earthMoonExt * (float) Math.log(_earthMoonInt * 0.0015f + 1));

SectorEntityToken earthField = system.addTerrain(Terrain.MAGNETIC_FIELD, new MagneticFieldTerrainPlugin.MagneticFieldParams(200f, 350f, Earth, vanAllenInner, vanAllenOuter, new Color(50, 20, 100, 50), .3f));
earthField.setName("Van Allen Belts");
earthField.setCircularOrbit(Earth, 0, 0, calc.getTime(10f));


// GEO Comm Relay
float geoCommRelayDist = sz_Earth + 80f;
SectorEntityToken earthRelay = system.addCustomEntity("earth_comm_relay", "GEO Comm Relay", "comm_relay", "remnant");
earthRelay.setCircularOrbitPointingDown(Earth, 0, geoCommRelayDist, calc.getOrbRot(1f));

earthRelay.setDiscoverable(true);

// ## EARTH-MOON LAGRANGE POINTS--------------------------------------------

// Tiangong 347 | Orbital Station | Luna L4
SectorEntityToken tiangong = system.addCustomEntity("tiangong_station", "Tiangong 347", "station_hightech1", "neutral");
tiangong.setCircularOrbitPointingDown(Earth, lunaAngle + 60f, dist_Luna, p_Luna);

tiangong.setInteractionImage("illustrations", "abandoned_station2");
Misc.setAbandonedStationMarket("tiangong_market", tiangong);

tiangong.getMarket().setName("Tiangong 347");
tiangong.getMarket().addCondition("abandoned_station");
tiangong.getMarket().addCondition("sol_no_atmosphere_bodgejob");

for (MarketConditionAPI condition : tiangong.getMarket().getConditions()) {
    condition.setSurveyed(true);
}

tiangong.setDiscoverable(true);
tiangong.setSensorProfile(4000f);

// International Space Station | Orbital Station | Luna L5
SectorEntityToken iss = system.addCustomEntity("iss_station", "Apollo station", "station_lowtech3", "neutral");
iss.setCircularOrbitPointingDown(Earth, lunaAngle - 60f, dist_Luna, p_Luna);

iss.setInteractionImage("illustrations", "abandoned_station2");
Misc.setAbandonedStationMarket("iss_market", iss);

iss.getMarket().setName("Apollo station");
iss.getMarket().addCondition("abandoned_station");
iss.getMarket().addCondition("decivilized");
iss.getMarket().addCondition("sol_megaforges");

for (MarketConditionAPI condition : iss.getMarket().getConditions()) {
    condition.setSurveyed(true);
}

iss.setDiscoverable(true);
iss.setSensorProfile(4000f);

// Sol Gate (Luna L3)
SectorEntityToken lunaL3 = system.addCustomEntity("luna_l3", "Sol Gate", "inactive_gate", "neutral");
lunaL3.setCircularOrbit(Earth, lunaAngle - 180, dist_Luna, p_Luna);

lunaL3.setDiscoverable(true);

// Luna Mirrors
SectorEntityToken LunaAlpha = system.addCustomEntity(null, "Luna Mirror Alpha", "stellar_mirror", "neutral");
LunaAlpha.setCircularOrbitPointingDown(Luna, 0, sz_Luna * 1.5f, 5);
LunaAlpha.setDiscoverable(true);
LunaAlpha.setSensorProfile(1000f);

SectorEntityToken LunaBeta = system.addCustomEntity(null, "Luna Mirror Beta", "stellar_mirror", "neutral");
LunaBeta.setCircularOrbitPointingDown(Luna, 120, sz_Luna * 1.5f, 5);
LunaBeta.setDiscoverable(true);
LunaBeta.setSensorProfile(1000f);

SectorEntityToken LunaGamma = system.addCustomEntity(null, "Luna Mirror Gamma", "stellar_mirror", "neutral");
LunaGamma.setCircularOrbitPointingDown(Luna, 240, sz_Luna * 1.5f, 5);
LunaGamma.setDiscoverable(true);
LunaGamma.setSensorProfile(1000f);

// =========================================================================
// ==================== EARTH QUASI-SATELLITES =============================
// =========================================================================
float map_1AU_at_Earth = 2 * (calc.getDist(1.5f, star));

// Kamooalewa | Quasi-moon of Earth
SectorEntityToken Kamooalewa = calc.spawnSPSObject(system, star, "Kamooalewa", "Kamooalewa", "asteroid", showNameMinor, 0.05f, dist_EarthRaw, 0.224f, 65.651f, 304.548f, 2025.76f, zeroDegGlobal, 0.019f, 1f);
Kamooalewa.setCustomDescriptionId("sol_kamooalewa");


// =========================================================================
// ==================== EARTH L4 TROJANS (LEADING) =========================
// =========================================================================

// Earth Nav Buoy (Anchor)
SectorEntityToken earthNavBuoy = system.addCustomEntity("earth_nav_buoy", "Earth Nav Buoy", "nav_buoy_makeshift", "remnant");
earthNavBuoy.setCircularOrbit(star, earthAngle + 60f, dist_Earth, p_Earth);
float ang_L4_Earth = earthAngle + 60f + 90f;
earthNavBuoy.setDiscoverable(true);

// SectorEntityToken EarthL4Field = system.addTerrain(Terrain.ASTEROID_FIELD, new AsteroidFieldTerrainPlugin.AsteroidFieldParams(75f, 75f, 2, 2, 3f, 3f, "2010 TK7 and 2020 XL5"));
// EarthL4Field.setCircularOrbit(star, earthAngle + 60f, dist_Earth, p_Earth);

// =========================================================================
// =================== EARTH-SUN LAGRANGE POINTS ===========================
// =========================================================================

// Distance roughly 1% of AU (Hill Sphere / L1/L2 region)
float LPointOrbitDistance = sz_Earth + (_earthMoonExt * (float) Math.log(_earthMoonInt * 0.012f + 1));

// ## L1 & L2 STRUCTURES

// Lagrange Mirror Beta (L1)
SectorEntityToken LagrangeMirrorBeta = system.addCustomEntity(null, "Lagrange Mirror Beta", "stellar_mirror", "neutral");
LagrangeMirrorBeta.setCircularOrbitPointingDown(Earth, earthAngle, LPointOrbitDistance, p_Earth);
LagrangeMirrorBeta.setDiscoverable(true);
LagrangeMirrorBeta.setSensorProfile(1000f);

// Lagrange Mirror Alpha (L1)
SectorEntityToken LagrangeMirrorAlpha = system.addCustomEntity(null, "Lagrange Mirror Alpha", "stellar_mirror", "neutral");
LagrangeMirrorAlpha.setCircularOrbitPointingDown(Earth, earthAngle - 6f, LPointOrbitDistance, p_Earth);
LagrangeMirrorAlpha.setDiscoverable(true);
LagrangeMirrorAlpha.setSensorProfile(1000f);

// Lagrange Mirror Gamma (L1)
SectorEntityToken LagrangeMirrorGamma = system.addCustomEntity(null, "Lagrange Mirror Gamma", "stellar_mirror", "neutral");
LagrangeMirrorGamma.setCircularOrbitPointingDown(Earth, earthAngle + 6f, LPointOrbitDistance, p_Earth);
LagrangeMirrorGamma.setDiscoverable(true);
LagrangeMirrorGamma.setSensorProfile(1000f);

// Lagrange Shade Beta (L2)
SectorEntityToken LagrangeShadeBeta = system.addCustomEntity(null, "Lagrange Shade Beta", "stellar_shade", "neutral");
LagrangeShadeBeta.setCircularOrbitPointingDown(Earth, earthAngle + 180f, LPointOrbitDistance, p_Earth);
LagrangeShadeBeta.setDiscoverable(true);
LagrangeShadeBeta.setSensorProfile(1000f);

// Lagrange Shade Alpha (L2)
SectorEntityToken LagrangeShadeAlpha = system.addCustomEntity(null, "Lagrange Shade Alpha", "stellar_shade", "neutral");
LagrangeShadeAlpha.setCircularOrbitPointingDown(Earth, earthAngle + 174f, LPointOrbitDistance, p_Earth);
LagrangeShadeAlpha.setDiscoverable(true);
LagrangeShadeAlpha.setSensorProfile(1000f);

// Lagrange Shade Gamma (L2)
SectorEntityToken LagrangeShadeGamma = system.addCustomEntity(null, "Lagrange Shade Gamma", "stellar_shade", "neutral");
LagrangeShadeGamma.setCircularOrbitPointingDown(Earth, earthAngle + 186f, LPointOrbitDistance, p_Earth);
LagrangeShadeGamma.setDiscoverable(true);
LagrangeShadeGamma.setSensorProfile(1000f);

// ## EARTH L5
// Earth Sensor Array
SectorEntityToken earthSensorArray = system.addCustomEntity("earth_sensor_array", "Earth Sensor Array", "sensor_array", "remnant");
earthSensorArray.setCircularOrbit(star, earthAngle - 60f, dist_Earth, p_Earth);
earthSensorArray.setDiscoverable(true);

// Cruithne
SectorEntityToken Cruithne = calc.spawnSPSObject2(system, star, "Cruithne", "Cruithne", "asteroid", showNameMinor, 5f, 1f, 0.5149f, 126.189f, 43.879f, 2025.93f, zeroDegGlobal, 1.138f, 1f, p_Earth, dist_Earth);

// =========================================================================
// ============================ MARS SYSTEM ================================
// =========================================================================

// Mars
float dist_MarsRaw = 1.524f;
float sz_Mars = calc.getSize(6779f);
PlanetAPI Mars = (PlanetAPI) calc.spawnSPSObject(system, star, "Mars", "Mars", "barren-desert", null, 6779f, dist_MarsRaw, 0f, 49.579f, 286.5f, 2022.60f, zeroDegGlobal, null, 1f);
float angleMars = Mars.getCircularOrbitAngle();
float dist_Mars = Mars.getCircularOrbitRadius();
float p_Mars = Mars.getCircularOrbitPeriod();

calc.addConditions(Mars.getMarket(), new String[] {
    "cold",
    "thin_atmosphere",
    "volatiles_diffuse",
    "ruins_vast",
    "organics_common",
    "sol_degenerate",
    "sol_ancient_orbital_manufactories",
    "sol_dust_storms",
    "sol_space_elevator_nearby",
    "sol_oort_strikes"
});

Mars.getSpec().setTexture("graphics/planets/mars_tx.jpg"); 
Mars.getSpec().setAtmosphereThickness(0.15f); 
Mars.getSpec().setAtmosphereThicknessMin(10f); 
Mars.getSpec().setAtmosphereColor(new Color(200, 150, 100, 100)); 
Mars.getSpec().setIconColor(new Color(157, 114, 54, 255)); 
Mars.getSpec().setTilt(25.2f); 
Mars.getSpec().setPitch(65f); 
Mars.getSpec().setRotation(calc.getRot(1.027f)); 
Mars.getSpec().setCloudTexture("graphics/planets/clouds_mars.png"); 
Mars.getSpec().setCloudColor(new Color(255, 200, 150, 50)); 
Mars.getSpec().setCloudRotation(-9.0f); 
Mars.applySpecChanges(); 

// =========================================================================
// ========================== MARS MOONS ===================================
// =========================================================================

// Phobos
float p_Phobos = calc.getTime(0.319f);
PlanetAPI Phobos = system.addPlanet("phobos", Mars, "Phobos", "barren", 0f, calc.getSize(22f), sz_Mars * 2f, p_Phobos);

Phobos.getSpec().setTexture("graphics/planets/phobos_tx.jpg");
Phobos.getSpec().setAtmosphereThickness(0f);
Phobos.getSpec().setAtmosphereThicknessMin(10f);
Phobos.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
Phobos.getSpec().setIconColor(new Color(140, 130, 120, 255));
Phobos.getSpec().setTilt(25.2f);
Phobos.getSpec().setPitch(90f);
Phobos.getSpec().setRotation(-360f / (p_Phobos * 10f));
Phobos.applySpecChanges();

calc.addConditions(Phobos.getMarket(), new String[] {
    "no_atmosphere",
    "low_gravity",
    "cold",
    "sol_space_elevator",
    "ruins_widespread",
    "sol_tiny_stripped"
});
if(generateElevators){
    SectorEntityToken elevatorPhobos1 = system.addCustomEntity("elevator2", " Phobos elevator system", "elevator2", "neutral"); 
    elevatorPhobos1.setCircularOrbitPointingDown(Phobos, 0f, calc.getSize(22f)+45f, p_Phobos);

    SectorEntityToken elevatorPhobos2 = system.addCustomEntity("elevator1", " Phobos elevator system", "elevator1", "neutral"); 
    elevatorPhobos2.setCircularOrbitPointingDown(Phobos, 180f, calc.getSize(22f)+25f, p_Phobos);
}

// Deimos
float p_Deimos = calc.getTime(1.262f);
PlanetAPI Deimos = system.addPlanet("deimos", Mars, "Deimos", "barren", 210f, calc.getSize(12f), sz_Mars * 6f, p_Deimos);

Deimos.getSpec().setTexture("graphics/planets/deimos_tx.jpg");
Deimos.getSpec().setAtmosphereThickness(0f);
Deimos.getSpec().setAtmosphereThicknessMin(10f);
Deimos.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
Deimos.getSpec().setIconColor(new Color(150, 140, 130, 255));
Deimos.getSpec().setTilt(25.2f);
Deimos.getSpec().setPitch(90f);
Deimos.getSpec().setRotation(-360f / (p_Deimos * 10f));
Deimos.applySpecChanges();

calc.addConditions(Deimos.getMarket(), new String[] {
    "no_atmosphere",
    "low_gravity",
    "ore_sparse",
    "volatiles_trace",
    "cold",
    "ruins_scattered",
    "sol_tiny_polity",
    "sol_space_elevator_nearby"
});
// =========================================================================
// ========================= MARS TROJANS ==================================
// =========================================================================

// MARS L4 TROJANS (LEADING +60) 

SectorEntityToken ghostMarsL4 = system.addCustomEntity(null, "Mars L4 Stable Point", "stable_location", "neutral");
ghostMarsL4.setCircularOrbit(star, angleMars + 60f, dist_Mars, p_Mars);

// SectorEntityToken MarsL4Trojan = system.addTerrain(Terrain.ASTEROID_FIELD, new AsteroidFieldTerrainPlugin.AsteroidFieldParams(10f, 10f, 1, 1, 3f, 3f, "1999 UJ7"));
// MarsL4Trojan.setCircularOrbit(star, 277f + 59f, dist_Mars, p_Mars);

// L5 TROJANS (TRAILING -60)
// Eureka Collisional Family

SectorEntityToken ghostMarsL5 = system.addCustomEntity(null, "Mars L5 Stable Point", "stable_location", "neutral");
ghostMarsL5.setCircularOrbit(star, angleMars - 60f, dist_Mars, p_Mars);

// Eureka | Mars L5 Trojan
float p_EurekaBeta = calc.getTime(0.5f);
float sz_Eureka = calc.getSize(2.0f);
SectorEntityToken Eureka = calc.spawnSPSObject(system, star, "Eureka", "Eureka", "asteroid", showNameMinor, 2.0f, dist_MarsRaw, 0.0649f, 245.013f, 95.543f, 2026.56f, zeroDegGlobal, -p_EurekaBeta / rotMult, 1f);
Eureka.setCustomDescriptionId("sol_eureka");

calc.spawnMoon(system, Eureka, "5261 Beta", 2f, sz_Eureka * 2.1f, calc.getTime(0.5f), 45f, showProvisionalNames);

// SectorEntityToken MarsL5Trojans = system.addTerrain(Terrain.ASTEROID_FIELD, new AsteroidFieldTerrainPlugin.AsteroidFieldParams(100f,100f, 3, 3, 3f, 3f, "1998 VF31, 2007 NS2, and 2001 DH47"));
// MarsL5Trojans.setCircularOrbit(star, 277f - 60f, dist_Mars, p_Mars);

// =========================================================================
// ==================== LOW PERIAPSIS & SUN GRAZERS ========================
// =========================================================================

// Atira
float sz_Atira = calc.getSize(4.8f);
float p_AtiraAlphaBeta =  calc.getTime(.649f);
SectorEntityToken Atira = calc.spawnSPSObject(system, star, "Atira", "Atira", "asteroid", showNameMinor, 4.8f, 0.7427f, 0.3221f, 104.397f, 252.825f, 2003.05f, zeroDegGlobal, -p_AtiraAlphaBeta / rotMult, 1f);
Atira.setCustomDescriptionId("sol_atira");

calc.spawnMoon(system, Atira, "163693 Beta", 2f, sz_Atira * 3.25f, p_AtiraAlphaBeta, 45f, showProvisionalNames);

// Aylochaxnim
SectorEntityToken Aylochaxnim = calc.spawnSPSObject(system, star, "Aylochaxnim", "Aylochaxnim", "asteroid", showNameMinor, 1.5f, 0.5554f, 0.1770f, 6.703f, 187.311f, 2020.10f, zeroDegGlobal, null, 1f);
Aylochaxnim.setCustomDescriptionId("sol_alylochaxnim");

if(!innerSolShortlist){
    // Moshup
    SectorEntityToken Moshup = calc.spawnSPSObject(system, star, "Moshup", "Moshup", "asteroid", showNameMinor, 1.3f, 0.6423f, 0.6884f, 244.912f, 192.628f, 2001.40f, zeroDegGlobal, 0.115f, 1f);
    // Squannit (Moon)
    calc.spawnMoon(system, Moshup, "Squannit", calc.getSize(0.45f), calc.getSize(1.3f) * 5f, calc.getTime(0.7f), 0f, showMinorNames);
}

// =========================================================================
// ================== MAJOR VENUS CROSSERS =================================
// =========================================================================

// Apollo
SectorEntityToken Apollo = calc.spawnSPSObject(system, star, "Apollo", "Apollo", "asteroid", showNameMinor, 1.5f, 1.4702f, 0.5599f, 35.717f, 285.820f, 1932.51f, zeroDegGlobal, 0.128f, 1f);
calc.spawnMoon(system, Apollo, "1862 Beta", 1f, calc.getSize(1.5f) * 2.6f, calc.getTime(1.14f), 0f, showProvisionalNames);

// Phaethon
SectorEntityToken Phaethon = calc.spawnSPSObject(system, star, "Phaethon", "Phaethon", "asteroid", showNameMinor, 6f, 1.2714f, 0.8898f, 265.220f, 322.180f, 2020.96f, zeroDegGlobal, 0.150f, 1f);
Phaethon.setCustomDescriptionId("sol_phaethon");

// Attach Jump Point
JumpPointAPI jpPhaethon = Global.getFactory().createJumpPoint("jp_phaethon", "Phaethon Jump Point");
jpPhaethon.setStandardWormholeToHyperspaceVisual();
jpPhaethon.setCircularOrbit(Phaethon, 20, 35, 10);
system.addEntity(jpPhaethon);

// Toutatis
SectorEntityToken Toutatis = calc.spawnSPSObject(system, star, "Toutatis", "Toutatis", "custom_entity", "Touatis", 5.4f, 2.5321f, 0.6288f, 124.300f, 278.750f, 2012.95f, zeroDegGlobal, 5.410f, 1f);

if(!visitedAsteroidsShortlist){
    // Bennu | just a rock
    SectorEntityToken Bennu = calc.spawnSPSObject(system, star, "Bennu", "Bennu", "custom_entity", "Bennu", 0.5f, 1.1264f, 0.2037f, 2.061f, 66.223f, 2010.66f, zeroDegGlobal, 0.179f, 1f);
    // TODO ASTEROID TEXTURE
    // Ryugu | just a rock
    SectorEntityToken Ryugu = calc.spawnSPSObject(system, star, "Ryugu", "Ryugu", "asteroid", showNameMinor, 0.9f, 1.1896f, 0.1902f, 251.620f, 211.430f, 2016.73f, zeroDegGlobal, 0.318f, 1f);
    // Itokawa | just a shitty contact binary | I can't wait for YORP to fission this peice of shit
    // TODO ASTEROID TEXTURE
    SectorEntityToken Itokawa = calc.spawnSPSObject(system, star, "Itokawa", "Itokawa", "asteroid", showNameMinor, 0.33f, 1.3241f, 0.2801f, 69.081f, 162.820f, 2024.36f, zeroDegGlobal, 0.505f, 1f);
}

// Icarus
SectorEntityToken Icarus = calc.spawnSPSObject(system, star, "Icarus", "Icarus", "asteroid", showNameMinor, 1.4f, 1.0779f, 0.8268f, 88.033f, 31.326f, 2015.46f, zeroDegGlobal, 0.095f, 1f);
Icarus.setCustomDescriptionId("sol_icarus");

if(!innerSolShortlist){
    // 1950 DA
    SectorEntityToken DA1950 = calc.spawnSPSObject(system, star, "1950 DA", "1950 DA", "asteroid", showNameProv, 1.1f, 1.6986f, 0.5074f, 356.680f, 224.620f, 2022.39f, zeroDegGlobal, 0.088f, 1f);
    //  ANGLO AMERICAN PLATINIDES
    SectorEntityToken angloStation = DerelictThemeGenerator.addSalvageEntity(system, "station_mining_remnant", Factions.DERELICT);
    angloStation.setName("Anglo-American Platinides");
    angloStation.setCircularOrbitPointingDown(DA1950, 30, 20, calc.getTime(10f));
    angloStation.setDiscoverable(true);
    angloStation.setSensorProfile(2000f);
    // Sisyphus
    SectorEntityToken Sisyphus = calc.spawnSPSObject(system, star, "Sisyphus", "Sisyphus", "asteroid", showNameMinor, 8.5f, 1.8937f, 0.5386f, 63.498f, 293.090f, 2017.68f, zeroDegGlobal, 0.100f, 1f);
    // Sisyphus moonlet | as/rp=2.6 | Ps=1.14 d | ds=0.84 km
    calc.spawnMoon(system, Sisyphus, "1866  Beta", calc.getSize(0.84f), calc.getSize(8.5f) * 2.6f, calc.getTime(1.14f), 0f, showProvisionalNames);
    // Adonis
    SectorEntityToken Adonis = calc.spawnSPSObject(system, star, "Adonis", "Adonis", "asteroid", showNameMinor, 0.6f, 1.8737f, 0.7644f, 350.596f, 42.348f, 1936.18f, zeroDegGlobal, null, 1f);
    // Oljato
    SectorEntityToken Oljato = calc.spawnSPSObject(system, star, "Oljato", "Oljato", "asteroid", showNameMinor, 1.8f, 2.1718f, 0.7132f, 76.657f, 96.486f, 2018.47f, zeroDegGlobal, 1.083f, 1f);
    // Heracles
    SectorEntityToken Heracles = calc.spawnSPSObject(system, star, "Heracles", "Heracles", "asteroid", showNameMinor, 5f, 1.8336f, 0.7723f, 226.741f, 299.715f, 2022.53f, zeroDegGlobal, -0.113f, 1f);
    // Moon: S/2011 (5143)
    calc.spawnMoon(system, Heracles, "5143 Beta", calc.getSize(0.6f), calc.getSize(4.8f) * 5f, -calc.getTime(0.7f), 45f, showProvisionalNames);
    // Syrinx
    SectorEntityToken Syrinx = calc.spawnSPSObject(system, star, "Syrinx", "Syrinx", "asteroid", showNameMinor, 1.8f, 2.4680f, 0.7429f, 244.460f, 272.820f, 2012.87f, zeroDegGlobal, null, 1f);
}

// =========================================================================
// ========================== AMOR ASTEROIDS ===============================
// =========================================================================

// Eros system
SectorEntityToken Eros = (falseMoons ? calc.spawnSPSObject4(system, Mars, "Eros", "Eros", "custom_entity", "Eros", 17f, 1.4583f, 0.2227f, 304.320f, 178.820f, 2019.85f, zeroDegGlobal, 0.220f, 1f, null, null, true, dist_Mars, angleMars + 180f, p_Mars, star) : calc.spawnSPSObject(system, star, "Eros", "Eros", "custom_entity", "Eros", 17, 1.4583f, 0.2227f, 304.320f, 178.820f, 2019.85f, zeroDegGlobal, 0.220f, 1f));
if (!isSettled) {
    Eros.setInteractionImage("illustrations", "abandoned_station2");
    Misc.setAbandonedStationMarket("marketEros", Eros);

    Eros.getMarket().setName("Eros");
    Eros.getMarket().addCondition("abandoned_station");
    Eros.getMarket().addCondition("low_gravity");
    Eros.getMarket().addCondition("sol_tiny_stripped");
    Eros.getMarket().addCondition("volatiles_trace");
    Eros.getMarket().addCondition("hot");

    if(mercuryCold){
    Eros.getMarket().addCondition("cold");    
    }

    for (MarketConditionAPI condition : Eros.getMarket().getConditions()) {
        condition.setSurveyed(true);
    }

    Eros.setDiscoverable(true);
    Eros.setSensorProfile(4000f);
}

// Ganymed
SectorEntityToken Ganymed = calc.spawnSPSObject(system, star, "Ganymed", "Ganymed", "asteroid", showNameMinor, 35, 2.6650f, 0.5332f, 215.441f, 132.503f, 2024.71f, zeroDegGlobal, 0.429f, 1f);

// Don Quixote
SectorEntityToken DonQuixote = calc.spawnSPSObject(system, star, "Don Quixote", "Don Quixote", "asteroid", showNameMinor, 19, 4.2572f, 0.7087f, 350.013f, 316.432f, 2018.33f, zeroDegGlobal, 0.321f, 1f);

if(!innerSolShortlist){
    // 2100 Ra-Shalom
    SectorEntityToken RaShalom = calc.spawnSPSObject(system, star, "Ra-Shalom", "Ra-Shalom", "asteroid", showNameMinor, 2.3f, 0.8321f, 0.4365f, 170.880f, 355.980f, 2016.64f, zeroDegGlobal, 0.825f, 1f);
}

// DARK COMET ==============================================================
// 1998 KY 26 | Hyabusa mission | 5 min rotation | dark comet????? <- this is probably the same type of shit that oumouamoua was on
SectorEntityToken KY26 = calc.spawnSPSObject(system, star, "KY26", "1998 KY26", "asteroid", showNameProv, 0.011f, 1.2289f, 0.2001f, 84.182f, 210.004f, 2025.88f, zeroDegGlobal, 0.00371f, 1f);

// =========================================================================
// ====================== MAJOR MARS-CROSSERS ==============================
// =========================================================================

// Aethra
SectorEntityToken Aethra = calc.spawnSPSObject(system, star, "Aethra", "Aethra", "asteroid", showNameMinor, 43f, 2.6082f, 0.3888f, 259.740f, 252.560f, 2021.12f, zeroDegGlobal, 0.216f, 1f);

// Brucia
SectorEntityToken Brucia = calc.spawnSPSObject(system, star, "Brucia", "Brucia", "asteroid", showNameMinor, 33f, 2.6345f, 0.3014f, 276.510f, 296.240f, 2023.68f, zeroDegGlobal, 0.394f, 1f);

if(!innerSolShortlist){
    // Kepler
    SectorEntityToken Kepler = calc.spawnSPSObject(system, star, "Kepler", "Kepler", "asteroid", showNameMinor, 4f, 2.6825f, 0.4651f, 6.410f, 330.490f, 2023.18f, zeroDegGlobal, 0.120f, 1f);
}

// =========================================================================
// Asteroid Belt (2.1–3.7 AU)
// =========================================================================

// --- Terrain ---
calc.smartBelt(system, star, "Hungarians",     gen_Hungarians, calc.getDist(1.84f, star), calc.getDist(1.95f, star), calc.getTime(950f),  calc.getTime(1150f));
calc.smartBelt(system, star, "Inner Belt",     gen_InnerBelt,  calc.getDist(2.15f, star), calc.getDist(2.40f, star), calc.getTime(1150f), calc.getTime(1350f));
calc.smartBelt(system, star, "Main Belt Core", gen_CoreBelt,   calc.getDist(2.55f, star), calc.getDist(2.80f, star), calc.getTime(1550f), calc.getTime(1750f));
calc.smartBelt(system, star, "Outer Belt",     gen_OuterBelt,  calc.getDist(2.9f, star),  calc.getDist(3.2f, star),  calc.getTime(1950f), calc.getTime(2200f));
calc.smartBelt(system, star, "Cybeles",        gen_Cybeles,    calc.getDist(3.27f, star), calc.getDist(3.37f, star), calc.getTime(2200f), calc.getTime(2400f));

// --- Hungaria group (1.78–2.0 AU, inside 4:1 Kirkwood) ---
calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 1, calc.getDist(1.78f, star), calc.getDist(2.06f, star), calc.getTime(950f));

// --- Inner belt (2.12–2.55 AU) ---
calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 0, calc.getDist(2.12f, star), calc.getDist(2.48f, star), calc.getTime(1140f));  
calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 0, calc.getDist(2.15f, star), calc.getDist(2.55f, star), calc.getTime(1270f));  
calc.smartRingTex(system, star, "sol_rings", "rings_rocks1", 256, 1, calc.getDist(2.25f, star), calc.getDist(2.45f, star), calc.getTime(1270f));  
calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 1, calc.getDist(2.3f, star), calc.getDist(2.6f, star), calc.getTime(1270f));  

// --- Mid belt (2.45–2.88 AU) ---
// pre 2.7 AU falloff (8:3 resonance)
calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 1, calc.getDist(2.45f, star), calc.getDist(2.75f, star), calc.getTime(1550f));
calc.smartRingTex(system, star, "sol_rings", "rings_rocks1", 256, 1, calc.getDist(2.52f, star), calc.getDist(2.68f, star), calc.getTime(1600f)); 
// Post 8:3
calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 1, calc.getDist(2.60f, star), calc.getDist(2.82f, star), calc.getTime(1620f));  
calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 0, calc.getDist(2.65f, star), calc.getDist(2.88f, star), calc.getTime(1650f));  

// --- Outer belt (2.82–3.27 AU) --- 
// Bisected by Kirkwood 7:3
// Inner outer belt
// Consider Sawtooth??? there a lot of resonances not shown or ignored bn 
calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 0, calc.getDist(2.82f, star), calc.getDist(3.03f, star), calc.getTime(1850f));  
calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 0, calc.getDist(2.86f, star), calc.getDist(3.05f, star), calc.getTime(1800f));  

calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 3, calc.getDist(2.95f, star), calc.getDist(3.24f, star), calc.getTime(1990f));  

// 11:5 material, but not shown (makes stuff look wierd)

// Outer outer belt
calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 0, calc.getDist(2.96f, star), calc.getDist(3.1f, star), calc.getTime(1900f)); 
calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 1, calc.getDist(2.96f, star), calc.getDist(3.20f, star), calc.getTime(1950f)); 
calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 1, calc.getDist(2.97f, star), calc.getDist(3.05f, star), calc.getTime(2150f));
calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 3, calc.getDist(3.05f, star), calc.getDist(3.15f, star), calc.getTime(1930f)); 
calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 3, calc.getDist(3.05f, star), calc.getDist(3.27f, star), calc.getTime(2100f));

// --- Cybele/Thule (3.27–3.7 AU, beyond 2:1 Kirkwood) ---
calc.smartRingTex(system, star, "sol_rings", "rings_rocks0", 256, 1, calc.getDist(3.2f, star), calc.getDist(3.50f, star), calc.getTime(2400f));

// --- dust ---
calc.smartRingTex(system, star, "sol_rings", "rings_alpha1", 256, 0, calc.getDist(2.05f, star), calc.getDist(2.50f, star), calc.getTime(1200f)); 
calc.smartRingTex(system, star, "sol_rings", "rings_alpha1", 256, 1, calc.getDist(2.50f, star), calc.getDist(2.85f, star), calc.getTime(1600f)); 
calc.smartRingTex(system, star, "sol_rings", "rings_alpha1", 256, 0, calc.getDist(2.85f, star), calc.getDist(3.30f, star), calc.getTime(1950f));  

// =========================================================================
// GROUP 1: THE BIG FOUR (Major Protoplanets)
// =========================================================================

// Ceres system
float dist_CeresRaw = 2.7656f;
PlanetAPI Ceres = (PlanetAPI) calc.spawnSPSObject(system, star, "Ceres", "Ceres", "rocky_ice", null, 940f, dist_CeresRaw, 0f, 80.250f, 73.300f, 2027.53f, zeroDegGlobal, 0.378f, 1f);
float angleCeres = Ceres.getCircularOrbitAngle();
float sz_Ceres = Ceres.getRadius();
float dist_Ceres = Ceres.getCircularOrbitRadius();
float p_Ceres = Ceres.getCircularOrbitPeriod();

Ceres.getSpec().setTexture("graphics/planets/ceres_tx.jpg"); 
Ceres.getSpec().setAtmosphereThickness(0.00f); 
Ceres.getSpec().setAtmosphereThicknessMin(0f); 
Ceres.getSpec().setAtmosphereColor(new Color(200, 220, 255, 40)); 
Ceres.getSpec().setIconColor(new Color(180, 180, 190, 255)); 
Ceres.getSpec().setTilt(10.6f); 
Ceres.getSpec().setPitch(94f); 
Ceres.getSpec().setRotation(calc.getRot(.376f)); 
Ceres.applySpecChanges();

calc.addConditions(Ceres.getMarket(), new String[] {
    "volatiles_plentiful",
    "cold",
    "ore_sparse",
    "rare_ore_sparse",
    "ruins_vast",
    "low_gravity",
    "no_atmosphere",
    "sol_meteoroids",
    "sol_orbital_ring",
    "sol_jump_point_nearby"
});
// Ceres Jump Point
JumpPointAPI _jp1 = Global.getFactory().createJumpPoint("jp_ceres", "Ceres Jump Point");
_jp1.setStandardWormholeToHyperspaceVisual();
_jp1.setCircularOrbit(Ceres, 0, 100, calc.getTime(30f));
system.addEntity(_jp1);

if(generateElevators){
    SectorEntityToken ringCeres = system.addCustomEntity("ring1", "Ceres ring", "ring1", "neutral"); 
    ringCeres.setCircularOrbitPointingDown(Ceres, 0f, 1, calc.getOrbRot(0.378f));
}

// 2000 EU16 | quasi-satellite of Ceres | a=2.766 AU
SectorEntityToken EU16 = calc.spawnSPSObject2(system, star, "2000 EU16", "2000 EU16", "asteroid", showNameProv, 5f, 2.7665f, 0.1655f, 334.236f, 348.004f, 2024.99f, zeroDegGlobal, null, 1f, p_Ceres, dist_CeresRaw);
EU16.setCustomDescriptionId("sol_eu16");

// Vesta
PlanetAPI Vesta = (PlanetAPI) (falseMoons ? calc.spawnSPSObject4(system, Ceres, "Vesta", "Vesta", "rocky_metallic", null, 525f, 2.3615f, 0.0902f, 103.702f, 151.537f, 2025.62f, zeroDegGlobal, 0.223f, 1f, null, null, true, dist_Ceres, angleCeres + 180f, p_Ceres, star) : calc.spawnSPSObject(system, star, "Vesta", "Vesta", "barren", null, 525f, 2.3615f, 0.0902f, 103.702f, 151.537f, 2025.62f, zeroDegGlobal, 0.223f, 1f));

Vesta.getSpec().setTexture("graphics/planets/vesta_tx.jpg");
Vesta.getSpec().setAtmosphereThickness(0f);
Vesta.getSpec().setAtmosphereThicknessMin(10f);
Vesta.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
Vesta.getSpec().setIconColor(new Color(180, 170, 160, 255));
Vesta.getSpec().setTilt(7.1f);
Vesta.getSpec().setPitch(65f);
Vesta.getSpec().setRotation(calc.getRot(.222f));
Vesta.applySpecChanges();

if (!isSettled) {
calc.addConditions(Vesta.getMarket(), new String[] {
    "cold",
    "ruins_widespread",
    "decivilized",
    "no_atmosphere",
    "low_gravity",
    "riore_ch",
    "volatiles_diffuse",
    "rare_ore_moderate",
    "sol_meteoroids",
    "sol_orbital_ring",
    "sol_fast_rotator"
});} else {
    Vesta.getMarket().addCondition("sol_porus");
}

if(generateElevators){
    SectorEntityToken ringVesta = system.addCustomEntity("ring1", "Vesta ring", "ring1", "neutral"); 
    ringVesta.setCircularOrbitPointingDown(Vesta, 0f, 1, calc.getOrbRot(0.222f));
}
// Pallas
PlanetAPI Pallas = (PlanetAPI) (falseMoons ? calc.spawnSPSObject4(system, Ceres, "Pallas", "Pallas", "rocky_ice", null, 510f, 2.7699f, 0.2306f, 172.889f, 310.933f, 2027.79f, zeroDegGlobal, 0.326f, 1f, null, null, true, dist_Ceres, angleCeres + 180f, p_Ceres, star) : calc.spawnSPSObject(system, star, "Pallas", "Pallas", "rocky_ice", null, 510f, 2.7699f, 0.2306f, 172.889f, 310.933f, 2027.79f, zeroDegGlobal, 0.326f, 1f));

Pallas.getSpec().setTexture("graphics/planets/pallas_tx.jpg");
Pallas.getSpec().setAtmosphereThickness(0f);
Pallas.getSpec().setAtmosphereThicknessMin(10f);
Pallas.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
Pallas.getSpec().setIconColor(new Color(140, 140, 160, 255));
Pallas.getSpec().setTilt(34.8f);
Pallas.getSpec().setPitch(10f);
Pallas.getSpec().setRotation(calc.getRot(.325f));
Pallas.applySpecChanges();

calc.addConditions(Pallas.getMarket(), new String[] {
    "cold",
    "no_atmosphere",
    "low_gravity",
    "ore_moderate",
    "rare_ore_sparse",
    "ruins_widespread",
    "sol_meteoroids",
    "sol_space_elevator"
});
if(generateElevators){
    SectorEntityToken elevatorPallas = system.addCustomEntity("elevator1", "Pallas Elevator", "elevator1", "neutral"); 
    elevatorPallas.setCircularOrbitPointingDown(Pallas, 0f, calc.getSize(510f)+33f, calc.getOrbRot(0.326f));
}

// Hygiea
PlanetAPI Hygiea = (PlanetAPI) (falseMoons ? calc.spawnSPSObject4(system, Ceres, "Hygiea", "Hygiea", "barren-bombarded", null, 430f, 3.1476f, 0.1082f, 283.122f, 312.606f, 2028.11f, zeroDegGlobal, 1.151f, 1f, null, null, true, dist_Ceres, angleCeres + 180f, p_Ceres, star) : calc.spawnSPSObject(system, star, "Hygiea", "Hygiea", "rocky_ice", null, 430f, 3.1476f, 0.1082f, 283.122f, 312.606f, 2028.11f, zeroDegGlobal, 1.151f, 1f));

Hygiea.getSpec().setTexture("graphics/planets/hygiea_tx.jpg");
Hygiea.getSpec().setAtmosphereThickness(0f);
Hygiea.getSpec().setAtmosphereThicknessMin(10f);
Hygiea.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
Hygiea.getSpec().setIconColor(new Color(80, 80, 90, 255));
Hygiea.getSpec().setTilt(3.8f);
Hygiea.getSpec().setPitch(30f);
Hygiea.getSpec().setRotation(calc.getRot(.576f));
Hygiea.applySpecChanges();

calc.addConditions(Hygiea.getMarket(), new String[] {
    "cold",
    "no_atmosphere",
    "low_gravity",
    "ore_moderate",
    "volatiles_diffuse",
    "ruins_scattered",
    "sol_meteoroids",
    "sol_space_elevator",
    "sol_degenerate"
});
if(generateElevators){
    SectorEntityToken elevatorHygiea = system.addCustomEntity("elevator1", "Hygiea Elevator", "elevator1", "neutral"); 
    elevatorHygiea.setCircularOrbitPointingDown(Hygiea, 0f, calc.getSize(430f)+33f, calc.getOrbRot(0.576f));
}

// =========================================================================
// GROUP 2: THE CYBELE GROUP (Outer Belt Edge)
// =========================================================================

// Cybele
SectorEntityToken Cybele = calc.spawnSPSObject(system, star, "Cybele", "Cybele", "asteroid", showNameMinor, 237f, 3.4283f, 0.1114f, 155.630f, 102.370f, 2021.06f, zeroDegGlobal, 0.253f, 1f);

// Sylvia
SectorEntityToken Sylvia = calc.spawnSPSObject(system, star, "Sylvia", "Sylvia", "asteroid", showNameMinor, 253f, 3.4770f, 0.0939f, 73.010f, 264.400f, 2024.15f, zeroDegGlobal, 0.216f, 1f);

// Romulus (Moon)
SectorEntityToken Romulus = calc.spawnMoon(system, Sylvia, "Romulus", calc.getSize(10.8f), calc.getSize(250f) * 10f, calc.getTime(3.65f), 45f, showMinorNames);
Romulus.setCustomDescriptionId("sol_romulus");
// Remus (Moon)
SectorEntityToken Remus = calc.spawnMoon(system, Sylvia, "Remus", calc.getSize(10.6f), calc.getSize(250f) * 5f, calc.getTime(1.37f), 225f, showMinorNames);
Remus.setCustomDescriptionId("sol_remus");

if(!asteroidBeltShortlist){
    // Camilla
    SectorEntityToken Camilla = calc.spawnSPSObject(system, star, "Camilla", "Camilla", "asteroid", showNameMinor, 210f, 3.4860f, 0.0653f, 172.590f, 305.220f, 2026.42f, zeroDegGlobal, 0.202f, 1f);
    // S/2001 (107) 1 (Moon)
    SectorEntityToken CamillaBeta = calc.spawnMoon(system, Camilla, "107 Beta", calc.getSize(16f), calc.getSize(219f) * 10f, 3.7f, 45f, showProvisionalNames);
    // S/2016 (107) 2 (Moon)
    SectorEntityToken CamillaGamma = calc.spawnMoon(system, Camilla, "107 Gamma", calc.getSize(3.5f), calc.getSize(219f) * 4f, 0.53f, 225f, showProvisionalNames);
    // Hermione
    SectorEntityToken Hermione = calc.spawnSPSObject(system, star, "Hermione", "Hermione", "asteroid", showNameMinor, 209f, 3.4530f, 0.1257f, 72.880f, 296.930f, 2021.83f, zeroDegGlobal, 0.231f, 1f);
    // LaFayette (Moon)
    SectorEntityToken HermioneBeta = calc.spawnMoon(system, Hermione, "LaFayette", calc.getSize(8f), calc.getSize(190f) * 5f, calc.getTime(20f), 45f, showMinorNames);
}

// =========================================================================
// GROUP 3: THE THEMIS FAMILY (Outer Belt C/B-Type)
// =========================================================================

// Themis
SectorEntityToken Themis = calc.spawnSPSObject(system, star, "Themis", "Themis", "asteroid", showNameMinor, 198f, 3.1490f, 0.1165f, 36.500f, 108.060f, 2025.00f, zeroDegGlobal, 0.349f, 1f);

// Antiope
float sz_Antiope_Comp = calc.getSize(88f);
float p_Antiope = calc.getTime(2022f);
SectorEntityToken antiopeBarycenter = calc.spawnSPSObject(system, star, "AntiopeBarycenter", "Antiope Barycenter", "custom_entity", "empty", 120f, 3.1560f, 0.1560f, 70.200f, 242.400f, 2021.46f, zeroDegGlobal, 0.688f, 1f);

SectorEntityToken AntiopeA = calc.spawnMoon(system, antiopeBarycenter, "Antiope A", sz_Antiope_Comp, sz_Antiope_Comp * 3f, 1f, 0f, showMinorNames);
SectorEntityToken AntiopeB = calc.spawnMoon(system, antiopeBarycenter, "Antiope B", sz_Antiope_Comp, sz_Antiope_Comp * 3f, 1f, 180f, showMinorNames);

// Antiope Station | Orbital Station 
// SectorEntityToken antiopeStation = system.addCustomEntity("antiope_station", "Antiope Station", "station_side02", "neutral");
// antiopeStation.setCircularOrbitPointingDown(antiopeBarycenter, 0, 1, 1f);

// antiopeStation.setInteractionImage("illustrations", "abandoned_station2");
// Misc.setAbandonedStationMarket("antiope_station_market", antiopeStation);

// antiopeStation.getMarket().setName("Antiope Station");
// antiopeStation.getMarket().addCondition("abandoned_station");
// antiopeStation.getMarket().addCondition("sol_meteoroids");
// antiopeStation.getMarket().addCondition("volatiles_trace");
// antiopeStation.getMarket().addCondition("ore_moderate");
// antiopeStation.getMarket().addCondition("rare_ore_rich");

// for (MarketConditionAPI condition : antiopeStation.getMarket().getConditions()) {
// condition.setSurveyed(true);
// }

// antiopeStation.setDiscoverable(true);
// antiopeStation.setSensorProfile(4000f);

if(!asteroidBeltShortlist){
// Thisbe
    SectorEntityToken Thisbe = calc.spawnSPSObject(system, star, "Thisbe", "Thisbe", "asteroid", showNameMinor, 218f, 2.7680f, 0.1650f, 276.770f, 36.590f, 2023.28f, zeroDegGlobal, 0.252f, 1f);
}

// =========================================================================
// GROUP 4: THE FLORA FAMILY (Inner Belt S-Type)
// =========================================================================

// Flora 
SectorEntityToken Flora = calc.spawnSPSObject(system, star, "Flora", "Flora", "asteroid", showNameMinor, 140f, 2.2014f, 0.1566f, 110.850f, 285.400f, 2024.09f, zeroDegGlobal, 0.536f, 1f);

SectorEntityToken Donaldjohanson = calc.spawnSPSObject(system, star, "Donaldjohanson", "Donaldjohanson", "custom_entity", "Donaldjohanson", 3.9f, 2.3835f, 0.1869f, 262.776f, 212.836f, 2024.92f, zeroDegGlobal, 10.500f, 1f);

SectorEntityToken Gaspra = calc.spawnSPSObject(system, star, "Gaspra", "Gaspra", "custom_entity", "Gaspra", 12.2f, 2.2102f, 0.1734f, 252.988f, 130.037f, 2025.41f, zeroDegGlobal, 0.293f, 1f);

// =========================================================================
// GROUP 5: METALLIC (M-TYPE) & HIGH-DENSITY OBJECTS
// =========================================================================

// Psyche
SectorEntityToken Psyche = (falseMoons ? calc.spawnSPSObject4(system, Ceres, "Psyche", "Psyche", "custom_entity", "Psyche", 222f, 2.9233f, 0.1343f, 150.010f, 229.753f, 2025.32f, zeroDegGlobal, 0.175f, 1f, null, null, true, dist_Ceres, angleCeres + 180f, p_Ceres, star) : calc.spawnSPSObject(system, star, "Psyche", "Psyche", "custom_entity", "Psyche", 222f, 2.9233f, 0.1343f, 150.010f, 229.753f, 2025.32f, zeroDegGlobal, 0.175f, 1f));
if (!isSettled) {
    Psyche.setInteractionImage("illustrations", "abandoned_station2");
    Misc.setAbandonedStationMarket("marketPsyche", Psyche);

    Psyche.getMarket().setName("Psyche");
    Psyche.getMarket().addCondition("abandoned_station");
    Psyche.getMarket().addCondition("low_gravity");
    Psyche.getMarket().addCondition("cold");
    Psyche.getMarket().addCondition("ore_ultrarich");
    Psyche.getMarket().addCondition("rare_ore_ultrarich");
    Psyche.getMarket().addCondition("sol_meteoroids");
    Psyche.getMarket().addCondition("sol_orbital_ring");
    Psyche.getMarket().addCondition("sol_fast_rotator");
    Psyche.getMarket().addCondition("sol_no_atmosphere_bodgejob");

    for (MarketConditionAPI condition : Psyche.getMarket().getConditions()) {
        condition.setSurveyed(true);
    }

    Psyche.setDiscoverable(true);
    Psyche.setSensorProfile(4000f);
}

// Kleopatra
SectorEntityToken Kleopatra = calc.spawnSPSObject(system, star, "Kleopatra", "Kleopatra", "asteroid", showNameMinor, 135f, 2.7953f, 0.2520f, 215.390f, 179.640f, 2021.15f, zeroDegGlobal, 0.224f, 1f);

// Alexhelios (Outer)
SectorEntityToken Alexhelios = calc.spawnMoon(system, Kleopatra, "Alexhelios", calc.getSize(5f), 40, calc.getTime(20f), 90f, showMinorNames);
// Cleoselene (Inner)
SectorEntityToken Cleoselene = calc.spawnMoon(system, Kleopatra, "Cleoselene", calc.getSize(3f), 100, calc.getTime(10f), 270f, showMinorNames);

// 22 Kalliope
SectorEntityToken Kalliope = calc.spawnSPSObject(system, star, "Kalliope", "Kalliope", "asteroid", showNameMinor, 166f, 2.9090f, 0.1028f, 66.060f, 356.350f, 2023.35f, zeroDegGlobal, 0.173f, 1f);

// Linus (Moon)
SectorEntityToken Linus = calc.spawnMoon(system, Kalliope, "Linus", calc.getSize(10f), calc.getSize(166f) * 6f, calc.getTime(15f), 0f, showMinorNames);

// GLENCORE INTERPLANETARY
SectorEntityToken glencoreStation = DerelictThemeGenerator.addSalvageEntity(system, "station_mining_remnant", Factions.DERELICT);
glencoreStation.setName("Rio-Glencore Interplanetary");
glencoreStation.setCircularOrbitPointingDown(Kalliope, 45, 180, calc.getTime(40f));
glencoreStation.setDiscoverable(true);
glencoreStation.setSensorProfile(2000f);

// Lutetia 
SectorEntityToken Lutetia = calc.spawnSPSObject(system, star, "Lutetia", "Lutetia", "custom_entity", "Lutetia", 100f, 2.4349f, 0.1636f, 80.900f, 250.210f, 2022.05f, zeroDegGlobal, 0.340f, 1f);

// VALE LUTETIA 
SectorEntityToken valeStation = DerelictThemeGenerator.addSalvageEntity(system, "station_mining_remnant", Factions.DERELICT);
valeStation.setName("Vale Lutetia");
valeStation.setCircularOrbitPointingDown(Lutetia, 45, 180, calc.getTime(55f));
valeStation.setDiscoverable(true);
valeStation.setSensorProfile(2000f);

// Antigone
SectorEntityToken Antigone = calc.spawnSPSObject(system, star, "Antigone", "Antigone", "asteroid", showNameMinor, 125f, 2.8670f, 0.2131f, 135.660f, 108.310f, 2022.75f, zeroDegGlobal, 0.206f, 1f);
// "Antigone Beta" — lightcurve-only hint from 1979 Tedesco survey
if(speculativeBodiesExtreme){
    calc.spawnMoon(system, Antigone, "Antigone Beta", calc.getSize(6f), calc.getSize(125f) * 4f, calc.getTime(2.5f), 90f, showProvisionalNames);
}

// NEONORNICKEL
SectorEntityToken NeoNorNickel = DerelictThemeGenerator.addSalvageEntity(system, "station_mining_remnant", Factions.DERELICT);
NeoNorNickel.setName("NeoNorNickel");
NeoNorNickel.setCircularOrbitPointingDown(Antigone, 90, 150, calc.getTime(45f));
NeoNorNickel.setDiscoverable(true);
NeoNorNickel.setSensorProfile(2000f);

// Klotho
SectorEntityToken Klotho = calc.spawnSPSObject(system, star, "Klotho", "Klotho", "asteroid", showNameMinor, 82f, 2.6680f, 0.2565f, 159.250f, 268.420f, 2022.65f, zeroDegGlobal, 1.463f, 1f);

// NEWMONT GOLDCORP
SectorEntityToken newmontStation = DerelictThemeGenerator.addSalvageEntity(system, "station_mining_remnant", Factions.DERELICT);
newmontStation.setName("NewMont GoldCorp");
newmontStation.setCircularOrbitPointingDown(Klotho, 180, 140, calc.getTime(42f));
newmontStation.setDiscoverable(true);
newmontStation.setSensorProfile(2000f);

// =========================================================================
// GROUP 6: NAMED FAMILIES (Middle & Outer Belt)
// =========================================================================

// Eunomia
SectorEntityToken Eunomia = calc.spawnSPSObject(system, star, "Eunomia", "Eunomia", "asteroid", showNameMinor, 268f, 2.6430f, 0.1870f, 293.300f, 97.900f, 2024.55f, zeroDegGlobal, 0.254f, 1f);

// Ida
SectorEntityToken Ida = calc.spawnSPSObject(system, star, "Ida", "Ida", "custom_entity", "Ida", 32f, 2.8610f, 0.0444f, 323.600f, 113.900f, 2025.73f, zeroDegGlobal, 0.193f, 1f);

// Dactyl (Moon)
SectorEntityToken Dactyl = system.addCustomEntity("Dactyl", "Dactyl", "Dactyl", "neutral"); 
Dactyl.setCircularOrbitPointingDown(Ida, 0f, calc.getSize(31.4f) * 5f,  calc.getTime(20f));

// 31 Euphrosyne
SectorEntityToken Euphrosyne = calc.spawnSPSObject(system, star, "Euphrosyne", "Euphrosyne", "asteroid", showNameMinor, 267f, 3.1590f, 0.2160f, 30.800f, 61.800f, 2024.82f, zeroDegGlobal, 0.230f, 1f);

// S/2019 (31) 1 (Moon)
SectorEntityToken EuphrosyneBeta = calc.spawnMoon(system, Euphrosyne, "31 Beta", calc.getSize(6.7f), calc.getSize(260f) * 5f, calc.getTime(1.2f), 200f, showProvisionalNames);

if(!asteroidBeltShortlist){
    // 25 Phocaea
    SectorEntityToken Phocaea = calc.spawnSPSObject(system, star, "Phocaea", "Phocaea", "asteroid", showNameMinor, 61f, 2.4000f, 0.2550f, 214.100f, 90.200f, 2024.90f, zeroDegGlobal, 0.414f, 1f);
    // 128 Nemesis
    SectorEntityToken Nemesis = calc.spawnSPSObject(system, star, "Nemesis", "Nemesis", "asteroid", showNameMinor, 163f, 2.7490f, 0.1270f, 76.200f, 302.800f, 2026.62f, zeroDegGlobal, 3.242f, 1f);
    // Alauda
    SectorEntityToken Alauda = calc.spawnSPSObject(system, star, "Alauda", "Alauda", "asteroid", showNameMinor, 191f, 3.1950f, 0.0161f, 289.710f, 353.930f, 2018.49f, zeroDegGlobal, 0.696f, 1f);
    // P/2007 (702) 1 (Moon)
    SectorEntityToken AlaudaBeta = calc.spawnMoon(system, Alauda, "Pichi Unem", calc.getSize(3.5f), calc.getSize(191f) * 6f, calc.getTime(4.9f), 90f, showProvisionalNames);
    // Ursula
    SectorEntityToken Ursula = calc.spawnSPSObject(system, star, "Ursula", "Ursula", "asteroid", showNameMinor, 216f, 3.1240f, 0.1060f, 336.410f, 342.150f, 2020.15f, zeroDegGlobal, 0.704f, 1f);
    // Eugenia
    SectorEntityToken Eugenia = calc.spawnSPSObject(system, star, "Eugenia", "Eugenia", "asteroid", showNameMinor, 202f, 2.7200f, 0.0820f, 147.940f, 85.140f, 2023.30f, zeroDegGlobal, 0.238f, 1f);
    // S/2004 (45)
    SectorEntityToken EugeniaGamma = calc.spawnMoon(system, Eugenia, "S/2004 (45) 1", calc.getSize(6f), calc.getSize(202f) * 5.9f, calc.getTime(1.793f), 0f, showProvisionalNames);
    // Petit-Prince (Moon)
    SectorEntityToken PetitPrince = calc.spawnMoon(system, Eugenia, "Petit-Prince", calc.getSize(7f), calc.getSize(200f) * 5f, calc.getTime(10f), 180f, showMinorNames);
}

// =========================================================================
// GROUP 7: RESONANT & ECCENTRIC GROUPS (Outliers)
// =========================================================================

// Hungaria
SectorEntityToken Hungaria = calc.spawnSPSObject(system, star, "Hungaria", "Hungaria", "asteroid", showNameMinor, 11f, 1.9440f, 0.0736f, 175.400f, 123.970f, 2024.34f, zeroDegGlobal, 1.105f, 1f);

// Thule
SectorEntityToken Thule = calc.spawnSPSObject(system, star, "Thule", "Thule", "asteroid", showNameMinor, 127f, 4.2690f, 0.0123f, 75.580f, 234.290f, 2023.37f, zeroDegGlobal, 0.310f, 1f);

// Griqua
SectorEntityToken Griqua = calc.spawnSPSObject(system, star, "Griqua", "Griqua", "asteroid", showNameMinor, 30f, 3.2220f, 0.3700f, 121.410f, 262.190f, 2022.93f, zeroDegGlobal, 0.288f, 1f);

// Alinda
SectorEntityToken Alinda = calc.spawnSPSObject(system, star, "Alinda", "Alinda", "asteroid", showNameMinor, 4f, 2.4730f, 0.5670f, 110.550f, 349.330f, 2025.04f, zeroDegGlobal, 3.082f, 1f);

// =========================================================================
// GROUP 8: LARGE BACKGROUND ASTEROIDS (S-Type / Stony)
// =========================================================================

// Juno
SectorEntityToken Juno = calc.spawnSPSObject(system, star, "Juno", "Juno", "asteroid", showNameMinor, 246f, 2.6680f, 0.2580f, 169.870f, 248.410f, 2024.64f, zeroDegGlobal, 0.300f, 1f);

// Iris
SectorEntityToken Iris = calc.spawnSPSObject(system, star, "Iris", "Iris", "asteroid", showNameMinor, 200f, 2.3860f, 0.2300f, 259.560f, 145.200f, 2022.48f, zeroDegGlobal, 0.297f, 1f);

// ## ASTEROIDE TINTO ------------------------------------------------------
// Location: 7 Iris (Stony-Iron, very bright/reflective)
SectorEntityToken tintoStation = DerelictThemeGenerator.addSalvageEntity(system, "station_mining_remnant", Factions.DERELICT);
tintoStation.setName("Asteroide Tinto");
tintoStation.setCircularOrbitPointingDown(Iris, 120, 140, calc.getTime(44f));
tintoStation.setDiscoverable(true);
tintoStation.setSensorProfile(2000f);

if(!asteroidBeltShortlist){
    // Hebe
    SectorEntityToken Hebe = calc.spawnSPSObject(system, star, "Hebe", "Hebe", "asteroid", showNameMinor, 186f, 2.4250f, 0.2030f, 138.640f, 239.490f, 2024.35f, zeroDegGlobal, 0.303f, 1f);
    // Metis
    SectorEntityToken MetisA = calc.spawnSPSObject(system, star, "Metis", "Metis", "asteroid", showNameMinor, 190f, 2.3860f, 0.1220f, 68.850f, 6.270f, 2025.79f, zeroDegGlobal, 0.212f, 1f);
    // Amphitrite
    SectorEntityToken Amphitrite = calc.spawnSPSObject(system, star, "Amphitrite", "Amphitrite", "asteroid", showNameMinor, 212f, 2.5540f, 0.0730f, 356.360f, 63.340f, 2025.12f, zeroDegGlobal, 0.224f, 1f);
    // Herculina
    SectorEntityToken Herculina = calc.spawnSPSObject(system, star, "Herculina", "Herculina", "asteroid", showNameMinor, 168f, 2.7720f, 0.1770f, 107.600f, 57.730f, 2023.36f, zeroDegGlobal, 0.392f, 1f);
}

// =========================================================================
// GROUP 9: LARGE BACKGROUND ASTEROIDS (C-Type / Carbonaceous)
// =========================================================================

// Interamnia
SectorEntityToken Interamnia = calc.spawnSPSObject(system, star, "Interamnia", "Interamnia", "asteroid", showNameMinor, 306f, 3.0580f, 0.1559f, 280.170f, 94.410f, 2023.20f, zeroDegGlobal, 0.364f, 1f);

// Davida
SectorEntityToken Davida = calc.spawnSPSObject(system, star, "Davida", "Davida", "asteroid", showNameMinor, 289f, 3.1660f, 0.1880f, 117.800f, 299.700f, 2024.80f, zeroDegGlobal, 0.214f, 1f);

// Europa
SectorEntityToken EuropaA = calc.spawnSPSObject(system, star, "Europa", "Europa", "asteroid", showNameMinor, 304f, 3.0930f, 0.1110f, 128.580f, 342.910f, 2021.20f, zeroDegGlobal, 0.235f, 1f);

// Elektra
SectorEntityToken Elektra = calc.spawnSPSObject(system, star, "Elektra", "Elektra", "asteroid", showNameMinor, 199f, 3.1260f, 0.2100f, 144.990f, 237.750f, 2020.10f, zeroDegGlobal, 0.218f, 1f);

// S/2003 (130) 1 | as/rp=13.0 | es=0.0835 | Ps=5.287 d | longPeri=360.5° | M=117.3°
SectorEntityToken ElektraBeta = calc.spawnWithEllipticalOrbit(system, Elektra, "ElektraBeta", "130 Beta", "asteroid", showNameProv, calc.getSize(6f), calc.getSize(199f) * 13.0f, 0.0835f, 360.5f, calc.getTime(5.287f), 117.3f, calc.getTime(5.287f) / rotMult);

// S/2014 (130) 1 | as/rp=5.0 | es=0.157 | Ps=1.256 d | longPeri=509.5°→149.5° | M=50.6°
SectorEntityToken ElektraGamma = calc.spawnWithEllipticalOrbit(system, Elektra, "ElektraGamma", "130 Gamma", "asteroid", showNameProv, calc.getSize(2f), calc.getSize(199f) * 5.0f, 0.157f, 149.5f, calc.getTime(1.256f), 50.6f, calc.getTime(1.256f) / rotMult);

// S/2014 (130) 2 (third companion) | as/rp=3.5 | es=? | Ps=0.7 d
SectorEntityToken ElektraDelta = calc.spawnMoon(system, Elektra, "130 Delta", calc.getSize(1.6f), calc.getSize(199f) * 3.5f, calc.getTime(0.7f), 0f, showProvisionalNames);

// GANFENG ALKALI Mining station
SectorEntityToken ganfengStation = DerelictThemeGenerator.addSalvageEntity(system, "station_mining_remnant", Factions.DERELICT);
ganfengStation.setName("GanfengAlkali");
ganfengStation.setCircularOrbitPointingDown(Elektra, 330, 170, calc.getTime(50f));
ganfengStation.setDiscoverable(true);
ganfengStation.setSensorProfile(2000f);

// SALYUT-3 AA GUN MODULE
SectorEntityToken salyutGun = DerelictThemeGenerator.addSalvageEntity(system, "weapons_cache_small_low", Factions.DERELICT);
salyutGun.setName("Salyut-3 AA Gun Module");
salyutGun.setCircularOrbit(star, 230f, calc.getDist(2.8f, star), calc.getTime(1700f));

if(!asteroidBeltShortlist){
    // Bamberga
    SectorEntityToken Bamberga = calc.spawnSPSObject(system, star, "Bamberga", "Bamberga", "asteroid", showNameMinor, 221f, 2.6810f, 0.3410f, 327.850f, 44.150f, 2022.60f, zeroDegGlobal, 1.226f, 1f);
    // Fortuna
    SectorEntityToken Fortuna = calc.spawnSPSObject(system, star, "Fortuna", "Fortuna", "asteroid", showNameMinor, 211f, 2.4410f, 0.1590f, 211.380f, 182.090f, 2024.15f, zeroDegGlobal, 0.310f, 1f);
    // Egeria
    SectorEntityToken Egeria = calc.spawnSPSObject(system, star, "Egeria", "Egeria", "asteroid", showNameMinor, 202f, 2.5760f, 0.0860f, 43.210f, 79.830f, 2021.40f, zeroDegGlobal, 0.294f, 1f);
    // Patientia
    SectorEntityToken Patientia = calc.spawnSPSObject(system, star, "Patientia", "Patientia", "asteroid", showNameMinor, 225f, 3.0600f, 0.0760f, 89.250f, 337.060f, 2022.70f, zeroDegGlobal, 0.405f, 1f);
    // Diotima
    SectorEntityToken Diotima = calc.spawnSPSObject(system, star, "Diotima", "Diotima", "asteroid", showNameMinor, 176f, 3.0680f, 0.0350f, 69.350f, 199.580f, 2023.75f, zeroDegGlobal, 0.199f, 1f);
    // Doris
    SectorEntityToken Doris = calc.spawnSPSObject(system, star, "Doris", "Doris", "asteroid", showNameMinor, 216f, 3.1130f, 0.0670f, 183.440f, 251.020f, 2023.70f, zeroDegGlobal, 0.496f, 1f);
}

// =========================================================================
// ====================== OTHER ACTIVE ASTEROIDS ===========================
// =========================================================================

// 311P/PanSTARRS (P/2013 P5) — active asteroid / main-belt comet
SectorEntityToken P311 = calc.spawnSPSObject(system, star, "311P PanSTARRS", "311P PanSTARRS", "asteroid", showNameProv, 0.6f, 2.1887f, 0.1157f, 279.260f, 143.999f, 2017.52f, zeroDegGlobal, 4.966f, 1f);

// 2006 VW139 | Active eccentric binary asteroid 
// Wierd asf object
SectorEntityToken vw139Barycenter = calc.spawnSPSObject(system, star, "vw139_barycenter", "2006 VW139 Barycenter", "custom_entity", "empty", 1f, 3.050600317f, 0.201470979f, 280.454365f, 83.183311f, 2022.169f, zeroDegGlobal, null, 1f);

float sz_VW139a = (1.69f * 3f);
float sz_VW139b = (1.2f* 3f);
float p_VW139binary = calc.getTime(119f);

SectorEntityToken[] vw139Binary = calc.spawnEllipticalBinary(system, vw139Barycenter,"vw139a", "2006 VW139", sz_VW139a, "asteroid", showNameProv,"vw139b", "2006 VW139 B", sz_VW139b, "asteroid", showNameProv, sz_VW139a * 18f, 0.46f, p_VW139binary, 0f);

SectorEntityToken VW139a = vw139Binary[0];
SectorEntityToken VW139b = vw139Binary[1];

// =========================================================================
// =========================== JUPITER SYSTEM ==============================
// =========================================================================

// ## JUPITER (The Primary)
// Jupiter
float dist_JupiterRaw = 5.2038f;
PlanetAPI Jupiter = (PlanetAPI) calc.spawnSPSObject(system, star, "jupiter", "Jupiter", "gas_giant", null, 142984f, dist_JupiterRaw, 0f, 100.464f, 273.867f, 2023.06f, zeroDegGlobal, null, 1f);
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
ioField.setCircularOrbit(Io, 0, 0, -10);


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
    new GiantMoonsTotal().spawn(system, Jupiter, "jupiter", zeroDegGlobal);
}

// =========================================================================
// ===================== JUPITER Trojans ===================================
// =========================================================================

float p_Jupiter_Libration = p_Jupiter;

float map_1AU_at_Jupiter = (calc.getDist(5.7f, star) - calc.getDist(4.7f, star)) / 2f;

// -------------------------------------------------------------------------
// ## JUPITER L4 GREEK CAMP (Leading +60)
// -------------------------------------------------------------------------

SectorEntityToken L4_Jupiter = system.addCustomEntity(null, "Jupiter L4 Stable Location", "stable_location", "neutral");
L4_Jupiter.setCircularOrbit(star, angleJupiter + 60f, dist_Jupiter, p_Jupiter);

float ang_L4_Jup = angleJupiter + 60f + 90f; // Tangent

// --- L4 GENERIC FIELDS (6 Evenly Spaced Nodes) ---
// calc.spawnBeanEntity(system, L4_Jupiter, null, "Greek Camp", "field", null, 0f, map_1AU_at_Jupiter, 6.0f, 1.2f, ang_L4_Jup, p_Jupiter_Libration, 0f);
// calc.spawnBeanEntity(system, L4_Jupiter, null, "Greek Camp", "field", null, 0f, map_1AU_at_Jupiter, 6.2f, 1.1f, ang_L4_Jup, p_Jupiter_Libration, 60f);
// calc.spawnBeanEntity(system, L4_Jupiter, null, "Greek Camp", "field", null, 0f, map_1AU_at_Jupiter, 5.8f, 1.3f, ang_L4_Jup, p_Jupiter_Libration, 120f);
// calc.spawnBeanEntity(system, L4_Jupiter, null, "Greek Camp", "field", null, 0f, map_1AU_at_Jupiter, 5.2f, 0.8f, ang_L4_Jup, p_Jupiter_Libration, 180f);
// calc.spawnBeanEntity(system, L4_Jupiter, null, "Greek Camp", "field", null, 0f, map_1AU_at_Jupiter, 5.5f, 1.1f, ang_L4_Jup, p_Jupiter_Libration, 240f);
// calc.spawnBeanEntity(system, L4_Jupiter, null, "Greek Camp", "field", null, 0f, map_1AU_at_Jupiter, 5.0f, 0.9f, ang_L4_Jup, p_Jupiter_Libration, 300f);

// Agamemnon | 131 km
// Does its observation arc know where agamemnon was at on 09/11/2001?
float sz_Agamemnon = calc.getSize(131f);
PlanetAPI Agamemnon = (PlanetAPI) (falseMoons ? calc.spawnSPSObject4(system, Jupiter, "Agamemnon", "Agamemnon", "barren", null, sz_Agamemnon, 5.2864f, 0.0669f, 338.020f, 82.416f, 2022.44f, zeroDegGlobal, 0.275f, 1f, p_Jupiter, dist_JupiterRaw, true, dist_Jupiter, angleJupiter + 180f, p_Jupiter, star) : calc.spawnSPSObject2(system, star, "Agamemnon", "Agamemnon", "barren", null, sz_Agamemnon, 5.2864f, 0.0669f, 338.020f, 82.416f, 2022.44f, zeroDegGlobal, 0.275f, 1f, p_Jupiter, dist_JupiterRaw));

Agamemnon.getSpec().setTexture("graphics/planets/agamemnon_tx.jpg");
Agamemnon.getSpec().setPlanetColor(new Color(200, 190, 180, 255));
Agamemnon.getSpec().setAtmosphereThickness(0f);
Agamemnon.getSpec().setAtmosphereThicknessMin(10f);
Agamemnon.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
Agamemnon.getSpec().setIconColor(new Color(140, 130, 120, 255));
Agamemnon.getSpec().setTilt(21.7f);
Agamemnon.getSpec().setPitch(50f);
Agamemnon.getSpec().setRotation(calc.getRot(.275f));
Agamemnon.applySpecChanges();

calc.addConditions(Agamemnon.getMarket(), new String[] {
    "cold",
    "no_atmosphere",
    "low_gravity",
    "rare_ore_sparse",
    "ore_moderate",
    "volatiles_diffuse",
    "sol_meteoroids",
    "poor_light",
    "ruins_scattered",
    "sol_degenerate"
});
// "Agamemnon Beta" — single chord occultation evidence (Timerson et al. 2013)
// ~5 km satellite at ~278 km separation; unconfirmed, single-event detection
if(speculativeBodies){
calc.spawnMoon(system, Agamemnon, "Agamemnon Beta", calc.getSize(5f), sz_Agamemnon * 4.24f, calc.getTime(2.1f), 90f, showProvisionalNames);
}

// 624 HEKTOR SYSTEM | 225 km | a=5.276 AU | contact binary
float sz_Hektor = calc.getSize(225f);
float p_HektorBinary = -0.29f;
float sz_HektorLobe = sz_Hektor * 0.7f;
float[] hekOffsets = calc.getBinaryOffsetsReal(225f, 158f, 1.5f);

float[][] hektorExtras = new float[][]{
    // {a,              ecc, longPeri,      period,          meanAnom, sign}
    { hekOffsets[0],    0f,  0f,            p_HektorBinary,  0f,       +1f }, // binary wobble
    { dist_Jupiter,     0f,  angleJupiter + 180f,  p_Jupiter,       0f,       +1f }  // Jupiter pin
};

PlanetAPI Hektor = (PlanetAPI) calc.spawnSPSObject5(system, Jupiter,
    "Hektor", "Hektor", "barren-bombarded", null,
    225f, 5.2763f, 0.0243f, 342.803f, 180.684f, 2025.58f, zeroDegGlobal,
    null, 1f, null, null,
    hektorExtras,
    star);
Hektor.getSpec().setTexture("graphics/planets/hektor_tx.jpg");
Hektor.getSpec().setPlanetColor(new Color(210, 160, 140, 255));
Hektor.getSpec().setAtmosphereThickness(0f);
Hektor.getSpec().setAtmosphereThicknessMin(10f);
Hektor.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0));
Hektor.getSpec().setIconColor(new Color(160, 100, 80, 255));
Hektor.getSpec().setTilt(18.1f);
Hektor.getSpec().setPitch(90f);
Hektor.getSpec().setRotation(-360f / (p_HektorBinary * 10f));
Hektor.applySpecChanges();

calc.addConditions(Hektor.getMarket(), new String[] {
    "cold",
    "no_atmosphere",
    "low_gravity",
    "ore_moderate",
    "rare_ore_sparse",
    "volatiles_diffuse",
    "sol_meteoroids",
    "ruins_widespread",
    "poor_light",
    "sol_contact_binary",
    "sol_fast_rotator"
});
// Secondary Lobe — orbits Hektor
calc.spawnMoon(system, Hektor, "Hektor (Lobe)", sz_HektorLobe, hekOffsets[0] + hekOffsets[1], p_HektorBinary, 180f, false);

// Hektor barycenter
SectorEntityToken hektorBarycenter = system.addCustomEntity("plutoBarycenter", "plutoBarycenter", "empty", "neutral"); 
hektorBarycenter.setCircularOrbitPointingDown(Hektor, 180f, hekOffsets[0], p_HektorBinary);

// Skamandrios (Moon) — orbits Hektor | as/rp=10.4 | es=0.31 | Ps=2.965 d
calc.spawnWithEllipticalOrbit(system, hektorBarycenter, "Skamandrios", "Skamandrios", "asteroid", showNameMinor, calc.getSize(12f), sz_Hektor * 10.4f, 0.31f, 0f, calc.getTime(2.965f), 90f, null);

// -------------------------------------------------------------------------
// L4 ASTEROID POPULATION (The Greeks) 
// -------------------------------------------------------------------------

if(!jupiterTrojansShortlist){
    // Leonteus | 112 km
    calc.spawnSPSObject2(system, star, "Leonteus", "Leonteus", "asteroid", showNameMinor, 112f, 5.2278f, 0.0909f, 200.527f, 263.269f, 2023.69f, zeroDegGlobal, 0.234f, 1f, p_Jupiter, dist_JupiterRaw);
    // Achilles | 135 km
    calc.spawnSPSObject2(system, star, "Achilles", "Achilles", "asteroid", showNameMinor, 135f, 5.2147f, 0.1483f, 316.533f, 134.215f, 2023.35f, zeroDegGlobal, 0.304f, 1f, p_Jupiter, dist_JupiterRaw);
    // Nestor | 112 km
    calc.spawnSPSObject2(system, star, "Nestor", "Nestor", "asteroid", showNameMinor, 112f, 5.1698f, 0.1157f, 350.757f, 343.679f, 2031.68f, zeroDegGlobal, 0.666f, 1f, p_Jupiter, dist_JupiterRaw);
    // Eurybates | 64 km
    SectorEntityToken Eurybates = calc.spawnSPSObject2(system, star, "Eurybates", "Eurybates", "asteroid", showNameMinor, 64f, 5.2158f, 0.0909f, 43.555f, 28.569f, 2022.27f, zeroDegGlobal, 0.363f, 1f, p_Jupiter, dist_JupiterRaw);
    calc.spawnMoon(system, Eurybates, "Queta", calc.getSize(1.2f), calc.getSize(64f) * 10f, calc.getTime(82.5f), 1f, showMinorNames);
    // Odysseus | 115 km
    calc.spawnSPSObject2(system, star, "Odysseus", "Odysseus", "asteroid", showNameMinor, 115f, 5.2281f, 0.0902f, 221.220f, 238.649f, 2024.15f, zeroDegGlobal, 0.421f, 1f, p_Jupiter, dist_JupiterRaw);
    // Diomedes | 118 km
    calc.spawnSPSObject2(system, star, "Diomedes", "Diomedes", "asteroid", showNameMinor, 118f, 5.2268f, 0.0461f, 315.777f, 131.145f, 2022.36f, zeroDegGlobal, 1.020f, 1f, p_Jupiter, dist_JupiterRaw);
    // Teucer | 89 km
    calc.spawnSPSObject2(system, star, "Teucer", "Teucer", "asteroid", showNameMinor, 89f, 5.1144f, 0.0893f, 69.939f, 49.045f, 2024.18f, zeroDegGlobal, 0.423f, 1f, p_Jupiter, dist_JupiterRaw);
    // Red Rising Reference?!?!
    // Ajax | 84 km
    calc.spawnSPSObject2(system, star, "Ajax", "Ajax", "asteroid", showNameMinor, 84f, 5.2832f, 0.1124f, 332.877f, 61.741f, 2022.09f, zeroDegGlobal, 1.226f, 1f, p_Jupiter, dist_JupiterRaw);
    // Menelaus | 43 km
    calc.spawnSPSObject2(system, star, "Menelaus", "Menelaus", "asteroid", showNameMinor, 43f, 5.2002f, 0.0232f, 240.290f, 299.019f, 2026.75f, zeroDegGlobal, 0.739f, 1f, p_Jupiter, dist_JupiterRaw);
    // Thestor | 69 km 
    calc.spawnSPSObject2(system, star, "Thestor", "Thestor", "asteroid", showNameMinor, 69f, 5.2749f, 0.0561f, 233.700f, 199.846f, 2023.23f, zeroDegGlobal, 0.561f, 1f, p_Jupiter, dist_JupiterRaw);
    // Telamon | 65 km
    calc.spawnSPSObject2(system, star, "Telamon", "Telamon", "asteroid", showNameMinor, 65f, 5.1401f, 0.1086f, 340.835f, 114.469f, 2023.78f, zeroDegGlobal, 0.476f, 1f, p_Jupiter, dist_JupiterRaw);
    // Polymele | 21 km | LUCY flyby planned
    SectorEntityToken Polymele = calc.spawnSPSObject2(system, star, "Polymele", "Polymele", "asteroid", showNameMinor, 21f, 5.1899f, 0.0963f, 50.326f, 5.793f, 2021.72f, zeroDegGlobal, 0.244f, 1f, p_Jupiter, dist_JupiterRaw);
    // "Shaun" | 5km
    calc.spawnMoon(system, Polymele, "Shaun", calc.getSize(5f), calc.getSize(21f) * 19.5f, calc.getTime(15.5f), 90f, showMinorNames);
    // Leucus | 34 km | 446h rotation (extreme slow rotator)
    calc.spawnSPSObject2(system, star, "Leucus", "Leucus", "asteroid", showNameMinor, 34f, 5.3110f, 0.0653f, 251.075f, 162.335f, 2021.70f, zeroDegGlobal, 18.580f, 1f, p_Jupiter, dist_JupiterRaw);
    // Orus | 51 km 
    calc.spawnSPSObject2(system, star, "Orus", "Orus", "asteroid", showNameMinor, 51f, 5.1234f, 0.0370f, 258.551f, 182.781f, 2023.32f, zeroDegGlobal, 0.560f, 1f, p_Jupiter, dist_JupiterRaw);
}

/// =========================================================================
// ## JUPITER L5 TROJAN CAMP (Trailing -60)
// =========================================================================

SectorEntityToken L5_Jupiter = system.addCustomEntity(null, "Jupiter L5 Stable Location", "stable_location", "neutral");
L5_Jupiter.setCircularOrbit(star, angleJupiter - 60f, dist_Jupiter, p_Jupiter);

float ang_L5_Jup = angleJupiter - 60f + 90f;

// --- L5 GENERIC FIELDS (5 Evenly Spaced Nodes) --- UNCHANGED
// calc.spawnBeanEntity(system, L5_Jupiter, null, "Trojan Camp", "field", null, 0f, map_1AU_at_Jupiter, 6.0f, 1.2f, ang_L5_Jup, p_Jupiter_Libration, 0f);
// calc.spawnBeanEntity(system, L5_Jupiter, null, "Trojan Camp", "field", null, 0f, map_1AU_at_Jupiter, 6.2f, 1.1f, ang_L5_Jup, p_Jupiter_Libration, 72f);
// calc.spawnBeanEntity(system, L5_Jupiter, null, "Trojan Camp", "field", null, 0f, map_1AU_at_Jupiter, 5.8f, 1.3f, ang_L5_Jup, p_Jupiter_Libration, 144f);
// calc.spawnBeanEntity(system, L5_Jupiter, null, "Trojan Camp", "field", null, 0f, map_1AU_at_Jupiter, 5.2f, 0.8f, ang_L5_Jup, p_Jupiter_Libration, 216f);
// calc.spawnBeanEntity(system, L5_Jupiter, null, "Trojan Camp", "field", null, 0f, map_1AU_at_Jupiter, 5.5f, 1.1f, ang_L5_Jup, p_Jupiter_Libration, 288f);

// ==========================================
// 617 PATROCLUS-MENOETIUS SYSTEM
// ==========================================

float sz_Patroclus = calc.getSize(122f);
float sz_Menoetius = calc.getSize(113f);
float p_patroclusmenoetius = calc.getTime(4.3f); 

// Peleus Station | Orbital Station 
// Technically this is NEA
SectorEntityToken peleusStation = calc.spawnSPSObject2(
    system, star, "PatMen", "Peleus Station", "custom_entity", "PatMen", 
    140f, 5.2061f, 0.1394f, 44.350f, 308.767f, 2024.50f, zeroDegGlobal, 
    -p_patroclusmenoetius / rotMult, 1f, p_Jupiter, dist_JupiterRaw
);

peleusStation.setInteractionImage("illustrations", "abandoned_station2");
Misc.setAbandonedStationMarket("peleus_station_market", peleusStation);

MarketAPI marketPeleus = peleusStation.getMarket();
marketPeleus.setName("Peleus Station");
marketPeleus.addCondition("abandoned_station");
marketPeleus.addCondition("sol_meteoroids");
marketPeleus.addCondition("volatiles_diffuse");
marketPeleus.addCondition("ore_moderate");
marketPeleus.addCondition("rare_ore_sparse");
marketPeleus.addCondition("cold");
marketPeleus.addCondition("poor_light");
marketPeleus.addCondition("ruins_widespread");
marketPeleus.addCondition("sol_no_atmosphere_bodgejob");

for (MarketConditionAPI condition : marketPeleus.getConditions()) {
    condition.setSurveyed(true);
}

peleusStation.setDiscoverable(true);
peleusStation.setSensorProfile(4000f);

SectorEntityToken PatMenLine = system.addCustomEntity("PatMenLine", "PatMenLine", "PatMenLine", "neutral"); 
PatMenLine.setCircularOrbitPointingDown(peleusStation, 0f, 1, p_patroclusmenoetius);

SectorEntityToken Patroclus = system.addCustomEntity("Patroclus", "Patroclus", "Patroclus", "neutral"); 
Patroclus.setCircularOrbitPointingDown(peleusStation, 0f, 75, p_patroclusmenoetius);

SectorEntityToken Menoetius = system.addCustomEntity("Menoetius", "Menoetius", "Menoetius", "neutral"); 
Menoetius.setCircularOrbitPointingDown(peleusStation, 180f, 88, p_patroclusmenoetius);

// Mentor | 126 km
SectorEntityToken Mentor = calc.spawnSPSObject2(system, star, "Mentor", "Mentor", "asteroid", showNameMinor, 126f, 5.1951f, 0.0746f, 179.479f, 134.727f, 2022.30f, zeroDegGlobal, 0.321f, 1f, p_Jupiter, dist_JupiterRaw);

// -------------------------------------------------------------------------
// L5 ASTEROID POPULATION (The Trojans) 
// -------------------------------------------------------------------------

if(!jupiterTrojansShortlist){
    // Aeneas | 118 km
    calc.spawnSPSObject2(system, star, "Aeneas", "Aeneas", "asteroid", showNameMinor, 118f, 5.2389f, 0.1054f, 247.257f, 52.484f, 2022.28f, zeroDegGlobal, 0.363f, 1f, p_Jupiter, dist_JupiterRaw);
    // Anchises | 100 km
    calc.spawnSPSObject2(system, star, "Anchises", "Anchises", "asteroid", showNameMinor, 100f, 5.2700f, 0.1376f, 283.901f, 41.439f, 2024.18f, zeroDegGlobal, 0.483f, 1f, p_Jupiter, dist_JupiterRaw);
    // Priamus | 101 km
    calc.spawnSPSObject2(system, star, "Priamus", "Priamus", "asteroid", showNameMinor, 101f, 5.2101f, 0.1235f, 301.436f, 337.539f, 2021.61f, zeroDegGlobal, 0.286f, 1f, p_Jupiter, dist_JupiterRaw);
    // Troilus | 100 km | 56h rotation
    calc.spawnSPSObject2(system, star, "Troilus", "Troilus", "asteroid", showNameMinor, 100f, 5.2548f, 0.0926f, 48.547f, 296.719f, 2023.94f, zeroDegGlobal, 2.340f, 1f, p_Jupiter, dist_JupiterRaw);
    // Deiphobus | 118 km | 59h rotation
    calc.spawnSPSObject2(system, star, "Deiphobus", "Deiphobus", "asteroid", showNameMinor, 118f, 5.1352f, 0.0447f, 283.687f, 1.077f, 2021.91f, zeroDegGlobal, 2.444f, 1f, p_Jupiter, dist_JupiterRaw);
    // Glaukos | 48 km
    calc.spawnSPSObject2(system, star, "Glaukos", "Glaukos", "asteroid", showNameMinor, 48f, 5.2366f, 0.0326f, 176.301f, 131.459f, 2023.07f, zeroDegGlobal, 0.249f, 1f, p_Jupiter, dist_JupiterRaw);
    // Astyanax | 28 km
    calc.spawnSPSObject2(system, star, "Astyanax", "Astyanax", "asteroid", showNameMinor, 28f, 5.2183f, 0.0331f, 145.769f, 166.231f, 2024.11f, zeroDegGlobal, 0.272f, 1f, p_Jupiter, dist_JupiterRaw);
    // Helenos | 34 km
    calc.spawnSPSObject2(system, star, "Helenos", "Helenos", "asteroid", showNameMinor, 34f, 5.3198f, 0.0486f, 188.771f, 116.029f, 2022.74f, zeroDegGlobal, null, 1f, p_Jupiter, dist_JupiterRaw);
    // Antenor | 98 km
    calc.spawnSPSObject2(system, star, "Antenor", "Antenor", "asteroid", showNameMinor, 98f, 5.1726f, 0.0138f, 159.018f, 300.965f, 2027.42f, zeroDegGlobal, 0.332f, 1f, p_Jupiter, dist_JupiterRaw);
    // Sarpedon | 77 km
    calc.spawnSPSObject2(system, star, "Sarpedon", "Sarpedon", "asteroid", showNameMinor, 77f, 5.2591f, 0.0176f, 220.789f, 57.247f, 2021.49f, zeroDegGlobal, 0.948f, 1f, p_Jupiter, dist_JupiterRaw);
}

// =========================================================================
// ========================== THE HILDA TRIANGLE ===========================
// =========================================================================
// 3:2 Resonance with Jupiter.
// They orbit the Sun, but their 3:2 period keeps them clustered in a triangle relative to Jupiter.

float p_Hilda = p_Jupiter * (2f/3f); 
// The mathematically perfect 3:2 resonant SMA to prevent 8,000-year drift
float hildaResonantSMA = dist_JupiterRaw * (float)Math.pow(2.0/3.0, 2.0/3.0); // ~3.9712f

// 153 Hilda | 171 km 
PlanetAPI Hilda = (PlanetAPI) (falseMoons ? calc.spawnSPSObject4(system, Jupiter, "Hilda", "Hilda", "barren-bombarded", null, 171f, 3.9689f, 0.1385f, 228.080f, 39.155f, 2023.39f, zeroDegGlobal, 0.248f, 1f, p_Hilda, hildaResonantSMA, true, dist_Jupiter, angleJupiter + 180f, p_Jupiter, star) : calc.spawnSPSObject2(system, star, "Hilda", "Hilda", "barren", null, 171f, 3.9689f, 0.1385f, 228.080f, 39.155f, 2023.39f, zeroDegGlobal, 0.248f, 1f, p_Hilda, hildaResonantSMA));

Hilda.getSpec().setTexture("graphics/planets/hilda_tx.jpg"); 
Hilda.getSpec().setPlanetColor(new Color(180, 170, 170, 255)); 
Hilda.getSpec().setAtmosphereThickness(0f); 
Hilda.getSpec().setAtmosphereThicknessMin(0f); 
Hilda.getSpec().setAtmosphereColor(new Color(180, 170, 170, 255)); 
Hilda.getSpec().setIconColor(new Color(180, 170, 170, 255)); 
Hilda.getSpec().setTilt(7.8f);
Hilda.getSpec().setPitch(60f); 
Hilda.getSpec().setRotation(calc.getRot(0.248f)); 
Hilda.applySpecChanges();

calc.addConditions(Hilda.getMarket(), new String[] {
    "low_gravity",
    "no_atmosphere",
    "volatiles_trace",
    "ore_sparse",
    "cold",
    "ruins_scattered",
    "sol_meteoroids"
});
// 1911 Schubart | 67 km
calc.spawnSPSObject2(system, star, "Schubart", "Schubart", "asteroid", showNameMinor, 67f, 3.9807f, 0.1731f, 284.773f, 181.886f, 2022.58f, zeroDegGlobal, 0.496f, 1f, p_Hilda, hildaResonantSMA);

if(!asteroidBeltShortlist){
    // 190 Ismene | 159 km 
    calc.spawnSPSObject2(system, star, "Ismene", "Ismene", "asteroid", showNameMinor, 159f, 3.9949f, 0.1683f, 175.396f, 271.448f, 2029.54f, zeroDegGlobal, 0.272f, 1f, p_Hilda, hildaResonantSMA);
    // 361 Bononia | 154 km
    calc.spawnSPSObject2(system, star, "Bononia", "Bononia", "asteroid", showNameMinor, 154f, 3.9786f, 0.2107f, 18.557f, 66.823f, 2025.11f, zeroDegGlobal, 0.576f, 1f, p_Hilda, hildaResonantSMA);
}

// =========================================================================
// ============================= SATURN SYSTEM =============================
// =========================================================================

// ## SATURN (The Primary)--------------------------------------------------
// Saturn
float dist_SaturnRaw = 9.5826f;
PlanetAPI Saturn = (PlanetAPI) calc.spawnSPSObject(system, star, "saturn", "Saturn", "gas_giant", null, 120536f, dist_SaturnRaw, 0f, 113.665f, 339.392f, 2032.91f, zeroDegGlobal, null, 1f);
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

calc.smartRingTex(system, Saturn, "sol_rings", "saturn_rings0", 8389, 0, satRingGameStart, satRingGameEnd, -10);
calc.setSatRingScale(satRingGameStart, w_satRings);

// SATURN MAGNETOSPHERE
float satMagOuter = calc.getSatRingPos(66900f) - 60f;
SectorEntityToken saturnField = system.addTerrain(Terrain.MAGNETIC_FIELD, new MagneticFieldTerrainPlugin.MagneticFieldParams(
    satMagOuter, satMagOuter / 2f, Saturn, sz_Saturn * 0.9f, sz_Saturn * 1.7f, new Color(230, 200, 120, 10), .5f));
saturnField.setCircularOrbit(Saturn, 0, 0, -30);
saturnField.setName("Saturn's Magnetosphere");

// Ring System terrain (D Ring → F Ring)
calc.smartRingTerrain(system, Saturn, "Saturn Ring System", calc.getSatRingPos(66900f), calc.getSatRingPos(140180f), -0.40f);

// F Ring visual boost
calc.smartRingTex(system, Saturn, "sol_rings", "rings_alpha3", 256, 3, calc.getSatRingPos(140180f) - 3.5f, calc.getSatRingPos(140180f) + 3.5f, -10);

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
    new GiantMoonsTotal().spawn(system, Saturn,  "saturn",  zeroDegGlobal);
}
// =========================================================================
// ## SATURN TROJAN
// =========================================================================
// Singular cuz jupiter and neptune really scammed the middle gas giants of their trojans.

// 2019 UO14 | ~77 km
SectorEntityToken UO14 = calc.spawnSPSObject2(system, star, "UO14", "2019 UO14", "asteroid", showNameProv, 77f, 9.7969f, 0.2350f, 244.642f, 144.366f, 2019.78f, zeroDegGlobal, 0.947f, 1f, p_Saturn, dist_SaturnRaw);
UO14.setCustomDescriptionId("sol_uo14");

// =========================================================================
// ============================= URANUS SYSTEM =============================
// =========================================================================
// for some processed sexy moon pix
// https://www.sciencedirect.com/science/article/pii/S0019103501965972?ref=pdf_download&fr=RR-2&rr=9e6a82bfad1b24e5

// ## URANUS (The Primary)--------------------------------------------------
// Uranus | Semi Major Axis: 19.19 AU | Diameter: ~50,724 km | Period: 30,660 days
float dist_UranusRaw = 19.1913f;
PlanetAPI Uranus = (PlanetAPI) calc.spawnSPSObject(system, star, "uranus", "Uranus", "ice_giant", null, 51118f, dist_UranusRaw, 0f, 74.006f, 96.9989f, 2050.63f, zeroDegGlobal, null, 1f);
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
uranusInnerField.setCircularOrbit(Uranus, 0, 0, -30);
uranusInnerField.setName("Uranus' Magnetosphere");

// ## URANUS RINGS----------------------------------------------------------

// Zeta Ring (1986U2R) - broad, faint inner dusty ring
// Real (extended): 26,840–39,500 km → 0.000179–0.000264 AU
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha0", 256, 0, calc.getDistUranus(0.000179f), calc.getDistUranus(0.000247f), -30);
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha1", 256, 0, calc.getDistUranus(0.000247f), calc.getDistUranus(0.000264f), -30);

// Inner Narrow Rings (6, 5, 4, Alpha, Beta, Eta, Gamma, Delta)
// Real: 41,837–48,300 km → 0.000280–0.000323 AU
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000278f), calc.getDistUranus(0.000282f), -30); // 6
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000281f), calc.getDistUranus(0.000284f), -30); // 5
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000283f), calc.getDistUranus(0.000287f), -30); // 4
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000297f), calc.getDistUranus(0.000301f), -30); // Alpha
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000303f), calc.getDistUranus(0.000307f), -30); // Beta
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000313f), calc.getDistUranus(0.000317f), -30); // Eta
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000316f), calc.getDistUranus(0.000320f), -30); // Gamma
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000321f), calc.getDistUranus(0.000325f), -30); // Delta

// Lambda Ring — Real: ~50,024 km → 0.000334 AU
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha2", 256, 1, calc.getDistUranus(0.000332f), calc.getDistUranus(0.000336f), -30);

// Terrain: Dense Zeta through Lambda
calc.smartRingTerrain(system, Uranus, "Inner Ring System", calc.getDistUranus(0.000247f), calc.getDistUranus(0.000336f), -0.25f);

// Epsilon Ring — Real: ~51,149 km → 0.000342 AU
calc.smartRingTex(system, Uranus, "misc", "rings_dust0", 256, 1, calc.getDistUranus(0.000336f), calc.getDistUranus(0.000341f), -30);
calc.smartRingTex(system, Uranus, "misc", "rings_special0", 256, 1, calc.getDistUranus(0.000340f), calc.getDistUranus(0.000345f), -30);
calc.smartRingTerrain(system, Uranus, "Epsilon Ring", calc.getDistUranus(0.000336f), calc.getDistUranus(0.000345f), -0.30f);

// Nu Ring — Real: 66,100–69,900 km → 0.000442–0.000467 AU
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha0", 256, 0, calc.getDistUranus(0.000442f), calc.getDistUranus(0.000467f), -30);
calc.smartRingTerrain(system, Uranus, "Nu Ring", calc.getDistUranus(0.000442f), calc.getDistUranus(0.000467f), -0.35f);

// Mu Ring — Real: 86,000–103,000 km → 0.000575–0.000689 AU
calc.smartRingTex(system, Uranus, "sol_rings", "rings_alpha1", 256, 0, calc.getDistUranus(0.000575f), calc.getDistUranus(0.000689f), -30);
calc.smartRingTerrain(system, Uranus, "Mu Ring", calc.getDistUranus(0.000575f), calc.getDistUranus(0.000689f), -0.40f);

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

// =========================================================================
// ========================= URANUS TROJANS ================================
// =========================================================================

// 2011 QF99 | 60 km
SectorEntityToken QF99 = calc.spawnSPSObject2(system, star, "QF99", "2011 QF99", "asteroid", showNameProv, 60f, 19.2150f, 0.1804f, 222.436f, 286.218f, 2034.94f, zeroDegGlobal, null, 1f, p_Uranus, dist_UranusRaw);

SectorEntityToken QF99Probe = DerelictThemeGenerator.addSalvageEntity(system, Entities.DERELICT_SURVEY_PROBE, Factions.DERELICT); 
QF99Probe.setCircularOrbitPointingDown(QF99, 45, 40f, calc.getTime(10f));

// 2014 YX49 | 70 km
SectorEntityToken YX49 = calc.spawnSPSObject2(system, star, "YX49", "2014 YX49", "asteroid", showNameProv, 70f, 19.1762f, 0.2771f, 91.300f, 281.105f, 1999.63f, zeroDegGlobal, null, 1f, p_Uranus, dist_UranusRaw);

/// =========================================================================
// =========================== NEPTUNE SYSTEM ==============================
// =========================================================================

// Neptune
float dist_NeptuneRaw = 30.07f;
PlanetAPI Neptune = (PlanetAPI) calc.spawnSPSObject(system, star, "neptune", "Neptune", "ice_giant", null, 49244f, 30.07f, 0f, 131.783f, 273.187f, 2042.67f, zeroDegGlobal, null, 1f);
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
neptuneInnerField.setCircularOrbit(Neptune, 0, 0, -100);
neptuneInnerField.setName("Neptune's Magnetosphere");

// =========================================================================
// NEPTUNE RINGS
// =========================================================================

// Galle Ring — Real: 40,900–42,900 km → 0.000273–0.000287 AU
calc.smartRingTex(system, Neptune, "sol_rings", "rings_alpha1", 256, 0, calc.getDistNeptune(0.000273f), calc.getDistNeptune(0.000287f), -30);
calc.smartRingTerrain(system, Neptune, "Galle Ring", calc.getDistNeptune(0.000273f), calc.getDistNeptune(0.000287f), -0.20f);

// Le Verrier Ring — Real: ~53,200 km → 0.000356 AU
calc.smartRingTex(system, Neptune, "misc", "rings_dust0", 256, 1, calc.getDistNeptune(0.000354f), calc.getDistNeptune(0.000358f), -30);

// Lassell Ring — Real: 53,200–57,200 km → 0.000356–0.000382 AU
calc.smartRingTex(system, Neptune, "sol_rings", "rings_alpha1", 256, 0, calc.getDistNeptune(0.000358f), calc.getDistNeptune(0.000382f), -30);
calc.smartRingTex(system, Neptune, "sol_rings", "rings_alpha0", 256, 1, calc.getDistNeptune(0.000362f), calc.getDistNeptune(0.000378f), -30);

// Arago Ring — Real: ~57,200 km → 0.000382 AU
calc.smartRingTex(system, Neptune, "misc", "rings_dust0", 256, 1, calc.getDistNeptune(0.000380f), calc.getDistNeptune(0.000384f), -30);

// Le Verrier–Arago terrain
calc.smartRingTerrain(system, Neptune, "Le Verrier–Arago Rings", calc.getDistNeptune(0.000354f), calc.getDistNeptune(0.000384f), -0.28f);

// Adams Ring — Real: ~62,930 km → 0.000421 AU
calc.smartRingTex(system, Neptune, "misc", "rings_special0", 256, 1, calc.getDistNeptune(0.000420f), calc.getDistNeptune(0.000422f), -30);
calc.smartRingTerrain(system, Neptune, "Adams Ring", calc.getDistNeptune(0.000420f), calc.getDistNeptune(0.000422f), -0.38f);

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

// =========================================================================
// ========================== NEPTUNE TROJANS ==============================
// =========================================================================

// -------------------------------------------------------------------------
// NEPTUNE QUASI-MOON
// -------------------------------------------------------------------------

// 2007 RW10 | largest known co-orbital in the solar system
SectorEntityToken RW10 = calc.spawnSPSObject2(system, star, "2007 RW10", "2007 RW10", "asteroid", showNameProv, 250f, 30.3368f, 0.2977f, 187.028f, 96.729f, 1985.29f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw); // 
RW10.setCustomDescriptionId("sol_rw10"); 

// -------------------------------------------------------------------------
// NEPTUNE L4 (Leading +60)
// -------------------------------------------------------------------------

SectorEntityToken L4_Center = system.addCustomEntity(null, "Neptune L4 Stable Location", "stable_location", "neutral");
L4_Center.setCircularOrbit(star, angleNeptune + 60f, dist_Neptune, p_Neptune);
float ang_L4 = angleNeptune + 60f + 90f; 

// Otrera | 42 km | a=30.319 AU
SectorEntityToken Otrera = calc.spawnSPSObject2(system, star, "Otrera", "Otrera", "asteroid", showNameMinor, 42f, 30.3193f, 0.0319f, 34.803f, 7.780f, 2021.59f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);

// Clete | 100 km | a=30.312 AU
PlanetAPI Clete = (PlanetAPI) (falseMoons ? calc.spawnSPSObject4(system, Neptune, "Clete", "Clete", "rocky_ice", null, 100f, 30.3121f, 0.0568f, 169.382f, 300.673f, 2050.36f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw, true, dist_Neptune, angleNeptune + 180f, p_Neptune, star) : calc.spawnSPSObject2(system, star, "Clete", "Clete", "rocky_ice", null, 100f, 30.3121f, 0.0568f, 169.382f, 300.673f, 2050.36f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw));

Clete.getSpec().setTexture("graphics/planets/clete_tx.jpg"); 
Clete.getSpec().setAtmosphereThickness(0f); 
Clete.getSpec().setAtmosphereThicknessMin(0f); 
Clete.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
Clete.getSpec().setIconColor(new Color(130, 110, 100, 255)); 
Clete.getSpec().setTilt(0f); 
Clete.getSpec().setPitch(30f); 
Clete.getSpec().setRotation(calc.getRot(calc.getRandomRotationPeriod(100f)));//UNKOWN 
Clete.applySpecChanges(); 

if (!isSettled) {
calc.addConditions(Clete.getMarket(), new String[] {
    "very_cold",
    "low_gravity",
    "no_atmosphere",
    "volatiles_plentiful",
    "ore_sparse",
    "ruins_widespread",
    "rare_ore_sparse",
    "dark",
    "sol_dist_abyssal"
});} else {
    Clete.getMarket().addCondition("sol_porus");
}

// --- L4 NAMED TROJANS ---

// 2011 WG157 | 160 km
SectorEntityToken WG157 = calc.spawnSPSObject2(system, star, "2011 WG157", "2011 WG157", "asteroid", showNameProv, 160f, 30.2142f, 0.0248f, 352.182f, 203.841f, 2081.11f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);

// 2001 QR322 | 140 km
SectorEntityToken QR322 = calc.spawnSPSObject2(system, star, "2001 QR322", "2001 QR322", "asteroid", showNameProv, 140f, 30.3907f, 0.0266f, 151.579f, 169.170f, 1985.55f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);

// 2013 RC158 | 140 km
SectorEntityToken RC158 = calc.spawnSPSObject2(system, star, "2013 RC158", "2013 RC158", "asteroid", showNameProv, 140f, 30.2048f, 0.0562f, 81.624f, 81.061f, 2079.08f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);

// 2010 EN65 (secretly wearing 2011 SO277's skin) | 176 km
SectorEntityToken EN65 = calc.spawnSPSObject2(system, star, "2010 EN65", "2010 EN65", "asteroid", showNameProv, 176f, 30.3150f, 0.0069f, 113.609f, 108.174f, 2107.38f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);
EN65.setCustomDescriptionId("sol_en65");

// 2014 QO441 | 130 km
SectorEntityToken QO441 = calc.spawnSPSObject2(system, star, "2014 QO441", "2014 QO441", "asteroid", showNameProv, 130f, 30.2432f, 0.1007f, 107.104f, 113.644f, 2103.92f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);
QO441.setCustomDescriptionId("sol_qo441");

// 2006 RJ103 | 120 km
SectorEntityToken RJ103 = calc.spawnSPSObject2(system, star, "2006 RJ103", "2006 RJ103", "asteroid", showNameProv, 120f, 30.2174f, 0.0320f, 120.933f, 17.062f, 2060.01f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);
SectorEntityToken cryo = system.addCustomEntity(null, null, "derelict_cryosleeper", "neutral");
cryo.setCircularOrbitWithSpin(RJ103, 150, 1000, p_Neptune, 0,-50);
cryo.setDiscoverable(true);
cryo.setSensorProfile(4000f);

// 2013 VX30 | 150 km
SectorEntityToken VX30 = calc.spawnSPSObject2(system, star, "2013 VX30", "2013 VX30", "asteroid", showNameProv, 150f, 30.2808f, 0.0890f, 192.514f, 217.223f, 2019.99f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);
VX30.setCustomDescriptionId("sol_vx30");

// --- SMALL L4 BODIES ---
if(!neptuneTrojansShortlist){
// HOW THE FUCK DID AN AI THINK 2008 GW171 IS A NEPTUNE TROJAN?!?!
// 2010 TT191 | 30 km
calc.spawnSPSObject2(system, star, "2010 TT191", "2010 TT191", "asteroid", showNameProv, 30f, 30.2667f, 0.0642f, 249.251f, 6.879f, 2105.84f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);
// 2007 VL305 | 30 km
calc.spawnSPSObject2(system, star, "2007 VL305", "2007 VL305", "asteroid", showNameProv, 30f, 30.2290f, 0.0683f, 188.589f, 219.848f, 2014.04f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);
// 2012 UD185 | 30 km
calc.spawnSPSObject2(system, star, "2012 UD185", "2012 UD185", "asteroid", showNameProv, 30f, 30.3584f, 0.0390f, 246.216f, 351.914f, 2106.07f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);
// 2014 SC374 | 30 km
calc.spawnSPSObject2(system, star, "2014 SC374", "2014 SC374", "asteroid", showNameProv, 30f, 30.2344f, 0.0944f, 176.264f, 43.420f, 2100.19f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);
// 2014 UU240 | 30 km
calc.spawnSPSObject2(system, star, "2014 UU240", "2014 UU240", "asteroid", showNameProv, 30f, 30.2043f, 0.0489f, 82.058f, 67.472f, 2068.61f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);
// 2015 RW277 | 30 km
calc.spawnSPSObject2(system, star, "2015 RW277", "2015 RW277", "asteroid", showNameProv, 30f, 30.1407f, 0.0778f, 1.320f, 129.440f, 2068.83f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);
// 2015 VU207 | 30 km
calc.spawnSPSObject2(system, star, "2015 VU207", "2015 VU207", "asteroid", showNameProv, 30f, 30.1850f, 0.0360f, 114.252f, 33.229f, 2072.63f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);
// 2015 VV165 | 30 km
calc.spawnSPSObject2(system, star, "2015 VV165", "2015 VV165", "asteroid", showNameProv, 30f, 30.3232f, 0.0913f, 225.102f, 243.895f, 2039.85f, zeroDegGlobal, null, 1f, p_Neptune,dist_NeptuneRaw);
// 2015 VW165 | 30 km
calc.spawnSPSObject2(system, star, "2015 VW165", "2015 VW165", "asteroid", showNameProv, 30f, 30.2929f, 0.0538f, 78.939f, 59.343f, 2054.99f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);
// 2012 UV177 | 80 km
calc.spawnSPSObject2(system, star, "2012 UV177", "2012 UV177", "asteroid", showNameProv, 80f, 30.2074f, 0.0769f, 265.711f, 201.786f, 2044.52f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);
}

// -------------------------------------------------------------------------
// ## NEPTUNE L5 ANCHOR (Trailing -60)
// -------------------------------------------------------------------------

SectorEntityToken L5_Anchor = system.addCustomEntity(null, "Neptune L5 Stable Location", "stable_location", "neutral");
L5_Anchor.setCircularOrbit(star, angleNeptune - 60f, dist_Neptune, p_Neptune);

float ang_L5 = angleNeptune - 60f + 90f;

// POLYSO STATION | Orbital Station | Neptune L5
// secretly 2011 HM102 | 100km
// Luddic Path Size 5 Colony
SectorEntityToken polystation = (falseMoons ? calc.spawnSPSObject4(system, Neptune, "polyso_station", "Polyso", "custom_entity", "Polyso", 100f, 30.0498f, 0.0784f, 101.055f, 151.548f, 2002.26f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw, true, dist_Neptune, angleNeptune + 180f, p_Neptune, star) : calc.spawnSPSObject2(system, star, "polyso_station", "Polyso", "custom_entity", "Polyso", 100f, 30.0498f, 0.0784f, 101.055f, 151.548f, 2002.26f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw));

if (!isSettled) {
    polystation.setInteractionImage("illustrations", "abandoned_station2");
    Misc.setAbandonedStationMarket("marketPolysoPath", polystation);

    polystation.getMarket().setName("Polyso Station");
    polystation.getMarket().addCondition("very_cold");
    polystation.getMarket().addCondition("dark");
    polystation.getMarket().addCondition("sol_dist_abyssal");

    for (MarketConditionAPI condition : polystation.getMarket().getConditions()) {
        condition.setSurveyed(true);
    }

    polystation.setDiscoverable(true);
    polystation.setSensorProfile(4000f);
}

// -------------------------------------------------------------------------
// ## OTHER L5 TROJANS
// -------------------------------------------------------------------------

// 2008 LC18 | 100 km | a=29.887 AU
SectorEntityToken LC18 = calc.spawnSPSObject2(system, star, "2008 LC18", "2008 LC18", "asteroid", showNameProv, 100f, 29.8871f, 0.0857f, 88.581f, 5.808f, 2096.49f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);
LC18.setCustomDescriptionId("sol_lc18");

// 2004 KV18 | 56 km | a=30.088 AU | e=0.186 (high for a trojan)
SectorEntityToken KV18 = calc.spawnSPSObject2(system, star, "2004 KV18", "2004 KV18", "asteroid", showNameProv, 56f, 30.0877f, 0.1861f, 235.689f, 293.854f, 1984.61f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);
KV18.setCustomDescriptionId("sol_kv18");

SectorEntityToken KV18Ship = DerelictThemeGenerator.addSalvageEntity(system, Entities.DERELICT_SURVEY_SHIP, Factions.DERELICT);
KV18Ship.setCircularOrbitWithSpin(KV18, 90, 40f, calc.getTime(5f), 10, -10);

// 2013 KY18 | 30 km | a=30.031 AU | e=0.121
calc.spawnSPSObject2(system, star, "2013 KY18", "2013 KY18", "asteroid", showNameProv, 30f, 30.0310f, 0.1207f, 84.424f, 272.927f, 2056.59f, zeroDegGlobal, null, 1f, p_Neptune, dist_NeptuneRaw);

new cometsCentaursTNOs().spawn(system, star, zeroDegGlobal);

SectorEntityToken SolIX = system.getEntityById("SolIX");
SectorEntityToken DeeDee = system.getEntityById("DeeDee");
SectorEntityToken Eris = system.getEntityById("Eris");
SectorEntityToken Chiminigagua = system.getEntityById("Chiminigagua");
SectorEntityToken Namaka = system.getEntityById("Namaka");
SectorEntityToken Farfarout = system.getEntityById("Farfarout");
SectorEntityToken Pluto = system.getEntityById("Pluto");
SectorEntityToken Haumea = system.getEntityById("Haumea");
SectorEntityToken Chiron = system.getEntityById("Chiron");
SectorEntityToken SolX = system.getEntityById("SolX");
SectorEntityToken Nix = system.getEntityById("Nix");
SectorEntityToken Farout = system.getEntityById("Farout");

system.updateAllOrbits();
system.autogenerateHyperspaceJumpPoints(true, false);

// They are small, and low density
if(Uranus_And_Neptune_Have_Normal_Gravity){
    Uranus.getMarket().removeCondition("high_gravity");
    Neptune.getMarket().removeCondition("high_gravity");
    if(planetNine){
        SolIX.getMarket().removeCondition("high_gravity");
    }
}

// =========================================================================
// Deep space probes 
// =========================================================================
// Dawn deorbits in a century, and we like to smash probes into what were studying to gleem some last bit of science.
// Graveyard orbits would still exist in earth and mars-3 might last 10,000 years 
// 1 day rotational period even though by game start these will likely be the most super of superrotating objects, with rotational periods in the fractions of a second

if(!(deepSpaceProbes == 0)){
    //Assuming Esa doesnt order a suicide
    calc.spawnSPSObject(system, star, "lucy", "Lucy", "custom_entity", "Sol_probe", 0.005f, 3.30f, 0.70f, 0f, 308f, 2033.17f, zeroDegGlobal, 1f, 1f);
    //Assuming JAXA doesnt order a suicide
    calc.spawnSPSObject(system, star, "hayabusa2", "Hayabusa2", "custom_entity", "Sol_probe", 0.005f, 1.18f, 0.17f, 0f, 150f, 2020.93f, zeroDegGlobal, 1f, 1f);

    calc.spawnSPSObject(system, star, "mariner_10", "Mariner 10", "custom_entity", "Sol_probe", 0.005f, 0.58f, 0.23f, 0f, 185f, 1973.84f, zeroDegGlobal, 1f, 1f);
    //Assuming CNSA doesnt order a suicide
    SectorEntityToken Tianwen = system.addCustomEntity("Tianwen-2" , "Tianwen-2", "Sol_probe", "neutral");
    Tianwen.setCircularOrbitWithSpin(P311, 180, 50, 10, -10, -10);
}
if(deepSpaceProbes == 2){
    calc.spawnSPSObject(system, star, "helios_a", "Helios-A", "custom_entity", "Sol_probe", 0.005f, 0.65f, 0.5218f, 0f, 258f, 1975.20f, zeroDegGlobal, 1f, 1f);

    calc.spawnSPSObject(system, star, "helios_b", "Helios-B", "custom_entity", "Sol_probe", 0.005f, 0.635f, 0.5456f, 0f, 270f, 1976.29f, zeroDegGlobal, 1f, 1f);

    calc.spawnSPSObject(system, star, "giotto", "Giotto", "custom_entity", "Sol_probe", 0.005f, 0.885f, 0.17334f, 0f, 310f, 1992.52f, zeroDegGlobal, 1f, 1f);

    calc.spawnSPSObject(system, star, "stardust", "Stardust", "custom_entity", "Sol_probe", 0.005f, 1.58f, 0.35f, 0f, 300f, 1999.10f, zeroDegGlobal, 1f, 1f);

    calc.spawnSPSObject(system, star, "solar_orbiter", "Solar Orbiter", "custom_entity", "Sol_probe", 0.005f, 0.595f, 0.53f, 0f, 260f, 2020.11f, zeroDegGlobal, 1f, 1f);

    calc.spawnSPSObject(system, star, "mariner_2", "Mariner 2", "custom_entity", "Sol_probe", 0.005f, 0.82f, 0.15f, 0f, 75f, 1962.66f, zeroDegGlobal, 1f, 1f);
}

// =========================================================================
// Industrial evolution
// =========================================================================

if (Global.getSettings().getModManager().isModEnabled("IndEvo")) { 

    SectorEntityToken SunYards = system.addCustomEntity(null , null, "IndEvo_GachaStation", "neutral");
    SunYards.setCircularOrbitPointingDown(star, 180, dist_VulcanShunt, p_Vulcan );

    SunYards.setDiscoverable(true); 
    SunYards.setSensorProfile(4000f);

    Mercury.getMarket().addCondition("IndEvo_ArtilleryStationCondition");
    Luna.getMarket().addCondition("IndEvo_ArtilleryStationCondition");
    Mars.getMarket().addCondition("IndEvo_ArtilleryStationCondition");

    SectorEntityToken watchtowerZoozve= system.addCustomEntity(null , null, "IndEvo_Watchtower", "remnant");
    watchtowerZoozve.setCircularOrbitPointingDown(Zoozve, 0, 50, calc.getTime(10f));
    watchtowerZoozve.setDiscoverable(true); 
    watchtowerZoozve.setSensorProfile(1000f);

    SectorEntityToken watchtowerKamooalewa= system.addCustomEntity(null , null, "IndEvo_Watchtower", "remnant");
    watchtowerKamooalewa.setCircularOrbitPointingDown(Kamooalewa, 0, 50, calc.getTime(10f));
    watchtowerKamooalewa.setDiscoverable(true); 
    watchtowerKamooalewa.setSensorProfile(1000f);

    SectorEntityToken watchtowerElara= system.addCustomEntity(null , null, "IndEvo_Watchtower", "remnant");
    watchtowerElara.setCircularOrbitPointingDown(Elara, 0, 100, calc.getTime(10f));
    watchtowerElara.setDiscoverable(true); 
    watchtowerElara.setSensorProfile(1000f);

    SectorEntityToken watchtowerPhoebe= system.addCustomEntity(null, null, "IndEvo_Watchtower", "remnant");
    watchtowerPhoebe.setCircularOrbitPointingDown(Phoebe, 180, 100, calc.getTime(10f));
    watchtowerPhoebe.setDiscoverable(true); 
    watchtowerPhoebe.setSensorProfile(1000f);

    SectorEntityToken watchtowerNereid= system.addCustomEntity(null, null, "IndEvo_Watchtower", "remnant");
    watchtowerNereid.setCircularOrbitPointingDown(Nereid, 180, 100, calc.getTime(.48f) * rotMult);
    watchtowerNereid.setDiscoverable(true); 
    watchtowerNereid.setSensorProfile(1000f);

    SectorEntityToken neptunePets= system.addCustomEntity(null, null, "IndEvo_abandonedPetCenter", "neutral");
    neptunePets.setCircularOrbitPointingDown(WG157, 50, 50, calc.getTime(5f));
    neptunePets.setDiscoverable(true); 
    neptunePets.setSensorProfile(2000f);

    Mercury.getMarket().addCondition("IndEvo_RuinsCondition");
    Luna.getMarket().addCondition("IndEvo_RuinsCondition");
    Mars.getMarket().addCondition("IndEvo_RuinsCondition");
    Ceres.getMarket().addCondition("IndEvo_RuinsCondition");
    Callisto.getMarket().addCondition("IndEvo_RuinsCondition");
    Titan.getMarket().addCondition("IndEvo_RuinsCondition");
    Iapetus.getMarket().addCondition("IndEvo_RuinsCondition");
    Vesta.getMarket().addCondition("IndEvo_RuinsCondition");
    Oberon.getMarket().addCondition("IndEvo_RuinsCondition");
    Rhea.getMarket().addCondition("IndEvo_RuinsCondition");
    Triton.getMarket().addCondition("IndEvo_RuinsCondition");
    // Some code migrated to CometsCentaursTNOs
    // Biden.getMarket().addCondition("IndEvo_mineFieldCondition");
}

// =========================================================================
// Tasc difficulty
// =========================================================================
if (Global.getSettings().getModManager().isModEnabled("Terraforming & Station Construction")) {
    Mercury.getMarket().addCondition("pollution");
    Earth.getMarket().addCondition("pollution");
    Mars.getMarket().addCondition("pollution");
    Phobos.getMarket().addCondition("pollution");
    Ceres.getMarket().addCondition("pollution");
    Vesta.getMarket().addCondition("pollution");
}

// =========================================================================
// Aotd compat
// =========================================================================
if (Global.getSettings().getModManager().isModEnabled("aotd_vok")) {
    Phoebe.getMarket().addCondition("pre_collapse_facility");
    Farout.getMarket().addCondition("pre_collapse_facility");
    Chiminigagua.getMarket().addCondition("pre_collapse_facility");
    Agamemnon.getMarket().addCondition("pre_collapse_facility");
    Chiron.getMarket().addCondition("pre_collapse_facility");
    if(planetTen){
    SolX.getMarket().addCondition("pre_collapse_facility");
}
}

// =========================================================================
// Remnants
// =========================================================================
if(remnantHorde){
    // Small remnant fleets
    // RemnantSeededFleetManager solRemnants = new RemnantSeededFleetManager(system, 5, 5,5, 20, 0.5f );
    // system.addScript(solRemnants);

    // --- Mercury Remnant Orbit ---
    float mercNexusRadius = sz_Mercury + 75f;
    float mercNexusPeriod = calc.getTime(10f);

    // 1. First Nexus (0°)
    CampaignFleetAPI mercNexus1 = RemnantNexusFactory.spawnNexus(system, Mercury, "remnant_station2_Standard", 0f, mercNexusRadius, mercNexusPeriod, 5, 30, 40, null);

    // 2. Second Nexus (72°)
    CampaignFleetAPI mercNexus2 = RemnantNexusFactory.spawnNexus(system, Mercury, "remnant_station2_Standard", 72f, mercNexusRadius, mercNexusPeriod, 5, 30, 40, null);

    // 3. Third Nexus (Damaged) (144°)
    CampaignFleetAPI mercNexus3 = RemnantNexusFactory.spawnNexus(system, Mercury, "remnant_station2_Damaged", 144f, mercNexusRadius, mercNexusPeriod, 2, 5, 25, null);

    // 4. Vambrace Wreck (216°)
    SectorEntityToken nexusWreck = system.addCustomEntity("nexusWreck", "Nexus Wreckage", "derelict_vambrace", "derelict");
    nexusWreck.setCircularOrbitPointingDown(Mercury, 216f, mercNexusRadius, mercNexusPeriod);

    SectorEntityToken nexusNav = system.addCustomEntity("nexusNav", "Nexus Nav Buoy", "nav_buoy", "remnant");
    nexusNav.setCircularOrbitPointingDown(Mercury, 288f, mercNexusRadius, mercNexusPeriod);
    nexusNav.setDiscoverable(true);

    // Titan Nexus
    CampaignFleetAPI titanNexus = RemnantNexusFactory.spawnNexus(system, Titan, "remnant_station2_Standard", 72f, sz_Titan * 4f, calc.getTime(40f), 5, 30, 40, null);

    // Amalthea Nexus
    CampaignFleetAPI amaltheaNexus = RemnantNexusFactory.spawnNexus(system, Amalthea, "remnant_station2_Standard", 72f, sz_Amalthea * 4f, calc.getTime(.1f), 5, 30, 40, null);

    // Triton Nexus (Damaged)
    CampaignFleetAPI tritonNexusDamaged = RemnantNexusFactory.spawnNexus(system, Triton, "remnant_station2_Damaged", 144f, sz_Triton * 4f, calc.getTime(40f), 2, 5, 25, null);

    // Ceres opposite hyperjump
    CampaignFleetAPI ceresNexus = RemnantNexusFactory.spawnNexus(system, Ceres, "remnant_station2_Damaged", 180f, 100f, calc.getTime(30f), 2, 5, 25, null);

    // Mars opposite phobos
    CampaignFleetAPI marsNexus = RemnantNexusFactory.spawnNexus(system, Mars, "remnant_station2_Standard", 180f, sz_Mars * 2f, p_Phobos, 5, 30, 40, null);

    // Misc inner nexi
    CampaignFleetAPI vestaNexus = RemnantNexusFactory.spawnNexus(system, Vesta, "remnant_station2_Standard", 180f, 100f, calc.getTime(15f), 5, 30, 40, null);
    CampaignFleetAPI pallasNexus = RemnantNexusFactory.spawnNexus(system, Pallas, "remnant_station2_Damaged", 180f, 100f, calc.getTime(15f), 2, 5, 25, null);
    CampaignFleetAPI hygieaNexus = RemnantNexusFactory.spawnNexus(system, Hygiea, "remnant_station2_Damaged", 180f, 100f, calc.getTime(15f), 2, 5, 25, null);

    CampaignFleetAPI solNexusAlpha = RemnantNexusFactory.spawnNexus(system, star, "remnant_station2_Standard", 45f, dist_VulcanShunt, p_Vulcan, 2, 50, 80, null);
    CampaignFleetAPI solNexusBeta = RemnantNexusFactory.spawnNexus(system, star, "remnant_station2_Standard", 135f, dist_VulcanShunt, p_Vulcan, 2, 50, 80, null);
    CampaignFleetAPI solNexusGama = RemnantNexusFactory.spawnNexus(system, star, "remnant_station2_Standard", 225f, dist_VulcanShunt, p_Vulcan, 2, 50, 80, null);
    CampaignFleetAPI solNexusDelta = RemnantNexusFactory.spawnNexus(system, star, "remnant_station2_Standard", 315f, dist_VulcanShunt, p_Vulcan, 2, 50, 80, null);

    // Agamemnon Weapon Platform
    CampaignFleetAPI agamemnonNexus = RemnantNexusFactory.spawnNexus(system, Agamemnon, "remnant_weapon_platform1_Standard", 180f, 100f, calc.getTime(5f), 1, 30, 35, Commodities.BETA_CORE);

    // Mentor Weapon Platform
    CampaignFleetAPI mentorNexus = RemnantNexusFactory.spawnNexus(system, Mentor, "remnant_weapon_platform1_Standard", 180f, 100f, calc.getTime(5f), 1, 30, 35, Commodities.BETA_CORE);

    // Kleopatra Weapon Platform
    CampaignFleetAPI kleopatraNexus = RemnantNexusFactory.spawnNexus(system, Kleopatra, "station3_Standard", 90f, 200f, calc.getTime(30f), 2, 30, 50, null);

    // Alexhelios Weapon Platform (antipode of Kleopatra)
    CampaignFleetAPI alexNexus = RemnantNexusFactory.spawnNexus(system, Alexhelios, "station1_Standard", 90f, 30f, calc.getTime(20f), 1, 30, 40, Commodities.BETA_CORE);

    // Cleoselene Weapon Platform (antipode of Kleopatra)
    CampaignFleetAPI cleoNexus = RemnantNexusFactory.spawnNexus(system, Cleoselene, "station1_Standard", 270f, 30f, calc.getTime(10f), 1, 30, 40, Commodities.BETA_CORE);
    // --- Remnant Patrol Spawns ---

    // Ganymede (300 FP)
    CampaignFleetAPI ganiOrdo = patrolFactory.spawnPatrol(system, Ganymede, 300f);

    // Triton (200 FP)
    CampaignFleetAPI tritonOrdo1 = patrolFactory.spawnPatrol(system, Triton, 200f);
    CampaignFleetAPI tritonOrdo2 = patrolFactory.spawnPatrol(system, Triton, 200f);

    // Ceres (120 FP)
    CampaignFleetAPI ceresOrdo = patrolFactory.spawnPatrol(system, Ceres, 120f);

    // Mars (220 FP)
    CampaignFleetAPI marsOrdo = patrolFactory.spawnPatrol(system, Mars, 220f);

    // Mercury bosses
    CampaignFleetAPI mercOrdo1 = patrolFactory.spawnPatrol(system, Mars, 400f);
    CampaignFleetAPI mercOrdo2 = patrolFactory.spawnPatrol(system, Mars, 400f);
    CampaignFleetAPI mercOrdo3 = patrolFactory.spawnPatrol(system, Mars, 400f);
    RemnantThemeGenerator.addBeacon(system, RemnantThemeGenerator.RemnantSystemType.RESURGENT);
}

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
DerelictShipData phaethonWreckParams = new DerelictShipData(new PerShipData("phaeton_Standard", ShipCondition.BATTERED), false);
SectorEntityToken phaethonWreck = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, phaethonWreckParams);
phaethonWreck.setCircularOrbit(Phaethon, 90f, 50f, calc.getTime(10f));
phaethonWreck.setDiscoverable(true);

// ## HYPERION
DerelictShipData hyperionParams = new DerelictShipData(new PerShipData("hyperion_Strike", ShipCondition.BATTERED), false);
SectorEntityToken hyperionWreck = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, hyperionParams);
hyperionWreck.setCircularOrbit(Hyperion, 120f, 50f, calc.getTime(10f));
hyperionWreck.setDiscoverable(true);

// ## ATLAS
DerelictShipData atlasParams = new DerelictShipData(new PerShipData("atlas_Standard", ShipCondition.BATTERED), false);
SectorEntityToken atlasWreck = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, atlasParams);
atlasWreck.setCircularOrbit(Atlas, 210f, 20f, calc.getTime(2f));
atlasWreck.setDiscoverable(true);

// ## PROMETHEUS
DerelictShipData prometheusParams = new DerelictShipData(new PerShipData("prometheus_Super", ShipCondition.BATTERED), false);
SectorEntityToken prometheusWreck = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, prometheusParams);
prometheusWreck.setCircularOrbit(Prometheus, 300f, 20f, calc.getTime(2f));
prometheusWreck.setDiscoverable(true);

// ## MERCURY
// Wouldnt want to be op by giving a better mercury than battered would we?
DerelictShipData mercuryParams = new DerelictShipData(new PerShipData("mercury_Standard", ShipCondition.BATTERED), false);
SectorEntityToken mercuryWreck = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, mercuryParams);
mercuryWreck.setCircularOrbit(Mercury, 180f, 460f, calc.getTime(10f));
mercuryWreck.setDiscoverable(true);

// Thank you Suitable star systems for the hyperspace clearing code :\
// Doesn't fix the hyperspace distance problem thorugh :\
SolHyperspaceGen.clearHyperspaceNebulaAroundSystem(system);

if (isSettled) {
new SolEconomies().generate(system); 
}