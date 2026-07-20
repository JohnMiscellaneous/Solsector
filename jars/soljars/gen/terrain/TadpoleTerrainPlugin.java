package soljars.gen.terrain;

import java.awt.Color;
import java.util.EnumSet;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TerrainAIFlags;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidImpact;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import soljars.gen.utils.AstroCalc;

public class TadpoleTerrainPlugin extends BaseTerrain {

    public static class TadpoleParams {
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

        public TadpoleParams(SectorEntityToken primary, float massA, float massB,
                             float distanceAU, boolean leading, float extent,
                             float eMedian, float profileMult, String name, String texture) {
            this.primary = primary; this.massA = massA; this.massB = massB;
            this.distanceAU = distanceAU; this.leading = leading; this.extent = extent;
            this.eMedian = eMedian; this.profileMult = profileMult; this.name = name;
            this.texture = texture;
        }
    }

    protected static final int SEGMENTS = 96;         // rays traced around L4 for the contour
    protected static final int PROFILE = 64;          // angular samples along the arc

    protected static final float MAP_EDGE_WIDTH = 300f;  // edge band thickness, game units (matches vanilla belt)
    protected static final int TIP_BLEND = 3;            // samples over which the line closes to the point tips
    protected static final float STATIONARY_MULT = 0.25f;

    protected static final float TILE_SCALE = 1f;        // arc repeats; >1 = more/shorter tiles
    protected static final float END_FADE = 0.15f;       // fraction of the arc feathered at each tip

    protected TadpoleParams params;

    // frame polar, normalized to the A-B separation, origin at A
    // profMid/profHW are the band centerline radius and half-width per angular sample
    protected transient float[] profTh, profMid, profHW;
    // the two edges in frame xy, paired by index, for a non-fanning triangle-strip fill
    protected transient float[] edgeInX, edgeInY, edgeOutX, edgeOutY;
    // closed polygon: outer edge, tail cap, inner edge (reversed), planet-end cap
    protected transient float[] polyX, polyY;

    protected transient AstroCalc calc;
    protected transient SpriteAPI mapTex;
    protected transient SpriteAPI bandTex;

    @Override
    public void init(String terrainId, SectorEntityToken entity, Object param) {
        super.init(terrainId, entity, param);
        this.params = (TadpoleParams) param;
    }

    protected TadpoleParams p() { return params; }

    protected AstroCalc calc() {
        if (calc == null) calc = new AstroCalc();
        return calc;
    }

    protected int polyN() { return PROFILE * 2; }

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
     * Asymmetric: rises quickly to a peak near the planet end (u = PEAK_U), then
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
     * L3. Cached in frame units.
     */
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

        // --- 2. collapse to a radial profile vs heliocentric angle ---
        double thMin = Double.MAX_VALUE, thMax = -Double.MAX_VALUE;
        for (int i = 0; i < SEGMENTS; i++) {
            double th = Math.atan2(cy[i], cx[i]);
            if (th < thMin) thMin = th;
            if (th > thMax) thMax = th;
        }

        // the extreme angles are tangent to the contour - no edge strictly straddles
        // them, so pull the sample range in slightly
        double eps = (thMax - thMin) * 0.001;
        thMin += eps;
        thMax -= eps;

        profTh = new float[PROFILE];
        profMid = new float[PROFILE];
        profHW = new float[PROFILE];

