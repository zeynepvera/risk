package client;

import common.GameState;
import common.MessageType;
import common.Territory;
import common.ActionType;
import common.Player;
import common.Message;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.geom.RoundRectangle2D;

import common.MapPanel;
import client.gui.UIComponentFactory;
import client.network.NetworkManager;
import client.logic.GameLogicManager;

public class RiskClient extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int PORT = 9034;
    
    // Manager instances
    private NetworkManager networkManager;
    private GameLogicManager gameLogicManager;

    // GUI bileşenleri
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JPanel mainPanel;
    private JPanel menuPanel;
    private JPanel gameOverPanel;
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
    private JLabel winnerLabel;
    private String lastSystemMessage = null;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RiskClient client = new RiskClient();
            client.setVisible(true);
        });
    }

    public RiskClient() {
        super("Risk Oyunu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // CardLayout ile farklı ekranlar arasında geçiş yapılabilir
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        setContentPane(contentPanel);

        // Manager'ları başlat
        networkManager = new NetworkManager(this);
        gameLogicManager = new GameLogicManager(this);

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

    private void initializeMenuScreen() {
        menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBackground(new Color(240, 240, 255));

        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(40, 40, 90),
                        w, h, new Color(70, 70, 140));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        titlePanel.setPreferredSize(new Dimension(1200, 200));
        titlePanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("RISK OYUNU");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 72));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        menuPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(240, 240, 255));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));

        JPanel loginPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int width = getWidth();
                int height = getHeight();

                GradientPaint gp = new GradientPaint(0, 0, new Color(250, 250, 255),
                        0, height, new Color(235, 235, 255));
                g2d.setPaint(gp);

                RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(
                        0, 0, width - 1, height - 1, 20, 20);
                g2d.fill(roundedRectangle);

                g2d.setColor(new Color(200, 200, 230));
                g2d.draw(roundedRectangle);
            }
        };
        loginPanel.setLayout(new BorderLayout());
        loginPanel.setMaximumSize(new Dimension(600, 350));
        loginPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel loginHeader = new JLabel("Oyuna Katıl", JLabel.CENTER);
        loginHeader.setFont(new Font("Arial", Font.BOLD, 24));
        loginHeader.setForeground(new Color(60, 60, 120));
        loginHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        loginPanel.add(loginHeader, BorderLayout.NORTH);

        // Form paneli
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Kullanıcı adı
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel usernameLabel = new JLabel("Kullanıcı Adı:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        usernameField = UIComponentFactory.createStyledTextField("", "Oyunda görünecek adınızı girin");
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(usernameField, gbc);

        // Gelişmiş ayarlar Toggle
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        JCheckBox advancedOptionsCheckbox = new JCheckBox("Gelişmiş Sunucu Ayarları");
        advancedOptionsCheckbox.setFont(new Font("Arial", Font.PLAIN, 14));
        advancedOptionsCheckbox.setOpaque(false);
        formPanel.add(advancedOptionsCheckbox, gbc);

        // Gelişmiş ayarlar paneli (başlangıçta gizli)
        JPanel advancedPanel = new JPanel(new GridBagLayout());
        advancedPanel.setOpaque(false);
        advancedPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        advancedPanel.setVisible(false);
        advancedOptionsCheckbox.setVisible(false);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel serverIPLabel = new JLabel("Sunucu Adresi:");
        serverIPLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        advancedPanel.add(serverIPLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        serverIPField = UIComponentFactory.createStyledTextField("localhost", "Sunucu IP adresi");
        serverIPField.setFont(new Font("Arial", Font.PLAIN, 14));
        advancedPanel.add(serverIPField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        advancedPanel.add(portLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        portField = UIComponentFactory.createStyledTextField(String.valueOf(PORT), "Sunucu port numarası");
        portField.setFont(new Font("Arial", Font.PLAIN, 14));
        advancedPanel.add(portField, gbc);

        // Gelişmiş seçenekler toggle aksiyonu
        advancedOptionsCheckbox.addActionListener(e -> {
            advancedPanel.setVisible(advancedOptionsCheckbox.isSelected());
            loginPanel.revalidate();
            loginPanel.repaint();
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        formPanel.add(advancedPanel, gbc);

        loginPanel.add(formPanel, BorderLayout.CENTER);

        // Buton paneli
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton playButton = UIComponentFactory.createStylishButton("OYUNA BAŞLA", 200, 50, new Color(60, 130, 200), new Color(100, 160, 220));
        playButton.setFont(new Font("Arial", Font.BOLD, 16));
        playButton.addActionListener(e -> {
            if (usernameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen bir kullanıcı adı girin.", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (advancedOptionsCheckbox.isSelected() && serverIPField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen bir sunucu adresi girin.", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Oyun ekranına geç ve sunucuya bağlan
            cardLayout.show(contentPanel, "game");
            connectToServer();
        });

        JButton exitButton = UIComponentFactory.createStylishButton("ÇIKIŞ", 150, 50, new Color(190, 60, 60), new Color(220, 80, 80));
        exitButton.setFont(new Font("Arial", Font.BOLD, 16));
        exitButton.addActionListener(e -> System.exit(0));

        JButton howToPlayButton = UIComponentFactory.createStylishButton("NASIL OYNANIR?", 200, 50, new Color(50, 80, 130), new Color(70, 100, 160));
        howToPlayButton.setFont(new Font("Arial", Font.BOLD, 16));
        howToPlayButton.addActionListener(e -> showHowToPlayDialog());

        buttonPanel.add(howToPlayButton);
        buttonPanel.add(playButton);
        buttonPanel.add(exitButton);

        loginPanel.add(buttonPanel, BorderLayout.SOUTH);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(loginPanel);
        centerPanel.add(Box.createVerticalGlue());

        menuPanel.add(centerPanel, BorderLayout.CENTER);

        // Alt bilgi paneli
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(50, 50, 100));
        footerPanel.setPreferredSize(new Dimension(1200, 50));

        JLabel footerLabel = new JLabel("© 2025 Risk Oyunu - Tüm hakları saklıdır.");
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);

        menuPanel.add(footerPanel, BorderLayout.SOUTH);

        contentPanel.add(menuPanel, "menu");
    }

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

        winnerLabel = new JLabel("Kazanan: ");
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultPanel.add(winnerLabel);

        resultPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        // Butonlar
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 255));

        JButton newGameButton = UIComponentFactory.createMenuButton("Yeni Oyun", 200, 50);
        newGameButton.addActionListener(e -> {
            disconnectFromServer();
            resetClientState();
            cardLayout.show(contentPanel, "menu");
        });

        JButton mainMenuButton = UIComponentFactory.createMenuButton("Ana Menü", 200, 50);
        mainMenuButton.addActionListener(e -> {
            disconnectFromServer();
            resetClientState();
            cardLayout.show(contentPanel, "menu");
        });

        JButton exitButton = UIComponentFactory.createMenuButton("Çıkış", 200, 50);
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

        contentPanel.add(gameOverPanel, "gameOver");
    }

    private void resetClientState() {
        if (statusLabel != null) statusLabel.setText("Bağlantı durumu: Bağlı değil");
        setGameControlsEnabled(false);
        if (chatField != null) chatField.setText("");
        setChatEnabled(false);
        if (chatArea != null) chatArea.setText("");
    }
    
private void initializeGameScreen() {
        mainPanel = new JPanel(new BorderLayout());

        // Oyun haritası paneli
        mapPanel = new MapPanel(this);
        mainPanel.add(mapPanel, BorderLayout.CENTER);

        // Sağ panel (kontrol + sohbet)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, 0));
        mainPanel.add(rightPanel, BorderLayout.EAST);

        // Kontrol paneli
        controlPanel = createControlPanel();
        rightPanel.add(controlPanel, BorderLayout.NORTH);

        // Durum bilgisi paneli
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Durum Bilgisi"));
        statusLabel = new JLabel("Bağlantı durumu: Bağlı değil", JLabel.LEFT);
        statusPanel.add(statusLabel);
        rightPanel.add(statusPanel, BorderLayout.CENTER);

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

        contentPanel.add(mainPanel, "game");
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 244, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Oyun Kontrolleri", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(40, 40, 90));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel armyCountPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        armyCountPanel.setOpaque(false);
        JLabel armyLabel = new JLabel("Birlik Sayısı:");
        armyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        armyCountPanel.add(armyLabel);

        armyCountComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 5, 10});
        armyCountComboBox.setPreferredSize(new Dimension(80, 25));
        armyCountComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        armyCountPanel.add(armyCountComboBox);
        content.add(armyCountPanel);
        content.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        buttonPanel.setOpaque(false);

        Color gradientBase = new Color(85, 110, 155);
        placeArmyButton = UIComponentFactory.createGradientButton("Birlik Yerleştir", gradientBase);
        attackButton = UIComponentFactory.createGradientButton("Saldır", gradientBase);
        fortifyButton = UIComponentFactory.createGradientButton("Takviye", gradientBase);

        // Teslim Ol butonu
        JButton surrenderButton = UIComponentFactory.createGradientButton("Teslim Ol", new Color(200, 80, 60));
        surrenderButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Gerçekten teslim olmak istiyor musunuz? Oyun sizin için sona erecek.",
                    "Teslim Ol",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                Message surrenderMsg = new Message(networkManager.getUsername(), "", MessageType.SURRENDER);
                networkManager.sendMessage(surrenderMsg);
            }
        });

        // Oyundan Çık butonu
        JButton quitButton = UIComponentFactory.createGradientButton("Oyundan Çık", new Color(180, 60, 60));
        quitButton.addActionListener(e -> {
    int confirm = JOptionPane.showConfirmDialog(
            this,
            "Ana menüye dönmek istediğinizden emin misiniz? Tüm oyun verisi sıfırlanacaktır.",
            "Ana Menüye Dön",
            JOptionPane.YES_NO_OPTION
    );

    if (confirm == JOptionPane.YES_OPTION) {
        completeReset(); // YENİ METOD
        cardLayout.show(contentPanel, "menu");
    }
});

