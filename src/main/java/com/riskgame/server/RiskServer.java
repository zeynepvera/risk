package com.riskgame.server;

import com.riskgame.common.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Risk oyunu sunucu uygulaması. AWS üzerinde çalıştırılması planlanmıştır.
 */
public class RiskServer {

    private static final int PORT = 9876;
    private ServerSocket serverSocket;
    private boolean running;
    private Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private ServerGameState gameState;
    private int maxPlayers = 6;
    private int currentPlayerIndex = 0;
    private String serverIPForAWS;

    /**
     * Ana metod, sunucuyu başlatır.
     */
    public static void main(String[] args) {
        RiskServer server = new RiskServer();
        server.startServer();
    }

    /**
     * RiskServer constructor'ı. AWS bilgilerini de başlatır.
     */
    public RiskServer() {
        try {
            // AWS ortamında mevcut EC2 instance'ın public IP'sini al
            serverIPForAWS = getAWSInstanceIP();
            System.out.println("AWS Instance IP: " + serverIPForAWS);
        } catch (Exception e) {
            System.err.println("AWS IP bilgisi alınamadı: " + e.getMessage());
        }
    }

    /**
     * AWS EC2 instance'ının public IP adresini alır.
     */
    private String getAWSInstanceIP() {
        try {
            // AWS EC2 Metadata Service URL
            URL url = new URL("http://169.254.169.254/latest/meta-data/public-ipv4");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            return reader.readLine();
        } catch (Exception e) {
            System.err.println("AWS IP bilgisi alınamadı. Yerel modda çalışılıyor.");
            return "localhost";
        }
    }

