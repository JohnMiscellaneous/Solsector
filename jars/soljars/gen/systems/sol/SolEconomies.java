package soljars.gen.systems.sol;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;

public class SolEconomies {

    public void generate(StarSystemAPI system) {
        boolean mercuryCold = true;
        try {
            mercuryCold = Global.getSettings().loadJSON("data/config/sol_settings.json").optBoolean("Mercury_And_Venus_Have_Poor_Light", true);
        } catch (Exception e) {}

        // --------------------------------------------------------
        // TOKEN RETRIEVAL
        // --------------------------------------------------------
        SectorEntityToken Eros = system.getEntityById("Eros");
        SectorEntityToken Vesta = system.getEntityById("Vesta");
        SectorEntityToken Psyche = system.getEntityById("Psyche");
        SectorEntityToken Tethys = system.getEntityById("Tethys");
        SectorEntityToken Dione = system.getEntityById("Dione");
        SectorEntityToken Mimas = system.getEntityById("Mimas");
        SectorEntityToken Puck = system.getEntityById("Puck");
        SectorEntityToken polystation = system.getEntityById("polyso_station"); 
        SectorEntityToken ChaosStation = system.getEntityById("Chaos");
        SectorEntityToken Biden = system.getEntityById("Biden");
        SectorEntityToken Aya = system.getEntityById("Aya");
        SectorEntityToken Farfarout = system.getEntityById("Farfarout");
        SectorEntityToken Clete = system.getEntityById("Clete");

        // --------------------------------------------------------
        // EROS
        // --------------------------------------------------------
        if (Eros != null) {
            Eros.setFaction("pirates");
            MarketAPI marketEros = Global.getFactory().createMarket("marketEros", "Eros", 3);
            marketEros.setPrimaryEntity(Eros);
            marketEros.setFactionId(Factions.PIRATES);

            marketEros.addCondition("low_gravity");
            marketEros.addCondition("sol_tiny_stripped");
            marketEros.addCondition("sol_improvised_hydroponics");
            marketEros.addCondition("volatiles_trace");
            marketEros.addCondition("hot");
            if (Global.getSettings().getModManager().isModEnabled("Terraforming & Station Construction")) {
                marketEros.addCondition("pollution");
            } else {
                marketEros.addCondition("sol_no_atmosphere_bodgejob");
            }
            if (mercuryCold) marketEros.addCondition("cold");

            Eros.setMarket(marketEros);
            marketEros.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

            marketEros.addIndustry(Industries.SPACEPORT);
            marketEros.addIndustry(Industries.POPULATION);
            marketEros.addIndustry(Industries.GROUNDDEFENSES);
            marketEros.addIndustry(Industries.REFINING);
            marketEros.addIndustry(Industries.FARMING);
            marketEros.getIndustry(Industries.POPULATION).setAICoreId(Commodities.GAMMA_CORE);
            marketEros.getIndustry(Industries.GROUNDDEFENSES).setAICoreId(Commodities.BETA_CORE);

            marketEros.addSubmarket(Submarkets.SUBMARKET_OPEN);
            marketEros.addSubmarket(Submarkets.SUBMARKET_STORAGE);
            marketEros.addSubmarket(Submarkets.SUBMARKET_BLACK);

            Global.getSector().getEconomy().addMarket(marketEros, true);
            generateSystemContacts(marketEros);
            
            // HARDCODED TARIFF (18%)
            marketEros.getTariff().setBaseValue(0.18f);

            for (MarketConditionAPI condition : marketEros.getConditions()) condition.setSurveyed(true);
            Eros.setDiscoverable(true);                
        }

        // --------------------------------------------------------
        // VESTA
        // --------------------------------------------------------
        if (Vesta != null) {
            MarketAPI marketVesta = Vesta.getMarket();
            if (marketVesta != null) {
                marketVesta.setFactionId("independent");
                marketVesta.setSize(6);
                marketVesta.setPlanetConditionMarketOnly(false);
                marketVesta.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

                marketVesta.removeCondition("sol_porus");
                marketVesta.addCondition("cold");
                marketVesta.addCondition("no_atmosphere");
                marketVesta.addCondition("low_gravity");
                marketVesta.addCondition("ore_rich");
                marketVesta.addCondition("volatiles_diffuse");
                marketVesta.addCondition("rare_ore_sparse");
                marketVesta.addCondition("sol_meteoroids");
                marketVesta.addCondition("sol_orbital_ring");
                marketVesta.addCondition("sol_fast_rotator");
                marketVesta.addCondition("sol_lphhq");

                marketVesta.addIndustry(Industries.SPACEPORT);
                marketVesta.addIndustry(Industries.POPULATION);
                marketVesta.addIndustry(Industries.GROUNDDEFENSES);
                marketVesta.addIndustry(Industries.LIGHTINDUSTRY);
                marketVesta.addIndustry(Industries.FARMING);
                marketVesta.addIndustry(Industries.MINING);
                marketVesta.getIndustry(Industries.MINING).setAICoreId(Commodities.GAMMA_CORE);
                marketVesta.getIndustry(Industries.POPULATION).setAICoreId(Commodities.GAMMA_CORE);
                marketVesta.getIndustry(Industries.GROUNDDEFENSES).setAICoreId(Commodities.GAMMA_CORE);
                marketVesta.getIndustry(Industries.SPACEPORT).setAICoreId(Commodities.GAMMA_CORE);
                marketVesta.getIndustry(Industries.FARMING).setAICoreId(Commodities.BETA_CORE);
                marketVesta.getIndustry(Industries.LIGHTINDUSTRY).setAICoreId(Commodities.BETA_CORE);
                marketVesta.getIndustry(Industries.FARMING).setImproved(true);

                marketVesta.addSubmarket(Submarkets.SUBMARKET_OPEN);
                marketVesta.addSubmarket(Submarkets.SUBMARKET_BLACK);
                marketVesta.addSubmarket(Submarkets.SUBMARKET_STORAGE);

                Global.getSector().getEconomy().addMarket(marketVesta, true);
                generateSystemContacts(marketVesta);
                
                // HARDCODED TARIFF (18%)
                marketVesta.getTariff().setBaseValue(0.18f);

                Vesta.setFaction("independent");
                for (MarketConditionAPI condition : Vesta.getMarket().getConditions()) condition.setSurveyed(true);
                Vesta.setDiscoverable(true);                
            }
        }

        // --------------------------------------------------------
        // PSYCHE
        // --------------------------------------------------------
        if (Psyche != null) {
            Psyche.setFaction("pirates");
            MarketAPI marketPsyche = Global.getFactory().createMarket("marketPsyche", "Psyche", 6);
            marketPsyche.setPrimaryEntity(Psyche);
            marketPsyche.setFactionId(Factions.PIRATES);

            marketPsyche.addCondition("low_gravity");
            marketPsyche.addCondition("cold");
            marketPsyche.addCondition("ore_ultrarich");
            marketPsyche.addCondition("rare_ore_ultrarich");
            marketPsyche.addCondition("sol_meteoroids");
            marketPsyche.addCondition("sol_orbital_ring");
            marketPsyche.addCondition("sol_fast_rotator");
            marketPsyche.addCondition("sol_no_atmosphere_bodgejob");
            if (Global.getSettings().getModManager().isModEnabled("Terraforming & Station Construction")) {marketPsyche.addCondition("pollution");}

            Psyche.setMarket(marketPsyche);
            marketPsyche.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

            marketPsyche.addIndustry(Industries.SPACEPORT);
            marketPsyche.addIndustry(Industries.MINING);
            marketPsyche.addIndustry(Industries.POPULATION);
            marketPsyche.addIndustry(Industries.HEAVYBATTERIES);
            marketPsyche.addIndustry(Industries.PLANETARYSHIELD);
            marketPsyche.addIndustry(Industries.REFINING);
            marketPsyche.addIndustry(Industries.HEAVYINDUSTRY);
            marketPsyche.getIndustry(Industries.MINING).setAICoreId(Commodities.GAMMA_CORE);
            marketPsyche.getIndustry(Industries.POPULATION).setAICoreId(Commodities.GAMMA_CORE);
            marketPsyche.getIndustry(Industries.HEAVYBATTERIES).setAICoreId(Commodities.ALPHA_CORE);
            marketPsyche.getIndustry(Industries.PLANETARYSHIELD).setAICoreId(Commodities.ALPHA_CORE);
            marketPsyche.getIndustry(Industries.REFINING).setAICoreId(Commodities.BETA_CORE);
            marketPsyche.getIndustry(Industries.HEAVYINDUSTRY).setAICoreId(Commodities.BETA_CORE);
            marketPsyche.getIndustry(Industries.MINING).setSpecialItem(new SpecialItemData(Items.MANTLE_BORE, null));
            marketPsyche.getIndustry(Industries.HEAVYINDUSTRY).setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));
            marketPsyche.getIndustry(Industries.HEAVYBATTERIES).setSpecialItem(new SpecialItemData(Items.DRONE_REPLICATOR, null));
            marketPsyche.getIndustry(Industries.HEAVYBATTERIES).setImproved(true);
            marketPsyche.getIndustry(Industries.PLANETARYSHIELD).setImproved(true);

