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

// Shared by LagrangeBean and LagrangeBeanMinor
public abstract class LagrangeBeanBase extends BaseTerrain {

    protected static final int PROFILE = 64;

    protected static final float MAP_EDGE_WIDTH = 300f;
    protected static final float STATIONARY_MULT = 0.25f;

    protected static final float TILE_SCALE = 1f;
    protected static final float END_FADE = 0.15f;

    protected transient float[] polyX, polyY;
    protected transient float[] edgeInX, edgeInY, edgeOutX, edgeOutY;

    protected transient float[] wpX, wpY;
    protected transient float[] woX, woY, wiX, wiY;
    protected transient float[] railAX, railAY, railBX, railBY;
    protected transient float[] railSeg;
    protected transient float[] arcLen;
    protected transient float[] fadeA;
    protected transient float maxThick;
    protected transient float minR, maxR;
    protected transient float renderRange;
    protected transient boolean built;

    protected transient float[] arcV;
    protected transient float[] railV;

    protected transient AstroCalc calc;
    protected transient SpriteAPI mapTex, bandTex;
    protected transient Color mapColor;

    // =====================================================================
    // Subclass contract
    // =====================================================================

    protected abstract SectorEntityToken primary();

    protected abstract float distanceAU();

    protected abstract boolean leading();

    protected abstract String name();

    protected abstract Color bandColor();

    protected abstract String bandTextureId();

    protected abstract String mapTextureId();

    protected abstract float sensorBase();

    protected abstract float sensorSlow();

    protected abstract boolean hasImpacts();

    protected abstract void buildPolygon();

    // =====================================================================
    // Build + cache
    // =====================================================================

    protected AstroCalc calc() {
        if (calc == null) calc = new AstroCalc();
        return calc;
    }

    protected int polyN() { return PROFILE * 2; }

    protected static float clamp01(float x) {
        return x < 0f ? 0f : (x > 1f ? 1f : x);
    }

    protected void ensureBuilt() {
        if (built) return;
        buildPolygon();
        cacheWorld();
        built = true;
    }

    protected void toCache(float[] fx, float[] fy, float[] ox, float[] oy) {
        for (int i = 0; i < fx.length; i++) {
            double auR = Math.sqrt(fx[i] * fx[i] + fy[i] * fy[i]) * distanceAU();
            double th = Math.atan2(fy[i], fx[i]);
            float r = calc().getDist((float) auR, primary());
            ox[i] = r * (float) Math.cos(th);
            oy[i] = r * (float) Math.sin(th);
        }
    }

    protected void cacheWorld() {
        int m = polyN();
        wpX = new float[m]; wpY = new float[m];
        toCache(polyX, polyY, wpX, wpY);

        woX = new float[PROFILE]; woY = new float[PROFILE];
        wiX = new float[PROFILE]; wiY = new float[PROFILE];
        toCache(edgeOutX, edgeOutY, woX, woY);
        toCache(edgeInX, edgeInY, wiX, wiY);

        minR = Float.MAX_VALUE; maxR = 0f;
        for (int i = 0; i < m; i++) {
            float r = (float) Math.hypot(wpX[i], wpY[i]);
            if (r < minR) minR = r;
            if (r > maxR) maxR = r;
        }

        // rotation preserves lengths, so arc length, cross-section and the tip
        // feather are all fixed for the life of the terrain
        arcLen = new float[PROFILE];
        fadeA = new float[PROFILE];
        float acc = 0f;
        maxThick = 0f;
        for (int i = 0; i < PROFILE; i++) {
            if (i > 0) {
                float mx0 = (woX[i - 1] + wiX[i - 1]) * 0.5f, my0 = (woY[i - 1] + wiY[i - 1]) * 0.5f;
                float mx1 = (woX[i] + wiX[i]) * 0.5f,         my1 = (woY[i] + wiY[i]) * 0.5f;
                acc += (float) Math.hypot(mx1 - mx0, my1 - my0);
            }
            arcLen[i] = acc;

            float th = (float) Math.hypot(woX[i] - wiX[i], woY[i] - wiY[i]);
            if (th > maxThick) maxThick = th;

            float t = i / (float) (PROFILE - 1);
            float fade = clamp01(Math.min(t, 1f - t) / END_FADE);
            fadeA[i] = fade * fade * (3f - 2f * fade);
        }

        buildRails();
        buildRenderRange();

        arcV = null;
        railV = null;
    }

