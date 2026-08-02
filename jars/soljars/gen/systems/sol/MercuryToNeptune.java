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

import soljars.gen.utils.AstroCalc;

import com.fs.starfarer.api.campaign.CampaignTerrainPlugin;

//TOdo Mithra, Crimea, Bacchus, 1996hw1, astrea, scheilla, cerberus, castalia

public class MercuryToNeptune {

    public void generate(StarSystemAPI system, SectorEntityToken star) {

        AstroCalc calc = new AstroCalc();

        // =========================================================================
        // ========================== MERCURY SYSTEM ===============================
        // =========================================================================
        // Mercury
        float dist_MercuryRaw = 0.3871f;
        PlanetAPI Mercury = (PlanetAPI) calc.spawnSPSObject(system, star, "Mercury", "Mercury", "barren_castiron", null, 4880f, dist_MercuryRaw, 0.2056f, 48.331f, 29.124f, 2026.06f, 0f, 58.646f, 1f);
        float angleMercury = Mercury.getCircularOrbitAngle();
        float sz_Mercury = Mercury.getRadius();
        float p_Mercury = Mercury.getCircularOrbitPeriod();
        float dist_Mercury = Mercury.getCircularOrbitRadius();

        Mercury.getSpec().setTexture("graphics/planets/mercury_tx.jpg"); 
        Mercury.getSpec().setAtmosphereThickness(0f); 
        Mercury.getSpec().setAtmosphereThicknessMin(10f); 
        Mercury.getSpec().setAtmosphereColor(new Color(0, 0, 0, 0)); 
        Mercury.getSpec().setIconColor(new Color(140, 130, 120, 255)); 
        Mercury.getSpec().setTilt(7.0f); 
        Mercury.getSpec().setPitch(90f); 
        Mercury.getSpec().setRotation(calc.getRot(58.6f)); 
        Mercury.applySpecChanges();

        calc.addConditions(Mercury.getMarket(), new String[] {
            "very_hot",
            "irradiated",
            "ore_ultrarich",
            "ruins_vast",
            "no_atmosphere",
            "sol_degenerate",
            "sol_megaforges",
            "sol_ancient_orbital_manufactories",
            "sol_space_ladder",
            "sol_ai_security_systems",
            "sol_oort_strikes",
            "sol_orbital_fleetworks"
        });
    }
}
