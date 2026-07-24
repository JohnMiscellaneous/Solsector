package soljars.gen.terrain;

import java.awt.Color;
import java.util.EnumSet;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * Coma/tail-free sensor haze around an active comet. Plain disc centered on the
 * terrain entity, radius set by the comet's magnitude. No impacts, no burn
 * penalty - it is only cover.
 */
public class CometComa extends BaseTerrain {

    public static class CometComaParams {
        public float cometMagnitude;   // coma radius, game units
        public String name;

        public CometComaParams(float cometMagnitude, String name) {
            this.cometMagnitude = cometMagnitude;
            this.name = name;
        }
    }

    protected static final float BASE_MULT = 0.7f;   // moving detect mult
    protected static final float SLOW_MULT = 0.5f;   // stationary/slow-moving detect mult
    protected static final int SEGMENTS = 48;                       // fan segments around the rim
    protected static final Color MAP_COLOR = new Color(150, 200, 255, 255);   // map tint only

    protected CometComaParams params;
    protected transient SpriteAPI mapTex;
    protected transient SpriteAPI comaTex;

    @Override
    public void init(String terrainId, SectorEntityToken entity, Object param) {
        super.init(terrainId, entity, param);
        this.params = (CometComaParams) param;
    }

    // =====================================================================
    // Geometry
    // =====================================================================

    @Override
    public boolean containsPoint(Vector2f point, float radius) {
        Vector2f c = entity.getLocation();
        float dx = point.x - c.x, dy = point.y - c.y;
        float r = params.cometMagnitude + radius;
        return dx * dx + dy * dy <= r * r;
    }

    @Override
    public float getRenderRange() {
        return params.cometMagnitude;
    }

    // =====================================================================
    // Rendering
    // =====================================================================

    @Override
    public EnumSet<CampaignEngineLayers> getActiveLayers() {
        return EnumSet.of(CampaignEngineLayers.TERRAIN_1);
    }

    /**
     * Textured disc. U is the radial axis - u=0 at the center where the texture is
     * solid, u=1 at the rim where it's transparent - and V runs around the
     * circumference, so the left-hand column wraps into a pure outward gradient.
     */
    protected void renderFan(SpriteAPI tex, Color color, float factor, float alphaMult) {
        float rf = color.getRed() / 255f;
        float gf = color.getGreen() / 255f;
        float bf = color.getBlue() / 255f;
        float baseA = (color.getAlpha() / 255f) * alphaMult;

        Vector2f loc = entity.getLocation();
        float cx = loc.x * factor, cy = loc.y * factor;
        float r = params.cometMagnitude * factor;

        // texture may be padded to power-of-two; these are the used fractions
        float tw = tex.getTextureWidth();
        float th = tex.getTextureHeight();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        tex.bindTexture();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(rf, gf, bf, baseA);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glTexCoord2f(0f, 0f);                  // center: solid edge of the gradient
        GL11.glVertex2f(cx, cy);
        for (int i = 0; i <= SEGMENTS; i++) {
            float t = i / (float) SEGMENTS;
            double a = t * Math.PI * 2.0;
            GL11.glTexCoord2f(tw, t * th);          // rim: transparent edge
            GL11.glVertex2f(cx + r * (float) Math.cos(a), cy + r * (float) Math.sin(a));
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        if (params == null) return;
        if (comaTex == null) comaTex = Global.getSettings().getSprite("sol_rings", "rings_cometcoma0");
        renderFan(comaTex, Color.WHITE, 1f, 1f);
    }

    @Override
    public void renderOnMap(float factor, float alphaMult) {
        if (params == null) return;
        if (mapTex == null) mapTex = Global.getSettings().getSprite("systemMap", "map_comet_coma");
        renderFan(mapTex, MAP_COLOR, factor, alphaMult);
    }

    // =====================================================================
    // Effect + tooltip
    // =====================================================================

    @Override
    public String getTerrainName() {
        return params.name != null ? params.name : "Coma";
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