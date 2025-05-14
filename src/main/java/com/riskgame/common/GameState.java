package com.riskgame.common;

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
}