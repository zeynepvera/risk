package server;

import model.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends Thread {

    public int clientId;
    ServerSocket ssocket;
    public ArrayList<SClient> clients;

    // Game-specific variables
    public GameMap map;
    public GameState gameState;
    public List<Player> playerList;

    public Server(int port) throws IOException {
        this.clientId = 0;
        this.ssocket = new ServerSocket(port);
        this.clients = new ArrayList<>();
    }

    public void StartAcceptance() {
        this.start();
    }

    public void SendConnectedClientIdsToAll() throws IOException {
        String data = "";
        for (SClient client : clients) {
            data += client.id + ",";
        }

        String msg = Message.GenerateMsg(Message.Type.CLIENTIDS, data);
        this.SendBroadcastMsg(msg.getBytes());
    }

    public void SendMessageToClient(int id, String msg) throws IOException {
        for (SClient client : clients) {
            if (client.id == id) {
                String rmsg = Message.GenerateMsg(Message.Type.MSGFROMCLIENT, msg);
                client.SendMessage(rmsg.getBytes());
                break;
            }
        }
    }

    public void SendBroadcastMsg(byte[] bmsg) throws IOException {
        for (SClient client : clients) {
            client.SendMessage(bmsg);
        }
    }

    public String serializeMap(GameMap map) {
        StringBuilder sb = new StringBuilder();
        for (Territory t : map.getAllTerritories()) {
            sb.append(t.getName())
                    .append(" - Sahibi: ").append(t.getOwnerId())
                    .append(", Asker: ").append(t.getArmyCount())
                    .append("\n");
        }
        return sb.toString();
    }

    @Override
    public void run() {
        try {
            while (!this.ssocket.isClosed()) {
                Socket csocket = this.ssocket.accept();
                SClient newClient = new SClient(csocket, this);
                newClient.StartListening();
                this.clients.add(newClient);
                this.SendConnectedClientIdsToAll();

                // Start game when 2 players are connected
                if (clients.size() == 2) {
                    System.out.println("İki oyuncu bağlandı. Oyun başlatılıyor...");

                    // Create players
                    playerList = new ArrayList<>();
                    playerList.add(new Player(clients.get(0).id, "Oyuncu 1"));
                    playerList.add(new Player(clients.get(1).id, "Oyuncu 2"));

                    // Create game map and state
                    map = new GameMap();
                    gameState = new GameState(map, playerList);

                    // Auto assign territories
                    int i = 0;
                    for (Territory t : map.getAllTerritories()) {
                        Player owner = playerList.get(i % 2);
                        t.setOwnerId(owner.getId());
                        t.setArmyCount(1);
                        owner.addTerritory(t);
                        i++;
                    }

                    System.out.println("Oyun başlatıldı! İlk oyuncu: " + gameState.getCurrentPlayer().getName());
                    for (Player p : playerList) {
                        String info = p.getId() + "," + p.getName();
                        String msg = Message.GenerateMsg(Message.Type.PLAYERINFO, info);
                        this.SendMessageToClient(p.getId(), msg);
                    }

                    String mapText = serializeMap(map);
                    String mapMsg = Message.GenerateMsg(Message.Type.MAPDATA, mapText);
                    this.SendBroadcastMsg(mapMsg.getBytes());

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        try {
            Server s1 = new Server(6000);
            s1.StartAcceptance();
            System.out.println("Server started. Waiting for connections...");
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
