
package client;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Zar atma diyaloğu. Saldırı sırasında zarları gösterir.
 */
public class DiceDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private JPanel attackPanel;
    private JPanel defensePanel;
    private JButton rollButton;
    private JButton continueButton;
    
    private int[] attackDice;
    private int[] defenseDice;
    
    public DiceDialog(JFrame parent, String attackerName, String defenderName, int attackerCount, int defenderCount) {
        super(parent, "Zar Atma", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Bilgi paneli
        JPanel infoPanel = new JPanel(new GridLayout(1, 2));
        
        JLabel attackerLabel = new JLabel("Saldıran: " + attackerName);
        attackerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(attackerLabel);
        
        JLabel defenderLabel = new JLabel("Savunan: " + defenderName);
        defenderLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(defenderLabel);
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Zar panelleri
        JPanel dicePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        attackPanel = new JPanel();
        attackPanel.setBorder(BorderFactory.createTitledBorder("Saldıran Zarları (" + attackerCount + ")"));
        attackPanel.setPreferredSize(new Dimension(180, 150));
        dicePanel.add(attackPanel);
        
        defensePanel = new JPanel();
        defensePanel.setBorder(BorderFactory.createTitledBorder("Savunan Zarları (" + defenderCount + ")"));
        defensePanel.setPreferredSize(new Dimension(180, 150));
        dicePanel.add(defensePanel);
        
        mainPanel.add(dicePanel, BorderLayout.CENTER);
        
        // Buton paneli
        JPanel buttonPanel = new JPanel();
        
        rollButton = new JButton("Zar At");
        rollButton.addActionListener(e -> rollDice(attackerCount, defenderCount));
        buttonPanel.add(rollButton);
        
        continueButton = new JButton("Devam Et");
        continueButton.setEnabled(false);
        continueButton.addActionListener(e -> dispose());
        buttonPanel.add(continueButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private void rollDice(int attackerCount, int defenderCount) {
        // Zarları at
        attackDice = new int[attackerCount];
        defenseDice = new int[defenderCount];
        
        for (int i = 0; i < attackerCount; i++) {
            attackDice[i] = (int)(Math.random() * 6) + 1;
        }
        
        for (int i = 0; i < defenderCount; i++) {
            defenseDice[i] = (int)(Math.random() * 6) + 1;
        }
        
        // Zarları sırala (büyükten küçüğe)
        Arrays.sort(attackDice);
        reverseArray(attackDice);
        Arrays.sort(defenseDice);
        reverseArray(defenseDice);
        
        // Zarları göster
        displayDice(attackPanel, attackDice, Color.RED);
        displayDice(defensePanel, defenseDice, Color.BLUE);
        
        // Butonları güncelle
        rollButton.setEnabled(false);
        continueButton.setEnabled(true);
    }
    
    private void displayDice(JPanel panel, int[] dice, Color color) {
        panel.removeAll();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));
        
        for (int value : dice) {
            JLabel diceLabel = new JLabel(createDiceIcon(value, color));
            panel.add(diceLabel);
        }
        
        panel.revalidate();
        panel.repaint();
    }
    
    private Icon createDiceIcon(int value, Color color) {
        // Basit bir zar ikonu oluştur
        int size = 50;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Düzgün çizim için anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Zar arka planı
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(0, 0, size - 1, size - 1, 10, 10);
        
        // Zar kenarı
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(0, 0, size - 1, size - 1, 10, 10);
        
        // Zar noktaları
        g2d.setColor(Color.BLACK);
        
        int dotSize = 8;
        int margin = 10;
        
        // Zar değerine göre noktaları çiz
        switch (value) {
            case 1:
                drawDot(g2d, size / 2, size / 2, dotSize);
                break;
            case 2:
                drawDot(g2d, margin, margin, dotSize);
                drawDot(g2d, size - margin, size - margin, dotSize);
                break;
            case 3:
                drawDot(g2d, margin, margin, dotSize);
                drawDot(g2d, size / 2, size / 2, dotSize);
                drawDot(g2d, size - margin, size - margin, dotSize);
                break;
            case 4:
                drawDot(g2d, margin, margin, dotSize);
                drawDot(g2d, margin, size - margin, dotSize);
                drawDot(g2d, size - margin, margin, dotSize);
                drawDot(g2d, size - margin, size - margin, dotSize);
                break;
            case 5:
                drawDot(g2d, margin, margin, dotSize);
                drawDot(g2d, margin, size - margin, dotSize);
                drawDot(g2d, size / 2, size / 2, dotSize);
                drawDot(g2d, size - margin, margin, dotSize);
                drawDot(g2d, size - margin, size - margin, dotSize);
                break;
            case 6:
                drawDot(g2d, margin, margin, dotSize);
                drawDot(g2d, margin, size / 2, dotSize);
                drawDot(g2d, margin, size - margin, dotSize);
                drawDot(g2d, size - margin, margin, dotSize);
                drawDot(g2d, size - margin, size / 2, dotSize);
                drawDot(g2d, size - margin, size - margin, dotSize);
                break;
        }
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    private void drawDot(Graphics2D g, int x, int y, int size) {
        g.fillOval(x - size / 2, y - size / 2, size, size);
    }
    
    private void reverseArray(int[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            int temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }
    
    public int[] getAttackDice() {
        return attackDice;
    }
    
    public int[] getDefenseDice() {
        return defenseDice;
    }
}