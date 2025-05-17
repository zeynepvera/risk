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
            System.out.println("ClientListener başlatıldı");
            
            while (running) {
                try {
                    System.out.println("Sunucudan mesaj bekleniyor...");
                    Message message = (Message) client.getInput().readObject();
                    System.out.println("Mesaj alındı: " + message.getType());
                    
                    if (message != null) {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                handleMessage(message);
                            } catch (Exception e) {
                                System.err.println("Mesaj işleme hatası: " + e);
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Mesaj okuma hatası: " + e);
                    e.printStackTrace();
                    if (running) {
                        SwingUtilities.invokeLater(() -> {
                            client.addLogMessage("Sunucu bağlantısı kesildi: " + e);
                            client.disconnectFromServer();
                        });
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("ClientListener hatası: " + e);
            e.printStackTrace();
        }
        
        System.out.println("ClientListener sonlandırıldı");
    }
    
    /**
     * Gelen mesajı işler.
     * İyileştirilmiş oyun sonu yönetimi eklendi.
     */
    private void handleMessage(Message message) {
        System.out.println("Mesaj işleniyor: " + message.getType());
        
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
            case PLAYER_READY:  // YENİ EKLENEN
                client.addLogMessage(message.getContent()); // Hazır oyuncuları göster
                break;
            case GAME_READY:
                client.addLogMessage(message.getContent());
                int response = JOptionPane.showConfirmDialog(client, 
                                                          "Oyun başlatılabilir. Başlatmak istiyor musunuz?", 
                                                          "Oyun Hazır", 
                                                          JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    // START_GAME yerine PLAYER_READY gönder
                    try {
                        Message readyMessage = new Message(client.getUsername(), "", MessageType.PLAYER_READY);
                        client.getOutput().writeObject(readyMessage);
                        client.getOutput().flush();
                        client.addLogMessage("Oyuna başlamak için hazır olduğunuzu bildirdiniz. Diğer oyuncular bekleniyor...");
                    } catch (IOException e) {
                        client.addLogMessage("Hazır mesajı gönderilemedi: " + e.getMessage());
                    }
                }
                break;
            case GAME_STARTED:
                client.addLogMessage("Oyun başladı!");
                break;
            case GAME_ENDED:
                client.addLogMessage(message.getContent());
                client.setGameControlsEnabled(false);
                
                // Kazananı kontrol et
                String winner = null;
                if (message.getContent().contains(" kazandı!")) {
                    // "Oyuncu1 kazandı! Oyun sona erdi." formatından kazananı çıkar
                    winner = message.getContent().split(" kazandı!")[0].trim();
                }
                client.handleGameEnd(winner);
                break;
            case TURN_CHANGED:
                client.addLogMessage(message.getContent());
                break;
            case GAME_STATE:
                if (message.getGameState() != null) {
                    System.out.println("Yeni oyun durumu alındı, güncelleniyor...");
                    client.updateGameState(message.getGameState());
                    System.out.println("Oyun durumu güncellendi.");
                } else {
                    client.addLogMessage("Hata: Geçersiz oyun durumu alındı.");
                }
                break;
            case MOVE_APPLIED:
                client.addLogMessage(message.getContent());
                break;
            case INVALID_MOVE:
                client.addLogMessage(message.getContent());
                break;
            default:
                client.addLogMessage("Bilinmeyen mesaj türü: " + message.getType());
                break;
        }
    }
}