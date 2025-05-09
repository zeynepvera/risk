package client;

import java.io.*;
import java.net.Socket;
import server.Message;

public class GameClient {

    private Thread listenThread;
    public GameClientListener listener; // GUI'den bağlayacağız

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public GameClient(String serverIP, int port) {
        try {
            socket = new Socket(serverIP, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            System.out.println("Sunucuya bağlanıldı: " + serverIP + ":" + port);
        } catch (IOException e) {
            System.out.println("Bağlantı hatası: " + e.getMessage());
        }
        startListening();

    }

    private void startListening() {
        listenThread = new Thread(() -> {
            while (!socket.isClosed()) {
                try {
                    int size = in.readInt();
                    byte[] buffer = new byte[size];
                    in.readFully(buffer);
                    String message = new String(buffer);
                    parseMessage(message);
                } catch (IOException e) {
                    System.out.println("Dinleme hatası: " + e.getMessage());
                    break;
                }
            }
        });
        listenThread.start();
    }

    private void parseMessage(String msg) {
        try {
            String[] tokens = msg.split("#", 2);
            Message.Type type = Message.Type.valueOf(tokens[0].trim());
            String data = tokens.length > 1 ? tokens[1] : "";

            switch (type) {
                case MAPDATA:
                    if (listener != null) {
                        listener.onMapDataReceived(data);
                    }
                    break;

                case PLAYERINFO:
                    if (listener != null) {
                        String[] parts = data.split(",");
                        int id = Integer.parseInt(parts[0]);
                        String name = parts[1];
                        listener.onPlayerInfoReceived(id, name);
                    }
                    break;

                default:
                    System.out.println("Gelen mesaj: " + msg);
            }
        } catch (Exception e) {
            System.out.println("Mesaj ayrıştırılamadı: " + msg);
        }
    }

    public void sendMessage(String msg) {
        try {
            out.writeInt(msg.length());
            out.write(msg.getBytes());
        } catch (IOException e) {
            System.out.println("Mesaj gönderilemedi: " + e.getMessage());
        }
        System.out.println(">> Gönderilen mesaj: " + msg);

    }

    public String receiveMessage() {
        try {
            int size = in.readInt();
            byte[] buffer = new byte[size];
            in.readFully(buffer);
            return new String(buffer);
        } catch (IOException e) {
            System.out.println("Mesaj alınamadı: " + e.getMessage());
            return null;
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Bağlantı kapatılamadı: " + e.getMessage());
        }
    }

    public interface GameClientListener {

        void onMapDataReceived(String mapText);
        void onPlayerInfoReceived(int id, String name);

    }

}
