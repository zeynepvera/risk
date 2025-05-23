package server;

import common.*;
import java.util.*;

public class GameRoom {
    private final ServerGameState gameState;
    private final List<ClientHandler> players = new ArrayList<>();

    public GameRoom() {
        this.gameState = new ServerGameState();
    }

    public ServerGameState getGameState() {
        return gameState;
    }

    public List<ClientHandler> getPlayers() {
        return players;
    }

    public boolean isFull() {
        return players.size() == 2;
    }

   public void removePlayer(ClientHandler client) {
    players.remove(client);

    if (players.size() == 1) {
        ClientHandler winner = players.get(0);
        winner.sendMessage(new Message("SERVER", "Rakibiniz oyunu bıraktı, siz kazandınız!", MessageType.GAME_ENDED));
    }

    sendGameStateToAll(); // Her durumda güncel durumu kalan oyunculara gönder
}


    public void startGame() {
        List<String> usernames = new ArrayList<>();
        for (ClientHandler ch : players) {
            usernames.add(ch.getUsername());
        }

        gameState.initializeGame(usernames);
        gameState.setCurrentPlayer(usernames.get(0));
        gameState.setReinforcementArmies(usernames.get(0), gameState.calculateReinforcementArmies(usernames.get(0)));

        for (ClientHandler ch : players) {
            ch.sendMessage(new Message("SERVER", "Oyun başlıyor!", MessageType.GAME_STARTED));
        }

        sendGameStateToAll();
    }

    public void applyMove(ClientHandler player, GameAction action) {
        String username = player.getUsername();

        if (!username.equals(gameState.getCurrentPlayer())) {
            player.sendMessage(new Message("SERVER", "Sıra sizde değil!", MessageType.INVALID_MOVE));
            return;
        }

        boolean valid = switch (action.getType()) {
            case PLACE_ARMY -> gameState.canPlaceArmy(username, action.getSourceTerritory(), action.getArmyCount());
            case ATTACK -> gameState.canAttack(username, action.getSourceTerritory(), action.getTargetTerritory(), action.getArmyCount());
            case FORTIFY -> gameState.canFortify(username, action.getSourceTerritory(), action.getTargetTerritory(), action.getArmyCount());
            case END_TURN -> true;
            default -> false;
        };

        if (!valid) {
            player.sendMessage(new Message("SERVER", "Geçersiz hareket!", MessageType.INVALID_MOVE));
            return;
        }

        switch (action.getType()) {
            case PLACE_ARMY -> {
                gameState.placeArmy(username, action.getSourceTerritory(), action.getArmyCount());
                broadcast(new Message("SERVER", username + " " + action.getSourceTerritory() + " bölgesine " + action.getArmyCount() + " birlik yerleştirdi.", MessageType.MOVE_APPLIED));
                nextTurn();
            }
            case ATTACK -> {
                AttackResult result = gameState.attack(username, action.getSourceTerritory(), action.getTargetTerritory(), action.getArmyCount());
                String desc = username + ", " + action.getSourceTerritory() + " → " + action.getTargetTerritory() + ": " + result.getDescription();
                broadcast(new Message("SERVER", desc, MessageType.MOVE_APPLIED));

                if (result.getEliminatedPlayer() != null) {
                    broadcast(new Message("SERVER", result.getEliminatedPlayer() + " oyundan elendi!", MessageType.PLAYER_ELIMINATED));
                }

                String winner = gameState.checkWinner();
                if (winner != null) {
                    broadcast(new Message("SERVER", winner + " kazandı!", MessageType.GAME_ENDED));
                    return;
                }

                nextTurn();
            }
            case FORTIFY -> {
                gameState.fortify(username, action.getSourceTerritory(), action.getTargetTerritory(), action.getArmyCount());
                broadcast(new Message("SERVER", username + " " + action.getSourceTerritory() + " → " + action.getTargetTerritory() + " arasında " + action.getArmyCount() + " birlik taşıdı.", MessageType.MOVE_APPLIED));
                nextTurn();
            }
            case END_TURN -> {
                broadcast(new Message("SERVER", username + " turunu bitirdi.", MessageType.MOVE_APPLIED));
                nextTurn();
            }
        }

        sendGameStateToAll();
    }

    private void nextTurn() {
        List<String> playersList = gameState.getPlayerList();
        int index = (playersList.indexOf(gameState.getCurrentPlayer()) + 1) % playersList.size();
        String nextPlayer = playersList.get(index);
        gameState.setCurrentPlayer(nextPlayer);
        gameState.setReinforcementArmies(nextPlayer, gameState.calculateReinforcementArmies(nextPlayer));
    }

    private void sendGameStateToAll() {
        GameState clientState = convertToClientState();
        Message stateMsg = new Message("SERVER", "", MessageType.GAME_STATE);
        stateMsg.setGameState(clientState);
        broadcast(stateMsg);
    }

    void broadcast(Message msg) {
        for (ClientHandler ch : players) {
            ch.sendMessage(msg);
        }
    }

    private GameState convertToClientState() {
        GameState clientState = new GameState();

        for (Map.Entry<String, Territory> entry : gameState.getTerritories().entrySet()) {
            clientState.getTerritories().put(entry.getKey(), new Territory(entry.getValue()));
        }

        for (Map.Entry<String, Continent> entry : gameState.getContinents().entrySet()) {
            clientState.getContinents().put(entry.getKey(), new Continent(entry.getValue().getName(), entry.getValue().getBonus()));
        }

        for (Map.Entry<String, Player> entry : gameState.getPlayers().entrySet()) {
            Player p = new Player(entry.getKey());
            p.setReinforcementArmies(entry.getValue().getReinforcementArmies());
            for (String t : entry.getValue().getTerritories()) {
                p.addTerritory(t);
            }
            clientState.getPlayers().put(entry.getKey(), p);
        }

        clientState.getPlayerList().addAll(gameState.getPlayerList());
        clientState.setCurrentPlayer(gameState.getCurrentPlayer());
        clientState.setGameStarted(gameState.isGameStarted());

        return clientState;
    }
}
