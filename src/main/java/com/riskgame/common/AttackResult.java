package com.riskgame.common;

import java.io.Serializable;

/**
 * Saldırı sonucunu temsil eden sınıf.
 */
public class AttackResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean conquered;        // Bölge ele geçirildi mi
    private String description;       // Saldırı sonucu açıklaması
    private String eliminatedPlayer;  // Elenen oyuncu (varsa)
    
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
}
