package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonySizeChangeListener;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sol_civilised_subpop extends BaseMarketConditionPlugin implements ColonySizeChangeListener, EconomyTickListener {

    public static final String ID = "sol_civilised_subpop";

    private static final String KEY_TICKS = "$sol_civ_ticks"; 
    private static final String KEY_STAGE = "$sol_civ_stage";

    private static final String KEY_ANPT_TIER = "$sol_anpt_tier";
    private static final int TIER_SECRET_BREACH = 0;

    // Stages
    private static final int STAGE_INITIAL = 0;
    private static final int STAGE_MICROSTATES = 1; //sz6
    private static final int STAGE_SMALL_UNION = 2; //sz7
    private static final int STAGE_CONTINENTAL = 3; //sz8
    private static final int STAGE_GLOBAL = 4; //sz9

    // In month economy ticks
    private static final float DURATION_INITIAL = 12f; // 0 -> 1
    private static final float DURATION_MICROSTATES = 24f; // 1 -> 2
    private static final float DURATION_SMALL_UNION = 48f; // 2 -> 3
    private static final float DURATION_CONTINENTAL = 96f; // 3 -> 4

    // what were you thinking with listeners?
    public void apply(String id) {
        super.apply(id);

        Global.getSector().getListenerManager().removeListener(this);
        Global.getSector().getListenerManager().addListener(this);

        MemoryAPI mem = market.getMemoryWithoutUpdate();
        int stage = STAGE_INITIAL;
        if (mem.contains(KEY_STAGE)) stage = mem.getInt(KEY_STAGE);
        
        String desc = condition.getName();

        // --- POPULATION GROWTH ---
        int size = market.getSize();
        float growthBonus = 20f;
        if (size > 3) {
            growthBonus -= (size - 3) * 5f;
        }
        
        market.getIncoming().getWeight().modifyFlat(id, growthBonus, desc);

        // --- UPKEEP ---
        // Stage 0-1: 3x Upkeep, Stage 2+: 2x Upkeep
        float upkeepMult = (stage < STAGE_SMALL_UNION) ? 3f : 2f;
        
        for (Industry ind : market.getIndustries()) {
            ind.getUpkeep().modifyMult(id, upkeepMult, desc);
        }

        // --- INCOME ---
        // Stage 0-1 Only
        if (stage < STAGE_SMALL_UNION) {
            float incomeMult = (size == 6) ? 1.5f : 2f;
            
            Industry popInd = market.getIndustry(Industries.POPULATION);
            if (popInd != null) {
                popInd.getIncome().modifyMult(id, incomeMult, desc);
            }
        }

        // --- MAX INDUSTRIES ---
        int maxIndMod = -1;
        
        if (stage >= STAGE_CONTINENTAL) {
            maxIndMod += 1;
        }
        
        if (stage >= STAGE_GLOBAL) {
            maxIndMod += 1; 
        }

        if (maxIndMod != 0) {
            market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).modifyFlat(id, maxIndMod, desc);
        }
    }

    public void unapply(String id) {
        super.unapply(id);
        
        // Remove Listener (Removes BOTH listeners)
        Global.getSector().getListenerManager().removeListener(this);

        market.getIncoming().getWeight().unmodify(id);
        market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodify(id);
        
        for (Industry ind : market.getIndustries()) {
            ind.getUpkeep().unmodify(id);
            ind.getIncome().unmodify(id);
        }
    }

    @Override
    public void reportColonySizeChanged(MarketAPI market, int prevSize) {
        if (market != this.market) return;
        
        if (!market.hasCondition(ID)) {
            Global.getSector().getListenerManager().removeListener(this);
            return;
        }

        // Force re-application of conditions to update Income/Upkeep stats immediately
        market.reapplyCondition(ID);

        MemoryAPI mem = market.getMemoryWithoutUpdate();
        int stage = STAGE_INITIAL;
        if (mem.contains(KEY_STAGE)) stage = mem.getInt(KEY_STAGE);

        // --- ABANDONED / PRE-COLONIZATION CHECK ---
        if (stage == STAGE_INITIAL && market.getSize() < 3) {
            market.removeCondition(Conditions.DECIVILIZED);
            market.removeCondition(Conditions.DECIVILIZED_SUBPOP);
            sol_remove_replace.execute(market, ID, "sol_civilised_world");
            
            Global.getSector().getListenerManager().removeListener(this);
        }
    }

    @Override
    public void reportEconomyTick(int iterIndex) {
        // ZOMBIE CHECK
        if (!market.hasCondition(ID)) {
            Global.getSector().getListenerManager().removeListener(this);
            return;
        }
    }

    @Override
    public void reportEconomyMonthEnd() {
        // ZOMBIE CHECK
        if (!market.hasCondition(ID)) {
            Global.getSector().getListenerManager().removeListener(this);
            return;
        }
        
        MemoryAPI mem = market.getMemoryWithoutUpdate();

        int stage = STAGE_INITIAL;
        if (mem.contains(KEY_STAGE)) stage = mem.getInt(KEY_STAGE);

        // Check for Hard Blocking Conditions
        boolean hasWar = market.hasCondition("sol_world_war");
        boolean hasBreacher = market.hasCondition("sol_antimatter_nonproliferation_treaty_breacher");
        boolean hasSignatory = market.hasCondition("sol_antimatter_nonproliferation_treaty_signatory");
        
        if (hasWar || hasBreacher || hasSignatory) {
            return; // Halt progress
        }

        // --- STAGE SYNCHRONIZATION ---
        boolean synced = false;
        
        if (stage == STAGE_INITIAL) {
            if (market.getSize() >= 9) { stage = STAGE_GLOBAL; synced = true; }
            else if (market.getSize() == 8) { stage = STAGE_CONTINENTAL; synced = true; }
            else if (market.getSize() == 7) { stage = STAGE_SMALL_UNION; synced = true; }
            else if (market.getSize() == 6) { stage = STAGE_MICROSTATES; synced = true; }
        }
        else if (stage == STAGE_MICROSTATES && market.getSize() >= 7) { stage = STAGE_SMALL_UNION; synced = true; }
        else if (stage == STAGE_SMALL_UNION && market.getSize() >= 8) { stage = STAGE_CONTINENTAL; synced = true; }
        else if (stage == STAGE_CONTINENTAL && market.getSize() >= 9) { stage = STAGE_GLOBAL; synced = true; }
        
        if (synced) {
            mem.set(KEY_STAGE, stage);
            mem.set(KEY_TICKS, 0f);
            market.reapplyCondition(ID);
            return;
        }

        // Check for Soft Block
        boolean forbidden = mem.getBoolean("$unification_forbidden");
        float increment = forbidden ? 0.5f : 1.0f;

        // Increment Ticks
        float ticks = 0f;
        if (mem.contains(KEY_TICKS)) ticks = mem.getFloat(KEY_TICKS);
        ticks += increment;
        
        // --- PROGRESSION LOGIC ---

        // Stage 1: Microstates Annexed (should skip if size == 6)
        if (stage == STAGE_INITIAL && ticks >= DURATION_INITIAL) {
            stage = STAGE_MICROSTATES;
            mem.set(KEY_STAGE, stage);
            mem.set(KEY_TICKS, 0f);
            
            if (market.getSize() < 6) {
                incrementMarketSize(6);
                if (market.isPlayerOwned()) {
                    String text = "Our diplomatic overtures have succeeded in integrating a dozen independent city-states and micro-nations surrounding our primary settlement. While not a global power shift, this consolidation provides a stable buffer zone and legitimizes our presence on the world stage.";
                    sendMicrostatesIntel(stage, text);
                }
            }
            market.reapplyCondition(ID); 
            return;
        }

        // Stage 2: Small Union
        if (stage == STAGE_MICROSTATES && ticks >= DURATION_MICROSTATES) {
            stage = STAGE_SMALL_UNION;
            mem.set(KEY_STAGE, stage);
            mem.set(KEY_TICKS, 0f);
            
            if (market.getSize() < 7) {
                incrementMarketSize(7);
                if (market.isPlayerOwned()) {
                    String text = "Negotiations between our diplomatic teams and several small factions have led to the integration or functional control of a small slice of " + market.getName() + ". Local bureaucracies have been standardized to our protocols, and the borders are beginning to blur.";
                    sendUnificationIntel(stage, text);
                }
            }
            market.reapplyCondition(ID); 
            return;
        }
        
        // Stage 3: Continental Block
        if (stage == STAGE_SMALL_UNION && ticks >= DURATION_SMALL_UNION) {
            stage = STAGE_CONTINENTAL;
            mem.set(KEY_STAGE, stage);
            mem.set(KEY_TICKS, 0f);
            
            if (market.getSize() < 8) {
                incrementMarketSize(8);
                if (market.isPlayerOwned()) {
                    String text = "Through influence, soft power, and covert action, we have acquired functional control over an entire continent on " + market.getName() + ". However, the remaining superpowers have entrenched themselves, their leadership conscious of our plans, and digging in for a prolonged campaign.";
                    sendUnificationIntel(stage, text);
                }
            }
            market.reapplyCondition(ID);
            return;
        }

        // Stage 4: Global Union
        if (stage == STAGE_CONTINENTAL && ticks >= DURATION_CONTINENTAL) {
            stage = STAGE_GLOBAL;
            mem.set(KEY_STAGE, stage);
            mem.set(KEY_TICKS, 0f);

            if (market.getSize() < 9) {
                incrementMarketSize(9);
                if (market.isPlayerOwned()) {
                    String text = "Though the flags of many factions still wave on " + market.getName() + ", the entire planet is run at a distance by our administrators, cooperative politicians, and generals. In time these flags will be lowered and forgotten as our insignia takes their place. The world is ours.";
                    sendUnificationIntel(stage, text);
                }
            }
            market.reapplyCondition(ID);
            return;
        }
        
        mem.set(KEY_TICKS, ticks);
    }

    private void incrementMarketSize(int targetSize) {
        int currentSize = market.getSize();
        String oldCond = "population_" + currentSize;
        if (market.hasCondition(oldCond)) {
            market.removeCondition(oldCond);
        }
        market.setSize(targetSize);
        String newCond = "population_" + targetSize;
        market.addCondition(newCond);
    }

    private void sendMicrostatesIntel(final int stage, final String text) {
        final String marketName = market.getName();
        final MarketAPI marketRef = market; 
        
        Global.getSector().getIntelManager().addIntel(new BaseIntelPlugin() {
            @Override public String getName() { return "Microstates Annexed"; }
            @Override public String getIcon() { return "graphics/icons/markets/urbanized_polity.png"; }
            @Override public Set<String> getIntelTags(SectorMapAPI map) {
                Set<String> tags = super.getIntelTags(map);
                tags.add("Colony events");
                return tags;
            }
            @Override public SectorEntityToken getMapLocation(SectorMapAPI map) { return marketRef.getPrimaryEntity(); }
            @Override public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
                Color c = Misc.getBasePlayerColor();
                info.addPara(getName(), c, 0f);
                info.addPara("Regional expansion on " + marketName, 0f, Misc.getGrayColor(), Misc.getHighlightColor(), marketName);
            }
            @Override public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
                Color h = Misc.getHighlightColor();
                float pad = 10f;
                String imagePath = "graphics/illustrations/covert_squad.jpg";
                info.addImage(imagePath, width, pad);
                info.addPara(text, pad, h, marketName);
                info.addPara("Buffer zone established.", pad);
                bullet(info);
                info.addPara("Population size increased to 6", 0f, Misc.getPositiveHighlightColor(), "6");
                info.addPara("Influence expanded", 0f, Misc.getPositiveHighlightColor(), "expanded");
                unindent(info);
            }
        }, false);
    }

    private void sendUnificationIntel(final int stage, final String text) {
        final String marketName = market.getName();
        final MarketAPI marketRef = market; 
        
        final String title;
        if (stage == STAGE_SMALL_UNION) title = "Regional Integration";
        else if (stage == STAGE_CONTINENTAL) title = "Continental Unification";
        else title = "Planetary Subjugation";

        Global.getSector().getIntelManager().addIntel(new BaseIntelPlugin() {
            @Override public String getName() { return title; }
            @Override public String getIcon() { return "graphics/icons/markets/urbanized_polity.png"; }
            @Override public Set<String> getIntelTags(SectorMapAPI map) {
                Set<String> tags = super.getIntelTags(map);
                tags.add("Colony events");
                return tags;
            }
            @Override public SectorEntityToken getMapLocation(SectorMapAPI map) { return marketRef.getPrimaryEntity(); }
            @Override public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
                Color c = Misc.getBasePlayerColor();
                info.addPara(getName(), c, 0f);
                info.addPara("Political development on " + marketName, 0f, Misc.getGrayColor(), Misc.getHighlightColor(), marketName);
            }
            @Override public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
                Color h = Misc.getHighlightColor();
                float pad = 10f;
                String imagePath = "graphics/illustrations/covert_squad.jpg";
                info.addImage(imagePath, width, pad);
                info.addPara(text, pad, h, marketName);
                
                if (stage == STAGE_SMALL_UNION) {
                    info.addPara("Local power structures integrated.", pad);
                    bullet(info);
                    info.addPara("Population size increased to 7", 0f, Misc.getPositiveHighlightColor(), "7");
                    info.addPara("Upkeep reduced to (2x)", 0f, Misc.getPositiveHighlightColor(), "reduced");
                    unindent(info);
                } else if (stage == STAGE_CONTINENTAL) {
                    info.addPara("Continental power bloc secured.", pad);
                    bullet(info);
                    info.addPara("Population size increased to 8", 0f, Misc.getPositiveHighlightColor(), "8");
                    info.addPara("Industry cap penalty removed", 0f, Misc.getPositiveHighlightColor(), "removed");
                    unindent(info);
                } else {
                    info.addPara("Global dominance achieved.", pad);
                    bullet(info);
                    info.addPara("Population size increased to 9", 0f, Misc.getPositiveHighlightColor(), "9");
                    info.addPara("Industry cap increased (+1)", 0f, Misc.getPositiveHighlightColor(), "+1");
                    unindent(info);
                }
            }
        }, false);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        
        MemoryAPI mem = market.getMemoryWithoutUpdate();
        int stage = STAGE_INITIAL;
        if (mem.contains(KEY_STAGE)) stage = mem.getInt(KEY_STAGE);

        float pad = 10f;
        Color h = Misc.getHighlightColor();
        String mName = market.getName();

        // Civilization Description
        if (stage == STAGE_INITIAL) {
            tooltip.addPara("Civilization on " + mName + " still exists with a stable network of ruling polities laying claim to the entire planet. However, without spaceflight, the countries of " + mName + " cannot project into the system or sector.", pad);
        } else if (stage == STAGE_MICROSTATES) {
            tooltip.addPara("Civilization on " + mName + " is beginning to fracture. Several microstates and independent cities have aligned with us, but the major powers remain sovereign and largely unthreatened.", pad);
        } else if (stage == STAGE_SMALL_UNION) {
            tooltip.addPara("Civilization on " + mName + " is in flux. The once-stable network of ruling polities is being disrupted by the influx of off-world trade and ideology, though they still lack the means to project power outwards.", pad);
        } else if (stage == STAGE_CONTINENTAL) {
             tooltip.addPara("Civilization on " + mName + " has been irrevocably altered. The balance of power has shattered, with traditional nations falling into the orbit of a new, technologically superior continental hegemon.", pad);
        } else if (stage >= STAGE_GLOBAL) {
             tooltip.addPara("Civilization on " + mName + " has been fully integrated into the galactic community. The old nation-states are now merely administrative districts, their inability to project power replaced by the fleets of their new masters.", pad);
        }

        // Control Description
        tooltip.addSpacer(10f);
        if (stage <= STAGE_MICROSTATES) { 
            String desc = mName + " is a patchwork of defined factions, each with immense amounts of resources that the outpost on it must carefully navigate, should it prosper.";
            tooltip.addPara(desc, pad);
        } else if (stage == STAGE_SMALL_UNION) {
             String desc = mName + " has many power blocs, but a collective bargaining group of factions with a shared interest secretly obeys the interests of other worlds.";
             tooltip.addPara(desc, pad);
        } else if (stage == STAGE_CONTINENTAL) {
            String desc = "A continent on " + mName + " is a patchwork of official and informal economic, political, and cultural control operating at the behest of a spacefaring faction.";
            tooltip.addPara(desc, pad);
        } else if (stage >= STAGE_GLOBAL) {
            String desc = "The entire world operates under the thumb of a spacefaring faction. While local sovereignty is a convenient fiction, every nation, city-state, and corporation answers to the planetary governor, their resources tithed to fuel ambitions that span the sector.";
            tooltip.addPara(desc, pad);
        }

        // Officer Assessment
        tooltip.addSpacer(10f);
        if (market.isPlayerOwned()) {
            boolean hasWar = market.hasCondition("sol_world_war");
            boolean hasBreacher = market.hasCondition("sol_antimatter_nonproliferation_treaty_breacher");
            boolean hasSignatory = market.hasCondition("sol_antimatter_nonproliferation_treaty_signatory");
            boolean forbidden = mem.getBoolean("$unification_forbidden");

            if (hasWar) {
                tooltip.addPara("The ongoing world war on " + mName + " leaves the security apparatus of many factions jumpy, the leadership paranoid, and ultimately subsuming control infeasible given our limited knowledge and expertise.", pad);
            } 
            else if (hasBreacher) {
                tooltip.addPara("The consensus among your officers is grim. %s %s will be doomed before we can subsume the structures of power.", 
                    pad, h, "Without direct military action", mName);
            } 
            else if (hasSignatory) {
                int tier = -1;
                if (mem.contains(KEY_ANPT_TIER)) tier = mem.getInt(KEY_ANPT_TIER);
                
                if (tier == TIER_SECRET_BREACH) {
                    tooltip.addPara("Your officers say that peaceful unification is %s given our secret defection.", 
                        pad, h, "off the table");
                } else {
                    tooltip.addPara("The current ceasefire is tenuous, with prospects. While some officers believe that %s may still be doomed, most say the world will come to pass its antimatter crisis. They however believe that %s would be counterproductive to our ultimate goal.", 
                        pad, h, mName, "making any moves at this time");
                }
            } 
            else if (forbidden) {
                tooltip.addPara("While %s has weathered its antimatter crisis, our lackluster commitment to enforcement - given that the crisis was the Persean Sector's fault - has left us with little good will and the target for blame when it comes to blasted cities and countrysides in ruins, making it %s to subsume power in %s.", 
                    pad, h, mName, "significantly more difficult", mName);
            } 
            else {
                // Final Stage Check
                if (stage >= STAGE_GLOBAL) {
                    tooltip.addPara("Your officers weep for there is no more of " + mName + " left to conquer; instead, many of their colleagues have been transferred over to navigating the hellish bureaucracy and the constant suppression that now daunt the managers of your secret state.", pad);
                } else {
                    tooltip.addPara("With the end of conflict and tensions, our diplomatic teams and covert ops have begun carving out an invisible domain on " + mName + ".", pad);
                }
            }
        } 
        else {
            // Not owned logic
            String assessment = "Your officers believe that with %s, the planet's existing power structures could be subsumed and brought under your administration.";
            tooltip.addPara(assessment, pad, h, "sufficient time and resources");

            boolean hasWar = market.hasCondition("sol_world_war");
            boolean hasSignatory = market.hasCondition("sol_antimatter_nonproliferation_treaty_signatory");
            boolean hasBreacher = market.hasCondition("sol_antimatter_nonproliferation_treaty_breacher");

            if (hasWar || hasSignatory || hasBreacher) {
                String blocker = "The %s must first abate, however.";
                Color negative = Misc.getNegativeHighlightColor();
                tooltip.addPara(blocker, pad, negative, "ongoing geopolitical tensions");
            }
        } // Jesus christ.

        // Stats
        tooltip.addSpacer(10f);

        int size = market.getSize();
        float growthBonus = 20f;
        if (size > 3) growthBonus -= (size - 3) * 5f;
        String growthStr = (growthBonus >= 0 ? "+" : "") + (int)growthBonus;
        tooltip.addPara("%s Population growth", pad, h, growthStr);

        if (stage < STAGE_SMALL_UNION) {
            tooltip.addPara("%s Upkeep (Diplomatic Burden)", pad, h, "3x");
            
            if (size == 6) {
                tooltip.addPara("%s Population income", pad, h, "1.5x");
            } else {
                tooltip.addPara("%s Population income", pad, h, "2x");
            }
        } else {
            tooltip.addPara("%s Upkeep (Administrative Overhead)", pad, h, "2x");
        }

        if (stage < STAGE_CONTINENTAL) {
            tooltip.addPara("%s Maximum industries", pad, h, "-1");
        } else if (stage >= STAGE_GLOBAL) {
            tooltip.addPara("%s Maximum industries (Global Dominance)", pad, h, "+1");
        }
    }
}