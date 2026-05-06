package data.scripts.world.systems;

import com.fs.starfarer.api.campaign.*;
import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
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
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.WeightedRandomPicker;
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

import org.lwjgl.util.vector.Vector2f;

public class AstroCalc {
// Scaling Constants
float Eccentricity_Cutoff = 0.01f;

float extComp = 10500f; float intComp = 0.4f; float distLin = 0f; float distLinGiant = 0f; float distMax = 1000000f;
float sizeExt = 220f; float sizeInt = 0.00005f; float sizeDenom = 0.001f; float sizeLin = 0f; float sizeConst = 3f; float sizeStop = 1f;
float timeExt = 120000f; float timeInt = 0.000019f; float timeLin = 0f; float giantTimeExt = 10f; float giantTimeInt = 0.8f; float giantTimeLin = 0f;

float distLinMod = 1f; float distLinGiantMod = 1f;
float timeLinMod = 1f; float giantTimeLinMod = 1f;
float sizeLinMod = 1f; 

float rotMult = 4f;
float currentYear = 9995f; 
boolean occultOrbitBeta = true;
float progradeMult = -1f;

// Base Radii
float jupRad, satRad, uraRad, nepRad;
float jupExt, jupInt, satExt, satInt, uraExt, uraInt, nepExt, nepInt;
int linearTime ;

public AstroCalc() {
try {

    JSONObject settings = Global.getSettings().loadJSON("data/config/sol_settings.json");
    Eccentricity_Cutoff = (float) settings.optDouble("Eccentricity_Cutoff", 0.01); 

    extComp = (float) settings.optDouble("extComp", 10500f); 
    intComp = (float) settings.optDouble("intComp", 0.4f); 
    distMax = (float) settings.optDouble("distMax", 1000000f); 

    sizeExt = (float) settings.optDouble("sizeExt", 220f); 
    sizeInt = (float) settings.optDouble("sizeInt", 0.00005f); 
    sizeDenom = (float) settings.optDouble("sizeDenom", 0.001f); // Boy does this shit look cringe now that I'm better at math 
    sizeConst = (float) settings.optDouble("sizeConst", 3f); 

    timeExt = (float) settings.optDouble("timeExt", 120000f); 
    timeInt = (float) settings.optDouble("timeInt", 0.000019f); 
    giantTimeExt = (float) settings.optDouble("giantTimeExt", 10f); 
    giantTimeInt = (float) settings.optDouble("giantTimeInt", 0.8f); 
    progradeMult = (float) settings.optDouble("progradeMult", -1f); 

    distLinMod = (float) settings.optDouble("distLinMod", 1f);
    
    timeLinMod = (float) settings.optDouble("timeLinMod", 1f); 
    giantTimeLinMod = (float) settings.optDouble("giantTimeLinMod", 1f); 
    sizeLinMod = (float) settings.optDouble("sizeLinMod", 1f); 

    currentYear = (float) settings.optDouble("Current_Year", 9995f); 

    rotMult = (float) settings.optDouble("rotMult", 4f);

    boolean linearDistance = settings.optBoolean("Linear_Distance", false);
    linearTime = settings.optInt("Linear_Time", 1);
    boolean linearSize     = settings.optBoolean("Linear_Size", false);
    boolean toScale        = settings.optBoolean("To_Scale", false);
    boolean toScaleGiant   = settings.optBoolean("To_Scale_Gas_Giant", false);

    // --- DISTANCE ---
    // Linear distance strips the log curve and uses a flat km-per-AU factor.
    // Gas giants get the same treatment unless To_Scale_Gas_Giant overrides below.
    if (linearDistance) {
        extComp = 0; intComp = 0; 
        distLin = distLinMod;
        if (!toScaleGiant) {
            distLinGiant = distLin;
        }
    }
    // --- TIME ---
    // Linear time :\
    if (linearTime >= 1) {
        timeExt = 0; timeInt = 0; 
        timeLin = progradeMult * timeLinMod * 1f;
    }
    if (linearTime >= 2){
        giantTimeExt = 0f; giantTimeInt = 0f;
        giantTimeLin = progradeMult * timeLinMod *  1f;
    }
    // --- SIZE ---
    // To_Scale (with Linear_Distance, no To_Scale_Gas_Giant) takes precedence over plain Linear_Size
    // because it computes the size-per-km factor from the distance factor for true 1:1 scale.
    if (toScale && linearDistance && !toScaleGiant) {
        sizeExt = 0; sizeInt = 0;
        sizeStop = 0f; sizeConst = 1f;
        sizeLin = (distLin * distLinMod) / 149597870.7f;
    } else if (linearSize) {
        sizeExt = 0; sizeInt = 0;
        sizeStop = 0f;
        sizeLin = sizeLinMod;
    }
    // --- GAS GIANT DISTANCES (depends on final sizeLin) ---
    // Converts gas giant distances from AU to km by matching the size-per-km factor.
    // Must run after the size block because it consumes sizeLin.
    if (toScaleGiant && linearSize) {
        distLinGiant = sizeLin * 149597870.7f;
    }

} catch (Exception e) {
    Global.getLogger(this.getClass()).error("SolMod: Failed to load settings", e);
}
jupRad = getSize(139820f); jupExt = jupRad * 1.2f * ((extComp * intComp)/4800f); jupInt = jupExt * 1.3f;
satRad = getSize(116460f); satExt = satRad * 1.2f * ((extComp * intComp)/4800f); satInt = satExt * 5;
uraRad = getSize(50724f); uraExt = uraRad * 1.2f * ((extComp * intComp)/4800f); uraInt = uraExt * 7f;
nepRad = getSize(49244f); nepExt = nepRad * 1f * ((extComp * intComp)/4800f); nepInt = nepExt * 20;
}

// --- CALCULATORS ---
public float getSize(float km) { return sizeExt * (float) Math.log(sizeInt * km + 1) + ((float)(Math.sqrt(km)) / (1 + sizeDenom * km) ) * sizeStop + sizeConst + sizeLin * km; }
public float getDist(float au, SectorEntityToken primary) { 
    float radiusOffset = 0f;

    if (primary != null) {
        radiusOffset = primary.getRadius();
    } else {
        radiusOffset = 0F; 
    }

    // Calculate the theoretical distance
    float calculatedDist = radiusOffset + extComp * (float) Math.log(intComp * au + 1) + distLin * au + 1;

    // Apply the Max Limit
    if (calculatedDist > distMax) {
        return distMax;
    }
    
    return calculatedDist;
}
public float getTime(float days) { return progradeMult * timeExt * (float) Math.log(timeInt * days + 1) + timeLin * days; }
public float getTimeGiant(float days) { return progradeMult * giantTimeExt * (float) Math.log(giantTimeInt * days + 1) + giantTimeLin * days; }

public float getRot(float days) { return -1 * progradeMult * 360f / (days * rotMult * 10f); }
public float getOrbRot(float days) { return (days * progradeMult * rotMult); }

// Gas Giant Distances
public float getDistJupiter(float au) { return jupRad + jupExt * (float) Math.log(jupInt * au + 1) + distLinGiant * au + 1; }
public float getDistSaturn(float au) { return satRad + satExt * (float) Math.log(satInt * au + 1) + distLinGiant * au + 1; }
public float getDistUranus(float au) { return uraRad + uraExt * (float) Math.log(uraInt * au + 1) + distLinGiant * au + 1; }
public float getDistNeptune(float au) { return nepRad + nepExt * (float) Math.log(nepInt * au + 1) + distLinGiant * au + 1; }

// For Extreme distance binaries
public float distDToPeriodD(float distDivisor) { return (float) Math.pow(distDivisor, 1.5); }

// ===============================================================
// RING STUFF
// ===============================================================

private float satRingGameStart, satRingW;

public void setSatRingScale(float start, float width) {
    satRingGameStart = start;
    satRingW = width;
}

public float getSatRingPos(float km) {
    return satRingGameStart + ((km - 70070f) / 70905f) * satRingW;
}

// --- SMART RING TEXTURE ---
public void smartRingTex(StarSystemAPI system, SectorEntityToken parent,
        String sheet, String tex, int texPx, int index,
        float inner, float outer, float orbitDays) {
    system.addRingBand(parent, sheet, tex, texPx, index,
        java.awt.Color.RED, outer - inner, (inner + outer) / 2f, orbitDays);
}

// --- SMART RING TERRAIN ---
public SectorEntityToken smartRingTerrain(StarSystemAPI system, SectorEntityToken parent,
        String name, float inner, float outer, float orbitDays) {
    SectorEntityToken t = system.addTerrain(Terrain.RING,
        new RingSystemTerrainPlugin.RingParams(outer - inner, (inner + outer) / 2f, parent, name));
    t.setCircularOrbit(parent, 0, 0, orbitDays);
    return t;
}

// --- SMART BELT ---
public SectorEntityToken smartBelt(StarSystemAPI system, SectorEntityToken parent,
        String name, int count, float inner, float outer, float minOrbitDays, float maxOrbitDays) {
    float width = outer - inner;
    float radius = (inner + outer) / 2f;
    return system.addAsteroidBelt(parent, count, radius, width, minOrbitDays, maxOrbitDays, Terrain.ASTEROID_BELT, name);
}

// --- ORBIT MATH ---
class Orbit {
public float A, B, C, R, E; 
public Orbit(float a, float b, float c, float r, float e) { A=a; B=b; C=c; R=r; E=e; }
}

private Orbit calculateOrbit(float dPeri, float dApo) {
float vA = (dPeri + dApo) / 2f;
float vC = dApo - vA;
float vB = (float) Math.sqrt((vA * vA) - (vC * vC));
return new Orbit(vA, vB, vC, (vA + vB) / 2f, (vA - vB) / 2f);
}

// General Solar Orbit
public Orbit getOrbit(float SMA, float ecc, SectorEntityToken primary) {
float dPeri = getDist(SMA * (1f - ecc), primary);
float dApo  = getDist(SMA * (1f + ecc), primary);
return calculateOrbit(dPeri, dApo);
}

// Gas Giant Specific Orbits
public Orbit getOrbitJupiter(float SMA, float ecc) {
float dPeri = getDistJupiter(SMA * (1f - ecc));
float dApo  = getDistJupiter(SMA * (1f + ecc));
return calculateOrbit(dPeri, dApo);
}

public Orbit getOrbitSaturn(float SMA, float ecc) {
float dPeri = getDistSaturn(SMA * (1f - ecc));
float dApo  = getDistSaturn(SMA * (1f + ecc));
return calculateOrbit(dPeri, dApo);
}

public Orbit getOrbitUranus(float SMA, float ecc) {
float dPeri = getDistUranus(SMA * (1f - ecc));
float dApo  = getDistUranus(SMA * (1f + ecc));
return calculateOrbit(dPeri, dApo);
}

public Orbit getOrbitNeptune(float SMA, float ecc) {
float dPeri = getDistNeptune(SMA * (1f - ecc));
float dApo  = getDistNeptune(SMA * (1f + ecc));
return calculateOrbit(dPeri, dApo);
}

// Libration (Trojans)
public Orbit getLibration(float major, float minor) {
float R = (major + minor) / 2f; 
float E = (major - minor) / 2f; 
return new Orbit(major, minor, 0, R, E); 
}

// Assumption: Equal density (Mass ratio derived from Radius^3)
public float[] getBinaryOffsets(float rPrimary, float rSecondary, float separationScale) {
float separation = rPrimary * separationScale;

double massRatio = Math.pow(rSecondary / rPrimary, 3);

float distPrimary = (float) (separation * (massRatio / (1.0 + massRatio)));
float distSecondary = (float) (separation * (1.0 / (1.0 + massRatio)));

return new float[]{distPrimary, distSecondary};
}

// ==========================================
// BINARY OFFSET CALCULATOR (REAL DIAMETER)
// ==========================================
// FIX CUZ DIAMETER IS LOGGED, ocus-vanh dist is less than pluo-chaon, but using linear primary radii as the measurement orcus-vanth looks farther than pluo-chaon
// ERS's 40,000 KM between it and Dysonoia also is problematic
public float[] getBinaryOffsetsReal(float realDiamPri, float realDiamSec, float separationScale) {
float visRadPri = getSize(realDiamPri);

float separation = visRadPri * separationScale;

double massRatio = Math.pow(realDiamSec / realDiamPri, 3);

float distPrimary = (float) (separation * (massRatio / (1.0 + massRatio)));

float distSecondary = (float) (separation * (1.0 / (1.0 + massRatio)));

return new float[]{distPrimary, distSecondary};
}

// Bulk-add market conditions from an explicit array
public void addConditions(MarketAPI market, String[] conditions) {
    for (String c : conditions) {
        market.addCondition(c);
    }
}

// =================================================================================================================
// SECTION: HOST DISTANCE Gas giants and sol, in perfect unison
// =================================================================================================================

public Orbit getOrbitGasGiant(String hostName, float SMA, float ecc, SectorEntityToken primary) {
    String name = (hostName == null) ? "" : hostName.toLowerCase();

    if (name.equals("sol"))     return getOrbit(SMA, ecc, primary);
    if (name.equals("jupiter")) return getOrbitJupiter(SMA, ecc);
    if (name.equals("saturn"))  return getOrbitSaturn(SMA, ecc);
    if (name.equals("uranus"))  return getOrbitUranus(SMA, ecc);
    if (name.equals("neptune")) return getOrbitNeptune(SMA, ecc);

    return getOrbitJupiter(SMA, ecc);
}

public float getDistGasGiant(String hostName, float SMA, SectorEntityToken primary) {
    String name = (hostName == null) ? "" : hostName.toLowerCase();

    if (name.equals("sol"))     return getDist(SMA, primary);
    if (name.equals("jupiter")) return getDistJupiter(SMA);
    if (name.equals("saturn"))  return getDistSaturn(SMA);
    if (name.equals("uranus"))  return getDistUranus(SMA);
    if (name.equals("neptune")) return getDistNeptune(SMA);

    return getDistJupiter(SMA);
}


// =================================================================================================================
// SECTION: SIMPLE MOON SPAWNERS
// =================================================================================================================

public SectorEntityToken spawnMoon(StarSystemAPI system, SectorEntityToken primary, String name, float size, float orbitRadius, float orbitPeriod, float angle) {
    return spawnMoon(system, primary, name, size, orbitRadius, orbitPeriod, angle, true);
}

public SectorEntityToken spawnMoon(StarSystemAPI system, SectorEntityToken primary, String name, float size, float orbitRadius, float orbitPeriod, float angle, boolean nametag) {
    SectorEntityToken moon = SolAsteroidFactory.createAsteroid(system, size, name, name, nametag);
    moon.setCircularOrbitPointingDown(primary, angle, orbitRadius, orbitPeriod);
    return moon;
}

public static class SolAsteroidFactory {
    public static SectorEntityToken createAsteroid(StarSystemAPI solSystem, float size, String uniqueEntityId, String displayName, boolean nametag) {
        int sizeSafe = Math.round(size);
        if (sizeSafe < 1) sizeSafe = 1;
        if (sizeSafe > 30) sizeSafe = 30;

        String solConfigId = nametag
            ? "sol_asteroid" + sizeSafe
            : "sol_asteroid_no_nametag" + sizeSafe;

        return solSystem.addCustomEntity(uniqueEntityId, displayName, solConfigId, "neutral");
    }
}

// =================================================================================================================
// KeplerComponent — Optimized
// =================================================================================================================

static class KeplerComponent {
    // --- Mutable state ---
    float meanAnomaly;

