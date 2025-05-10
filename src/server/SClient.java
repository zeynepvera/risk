package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Territory;

public class SClient extends Thread {

    int id;
    Socket csocket;
    OutputStream coutput;
    DataInputStream cinput;
    Server ownerServer;

    public SClient(Socket connectedSocket, Server server) throws IOException {
        this.csocket = connectedSocket;
        this.coutput = this.csocket.getOutputStream();
        this.cinput = new DataInputStream(this.csocket.getInputStream());
        this.ownerServer = server;
        this.id = server.clientId;
        server.clientId++;
    }

    public void MsgParser(String msg) throws IOException {
        System.out.println("Gelen mesaj: " + msg);

        String[] tokens = msg.split("#");
        Message.Type mt;

        try {
            mt = Message.Type.valueOf(tokens[0].trim());
        } catch (IllegalArgumentException e) {
            System.out.println("Bilinmeyen mesaj tipi: " + tokens[0]);
            return;
        }

        switch (mt) {
            case PLAYERNAME:
                this.ownerServer.playerList
                        .stream()
                        .filter(p -> p.getId() == this.id)
                        .findFirst()
                        .ifPresent(p -> p.setName(tokens[1]));
                break;

            case TOCLIENT:
                String[] datas = tokens[1].split(",");
                int targetId = Integer.parseInt(datas[0]);
                this.ownerServer.SendMessageToClient(targetId, datas[1]);
                break;

            case ATTACK:
                String[] bolgeler = tokens[1].split(",");
                String kaynak = bolgeler[0];
                String hedef = bolgeler[1];

                System.out.println(" Saldırı: " + kaynak + " → " + hedef);

                Territory kaynakBolge = this.ownerServer.map.getTerritory(kaynak);
                Territory hedefBolge = this.ownerServer.map.getTerritory(hedef);

                if (kaynakBolge == null || hedefBolge == null) {
                    System.out.println(" Geçersiz bölge adı.");
                    return;
                }

                int saldiranId = this.id;

                if (kaynakBolge.getOwnerId() != saldiranId) {
                    System.out.println(" Saldıran, kaynağın sahibi değil.");
                    return;
                }

                // Saldıran her zaman kazanır (şimdilik)
                hedefBolge.setOwnerId(saldiranId);
                hedefBolge.setArmyCount(1);
                kaynakBolge.removeArmies(1);

                this.ownerServer.playerList.forEach(p -> p.removeTerritory(hedefBolge));
                this.ownerServer.playerList.stream()
                        .filter(p -> p.getId() == saldiranId)
                        .findFirst()
                        .ifPresent(p -> p.addTerritory(hedefBolge));

                // Haritayı yeniden gönder
                String mapText = this.ownerServer.serializeMap(this.ownerServer.map);
                String mapMsg = Message.GenerateMsg(Message.Type.MAPDATA, mapText);
                this.ownerServer.SendBroadcastMsg(mapMsg.getBytes());

                this.ownerServer.gameState.nextTurn();
                break;

            default:
                System.out.println(" Tanımsız mesaj tipi: " + mt);
        }
    }

    public void StartListening() {
        this.start();
    }

    @Override
    public void run() {
        try {
            while (!this.csocket.isClosed()) {
                int bsize = this.cinput.read();
                byte[] buffer = new byte[bsize];
                this.cinput.readFully(buffer);
                String rsMsg = new String(buffer);
                this.MsgParser(rsMsg);
            }
        } catch (IOException ex) {
            this.ownerServer.clients.remove(this);
            try {
                this.ownerServer.SendConnectedClientIdsToAll();
            } catch (IOException ex1) {
                Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    public void SendMessage(byte[] msg) throws IOException {
        this.coutput.write(msg.length);
        this.coutput.write(msg);
    }
}
