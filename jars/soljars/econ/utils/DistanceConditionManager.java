package soljars.econ.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;

public class DistanceConditionManager implements EconomyTickListener {

    public static final String MANAGED_TAG = "sol_distance_managed";

    public static final String ID_DISTANT   = "sol_dist_distant";
    public static final String ID_ABYSSAL   = "sol_dist_abyssal";
    public static final String ID_HADAL     = "sol_dist_hadal";
    public static final String ID_EREBAL    = "sol_dist_erebal";
    public static final String ID_TARTAREAN = "sol_dist_tartarean";
    public static final String ID_OORTAL    = "sol_dist_oortal";

    public static final String ID_IRRADIATED    = "irradiated";      
    public static final String ID_CIRCUMSTELLAR = "sol_circumstellar";



    // 0-20 distant, 
    // 20-60 abyssal, 
    // 60-100 hadal, 
    // 100-122.5 erebal,
    // 122.5-127.5 erebal, irradiated
    // 127.5-200 erebal, cicumstellar
    // 200-2000 tartarean, cicumstellar
    // 2000+ oortal, cicumstellar
    public static final float DISTANT_MAX_AU   = 20f;
    public static final float ABYSSAL_MAX_AU   = 60f;
    public static final float HADAL_MAX_AU     = 100f;
    public static final float EREBAL_MAX_AU    = 200f;
    public static final float TARTAREAN_MAX_AU = 2000f;

    public static final float IRRADIATED_MIN_AU = 122.5f;
    public static final float IRRADIATED_MAX_AU = 127.5f;
    public static final float CIRCUMSTELLAR_MIN_AU = 127.5f;

    private transient Set<MarketAPI> tracked = new HashSet<>();

    // rebuild registry
    public static void install() {
        ListenerManagerAPI lm = Global.getSector().getListenerManager();
        lm.removeListenerOfClass(DistanceConditionManager.class);

        DistanceConditionManager mgr = new DistanceConditionManager();
        mgr.rebuildRegistry();
        lm.addListener(mgr, true);
    }

    public static void track(MarketAPI market) {
        if (market == null) return;

        if (!market.hasTag(MANAGED_TAG)) {
            market.addTag(MANAGED_TAG);
        }

        DistanceConditionManager mgr = find();
        if (mgr != null) {
            mgr.tracked.add(market);
        }

        reconcile(market);
    }

    public static void untrack(MarketAPI market) {
        if (market == null) return;

        market.removeTag(MANAGED_TAG);

        DistanceConditionManager mgr = find();
        if (mgr != null) {
            mgr.tracked.remove(market);
        }

        String[] toRemove = {
                ID_DISTANT, ID_ABYSSAL, ID_HADAL, ID_EREBAL, ID_TARTAREAN, ID_OORTAL,
                ID_IRRADIATED, ID_CIRCUMSTELLAR,
        };
        for (String id : toRemove) {
            if (market.hasCondition(id)) {
                market.removeCondition(id);
            }
        }
    }

    private static DistanceConditionManager find() {
        ListenerManagerAPI lm = Global.getSector().getListenerManager();
        List<DistanceConditionManager> list =
                lm.getListeners(DistanceConditionManager.class);
        if (list == null || list.isEmpty()) return null;
        return list.get(0);
    }

    private void rebuildRegistry() {
        tracked = new HashSet<>();

        // Colonized markets in the economy.
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            if (m != null && m.hasTag(MANAGED_TAG)) {
                tracked.add(m);
            }
        }

        // PCMs on planets across all star systems.
        for (com.fs.starfarer.api.campaign.StarSystemAPI sys
                : Global.getSector().getStarSystems()) {
            for (com.fs.starfarer.api.campaign.PlanetAPI p : sys.getPlanets()) {
                MarketAPI pcm = p.getMarket();
                if (pcm != null && pcm.hasTag(MANAGED_TAG)) {
                    tracked.add(pcm);
                }
            }
        }
    }

    @Override
    public void reportEconomyTick(int iterIndex) {
        // nothing
    }

    @Override
    public void reportEconomyMonthEnd() {

        Set<MarketAPI> snapshot = new HashSet<>(tracked);
        for (MarketAPI m : snapshot) {
            if (m == null) continue;

            if (!m.hasTag(MANAGED_TAG)) {
                tracked.remove(m);
                continue;
            }

            reconcile(m);
        }
    }

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
        }

        // Irradiated overlay.
        boolean shouldIrradiate =
                au >= IRRADIATED_MIN_AU && au < IRRADIATED_MAX_AU;
        boolean hasIrradiated = market.hasCondition(ID_IRRADIATED);
        if (shouldIrradiate && !hasIrradiated) {
            market.addCondition(ID_IRRADIATED);
        } else if (!shouldIrradiate && hasIrradiated) {
            market.removeCondition(ID_IRRADIATED);
        }

        // Circumstellar overlay.
        boolean shouldCircumstellar = au >= CIRCUMSTELLAR_MIN_AU;
        boolean hasCircumstellar = market.hasCondition(ID_CIRCUMSTELLAR);
        if (shouldCircumstellar && !hasCircumstellar) {
            market.addCondition(ID_CIRCUMSTELLAR);
        } else if (!shouldCircumstellar && hasCircumstellar) {
            market.removeCondition(ID_CIRCUMSTELLAR);
        }
    }

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

    // DEBUG
    public static void logAll() {
        org.apache.log4j.Logger log =
                Global.getLogger(DistanceConditionManager.class);

        DistanceConditionManager mgr = find();
        if (mgr == null) {
            log.info("DistanceConditionManager: no manager installed.");
            return;
        }

        log.info("DistanceConditionManager: " + mgr.tracked.size() + " tracked market(s).");
        log.info(String.format("  Bands: distant <%.1f, abyssal <%.1f, hadal <%.1f, erebal <%.1f, tartarean <%.1f, oortal >=%.1f",
                DISTANT_MAX_AU, ABYSSAL_MAX_AU, HADAL_MAX_AU, EREBAL_MAX_AU, TARTAREAN_MAX_AU, TARTAREAN_MAX_AU));
        log.info(String.format("  Overlays: irradiated [%.1f, %.1f), circumstellar >=%.1f",
                IRRADIATED_MIN_AU, IRRADIATED_MAX_AU, CIRCUMSTELLAR_MIN_AU));

        for (MarketAPI m : mgr.tracked) {
            if (m == null) {
                log.info("  [null market in registry]");
                continue;
            }
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

            log.info(String.format("  %-30s  %7.2f AU  desired=%-15s current=%-15s %s%s%s",
                    m.getName() + " (" + m.getId() + ")",
                    au,
                    desired,
                    current == null ? "none" : current,
                    overlayState,
                    mismatch,
                    overlayMismatch));
        }
    }
}