        for (int i = 0; i < PROFILE; i++) {
            double th = thMin + (thMax - thMin) * i / (double) (PROFILE - 1);
            profTh[i] = (float) th;

            // where the contour's edges cross this ray
            double rLo = Double.MAX_VALUE, rHi = 0;
            for (int j = 0, k = SEGMENTS - 1; j < SEGMENTS; k = j++) {
                double tj = Math.atan2(cy[j], cx[j]);
                double tk = Math.atan2(cy[k], cx[k]);
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

    /** Rotation of the A->B axis, in degrees, derived from where this terrain sits. */
    protected float getFrameAngle() {
        // Take the orbit angle the engine advances in double each frame, not
        // Misc.getAngleInDegrees on the entity's float position: at the orbital
        // radius that position quantizes to a few units, and atan2 of it wobbles
        // the whole band tangentially (lateral jitter). Fall back to position only
        // if the terrain somehow isn't on a circular orbit.
        float angSelf = entity.getOrbit() != null
                ? entity.getCircularOrbitAngle()
                : Misc.getAngleInDegrees(params.primary.getLocation(), entity.getLocation());
        return angSelf - (params.leading ? 60f : -60f);
    }

    // =====================================================================
    // Frame <-> world. The polygon is real-space, normalized to the A-B
    // separation; game radii are log-scaled, so every vertex goes through
    // AstroCalc rather than a flat multiply.
    // =====================================================================

    protected Vector2f frameToWorld(float fx, float fy) {
        Vector2f a = params.primary.getLocation();
        double angB = Math.toRadians(getFrameAngle());

        // rotate frame -> heliocentric, still in units of the A-B separation
        double hx = fx * Math.cos(angB) - fy * Math.sin(angB);
        double hy = fx * Math.sin(angB) + fy * Math.cos(angB);

        double auR = Math.sqrt(hx * hx + hy * hy) * params.distanceAU;  // real AU from A
        double th = Math.atan2(hy, hx);                                 // heliocentric angle

        float gameR = calc().getDist((float) auR, params.primary);      // AU -> game units
        return new Vector2f(a.x + gameR * (float) Math.cos(th),
                            a.y + gameR * (float) Math.sin(th));
    }

    // =====================================================================
    // Containment
    // =====================================================================

    @Override
    public boolean containsPoint(Vector2f point, float radius) {
        if (polyX == null) buildPolygon();

        Vector2f a = params.primary.getLocation();
        float dx = point.x - a.x, dy = point.y - a.y;

        float gameR = (float) Math.sqrt(dx * dx + dy * dy);
        float auR = calc().getAU(gameR);                                // game units -> real AU
        double th = Math.atan2(dy, dx) - Math.toRadians(getFrameAngle());

        // back to frame units (normalized to the A-B separation)
        float rf = auR / params.distanceAU;
        float fx = rf * (float) Math.cos(th);
        float fy = rf * (float) Math.sin(th);

        int n = polyN();
        boolean in = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            if ((polyY[i] > fy) != (polyY[j] > fy) &&
                fx < (polyX[j] - polyX[i]) * (fy - polyY[i]) / (polyY[j] - polyY[i]) + polyX[i]) {
                in = !in;
            }
        }
        return in;
    }

    // =====================================================================
    // Campaign layer rendering
    // =====================================================================

    @Override
    public EnumSet<CampaignEngineLayers> getActiveLayers() {
        return EnumSet.of(CampaignEngineLayers.TERRAIN_1);
    }

    @Override
    public float getRenderRange() {
        // lobe reaches roughly half a separation past L4
        return calc().getDist(params.distanceAU * 1.6f, params.primary);
    }

    /**
     * Textured band across the crescent. The triangle strip pairs the inner and
     * outer edges by index so it follows the arc without a fan; the sol_rings
     * texture spans the band thickness once (S axis) and tiles along the arc
     * (T axis), so it reads as a vertical ring pattern repeating down the length.
     * Per-vertex alpha feathers the last {@code END_FADE} of the arc at each point
     * tip so the texture fades out instead of ending on a hard pinch.
     */
    protected void renderBand(float alphaMult) {
        if (polyX == null) buildPolygon();
        if (bandTex == null) bandTex = Global.getSettings().getSprite("sol_rings", params.texture);

        // resolve both edges to world space once, accumulating arc length along the
        // centerline so tiling is uniform regardless of PROFILE; track the widest
        // cross-section for an aspect-preserving tile size
        float[] ox = new float[PROFILE], oy = new float[PROFILE];
        float[] ix = new float[PROFILE], iy = new float[PROFILE];
        float[] arc = new float[PROFILE];
        float acc = 0f, maxThick = 0f;
        for (int i = 0; i < PROFILE; i++) {
            Vector2f wo = frameToWorld(edgeOutX[i], edgeOutY[i]);
            Vector2f wi = frameToWorld(edgeInX[i], edgeInY[i]);
            ox[i] = wo.x; oy[i] = wo.y; ix[i] = wi.x; iy[i] = wi.y;
            if (i > 0) {
                float mx0 = (ox[i - 1] + ix[i - 1]) * 0.5f, my0 = (oy[i - 1] + iy[i - 1]) * 0.5f;
                float mx1 = (ox[i] + ix[i]) * 0.5f, my1 = (oy[i] + iy[i]) * 0.5f;
                acc += (float) Math.hypot(mx1 - mx0, my1 - my0);
            }
            arc[i] = acc;
            float th = (float) Math.hypot(ox[i] - ix[i], oy[i] - iy[i]);
            if (th > maxThick) maxThick = th;
        }

        // one repeat spans (belly thickness) * (texH/texW), so the texture is
        // undistorted at the widest part and only compresses toward the thinning
        // tips where it is already fading
        float aspect = bandTex.getHeight() / Math.max(1f, bandTex.getWidth());
        float tileLen = Math.max(1f, maxThick * aspect * TILE_SCALE);
        float[] v = new float[PROFILE];
        for (int i = 0; i < PROFILE; i++) v[i] = arc[i] / tileLen;   // repeats along the arc

        Color c = params.color;
        float baseA = (c.getAlpha() / 255f) * alphaMult;
        float rf = c.getRed() / 255f, gf = c.getGreen() / 255f, bf = c.getBlue() / 255f;

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        bandTex.bindTexture();
        // tile down the arc (T), clamp across the thickness (S)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        for (int i = 0; i < PROFILE; i++) {
            float t = i / (float) (PROFILE - 1);
            float edge = Math.min(t, 1f - t);                      // 0 at a tip, 0.5 at center
            float fade = clamp01(edge / END_FADE);
            fade = fade * fade * (3f - 2f * fade);                 // smoothstep
            GL11.glColor4f(rf, gf, bf, baseA * fade);

            GL11.glTexCoord2f(1f, v[i]);                           // outer edge, S=1
            GL11.glVertex2f(ox[i], oy[i]);
            GL11.glTexCoord2f(0f, v[i]);                           // inner edge, S=0
            GL11.glVertex2f(ix[i], iy[i]);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    protected static float clamp01(float x) {
        return x < 0f ? 0f : (x > 1f ? 1f : x);
    }

    @Override
    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        renderBand(1f);
    }

    // =====================================================================
    // Map rendering. Per RingRenderer, map space is world coords * factor.
    // Two constant-width textured ribbons trace the outer and inner edges;
    // the V-advance is normalized to on-screen segment length / band
    // thickness so texel density holds through zoom.
    // =====================================================================

        @Override
    public void renderOnMap(float factor, float alphaMult) {
        if (params == null) return;
        if (polyX == null) buildPolygon();
        if (mapTex == null) mapTex = Global.getSettings().getSprite("systemMap", "map_asteroid_belt");

        Color c = Global.getSettings().getColor("asteroidBeltMapColor");

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        mapTex.bindTexture();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4ub((byte) c.getRed(), (byte) c.getGreen(), (byte) c.getBlue(),
                        (byte) (int) (c.getAlpha() * alphaMult));

        float hw = MAP_EDGE_WIDTH * 0.5f * factor;       // outline half-width, scales with zoom
        float texHeight = mapTex.getTextureHeight();
        float imageHeight = mapTex.getHeight();
        float bandWidthInTexture = mapTex.getWidth();    // across-band axis (U spans this)

        // polyX/polyY is the closed shape boundary (outer edge, tail cap, inner edge,
        // planet cap) in frame units. Draw a constant-width textured ribbon centered
        // on it, closed into a loop, so the outline traces the whole border and the
        // rounded ends are included.
        int m = polyN();
        float[] bx = new float[m], by = new float[m];    // boundary in world space
        for (int i = 0; i < m; i++) {
            Vector2f wpt = frameToWorld(polyX[i], polyY[i]);
            bx[i] = wpt.x * factor;
            by[i] = wpt.y * factor;
        }

        GL11.glBegin(GL11.GL_QUAD_STRIP);
        float texProgress = 0f;
        for (int i = 0; i <= m; i++) {                   // <= m to close the loop
            int c0 = i % m;
            int prev = (c0 - 1 + m) % m, next = (c0 + 1) % m;

            // path tangent from neighbors, normal = tangent rotated 90
            float tx = bx[next] - bx[prev], ty = by[next] - by[prev];
            float len = (float) Math.hypot(tx, ty);
            if (len < 1e-6f) len = 1f;
            float ox = -ty / len, oy = tx / len;         // outward-ish normal (across ribbon)

            GL11.glTexCoord2f(0f, texProgress);
            GL11.glVertex2f(bx[c0] - ox * hw, by[c0] - oy * hw);
            GL11.glTexCoord2f(1f, texProgress);
            GL11.glVertex2f(bx[c0] + ox * hw, by[c0] + oy * hw);

            int nn = (i + 1) % m;
            float segLen = (float) Math.hypot(bx[nn] - bx[c0], by[nn] - by[c0]);
            texProgress += segLen * texHeight / imageHeight * bandWidthInTexture / (2f * hw);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    // =====================================================================
    // Effect + tooltip
    // =====================================================================

    @Override
    public String getTerrainName() {
        return params.name != null ? params.name : "Trojan Cloud";
    }

    @Override
    public void applyEffect(SectorEntityToken e, float days) {
        if (!(e instanceof CampaignFleetAPI)) return;
        CampaignFleetAPI fleet = (CampaignFleetAPI) e;

        float mult = Misc.isSlowMoving(fleet) ? STATIONARY_MULT : params.profileMult;

        fleet.getStats().addTemporaryModMult(
            0.1f,                          // duration, days - refreshed every frame while inside
            getModId(),                    // source key
            getTerrainName(),              // shown in the sensor tooltip breakdown
            mult,
            fleet.getStats().getDetectedRangeMod());

        // asteroid impacts, paced exactly like a vanilla belt: each frame with no
        // impact banks a little "skipped" time that raises the next hit chance, so
        // strikes arrive at a steady cadence instead of clumping. AsteroidImpact
        // itself no-ops for slow/stopped fleets, matching the tooltip.
        if (!fleet.isInHyperspaceTransition()) {
            String key = "$asteroidImpactTimeout";
            String sKey = "$skippedImpacts";
            String recentKey = "$recentImpact";
            float probPerSkip = 0.15f;
            float maxProb = 1f;
            float maxSkipsToTrack = 7f;
            float durPerSkip = 0.2f;

            MemoryAPI mem = fleet.getMemoryWithoutUpdate();
            if (!mem.contains(key)) {
                float expire = mem.getExpire(sKey);
                if (expire < 0) expire = 0;

                float hitProb = Math.min(maxProb, expire / durPerSkip * probPerSkip);
                if ((float) Math.random() < hitProb) {
                    boolean hadRecent = mem.is(recentKey, true) && (float) Math.random() > 0.5f;
                    fleet.addScript(new AsteroidImpact(fleet, hadRecent));
                    mem.set(sKey, true, 0);
                    mem.set(recentKey, true, 0.5f + (float) Math.random());
                } else {
                    mem.set(sKey, true, Math.min(expire + durPerSkip, maxSkipsToTrack * durPerSkip));
                }
                mem.set(key, true, 0.05f + 0.1f * (float) Math.random());
            }
        }
    }

    @Override
    public boolean hasAIFlag(Object flag) {
        // impact-associated danger flags only; the tadpole has no burn penalty, so
        // no REDUCES_SPEED_LARGE
        return flag == TerrainAIFlags.DANGEROUS_UNLESS_GO_SLOW ||
               flag == TerrainAIFlags.NOT_SUPER_DANGEROUS_UNLESS_GO_SLOW;
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    @Override
    public void createTooltip(TooltipMakerAPI t, boolean expanded) {
        String stop = Global.getSettings().getControlStringForEnumName("GO_SLOW");

        t.addTitle(getTerrainName());
        t.addPara("Reduces the range at which fleets inside it can be detected by %s.", 10f,
            Misc.getHighlightColor(),
            (int) Math.round((1f - params.profileMult) * 100f) + "%");
        t.addPara("Stationary or slow-moving* fleets are instead detected at %s reduced range.", 10f,
            Misc.getHighlightColor(),
            (int) Math.round((1f - STATIONARY_MULT) * 100f) + "%");
        t.addPara("Chance of asteroid impacts that briefly knock the fleet off course and " +
            "may occasionally strike ships directly, dealing moderate damage. Smaller and " +
            "slow-moving* fleets are able to avoid them.", 10f,
            Misc.getHighlightColor(), "slow-moving");
        t.addPara("*Press and hold %s to stop; combine with holding the left mouse button down to move slowly.", 10f,
            Misc.getGrayColor(), Misc.getHighlightColor(),
            stop);
    }

    @Override
    public String getEffectCategory() {
        return "sol_libration";
    }
}