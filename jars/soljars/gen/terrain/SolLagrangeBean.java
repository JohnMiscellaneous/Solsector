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

/**
 * Symmetric curved-oval libration cloud for small trojan fields (Mars/Eureka,
 * Earth, Venus) where the CR3BP tadpole contour degenerates into a thin, sharply
 * pointed comma that log-scales badly. Instead of tracing a zero-velocity contour,
 * this is a plain lens: an ellipse in (arc-length x radius) space centered on
 * L4/L5, wrapped onto the orbital arc so it curves with the orbit. Symmetric both
 * along the arc and radially about the Lagrange point.
 *
 * Frame convention matches {@code TadpoleTerrainPlugin}: coordinates are normalized
 * to the A-B separation, origin at A (the star), A->B along +x, so L4 sits at
 * (0.5, sqrt(3)/2) and L5 at (0.5, -sqrt(3)/2). frame<->world, containment, and
 * rendering are therefore identical to the tadpole.
 */
public class SolLagrangeBean extends BaseTerrain {

    public static class LagrangeBeanParams {
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

        public LagrangeBeanParams(SectorEntityToken primary, float distanceAU, boolean leading,
                                  float extent, float eMedian, float profileMult, boolean isDense,
                                  String name, String texture) {
            this.primary = primary; this.distanceAU = distanceAU; this.leading = leading;
            this.extent = extent; this.eMedian = eMedian; this.profileMult = profileMult;
            this.isDense = isDense; this.name = name; this.texture = texture;
        }
    }

    protected static final int PROFILE = 64;               // along-arc samples per edge

    protected static final float MAX_ARC_HALF_DEG = 45f;   // extent=1 -> +-45 deg of arc half-width
    protected static final float MIN_RAD_HALF = 0.02f;     // radial half-width floor, frame units

    protected static final float MAP_EDGE_WIDTH = 300f;    // edge band thickness, game units
    protected static final float STATIONARY_MULT = 0.25f;  // dense-field slow-moving detect mult

    protected static final float SPARSE_BASE_MULT = 0.75f; // sparse-field moving detect mult
    protected static final float SPARSE_SLOW_MULT = 0.5f;  // sparse-field slow-moving detect mult

    protected static final float TILE_SCALE = 1f;          // arc repeats; >1 = more/shorter tiles
    protected static final float END_FADE = 0.15f;         // fraction of the arc feathered at each tip

    protected LagrangeBeanParams params;

    // the two edges in frame xy, paired by index, for a non-fanning triangle-strip fill
    protected transient float[] edgeInX, edgeInY, edgeOutX, edgeOutY;
    // closed polygon: outer edge forward, inner edge reversed
    protected transient float[] polyX, polyY;

    protected transient AstroCalc calc;
    protected transient SpriteAPI mapTex;
    protected transient SpriteAPI bandTex;
    protected transient float renderRange = -1f;

    @Override
    public void init(String terrainId, SectorEntityToken entity, Object param) {
        super.init(terrainId, entity, param);
        this.params = (LagrangeBeanParams) param;
    }

    protected AstroCalc calc() {
        if (calc == null) calc = new AstroCalc();
        return calc;
    }

