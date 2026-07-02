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

public class CometExtreme extends BaseMarketConditionPlugin {

    public static final String ID = "sol_comet_extreme";

    private static final String SYNCHROTRON_CORE = "synchrotron_core";
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
            mining.getSupply(Commodities.VOLATILES).getQuantity().modifyFlat(id, 6, desc);
            mining.getUpkeep().modifyMult(id, 0.5f, desc);
        }

        Industry fuel = IndustryCompat.getFuelProduction(market);
        if (hasInstalledItem(fuel, SYNCHROTRON_CORE)) {
            fuel.getSupply(Commodities.FUEL).getQuantity().modifyFlat(id, -2, desc);
        }

        Industry refining = IndustryCompat.getRefining(market);
        if (hasInstalledItem(refining, CATALYTIC_CORE)) {
            refining.getSupply(Commodities.METALS).getQuantity().modifyFlat(id, -2, desc);
            refining.getSupply(Commodities.RARE_METALS).getQuantity().modifyFlat(id, -2, desc);
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
                "A %s in orbit of " + market.getName() + " is accelerating the once-natural process of "
                + market.getName() + "'s degradation, heightening tectonic activity, and rendering vacuum "
                + "processes highly inefficient, as the surface is blown away in a stunning display of "
                + "light and resource extraction.",
                pad, h,
                "fusion lamp");

        Industry mining = IndustryCompat.getMining(market);
        Industry fuel = IndustryCompat.getFuelProduction(market);
        Industry refining = IndustryCompat.getRefining(market);

        tooltip.addPara("%s volatiles output (mining)", pad, h, "+6");
        tooltip.addPara("%s upkeep cost (mining)", pad, h, "-50%");
        tooltip.addPara("%s metals and transplutonics output (catalytic core)", pad, h, "-2");
        tooltip.addPara("%s fuel output (synchrotron core)", pad, h, "-2");

        OrbitRulerHelper.renderCometRuler(tooltip, market, pad);
    }
}