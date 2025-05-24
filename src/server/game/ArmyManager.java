package server.game;

import common.Territory;
import common.Player;
import java.util.*;

/**
 * Birlik yönetimi ve yerleştirme kurallarından sorumlu sınıf
 */
public class ArmyManager {
    
    private Map<String, Territory> territories;
    private Map<String, Player> players;
    
    public ArmyManager(Map<String, Territory> territories, Map<String, Player> players) {
        this.territories = territories;
        this.players = players;
    }
    
    /**
     * Oyuncunun takviye birlik sayısını ayarlar
     */
    public void setReinforcementArmies(String playerName, int armies) {
        Player player = players.get(playerName);
        if (player != null) {
            player.setReinforcementArmies(armies);
        }
    }
    
    /**
     * Birlik yerleştirilebilir mi kontrol eder
     */
    public boolean canPlaceArmy(String playerName, String territoryName, int armies) {
        Player player = players.get(playerName);
        Territory territory = territories.get(territoryName);

        if (player == null || territory == null) {
            return false;
        }
        if (!territory.getOwner().equals(playerName)) {
            return false;
        }
        if (player.getReinforcementArmies() < armies) {
            return false;
        }

        return true;
    }
    
    /**
     * Birlik yerleştirir
     */
    public void placeArmy(String playerName, String territoryName, int armies) {
        System.out.println("\n=== BIRLIK YERLEŞTIRME İŞLEMI ===");
        System.out.println("placeArmy çağrıldı: Oyuncu=" + playerName + ", Bölge=" + territoryName + ", Birlik=" + armies);
        
        if (!canPlaceArmy(playerName, territoryName, armies)) {
            System.out.println("HATA: canPlaceArmy kontrolleri başarısız!");
            return;
        }

        Player player = players.get(playerName);
        Territory territory = territories.get(territoryName);

        // Birlik eklemeden önce
        System.out.println("Önceki birlik sayısı: " + territory.getArmies());
        
        // Birlik ekleme
        territory.addArmies(armies);
        player.setReinforcementArmies(player.getReinforcementArmies() - armies);
        
        // Birlik ekledikten sonra
        System.out.println("Yeni birlik sayısı: " + territory.getArmies());
        System.out.println("Kalan takviye birlik: " + player.getReinforcementArmies());
        System.out.println("=== BIRLIK YERLEŞTIRME TAMAMLANDI ===\n");
    }
    
    /**
     * Takviye yapılabilir mi kontrol eder
     */
    public boolean canFortify(String playerName, String sourceTerritory, String targetTerritory, int armies) {
        Territory source = territories.get(sourceTerritory);
        Territory target = territories.get(targetTerritory);

        if (source == null || target == null) {
            return false;
        }
        if (!source.getOwner().equals(playerName) || !target.getOwner().equals(playerName)) {
            return false;
        }
        if (!isConnected(sourceTerritory, targetTerritory, playerName)) {
            return false;
        }
        if (source.getArmies() <= armies) {
            return false; // En az bir birlik geride kalmalı
        }
        return true;
    }
    
    /**
     * Takviye yapar (birlik transfer eder)
     */
    public void fortify(String playerName, String sourceTerritory, String targetTerritory, int armies) {
        System.out.println("\n=== TAKVİYE İŞLEMİ ===");
        System.out.println("fortify çağrıldı: Oyuncu=" + playerName + 
                          ", Kaynak=" + sourceTerritory + 
                          ", Hedef=" + targetTerritory + 
                          ", Birlik=" + armies);
        
        if (!canFortify(playerName, sourceTerritory, targetTerritory, armies)) {
            System.out.println("HATA: canFortify kontrolleri başarısız!");
            return;
        }

        Territory source = territories.get(sourceTerritory);
        Territory target = territories.get(targetTerritory);

        // Takviye yapmadan önce
        System.out.println("Kaynak bölge birlik sayısı: " + source.getArmies());
        System.out.println("Hedef bölge birlik sayısı: " + target.getArmies());
        
        // Takviye yap
        source.removeArmies(armies);
        target.addArmies(armies);
        
        // Takviye yaptıktan sonra
        System.out.println("Takviye sonrası - Kaynak: " + source.getArmies() + 
                          ", Hedef: " + target.getArmies());
        System.out.println("=== TAKVİYE TAMAMLANDI ===\n");
    }
    
