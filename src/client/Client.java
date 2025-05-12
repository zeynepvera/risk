package client;

import common.Message;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            Scanner scanner = new Scanner(System.in);
            System.out.println("Bolge adı girin:");
            String territory = scanner.nextLine();

            System.out.println("Kac asker eklemek istiyorsunuz?");
            int troops = scanner.nextInt();

            Message msg = new Message("place", "Player1");  // Şimdilik hep Player1
            msg.addData("territory", territory);
            msg.addData("troops", troops);

            out.writeObject(msg);
            System.out.println("Yerlestirme komutu sunucuya gonderildi.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
