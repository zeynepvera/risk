package com.riskgame.client;

import com.riskgame.common.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Risk oyunu için başlangıç ve bitiş ekranlarını içeren genişletilmiş istemci
 * uygulaması.
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
    private JLabel winnerLabel; // Kazanan gösterimi için eklendi

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
    
    /**
 * Başlangıç menü ekranını oluşturur.
 * Geliştirilmiş ve profesyonel kullanıcı arayüzü.
 */
private void initializeMenuScreen() {
    menuPanel = new JPanel(new BorderLayout());
    menuPanel.setBackground(new Color(240, 240, 255));
    
    // ---- Logo/başlık paneli ----
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
    
    // ---- Ana içerik paneli ----
    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
    centerPanel.setBackground(new Color(240, 240, 255));
    centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
    
    // ---- Giriş paneli ----
    JPanel loginPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            
            // Panel için gradient arka plan
            GradientPaint gp = new GradientPaint(0, 0, new Color(250, 250, 255), 
                                               0, height, new Color(235, 235, 255));
            g2d.setPaint(gp);
            
            // Yuvarlak köşeli panel
            RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(
                    0, 0, width-1, height-1, 20, 20);
            g2d.fill(roundedRectangle);
            
            // İnce kenar çizgisi
            g2d.setColor(new Color(200, 200, 230));
            g2d.draw(roundedRectangle);
        }
    };
    loginPanel.setLayout(new BorderLayout());
    loginPanel.setMaximumSize(new Dimension(600, 350));
    loginPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    loginPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
    
    // Üst başlık
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
    usernameField = createStyledTextField("", "Oyunda görünecek adınızı girin");
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
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    JLabel serverIPLabel = new JLabel("Sunucu Adresi:");
    serverIPLabel.setFont(new Font("Arial", Font.PLAIN, 14));
    advancedPanel.add(serverIPLabel, gbc);
    
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    serverIPField = createStyledTextField("localhost", "Sunucu IP adresi");
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
    portField = createStyledTextField(String.valueOf(PORT), "Sunucu port numarası");
    portField.setFont(new Font("Arial", Font.PLAIN, 14));
    advancedPanel.add(portField, gbc);
    
    // AWS buton için ayrı panel
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 3;
    JPanel awsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    awsPanel.setOpaque(false);
    
    JButton awsConnectButton = new JButton("AWS Sunucusu Bağlan");
    awsConnectButton.setFont(new Font("Arial", Font.PLAIN, 12));
    awsConnectButton.setFocusPainted(false);
    awsConnectButton.addActionListener(e -> {
        String awsIp = JOptionPane.showInputDialog(this, 
            "AWS instance IP adresini girin:", 
            "AWS Bağlantısı", 
            JOptionPane.QUESTION_MESSAGE);
        
        if (awsIp != null && !awsIp.trim().isEmpty()) {
            serverIPField.setText(awsIp.trim());
            // Gelişmiş seçenekleri görünür yap
            advancedOptionsCheckbox.setSelected(true);
            advancedPanel.setVisible(true);
        }
    });
    awsPanel.add(awsConnectButton);
    advancedPanel.add(awsPanel, gbc);
    
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
    
    JButton playButton = createStylishButton("OYUNA BAŞLA", 200, 50, new Color(60, 130, 200), new Color(100, 160, 220));
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
    
    JButton exitButton = createStylishButton("ÇIKIŞ", 150, 50, new Color(190, 60, 60), new Color(220, 80, 80));
    exitButton.setFont(new Font("Arial", Font.BOLD, 16));
    exitButton.addActionListener(e -> System.exit(0));
    
    buttonPanel.add(playButton);
    buttonPanel.add(exitButton);
    
    loginPanel.add(buttonPanel, BorderLayout.SOUTH);
    
    centerPanel.add(Box.createVerticalGlue());
    centerPanel.add(loginPanel);
    centerPanel.add(Box.createVerticalGlue());
    
    menuPanel.add(centerPanel, BorderLayout.CENTER);
    
    // ---- Alt bilgi paneli ----
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
     * Stilize edilmiş metin alanı oluşturur.
     */
