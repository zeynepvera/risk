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
 * Risk oyunu için başlangıç ve bitiş ekranlarını içeren genişletilmiş istemci uygulaması.
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
    private CardLayout cardLayout; // Farklı ekranlar arasında geçiş için
    private JPanel contentPanel; // Ana içerik paneli (tüm ekranları içerir)
    private JPanel mainPanel; // Oyun ana paneli
    private JPanel menuPanel; // Başlangıç menü paneli
    private JPanel gameOverPanel; // Oyun sonu paneli
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
    private JTextField portField;
    
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
        
        // CardLayout ile farklı ekranlar arasında geçiş yapılabilir
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        setContentPane(contentPanel);
        
        // Ekranları oluştur ve ekle
        initializeMenuScreen();
        initializeGameScreen();
        initializeGameOverScreen();
        
        // Başlangıçta menü ekranını göster
        cardLayout.show(contentPanel, "menu");
        
        // Pencere kapanırken bağlantıyı kapat
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnectFromServer();
            }
        });
    }
    
    /**
     * Başlangıç menü ekranını oluşturur.
     */
    private void initializeMenuScreen() {
        menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBackground(new Color(240, 240, 255));
        
        // Logo/başlık paneli
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(50, 50, 100));
        titlePanel.setPreferredSize(new Dimension(1200, 200));
        
        JLabel titleLabel = new JLabel("RISK OYUNU");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 72));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        menuPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Menü seçenekleri
        JPanel optionsPanel = new JPanel();
        optionsPanel.setBackground(new Color(240, 240, 255));
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
        
        // Bağlantı bilgileri paneli
        JPanel connectionInfoPanel = new JPanel(new GridBagLayout());
        connectionInfoPanel.setBackground(new Color(240, 240, 255));
        connectionInfoPanel.setBorder(BorderFactory.createTitledBorder("Sunucu Bağlantı Bilgileri"));
        connectionInfoPanel.setMaximumSize(new Dimension(500, 250));
        connectionInfoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        connectionInfoPanel.add(new JLabel("Sunucu IP:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        serverIPField = new JTextField("", 15);
        serverIPField.setToolTipText("Sunucu IP adresi (örn: 54.123.456.789)");
        connectionInfoPanel.add(serverIPField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        connectionInfoPanel.add(new JLabel("Port:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        portField = new JTextField(String.valueOf(PORT), 15);
        portField.setToolTipText("Sunucu port numarası (varsayılan: 9876)");
        connectionInfoPanel.add(portField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        connectionInfoPanel.add(new JLabel("Kullanıcı Adı:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        usernameField = new JTextField(15);
        connectionInfoPanel.add(usernameField, gbc);
        
        optionsPanel.add(connectionInfoPanel);
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Menü butonları
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 255));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton playButton = createMenuButton("Oyuna Başla", 250, 50);
        playButton.addActionListener(e -> {
            if (usernameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen bir kullanıcı adı girin.", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (serverIPField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen bir sunucu IP adresi girin.", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Oyun ekranına geç ve sunucuya bağlan
            cardLayout.show(contentPanel, "game");
            connectToServer();
        });
        
        JButton awsConnectButton = createMenuButton("AWS'ye Bağlan", 250, 50);
        awsConnectButton.setBackground(new Color(255, 153, 0)); // AWS turuncu rengi
        awsConnectButton.setForeground(Color.WHITE);
        awsConnectButton.addActionListener(e -> {
            String awsIp = JOptionPane.showInputDialog(this, 
                "AWS instance IP adresini girin:", 
                "AWS Bağlantısı", 
                JOptionPane.QUESTION_MESSAGE);
            
            if (awsIp != null && !awsIp.trim().isEmpty()) {
                serverIPField.setText(awsIp.trim());
            }
        });
        
        JButton exitButton = createMenuButton("Çıkış", 250, 50);
        exitButton.addActionListener(e -> System.exit(0));
        
        buttonPanel.add(playButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(awsConnectButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(exitButton);
        
        optionsPanel.add(buttonPanel);
        menuPanel.add(optionsPanel, BorderLayout.CENTER);
        
        // Alt bilgi paneli
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(50, 50, 100));
        footerPanel.setPreferredSize(new Dimension(1200, 50));
        
        JLabel footerLabel = new JLabel("© 2025 Risk Oyunu - Tüm hakları saklıdır.");
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);
        menuPanel.add(footerPanel, BorderLayout.SOUTH);
        
        // İçerik paneline ekle
        contentPanel.add(menuPanel, "menu");
    }
    
    /**
     * Menü butonu oluşturur.
     */
    private JButton createMenuButton(String text, int width, int height) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(width, height));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        return button;
    }
    
    /**
     * Oyun sonu ekranını oluşturur.
     */
    private void initializeGameOverScreen() {
        gameOverPanel = new JPanel(new BorderLayout());
        gameOverPanel.setBackground(new Color(240, 240, 255));
        
        // Başlık paneli
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(50, 50, 100));
        titlePanel.setPreferredSize(new Dimension(1200, 150));
        
        JLabel titleLabel = new JLabel("OYUN SONA ERDİ");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        gameOverPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Sonuç paneli
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(new Color(240, 240, 255));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        
        JLabel winnerLabel = new JLabel("Kazanan: ");
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultPanel.add(winnerLabel);
        
        resultPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        
        // Butonlar
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 255));
        
        JButton newGameButton = createMenuButton("Yeni Oyun", 200, 50);
        newGameButton.addActionListener(e -> {
            disconnectFromServer();
            cardLayout.show(contentPanel, "menu");
        });
        
        JButton mainMenuButton = createMenuButton("Ana Menü", 200, 50);
        mainMenuButton.addActionListener(e -> {
            disconnectFromServer();
            cardLayout.show(contentPanel, "menu");
        });
        
        JButton exitButton = createMenuButton("Çıkış", 200, 50);
        exitButton.addActionListener(e -> System.exit(0));
        
        buttonPanel.add(newGameButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(mainMenuButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(exitButton);
        
        resultPanel.add(buttonPanel);
        gameOverPanel.add(resultPanel, BorderLayout.CENTER);
        
        // Alt bilgi paneli
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(50, 50, 100));
        footerPanel.setPreferredSize(new Dimension(1200, 50));
        
        JLabel footerLabel = new JLabel("© 2025 Risk Oyunu - Tüm hakları saklıdır.");
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);
        gameOverPanel.add(footerPanel, BorderLayout.SOUTH);
        
        // İçerik paneline ekle
        contentPanel.add(gameOverPanel, "gameOver");
    }
    
    /**
     * Oyun ekranını oluşturur.
     */
    private void initializeGameScreen() {
        mainPanel = new JPanel(new BorderLayout());
        
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
        
        // Ana menüye dönüş butonu
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton menuButton = new JButton("Ana Menüye Dön");
        menuButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, 
                "Ana menüye dönmek istediğinizden emin misiniz? Aktif oyun sonlandırılacaktır.", 
                "Ana Menüye Dön", 
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                disconnectFromServer();
                cardLayout.show(contentPanel, "menu");
            }
        });
        topPanel.add(menuButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Başlangıçta oyun kontrollerini devre dışı bırak
        setGameControlsEnabled(false);
        
        // İçerik paneline ekle
        contentPanel.add(mainPanel, "game");
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
        gbc.gridy = 4;
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
        chatField.setEnabled(false); // Başlangıçta devre dışı
        inputPanel.add(chatField, BorderLayout.CENTER);
        
        sendButton = new JButton("Gönder");
        sendButton.addActionListener(e -> sendChatMessage());
        sendButton.setEnabled(false); // Başlangıçta devre dışı
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
        
        if (serverIP.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen bir sunucu IP adresi girin.", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen bir kullanıcı adı girin.", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Port numarasını alma
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
            if (port < 1 || port > 65535) {
                throw new NumberFormatException("Geçersiz port aralığı");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Geçerli bir port numarası girin (1-65535).", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            statusLabel.setText("Bağlanıyor: " + serverIP + ":" + port + "...");
            
            // Bağlantı timeout süresi ekleyelim (5 saniye)
            socket = new Socket(serverIP, port);
            socket.setSoTimeout(10000); // 10 saniye timeout
            
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            
            // Giriş mesajı gönder
            Message loginMessage = new Message(username, "", MessageType.LOGIN);
            output.writeObject(loginMessage);
            output.flush(); // Çıktı tamponunu boşaltmayı unutmayalım
            
            // Dinleyici başlat
            clientListener = new ClientListener(this);
            new Thread(clientListener).start();
            
            connected = true;
            statusLabel.setText("Bağlantı durumu: Bağlı - " + serverIP + ":" + port);
            
            chatField.setEnabled(true);
            sendButton.setEnabled(true);
            
            addLogMessage("Sunucuya bağlandı: " + serverIP + ":" + port);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Sunucuya bağlanılamadı: " + e.getMessage() + "\n" +
                "AWS sunucusunun çalıştığından ve güvenlik grubunun " + port + " portuna izin verdiğinden emin olun.", 
                "Bağlantı Hatası", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Bağlantı durumu: Bağlantı hatası");
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
                output.flush();
                
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
                
                chatField.setEnabled(false);
                sendButton.setEnabled(false);
                
                setGameControlsEnabled(false);
                
                addLogMessage("Sunucu bağlantısı kesildi.");
            } catch (IOException e) {
                addLogMessage("Bağlantı kapatılırken hata: " + e.getMessage());
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
                    output.flush();
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
            output.flush();
            
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
                output.flush();
                
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
                output.flush();
                
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
                output.flush();
                
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
                output.flush();
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
     * Bağlantının sağlıklı olup olmadığını kontrol eder.
     */
    public void checkConnection() {
        if (connected && socket != null) {
            if (socket.isClosed() || !socket.isConnected()) {
                addLogMessage("Sunucu bağlantısı kesildi.");
                disconnectFromServer();
            }
        }
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