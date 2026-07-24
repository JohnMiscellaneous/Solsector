package soljars.gen.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;

import soljars.gen.terrain.CometComa;
import soljars.gen.terrain.CometTail;

/**
 * Adds and removes coma/tail terrain on comet entities as they cross their
 * activation distance. Entity-side counterpart to DistanceConditionManager -
 * that one handles planet market conditions (of the comets, only Chiron is a
 * planet); this one handles terrain on the rest.
 */
public class CometTerrainManager implements EconomyTickListener {

    public static final String MEM_REGISTRY = "$sol_comet_terrain_registry";  // List<String>

    public static final String MEM_COMET_TERRAIN_DISTANCE = "$sol_comet_terrain_distance";  // AU
    public static final String MEM_COMET_MAGNITUDE = "$sol_comet_magnitude";                // game units

    private static final String MEM_TERRAIN_IDS = "$sol_comet_terrain_ids";   // List<String>
    private static final String MEM_BUILT_MAGNITUDE = "$sol_comet_terrain_built_magnitude";
    public static final String MEM_COMET_NO_TAIL = "$sol_comet_no_tail";
    // ------------------------------------------------------------------
    // Listener registration. Plugin calls install() on every onGameLoad.
    // ------------------------------------------------------------------

    public static void install() {
        ListenerManagerAPI lm = Global.getSector().getListenerManager();
        lm.removeListenerOfClass(CometTerrainManager.class);
        lm.addListener(new CometTerrainManager());
    }

    // ------------------------------------------------------------------
    // Registry: entity IDs in sector memory, saved natively.
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static List<String> registry() {
        MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();
        List<String> list = (List<String>) mem.get(MEM_REGISTRY);
        if (list == null) {
            list = new ArrayList<>();
            mem.set(MEM_REGISTRY, list);
        }
        return list;
    }

    public static void track(SectorEntityToken comet) {
        if (comet == null) return;
        List<String> reg = registry();
        if (!reg.contains(comet.getId())) reg.add(comet.getId());
        reconcile(comet);
    }

    public static void untrack(SectorEntityToken comet) {
        if (comet == null) return;
        registry().remove(comet.getId());
        clearTerrain(comet);
    }

    private static List<SectorEntityToken> resolveAll() {
        Map<String, SectorEntityToken> index = new HashMap<>();
        for (StarSystemAPI sys : Global.getSector().getStarSystems()) {
            for (SectorEntityToken e : sys.getAllEntities()) {
                if (e != null) index.put(e.getId(), e);
            }
        }

        List<SectorEntityToken> out = new ArrayList<>();
        List<String> reg = registry();
        for (String id : new ArrayList<>(reg)) {
            SectorEntityToken e = index.get(id);
            if (e != null) out.add(e);
            else reg.remove(id);
        }
        return out;
    }

    // ------------------------------------------------------------------
    // Monthly hook.
    // ------------------------------------------------------------------

    @Override
    public void reportEconomyTick(int iterIndex) {
        for (SectorEntityToken comet : resolveAll()) {
            reconcile(comet);
        }
    }

    @Override
    public void reportEconomyMonthEnd() {

    }

    // ------------------------------------------------------------------
    // Reconcile: terrain present iff the comet is inside its activation
    // distance, and built at the magnitude currently on the entity.
    // ------------------------------------------------------------------

    public static void reconcile(SectorEntityToken comet) {
        if (comet == null) return;

        MemoryAPI mem = comet.getMemoryWithoutUpdate();
        if (!mem.contains(MEM_COMET_TERRAIN_DISTANCE)) return;

        float magnitude = mem.contains(MEM_COMET_MAGNITUDE) ? mem.getFloat(MEM_COMET_MAGNITUDE) : 0f;
        boolean shouldHave = magnitude > 0f && getAU(comet) < mem.getFloat(MEM_COMET_TERRAIN_DISTANCE);

        if (!shouldHave) {
            clearTerrain(comet);
            return;
        }

        // rebuild if the magnitude changed under a live terrain, since the
        // plugins bake their geometry from it
        if (hasTerrain(comet)) {
            if (mem.getFloat(MEM_BUILT_MAGNITUDE) == magnitude) return;
            clearTerrain(comet);
        }

        addTerrain(comet, magnitude);
        mem.set(MEM_BUILT_MAGNITUDE, magnitude);
    }

    private static float getAU(SectorEntityToken comet) {
        float x = comet.getLocation().x, y = comet.getLocation().y;
        return new AstroCalc().getAU((float) Math.sqrt(x * x + y * y));
    }

    // ------------------------------------------------------------------
    // Terrain add/remove. Terrain entity IDs are stored on the comet so the
    // pair can be found again after a load with no listener-side state.
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static List<String> terrainIds(SectorEntityToken comet) {
        MemoryAPI mem = comet.getMemoryWithoutUpdate();
        List<String> list = (List<String>) mem.get(MEM_TERRAIN_IDS);
        if (list == null) {
            list = new ArrayList<>();
            mem.set(MEM_TERRAIN_IDS, list);
        }
        return list;
    }

    private static boolean hasTerrain(SectorEntityToken comet) {
        return !terrainIds(comet).isEmpty();
    }

    private static void addTerrain(SectorEntityToken comet, float magnitude) {
        LocationAPI loc = comet.getContainingLocation();
        if (!(loc instanceof StarSystemAPI)) return;
        StarSystemAPI system = (StarSystemAPI) loc;

        List<String> ids = terrainIds(comet);

        SectorEntityToken coma = system.addTerrain("sol_comet_coma",
                new CometComa.CometComaParams(magnitude, comet.getName() + " Coma"));
        coma.setCircularOrbit(comet, 0f, 0f, 100f);
        ids.add(coma.getId());

        if (comet.getMemoryWithoutUpdate().getBoolean(MEM_COMET_NO_TAIL)) return;

        SectorEntityToken tail = system.addTerrain("sol_comet_tail",
                new CometTail.CometTailParams(magnitude, comet.getName() + " Tail"));
        tail.setCircularOrbit(comet, 0f, 0f, 100f);
        ids.add(tail.getId());
    }

    private static void clearTerrain(SectorEntityToken comet) {
        List<String> ids = terrainIds(comet);
        if (ids.isEmpty()) return;

        LocationAPI loc = comet.getContainingLocation();
        if (loc != null) {
            for (String id : ids) {
                SectorEntityToken t = loc.getEntityById(id);
                if (t != null) loc.removeEntity(t);
            }
        }
        ids.clear();
        comet.getMemoryWithoutUpdate().unset(MEM_BUILT_MAGNITUDE);
    }

        /** Flag a comet for coma/tail terrain and reconcile it immediately. */
    public static void setup(SectorEntityToken comet, float activationAU, float magnitude) {
        if (comet == null) return;
        MemoryAPI mem = comet.getMemoryWithoutUpdate();
        mem.set(MEM_COMET_TERRAIN_DISTANCE, activationAU);
        mem.set(MEM_COMET_MAGNITUDE, magnitude);
        track(comet);
    }
}