package soljars.econ.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

// pluto
// I                       II                                        II        \                                II
//O          \--o----\                       
// I                       II                                        II        /                                II
// the goblin
// I                       II                                        II        \                                II
//O                            \-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------o---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\
// I                       II                                        II        /                                II
public class OrbitRulerHelper {

    public static final String MEM_ORBIT_MIN = "$sol_orbit_min_au";
    public static final String MEM_ORBIT_MAX = "$sol_orbit_max_au";

    // Font geometry
    private static final float PX_PER_AU = 1.6f;
    private static final int SUN_PX   = 10;
    private static final int SPACE_PX = 4;
    private static final int DASH_PX  = 4;
    private static final int O_PX     = 8;
    private static final int SLASH_PX = 4;
    private static final int II_PX    = 4;  // two I's at 2px each

    // 'o' represents 5 AU of orbital span (8px / 1.6 px-per-AU)
    private static final float O_AU_SPAN = 5f;

    // Tick AU positions (the II markers on top/bottom rows)
    private static final float[] TICK_AUS = {
            DistanceConditionManager.DISTANT_MAX_AU,    // 20
            DistanceConditionManager.ABYSSAL_MAX_AU,    // 60
            DistanceConditionManager.HADAL_MAX_AU,      // 100
            DistanceConditionManager.EREBAL_MAX_AU      // 200
    };

    /**
     * Append the three-row orbit ruler to the given tooltip, with colored
     * highlights on the orbit row matching the band zones.
     */
    public static void render(TooltipMakerAPI tooltip, MarketAPI market, float pad) {
        if (tooltip == null || market == null) return;

        SectorEntityToken primary = market.getPrimaryEntity();
        if (primary == null) return;

        float currentAu = DistanceCheck.getMarketAU(market);
        float minAu = currentAu;
        float maxAu = currentAu;

        MemoryAPI mem = primary.getMemoryWithoutUpdate();
        if (mem != null) {
            if (mem.contains(MEM_ORBIT_MIN)) minAu = mem.getFloat(MEM_ORBIT_MIN);
            if (mem.contains(MEM_ORBIT_MAX)) maxAu = mem.getFloat(MEM_ORBIT_MAX);
        }
        if (minAu > maxAu) {
            float t = minAu; minAu = maxAu; maxAu = t;
        }

        // Section heading: blue centered banner with the market name and AU
        tooltip.addSectionHeading(
                String.format("%s is %.1f AU away from its star",
                        market.getName(), currentAu),
                Alignment.MID, pad);

        String topRow    = makeTickRow(true);
        String orbitRow  = makeOrbitRow(minAu, maxAu, currentAu);
        String bottomRow = makeTickRow(false);

        // Top + bottom rows render in a faint color; orbit row gets the band
        // colors via highlights.
        tooltip.addPara(topRow, Misc.getGrayColor(), 3f);

        // Orbit row: build highlight arrays so segments under different bands
        // get different colors.
        List<String> highlights = new ArrayList<>();
        List<Color> colors = new ArrayList<>();
        buildOrbitHighlights(orbitRow, minAu, maxAu, currentAu, highlights, colors);

        tooltip.addPara(orbitRow, 3f,
                colors.toArray(new Color[0]),
                highlights.toArray(new String[0]));

        tooltip.addPara(bottomRow, Misc.getGrayColor(), 3f);
    }

    // ------------------------------------------------------------------
    // Row builders
    // ------------------------------------------------------------------

    private static String makeTickRow(boolean top) {
        // Start with " I" centering under the sun's O
        StringBuilder sb = new StringBuilder(" I");
        int cursorPx = SPACE_PX + 2;  // space + I

        // Build event list: II ticks + heliopause slashes, sorted by AU
        // Heliopause: both slashes on the top row are '\', both on the bottom row are '/'.
        List<float[]> events = new ArrayList<>();  // {au, widthPx, kind}
        for (float au : TICK_AUS) events.add(new float[] { au, II_PX, 0 });
        int helioKind = top ? 1 : 2;  // 1='\', 2='/'
        events.add(new float[] {
                DistanceConditionManager.IRRADIATED_MIN_AU, SLASH_PX, helioKind
        });
        events.add(new float[] {
                DistanceConditionManager.IRRADIATED_MAX_AU, SLASH_PX, helioKind
        });
        events.sort((a, b) -> Float.compare(a[0], b[0]));

        for (float[] e : events) {
            float au = e[0];
            int markerPx = (int) e[1];
            int kind = (int) e[2];
            String marker = (kind == 0) ? "II" : (kind == 1) ? "\\" : "/";

            // Snap target to a 4px grid so the gap is a whole number of spaces.
            // Without this snap, gap/SPACE_PX rounds and can shift markers ±2px
            // from where their AU value actually sits.
            int rawCenterPx = SUN_PX + Math.round(au * PX_PER_AU);
            int rawStartPx = rawCenterPx - markerPx / 2;
            int startPx = snapToGrid(rawStartPx, SPACE_PX);

            int gap = startPx - cursorPx;
            int nSpaces = Math.max(0, gap / SPACE_PX);
            for (int i = 0; i < nSpaces; i++) sb.append(' ');
            sb.append(marker);
            cursorPx = cursorPx + nSpaces * SPACE_PX + markerPx;
        }
        return sb.toString();
    }

