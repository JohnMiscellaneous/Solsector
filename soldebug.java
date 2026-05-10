runcode import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import org.lwjgl.util.vector.Vector2f;

import soljars.econ.utils.DistanceCheck;

StarSystemAPI sys = (StarSystemAPI) Global.getSector().getPlayerFleet().getContainingLocation();
SectorEntityToken market = sys.getEntityById("Sedna");
MarketAPI marketsedna = market.getMarket();
float au = (float) DistanceCheck.getMarketAU(marketsedna);


Global.getSector().getCampaignUI().addMessage("AU:      " + au, Misc.getBasePlayerColor());