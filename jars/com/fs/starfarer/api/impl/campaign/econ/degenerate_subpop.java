package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import com.fs.starfarer.api.impl.campaign.econ.sol_industry_compat;
import com.fs.starfarer.api.impl.campaign.econ.sol_remove_replace;

public class degenerate_subpop extends BaseMarketConditionPlugin implements MarketImmigrationModifier {

    public static final float DEFENSE_UPKEEP_MULT = 2f;

    private static final String MEM_KEY_SWAP_QUEUED = "$degenerate_subpop_swap_queued";

    private static class DegenerateData {
        String popFlavor = "";
        String defenseFlavor = "";
        boolean highlightDefenseName = false;
        int stability = 0;
        int growth = 0;
        float hazard = 0f;
    }

    private DegenerateData getData(int size, boolean hasDefenses, String mName) {
        DegenerateData data = new DegenerateData();
        // Size & industry dependent flavor text
        // Consider... Not doing this again
        if (size <= 3) {
            data.popFlavor = "Civilization is being brought to " + mName + " at the end of a gauss rifle. Initial surveys indicate the unincorporated population is thinly dispersed, and attempts at contact with most groups have usually ended in violence.";
            
            if (hasDefenses) {
                data.defenseFlavor = "Patrols rove the countryside, bringing protection to " + mName + ". However, strict military protocols have instilled a shoot-first mentality, significantly decreasing recruitment.";
                data.stability = -2;
                data.growth = 1; 
                data.hazard = 0.15f;
            } else {
                data.defenseFlavor = "The colony is currently exposed. Establishing ground defenses is essential to secure the perimeter against the hostile unincorporated population.";
                data.highlightDefenseName = true;
                data.stability = -4;
                data.growth = 2; 
                data.hazard = 0.30f;
            }
        } 
        else if (size == 4) {
            data.popFlavor = "Civilization has come to " + mName + ". Unincorporated population estimates are in the low hundreds of thousands. Outreach initiatives provide diminishing returns as the official population grows to match the unincorporated population.";

            if (hasDefenses) {
                data.defenseFlavor = "Hardened defenses surround administrative buildings. Patrols scout the outskirts and tunnel rats map ruins, though shoot-on-sight authorization makes recruitment a rarity.";
                data.stability = -2;
                data.growth = 0; 
                data.hazard = 0.10f;
            } else {
                data.defenseFlavor = "Without ground defenses, the outskirts remain dangerous, limiting the colony's ability to effectively manage the unincorporated population.";
                data.highlightDefenseName = true;
                data.stability = -3;
                data.growth = 1; 
                data.hazard = 0.25f;
            }
        } 
        else if (size == 5) {
            data.popFlavor = "Populations are at parity. While some tribes of the unincorporated population have unified in resistance, they can not pose an existential threat to the colony's stability.";

            if (hasDefenses) {
                data.defenseFlavor = "Heavy military infrastructure and a system of informants ensure that resistance is crushed before it can organize. The local population is pacified, though tension remains high.";
                data.stability = -1;
                data.growth = 0;
                data.hazard = 0.05f;
            } else {
                data.defenseFlavor = "While the colony is stable, the lack of ground defenses allows tribal remnants to harass logistics and infrastructure with impunity.";
                data.highlightDefenseName = true;
                data.stability = -2;
                data.growth = 0;
                data.hazard = 0.20f;
            }
        } 
        else if (size >= 6) {
            data.popFlavor = "Our population now dwarfs the unincorporated population. Remnants have integrated or faded into the deepest wilderness, posing little threat to the thriving metropolis.";

            if (hasDefenses) {
                data.defenseFlavor = "With overwhelming military superiority, the unincorporated population is a mere footnote in history. Total security is assured.";
                data.stability = 0;
                data.growth = 0;
                data.hazard = 0f;
            } else {
                data.defenseFlavor = "The metropolis is largely safe, though the absence of ground defenses means the occasional raid still slips through the cracks.";
                data.highlightDefenseName = true;
                data.stability = -1;
                data.growth = 0;
                data.hazard = 0.15f;
            }
        }
        
        return data;
    }

