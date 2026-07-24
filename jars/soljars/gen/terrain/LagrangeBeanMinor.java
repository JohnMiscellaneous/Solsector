package soljars.gen.terrain;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.SectorEntityToken;

/**
 * Symmetric curved-oval libration cloud for small trojan fields (Mars/Eureka,
 * Earth, Venus) where the CR3BP tadpole contour degenerates into a thin, sharply
 * pointed comma that log-scales badly. Instead of tracing a zero-velocity contour,
 * this is a plain lens: an ellipse in (arc-length x radius) space centered on
 * L4/L5, wrapped onto the orbital arc so it curves with the orbit. Symmetric both
 * along the arc and radially about the Lagrange point.
 *
 * Because the shape is analytic, containment is closed-form and overrides the
 * base class's point-in-polygon walk. Everything else is in {@link LagrangeBeanBase}.
 */
public class LagrangeBeanMinor extends LagrangeBeanBase {

    public static class LagrangeBeanMinorParams {
        public SectorEntityToken primary;  // body A (frame origin, i.e. the star)
        public float distanceAU;           // A-B separation in AU (raw, pre-log-scale)
        public boolean leading;            // true = L4 (60 deg ahead), false = L5
        public float extent;               // 0..1; along-arc half-width as a fraction of MAX_ARC_HALF_DEG
        public float eMedian;              // median swarm eccentricity; radial half-width = a * e
        public float profileMult;          // detected-at range multiplier (moving); used only when isDense
        public boolean isDense;            // dense fields pelt fleets with asteroid impacts; sparse ones don't
        public String name;
        public String texture;             // sprite id within the "sol_rings" group
        public Color color = new Color(255, 255, 255, 255);  // band tint; texture supplies its own detail/alpha

        public LagrangeBeanMinorParams(SectorEntityToken primary, float distanceAU, boolean leading,
                                  float extent, float eMedian, float profileMult, boolean isDense,
                                  String name, String texture) {
            this.primary = primary; this.distanceAU = distanceAU; this.leading = leading;
            this.extent = extent; this.eMedian = eMedian; this.profileMult = profileMult;
            this.isDense = isDense; this.name = name; this.texture = texture;
        }
    }

    protected static final float MAX_ARC_HALF_DEG = 45f;   // extent=1 -> +-45 deg of arc half-width
    protected static final float MIN_RAD_HALF = 0.02f;     // radial half-width floor, frame units

    protected static final float SPARSE_BASE_MULT = 0.75f; // sparse-field moving detect mult
    protected static final float SPARSE_SLOW_MULT = 0.5f;  // sparse-field slow-moving detect mult

    protected LagrangeBeanMinorParams params;

    // lens parameters in frame space, for the closed-form containment test
    protected transient float phi0, arcHalf, radHalf;

    @Override
    public void init(String terrainId, SectorEntityToken entity, Object param) {
        super.init(terrainId, entity, param);
        this.params = (LagrangeBeanMinorParams) param;
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
    @Override protected String mapTextureId()       { return params.isDense ? "map_asteroid_belt" : "map_asteroid_belt_thin"; }

    // a dense field hides fleets more (and pelts them), a sparse one is thinner cover
    @Override protected float sensorBase()          { return params.isDense ? params.profileMult : SPARSE_BASE_MULT; }
    @Override protected float sensorSlow()          { return params.isDense ? STATIONARY_MULT    : SPARSE_SLOW_MULT; }
    @Override protected boolean hasImpacts()        { return params.isDense; }

    // =====================================================================
    // Geometry: an ellipse in (arc-length, radius), wrapped onto the arc
    // =====================================================================

    /**
     * Build the two edges as a lens centered on L4/L5. Sweeping f from -1..1 walks
     * the arc from one tip to the other; at each step the radial half-height traces
     * a semicircle (ellipse profile), so inner/outer edges are symmetric about the
     * Lagrange radius and meet at a point at each tip. Wrapping the arc offset into
     * the heliocentric angle is what gives the oval its curve.
     */
    @Override
    protected void buildPolygon() {
        phi0 = (float) Math.toRadians(params.leading ? 60f : -60f);  // L4/L5 heliocentric angle
        float r0 = 1f;                                               // Lagrange radius (normalized)

        float ext = Math.max(0f, Math.min(1f, params.extent));
        arcHalf = (float) Math.toRadians(MAX_ARC_HALF_DEG) * ext;    // radians of arc, each side
        radHalf = Math.max(MIN_RAD_HALF, params.eMedian);            // frame units, each side

        float[] inX = new float[PROFILE], inY = new float[PROFILE];
        float[] outX = new float[PROFILE], outY = new float[PROFILE];

        for (int i = 0; i < PROFILE; i++) {
            float f = 2f * (i / (float) (PROFILE - 1)) - 1f;                 // -1..1 along the arc
            float w = radHalf * (float) Math.sqrt(Math.max(0f, 1f - f * f)); // ellipse half-height
            float phi = phi0 + arcHalf * f;

            float cph = (float) Math.cos(phi), sph = (float) Math.sin(phi);
            float rIn = r0 - w, rOut = r0 + w;
            inX[i]  = rIn  * cph;  inY[i]  = rIn  * sph;
            outX[i] = rOut * cph;  outY[i] = rOut * sph;
        }

        edgeInX = inX; edgeInY = inY;
        edgeOutX = outX; edgeOutY = outY;

        polyX = new float[polyN()];
        polyY = new float[polyN()];
        int n = 0;
        for (int i = 0; i < PROFILE; i++) {              // outer edge, tip -> tip
            polyX[n] = outX[i]; polyY[n] = outY[i]; n++;
        }
        for (int i = PROFILE - 1; i >= 0; i--) {         // inner edge back
            polyX[n] = inX[i]; polyY[n] = inY[i]; n++;
        }
    }

    // =====================================================================
    // Containment, closed form. The lens is an ellipse in (arc, radius), so the
    // base class's 128-edge polygon walk is unnecessary here - a radial reject
    // in world space, then ten operations in frame space.
    // =====================================================================

    @Override
    public boolean containsPoint(Vector2f point, float radius) {
        if (params == null) return false;
        ensureBuilt();

        Vector2f a = params.primary.getLocation();
        float dx = point.x - a.x, dy = point.y - a.y;

        float gameR = (float) Math.sqrt(dx * dx + dy * dy);
        if (gameR < minR - radius || gameR > maxR + radius) return false;

        float rf = calc().getAU(gameR) / params.distanceAU;      // frame units
        double phi = Math.atan2(dy, dx) - Math.toRadians(getFrameAngle()) - phi0;
        while (phi > Math.PI) phi -= 2.0 * Math.PI;
        while (phi < -Math.PI) phi += 2.0 * Math.PI;

        float f = (float) (phi / arcHalf);
        if (f < -1f || f > 1f) return false;
        return Math.abs(rf - 1f) <= radHalf * (float) Math.sqrt(1f - f * f);
    }
}