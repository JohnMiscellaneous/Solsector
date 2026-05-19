package soljars.gen.utils;

import com.fs.starfarer.api.campaign.SectorEntityToken;

import soljars.gen.utils.AstroCalc.CompoundOrbit;
import soljars.gen.utils.AstroCalc.KeplerComponent;

// wraper of compoundorbit for janino console
public class CompoundOrbitTool {

    public static void attach(SectorEntityToken entity,
                              SectorEntityToken focus,
                              KeplerComponent[] comps) {
        CompoundOrbit orbit = new CompoundOrbit(focus, comps);
        orbit.setEntity(entity);
        entity.setOrbit(orbit);
        orbit.advance(0f);
    }

    // spin variant
    public static void attachWithSpin(SectorEntityToken entity,
                                      SectorEntityToken focus,
                                      KeplerComponent[] comps,
                                      float spinMin,
                                      float spinMax) {
        java.util.ArrayList list = new java.util.ArrayList(comps.length);
        for (int i = 0; i < comps.length; i++) list.add(comps[i]);
        CompoundOrbit orbit = new CompoundOrbit(focus, list, spinMin, spinMax);
        orbit.setEntity(entity);
        entity.setOrbit(orbit);
        orbit.advance(0f);
    }
    
    // does a faux moon
    public static void attachFaux(SectorEntityToken entity,
                                  SectorEntityToken declaredFocus,
                                  SectorEntityToken anchor,
                                  KeplerComponent[] comps) {
        java.util.ArrayList list = new java.util.ArrayList(comps.length);
        for (int i = 0; i < comps.length; i++) list.add(comps[i]);
        CompoundOrbit orbit = new CompoundOrbit(declaredFocus, anchor, list);
        orbit.setEntity(entity);
        entity.setOrbit(orbit);
        orbit.advance(0f);
    }

    public static KeplerComponent kc(float sma, float ecc, float longPeri,
                                     float period, float meanAnomaly, float sign) {
        return new KeplerComponent(sma, ecc, longPeri, period, meanAnomaly, sign);
    }
}