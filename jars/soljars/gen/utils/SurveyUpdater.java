package soljars.gen.utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.campaign.listeners.SurveyPlanetListener;

public class SurveyUpdater implements SurveyPlanetListener {

    private static final String[] TARGETS = { "Goblin" };

    public static void install() {
        ListenerManagerAPI lm = Global.getSector().getListenerManager();
        lm.removeListenerOfClass(SurveyUpdater.class);

        if (allSurveyed()) return;

        lm.addListener(new SurveyUpdater());
    }

    private static boolean allSurveyed() {
        for (String id : TARGETS) {
            PlanetAPI planet = findPlanet(id);
            if (planet == null) continue;

            MarketAPI market = planet.getMarket();
            if (market == null) continue;

            if (market.getSurveyLevel() != MarketAPI.SurveyLevel.FULL) return false;
        }
        return true;
    }

    private static PlanetAPI findPlanet(String id) {
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            for (PlanetAPI planet : system.getPlanets()) {
                if (id.equals(planet.getId())) return planet;
            }
        }
        return null;
    }

    @Override
    public void reportPlayerSurveyedPlanet(PlanetAPI planet) {
        if (planet == null) return;

        if ("Goblin".equals(planet.getId())) {
            planet.setCustomDescriptionId("sol_the_goblin_surveyed");
        }
    }
}