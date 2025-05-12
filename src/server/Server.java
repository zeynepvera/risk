package server;

import common.Territory;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class Server {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server started. Waiting for clients...");

            // Oyun motorunu başlat
            List<String> players = Arrays.asList("Player1", "Player2");
            GameEngine game = new GameEngine(players);

            // Harita durumunu yazdır
            System.out.println("Baslangic haritası:");
            for (Territory t : game.getTerritories().values()) {
                System.out.println(
                        t.getName() + " - Sahip: " + t.getOwner() + ", Asker: " + t.getTroops()
                );
            }

            // Client 1 bağlantısı
            Socket socket1 = serverSocket.accept();
            System.out.println("Client 1 connected.");
            new SClient(socket1, game).start();

            // Client 2 bağlantısı
            Socket socket2 = serverSocket.accept();
            System.out.println("Client 2 connected.");
            new SClient(socket2, game).start();

            // Sunucu burada çalışmaya devam eder...

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
