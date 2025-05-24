package server.game;

import common.Continent;
import common.Territory;
import common.Player;
import java.util.*;

/**
 * Harita oluşturma ve başlangıç kurulumu işlemlerinden sorumlu sınıf
 */
public class MapInitializer {
    
    private Map<String, Territory> territories;
    private Map<String, Continent> continents;
    private Map<String, Player> players;
    private List<String> playerList;
    
    public MapInitializer(Map<String, Territory> territories, 
                         Map<String, Continent> continents,
                         Map<String, Player> players,
                         List<String> playerList) {
        this.territories = territories;
        this.continents = continents;
        this.players = players;
        this.playerList = playerList;
    }
    
    /**
     * Oyun haritasını başlatır
     */
    public void initializeMap() {
        createContinents();
        createTerritories();
        distributeTerritoriesRandomly();
        
        // Her bölgeye başlangıçta bir birlik yerleştir
        for (Territory territory : territories.values()) {
            territory.setArmies(1);
        }
    }
    
    /**
     * Kıtaları oluşturur
     */
    private void createContinents() {
        continents.put("KUZEY_AMERIKA", new Continent("KUZEY_AMERIKA", 5));
        continents.put("GUNEY_AMERIKA", new Continent("GUNEY_AMERIKA", 2));
        continents.put("AVRUPA", new Continent("AVRUPA", 5));
        continents.put("AFRIKA", new Continent("AFRIKA", 3));
        continents.put("ASYA", new Continent("ASYA", 7));
        continents.put("AVUSTRALYA", new Continent("AVUSTRALYA", 2));
    }
    
    /**
     * Bölgeleri oluşturur ve komşuluk ilişkilerini kurar
     */
    private void createTerritories() {
        // Kuzey Amerika bölgeleri
        territories.put("ALASKA", new Territory("ALASKA", "KUZEY_AMERIKA"));
        territories.put("KUZEYBATI_BOLGE", new Territory("KUZEYBATI_BOLGE", "KUZEY_AMERIKA"));
        territories.put("GRÖNLAND", new Territory("GRÖNLAND", "KUZEY_AMERIKA"));
        territories.put("ALBERTA", new Territory("ALBERTA", "KUZEY_AMERIKA"));
        territories.put("ONTARIO", new Territory("ONTARIO", "KUZEY_AMERIKA"));
        territories.put("QUEBEC", new Territory("QUEBEC", "KUZEY_AMERIKA"));
        territories.put("BATI_ABD", new Territory("BATI_ABD", "KUZEY_AMERIKA"));
        territories.put("DOGU_ABD", new Territory("DOGU_ABD", "KUZEY_AMERIKA"));
        territories.put("ORTA_AMERIKA", new Territory("ORTA_AMERIKA", "KUZEY_AMERIKA"));

        // Güney Amerika bölgeleri
        territories.put("VENEZUELA", new Territory("VENEZUELA", "GUNEY_AMERIKA"));
        territories.put("PERU", new Territory("PERU", "GUNEY_AMERIKA"));
        territories.put("BREZILYA", new Territory("BREZILYA", "GUNEY_AMERIKA"));
        territories.put("ARJANTIN", new Territory("ARJANTIN", "GUNEY_AMERIKA"));

        // Avrupa bölgeleri
        territories.put("IZLANDA", new Territory("IZLANDA", "AVRUPA"));
        territories.put("ISKANDINAVYA", new Territory("ISKANDINAVYA", "AVRUPA"));
        territories.put("BUYUK_BRITANYA", new Territory("BUYUK_BRITANYA", "AVRUPA"));
        territories.put("BATI_AVRUPA", new Territory("BATI_AVRUPA", "AVRUPA"));
        territories.put("KUZEY_AVRUPA", new Territory("KUZEY_AVRUPA", "AVRUPA"));
        territories.put("GUNEY_AVRUPA", new Territory("GUNEY_AVRUPA", "AVRUPA"));
        territories.put("UKRAYNA", new Territory("UKRAYNA", "AVRUPA"));

        // Afrika bölgeleri
        territories.put("KUZEY_AFRIKA", new Territory("KUZEY_AFRIKA", "AFRIKA"));
        territories.put("MISIR", new Territory("MISIR", "AFRIKA"));
        territories.put("DOGU_AFRIKA", new Territory("DOGU_AFRIKA", "AFRIKA"));
        territories.put("KONGO", new Territory("KONGO", "AFRIKA"));
        territories.put("GUNEY_AFRIKA", new Territory("GUNEY_AFRIKA", "AFRIKA"));
        territories.put("MADAGASKAR", new Territory("MADAGASKAR", "AFRIKA"));

        // Asya bölgeleri
        territories.put("URAL", new Territory("URAL", "ASYA"));
        territories.put("SIBIRYA", new Territory("SIBIRYA", "ASYA"));
        territories.put("YAKUTSK", new Territory("YAKUTSK", "ASYA"));
        territories.put("KAMCHATKA", new Territory("KAMCHATKA", "ASYA"));
        territories.put("IRKUTSK", new Territory("IRKUTSK", "ASYA"));
        territories.put("MOĞOLISTAN", new Territory("MOĞOLISTAN", "ASYA"));
        territories.put("JAPONYA", new Territory("JAPONYA", "ASYA"));
        territories.put("AFGANISTAN", new Territory("AFGANISTAN", "ASYA"));
        territories.put("ÇIN", new Territory("ÇIN", "ASYA"));
        territories.put("ORTA_DOĞU", new Territory("ORTA_DOĞU", "ASYA"));
        territories.put("HINDISTAN", new Territory("HINDISTAN", "ASYA"));
        territories.put("SIAM", new Territory("SIAM", "ASYA"));

        // Avustralya bölgeleri
        territories.put("ENDONEZYA", new Territory("ENDONEZYA", "AVUSTRALYA"));
        territories.put("YENI_GINE", new Territory("YENI_GINE", "AVUSTRALYA"));
        territories.put("BATI_AVUSTRALYA", new Territory("BATI_AVUSTRALYA", "AVUSTRALYA"));
        territories.put("DOGU_AVUSTRALYA", new Territory("DOGU_AVUSTRALYA", "AVUSTRALYA"));

        // Komşuluk ilişkilerini tanımla
        setupNeighborhoods();
    }
    
