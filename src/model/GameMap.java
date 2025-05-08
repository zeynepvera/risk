/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author WINCHESTER
 */
public class GameMap {
     private HashMap<String, Territory> territories;
    private HashMap<String, List<String>> neighbors;

    public GameMap() {
        territories = new HashMap<>();
        neighbors = new HashMap<>();

        // 6 basit bölge tanımlıyoruz
        String[] names = {"A", "B", "C", "D", "E", "F"};
        for (String name : names) {
            territories.put(name, new Territory(name));
            neighbors.put(name, new ArrayList<>());
        }

        // Komşulukları manuel belirliyoruz (basit örnek)
        neighbors.get("A").addAll(Arrays.asList("B", "C"));
        neighbors.get("B").addAll(Arrays.asList("A", "D"));
        neighbors.get("C").addAll(Arrays.asList("A", "E"));
        neighbors.get("D").addAll(Arrays.asList("B", "F"));
        neighbors.get("E").addAll(Arrays.asList("C", "F"));
        neighbors.get("F").addAll(Arrays.asList("D", "E"));
    }

    public Territory getTerritory(String name) {
        return territories.get(name);
    }

    public List<String> getNeighbors(String name) {
        return neighbors.get(name);
    }

    public Collection<Territory> getAllTerritories() {
        return territories.values();
    }
    
}
