package soljars.gen.terrain;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.SectorEntityToken;


public class LagrangeBeanMinor extends LagrangeBeanBase {

    public static class LagrangeBeanMinorParams {
        public SectorEntityToken primary;  
        public float distanceAU;           
        public boolean leading;            
        public float extent;               
        public float eMedian;              
        public float profileMult;          
        public boolean isDense;            
        public String name;
        public String texture;             
        public Color color = new Color(255, 255, 255, 255);

        public LagrangeBeanMinorParams(SectorEntityToken primary, float distanceAU, boolean leading,
                                  float extent, float eMedian, float profileMult, boolean isDense,
                                  String name, String texture) {
            this.primary = primary; this.distanceAU = distanceAU; this.leading = leading;
            this.extent = extent; this.eMedian = eMedian; this.profileMult = profileMult;
            this.isDense = isDense; this.name = name; this.texture = texture;
        }
    }

    protected static final float MAX_ARC_HALF_DEG = 45f;
    protected static final float MIN_RAD_HALF = 0.02f;

    protected static final float SPARSE_BASE_MULT = 0.75f;
    protected static final float SPARSE_SLOW_MULT = 0.5f;

    protected LagrangeBeanMinorParams params;

    protected transient float phi0, arcHalf, radHalf;

    @Override
    public void init(String terrainId, SectorEntityToken entity, Object param) {
        super.init(terrainId, entity, param);
        this.params = (LagrangeBeanMinorParams) param;
    }


    @Override protected SectorEntityToken primary() { return params == null ? null : params.primary; }
    @Override protected float distanceAU()          { return params.distanceAU; }
    @Override protected boolean leading()           { return params.leading; }
    @Override protected String name()               { return params.name; }
    @Override protected Color bandColor()           { return params.color; }
    @Override protected String bandTextureId()      { return params.texture; }
    @Override protected String mapTextureId()       { return params.isDense ? "map_asteroid_belt" : "map_asteroid_belt_thin"; }

    @Override protected float sensorBase()          { return params.isDense ? params.profileMult : SPARSE_BASE_MULT; }
    @Override protected float sensorSlow()          { return params.isDense ? STATIONARY_MULT    : SPARSE_SLOW_MULT; }
    @Override protected boolean hasImpacts()        { return params.isDense; }

    // Geometry

    @Override
    protected void buildPolygon() {
        phi0 = (float) Math.toRadians(params.leading ? 60f : -60f);
        float r0 = 1f;                                               

        float ext = Math.max(0f, Math.min(1f, params.extent));
        arcHalf = (float) Math.toRadians(MAX_ARC_HALF_DEG) * ext;    
        radHalf = Math.max(MIN_RAD_HALF, params.eMedian);            

        float[] inX = new float[PROFILE], inY = new float[PROFILE];
        float[] outX = new float[PROFILE], outY = new float[PROFILE];

        for (int i = 0; i < PROFILE; i++) {
            float f = 2f * (i / (float) (PROFILE - 1)) - 1f;                
            float w = radHalf * (float) Math.sqrt(Math.max(0f, 1f - f * f));
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
        for (int i = 0; i < PROFILE; i++) {
            polyX[n] = outX[i]; polyY[n] = outY[i]; n++;
        }
        for (int i = PROFILE - 1; i >= 0; i--) {
            polyX[n] = inX[i]; polyY[n] = inY[i]; n++;
        }
    }

    // Containment, closed form.

    @Override
    public boolean containsPoint(Vector2f point, float radius) {
        if (params == null) return false;
        ensureBuilt();

        Vector2f a = params.primary.getLocation();
        float dx = point.x - a.x, dy = point.y - a.y;

        float gameR = (float) Math.sqrt(dx * dx + dy * dy);
        if (gameR < minR - radius || gameR > maxR + radius) return false;

        float rf = calc().getAU(gameR) / params.distanceAU;
        double phi = Math.atan2(dy, dx) - Math.toRadians(getFrameAngle()) - phi0;
        while (phi > Math.PI) phi -= 2.0 * Math.PI;
        while (phi < -Math.PI) phi += 2.0 * Math.PI;

        float f = (float) (phi / arcHalf);
        if (f < -1f || f > 1f) return false;
        return Math.abs(rf - 1f) <= radHalf * (float) Math.sqrt(1f - f * f);
    }
}