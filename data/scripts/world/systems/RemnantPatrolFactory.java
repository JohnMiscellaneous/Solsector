package data.scripts.world.systems;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;

public class RemnantPatrolFactory {

    public CampaignFleetAPI spawnPatrol(StarSystemAPI system, SectorEntityToken source, float combatPoints) {
        FleetParamsV3 params = new FleetParamsV3(
                null,
                source.getLocationInHyperspace(),
                Factions.REMNANTS,
                1f,
                FleetTypes.PATROL_LARGE,
                combatPoints,
                0f, 0f, 0f, 0f, 0f, 0f
        );

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        system.addEntity(fleet);

        fleet.setLocation(source.getLocation().x, source.getLocation().y);

        RemnantSeededFleetManager.initRemnantFleetProperties(new java.util.Random(), fleet, false);

        fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, source, 1000000f);

        return fleet;
    }
}