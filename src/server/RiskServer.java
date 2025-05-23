package server;

import common.GameAction;
import common.GameState;
import common.Message;
import common.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RiskServer {

    private static final int PORT = 9034;
    private ServerSocket serverSocket;
    private boolean running;
    private Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private String serverIPForAWS;
    private List<GameRoom> activeRooms = new ArrayList<>();

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        RiskServer server = new RiskServer();
        server.startServer();
    }

    public RiskServer() {
        try {
            serverIPForAWS = getAWSInstanceIP();
            System.out.println("AWS Instance IP: " + serverIPForAWS);
        } catch (Exception e) {
            System.err.println("AWS IP bilgisi alınamadı: " + e.getMessage());
        }
    }

    private String getAWSInstanceIP() {
        try {
            ProcessBuilder pb = new ProcessBuilder("curl", "-s", "http://169.254.169.254/latest/meta-data/public-ipv4");
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String publicIP = reader.readLine();
                process.waitFor();

                if (publicIP != null && !publicIP.trim().isEmpty() && !publicIP.contains("404")) {
                    return publicIP.trim();
                }
            }
        } catch (Exception ignored) {}

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
            return "localhost";
        }
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT, 20, InetAddress.getByName("0.0.0.0"));
            running = true;
            System.out.println("Risk Oyunu Sunucusu başlatıldı. Port: " + PORT);

            if (!"localhost".equals(serverIPForAWS)) {
                System.out.println("AWS Public IP: " + serverIPForAWS);
                System.out.println("Bağlantı adresi: " + serverIPForAWS + ":" + PORT);
            }

            System.out.println("Oyuncular bekleniyor...");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(300000);
                    System.out.println("Yeni bağlantı: " + clientSocket.getInetAddress());

                    if (clients.size() < 100) {
                        ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                        Thread clientThread = new Thread(clientHandler);
                        clientThread.start();
                    } else {
                        System.out.println("Sunucu kapasitesine ulaşıldı. Bağlantı reddedildi.");
                        try {
                            Message fullMessage = new Message("SERVER", "Sunucu dolu.", MessageType.SERVER_FULL);
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

    public synchronized void registerClient(String username, ClientHandler clientHandler) {
        try {
            if (username == null || username.trim().isEmpty()
                    || !username.matches("^[a-zA-Z0-9_-]{3,16}$")
                    || clients.containsKey(username)) {
                clientHandler.sendMessage(new Message("SERVER", "Geçersiz veya kullanımda olan kullanıcı adı.", MessageType.SERVER_FULL));
                clientHandler.closeConnection();
                return;
            }

            clients.put(username, clientHandler);
            System.out.println(username + " oyuna katıldı.");
            broadcastMessage(new Message("SERVER", username + " oyuna katıldı.", MessageType.PLAYER_JOINED));

            GameRoom room = findOrCreateRoom();
            room.getPlayers().add(clientHandler);
            clientHandler.setCurrentRoom(room);

            if (room.isFull()) {
                room.startGame();
            }

        } catch (Exception e) {
            System.err.println("Kullanıcı kaydı sırasında hata: " + e.getMessage());
            clientHandler.sendMessage(new Message("SERVER", "Sunucu hatası: " + e.getMessage(), MessageType.SERVER_FULL));
            clientHandler.closeConnection();
        }
    }

    private GameRoom findOrCreateRoom() {
        for (GameRoom room : activeRooms) {
            if (!room.isFull()) return room;
        }
        GameRoom newRoom = new GameRoom();
        activeRooms.add(newRoom);
        return newRoom;
    }

    public synchronized void unregisterClient(String username) {
        ClientHandler client = clients.remove(username);
        if (client != null) {
            GameRoom room = client.getCurrentRoom();
            if (room != null) {
                room.removePlayer(client);
            }
            client.closeConnection();
            System.out.println(username + " oyundan ayrıldı.");
        }
    }

    public void broadcastMessage(Message message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }

    public void applyMove(String playerName, GameAction action) {
        ClientHandler client = clients.get(playerName);
        if (client == null) return;

        GameRoom room = client.getCurrentRoom();
        if (room != null) {
            room.applyMove(client, action);
        }
    }

    public String getServerIP() {
        return serverIPForAWS != null ? serverIPForAWS : "localhost";
    }
}
