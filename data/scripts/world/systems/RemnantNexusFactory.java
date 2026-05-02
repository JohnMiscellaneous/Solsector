package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantOfficerGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantStationFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator;
import com.fs.starfarer.api.util.Misc;

public class RemnantNexusFactory {

    public static CampaignFleetAPI spawnNexus(StarSystemAPI system, SectorEntityToken focus,
                                               String variant, float angle, float radius,
                                               float period, int maxFleets,
                                               int minFleetSize, int maxFleetSize,
                                               String coreType) {

        if (coreType == null) {
            coreType = Commodities.ALPHA_CORE;
        }

        CampaignFleetAPI nexus = FleetFactoryV3.createEmptyFleet(Factions.REMNANTS, FleetTypes.BATTLESTATION, null);
        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);
        nexus.getFleetData().addFleetMember(member);
        nexus.getFleetData().setFlagship(member);
        nexus.setStationMode(true);
        nexus.setAI(null);

        MemoryAPI mem = nexus.getMemoryWithoutUpdate();
        mem.set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        mem.set(MemFlags.MEMORY_KEY_NO_JUMP, true);
        mem.set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);
        nexus.addTag(Tags.NEUTRINO_HIGH);

        nexus.getAbility("transponder").activate();
        nexus.getDetectedRangeMod().modifyFlat("gen", 1000f);

        PersonAPI commander = Misc.getAICoreOfficerPlugin(coreType)
                .createPerson(coreType, Factions.REMNANTS, new java.util.Random());
        member.setCaptain(commander);
        nexus.setCommander(commander);

        RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(member);

        system.addScript(new RemnantStationFleetManager(
                nexus, 1f, 1, maxFleets, 150f, minFleetSize, maxFleetSize));

        system.addEntity(nexus);
        nexus.setCircularOrbitPointingDown(focus, angle, radius, period);
        RemnantThemeGenerator.addRemnantStationInteractionConfig(nexus);

        return nexus;
    }
}