package common;

import java.io.Serializable;
import java.util.Arrays;


/**
 * Oyun içinde gerçekleştirilen bir hareketi temsil eden sınıf.
 */
public class GameAction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private ActionType type;          // Hareket türü
    private String sourceTerritory;   // Kaynak bölge
    private String targetTerritory;   // Hedef bölge
    private int armyCount;            // Birlik sayısı
    
     // Zar sonuçları - Bu alanları ekleyin
    private int[] attackDice;
    private int[] defenseDice;
    
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
    
    
     // Zar sonuçları için getter ve setter metodları ekleyin
    public int[] getAttackDice() {
        return attackDice;
    }
    
    public void setAttackDice(int[] attackDice) {
        this.attackDice = attackDice;
    }
    
    public int[] getDefenseDice() {
        return defenseDice;
    }
    
    public void setDefenseDice(int[] defenseDice) {
        this.defenseDice = defenseDice;
    }
    
    @Override
    public String toString() {
        return "GameAction{type=" + type + 
               ", source='" + sourceTerritory + "', target='" + targetTerritory + 
               "', armies=" + armyCount + 
               ", attackDice=" + (attackDice != null ? Arrays.toString(attackDice) : "null") + 
               ", defenseDice=" + (defenseDice != null ? Arrays.toString(defenseDice) : "null") + 
               "}";
    }

    
}
