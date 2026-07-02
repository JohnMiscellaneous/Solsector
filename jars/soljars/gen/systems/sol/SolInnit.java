package soljars.gen.systems.sol;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;

import soljars.gen.systems.sol.SolInner;

public class SolInnit implements EveryFrameScript {
    // Delay the start to avoid cross mod interactions on game start (widehorizons planet orbit changes) (texture changes)

    private final StarSystemAPI system;
    private final SectorEntityToken star;
    private boolean done = false;

    public SolInnit(StarSystemAPI system, SectorEntityToken star) {
        this.system = system;
        this.star = star;
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

        if (system != null && star != null) {
            new SolInner().generate(system, (PlanetAPI) star);
        }

        done = true;
    }
}