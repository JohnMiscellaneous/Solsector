package soljars.compat.industrialevolution;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

import indevo.ids.Ids;
import indevo.industries.artillery.conditions.ArtilleryStationCondition;
import indevo.industries.artillery.scripts.ArtilleryStationScript;
import indevo.industries.artillery.utils.ArtilleryStationPlacer;

public class ArtillerySpawnTool {

    // types: "mortar", "railgun", "missile"
    public static ArtilleryFactionScript spawnArtilleryStation(MarketAPI market, String factionId, String type) {
        if (market == null) return null;

        SectorEntityToken planet = market.getPrimaryEntity();
        if (planet == null) return null;

        if (planet.hasScriptOfClass(ArtilleryStationScript.class) || planet.hasTag(Ids.TAG_ENTITY_HAS_ARTILLERY_STATION))
            return null;

        // getType() reads TYPE_KEY off market memory; set it before the script places the station
        if (type != null) market.getMemoryWithoutUpdate().set(ArtilleryStationScript.TYPE_KEY, type);

        planet.addTag(Ids.TAG_ENTITY_HAS_ARTILLERY_STATION);

        ArtilleryFactionScript script = new ArtilleryFactionScript(market, factionId);
        script.setDestroyed(false);
        planet.addScript(script);
        planet.getMemoryWithoutUpdate().set(ArtilleryStationScript.SCRIPT_KEY, script);

        market.addTag(Ids.TAG_ARTILLERY_STATION);
        planet.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
        planet.getContainingLocation().addTag(Ids.TAG_SYSTEM_HAS_ARTILLERY);

        StarSystemAPI system = planet.getStarSystem();
        if (system.getEntitiesWithTag(Ids.TAG_WATCHTOWER).isEmpty())
            ArtilleryStationPlacer.placeWatchtowers(system, factionId);

        if (!market.hasCondition(ArtilleryStationCondition.ID))
            market.addCondition(ArtilleryStationCondition.ID);

        return script;
    }
}