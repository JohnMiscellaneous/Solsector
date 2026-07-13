package soljars.compat.industrialevolution;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;

import indevo.industries.artillery.scripts.CampaignAttackScript;

public class SolCampaignAttackScript extends CampaignAttackScript {
    // Railguns fuck with fleets on circular orbits. This fixes it.
    public SolCampaignAttackScript(SectorEntityToken entity, String type) {
        super(entity, type);
    }

    @Override
    public boolean isHostileTo(CampaignFleetAPI target) {
        if (target.isStationMode()) return false;
        return super.isHostileTo(target);
    }
}