    final float a, ecc, longPeri, sign, degPerSec;
    final float longPeriRad;        // Math.toRadians(longPeri)
    final float oneMinusEccSq;      // 1 - e²
    final float sqrtOnePlusE;       // √(1+e)
    final float sqrtOneMinusE;      // √(1-e)
    final float signA;              // sign * a  (for circular fast-path)
    final boolean isCircular;       // ecc == 0f

    KeplerComponent(float a, float ecc, float longPeri, float period,
                    float startAnomaly, float sign) {
        this.a = a;
        this.ecc = ecc;
        this.longPeri = longPeri;
        this.meanAnomaly = startAnomaly;
        this.sign = sign;
        this.degPerSec = -360f / (period * Global.getSector().getClock().getSecondsPerDay());

        // --- Precomputed invariants ---
        this.longPeriRad = (float) Math.toRadians(longPeri);
        this.oneMinusEccSq = 1f - ecc * ecc;
        this.sqrtOnePlusE = (float) Math.sqrt(1.0 + ecc);
        this.sqrtOneMinusE = (float) Math.sqrt(1.0 - ecc);
        this.signA = sign * a;
        this.isCircular = (ecc == 0f);
    }

    static KeplerComponent circular(float radius, float startAngle,
                                    float period, float sign) {
        return new KeplerComponent(radius, 0f, startAngle, period, 0f, sign);
    }

