package com.riskgame.client;

import com.riskgame.common.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;

/**
 * Risk oyunu istemci uygulaması.
 * Kullanıcı dostu grafiksel arayüz içerir.
 */
public class RiskClient extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Ağ bağlantısı
    private static final int PORT = 9876;
    private String serverIP;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String username;
    private boolean connected;
    private ClientListener clientListener;
    
    // Oyun durumu
    private GameState gameState;
    private String selectedTerritory;
    private ActionType currentAction;
    
    // GUI bileşenleri
    private JPanel mainPanel;
    private MapPanel mapPanel;
    private JTextArea chatArea;
    private JTextField chatField;
    private JButton sendButton;
    private JPanel controlPanel;
    private JLabel statusLabel;
    private JButton placeArmyButton;
    private JButton attackButton;
    private JButton fortifyButton;
    private JButton endTurnButton;
    private JComboBox<Integer> armyCountComboBox;
    private JButton connectButton;
    private JTextField serverIPField;
    private JTextField usernameField;
    
    /**
     * Ana metod, istemciyi başlatır.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RiskClient client = new RiskClient();
            client.setVisible(true);
        });
    }
    
    /**
     * Risk istemcisi constructor'ı.
     */
    public RiskClient() {
        super("Risk Oyunu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        initializeGUI();
        
        // Pencere kapanırken bağlantıyı kapat
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnectFromServer();
            }
        });
    }
    
    /**
     * GUI bileşenlerini oluşturur ve yerleştirir.
     */
    private void initializeGUI() {
        mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);
        
        // Oyun haritası paneli
        mapPanel = new MapPanel(this);
        mainPanel.add(mapPanel, BorderLayout.CENTER);
        
        // Sağ panel (kontrol + sohbet)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, 0));
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        // Bağlantı paneli
        JPanel connectionPanel = createConnectionPanel();
        rightPanel.add(connectionPanel, BorderLayout.NORTH);
        
        // Kontrol paneli
        controlPanel = createControlPanel();
        rightPanel.add(controlPanel, BorderLayout.CENTER);
        
        // Sohbet paneli
        JPanel chatPanel = createChatPanel();
        rightPanel.add(chatPanel, BorderLayout.SOUTH);
        
        // Başlangıçta oyun kontrollerini devre dışı bırak
        setGameControlsEnabled(false);
    }
    
    /**
     * Bağlantı panelini oluşturur.
     */
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Sunucu Bağlantısı"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Sunucu IP:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        serverIPField = new JTextField("localhost", 15);
        panel.add(serverIPField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Kullanıcı Adı:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        connectButton = new JButton("Bağlan");
        connectButton.addActionListener(e -> {
            if (!connected) {
                connectToServer();
            } else {
                disconnectFromServer();
            }
        });
        panel.add(connectButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        statusLabel = new JLabel("Bağlantı durumu: Bağlı değil", JLabel.CENTER);
        panel.add(statusLabel, gbc);
        
        return panel;
    }
    
    /**
     * Kontrol panelini oluşturur.
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Oyun Kontrolleri"));
        
        // Birlik sayısı seçimi
        JPanel armyPanel = new JPanel();
        armyPanel.add(new JLabel("Birlik sayısı:"));
        armyCountComboBox = new JComboBox<>(new Integer[] {1, 2, 3, 5, 10});
        armyPanel.add(armyCountComboBox);
        panel.add(armyPanel);
        
        // Aksiyon butonları
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        placeArmyButton = new JButton("Birlik Yerleştir");
        placeArmyButton.addActionListener(e -> setCurrentAction(ActionType.PLACE_ARMY));
        buttonPanel.add(placeArmyButton);
        
        attackButton = new JButton("Saldır");
        attackButton.addActionListener(e -> setCurrentAction(ActionType.ATTACK));
        buttonPanel.add(attackButton);
        
        fortifyButton = new JButton("Takviye");
        fortifyButton.addActionListener(e -> setCurrentAction(ActionType.FORTIFY));
        buttonPanel.add(fortifyButton);
        
        endTurnButton = new JButton("Turu Bitir");
        endTurnButton.addActionListener(e -> endTurn());
        buttonPanel.add(endTurnButton);
        
        panel.add(buttonPanel);
        
        // Oyun bilgileri
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Oyun Bilgileri"));
        
        // Oyuncu bilgileri dinamik olarak eklenecek
        
        panel.add(infoPanel);
        
        return panel;
    }
    
    /**
     * Sohbet panelini oluşturur.
     */
    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Sohbet"));
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        chatField = new JTextField();
        chatField.addActionListener(e -> sendChatMessage());
        inputPanel.add(chatField, BorderLayout.CENTER);
        
        sendButton = new JButton("Gönder");
        sendButton.addActionListener(e -> sendChatMessage());
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        panel.add(inputPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Sunucuya bağlanır.
     */
    public void connectToServer() {
        serverIP = serverIPField.getText().trim();
        username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen bir kullanıcı adı girin.", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            socket = new Socket(serverIP, PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            
            // Giriş mesajı gönder
            Message loginMessage = new Message(username, "", MessageType.LOGIN);
            output.writeObject(loginMessage);
            
            // Dinleyici başlat
            clientListener = new ClientListener(this);
            new Thread(clientListener).start();
            
            connected = true;
            statusLabel.setText("Bağlantı durumu: Bağlı");
            connectButton.setText("Bağlantıyı Kes");
            
            serverIPField.setEnabled(false);
            usernameField.setEnabled(false);
            
            chatField.setEnabled(true);
            sendButton.setEnabled(true);
            
            addLogMessage("Sunucuya bağlandı: " + serverIP);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Sunucuya bağlanılamadı: " + e.getMessage(), "Bağlantı Hatası", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Sunucu bağlantısını keser.
     */
    public void disconnectFromServer() {
        if (connected) {
            try {
                // Çıkış mesajı gönder
                Message logoutMessage = new Message(username, "", MessageType.LOGOUT);
                output.writeObject(logoutMessage);
                
                // Dinleyiciyi durdur
                if (clientListener != null) {
                    clientListener.stop();
                }
                
                // Bağlantıyı kapat
                if (output != null) output.close();
                if (input != null) input.close();
                if (socket != null) socket.close();
                
                connected = false;
                statusLabel.setText("Bağlantı durumu: Bağlı değil");
                connectButton.setText("Bağlan");
                
                serverIPField.setEnabled(true);
                usernameField.setEnabled(true);
                
                chatField.setEnabled(false);
                sendButton.setEnabled(false);
                
                setGameControlsEnabled(false);
                
                addLogMessage("Sunucu bağlantısı kesildi.");
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    /**
     * Sohbet mesajı gönderir.
     */
    private void sendChatMessage() {
        if (connected) {
            String chatMessage = chatField.getText().trim();
            if (!chatMessage.isEmpty()) {
                try {
                    Message message = new Message(username, chatMessage, MessageType.CHAT);
                    output.writeObject(message);
                    chatField.setText("");
                } catch (IOException e) {
                    addLogMessage("Mesaj gönderilemedi: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Oyun kontrollerini etkinleştirir veya devre dışı bırakır.
     */
    public void setGameControlsEnabled(boolean enabled) {
        placeArmyButton.setEnabled(enabled);
        attackButton.setEnabled(enabled);
        fortifyButton.setEnabled(enabled);
        endTurnButton.setEnabled(enabled);
        armyCountComboBox.setEnabled(enabled);
    }
    
    /**
     * Geçerli aksiyonu ayarlar.
     */
    private void setCurrentAction(ActionType action) {
        currentAction = action;
        selectedTerritory = null;
        
        // Butonların görsel durumunu güncelle
        placeArmyButton.setBackground(null);
        attackButton.setBackground(null);
        fortifyButton.setBackground(null);
        
        switch (action) {
            case PLACE_ARMY:
                placeArmyButton.setBackground(Color.LIGHT_GRAY);
                statusLabel.setText("Birlik yerleştirmek için bir bölge seçin");
                break;
            case ATTACK:
                attackButton.setBackground(Color.LIGHT_GRAY);
                statusLabel.setText("Saldırmak için önce kendi bölgenizi seçin");
                break;
            case FORTIFY:
                fortifyButton.setBackground(Color.LIGHT_GRAY);
                statusLabel.setText("Takviye için önce kaynak bölgeyi seçin");
                break;
            default:
                break;
        }
        
        mapPanel.repaint();
    }
    
    /**
     * Bir bölgeye tıklandığında çağrılır.
     */
    public void territoryClicked(String territoryName) {
        if (!connected || gameState == null || !gameState.isGameStarted()) {
            return;
        }
        
        // Oyuncunun sırası mı kontrol et
        String currentPlayer = gameState.getCurrentPlayer();
        if (currentPlayer == null || !currentPlayer.equals(username)) {
            addLogMessage("Şu anda sizin sıranız değil. Sıra " + currentPlayer + " oyuncusunda.");
            return;
        }
        
        Territory territory = gameState.getTerritories().get(territoryName);
        
        if (currentAction == null) {
            selectedTerritory = territoryName;
            statusLabel.setText("Seçilen bölge: " + territoryName + " (" + territory.getOwner() + ", " + territory.getArmies() + " birlik)");
        } else {
            switch (currentAction) {
                case PLACE_ARMY:
                    handlePlaceArmy(territoryName);
                    break;
                case ATTACK:
                    handleAttack(territoryName);
                    break;
                case FORTIFY:
                    handleFortify(territoryName);
                    break;
                default:
                    break;
            }
        }
        
        mapPanel.repaint();
    }
    
    /**
     * Birlik yerleştirme işlemini gerçekleştirir.
     */
    private void handlePlaceArmy(String territoryName) {
        Territory territory = gameState.getTerritories().get(territoryName);
        
        if (!territory.getOwner().equals(username)) {
            addLogMessage("Sadece kendi bölgelerinize birlik yerleştirebilirsiniz.");
            return;
        }
        
        int armies = (Integer) armyCountComboBox.getSelectedItem();
        Player player = gameState.getPlayers().get(username);
        
        if (player.getReinforcementArmies() < armies) {
            addLogMessage("Yeterli takviye birliğiniz yok. Kalan: " + player.getReinforcementArmies());
            return;
        }
        
        try {
            GameAction action = new GameAction(ActionType.PLACE_ARMY, territoryName, null, armies);
            Message actionMessage = new Message(username, "", MessageType.GAME_ACTION);
            actionMessage.setGameAction(action);
            output.writeObject(actionMessage);
            
            currentAction = null;
            selectedTerritory = null;
        } catch (IOException e) {
            addLogMessage("Hareket gönderilemedi: " + e.getMessage());
        }
    }
    
    /**
     * Saldırı işlemini gerçekleştirir.
     */
    private void handleAttack(String territoryName) {
        if (selectedTerritory == null) {
            // İlk tıklama - saldıran bölgeyi seç
            Territory territory = gameState.getTerritories().get(territoryName);
            
            if (!territory.getOwner().equals(username)) {
                addLogMessage("Saldırı için önce kendi bölgelerinizden birini seçmelisiniz.");
                return;
            }
            
            if (territory.getArmies() < 2) {
                addLogMessage("Saldırmak için en az 2 birliğe ihtiyacınız var.");
                return;
            }
            
            selectedTerritory = territoryName;
            statusLabel.setText("Saldıran bölge: " + territoryName + ". Şimdi hedef bölgeyi seçin.");
        } else {
            // İkinci tıklama - hedef bölgeyi seç
            Territory sourceTerritory = gameState.getTerritories().get(selectedTerritory);
            Territory targetTerritory = gameState.getTerritories().get(territoryName);
            
            if (targetTerritory.getOwner().equals(username)) {
                addLogMessage("Kendi bölgenize saldıramazsınız.");
                selectedTerritory = null;
                return;
            }
            
            if (!sourceTerritory.isNeighbor(territoryName)) {
                addLogMessage("Sadece komşu bölgelere saldırabilirsiniz.");
                selectedTerritory = null;
                return;
            }
            
            int armies = Math.min((Integer) armyCountComboBox.getSelectedItem(), sourceTerritory.getArmies() - 1);
            
            try {
                GameAction action = new GameAction(ActionType.ATTACK, selectedTerritory, territoryName, armies);
                Message actionMessage = new Message(username, "", MessageType.GAME_ACTION);
                actionMessage.setGameAction(action);
                output.writeObject(actionMessage);
                
                currentAction = null;
                selectedTerritory = null;
            } catch (IOException e) {
                addLogMessage("Hareket gönderilemedi: " + e.getMessage());
            }
        }
    }
    
    /**
     * Takviye işlemini gerçekleştirir.
     */
    private void handleFortify(String territoryName) {
        if (selectedTerritory == null) {
            // İlk tıklama - kaynak bölgeyi seç
            Territory territory = gameState.getTerritories().get(territoryName);
            
            if (!territory.getOwner().equals(username)) {
                addLogMessage("Takviye için önce kendi bölgelerinizden birini seçmelisiniz.");
                return;
            }
            
            if (territory.getArmies() < 2) {
                addLogMessage("Takviye yapmak için bölgede en az 2 birlik olmalıdır.");
                return;
            }
            
            selectedTerritory = territoryName;
            statusLabel.setText("Kaynak bölge: " + territoryName + ". Şimdi hedef bölgeyi seçin.");
        } else {
            // İkinci tıklama - hedef bölgeyi seç
            Territory sourceTerritory = gameState.getTerritories().get(selectedTerritory);
            Territory targetTerritory = gameState.getTerritories().get(territoryName);
            
            if (!targetTerritory.getOwner().equals(username)) {
                addLogMessage("Sadece kendi bölgelerinize takviye yapabilirsiniz.");
                selectedTerritory = null;
                return;
            }
            
            if (selectedTerritory.equals(territoryName)) {
                addLogMessage("Farklı bir bölge seçmelisiniz.");
                selectedTerritory = null;
                return;
            }
            
            int armies = Math.min((Integer) armyCountComboBox.getSelectedItem(), sourceTerritory.getArmies() - 1);
            
            try {
                GameAction action = new GameAction(ActionType.FORTIFY, selectedTerritory, territoryName, armies);
                Message actionMessage = new Message(username, "", MessageType.GAME_ACTION);
                actionMessage.setGameAction(action);
                output.writeObject(actionMessage);
                
                currentAction = null;
                selectedTerritory = null;
            } catch (IOException e) {
                addLogMessage("Hareket gönderilemedi: " + e.getMessage());
            }
        }
    }
    
    /**
     * Turu bitirir.
     */
    private void endTurn() {
        if (connected && gameState != null && gameState.isGameStarted()) {
            try {
                GameAction action = new GameAction(ActionType.END_TURN, null, null, 0);
                Message actionMessage = new Message(username, "", MessageType.GAME_ACTION);
                actionMessage.setGameAction(action);
                output.writeObject(actionMessage);
                
                currentAction = null;
                selectedTerritory = null;
                statusLabel.setText("Tur sona erdi. Sıradaki oyuncu bekleniyor...");
            } catch (IOException e) {
                addLogMessage("Hareket gönderilemedi: " + e.getMessage());
            }
        }
    }
    
    /**
     * Oyunu başlatır.
     */
    public void startGame() {
        if (connected) {
            try {
                Message startGameMessage = new Message(username, "", MessageType.START_GAME);
                output.writeObject(startGameMessage);
            } catch (IOException e) {
                addLogMessage("Oyun başlatma isteği gönderilemedi: " + e.getMessage());
            }
        }
    }
    
    /**
     * Log mesajı ekler.
     */
    public void addLogMessage(String message) {
        chatArea.append("[Sistem] " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    /**
     * Sohbet mesajı ekler.
     */
    public void addChatMessage(String sender, String message) {
        chatArea.append("[" + sender + "] " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    /**
     * Oyun durumunu günceller.
     */
    public void updateGameState(GameState newState) {
        gameState = newState;
        mapPanel.setGameState(gameState);
        mapPanel.repaint();
        
        // Kontrol panelini güncelle
        if (gameState.isGameStarted()) {
            setGameControlsEnabled(gameState.getCurrentPlayer().equals(username));
            
            // Oyuncu bilgilerini güncelle
            updatePlayerInfo();
        }
    }
    
    /**
     * Oyuncu bilgilerini günceller.
     */
    private void updatePlayerInfo() {
        if (gameState == null) return;
        
        Component[] components = controlPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel && ((JPanel) component).getBorder() instanceof TitledBorder) {
                TitledBorder border = (TitledBorder) ((JPanel) component).getBorder();
                if (border.getTitle().equals("Oyun Bilgileri")) {
                    JPanel infoPanel = (JPanel) component;
                    infoPanel.removeAll();
                    
                    // Oyuncu bilgilerini ekle
                    for (String playerName : gameState.getPlayerList()) {
                        Player player = gameState.getPlayers().get(playerName);
                        JLabel playerLabel = new JLabel(playerName + ": " + player.getTerritories().size() + " bölge, " +
                                                       (playerName.equals(username) ? player.getReinforcementArmies() + " takviye birlik" : ""));
                        
                        // Geçerli oyuncuyu vurgula
                        if (playerName.equals(gameState.getCurrentPlayer())) {
                            playerLabel.setFont(playerLabel.getFont().deriveFont(Font.BOLD));
                            playerLabel.setForeground(Color.BLUE);
                        }
                        
                        infoPanel.add(playerLabel);
                    }
                    
                    infoPanel.revalidate();
                    infoPanel.repaint();
                    break;
                }
            }
        }
    }
    
    // Getter metodları (ClientListener tarafından kullanılır)
    public String getUsername() {
        return username;
    }
    
    public String getSelectedTerritory() {
        return selectedTerritory;
    }
    
    public ObjectInputStream getInput() {
        return input;
    }
}