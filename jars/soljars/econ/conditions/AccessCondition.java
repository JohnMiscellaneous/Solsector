package soljars.econ.conditions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class AccessCondition extends BaseMarketConditionPlugin implements ColonyInteractionListener {

    private static Map<String, Float> ACCESS_DATA = new HashMap<String, Float>();
    private static Map<String, Float> ELEVATOR_DATA = new HashMap<String, Float>();
    private static boolean dataLoaded = false;

    private static Map<String, AccessCondition> ACTIVE_LISTENERS = new HashMap<String, AccessCondition>();

    private String applyId = null;

    private String getListenerKey() {
        return market.getId() + "::" + condition.getId();
    }

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
        loadData();

        this.applyId = id;
        String condId = condition.getId();

        Float access = ACCESS_DATA.get(condId);
        if (access != null) {
            market.getAccessibilityMod().modifyFlat(id, access, condition.getName());
        }

        if (ELEVATOR_DATA.containsKey(condId)) {
            updateElevatorModifier(id);

            ListenerManagerAPI lm = Global.getSector().getListenerManager();
            String key = getListenerKey();

            AccessCondition prior = ACTIVE_LISTENERS.get(key);
            if (prior != null && prior != this) {
                lm.removeListener(prior);
            }

            lm.removeListener(this);
            lm.addListener(this);

            ACTIVE_LISTENERS.put(key, this);
        }
    }

    @Override
    public void unapply(String id) {
        market.getAccessibilityMod().unmodifyFlat(id);
        market.getAccessibilityMod().unmodifyFlat(id + "_elevator");

        ListenerManagerAPI lm = Global.getSector().getListenerManager();
        String key = getListenerKey();

        AccessCondition tracked = ACTIVE_LISTENERS.remove(key);
        if (tracked != null) {
            lm.removeListener(tracked);
        }
        lm.removeListener(this);

        this.applyId = null;
    }

    @Override
    public Map<String, String> getTokenReplacements() {
        return super.getTokenReplacements();
    }

    private boolean hasFullereneSpoolAtPort() {
        if (market == null) return false;

        Industry port = market.getIndustry(Industries.SPACEPORT);
        if (port == null) port = market.getIndustry(Industries.MEGAPORT);
        if (port == null) return false;

        SpecialItemData sid = port.getSpecialItem();
        if (sid == null) return false;

        return Items.FULLERENE_SPOOL.equals(sid.getId());
    }

    private void updateElevatorModifier(String id) {
        String modId = id + "_elevator";
        Float modifier = ELEVATOR_DATA.get(condition.getId());

        if (modifier != null && hasFullereneSpoolAtPort()) {
            market.getAccessibilityMod().modifyFlat(modId, modifier,
                    condition.getName() + " (Fullerene Tether)");
        } else {
            market.getAccessibilityMod().unmodifyFlat(modId);
        }
    }

    // ----- ColonyInteractionListener -----
    // for the tethers

    @Override
    public void reportPlayerOpenedMarket(MarketAPI market) {
    }

    @Override
    public void reportPlayerClosedMarket(MarketAPI closedMarket) {
        if (closedMarket == null || this.market == null) return;
        if (closedMarket != this.market) return;
        if (applyId == null) return;

        updateElevatorModifier(applyId);
    }

    @Override
    public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {
    }

    @Override
    public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
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