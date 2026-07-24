package soljars.gen.terrain;

import java.awt.Color;

import com.fs.starfarer.api.campaign.SectorEntityToken;

/**
 * CR3BP tadpole libration cloud. Traces the zero-velocity contour around L4/L5,
 * collapses it to a radial profile against heliocentric angle, and dilates it
 * radially by the swarm's median eccentricity. Frame transform, containment,
 * rendering and effects are in {@link LagrangeBeanBase}.
 */
public class LagrangeBean extends LagrangeBeanBase {

    public static class LagrangeBeanParams {
        public SectorEntityToken primary;  // body A (frame origin, i.e. the star)
        public float massA, massB;         // solar masses
        public float distanceAU;           // A-B separation in AU (raw, pre-log-scale)
        public boolean leading;            // true = L4 (60 deg ahead), false = L5
        public float extent;               // 0..1; 1 = tadpole stretched toward L3
        public float eMedian;              // median swarm eccentricity; radial spread = a * e
        public float profileMult;          // detected-at range multiplier (moving)
        public String name;
        public String texture;             // sprite id within the "sol_rings" group
        public Color color = new Color(255, 255, 255, 255);  // band tint; texture supplies its own detail/alpha

        public LagrangeBeanParams(SectorEntityToken primary, float massA, float massB,
                             float distanceAU, boolean leading, float extent,
                             float eMedian, float profileMult, String name, String texture) {
            this.primary = primary; this.massA = massA; this.massB = massB;
            this.distanceAU = distanceAU; this.leading = leading; this.extent = extent;
            this.eMedian = eMedian; this.profileMult = profileMult; this.name = name;
            this.texture = texture;
        }
    }

    protected static final int SEGMENTS = 96;         // rays traced around L4 for the contour

    protected LagrangeBeanParams params;

    @Override
    public void init(String terrainId, SectorEntityToken entity, Object param) {
        super.init(terrainId, entity, param);
        this.params = (LagrangeBeanParams) param;
    }

    // =====================================================================
    // Subclass contract
    // =====================================================================

    @Override protected SectorEntityToken primary() { return params == null ? null : params.primary; }
    @Override protected float distanceAU()          { return params.distanceAU; }
    @Override protected boolean leading()           { return params.leading; }
    @Override protected String name()               { return params.name; }
    @Override protected Color bandColor()           { return params.color; }
    @Override protected String bandTextureId()      { return params.texture; }
    @Override protected String mapTextureId()       { return "map_asteroid_belt"; }

    @Override protected float sensorBase()          { return params.profileMult; }
    @Override protected float sensorSlow()          { return STATIONARY_MULT; }
    @Override protected boolean hasImpacts()        { return true; }

    // =====================================================================
    // CR3BP effective potential, barycentric frame, unit separation
    // =====================================================================

    protected static double omega(double x, double y, double mu) {
        double r1 = Math.sqrt((x + mu) * (x + mu) + y * y);
        double r2 = Math.sqrt((x - 1 + mu) * (x - 1 + mu) + y * y);
        return 0.5 * (x * x + y * y) + (1 - mu) / r1 + mu / r2;
    }

    // dOmega/dx on the x-axis; root in (-2, -mu) is L3
    protected static double dOmegaDx(double x, double mu) {
        double a = x + mu, b = x - 1 + mu;
        return x - (1 - mu) * a / Math.abs(a * a * a) - mu * b / Math.abs(b * b * b);
    }

    protected static double solveL3(double mu) {
        double lo = -2.0, hi = -mu - 1e-4;
        for (int i = 0; i < 80; i++) {
            double mid = 0.5 * (lo + hi);
            if (dOmegaDx(lo, mu) * dOmegaDx(mid, mu) <= 0) hi = mid; else lo = mid;
        }
        return 0.5 * (lo + hi);
    }

    /**
     * Longitudinal width envelope along the arc.
     * u = 0 at the planet-side end, 1 at the L3-ward tail.
     * Asymmetric: rises quickly to a peak near the planet end (u = peak), then
     * falls monotonically all the way to a sharp tail at u = 1. Not mirrored about
     * the middle - the planet end is fat, the L3 end tapers to a point.
     */
    protected static float taper(float u, float peak) {
        if (u <= peak) {
            // rounded rise from the planet-side end up to the peak at L4
            float t = u / peak;
            return (float) Math.pow(t, 0.4);
        } else {
            // taper from the L4 peak down to a sharp point at the L3-ward tail
            float t = (u - peak) / (1f - peak);
            return (float) Math.pow(1f - t, 1.5);
        }
    }