// Game Over ekranındaki butonları da güncelle:
JButton newGameButton = UIComponentFactory.createMenuButton("Yeni Oyun", 200, 50);
newGameButton.addActionListener(e -> {
    completeReset(); // YENİ METOD
    cardLayout.show(contentPanel, "menu");
});

JButton mainMenuButton = UIComponentFactory.createMenuButton("Ana Menü", 200, 50);
mainMenuButton.addActionListener(e -> {
    completeReset(); // YENİ METOD  
    cardLayout.show(contentPanel, "menu");
});

        buttonPanel.add(placeArmyButton);
        buttonPanel.add(attackButton);
        buttonPanel.add(fortifyButton);
        buttonPanel.add(surrenderButton);
        buttonPanel.add(quitButton);

        placeArmyButton.addActionListener(e -> setCurrentAction(ActionType.PLACE_ARMY));
        attackButton.addActionListener(e -> setCurrentAction(ActionType.ATTACK));
        fortifyButton.addActionListener(e -> setCurrentAction(ActionType.FORTIFY));

        content.add(buttonPanel);
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 244, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Oyun Sohbeti", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setOpaque(true);
        title.setBackground(new Color(70, 130, 180));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(title, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chatArea.setBackground(new Color(250, 250, 255));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setPreferredSize(new Dimension(0, 180));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 230)));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        inputPanel.setBackground(new Color(240, 244, 255));

        chatField = new JTextField();
        chatField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chatField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 230)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        inputPanel.add(chatField, BorderLayout.CENTER);

        sendButton = new JButton("Gönder");
        sendButton.setBackground(new Color(95, 60, 70));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        inputPanel.add(sendButton, BorderLayout.EAST);

        panel.add(inputPanel, BorderLayout.SOUTH);
        sendButton.addActionListener(e -> sendChatMessage());
        chatField.addActionListener(e -> sendChatMessage());

        return panel;
    }

    // Network operations - delegated to NetworkManager
    public void connectToServer() {
        String username = usernameField.getText().trim();
        networkManager.connectToServer(username);
    }

    public void disconnectFromServer() {
        networkManager.disconnectFromServer();
        resetClientState();
    }

    // Game logic operations - delegated to GameLogicManager
    public void territoryClicked(String territoryName) {
        gameLogicManager.territoryClicked(territoryName);
    }

    public void setCurrentAction(ActionType action) {
        gameLogicManager.setCurrentAction(action);
    }

    public void updateGameState(GameState newState) {
        gameLogicManager.updateGameState(newState);
    }

    // Helper methods for managers
    public void updateButtonStates(ActionType action) {
        placeArmyButton.setBackground(null);
        attackButton.setBackground(null);
        fortifyButton.setBackground(null);

        switch (action) {
            case PLACE_ARMY:
                placeArmyButton.setBackground(Color.LIGHT_GRAY);
                break;
            case ATTACK:
                attackButton.setBackground(Color.LIGHT_GRAY);
                break;
            case FORTIFY:
                fortifyButton.setBackground(Color.LIGHT_GRAY);
                break;
        }
    }

    public int getSelectedArmyCount() {
        return (Integer) armyCountComboBox.getSelectedItem();
    }

    public void repaintMap() {
        if (mapPanel != null) {
            mapPanel.repaint();
        }
    }

    public void setGameStateToMap(GameState gameState) {
        if (mapPanel != null) {
            mapPanel.setGameState(gameState);
            mapPanel.repaint();
        }
    }

    public void setChatEnabled(boolean enabled) {
        if (chatField != null) chatField.setEnabled(enabled);
        if (sendButton != null) sendButton.setEnabled(enabled);
    }

    public void showMenuScreen() {
        cardLayout.show(contentPanel, "menu");
    }

    public void updateStatusLabel(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    public void setGameControlsEnabled(boolean enabled) {
        if (placeArmyButton != null) placeArmyButton.setEnabled(enabled);
        if (attackButton != null) attackButton.setEnabled(enabled);
        if (fortifyButton != null) fortifyButton.setEnabled(enabled);
        if (armyCountComboBox != null) armyCountComboBox.setEnabled(enabled);
    }

    public void updateArmyCountComboBox(ActionType action) {
        if (armyCountComboBox == null) return;
        
        armyCountComboBox.removeAllItems();

        GameState gameState = gameLogicManager.getGameState();
        if (gameState == null) {
            armyCountComboBox.addItem(1);
            return;
        }

        Player player = gameState.getPlayers().get(networkManager.getUsername());
        if (player == null) {
            armyCountComboBox.addItem(1);
            return;
        }

        switch (action) {
            case PLACE_ARMY:
                int maxArmies = player.getReinforcementArmies();
                if (maxArmies <= 0) {
                    armyCountComboBox.addItem(1);
                } else {
                    armyCountComboBox.addItem(1);
                    if (maxArmies >= 2) armyCountComboBox.addItem(2);
                    if (maxArmies >= 3) armyCountComboBox.addItem(3);
                    if (maxArmies >= 5) armyCountComboBox.addItem(5);
                    if (maxArmies >= 10) armyCountComboBox.addItem(10);
                    if (maxArmies > 10 && !containsItem(armyCountComboBox, maxArmies)) {
                        armyCountComboBox.addItem(maxArmies);
                    }
                }
                break;

            case ATTACK:
                if (gameLogicManager.getSelectedTerritory() == null) {
                    armyCountComboBox.addItem(1);
                    if (player.getTerritories().stream().anyMatch(t -> {
                        Territory ter = gameState.getTerritories().get(t);
                        return ter != null && ter.getArmies() >= 3;
                    })) {
                        armyCountComboBox.addItem(2);
                        armyCountComboBox.addItem(3);
                    }
                    break;
                }

                Territory attackTerritory = gameState.getTerritories().get(gameLogicManager.getSelectedTerritory());
                if (attackTerritory != null) {
                    int maxAttack = Math.min(3, attackTerritory.getArmies() - 1);
                    for (int i = 1; i <= maxAttack; i++) {
                        armyCountComboBox.addItem(i);
                    }
                } else {
                    armyCountComboBox.addItem(1);
                }
                break;

            case FORTIFY:
                if (gameLogicManager.getSelectedTerritory() == null) {
                    armyCountComboBox.addItem(1);
                    break;
                }

                Territory fortifyTerritory = gameState.getTerritories().get(gameLogicManager.getSelectedTerritory());
                if (fortifyTerritory != null) {
                    int maxFortify = fortifyTerritory.getArmies() - 1;
                    armyCountComboBox.addItem(1);
                    if (maxFortify >= 2) armyCountComboBox.addItem(2);
                    if (maxFortify >= 3) armyCountComboBox.addItem(3);
                    if (maxFortify >= 5) armyCountComboBox.addItem(5);
                    if (maxFortify >= 10) armyCountComboBox.addItem(10);
                    if (maxFortify > 10 && !containsItem(armyCountComboBox, maxFortify)) {
                        armyCountComboBox.addItem(maxFortify);
                    }
                } else {
                    armyCountComboBox.addItem(1);
                }
                break;

            default:
                armyCountComboBox.addItem(1);
                break;
        }

        if (armyCountComboBox.getItemCount() == 0) {
            armyCountComboBox.addItem(1);
        }
    }

    private boolean containsItem(JComboBox<Integer> comboBox, int value) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if ((Integer) comboBox.getItemAt(i) == value) {
                return true;
            }
        }
        return false;
    }

    private void showHowToPlayDialog() {
        JDialog dialog = new JDialog(this, "Nasıl Oynanır?", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.setBackground(new Color(250, 250, 255));

        JLabel title = new JLabel("🧠 RISK OYUNU KURALLARI");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(50, 60, 120));
        content.add(title, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea("""
        • Her oyuncu sırayla hamle yapar.
        • Turda 4 seçenek vardır:
            - Birlik Yerleştir
            - Saldır
            - Takviye Gönder
            - Turu Bitir
        
        • Saldırıda zarlar atılır ve en yüksek değerler karşılaştırılır.
        • Sadece komşu bölgelere saldırabilirsiniz.
        • Tüm bölgeleri ele geçiren oyuncu oyunu kazanır.
        
        Amaç: Haritadaki tüm bölgeleri ele geçir!

         Bol şans!
        """);
        textArea.setEditable(false);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setBackground(new Color(250, 250, 255));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        content.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JButton okButton = new JButton("Kapat");
        okButton.setBackground(new Color(70, 130, 180));
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(250, 250, 255));
        buttonPanel.add(okButton);
        content.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private void sendChatMessage() {
        if (networkManager.isConnected()) {
            String chatMessage = chatField.getText().trim();
            if (!chatMessage.isEmpty()) {
                Message message = new Message(networkManager.getUsername(), chatMessage, MessageType.CHAT);
                networkManager.sendMessage(message);
                chatField.setText("");
            }
        }
    }

    public void startGame() {
        if (networkManager.isConnected()) {
            Message startGameMessage = new Message(networkManager.getUsername(), "", MessageType.START_GAME);
            networkManager.sendMessage(startGameMessage);
        }
    }

    public void addLogMessage(String message) {
        String fullMessage = "[Sistem] " + message;

        if (fullMessage.equals(lastSystemMessage)) {
            return;
        }

        lastSystemMessage = fullMessage;

        if (chatArea != null) {
            chatArea.append(fullMessage + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    public void addChatMessage(String sender, String message) {
        if (chatArea != null) {
            chatArea.append("[" + sender + "] " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    public void checkConnection() {
        if (!networkManager.isConnected()) {
            addLogMessage("Sunucu bağlantısı kesildi.");
            disconnectFromServer();
        }
    }

    private void updatePlayerInfo() {
        GameState gameState = gameLogicManager.getGameState();
        if (gameState == null) {
            return;
        }

        Component[] components = controlPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel && ((JPanel) component).getBorder() instanceof TitledBorder) {
                TitledBorder border = (TitledBorder) ((JPanel) component).getBorder();
                if (border.getTitle().equals("Oyun Bilgileri")) {
                    JPanel infoPanel = (JPanel) component;
                    infoPanel.removeAll();

                    for (String playerName : gameState.getPlayerList()) {
                        Player player = gameState.getPlayers().get(playerName);
                        JLabel playerLabel = new JLabel(playerName + ": " + player.getTerritories().size() + " bölge, "
                                + (playerName.equals(networkManager.getUsername()) ? player.getReinforcementArmies() + " takviye birlik" : ""));

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
    /**
 * Oyunu tamamen sıfırlar - yeni kullanıcı gibi yapar
 */
private void completeReset() {
    System.out.println("=== TAM SIFIRLAMA BAŞLATIYOR ===");
    
    // 1. Ağ bağlantısını kes
    networkManager.disconnectFromServer();
    
    // 2. GUI bileşenlerini sıfırla
    resetAllGUIComponents();
    
    // 3. Oyun durumunu sıfırla
    gameLogicManager = new GameLogicManager(this); // Yeni instance
    
    // 4. Status güncelle
    updateStatusLabel("Bağlantı durumu: Bağlı değil");
    
    System.out.println("=== TAM SIFIRLAMA TAMAMLANDI ===");
}

/**
 * Tüm GUI bileşenlerini sıfırlar
 */
private void resetAllGUIComponents() {
    // Username field'ını temizle
    if (usernameField != null) {
        usernameField.setText("");
        System.out.println("Username field temizlendi");
    }
    
    // Chat area'yı temizle
    if (chatArea != null) {
        chatArea.setText("");
        System.out.println("Chat area temizlendi");
    }
    
    // Chat field'ını temizle ve devre dışı bırak
    if (chatField != null) {
        chatField.setText("");
        chatField.setEnabled(false);
    }
    
    // Send button'ı devre dışı bırak
    if (sendButton != null) {
        sendButton.setEnabled(false);
    }
    
    // Oyun kontrollerini devre dışı bırak
    setGameControlsEnabled(false);
    
    // Haritayı temizle
    if (mapPanel != null) {
        mapPanel.setGameState(null);
        mapPanel.repaint();
        System.out.println("Harita temizlendi");
    }
    
    // Sistem mesajlarını sıfırla
    lastSystemMessage = null;
    
    System.out.println("Tüm GUI bileşenleri sıfırlandı");
}

    public void handleGameEnd(String winner) {
        SwingUtilities.invokeLater(() -> {
            if (winner != null && !winner.isEmpty()) {
                winnerLabel.setText("Kazanan: " + winner);
            } else {
                winnerLabel.setText("Oyun sona erdi.");
            }

            cardLayout.show(contentPanel, "gameOver");
        });
    }

    // Getter methods for managers and ClientListener
    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public GameLogicManager getGameLogicManager() {
        return gameLogicManager;
    }

    public String getUsername() {
        return networkManager.getUsername();
    }

    public String getSelectedTerritory() {
        return gameLogicManager.getSelectedTerritory();
    }

    public ObjectInputStream getInput() {
        return networkManager.getInput();
    }

    public ObjectOutputStream getOutput() {
        return networkManager.getOutput();
    }
}
    

