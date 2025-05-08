package client;

import java.io.*;
import java.net.Socket;

public class GameClient {
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
    }

    public void sendMessage(String msg) {
        try {
            out.writeInt(msg.length());
            out.write(msg.getBytes());
        } catch (IOException e) {
            System.out.println("Mesaj gönderilemedi: " + e.getMessage());
        }
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
}
