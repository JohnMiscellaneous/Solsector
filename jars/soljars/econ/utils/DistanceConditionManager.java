package soljars.econ.utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.util.Misc;

/**
 * Manages distance-based market conditions and atmosphere freeze/thaw.
 *
 * Registry storage: a List<String> of market IDs kept in sector memory under
 * MEM_REGISTRY. Single source of truth, persisted across saves natively by
 * sector memory. No transient state, no tag scans, no init-order fragility.
 *
 * track(market) appends the ID. The monthly listener reads the list,
 * resolves each via Economy.getMarket(id), and acts.
 */
public class DistanceConditionManager implements EconomyTickListener {

    public static final String MEM_REGISTRY = "$sol_dcm_registry";  // List<String>

    public static final String ID_DISTANT   = "sol_dist_distant";
    public static final String ID_ABYSSAL   = "sol_dist_abyssal";
    public static final String ID_HADAL     = "sol_dist_hadal";
    public static final String ID_EREBAL    = "sol_dist_erebal";
    public static final String ID_TARTAREAN = "sol_dist_tartarean";
    public static final String ID_OORTAL    = "sol_dist_oortal";

    public static final String ID_IRRADIATED    = "irradiated";
    public static final String ID_CIRCUMSTELLAR = "sol_circumstellar";

    public static final String ID_FROZEN_ATM  = "sol_frozen_atmosphere";
    public static final String ID_TENOUS_ATM  = "sol_tenous_atmosphere";

    public static final String THIN_ATM       = "thin_atmosphere";
    public static final String DENSE_ATM      = "dense_atmosphere";
    public static final String NO_ATM         = "no_atmosphere";
    public static final String HABITABLE      = "habitable";
    public static final String TOXIC_ATM      = "toxic_atmosphere";
    public static final String EXTREME_WEATH  = "extreme_weather";

    public static final String MEM_ATM_LEVEL  = "$sol_atmosphere_level";
    public static final String MEM_LAST_APPLIED = "$sol_atmosphere_last_applied";
    public static final String MEM_NO_FREEZE  = "$sol_no_freeze";

    public static final float SUBLIMATION_AU = 40f;

    public static final float DISTANT_MAX_AU   = 20f;
    public static final float ABYSSAL_MAX_AU   = 60f;
    public static final float HADAL_MAX_AU     = 100f;
    public static final float EREBAL_MAX_AU    = 200f;
    public static final float TARTAREAN_MAX_AU = 2000f;

    public static final float IRRADIATED_MIN_AU = 122.5f;
    public static final float IRRADIATED_MAX_AU = 127.5f;
    public static final float CIRCUMSTELLAR_MIN_AU = 127.5f;

    // ------------------------------------------------------------------
    // Listener registration. Plugin calls install() on every onGameLoad.
    // ------------------------------------------------------------------

    public static void install() {
        ListenerManagerAPI lm = Global.getSector().getListenerManager();
        lm.removeListenerOfClass(DistanceConditionManager.class);
        lm.addListener(new DistanceConditionManager());
        List<String> reg = registry();
        Global.getLogger(DistanceConditionManager.class).info(
                "[DCM] install() - listener registered. Registry has "
                + reg.size() + " entries. ListenerManager hasDCM: "
                + lm.hasListenerOfClass(DistanceConditionManager.class));
    }

    // ------------------------------------------------------------------
    // Registry: a List<String> of market IDs in sector memory. Sector
    // memory is saved natively, so this survives across loads with no
    // listener-side state.
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static List<String> registry() {
        MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();
        List<String> list = (List<String>) mem.get(MEM_REGISTRY);
        if (list == null) {
            list = new ArrayList<>();
            mem.set(MEM_REGISTRY, list);
            Global.getLogger(DistanceConditionManager.class).info(
                    "[DCM] registry() - created new empty registry in sector memory");
        }
        return list;
    }

