package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

public class organ_complex extends BaseHazardCondition {

    // Data container to keep logic clean and accessible for both apply() and tooltip
    private static class CloneData {
        String flavorText = "";
        int organsBonus = 0;
        int stabilityPenalty = 0;
        int growthBonus = 0;
    }

    private CloneData getData(int size, boolean isFreePort) {
        CloneData data = new CloneData();

        if (isFreePort) {
            // FREE PORT MODE
            // Organs: +2 normally, +3 if size > 5 (i.e. Size 6+)
            data.organsBonus = (size > 5) ? 3 : 2;
            data.stabilityPenalty = -1;
            data.growthBonus = 2;

            if (size < 5) {
                data.flavorText = "Only the most promising clones are retained for labor; the rest are harvested immediately. The facility operates with brutal efficiency, suppressing the population before it can become a threat.";
            } else {
                data.flavorText = "The facility has been converted into a slaughterhouse of industrial scale. Clones are decanted and processed with terrifying speed to meet export demands, leaving no room for resistance.";
            }

        } else {
            // STANDARD MODE
            // Growth: 6 decreasing by 2 per size above 3.
            // Size 3: 6, Size 4: 4, Size 5: 2, Size 6: 0
            data.growthBonus = Math.max(0, 6 - ((size - 3) * 2));

            // Stability:
            if (size >= 6) {
                data.stabilityPenalty = -1;
                data.flavorText = "Strict martial law and heavy surveillance manage the clone gangs. While tensions remain, the sheer weight of security forces keeps the facility operational.";
            } else if (size == 5) {
                data.stabilityPenalty = -2;
                data.flavorText = "The clones have begun to organize. No longer just wandering aimlessly, they have formed gangs and primitive factions deep within the facility, actively disrupting operations.";
            } else {
                // Size 3-4
                data.stabilityPenalty = -3;
                data.flavorText = "Ethical protocols prohibit harvesting, but the clones are ignorant and resentful. They wander the corridors without purpose, causing frequent accidents and social disruption.";
            }
        }

        return data;
    }

    @Override
    public void apply(String id) {
        super.apply(id);

        String desc = condition.getName();
        int size = market.getSize();
        boolean isFreePort = market.isFreePort();

        CloneData data = getData(size, isFreePort);

        // Application
        if (data.organsBonus > 0) {
            Industry pop = market.getIndustry(Industries.POPULATION);
            if (pop != null) {
                pop.getSupply(Commodities.ORGANS).getQuantity().modifyFlat(id, data.organsBonus, desc);
            }
        }

        if (data.stabilityPenalty != 0) {
            market.getStability().modifyFlat(id, data.stabilityPenalty, desc);
        }

        if (data.growthBonus > 0) {
            market.getIncoming().getWeight().modifyFlat(id, data.growthBonus, desc);
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        market.getIncoming().getWeight().unmodifyFlat(id);
        market.getStability().unmodifyFlat(id);

        Industry pop = market.getIndustry(Industries.POPULATION);
        if (pop != null) {
            pop.getSupply(Commodities.ORGANS).getQuantity().unmodifyFlat(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();

        int size = market.getSize();
        boolean isFreePort = market.isFreePort();

        CloneData fpData = getData(size, true);
        CloneData stdData = getData(size, false);
        
        CloneData activeData = isFreePort ? fpData : stdData;

        if (market.isPlanetConditionMarketOnly()) {
            // Uncolonized description
            tooltip.addPara("The facility continues to decant confused, disoriented clones into silent halls, where they wander without supervision until they dehydrate and the recyclers claim them.", pad);
        } else if (!activeData.flavorText.isEmpty()) {
            tooltip.addPara(activeData.flavorText, pad);
        }

        // FREE PORT MODE
        tooltip.addSectionHeading("Free Port Mode", Alignment.MID, pad);
        Color fpColor = isFreePort ? h : g;

        tooltip.setBulletedListMode(" - ");
        
        if (fpData.organsBonus > 0) {
            tooltip.addPara("Organs production (Population): %s", 0f, tc, fpColor, "+" + fpData.organsBonus);
        }
        tooltip.addPara("Stability: %s", 0f, tc, fpColor, "" + fpData.stabilityPenalty);
        if (fpData.growthBonus > 0) {
            tooltip.addPara("Population growth: %s", 0f, tc, fpColor, "+" + fpData.growthBonus);
        }

        tooltip.setBulletedListMode(null);

        // STANDARD MODE
        tooltip.addSectionHeading("Standard Mode", Alignment.MID, pad);
        Color stdColor = !isFreePort ? h : g;

        tooltip.setBulletedListMode(" - ");

        if (stdData.growthBonus > 0) {
            tooltip.addPara("Population growth: %s", 0f, tc, stdColor, "+" + stdData.growthBonus);
        }
        
        tooltip.addPara("Stability: %s", 0f, tc, stdColor, "" + stdData.stabilityPenalty);

        tooltip.setBulletedListMode(null);
    }
}