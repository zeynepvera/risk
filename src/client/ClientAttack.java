package client;

import common.Message;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientAttack {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            Scanner scanner = new Scanner(System.in);

            System.out.print("Saldiran bolge: ");
            String from = scanner.nextLine();

            System.out.print("Hedef bolge: ");
            String to = scanner.nextLine();

            Message msg = new Message("attack", "Player1"); // Player1 veya Player2'ye göre değiştir
            msg.addData("from", from);
            msg.addData("to", to);

            out.writeObject(msg);
            System.out.println("Saldiri komutu gonderildi.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
