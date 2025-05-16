package com.riskgame.server;

import com.riskgame.common.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

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
            // Soket zaman aşımını ayarla (5 dakika)
            socket.setSoTimeout(300000);
            
            // ObjectOutputStream'i ÖNCE oluşturmalıyız (Java'nın stream protokolü nedeniyle)
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush(); // Kritik: header bilgisinin hemen gönderilmesini sağlar
            input = new ObjectInputStream(socket.getInputStream());
            
            System.out.println("Yeni istemci bağlantısı başarıyla kuruldu: " + socket.getInetAddress());
        } catch (IOException e) {
            System.err.println("İstemci bağlantısı kurulurken hata: " + e.getMessage());
            e.printStackTrace();
            running = false;
        }
    }
    
   @Override
public void run() {
    try {
        System.out.println("ClientHandler başlatıldı: " + clientSocket.getInetAddress());
        
        // Kullanıcı adını al
        Message loginMessage = (Message) input.readObject();
        System.out.println("Mesaj alındı: " + loginMessage.getType());
        
        if (loginMessage.getType() == MessageType.LOGIN) {
            username = loginMessage.getSender();
            System.out.println("Kullanıcı adı: " + username);
            
            if (username != null && !username.isEmpty()) {
                server.registerClient(username, this);
                System.out.println("Kullanıcı kaydedildi: " + username);
            } else {
                System.out.println("Geçersiz kullanıcı adı");
                sendMessage(new Message("SERVER", "Geçersiz kullanıcı adı", MessageType.SERVER_FULL));
                closeConnection();
                return;
            }
        } else {
            System.out.println("İlk mesaj LOGIN değil: " + loginMessage.getType());
            closeConnection();
            return;
        }
        
        // Ana mesaj döngüsü
        while (running) {
            try {
                System.out.println(username + " için mesaj bekleniyor...");
                Message message = (Message) input.readObject();
                System.out.println("Mesaj alındı: " + message.getType() + " from " + username);
                handleMessage(message);
            } catch (Exception e) {
                System.err.println("Mesaj okuma hatası: " + e);
                e.printStackTrace();
                break;
            }
        }
    } catch (Exception e) {
        System.err.println("ClientHandler hatası: " + e);
        e.printStackTrace();
    } finally {
        System.out.println("ClientHandler sonlandırılıyor: " + username);
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
        if (message == null) {
            System.out.println("Null mesaj alındı!");
            return;
        }
        
        try {
            switch (message.getType()) {
                case CHAT:
                    // Sohbet mesajını tüm oyunculara ilet
                    System.out.println("Sohbet mesajı alındı: " + message.getContent());
                    server.broadcastMessage(message);
                    break;
                case START_GAME:
                    // Oyunu başlat
                    System.out.println(username + " kullanıcısı oyunu başlatmak istiyor");
                    server.startGame();
                    break;
                case GAME_ACTION:
                    // Oyun hareketini uygula
                    GameAction action = message.getGameAction();
                    if (action != null) {
                        System.out.println(username + " kullanıcısından oyun hareketi alındı: " + action.getType());
                        server.applyMove(username, action);
                    } else {
                        System.out.println("UYARI: GameAction null!");
                    }
                    break;
                case LOGOUT:
                    // Oyuncuyu çıkar
                    System.out.println(username + " kullanıcısı çıkış yapıyor");
                    server.unregisterClient(username);
                    running = false; // Döngüyü sonlandır
                    break;
                default:
                    // Diğer mesaj türleri
                    System.out.println("Beklenmedik mesaj türü (" + username + "): " + message.getType());
                    break;
            }
        } catch (Exception e) {
            System.err.println("Mesaj işleme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * İstemciye mesaj gönderir.
     */
    public void sendMessage(Message message) {
        try {
            if (output != null && running) {
                System.out.println(username + " kullanıcısına mesaj gönderiliyor. Tür: " + message.getType());
                output.writeObject(message);
                output.flush(); // Mesajın hemen gönderilmesini sağlar
                System.out.println("Mesaj başarıyla gönderildi");
            }
        } catch (IOException e) {
            System.err.println("Mesaj gönderilirken hata (" + username + "): " + e.getMessage());
            e.printStackTrace();
            closeConnection();
        }
    }
    
    /**
     * Bağlantıyı kapatır.
     */
    public void closeConnection() {
        if (!running) {
            return; // Zaten kapatıldıysa tekrar kapatma
        }
        
        running = false;
        System.out.println("Bağlantı kapatılıyor: " + username);
        
        try {
            if (input != null) {
                try { input.close(); } catch (IOException e) { System.err.println("Input stream kapatılırken hata: " + e.getMessage()); }
            }
            if (output != null) {
                try { output.close(); } catch (IOException e) { System.err.println("Output stream kapatılırken hata: " + e.getMessage()); }
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                try { clientSocket.close(); } catch (IOException e) { System.err.println("Socket kapatılırken hata: " + e.getMessage()); }
            }
            
            System.out.println("Bağlantı başarıyla kapatıldı: " + username);
        } catch (Exception e) {
            System.err.println("Bağlantı kapatılırken beklenmeyen hata: " + e.getMessage());
            e.printStackTrace();
        }
    }
}