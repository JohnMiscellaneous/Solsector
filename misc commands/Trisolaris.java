runcode import soljars.gen.utils.ThreeBodySolution;
import soljars.gen.utils.AstroCalc;
import soljars.gen.utils.AstroCalc.KeplerComponent;
import soljars.gen.utils.AstroCalc.CompoundOrbit;
import soljars.gen.utils.CompoundOrbitTool;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.Global;

AstroCalc calc = new AstroCalc();

StarSystemAPI system = (StarSystemAPI) Global.getSector().getPlayerFleet().getContainingLocation();
SectorEntityToken player = Global.getSector().getPlayerFleet();
SectorEntityToken oldStar = system.getStar();

List<SectorEntityToken> entities = new ArrayList<SectorEntityToken>(system.getAllEntities());
for (SectorEntityToken entity : entities) {
    if (!entity.equals(oldStar) && !entity.equals(player)) {
        if (entity.getMarket() != null) {
            Global.getSector().getEconomy().removeMarket(entity.getMarket());
        }
        system.removeEntity(entity);
    }
}
if (oldStar != null) system.removeEntity(oldStar);
system.setType(StarSystemType.TRINARY_2FAR);

SectorEntityToken anchor = system.createToken(0,0);

double[][] pos = {
    { 0.716248295713,  0.384288553041 },
    { 0.086172594591,  1.342795868577 },
    { 0.538777980808,  0.481049882656 }
};
double[][] vel = {
    {  1.245268230896,  2.444311951777 },
    { -0.675224323690, -0.962879613630 },
    { -0.570043907206, -1.481432338147 }
};

ThreeBodySolution.generate(
    system, anchor,
    8.094721,                                        // natural period
    .5f,                                              // game days per cycle
    8000f,                                           // scale
    new String[]{ "trinary_a", "trinary_b", "trinary_c" },
    new String[]{ "Trisolaris A", "Trisolaris B", "Trisolaris C" },
    new String[]{ "star_white", "star_yellow", "star_yellow" },
    new float[] { 300f, 400f, 400f },
    4048,                                            // nIterations
    128,                                             // kComponents
    true,                                            // useVectorInputs
    pos, vel, 
    true);

float sz_trisolaris_I  = 60f;
float sz_trisolaris_II = 40f;

// trisolaris (star)
float a_trisolaris        = 1000f;  
float e_trisolaris        = 0.5f; 
float lp_trisolaris       = 73f;    
float p_trisolaris        = .5f;  
// Trisolaris elements (internal)
float a_trisolaris_I_II = 15f; // in buggy primary primarywidths?  
float e_trisolaris_I_II = 0.5f;
float p_trisolaris_I_II = .23f;

float[] trisolerisOffsets = calc.getBinaryOffsetsReal(sz_trisolaris_I, sz_trisolaris_II, a_trisolaris_I_II);
SectorEntityToken trisolaris_A = system.getEntityById("trinary_a");

// Trisolaris I 
List<KeplerComponent> tSolI = new ArrayList<KeplerComponent>();
tSolI.add(CompoundOrbitTool.kc(a_trisolaris,   e_trisolaris,      lp_trisolaris, p_trisolaris,      0f,   +1f));
tSolI.add(CompoundOrbitTool.kc(trisolerisOffsets[0], e_trisolaris_I_II, 0f,            p_trisolaris_I_II, 180f, +1f));
KeplerComponent[] tSolIArr = (KeplerComponent[]) tSolI.toArray(new KeplerComponent[tSolI.size()]);

PlanetAPI Trisolaris = (PlanetAPI) system.addPlanet(
    "Trisolaris", trisolaris_A, "Trisolaris", "barren-desert",
    0f, sz_trisolaris_I, a_trisolaris, p_trisolaris);
CompoundOrbitTool.attach(Trisolaris, trisolaris_A, tSolIArr);

// Trisolaris II pinned to Trisolaris I
List<KeplerComponent> tSolII = new ArrayList<KeplerComponent>();
tSolII.add(CompoundOrbitTool.kc(trisolerisOffsets[1]+trisolerisOffsets[0], e_trisolaris_I_II, 180f, p_trisolaris_I_II, 180f, +1f));
KeplerComponent[] tSolIIArr = (KeplerComponent[]) tSolII.toArray(new KeplerComponent[tSolII.size()]);

PlanetAPI TrisolarisII = (PlanetAPI) system.addPlanet(
    "Trisolaris_II", Trisolaris, "Trisolaris I", "barren",
    0f, sz_trisolaris_II, a_trisolaris_I_II, p_trisolaris_I_II);
CompoundOrbitTool.attach(TrisolarisII, Trisolaris, tSolIIArr);

// l3 jump point (even if the mass ratio doesnt support lagranges)
JumpPointAPI jp_trisolaris = Global.getFactory().createJumpPoint(
    "jp_trisolaris", "Trisolaris Jump Point");
jp_trisolaris.setStandardWormholeToHyperspaceVisual();
system.addEntity(jp_trisolaris);

List<KeplerComponent> jpOrbit = new ArrayList<KeplerComponent>();
jpOrbit.add(CompoundOrbitTool.kc(trisolerisOffsets[1], e_trisolaris_I_II, 0f, p_trisolaris_I_II, 180f, +1f));
KeplerComponent[] jpArr = (KeplerComponent[]) jpOrbit.toArray(new KeplerComponent[jpOrbit.size()]);
CompoundOrbitTool.attach(jp_trisolaris, Trisolaris, jpArr);

// SectorEntityToken TrisolarisMirrorAlpha = system.addCustomEntity(null, "Trisolaris Shade Alpha", "stellar_shade", "neutral");
// List<KeplerComponent> tmOrbit = new ArrayList<KeplerComponent>();
// tmOrbit.add(CompoundOrbitTool.kc(1000f - 200, e_trisolaris, lp_trisolaris, p_trisolaris, 0f, +1f));
// KeplerComponent[] tmArr = (KeplerComponent[]) tmOrbit.toArray(new KeplerComponent[tmOrbit.size()]);
// CompoundOrbitTool.attach(TrisolarisMirrorAlpha, trisolaris_A, tmArr);
// TrisolarisMirrorAlpha.setDiscoverable(true);
// TrisolarisMirrorAlpha.setSensorProfile(1000f);

calc.addConditions(Trisolaris.getMarket(), new String[] {
    "cold",
    "very_hot",
    "thin_atmosphere",
    "pollution",
    "ruins_extensive",
    "ore_sparse",
    "rare_ore_sparse",
    "poor_light",
    "sol_meteoroids",
    "sol_degenerate", // book mentions a moonrise so not tidally locked
    "sol_jump_point_nearby",
    "sol_ancient_orbital_manufactories",          
    "irradiated"
});

calc.addConditions(TrisolarisII.getMarket(), new String[] {
    "cold",
    "very_hot",
    "no_atmosphere",
    "low_gravity",
    "ruins_widespread",
    "ore_sparse",
    "poor_light",
    "sol_meteoroids",
    "sol_tidal_lock",
    "sol_jump_point_nearby", 
    "irradiated"
});

system.updateAllOrbits();
system.autogenerateHyperspaceJumpPoints(true, false);