package soljars.gen.utils;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.campaign.listeners.PlayerColonizationListener;

// AOTD auto renames bodies, this restores them.
public class NameGuard implements PlayerColonizationListener {

    private static final Map<String, String> NAMES = new HashMap<>();
    static {
        NAMES.put("Mercury", "Mercury");
        NAMES.put("Venus", "Venus");
        NAMES.put("Earth", "Earth");
        NAMES.put("Luna", "Luna");
        NAMES.put("Mars", "Mars");
        NAMES.put("Phobos", "Phobos");
        NAMES.put("Deimos", "Deimos");
        NAMES.put("Ceres", "Ceres");
        NAMES.put("Vesta", "Vesta");
        NAMES.put("Hygiea", "Hygiea");
        NAMES.put("Pallas", "Pallas");
        NAMES.put("Hilda", "Hilda");
        NAMES.put("Jupiter", "Jupiter");
        NAMES.put("Amalthea", "Amalthea");
        NAMES.put("Io", "Io");
        NAMES.put("Europa", "Europa");
        NAMES.put("Ganymede", "Ganymede");
        NAMES.put("Callisto", "Callisto");
        NAMES.put("Himalia", "Himalia");
        NAMES.put("Hektor", "Hektor");
        NAMES.put("Agamemnon", "Agamemnon");
        NAMES.put("Saturn", "Saturn");
        NAMES.put("Prometheus", "Prometheus");
        NAMES.put("Mimas", "Mimas");
        NAMES.put("Enceladus", "Enceladus");
        NAMES.put("Tethys", "Tethys");
        NAMES.put("Dione", "Dione");
        NAMES.put("Rhea", "Rhea");
        NAMES.put("Titan", "Titan");
        NAMES.put("Hyperion", "Hyperion");
        NAMES.put("Iapetus", "Iapetus");
        NAMES.put("Phoebe", "Phoebe");
        NAMES.put("Chariklo", "Chariklo");
        NAMES.put("Chiron", "Chiron");
        NAMES.put("Uranus", "Uranus");
        NAMES.put("Miranda", "Miranda");
        NAMES.put("Ariel", "Ariel");
        NAMES.put("Umbriel", "Umbriel");
        NAMES.put("Titania", "Titania");
        NAMES.put("Oberon", "Oberon");
        NAMES.put("Neptune", "Neptune");
        NAMES.put("Larissa", "Larissa");
        NAMES.put("Proteus", "Proteus");
        NAMES.put("Triton", "Triton");
        NAMES.put("Nereid", "Nereid");
        NAMES.put("Gonggong", "Gonggong");
        NAMES.put("Salacia", "Salacia");
        NAMES.put("Clete", "Clete");
        NAMES.put("Lempo", "Lempo");
        NAMES.put("Hiisi", "Hiisi");
        NAMES.put("sol_Eris", "Eris");
        NAMES.put("Dysnomia", "Dysnomia");
        NAMES.put("Mani", "Mani");
        NAMES.put("Ixion", "Ixion");
        NAMES.put("Varda", "Varda");
        NAMES.put("Ilmare", "Ilmare");
        NAMES.put("Quaoar", "Quaoar");
        NAMES.put("Biden", "Biden");
        NAMES.put("Farout", "Farout");
        NAMES.put("Farfarout", "FarFarout");
        NAMES.put("Goblin", "The Goblin");
        NAMES.put("Gkunhomdima", "G'kun||'homdima");
        NAMES.put("Varuna", "Varuna");
        NAMES.put("Haumea", "Haumea");
        NAMES.put("Hiiaka", "Hiiaka");
        NAMES.put("Achlys", "Achlys");
        NAMES.put("Sedna", "Sedna");
        NAMES.put("Orcus", "Orcus");
        NAMES.put("Vanth", "Vanth");
        NAMES.put("Aya", "Aya");
        NAMES.put("Makemake", "Makemake");
        NAMES.put("Chiminigagua", "Chiminigagua");
        NAMES.put("Pluto", "Pluto");
        NAMES.put("Charon", "Charon");
        NAMES.put("SolIX", "Sol IX");
        NAMES.put("SolX", "Sol X");
        NAMES.put("SolXI", "Sol XI");
    }

    public static void install() {
        ListenerManagerAPI lm = Global.getSector().getListenerManager();
        lm.removeListenerOfClass(NameGuard.class);

        boolean preserve = false;
        try {
            preserve = Global.getSettings().loadJSON("data/config/sol_settings.json")
                    .optBoolean("Preserve_Planet_Names", false);
        } catch (Exception e) {
            Global.getLogger(NameGuard.class).error("SolMod: Failed to load settings", e);
        }
        if (!preserve) return;

        lm.addListener(new NameGuard());
    }

    @Override
    public void reportPlayerColonizedPlanet(PlanetAPI planet) {
        restore(planet);
    }

    @Override
    public void reportPlayerAbandonedColony(MarketAPI colony) {
        if (colony != null) restore(colony.getPlanetEntity());
    }

    private void restore(PlanetAPI planet) {
        if (planet == null) return;

        String name = NAMES.get(planet.getId());
        if (name == null) return;

        planet.setName(name);
        if (planet.getMarket() != null) planet.getMarket().setName(name);
    }
}