/**
     * Şık buton oluşturur.
     */
    

    /**
     * Stilize edilmiş metin alanı oluşturur.
     */
    private JTextField createStyledTextField(String initialText, String tooltip) {
        JTextField field = new JTextField(initialText, 15);
        field.setToolTipText(tooltip);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 210), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }

    /**
     * Şık buton oluşturur.
     */
    private JButton createStylishButton(String text, int width, int height, Color baseColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(width, height));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);

        // Hover efekti
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
            }
        });

        // Daha modern görünüm
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 40), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        return button;
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

        winnerLabel = new JLabel("Kazanan: ");
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
        armyCountComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 5, 10});
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
     * Sunucuya bağlanır. Geliştirilmiş soket bağlantısı kullanır.
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

        // Kullanıcı adı validasyonu
        if (!username.matches("^[a-zA-Z0-9_-]{3,16}$")) {
            JOptionPane.showMessageDialog(this,
                    "Geçersiz kullanıcı adı! Kullanıcı adı 3-16 karakter uzunluğunda olmalı ve sadece harfler, rakamlar, alt çizgi ve tire içermelidir.",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
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
        
        // Geliştirilmiş soket bağlantısı
        Socket socket = null;
        
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverIP, port), 10000); // 10 saniye bağlantı zaman aşımı
            socket.setSoTimeout(300000); // 5 dakika okuma zaman aşımı
            
            this.socket = socket;
            
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush(); // Kritik: header bilgisinin hemen gönderilmesini sağlar
            input = new ObjectInputStream(socket.getInputStream());
            
            // Giriş mesajı gönder
            Message loginMessage = new Message(username, "", MessageType.LOGIN);
            System.out.println("Giriş mesajı gönderiliyor...");
            output.writeObject(loginMessage);
            output.flush();
            System.out.println("Giriş mesajı gönderildi.");
            
            // Dinleyici başlat
            clientListener = new ClientListener(this);
            new Thread(clientListener).start();
            
            connected = true;
            statusLabel.setText("Bağlantı durumu: Bağlı - " + serverIP + ":" + port);
            
            chatField.setEnabled(true);
            sendButton.setEnabled(true);
            
            addLogMessage("Sunucuya bağlandı: " + serverIP + ":" + port);
        } catch (IOException e) {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException se) {
                    // Socket kapatma hatasını görmezden gel
                }
            }
            throw e;
        }
    } catch (IOException e) {
        e.printStackTrace(); // Stack trace ekleyin
        JOptionPane.showMessageDialog(this, 
            "Sunucuya bağlanılamadı: " + e.getMessage() + "\n" +
            "AWS sunucusunun çalıştığından ve güvenlik grubunun " + port + " portuna izin verdiğinden emin olun.", 
            "Bağlantı Hatası", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Bağlantı durumu: Bağlantı hatası");
        
        // Ana menüye dönelim
        cardLayout.show(contentPanel, "menu");
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
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
                if (socket != null) {
                    socket.close();
                }

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
        
        addLogMessage("DEBUG: Birlik yerleştirme komutu gönderildi. Bölge: " + territoryName + ", Birlik: " + armies);
        
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
    
    // Debug: Seçilen bölgenin birlik sayısını kontrol et
    if (selectedTerritory != null && gameState.getTerritories().containsKey(selectedTerritory)) {
        Territory territory = gameState.getTerritories().get(selectedTerritory);
        addLogMessage("DEBUG: Seçilen bölge '" + selectedTerritory + "' birlik sayısı: " + territory.getArmies());
    }
    
    mapPanel.setGameState(gameState);
    mapPanel.repaint();
    
    // Kontrol panelini güncelle
    if (gameState.isGameStarted()) {
        setGameControlsEnabled(gameState.getCurrentPlayer().equals(username));
        
        // Oyuncu bilgilerini güncelle
        updatePlayerInfo();
        
        // Debug: Kendi bölgelerinizin birlik sayısını kontrol et
        if (gameState.getPlayers().containsKey(username)) {
            Player player = gameState.getPlayers().get(username);
            for (String territoryName : player.getTerritories()) {
                Territory territory = gameState.getTerritories().get(territoryName);
                addLogMessage("DEBUG: Bölge '" + territoryName + "' birlik sayısı: " + territory.getArmies());
            }
        }
    }
}

    /**
     * Oyuncu bilgilerini günceller.
     */
    private void updatePlayerInfo() {
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

                    // Oyuncu bilgilerini ekle
                    for (String playerName : gameState.getPlayerList()) {
                        Player player = gameState.getPlayers().get(playerName);
                        JLabel playerLabel = new JLabel(playerName + ": " + player.getTerritories().size() + " bölge, "
                                + (playerName.equals(username) ? player.getReinforcementArmies() + " takviye birlik" : ""));

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

    /**
     * Oyun sonu durumunu yönetir ve kazananı gösterir.
     */
    public void handleGameEnd(String winner) {
        SwingUtilities.invokeLater(() -> {
            // Oyun sonu ekranında kazananı göster
            if (winner != null && !winner.isEmpty()) {
                winnerLabel.setText("Kazanan: " + winner);
            } else {
                winnerLabel.setText("Oyun sona erdi.");
            }

            // Oyun sonu ekranına geç
            cardLayout.show(contentPanel, "gameOver");
        });
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
