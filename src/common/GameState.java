package common;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Oyun durumunu temsil eden sınıf. Client ve server arasında iletişim için kullanılır.
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Map<String, Territory> territories = new HashMap<>();
    private Map<String, Continent> continents = new HashMap<>();
    private Map<String, Player> players = new HashMap<>();
    private List<String> playerList = new ArrayList<>();
    private boolean gameStarted = false;
    private String currentPlayer;
    
    public GameState() {
        // Boş constructor
    }
    
    // Yeni metod: Derin kopya oluştur
    public GameState createDeepCopy() {
        GameState copy = new GameState();
        
        // Territories kopyala
        for (Map.Entry<String, Territory> entry : territories.entrySet()) {
            Territory original = entry.getValue();
            Territory territoryCopy = new Territory(original.getName(), original.getContinent());
            territoryCopy.setOwner(original.getOwner());
            territoryCopy.setArmies(original.getArmies());
            for (String neighbor : original.getNeighbors()) {
                territoryCopy.addNeighbor(neighbor);
            }
            copy.territories.put(entry.getKey(), territoryCopy);
        }
        
        // Continents kopyala
        for (Map.Entry<String, Continent> entry : continents.entrySet()) {
            Continent original = entry.getValue();
            Continent continentCopy = new Continent(original.getName(), original.getBonus());
            copy.continents.put(entry.getKey(), continentCopy);
        }
        
        // Players kopyala
        for (Map.Entry<String, Player> entry : players.entrySet()) {
            Player original = entry.getValue();
            Player playerCopy = new Player(original.getName());
            playerCopy.setReinforcementArmies(original.getReinforcementArmies());
            for (String territory : original.getTerritories()) {
                playerCopy.addTerritory(territory);
            }
            copy.players.put(entry.getKey(), playerCopy);
        }
        
        // PlayerList kopyala
        copy.playerList.addAll(playerList);
        
        // Diğer alanlar
        copy.gameStarted = gameStarted;
        copy.currentPlayer = currentPlayer;
        
        return copy;
    }
    
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
    
    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }
    
    @Override
    public String toString() {
        return "GameState{territories=" + territories.size() + 
               ", players=" + players.size() + 
               ", gameStarted=" + gameStarted + 
               ", currentPlayer='" + currentPlayer + "'}";
    }
}