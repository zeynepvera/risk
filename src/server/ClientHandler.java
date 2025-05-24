package server;

import common.GameAction;
import common.Message;
import common.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private final RiskServer server;
    private String username;
    private boolean running;
    private GameRoom currentRoom;

   // CLIENTIN BAĞLANTI İŞLEMESİ(Sunucuda her bir istemci bağlantısını yönetiyo)
    public ClientHandler(Socket socket, RiskServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.running = true;
        try {
            this.output = new ObjectOutputStream(clientSocket.getOutputStream());
            this.output.flush();
            this.input = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.err.println("Stream başlatma hatası: " + e.getMessage());
            running = false;
        }
    }

    
    //BURADAD DA THREAD KULLANDIM. ilk login bekliyom sonra sürekli mesaj okuma ilmee döngüsü
    @Override
    public void run() {
        try {
            Message loginMessage = (Message) input.readObject();
            if (loginMessage.getType() == MessageType.LOGIN) {
                this.username = loginMessage.getSender();
                server.registerClient(username, this);
            } else {
                sendMessage(new Message("SERVER", "Geçersiz ilk mesaj", MessageType.SERVER_FULL));
                closeConnection();
                return;
            }

            while (running) {
                try {
                    Message message = (Message) input.readObject();
                    handleMessage(message);
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println(username + " bağlantı koptu.");
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("İstemci döngü hatası: " + e.getMessage());
        } finally {
            if (username != null) {
                server.unregisterClient(username);
            }
            closeConnection();
        }
    }

    private void handleMessage(Message message) {
        if (message == null) {
            return;
        }

        switch (message.getType()) {
            case CHAT ->
                server.broadcastMessage(message);
            case START_GAME -> {
                if (currentRoom != null) {
                    currentRoom.startGame();
                }
            }
            case GAME_ACTION -> {
                GameAction action = message.getGameAction();
                if (action != null && currentRoom != null) {
                    currentRoom.applyMove(this, action);
                }
            }

          case SURRENDER -> {
    GameRoom room = getCurrentRoom();
    if (room != null) {
        room.handlePlayerSurrender(this); // YENİ METOD
    }
    running = false;
}



            case LOGOUT -> {
                running = false;
                server.unregisterClient(username);
            }
            default ->
                System.out.println("Bilinmeyen mesaj türü: " + message.getType());
        }
    }

    //SERVER TARAFI
    public void sendMessage(Message message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            System.err.println("Mesaj gönderilemedi: " + e.getMessage());
            closeConnection();
        }
    }

    public void closeConnection() {
        try {
            running = false;
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Bağlantı kapatma hatası: " + e.getMessage());
        }
    }

    // Getter-setter
    public String getUsername() {
        return username;
    }

    public GameRoom getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(GameRoom room) {
        this.currentRoom = room;
    }
}
