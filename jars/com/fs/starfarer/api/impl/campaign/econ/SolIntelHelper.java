package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SolIntelHelper {

    private String name;
    private String iconPath;
    private String tag = "Colony events";
    private MarketAPI market;
    private SectorEntityToken mapEntity; 
    private String subtitle;
    private String subtitleHighlight; 
    
    
    private String imagePath;
    private String imageSpriteCategory; 
    private String imageSpriteId;
    private String descriptionText;
    private String[] descriptionHighlights;
    private String summaryText;
    private final List<BulletItem> bullets = new ArrayList<>();

    public static class BulletItem {
        public final String text;
        public final String highlight;
        public final Color color;

        public BulletItem(String text, String highlight, Color color) {
            this.text = text;
            this.highlight = highlight;
            this.color = color;
        }
    }

    private SolIntelHelper() {}

    // =========================================================================
    // BUILDER ENTRY POINT
    // =========================================================================

    public static SolIntelHelper create(String name, String iconPath) {
        SolIntelHelper h = new SolIntelHelper();
        h.name = name;
        h.iconPath = iconPath;
        return h;
    }

    // =========================================================================
    // BUILDER METHODS
    // =========================================================================

    public SolIntelHelper market(MarketAPI market) {
        this.market = market;
        return this;
    }

    public SolIntelHelper mapEntity(SectorEntityToken entity) {
        this.mapEntity = entity;
        return this;
    }

    public SolIntelHelper tag(String tag) {
        this.tag = tag;
        return this;
    }

    public SolIntelHelper subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public SolIntelHelper subtitleHighlight(String highlight) {
        this.subtitleHighlight = highlight;
        return this;
    }

    public SolIntelHelper image(String spriteCategory, String spriteId) {
        this.imageSpriteCategory = spriteCategory;
        this.imageSpriteId = spriteId;
        return this;
    }

    public SolIntelHelper imagePath(String path) {
        this.imagePath = path;
        return this;
    }

    public SolIntelHelper description(String text, String... highlights) {
        this.descriptionText = text;
        this.descriptionHighlights = highlights;
        return this;
    }

    public SolIntelHelper summary(String text) {
        this.summaryText = text;
        return this;
    }

    public SolIntelHelper bulletNeg(String text, String highlight) {
        bullets.add(new BulletItem(text, highlight, Misc.getNegativeHighlightColor()));
        return this;
    }

    public SolIntelHelper bulletPos(String text, String highlight) {
        bullets.add(new BulletItem(text, highlight, Misc.getPositiveHighlightColor()));
        return this;
    }

    public SolIntelHelper bulletHL(String text, String highlight) {
        bullets.add(new BulletItem(text, highlight, Misc.getHighlightColor()));
        return this;
    }

    public SolIntelHelper bullet(String text, String highlight, Color color) {
        bullets.add(new BulletItem(text, highlight, color));
        return this;
    }

    // =========================================================================
    // SEND
    // =========================================================================

    public void send() {
        send(false);
    }

    public void send(boolean showMessage) {
        final String fName = this.name;
        final String fIconPath = this.iconPath;
        final String fTag = this.tag;
        final MarketAPI fMarket = this.market;
        final SectorEntityToken fMapEntity = this.mapEntity;
        final String fSubtitle = this.subtitle;
        final String fSubtitleHL = (this.subtitleHighlight != null) 
            ? this.subtitleHighlight 
            : (fMarket != null ? fMarket.getName() : null);
        final String fImagePath = this.imagePath;
        final String fImageCat = this.imageSpriteCategory;
        final String fImageId = this.imageSpriteId;
        final String fDescText = this.descriptionText;
        final String[] fDescHL = this.descriptionHighlights;
        final String fSummary = this.summaryText;
        final List<BulletItem> fBullets = new ArrayList<>(this.bullets);

        Global.getSector().getIntelManager().addIntel(new BaseIntelPlugin() {
            @Override
            public String getName() {
                return fName;
            }

            @Override
            public String getIcon() {
                return fIconPath;
            }

            @Override
            public Set<String> getIntelTags(SectorMapAPI map) {
                Set<String> tags = super.getIntelTags(map);
                if (fTag != null) tags.add(fTag);
                return tags;
            }

            @Override
            public SectorEntityToken getMapLocation(SectorMapAPI map) {
                if (fMapEntity != null) return fMapEntity;
                if (fMarket != null) return fMarket.getPrimaryEntity();
                return null;
            }

            @Override
            public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
                Color c = Misc.getBasePlayerColor();
                info.addPara(getName(), c, 0f);
                if (fSubtitle != null) {
                    bullet(info);
                    if (fSubtitleHL != null) {
                        info.addPara(fSubtitle, 0f, Misc.getGrayColor(), Misc.getHighlightColor(), fSubtitleHL);
                    } else {
                        info.addPara(fSubtitle, 0f, Misc.getGrayColor());
                    }
                    unindent(info);
                }
            }

            @Override
            public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
                Color h = Misc.getHighlightColor();
                float pad = 10f;

                // Image
                if (fImageCat != null && fImageId != null) {
                    String resolved = Global.getSettings().getSpriteName(fImageCat, fImageId);
                    info.addImage(resolved, width, pad);
                } else if (fImagePath != null) {
                    info.addImage(fImagePath, width, pad);
                }

                // Description
                if (fDescText != null) {
                    if (fDescHL != null && fDescHL.length > 0) {
                        info.addPara(fDescText, pad, h, fDescHL);
                    } else {
                        info.addPara(fDescText, pad);
                    }
                }

                // Summary + bullets
                if (fSummary != null) {
                    info.addPara(fSummary, pad);
                }
                if (!fBullets.isEmpty()) {
                    bullet(info);
                    for (BulletItem item : fBullets) {
                        info.addPara(item.text, 0f, item.color, item.highlight);
                    }
                    unindent(info);
                }
            }
        }, showMessage);
    }

    // =========================================================================
    // CHAIN INTEL (persistent, multi-update)
    // =========================================================================

    private static final Map<String, SolChainIntel> chainRegistry = new LinkedHashMap<>();

    public static SolChainIntel createChain(String chainId, String name, String iconPath) {

        SolChainIntel existing = getChain(chainId);
        if (existing != null) return existing;

        SolChainIntel chain = new SolChainIntel(chainId, name, iconPath);
        chainRegistry.put(chainId, chain);
        Global.getSector().getIntelManager().addIntel(chain, false);
        return chain;
    }

   
    public static SolChainIntel getChain(String chainId) {
        SolChainIntel cached = chainRegistry.get(chainId);
        if (cached != null) return cached;

        for (Object raw : Global.getSector().getIntelManager().getIntel(SolChainIntel.class)) {
            SolChainIntel chain = (SolChainIntel) raw;
            if (chainId.equals(chain.getChainId())) {
                chainRegistry.put(chainId, chain);
                return chain;
            }
        }
        return null;
    }

    public static class SolChainIntel extends BaseIntelPlugin {

        private String chainId;
        private String name;
        private String iconPath;
        private String tag = "Colony events";
        private String marketId;
        private String imagePath;
        private String imageSpriteCategory;
        private String imageSpriteId;
        private final List<ChainUpdate> updates = new ArrayList<>();

        public SolChainIntel() {}

        public SolChainIntel(String chainId, String name, String iconPath) {
            this.chainId = chainId;
            this.name = name;
            this.iconPath = iconPath;
        }

        public String getChainId() { return chainId; }

        public SolChainIntel market(MarketAPI market) {
            this.marketId = (market != null) ? market.getId() : null;
            return this;
        }

        public SolChainIntel tag(String tag) {
            this.tag = tag;
            return this;
        }

        public SolChainIntel imagePath(String path) {
            this.imagePath = path;
            this.imageSpriteCategory = null;
            this.imageSpriteId = null;
            return this;
        }

        public SolChainIntel image(String spriteCategory, String spriteId) {
            this.imageSpriteCategory = spriteCategory;
            this.imageSpriteId = spriteId;
            this.imagePath = null;
            return this;
        }

        public void setName(String name) { this.name = name; }
        public void setIcon(String iconPath) { this.iconPath = iconPath; }

        public ChainUpdateBuilder addUpdate(String heading) {
            return new ChainUpdateBuilder(this, heading);
        }

        public List<ChainUpdate> getUpdates() {
            return updates;
        }

        public ChainUpdate getLatestUpdate() {
            if (updates.isEmpty()) return null;
            return updates.get(updates.size() - 1);
        }

        void pushUpdate(ChainUpdate update) {
            updates.add(update);
            chainRegistry.put(chainId, this);
        }

        private MarketAPI resolveMarket() {
            if (marketId == null) return null;
            return Global.getSector().getEconomy().getMarket(marketId);
        }

        @Override
        public String getName() { return name; }

        @Override
        public String getIcon() { return iconPath; }

        @Override
        public Set<String> getIntelTags(SectorMapAPI map) {
            Set<String> tags = super.getIntelTags(map);
            if (tag != null) tags.add(tag);
            return tags;
        }

        @Override
        public SectorEntityToken getMapLocation(SectorMapAPI map) {
            MarketAPI m = resolveMarket();
            if (m != null) return m.getPrimaryEntity();
            return null;
        }

        @Override
        public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
            Color c = Misc.getBasePlayerColor();
            info.addPara(getName(), c, 0f);

            ChainUpdate latest = getLatestUpdate();
            if (latest != null && latest.subtitle != null) {
                MarketAPI m = resolveMarket();
                String hl = (latest.subtitleHighlight != null) 
                    ? latest.subtitleHighlight 
                    : (m != null ? m.getName() : null);
                bullet(info);
                if (hl != null) {
                    info.addPara(latest.subtitle, 0f, Misc.getGrayColor(), Misc.getHighlightColor(), hl);
                } else {
                    info.addPara(latest.subtitle, 0f, Misc.getGrayColor());
                }
                unindent(info);
            }
        }

        @Override
        public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
            Color h = Misc.getHighlightColor();
            float pad = 10f;

            if (imageSpriteCategory != null && imageSpriteId != null) {
                String resolved = Global.getSettings().getSpriteName(imageSpriteCategory, imageSpriteId);
                info.addImage(resolved, width, pad);
            } else if (imagePath != null) {
                info.addImage(imagePath, width, pad);
            }

            for (int i = updates.size() - 1; i >= 0; i--) {
                ChainUpdate upd = updates.get(i);

                String heading = upd.heading;
                if (upd.timestamp != null) {
                    heading += " - " + upd.timestamp;
                }
                info.addSectionHeading(heading, Alignment.MID, pad);

                if (upd.descriptionText != null) {
                    if (upd.descriptionHighlights != null && upd.descriptionHighlights.length > 0) {
                        info.addPara(upd.descriptionText, pad, h, upd.descriptionHighlights);
                    } else {
                        info.addPara(upd.descriptionText, pad);
                    }
                }

                if (upd.summaryText != null) {
                    info.addPara(upd.summaryText, pad);
                }

                if (!upd.bullets.isEmpty()) {
                    bullet(info);
                    for (BulletItem item : upd.bullets) {
                        info.addPara(item.text, 0f, item.color, item.highlight);
                    }
                    unindent(info);
                }
            }
        }
    }

    // =========================================================================
    // ChainUpdate - a single entry in a chain
    // =========================================================================

    public static class ChainUpdate {
        public String heading;
        public String subtitle;
        public String subtitleHighlight;
        public String descriptionText;
        public String[] descriptionHighlights;
        public String summaryText;
        public String timestamp; // e.g. "Cycle 206, Month 3"
        public final List<BulletItem> bullets = new ArrayList<>();
    }

    // =========================================================================
    // ChainUpdateBuilder - fluent builder for a single update
    // =========================================================================

    public static class ChainUpdateBuilder {
        private final SolChainIntel parent;
        private final ChainUpdate update;

        ChainUpdateBuilder(SolChainIntel parent, String heading) {
            this.parent = parent;
            this.update = new ChainUpdate();
            this.update.heading = heading;
            this.update.timestamp = Global.getSector().getClock().getDateString();
        }

        public ChainUpdateBuilder subtitle(String subtitle) {
            update.subtitle = subtitle;
            return this;
        }

        public ChainUpdateBuilder subtitleHighlight(String highlight) {
            update.subtitleHighlight = highlight;
            return this;
        }

        public ChainUpdateBuilder description(String text, String... highlights) {
            update.descriptionText = text;
            update.descriptionHighlights = highlights;
            return this;
        }

        public ChainUpdateBuilder summary(String text) {
            update.summaryText = text;
            return this;
        }

        public ChainUpdateBuilder bulletNeg(String text, String highlight) {
            update.bullets.add(new BulletItem(text, highlight, Misc.getNegativeHighlightColor()));
            return this;
        }

        public ChainUpdateBuilder bulletPos(String text, String highlight) {
            update.bullets.add(new BulletItem(text, highlight, Misc.getPositiveHighlightColor()));
            return this;
        }

        public ChainUpdateBuilder bulletHL(String text, String highlight) {
            update.bullets.add(new BulletItem(text, highlight, Misc.getHighlightColor()));
            return this;
        }

        public ChainUpdateBuilder bullet(String text, String highlight, Color color) {
            update.bullets.add(new BulletItem(text, highlight, color));
            return this;
        }

        public ChainUpdateBuilder timestamp(String timestamp) {
            update.timestamp = timestamp;
            return this;
        }

        public SolChainIntel push() {
            parent.pushUpdate(update);
            return parent;
        }
    }
}