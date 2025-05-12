
package server;

import common.Territory;
import java.util.*;

public class GameEngine {

    private Map<String, Territory> territories;
    private String currentPlayer;
    private List<String> players;

    public GameEngine(List<String> players) {
        this.players = players;
        this.currentPlayer = players.get(0); // Oyuna ilk oyuncu başlar
        initializeTerritories();
        assignInitialTerritories();
    }

    private void initializeTerritories() {
        territories = new HashMap<>();
        // Örnek harita: 6 bölge
        territories.put("A", new Territory("A", Arrays.asList("B", "C")));
        territories.put("B", new Territory("B", Arrays.asList("A", "D")));
        territories.put("C", new Territory("C", Arrays.asList("A", "D")));
        territories.put("D", new Territory("D", Arrays.asList("B", "C", "E")));
        territories.put("E", new Territory("E", Arrays.asList("D", "F")));
        territories.put("F", new Territory("F", Arrays.asList("E")));
    }

    private void assignInitialTerritories() {
        // Oyuncular sırayla bölge alır (basit dağıtım)
        int i = 0;
        for (String name : territories.keySet()) {
            String owner = players.get(i % players.size());
            territories.get(name).setOwner(owner);
            territories.get(name).setTroops(1); // Başlangıçta 1 asker
            i++;
        }
    }

    public Map<String, Territory> getTerritories() {
        return territories;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public void nextTurn() {
        int index = players.indexOf(currentPlayer);
        currentPlayer = players.get((index + 1) % players.size());
    }

    // Daha sonra: attack(), placeTroops(), moveTroops() gibi metodlar eklenecek
    
    
    public boolean placeTroops(String player, String territoryName, int troopCount) {
    Territory territory = territories.get(territoryName);
    if (territory == null) {
        System.out.println("Bolge bulunamadı: " + territoryName);
        return false;
    }

    if (!territory.getOwner().equals(player)) {
        System.out.println("Oyuncu bu bolgenin sahibi degil.");
        return false;
    }

    territory.addTroops(troopCount);
    System.out.println(player + " oyuncusu " + territoryName + " bolgesine " + troopCount + " asker ekledi.");
    return true;
}
    
    public boolean attack(String attacker, String from, String to) {
    Territory fromTerritory = territories.get(from);
    Territory toTerritory = territories.get(to);

    // Geçersiz bölge kontrolü
    if (fromTerritory == null || toTerritory == null) {
        System.out.println("Bölgelerden biri bulunamadı.");
        return false;
    }

    // Sahiplik kontrolü
    if (!fromTerritory.getOwner().equals(attacker)) {
        System.out.println("Bu bolge oyuncuya ait degil: " + from);
        return false;
    }

    if (toTerritory.getOwner().equals(attacker)) {
        System.out.println("Kendi bolgesine saldiramaz.");
        return false;
    }

    // Komşuluk kontrolü
    if (!fromTerritory.getNeighbors().contains(to)) {
        System.out.println("Bolgeler komşu degil.");
        return false;
    }

    // Asker kontrolü
    if (fromTerritory.getTroops() < 2) {
        System.out.println("Saldiri icin yeterli asker yok.");
        return false;
    }

    // Zar mekaniği (basitleştirilmiş)
    int attackRoll = (int) (Math.random() * 6 + 1);
    int defendRoll = (int) (Math.random() * 6 + 1);

    System.out.println("Saldiri zari: " + attackRoll + " | Savunma zari: " + defendRoll);

    if (attackRoll > defendRoll) {
        // Savunma kaybeder
        toTerritory.setOwner(attacker);
        toTerritory.setTroops(fromTerritory.getTroops() - 1);
        fromTerritory.setTroops(1);
        System.out.println(attacker + ", " + to + " bolgesini ele gecirdi!");
    } else {
        // Saldıran kaybeder
        fromTerritory.removeTroops(1);
        System.out.println(attacker + " saldirisinda basarisiz oldu.");
    }

    return true;
}


}
