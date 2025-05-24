package server.game;

import common.Continent;
import common.Territory;
import common.Player;
import java.util.*;

/**
 * Kıta yönetimi ve bonus hesaplamalarından sorumlu sınıf
 */
public class ContinentManager {
    
    private Map<String, Territory> territories;
    private Map<String, Continent> continents;
    private Map<String, Player> players;
    
    public ContinentManager(Map<String, Territory> territories, 
                           Map<String, Continent> continents,
                           Map<String, Player> players) {
        this.territories = territories;
        this.continents = continents;
        this.players = players;
    }
    
    /**
     * Oyuncunun takviye birlik sayısını hesaplar
     */
    public int calculateReinforcementArmies(String playerName) {
        Player player = players.get(playerName);
        if (player == null) {
            return 0;
        }

        // Temel birlik: Bölge sayısı / 3 (minimum 3)
        int baseArmies = Math.max(3, player.getTerritories().size() / 3);

        // Kıta bonusu
        int continentBonus = calculateContinentBonus(playerName);

        System.out.println("\n=== TAKVİYE BİRLİK HESAPLAMA ===");
        System.out.println("Oyuncu: " + playerName);
        System.out.println("Sahip olunan bölge sayısı: " + player.getTerritories().size());
        System.out.println("Temel birlik (bölge/3, min 3): " + baseArmies);
        System.out.println("Kıta bonusu: " + continentBonus);
        System.out.println("Toplam takviye birlik: " + (baseArmies + continentBonus));
        System.out.println("=== HESAPLAMA TAMAMLANDI ===\n");

        return baseArmies + continentBonus;
    }
    
    /**
     * Oyuncunun kıta bonusunu hesaplar
     */
    public int calculateContinentBonus(String playerName) {
        int totalBonus = 0;
        
        for (Continent continent : continents.values()) {
            if (controlsContinent(playerName, continent.getName())) {
                totalBonus += continent.getBonus();
                System.out.println("🌍 " + playerName + " " + continent.getName() + 
                                 " kıtasını kontrol ediyor (Bonus: +" + continent.getBonus() + ")");
            }
        }
        
        return totalBonus;
    }
    
    /**
     * Oyuncunun belirli bir kıtayı tamamen kontrol edip etmediğini kontrol eder
     */
    public boolean controlsContinent(String playerName, String continentName) {
        for (Territory territory : territories.values()) {
            if (territory.getContinent().equals(continentName)) {
                if (!territory.getOwner().equals(playerName)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Oyuncunun kontrol ettiği kıtaları listeler
     */
    public List<String> getControlledContinents(String playerName) {
        List<String> controlledContinents = new ArrayList<>();
        
        for (Continent continent : continents.values()) {
            if (controlsContinent(playerName, continent.getName())) {
                controlledContinents.add(continent.getName());
            }
        }
        
        return controlledContinents;
    }
    
    /**
     * Belirli bir kıtada oyuncunun kaç bölgesi olduğunu hesaplar
     */
    public int getTerritoryCountInContinent(String playerName, String continentName) {
        int count = 0;
        
        for (Territory territory : territories.values()) {
            if (territory.getContinent().equals(continentName) && 
                territory.getOwner().equals(playerName)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Belirli bir kıtadaki toplam bölge sayısını döner
     */
    public int getTotalTerritoriesInContinent(String continentName) {
        int count = 0;
        
        for (Territory territory : territories.values()) {
            if (territory.getContinent().equals(continentName)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Kıta hakimiyet durumunu detaylı raporlar
     */
    public Map<String, ContinentStatus> getContinentStatusReport() {
        Map<String, ContinentStatus> report = new HashMap<>();
        
        for (Continent continent : continents.values()) {
            String continentName = continent.getName();
            Map<String, Integer> playerTerritories = new HashMap<>();
            int totalTerritories = 0;
            
            // Her oyuncunun bu kıtadaki bölge sayısını hesapla
            for (Territory territory : territories.values()) {
                if (territory.getContinent().equals(continentName)) {
                    totalTerritories++;
                    String owner = territory.getOwner();
                    playerTerritories.put(owner, playerTerritories.getOrDefault(owner, 0) + 1);
                }
            }
            
            // Dominant oyuncuyu bul
            String dominantPlayer = null;
            int maxTerritories = 0;
            for (Map.Entry<String, Integer> entry : playerTerritories.entrySet()) {
                if (entry.getValue() > maxTerritories) {
                    maxTerritories = entry.getValue();
                    dominantPlayer = entry.getKey();
                }
            }
            
            // Tam kontrol var mı?
            boolean fullyControlled = maxTerritories == totalTerritories;
            
            report.put(continentName, new ContinentStatus(
                continentName,
                continent.getBonus(),
                totalTerritories,
                playerTerritories,
                dominantPlayer,
                maxTerritories,
                fullyControlled
            ));
        }
        
        return report;
    }
    
    /**
     * Tüm oyuncuların kıta durumunu özetler
     */
    public void printContinentReport() {
        System.out.println("\n=== KITA DURUM RAPORU ===");
        
        Map<String, ContinentStatus> report = getContinentStatusReport();
        
        for (ContinentStatus status : report.values()) {
            System.out.println("\n🌍 " + status.continentName + " (Bonus: +" + status.bonus + ")");
            System.out.println("   Toplam bölge: " + status.totalTerritories);
            
            if (status.fullyControlled) {
                System.out.println("   ✅ TAM KONTROL: " + status.dominantPlayer + 
                                 " (" + status.maxTerritories + "/" + status.totalTerritories + ")");
            } else {
                System.out.println("   ⚡ MÜCADELE HALINDE:");
                for (Map.Entry<String, Integer> entry : status.playerTerritories.entrySet()) {
                    System.out.println("      " + entry.getKey() + ": " + entry.getValue() + 
                                     "/" + status.totalTerritories + " bölge");
                }
            }
        }
        
        System.out.println("=== RAPOR SONU ===\n");
    }
    
    /**
     * Kıta durumu bilgilerini tutan inner class
     */
    public static class ContinentStatus {
        public final String continentName;
        public final int bonus;
        public final int totalTerritories;
        public final Map<String, Integer> playerTerritories;
        public final String dominantPlayer;
        public final int maxTerritories;
        public final boolean fullyControlled;
        
        public ContinentStatus(String continentName, int bonus, int totalTerritories,
                              Map<String, Integer> playerTerritories, String dominantPlayer,
                              int maxTerritories, boolean fullyControlled) {
            this.continentName = continentName;
            this.bonus = bonus;
            this.totalTerritories = totalTerritories;
            this.playerTerritories = new HashMap<>(playerTerritories);
            this.dominantPlayer = dominantPlayer;
            this.maxTerritories = maxTerritories;
            this.fullyControlled = fullyControlled;
        }
    }
}