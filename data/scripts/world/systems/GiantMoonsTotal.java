package data.scripts.world.systems;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.Global;

// More cause I can and wanted to see what it looked like than anything productive

public class GiantMoonsTotal {

    private static final float MASS_JUPITER = 0.0009546f;
    private static final float MASS_SATURN  = 0.0002857f;
    private static final String CFG = "data/config/sol_settings.json";

    public void spawn(StarSystemAPI system, SectorEntityToken primary, String giantName, float angleOffset) {
        AstroCalc calc = new AstroCalc();

        int showNamesSetting = optInt("Show_Names", 1);
        String showNameMinor;
        String showNameProv;
        if (showNamesSetting == 2)        { showNameMinor = null;      showNameProv = null;      }
        else if (showNamesSetting == 0)   { showNameMinor = "no_name"; showNameProv = "no_name"; }
        else                              { showNameMinor = null;      showNameProv = "no_name"; }

        String key = (giantName == null) ? "" : giantName.toLowerCase();
        if (key.equals("jupiter")) {
            spawnJupiter(system, primary, angleOffset, calc, showNameMinor, showNameProv);
        } else if (key.equals("saturn")) {
            spawnSaturn(system, primary, angleOffset, calc, showNameMinor, showNameProv);
        }
    }

    private void spawnJupiter(StarSystemAPI system, SectorEntityToken Jupiter, float angleOffset,
                              AstroCalc calc, String showNameMinor, String showNameProv) {

        // Callirrhoe
        SectorEntityToken Callirrhoe = calc.spawnIrregularBody2(system, Jupiter, "Callirrhoe", "Callirrhoe", "moon", showNameMinor, 9.6f, 0.159022f, 0.29f, 117.9f, 13.5f, 2001.586f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Magaclite
        SectorEntityToken Magaclite = calc.spawnIrregularBody2(system, Jupiter, "Magaclite", "Magaclite", "moon", showNameMinor, 6.0f, 0.158024f, 0.421f, 116.1f, 309.9f, 2001.651f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Taygete
        SectorEntityToken Taygete = calc.spawnIrregularBody2(system, Jupiter, "Taygete", "Taygete", "moon", showNameMinor, 5.0f, 0.154437f, 0.257f, 116.5f, 207.7f, 2001.642f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Chaldene
        SectorEntityToken Chaldene = calc.spawnIrregularBody2(system, Jupiter, "Chaldene", "Chaldene", "moon", showNameMinor, 4.0f, 0.153253f, 0.261f, 275.3f, 231.6f, 2000.726f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Harpalyke
        SectorEntityToken Harpalyke = calc.spawnIrregularBody2(system, Jupiter, "Harpalyke", "Harpalyke", "moon", showNameMinor, 4.0f, 0.139624f, 0.239f, 255.4f, 104.3f, 2000.167f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Kalyke
        SectorEntityToken Kalyke = calc.spawnIrregularBody2(system, Jupiter, "Kalyke", "Kalyke", "moon", showNameMinor, 6.9f, 0.155738f, 0.261f, 224.9f, 240.5f, 2000.208f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Iocaste
        SectorEntityToken Iocaste = calc.spawnIrregularBody2(system, Jupiter, "Iocaste", "Iocaste", "moon", showNameMinor, 5.0f, 0.140793f, 0.223f, 182.0f, 91.4f, 2001.277f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Erinome
        SectorEntityToken Erinome = calc.spawnIrregularBody2(system, Jupiter, "Erinome", "Erinome", "moon", showNameMinor, 3.0f, 0.153927f, 0.272f, 273.1f, 359.9f, 2001.69f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Isonoe
        SectorEntityToken Isonoe = calc.spawnIrregularBody2(system, Jupiter, "Isonoe", "Isonoe", "moon", showNameMinor, 4.0f, 0.153587f, 0.249f, 116.3f, 131.0f, 2000.726f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Praxidike
        SectorEntityToken Praxidike = calc.spawnIrregularBody2(system, Jupiter, "Praxidike", "Praxidike", "moon", showNameMinor, 7.0f, 0.139916f, 0.245f, 128.0f, 182.5f, 2001.324f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Autonoe
        SectorEntityToken Autonoe = calc.spawnIrregularBody2(system, Jupiter, "Autonoe", "Autonoe", "moon", showNameMinor, 4.0f, 0.158994f, 0.326f, 134.0f, 61.2f, 2001.51f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Thyone
        SectorEntityToken Thyone = calc.spawnIrregularBody2(system, Jupiter, "Thyone", "Thyone", "moon", showNameMinor, 4.0f, 0.140194f, 0.235f, 242.5f, 105.1f, 2001.136f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Hermippe
        SectorEntityToken Hermippe = calc.spawnIrregularBody2(system, Jupiter, "Hermippe", "Hermippe", "moon", showNameMinor, 4.0f, 0.141069f, 0.22f, 158.6f, 277.2f, 2001.573f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Aitne
        SectorEntityToken Aitne = calc.spawnIrregularBody2(system, Jupiter, "Aitne", "Aitne", "moon", showNameMinor, 3.0f, 0.154143f, 0.273f, 120.6f, 72.2f, 2001.95f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Eurydome
        SectorEntityToken Eurydome = calc.spawnIrregularBody2(system, Jupiter, "Eurydome", "Eurydome", "moon", showNameMinor, 3.0f, 0.15304f, 0.287f, 281.8f, 223.4f, 2001.603f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Euanthe
        SectorEntityToken Euanthe = calc.spawnIrregularBody2(system, Jupiter, "Euanthe", "Euanthe", "moon", showNameMinor, 3.0f, 0.139192f, 0.243f, 353.5f, 312.4f, 2001.238f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Euporie
        SectorEntityToken Euporie = calc.spawnIrregularBody2(system, Jupiter, "Euporie", "Euporie", "moon", showNameMinor, 2.0f, 0.128758f, 0.148f, 56.4f, 109.5f, 2000.263f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Orthosie
        SectorEntityToken Orthosie = calc.spawnIrregularBody2(system, Jupiter, "Orthosie", "Orthosie", "moon", showNameMinor, 2.0f, 0.139693f, 0.294f, 161.1f, 244.7f, 2000.997f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Sponde
        SectorEntityToken Sponde = calc.spawnIrregularBody2(system, Jupiter, "Sponde", "Sponde", "moon", showNameMinor, 2.0f, 0.157346f, 0.323f, 177.4f, 56.6f, 2000.613f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Kale
        SectorEntityToken Kale = calc.spawnIrregularBody2(system, Jupiter, "Kale", "Kale", "moon", showNameMinor, 2.0f, 0.154065f, 0.262f, 175.9f, 69.2f, 2000.308f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Pasithee
        SectorEntityToken Pasithee = calc.spawnIrregularBody2(system, Jupiter, "Pasithee", "Pasithee", "moon", showNameMinor, 2.0f, 0.152681f, 0.274f, 237.8f, 207.9f, 2001.706f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Hegemone
        SectorEntityToken Hegemone = calc.spawnIrregularBody2(system, Jupiter, "Hegemone", "Hegemone", "moon", showNameMinor, 3.0f, 0.156036f, 0.357f, 241.1f, 192.2f, 2001.726f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Mneme
        SectorEntityToken Mneme = calc.spawnIrregularBody2(system, Jupiter, "Mneme", "Mneme", "moon", showNameMinor, 2.0f, 0.139145f, 0.24f, 236.1f, 50.1f, 2000.004f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Aoede
        SectorEntityToken Aoede = calc.spawnIrregularBody2(system, Jupiter, "Aoede", "Aoede", "moon", showNameMinor, 4.0f, 0.158913f, 0.437f, 207.6f, 43.1f, 2000.892f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Thelxinoe
        SectorEntityToken Thelxinoe = calc.spawnIrregularBody2(system, Jupiter, "Thelxinoe", "Thelxinoe", "moon", showNameMinor, 2.0f, 0.140191f, 0.229f, 275.1f, 307.0f, 2000.825f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Arche
        SectorEntityToken Arche = calc.spawnIrregularBody2(system, Jupiter, "Arche", "Arche", "moon", showNameMinor, 3.0f, 0.154369f, 0.263f, 34.0f, 172.5f, 2001.818f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Kallichore
        SectorEntityToken Kallichore = calc.spawnIrregularBody2(system, Jupiter, "Kallichore", "Kallichore", "moon", showNameMinor, 2.0f, 0.15386f, 0.253f, 66.8f, 352.6f, 2000.13f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Helike
        SectorEntityToken Helike = calc.spawnIrregularBody2(system, Jupiter, "Helike", "Helike", "moon", showNameMinor, 4.0f, 0.139784f, 0.155f, 45.8f, 305.1f, 2000.445f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Eukelade
        SectorEntityToken Eukelade = calc.spawnIrregularBody2(system, Jupiter, "Eukelade", "Eukelade", "moon", showNameMinor, 4.0f, 0.154163f, 0.274f, 234.2f, 290.1f, 2001.091f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Cyllene
        SectorEntityToken Cyllene = calc.spawnIrregularBody2(system, Jupiter, "Cyllene", "Cyllene", "moon", showNameMinor, 2.0f, 0.15809f, 0.421f, 153.3f, 174.6f, 2001.433f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Kore
        SectorEntityToken Kore = calc.spawnIrregularBody2(system, Jupiter, "Kore", "Kore", "moon", showNameMinor, 2.0f, 0.161789f, 0.338f, 40.6f, 135.3f, 2001.897f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Herse
        SectorEntityToken Herse = calc.spawnIrregularBody2(system, Jupiter, "Herse", "Herse", "moon", showNameMinor, 2.0f, 0.154726f, 0.258f, 123.1f, 344.1f, 2001.639f, angleOffset, null, MASS_JUPITER, "Jupiter", true);

        // Dia
        SectorEntityToken Dia = calc.spawnIrregularBody2(system, Jupiter, "Dia", "Dia", "moon", showNameMinor, 4.0f, 0.081939f, 0.232f, 320.9f, 164.1f, 1999.38f, angleOffset, null, MASS_JUPITER, "Jupiter", false);
        // Eirene
        SectorEntityToken Eirene = calc.spawnIrregularBody2(system, Jupiter, "Eirene", "Eirene", "moon", showNameMinor, 4.0f, 0.154088f, 0.263f, 336.7f, 86.4f, 2000.979f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Philophrosyn
        SectorEntityToken Philophrosyn = calc.spawnIrregularBody2(system, Jupiter, "Philophrosyn", "Philophrosyn", "moon", showNameMinor, 2.0f, 0.151073f, 0.221f, 88.9f, 0.9f, 2001.237f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Eupheme
        SectorEntityToken Eupheme = calc.spawnIrregularBody2(system, Jupiter, "Eupheme", "Eupheme", "moon", showNameMinor, 2.0f, 0.138795f, 0.234f, 339.8f, 73.6f, 2001.058f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // Valetudo
        SectorEntityToken Valetudo = calc.spawnIrregularBody2(system, Jupiter, "Valetudo", "Valetudo", "moon", showNameMinor, 1.0f, 0.124936f, 0.217f, 90.5f, 316.3f, 1998.844f, angleOffset, null, MASS_JUPITER, "Jupiter", false);
        // Pandia
        SectorEntityToken Pandia = calc.spawnIrregularBody2(system, Jupiter, "Pandia", "Pandia", "moon", showNameMinor, 3.0f, 0.076736f, 0.178f, 158.5f, 188.7f, 1999.523f, angleOffset, null, MASS_JUPITER, "Jupiter", false);
        // Ersa
        SectorEntityToken Ersa = calc.spawnIrregularBody2(system, Jupiter, "Ersa", "Ersa", "moon", showNameMinor, 3.0f, 0.0762f, 0.117f, 270.8f, 300.7f, 1999.785f, angleOffset, null, MASS_JUPITER, "Jupiter", false);

        // S/2010 J 1
        SectorEntityToken S2010_J_1 = calc.spawnIrregularBody2(system, Jupiter, "S2010_J_1", "S/2010 J 1"
, "moon", showNameProv, 2.0f, 0.154986f, 0.256f, 183.4f, 175.6f, 2001.562f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2010 J 2
        SectorEntityToken S2010_J_2 = calc.spawnIrregularBody2(system, Jupiter, "S2010_J_2", "S/2010 J 2"
, "moon", showNameProv, 1.0f, 0.138952f, 0.244f, 292.5f, 28.8f, 2001.664f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2010 J 3
        SectorEntityToken S2010_J_3 = calc.spawnIrregularBody2(system, Jupiter, "S2010_J_3", "S/2010 J 3"
, "moon", showNameProv, 1.0f, 0.159514f, 0.313f, 40.1f, 30.2f, 2001.79f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2010 J 4
        SectorEntityToken S2010_J_4 = calc.spawnIrregularBody2(system, Jupiter, "S2010_J_4", "S/2010 J 4"
, "moon", showNameProv, 1.0f, 0.152364f, 0.278f, 80.4f, 161.9f, 2000.789f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2010 J 5
        SectorEntityToken S2010_J_5 = calc.spawnIrregularBody2(system, Jupiter, "S2010_J_5", "S/2010 J 5"
, "moon", showNameProv, 1.0f, 0.157629f, 0.257f, 233.7f, 270.4f, 2001.742f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2010 J 6
        SectorEntityToken S2010_J_6 = calc.spawnIrregularBody2(system, Jupiter, "S2010_J_6", "S/2010 J 6"
, "moon", showNameProv, 2.0f, 0.14365f, 0.297f, 87.8f, 73.2f, 2001.366f, angleOffset, null, MASS_JUPITER, "Jupiter", true);

        // S/2011 J 1
        SectorEntityToken S2011_J_1 = calc.spawnIrregularBody2(system, Jupiter, "S2011_J_1", "S/2011 J 1"
, "moon", showNameProv, 2.0f, 0.154553f, 0.269f, 225.4f, 50.4f, 2001.385f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2011 J 2
        SectorEntityToken S2011_J_2 = calc.spawnIrregularBody2(system, Jupiter, "S2011_J_2", "S/2011 J 2"
, "moon", showNameProv, 1.0f, 0.1531f, 0.358f, 297.6f, 262.9f, 2000.134f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2011 J 3
        SectorEntityToken S2011_J_3 = calc.spawnIrregularBody2(system, Jupiter, "S2011_J_3", "S/2011 J 3"
, "moon", showNameProv, 3.0f, 0.078322f, 0.192f, 359.4f, 134.7f, 1999.757f, angleOffset, null, MASS_JUPITER, "Jupiter", false);
        // S/2011 J 4
        SectorEntityToken S2011_J_4 = calc.spawnIrregularBody2(system, Jupiter, "S2011_J_4", "S/2011 J 4"
, "moon", showNameProv, 3.0f, 0.07423f, 0.128f, 314.6f, 46.2f, 1999.378f, angleOffset, null, MASS_JUPITER, "Jupiter", false);
        // S/2011 J 5
        SectorEntityToken S2011_J_5 = calc.spawnIrregularBody2(system, Jupiter, "S2011_J_5", "S/2011 J 5"
, "moon", showNameProv, 2.0f, 0.157274f, 0.251f, 66.6f, 90.5f, 2000.245f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2011 J 6
        SectorEntityToken S2011_J_6 = calc.spawnIrregularBody2(system, Jupiter, "S2011_J_6", "S/2011 J 6"
, "moon", showNameProv, 1.0f, 0.155341f, 0.261f, 54.2f, 211.5f, 2001.71f, angleOffset, null, MASS_JUPITER, "Jupiter", true);

        // S/2003 J 2
        SectorEntityToken S2003_J_2  = calc.spawnIrregularBody2(system, Jupiter, "S2003_J_2",  "S/2003 J 2"
,  "moon", showNameProv, 2.0f, 0.140329f, 0.225f, 42.3f,  170.6f, 2001.647f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2003 J 4
        SectorEntityToken S2003_J_4  = calc.spawnIrregularBody2(system, Jupiter, "S2003_J_4",  "S/2003 J 4"
,  "moon", showNameProv, 2.0f, 0.153226f, 0.327f, 216.5f, 208.8f, 2000.946f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2003 J 9
        SectorEntityToken S2003_J_9  = calc.spawnIrregularBody2(system, Jupiter, "S2003_J_9",  "S/2003 J 9"
,  "moon", showNameProv, 1.0f, 0.15505f,  0.268f, 359.8f, 288.4f, 2000.254f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2003 J 10
        SectorEntityToken S2003_J_10 = calc.spawnIrregularBody2(system, Jupiter, "S2003_J_10", "S/2003 J 10"
, "moon", showNameProv, 2.0f, 0.156315f, 0.257f, 272.8f, 187.6f, 2000.865f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2003 J 12
        SectorEntityToken S2003_J_12 = calc.spawnIrregularBody2(system, Jupiter, "S2003_J_12", "S/2003 J 12"
, "moon", showNameProv, 1.0f, 0.140104f, 0.235f, 160.7f, 66.1f,  2000.255f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2003 J 16
        SectorEntityToken S2003_J_16 = calc.spawnIrregularBody2(system, Jupiter, "S2003_J_16", "S/2003 J 16"
, "moon", showNameProv, 2.0f, 0.139557f, 0.238f, 298.2f, 55.6f,  2000.025f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2003 J 18
        SectorEntityToken S2003_J_18 = calc.spawnIrregularBody2(system, Jupiter, "S2003_J_18", "S/2003 J 18"
, "moon", showNameProv, 2.0f, 0.135916f, 0.102f, 252.5f, 86.2f,  2000.751f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2003 J 19
        SectorEntityToken S2003_J_19 = calc.spawnIrregularBody2(system, Jupiter, "S2003_J_19", "S/2003 J 19"
, "moon", showNameProv, 2.0f, 0.154769f, 0.264f, 196.9f, 186.8f, 2000.12f,  angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2003 J 23
        SectorEntityToken S2003_J_23 = calc.spawnIrregularBody2(system, Jupiter, "S2003_J_23", "S/2003 J 23"
, "moon", showNameProv, 2.0f, 0.159254f, 0.306f, 140.6f, 256.6f, 2000.177f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2003 J 24
        SectorEntityToken S2003_J_24 = calc.spawnIrregularBody2(system, Jupiter, "S2003_J_24", "S/2003 J 24"
, "moon", showNameProv, 2.0f, 0.152959f, 0.263f, 121.6f, 84.1f,  2001.123f, angleOffset, null, MASS_JUPITER, "Jupiter", true);

        // S/2016 J 1
        SectorEntityToken S2016_J_1 = calc.spawnIrregularBody2(system, Jupiter, "S2016_J_1", "S/2016 J 1"
, "moon", showNameProv, 1.0f, 0.139017f, 0.245f, 215.3f, 298.5f, 2001.154f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2016 J 3
        SectorEntityToken S2016_J_3 = calc.spawnIrregularBody2(system, Jupiter, "S2016_J_3", "S/2016 J 3"
, "moon", showNameProv, 2.0f, 0.151869f, 0.251f, 59.1f,  94.7f,  2000.196f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2016 J 4
        SectorEntityToken S2016_J_4 = calc.spawnIrregularBody2(system, Jupiter, "S2016_J_4", "S/2016 J 4"
, "moon", showNameProv, 1.0f, 0.154507f, 0.294f, 217.5f, 249.4f, 2001.11f,  angleOffset, null, MASS_JUPITER, "Jupiter", true);

        // S/2017 J 1
        SectorEntityToken S2017_J_1  = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_1",  "S/2017 J 1"
,  "moon", showNameProv, 2.0f, 0.158689f, 0.321f, 250.1f, 74.7f,  2001.431f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2017 J 2
        SectorEntityToken S2017_J_2  = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_2",  "S/2017 J 2"
,  "moon", showNameProv, 2.0f, 0.153409f, 0.27f,  278.2f, 153.0f, 2001.942f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2017 J 3
        SectorEntityToken S2017_J_3  = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_3",  "S/2017 J 3"
,  "moon", showNameProv, 2.0f, 0.139952f, 0.238f, 235.4f, 105.7f, 2000.131f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2017 J 5
        SectorEntityToken S2017_J_5  = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_5",  "S/2017 J 5"
,  "moon", showNameProv, 2.0f, 0.155096f, 0.261f, 69.3f,  292.5f, 2000.232f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2017 J 6
        SectorEntityToken S2017_J_6  = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_6",  "S/2017 J 6"
,  "moon", showNameProv, 2.0f, 0.155425f, 0.333f, 146.0f, 15.5f,  2001.723f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2017 J 7
        SectorEntityToken S2017_J_7  = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_7",  "S/2017 J 7"
,  "moon", showNameProv, 2.0f, 0.140112f, 0.235f, 353.0f, 288.7f, 2001.249f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2017 J 8
        SectorEntityToken S2017_J_8  = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_8",  "S/2017 J 8"
,  "moon", showNameProv, 1.0f, 0.15254f,  0.259f, 349.4f, 330.5f, 2000.521f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2017 J 9
        SectorEntityToken S2017_J_9  = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_9",  "S/2017 J 9"
,  "moon", showNameProv, 3.0f, 0.145485f, 0.197f, 243.3f, 276.4f, 2001.224f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2017 J 10
        SectorEntityToken S2017_J_10 = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_10", "S/2017 J 10"
, "moon", showNameProv, 1.0f, 0.140883f, 0.209f, 157.7f, 195.3f, 2001.567f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2017 J 11
        SectorEntityToken S2017_J_11 = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_11", "S/2017 J 11"
, "moon", showNameProv, 1.0f, 0.153687f, 0.268f, 236.9f, 25.5f,  2000.731f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2017 J 12
        SectorEntityToken S2017_J_12 = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_12", "S/2017 J 12"
, "moon", showNameProv, 1.0f, 0.155554f, 0.257f, 273.2f, 330.7f, 2001.306f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2017 J 13
        SectorEntityToken S2017_J_13 = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_13", "S/2017 J 13"
, "moon", showNameProv, 1.0f, 0.152694f, 0.277f, 26.8f,  338.2f, 2000.622f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2017 J 14
        SectorEntityToken S2017_J_14 = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_14", "S/2017 J 14"
, "moon", showNameProv, 1.0f, 0.156503f, 0.436f, 100.3f, 150.2f, 2001.638f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2017 J 15
        SectorEntityToken S2017_J_15 = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_15", "S/2017 J 15"
, "moon", showNameProv, 2.0f, 0.154884f, 0.232f, 324.6f, 58.1f,  2001.667f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2017 J 16
        SectorEntityToken S2017_J_16 = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_16", "S/2017 J 16"
, "moon", showNameProv, 1.0f, 0.153798f, 0.268f, 148.8f, 178.6f, 2000.025f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2017 J 17
        SectorEntityToken S2017_J_17 = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_17", "S/2017 J 17"
, "moon", showNameProv, 1.0f, 0.078718f, 0.164f, 244.5f, 95.0f,  1999.36f,  angleOffset, null, MASS_JUPITER, "Jupiter", false);
        // S/2017 J 18
        SectorEntityToken S2017_J_18 = calc.spawnIrregularBody2(system, Jupiter, "S2017_J_18", "S/2017 J 18"
, "moon", showNameProv, 2.0f, 0.153236f, 0.254f, 40.6f,  238.8f, 2000.215f, angleOffset, null, MASS_JUPITER, "Jupiter", true);

        // S/2018 J 2
        SectorEntityToken S2018_J_2 = calc.spawnIrregularBody2(system, Jupiter, "S2018_J_2", "S/2018 J 2"
, "moon", showNameProv, 3.0f, 0.076336f, 0.152f, 115.5f, 288.6f, 1999.826f, angleOffset, null, MASS_JUPITER, "Jupiter", false);
        // S/2018 J 3
        SectorEntityToken S2018_J_3 = calc.spawnIrregularBody2(system, Jupiter, "S2018_J_3", "S/2018 J 3"
, "moon", showNameProv, 1.0f, 0.156421f, 0.268f, 279.0f, 177.5f, 2000.755f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2018 J 4
        SectorEntityToken S2018_J_4 = calc.spawnIrregularBody2(system, Jupiter, "S2018_J_4", "S/2018 J 4"
, "moon", showNameProv, 2.0f, 0.109149f, 0.177f, 138.3f, 285.0f, 1999.831f, angleOffset, null, MASS_JUPITER, "Jupiter", false);
        // S/2018 J 5
        SectorEntityToken S2018_J_5 = calc.spawnIrregularBody2(system, Jupiter, "S2018_J_5", "S/2018 J 5"
, "moon", showNameProv, 2.0f, 0.15555f,  0.261f, 82.9f,  292.4f, 2001.045f, angleOffset, null, MASS_JUPITER, "Jupiter", true);

        // S/2021 J 1
        SectorEntityToken S2021_J_1 = calc.spawnIrregularBody2(system, Jupiter, "S2021_J_1", "S/2021 J 1"
, "moon", showNameProv, 1.0f, 0.140074f, 0.228f, 129.1f, 20.6f,  2001.136f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2021 J 2
        SectorEntityToken S2021_J_2 = calc.spawnIrregularBody2(system, Jupiter, "S2021_J_2", "S/2021 J 2"
, "moon", showNameProv, 1.0f, 0.139886f, 0.242f, 110.8f, 25.9f,  2001.268f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2021 J 3
        SectorEntityToken S2021_J_3 = calc.spawnIrregularBody2(system, Jupiter, "S2021_J_3", "S/2021 J 3"
, "moon", showNameProv, 2.0f, 0.138883f, 0.239f, 167.9f, 218.6f, 2000.659f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2021 J 4
        SectorEntityToken S2021_J_4 = calc.spawnIrregularBody2(system, Jupiter, "S2021_J_4", "S/2021 J 4"
, "moon", showNameProv, 1.0f, 0.153877f, 0.265f, 218.9f, 140.5f, 2000.705f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2021 J 5
        SectorEntityToken S2021_J_5 = calc.spawnIrregularBody2(system, Jupiter, "S2021_J_5", "S/2021 J 5"
, "moon", showNameProv, 2.0f, 0.156517f, 0.272f, 236.5f, 273.4f, 2000.86f,  angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2021 J 6
        SectorEntityToken S2021_J_6 = calc.spawnIrregularBody2(system, Jupiter, "S2021_J_6", "S/2021 J 6"
, "moon", showNameProv, 1.0f, 0.152879f, 0.271f, 208.5f, 146.2f, 2000.469f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2021 J 7
        SectorEntityToken S2021_J_7 = calc.spawnIrregularBody2(system, Jupiter, "S2021_J_7", "S/2021 J 7"
, "moon", showNameProv, 1.0f, 0.15579f,  0.253f, 293.4f, 13.3f,  2001.584f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2021 J 8
        SectorEntityToken S2021_J_8 = calc.spawnIrregularBody2(system, Jupiter, "S2021_J_8", "S/2021 J 8"
, "moon", showNameProv, 1.0f, 0.140235f, 0.243f, 85.0f,  34.6f,  2000.623f, angleOffset, null, MASS_JUPITER, "Jupiter", true);

        // S/2022 J 1
        SectorEntityToken S2022_J_1 = calc.spawnIrregularBody2(system, Jupiter, "S2022_J_1", "S/2022 J 1"
, "moon", showNameProv, 1.0f, 0.152039f, 0.257f, 42.2f,  232.4f, 2001.657f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2022 J 2
        SectorEntityToken S2022_J_2 = calc.spawnIrregularBody2(system, Jupiter, "S2022_J_2", "S/2022 J 2"
, "moon", showNameProv, 1.0f, 0.154236f, 0.263f, 98.9f,  357.0f, 2000.26f,  angleOffset, null, MASS_JUPITER, "Jupiter", true);
        // S/2022 J 3
        SectorEntityToken S2022_J_3 = calc.spawnIrregularBody2(system, Jupiter, "S2022_J_3", "S/2022 J 3"
, "moon", showNameProv, 1.0f, 0.140477f, 0.248f, 271.7f, 351.6f, 2001.103f, angleOffset, null, MASS_JUPITER, "Jupiter", true);

        // S/2024 J 1
        SectorEntityToken S2024_J_1 = calc.spawnIrregularBody2(system, Jupiter, "S2024_J_1", "S/2024 J 1"
, "moon", showNameProv, 2.0f, 0.156834f, 0.273f, 147.7f, 108.8f, 2001.521f, angleOffset, null, MASS_JUPITER, "Jupiter", true);
    }

    private void spawnSaturn(StarSystemAPI system, SectorEntityToken Saturn, float angleOffset,
                             AstroCalc calc, String showNameMinor, String showNameProv) {

        // Suttungr
        SectorEntityToken Suttungr = calc.spawnIrregularBody2(system, Saturn, "Suttungr", "Suttungr", "moon", showNameMinor, 7.0f, 0.129627f, 0.116f, 171.0f, 65.0f, 2001.941f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Mundilfari
        SectorEntityToken Mundilfari = calc.spawnIrregularBody2(system, Saturn, "Mundilfari", "Mundilfari", "moon", showNameMinor, 7.0f, 0.124254f, 0.211f, 299.0f, 305.4f, 2000.57f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Erriapus
        SectorEntityToken Erriapus = calc.spawnIrregularBody2(system, Saturn, "Erriapus", "Erriapus", "moon", showNameMinor, 10.0f, 0.117026f, 0.475f, 14.3f, 279.8f, 1999.038f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // Thrymr
        SectorEntityToken Thrymr = calc.spawnIrregularBody2(system, Saturn, "Thrymr", "Thrymr", "moon", showNameMinor, 8.0f, 0.135901f, 0.467f, 216.2f, 92.2f, 2002.035f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Narvi
        SectorEntityToken Narvi = calc.spawnIrregularBody2(system, Saturn, "Narvi", "Narvi", "moon", showNameMinor, 7.0f, 0.128912f, 0.441f, 111.5f, 177.9f, 2001.361f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Aegir
        SectorEntityToken Aegir = calc.spawnIrregularBody2(system, Saturn, "Aegir", "Aegir", "moon", showNameMinor, 6.0f, 0.138133f, 0.255f, 23.5f, 244.7f, 2001.534f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Bergelmir
        SectorEntityToken Bergelmir = calc.spawnIrregularBody2(system, Saturn, "Bergelmir", "Bergelmir", "moon", showNameMinor, 5.0f, 0.128799f, 0.145f, 255.7f, 133.9f, 2001.579f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Farbauti
        SectorEntityToken Farbauti = calc.spawnIrregularBody2(system, Saturn, "Farbauti", "Farbauti", "moon", showNameMinor, 5.0f, 0.135635f, 0.249f, 9.0f, 343.6f, 2001.133f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Fenrir
        SectorEntityToken Fenrir = calc.spawnIrregularBody2(system, Saturn, "Fenrir", "Fenrir", "moon", showNameMinor, 4.0f, 0.149272f, 0.137f, 38.6f, 114.5f, 2002.223f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Hati
        SectorEntityToken Hati = calc.spawnIrregularBody2(system, Saturn, "Hati", "Hati", "moon", showNameMinor, 5.0f, 0.131653f, 0.372f, 270.6f, 10.4f, 2002.483f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Hyrrokkin
        SectorEntityToken Hyrrokkin = calc.spawnIrregularBody2(system, Saturn, "Hyrrokkin", "Hyrrokkin", "moon", showNameMinor, 8.0f, 0.122601f, 0.336f, 214.8f, 264.8f, 2000.284f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Kari
        SectorEntityToken Kari = calc.spawnIrregularBody2(system, Saturn, "Kari", "Kari", "moon", showNameMinor, 6.0f, 0.147275f, 0.469f, 83.1f, 160.2f, 2002.677f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Loge
        SectorEntityToken Loge = calc.spawnIrregularBody2(system, Saturn, "Loge", "Loge", "moon", showNameMinor, 5.0f, 0.153205f, 0.191f, 32.1f, 20.5f, 2003.281f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Skoll
        SectorEntityToken Skoll = calc.spawnIrregularBody2(system, Saturn, "Skoll", "Skoll", "moon", showNameMinor, 5.0f, 0.117805f, 0.463f, 181.2f, 186.1f, 2001.953f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Surtur
        SectorEntityToken Surtur = calc.spawnIrregularBody2(system, Saturn, "Surtur", "Surtur", "moon", showNameMinor, 6.0f, 0.152061f, 0.448f, 9.9f, 314.4f, 2002.487f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Jarnsaxa
        SectorEntityToken Jarnsaxa = calc.spawnIrregularBody2(system, Saturn, "Jarnsaxa", "Jarnsaxa", "moon", showNameMinor, 6.0f, 0.128835f, 0.218f, 175.5f, 229.2f, 2000.07f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Greip
        SectorEntityToken Greip = calc.spawnIrregularBody2(system, Saturn, "Greip", "Greip", "moon", showNameMinor, 5.0f, 0.122863f, 0.317f, 355.9f, 146.2f, 2002.376f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Tarqeq
        SectorEntityToken Tarqeq = calc.spawnIrregularBody2(system, Saturn, "Tarqeq", "Tarqeq", "moon", showNameMinor, 7.0f, 0.118658f, 0.144f, 292.8f, 72.2f, 1999.374f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // Gridr
        SectorEntityToken Gridr = calc.spawnIrregularBody2(system, Saturn, "Gridr", "Gridr", "moon", showNameMinor, 4.0f, 0.128682f, 0.187f, 197.0f, 271.4f, 2002.509f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Angrboda
        SectorEntityToken Angrboda = calc.spawnIrregularBody2(system, Saturn, "Angrboda", "Angrboda", "moon", showNameMinor, 4.0f, 0.137646f, 0.216f, 263.0f, 104.2f, 2002.342f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Skrymir
        SectorEntityToken Skrymir = calc.spawnIrregularBody2(system, Saturn, "Skrymir", "Skrymir", "moon", showNameMinor, 4.0f, 0.143367f, 0.437f, 68.9f, 68.1f, 2001.585f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Gerd
        SectorEntityToken Gerd = calc.spawnIrregularBody2(system, Saturn, "Gerd", "Gerd", "moon", showNameMinor, 4.0f, 0.140025f, 0.517f, 49.5f, 307.2f, 2002.242f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Eggther
        SectorEntityToken Eggther = calc.spawnIrregularBody2(system, Saturn, "Eggther", "Eggther", "moon", showNameMinor, 6.0f, 0.132653f, 0.157f, 111.7f, 130.1f, 2000.671f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Beli
        SectorEntityToken Beli = calc.spawnIrregularBody2(system, Saturn, "Beli", "Beli", "moon", showNameMinor, 4.0f, 0.138396f, 0.087f, 191.7f, 263.5f, 2002.188f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Gunnlod
        SectorEntityToken Gunnlod = calc.spawnIrregularBody2(system, Saturn, "Gunnlod", "Gunnlod", "moon", showNameMinor, 4.0f, 0.141324f, 0.251f, 311.4f, 24.7f, 2002.56f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Thiazzi
        SectorEntityToken Thiazzi = calc.spawnIrregularBody2(system, Saturn, "Thiazzi", "Thiazzi", "moon", showNameMinor, 4.0f, 0.157606f, 0.511f, 334.7f, 303.2f, 2000.752f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Alvaldi
        SectorEntityToken Alvaldi = calc.spawnIrregularBody2(system, Saturn, "Alvaldi", "Alvaldi", "moon", showNameMinor, 6.0f, 0.147019f, 0.238f, 38.3f, 188.1f, 2002.98f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // Geirrod
        SectorEntityToken Geirrod = calc.spawnIrregularBody2(system, Saturn, "Geirrod", "Geirrod", "moon", showNameMinor, 4.0f, 0.148795f, 0.539f, 202.2f, 349.6f, 2001.206f, angleOffset, null, MASS_SATURN, "Saturn", true);

        // S/2004 S 7
        SectorEntityToken S2004_S_7  = calc.spawnIrregularBody2(system, Saturn, "S2004_S_7",  "S/2004 S 7"
,  "moon", showNameProv, 5.0f, 0.142566f, 0.511f, 116.9f, 54.9f,  2002.864f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 12
        SectorEntityToken S2004_S_12 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_12", "S/2004 S 12"
, "moon", showNameProv, 4.0f, 0.132362f, 0.337f, 10.9f,  88.1f,  2002.421f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 13
        SectorEntityToken S2004_S_13 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_13", "S/2004 S 13"
, "moon", showNameProv, 4.0f, 0.123355f, 0.265f, 51.9f,  355.4f, 2001.551f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 17
        SectorEntityToken S2004_S_17 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_17", "S/2004 S 17"
, "moon", showNameProv, 4.0f, 0.13168f,  0.162f, 227.6f, 184.5f, 2000.098f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 21
        SectorEntityToken S2004_S_21 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_21", "S/2004 S 21"
, "moon", showNameProv, 4.0f, 0.154821f, 0.394f, 198.7f, 174.9f, 2001.124f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 24
        SectorEntityToken S2004_S_24 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_24", "S/2004 S 24"
, "moon", showNameProv, 4.0f, 0.156006f, 0.071f, 122.1f, 338.4f, 1996.416f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2004 S 26
        SectorEntityToken S2004_S_26 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_26", "S/2004 S 26"
, "moon", showNameProv, 5.0f, 0.174451f, 0.147f, 193.6f, 148.4f, 2003.937f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 28
        SectorEntityToken S2004_S_28 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_28", "S/2004 S 28"
, "moon", showNameProv, 5.0f, 0.146165f, 0.159f, 95.8f,  191.9f, 2001.182f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 29
        SectorEntityToken S2004_S_29 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_29", "S/2004 S 29"
, "moon", showNameProv, 5.0f, 0.114065f, 0.485f, 244.1f, 309.5f, 1999.001f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2004 S 31
        SectorEntityToken S2004_S_31 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_31", "S/2004 S 31"
, "moon", showNameProv, 5.0f, 0.116961f, 0.159f, 261.8f, 253.9f, 1999.458f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2004 S 34
        SectorEntityToken S2004_S_34 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_34", "S/2004 S 34"
, "moon", showNameProv, 4.0f, 0.161405f, 0.279f, 160.0f, 354.5f, 2003.117f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 36
        SectorEntityToken S2004_S_36 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_36", "S/2004 S 36"
, "moon", showNameProv, 4.0f, 0.156358f, 0.625f, 132.0f, 213.4f, 2002.161f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 37
        SectorEntityToken S2004_S_37 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_37", "S/2004 S 37"
, "moon", showNameProv, 4.0f, 0.106661f, 0.448f, 187.6f, 72.4f,  2000.774f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 39
        SectorEntityToken S2004_S_39 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_39", "S/2004 S 39"
, "moon", showNameProv, 4.0f, 0.155032f, 0.1f,   83.8f,  223.3f, 2001.694f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 40
        SectorEntityToken S2004_S_40 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_40", "S/2004 S 40"
, "moon", showNameProv, 4.0f, 0.107459f, 0.297f, 291.4f, 127.5f, 2000.219f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 41
        SectorEntityToken S2004_S_41 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_41", "S/2004 S 41"
, "moon", showNameProv, 4.0f, 0.12096f,  0.301f, 343.6f, 232.6f, 2000.301f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 42
        SectorEntityToken S2004_S_42 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_42", "S/2004 S 42"
, "moon", showNameProv, 4.0f, 0.121932f, 0.158f, 325.2f, 107.2f, 2000.027f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 43
        SectorEntityToken S2004_S_43 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_43", "S/2004 S 43"
, "moon", showNameProv, 4.0f, 0.126577f, 0.432f, 14.5f,  234.0f, 2001.42f,  angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 44
        SectorEntityToken S2004_S_44 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_44", "S/2004 S 44"
, "moon", showNameProv, 5.0f, 0.130452f, 0.129f, 292.8f, 176.4f, 2000.306f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 45
        SectorEntityToken S2004_S_45 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_45", "S/2004 S 45"
, "moon", showNameProv, 4.0f, 0.131644f, 0.551f, 75.7f,  350.6f, 2002.401f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 46
        SectorEntityToken S2004_S_46 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_46", "S/2004 S 46"
, "moon", showNameProv, 3.0f, 0.137126f, 0.249f, 209.2f, 298.1f, 2002.345f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 47
        SectorEntityToken S2004_S_47 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_47", "S/2004 S 47"
, "moon", showNameProv, 4.0f, 0.107292f, 0.291f, 25.2f,  295.4f, 2001.702f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 48
        SectorEntityToken S2004_S_48 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_48", "S/2004 S 48"
, "moon", showNameProv, 4.0f, 0.147979f, 0.374f, 195.0f, 286.6f, 2001.821f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 49
        SectorEntityToken S2004_S_49 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_49", "S/2004 S 49"
, "moon", showNameProv, 4.0f, 0.149731f, 0.453f, 19.2f,  199.0f, 2002.106f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 50
        SectorEntityToken S2004_S_50 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_50", "S/2004 S 50"
, "moon", showNameProv, 3.0f, 0.149367f, 0.45f,  71.5f,  197.8f, 2001.668f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 51
        SectorEntityToken S2004_S_51 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_51", "S/2004 S 51"
, "moon", showNameProv, 4.0f, 0.168499f, 0.201f, 85.1f,  73.4f,  2003.338f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 52
        SectorEntityToken S2004_S_52 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_52", "S/2004 S 52"
, "moon", showNameProv, 3.0f, 0.176783f, 0.291f, 333.0f, 292.6f, 2002.5f,   angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 53
        SectorEntityToken S2004_S_53 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_53", "S/2004 S 53"
, "moon", showNameProv, 4.0f, 0.155616f, 0.24f,  37.3f,  284.3f, 2002.176f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 54
        SectorEntityToken S2004_S_54 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_54", "S/2004 S 54"
, "moon", showNameProv, 4.0f, 0.075385f, 0.373f, 69.3f,  108.4f, 1999.727f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2004 S 55
        SectorEntityToken S2004_S_55 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_55", "S/2004 S 55"
, "moon", showNameProv, 3.0f, 0.0755f,   0.26f,  59.6f,  80.0f,  1999.33f,  angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2004 S 56
        SectorEntityToken S2004_S_56 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_56", "S/2004 S 56"
, "moon", showNameProv, 5.0f, 0.09138f,  0.339f, 165.7f, 96.4f,  2001.557f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 57
        SectorEntityToken S2004_S_57 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_57", "S/2004 S 57"
, "moon", showNameProv, 4.0f, 0.121329f, 0.263f, 202.7f, 60.9f,  2000.272f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 58
        SectorEntityToken S2004_S_58 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_58", "S/2004 S 58"
, "moon", showNameProv, 5.0f, 0.122024f, 0.249f, 180.1f, 120.0f, 1998.008f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2004 S 59
        SectorEntityToken S2004_S_59 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_59", "S/2004 S 59"
, "moon", showNameProv, 3.0f, 0.128148f, 0.262f, 282.6f, 222.1f, 2000.944f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 60
        SectorEntityToken S2004_S_60 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_60", "S/2004 S 60"
, "moon", showNameProv, 3.0f, 0.130463f, 0.28f,  357.6f, 264.3f, 2002.491f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2004 S 61
        SectorEntityToken S2004_S_61 = calc.spawnIrregularBody2(system, Saturn, "S2004_S_61", "S/2004 S 61"
, "moon", showNameProv, 4.0f, 0.140289f, 0.466f, 175.6f, 40.4f,  2001.523f, angleOffset, null, MASS_SATURN, "Saturn", true);

        // S/2005 S 4
        SectorEntityToken S2005_S_4 = calc.spawnIrregularBody2(system, Saturn, "S2005_S_4", "S/2005 S 4"
, "moon", showNameProv, 5.0f, 0.0757f,   0.315f, 247.1f, 107.1f, 1999.42f,  angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2005 S 5
        SectorEntityToken S2005_S_5 = calc.spawnIrregularBody2(system, Saturn, "S2005_S_5", "S/2005 S 5"
, "moon", showNameProv, 3.0f, 0.142816f, 0.588f, 9.9f,   284.6f, 2000.751f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2005 S 6
        SectorEntityToken S2005_S_6 = calc.spawnIrregularBody2(system, Saturn, "S2005_S_6", "S/2005 S 6"
, "moon", showNameProv, 4.0f, 0.12104f,  0.084f, 163.4f, 77.4f,  1998.848f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2005 S 7
        SectorEntityToken S2005_S_7 = calc.spawnIrregularBody2(system, Saturn, "S2005_S_7", "S/2005 S 7"
, "moon", showNameProv, 3.0f, 0.123682f, 0.565f, 181.1f, 132.7f, 1998.072f, angleOffset, null, MASS_SATURN, "Saturn", false);

        // S/2006 S 1
        SectorEntityToken S2006_S_1  = calc.spawnIrregularBody2(system, Saturn, "S2006_S_1",  "S/2006 S 1"
,  "moon", showNameProv, 5.0f, 0.125311f, 0.105f, 86.3f,  159.7f, 2002.431f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 3
        SectorEntityToken S2006_S_3  = calc.spawnIrregularBody2(system, Saturn, "S2006_S_3",  "S/2006 S 3"
,  "moon", showNameProv, 5.0f, 0.142737f, 0.432f, 125.3f, 191.6f, 2001.89f,  angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 9
        SectorEntityToken S2006_S_9  = calc.spawnIrregularBody2(system, Saturn, "S2006_S_9",  "S/2006 S 9"
,  "moon", showNameProv, 3.0f, 0.096303f, 0.249f, 65.2f,  341.9f, 2000.177f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 10
        SectorEntityToken S2006_S_10 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_10", "S/2006 S 10"
, "moon", showNameProv, 3.0f, 0.126873f, 0.151f, 173.5f, 79.7f,  2002.605f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 11
        SectorEntityToken S2006_S_11 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_11", "S/2006 S 11"
, "moon", showNameProv, 3.0f, 0.131766f, 0.143f, 353.1f, 159.9f, 2001.999f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 12
        SectorEntityToken S2006_S_12 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_12", "S/2006 S 12"
, "moon", showNameProv, 4.0f, 0.130819f, 0.542f, 249.6f, 78.1f,  1998.794f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2006 S 13
        SectorEntityToken S2006_S_13 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_13", "S/2006 S 13"
, "moon", showNameProv, 4.0f, 0.13338f,  0.313f, 248.5f, 254.7f, 2000.355f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 14
        SectorEntityToken S2006_S_14 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_14", "S/2006 S 14"
, "moon", showNameProv, 3.0f, 0.140793f, 0.06f,  264.7f, 136.1f, 2002.382f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 15
        SectorEntityToken S2006_S_15 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_15", "S/2006 S 15"
, "moon", showNameProv, 4.0f, 0.145721f, 0.117f, 313.6f, 224.8f, 2000.111f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 16
        SectorEntityToken S2006_S_16 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_16", "S/2006 S 16"
, "moon", showNameProv, 3.0f, 0.145197f, 0.204f, 62.5f,  282.9f, 2001.503f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 17
        SectorEntityToken S2006_S_17 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_17", "S/2006 S 17"
, "moon", showNameProv, 4.0f, 0.149629f, 0.425f, 206.6f, 149.4f, 2002.607f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 18
        SectorEntityToken S2006_S_18 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_18", "S/2006 S 18"
, "moon", showNameProv, 4.0f, 0.152145f, 0.131f, 14.5f,  62.5f,  2000.464f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 19
        SectorEntityToken S2006_S_19 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_19", "S/2006 S 19"
, "moon", showNameProv, 4.0f, 0.159097f, 0.467f, 195.9f, 19.2f,  2002.931f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 20
        SectorEntityToken S2006_S_20 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_20", "S/2006 S 20"
, "moon", showNameProv, 5.0f, 0.088194f, 0.206f, 153.1f, 77.4f,  2000.262f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 21
        SectorEntityToken S2006_S_21 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_21", "S/2006 S 21"
, "moon", showNameProv, 3.0f, 0.100112f, 0.204f, 40.7f,  157.4f, 2001.708f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 22
        SectorEntityToken S2006_S_22 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_22", "S/2006 S 22"
, "moon", showNameProv, 3.0f, 0.101001f, 0.246f, 254.1f, 71.7f,  2001.219f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 23
        SectorEntityToken S2006_S_23 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_23", "S/2006 S 23"
, "moon", showNameProv, 3.0f, 0.122125f, 0.19f,  309.9f, 72.5f,  1998.197f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2006 S 24
        SectorEntityToken S2006_S_24 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_24", "S/2006 S 24"
, "moon", showNameProv, 3.0f, 0.121731f, 0.352f, 10.3f,  39.8f,  2001.429f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 25
        SectorEntityToken S2006_S_25 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_25", "S/2006 S 25"
, "moon", showNameProv, 3.0f, 0.124149f, 0.303f, 348.5f, 130.1f, 2001.994f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 26
        SectorEntityToken S2006_S_26 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_26", "S/2006 S 26"
, "moon", showNameProv, 3.0f, 0.124462f, 0.248f, 201.0f, 2.5f,   2001.066f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 27
        SectorEntityToken S2006_S_27 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_27", "S/2006 S 27"
, "moon", showNameProv, 4.0f, 0.128382f, 0.14f,  6.1f,   91.6f,  2000.028f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 28
        SectorEntityToken S2006_S_28 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_28", "S/2006 S 28"
, "moon", showNameProv, 4.0f, 0.146761f, 0.21f,  98.4f,  242.2f, 2001.411f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2006 S 29
        SectorEntityToken S2006_S_29 = calc.spawnIrregularBody2(system, Saturn, "S2006_S_29", "S/2006 S 29"
, "moon", showNameProv, 3.0f, 0.168532f, 0.239f, 101.5f, 301.4f, 2000.937f, angleOffset, null, MASS_SATURN, "Saturn", true);

        // S/2007 S 2
        SectorEntityToken S2007_S_2  = calc.spawnIrregularBody2(system, Saturn, "S2007_S_2",  "S/2007 S 2"
,  "moon", showNameProv, 5.0f, 0.106546f, 0.232f, 10.0f,  46.4f,  2000.613f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2007 S 3
        SectorEntityToken S2007_S_3  = calc.spawnIrregularBody2(system, Saturn, "S2007_S_3",  "S/2007 S 3"
,  "moon", showNameProv, 5.0f, 0.131114f, 0.15f,  304.5f, 323.1f, 2000.775f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2007 S 5
        SectorEntityToken S2007_S_5  = calc.spawnIrregularBody2(system, Saturn, "S2007_S_5",  "S/2007 S 5"
,  "moon", showNameProv, 4.0f, 0.105854f, 0.104f, 23.2f,  44.4f,  2000.729f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2007 S 6
        SectorEntityToken S2007_S_6  = calc.spawnIrregularBody2(system, Saturn, "S2007_S_6",  "S/2007 S 6"
,  "moon", showNameProv, 3.0f, 0.123966f, 0.168f, 347.8f, 19.2f,  2002.324f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2007 S 7
        SectorEntityToken S2007_S_7  = calc.spawnIrregularBody2(system, Saturn, "S2007_S_7",  "S/2007 S 7"
,  "moon", showNameProv, 4.0f, 0.106496f, 0.217f, 63.4f,  149.6f, 2000.022f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2007 S 8
        SectorEntityToken S2007_S_8  = calc.spawnIrregularBody2(system, Saturn, "S2007_S_8",  "S/2007 S 8"
,  "moon", showNameProv, 4.0f, 0.113965f, 0.49f,  6.3f,   258.7f, 1998.722f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2007 S 9
        SectorEntityToken S2007_S_9  = calc.spawnIrregularBody2(system, Saturn, "S2007_S_9",  "S/2007 S 9"
,  "moon", showNameProv, 4.0f, 0.134859f, 0.36f,  240.3f, 125.3f, 2002.192f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2007 S 10
        SectorEntityToken S2007_S_10 = calc.spawnIrregularBody2(system, Saturn, "S2007_S_10", "S/2007 S 10"
, "moon", showNameProv, 4.0f, 0.07597f,  0.367f, 23.6f,  94.2f,  1999.012f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2007 S 11
        SectorEntityToken S2007_S_11 = calc.spawnIrregularBody2(system, Saturn, "S2007_S_11", "S/2007 S 11"
, "moon", showNameProv, 4.0f, 0.116542f, 0.499f, 233.9f, 348.9f, 1999.648f, angleOffset, null, MASS_SATURN, "Saturn", false);

        // S/2019 S 1
        SectorEntityToken S2019_S_1  = calc.spawnIrregularBody2(system, Saturn, "S2019_S_1",  "S/2019 S 1"
,  "moon", showNameProv, 5.0f, 0.075171f, 0.383f, 36.5f,  86.6f,  1999.372f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2019 S 2
        SectorEntityToken S2019_S_2  = calc.spawnIrregularBody2(system, Saturn, "S2019_S_2",  "S/2019 S 2"
,  "moon", showNameProv, 3.0f, 0.110699f, 0.279f, 185.1f, 103.2f, 2000.577f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 3
        SectorEntityToken S2019_S_3  = calc.spawnIrregularBody2(system, Saturn, "S2019_S_3",  "S/2019 S 3"
,  "moon", showNameProv, 4.0f, 0.114155f, 0.248f, 150.3f, 129.2f, 2001.545f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 4
        SectorEntityToken S2019_S_4  = calc.spawnIrregularBody2(system, Saturn, "S2019_S_4",  "S/2019 S 4"
,  "moon", showNameProv, 3.0f, 0.120001f, 0.408f, 217.8f, 247.0f, 2002.059f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 5
        SectorEntityToken S2019_S_5  = calc.spawnIrregularBody2(system, Saturn, "S2019_S_5",  "S/2019 S 5"
,  "moon", showNameProv, 3.0f, 0.127609f, 0.216f, 59.4f,  321.5f, 2002.031f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 6
        SectorEntityToken S2019_S_6  = calc.spawnIrregularBody2(system, Saturn, "S2019_S_6",  "S/2019 S 6"
,  "moon", showNameProv, 5.0f, 0.121697f, 0.12f,  185.7f, 286.9f, 1998.924f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2019 S 7
        SectorEntityToken S2019_S_7  = calc.spawnIrregularBody2(system, Saturn, "S2019_S_7",  "S/2019 S 7"
,  "moon", showNameProv, 4.0f, 0.134929f, 0.233f, 336.0f, 6.3f,   2001.845f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 8
        SectorEntityToken S2019_S_8  = calc.spawnIrregularBody2(system, Saturn, "S2019_S_8",  "S/2019 S 8"
,  "moon", showNameProv, 4.0f, 0.135613f, 0.311f, 31.8f,  144.6f, 2001.383f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 9
        SectorEntityToken S2019_S_9  = calc.spawnIrregularBody2(system, Saturn, "S2019_S_9",  "S/2019 S 9"
,  "moon", showNameProv, 4.0f, 0.136096f, 0.433f, 244.7f, 316.4f, 2000.942f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 10
        SectorEntityToken S2019_S_10 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_10", "S/2019 S 10"
, "moon", showNameProv, 3.0f, 0.138373f, 0.248f, 149.5f, 352.9f, 2002.323f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 11
        SectorEntityToken S2019_S_11 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_11", "S/2019 S 11"
, "moon", showNameProv, 4.0f, 0.138132f, 0.513f, 190.5f, 28.2f,  2002.763f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 12
        SectorEntityToken S2019_S_12 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_12", "S/2019 S 12"
, "moon", showNameProv, 4.0f, 0.139674f, 0.476f, 221.1f, 240.2f, 2002.784f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 13
        SectorEntityToken S2019_S_13 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_13", "S/2019 S 13"
, "moon", showNameProv, 3.0f, 0.140139f, 0.318f, 138.5f, 219.8f, 2002.607f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 14
        SectorEntityToken S2019_S_14 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_14", "S/2019 S 14"
, "moon", showNameProv, 4.0f, 0.119339f, 0.172f, 240.9f, 303.2f, 1998.987f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2019 S 15
        SectorEntityToken S2019_S_15 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_15", "S/2019 S 15"
, "moon", showNameProv, 3.0f, 0.141654f, 0.257f, 302.9f, 17.9f,  2000.193f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 16
        SectorEntityToken S2019_S_16 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_16", "S/2019 S 16"
, "moon", showNameProv, 3.0f, 0.155518f, 0.25f,  351.8f, 280.6f, 2002.083f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 17
        SectorEntityToken S2019_S_17 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_17", "S/2019 S 17"
, "moon", showNameProv, 4.0f, 0.151892f, 0.546f, 40.5f,  317.0f, 2001.219f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 18
        SectorEntityToken S2019_S_18 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_18", "S/2019 S 18"
, "moon", showNameProv, 3.0f, 0.154678f, 0.509f, 340.4f, 304.5f, 2002.305f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 19
        SectorEntityToken S2019_S_19 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_19", "S/2019 S 19"
, "moon", showNameProv, 3.0f, 0.154042f, 0.458f, 335.8f, 305.8f, 2002.466f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 20
        SectorEntityToken S2019_S_20 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_20", "S/2019 S 20"
, "moon", showNameProv, 3.0f, 0.158277f, 0.354f, 281.1f, 198.6f, 2001.827f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 21
        SectorEntityToken S2019_S_21 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_21", "S/2019 S 21"
, "moon", showNameProv, 4.0f, 0.176737f, 0.155f, 346.4f, 16.7f,  2004.185f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 22
        SectorEntityToken S2019_S_22 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_22", "S/2019 S 22"
, "moon", showNameProv, 3.0f, 0.07557f,  0.369f, 223.0f, 100.5f, 1999.928f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2019 S 23
        SectorEntityToken S2019_S_23 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_23", "S/2019 S 23"
, "moon", showNameProv, 3.0f, 0.075604f, 0.255f, 1.0f,   81.5f,  1998.783f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2019 S 24
        SectorEntityToken S2019_S_24 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_24", "S/2019 S 24"
, "moon", showNameProv, 4.0f, 0.07594f,  0.345f, 79.3f,  74.7f,  1999.294f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2019 S 25
        SectorEntityToken S2019_S_25 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_25", "S/2019 S 25"
, "moon", showNameProv, 4.0f, 0.075732f, 0.271f, 358.9f, 73.9f,  1999.268f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2019 S 26
        SectorEntityToken S2019_S_26 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_26", "S/2019 S 26"
, "moon", showNameProv, 3.0f, 0.076143f, 0.365f, 164.1f, 111.3f, 1999.587f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2019 S 27
        SectorEntityToken S2019_S_27 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_27", "S/2019 S 27"
, "moon", showNameProv, 3.0f, 0.108738f, 0.42f,  152.1f, 97.2f,  2000.678f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 28
        SectorEntityToken S2019_S_28 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_28", "S/2019 S 28"
, "moon", showNameProv, 4.0f, 0.116954f, 0.199f, 307.9f, 95.6f,  2001.763f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 29
        SectorEntityToken S2019_S_29 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_29", "S/2019 S 29"
, "moon", showNameProv, 3.0f, 0.116004f, 0.441f, 133.8f, 332.7f, 1998.008f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2019 S 30
        SectorEntityToken S2019_S_30 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_30", "S/2019 S 30"
, "moon", showNameProv, 3.0f, 0.118383f, 0.107f, 187.8f, 5.3f,   2002.388f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 31
        SectorEntityToken S2019_S_31 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_31", "S/2019 S 31"
, "moon", showNameProv, 3.0f, 0.118579f, 0.488f, 35.2f,  37.8f,  1998.835f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2019 S 32
        SectorEntityToken S2019_S_32 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_32", "S/2019 S 32"
, "moon", showNameProv, 5.0f, 0.120059f, 0.276f, 20.1f,  281.4f, 1998.509f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2019 S 33
        SectorEntityToken S2019_S_33 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_33", "S/2019 S 33"
, "moon", showNameProv, 4.0f, 0.124976f, 0.289f, 245.6f, 46.9f,  2001.353f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 34
        SectorEntityToken S2019_S_34 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_34", "S/2019 S 34"
, "moon", showNameProv, 3.0f, 0.123309f, 0.536f, 78.5f,  226.7f, 1998.795f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2019 S 35
        SectorEntityToken S2019_S_35 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_35", "S/2019 S 35"
, "moon", showNameProv, 3.0f, 0.124051f, 0.577f, 259.0f, 181.1f, 2000.454f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 36
        SectorEntityToken S2019_S_36 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_36", "S/2019 S 36"
, "moon", showNameProv, 3.0f, 0.133045f, 0.161f, 145.8f, 278.1f, 2002.818f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 37
        SectorEntityToken S2019_S_37 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_37", "S/2019 S 37"
, "moon", showNameProv, 3.0f, 0.133671f, 0.404f, 135.8f, 355.3f, 2002.195f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 38
        SectorEntityToken S2019_S_38 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_38", "S/2019 S 38"
, "moon", showNameProv, 3.0f, 0.14705f,  0.399f, 138.3f, 303.7f, 2002.007f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 39
        SectorEntityToken S2019_S_39 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_39", "S/2019 S 39"
, "moon", showNameProv, 3.0f, 0.15899f,  0.098f, 92.5f,  327.0f, 2002.845f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 40
        SectorEntityToken S2019_S_40 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_40", "S/2019 S 40"
, "moon", showNameProv, 3.0f, 0.161017f, 0.088f, 347.6f, 242.4f, 2000.471f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 41
        SectorEntityToken S2019_S_41 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_41", "S/2019 S 41"
, "moon", showNameProv, 3.0f, 0.16373f,  0.257f, 320.7f, 40.0f,  2000.124f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 42
        SectorEntityToken S2019_S_42 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_42", "S/2019 S 42"
, "moon", showNameProv, 4.0f, 0.161176f, 0.121f, 351.4f, 84.5f,  2003.383f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 43
        SectorEntityToken S2019_S_43 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_43", "S/2019 S 43"
, "moon", showNameProv, 3.0f, 0.178238f, 0.277f, 306.2f, 108.2f, 2001.434f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2019 S 44
        SectorEntityToken S2019_S_44 = calc.spawnIrregularBody2(system, Saturn, "S2019_S_44", "S/2019 S 44"
, "moon", showNameProv, 3.0f, 0.179126f, 0.512f, 330.6f, 332.7f, 2002.778f, angleOffset, null, MASS_SATURN, "Saturn", true);

        // S/2020 S 1
        SectorEntityToken S2020_S_1  = calc.spawnIrregularBody2(system, Saturn, "S2020_S_1",  "S/2020 S 1"
,  "moon", showNameProv, 4.0f, 0.075794f, 0.337f, 2.1f,   73.2f,  1999.446f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2020 S 2
        SectorEntityToken S2020_S_2  = calc.spawnIrregularBody2(system, Saturn, "S2020_S_2",  "S/2020 S 2"
,  "moon", showNameProv, 3.0f, 0.119447f, 0.152f, 19.2f,  130.7f, 2000.652f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 3
        SectorEntityToken S2020_S_3  = calc.spawnIrregularBody2(system, Saturn, "S2020_S_3",  "S/2020 S 3"
,  "moon", showNameProv, 3.0f, 0.120702f, 0.142f, 135.8f, 317.6f, 1998.506f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2020 S 4
        SectorEntityToken S2020_S_4  = calc.spawnIrregularBody2(system, Saturn, "S2020_S_4",  "S/2020 S 4"
,  "moon", showNameProv, 3.0f, 0.121903f, 0.496f, 178.7f, 157.7f, 1999.526f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2020 S 5
        SectorEntityToken S2020_S_5  = calc.spawnIrregularBody2(system, Saturn, "S2020_S_5",  "S/2020 S 5"
,  "moon", showNameProv, 3.0f, 0.122936f, 0.22f,  232.0f, 285.7f, 1998.478f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2020 S 6
        SectorEntityToken S2020_S_6  = calc.spawnIrregularBody2(system, Saturn, "S2020_S_6",  "S/2020 S 6"
,  "moon", showNameProv, 3.0f, 0.14207f,  0.48f,  271.2f, 27.9f,  2000.062f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 7
        SectorEntityToken S2020_S_7  = calc.spawnIrregularBody2(system, Saturn, "S2020_S_7",  "S/2020 S 7"
,  "moon", showNameProv, 3.0f, 0.116272f, 0.5f,   215.8f, 298.8f, 2001.145f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 8
        SectorEntityToken S2020_S_8  = calc.spawnIrregularBody2(system, Saturn, "S2020_S_8",  "S/2020 S 8"
,  "moon", showNameProv, 3.0f, 0.146842f, 0.252f, 181.2f, 45.0f,  2000.386f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 9
        SectorEntityToken S2020_S_9  = calc.spawnIrregularBody2(system, Saturn, "S2020_S_9",  "S/2020 S 9"
,  "moon", showNameProv, 4.0f, 0.169847f, 0.531f, 46.4f,  171.0f, 2000.173f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 10
        SectorEntityToken S2020_S_10 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_10", "S/2020 S 10"
, "moon", showNameProv, 3.0f, 0.169222f, 0.296f, 158.8f, 213.5f, 2002.786f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 11
        SectorEntityToken S2020_S_11 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_11", "S/2020 S 11"
, "moon", showNameProv, 3.0f, 0.075506f, 0.372f, 95.9f,  110.4f, 1999.679f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2020 S 12
        SectorEntityToken S2020_S_12 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_12", "S/2020 S 12"
, "moon", showNameProv, 3.0f, 0.075633f, 0.26f,  350.8f, 81.2f,  1999.759f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2020 S 13
        SectorEntityToken S2020_S_13 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_13", "S/2020 S 13"
, "moon", showNameProv, 3.0f, 0.076309f, 0.373f, 230.4f, 96.4f,  1999.856f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2020 S 14
        SectorEntityToken S2020_S_14 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_14", "S/2020 S 14"
, "moon", showNameProv, 3.0f, 0.108198f, 0.313f, 209.3f, 54.7f,  2002.078f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 15
        SectorEntityToken S2020_S_15 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_15", "S/2020 S 15"
, "moon", showNameProv, 3.0f, 0.111828f, 0.462f, 141.7f, 275.8f, 1997.922f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2020 S 16
        SectorEntityToken S2020_S_16 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_16", "S/2020 S 16"
, "moon", showNameProv, 3.0f, 0.113393f, 0.405f, 185.2f, 73.1f,  2001.685f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 17
        SectorEntityToken S2020_S_17 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_17", "S/2020 S 17"
, "moon", showNameProv, 4.0f, 0.114268f, 0.378f, 359.0f, 287.2f, 2001.338f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 18
        SectorEntityToken S2020_S_18 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_18", "S/2020 S 18"
, "moon", showNameProv, 3.0f, 0.118838f, 0.18f,  218.7f, 165.9f, 2001.373f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 19
        SectorEntityToken S2020_S_19 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_19", "S/2020 S 19"
, "moon", showNameProv, 3.0f, 0.118496f, 0.159f, 80.4f,  65.5f,  1999.624f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2020 S 20
        SectorEntityToken S2020_S_20 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_20", "S/2020 S 20"
, "moon", showNameProv, 3.0f, 0.120305f, 0.133f, 330.0f, 230.5f, 2000.637f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 21
        SectorEntityToken S2020_S_21 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_21", "S/2020 S 21"
, "moon", showNameProv, 3.0f, 0.126085f, 0.307f, 95.5f,  188.0f, 2002.415f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 22
        SectorEntityToken S2020_S_22 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_22", "S/2020 S 22"
, "moon", showNameProv, 3.0f, 0.129968f, 0.059f, 128.8f, 38.4f,  2002.089f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 23
        SectorEntityToken S2020_S_23 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_23", "S/2020 S 23"
, "moon", showNameProv, 3.0f, 0.132365f, 0.089f, 65.1f,  61.5f,  2001.215f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 24
        SectorEntityToken S2020_S_24 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_24", "S/2020 S 24"
, "moon", showNameProv, 3.0f, 0.137825f, 0.23f,  301.4f, 114.8f, 2000.355f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 25
        SectorEntityToken S2020_S_25 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_25", "S/2020 S 25"
, "moon", showNameProv, 3.0f, 0.138797f, 0.316f, 318.1f, 329.0f, 2002.058f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 26
        SectorEntityToken S2020_S_26 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_26", "S/2020 S 26"
, "moon", showNameProv, 3.0f, 0.142144f, 0.273f, 141.3f, 143.0f, 2002.227f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 27
        SectorEntityToken S2020_S_27 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_27", "S/2020 S 27"
, "moon", showNameProv, 3.0f, 0.145739f, 0.255f, 32.0f,  314.6f, 2002.966f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 28
        SectorEntityToken S2020_S_28 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_28", "S/2020 S 28"
, "moon", showNameProv, 3.0f, 0.147019f, 0.474f, 2.2f,   226.7f, 2000.564f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 29
        SectorEntityToken S2020_S_29 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_29", "S/2020 S 29"
, "moon", showNameProv, 3.0f, 0.149076f, 0.047f, 115.9f, 298.7f, 2002.42f,  angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 30
        SectorEntityToken S2020_S_30 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_30", "S/2020 S 30"
, "moon", showNameProv, 3.0f, 0.145662f, 0.601f, 60.3f,  352.3f, 2001.207f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 31
        SectorEntityToken S2020_S_31 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_31", "S/2020 S 31"
, "moon", showNameProv, 3.0f, 0.150118f, 0.238f, 0.1f,   196.2f, 2001.415f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 32
        SectorEntityToken S2020_S_32 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_32", "S/2020 S 32"
, "moon", showNameProv, 3.0f, 0.146286f, 0.502f, 88.9f,  198.2f, 2003.232f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 33
        SectorEntityToken S2020_S_33 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_33", "S/2020 S 33"
, "moon", showNameProv, 3.0f, 0.153227f, 0.555f, 165.2f, 4.3f,   2001.594f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 34
        SectorEntityToken S2020_S_34 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_34", "S/2020 S 34"
, "moon", showNameProv, 3.0f, 0.149973f, 0.154f, 116.4f, 162.8f, 2000.073f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 35
        SectorEntityToken S2020_S_35 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_35", "S/2020 S 35"
, "moon", showNameProv, 3.0f, 0.153948f, 0.225f, 98.0f,  19.8f,  2003.421f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 36
        SectorEntityToken S2020_S_36 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_36", "S/2020 S 36"
, "moon", showNameProv, 3.0f, 0.15245f,  0.336f, 216.1f, 139.7f, 2000.263f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 37
        SectorEntityToken S2020_S_37 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_37", "S/2020 S 37"
, "moon", showNameProv, 3.0f, 0.158771f, 0.344f, 182.3f, 211.7f, 2000.261f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 38
        SectorEntityToken S2020_S_38 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_38", "S/2020 S 38"
, "moon", showNameProv, 4.0f, 0.157649f, 0.513f, 99.4f,  246.3f, 2002.921f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 39
        SectorEntityToken S2020_S_39 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_39", "S/2020 S 39"
, "moon", showNameProv, 3.0f, 0.162184f, 0.305f, 165.2f, 28.9f,  2003.421f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 40
        SectorEntityToken S2020_S_40 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_40", "S/2020 S 40"
, "moon", showNameProv, 3.0f, 0.158999f, 0.412f, 64.0f,  99.4f,  2002.61f,  angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 41
        SectorEntityToken S2020_S_41 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_41", "S/2020 S 41"
, "moon", showNameProv, 3.0f, 0.172973f, 0.402f, 63.1f,  224.9f, 2000.335f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 42
        SectorEntityToken S2020_S_42 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_42", "S/2020 S 42"
, "moon", showNameProv, 3.0f, 0.169317f, 0.506f, 278.4f, 59.1f,  2000.176f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 43
        SectorEntityToken S2020_S_43 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_43", "S/2020 S 43"
, "moon", showNameProv, 3.0f, 0.178194f, 0.203f, 252.6f, 257.0f, 2000.849f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 44
        SectorEntityToken S2020_S_44 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_44", "S/2020 S 44"
, "moon", showNameProv, 3.0f, 0.182218f, 0.199f, 318.0f, 283.8f, 2000.358f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 45
        SectorEntityToken S2020_S_45 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_45", "S/2020 S 45"
, "moon", showNameProv, 3.0f, 0.157139f, 0.199f, 190.2f, 195.7f, 2001.874f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 46
        SectorEntityToken S2020_S_46 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_46", "S/2020 S 46"
, "moon", showNameProv, 3.0f, 0.126286f, 0.207f, 16.0f,  331.2f, 2002.385f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 47
        SectorEntityToken S2020_S_47 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_47", "S/2020 S 47"
, "moon", showNameProv, 3.0f, 0.143033f, 0.564f, 283.9f, 5.4f,   2003.125f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2020 S 48
        SectorEntityToken S2020_S_48 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_48", "S/2020 S 48"
, "moon", showNameProv, 3.0f, 0.075904f, 0.373f, 21.9f,  114.9f, 1999.129f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2020 S 49
        SectorEntityToken S2020_S_49 = calc.spawnIrregularBody2(system, Saturn, "S2020_S_49", "S/2020 S 49"
, "moon", showNameProv, 2.0f, 0.075575f, 0.373f, 313.4f, 98.2f,  1999.584f, angleOffset, null, MASS_SATURN, "Saturn", false);

        // S/2023 S 1
        SectorEntityToken S2023_S_1  = calc.spawnIrregularBody2(system, Saturn, "S2023_S_1",  "S/2023 S 1"
,  "moon", showNameProv, 3.0f, 0.074903f, 0.386f, 263.4f, 296.8f, 1999.462f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2023 S 2
        SectorEntityToken S2023_S_2  = calc.spawnIrregularBody2(system, Saturn, "S2023_S_2",  "S/2023 S 2"
,  "moon", showNameProv, 3.0f, 0.075602f, 0.339f, 257.2f, 117.3f, 1999.133f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2023 S 3
        SectorEntityToken S2023_S_3  = calc.spawnIrregularBody2(system, Saturn, "S2023_S_3",  "S/2023 S 3"
,  "moon", showNameProv, 3.0f, 0.117959f, 0.178f, 272.6f, 264.0f, 1999.376f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2023 S 4
        SectorEntityToken S2023_S_4  = calc.spawnIrregularBody2(system, Saturn, "S2023_S_4",  "S/2023 S 4"
,  "moon", showNameProv, 3.0f, 0.118749f, 0.276f, 123.9f, 9.5f,   2001.778f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 5
        SectorEntityToken S2023_S_5  = calc.spawnIrregularBody2(system, Saturn, "S2023_S_5",  "S/2023 S 5"
,  "moon", showNameProv, 3.0f, 0.171015f, 0.599f, 334.2f, 94.9f,  2002.731f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 6
        SectorEntityToken S2023_S_6  = calc.spawnIrregularBody2(system, Saturn, "S2023_S_6",  "S/2023 S 6"
,  "moon", showNameProv, 3.0f, 0.079902f, 0.336f, 133.4f, 103.7f, 1999.798f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2023 S 7
        SectorEntityToken S2023_S_7  = calc.spawnIrregularBody2(system, Saturn, "S2023_S_7",  "S/2023 S 7"
,  "moon", showNameProv, 4.0f, 0.081109f, 0.284f, 102.3f, 102.5f, 1998.901f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2023 S 8
        SectorEntityToken S2023_S_8  = calc.spawnIrregularBody2(system, Saturn, "S2023_S_8",  "S/2023 S 8"
,  "moon", showNameProv, 3.0f, 0.09371f,  0.122f, 149.6f, 278.8f, 2001.132f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 9
        SectorEntityToken S2023_S_9  = calc.spawnIrregularBody2(system, Saturn, "S2023_S_9",  "S/2023 S 9"
,  "moon", showNameProv, 3.0f, 0.088019f, 0.141f, 285.2f, 209.8f, 2001.393f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 10
        SectorEntityToken S2023_S_10 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_10", "S/2023 S 10"
, "moon", showNameProv, 3.0f, 0.103612f, 0.302f, 70.9f,  124.7f, 2001.688f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 11
        SectorEntityToken S2023_S_11 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_11", "S/2023 S 11"
, "moon", showNameProv, 3.0f, 0.093892f, 0.3f,   334.2f, 76.1f,  2000.912f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 12
        SectorEntityToken S2023_S_12 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_12", "S/2023 S 12"
, "moon", showNameProv, 3.0f, 0.105656f, 0.601f, 339.7f, 182.5f, 2001.698f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 13
        SectorEntityToken S2023_S_13 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_13", "S/2023 S 13"
, "moon", showNameProv, 3.0f, 0.101559f, 0.179f, 173.9f, 107.5f, 2000.714f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 14
        SectorEntityToken S2023_S_14 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_14", "S/2023 S 14"
, "moon", showNameProv, 3.0f, 0.112655f, 0.497f, 62.7f,  351.4f, 2001.812f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 15
        SectorEntityToken S2023_S_15 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_15", "S/2023 S 15"
, "moon", showNameProv, 3.0f, 0.121936f, 0.549f, 159.7f, 123.2f, 2001.935f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 16
        SectorEntityToken S2023_S_16 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_16", "S/2023 S 16"
, "moon", showNameProv, 3.0f, 0.113673f, 0.27f,  80.0f,  116.9f, 2001.704f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 17
        SectorEntityToken S2023_S_17 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_17", "S/2023 S 17"
, "moon", showNameProv, 3.0f, 0.116214f, 0.498f, 128.1f, 246.7f, 1998.433f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2023 S 18
        SectorEntityToken S2023_S_18 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_18", "S/2023 S 18"
, "moon", showNameProv, 3.0f, 0.116189f, 0.448f, 169.7f, 280.0f, 1999.492f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2023 S 19
        SectorEntityToken S2023_S_19 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_19", "S/2023 S 19"
, "moon", showNameProv, 3.0f, 0.117584f, 0.092f, 325.5f, 205.4f, 1999.668f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2023 S 20
        SectorEntityToken S2023_S_20 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_20", "S/2023 S 20"
, "moon", showNameProv, 3.0f, 0.115383f, 0.442f, 200.6f, 322.1f, 2000.726f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 21
        SectorEntityToken S2023_S_21 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_21", "S/2023 S 21"
, "moon", showNameProv, 3.0f, 0.118688f, 0.077f, 38.7f,  341.9f, 2000.834f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 22
        SectorEntityToken S2023_S_22 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_22", "S/2023 S 22"
, "moon", showNameProv, 4.0f, 0.124183f, 0.182f, 4.2f,   266.5f, 1999.124f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2023 S 23
        SectorEntityToken S2023_S_23 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_23", "S/2023 S 23"
, "moon", showNameProv, 3.0f, 0.125561f, 0.35f,  81.8f,  91.2f,  2001.367f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 24
        SectorEntityToken S2023_S_24 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_24", "S/2023 S 24"
, "moon", showNameProv, 3.0f, 0.122674f, 0.374f, 54.4f,  319.7f, 2000.01f,  angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 25
        SectorEntityToken S2023_S_25 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_25", "S/2023 S 25"
, "moon", showNameProv, 3.0f, 0.12792f,  0.281f, 186.2f, 293.8f, 2001.628f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 26
        SectorEntityToken S2023_S_26 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_26", "S/2023 S 26"
, "moon", showNameProv, 3.0f, 0.132985f, 0.306f, 132.2f, 108.2f, 2001.175f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 27
        SectorEntityToken S2023_S_27 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_27", "S/2023 S 27"
, "moon", showNameProv, 3.0f, 0.132489f, 0.652f, 91.2f,  117.0f, 2002.429f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 28
        SectorEntityToken S2023_S_28 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_28", "S/2023 S 28"
, "moon", showNameProv, 3.0f, 0.132896f, 0.575f, 189.6f, 2.3f,   2000.474f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 29
        SectorEntityToken S2023_S_29 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_29", "S/2023 S 29"
, "moon", showNameProv, 3.0f, 0.133975f, 0.141f, 162.8f, 47.0f,  2001.832f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 30
        SectorEntityToken S2023_S_30 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_30", "S/2023 S 30"
, "moon", showNameProv, 3.0f, 0.121916f, 0.493f, 2.7f,   191.3f, 2001.749f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 31
        SectorEntityToken S2023_S_31 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_31", "S/2023 S 31"
, "moon", showNameProv, 3.0f, 0.138566f, 0.182f, 308.7f, 240.9f, 2002.213f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 32
        SectorEntityToken S2023_S_32 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_32", "S/2023 S 32"
, "moon", showNameProv, 2.0f, 0.136729f, 0.037f, 30.0f,  90.3f,  2001.621f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 33
        SectorEntityToken S2023_S_33 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_33", "S/2023 S 33"
, "moon", showNameProv, 3.0f, 0.144533f, 0.665f, 49.1f,  258.6f, 2001.89f,  angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 34
        SectorEntityToken S2023_S_34 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_34", "S/2023 S 34"
, "moon", showNameProv, 3.0f, 0.139065f, 0.57f,  293.6f, 275.5f, 2002.582f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 35
        SectorEntityToken S2023_S_35 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_35", "S/2023 S 35"
, "moon", showNameProv, 3.0f, 0.148864f, 0.151f, 92.9f,  165.5f, 2003.217f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 36
        SectorEntityToken S2023_S_36 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_36", "S/2023 S 36"
, "moon", showNameProv, 3.0f, 0.148602f, 0.359f, 312.2f, 157.0f, 2003.06f,  angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 37
        SectorEntityToken S2023_S_37 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_37", "S/2023 S 37"
, "moon", showNameProv, 3.0f, 0.132955f, 0.215f, 244.7f, 241.2f, 2000.505f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 38
        SectorEntityToken S2023_S_38 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_38", "S/2023 S 38"
, "moon", showNameProv, 3.0f, 0.08572f,  0.909f, 244.7f, 241.2f, 2000.263f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 39
        SectorEntityToken S2023_S_39 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_39", "S/2023 S 39"
, "moon", showNameProv, 3.0f, 0.139203f, 0.124f, 345.1f, 351.9f, 2001.009f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 40
        SectorEntityToken S2023_S_40 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_40", "S/2023 S 40"
, "moon", showNameProv, 3.0f, 0.140811f, 0.342f, 3.6f,   334.7f, 2002.356f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 41
        SectorEntityToken S2023_S_41 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_41", "S/2023 S 41"
, "moon", showNameProv, 3.0f, 0.142291f, 0.279f, 29.1f,  323.0f, 2000.063f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 42
        SectorEntityToken S2023_S_42 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_42", "S/2023 S 42"
, "moon", showNameProv, 3.0f, 0.145971f, 0.059f, 315.9f, 33.2f,  2003.254f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 43
        SectorEntityToken S2023_S_43 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_43", "S/2023 S 43"
, "moon", showNameProv, 3.0f, 0.15083f,  0.264f, 92.7f,  218.0f, 2002.38f,  angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 44
        SectorEntityToken S2023_S_44 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_44", "S/2023 S 44"
, "moon", showNameProv, 3.0f, 0.128962f, 0.434f, 232.9f, 166.3f, 2001.369f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 45
        SectorEntityToken S2023_S_45 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_45", "S/2023 S 45"
, "moon", showNameProv, 3.0f, 0.156676f, 0.633f, 336.8f, 334.1f, 2001.041f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 46
        SectorEntityToken S2023_S_46 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_46", "S/2023 S 46"
, "moon", showNameProv, 3.0f, 0.165169f, 0.336f, 230.0f, 100.4f, 2003.384f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 47
        SectorEntityToken S2023_S_47 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_47", "S/2023 S 47"
, "moon", showNameProv, 3.0f, 0.167799f, 0.101f, 97.5f,  257.3f, 2002.354f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 48
        SectorEntityToken S2023_S_48 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_48", "S/2023 S 48"
, "moon", showNameProv, 3.0f, 0.133887f, 0.022f, 315.6f, 108.2f, 2002.726f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 49
        SectorEntityToken S2023_S_49 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_49", "S/2023 S 49"
, "moon", showNameProv, 3.0f, 0.1455f,   0.026f, 82.5f,  110.7f, 2001.767f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 50
        SectorEntityToken S2023_S_50 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_50", "S/2023 S 50"
, "moon", showNameProv, 3.0f, 0.077919f, 0.263f, 79.8f,  326.3f, 2000.104f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 51
        SectorEntityToken S2023_S_51 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_51", "S/2023 S 51"
, "moon", showNameProv, 3.0f, 0.15663f,  0.191f, 310.9f, 303.4f, 2002.174f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 52
        SectorEntityToken S2023_S_52 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_52", "S/2023 S 52"
, "moon", showNameProv, 3.0f, 0.15059f,  0.124f, 267.3f, 88.7f,  2000.411f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 53
        SectorEntityToken S2023_S_53 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_53", "S/2023 S 53"
, "moon", showNameProv, 3.0f, 0.114849f, 0.103f, 179.0f, 55.0f,  2002.012f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 54
        SectorEntityToken S2023_S_54 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_54", "S/2023 S 54"
, "moon", showNameProv, 3.0f, 0.116881f, 0.48f,  48.2f,  60.4f,  1999.207f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2023 S 55
        SectorEntityToken S2023_S_55 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_55", "S/2023 S 55"
, "moon", showNameProv, 3.0f, 0.112803f, 0.491f, 68.8f,  60.8f,  1998.81f,  angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2023 S 56
        SectorEntityToken S2023_S_56 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_56", "S/2023 S 56"
, "moon", showNameProv, 3.0f, 0.075452f, 0.358f, 341.9f, 89.8f,  1999.077f, angleOffset, null, MASS_SATURN, "Saturn", false);
        // S/2023 S 57
        SectorEntityToken S2023_S_57 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_57", "S/2023 S 57"
, "moon", showNameProv, 2.0f, 0.137275f, 0.245f, 318.0f, 183.5f, 2000.721f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 58
        SectorEntityToken S2023_S_58 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_58", "S/2023 S 58"
, "moon", showNameProv, 2.0f, 0.115015f, 0.093f, 175.6f, 70.6f,  2000.765f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 59
        SectorEntityToken S2023_S_59 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_59", "S/2023 S 59"
, "moon", showNameProv, 2.0f, 0.13412f,  0.467f, 66.5f,  10.5f,  2002.727f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 60
        SectorEntityToken S2023_S_60 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_60", "S/2023 S 60"
, "moon", showNameProv, 2.0f, 0.116938f, 0.206f, 93.9f,  258.1f, 2000.534f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 61
        SectorEntityToken S2023_S_61 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_61", "S/2023 S 61"
, "moon", showNameProv, 3.0f, 0.120775f, 0.557f, 75.3f,  37.7f,  2002.095f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 62
        SectorEntityToken S2023_S_62 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_62", "S/2023 S 62"
, "moon", showNameProv, 2.0f, 0.093757f, 0.467f, 241.3f, 206.6f, 2001.678f, angleOffset, null, MASS_SATURN, "Saturn", true);
        // S/2023 S 63
        SectorEntityToken S2023_S_63 = calc.spawnIrregularBody2(system, Saturn, "S2023_S_63", "S/2023 S 63"
, "moon", showNameProv, 4.0f, 0.123549f, 0.266f, 225.6f, 299.9f, 2000.726f, angleOffset, null, MASS_SATURN, "Saturn", true);
    }

    private static int optInt(String key, int def) {
        try { return Global.getSettings().loadJSON(CFG).optInt(key, def); }
        catch (Exception e) { return def; }
    }
}   