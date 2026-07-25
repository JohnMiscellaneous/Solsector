package soljars.gen.systems.sol;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator;

import org.json.JSONObject;

import soljars.gen.utils.RemnantNexusFactory;
import soljars.gen.utils.RemnantPatrolFactory;
import soljars.gen.utils.AstroCalc;

public class RemnantHordeGen {

    public void generate(StarSystemAPI system) {

        SectorEntityToken star = system.getStar();

        RemnantPatrolFactory patrolFactory = new RemnantPatrolFactory();
        AstroCalc calc = new AstroCalc();

        JSONObject cfg;
        try {
            cfg = Global.getSettings().loadJSON("data/config/sol_settings.json");
        } catch (Exception e) {
            cfg = new JSONObject();
        }
        int remnantHorde        = cfg.optInt("remnant_difficulty", 1);
        int remnantSizeModifier = 0;
        if(remnantHorde == 1){ remnantSizeModifier = -10;}
        if(remnantHorde == 2){ remnantSizeModifier = 0;}
        if(remnantHorde == 3){ remnantSizeModifier = 10;}

        SectorEntityToken Mercury      = system.getEntityById("mercury");
        SectorEntityToken Mars         = system.getEntityById("Mars");
        SectorEntityToken Ceres        = system.getEntityById("Ceres");
        SectorEntityToken Vesta        = system.getEntityById("Vesta");
        SectorEntityToken Pallas       = system.getEntityById("Pallas");
        SectorEntityToken Hygiea       = system.getEntityById("Hygiea");
        SectorEntityToken Alexhelios   = system.getEntityById("Alexhelios");
        SectorEntityToken Cleoselene   = system.getEntityById("Cleoselene");
        SectorEntityToken Agamemnon    = system.getEntityById("Agamemnon");
        SectorEntityToken Mentor       = system.getEntityById("Mentor");
        SectorEntityToken Titan        = system.getEntityById("Titan");
        SectorEntityToken Amalthea     = system.getEntityById("Amalthea");
        SectorEntityToken Ganymede     = system.getEntityById("Ganymede");
        SectorEntityToken Triton       = system.getEntityById("Triton");
        SectorEntityToken VulcanEnergy = system.getEntityById("VulcanEnergy");
        SectorEntityToken Phobos       = system.getEntityById("Phobos");
        SectorEntityToken CeresJump    = system.getEntityById("jp_ceres");

        float sz_Mercury = Mercury.getRadius();
        float sz_Mars    = Mars.getRadius();
        float sz_Titan   = Titan.getRadius();
        float sz_Amalthea = Amalthea.getRadius();
        float p_Phobos   = Phobos.getCircularOrbitPeriod();
        float p_Vulcan   = VulcanEnergy.getCircularOrbitPeriod();
        float dist_VulcanShunt = VulcanEnergy.getCircularOrbitRadius();
        float angle_Phobos = Phobos.getCircularOrbitAngle();
        float angle_Vulcan = VulcanEnergy.getCircularOrbitAngle();
        float angle_CeresJump = CeresJump.getCircularOrbitAngle();

        if(remnantHorde >= 1){
            // Small remnant fleets
            // RemnantSeededFleetManager solRemnants = new RemnantSeededFleetManager(system, 5, 5,5, 20, 0.5f );
            // system.addScript(solRemnants);

            // --- Mercury Remnant Orbit ---
            float mercNexusRadius = sz_Mercury + 75f;
            float mercNexusPeriod = calc.getTime(10f);

            // First Nexus
            CampaignFleetAPI mercNexus1 = RemnantNexusFactory.spawnNexus(system, Mercury, "remnant_station2_Standard", 0f, mercNexusRadius, mercNexusPeriod, 5, 30 + remnantSizeModifier, 50 + remnantSizeModifier, null);

            // Second Nexus
            CampaignFleetAPI mercNexus2 = RemnantNexusFactory.spawnNexus(system, Mercury, "remnant_station2_Standard", 72f, mercNexusRadius, mercNexusPeriod, 5, 30 + remnantSizeModifier, 50 + remnantSizeModifier, null);

            // Third Nexus (Damaged)
            CampaignFleetAPI mercNexus3 = RemnantNexusFactory.spawnNexus(system, Mercury, "remnant_station2_Damaged", 144f, mercNexusRadius, mercNexusPeriod, 5, 5, 35 + remnantSizeModifier, Commodities.BETA_CORE);

            // Vambrace Wreck
            SectorEntityToken nexusWreck = system.addCustomEntity("nexusWreck", "Nexus Wreckage", "derelict_vambrace", "derelict");
            nexusWreck.setCircularOrbitPointingDown(Mercury, 216f, mercNexusRadius, mercNexusPeriod);

            SectorEntityToken nexusNav = system.addCustomEntity("nexusNav", "Nexus Nav Buoy", "nav_buoy", "remnant");
            nexusNav.setCircularOrbitPointingDown(Mercury, 288f, mercNexusRadius, mercNexusPeriod);
            nexusNav.setDiscoverable(true);

            // Mars opposite phobos
            CampaignFleetAPI marsNexus = RemnantNexusFactory.spawnNexus(system, Mars, "remnant_station2_Standard", (angle_Phobos + 180f), sz_Mars * 2f, p_Phobos, 5, 30 + remnantSizeModifier, 40 + remnantSizeModifier, null);

            CampaignFleetAPI solNexusAlpha = RemnantNexusFactory.spawnNexus(system, star, "remnant_station2_Standard", angle_Vulcan + 45f, dist_VulcanShunt, p_Vulcan, 5, 50 + remnantSizeModifier, 60 + remnantSizeModifier, null);
            CampaignFleetAPI solNexusBeta = RemnantNexusFactory.spawnNexus(system, star, "remnant_station2_Standard", angle_Vulcan + 135f, dist_VulcanShunt, p_Vulcan, 5, 50 + remnantSizeModifier, 60 + remnantSizeModifier, null);
            CampaignFleetAPI solNexusGama = RemnantNexusFactory.spawnNexus(system, star, "remnant_station2_Standard", angle_Vulcan + 225f, dist_VulcanShunt, p_Vulcan, 5, 50 + remnantSizeModifier, 60 + remnantSizeModifier, null);
            CampaignFleetAPI solNexusDelta = RemnantNexusFactory.spawnNexus(system, star, "remnant_station2_Standard", angle_Vulcan + 315f, dist_VulcanShunt, p_Vulcan, 5, 50 + remnantSizeModifier, 60 + remnantSizeModifier, null);
            if(remnantHorde >= 2){
            CampaignFleetAPI mercOrdo1 = patrolFactory.spawnPatrol(system, Mercury, 400f);
            } else {
            CampaignFleetAPI mercOrdo1 = patrolFactory.spawnPatrol(system, Mercury, 200f);
            }
            RemnantThemeGenerator.addBeacon(system, RemnantThemeGenerator.RemnantSystemType.RESURGENT);
        }
        if(remnantHorde >= 2){
            // Misc inner nexi
            CampaignFleetAPI vestaNexus = RemnantNexusFactory.spawnNexus(system, Vesta, "remnant_station2_Standard", 180f, 100f, calc.getTime(15f), 3, 20 + remnantSizeModifier, 40 + remnantSizeModifier, null);
            if (Pallas != null) {
                CampaignFleetAPI pallasNexus = RemnantNexusFactory.spawnNexus(system, Pallas, "remnant_station2_Damaged", 180f, 100f, calc.getTime(15f), 2, 5, 25 + remnantSizeModifier, null);
            }
            if (Hygiea != null) {
                CampaignFleetAPI hygieaNexus = RemnantNexusFactory.spawnNexus(system, Hygiea, "remnant_station2_Damaged", 180f, 100f, calc.getTime(15f), 2, 5, 25 + remnantSizeModifier, null);
            }
            CampaignFleetAPI lutetiaNexus = RemnantNexusFactory.spawnNexus(system, Vesta, "remnant_station2_Standard", 180f, 100f, calc.getTime(15f), 3, 20 + remnantSizeModifier, 40 + remnantSizeModifier, null);

            // Ceres opposite hyperjump
            CampaignFleetAPI ceresNexus = RemnantNexusFactory.spawnNexus(system, Ceres, "remnant_station2_Damaged", angle_CeresJump + 180f, 100f, calc.getTime(30f), 2, 5, 25 + remnantSizeModifier, null);

            // Alexhelios Weapon Platform (antipode of Kleopatra)
            if (Alexhelios != null) {
                CampaignFleetAPI alexNexus = RemnantNexusFactory.spawnNexus(system, Alexhelios, "station1_Standard", Alexhelios.getCircularOrbitAngle(), 30f, calc.getTime(20f), 1, 20 + remnantSizeModifier, 30 + remnantSizeModifier, Commodities.BETA_CORE);
            }

            // Cleoselene Weapon Platform (antipode of Kleopatra)
            if (Cleoselene != null) {
                CampaignFleetAPI cleoNexus = RemnantNexusFactory.spawnNexus(system, Cleoselene, "station1_Standard", Cleoselene.getCircularOrbitAngle(), 30f, calc.getTime(10f), 1, 20 + remnantSizeModifier, 30 + remnantSizeModifier, Commodities.BETA_CORE);
            }

            // Ceres (120 FP)
            CampaignFleetAPI ceresOrdo = patrolFactory.spawnPatrol(system, Ceres, 120f);

            // Mars (220 FP)
            CampaignFleetAPI marsOrdo = patrolFactory.spawnPatrol(system, Mars, 220f);

            // Mercury boss
            CampaignFleetAPI mercOrdo2 = patrolFactory.spawnPatrol(system, Mercury, 400f);
        }
        if(remnantHorde >= 3){
            // Agamemnon Weapon Platform
            if (Agamemnon != null) {
                CampaignFleetAPI agamemnonNexus = RemnantNexusFactory.spawnNexus(system, Agamemnon, "remnant_weapon_platform1_Standard", 180f, 100f, calc.getTime(5f), 1, 30 + remnantSizeModifier, 35 + remnantSizeModifier, Commodities.BETA_CORE);
            }

            // Mentor Weapon Platform
            if (Mentor != null) {
                CampaignFleetAPI mentorNexus = RemnantNexusFactory.spawnNexus(system, Mentor, "remnant_weapon_platform1_Standard", 180f, 100f, calc.getTime(5f), 1, 30 + remnantSizeModifier, 35 + remnantSizeModifier, Commodities.BETA_CORE);
            }

            // Titan Nexus
            CampaignFleetAPI titanNexus = RemnantNexusFactory.spawnNexus(system, Titan, "remnant_station2_Standard", 72f, sz_Titan * 4f, calc.getTime(10f), 3, 30 + remnantSizeModifier, 40 + remnantSizeModifier, null);

            // Amalthea Nexus
            CampaignFleetAPI amaltheaNexus = RemnantNexusFactory.spawnNexus(system, Amalthea, "remnant_station2_Standard", 72f, sz_Amalthea * 4f, calc.getTime(.1f), 3, 30 + remnantSizeModifier, 40 + remnantSizeModifier, null);

            // Triton Nexus
            CampaignFleetAPI tritonNexus = RemnantNexusFactory.spawnNexus(system, Titan, "remnant_station2_Standard", 72f, sz_Titan * 1f + 300, calc.getTime(10f), 3, 30 + remnantSizeModifier, 40 + remnantSizeModifier, null);

            // Ganymede Fleet(300 FP)
            CampaignFleetAPI ganiOrdo = patrolFactory.spawnPatrol(system, Ganymede, 300f);

            // Triton Fleets (200 FP)
            CampaignFleetAPI tritonOrdo1 = patrolFactory.spawnPatrol(system, Triton, 200f);
            CampaignFleetAPI tritonOrdo2 = patrolFactory.spawnPatrol(system, Triton, 200f);

            // Mercury Boss (800 FP)
            CampaignFleetAPI mercOrdo3 = patrolFactory.spawnPatrol(system, Mercury, 800f);
        }
    }
}