    @Override
    public void apply(String id) {
        String desc = condition.getName();

        // If the market is just a survey target (uncolonized) OR the population size drops below 3,
        // revert to sol_degenerate
        if (market.isPlanetConditionMarketOnly() || market.getSize() < 3) {
            
            if (!market.getMemoryWithoutUpdate().getBoolean(MEM_KEY_SWAP_QUEUED)) {
                market.getMemoryWithoutUpdate().set(MEM_KEY_SWAP_QUEUED, true);
                sol_remove_replace.execute(market, "sol_degenerate_subpop", "sol_degenerate");
            }
            return;
        }

        // Get the defense industry compat
        Industry defense = sol_industry_compat.getDefense(market);
        boolean hasDefenses = (defense != null && defense.isFunctional());
        
        // Calculate data based on presence of defenses
        DegenerateData data = getData(market.getSize(), hasDefenses, market.getName());

        if (data.stability != 0) {
            market.getStability().modifyFlat(id, data.stability, desc);
        }
        
        market.addTransientImmigrationModifier(this);

        if (data.hazard != 0) {
            market.getHazard().modifyFlat(id, data.hazard, desc);
        }

        // Apply Upkeep Penalty to the specific defense industry found
        if (defense != null) {
            defense.getUpkeep().modifyMult(id, DEFENSE_UPKEEP_MULT, desc);
        }
    }

    @Override
    public void unapply(String id) {
        market.getMemoryWithoutUpdate().unset(MEM_KEY_SWAP_QUEUED);
        market.getStability().unmodifyFlat(id);
        
        market.removeTransientImmigrationModifier(this);
        
        market.getHazard().unmodifyFlat(id);

        // Unapply Upkeep Penalty
        Industry defense = sol_industry_compat.getDefense(market);
        if (defense != null) {
            defense.getUpkeep().unmodifyMult(id);
        }
    }
    
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        // Safe check for modifyIncoming context
        Industry defenseInd = sol_industry_compat.getDefense(market);
        boolean hasDefenses = (defenseInd != null && defenseInd.isFunctional());
        
        DegenerateData data = getData(market.getSize(), hasDefenses, market.getName());
        
        if (data.growth != 0) {
            incoming.getWeight().modifyFlat(condition.getId(), data.growth, condition.getName());
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        int size = market.getSize();
        Industry defenseInd = sol_industry_compat.getDefense(market);
        boolean hasDefenses = (defenseInd != null && defenseInd.isFunctional());
        
        DegenerateData data = getData(size, hasDefenses, market.getName());

        if (!data.popFlavor.isEmpty()) {
            tooltip.addPara(data.popFlavor, 10f);
        }
        
        if (!data.defenseFlavor.isEmpty()) {
            if (data.highlightDefenseName) {
                tooltip.addPara(data.defenseFlavor, 10f, Misc.getHighlightColor(), "ground defenses");
            } else {
                tooltip.addPara(data.defenseFlavor, 10f);
            }
        }

        if (data.stability == 0) {
            tooltip.addPara("Negative effects cancelled by ground defenses.", 10f, Misc.getPositiveHighlightColor(), "Negative effects cancelled");
        } else {
            if (data.hazard != 0) {
                tooltip.addPara("%s hazard rating", 10f, Misc.getHighlightColor(), "+" + (int)(data.hazard * 100) + "%");
            }
            if (data.stability != 0) {
                tooltip.addPara("%s stability", 10f, Misc.getHighlightColor(), "" + data.stability);
            }
            if (data.growth != 0) {
                tooltip.addPara("%s population growth", 10f, Misc.getHighlightColor(), "+" + data.growth);
            }
        }

        tooltip.addPara("2x upkeep for ground defenses", 10f, Misc.getHighlightColor(), "2x");
    }
}