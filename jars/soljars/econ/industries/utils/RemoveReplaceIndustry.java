package soljars.econ.industries.utils;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

/**
 * Industry counterpart to {@link soljars.econ.utils.RemoveReplace}.
 * <p>
 * Defers the swap to the next campaign tick so it's safe to call from
 * inside listener callbacks, tooltips, or condition apply/unapply paths
 * where the market's industry list may be locked.
 * <p>
 * Behaviour:
 * <ul>
 *   <li>If {@code replaceId} is non-null and the market doesn't already have
 *       it, the replacement is added before the original is removed (avoids
 *       a frame with neither industry present).</li>
 *   <li>If {@code replaceId} is null, the original is simply removed.</li>
 *   <li>If the market already has {@code replaceId}, the original is removed
 *       and nothing else happens - no duplicate.</li>
 *   <li>If {@code removeId} isn't on the market when the script runs, the
 *       script no-ops and exits.</li>
 * </ul>
 */
public class RemoveReplaceIndustry {

    public static void execute(final MarketAPI market, final String removeId, final String replaceId) {
        if (market == null || removeId == null) return;

        Global.getSector().addScript(new EveryFrameScript() {
            private boolean done = false;

            @Override
            public boolean isDone() {
                return done;
            }

            @Override
            public boolean runWhilePaused() {
                return true;
            }

            @Override
            public void advance(float amount) {
                if (done) return;

                try {
                    Industry existing = market.getIndustry(removeId);
                    if (existing == null) {
                        done = true;
                        return;
                    }

                    if (replaceId != null && !market.hasIndustry(replaceId)) {
                        market.addIndustry(replaceId);
                    }

                    market.removeIndustry(removeId, null, false);
                    done = true;
                } catch (Exception e) {
                    Global.getSector().getCampaignUI().addMessage(
                        "Error in sol_remove_replace_industry: " + e.getMessage());
                    done = true;
                }
            }
        });
    }
}