    /**
     * Tüm komşuluk ilişkilerini kurar
     */
    private void setupNeighborhoods() {
        // Kuzey Amerika komşulukları
        addNeighbors("ALASKA", "KUZEYBATI_BOLGE", "KAMCHATKA", "ALBERTA");
        addNeighbors("KUZEYBATI_BOLGE", "ALASKA", "ALBERTA", "ONTARIO", "GRÖNLAND");
        addNeighbors("GRÖNLAND", "KUZEYBATI_BOLGE", "ONTARIO", "QUEBEC", "IZLANDA");
        addNeighbors("ALBERTA", "ALASKA", "KUZEYBATI_BOLGE", "ONTARIO", "BATI_ABD");
        addNeighbors("ONTARIO", "ALBERTA", "KUZEYBATI_BOLGE", "GRÖNLAND", "QUEBEC", "BATI_ABD", "DOGU_ABD");
        addNeighbors("QUEBEC", "ONTARIO", "GRÖNLAND", "DOGU_ABD");
        addNeighbors("BATI_ABD", "ALBERTA", "ONTARIO", "DOGU_ABD", "ORTA_AMERIKA");
        addNeighbors("DOGU_ABD", "ONTARIO", "QUEBEC", "BATI_ABD", "ORTA_AMERIKA");
        addNeighbors("ORTA_AMERIKA", "BATI_ABD", "DOGU_ABD", "VENEZUELA");

        // Güney Amerika komşulukları
        addNeighbors("VENEZUELA", "ORTA_AMERIKA", "PERU", "BREZILYA");
        addNeighbors("PERU", "VENEZUELA", "BREZILYA", "ARJANTIN");
        addNeighbors("BREZILYA", "VENEZUELA", "PERU", "ARJANTIN", "KUZEY_AFRIKA");
        addNeighbors("ARJANTIN", "PERU", "BREZILYA");

        // Avrupa komşulukları
        addNeighbors("IZLANDA", "GRÖNLAND", "BUYUK_BRITANYA", "ISKANDINAVYA");
        addNeighbors("ISKANDINAVYA", "IZLANDA", "BUYUK_BRITANYA", "KUZEY_AVRUPA", "UKRAYNA");
        addNeighbors("BUYUK_BRITANYA", "IZLANDA", "ISKANDINAVYA", "KUZEY_AVRUPA", "BATI_AVRUPA");
        addNeighbors("BATI_AVRUPA", "BUYUK_BRITANYA", "KUZEY_AVRUPA", "GUNEY_AVRUPA", "KUZEY_AFRIKA");
        addNeighbors("KUZEY_AVRUPA", "BUYUK_BRITANYA", "ISKANDINAVYA", "UKRAYNA", "GUNEY_AVRUPA", "BATI_AVRUPA");
        addNeighbors("GUNEY_AVRUPA", "BATI_AVRUPA", "KUZEY_AVRUPA", "UKRAYNA", "ORTA_DOĞU", "MISIR", "KUZEY_AFRIKA");
        addNeighbors("UKRAYNA", "ISKANDINAVYA", "KUZEY_AVRUPA", "GUNEY_AVRUPA", "ORTA_DOĞU", "AFGANISTAN", "URAL");

        // Afrika komşulukları
        addNeighbors("KUZEY_AFRIKA", "BATI_AVRUPA", "GUNEY_AVRUPA", "MISIR", "DOGU_AFRIKA", "KONGO", "BREZILYA");
        addNeighbors("MISIR", "GUNEY_AVRUPA", "ORTA_DOĞU", "DOGU_AFRIKA", "KUZEY_AFRIKA");
        addNeighbors("DOGU_AFRIKA", "MISIR", "ORTA_DOĞU", "KUZEY_AFRIKA", "KONGO", "GUNEY_AFRIKA", "MADAGASKAR");
        addNeighbors("KONGO", "KUZEY_AFRIKA", "DOGU_AFRIKA", "GUNEY_AFRIKA");
        addNeighbors("GUNEY_AFRIKA", "KONGO", "DOGU_AFRIKA", "MADAGASKAR");
        addNeighbors("MADAGASKAR", "DOGU_AFRIKA", "GUNEY_AFRIKA");

        // Asya komşulukları
        addNeighbors("URAL", "UKRAYNA", "AFGANISTAN", "ÇIN", "SIBIRYA");
        addNeighbors("SIBIRYA", "URAL", "ÇIN", "MOĞOLISTAN", "IRKUTSK", "YAKUTSK");
        addNeighbors("YAKUTSK", "SIBIRYA", "IRKUTSK", "KAMCHATKA");
        addNeighbors("KAMCHATKA", "YAKUTSK", "IRKUTSK", "MOĞOLISTAN", "JAPONYA", "ALASKA");
        addNeighbors("IRKUTSK", "SIBIRYA", "YAKUTSK", "KAMCHATKA", "MOĞOLISTAN");
        addNeighbors("MOĞOLISTAN", "SIBIRYA", "IRKUTSK", "KAMCHATKA", "JAPONYA", "ÇIN");
        addNeighbors("JAPONYA", "KAMCHATKA", "MOĞOLISTAN");
        addNeighbors("AFGANISTAN", "UKRAYNA", "URAL", "ÇIN", "HINDISTAN", "ORTA_DOĞU");
        addNeighbors("ÇIN", "URAL", "SIBIRYA", "MOĞOLISTAN", "AFGANISTAN", "HINDISTAN", "SIAM");
        addNeighbors("ORTA_DOĞU", "UKRAYNA", "GUNEY_AVRUPA", "MISIR", "DOGU_AFRIKA", "AFGANISTAN", "HINDISTAN");
        addNeighbors("HINDISTAN", "ORTA_DOĞU", "AFGANISTAN", "ÇIN", "SIAM");
        addNeighbors("SIAM", "HINDISTAN", "ÇIN", "ENDONEZYA");

        // Avustralya komşulukları
        addNeighbors("ENDONEZYA", "SIAM", "YENI_GINE", "BATI_AVUSTRALYA");
        addNeighbors("YENI_GINE", "ENDONEZYA", "BATI_AVUSTRALYA", "DOGU_AVUSTRALYA");
        addNeighbors("BATI_AVUSTRALYA", "ENDONEZYA", "YENI_GINE", "DOGU_AVUSTRALYA");
        addNeighbors("DOGU_AVUSTRALYA", "YENI_GINE", "BATI_AVUSTRALYA");
    }
    
