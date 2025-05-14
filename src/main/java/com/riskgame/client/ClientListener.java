package com.riskgame.client;

import com.riskgame.common.*;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
        
/**
 * İstemci dinleyici sınıfı. Sunucudan gelen mesajları işler.
 */
public class ClientListener implements Runnable {
    private boolean running;
    private RiskClient client;
    
    public ClientListener(RiskClient client) {
        this.client = client;
        this.running = true;
    }
    
    public void stop() {
        this.running = false;
    }
    
    @Override
    public void run() {
        try {
            while (running) {
                Message message = (Message) client.getInput().readObject();
                if (message != null) {
                    SwingUtilities.invokeLater(() -> handleMessage(message));
                }
            }
        } catch (IOException e) {
            if (running) {
                SwingUtilities.invokeLater(() -> {
                    client.addLogMessage("Sunucu bağlantısı kesildi: " + e.getMessage());
                    client.disconnectFromServer();
                });
            }
        } catch (ClassNotFoundException e) {
            SwingUtilities.invokeLater(() -> {
                client.addLogMessage("Geçersiz mesaj formatı: " + e.getMessage());
                client.disconnectFromServer();
            });
        }
    }
    
    /**
     * Gelen mesajı işler.
     */
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case CHAT:
                client.addChatMessage(message.getSender(), message.getContent());
                break;
            case SERVER_FULL:
                client.addLogMessage("Sunucu: " + message.getContent());
                client.disconnectFromServer();
                break;
            case PLAYER_JOINED:
            case PLAYER_LEFT:
            case PLAYER_ELIMINATED:
                client.addLogMessage(message.getContent());
                break;
            case GAME_READY:
                client.addLogMessage(message.getContent());
                int response = JOptionPane.showConfirmDialog(client, 
                                                          "Oyun başlatılabilir. Başlatmak istiyor musunuz?", 
                                                          "Oyun Hazır", 
                                                          JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    client.startGame();
                }
                break;
            case GAME_STARTED:
                client.addLogMessage("Oyun başladı!");
                break;
            case GAME_ENDED:
                client.addLogMessage(message.getContent());
                client.setGameControlsEnabled(false);
                break;
            case TURN_CHANGED:
                client.addLogMessage(message.getContent());
                break;
            case GAME_STATE:
                if (message.getGameState() != null) {
                    client.updateGameState(message.getGameState());
                } else {
                    client.addLogMessage("Hata: Geçersiz oyun durumu alındı.");
                }
                break;
            case MOVE_APPLIED:
            case INVALID_MOVE:
                client.addLogMessage(message.getContent());
                break;
            default:
                client.addLogMessage("Bilinmeyen mesaj türü: " + message.getType());
                break;
        }
    }
}