package com.riskgame.common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Oyuncu sınıfı. Bir oyuncuyu temsil eder.
 */
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private Set<String> territories;
    private int reinforcementArmies;
    
    public Player(String name) {
        this.name = name;
        this.territories = new HashSet<>();
        this.reinforcementArmies = 0;
    }
    
    public void addTerritory(String territory) {
        territories.add(territory);
    }
    
    public void removeTerritory(String territory) {
        territories.remove(territory);
    }
    
    public String getName() {
        return name;
    }
    
    public Set<String> getTerritories() {
        return territories;
    }
    
    public int getReinforcementArmies() {
        return reinforcementArmies;
    }
    
    public void setReinforcementArmies(int reinforcementArmies) {
        this.reinforcementArmies = reinforcementArmies;
    }
}