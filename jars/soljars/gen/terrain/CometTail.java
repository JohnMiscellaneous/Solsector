package soljars.gen.terrain;

import java.awt.Color;
import java.util.EnumSet;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * Anti-sunward ion tail trailing an active comet. Constant-width strip rooted at
 * the comet, four magnitudes long and half a magnitude wide, dissolving over a
 * long alpha fade rather than tapering - a tapering strip makes every quad a
 * trapezoid, and affine texture interpolation bends the midline at each
 * triangle diagonal, which reads as a zig-zag down the centerline.
 *
 * Geometry is built once in local space with the tail along +x and the origin at
 * the comet; the only per-frame work is resolving the heliocentric angle, which
 * is pushed onto the entity as its facing. Vertices are rotated in software at
 * draw time, as SolLagrangeBean.frameToWorld does, with the trig hoisted out of
 * the loop since the facing is constant across the strip. Comets are eccentric,
 * so the facing comes from the star->comet vector directly - there is no circular
 * orbit angle to borrow - with a small deadband to suppress the position
 * quantization chatter that would otherwise wobble the strip.
 */
public class CometTail extends BaseTerrain {

    public static class CometTailParams {
        public float cometMagnitude;
        public String name;

        public CometTailParams(float cometMagnitude, String name) {
            this.cometMagnitude = cometMagnitude;
            this.name = name;
        }
    }

    protected static final float BASE_MULT = 0.7f;    // moving detect mult
    protected static final float SLOW_MULT = 0.5f;    // stationary/slow-moving detect mult

    protected static final float LENGTH_MULT = 4f;    // length, in magnitudes
    protected static final float WIDTH = 1.5f;        // constant width, in magnitudes

    // the strip is a rectangle, so segments exist only to sample the alpha curve;
    // spacing has to stay well under HEAD_FADE or the root ramp is lost
    protected static final int SEGMENTS = 8;
    protected static final float END_FADE = 0.65f;    // long dissolve toward the tip
    protected static final float HEAD_FADE = 0.08f;   // short ramp at the root, blends into the coma
    protected static final float FACING_DEADBAND = 0.03f;  // degrees

    protected static final Color MAP_COLOR = new Color(150, 200, 255, 255);   // map tint only

    protected CometTailParams params;

    protected float facing = Float.NaN;               // persists; recomputed on first advance

    // local-space strip, +x down the tail, origin at the comet
    protected transient float[] localX, localY, localT, localA;
    protected transient SpriteAPI mapTex;
    protected transient SpriteAPI tailTex;

    @Override
    public void init(String terrainId, SectorEntityToken entity, Object param) {
        super.init(terrainId, entity, param);
        this.params = (CometTailParams) param;
    }

    // =====================================================================
    // Geometry, built once in local space
    // =====================================================================

    protected float length() { return params.cometMagnitude * LENGTH_MULT; }

    protected float halfWidth() { return params.cometMagnitude * WIDTH * 0.5f; }

    /**
     * Quad strip vertex pairs, plus the T coordinate and alpha fade each pair
     * carries. Alpha smoothsteps to zero over the last {@code END_FADE} of the
     * length so the tail dissolves instead of ending on a hard edge, and ramps up
     * over the first {@code HEAD_FADE} so the root blends into the coma.
     */
    protected void buildLocal() {
        int n = (SEGMENTS + 1) * 2;
        localX = new float[n];
        localY = new float[n];
        localT = new float[SEGMENTS + 1];
        localA = new float[SEGMENTS + 1];

        float len = length();
        float hw = halfWidth();
        for (int i = 0; i <= SEGMENTS; i++) {
            float t = i / (float) SEGMENTS;

            localX[i * 2]     = len * t;  localY[i * 2]     = -hw;
            localX[i * 2 + 1] = len * t;  localY[i * 2 + 1] =  hw;

            localT[i] = t;
            float tip = clamp01((1f - t) / END_FADE);
            float head = clamp01(t / HEAD_FADE);
            float fade = Math.min(tip, head);
            localA[i] = fade * fade * (3f - 2f * fade);   // smoothstep
        }
    }

    protected static float clamp01(float x) {
        return x < 0f ? 0f : (x > 1f ? 1f : x);
    }

    // =====================================================================
    // Rotation. The tail is a real rotator; its facing is just pinned to the
    // radius vector rather than driven at a fixed rate.
    // =====================================================================

    @Override
    public void advance(float amount) {
        super.advance(amount);
        if (entity == null) return;

        LocationAPI loc = entity.getContainingLocation();
        if (!(loc instanceof StarSystemAPI)) return;

        Vector2f c = entity.getLocation();
        Vector2f s = ((StarSystemAPI) loc).getCenter().getLocation();
        float target = Misc.getAngleInDegrees(s, c);

        if (Float.isNaN(facing) || Math.abs(Misc.getAngleDiff(facing, target)) > FACING_DEADBAND) {
            facing = target;
            entity.setFacing(facing);
        }
    }

