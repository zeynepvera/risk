package com.riskgame.common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Bölge sınıfı. Oyun haritasındaki bir bölgeyi temsil eder.
 */
public class Territory implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String continent;
    private String owner;
    private int armies;
    private Set<String> neighbors;
    
    public Territory(String name, String continent) {
        this.name = name;
        this.continent = continent;
        this.armies = 0;
        this.neighbors = new HashSet<>();
    }
    
    public void addNeighbor(String territory) {
        neighbors.add(territory);
    }
    
    public boolean isNeighbor(String territory) {
        return neighbors.contains(territory);
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    public void setArmies(int armies) {
        this.armies = armies;
    }
    
    public void addArmies(int count) {
        this.armies += count;
    }
    
    public void removeArmies(int count) {
        this.armies = Math.max(0, this.armies - count);
    }
    
    public String getName() {
        return name;
    }
    
    public String getContinent() {
        return continent;
    }
    
    public String getOwner() {
        return owner;
    }
    
    public int getArmies() {
        return armies;
    }
    
    public Set<String> getNeighbors() {
        return neighbors;
    }
}
