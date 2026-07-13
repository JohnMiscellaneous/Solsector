package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import org.json.JSONObject;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;

// Scripts
import soljars.gen.systems.sol.SolTotal;
import soljars.gen.systems.sol.SolDeferredSetupScript;
import soljars.econ.utils.DistanceConditionManager;
import soljars.gen.systems.sol.SolEconomies;
import soljars.gen.utils.SurveyUpdater;
import soljars.gen.utils.NameGuard;

public class SolModPlugin extends BaseModPlugin {

    @Override
    public void onNewGame() {
        boolean Generate_Sol = true;
        try {
            JSONObject settings = Global.getSettings().loadJSON("data/config/sol_settings.json");
            Generate_Sol = settings.optBoolean("Generate_Sol", true);
        } catch (Exception e) {
            Global.getLogger(this.getClass()).error("SolMod: Failed to load settings", e);
        }
        if (Generate_Sol) {
            new SolTotal().generate(Global.getSector());
        }
    }

    @Override
    public void onApplicationLoad() {
        ResourceDepositsCondition.COMMODITY.put("sol_improvised_hydroponics", Commodities.FOOD);
        ResourceDepositsCondition.MODIFIER.put("sol_improvised_hydroponics", -2);
        ResourceDepositsCondition.COMMODITY.put("sol_tyson_hq", Commodities.FOOD);
    }
    
    @Override
    public void onGameLoad(boolean newGame) {
        boolean isSettled = true;
        boolean instantMarkets = false;
        try {
            JSONObject settings = Global.getSettings().loadJSON("data/config/sol_settings.json");
            isSettled = settings.optBoolean("Generate_Settled_Planets", true);
            instantMarkets = settings.optBoolean("Settled_Planets_Spawn_In_Instantly", false);
        } catch (Exception e) {
            Global.getLogger(this.getClass()).error("SolMod: Failed to load settings", e);
        }

        MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();
        StarSystemAPI sol = Global.getSector().getStarSystem("sol");

        if (sol != null && isSettled && instantMarkets && !mem.getBoolean("$sol_discovery_done")) {
            new SolEconomies().generate(sol);
            mem.set("$sol_discovery_done", true);
            if (!mem.getBoolean("$sol_instant_init_notified")) {
                try {
                    Global.getSector().getCampaignUI().addMessage("Markets for Sol initialized");
                } catch (Exception ignore) {}
                mem.set("$sol_instant_init_notified", true);
            }
        }

        Global.getSector().addTransientScript(new SolDeferredSetupScript());
        DistanceConditionManager.install();
        SurveyUpdater.install();
        NameGuard.install();
    }
}