    protected float facing() {
        if (Float.isNaN(facing)) advance(0f);
        return Float.isNaN(facing) ? 0f : facing;
    }

    /** Local (tail-space) to world; ca/sa are the facing's cosine and sine. */
    protected Vector2f localToWorld(float lx, float ly, float ca, float sa) {
        Vector2f loc = entity.getLocation();
        return new Vector2f(loc.x + lx * ca - ly * sa,
                            loc.y + lx * sa + ly * ca);
    }

    // =====================================================================
    // Containment: inverse-rotate the point, then an analytic strip test
    // =====================================================================

    @Override
    public boolean containsPoint(Vector2f point, float radius) {
        Vector2f c = entity.getLocation();
        double ang = Math.toRadians(facing());
        float ca = (float) Math.cos(ang), sa = (float) Math.sin(ang);

        float dx = point.x - c.x, dy = point.y - c.y;
        float along = dx * ca + dy * sa;          // local x
        float perp  = -dx * sa + dy * ca;         // local y

        float len = length();
        if (along < -radius || along > len + radius) return false;
        return Math.abs(perp) <= halfWidth() + radius;
    }

    @Override
    public float getRenderRange() {
        return length() + params.cometMagnitude;
    }

    // =====================================================================
    // Rendering
    // =====================================================================

    @Override
    public EnumSet<CampaignEngineLayers> getActiveLayers() {
        return EnumSet.of(CampaignEngineLayers.TERRAIN_1);
    }

    /**
     * Constant-width strip; U spans the width, V runs the length. The texture is
     * centerline-dense and transparent at both edges, so U runs 0 to 1 straight
     * across the band. GL_MODULATE is set explicitly: the campaign pass can leave
     * the environment on GL_REPLACE, which discards both the vertex tint and the
     * per-vertex alpha and renders the strip flat and opaque.
     */
    protected void renderStrip(SpriteAPI tex, Color color, float factor, float alphaMult) {
        if (localX == null) buildLocal();

        float rf = color.getRed() / 255f;
        float gf = color.getGreen() / 255f;
        float bf = color.getBlue() / 255f;
        float baseA = (color.getAlpha() / 255f) * alphaMult;

        double ang = Math.toRadians(facing());
        float ca = (float) Math.cos(ang), sa = (float) Math.sin(ang);

        // texture may be padded to power-of-two; these are the used fractions
        float tw = tex.getTextureWidth();
        float th = tex.getTextureHeight();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        tex.bindTexture();
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= SEGMENTS; i++) {
            GL11.glColor4f(rf, gf, bf, baseA * localA[i]);

            int a = i * 2, b = i * 2 + 1;
            Vector2f wa = localToWorld(localX[a], localY[a], ca, sa);
            Vector2f wb = localToWorld(localX[b], localY[b], ca, sa);

            GL11.glTexCoord2f(0f, localT[i] * th);
            GL11.glVertex2f(wa.x * factor, wa.y * factor);
            GL11.glTexCoord2f(tw, localT[i] * th);
            GL11.glVertex2f(wb.x * factor, wb.y * factor);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        if (params == null) return;
        if (tailTex == null) tailTex = Global.getSettings().getSprite("sol_rings", "rings_comettail0");
        renderStrip(tailTex, Color.WHITE, 1f, 1f);
    }

    @Override
    public void renderOnMap(float factor, float alphaMult) {
        if (params == null) return;
        if (mapTex == null) mapTex = Global.getSettings().getSprite("systemMap", "map_comet_tail");
        renderStrip(mapTex, MAP_COLOR, factor, alphaMult);
    }

    // =====================================================================
    // Effect + tooltip
    // =====================================================================

    @Override
    public String getTerrainName() {
        return params.name != null ? params.name : "Comet Tail";
    }

    @Override
    public void applyEffect(SectorEntityToken e, float days) {
        if (!(e instanceof CampaignFleetAPI)) return;
        CampaignFleetAPI fleet = (CampaignFleetAPI) e;

        float mult = Misc.isSlowMoving(fleet) ? SLOW_MULT : BASE_MULT;

        fleet.getStats().addTemporaryModMult(
            0.1f,
            getModId(),
            getTerrainName(),
            mult,
            fleet.getStats().getDetectedRangeMod());
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
            (int) Math.round((1f - BASE_MULT) * 100f) + "%");
        t.addPara("Stationary or slow-moving* fleets are instead detected at %s reduced range.", 10f,
            Misc.getHighlightColor(),
            (int) Math.round((1f - SLOW_MULT) * 100f) + "%");
        t.addPara("*Press and hold %s to stop; combine with holding the left mouse button down to move slowly.", 10f,
            Misc.getGrayColor(), Misc.getHighlightColor(),
            stop);
    }

    @Override
    public String getEffectCategory() {
        return "sol_coma";
    }
}