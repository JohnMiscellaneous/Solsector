package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.sol_remove_replace;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

// Upon colonization replaced with degenerate
// cargo-cult of the decivilized code
public class degenerate extends BaseHazardCondition {

    private static final String MEM_KEY_SWAP_QUEUED = "$degenerate_swap_queued";

    @Override
    public void apply(String id) {
        super.apply(id);

        if (market.isPlanetConditionMarketOnly()) return;
        if (market.getSize() < 3) return;

        if (market.getMemoryWithoutUpdate().getBoolean(MEM_KEY_SWAP_QUEUED)) return;
        market.getMemoryWithoutUpdate().set(MEM_KEY_SWAP_QUEUED, true);

        sol_remove_replace.execute(market, "sol_degenerate", "sol_degenerate_subpop");
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getMemoryWithoutUpdate().unset(MEM_KEY_SWAP_QUEUED);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        if (market.isPlanetConditionMarketOnly()) {
            tooltip.addPara("Your officers estimate that a colony backed by adequate %s and the sheer %s of an established settlement could mitigate most of the danger posed by the unincorporated population.",
                10f, Misc.getHighlightColor(), "ground defenses", "size");
        }
    }
}