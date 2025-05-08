/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.ArrayList;

/**
 *
 * @author WINCHESTER
 */
public class Player {
      private int id;
    private String name;
    private ArrayList<Territory> ownedTerritories;

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.ownedTerritories = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void addTerritory(Territory t) {
        this.ownedTerritories.add(t);
    }

    public void removeTerritory(Territory t) {
        this.ownedTerritories.remove(t);
    }

    public ArrayList<Territory> getOwnedTerritories() {
        return ownedTerritories;
    }

    public boolean isAlive() {
        return !ownedTerritories.isEmpty();
    }
    
}
