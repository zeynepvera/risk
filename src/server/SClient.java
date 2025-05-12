package server;

import common.Message;

import java.io.ObjectInputStream;
import java.net.Socket;

public class SClient extends Thread {

    private Socket socket;
    private GameEngine game;
    private ObjectInputStream in;

    public SClient(Socket socket, GameEngine game) {
        this.socket = socket;
        this.game = game;
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Message msg = (Message) in.readObject();
                System.out.println("[" + msg.getFromPlayer() + "] " + msg.getType());

                switch (msg.getType()) {
                    case "place" -> {
                        String territory = (String) msg.getData("territory");
                        int troops = (int) msg.getData("troops");
                        game.placeTroops(msg.getFromPlayer(), territory, troops);
                    }
                    case "attack" -> {
                        String from = (String) msg.getData("from");
                        String to = (String) msg.getData("to");
                        game.attack(msg.getFromPlayer(), from, to);
                    }
                    default -> System.out.println("Bilinmeyen mesaj türü: " + msg.getType());
                }
            }

        } catch (Exception e) {
            System.out.println("İstemci bağlantısı kesildi.");
        }
    }
}