            marketPsyche.addSubmarket(Submarkets.SUBMARKET_OPEN);
            marketPsyche.addSubmarket(Submarkets.SUBMARKET_STORAGE);

            Global.getSector().getEconomy().addMarket(marketPsyche, true);
            generateSystemContacts(marketPsyche);
            
            // HARDCODED TARIFF (18%)
            marketPsyche.getTariff().setBaseValue(0.18f);

            for (MarketConditionAPI condition : marketPsyche.getConditions()) condition.setSurveyed(true);
            Psyche.setDiscoverable(true);
        }

        // --------------------------------------------------------
        // TETHYS
        // --------------------------------------------------------
        if (Tethys != null) {
            MarketAPI marketTethys = Tethys.getMarket();
            if (marketTethys != null) {
                marketTethys.setFactionId("independent");
                marketTethys.setSize(3);
                marketTethys.setPlanetConditionMarketOnly(false);
                marketTethys.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

                marketTethys.removeCondition("sol_porus");
                marketTethys.addCondition("tectonic_activity");
                marketTethys.addCondition("very_cold");
                marketTethys.addCondition("low_gravity");
                marketTethys.addCondition("no_atmosphere");
                marketTethys.addCondition("poor_light");
                marketTethys.addCondition("volatiles_abundant");
                marketTethys.addCondition("ore_sparse");

                marketTethys.addIndustry(Industries.SPACEPORT);
                marketTethys.addIndustry(Industries.POPULATION);
                marketTethys.addIndustry(Industries.GROUNDDEFENSES);
                marketTethys.getIndustry(Industries.POPULATION).setAICoreId(Commodities.BETA_CORE);
                marketTethys.getIndustry(Industries.GROUNDDEFENSES).setAICoreId(Commodities.BETA_CORE);
                marketTethys.getIndustry(Industries.SPACEPORT).setAICoreId(Commodities.BETA_CORE);

                marketTethys.addSubmarket(Submarkets.SUBMARKET_OPEN);
                marketTethys.addSubmarket(Submarkets.SUBMARKET_BLACK);
                marketTethys.addSubmarket(Submarkets.SUBMARKET_STORAGE);

                Global.getSector().getEconomy().addMarket(marketTethys, true);
                generateSystemContacts(marketTethys);
                
                // HARDCODED TARIFF (18%)
                marketTethys.getTariff().setBaseValue(0.18f);

                Tethys.setFaction("independent");
                for (MarketConditionAPI condition : marketTethys.getConditions()) condition.setSurveyed(true);
                Tethys.setDiscoverable(true);
            }
        }

        // --------------------------------------------------------
        // DIONE
        // --------------------------------------------------------
        if (Dione != null) {
            MarketAPI marketDione = Dione.getMarket();
            if (marketDione != null) {
                marketDione.setFactionId("independent");
                marketDione.setSize(5);
                marketDione.setPlanetConditionMarketOnly(false);
                marketDione.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

                marketDione.removeCondition("sol_porus");
                marketDione.addCondition("tectonic_activity");
                marketDione.addCondition("very_cold");
                marketDione.addCondition("low_gravity");
                marketDione.addCondition("no_atmosphere");
                marketDione.addCondition("poor_light");
                marketDione.addCondition("volatiles_plentiful");
                marketDione.addCondition("ore_sparse");
                marketDione.addCondition("sol_subsurface_ocean");

                marketDione.addIndustry(Industries.SPACEPORT);
                marketDione.addIndustry(Industries.POPULATION);
                marketDione.addIndustry(Industries.HEAVYBATTERIES);
                marketDione.addIndustry("sol_subsurface_aquaponics");
                marketDione.addIndustry(Industries.MINING);
                marketDione.getIndustry(Industries.POPULATION).setAICoreId(Commodities.GAMMA_CORE);
                marketDione.getIndustry(Industries.HEAVYBATTERIES).setAICoreId(Commodities.GAMMA_CORE);
                marketDione.getIndustry(Industries.SPACEPORT).setAICoreId(Commodities.GAMMA_CORE);
                marketDione.getIndustry("sol_subsurface_aquaponics").setAICoreId(Commodities.BETA_CORE);
                marketDione.getIndustry(Industries.MINING).setAICoreId(Commodities.GAMMA_CORE);
                marketDione.getIndustry("sol_subsurface_aquaponics").setImproved(true);

                marketDione.addSubmarket(Submarkets.SUBMARKET_OPEN);
                marketDione.addSubmarket(Submarkets.SUBMARKET_BLACK);
                marketDione.addSubmarket(Submarkets.SUBMARKET_STORAGE);

                Global.getSector().getEconomy().addMarket(marketDione, true);
                generateSystemContacts(marketDione);
                
                // HARDCODED TARIFF (18%)
                marketDione.getTariff().setBaseValue(0.18f);

                Dione.setFaction("independent");
                for (MarketConditionAPI condition : marketDione.getConditions()) condition.setSurveyed(true);
                Dione.setDiscoverable(true);
            }
        }

        // --------------------------------------------------------
        // Mimas
        // --------------------------------------------------------
        if (Mimas != null) {
            MarketAPI marketMimas = Mimas.getMarket();
            if (marketMimas != null) {
                marketMimas.setFactionId("independent");
                marketMimas.setSize(4);
                marketMimas.setPlanetConditionMarketOnly(false);
                marketMimas.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

                marketMimas.addCondition("organics_trace");
                marketMimas.addCondition("sol_subsurface_ocean");

                marketMimas.addIndustry(Industries.SPACEPORT);
                marketMimas.addIndustry(Industries.POPULATION);
                marketMimas.addIndustry("sol_subsurface_aquaponics");
                marketMimas.addIndustry(Industries.MINING);
                marketMimas.getIndustry(Industries.MINING).setAICoreId(Commodities.GAMMA_CORE);
                marketMimas.getIndustry(Industries.POPULATION).setAICoreId(Commodities.GAMMA_CORE);
                marketMimas.getIndustry(Industries.SPACEPORT).setAICoreId(Commodities.GAMMA_CORE);
                marketMimas.getIndustry(Industries.SPACEPORT).setImproved(true);
                marketMimas.getIndustry("sol_subsurface_aquaponics").setImproved(true);
                marketMimas.getIndustry("sol_subsurface_aquaponics").setAICoreId(Commodities.BETA_CORE);

                marketMimas.addSubmarket(Submarkets.SUBMARKET_OPEN);
                marketMimas.addSubmarket(Submarkets.SUBMARKET_BLACK);
                marketMimas.addSubmarket(Submarkets.SUBMARKET_STORAGE);

                Global.getSector().getEconomy().addMarket(marketMimas, true);
                generateSystemContacts(marketMimas);
                
                // HARDCODED TARIFF (18%)
                marketMimas.getTariff().setBaseValue(0.18f);

                Mimas.setFaction("independent");
                for (MarketConditionAPI condition : marketMimas.getConditions()) condition.setSurveyed(true);
                Mimas.setDiscoverable(true);
            }
        }

        // --------------------------------------------------------
        // PUCK
        // --------------------------------------------------------
        if (Puck != null) {
            Puck.setFaction("luddic_path");
            MarketAPI marketPuck = Global.getFactory().createMarket("marketPuck", "Puck", 4);
            marketPuck.setPrimaryEntity(Puck);
            marketPuck.setFactionId(Factions.LUDDIC_PATH);
            marketPuck.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

            marketPuck.addCondition("very_cold");
            marketPuck.addCondition("low_gravity");
            marketPuck.addCondition("ore_sparse");
            marketPuck.addCondition("volatiles_trace");
            marketPuck.addCondition("dark");
            marketPuck.addCondition("irradiated");
            marketPuck.addCondition("sol_no_atmosphere_bodgejob");
            marketPuck.addCondition("sol_improvised_hydroponics");

            if (Global.getSettings().getModManager().isModEnabled("Terraforming & Station Construction")) {marketPuck.addCondition("pollution");}

            Puck.setMarket(marketPuck);
            marketPuck.addIndustry(Industries.SPACEPORT);
            marketPuck.addIndustry(Industries.POPULATION);
            marketPuck.addIndustry(Industries.PATROLHQ);
            marketPuck.addIndustry(Industries.MINING);
            marketPuck.addIndustry(Industries.FARMING);
            marketPuck.addIndustry(Industries.HEAVYBATTERIES);

            marketPuck.addSubmarket(Submarkets.SUBMARKET_OPEN);
            marketPuck.addSubmarket(Submarkets.SUBMARKET_BLACK);
            marketPuck.addSubmarket(Submarkets.SUBMARKET_STORAGE);

            Global.getSector().getEconomy().addMarket(marketPuck, true);
            generateSystemContacts(marketPuck);
            
            // HARDCODED TARIFF (18%)
            marketPuck.getTariff().setBaseValue(0.18f);

            for (MarketConditionAPI condition : marketPuck.getConditions()) condition.setSurveyed(true);
        }

        // --------------------------------------------------------
        // POLYSO STATION
        // --------------------------------------------------------
        if (polystation != null) {
            polystation.setFaction("luddic_path");
            MarketAPI marketPolysoPath = Global.getFactory().createMarket("polyso_market", "Polyso Station", 5);
            marketPolysoPath.setPrimaryEntity(polystation);
            marketPolysoPath.setFactionId(Factions.LUDDIC_PATH);
            marketPolysoPath.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

            marketPolysoPath.addCondition("very_cold");
            marketPolysoPath.addCondition("dark");
            marketPolysoPath.addCondition("sol_dist_abyssal");
            marketPolysoPath.addCondition("sol_improvised_hydroponics");
            marketPolysoPath.addCondition("volatiles_trace");
            if (Global.getSettings().getModManager().isModEnabled("Terraforming & Station Construction")) {marketPolysoPath.addCondition("pollution");}

            polystation.setMarket(marketPolysoPath);
            marketPolysoPath.addIndustry(Industries.POPULATION);
            marketPolysoPath.addIndustry(Industries.SPACEPORT);
            marketPolysoPath.addIndustry(Industries.FARMING);
            marketPolysoPath.addIndustry(Industries.PATROLHQ);
            marketPolysoPath.addIndustry(Industries.GROUNDDEFENSES);

            marketPolysoPath.addSubmarket(Submarkets.SUBMARKET_OPEN);
            marketPolysoPath.addSubmarket(Submarkets.SUBMARKET_BLACK);
            marketPolysoPath.addSubmarket(Submarkets.SUBMARKET_STORAGE);

            Global.getSector().getEconomy().addMarket(marketPolysoPath, true);
            generateSystemContacts(marketPolysoPath);
            
            // HARDCODED TARIFF (18%)
            marketPolysoPath.getTariff().setBaseValue(0.18f);

            for (MarketConditionAPI condition : marketPolysoPath.getConditions()) condition.setSurveyed(true);
            polystation.setDiscoverable(true);
            polystation.setSensorProfile(4000f);
        }

        // --------------------------------------------------------
        // CHAOS (Pirates)
        // --------------------------------------------------------
        if (ChaosStation != null) {
            ChaosStation.setFaction("pirates");
            MarketAPI marketChaosPirate = Global.getFactory().createMarket("marketChaosPirate", "Chaos", 4);
            marketChaosPirate.setPrimaryEntity(ChaosStation);
            marketChaosPirate.setFactionId(Factions.PIRATES);
            marketChaosPirate.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

            marketChaosPirate.addCondition("very_cold");
            marketChaosPirate.addCondition("dark");
            marketChaosPirate.addCondition("low_gravity");
            marketChaosPirate.addCondition("no_atmosphere");
            marketChaosPirate.addCondition("ore_abundant");
            marketChaosPirate.addCondition("rare_ore_moderate");
            marketChaosPirate.addCondition("volatiles_plentiful");
            marketChaosPirate.addCondition("sol_dist_abyssal");
            marketChaosPirate.addCondition("sol_contact_binary");
            if (Global.getSettings().getModManager().isModEnabled("Terraforming & Station Construction")) {marketChaosPirate.addCondition("pollution");}
            
            ChaosStation.setMarket(marketChaosPirate);
            marketChaosPirate.addIndustry(Industries.POPULATION);
            marketChaosPirate.addIndustry(Industries.SPACEPORT);
            marketChaosPirate.addIndustry(Industries.PATROLHQ);
            marketChaosPirate.addIndustry(Industries.MINING);
            marketChaosPirate.getIndustry(Industries.PATROLHQ).setAICoreId(Commodities.BETA_CORE);
            marketChaosPirate.getIndustry(Industries.POPULATION).setAICoreId(Commodities.BETA_CORE);
            marketChaosPirate.getIndustry(Industries.POPULATION).setSpecialItem(new SpecialItemData(Items.ORBITAL_FUSION_LAMP, null));
            marketChaosPirate.getIndustry(Industries.MINING).setAICoreId(Commodities.GAMMA_CORE);
            marketChaosPirate.getIndustry(Industries.MINING).setImproved(true);

            marketChaosPirate.addSubmarket(Submarkets.SUBMARKET_OPEN);
            marketChaosPirate.addSubmarket(Submarkets.SUBMARKET_BLACK);
            marketChaosPirate.addSubmarket(Submarkets.SUBMARKET_STORAGE);

            // Manual Admin Logic
            PersonAPI chaosAdmin = Global.getFactory().createPerson();
            chaosAdmin.setAICoreId(Commodities.ALPHA_CORE);
            chaosAdmin.setFaction(Factions.PIRATES); 
            chaosAdmin.setPostId(Ranks.POST_ADMINISTRATOR);
            chaosAdmin.setRankId(Ranks.SPACE_COMMANDER);
            chaosAdmin.setPortraitSprite("graphics/portraits/portrait_pirate01.png");
            chaosAdmin.getStats().setLevel(7); 
            chaosAdmin.getStats().setSkillLevel(Skills.HYPERCOGNITION, 1);       
            chaosAdmin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1); 

            marketChaosPirate.setAdmin(chaosAdmin);
            marketChaosPirate.getCommDirectory().addPerson(chaosAdmin); 
            
            Global.getSector().getEconomy().addMarket(marketChaosPirate, true);
            generateSystemContacts(marketChaosPirate);
            
            // HARDCODED TARIFF (18%)
            marketChaosPirate.getTariff().setBaseValue(0.18f);

            for (MarketConditionAPI condition : marketChaosPirate.getConditions()) condition.setSurveyed(true);
            ChaosStation.setDiscoverable(true);
        }

        // --------------------------------------------------------
        // BIDEN
        // --------------------------------------------------------
        if (Biden != null) {
            MarketAPI marketBiden = Biden.getMarket();
            if (marketBiden != null) {
                marketBiden.setFactionId("luddic_path");
                marketBiden.setSize(4);
                marketBiden.setPlanetConditionMarketOnly(false);
                marketBiden.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

                marketBiden.addIndustry(Industries.SPACEPORT);
                marketBiden.addIndustry(Industries.POPULATION);
                marketBiden.addIndustry(Industries.TECHMINING);

                marketBiden.addSubmarket(Submarkets.SUBMARKET_OPEN);
                marketBiden.addSubmarket(Submarkets.SUBMARKET_BLACK);
                marketBiden.addSubmarket(Submarkets.SUBMARKET_STORAGE);

                Global.getSector().getEconomy().addMarket(marketBiden, true);
                generateSystemContacts(marketBiden);
                
                // HARDCODED TARIFF (18%)
                marketBiden.getTariff().setBaseValue(0.18f);

                Biden.setFaction("luddic_path");
                for (MarketConditionAPI condition : marketBiden.getConditions()) condition.setSurveyed(true);
                Biden.setDiscoverable(true);
            }
        }
        // --------------------------------------------------------
        // AYA
        // --------------------------------------------------------
        if (Aya != null) {
            MarketAPI marketAya = Aya.getMarket();
            if (marketAya != null) {
                marketAya.setFactionId(Factions.PIRATES);
                marketAya.setSize(3);
                marketAya.setPlanetConditionMarketOnly(false);
                marketAya.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

                marketAya.addIndustry(Industries.SPACEPORT);
                marketAya.addIndustry(Industries.POPULATION);
                marketAya.addIndustry(Industries.PATROLHQ);
                marketAya.addIndustry(Industries.LIGHTINDUSTRY);

                marketAya.addSubmarket(Submarkets.SUBMARKET_OPEN);
                marketAya.addSubmarket(Submarkets.SUBMARKET_BLACK);
                marketAya.addSubmarket(Submarkets.SUBMARKET_STORAGE);

                marketAya.getIndustry(Industries.PATROLHQ).setAICoreId(Commodities.BETA_CORE);
                marketAya.getIndustry(Industries.LIGHTINDUSTRY).setAICoreId(Commodities.BETA_CORE);
                marketAya.getIndustry(Industries.SPACEPORT).setAICoreId(Commodities.GAMMA_CORE);

                Global.getSector().getEconomy().addMarket(marketAya, true);
                generateSystemContacts(marketAya);
                
                // HARDCODED TARIFF (18%)
                marketAya.getTariff().setBaseValue(0.18f);

                Aya.setFaction("pirates");
                for (MarketConditionAPI condition : marketAya.getConditions()) condition.setSurveyed(true);
                Aya.setDiscoverable(true);
            }
        }
        
        // --------------------------------------------------------
        // FARFAROUT
        // --------------------------------------------------------
        if (Farfarout != null) {
            MarketAPI marketFarfarout = Farfarout.getMarket();
            if (marketFarfarout != null) {
                marketFarfarout.setFactionId(Factions.PIRATES);
                marketFarfarout.setSize(3);
                marketFarfarout.setPlanetConditionMarketOnly(false);
                marketFarfarout.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

                marketFarfarout.addIndustry(Industries.SPACEPORT);
                marketFarfarout.addIndustry(Industries.POPULATION);
                marketFarfarout.addIndustry(Industries.WAYSTATION);

                marketFarfarout.addSubmarket(Submarkets.SUBMARKET_OPEN);
                marketFarfarout.addSubmarket(Submarkets.SUBMARKET_BLACK);
                marketFarfarout.addSubmarket(Submarkets.SUBMARKET_STORAGE);

                marketFarfarout.getIndustry(Industries.WAYSTATION).setAICoreId(Commodities.GAMMA_CORE);
                marketFarfarout.getIndustry(Industries.SPACEPORT).setAICoreId(Commodities.GAMMA_CORE);

                Global.getSector().getEconomy().addMarket(marketFarfarout, true);
                generateSystemContacts(marketFarfarout);
                
                // HARDCODED TARIFF (18%)
                marketFarfarout.getTariff().setBaseValue(0.18f);

                Farfarout.setFaction("pirates");
                for (MarketConditionAPI condition : marketFarfarout.getConditions()) condition.setSurveyed(true);
                Farfarout.setDiscoverable(true);
            }
        }
        // --------------------------------------------------------
        // CLETE
        // --------------------------------------------------------
        // --------------------------------------------------------
        // CLETE
        // --------------------------------------------------------
        if (Clete != null) {
            MarketAPI marketClete = Clete.getMarket();
            if (marketClete != null) {
                marketClete.setFactionId(Factions.TRITACHYON);
                marketClete.setSize(3); 
                marketClete.setPlanetConditionMarketOnly(false);
                marketClete.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

                marketClete.removeCondition("sol_porus");
                marketClete.addCondition("very_cold");
                marketClete.addCondition("low_gravity");
                marketClete.addCondition("no_atmosphere");
                marketClete.addCondition("volatiles_plentiful");
                marketClete.addCondition("ore_rich");
                marketClete.addCondition("ruins_widespread");
                marketClete.addCondition("rare_ore_sparse");
                marketClete.addCondition("dark");
                marketClete.addCondition("sol_dist_abyssal");

                marketClete.addIndustry(Industries.SPACEPORT);
                marketClete.addIndustry(Industries.POPULATION);
                marketClete.addIndustry(Industries.TECHMINING);

                marketClete.getIndustry(Industries.POPULATION).setAICoreId(Commodities.GAMMA_CORE);
                marketClete.getIndustry(Industries.SPACEPORT).setAICoreId(Commodities.GAMMA_CORE);
                marketClete.getIndustry(Industries.TECHMINING).setAICoreId(Commodities.BETA_CORE);

                marketClete.addSubmarket(Submarkets.SUBMARKET_OPEN);
                marketClete.addSubmarket(Submarkets.SUBMARKET_BLACK);
                marketClete.addSubmarket(Submarkets.SUBMARKET_STORAGE);

                Global.getSector().getEconomy().addMarket(marketClete, true);
                generateSystemContacts(marketClete);
                
                // HARDCODED TARIFF (18%)
                marketClete.getTariff().setBaseValue(0.18f);

                Clete.setFaction(Factions.TRITACHYON);
                for (MarketConditionAPI condition : marketClete.getConditions()) condition.setSurveyed(true);
                Clete.setDiscoverable(true);
            }
        }
    }

    private void generateSystemContacts(MarketAPI market) {
        if (market == null) return;
        
        FactionAPI faction = market.getFaction();

        // 1. ADMINISTRATOR
        PersonAPI currentAdmin = market.getAdmin();
        boolean isAICore = (currentAdmin != null && currentAdmin.isAICore());

        if (!isAICore) {
            PersonAPI admin = faction.createRandomPerson(); 
            admin.setPostId(Ranks.POST_ADMINISTRATOR);
            admin.setRankId(Ranks.CITIZEN); 
            
            market.setAdmin(admin);
            market.getCommDirectory().addPerson(admin);
        }

        // 2. BASE COMMANDER
        if (market.hasIndustry(Industries.MILITARYBASE) || market.hasIndustry(Industries.HIGHCOMMAND)) {
            PersonAPI commander = faction.createRandomPerson();
            commander.setPostId(Ranks.POST_BASE_COMMANDER);
            commander.setRankId(Ranks.SPACE_COMMANDER); 
            
            market.getCommDirectory().addPerson(commander);
            market.getCommDirectory().getEntryForPerson(commander).setHidden(false);
        }

        // 3. STATION COMMANDER
        if (market.hasIndustry(Industries.ORBITALSTATION) || 
            market.hasIndustry(Industries.BATTLESTATION) || 
            market.hasIndustry(Industries.STARFORTRESS)) {
            
            PersonAPI stationCmd = faction.createRandomPerson();
            stationCmd.setPostId(Ranks.POST_STATION_COMMANDER);
            stationCmd.setRankId(Ranks.SPACE_CAPTAIN);
            
            market.getCommDirectory().addPerson(stationCmd);
        }

        // 4. PORTMASTER
        if (market.hasSpaceport()) {
            PersonAPI portmaster = faction.createRandomPerson();
            portmaster.setPostId(Ranks.POST_PORTMASTER); 
            portmaster.setRankId(Ranks.SPACE_CAPTAIN);
            
            market.getCommDirectory().addPerson(portmaster);
        }

        // 5. QUARTERMASTER
        if (market.hasSpaceport()) {
            PersonAPI quartermaster = faction.createRandomPerson();
            quartermaster.setPostId(Ranks.POST_SUPPLY_OFFICER);
            quartermaster.setRankId(Ranks.SPACE_COMMANDER);
            
            market.getCommDirectory().addPerson(quartermaster);
        }
    }
}