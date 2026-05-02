package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;

import com.fs.starfarer.api.impl.campaign.econ.sol_remove_replace;

public class goblin_world extends BaseHazardCondition {

    private static final String MEM_KEY_SWAP_QUEUED = "$goblin_world_swap_queued";

    @Override
    public void apply(String id) {
        super.apply(id);

        if (market.isPlanetConditionMarketOnly()) return;
        if (market.getSize() < 3) return;

        if (market.getMemoryWithoutUpdate().getBoolean(MEM_KEY_SWAP_QUEUED)) return;
        market.getMemoryWithoutUpdate().set(MEM_KEY_SWAP_QUEUED, true);

        sol_remove_replace.execute(market, "sol_goblin_world", "sol_goblin_subpop");
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getMemoryWithoutUpdate().unset(MEM_KEY_SWAP_QUEUED);
    }
}