    public static void track(MarketAPI market) {
        if (market == null) {
            Global.getLogger(DistanceConditionManager.class).warn(
                    "[DCM] track(null) - ignored");
            return;
        }

        List<String> reg = registry();
        boolean wasNew = !reg.contains(market.getId());
        if (wasNew) {
            reg.add(market.getId());
        }

        Global.getLogger(DistanceConditionManager.class).info(
                "[DCM] track(" + market.getId() + ") - " + (wasNew ? "ADDED" : "already present")
                + ". Registry now has " + reg.size() + " entries.");

        // Run detect+reconcile right away so initial state lands at spawn.
        detectAtmosphereChanges(market);
        reconcile(market);
    }

    public static void untrack(MarketAPI market) {
        if (market == null) return;

        boolean removed = registry().remove(market.getId());
        Global.getLogger(DistanceConditionManager.class).info(
                "[DCM] untrack(" + market.getId() + ") - "
                + (removed ? "REMOVED" : "not in registry")
                + ". Registry now has " + registry().size() + " entries.");

        String[] toRemove = {
                ID_DISTANT, ID_ABYSSAL, ID_HADAL, ID_EREBAL, ID_TARTAREAN, ID_OORTAL,
                ID_IRRADIATED, ID_CIRCUMSTELLAR,
                ID_FROZEN_ATM, ID_TENOUS_ATM,
        };
        for (String id : toRemove) {
            if (market.hasCondition(id)) market.removeCondition(id);
        }
    }

    /** Resolve registry IDs to actual market objects. Economy.getMarket(id)
     *  only finds COLONIZED markets - uncolonized PCMs (Pluto, Eris, etc.)
     *  live on PlanetAPI.getMarket() but aren't in the economy. So we build
     *  a full id->market map by walking both sources, then look up each
     *  registry ID against it. */
    private static List<MarketAPI> resolveAll() {
        // Build id -> market index from every market we can find in the sector.
        java.util.Map<String, MarketAPI> index = new java.util.HashMap<>();
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            if (m != null) index.put(m.getId(), m);
        }
        for (StarSystemAPI sys : Global.getSector().getStarSystems()) {
            for (PlanetAPI p : sys.getPlanets()) {
                MarketAPI pcm = p.getMarket();
                if (pcm != null) index.put(pcm.getId(), pcm);
            }
        }

