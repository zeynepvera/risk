package server;

import common.*;
import server.game.*;
import java.io.Serializable;
import java.util.*;

/**
 * Sunucu oyun durumu - Manager'ları koordine eden ana sınıf
 * Artık sadece koordinasyon ve delegasyon yapar, iş mantığı manager'larda
 */
public class ServerGameState implements Serializable {

    private static final long serialVersionUID = 1L;

    // Oyun verisi
    private Map<String, Territory> territories = new HashMap<>();
    private Map<String, Continent> continents = new HashMap<>();
    private Map<String, Player> players = new HashMap<>();
    private List<String> playerList = new ArrayList<>();
    private boolean gameStarted = false;
    private String currentPlayer;

    // Manager'lar - İş mantığını bunlar halleder
    private MapInitializer mapInitializer;
    private BattleManager battleManager;
    private ArmyManager armyManager;
    private ContinentManager continentManager;

    /**
     * Constructor - Manager'ları başlatır
     */
    public ServerGameState() {
        initializeManagers();
    }

    /**
     * Manager'ları başlatır
     */
    private void initializeManagers() {
        mapInitializer = new MapInitializer(territories, continents, players, playerList);
        battleManager = new BattleManager(territories, players);
        armyManager = new ArmyManager(territories, players);
        continentManager = new ContinentManager(territories, continents, players);
    }

    /**
     * Oyunu başlatır
     */
    public void initializeGame(List<String> playerNames) {
        System.out.println("\n=== OYUN BAŞLATILIYOR ===");
        
        // Oyun verisini sıfırla
        resetGameData();

        // Oyuncuları oluştur
        createPlayers(playerNames);

        // Haritayı başlat (MapInitializer kullanarak)
        mapInitializer.initializeMap();

        // Başlangıç birliklerini hesapla ve dağıt
        distributeInitialArmies();

        gameStarted = true;
        System.out.println("=== OYUN BAŞLATILDI ===\n");
    }

    /**
     * Oyun verisini sıfırlar
     */
    private void resetGameData() {
        territories.clear();
        continents.clear();
        players.clear();
        playerList.clear();
        gameStarted = false;
        currentPlayer = null;
        
        // Manager'ları yeniden başlat
        initializeManagers();
    }

    /**
     * Oyuncuları oluşturur
     */
    private void createPlayers(List<String> playerNames) {
        for (String name : playerNames) {
            Player player = new Player(name);
            players.put(name, player);
            playerList.add(name);
        }

        if (!playerList.isEmpty()) {
            currentPlayer = playerList.get(0);
        }
    }

    /**
     * Başlangıç birliklerini dağıtır
     */
    private void distributeInitialArmies() {
        int initialArmies = mapInitializer.calculateInitialArmies(playerList.size());
        
        for (Player player : players.values()) {
            int reinforcementArmies = initialArmies - player.getTerritories().size();
            armyManager.setReinforcementArmies(player.getName(), reinforcementArmies);
        }
    }

    // ============ DELEGATION METODLARI ============
    // Tüm iş mantığı manager'lara yönlendiriliyor

    /**
     * Takviye birlik sayısını hesaplar (ContinentManager'a delegate eder)
     */
    public int calculateReinforcementArmies(String playerName) {
        return continentManager.calculateReinforcementArmies(playerName);
    }

    /**
     * Takviye birlik sayısını ayarlar (ArmyManager'a delegate eder)
     */
    public void setReinforcementArmies(String playerName, int armies) {
        armyManager.setReinforcementArmies(playerName, armies);
    }

    /**
     * Birlik yerleştirilebilir mi kontrol eder (ArmyManager'a delegate eder)
     */
    public boolean canPlaceArmy(String playerName, String territoryName, int armies) {
        return armyManager.canPlaceArmy(playerName, territoryName, armies);
    }

    /**
     * Birlik yerleştirir (ArmyManager'a delegate eder)
     */
    public void placeArmy(String playerName, String territoryName, int armies) {
        armyManager.placeArmy(playerName, territoryName, armies);
    }

    /**
     * Saldırı yapılabilir mi kontrol eder (BattleManager'a delegate eder)
     */
    public boolean canAttack(String playerName, String sourceTerritory, String targetTerritory, int armies) {
        return battleManager.canAttack(playerName, sourceTerritory, targetTerritory, armies);
    }

    /**
     * Saldırı yapar (BattleManager'a delegate eder)
     */
    public AttackResult attack(String playerName, String sourceTerritory, String targetTerritory, int attackingArmies) {
        return battleManager.attack(playerName, sourceTerritory, targetTerritory, attackingArmies);
    }

