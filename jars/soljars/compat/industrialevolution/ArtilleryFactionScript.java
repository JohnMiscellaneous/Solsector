package soljars.compat.industrialevolution;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import indevo.industries.artillery.scripts.ArtilleryStationScript;

public class ArtilleryFactionScript extends ArtilleryStationScript {

    private final String forcedFactionId;

    public ArtilleryFactionScript(MarketAPI market, String forcedFactionId) {
        super(market);
        this.forcedFactionId = forcedFactionId;
    }

    @Override
    public void updateFaction() {
        updateFaction(forcedFactionId);
    }
}