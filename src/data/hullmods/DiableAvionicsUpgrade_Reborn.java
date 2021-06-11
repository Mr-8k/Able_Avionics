package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
//import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import java.awt.Color;
import java.util.EnumSet;
import java.util.HashMap;
//import java.util.HashSet;
import java.util.Map;
//import java.util.Set;

public class DiableAvionicsUpgrade_Reborn extends BaseHullMod {

//    private final float SHIELD_BONUS_UNFOLD = 200f;
    private final float CHECK=1f;
    private float timer=0, previous=0;
    private final String ID="Target Analysis";
        
//    @Override
//    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
//        //unfold rate
//        stats.getShieldUnfoldRateMult().modifyPercent(id, SHIELD_BONUS_UNFOLD);	
//    }
    
        private static class BuffData {
        float increment;
        float autoaimMin;
        float autoaimMax;
        float accelBuff;
        float turnBuff;
        float weaponTurn;
        float shieldUnfold;
        float shieldTurn;

        public BuffData(float increment, float autoaimMin, float autoaimMax, float accelBuff, float turnBuff, float weaponTurn, float shieldUnfold, float shieldTurn) {
            this.increment = increment;
            //the effect ranges from 0 to 1. ie an increment of 1 goes straight from 0 to 1, while an increment of 0.25 needs 4 steps.
            this.autoaimMin = autoaimMin;
            this.autoaimMax = autoaimMax;
            this.accelBuff = accelBuff;
            this.turnBuff = turnBuff;
            this.weaponTurn = weaponTurn;
            this.shieldUnfold = shieldUnfold;
            this.shieldTurn = shieldTurn;
        }
    }    
    
    private static final BuffData powerBuff = new BuffData(1f/4f, -15, +100, 100, 100, 50,
            100, 100);

    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        timer=Global.getCombatEngine().getTotalElapsedTime(false);
        
        ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
        if(previous>=timer || timer>previous+CHECK || ship==playerShip){
            
            if(ship!=playerShip){
                previous=timer;
            }

            int tick;

            switch (ship.getHullSize()) {
                case FRIGATE:
                    tick = (int)ship.getTimeDeployedForCRReduction()/30;
                    break;
                case DESTROYER:
                    tick = (int)ship.getTimeDeployedForCRReduction()/45;
                    break;
                case CRUISER:
                    tick = (int)ship.getTimeDeployedForCRReduction()/60;
                    break;
                case CAPITAL_SHIP:
                    tick = (int)ship.getTimeDeployedForCRReduction()/75;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + ship.getHullSize());
            }


            float effectLevel = Math.min(1, (powerBuff.increment)*tick);
            float autoaimEffect = (powerBuff.autoaimMin) + effectLevel * ((powerBuff.autoaimMax)-(powerBuff.autoaimMin));

            ship.getMutableStats().getWeaponTurnRateBonus().modifyMult(ID, 1+((powerBuff.weaponTurn)*effectLevel/100));            
            ship.getMutableStats().getAutofireAimAccuracy().modifyMult(ID, 1+(autoaimEffect/100));
            ship.getMutableStats().getRecoilDecayMult().modifyMult(ID, 1+(autoaimEffect/100));
            ship.getMutableStats().getRecoilPerShotMult().modifyMult(ID, 1-(autoaimEffect/100));   

            ship.getMutableStats().getAcceleration().modifyMult(ID, 1+((powerBuff.accelBuff)*effectLevel/100));
            ship.getMutableStats().getDeceleration().modifyMult(ID, 1+((powerBuff.accelBuff)*effectLevel/100));
            ship.getMutableStats().getTurnAcceleration().modifyMult(ID, 1+((powerBuff.turnBuff)*effectLevel/100));

            ship.getMutableStats().getShieldTurnRateMult().modifyMult(ID, 1+((powerBuff.shieldTurn)*effectLevel/100));
            ship.getMutableStats().getShieldUnfoldRateMult().modifyMult(ID, 1+((powerBuff.shieldUnfold)*effectLevel/100));

            if (effectLevel >= 1) {
                ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult(ID, 1.1f);
                ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult(ID, 1.1f);
            }

            EnumSet WEAPON_TYPES = EnumSet.of(WeaponType.BALLISTIC,WeaponType.ENERGY);

/*            if (0.01f < effectLevel && effectLevel <  0.25f){
                ship.setWeaponGlow(
                    1f,
                        new Color(0, 127, 0, 64),
                        WEAPON_TYPES);
            }else if (effectLevel < 0.5f){
                ship.setWeaponGlow(
                        0f,
                        new Color(0,0,0),
                        WEAPON_TYPES);
            }else*/ if (effectLevel == 0.75f){
                ship.setWeaponGlow(
                        0.75f,
                        new Color(255,127,0, 128),
                        WEAPON_TYPES);
            }else if (effectLevel == 1f){
                ship.setWeaponGlow(
                        1f,
                        new Color(255,0,0,128),
                        WEAPON_TYPES);
            }

            if(ship==playerShip){
                Global.getCombatEngine().maintainStatusForPlayerShip(
                        "AdvancedAvionicsBoost",
                        "graphics/icons/hullsys/high_energy_focus.png",
                        "Pattern Analysis:",
                        Math.round(effectLevel*100)+"% complete.",
                        effectLevel < 0.25f);
            }
        }
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "-15%";
        }
        if (index == 1) {
            return "30/45/60/75 seconds";
        }
        if (index == 2) {
            return "120/180/240/300 seconds";
        }
        if (index == 3) {
            return "100%";
        }
        if (index == 4) {
            return "100%";
        }
        if (index == 5) {
            return "100%";
        }
        if (index == 6) {
            return "10%";
        }
        return null;
    }

}
