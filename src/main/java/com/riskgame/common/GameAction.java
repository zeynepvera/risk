package com.riskgame.common;

import java.io.Serializable;

/**
 * Oyun içinde gerçekleştirilen bir hareketi temsil eden sınıf.
 */
public class GameAction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private ActionType type;          // Hareket türü
    private String sourceTerritory;   // Kaynak bölge
    private String targetTerritory;   // Hedef bölge
    private int armyCount;            // Birlik sayısı
    
    public GameAction(ActionType type, String sourceTerritory, String targetTerritory, int armyCount) {
        this.type = type;
        this.sourceTerritory = sourceTerritory;
        this.targetTerritory = targetTerritory;
        this.armyCount = armyCount;
    }
    
    public ActionType getType() {
        return type;
    }
    
    public String getSourceTerritory() {
        return sourceTerritory;
    }
    
    public String getTargetTerritory() {
        return targetTerritory;
    }
    
    public int getArmyCount() {
        return armyCount;
    }
}