    void advance(float dt) {
        float ma = meanAnomaly + dt * degPerSec;
        // Branchless modulo for small overshoots is not possible here because
        // dt may be large (e.g. fast-forward); fall back to general form.
        ma %= 360f;
        if (ma < 0f) ma += 360f;
        meanAnomaly = ma;
    }

    void evalInto(float[] scratch) {
        // --- Circular fast-path ---
        if (isCircular) {
            double ang = longPeriRad + Math.toRadians(meanAnomaly);
            scratch[0] += signA * (float) Math.cos(ang);
            scratch[1] += signA * (float) Math.sin(ang);
            return;
        }

        // --- Elliptical: Kepler solve in radians ---
        double M = Math.toRadians(meanAnomaly);
        double E = M;
        for (int i = 0; i < 8; i++) {
            double sinE = Math.sin(E);
            double cosE = Math.cos(E);
            double dE = (E - ecc * sinE - M) / (1.0 - ecc * cosE);
            E -= dE;
            if (dE < 1e-6 && dE > -1e-6) break;
        }

        // True anomaly
        double halfE = E * 0.5;
        double v = 2.0 * Math.atan2(
            sqrtOnePlusE * Math.sin(halfE),
            sqrtOneMinusE * Math.cos(halfE));

        // Radius and position
        double cosV = Math.cos(v);
        double r = a * oneMinusEccSq / (1.0 + ecc * cosV);
        double ang = longPeriRad + v;
        scratch[0] += sign * (float)(r * Math.cos(ang));
        scratch[1] += sign * (float)(r * Math.sin(ang));
    }

    KeplerComponent copy() {
        float secondsPerDay = Global.getSector().getClock().getSecondsPerDay();
        float period = -360f / (degPerSec * secondsPerDay);
        return new KeplerComponent(a, ecc, longPeri, period, meanAnomaly, sign);
    }
}


// ============================================================================
// CompoundOrbit ================= Prominent in HP Lovecraft's work ===========
// ============================================================================
class CompoundOrbit implements OrbitAPI {
    final SectorEntityToken focus;            // math anchor
    final SectorEntityToken declaredFocus;    // null = honest; non-null = faux (what getFocus reports)
    SectorEntityToken entity;
    final KeplerComponent[] components;
    final boolean singleComponent;

    private final float[] scratch = new float[2];
    private final Vector2f reusableVec = new Vector2f();

    float currentX, currentY;

    float facing = 0f;
    float spinRate = 0f;
    final boolean hasSpin;

    // ---- Honest constructors ----

    CompoundOrbit(SectorEntityToken focus, List<KeplerComponent> components) {
        this.focus = focus;
        this.declaredFocus = null;
        int n = components.size();
        KeplerComponent[] arr = new KeplerComponent[n];
        for (int i = 0; i < n; i++) {
            arr[i] = (KeplerComponent) components.get(i);
        }
        this.components = arr;
        this.singleComponent = (n == 1);
        this.spinRate = 0f;
        this.hasSpin = false;
    }

    CompoundOrbit(SectorEntityToken focus, KeplerComponent... components) {
        this.focus = focus;
        this.declaredFocus = null;
        this.components = components;
        this.singleComponent = (components.length == 1);
        this.spinRate = 0f;
        this.hasSpin = false;
    }

    CompoundOrbit(SectorEntityToken focus, List<KeplerComponent> components,
                  float spinMin, float spinMax) {
        this.focus = focus;
        this.declaredFocus = null;
        int n = components.size();
        KeplerComponent[] arr = new KeplerComponent[n];
        for (int i = 0; i < n; i++) {
            arr[i] = (KeplerComponent) components.get(i);
        }
        this.components = arr;
        this.singleComponent = (n == 1);

        float sr;
        if (spinMin == spinMax) {
            sr = spinMin;
        } else {
            sr = spinMin + (float)(Math.random() * (spinMax - spinMin));
        }
        this.spinRate = sr;
        this.hasSpin = (sr != 0f);
    }

    CompoundOrbit(SectorEntityToken focus, KeplerComponent[] components, float spinRate) {
        this.focus = focus;
        this.declaredFocus = null;
        this.components = components;
        this.singleComponent = (components.length == 1);
        this.spinRate = spinRate;
        this.hasSpin = (spinRate != 0f);
    }

    // ---- Faux constructors (declaredFocus reported by getFocus, anchor used for math) ----

    CompoundOrbit(SectorEntityToken declaredFocus, SectorEntityToken anchor,
                  List<KeplerComponent> components) {
        this.focus = anchor;
        this.declaredFocus = declaredFocus;
        int n = components.size();
        KeplerComponent[] arr = new KeplerComponent[n];
        for (int i = 0; i < n; i++) {
            arr[i] = (KeplerComponent) components.get(i);
        }
        this.components = arr;
        this.singleComponent = (n == 1);
        this.spinRate = 0f;
        this.hasSpin = false;
    }