    protected void buildRails() {
        int m = polyN();
        float hw = MAP_EDGE_WIDTH * 0.5f;

        railAX = new float[m]; railAY = new float[m];
        railBX = new float[m]; railBY = new float[m];
        railSeg = new float[m];

        for (int i = 0; i < m; i++) {
            int prev = (i - 1 + m) % m, next = (i + 1) % m;

            // path tangent from neighbors, normal = tangent rotated 90
            float tx = wpX[next] - wpX[prev], ty = wpY[next] - wpY[prev];
            float len = (float) Math.hypot(tx, ty);
            if (len < 1e-6f) len = 1f;
            float nx = -ty / len, ny = tx / len;

            railAX[i] = wpX[i] - nx * hw; railAY[i] = wpY[i] - ny * hw;
            railBX[i] = wpX[i] + nx * hw; railBY[i] = wpY[i] + ny * hw;

            int nn = (i + 1) % m;
            railSeg[i] = (float) Math.hypot(wpX[nn] - wpX[i], wpY[nn] - wpY[i]);
        }
    }

    protected void buildRenderRange() {
        renderRange = calc().getDist(distanceAU() * 1.6f, primary());
        if (entity == null) return;

        double ang = Math.toRadians(getFrameAngle());
        float ca = (float) Math.cos(ang), sa = (float) Math.sin(ang);

        Vector2f a = primary().getLocation(), e = entity.getLocation();
        float dx = e.x - a.x, dy = e.y - a.y;
        float ex = dx * ca + dy * sa, ey = -dx * sa + dy * ca;

        float max = 0f;
        for (int i = 0; i < polyN(); i++) {
            float d = (float) Math.hypot(wpX[i] - ex, wpY[i] - ey);
            if (d > max) max = d;
        }
        renderRange = max + 100f;
    }

    protected float getFrameAngle() {
        float angSelf = entity.getOrbit() != null
                ? entity.getCircularOrbitAngle()
                : Misc.getAngleInDegrees(primary().getLocation(), entity.getLocation());
        return angSelf - (leading() ? 60f : -60f);
    }

    // Containment

