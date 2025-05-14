package com.riskgame.client;

import com.riskgame.common.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

/**
 * Harita paneli sınıfı.
 */
public class MapPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private RiskClient client;
    private GameState gameState;
    private Map<String, Polygon> territoryPolygons;
    private Map<String, Point> territoryCenters;
    
    public MapPanel(RiskClient client) {
        this.client = client;
        this.territoryPolygons = new HashMap<>();
        this.territoryCenters = new HashMap<>();
        
        setBackground(Color.WHITE);
        
        // Bölge tıklamaları için dinleyici
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameState != null) {
                    for (Map.Entry<String, Polygon> entry : territoryPolygons.entrySet()) {
                        if (entry.getValue().contains(e.getPoint())) {
                            client.territoryClicked(entry.getKey());
                            break;
                        }
                    }
                }
            }
        });
        
        // Bölge polygon'larını oluştur
        initializeTerritoryPolygons();
    }
    
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
    
    /**
     * Bölge polygonlarını oluşturur.
     */
    private void initializeTerritoryPolygons() {
        // Kuzey Amerika
        createTerritory("ALASKA", new int[]{50, 100, 150, 100}, new int[]{50, 50, 100, 100}, new Point(100, 75));
        createTerritory("KUZEYBATI_BOLGE", new int[]{150, 200, 250, 200, 150}, new int[]{100, 50, 100, 150, 100}, new Point(200, 100));
        createTerritory("GRÖNLAND", new int[]{250, 300, 350, 300, 250}, new int[]{100, 50, 100, 150, 100}, new Point(300, 100));
        createTerritory("ALBERTA", new int[]{100, 150, 200, 150, 100}, new int[]{100, 100, 150, 200, 150}, new Point(150, 150));
        createTerritory("ONTARIO", new int[]{200, 250, 300, 250, 200}, new int[]{150, 150, 200, 250, 200}, new Point(250, 200));
        createTerritory("QUEBEC", new int[]{300, 350, 400, 350, 300}, new int[]{150, 150, 200, 250, 200}, new Point(350, 200));
        createTerritory("BATI_ABD", new int[]{100, 150, 200, 150, 100}, new int[]{150, 200, 250, 300, 250}, new Point(150, 250));
        createTerritory("DOGU_ABD", new int[]{200, 250, 300, 250, 200}, new int[]{200, 250, 300, 350, 300}, new Point(250, 300));
        createTerritory("ORTA_AMERIKA", new int[]{150, 200, 250, 200, 150}, new int[]{300, 350, 400, 450, 400}, new Point(200, 400));
        
        // Güney Amerika
        createTerritory("VENEZUELA", new int[]{200, 250, 300, 250, 200}, new int[]{450, 450, 500, 550, 500}, new Point(250, 500));
        createTerritory("PERU", new int[]{200, 250, 300, 250, 200}, new int[]{500, 550, 600, 650, 600}, new Point(250, 600));
        createTerritory("BREZILYA", new int[]{250, 300, 350, 300, 250}, new int[]{500, 500, 550, 600, 550}, new Point(300, 550));
        createTerritory("ARJANTIN", new int[]{200, 250, 300, 250, 200}, new int[]{600, 650, 700, 750, 700}, new Point(250, 700));
        
        // Avrupa
        createTerritory("IZLANDA", new int[]{400, 450, 500, 450, 400}, new int[]{50, 50, 100, 150, 100}, new Point(450, 100));
        createTerritory("ISKANDINAVYA", new int[]{450, 500, 550, 500, 450}, new int[]{100, 50, 100, 150, 100}, new Point(500, 100));
        createTerritory("BUYUK_BRITANYA", new int[]{400, 450, 500, 450, 400}, new int[]{100, 150, 200, 250, 200}, new Point(450, 200));
        createTerritory("BATI_AVRUPA", new int[]{400, 450, 500, 450, 400}, new int[]{200, 250, 300, 350, 300}, new Point(450, 300));
        createTerritory("KUZEY_AVRUPA", new int[]{450, 500, 550, 500, 450}, new int[]{150, 150, 200, 250, 200}, new Point(500, 200));
        createTerritory("GUNEY_AVRUPA", new int[]{450, 500, 550, 500, 450}, new int[]{250, 250, 300, 350, 300}, new Point(500, 300));
        createTerritory("UKRAYNA", new int[]{500, 550, 600, 550, 500}, new int[]{150, 150, 200, 250, 200}, new Point(550, 200));
        
        // Afrika
        createTerritory("KUZEY_AFRIKA", new int[]{400, 450, 500, 450, 400}, new int[]{300, 350, 400, 450, 400}, new Point(450, 400));
        createTerritory("MISIR", new int[]{450, 500, 550, 500, 450}, new int[]{350, 350, 400, 450, 400}, new Point(500, 400));
        createTerritory("DOGU_AFRIKA", new int[]{500, 550, 600, 550, 500}, new int[]{400, 400, 450, 500, 450}, new Point(550, 450));
        createTerritory("KONGO", new int[]{450, 500, 550, 500, 450}, new int[]{450, 450, 500, 550, 500}, new Point(500, 500));
        createTerritory("GUNEY_AFRIKA", new int[]{450, 500, 550, 500, 450}, new int[]{550, 550, 600, 650, 600}, new Point(500, 600));
        createTerritory("MADAGASKAR", new int[]{550, 600, 650, 600, 550}, new int[]{500, 500, 550, 600, 550}, new Point(600, 550));
        
        // Asya
        createTerritory("URAL", new int[]{600, 650, 700, 650, 600}, new int[]{150, 150, 200, 250, 200}, new Point(650, 200));
        createTerritory("SIBIRYA", new int[]{650, 700, 750, 700, 650}, new int[]{100, 100, 150, 200, 150}, new Point(700, 150));
        createTerritory("YAKUTSK", new int[]{700, 750, 800, 750, 700}, new int[]{50, 50, 100, 150, 100}, new Point(750, 100));
        createTerritory("KAMCHATKA", new int[]{750, 800, 850, 800, 750}, new int[]{100, 50, 100, 150, 100}, new Point(800, 100));
        createTerritory("IRKUTSK", new int[]{700, 750, 800, 750, 700}, new int[]{150, 150, 200, 250, 200}, new Point(750, 200));
        createTerritory("MOĞOLISTAN", new int[]{700, 750, 800, 750, 700}, new int[]{200, 200, 250, 300, 250}, new Point(750, 250));
        createTerritory("JAPONYA", new int[]{800, 850, 900, 850, 800}, new int[]{150, 150, 200, 250, 200}, new Point(850, 200));
        createTerritory("AFGANISTAN", new int[]{600, 650, 700, 650, 600}, new int[]{250, 250, 300, 350, 300}, new Point(650, 300));
        createTerritory("ÇIN", new int[]{650, 700, 750, 700, 650}, new int[]{300, 300, 350, 400, 350}, new Point(700, 350));
        createTerritory("ORTA_DOĞU", new int[]{550, 600, 650, 600, 550}, new int[]{300, 300, 350, 400, 350}, new Point(600, 350));
        createTerritory("HINDISTAN", new int[]{600, 650, 700, 650, 600}, new int[]{350, 350, 400, 450, 400}, new Point(650, 400));
        createTerritory("SIAM", new int[]{650, 700, 750, 700, 650}, new int[]{400, 400, 450, 500, 450}, new Point(700, 450));
        
        // Avustralya
        createTerritory("ENDONEZYA", new int[]{700, 750, 800, 750, 700}, new int[]{500, 500, 550, 600, 550}, new Point(750, 550));
        createTerritory("YENI_GINE", new int[]{750, 800, 850, 800, 750}, new int[]{550, 550, 600, 650, 600}, new Point(800, 600));
        createTerritory("BATI_AVUSTRALYA", new int[]{700, 750, 800, 750, 700}, new int[]{600, 600, 650, 700, 650}, new Point(750, 650));
        createTerritory("DOGU_AVUSTRALYA", new int[]{750, 800, 850, 800, 750}, new int[]{650, 650, 700, 750, 700}, new Point(800, 700));
    }
    
    /**
     * Bölge oluşturur.
     */
    private void createTerritory(String name, int[] xpoints, int[] ypoints, Point center) {
        Polygon polygon = new Polygon(xpoints, ypoints, xpoints.length);
        territoryPolygons.put(name, polygon);
        territoryCenters.put(name, center);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Haritayı çiz
        drawMap(g2d);
    }
    
    /**
     * Haritayı çizer.
     */
    private void drawMap(Graphics2D g2d) {
        if (gameState == null) {
            // Boş harita çiz
            g2d.setColor(Color.LIGHT_GRAY);
            for (Map.Entry<String, Polygon> entry : territoryPolygons.entrySet()) {
                g2d.fill(entry.getValue());
                g2d.setColor(Color.BLACK);
                g2d.draw(entry.getValue());
                g2d.setColor(Color.LIGHT_GRAY);
            }
            return;
        }
        
        // Kıtaları farklı tonlarda çiz
        Map<String, Color> continentColors = new HashMap<>();
        continentColors.put("KUZEY_AMERIKA", new Color(255, 200, 200)); // Açık kırmızı
        continentColors.put("GUNEY_AMERIKA", new Color(255, 255, 200)); // Açık sarı
        continentColors.put("AVRUPA", new Color(200, 200, 255)); // Açık mavi
        continentColors.put("AFRIKA", new Color(255, 220, 180)); // Turuncu
        continentColors.put("ASYA", new Color(200, 255, 200)); // Açık yeşil
        continentColors.put("AVUSTRALYA", new Color(255, 200, 255)); // Açık mor
        
        // Oyuncu renkleri
        Map<String, Color> playerColors = new HashMap<>();
        String[] colorNames = {"red", "blue", "green", "yellow", "cyan", "magenta"};
        Color[] colors = {
            new Color(220, 0, 0),     // Kırmızı
            new Color(0, 0, 220),     // Mavi
            new Color(0, 180, 0),     // Yeşil
            new Color(220, 220, 0),   // Sarı
            new Color(0, 180, 180),   // Turkuaz
            new Color(180, 0, 180)    // Mor
        };
        
        int colorIndex = 0;
        for (String playerName : gameState.getPlayerList()) {
            playerColors.put(playerName, colors[colorIndex % colors.length]);
            colorIndex++;
        }
        
        // Bölgeleri çiz
        for (Map.Entry<String, Territory> entry : gameState.getTerritories().entrySet()) {
            String territoryName = entry.getKey();
            Territory territory = entry.getValue();
            Polygon polygon = territoryPolygons.get(territoryName);
            
            if (polygon != null) {
                // Bölge rengini belirle
                String owner = territory.getOwner();
                Color baseColor = continentColors.get(territory.getContinent());
                Color playerColor = playerColors.get(owner);
                
                // Seçili bölgeyi vurgula
                if (territoryName.equals(client.getSelectedTerritory())) {
                    g2d.setColor(Color.YELLOW);
                    g2d.fill(polygon);
                } else {
                    g2d.setColor(playerColor);
                    g2d.fill(polygon);
                }
                
                // Bölge sınırlarını çiz
                g2d.setColor(Color.BLACK);
                g2d.draw(polygon);
                
                // Birlik sayısını göster
                Point center = territoryCenters.get(territoryName);
                String armyCount = String.valueOf(territory.getArmies());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(armyCount);
                int textHeight = fm.getHeight();
                
                g2d.setColor(Color.WHITE);
                g2d.fillOval(center.x - 10, center.y - 10, 20, 20);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(center.x - 10, center.y - 10, 20, 20);
                g2d.drawString(armyCount, center.x - textWidth / 2, center.y + textHeight / 4);
                
                // Bölge adını göster
                String shortName = getShortName(territoryName);
                textWidth = fm.stringWidth(shortName);
                g2d.drawString(shortName, center.x - textWidth / 2, center.y - 15);
            }
        }
        
        // Mevcut oyuncuyu ve tur bilgisini göster
        String currentPlayer = gameState.getCurrentPlayer();
        if (currentPlayer != null) {
            g2d.setColor(Color.BLACK);
            g2d.drawString("Sıra: " + currentPlayer + " oyuncusunda", 20, 20);
            
            // Eğer mevcut oyuncu bu istemcinin kullanıcısıysa, birlik takviyesi bilgisini göster
            if (currentPlayer.equals(client.getUsername())) {
                Player player = gameState.getPlayers().get(client.getUsername());
                if (player != null) {
                    g2d.drawString("Takviye birlikler: " + player.getReinforcementArmies(), 20, 40);
                }
            }
        }
        
        // Kıta lejantını göster
        int legendY = 60;
        g2d.setColor(Color.BLACK);
        g2d.drawString("Kıtalar:", 20, legendY);
        legendY += 20;
        
        for (Map.Entry<String, Color> entry : continentColors.entrySet()) {
            g2d.setColor(entry.getValue());
            g2d.fillRect(20, legendY, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(20, legendY, 15, 15);
            g2d.drawString(entry.getKey(), 40, legendY + 12);
            legendY += 20;
        }
        
        // Oyuncu lejantını göster
        legendY += 10;
        g2d.setColor(Color.BLACK);
        g2d.drawString("Oyuncular:", 20, legendY);
        legendY += 20;
        
        for (Map.Entry<String, Color> entry : playerColors.entrySet()) {
            g2d.setColor(entry.getValue());
            g2d.fillRect(20, legendY, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(20, legendY, 15, 15);
            g2d.drawString(entry.getKey(), 40, legendY + 12);
            legendY += 20;
        }
    }
    
    /**
     * Bölge adını kısaltır.
     */
    private String getShortName(String name) {
        if (name.length() <= 5) {
            return name;
        } else {
            // İlk 5 karakteri al
            return name.substring(0, 5);
        }
    }}