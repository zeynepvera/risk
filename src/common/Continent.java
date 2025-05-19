package common;

import java.io.Serializable;

/**
 * Kıta sınıfı. Oyun haritasındaki kıtaları temsil eder.
 */
public class Continent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;  // Kıta adı
    private int bonus;    // Kıtayı tamamen ele geçirme bonusu
    
    public Continent(String name, int bonus) {
        this.name = name;
        this.bonus = bonus;
    }
    
    public String getName() {
        return name;
    }
    
    public int getBonus() {
        return bonus;
    }
}
