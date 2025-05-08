package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SClient extends Thread {

    int id;
    Socket csocket;
    OutputStream coutput;
    InputStream cinput;
    Server ownerServer;

    public SClient(Socket connectedSocket, Server server) throws IOException {
        this.csocket = connectedSocket;
        this.coutput = this.csocket.getOutputStream();
        this.cinput = this.csocket.getInputStream();
        this.ownerServer = server;
        this.id = server.clientId;
        server.clientId++;
    }

    public void MsgParser(String msg) throws IOException {
        String tokens[] = msg.split("#");
        Message.Type mt = Message.Type.valueOf(tokens[0].trim());
        switch (mt) {
            case TOCLIENT:
                String datas[] = tokens[1].split(",");
                int targetId = Integer.parseInt(datas[0]);
                this.ownerServer.SendMessageToClient(targetId, datas[1]);
                break;
            default:
                System.out.println("Bilinmeyen mesaj tipi: " + mt);
        }
    }

    public void StartListening() {
        this.start();
    }

    public void run() {
        try {
            while (!this.csocket.isClosed()) {
                int bsize = this.cinput.read();
                byte buffer[] = new byte[bsize];
                this.cinput.read(buffer);
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