    /**
     * Libration envelope from the CR3BP contour, dilated radially by the swarm's
     * eccentricity. The contour alone tapers to nothing at the tail; the +-a*e
     * excursion supplies the bulk of the width, tapered along the arc so the
     * cloud rounds off at the planet-side end, bulges mid-lobe, and thins toward
     * L3. Built in frame units.
     */
    @Override
    protected void buildPolygon() {
        double mu = params.massB / (params.massA + params.massB);
        double sign = params.leading ? 1.0 : -1.0;

        double l4x = 0.5 - mu, l4y = sign * Math.sqrt(3.0) / 2.0;
        double oL4 = omega(l4x, l4y, mu);
        double oL3 = omega(solveL3(mu), 0, mu);

        // C rises from Omega(L4) toward Omega(L3); L4 is a MINIMUM of Omega
        double ext = Math.max(0.01f, Math.min(0.999f, params.extent));
        double C = oL4 + ext * (oL3 - oL4);

        // --- 1. trace the zero-velocity contour around L4/L5 ---
        float[] cx = new float[SEGMENTS], cy = new float[SEGMENTS];
        for (int i = 0; i < SEGMENTS; i++) {
            double th = 2.0 * Math.PI * i / SEGMENTS;
            double dx = Math.cos(th), dy = Math.sin(th);

            // march out from L4 until Omega rises above C, then bisect
            double lo = 0, hi = 0, step = 0.005;
            for (double r = step; r <= 2.0; r += step) {
                if (omega(l4x + dx * r, l4y + dy * r, mu) > C) { hi = r; lo = r - step; break; }
                hi = r;
            }
            for (int k = 0; k < 40 && hi > lo; k++) {
                double mid = 0.5 * (lo + hi);
                if (omega(l4x + dx * mid, l4y + dy * mid, mu) <= C) lo = mid; else hi = mid;
            }
            double r = 0.5 * (lo + hi);

            // relative to A (shift barycentric x by +mu)
            cx[i] = (float) (l4x + dx * r + mu);
            cy[i] = (float) (l4y + dy * r);
        }

        // contour angles, hoisted: the ray-crossing search below reads each of
        // these PROFILE times over, and atan2 is not cheap
        double[] cAng = new double[SEGMENTS];
        double thMin = Double.MAX_VALUE, thMax = -Double.MAX_VALUE;
        for (int i = 0; i < SEGMENTS; i++) {
            cAng[i] = Math.atan2(cy[i], cx[i]);
            if (cAng[i] < thMin) thMin = cAng[i];
            if (cAng[i] > thMax) thMax = cAng[i];
        }

        // --- 2. collapse to a radial profile vs heliocentric angle ---
        // the extreme angles are tangent to the contour - no edge strictly straddles
        // them, so pull the sample range in slightly
        double eps = (thMax - thMin) * 0.001;
        thMin += eps;
        thMax -= eps;

        float[] profTh = new float[PROFILE];
        float[] profMid = new float[PROFILE];
        float[] profHW = new float[PROFILE];

        for (int i = 0; i < PROFILE; i++) {
            double th = thMin + (thMax - thMin) * i / (double) (PROFILE - 1);
            profTh[i] = (float) th;

            // where the contour's edges cross this ray
            double rLo = Double.MAX_VALUE, rHi = 0;
            for (int j = 0, k = SEGMENTS - 1; j < SEGMENTS; k = j++) {
                double tj = cAng[j], tk = cAng[k];
                if ((tj > th) == (tk > th)) continue;   // edge doesn't span this angle
                double f = (th - tk) / (tj - tk);
                double ex = cx[k] + f * (cx[j] - cx[k]);
                double ey = cy[k] + f * (cy[j] - cy[k]);
                double r = Math.sqrt(ex * ex + ey * ey);
                if (r < rLo) rLo = r;
                if (r > rHi) rHi = r;
            }
            if (rHi == 0) { profMid[i] = Float.NaN; profHW[i] = Float.NaN; continue; }

            // decompose into centerline + half-width so the taper shapes the total
            // width around a stable centerline (no double-pinch from the contour
            // narrowing and the taper narrowing at the same time)
            profMid[i] = (float) (0.5 * (rLo + rHi));
            profHW[i]  = (float) (0.5 * (rHi - rLo));
        }

        // backfill degenerate samples (arc ends) from the nearest valid neighbor
        for (int i = 0; i < PROFILE; i++) {
            if (!Float.isNaN(profMid[i])) continue;
            int lo = i, hi = i;
            while (lo > 0 && Float.isNaN(profMid[lo])) lo--;
            while (hi < PROFILE - 1 && Float.isNaN(profMid[hi])) hi++;
            int src = Float.isNaN(profMid[lo]) ? hi : lo;
            profMid[i] = profMid[src];
            profHW[i] = profHW[src];
        }

        // --- 3. build the two edges directly from the contour, each offset outward
        // along ITS OWN normal by the tapered eccentricity. The raw contour edges
        // (rLo/rHi) are smooth isolines; offsetting each along its own normal keeps
        // them smooth. The earlier centerline+shared-normal reconstruction went
        // concave at the planet end, where the centerline dives inward steeply while
        // the width grows - that is what manufactured the inner concavity.
        float[] inX = new float[PROFILE], inY = new float[PROFILE];
        float[] outX = new float[PROFILE], outY = new float[PROFILE];
        for (int i = 0; i < PROFILE; i++) {
            float cth = (float) Math.cos(profTh[i]), sth = (float) Math.sin(profTh[i]);
            // contour edge points (radial, from rLo/rHi captured as mid -/+ hw)
            float rIn  = profMid[i] - profHW[i];
            float rOut = profMid[i] + profHW[i];
            inX[i]  = rIn  * cth;  inY[i]  = rIn  * sth;
            outX[i] = rOut * cth;  outY[i] = rOut * sth;
        }

        // width peaks at the L4 point (heliocentric +-60 deg), not near the planet
        // end. Locate L4 in the taper's planet->L3 parameterization; extent stretches
        // the tail toward L3, so this fraction is not fixed.
        double thL4 = sign * Math.PI / 3.0;
        float uPeak = (float) ((thL4 - thMin) / (thMax - thMin));
        if (!params.leading) uPeak = 1f - uPeak;         // match the planet-side=0 flip below
        uPeak = Math.max(0.05f, Math.min(0.95f, uPeak));

        float[] eOff = new float[PROFILE];
        for (int i = 0; i < PROFILE; i++) {
            float u = i / (float) (PROFILE - 1);
            if (!params.leading) u = 1f - u;             // planet-side end is thMax for L5
            eOff[i] = params.eMedian * taper(u, uPeak);
        }

        pushEdge(outX, outY, eOff, +1f);                 // outer edge pushed outward
        pushEdge(inX, inY, eOff, -1f);                   // inner edge pushed inward

        // snap each tip to a single shared point (midpoint of the two edge ends), so
        // the boundary closes to an exact point - otherwise the tiny residual gap
        // between out[tip] and in[tip] lets the outline ribbon twist/cross there
        int tip = PROFILE - 1;
        float pmx = 0.5f * (outX[0] + inX[0]), pmy = 0.5f * (outY[0] + inY[0]);
        outX[0] = pmx; outY[0] = pmy; inX[0] = pmx; inY[0] = pmy;
        float tmx = 0.5f * (outX[tip] + inX[tip]), tmy = 0.5f * (outY[tip] + inY[tip]);
        outX[tip] = tmx; outY[tip] = tmy; inX[tip] = tmx; inY[tip] = tmy;

        // --- 4. assemble the closed polygon: outer edge out, inner edge back ---
        polyX = new float[polyN()];
        polyY = new float[polyN()];
        int n = 0;

        for (int i = 0; i < PROFILE; i++) {              // outer edge, planet -> tail
            polyX[n] = outX[i]; polyY[n] = outY[i]; n++;
        }
        for (int i = PROFILE - 1; i >= 0; i--) {         // inner edge, tail -> planet
            polyX[n] = inX[i]; polyY[n] = inY[i]; n++;
        }

        // keep the paired edges for a triangle-strip fill (a fan from vertex 0 would
        // splay triangles across this crescent's concave interior - that is the X)
        edgeInX = inX; edgeInY = inY;
        edgeOutX = outX; edgeOutY = outY;
    }