    /**
     * Sunucuyu başlatır ve istemci bağlantılarını kabul eder.
     */
   public void startServer() {
    try {
        serverSocket = new ServerSocket(PORT);
        running = true;
        gameState = new ServerGameState();
        System.out.println("Risk Oyunu Sunucusu başlatıldı. Port: " + PORT);
        
        if (!"localhost".equals(serverIPForAWS)) {
            System.out.println("AWS Public IP: " + serverIPForAWS);
            System.out.println("Bağlantı adresi: " + serverIPForAWS + ":" + PORT);
        }
        
        System.out.println("Oyuncular bekleniyor...");
        
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(300000); // 5 dakika
                System.out.println("Yeni bağlantı: " + clientSocket.getInetAddress());
                
                if (clients.size() < maxPlayers) {
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();
                } else {
                    // Maksimum oyuncu sayısına ulaşıldı
                    System.out.println("Maksimum oyuncu sayısına ulaşıldı. Bağlantı reddedildi.");
                    try {
                        Message fullMessage = new Message("SERVER", "Sunucu dolu. Maksimum oyuncu sayısına ulaşıldı.", MessageType.SERVER_FULL);
                        new ObjectOutputStream(clientSocket.getOutputStream()).writeObject(fullMessage);
                        clientSocket.close();
                    } catch (IOException e) {
                        System.err.println("Bağlantı reddedilirken hata: " + e);
                    }
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Bağlantı kabul edilirken hata: " + e);
                }
            }
        }
    } catch (IOException e) {
        System.err.println("Sunucu başlatılırken hata: " + e);
    } finally {
        stopServer();
    }
}

    /**
     * Sunucuyu durdurur ve tüm bağlantıları kapatır.
     */
    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            for (ClientHandler client : clients.values()) {
                client.closeConnection();
            }

            clients.clear();
            System.out.println("Sunucu kapatıldı.");
        } catch (IOException e) {
            System.err.println("Sunucu kapatılırken hata: " + e.getMessage());
        }
    }

    /**
     * Yeni bir istemciyi sunucuya kaydeder. Geliştirilmiş hata yönetimi içerir.
     */
    public synchronized void registerClient(String username, ClientHandler clientHandler) {
        try {
            // Kullanıcı adı doğrulama
            if (username == null || username.trim().isEmpty()) {
                clientHandler.sendMessage(new Message("SERVER", "Geçersiz kullanıcı adı.", MessageType.SERVER_FULL));
                clientHandler.closeConnection();
                return;
            }

            // Geçersiz karakterler içeriyor mu kontrol et
            if (!username.matches("^[a-zA-Z0-9_-]{3,16}$")) {
                clientHandler.sendMessage(new Message("SERVER", "Kullanıcı adı sadece harf, rakam, alt çizgi ve tire içerebilir (3-16 karakter).", MessageType.SERVER_FULL));
                clientHandler.closeConnection();
                return;
            }

            // Kullanıcı adının halihazırda kullanımda olup olmadığını kontrol et
            if (clients.containsKey(username)) {
                clientHandler.sendMessage(new Message("SERVER", "Bu kullanıcı adı zaten kullanımda.", MessageType.SERVER_FULL));
                clientHandler.closeConnection();
                return;
            }

            // Oyunun başlamış olup olmadığını kontrol et
            if (hasGameStarted()) {
                clientHandler.sendMessage(new Message("SERVER", "Oyun zaten başlamış durumda.", MessageType.SERVER_FULL));
                clientHandler.closeConnection();
                return;
            }

            // Oyuncu sayısı sınırını kontrol et
            if (clients.size() >= maxPlayers) {
                clientHandler.sendMessage(new Message("SERVER", "Oyun dolu.", MessageType.SERVER_FULL));
                clientHandler.closeConnection();
                return;
            }

            // Yeni istemciyi kaydet
            clients.put(username, clientHandler);
            System.out.println(username + " oyuna katıldı. Toplam oyuncu: " + clients.size());
            broadcastMessage(new Message("SERVER", username + " oyuna katıldı.", MessageType.PLAYER_JOINED));

            // Oyuncu sayısı 2 veya daha fazla ise oyunu başlatmak için sor
            if (clients.size() >= 2) {
                broadcastMessage(new Message("SERVER", "Oyun başlatılabilir. Hazır mısınız?", MessageType.GAME_READY));
            }
        } catch (Exception e) {
            System.err.println("Kullanıcı kaydı sırasında hata: " + e.getMessage());
            clientHandler.sendMessage(new Message("SERVER", "Sunucu hatası: " + e.getMessage(), MessageType.SERVER_FULL));
            clientHandler.closeConnection();
        }
    }

    /**
     * Bir istemciyi sunucudan çıkarır.
     */
    public synchronized void unregisterClient(String username) {
        ClientHandler client = clients.remove(username);
        if (client != null) {
            System.out.println(username + " oyundan ayrıldı. Kalan oyuncu: " + clients.size());
            broadcastMessage(new Message("SERVER", username + " oyundan ayrıldı.", MessageType.PLAYER_LEFT));

            // Oyun başlamışsa ve oyuncu ayrıldıysa, oyunu sonlandır
            if (hasGameStarted() && clients.size() < 2) {
                endGame();
            }
        }
    }

    /**
     * Tüm istemcilere mesaj gönderir.
     */
    public void broadcastMessage(Message message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }

    /**
     * Oyunu başlatır ve bölgeleri oyunculara dağıtır.
     */
    public synchronized void startGame() {
        if (clients.size() >= 2 && !hasGameStarted()) {
            System.out.println("Oyun başlatılıyor...");
            gameState.initializeGame(new ArrayList<>(clients.keySet()));
            broadcastMessage(new Message("SERVER", "Oyun başlıyor!", MessageType.GAME_STARTED));
            broadcastGameState();

            // İlk oyuncunun sırasını belirt
            currentPlayerIndex = 0;
            nextTurn();
        }
    }

    /**
     * Oyunu sonlandırır.
     */
    public synchronized void endGame() {
        String winner = gameState.checkWinner();
        String message = "Oyun sona erdi.";
        if (winner != null) {
            message = winner + " kazandı! " + message;
        }
        broadcastMessage(new Message("SERVER", message, MessageType.GAME_ENDED));
        gameState = new ServerGameState();
        currentPlayerIndex = 0;
    }

    /**
     * Sıradaki oyuncuya geçiş yapar.
     */
    public synchronized void nextTurn() {
        if (hasGameStarted()) {
            List<String> playerList = gameState.getPlayerList();
            if (!playerList.isEmpty()) {
                // Geçerli indeksi kontrol et
                if (currentPlayerIndex >= playerList.size()) {
                    currentPlayerIndex = 0;
                }

                // Bir sonraki oyuncuya geç
                String currentPlayer = playerList.get(currentPlayerIndex);
                gameState.setCurrentPlayer(currentPlayer);

                broadcastMessage(new Message("SERVER", "Sıra " + currentPlayer + " oyuncusunda.", MessageType.TURN_CHANGED));

                // Yeni birlikleri hesapla ve ekle
                int newArmies = gameState.calculateReinforcementArmies(currentPlayer);
                gameState.setReinforcementArmies(currentPlayer, newArmies);

                // Güncel oyun durumunu gönder
                broadcastGameState();

                // Bir sonraki oyuncu için indeksi hazırla
                currentPlayerIndex = (currentPlayerIndex + 1) % playerList.size();
            }
        }
    }

    /**
     * Oyun durumunu tüm istemcilere gönderir.
     */
    public void broadcastGameState() {
        try {
            com.riskgame.common.GameState clientGameState = convertGameState();
            Message stateMessage = new Message("SERVER", "", MessageType.GAME_STATE);
            stateMessage.setGameState(clientGameState);
            broadcastMessage(stateMessage);
        } catch (Exception e) {
            System.err.println("Oyun durumu gönderilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ServerGameState'i client tarafına gönderilecek GameState'e dönüştürür
     */
    private com.riskgame.common.GameState convertGameState() {
        com.riskgame.common.GameState clientState = new com.riskgame.common.GameState();

        // Temel alanları kopyala
        clientState.getTerritories().putAll(gameState.getTerritories());
        clientState.getContinents().putAll(gameState.getContinents());
        clientState.getPlayers().putAll(gameState.getPlayers());
        clientState.getPlayerList().addAll(gameState.getPlayerList());
        clientState.setGameStarted(gameState.isGameStarted());
        clientState.setCurrentPlayer(gameState.getCurrentPlayer());

        return clientState;
    }

    /**
     * Oyunun başlayıp başlamadığını kontrol eder.
     */
    public boolean hasGameStarted() {
        return gameState != null && gameState.isGameStarted();
    }

    /**
     * Bir hareketin geçerli olup olmadığını kontrol eder.
     */
    public boolean isValidMove(String player, GameAction action) {
        if (!hasGameStarted()) {
            return false;
        }

        List<String> playerList = gameState.getPlayerList();
        if (playerList.isEmpty()) {
            return false;
        }

        String currentPlayer = gameState.getCurrentPlayer();
        if (!player.equals(currentPlayer)) {
            return false;
        }

        // Hareket türüne göre kontroller yapılır
        switch (action.getType()) {
            case PLACE_ARMY:
                return gameState.canPlaceArmy(player, action.getSourceTerritory(), action.getArmyCount());
            case ATTACK:
                return gameState.canAttack(player, action.getSourceTerritory(), action.getTargetTerritory(), action.getArmyCount());
            case FORTIFY:
                return gameState.canFortify(player, action.getSourceTerritory(), action.getTargetTerritory(), action.getArmyCount());
            case END_TURN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Bir hareketi uygular.
     */
    public void applyMove(String player, GameAction action) {
        if (!isValidMove(player, action)) {
            ClientHandler client = clients.get(player);
            if (client != null) {
                client.sendMessage(new Message("SERVER", "Geçersiz hareket!", MessageType.INVALID_MOVE));
            }
            return;
        }

        switch (action.getType()) {
            case PLACE_ARMY:
                gameState.placeArmy(player, action.getSourceTerritory(), action.getArmyCount());
                broadcastMessage(new Message("SERVER", player + ", " + action.getSourceTerritory() + " bölgesine " + action.getArmyCount() + " birlik yerleştirdi.", MessageType.MOVE_APPLIED));
                break;
            case ATTACK:
                AttackResult result = gameState.attack(player, action.getSourceTerritory(), action.getTargetTerritory(), action.getArmyCount());
                broadcastMessage(new Message("SERVER", player + ", " + action.getSourceTerritory() + " bölgesinden " + action.getTargetTerritory() + " bölgesine saldırdı. Sonuç: " + result.getDescription(), MessageType.MOVE_APPLIED));

                // Eğer bir oyuncu tüm bölgeleri kaybettiyse
                String eliminatedPlayer = result.getEliminatedPlayer();
                if (eliminatedPlayer != null) {
                    broadcastMessage(new Message("SERVER", eliminatedPlayer + " oyundan elendi!", MessageType.PLAYER_ELIMINATED));

                    // Kazanan kontrolü
                    String winner = gameState.checkWinner();
                    if (winner != null) {
                        broadcastMessage(new Message("SERVER", winner + " kazandı! Oyun bitti!", MessageType.GAME_ENDED));
                        // Oyunu sıfırla
                        gameState = new ServerGameState();
                        currentPlayerIndex = 0;
                        return; // Tur bitirmeye gerek yok
                    }
                }
                break;
            case FORTIFY:
                gameState.fortify(player, action.getSourceTerritory(), action.getTargetTerritory(), action.getArmyCount());
                broadcastMessage(new Message("SERVER", player + ", " + action.getSourceTerritory() + " bölgesinden " + action.getTargetTerritory() + " bölgesine " + action.getArmyCount() + " birlik taşıdı.", MessageType.MOVE_APPLIED));
                break;
            case END_TURN:
                broadcastMessage(new Message("SERVER", player + " turunu bitirdi.", MessageType.MOVE_APPLIED));
                nextTurn();
                return; // Tur sonlandı, gameState'i tekrar göndermemek için
        }

        // Güncel oyun durumunu gönder (END_TURN dışındaki durumlar için)
        broadcastGameState();
        System.out.println("Güncel oyun durumu gönderiliyor...");
        broadcastGameState();
        System.out.println("Oyun durumu gönderildi.");
    }

    /**
     * Sunucunun IP adresini döndürür. AWS üzerinde çalışıyorsa AWS IP adresini,
     * değilse localhost döndürür.
     */
    public String getServerIP() {
        return serverIPForAWS != null ? serverIPForAWS : "localhost";
    }
}
