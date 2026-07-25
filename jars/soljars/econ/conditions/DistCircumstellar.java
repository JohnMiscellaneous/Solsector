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

public class DistCircumstellar extends BaseMarketConditionPlugin {

    public static final String ID = "sol_dist_circumstellar";

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

        market.getHazard().modifyFlat(id, 0.15f, desc);

        Industry fuel = IndustryCompat.getFuelProduction(market);
        if (hasInstalledItem(fuel, SYNCHROTRON_CORE) && fuel.isFunctional()) {
            fuel.getSupply(Commodities.FUEL).getQuantity().modifyFlat(id, 1, desc);
            fuel.getUpkeep().modifyMult(id, 0.75f, desc);
        }

        Industry refining = IndustryCompat.getRefining(market);
        if (hasInstalledItem(refining, CATALYTIC_CORE) && refining.isFunctional()) {
            refining.getSupply(Commodities.METALS).getQuantity().modifyFlat(id, 1, desc);
            refining.getSupply(Commodities.RARE_METALS).getQuantity().modifyFlat(id, 1, desc);
            refining.getUpkeep().modifyMult(id, 0.75f, desc);
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        market.getHazard().unmodifyFlat(id);

        Industry fuel = IndustryCompat.getFuelProduction(market);
        if (fuel != null) {
            fuel.getSupply(Commodities.FUEL).getQuantity().unmodifyFlat(id);
            fuel.getUpkeep().unmodifyMult(id);
        }

        Industry refining = IndustryCompat.getRefining(market);
        if (refining != null) {
            refining.getSupply(Commodities.METALS).getQuantity().unmodifyFlat(id);
            refining.getSupply(Commodities.RARE_METALS).getQuantity().unmodifyFlat(id);
            refining.getUpkeep().unmodifyMult(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();

        tooltip.addPara("%s hazard rating", pad, h, "+15%");
        tooltip.addPara("%s metals and transplutonics output (catalytic core)", pad, h, "+1");
        tooltip.addPara("%s industry upkeep (catalytic core)", pad, h, "x0.75");
        tooltip.addPara("%s fuel production (synchrotron core)", pad, h, "+1");
        tooltip.addPara("%s industry upkeep (synchrotron core)", pad, h, "x0.75");

        OrbitRulerHelper.renderCircumstellarRuler(tooltip, market, pad);
    }
}