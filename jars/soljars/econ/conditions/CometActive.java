package soljars.econ.conditions;

import java.awt.Color;

import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;

import soljars.econ.utils.IndustryCompat;
import soljars.econ.utils.OrbitRulerHelper;

public class CometActive extends BaseMarketConditionPlugin {

    public static final String ID = "sol_comet_active";

    private static final String SYNCHROTRON_CORE = "synchrotron";
    private static final String CATALYTIC_CORE = "catalytic_core";

    private static boolean hasInstalledItem(Industry ind, String itemId) {
        if (ind == null) return false;
        SpecialItemData s = ind.getSpecialItem();
        return s != null && itemId.equals(s.getId());
    }

    @Override
    public void apply(String id) {
        super.apply(id);
        String desc = condition.getName();

        Industry mining = IndustryCompat.getMining(market);
        if (mining != null) {
            mining.getSupply(Commodities.VOLATILES).getQuantity().modifyFlat(id, 3, desc);
            mining.getUpkeep().modifyMult(id, 0.75f, desc);
        }

        Industry fuel = IndustryCompat.getFuelProduction(market);
        if (hasInstalledItem(fuel, SYNCHROTRON_CORE) && fuel.isFunctional()) {
            fuel.getSupply(Commodities.FUEL).getQuantity().modifyFlat(id, -1, desc);
        }

        Industry refining = IndustryCompat.getRefining(market);
        if (hasInstalledItem(refining, CATALYTIC_CORE) && refining.isFunctional()) {
            refining.getSupply(Commodities.METALS).getQuantity().modifyFlat(id, -1, desc);
            refining.getSupply(Commodities.RARE_METALS).getQuantity().modifyFlat(id, -1, desc);
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        Industry mining = IndustryCompat.getMining(market);
        if (mining != null) {
            mining.getSupply(Commodities.VOLATILES).getQuantity().unmodifyFlat(id);
            mining.getUpkeep().unmodifyMult(id);
        }

        Industry fuel = IndustryCompat.getFuelProduction(market);
        if (fuel != null) {
            fuel.getSupply(Commodities.FUEL).getQuantity().unmodifyFlat(id);
        }

        Industry refining = IndustryCompat.getRefining(market);
        if (refining != null) {
            refining.getSupply(Commodities.METALS).getQuantity().unmodifyFlat(id);
            refining.getSupply(Commodities.RARE_METALS).getQuantity().unmodifyFlat(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();

        tooltip.addPara(
                market.getName() + " shines brilliantly as its surface sublimates, and blows like the "
                + "flame of an ancient candle in the solar wind. While the dust cloud allows for specialized extraction, "
                + "surface stability and vacuum processes will suffer.",
                pad);

        tooltip.addPara(
                "With a %s, the sublimation of " + market.getName() + " can be accelerated and brought "
                + "beyond the distance where cometary activity stops.",
                pad, h,
                "fusion lamp");

        Industry mining = IndustryCompat.getMining(market);
        Industry fuel = IndustryCompat.getFuelProduction(market);
        Industry refining = IndustryCompat.getRefining(market);

        tooltip.addPara("%s volatiles output (mining)", pad, h, "+3");
        tooltip.addPara("%s upkeep cost (mining)", pad, h, "x0.75");
        tooltip.addPara("%s metals and transplutonics output (catalytic core)", pad, h, "-1");
        tooltip.addPara("%s fuel output (synchrotron core)", pad, h, "-1");

        OrbitRulerHelper.renderCometRuler(tooltip, market, pad);
    }
}