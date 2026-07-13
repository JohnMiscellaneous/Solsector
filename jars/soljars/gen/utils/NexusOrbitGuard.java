package soljars.gen.utils;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;

public class NexusOrbitGuard implements EveryFrameScript {

    private final CampaignFleetAPI nexus;
    private final SectorEntityToken anchor;
    private boolean done = false;

    public NexusOrbitGuard(CampaignFleetAPI nexus, SectorEntityToken anchor) {
        this.nexus = nexus;
        this.anchor = anchor;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (done) return;

        if (nexus == null || nexus.isExpired() || !nexus.isAlive()) {
            if (anchor != null && anchor.getContainingLocation() != null)
                anchor.getContainingLocation().removeEntity(anchor);
            done = true;
            return;
        }

        if (nexus.getOrbit() == null) {
            nexus.setCircularOrbit(anchor, 0f, 0f, 100f);
        }
    }
}