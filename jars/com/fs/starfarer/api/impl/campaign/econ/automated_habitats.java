package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class automated_habitats extends BaseMarketConditionPlugin implements MarketImmigrationModifier {
    
    private static class HabitatData {
        String flavorText = "";
        int growth = 0;
        float hazard = 0f;
    }

    private HabitatData getData(int size) {
        HabitatData data = new HabitatData();

        if (size < 3){
            data.flavorText = "Automated custodians drift through the silent avenues, maintaining a perfect, sterile environment for a population that vanished centuries ago.";
            data.growth = 4;
            data.hazard = -0.25f;
        }  
        else if (size == 3) {
            data.flavorText = "Ancient automated systems hum with efficiency, vastly outstripping the needs of the small population. Housing and life support are plentiful, making survival trivial compared to standard worlds.";
            data.growth = 4;
            data.hazard = -0.25f;
        } 
        else if (size == 4) {
            data.flavorText = "The automated infrastructure barely keeps pace with the growing population. Every available habitation unit is occupied, and systems are running near capacity, satisfying needs but offering little surplus.";
            data.growth = 2;
            data.hazard = -0.20f;
        } 
        else if (size == 5) {
            data.flavorText = "The surviving automated infrastructure is now woefully inadequate. New expansion must be conducted manually, as the ancient systems can no longer support the swelling numbers on their own.";
            data.growth = 0;
            data.hazard = -0.10f;
        } 
        else { // size >= 6
            data.flavorText = "The physical capacity of the automated habitat has been eclipsed by the metropolis. All that remains is the advanced knowledge and engineering techniques gleaned from studying the ancient machinery.";
            data.growth = 0;
            data.hazard = -0.05f;
        }
        
        return data;
    }

    @Override
    public void apply(String id) {
        HabitatData data = getData(market.getSize());

        if (data.hazard != 0) {
            market.getHazard().modifyFlat(id, data.hazard, condition.getName());
        }
        
        market.addTransientImmigrationModifier(this);
    }

    @Override
    public void unapply(String id) {
        market.getHazard().unmodifyFlat(id);

        market.removeTransientImmigrationModifier(this);
    }
    
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        HabitatData data = getData(market.getSize());
        if (data.growth != 0) {
            incoming.getWeight().modifyFlat(condition.getId(), data.growth, condition.getName());
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        int size = market.getSize();
        HabitatData data = getData(size);

        if (!data.flavorText.isEmpty()) {
            tooltip.addPara(data.flavorText, 10f);
        }

        if (data.hazard != 0) {
            String sign = data.hazard > 0 ? "+" : ""; 
            tooltip.addPara("%s hazard rating", 10f, Misc.getHighlightColor(), sign + (int)(data.hazard * 100) + "%");
        }
        if (data.growth != 0) {
            tooltip.addPara("%s population growth", 10f, Misc.getHighlightColor(), "+" + data.growth);
        }
    }
}