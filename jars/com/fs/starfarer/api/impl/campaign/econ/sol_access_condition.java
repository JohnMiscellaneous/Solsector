package com.fs.starfarer.api.impl.campaign.econ;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sol_access_condition extends BaseMarketConditionPlugin {

    private static Map<String, Float> ACCESS_DATA = new HashMap<String, Float>();
    private static boolean dataLoaded = false;

    private static void loadData() {
        if (dataLoaded) return;
        
        try {
            JSONArray csvData = Global.getSettings().getMergedSpreadsheetDataForMod("id", "data/campaign/procgen/sol_access.csv", "Solsector");
            
            for (int i = 0; i < csvData.length(); i++) {
                JSONObject row = csvData.getJSONObject(i);
                String id = row.getString("id");
                
                if (row.has("access")) {
                    float access = (float) row.getDouble("access");
                    if (access != 0) {
                        ACCESS_DATA.put(id, access);
                    }
                }
            }
        } catch (IOException | JSONException e) {
            Global.getLogger(sol_access_condition.class).error("Failed to load sol_access.csv", e);
        } finally {
            dataLoaded = true;
        }
    }

    @Override
    public void apply(String id) {
        loadData();
        
        if (ACCESS_DATA.containsKey(condition.getId())) {
            float access = ACCESS_DATA.get(condition.getId());
            market.getAccessibilityMod().modifyFlat(id, access, condition.getName());
        }
    }

    @Override
    public void unapply(String id) {
        market.getAccessibilityMod().unmodifyFlat(id);
    }

    @Override
    public Map<String, String> getTokenReplacements() {
        return super.getTokenReplacements();
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        loadData();

        if (ACCESS_DATA.containsKey(condition.getId())) {
            float access = ACCESS_DATA.get(condition.getId());
            
            if (access != 0) {
                String sign = access > 0 ? "+" : "";
                String pct = sign + (int)Math.round(access * 100f) + "%";
                
                tooltip.addPara("%s accessibility", 10f, Misc.getHighlightColor(), pct);
            }
        }
    }
}