    @Override
    public boolean containsPoint(Vector2f point, float radius) {
        if (primary() == null) return false;
        ensureBuilt();

        Vector2f a = primary().getLocation();
        float dx = point.x - a.x, dy = point.y - a.y;

        float r = (float) Math.sqrt(dx * dx + dy * dy);
        if (r < minR - radius || r > maxR + radius) return false;   // cheap radial reject

        double ang = Math.toRadians(getFrameAngle());
        float ca = (float) Math.cos(ang), sa = (float) Math.sin(ang);
        float px = dx * ca + dy * sa, py = -dx * sa + dy * ca;

        int n = polyN();
        boolean in = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            if ((wpY[i] > py) != (wpY[j] > py) &&
                px < (wpX[j] - wpX[i]) * (py - wpY[i]) / (wpY[j] - wpY[i]) + wpX[i]) {
                in = !in;
            }
        }
        return in;
    }

    // Campaign layer rendering

    @Override
    public EnumSet<CampaignEngineLayers> getActiveLayers() {
        return EnumSet.of(CampaignEngineLayers.TERRAIN_1);
    }

    @Override
    public float getRenderRange() {
        if (primary() == null) return 0f;
        ensureBuilt();
        return renderRange;
    }

    // Textured band
    protected void renderBand(float alphaMult) {
        ensureBuilt();
        if (bandTex == null) bandTex = Global.getSettings().getSprite("sol_rings", bandTextureId());

        if (arcV == null) {
            float aspect = bandTex.getHeight() / Math.max(1f, bandTex.getWidth());
            float tileLen = Math.max(1f, maxThick * aspect * TILE_SCALE);
            arcV = new float[PROFILE];
            for (int i = 0; i < PROFILE; i++) arcV[i] = arcLen[i] / tileLen;
        }

        double ang = Math.toRadians(getFrameAngle());
        float ca = (float) Math.cos(ang), sa = (float) Math.sin(ang);
        Vector2f a = primary().getLocation();

        Color c = bandColor();
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
            GL11.glColor4f(rf, gf, bf, baseA * fadeA[i]);

            GL11.glTexCoord2f(1f, arcV[i]);                        // outer edge, S=1
            GL11.glVertex2f(a.x + woX[i] * ca - woY[i] * sa,
                            a.y + woX[i] * sa + woY[i] * ca);
            GL11.glTexCoord2f(0f, arcV[i]);                        // inner edge, S=0
            GL11.glVertex2f(a.x + wiX[i] * ca - wiY[i] * sa,
                            a.y + wiX[i] * sa + wiY[i] * ca);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        if (primary() == null) return;
        renderBand(1f);
    }


    @Override
    public void renderOnMap(float factor, float alphaMult) {
        if (primary() == null) return;
        ensureBuilt();
        if (mapTex == null) mapTex = Global.getSettings().getSprite("systemMap", mapTextureId());
        if (mapColor == null) mapColor = Global.getSettings().getColor("asteroidBeltMapColor");

        int m = polyN();
        if (railV == null) {
            float k = mapTex.getTextureHeight() / mapTex.getHeight()
                    * mapTex.getWidth() / MAP_EDGE_WIDTH;
            railV = new float[m + 1];
            float acc = 0f;
            for (int i = 0; i <= m; i++) {
                railV[i] = acc;
                acc += railSeg[i % m] * k;
            }
        }

        double ang = Math.toRadians(getFrameAngle());
        float ca = (float) Math.cos(ang), sa = (float) Math.sin(ang);
        Vector2f a = primary().getLocation();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        mapTex.bindTexture();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4ub((byte) mapColor.getRed(), (byte) mapColor.getGreen(), (byte) mapColor.getBlue(),
                        (byte) (int) (mapColor.getAlpha() * alphaMult));

        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= m; i++) {
            int c0 = i % m;

            GL11.glTexCoord2f(0f, railV[i]);
            GL11.glVertex2f((a.x + railAX[c0] * ca - railAY[c0] * sa) * factor,
                            (a.y + railAX[c0] * sa + railAY[c0] * ca) * factor);
            GL11.glTexCoord2f(1f, railV[i]);
            GL11.glVertex2f((a.x + railBX[c0] * ca - railBY[c0] * sa) * factor,
                            (a.y + railBX[c0] * sa + railBY[c0] * ca) * factor);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    // Effect + tooltip

    @Override
    public String getTerrainName() {
        return name() != null ? name() : "Trojan Cloud";
    }

    @Override
    public void applyEffect(SectorEntityToken e, float days) {
        if (!(e instanceof CampaignFleetAPI)) return;
        CampaignFleetAPI fleet = (CampaignFleetAPI) e;

        float mult = Misc.isSlowMoving(fleet) ? sensorSlow() : sensorBase();

        fleet.getStats().addTemporaryModMult(
            0.1f,
            getModId(),
            getTerrainName(),
            mult,
            fleet.getStats().getDetectedRangeMod());

        if (!hasImpacts()) return;
        applyAsteroidImpacts(fleet);
    }

    // Asteroid impacts
    protected static void applyAsteroidImpacts(CampaignFleetAPI fleet) {
        if (fleet.isInHyperspaceTransition()) return;

        String key = "$asteroidImpactTimeout";
        String sKey = "$skippedImpacts";
        String recentKey = "$recentImpact";
        float probPerSkip = 0.15f;
        float maxProb = 1f;
        float maxSkipsToTrack = 7f;
        float durPerSkip = 0.2f;

        MemoryAPI mem = fleet.getMemoryWithoutUpdate();
        if (mem.contains(key)) return;

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

    @Override
    public boolean hasAIFlag(Object flag) {
        if (!hasImpacts()) return false;
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
        if (hasImpacts()) {
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