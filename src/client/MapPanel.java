package client;

import common.GameState;
import common.Player;
import common.Territory;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

/**
 * Enhanced map panel for Risk game. Geliştirilmiş harita görselleştirmesi
 * içerir.
 */
public class MapPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private RiskClient client;
    private GameState gameState;
    private Map<String, Polygon> territoryPolygons;
    private Map<String, Point> territoryCenters;
    private Map<String, String> continentMap;
    private Map<String, Color> continentColors;
    private BufferedImage mapImage;
    private Font territoryFont;
    private Font countFont;
    private String mapStyle = "CLASSIC"; // CLASSIC, MODERN, SATELLITE

    // Ölçekleme faktörü ekleyin
    private float scaleFactor = 1.0f;

    public MapPanel(RiskClient client) {
        this.client = client;
        this.territoryPolygons = new HashMap<>();
        this.territoryCenters = new HashMap<>();
        this.continentMap = new HashMap<>();

        setBackground(new Color(205, 230, 250)); // Light blue background like an ocean

        // Ekran çözünürlüğüne göre ölçekleme faktörünü hesapla
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        int screenWidth = gd.getDisplayMode().getWidth();

        // Düşük çözünürlük ekranlar için daha küçük font ve ölçekleme
        if (screenWidth < 1366) {
            scaleFactor = 0.85f;
            territoryFont = new Font("Arial", Font.BOLD, (int) (9 * scaleFactor));
            countFont = new Font("Arial", Font.BOLD, (int) (12 * scaleFactor));
        } else {
            // Normal boyutlar
            territoryFont = new Font("Arial", Font.BOLD, 10);
            countFont = new Font("Arial", Font.BOLD, 14);
        }

        // Better fonts
        territoryFont = new Font("Arial", Font.BOLD, 10);
        countFont = new Font("Arial", Font.BOLD, 14);

        // Territory click listener
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

        // Initialize continent colors with the selected style
        initializeColorSchemes();

        // Create territory polygons
        initializeTerritoryPolygons();

        // Pre-render the map background
        createMapBackground();
    }

    /**
     * Harita stiline göre renk şemalarını başlatır.
     */
    private void initializeColorSchemes() {
        continentColors = new HashMap<>();

        if ("MODERN".equals(mapStyle)) {
            // Modern renk şeması (daha keskin, tarz görünüm)
            continentColors.put("KUZEY_AMERIKA", new Color(241, 90, 90));
            continentColors.put("GUNEY_AMERIKA", new Color(255, 215, 0));
            continentColors.put("AVRUPA", new Color(79, 134, 247));
            continentColors.put("AFRIKA", new Color(255, 152, 0));
            continentColors.put("ASYA", new Color(76, 187, 23));
            continentColors.put("AVUSTRALYA", new Color(173, 20, 87));
        } else if ("SATELLITE".equals(mapStyle)) {
            // Uydu görünümü (daha gerçekçi renkler)
            continentColors.put("KUZEY_AMERIKA", new Color(139, 169, 110));
            continentColors.put("GUNEY_AMERIKA", new Color(86, 130, 89));
            continentColors.put("AVRUPA", new Color(207, 197, 168));
            continentColors.put("AFRIKA", new Color(209, 178, 110));
            continentColors.put("ASYA", new Color(177, 170, 141));
            continentColors.put("AVUSTRALYA", new Color(194, 157, 115));
        } else {
            // Classic (varsayılan) renk şeması
            continentColors.put("KUZEY_AMERIKA", new Color(255, 150, 150));
            continentColors.put("GUNEY_AMERIKA", new Color(255, 215, 0));
            continentColors.put("AVRUPA", new Color(100, 149, 237));
            continentColors.put("AFRIKA", new Color(255, 165, 0));
            continentColors.put("ASYA", new Color(144, 238, 144));
            continentColors.put("AVUSTRALYA", new Color(218, 112, 214));
        }
    }

    /**
     * Harita stilini değiştirir.
     */
    public void setMapStyle(String style) {
        this.mapStyle = style;
        initializeColorSchemes();
        createMapBackground();
        repaint();
    }

    public void setGameState(GameState gameState) {
        System.out.println("\n=== HARITA PANELI: OYUN DURUMU GÜNCELLENIYOR ===");

        if (gameState == null) {
            System.out.println("GameState null, harita temizleniyor...");
            this.gameState = null;
            repaint();
            return;
        }

        // Yeni oyun durumunu ata
        this.gameState = gameState;

        // Bölge durumlarını log'a yaz
        System.out.println("Haritada gösterilecek bölge durumları:");
        for (Map.Entry<String, Territory> entry : gameState.getTerritories().entrySet()) {
            String territoryName = entry.getKey();
            Territory territory = entry.getValue();
            System.out.println("Bölge: " + territoryName
                    + " | Sahibi: " + territory.getOwner()
                    + " | Birlik: " + territory.getArmies());
        }

        // Ekranı zorla yenile
        repaint();

        System.out.println("=== HARITA GÜNCELLEME TAMAMLANDI ===\n");
    }

    /**
     * Creates a pre-rendered map background for better performance
     */
    private void createMapBackground() {

        // Harita boyutunu ölçekleme faktörüne göre ayarlayın
        int mapWidth = (int) (1000 * scaleFactor);
        int mapHeight = (int) (800 * scaleFactor);

        mapImage = new BufferedImage(mapWidth, mapHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = mapImage.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw ocean background
        if ("SATELLITE".equals(mapStyle)) {
            // Daha koyu mavi okyanus (uydu görünümü)
            g2.setColor(new Color(35, 95, 155));
        } else {
            // Standart açık mavi okyanus
            g2.setColor(new Color(205, 230, 250));
        }
        g2.fillRect(0, 0, 1000, 800);

        // Draw a subtle grid pattern
        if (!"SATELLITE".equals(mapStyle)) {
            // Satellit modunda grid çizme
            g2.setColor(new Color(195, 220, 240));
            for (int i = 0; i < 1000; i += 20) {
                g2.drawLine(i, 0, i, 800);
                g2.drawLine(0, i, 1000, i);
            }
        }

        // Draw continent shapes (faint outlines to give context)
        drawContinentOutlines(g2);

        // Harita adını çiz
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.setColor(new Color(50, 50, 50, 120));
        g2.drawString("Risk Game Map - " + mapStyle + " Style", 20, 780);

        g2.dispose();
    }

    /**
     * Draws continent outlines for the background
     */
    private void drawContinentOutlines(Graphics2D g2) {
        // For each continent, draw a faint outline around all its territories
        Map<String, Polygon> continentOutlines = new HashMap<>();

        // This would be enhanced in a full implementation
        // Here just drawing faint borders for each continent
        if ("SATELLITE".equals(mapStyle)) {
            g2.setColor(new Color(255, 255, 255, 40)); // Uydu görünümünde hafif beyaz sınırlar
        } else if ("MODERN".equals(mapStyle)) {
            g2.setColor(new Color(50, 50, 50, 70)); // Modern görünümde koyu sınırlar
        } else {
            g2.setColor(new Color(180, 210, 230)); // Klasik görünümde standart sınırlar
        }

        // Çizgi stili - stil seçimine göre değişir
        float strokeWidth = "MODERN".equals(mapStyle) ? 2.0f : 3.0f;
        Stroke dashedStroke;

        if ("SATELLITE".equals(mapStyle)) {
            // Uydu görünümünde daha ince kesikli çizgiler
            dashedStroke = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                    1.0f, new float[]{5.0f, 3.0f}, 0.0f);
        } else if ("MODERN".equals(mapStyle)) {
            // Modern görünümde kesintisiz çizgiler
            dashedStroke = new BasicStroke(strokeWidth);
        } else {
            // Klasik kesikli çizgiler
            dashedStroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                    1.0f, new float[]{10.0f, 5.0f}, 0.0f);
        }

        g2.setStroke(dashedStroke);

        // Simplified continent outlines
        // North America
        g2.drawRect(50, 40, 200, 350);

        // South America
        g2.drawRect(170, 400, 150, 350);

        // Europe
        g2.drawRect(400, 40, 200, 300);

        // Africa
        g2.drawRect(400, 300, 200, 350);

        // Asia
        g2.drawRect(600, 40, 300, 400);

        // Australia
        g2.drawRect(700, 500, 150, 250);
    }

    /**
     * Initialize territory polygons with more natural-looking shapes.
     */
    private void initializeTerritoryPolygons() {
        // North America - More natural looking boundaries
        createTerritory("ALASKA", "KUZEY_AMERIKA",
                new int[]{30, 80, 120, 140, 130, 100, 40},
                new int[]{60, 40, 50, 90, 120, 130, 100},
                new Point(90, 80));

        createTerritory("KUZEYBATI_BOLGE", "KUZEY_AMERIKA",
                new int[]{130, 200, 230, 210, 180, 140},
                new int[]{120, 70, 100, 140, 180, 90},
                new Point(180, 120));

        createTerritory("GRÖNLAND", "KUZEY_AMERIKA",
                new int[]{250, 320, 380, 350, 310, 280, 260},
                new int[]{40, 30, 70, 120, 150, 130, 90},
                new Point(320, 80));

        createTerritory("ALBERTA", "KUZEY_AMERIKA",
                new int[]{100, 140, 180, 190, 160, 120, 90},
                new int[]{130, 90, 180, 230, 260, 220, 170},
                new Point(150, 180));

        createTerritory("ONTARIO", "KUZEY_AMERIKA",
                new int[]{180, 210, 250, 270, 230, 190},
                new int[]{180, 140, 170, 230, 260, 230},
                new Point(220, 200));

        createTerritory("QUEBEC", "KUZEY_AMERIKA",
                new int[]{270, 310, 350, 330, 290, 250},
                new int[]{230, 200, 240, 280, 290, 260},
                new Point(310, 250));

        createTerritory("BATI_ABD", "KUZEY_AMERIKA",
                new int[]{90, 120, 160, 180, 150, 90, 70},
                new int[]{170, 220, 260, 310, 330, 280, 210},
                new Point(130, 250));

        createTerritory("DOGU_ABD", "KUZEY_AMERIKA",
                new int[]{190, 230, 270, 250, 220, 180, 150},
                new int[]{230, 260, 290, 330, 360, 310, 260},
                new Point(220, 290));

        createTerritory("ORTA_AMERIKA", "KUZEY_AMERIKA",
                new int[]{90, 150, 180, 220, 190, 160, 120, 80},
                new int[]{280, 330, 370, 410, 430, 390, 350, 310},
                new Point(150, 350));

        // South America
        createTerritory("VENEZUELA", "GUNEY_AMERIKA",
                new int[]{190, 240, 290, 280, 220, 170},
                new int[]{430, 420, 450, 500, 510, 480},
                new Point(230, 470));

        createTerritory("PERU", "GUNEY_AMERIKA",
                new int[]{170, 220, 270, 250, 200, 150},
                new int[]{480, 510, 550, 600, 620, 550},
                new Point(210, 550));

        createTerritory("BREZILYA", "GUNEY_AMERIKA",
                new int[]{220, 280, 320, 340, 300, 250, 270},
                new int[]{510, 500, 530, 590, 630, 600, 550},
                new Point(280, 570));

        createTerritory("ARJANTIN", "GUNEY_AMERIKA",
                new int[]{200, 250, 300, 280, 230, 180, 170},
                new int[]{620, 600, 630, 690, 730, 700, 650},
                new Point(230, 680));

        // Europe - More natural looking shapes
        createTerritory("IZLANDA", "AVRUPA",
                new int[]{400, 430, 470, 460, 420, 390},
                new int[]{80, 60, 90, 130, 140, 110},
                new Point(430, 100));

        createTerritory("ISKANDINAVYA", "AVRUPA",
                new int[]{460, 490, 530, 560, 520, 480, 450},
                new int[]{130, 70, 60, 110, 160, 180, 140},
                new Point(500, 120));

        createTerritory("BUYUK_BRITANYA", "AVRUPA",
                new int[]{390, 420, 450, 430, 400, 380},
                new int[]{170, 150, 180, 220, 230, 200},
                new Point(410, 190));

        createTerritory("BATI_AVRUPA", "AVRUPA",
                new int[]{400, 430, 470, 450, 420, 390, 370},
                new int[]{230, 220, 250, 290, 320, 310, 260},
                new Point(420, 270));

        createTerritory("KUZEY_AVRUPA", "AVRUPA",
                new int[]{450, 480, 520, 540, 510, 470},
                new int[]{180, 180, 200, 240, 270, 250},
                new Point(500, 220));

        createTerritory("GUNEY_AVRUPA", "AVRUPA",
                new int[]{420, 470, 510, 540, 500, 450},
                new int[]{320, 250, 270, 310, 340, 290},
                new Point(480, 300));

        createTerritory("UKRAYNA", "AVRUPA",
                new int[]{520, 560, 600, 620, 580, 540, 510},
                new int[]{160, 110, 150, 230, 260, 240, 200},
                new Point(560, 190));

        // Africa
        createTerritory("KUZEY_AFRIKA", "AFRIKA",
                new int[]{390, 450, 500, 480, 430, 370, 360},
                new int[]{310, 290, 340, 390, 420, 400, 350},
                new Point(420, 350));

        createTerritory("MISIR", "AFRIKA",
                new int[]{480, 520, 580, 550, 500, 470},
                new int[]{340, 320, 370, 410, 400, 370},
                new Point(520, 370));

        createTerritory("DOGU_AFRIKA", "AFRIKA",
                new int[]{500, 550, 590, 570, 530, 490},
                new int[]{400, 410, 440, 490, 520, 470},
                new Point(540, 450));

        createTerritory("KONGO", "AFRIKA",
                new int[]{430, 490, 530, 510, 470, 410},
                new int[]{420, 470, 520, 560, 580, 510},
                new Point(480, 510));

        createTerritory("GUNEY_AFRIKA", "AFRIKA",
                new int[]{470, 510, 550, 540, 490, 440},
                new int[]{580, 560, 590, 650, 680, 630},
                new Point(510, 620));

        createTerritory("MADAGASKAR", "AFRIKA",
                new int[]{570, 590, 620, 610, 580, 560},
                new int[]{550, 530, 560, 600, 620, 590},
                new Point(590, 570));

        // Asia - More natural-looking shapes
        createTerritory("URAL", "ASYA",
                new int[]{620, 660, 700, 680, 640, 600},
                new int[]{150, 130, 160, 210, 230, 200},
                new Point(650, 180));

        createTerritory("SIBIRYA", "ASYA",
                new int[]{680, 720, 780, 760, 720, 660},
                new int[]{210, 150, 170, 220, 250, 230},
                new Point(720, 190));

        createTerritory("YAKUTSK", "ASYA",
                new int[]{760, 800, 860, 840, 790, 730},
                new int[]{170, 120, 150, 200, 210, 190},
                new Point(800, 170));

        createTerritory("KAMCHATKA", "ASYA",
                new int[]{840, 870, 910, 900, 860, 820},
                new int[]{150, 100, 130, 180, 200, 170},
                new Point(860, 150));

        createTerritory("IRKUTSK", "ASYA",
                new int[]{760, 790, 840, 830, 780, 730},
                new int[]{220, 210, 240, 280, 290, 250},
                new Point(790, 250));

        createTerritory("MOĞOLISTAN", "ASYA",
                new int[]{730, 780, 830, 810, 760, 710},
                new int[]{250, 290, 320, 360, 340, 290},
                new Point(780, 310));

        createTerritory("JAPONYA", "ASYA",
                new int[]{870, 890, 920, 910, 880, 860},
                new int[]{240, 210, 240, 270, 290, 260},
                new Point(890, 250));

        createTerritory("AFGANISTAN", "ASYA",
                new int[]{580, 640, 680, 670, 620, 570},
                new int[]{260, 230, 260, 310, 340, 310},
                new Point(630, 280));

        createTerritory("ÇIN", "ASYA",
                new int[]{710, 760, 810, 800, 750, 690},
                new int[]{290, 340, 380, 420, 410, 350},
                new Point(750, 360));

        createTerritory("ORTA_DOĞU", "ASYA",
                new int[]{530, 570, 620, 610, 560, 520},
                new int[]{310, 310, 340, 380, 400, 360},
                new Point(570, 350));

        createTerritory("HINDISTAN", "ASYA",
                new int[]{620, 670, 710, 700, 660, 610},
                new int[]{340, 350, 380, 430, 450, 380},
                new Point(660, 390));

        createTerritory("SIAM", "ASYA",
                new int[]{690, 730, 770, 760, 720, 680},
                new int[]{410, 410, 450, 500, 510, 450},
                new Point(730, 460));

        // Australia
        createTerritory("ENDONEZYA", "AVUSTRALYA",
                new int[]{720, 760, 800, 790, 750, 710},
                new int[]{490, 480, 520, 560, 570, 530},
                new Point(750, 530));

        createTerritory("YENI_GINE", "AVUSTRALYA",
                new int[]{790, 830, 870, 850, 810, 780},
                new int[]{560, 550, 580, 620, 630, 590},
                new Point(830, 590));

        createTerritory("BATI_AVUSTRALYA", "AVUSTRALYA",
                new int[]{720, 760, 800, 780, 740, 700},
                new int[]{590, 570, 610, 660, 680, 640},
                new Point(750, 630));

        createTerritory("DOGU_AVUSTRALYA", "AVUSTRALYA",
                new int[]{780, 820, 860, 840, 790, 750},
                new int[]{660, 640, 670, 710, 730, 690},
                new Point(810, 680));
    }

    /**
     * Create a territory with improved shapes.
     */
    private void createTerritory(String name, String continent, int[] xpoints, int[] ypoints, Point center) {
        Polygon polygon = new Polygon(xpoints, ypoints, xpoints.length);
        territoryPolygons.put(name, polygon);
        territoryCenters.put(name, center);
        continentMap.put(name, continent);
    }

    @Override
    protected void paintComponent(Graphics g) {
        System.out.println("DEBUG: MapPanel.paintComponent() çağrıldı");

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw pre-rendered background
        g2d.drawImage(mapImage, 0, 0, this);

        if (gameState != null) {
            System.out.println("DEBUG: Oyun durumu var, harita çiziliyor. Oyuncu sayısı: "
                    + gameState.getPlayerList().size() + ", Bölge sayısı: "
                    + gameState.getTerritories().size());
        } else {
            System.out.println("DEBUG: Oyun durumu NULL, boş harita çizilecek");
        }

        // Draw the territory overlays
        drawMap(g2d);
    }

    /**
     * Draw the map with enhanced graphics.
     */
    private void drawMap(Graphics2D g2d) {
        if (gameState == null) {
            // Draw empty map with continent colors
            for (Map.Entry<String, Polygon> entry : territoryPolygons.entrySet()) {
                String territory = entry.getKey();
                Polygon polygon = entry.getValue();
                String continent = continentMap.get(territory);
                Color baseColor = continentColors.get(continent);

                // Use gradient fill for more depth
                GradientPaint gradient = new GradientPaint(
                        0, 0, baseColor,
                        0, 50, baseColor.darker(),
                        true);
                g2d.setPaint(gradient);

                g2d.fill(polygon);
                g2d.setColor(new Color(50, 50, 50, 150));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(polygon);

                // Draw territory names
                g2d.setFont(territoryFont);
                Point center = territoryCenters.get(territory);
                drawCenteredString(g2d, getShortName(territory), center.x, center.y - 8, Color.BLACK);
            }
            System.out.println("UYARI: Oyun durumu boş, harita çizilemedi!");
            return;
        }

        System.out.println("\n=== HARITA CIZILIYOR ===");
        System.out.println("Oyun durumu: Oyun başladı=" + gameState.isGameStarted() + ", Mevcut oyuncu=" + gameState.getCurrentPlayer());
        System.out.println("Toplam bölge sayısı: " + gameState.getTerritories().size());

        // Bölgelerin sahibini ve birlik sayısını listele (hafıza adresleri dahil) 
        System.out.println("\n*** MEVCUT OYUN DURUMU (Harita çizimi başlangıcı) ***");
        System.out.println("GameState nesnesi: " + gameState);
        System.out.println("Territories nesnesi: " + gameState.getTerritories());
        for (Map.Entry<String, Territory> entry : gameState.getTerritories().entrySet()) {
            String territoryName = entry.getKey();
            Territory territory = entry.getValue();
            System.out.println("Bölge: " + territoryName
                    + " | Sahibi: " + territory.getOwner()
                    + " | Birlik Sayısı: " + territory.getArmies()
                    + " | Nesne: " + territory);
        }
        System.out.println("*** OYUN DURUMU SONU ***\n");

        // Player colors with improved aesthetics
        Map<String, Color> playerColors = new HashMap<>();
        Color[] colors = {
            new Color(192, 57, 43),
            new Color(44, 62, 80),
             new Color(40, 130, 40), // Koyu Yeşil
            new Color(210, 180, 0), // Altın
            new Color(110, 40, 130), // Mor
            new Color(40, 130, 130) // Turkuaz
        };

        int colorIndex = 0;
        for (String playerName : gameState.getPlayerList()) {
            playerColors.put(playerName, colors[colorIndex % colors.length]);
            colorIndex++;
        }

        // Draw territories with owner colors
        for (Map.Entry<String, Territory> entry : gameState.getTerritories().entrySet()) {
            String territoryName = entry.getKey();
            Territory territory = entry.getValue();
            Polygon polygon = territoryPolygons.get(territoryName);

            if (polygon != null) {
                // Log: Çizilen bölge
                System.out.println("Çiziliyor: " + territoryName
                        + " | Sahibi: " + territory.getOwner()
                        + " | Birlik: " + territory.getArmies());

                // Determine territory color based on owner
                String owner = territory.getOwner();
                Color playerColor = playerColors.get(owner);

                if (playerColor == null) {
                    System.err.println("HATA: " + owner + " oyuncusu için renk bulunamadı!");
                    playerColor = Color.GRAY; // Fallback color
                }

                // Highlight selected territory
                if (territoryName.equals(client.getSelectedTerritory())) {
                    // Highlight with bright yellow glow
                    g2d.setColor(new Color(255, 255, 100, 100));
                    g2d.setStroke(new BasicStroke(4.0f));
                    g2d.draw(polygon);

                    // Fill with slightly brighter color
                    GradientPaint gradient = new GradientPaint(
                            0, 0, playerColor.brighter(),
                            0, 50, playerColor,
                            true);
                    g2d.setPaint(gradient);
                } else {
                    // Normal gradient for owned territories
                    GradientPaint gradient = new GradientPaint(
                            0, 0, playerColor,
                            0, 50, playerColor.darker(),
                            true);
                    g2d.setPaint(gradient);
                }

                g2d.fill(polygon);

                // Draw territory border
                g2d.setColor(new Color(40, 40, 40, 180));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(polygon);

                // Draw army count with a nice circle background
                Point center = territoryCenters.get(territoryName);
                if (center == null) {
                    System.err.println("HATA: " + territoryName + " bölgesi için merkez noktası bulunamadı!");
                    continue;
                }

                // Birlik sayısını alın (asla null olmamalı)
                String armyCount = String.valueOf(territory.getArmies());
                System.out.println("Çizim: " + territoryName + " - Birlik Sayısı: " + armyCount);

                // Draw circle background for army count
                int circleSize = 24;
                g2d.setColor(Color.WHITE);
                g2d.fillOval(center.x - circleSize / 2, center.y - circleSize / 2, circleSize, circleSize);
                g2d.setColor(new Color(50, 50, 50));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawOval(center.x - circleSize / 2, center.y - circleSize / 2, circleSize, circleSize);

                // Draw army count - çok önemli!
                g2d.setFont(countFont);
                drawCenteredString(g2d, armyCount, center.x, center.y + 5, Color.BLACK);

                // Draw territory name
                g2d.setFont(territoryFont);
                g2d.setColor(Color.RED); // Daha belirgin olması için kırmızı renk kullanın
                drawCenteredString(g2d, getShortName(territoryName), center.x, center.y - 15, Color.BLACK);
            } else {
                System.err.println("HATA: '" + territoryName + "' bölgesi için polygon bulunamadı!");
            }
        }

        // Draw game status info in a nice panel
        drawGameStatusPanel(g2d);
        System.out.println("=== HARITA CIZIMI TAMAMLANDI ===\n");
    }

    /**
     * Draws a game status panel with current player info
     */
    private void drawGameStatusPanel(Graphics2D g2d) {
        String currentPlayer = gameState.getCurrentPlayer();
        if (currentPlayer == null) {
            return;
        }

        // Create a semi-transparent panel in the top-left
        RoundRectangle2D.Float panel = new RoundRectangle2D.Float(10, 10, 200, 50, 10, 10);
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fill(panel);
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(panel);

        // Draw text
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Sıra: " + currentPlayer, 20, 30);

        // If current player is this client's user, show reinforcement info
        if (currentPlayer.equals(client.getUsername())) {
            Player player = gameState.getPlayers().get(client.getUsername());
            if (player != null) {
                g2d.drawString("Takviye: " + player.getReinforcementArmies() + " birlik", 20, 50);
            }
        }
    }

    /**
     * Helper method to draw centered strings
     */
    private void drawCenteredString(Graphics2D g2d, String text, int x, int y, Color color) {
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);

        g2d.setColor(color);
        g2d.drawString(text, x - textWidth / 2, y);
    }

    /**
     * Get a short name for territory display
     */
    private String getShortName(String name) {
        if (name.length() <= 5) {
            return name;
        } else {
            // Get first 5 characters for display
            return name.substring(0, 5);
        }
    }
}
