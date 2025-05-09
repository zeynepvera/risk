package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        System.out.println("📩 Gelen mesaj: " + msg); // LOG

        String[] tokens = msg.split("#");
        Message.Type mt;

        try {
            mt = Message.Type.valueOf(tokens[0].trim());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Bilinmeyen mesaj tipi: " + tokens[0]);
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

            default:
                System.out.println("⚠️ İşlenmeyen mesaj tipi: " + mt);
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
                this.cinput.readFully(buffer); // 👈 Kritik düzeltme: tüm mesaj okunur
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
