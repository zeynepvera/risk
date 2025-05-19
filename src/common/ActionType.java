package common;

import java.io.Serializable;

/**
 * Oyunda gerçekleştirilebilecek hareket türleri.
 */
public enum ActionType implements Serializable {
    PLACE_ARMY,  // Birlik yerleştirme
    ATTACK,      // Saldırı
    FORTIFY,     // Takviye
    END_TURN     // Turu bitirme
}