/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.List;

/**
 *
 * @author WINCHESTER
 */
public class GameState {
    
    private GameMap map;
    private List<Player> players;
    private int currentPlayerIndex;
    private boolean gameOver;

    public GameState(GameMap map, List<Player> players) {
        this.map = map;
        this.players = players;
        this.currentPlayerIndex = 0;
        this.gameOver = false;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void checkVictoryCondition() {
        for (Player p : players) {
            if (p.getOwnedTerritories().size() == map.getAllTerritories().size()) {
                gameOver = true;
                System.out.println("Kazanan: " + p.getName());
                break;
            }
        }
    }

    public GameMap getMap() {
        return map;
    }
    
}