    CompoundOrbit(SectorEntityToken declaredFocus, SectorEntityToken anchor,
                  List<KeplerComponent> components, float spinMin, float spinMax) {
        this.focus = anchor;
        this.declaredFocus = declaredFocus;
        int n = components.size();
        KeplerComponent[] arr = new KeplerComponent[n];
        for (int i = 0; i < n; i++) {
            arr[i] = (KeplerComponent) components.get(i);
        }
        this.components = arr;
        this.singleComponent = (n == 1);

        float sr;
        if (spinMin == spinMax) {
            sr = spinMin;
        } else {
            sr = spinMin + (float)(Math.random() * (spinMax - spinMin));
        }
        this.spinRate = sr;
        this.hasSpin = (sr != 0f);
    }

    // ---- Core loop ----

    public void advance(float amount) {
        final KeplerComponent[] comps = components;
        if (singleComponent) {
            comps[0].advance(amount);
        } else {
            for (int i = 0, n = comps.length; i < n; i++) {
                comps[i].advance(amount);
            }
        }

        if (hasSpin) {
            float f = facing + amount * spinRate;
            f %= 360f;
            if (f < 0f) f += 360f;
            facing = f;
        }

        updatePosition();

        final SectorEntityToken e = entity;
        if (e != null) {
            e.getLocation().set(currentX, currentY);
            if (hasSpin) e.setFacing(facing);
        }
    }

    private void updatePosition() {
        final float[] s = scratch;
        s[0] = 0f; s[1] = 0f;

        final KeplerComponent[] comps = components;
        if (singleComponent) {
            comps[0].evalInto(s);
        } else {
            for (int i = 0, n = comps.length; i < n; i++) {
                comps[i].evalInto(s);
            }
        }

        final Vector2f focusLoc = focus.getLocation();
        currentX = focusLoc.x + s[0];
        currentY = focusLoc.y + s[1];
    }

    public OrbitAPI makeCopy() {
        KeplerComponent[] copies = new KeplerComponent[components.length];
        for (int i = 0; i < components.length; i++) {
            copies[i] = components[i].copy();
        }
        CompoundOrbit c;
        if (declaredFocus != null) {
            // Faux copy: rebuild via faux constructor so declaredFocus and anchor are preserved.
            // Need a List for the faux constructor; build it explicitly.
            List<KeplerComponent> copyList = new ArrayList<KeplerComponent>(copies.length);
            for (int i = 0; i < copies.length; i++) copyList.add(copies[i]);
            if (hasSpin) c = new CompoundOrbit(declaredFocus, focus, copyList, spinRate, spinRate);
            else         c = new CompoundOrbit(declaredFocus, focus, copyList);
        } else {
            c = new CompoundOrbit(focus, copies, spinRate);
        }
        c.facing = this.facing;
        return c;
    }

    public Vector2f computeCurrentLocation() {
        updatePosition();
        reusableVec.set(currentX, currentY);
        return reusableVec;
    }

    public SectorEntityToken getFocus() {
        return declaredFocus != null ? declaredFocus : focus;
    }

    public float getOrbitalPeriod() {
        float dps = components[0].degPerSec;
        return dps != 0f ? -360f / dps : 1f;
    }

    public void setEntity(SectorEntityToken e) { this.entity = e; }
    public SectorEntityToken getEntity() { return entity; }
    public void updateLocation() { advance(0); }

    public float interpolateAngle(float f) {
        KeplerComponent k = components[0];
        return k.longPeri + k.meanAnomaly;
    }

    public Vector2f interpolateLocation(float f) {
        return computeCurrentLocation();
    }

    // =============================================================================================
    // Vanilla-compatible orbit getters
    // =============================================================================================
    // Vok has been doin some orbit stuff, this is done in hope of not crashing game

    public float getCircularOrbitRadius() {
        return components[0].a;
    }

    public float getCircularOrbitAngle() { // Lol
        KeplerComponent k = components[0];
        float ang = (k.longPeri + k.meanAnomaly) % 360f;
        if (ang < 0f) ang += 360f;
        return ang;
    }

    public float getCircularOrbitPeriod() {
        return getOrbitalPeriod();
    }

    public void setCircularOrbitRadius(float radius) { // SMA
        KeplerComponent old = components[0];
        float secondsPerDay = Global.getSector().getClock().getSecondsPerDay();
        float period = old.degPerSec != 0f ? -360f / (old.degPerSec * secondsPerDay) : 1f;
        components[0] = new KeplerComponent(radius, old.ecc, old.longPeri, period,
                                            old.meanAnomaly, old.sign);
    }

    public void setCircularOrbitAngle(float angle) { // Dont touch
        KeplerComponent k = components[0];
        float ma = (angle - k.longPeri) % 360f;
        if (ma < 0f) ma += 360f;
        k.meanAnomaly = ma;
    }

