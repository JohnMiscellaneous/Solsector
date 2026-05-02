package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.sol_remove_replace;
// Upon colonization replaced with degenerate
// cargo-cult of the decivilized code
public class degenerate extends BaseHazardCondition {

    private static final String MEM_KEY_SWAP_QUEUED = "$degenerate_swap_queued";

    @Override
    public void apply(String id) {
        super.apply(id);

        if (market.isPlanetConditionMarketOnly()) return;
        if (market.getSize() < 3) return;

        // only queue the swap once per condition lifetime.
        if (market.getMemoryWithoutUpdate().getBoolean(MEM_KEY_SWAP_QUEUED)) return;
        market.getMemoryWithoutUpdate().set(MEM_KEY_SWAP_QUEUED, true);

        sol_remove_replace.execute(market, "sol_degenerate", "sol_degenerate_subpop");
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        // Clear the flag so if this condition is ever re-added, the swap can fire again.
        market.getMemoryWithoutUpdate().unset(MEM_KEY_SWAP_QUEUED);
    }
}