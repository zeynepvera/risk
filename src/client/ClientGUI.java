package client;

import common.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientGUI extends JFrame {
    
    private String playerName;


    private JTextField territoryField;
    private JTextField troopField;
    private JButton placeButton;

    private JTextField attackFromField;
    private JTextField attackToField;
    private JButton attackButton;

    private ObjectOutputStream out;

    public ClientGUI(String playerName) {
            this.playerName = playerName;

        setTitle("Risk Game - Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(6, 2));

        // Yerleştirme alanı
        add(new JLabel("Bölge Adı:"));
        territoryField = new JTextField();
        add(territoryField);

        add(new JLabel("Asker Sayısı:"));
        troopField = new JTextField();
        add(troopField);

        placeButton = new JButton("Yerleştir");
        add(placeButton);
        add(new JLabel("")); // boşluk

        // Saldırı alanı
        add(new JLabel("Saldıran Bölge:"));
        attackFromField = new JTextField();
        add(attackFromField);

        add(new JLabel("Hedef Bölge:"));
        attackToField = new JTextField();
        add(attackToField);

        attackButton = new JButton("Saldır");
        add(attackButton);
        add(new JLabel("")); // boşluk

        placeButton.addActionListener(this::sendPlaceCommand);
        attackButton.addActionListener(this::sendAttackCommand);

        connectToServer();

        setVisible(true);
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12345);
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Sunucuya bağlanılamadı!");
            System.exit(1);
        }
    }

    private void sendPlaceCommand(ActionEvent e) {
        try {
            String territory = territoryField.getText().trim();
            int troops = Integer.parseInt(troopField.getText().trim());

            Message msg = new Message("place", playerName);
            msg.addData("territory", territory);
            msg.addData("troops", troops);

            out.writeObject(msg);
            JOptionPane.showMessageDialog(this, "Yerleştirme komutu gönderildi!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hatalı giriş!");
        }
    }

    private void sendAttackCommand(ActionEvent e) {
        try {
            String from = attackFromField.getText().trim();
            String to = attackToField.getText().trim();

            Message msg = new Message("attack", playerName);
            msg.addData("from", from);
            msg.addData("to", to);

            out.writeObject(msg);
            JOptionPane.showMessageDialog(this, "Saldırı komutu gönderildi!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hatalı saldırı verisi!");
        }
    }

    public static void main(String[] args) {
    String name = JOptionPane.showInputDialog("Oyuncu adını gir (Player1 / Player2):");
    if (name == null || (!name.equals("Player1") && !name.equals("Player2"))) {
        JOptionPane.showMessageDialog(null, "Geçersiz oyuncu adı!");
        System.exit(0);
    }
    SwingUtilities.invokeLater(() -> new ClientGUI(name));
}

}
