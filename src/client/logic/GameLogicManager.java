package client.logic;

import client.DiceDialog;
import client.RiskClient;
import common.*;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;

public class GameLogicManager {
    
    private RiskClient parent;
    
    // Oyun durumu
    private GameState gameState;
    private String selectedTerritory;
    private ActionType currentAction;
    
    public GameLogicManager(RiskClient parent) {
        this.parent = parent;
    }
    
    public void territoryClicked(String territoryName) {
        if (!parent.getNetworkManager().isConnected() || gameState == null || !gameState.isGameStarted()) {
            return;
        }

        // Oyuncunun sırası mı kontrol et
        String currentPlayer = gameState.getCurrentPlayer();
        if (currentPlayer == null || !currentPlayer.equals(parent.getNetworkManager().getUsername())) {
            parent.addLogMessage("Şu anda sizin sıranız değil. Sıra " + currentPlayer + " oyuncusunda.");
            return;
        }

        Territory territory = gameState.getTerritories().get(territoryName);

        if (currentAction == null) {
            selectedTerritory = territoryName;
            parent.updateStatusLabel("Seçilen bölge: " + territoryName + " (" + territory.getOwner() + ", " + territory.getArmies() + " birlik)");
        } else {
            switch (currentAction) {
                case PLACE_ARMY:
                    handlePlaceArmy(territoryName);
                    break;
                case ATTACK:
                    handleAttack(territoryName);
                    if (selectedTerritory != null) {
                        parent.updateArmyCountComboBox(ActionType.ATTACK);
                    }
                    break;
                case FORTIFY:
                    handleFortify(territoryName);
                    if (selectedTerritory != null) {
                        parent.updateArmyCountComboBox(ActionType.FORTIFY);
                    }
                    break;
                default:
                    break;
            }
        }

        parent.repaintMap();
    }
    
    public void setCurrentAction(ActionType action) {
        System.out.println("setCurrentAction çağrıldı: " + action);

        currentAction = action;
        selectedTerritory = null;

        parent.updateButtonStates(action);

        switch (action) {
            case PLACE_ARMY:
                parent.updateStatusLabel("Birlik yerleştirmek için bir bölge seçin");
                break;
            case ATTACK:
                parent.updateStatusLabel("Saldırmak için önce kendi bölgenizi seçin");
                break;
            case FORTIFY:
                parent.updateStatusLabel("Takviye için önce kaynak bölgeyi seçin");
                break;
            default:
                break;
        }

        parent.updateArmyCountComboBox(action);
        parent.repaintMap();
    }
    
    private void handlePlaceArmy(String territoryName) {
        Territory territory = gameState.getTerritories().get(territoryName);

        if (!territory.getOwner().equals(parent.getNetworkManager().getUsername())) {
            parent.addLogMessage("Sadece kendi bölgelerinize birlik yerleştirebilirsiniz.");
            return;
        }

        int armies = parent.getSelectedArmyCount();
        Player player = gameState.getPlayers().get(parent.getNetworkManager().getUsername());

        if (player.getReinforcementArmies() < armies) {
            parent.addLogMessage("Yeterli takviye birliğiniz yok. Kalan: " + player.getReinforcementArmies());
            return;
        }
        
        try {
            GameAction action = new GameAction(ActionType.PLACE_ARMY, territoryName, null, armies);
            Message actionMessage = new Message(parent.getNetworkManager().getUsername(), "", MessageType.GAME_ACTION);
            actionMessage.setGameAction(action);
            parent.getNetworkManager().sendMessage(actionMessage);

            parent.repaintMap();

            currentAction = null;
            selectedTerritory = null;
        } catch (Exception e) {
            parent.addLogMessage("Hareket gönderilemedi: " + e.getMessage());
        }
    }
    