    /**
     * İki bölge arasında bağlantı var mı kontrol eder (oyuncunun kendi bölgeleri üzerinden)
     */
    private boolean isConnected(String sourceTerritory, String targetTerritory, String playerName) {
        Set<String> visited = new HashSet<>();
        return isConnectedDFS(sourceTerritory, targetTerritory, playerName, visited);
    }
    
    /**
     * Derinlik öncelikli arama ile bağlantı kontrol eder
     */
    private boolean isConnectedDFS(String currentTerritory, String targetTerritory, String playerName, Set<String> visited) {
        if (currentTerritory.equals(targetTerritory)) {
            return true;
        }

        visited.add(currentTerritory);
        Territory current = territories.get(currentTerritory);

        if (current == null) {
            return false;
        }

        for (String neighbor : current.getNeighbors()) {
            Territory neighborTerritory = territories.get(neighbor);
            if (neighborTerritory != null
                    && neighborTerritory.getOwner().equals(playerName)
                    && !visited.contains(neighbor)) {
                if (isConnectedDFS(neighbor, targetTerritory, playerName, visited)) {
                    return true;
                }
            }
        }

        return false;
    }
    
    /**
     * Oyuncunun sahip olduğu bölgelerin toplam birlik sayısını hesaplar
     */
    public int getTotalArmies(String playerName) {
        Player player = players.get(playerName);
        if (player == null) {
            return 0;
        }
        
        int totalArmies = 0;
        for (String territoryName : player.getTerritories()) {
            Territory territory = territories.get(territoryName);
            if (territory != null) {
                totalArmies += territory.getArmies();
            }
        }
        
        return totalArmies;
    }
    
    /**
     * Oyuncunun belirli bir bölgede minimum kaç birlik bulundurması gerektiğini kontrol eder
     */
    public boolean hasMinimumArmies(String playerName, String territoryName, int minimumArmies) {
        Territory territory = territories.get(territoryName);
        
        if (territory == null || !territory.getOwner().equals(playerName)) {
            return false;
        }
        
        return territory.getArmies() >= minimumArmies;
    }
    
    /**
     * Oyuncunun saldırabilir durumda olan bölgelerini listeler
     */
    public List<String> getAttackCapableTerritories(String playerName) {
        Player player = players.get(playerName);
        if (player == null) {
            return new ArrayList<>();
        }
        
        List<String> attackCapable = new ArrayList<>();
        
        for (String territoryName : player.getTerritories()) {
            Territory territory = territories.get(territoryName);
            if (territory != null && territory.getArmies() > 1) {
                // En az bir komşu düşman bölgesi var mı kontrol et
                for (String neighbor : territory.getNeighbors()) {
                    Territory neighborTerritory = territories.get(neighbor);
                    if (neighborTerritory != null && !neighborTerritory.getOwner().equals(playerName)) {
                        attackCapable.add(territoryName);
                        break;
                    }
                }
            }
        }
        
        return attackCapable;
    }
    
    /**
     * Oyuncunun takviye yapabilir durumda olan bölgelerini listeler
     */
    public List<String> getFortifyCapableTerritories(String playerName) {
        Player player = players.get(playerName);
        if (player == null) {
            return new ArrayList<>();
        }
        
        List<String> fortifyCapable = new ArrayList<>();
        
        for (String territoryName : player.getTerritories()) {
            Territory territory = territories.get(territoryName);
            if (territory != null && territory.getArmies() > 1) {
                fortifyCapable.add(territoryName);
            }
        }
        
        return fortifyCapable;
    }
    
    /**
     * Belirli bir bölgeden yapılabilecek maksimum saldırı birlik sayısını hesaplar
     */
    public int getMaxAttackArmies(String territoryName) {
        Territory territory = territories.get(territoryName);
        if (territory == null) {
            return 0;
        }
        
        return Math.min(3, territory.getArmies() - 1);
    }
    
    /**
     * Belirli bir bölgeden yapılabilecek maksimum takviye birlik sayısını hesaplar
     */
    public int getMaxFortifyArmies(String territoryName) {
        Territory territory = territories.get(territoryName);
        if (territory == null) {
            return 0;
        }
        
        return territory.getArmies() - 1;
    }
}