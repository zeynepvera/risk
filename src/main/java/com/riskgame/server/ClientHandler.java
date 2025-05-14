package com.riskgame.server;

import com.riskgame.common.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * İstemci işleyici sınıfı.
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private RiskServer server;
    private String username;
    private boolean running;
    
    public ClientHandler(Socket socket, RiskServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.running = true;
        try {
            // ObjectOutputStream'i ÖNCE oluşturmalıyız (Java'nın stream protokolü nedeniyle)
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush(); // Kritik: header bilgisinin hemen gönderilmesini sağlar
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("İstemci bağlantısı kurulurken hata: " + e.getMessage());
            running = false;
        }
    }
    
    @Override
    public void run() {
        try {
            // Kullanıcı adını al
            Message loginMessage = (Message) input.readObject();
            if (loginMessage.getType() == MessageType.LOGIN) {
                username = loginMessage.getSender();
                if (username != null && !username.isEmpty()) {
                    server.registerClient(username, this);
                } else {
                    // Geçersiz kullanıcı adı
                    sendMessage(new Message("SERVER", "Geçersiz kullanıcı adı", MessageType.SERVER_FULL));
                    closeConnection();
                    return;
                }
            } else {
                // İlk mesaj LOGIN değilse bağlantıyı kapat
                closeConnection();
                return;
            }
            
            // Ana mesaj döngüsü
            while (running) {
                Message message = (Message) input.readObject();
                handleMessage(message);
            }
        } catch (IOException e) {
            System.err.println("İstemci ile iletişim hatası: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Geçersiz mesaj formatı: " + e.getMessage());
        } finally {
            if (username != null) {
                server.unregisterClient(username);
            }
            closeConnection();
        }
    }
    
    /**
     * Gelen mesajı işler.
     */
    private void handleMessage(Message message) {
        if (message == null) return;
        
        switch (message.getType()) {
            case CHAT:
                // Sohbet mesajını tüm oyunculara ilet
                server.broadcastMessage(message);
                break;
            case START_GAME:
                // Oyunu başlat
                server.startGame();
                break;
            case GAME_ACTION:
                // Oyun hareketini uygula
                GameAction action = message.getGameAction();
                if (action != null) {
                    server.applyMove(username, action);
                }
                break;
            case LOGOUT:
                // Oyuncuyu çıkar
                server.unregisterClient(username);
                running = false; // Döngüyü sonlandır
                break;
            default:
                // Diğer mesaj türleri
                System.out.println("Beklenmedik mesaj türü: " + message.getType());
                break;
        }
    }
    
    /**
     * İstemciye mesaj gönderir.
     */
    public void sendMessage(Message message) {
        try {
            if (output != null && running) {
                output.writeObject(message);
                output.flush(); // Mesajın hemen gönderilmesini sağlar
            }
        } catch (IOException e) {
            System.err.println("Mesaj gönderilirken hata: " + e.getMessage());
            closeConnection();
        }
    }
    
    /**
     * Bağlantıyı kapatır.
     */
    public void closeConnection() {
        running = false;
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Bağlantı kapatılırken hata: " + e.getMessage());
        }
    }
}