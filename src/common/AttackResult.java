package common;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Saldırı sonucunu temsil eden sınıf.
 */
public class AttackResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean conquered;        // Bölge ele geçirildi mi
    private String description;       // Saldırı sonucu açıklaması
    private String eliminatedPlayer;  // Elenen oyuncu (varsa)
    
    // Yeni eklenen alanlar
    private int[] attackDice;         // Saldıran zarları
    private int[] defenseDice;        // Savunan zarları
    private int attackerLosses;       // Saldıran kayıpları
    private int defenderLosses;       // Savunan kayıpları
    
    public AttackResult(boolean conquered, String description, String eliminatedPlayer) {
        this.conquered = conquered;
        this.description = description;
        this.eliminatedPlayer = eliminatedPlayer;
    }
    
    public boolean isConquered() {
        return conquered;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getEliminatedPlayer() {
        return eliminatedPlayer;
    }
    
    // Yeni eklenen alanlar için getter/setter metodları
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
    
    public int getAttackerLosses() {
        return attackerLosses;
    }
    
    public void setAttackerLosses(int attackerLosses) {
        this.attackerLosses = attackerLosses;
    }
    
    public int getDefenderLosses() {
        return defenderLosses;
    }
    
    public void setDefenderLosses(int defenderLosses) {
        this.defenderLosses = defenderLosses;
    }
    
    @Override
    public String toString() {
        return "AttackResult{conquered=" + conquered + 
               ", attackDice=" + (attackDice != null ? Arrays.toString(attackDice) : "null") + 
               ", defenseDice=" + (defenseDice != null ? Arrays.toString(defenseDice) : "null") + 
               ", attackerLosses=" + attackerLosses + 
               ", defenderLosses=" + defenderLosses + 
               ", eliminatedPlayer='" + eliminatedPlayer + "'}";
    }
}