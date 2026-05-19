package soljars.econ.conditions;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class TinyStripped extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        super.apply(id);

        if (market.getSize() > 5) {
            market.getHazard().modifyFlat(id, 0.10f, condition.getName());
        } else {
            market.getHazard().unmodifyFlat(id);
        }

        Global.getSector().addScript(new StripAtmosphereScript(market));
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getHazard().unmodifyFlat(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        if (market.getSize() > 5) {
            tooltip.addPara("%s hazard rating", 10f, Misc.getHighlightColor(), "+10%");
        }

        tooltip.addPara("%s", 10f, Misc.getHighlightColor(), "Cannot hold atmosphere");
    }

    private static class StripAtmosphereScript implements EveryFrameScript {
        private final MarketAPI market;
        private boolean done = false;

        public StripAtmosphereScript(MarketAPI market) {
            this.market = market;
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

            if (market.hasCondition("no_atmosphere")) {
                done = true;
                return;
            }

            if (market.hasCondition("thin_atmosphere")) market.removeCondition("thin_atmosphere");
            if (market.hasCondition("toxic_atmosphere")) market.removeCondition("toxic_atmosphere");
            if (market.hasCondition("dense_atmosphere")) market.removeCondition("dense_atmosphere");
            if (market.hasCondition("extreme_weather")) market.removeCondition("extreme_weather");
            if (market.hasCondition("habitable")) market.removeCondition("habitable");

            market.addCondition("no_atmosphere");

            if (market.isPlayerOwned()) {
                Global.getSector().getCampaignUI().addMessage(market.getName() + " shines like a comet as its atmosphere bleeds into infinite void.", Misc.getHighlightColor());
            }

            done = true;
        }
    }
}