    private void handleAttack(String territoryName) {
        if (selectedTerritory == null) {
            // İlk tıklama - saldıran bölgeyi seç
            Territory territory = gameState.getTerritories().get(territoryName);

            if (!territory.getOwner().equals(parent.getNetworkManager().getUsername())) {
                parent.addLogMessage("Saldırı için önce kendi bölgelerinizden birini seçmelisiniz.");
                return;
            }

            if (territory.getArmies() < 2) {
                parent.addLogMessage("Saldırmak için en az 2 birliğe ihtiyacınız var.");
                return;
            }

            selectedTerritory = territoryName;
            parent.updateArmyCountComboBox(ActionType.ATTACK);
            parent.updateStatusLabel("Saldıran bölge: " + territoryName + ". Şimdi hedef bölgeyi seçin.");
        } else {
            // İkinci tıklama - hedef bölgeyi seç
            Territory sourceTerritory = gameState.getTerritories().get(selectedTerritory);
            Territory targetTerritory = gameState.getTerritories().get(territoryName);

            if (targetTerritory.getOwner().equals(parent.getNetworkManager().getUsername())) {
                parent.addLogMessage("Kendi bölgenize saldıramazsınız.");
                selectedTerritory = null;
                return;
            }

            if (!sourceTerritory.isNeighbor(territoryName)) {
                parent.addLogMessage("Sadece komşu bölgelere saldırabilirsiniz.");
                selectedTerritory = null;
                return;
            }

            // Maksimum saldırı birliği
            int maxAttackArmies = Math.min(3, sourceTerritory.getArmies() - 1);
            int selectedArmies = Math.min(parent.getSelectedArmyCount(), maxAttackArmies);

            // Zar atma diyaloğunu göster
            int defenderArmies = Math.min(2, targetTerritory.getArmies());
            DiceDialog diceDialog = new DiceDialog(
                    parent,
                    parent.getNetworkManager().getUsername(),
                    targetTerritory.getOwner(),
                    selectedArmies,
                    defenderArmies
            );
            diceDialog.setVisible(true);

            // Zar sonuçlarını al
            int[] attackDice = diceDialog.getAttackDice();
            int[] defenseDice = diceDialog.getDefenseDice();

            if (attackDice != null && defenseDice != null) {
                try {
                    GameAction action = new GameAction(
                            ActionType.ATTACK,
                            selectedTerritory,
                            territoryName,
                            selectedArmies
                    );

                    Message actionMessage = new Message(parent.getNetworkManager().getUsername(), "", MessageType.GAME_ACTION);
                    actionMessage.setGameAction(action);
                    parent.getNetworkManager().sendMessage(actionMessage);

                    parent.addLogMessage("Saldırı komutu gönderildi. " + selectedTerritory + " -> " + territoryName
                            + " (Zarlar: " + Arrays.toString(attackDice) + " vs " + Arrays.toString(defenseDice) + ")");

                    currentAction = null;
                    selectedTerritory = null;
                } catch (Exception e) {
                    parent.addLogMessage("Hareket gönderilemedi: " + e.getMessage());
                }
            } else {
                parent.addLogMessage("Zar atma iptal edildi.");
                currentAction = null;
                selectedTerritory = null;
            }
        }
    }
    
    private void handleFortify(String territoryName) {
        if (selectedTerritory == null) {
            // İlk tıklama - kaynak bölgeyi seç
            Territory territory = gameState.getTerritories().get(territoryName);

            if (!territory.getOwner().equals(parent.getNetworkManager().getUsername())) {
                parent.addLogMessage("Takviye için önce kendi bölgelerinizden birini seçmelisiniz.");
                return;
            }

            if (territory.getArmies() < 2) {
                parent.addLogMessage("Takviye yapmak için bölgede en az 2 birlik olmalıdır.");
                return;
            }

            selectedTerritory = territoryName;
            parent.updateStatusLabel("Kaynak bölge: " + territoryName + ". Şimdi hedef bölgeyi seçin.");
        } else {
            // İkinci tıklama - hedef bölgeyi seç
            Territory sourceTerritory = gameState.getTerritories().get(selectedTerritory);
            Territory targetTerritory = gameState.getTerritories().get(territoryName);

            if (!targetTerritory.getOwner().equals(parent.getNetworkManager().getUsername())) {
                parent.addLogMessage("Sadece kendi bölgelerinize takviye yapabilirsiniz.");
                selectedTerritory = null;
                return;
            }

            if (selectedTerritory.equals(territoryName)) {
                parent.addLogMessage("Farklı bir bölge seçmelisiniz.");
                selectedTerritory = null;
                return;
            }

            int armies = Math.min(parent.getSelectedArmyCount(), sourceTerritory.getArmies() - 1);

            try {
                GameAction action = new GameAction(ActionType.FORTIFY, selectedTerritory, territoryName, armies);
                Message actionMessage = new Message(parent.getNetworkManager().getUsername(), "", MessageType.GAME_ACTION);
                actionMessage.setGameAction(action);
                parent.getNetworkManager().sendMessage(actionMessage);

                currentAction = null;
                selectedTerritory = null;
            } catch (Exception e) {
                parent.addLogMessage("Hareket gönderilemedi: " + e.getMessage());
            }
        }
    }
    
    public void updateGameState(GameState newState) {
        System.out.println("\n=== YENİ OYUN DURUMU ALINDI ===");

        if (newState == null) {
            System.out.println("HATA: Alınan oyun durumu NULL!");
            return;
        }

        this.gameState = newState;
        parent.setGameStateToMap(gameState);

        if (gameState.isGameStarted()) {
            boolean isMyTurn = gameState.getCurrentPlayer().equals(parent.getNetworkManager().getUsername());
            parent.setGameControlsEnabled(isMyTurn);

            if (isMyTurn) {
                parent.addLogMessage("Sıra sizde.");
            } else {
                parent.addLogMessage("Sıra " + gameState.getCurrentPlayer() + " oyuncusunda.");
                // Ekstra güvenlik için tüm aktif eylemi iptal et
                currentAction = null;
                selectedTerritory = null;
            }
        }

        System.out.println("=== OYUN DURUMU GÜNCELLENDİ ===\n");
    }
    
    // Getter metodları
    public GameState getGameState() { return gameState; }
    public String getSelectedTerritory() { return selectedTerritory; }
    public ActionType getCurrentAction() { return currentAction; }
}