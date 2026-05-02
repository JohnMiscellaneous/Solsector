package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import org.json.JSONObject;

// Scripts
import data.scripts.world.systems.SolTotal;
import data.scripts.world.systems.SolDeferredSetupScript;

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
        // Deferred setup script (replaces SolStationListener + SolDiscoveryListener).
        // Transient — re-added every load. Self-removes on first tick if all work is already done.
        Global.getSector().addTransientScript(new SolDeferredSetupScript());
    }
}