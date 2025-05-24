package client.network;

import client.ClientListener;
import client.RiskClient;
import common.Message;
import common.MessageType;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class NetworkManager {
    
    private RiskClient parent;
    
    // Ağ bağlantısı
    private static final int PORT = 9034;
    private String serverIP;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String username;
    private boolean connected;
    private ClientListener clientListener;
    
    public NetworkManager(RiskClient parent) {
        this.parent = parent;
    }
    
    //CLIENT TARAFINDA SOCKET BAGLANTI 
    public void connectToServer(String username) {
        this.serverIP = "13.60.58.114";  // Sabit IP
        int port = 9034;                  // Sabit port
        this.username = username;

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Lütfen bir kullanıcı adı girin.", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!username.matches("^[a-zA-Z0-9_-]{3,16}$")) {
            JOptionPane.showMessageDialog(parent,
                    "Geçersiz kullanıcı adı! Kullanıcı adı 3-16 karakter uzunluğunda olmalı ve sadece harfler, rakamlar, alt çizgi ve tire içermelidir.",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            parent.updateStatusLabel("Bağlanıyor: " + serverIP + ":" + port + "...");
            Socket socket = new Socket();
            
            //TIMEOUT kontrolu burada
            socket.connect(new InetSocketAddress(serverIP, port), 10000);
            socket.setSoTimeout(300000);

            this.socket = socket;

            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());

            Message loginMessage = new Message(username, "", MessageType.LOGIN);
            System.out.println("Giriş mesajı gönderiliyor...");
            output.writeObject(loginMessage);
            output.flush();
            System.out.println("Giriş mesajı gönderildi.");

            //BURADA sunucudan geln nesajlari dinleyen thread
            
            clientListener = new ClientListener(parent);
            new Thread(clientListener).start();

            connected = true;
            parent.updateStatusLabel("Bağlantı durumu: Bağlı - " + serverIP + ":" + port);

            parent.setChatEnabled(true);

            parent.addLogMessage("Sunucuya bağlandı: " + serverIP + ":" + port);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Sunucuya bağlanılamadı: " + e.getMessage() + "\nAWS sunucusunun çalıştığından ve güvenlik grubunun " + port + " portuna izin verdiğinden emin olun.",
                    "Bağlantı Hatası", JOptionPane.ERROR_MESSAGE);
            parent.updateStatusLabel("Bağlantı durumu: Bağlantı hatası");
            parent.showMenuScreen();
        }
    }
    
    public void disconnectFromServer() {
        if (connected) {
            try {
                // Sunucuya çıkış mesajı gönder
                Message logoutMessage = new Message(username, "", MessageType.LOGOUT);
                output.writeObject(logoutMessage);
                output.flush();

                // Dinleyici durduruluyor
                if (clientListener != null) {
                    clientListener.stop();
                    clientListener = null;
                }

                // Bağlantı kapanıyor
                if (output != null) {
                    output.close();
                    output = null;
                }
                if (input != null) {
                    input.close();
                    input = null;
                }
                if (socket != null) {
                    socket.close();
                    socket = null;
                }

                connected = false;
                username = null;

                // GUI durumunu sıfırla
                parent.updateStatusLabel("Bağlantı durumu: Bağlı değil");
                parent.setChatEnabled(false);
                parent.setGameControlsEnabled(false);

                parent.addLogMessage("Sunucu bağlantısı kesildi.");
            } catch (IOException e) {
                parent.addLogMessage("Bağlantı kapatılırken hata: " + e.getMessage());
            }
        }
    }
    
    //CLIENT TARAFI
    
    public void sendMessage(Message message) {
        if (connected && output != null) {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                parent.addLogMessage("Mesaj gönderilemedi: " + e.getMessage());
            }
        }
    }
    
    // Getter metodları
    public boolean isConnected() { return connected; }
    public String getUsername() { return username; }
    public ObjectInputStream getInput() { return input; }
    public ObjectOutputStream getOutput() { return output; }
}