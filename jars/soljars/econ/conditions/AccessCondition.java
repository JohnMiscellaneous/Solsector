package soljars.econ.conditions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class AccessCondition extends BaseHazardCondition {

    private static Map<String, Float> ACCESS_DATA = new HashMap<String, Float>();
    private static Map<String, Float> ELEVATOR_DATA = new HashMap<String, Float>();
    private static boolean dataLoaded = false;

    private static void loadData() {
        if (dataLoaded) return;

        try {
            JSONArray csvData = Global.getSettings().getMergedSpreadsheetDataForMod("id", "data/campaign/procgen/sol_access.csv", "Solsector");

            for (int i = 0; i < csvData.length(); i++) {
                JSONObject row = csvData.getJSONObject(i);
                String id = row.getString("id");

                if (row.has("access")) {
                    float access = (float) row.optDouble("access", 0);
                    if (access != 0) {
                        ACCESS_DATA.put(id, access);
                    }
                }

                if (row.has("elevator_modifier")) {
                    double raw = row.optDouble("elevator_modifier", Double.NaN);
                    if (!Double.isNaN(raw) && raw != 0) {
                        ELEVATOR_DATA.put(id, (float) raw);
                    }
                }
            }
        } catch (IOException | JSONException e) {
            Global.getLogger(AccessCondition.class).error("Failed to load sol_access.csv", e);
        } finally {
            dataLoaded = true;
        }
    }

    @Override
    public void apply(String id) {
        super.apply(id);
        loadData();

        String condId = condition.getId();

        Float access = ACCESS_DATA.get(condId);
        if (access != null) {
            market.getAccessibilityMod().modifyFlat(id, access, condition.getName());
        }

        if (!market.isPlanetConditionMarketOnly()) {
            String modId = id + "_elevator";
            Float modifier = ELEVATOR_DATA.get(condId);
            if (modifier != null && hasFullereneSpoolAtPort()) {
                market.getAccessibilityMod().modifyFlat(modId, modifier,
                        condition.getName() + " (Fullerene Tether)");
            } else {
                market.getAccessibilityMod().unmodifyFlat(modId);
            }
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getAccessibilityMod().unmodifyFlat(id);
        market.getAccessibilityMod().unmodifyFlat(id + "_elevator");
    }

    @Override
    public Map<String, String> getTokenReplacements() {
        return super.getTokenReplacements();
    }

    private boolean hasFullereneSpoolAtPort() {
        if (market == null) return false;
        if (market.isPlanetConditionMarketOnly()) return false;

        Industry port = market.getIndustry(Industries.SPACEPORT);
        if (port == null) port = market.getIndustry(Industries.MEGAPORT);
        if (port == null) return false;
        if (!port.isFunctional()) return false;

        SpecialItemData sid = port.getSpecialItem();
        if (sid == null) return false;

        return Items.FULLERENE_SPOOL.equals(sid.getId());
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        loadData();

        float pad = 10f;
        String condId = condition.getId();

        Float access = ACCESS_DATA.get(condId);
        if (access != null && access != 0) {
            String sign = access > 0 ? "+" : "";
            String pct = sign + (int) Math.round(access * 100f) + "%";

            tooltip.addPara("%s accessibility", pad, Misc.getHighlightColor(), pct);
        }

        Float modifier = ELEVATOR_DATA.get(condId);
        if (modifier != null && modifier != 0) {
            String sign = modifier > 0 ? "+" : "";
            String pct = sign + (int) Math.round(modifier * 100f) + "%";

            tooltip.addPara("%s accessibility effect (fullerene spool)",
                    pad, Misc.getHighlightColor(), pct);
        }
    }
}