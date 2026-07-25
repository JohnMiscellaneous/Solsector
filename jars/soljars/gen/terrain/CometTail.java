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

public class CometTail extends BaseTerrain {

    public static class CometTailParams {
        public float cometMagnitude;
        public String name;

        public CometTailParams(float cometMagnitude, String name) {
            this.cometMagnitude = cometMagnitude;
            this.name = name;
        }
    }

    protected static final float BASE_MULT = 0.7f;
    protected static final float SLOW_MULT = 0.5f;

    protected static final float LENGTH_MULT = 4f;
    protected static final float WIDTH = 1.5f; // Diameter, not radius.

    protected static final int SEGMENTS = 8;
    protected static final float END_FADE = 0.65f;  
    protected static final float HEAD_FADE = 0.08f;
    protected static final float FACING_DEADBAND = 0.03f;

    protected static final Color MAP_COLOR = new Color(150, 200, 255, 255);

    protected CometTailParams params;

    protected float facing = Float.NaN;

    protected transient float[] localX, localY, localT, localA;
    protected transient SpriteAPI mapTex;
    protected transient SpriteAPI tailTex;

    @Override
    public void init(String terrainId, SectorEntityToken entity, Object param) {
        super.init(terrainId, entity, param);
        this.params = (CometTailParams) param;
    }

    // Geometry

    protected float length() { return params.cometMagnitude * LENGTH_MULT; }

    protected float halfWidth() { return params.cometMagnitude * WIDTH * 0.5f; }

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

    // Rotation. Tail transient to 0,0. This wont work with binary stars.

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

    protected Vector2f localToWorld(float lx, float ly, float ca, float sa) {
        Vector2f loc = entity.getLocation();
        return new Vector2f(loc.x + lx * ca - ly * sa,
                            loc.y + lx * sa + ly * ca);
    }

    // Containment: inverse-rotate the point, then an analytic strip test

    @Override
    public boolean containsPoint(Vector2f point, float radius) {
        Vector2f c = entity.getLocation();
        double ang = Math.toRadians(facing());
        float ca = (float) Math.cos(ang), sa = (float) Math.sin(ang);

        float dx = point.x - c.x, dy = point.y - c.y;
        float along = dx * ca + dy * sa;
        float perp  = -dx * sa + dy * ca;

        float len = length();
        if (along < -radius || along > len + radius) return false;
        return Math.abs(perp) <= halfWidth() + radius;
    }

    @Override
    public float getRenderRange() {
        return length() + params.cometMagnitude;
    }

    // Rendering

    @Override
    public EnumSet<CampaignEngineLayers> getActiveLayers() {
        return EnumSet.of(CampaignEngineLayers.TERRAIN_1);
    }

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

    // Effect + tooltip

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