        LinkedHashSet<MarketAPI> out = new LinkedHashSet<>();
        List<String> reg = registry();
        int pruned = 0;
        for (String id : new ArrayList<>(reg)) {
            MarketAPI m = index.get(id);
            if (m != null) {
                out.add(m);
            } else {
                reg.remove(id);
                pruned++;
            }
        }
        if (pruned > 0) {
            Global.getLogger(DistanceConditionManager.class).warn(
                    "[DCM] resolveAll() - pruned " + pruned + " stale registry entries");
        }
        Global.getLogger(DistanceConditionManager.class).info(
                "[DCM] resolveAll() - registry has " + reg.size() + " entries, "
                + out.size() + " resolved to live markets (index size: " + index.size() + ")");
        return new ArrayList<>(out);
    }

    // ------------------------------------------------------------------
    // Monthly hook.
    // ------------------------------------------------------------------

    @Override
    public void reportEconomyTick(int iterIndex) {
        if (iterIndex == 0) {
            Global.getLogger(DistanceConditionManager.class).info(
                    "[DCM] reportEconomyTick fired (iter=0)");
        }
    }

    @Override
    public void reportEconomyMonthEnd() {
        Global.getLogger(DistanceConditionManager.class).info(
                "[DCM] ===== reportEconomyMonthEnd FIRED =====");
        List<MarketAPI> markets = resolveAll();
        for (MarketAPI m : markets) {
            String before = stateSnapshot(m);
            detectAtmosphereChanges(m);
            reconcile(m);
            String after = stateSnapshot(m);
            if (!before.equals(after)) {
                Global.getLogger(DistanceConditionManager.class).info(
                        "[DCM] " + m.getId() + ": " + before + " -> " + after);
            }
        }
        Global.getLogger(DistanceConditionManager.class).info(
                "[DCM] ===== monthly hook done =====");
    }

    /** Compact one-line snapshot of all DCM-relevant state on a market.
     *  Used to log only the bodies that actually changed each tick. */
    private static String stateSnapshot(MarketAPI m) {
        StringBuilder sb = new StringBuilder();
        sb.append(currentBand(m));
        if (m.hasCondition(ID_IRRADIATED))    sb.append("+irr");
        if (m.hasCondition(ID_CIRCUMSTELLAR)) sb.append("+crc");
        if (m.hasCondition(ID_FROZEN_ATM))    sb.append("+frozen");
        if (m.hasCondition(ID_TENOUS_ATM))    sb.append("+tenous");
        if (m.hasCondition(NO_ATM))           sb.append("+noatm");
        if (m.hasCondition(THIN_ATM))         sb.append("+thin");
        if (m.hasCondition(DENSE_ATM))        sb.append("+dense");
        if (m.getMemoryWithoutUpdate().contains(MEM_ATM_LEVEL)) {
            sb.append("(lvl=").append(m.getMemoryWithoutUpdate().getInt(MEM_ATM_LEVEL));
            if (m.getMemoryWithoutUpdate().contains(MEM_LAST_APPLIED)) {
                sb.append(",last=").append(m.getMemoryWithoutUpdate().getInt(MEM_LAST_APPLIED));
            }
            sb.append(")");
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Reconcile: distance band + overlays + atmosphere freeze/thaw.
    // ------------------------------------------------------------------

    public static void reconcile(MarketAPI market) {
        if (market == null) return;

        float au = DistanceCheck.getMarketAU(market);

        // Primary band.
        String desired = bandFor(au);
        String current = currentBand(market);
        if (!desired.equals(current)) {
            if (current == null) {
                market.addCondition(desired);
            } else {
                RemoveReplace.execute(market, current, desired);
            }
            notify(market, market.getName() + " has moved from being "
                    + bandWord(current) + " to " + bandWord(desired) + ".");
        }

        // Irradiated overlay.
        boolean shouldIrradiate = au >= IRRADIATED_MIN_AU && au < IRRADIATED_MAX_AU;
        boolean hasIrradiated = market.hasCondition(ID_IRRADIATED);
        if (shouldIrradiate && !hasIrradiated) {
            market.addCondition(ID_IRRADIATED);
            notify(market, market.getName()
                    + " has entered a turbulent area of the heliosheath and become irradiated.");
        } else if (!shouldIrradiate && hasIrradiated) {
            market.removeCondition(ID_IRRADIATED);
            notify(market, market.getName()
                    + " has left a turbulent area of the heliosheath and is no longer irradiated.");
        }

        // Circumstellar overlay.
        boolean shouldCircumstellar = au >= CIRCUMSTELLAR_MIN_AU;
        boolean hasCircumstellar = market.hasCondition(ID_CIRCUMSTELLAR);
        if (shouldCircumstellar && !hasCircumstellar) {
            market.addCondition(ID_CIRCUMSTELLAR);
            notify(market, market.getName()
                    + " has passed beyond the heliopause and become circumstellar.");
        } else if (!shouldCircumstellar && hasCircumstellar) {
            market.removeCondition(ID_CIRCUMSTELLAR);
            notify(market, market.getName()
                    + " has reentered the heliosphere and is no longer circumstellar.");
        }

        // Atmosphere freeze/thaw - only flagged, non-overridden bodies.
        if (!market.getMemoryWithoutUpdate().contains(MEM_ATM_LEVEL)) return;
        if (market.getMemoryWithoutUpdate().getBoolean(MEM_NO_FREEZE)) return;

        boolean lampWarm = hasFusionLamp(market);
        boolean shouldFreeze = au > SUBLIMATION_AU && !lampWarm;
        int atmLevel = market.getMemoryWithoutUpdate().getInt(MEM_ATM_LEVEL);
        int target = shouldFreeze ? 0 : atmLevel;
        int observed = observedLevel(market);

        MemoryAPI mem = market.getMemoryWithoutUpdate();
        // Default last_applied to observed if unset - covers first-tick bodies.
        int lastApplied = mem.contains(MEM_LAST_APPLIED)
                ? mem.getInt(MEM_LAST_APPLIED) : observed;

        // Catastrophic loss: body was at its true level, dropped a tier.
        if (lastApplied == atmLevel && observed < lastApplied) {
            if (observed == 0) {
                // Total loss. Atmosphere is gone for good - tear down all
                // atmosphere state. Body is no longer managed by freeze/thaw.
                if (market.hasCondition(ID_FROZEN_ATM)) market.removeCondition(ID_FROZEN_ATM);
                if (market.hasCondition(ID_TENOUS_ATM)) market.removeCondition(ID_TENOUS_ATM);
                mem.unset(MEM_ATM_LEVEL);
                mem.unset(MEM_LAST_APPLIED);
                Global.getLogger(DistanceConditionManager.class).info(
                        "[DCM] " + market.getId() + " total atmosphere loss detected - "
                        + "atmosphere state cleared, body no longer managed");
                return;
            }
            // Partial loss - body keeps its (now lower) atmosphere.
            mem.set(MEM_ATM_LEVEL, observed);
            mem.set(MEM_LAST_APPLIED, observed);
            atmLevel = observed;
            target = shouldFreeze ? 0 : atmLevel;
            Global.getLogger(DistanceConditionManager.class).info(
                    "[DCM] " + market.getId() + " partial atmosphere loss detected: "
                    + "level dropped to " + observed);
        }

        // Already at target. Markers sync at the bottom.
        if (observed == target) {
            mem.set(MEM_LAST_APPLIED, observed);
            syncAtmosphereMarkers(market, observed, atmLevel, shouldFreeze);
            return;
        }

        // Step by 1 toward target.
        int nextLevel = observed < target ? observed + 1 : observed - 1;
        applyAtmosphereLevel(market, nextLevel);
        mem.set(MEM_LAST_APPLIED, nextLevel);

        // Fire transition notifications at the moment the body completes
        // a full freeze (down to 0) or thaw (up to atmLevel).
        if (shouldFreeze && nextLevel == 0) {
            notify(market, market.getName() + "'s " + atmWord(atmLevel)
                    + " atmosphere has frozen onto the surface.");
        } else if (!shouldFreeze && nextLevel == target) {
            notify(market, market.getName() + "'s surface layer of snow has sublimated into a "
                    + atmWord(target) + " atmosphere.");
        }

        syncAtmosphereMarkers(market, nextLevel, atmLevel, shouldFreeze);
    }

    /** Sync the FROZEN/TENOUS display markers and strip habitable/toxic/weather
     *  while the body is below its true atmosphere level. Display rules:
     *  - observed == atmLevel: TENOUS only.
     *  - 0 < observed < atmLevel: TENOUS + FROZEN.
     *  - observed == 0: FROZEN only.
     */
    private static void syncAtmosphereMarkers(MarketAPI market, int observed,
                                              int atmLevel, boolean shouldFreeze) {
        // Strip habitable/toxic/weather whenever the body is below its true
        // atmosphere level - those vanilla conditions depend on full pressure.
        if (observed < atmLevel) {
            if (market.hasCondition(HABITABLE))     market.removeCondition(HABITABLE);
            if (market.hasCondition(TOXIC_ATM))     market.removeCondition(TOXIC_ATM);
            if (market.hasCondition(EXTREME_WEATH)) market.removeCondition(EXTREME_WEATH);
        }

        boolean wantFrozen = observed < atmLevel;
        boolean wantTenous = observed > 0;

        if (wantFrozen && !market.hasCondition(ID_FROZEN_ATM)) market.addCondition(ID_FROZEN_ATM);
        if (!wantFrozen && market.hasCondition(ID_FROZEN_ATM)) market.removeCondition(ID_FROZEN_ATM);

        if (wantTenous && !market.hasCondition(ID_TENOUS_ATM)) market.addCondition(ID_TENOUS_ATM);
        if (!wantTenous && market.hasCondition(ID_TENOUS_ATM)) market.removeCondition(ID_TENOUS_ATM);
    }

    /** Set the visible atmosphere conditions to match a given level (0/1/2/3).
     *  Strips all three vanilla atmosphere conditions, then adds the one matching
     *  the target level. Level 2 has no condition (vanilla "normal atmosphere"
     *  is the absence of thin/dense/none). */
    private static void applyAtmosphereLevel(MarketAPI market, int level) {
        if (market.hasCondition(NO_ATM))    market.removeCondition(NO_ATM);
        if (market.hasCondition(THIN_ATM))  market.removeCondition(THIN_ATM);
        if (market.hasCondition(DENSE_ATM)) market.removeCondition(DENSE_ATM);

        if (level == 0) {
            market.addCondition(NO_ATM);
        } else if (level == 1) {
            market.addCondition(THIN_ATM);
        } else if (level == 3) {
            market.addCondition(DENSE_ATM);
        }
        // level == 2: absence of conditions = normal atmosphere.
    }

    /** Send a campaign message to the player if and only if the market is
     *  player-owned. Used by reconcile to surface band / overlay / atmosphere
     *  transitions on player colonies. */
    private static void notify(MarketAPI market, String msg) {
        if (market == null || !market.isPlayerOwned()) return;
        Global.getSector().getCampaignUI().addMessage(msg, Misc.getHighlightColor());
    }

    /** Human-readable name for a band condition ID. */
    private static String bandWord(String bandId) {
        if (bandId == null)              return "unbanded";
        if (ID_DISTANT.equals(bandId))   return "distant";
        if (ID_ABYSSAL.equals(bandId))   return "abyssal";
        if (ID_HADAL.equals(bandId))     return "hadal";
        if (ID_EREBAL.equals(bandId))    return "erebal";
        if (ID_TARTAREAN.equals(bandId)) return "tartarean";
        if (ID_OORTAL.equals(bandId))    return "oortal";
        return bandId;
    }

    /** Human-readable thickness word for an atmosphere level. */
    private static String atmWord(int level) {
        if (level <= 1) return "thin";
        if (level == 2) return "regular";
        return "dense";
    }

    // ------------------------------------------------------------------
    // Detection: adapt to unexpected atmosphere changes before reconcile.
    // ------------------------------------------------------------------

    public static void detectAtmosphereChanges(MarketAPI market) {
        if (market == null) return;
        if (market.getMemoryWithoutUpdate().getBoolean(MEM_NO_FREEZE)) return;

        if (market.getMemoryWithoutUpdate().contains(MEM_ATM_LEVEL)) {
            boolean hasFrozenCondition = market.hasCondition(ID_FROZEN_ATM);
            boolean hasOverridingAtm = market.hasCondition(THIN_ATM)
                    || market.hasCondition(DENSE_ATM);

            if (hasFrozenCondition && !hasOverridingAtm) return;
            if (hasFrozenCondition && hasOverridingAtm) {
                market.removeCondition(ID_FROZEN_ATM);
                if (market.hasCondition(NO_ATM)) market.removeCondition(NO_ATM);
            }

            int observed = observedLevel(market);
            int stored = market.getMemoryWithoutUpdate().getInt(MEM_ATM_LEVEL);
            if (observed > stored) {
                market.getMemoryWithoutUpdate().set(MEM_ATM_LEVEL, observed);
            }
        } else {
            if (market.hasCondition(NO_ATM)) return;
            int observed = observedLevel(market);
            if (observed >= 1) {
                market.getMemoryWithoutUpdate().set(MEM_ATM_LEVEL, observed);
            }
        }
    }

    /** Read what atmosphere level the body's current conditions correspond to.
     *  Returns 0 if no_atmosphere present, otherwise 1/2/3 based on thin/none/dense. */
    public static int observedLevel(MarketAPI market) {
        if (market.hasCondition(NO_ATM))    return 0;
        if (market.hasCondition(DENSE_ATM)) return 3;
        if (market.hasCondition(THIN_ATM))  return 1;
        return 2;
    }

    /** Detect an orbital fusion lamp on the population industry. Solar arrays
     *  are deliberately ignored here - they're insufficient warmth to drive
     *  the full freeze/thaw cycle. */
    public static boolean hasFusionLamp(MarketAPI market) {
        if (market == null) return false;
        Industry pop = market.getIndustry(Industries.POPULATION);
        if (pop == null) return false;
        SpecialItemData special = pop.getSpecialItem();
        if (special == null) return false;
        return Items.ORBITAL_FUSION_LAMP.equals(special.getId());
    }

    // ------------------------------------------------------------------
    // Band helpers.
    // ------------------------------------------------------------------

    @Deprecated
    public static void applyCorrectBand(MarketAPI market) {
        reconcile(market);
    }

    public static String bandFor(float au) {
        if (au < DISTANT_MAX_AU)   return ID_DISTANT;
        if (au < ABYSSAL_MAX_AU)   return ID_ABYSSAL;
        if (au < HADAL_MAX_AU)     return ID_HADAL;
        if (au < EREBAL_MAX_AU)    return ID_EREBAL;
        if (au < TARTAREAN_MAX_AU) return ID_TARTAREAN;
        return ID_OORTAL;
    }

    private static String currentBand(MarketAPI market) {
        if (market.hasCondition(ID_OORTAL))    return ID_OORTAL;
        if (market.hasCondition(ID_TARTAREAN)) return ID_TARTAREAN;
        if (market.hasCondition(ID_EREBAL))    return ID_EREBAL;
        if (market.hasCondition(ID_HADAL))     return ID_HADAL;
        if (market.hasCondition(ID_ABYSSAL))   return ID_ABYSSAL;
        if (market.hasCondition(ID_DISTANT))   return ID_DISTANT;
        return null;
    }

    // ------------------------------------------------------------------
    // Debug.
    // ------------------------------------------------------------------

    public static void logAll() {
        org.apache.log4j.Logger log = Global.getLogger(DistanceConditionManager.class);

        List<String> reg = registry();
        List<MarketAPI> markets = resolveAll();

        log.info("DistanceConditionManager: " + reg.size() + " registry entries, "
                + markets.size() + " resolved market(s).");
        log.info(String.format("  Bands: distant <%.1f, abyssal <%.1f, hadal <%.1f, erebal <%.1f, tartarean <%.1f, oortal >=%.1f",
                DISTANT_MAX_AU, ABYSSAL_MAX_AU, HADAL_MAX_AU, EREBAL_MAX_AU, TARTAREAN_MAX_AU, TARTAREAN_MAX_AU));
        log.info(String.format("  Overlays: irradiated [%.1f, %.1f), circumstellar >=%.1f",
                IRRADIATED_MIN_AU, IRRADIATED_MAX_AU, CIRCUMSTELLAR_MIN_AU));

        if (reg.size() != markets.size()) {
            log.info("  WARNING: " + (reg.size() - markets.size())
                    + " registry entries failed to resolve and were pruned.");
        }

        for (MarketAPI m : markets) {
            float au = DistanceCheck.getMarketAU(m);
            String desired = bandFor(au);
            String current = currentBand(m);
            String mismatch = desired.equals(current) ? "" : "  <-- BAND MISMATCH";

            boolean shouldIrradiate = au >= IRRADIATED_MIN_AU && au < IRRADIATED_MAX_AU;
            boolean hasIrradiated = m.hasCondition(ID_IRRADIATED);
            boolean shouldCirc = au >= CIRCUMSTELLAR_MIN_AU;
            boolean hasCirc = m.hasCondition(ID_CIRCUMSTELLAR);

            String overlayState =
                    (hasIrradiated ? "[irr]" : "     ") +
                    (hasCirc       ? "[crc]" : "     ");
            String overlayMismatch = "";
            if (shouldIrradiate != hasIrradiated) overlayMismatch += " IRR-MISMATCH";
            if (shouldCirc != hasCirc)            overlayMismatch += " CRC-MISMATCH";

            String atmState = "         ";
            if (m.getMemoryWithoutUpdate().contains(MEM_ATM_LEVEL)) {
                int lvl = m.getMemoryWithoutUpdate().getInt(MEM_ATM_LEVEL);
                boolean lampWarm = hasFusionLamp(m);
                boolean noFreeze = m.getMemoryWithoutUpdate().getBoolean(MEM_NO_FREEZE);
                String mark = m.hasCondition(ID_FROZEN_ATM) ? "FROZEN"
                        : m.hasCondition(ID_TENOUS_ATM) ? "TENOUS"
                        : "      ";
                atmState = String.format(" [atm%d %s%s%s]", lvl, mark,
                        lampWarm ? " L" : "  ",
                        noFreeze ? " NF" : "   ");
            }

            log.info(String.format("  %-30s  %7.2f AU  desired=%-15s current=%-15s %s%s%s%s",
                    m.getName() + " (" + m.getId() + ")",
                    au,
                    desired,
                    current == null ? "none" : current,
                    overlayState,
                    atmState,
                    mismatch,
                    overlayMismatch));
        }
    }
}