    /**
     * Push an edge polyline outward along its own per-vertex normal by off[i].
     * dir = +1 pushes toward increasing radius (outer edge), -1 toward the star
     * (inner edge). Normal is from the edge's local tangent, so each edge stays
     * smooth regardless of what the opposite edge or the centerline is doing.
     */
    protected static void pushEdge(float[] x, float[] y, float[] off, float dir) {
        int m = x.length;
        float[] ox = new float[m], oy = new float[m];
        for (int i = 0; i < m; i++) {
            int a = Math.max(0, i - 1), b = Math.min(m - 1, i + 1);
            float tx = x[b] - x[a], ty = y[b] - y[a];
            float len = (float) Math.hypot(tx, ty);
            if (len < 1e-6f) { ox[i] = x[i]; oy[i] = y[i]; continue; }
            // outward normal: tangent rotated -90, sign fixed to point away from origin
            float nx = ty / len, ny = -tx / len;
            if (nx * x[i] + ny * y[i] < 0) { nx = -nx; ny = -ny; }
            ox[i] = x[i] + dir * off[i] * nx;
            oy[i] = y[i] + dir * off[i] * ny;
        }
        System.arraycopy(ox, 0, x, 0, m);
        System.arraycopy(oy, 0, y, 0, m);
    }
}