package server;

import common.AttackResult;
import common.GameAction;
import common.Continent;
import common.MessageType;
import common.Territory;
import common.Player;
import common.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Risk oyunu sunucu uygulaması. AWS üzerinde çalıştırılması planlanmıştır.
 */
public class RiskServer {

    private static final int PORT = 9034;
    private ServerSocket serverSocket;
    private boolean running;
    private Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private ServerGameState gameState;
    private int maxPlayers = 6;
    private int currentPlayerIndex = 0;
    private String serverIPForAWS;
    private Map<String, Boolean> playerReadyState = new ConcurrentHashMap<>(); // Oyuncuların hazır olma durumunu takip eder

    /**
     * Ana metod, sunucuyu başlatır.
     */
    public static void main(String[] args) {

        // Karakter kodlamasını ayarla
        System.setProperty("file.encoding", "UTF-8");
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
            serverSocket = new ServerSocket(PORT, 0, InetAddress.getByName("0.0.0.0"));
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
            playerReadyState.clear(); // Hazır durumları temizle
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
            playerReadyState.put(username, false); // Başlangıçta oyuncu hazır değil
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
        playerReadyState.remove(username); // Hazırlık durumundan da çıkar

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
     * Oyuncunun hazır olduğunu işaretler ve gerekirse oyunu başlatır. YENİ
     * METOD
     */
    public synchronized void playerReady(String playerName) {
        System.out.println(playerName + " oyuncu hazır.");
        playerReadyState.put(playerName, true);

        // Tüm oyuncular hazır mı kontrol et
        boolean allReady = true;
        int readyCount = 0;

        for (String player : clients.keySet()) {
            Boolean ready = playerReadyState.getOrDefault(player, false);
            if (ready) {
                readyCount++;
            } else {
                allReady = false;
            }
        }

        // Hazır oyuncuların durumunu bildir
        StringBuilder readyPlayers = new StringBuilder("Hazır olan oyuncular: ");
        boolean first = true;
        for (Map.Entry<String, Boolean> entry : playerReadyState.entrySet()) {
            if (entry.getValue()) {
                if (!first) {
                    readyPlayers.append(", ");
                }
                readyPlayers.append(entry.getKey());
                first = false;
            }
        }
        readyPlayers.append(" (").append(readyCount).append("/").append(clients.size()).append(")");

        broadcastMessage(new Message("SERVER", readyPlayers.toString(), MessageType.PLAYER_READY));

        // Tüm oyuncular hazırsa oyunu başlat
        if (allReady && clients.size() >= 2) {
            startGame();
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

            // Hazırlık durumlarını sıfırla
            for (String player : clients.keySet()) {
                playerReadyState.put(player, false);
            }

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

        // Yeni oyun için hazırlık durumlarını sıfırla
        for (String player : clients.keySet()) {
            playerReadyState.put(player, false);
        }
    }

    /**
     * Sıradaki oyuncuya geçiş yapar.
     */
    public synchronized void nextTurn() {
        if (hasGameStarted()) {
            List<String> playerList = gameState.getPlayerList();
            if (!playerList.isEmpty()) {
                if (currentPlayerIndex >= playerList.size()) {
                    currentPlayerIndex = 0;
                }

                String currentPlayer = playerList.get(currentPlayerIndex);
                gameState.setCurrentPlayer(currentPlayer);

                // 🔴 İlgili tekrar eden mesaj bu satırda gönderiliyor
                //  broadcastMessage(new Message("SERVER", "Sıra " + currentPlayer + " oyuncusunda.", MessageType.TURN_CHANGED));
                int newArmies = gameState.calculateReinforcementArmies(currentPlayer);
                gameState.setReinforcementArmies(currentPlayer, newArmies);

                broadcastGameState();

                currentPlayerIndex = (currentPlayerIndex + 1) % playerList.size();
            }
        }
    }

    /**
     * Oyun durumunu tüm istemcilere gönderir.
     */
    public void broadcastGameState() {
        try {
            System.out.println("\n=== OYUN DURUMU YAYINLANIYOR ===");
            common.GameState clientGameState = convertGameState();

            // Log: Bazı bölgelerin durumlarını yazdır
            System.out.println("Güncellenmiş bölge örnekleri:");
            int count = 0;
            for (Map.Entry<String, Territory> entry : gameState.getTerritories().entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue().getArmies() + " birlik");
                count++;
                if (count >= 5) {
                    break; // Sadece birkaç örnek göster
                }
            }

            Message stateMessage = new Message("SERVER", "", MessageType.GAME_STATE);
            stateMessage.setGameState(clientGameState);
            broadcastMessage(stateMessage);
            System.out.println("Oyun durumu tüm istemcilere gönderildi.");
            System.out.println("=== OYUN DURUMU YAYINLAMA TAMAMLANDI ===\n");
        } catch (Exception e) {
            System.err.println("Oyun durumu gönderilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ServerGameState'i client tarafına gönderilecek GameState'e dönüştürür
     */
    private common.GameState convertGameState() {
        common.GameState clientState = new common.GameState();

        // Territories kopyala - derin kopya olarak
        for (Map.Entry<String, Territory> entry : gameState.getTerritories().entrySet()) {
            Territory original = entry.getValue();
            Territory territoryCopy = new Territory(original); // Derin kopya constructor'ı kullan
            clientState.getTerritories().put(entry.getKey(), territoryCopy);
        }

        // Continents kopyala
        for (Map.Entry<String, Continent> entry : gameState.getContinents().entrySet()) {
            Continent original = entry.getValue();
            Continent continentCopy = new Continent(original.getName(), original.getBonus());
            clientState.getContinents().put(entry.getKey(), continentCopy);
        }

        // Players kopyala
        for (Map.Entry<String, Player> entry : gameState.getPlayers().entrySet()) {
            Player original = entry.getValue();
            Player playerCopy = new Player(original.getName());
            playerCopy.setReinforcementArmies(original.getReinforcementArmies());
            for (String territory : original.getTerritories()) {
                playerCopy.addTerritory(territory);
            }
            clientState.getPlayers().put(entry.getKey(), playerCopy);
        }

        // PlayerList kopyala
        clientState.getPlayerList().addAll(gameState.getPlayerList());

        // Diğer alanlar
        clientState.setGameStarted(gameState.isGameStarted());
        clientState.setCurrentPlayer(gameState.getCurrentPlayer());

        System.out.println("\n=== GAME STATE DÖNÜŞTÜRME ===");
        System.out.println("Dönüştürülen oyun durumu:");

        // Daha ayrıntılı log: tüm bölgelerin birlik sayılarını ve sahiplerini yazdır
        System.out.println("Bölge durumları:");
        for (Map.Entry<String, Territory> entry : clientState.getTerritories().entrySet()) {
            System.out.println("Bölge: " + entry.getKey()
                    + " | Sahibi: " + entry.getValue().getOwner()
                    + " | Birlik: " + entry.getValue().getArmies());
        }

        System.out.println("=== GAME STATE DÖNÜŞTÜRME TAMAMLANDI ===\n");

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

                // Güncellenmiş attack metodunu çağır
                AttackResult result = gameState.attack(
                        player,
                        action.getSourceTerritory(),
                        action.getTargetTerritory(),
                        action.getArmyCount()
                );

                // Zar sonuçlarını içeren detaylı bir mesaj oluştur
                StringBuilder resultMessage = new StringBuilder();
                resultMessage.append(player)
                        .append(", ")
                        .append(action.getSourceTerritory())
                        .append(" bölgesinden ")
                        .append(action.getTargetTerritory())
                        .append(" bölgesine saldırdı. ");

                // Zar detaylarını ekle (opsiyonel)
                if (result.getAttackDice() != null && result.getDefenseDice() != null) {
                    resultMessage.append("Zarlar: [Saldıran: ");
                    for (int i = 0; i < result.getAttackDice().length; i++) {
                        resultMessage.append(result.getAttackDice()[i]);
                        if (i < result.getAttackDice().length - 1) {
                            resultMessage.append(",");
                        }
                    }
                    resultMessage.append(" vs Savunan: ");
                    for (int i = 0; i < result.getDefenseDice().length; i++) {
                        resultMessage.append(result.getDefenseDice()[i]);
                        if (i < result.getDefenseDice().length - 1) {
                            resultMessage.append(",");
                        }
                    }
                    resultMessage.append("]. ");
                }

                // Sonucu ekle
                resultMessage.append("Sonuç: ")
                        .append(result.getDescription());

                broadcastMessage(new Message("SERVER", resultMessage.toString(), MessageType.MOVE_APPLIED));

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
    }

    /**
     * Sunucunun IP adresini döndürür. AWS üzerinde çalışıyorsa AWS IP adresini,
     * değilse localhost döndürür.
     */
    public String getServerIP() {
        return serverIPForAWS != null ? serverIPForAWS : "localhost";
    }
}