    /**
     * İstemci zar sonuçları ile saldırı yapar (BattleManager'a delegate eder)
     */
    public AttackResult attack(String playerName, String sourceTerritory, String targetTerritory, 
                              int attackingArmies, int[] clientAttackDice, int[] clientDefenseDice) {
        return battleManager.attack(playerName, sourceTerritory, targetTerritory, 
                                   attackingArmies, clientAttackDice, clientDefenseDice);
    }

    /**
     * Takviye yapılabilir mi kontrol eder (ArmyManager'a delegate eder)
     */
    public boolean canFortify(String playerName, String sourceTerritory, String targetTerritory, int armies) {
        return armyManager.canFortify(playerName, sourceTerritory, targetTerritory, armies);
    }

    /**
     * Takviye yapar (ArmyManager'a delegate eder)
     */
    public void fortify(String playerName, String sourceTerritory, String targetTerritory, int armies) {
        armyManager.fortify(playerName, sourceTerritory, targetTerritory, armies);
    }

    /**
     * Kazananı kontrol eder
     */
    public String checkWinner() {
        String winner = null;
        int activePlayers = 0;

        for (Player player : players.values()) {
            if (!player.getTerritories().isEmpty()) {
                winner = player.getName();
                activePlayers++;
            }
        }

        return (activePlayers == 1) ? winner : null;
    }

    /**
     * Client'a gönderilecek oyun durumunu oluşturur
     */
    public common.GameState toClientState() {
        common.GameState clientState = new common.GameState();

        // Bölgeleri kopyala
        for (Map.Entry<String, Territory> entry : this.getTerritories().entrySet()) {
            clientState.getTerritories().put(entry.getKey(), new Territory(entry.getValue()));
        }

        // Kıtaları kopyala
        for (Map.Entry<String, Continent> entry : this.getContinents().entrySet()) {
            clientState.getContinents().put(entry.getKey(), 
                new Continent(entry.getValue().getName(), entry.getValue().getBonus()));
        }

        // Oyuncuları kopyala
        for (Map.Entry<String, Player> entry : this.getPlayers().entrySet()) {
            Player copy = new Player(entry.getValue().getName());
            copy.setReinforcementArmies(entry.getValue().getReinforcementArmies());
            for (String t : entry.getValue().getTerritories()) {
                copy.addTerritory(t);
            }
            clientState.getPlayers().put(entry.getKey(), copy);
        }

        // Sıra ve oyuncu listesi
        clientState.getPlayerList().addAll(this.getPlayerList());
        clientState.setGameStarted(this.isGameStarted());
        clientState.setCurrentPlayer(this.getCurrentPlayer());

        return clientState;
    }

    // ============ DIAGNOSTIC METODLARI ============

    /**
     * Kıta durum raporunu yazdırır (ContinentManager'a delegate eder)
     */
    public void printContinentReport() {
        continentManager.printContinentReport();
    }

    /**
     * Oyuncu istatistiklerini yazdırır
     */
    public void printPlayerStats() {
        System.out.println("\n=== OYUNCU İSTATİSTİKLERİ ===");
        
        for (String playerName : playerList) {
            Player player = players.get(playerName);
            System.out.println("\n👤 " + playerName + ":");
            System.out.println("   Bölge sayısı: " + player.getTerritories().size());
            System.out.println("   Takviye birlik: " + player.getReinforcementArmies());
            System.out.println("   Toplam birlik: " + armyManager.getTotalArmies(playerName));
            System.out.println("   Kontrol ettiği kıtalar: " + continentManager.getControlledContinents(playerName));
            System.out.println("   Saldırabilir bölgeler: " + armyManager.getAttackCapableTerritories(playerName).size());
        }
        
        System.out.println("=== İSTATİSTİKLER SONU ===\n");
    }

    // ============ GETTER VE SETTER METODLARI ============

    public Map<String, Territory> getTerritories() {
        return territories;
    }

    public Map<String, Continent> getContinents() {
        return continents;
    }

    public Map<String, Player> getPlayers() {
        return players;
    }

    public List<String> getPlayerList() {
        return playerList;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    // Manager'lara erişim (test ve debug için)
    public MapInitializer getMapInitializer() {
        return mapInitializer;
    }

    public BattleManager getBattleManager() {
        return battleManager;
    }

    public ArmyManager getArmyManager() {
        return armyManager;
    }

    public ContinentManager getContinentManager() {
        return continentManager;
    }
}