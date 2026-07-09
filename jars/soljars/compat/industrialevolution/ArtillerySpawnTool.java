package soljars.compat.industrialevolution;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import indevo.industries.artillery.scripts.ArtilleryStationScript;

public class ArtillerySpawnTool {

    public static ArtilleryFactionScript spawnArtilleryStation(MarketAPI market, String factionId, String type) {
        if (market == null) return null;

        SectorEntityToken primary = market.getPrimaryEntity();
        if (primary == null) return null;

        if (type != null) {
            market.getMemoryWithoutUpdate().set(ArtilleryStationScript.TYPE_KEY, type);
        }

        ArtilleryFactionScript script = new ArtilleryFactionScript(market, factionId);

        if (primary instanceof PlanetAPI) {
            primary.getMemoryWithoutUpdate().set(ArtilleryStationScript.SCRIPT_KEY, script);
        } else {
            primary.getOrbitFocus().getMemoryWithoutUpdate().set(ArtilleryStationScript.SCRIPT_KEY, script);
        }

        Global.getSector().addScript(script);
        return script;
    }

    public static ArtilleryFactionScript spawnArtilleryStation(MarketAPI market, String factionId) {
        return spawnArtilleryStation(market, factionId, null);
    }
}