    protected int polyN() { return PROFILE * 2; }

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
    protected void buildPolygon() {
        float phi0 = (float) Math.toRadians(params.leading ? 60f : -60f);  // L4/L5 heliocentric angle
        float r0 = 1f;                                                      // Lagrange radius (normalized)

        float ext = Math.max(0f, Math.min(1f, params.extent));
        float arcHalf = (float) Math.toRadians(MAX_ARC_HALF_DEG) * ext;    // radians of arc, each side
        float radHalf = Math.max(MIN_RAD_HALF, params.eMedian);            // frame units, each side

        float[] inX = new float[PROFILE], inY = new float[PROFILE];
        float[] outX = new float[PROFILE], outY = new float[PROFILE];

        for (int i = 0; i < PROFILE; i++) {
            float f = 2f * (i / (float) (PROFILE - 1)) - 1f;               // -1..1 along the arc
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
    // Frame <-> world. Polygon is real-space, normalized to the A-B
    // separation; game radii are log-scaled, so every vertex goes through
    // AstroCalc rather than a flat multiply.
    // =====================================================================

    protected Vector2f frameToWorld(float fx, float fy) {
        Vector2f a = params.primary.getLocation();
        double angB = Math.toRadians(getFrameAngle());

        double hx = fx * Math.cos(angB) - fy * Math.sin(angB);
        double hy = fx * Math.sin(angB) + fy * Math.cos(angB);

        double auR = Math.sqrt(hx * hx + hy * hy) * params.distanceAU;   // real AU from A
        double th = Math.atan2(hy, hx);                                  // heliocentric angle

        float gameR = calc().getDist((float) auR, params.primary);       // AU -> game units
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
        if (renderRange >= 0f) return renderRange;
        if (polyX == null) buildPolygon();
        if (entity == null) return calc().getDist(params.distanceAU, params.primary);

        Vector2f c = entity.getLocation();
        float max = 0f;
        for (int i = 0; i < polyN(); i++) {
            Vector2f w = frameToWorld(polyX[i], polyY[i]);
            float d = (float) Math.hypot(w.x - c.x, w.y - c.y);
            if (d > max) max = d;
        }
        renderRange = max + 100f;                                        // small pad
        return renderRange;
    }

    /**
     * Textured band across the lens. The triangle strip pairs the inner and outer
     * edges by index so it follows the arc without a fan; the sol_rings/rings_trojan
     * texture spans the band thickness once (S axis) and tiles along the arc (T axis),
     * so it reads as a vertical ring pattern repeating down the length. Per-vertex
     * alpha feathers the last {@code END_FADE} of the arc at each tip so the texture
     * fades out instead of ending on a hard pinch.
     */
    protected void renderBand(float alphaMult) {
        if (polyX == null) buildPolygon();
        if (bandTex == null) bandTex = Global.getSettings().getSprite("sol_rings", params.texture);

        // resolve both edges to world space once, accumulating arc length along the
        // centerline so tiling is uniform regardless of PROFILE
        float[] ox = new float[PROFILE], oy = new float[PROFILE];
        float[] ix = new float[PROFILE], iy = new float[PROFILE];
        float[] arc = new float[PROFILE];
        float acc = 0f;
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
        }

        // tile length that keeps the 256x512 texture undistorted at the belly: the
        // band thickness fills the 256 (S) axis, so one repeat should span
        // thickness * (512/256) of arc on the 512 (T) axis
        int mid = PROFILE / 2;
        float thickness = (float) Math.hypot(ox[mid] - ix[mid], oy[mid] - iy[mid]);
        float aspect = bandTex.getHeight() / Math.max(1f, bandTex.getWidth());
        float tileLen = Math.max(1f, thickness * aspect * TILE_SCALE);
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
    // Map rendering. Constant-width textured ribbon traced around the whole
    // boundary, closed into a loop, matching the vanilla belt look.
    // =====================================================================

    @Override
    public void renderOnMap(float factor, float alphaMult) {
        if (params == null) return;
        if (polyX == null) buildPolygon();
        if(params.isDense){
            if (mapTex == null) mapTex = Global.getSettings().getSprite("systemMap", "map_asteroid_belt");
        } else {
            if (mapTex == null) mapTex = Global.getSettings().getSprite("systemMap", "map_asteroid_belt_thin");
        }

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

            float tx = bx[next] - bx[prev], ty = by[next] - by[prev];
            float len = (float) Math.hypot(tx, ty);
            if (len < 1e-6f) len = 1f;
            float ox = -ty / len, oy = tx / len;         // across-ribbon normal

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

    // detect-range multipliers: a dense field hides fleets more (and pelts them),
    // a sparse one is thinner cover
    protected float sensorBase() { return params.isDense ? params.profileMult : SPARSE_BASE_MULT; }
    protected float sensorSlow() { return params.isDense ? STATIONARY_MULT   : SPARSE_SLOW_MULT; }

    @Override
    public void applyEffect(SectorEntityToken e, float days) {
        if (!(e instanceof CampaignFleetAPI)) return;
        CampaignFleetAPI fleet = (CampaignFleetAPI) e;

        float mult = Misc.isSlowMoving(fleet) ? sensorSlow() : sensorBase();

        fleet.getStats().addTemporaryModMult(
            0.1f,                          // duration, days - refreshed every frame while inside
            getModId(),                    // source key
            getTerrainName(),              // shown in the sensor tooltip breakdown
            mult,
            fleet.getStats().getDetectedRangeMod());

        if (!params.isDense) return;       // sparse fields are just light sensor cover

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
        // only dense fields carry impact danger; sparse ones are harmless cover
        if (!params.isDense) return false;
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
            (int) Math.round((1f - sensorBase()) * 100f) + "%");
        t.addPara("Stationary or slow-moving* fleets are instead detected at %s reduced range.", 10f,
            Misc.getHighlightColor(),
            (int) Math.round((1f - sensorSlow()) * 100f) + "%");
        if (params.isDense) {
            t.addPara("Chance of asteroid impacts that briefly knock the fleet off course and " +
                "may occasionally strike ships directly, dealing moderate damage. Smaller and " +
                "slow-moving* fleets are able to avoid them.", 10f,
                Misc.getHighlightColor(), "slow-moving");
        }
        t.addPara("*Press and hold %s to stop; combine with holding the left mouse button down to move slowly.", 10f,
            Misc.getGrayColor(), Misc.getHighlightColor(),
            stop);
    }

    @Override
    public String getEffectCategory() {
        return "sol_libration";
    }
}