    public void setCircularOrbitPeriod(float period) {
        KeplerComponent old = components[0];
        components[0] = new KeplerComponent(old.a, old.ecc, old.longPeri, period,
                                            old.meanAnomaly, old.sign);
    }
}

public class SPSUtils {
// =================================================================================================================
// 1. TIME CALCULATOR (Required by new Spawner)
// =================================================================================================================

public static float getMeanAnomalyFromEpoch(float currentYear, float perihelionYear, float SMA, Float resonantSMA) {
    // If a resonant SMA is provided, use it to calculate the locked period. 
    // Otherwise, use the object's actual SMA.
    double effectiveSMA = (resonantSMA != null) ? resonantSMA : SMA;
    double effectivePeriodYears = Math.sqrt(Math.pow(effectiveSMA, 3));

    // Calculate time passed since the object's true real-world perihelion
    double yearsSincePerihelion = currentYear - perihelionYear;
    
    // Calculate orbital phase using the locked speed
    double fractionOfOrbit = yearsSincePerihelion / effectivePeriodYears;
    double meanAnomaly = fractionOfOrbit * 360.0;

    // Normalize to 0-360 range
    meanAnomaly = meanAnomaly % 360.0;
    if (meanAnomaly < 0) meanAnomaly += 360.0;

    return (float) meanAnomaly;
}

// ==========================================
// 2. GEOMETRY SOLVERS
// ==========================================

public static float getDistanceAtAnomaly(float a, float e, float trueAnomalyDeg) {
double v_rad = Math.toRadians(trueAnomalyDeg);
double r = (a * (1 - (e * e))) / (1 + (e * Math.cos(v_rad)));
return (float) r;
}

public static float getEccentricAnomalyFromMean(float e, float meanAnomalyDeg) {
double M_rad = Math.toRadians(meanAnomalyDeg);
double E_rad = M_rad; // Initial guess

for (int i = 0; i < 10; i++) {
double dE = (E_rad - e * Math.sin(E_rad) - M_rad) / (1 - e * Math.cos(E_rad));
E_rad -= dE;
if (Math.abs(dE) < 1e-6) break;
}

return (float) Math.toDegrees(E_rad);
}

public static float getTrueAnomalyFromEccentric(float e, float eccentricAnomalyDeg) {
double E_rad = Math.toRadians(eccentricAnomalyDeg);
double tanV2 = Math.sqrt((1 + e) / (1 - e)) * Math.tan(E_rad / 2.0);
double v_rad = 2.0 * Math.atan(tanV2);
return (float) Math.toDegrees(v_rad);
}
}

// =================================================================================================================
// SECTION: IRREGULAR BODY SPAWNER 2 — thin wrapper, retrograde-aware
// =================================================================================================================
public SectorEntityToken spawnIrregularBody2(StarSystemAPI system,
                    SectorEntityToken primary,
                    String id,
                    String name,
                    String type,
                    String subType,
                    float diameterKM,
                    float SMA,
                    Float ecc,
                    float longAscNode,
                    float argPeri,
                    float perihelionYear,
                    float angleOffset,
                    Float rotationalPeriod,
                    Float hostStarMass,
                    String giantName,
                    boolean isRetrograde) {

    return spawnSPSObject6(system, primary,
                           id, name, type, subType,
                           diameterKM,
                           SMA, ecc,
                           longAscNode, argPeri,
                           perihelionYear, angleOffset,
                           rotationalPeriod, hostStarMass,
                           null, null,
                           null,
                           primary,
                           giantName,
                           isRetrograde);
}

// =================================================================================================================
// SECTION: IRREGULAR BODY SPAWNER 3 — thin wrapper, retrograde-aware
// =================================================================================================================
public SectorEntityToken spawnIrregularBody3(StarSystemAPI system,
                    SectorEntityToken primary,
                    String id,
                    String name,
                    String type,
                    String subType,
                    float diameterKM,
                    float SMA,
                    Float ecc,
                    float longAscNode,
                    float argPeri,
                    float perihelionYear,
                    float angleOffset,
                    Float rotationalPeriod,
                    Float hostStarMass,
                    float overridePeriod,
                    String giantName,
                    boolean isRetrograde) {

    return spawnSPSObject6(system, primary,
                           id, name, type, subType,
                           diameterKM,
                           SMA, ecc,
                           longAscNode, argPeri,
                           perihelionYear, angleOffset,
                           rotationalPeriod, hostStarMass,
                           overridePeriod, null,
                           null,
                           primary,
                           giantName,
                           isRetrograde);
}

// ==========================================
// SPS SPAWNER (Node + Argument Inputs)
// ==========================================

public SectorEntityToken spawnSPSObject(StarSystemAPI system, 
                    SectorEntityToken primary,
                    String id, 
                    String name, 
                    String type, 
                    String subType,
                    float diameterKM,
                    float SMA, 
                    Float ecc, 
                    float longAscNode,
                    float argPeri,
                    float perihelionYear, 
                    float angleOffset,
                    Float rotationalPeriod,
                    Float hostStarMass) {
    return spawnSPSObject4(system, primary, id, name, type, subType, 
                        diameterKM, SMA, ecc, longAscNode, argPeri, 
                        perihelionYear, angleOffset, rotationalPeriod, 
                        hostStarMass, null, null, false, 0f, 0f, 0f, primary);}

public SectorEntityToken spawnSPSObject2(StarSystemAPI system, 
                    SectorEntityToken primary,
                    String id, 
                    String name, 
                    String type, 
                    String subType,
                    float diameterKM,
                    float SMA, 
                    Float ecc, 
                    float longAscNode,
                    float argPeri,
                    float perihelionYear, 
                    float angleOffset,
                    Float rotationalPeriod,
                    Float hostStarMass,
                    Float overridePeriod,
                    Float resonantSMA) {
        return spawnSPSObject4(system, primary, id, name, type, subType, 
                        diameterKM, SMA, ecc, longAscNode, argPeri, 
                        perihelionYear, angleOffset, rotationalPeriod, 
                        hostStarMass, overridePeriod, resonantSMA, false, 0f, 0f, 0f, primary);}

public SectorEntityToken spawnSPSObject3(StarSystemAPI system, 
                    SectorEntityToken primary,
                    String id, 
                    String name, 
                    String type, 
                    String subType,
                    float diameterKM,
                    float SMA, 
                    Float ecc, 
                    float longAscNode,
                    float argPeri,
                    float perihelionYear, 
                    float angleOffset,
                    Float rotationalPeriod,
                    Float hostStarMass,
                    Float overridePeriod,
                    Float resonantSMA,
                    boolean isBinary,
                    float binaryRadius,
                    float binaryStartAngle,
                    float binaryPeriod) {
return spawnSPSObject4(system, primary, id, name, type, subType, 
                        diameterKM, SMA, ecc, longAscNode, argPeri, 
                        perihelionYear, angleOffset, rotationalPeriod, 
                        hostStarMass, overridePeriod, resonantSMA, isBinary, 
                        binaryRadius, binaryStartAngle, binaryPeriod, primary);}                        

public SectorEntityToken spawnSPSObject4(StarSystemAPI system,
                    SectorEntityToken primary,
                    String id, String name, String type, String subType,
                    float diameterKM,
                    float SMA, Float ecc,
                    float longAscNode, float argPeri,
                    float perihelionYear, float angleOffset,
                    Float rotationalPeriod, Float hostStarMass,
                    Float overridePeriod, Float resonantSMA,
                    boolean isBinary,
                    float binaryRadius, float binaryStartAngle, float binaryPeriod,
                    SectorEntityToken primaryOffset) {
    float[][] extras = null;
    if (isBinary) {
        extras = new float[][]{
            { binaryRadius, 0f, binaryStartAngle, binaryPeriod, 0f, +1f }
        };
    }
   return spawnSPSObject6(system, primary, id, name, type, subType,
                           diameterKM, SMA, ecc, longAscNode, argPeri,
                           perihelionYear, angleOffset, rotationalPeriod,
                           hostStarMass, overridePeriod, resonantSMA,
                           extras, primaryOffset,
                           "Sol", false);
}

public SectorEntityToken spawnSPSObject5(StarSystemAPI system,
                    SectorEntityToken primary,
                    String id, String name, String type, String subType,
                    float diameterKM,
                    float SMA, Float ecc,
                    float longAscNode, float argPeri,
                    float perihelionYear, float angleOffset,
                    Float rotationalPeriod, Float hostStarMass,
                    Float overridePeriod, Float resonantSMA,
                    float[][] extraParams,
                    SectorEntityToken primaryOffset) {
    return spawnSPSObject6(system, primary, id, name, type, subType,
                           diameterKM, SMA, ecc, longAscNode, argPeri,
                           perihelionYear, angleOffset, rotationalPeriod,
                           hostStarMass, overridePeriod, resonantSMA,
                           extraParams, primaryOffset,
                           "Sol", false);
}

// =================================================================================================================
// SECTION: SPS6 — backwards-compatible forwarder (no faux parenting)
// =================================================================================================================
public SectorEntityToken spawnSPSObject6(StarSystemAPI system,
                    SectorEntityToken primary,
                    String id, String name, String type, String subType,
                    float diameterKM,
                    float SMA, Float ecc,
                    float longAscNode, float argPeri,
                    float perihelionYear, float angleOffset,
                    Float rotationalPeriod, Float hostStarMass,
                    Float overridePeriod, Float resonantSMA,
                    float[][] extraParams,
                    SectorEntityToken primaryOffset,
                    String hostName,
                    boolean isRetrograde) {
    return spawnSPSObject7(system, primary, id, name, type, subType,
        diameterKM, SMA, ecc, longAscNode, argPeri,
        perihelionYear, angleOffset, rotationalPeriod, hostStarMass,
        overridePeriod, resonantSMA, extraParams, primaryOffset,
        hostName, isRetrograde, false);
}

// =================================================================================================================
// SECTION: SPS7 — host-aware + retrograde-aware + fauxParented (false-moon parentage trick)
// =================================================================================================================
// When fauxParented=true (and primary != primaryOffset, and occult path active):
//   - getFocus() reports `primary` (intel-screen / parentage queries see this as the parent)
//   - orbit math anchors on `primaryOffset` (the body actually orbits this)
// Only takes effect on the CompoundOrbit branches (BRANCH 2 circular+extras, BRANCH 3 elliptical occult).
public SectorEntityToken spawnSPSObject7(StarSystemAPI system,
                    SectorEntityToken primary,
                    String id, String name, String type, String subType,
                    float diameterKM,
                    float SMA, Float ecc,
                    float longAscNode, float argPeri,
                    float perihelionYear, float angleOffset,
                    Float rotationalPeriod, Float hostStarMass,
                    Float overridePeriod, Float resonantSMA,
                    float[][] extraParams,
                    SectorEntityToken primaryOffset,
                    String hostName,
                    boolean isRetrograde,
                    boolean fauxParented) {

    // ---------------------------------------------------------------------
    // Parameter normalisation
    // ---------------------------------------------------------------------
    float safeHostMass = (hostStarMass == null || hostStarMass <= 0f) ? 1.0f : hostStarMass;
    float size = getSize(diameterKM);

    float orbitRadius = getDistGasGiant(hostName, SMA, primaryOffset);

    double rawPeriodYears;
    if(linearTime == 1 && hostName == "Sol"){
        float gameDistAtOneAU = getDistGasGiant(hostName, 1, primaryOffset) - primaryOffset.getRadius();
        float distAU = ( orbitRadius - primaryOffset.getRadius() + 1f) / gameDistAtOneAU;
        rawPeriodYears = Math.sqrt(Math.pow(distAU, 3) / safeHostMass);
    } else {
        rawPeriodYears = Math.sqrt(Math.pow(SMA, 3) / safeHostMass);
    }

    float baseMagPeriod = (overridePeriod != null) ? overridePeriod : getTime((float) rawPeriodYears * 365.25f);
    float signedPeriod;

    if(overridePeriod == null){
        signedPeriod = isRetrograde ? -baseMagPeriod : baseMagPeriod;
    } else {
        signedPeriod = baseMagPeriod;
    }

    float longPeri = longAscNode + argPeri;
    float meanAnomaly = SPSUtils.getMeanAnomalyFromEpoch(currentYear, perihelionYear, SMA, resonantSMA);
    if (isRetrograde) meanAnomaly = -meanAnomaly;

    float safeE = (ecc == null) ? 0f : ecc;
    Float resolvedRot = rotationalPeriod;
    if (resolvedRot == null && (type.equals("custom_entity") || type.equals("asteroid") || type.equals("moon"))) {
        resolvedRot = getRandomRotationPeriod(diameterKM);
    }
    boolean hasCustomSpin = (resolvedRot != null) && resolvedRot != 0f;
    float customSpin = hasCustomSpin ? getRot(resolvedRot) : 0f;

    boolean hasExtras = (extraParams != null && extraParams.length > 0);
    String  typeKey   = type.toLowerCase();
    float   dirSign   = isRetrograde ? -1f : +1f;
    boolean isRock    = typeKey.equals("asteroid") || typeKey.equals("moon");

    boolean useFaux = fauxParented && occultOrbitBeta && (primary != primaryOffset);

    SectorEntityToken initialParent = useFaux ? primaryOffset : primary;

    // ---------------------------------------------------------------------
    // BRANCH 1: CIRCULAR ORBIT — legacy fast path (no CompoundOrbit needed)
    // ---------------------------------------------------------------------
    if (safeE < Eccentricity_Cutoff && (!occultOrbitBeta || !hasExtras)) {
        float currentAngle = meanAnomaly + longPeri + angleOffset;
        SectorEntityToken resultingEntity;

        if (typeKey.equals("custom_entity")) {
            resultingEntity = system.addCustomEntity(id, name, subType, "neutral");
            if (hasCustomSpin) resultingEntity.setCircularOrbitWithSpin(initialParent, currentAngle, orbitRadius, signedPeriod, customSpin, customSpin);
            else               resultingEntity.setCircularOrbit(initialParent, currentAngle, orbitRadius, signedPeriod);

        } else if (isRock) {
            resultingEntity = SolAsteroidFactory.createAsteroid(system, size, id, name, !(subType == "no_name"));
            resultingEntity.setCircularOrbitWithSpin(initialParent, currentAngle, orbitRadius, signedPeriod, customSpin, customSpin);

        } else {
            resultingEntity = system.addPlanet(id, initialParent, name, type, currentAngle, size, orbitRadius, signedPeriod);
        }
        return resultingEntity;
    }

    // ---------------------------------------------------------------------
    // BRANCH 2: CIRCULAR + EXTRAS (CompoundOrbit, e.g. binary epicycle)
    // ---------------------------------------------------------------------
    if (safeE < Eccentricity_Cutoff) {
        float currentAngle = meanAnomaly + longPeri + angleOffset;
        SectorEntityToken resultingEntity;

        if (typeKey.equals("custom_entity")) {
            resultingEntity = system.addCustomEntity(id, name, subType, "neutral");
            resultingEntity.setCircularOrbit(initialParent, 0f, orbitRadius, signedPeriod);
        } else if (isRock) {
            resultingEntity = SolAsteroidFactory.createAsteroid(system, size, id, name, !(subType == "no_name"));
            resultingEntity.setCircularOrbitWithSpin(initialParent, 0f, orbitRadius, signedPeriod, customSpin, customSpin);
        } else {
            resultingEntity = system.addPlanet(id, initialParent, name, type, 0f, size, orbitRadius, signedPeriod);
        }

        List<KeplerComponent> comps = new ArrayList<KeplerComponent>(1 + extraParams.length);
        comps.add(KeplerComponent.circular(orbitRadius, currentAngle, signedPeriod, dirSign));
        for (int i = 0; i < extraParams.length; i++) {
            float[] p = extraParams[i];
            comps.add(new KeplerComponent(p[0], p[1], p[2], p[3], p[4], p[5]));
        }

        boolean wantSpin = hasCustomSpin && (isRock || typeKey.equals("custom_entity"));
        CompoundOrbit orbit;
        if (useFaux) {
            orbit = wantSpin
                ? new CompoundOrbit(primary, primaryOffset, comps, customSpin, customSpin)
                : new CompoundOrbit(primary, primaryOffset, comps);
        } else {
            orbit = wantSpin
                ? new CompoundOrbit(primary, comps, customSpin, customSpin)
                : new CompoundOrbit(primary, comps);
        }
        orbit.setEntity(resultingEntity);
        resultingEntity.setOrbit(orbit);
        orbit.advance(0);
        return resultingEntity;
    }

    // ---------------------------------------------------------------------
    // BRANCH 3: ELLIPTICAL ORBIT — modern (CompoundOrbit Kepler) path
    // ---------------------------------------------------------------------
    Orbit o = getOrbitGasGiant(hostName, SMA, safeE, primaryOffset);

    if (occultOrbitBeta) {
        SectorEntityToken resultingEntity;

        if (typeKey.equals("custom_entity")) {
            resultingEntity = system.addCustomEntity(id, name, subType, "neutral");
            resultingEntity.setCircularOrbit(initialParent, 0f, orbitRadius, signedPeriod);
        } else if (isRock) {
            resultingEntity = SolAsteroidFactory.createAsteroid(system, size, id, name, !(subType == "no_name"));
            resultingEntity.setCircularOrbitWithSpin(initialParent, 0f, orbitRadius, signedPeriod, customSpin, customSpin);
        } else {
            resultingEntity = system.addPlanet(id, initialParent, name, type, 0f, size, orbitRadius, signedPeriod);
        }

        int extrasLen = hasExtras ? extraParams.length : 0;
        List<KeplerComponent> comps = new ArrayList<KeplerComponent>(1 + extrasLen);
        comps.add(new KeplerComponent(o.A, o.C / o.A, longPeri + angleOffset, signedPeriod, meanAnomaly, dirSign));
        if (hasExtras) {
            for (int i = 0; i < extraParams.length; i++) {
                float[] p = extraParams[i];
                comps.add(new KeplerComponent(p[0], p[1], p[2], p[3], p[4], p[5]));
            }
        }

        boolean wantSpin = hasCustomSpin && (isRock || typeKey.equals("custom_entity"));
        CompoundOrbit orbit;
        if (useFaux) {
            orbit = wantSpin
                ? new CompoundOrbit(primary, primaryOffset, comps, customSpin, customSpin)
                : new CompoundOrbit(primary, primaryOffset, comps);
        } else {
            orbit = wantSpin
                ? new CompoundOrbit(primary, comps, customSpin, customSpin)
                : new CompoundOrbit(primary, comps);
        }
        orbit.setEntity(resultingEntity);
        resultingEntity.setOrbit(orbit);
        orbit.advance(0);

        return resultingEntity;
    }

    // ---------------------------------------------------------------------
    // BRANCH 4: ELLIPTICAL ORBIT — legacy epicycle (Center + Tracer)
    // ---------------------------------------------------------------------
    float E_deg = SPSUtils.getEccentricAnomalyFromMean(safeE, meanAnomaly);

    SectorEntityToken center = system.addCustomEntity(null, null, "empty", "neutral");
    center.setCircularOrbit(primary, longPeri + 180f + angleOffset, o.C, Float.MAX_VALUE);

    SectorEntityToken tracer = system.addCustomEntity(null, null, "empty", "neutral");
    tracer.setCircularOrbit(center, longPeri + E_deg + angleOffset, o.R, signedPeriod);

    float flatteningAngle = (longPeri + angleOffset) - E_deg;
    float bodyOrbitPeriod = -signedPeriod;
    SectorEntityToken resultingEntity;

    if (typeKey.equals("custom_entity")) {
        resultingEntity = system.addCustomEntity(id, name, subType, "neutral");
        if (hasCustomSpin) {
            resultingEntity.setCircularOrbitWithSpin(tracer, flatteningAngle, o.E, bodyOrbitPeriod, customSpin, customSpin);
        } else {
            resultingEntity.setCircularOrbit(tracer, flatteningAngle, o.E, bodyOrbitPeriod);
        }
    } else if (isRock) {
        SectorEntityToken asteroid = SolAsteroidFactory.createAsteroid(system, size, id, name, !(subType == "no_name"));
        asteroid.setCircularOrbitWithSpin(tracer, flatteningAngle, o.E, bodyOrbitPeriod, customSpin, customSpin);
        resultingEntity = asteroid;
    } else {
        resultingEntity = system.addPlanet(id, tracer, name, type, flatteningAngle, size, o.E, bodyOrbitPeriod);
    }
    return resultingEntity;
}
    
// Yay spawnbeanentity depreciated
// YAY NO MORE SPAWN PLANETOID OR SPAWN ASTEROID
// Yay Binarycomponent - elliptical orbit fusion
// Consider - the ultimate fusion (sps passes to elliptical component)
private SectorEntityToken spawnEllipticalComponent(StarSystemAPI system, SectorEntityToken focus,
    String id, String name, float size,
    String type, String subType,
    float sma, float ecc,
    float period, float longPeri, float phase,
    Float rotationalPeriod) {

    float minorAxisFactor = (float) Math.sqrt(1f - ecc * ecc);
    float smb    = sma * minorAxisFactor;
    float linEcc = sma * ecc;
    float r_rot  = (sma + smb) * 0.5f;
    float r_cn   = (sma - smb) * 0.5f;

    boolean isPlanet = type.equalsIgnoreCase("planet");
    Float resolvedPeriod = rotationalPeriod;
    if (resolvedPeriod == null && !isPlanet) {
        resolvedPeriod = getRandomRotationPeriod(size);
    }
    boolean wantSpin = (resolvedPeriod != null) && resolvedPeriod != 0f && !isPlanet;
    float spin = wantSpin ? getRot(resolvedPeriod) : 0f;

    // ==========================================
    // BRANCH 1: CIRCULAR
    // ==========================================
    if (Math.abs(ecc) < Eccentricity_Cutoff) {
        if (type.equalsIgnoreCase("planet")) {
            String pType = (subType != null) ? subType : "barren";
            return system.addPlanet(id, focus, name, pType, phase, size, sma, period);
        }
        SectorEntityToken e = type.equalsIgnoreCase("custom_entity")
            ? system.addCustomEntity(id, name, subType, "neutral")
            : SolAsteroidFactory.createAsteroid(system, size, id, name, !(subType == "no_name"));
        if (wantSpin) e.setCircularOrbitWithSpin(focus, phase, sma, period, spin, spin);
        else          e.setCircularOrbit(focus, phase, sma, period);
        return e;
    }

    // ==========================================
    // BRANCH 2A: DIRECT KEPLER
    // ==========================================
    if (occultOrbitBeta) {
        SectorEntityToken e;
        if (type.equalsIgnoreCase("planet")) {
            String pType = (subType != null) ? subType : "barren";
            e = system.addPlanet(id, focus, name, pType, phase, size, sma, period);
        } else {
            e = type.equalsIgnoreCase("custom_entity")
                ? system.addCustomEntity(id, name, subType, "neutral")
                : SolAsteroidFactory.createAsteroid(system, size, id, name, !(subType == "no_name"));
            if (wantSpin) e.setCircularOrbitWithSpin(focus, phase, sma, period, spin, spin);
            else          e.setCircularOrbit(focus, phase, sma, period);
        }

        CompoundOrbit orbit = wantSpin
            ? new CompoundOrbit(focus,
                Arrays.asList(new KeplerComponent(sma, ecc, longPeri, period, phase, +1f)),
                spin, spin)
            : new CompoundOrbit(focus,
                new KeplerComponent(sma, ecc, longPeri, period, phase, +1f));
        orbit.setEntity(e);
        e.setOrbit(orbit);
        orbit.advance(0);
        return e;
    }

    // ==========================================
    // BRANCH 2B: LEGACY EPICYCLE
    // ==========================================
    SectorEntityToken center = system.addCustomEntity(null, null, "empty", "neutral");
    center.setCircularOrbit(focus, longPeri, linEcc, 10000000f);

    SectorEntityToken tracer = system.addCustomEntity(null, null, "empty", "neutral");
    tracer.setCircularOrbit(center, phase, r_rot, period);

    if (type.equalsIgnoreCase("planet")) {
        String pType = (subType != null) ? subType : "barren";
        return system.addPlanet(id, tracer, name, pType, phase, size, r_cn, -period);
    }
    SectorEntityToken e = type.equalsIgnoreCase("custom_entity")
        ? system.addCustomEntity(id, name, subType, "neutral")
        : SolAsteroidFactory.createAsteroid(system, size, id, name, !(subType == "no_name"));
    if (wantSpin) e.setCircularOrbitWithSpin(tracer, phase, r_cn, -period, spin, spin);
    else          e.setCircularOrbit(tracer, phase, r_cn, -period);
    return e;
}

// =================================================================================================================
// PUBLIC ENTRY POINTS
// =================================================================================================================

public SectorEntityToken[] spawnEllipticalBinary(StarSystemAPI system, SectorEntityToken barycenter,
        String id1, String name1, float size1, String type1, String subType1,
        String id2, String name2, float size2, String type2, String subType2,
        float totalSMA, float ecc, float period, float angleApoapsis) {

    double m1 = Math.pow(size1, 3);
    double m2 = Math.pow(size2, 3);
    double totalMass = m1 + m2;

    float sma1 = totalSMA * (float)(m2 / totalMass);
    float sma2 = totalSMA * (float)(m1 / totalMass);
    Float syncRotPeriod = progradeMult * period / rotMult;   // getRot inverts this back to sync rate

    SectorEntityToken body1 = spawnEllipticalComponent(system, barycenter,
        id1, name1, size1, type1, subType1,
        sma1, ecc, period, angleApoapsis, 0f, syncRotPeriod);

    SectorEntityToken body2 = spawnEllipticalComponent(system, barycenter,
        id2, name2, size2, type2, subType2,
        sma2, ecc, period, angleApoapsis + 180f, 0f, syncRotPeriod);

    return new SectorEntityToken[]{ body1, body2 };
}

public SectorEntityToken spawnWithEllipticalOrbit(
        StarSystemAPI system, SectorEntityToken primary,
        String id, String name, String type, String subType,
        float size, float SMA, float ecc,
        float longPeri, float period, float startAnomaly,
        Float rotationalPeriod) {  

    return spawnEllipticalComponent(system, primary,
        id, name, size, type, subType,
        SMA, ecc, period, longPeri, startAnomaly, rotationalPeriod); 
}

public SectorEntityToken[] spawnLempo(StarSystemAPI system, SectorEntityToken star, float zeroDegGlobal) {
// consider : not.
    float sz_Lempo = getSize(272f);
    float sz_Hiisi = getSize(251f);
    float sz_Paha  = getSize(132f);

    float funMult = 3.14159f;
    float p_Inner = getTime(1.9f) / funMult;
    float p_Outer = getTime(50f)  / funMult;

    double mL   = Math.pow(sz_Lempo, 3);
    double mH   = Math.pow(sz_Hiisi, 3);
    double mP   = Math.pow(sz_Paha,  3);
    double mLH  = mL + mH;
    double mAll = mLH + mP;

    float helio_SMA = 39.7192f;
    float helio_e   = 0.2298f;
    Orbit oHelio    = getOrbit(helio_SMA, helio_e, star);
    float helio_a   = oHelio.A;
    float helio_ge  = oHelio.C / oHelio.A;
    float helio_lp  = 97.167f + 295.825f + zeroDegGlobal;
    float helio_M   = SPSUtils.getMeanAnomalyFromEpoch(currentYear, 2015.73f, helio_SMA, null);
    float helio_p   = getTime((float)(Math.sqrt(Math.pow(helio_SMA, 3)) * 365.25f));

    float outer_sma = 292f;
    float outer_e   = 0.29f;
    float outer_lp  = 180f;
    float a_IB      = outer_sma * (float)(mP / mAll);

    float inner_sma = 80f;
    float inner_e   = 0.15f;
    float inner_lp  = 0f;
    float a_L       = inner_sma * (float)(mH / mLH);

    // =====================================================================

    PlanetAPI Lempo = (PlanetAPI) system.addPlanet("Lempo", star, "Lempo", "rocky_ice",
        0f, sz_Lempo, helio_a, helio_p);

    CompoundOrbit oL = new CompoundOrbit(star,
        new KeplerComponent(helio_a, helio_ge, helio_lp, helio_p, helio_M, +1f),
        new KeplerComponent(a_IB,    outer_e,  outer_lp, p_Outer, 0f,     +1f),
        new KeplerComponent(a_L,     inner_e,  inner_lp, p_Inner, 0f,     +1f)
    );
    oL.setEntity(Lempo); Lempo.setOrbit(oL); oL.advance(0);

    PlanetAPI Hiisi = (PlanetAPI) system.addPlanet("Hiisi", Lempo, "Hiisi", "rocky_ice",
        180f, sz_Hiisi, inner_sma, p_Inner);

    CompoundOrbit oH = new CompoundOrbit(Lempo,
        new KeplerComponent(inner_sma, inner_e, inner_lp, p_Inner, 0f, -1f)
    );
    oH.setEntity(Hiisi); Hiisi.setOrbit(oH); oH.advance(0);

    float pahaSpin = 100f / sz_Paha;
    SectorEntityToken Paha = SolAsteroidFactory.createAsteroid(system, sz_Paha, "Paha", "Paha", true);
    Paha.setCircularOrbit(Lempo, 0f, outer_sma, p_Outer);

    List<KeplerComponent> pahaComps = new ArrayList<KeplerComponent>(2);
    pahaComps.add(new KeplerComponent(outer_sma, outer_e, outer_lp, p_Outer, 0f, -1f));
    pahaComps.add(new KeplerComponent(a_L,       inner_e, inner_lp, p_Inner, 0f, -1f));

    CompoundOrbit oP = new CompoundOrbit(Lempo, pahaComps, pahaSpin, pahaSpin + 10f);
    oP.setEntity(Paha);
    Paha.setOrbit(oP);
    oP.advance(0);

    return new SectorEntityToken[]{ Lempo, Hiisi, Paha };
}

// =========================================================================
// RANDOM ROTATION PERIOD 
// =========================================================================
public float getRandomRotationPeriod(float diameterKM) {
    return getRandomRotationPeriod(diameterKM, new Random());
}

public float getRandomRotationPeriod(float diameterKM, Random rng) {
    float d = Math.max(diameterKM, 0.01f);
    double logD = Math.log10(d);

    double medianHours;
    if (logD < 4.0) {
        medianHours = 3.0 + 4.5 * Math.pow(Math.max(0, logD + 1.0) / 4.78, 1.6) * 5.0;
    } else {
        double t = (logD - 4.0) / 1.5;
        medianHours = 22.0 - 12.0 * Math.min(t, 1.0);
    }

    double sigma;
    if (logD < 1.5)        sigma = 0.55;
    else if (logD < 3.0)   sigma = 0.40;
    else if (logD < 4.0)   sigma = 0.30;
    else                   sigma = 0.10;

    double u1 = Math.max(rng.nextDouble(), 1e-9);
    double u2 = rng.nextDouble();
    double z = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);

    double sampledHours = medianHours * Math.exp(sigma * z);

    if (logD < 4.0 && rng.nextFloat() < 0.04f) {
        sampledHours *= 20.0 + rng.nextDouble() * 200.0;
    }

    sampledHours = Math.max(sampledHours, 1.8);

    float retroChance;
    if (logD < 2.0)        retroChance = 0.28f;
    else if (logD < 4.0)   retroChance = 0.15f;
    else                   retroChance = 0.08f;
    float sign = (rng.nextFloat() < retroChance) ? -1f : 1f;

    return sign * (float) (sampledHours / 24.0);
}
}