package soljars.gen.systems.sol;

import com.fs.starfarer.api.campaign.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.RingSystemTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantOfficerGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantStationFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator.RemnantSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantAssignmentAI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.List;
import java.util.ArrayList;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.EveryFrameScript;
import org.json.JSONObject;
import java.util.Random;

import soljars.gen.utils.*;
import soljars.gen.systems.sol.SolInnit;

import com.fs.starfarer.api.campaign.CampaignTerrainPlugin;

public class SolTotal {

    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem("Sol");
        LocationAPI hyper = Global.getSector().getHyperspace();

        AstroCalc calc = new AstroCalc();


        // =========================================================================
        // ============================ LET THERE BE LIGHT =========================
        // =========================================================================
        // Suun
        float zeroDegGlobal = 146f;

        float solRad = calc.getSize(1392700f);

        SectorEntityToken star = system.initStar("Sol", "star_yellow", solRad,  600f,  10f, 0.5f, 3f); 
        system.setLightColor(new Color(255, 245, 230)); 

        system.addTag(Tags.THEME_INTERESTING);
        system.addTag(Tags.THEME_UNSAFE);
        system.addTag(Tags.THEME_REMNANT);
        system.addTag(Tags.THEME_REMNANT_MAIN);
        system.addTag(Tags.THEME_REMNANT_RESURGENT);

        // =========================================================================
        // ========  On the Second Day, God Created the X-Y plane ==================
        // =========================================================================

        float finalX = 75000f;
        float finalY = 42000f;

        system.getLocation().set(finalX, finalY);

        // I didn’t bother changing the texture, the yellow_star texture is basically a slightly altered version of a popular sun true color.
        // Tags stolen from suitable star systems

        // =========================================================================
        // ========================  The sun and the stars =========================
        // =========================================================================

        system.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg"); 

        float solMapGridSize = 120000f; 
        system.setMapGridWidthOverride(solMapGridSize);
        system.setMapGridHeightOverride(solMapGridSize);

        // Phaethon
        SectorEntityToken Phaethon = calc.spawnSPSObject(system, star, "Phaethon", "Phaethon", "asteroid", "no_name", 6f, 1.2714f, 0.8898f, 265.220f, 322.180f, 2020.96f, zeroDegGlobal, 0.150f, 1f);
        Phaethon.setCustomDescriptionId("sol_phaethon");

        // Attach Jump Point
        JumpPointAPI jpPhaethon = Global.getFactory().createJumpPoint("jp_phaethon", "Phaethon Jump Point");
        jpPhaethon.setStandardWormholeToHyperspaceVisual();
        jpPhaethon.setCircularOrbit(Phaethon, 20, 35, 10);
        system.addEntity(jpPhaethon);
     
        system.autogenerateHyperspaceJumpPoints(true, false, false);

        Global.getSector().addScript(new SolInnit(system, star));
    }
}