    /** Snap a pixel value to the nearest multiple of grid (rounds half to even-ish). */
    private static int snapToGrid(int px, int grid) {
        int q = px / grid;
        int r = px - q * grid;
        if (r * 2 >= grid) q++;
        return q * grid;
    }

    private static String makeOrbitRow(float perihelionAu, float aphelionAu, float currentAu) {
        StringBuilder sb = new StringBuilder("O");
        int cursorPx = SUN_PX;

        // [ ends at perihelion AU. Snap to 4px grid so leading-space count is exact.
        int rawPeriPx = SUN_PX + Math.round(perihelionAu * PX_PER_AU);
        int rawStartPx = rawPeriPx - SLASH_PX;
        int slashStartPx = snapToGrid(rawStartPx, SPACE_PX);

        int nSpaces = Math.max(0, (slashStartPx - cursorPx) / SPACE_PX);
        for (int i = 0; i < nSpaces; i++) sb.append(' ');
        sb.append('[');
        cursorPx = slashStartPx + SLASH_PX;

        // o spans [current - 2.5, current + 2.5]
        float oStartAu = currentAu - O_AU_SPAN / 2f;
        float oEndAu   = currentAu + O_AU_SPAN / 2f;

        // Clamp o inside [perihelion, aphelion] — if orbit is too narrow, o
        // gets squeezed and dash counts may go negative; max(0,...) handles it.
        int dashesBefore = Math.max(0, Math.round((oStartAu - perihelionAu) / 2.5f));
        for (int i = 0; i < dashesBefore; i++) sb.append('-');
        cursorPx += dashesBefore * DASH_PX;

        sb.append('o');
        cursorPx += O_PX;

        int dashesAfter = Math.max(0, Math.round((aphelionAu - oEndAu) / 2.5f));
        for (int i = 0; i < dashesAfter; i++) sb.append('-');
        cursorPx += dashesAfter * DASH_PX;

        sb.append(']');
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Highlight extraction: pick out substrings of the orbit row that fall in
    // each band, color them appropriately.
    //
    // Band colors:
    //   0-60     positive (green)
    //   60-100   blue (highlight)
    //   100-200  standard (gray) — except 142.5-147.5 negative (irradiated)
    //   200+     negative (red)
    //
    // ------------------------------------------------------------------

    private static void buildOrbitHighlights(String orbitRow,
                                             float perihelionAu,
                                             float aphelionAu,
                                             float currentAu,
                                             List<String> outHighlights,
                                             List<Color> outColors) {
        // Highlight the 'o' marker in the color of the current band.
        outHighlights.add("o");
        outColors.add(colorForBand(currentAu));
    }

    private static Color colorForBand(float au) {
        if (au >= DistanceConditionManager.IRRADIATED_MIN_AU
                && au < DistanceConditionManager.IRRADIATED_MAX_AU) {
            return Misc.getNegativeHighlightColor();  // irradiated: red
        }
        if (au >= DistanceConditionManager.TARTAREAN_MAX_AU) {
            return Misc.getGrayColor();               // oortal: gray (deepest, off-radar)
        }
        if (au >= DistanceConditionManager.EREBAL_MAX_AU) {
            return Misc.getNegativeHighlightColor();  // tartarean: red
        }
        if (au >= DistanceConditionManager.HADAL_MAX_AU) {
            return Misc.getHighlightColor();          // erebal: yellow
        }
        if (au >= DistanceConditionManager.ABYSSAL_MAX_AU) {
            return Misc.getBrightPlayerColor();       // hadal: blue
        }
        if (au >= DistanceConditionManager.DISTANT_MAX_AU) {
            return Misc.getPositiveHighlightColor();  // abyssal: green
        }
        return Misc.getPositiveHighlightColor();// distant: green
    }

    // ------------------------------------------------------------------
    // Seasonal ruler: a second strip for the 40 AU sublimation threshold.
    // Used by frozen/tenuous atmosphere tooltips for seasonal worlds whose
    // orbit crosses 40 AU.
    // ------------------------------------------------------------------

    /** True if the body's orbit_min/max straddles the sublimation threshold. */
    public static boolean hasSeasonalCrossing(MarketAPI market) {
        if (market == null) return false;
        SectorEntityToken primary = market.getPrimaryEntity();
        if (primary == null) return false;
        MemoryAPI mem = primary.getMemoryWithoutUpdate();
        if (mem == null) return false;
        if (!mem.contains(MEM_ORBIT_MIN) || !mem.contains(MEM_ORBIT_MAX)) return false;
        float minAu = mem.getFloat(MEM_ORBIT_MIN);
        float maxAu = mem.getFloat(MEM_ORBIT_MAX);
        if (minAu > maxAu) { float t = minAu; minAu = maxAu; maxAu = t; }
        return minAu < DistanceConditionManager.SUBLIMATION_AU
                && maxAu > DistanceConditionManager.SUBLIMATION_AU;
    }

    // Called by the frozen/tenous atmosphereconditions (if they qualify)
    public static void renderSeasonalRuler(TooltipMakerAPI tooltip, MarketAPI market, float pad) {
        if (tooltip == null || market == null) return;
        SectorEntityToken primary = market.getPrimaryEntity();
        if (primary == null) return;

        float currentAu = DistanceCheck.getMarketAU(market);
        float minAu = currentAu;
        float maxAu = currentAu;
        MemoryAPI mem = primary.getMemoryWithoutUpdate();
        if (mem != null) {
            if (mem.contains(MEM_ORBIT_MIN)) minAu = mem.getFloat(MEM_ORBIT_MIN);
            if (mem.contains(MEM_ORBIT_MAX)) maxAu = mem.getFloat(MEM_ORBIT_MAX);
        }
        if (minAu > maxAu) { float t = minAu; minAu = maxAu; maxAu = t; }

        tooltip.addSectionHeading(
                String.format("%s is %.1f AU away from its star",
                        market.getName(), currentAu),
                Alignment.MID, pad);

        String topRow    = makeSeasonalTickRow();
        String orbitRow  = makeOrbitRow(minAu, maxAu, currentAu);
        String bottomRow = makeSeasonalTickRow();

        tooltip.addPara(topRow, Misc.getGrayColor(), 3f);

        // Color the 'o' by which side of the threshold it sits on:
        //   above 40 -> frozen (gray/blue)
        //   below 40 -> tenuous (positive highlight, this is the "alive" zone)
        Color oColor = currentAu > DistanceConditionManager.SUBLIMATION_AU
                ? Misc.getBrightPlayerColor()
                : Misc.getPositiveHighlightColor();
        tooltip.addPara(orbitRow, 3f,
                new Color[] { oColor },
                new String[] { "o" });

        tooltip.addPara(bottomRow, Misc.getGrayColor(), 3f);
    }

    /** Tick row for the seasonal ruler: a single 'll' at the 40 AU threshold. */
    private static String makeSeasonalTickRow() {
        StringBuilder sb = new StringBuilder(" I");
        int cursorPx = SPACE_PX + 2;

        // ll marker is 2 chars wide, treat as II_PX width
        int rawCenterPx = SUN_PX + Math.round(
                DistanceConditionManager.SUBLIMATION_AU * PX_PER_AU);
        int rawStartPx = rawCenterPx - II_PX / 2;
        int startPx = snapToGrid(rawStartPx, SPACE_PX);

        int gap = startPx - cursorPx;
        int nSpaces = Math.max(0, gap / SPACE_PX);
        for (int i = 0; i < nSpaces; i++) sb.append(' ');
        sb.append("ll");
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Comet ruler: orbit position plus the comet's freeze/transition line.
    // The single 'II' tick marks the comet transition distance stored in
    // market memory ($sol_comet_distance). Called by the sol_comet_*
    // conditions (extreme / active / inactive).
    // ------------------------------------------------------------------

    public static void renderCometRuler(TooltipMakerAPI tooltip, MarketAPI market, float pad) {
        if (tooltip == null || market == null) return;
        SectorEntityToken primary = market.getPrimaryEntity();
        if (primary == null) return;

        MemoryAPI marketMem = market.getMemoryWithoutUpdate();
        if (marketMem == null
                || !marketMem.contains(DistanceConditionManager.MEM_COMET_DISTANCE)) return;
        float transitionAu = marketMem.getFloat(DistanceConditionManager.MEM_COMET_DISTANCE);

        float currentAu = DistanceCheck.getMarketAU(market);
        float minAu = currentAu;
        float maxAu = currentAu;
        MemoryAPI mem = primary.getMemoryWithoutUpdate();
        if (mem != null) {
            if (mem.contains(MEM_ORBIT_MIN)) minAu = mem.getFloat(MEM_ORBIT_MIN);
            if (mem.contains(MEM_ORBIT_MAX)) maxAu = mem.getFloat(MEM_ORBIT_MAX);
        }
        if (minAu > maxAu) { float t = minAu; minAu = maxAu; maxAu = t; }

        tooltip.addSectionHeading(
                String.format("%s is %.1f AU away from its star",
                        market.getName(), currentAu),
                Alignment.MID, pad);

        String topRow    = makeCometTickRow(transitionAu);
        String orbitRow  = makeOrbitRow(minAu, maxAu, currentAu);
        String bottomRow = makeCometTickRow(transitionAu);

        tooltip.addPara(topRow, Misc.getGrayColor(), 3f);

        // Color the 'o' by which side of the transition the comet sits on:
        //   inside transition  -> blue
        //   outside transition -> gray
        Color oColor = currentAu < transitionAu
                ? Misc.getBrightPlayerColor()
                : Misc.getGrayColor();
        tooltip.addPara(orbitRow, 3f,
                new Color[] { oColor },
                new String[] { "o" });

        tooltip.addPara(bottomRow, Misc.getGrayColor(), 3f);
    }

    /** Tick row for the comet ruler: a single 'II' at the comet transition AU. */
    private static String makeCometTickRow(float transitionAu) {
        StringBuilder sb = new StringBuilder(" I");
        int cursorPx = SPACE_PX + 2;

        int rawCenterPx = SUN_PX + Math.round(transitionAu * PX_PER_AU);
        int rawStartPx = rawCenterPx - II_PX / 2;
        int startPx = snapToGrid(rawStartPx, SPACE_PX);

        int gap = startPx - cursorPx;
        int nSpaces = Math.max(0, gap / SPACE_PX);
        for (int i = 0; i < nSpaces; i++) sb.append(' ');
        sb.append("II");
        return sb.toString();
    }
    
    public static void renderCircumstellarRuler(TooltipMakerAPI tooltip, MarketAPI market, float pad) {
        if (tooltip == null || market == null) return;

        SectorEntityToken primary = market.getPrimaryEntity();
        if (primary == null) return;

        float currentAu = DistanceCheck.getMarketAU(market);
        float minAu = currentAu;
        float maxAu = currentAu;

        MemoryAPI mem = primary.getMemoryWithoutUpdate();
        if (mem != null) {
            if (mem.contains(MEM_ORBIT_MIN)) minAu = mem.getFloat(MEM_ORBIT_MIN);
            if (mem.contains(MEM_ORBIT_MAX)) maxAu = mem.getFloat(MEM_ORBIT_MAX);
        }
        if (minAu > maxAu) {
            float t = minAu; minAu = maxAu; maxAu = t;
        }

        tooltip.addSectionHeading(
                String.format("%s is %.1f AU away from its star",
                        market.getName(), currentAu),
                Alignment.MID, pad);

        String topRow    = makeCircumstellarTickRow(true);
        String orbitRow  = makeOrbitRow(minAu, maxAu, currentAu);
        String bottomRow = makeCircumstellarTickRow(false);

        tooltip.addPara(topRow, Misc.getGrayColor(), 3f);

        List<String> highlights = new ArrayList<>();
        List<Color> colors = new ArrayList<>();
        buildOrbitHighlights(orbitRow, minAu, maxAu, currentAu, highlights, colors);

        tooltip.addPara(orbitRow, 3f,
                colors.toArray(new Color[0]),
                highlights.toArray(new String[0]));

        tooltip.addPara(bottomRow, Misc.getGrayColor(), 3f);
    }

    /** Tick row for the circumstellar ruler: only the two heliopause slashes, no II band ticks. */
    private static String makeCircumstellarTickRow(boolean top) {
        StringBuilder sb = new StringBuilder(" I");
        int cursorPx = SPACE_PX + 2;  // space + I

        List<float[]> events = new ArrayList<>();  // {au, widthPx, kind}
        int helioKind = top ? 1 : 2;  // 1='\', 2='/'
        events.add(new float[] {
                DistanceConditionManager.IRRADIATED_MIN_AU, SLASH_PX, helioKind
        });
        events.add(new float[] {
                DistanceConditionManager.IRRADIATED_MAX_AU, SLASH_PX, helioKind
        });
        events.sort((a, b) -> Float.compare(a[0], b[0]));

        for (float[] e : events) {
            float au = e[0];
            int markerPx = (int) e[1];
            int kind = (int) e[2];
            String marker = (kind == 1) ? "\\" : "/";

            int rawCenterPx = SUN_PX + Math.round(au * PX_PER_AU);
            int rawStartPx = rawCenterPx - markerPx / 2;
            int startPx = snapToGrid(rawStartPx, SPACE_PX);

            int gap = startPx - cursorPx;
            int nSpaces = Math.max(0, gap / SPACE_PX);
            for (int i = 0; i < nSpaces; i++) sb.append(' ');
            sb.append(marker);
            cursorPx = cursorPx + nSpaces * SPACE_PX + markerPx;
        }
        return sb.toString();
    }
}