    /**
     * İki bölge arasında komşuluk ilişkisi kurar
     */
    private void addNeighbors(String territory, String... neighbors) {
        for (String neighbor : neighbors) {
            Territory t1 = territories.get(territory);
            Territory t2 = territories.get(neighbor);

            if (t1 != null && t2 != null) {
                t1.addNeighbor(neighbor);
                t2.addNeighbor(territory);
            }
        }
    }
    
    /**
     * Bölgeleri oyuncular arasında rastgele dağıtır
     */
    private void distributeTerritoriesRandomly() {
        if (playerList.isEmpty()) {
            return;
        }

        List<String> territoryNames = new ArrayList<>(territories.keySet());
        Collections.shuffle(territoryNames);

        int playerIndex = 0;
        for (String territoryName : territoryNames) {
            String playerName = playerList.get(playerIndex);
            Player player = players.get(playerName);
            Territory territory = territories.get(territoryName);

            territory.setOwner(playerName);
            player.addTerritory(territoryName);

            playerIndex = (playerIndex + 1) % playerList.size();
        }
    }
    
    /**
     * Oyuncu sayısına göre başlangıç birlik sayısını hesaplar
     */
    public int calculateInitialArmies(int playerCount) {
        switch (playerCount) {
            case 2:
                return 40;
            case 3:
                return 35;
            case 4:
                return 30;
            case 5:
                return 25;
            default:
                return 20;
        }
    }
}