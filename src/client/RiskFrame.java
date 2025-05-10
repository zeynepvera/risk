/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RiskFrame extends JFrame {
    private JLayeredPane layeredPane;
    private JPanel mapPanel;
    private JLabel background;
    private String selectedSource, selectedTarget;
    private JButton btnSaldir;
    private GameClient client; // var sayalım

    public RiskFrame() {
        super("Risk Oyunu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initComponents();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        // 1) Katmanlı yapı
        layeredPane = getLayeredPane();
        layeredPane.setPreferredSize(new Dimension(900, 500));

        // 2) Arka plan harita
        ImageIcon mapIcon = new ImageIcon(getClass().getResource("/image/risk_map.png"));
        background = new JLabel(mapIcon);
        background.setBounds(0, 0, mapIcon.getIconWidth(), mapIcon.getIconHeight());
        layeredPane.add(background, JLayeredPane.DEFAULT_LAYER);

        // 3) Üst panel (butonlar için)
        mapPanel = new JPanel(null);
        mapPanel.setOpaque(false);
        mapPanel.setBounds(0, 0, mapIcon.getIconWidth(), mapIcon.getIconHeight());
        layeredPane.add(mapPanel, JLayeredPane.PALETTE_LAYER);

        // 4) Bölge butonlarını ekle
        addRegionButton("Alaska", 20, 40, 80, 40);
        addRegionButton("NorthWestTerritory", 120, 40, 100, 40);
        // ... tüm bölgeler için devam et

        // 5) Saldır butonu
        btnSaldir = new JButton("Saldır");
        btnSaldir.setBounds(380, 460, 100, 30);
        btnSaldir.addActionListener(e -> doAttack());
        layeredPane.add(btnSaldir, JLayeredPane.MODAL_LAYER);
    }

    private void addRegionButton(String name, int x, int y, int w, int h) {
        JButton b = new JButton(name);
        b.setActionCommand(name);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(true);
        b.setBounds(x, y, w, h);
        b.addActionListener(e -> {
            if (selectedSource == null) {
                selectedSource = name;
                b.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
            } else {
                selectedTarget = name;
                b.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            }
        });
        mapPanel.add(b);
    }

    private void doAttack() {
        if (selectedSource != null && selectedTarget != null && client != null) {
            client.sendMessage("ATTACK#" + selectedSource + "," + selectedTarget);
            // Durumu güncelle, resetle vs.
            selectedSource = null;
            selectedTarget = null;
        } else {
            JOptionPane.showMessageDialog(this, "Önce kaynak ve hedef seçin.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RiskFrame::new);
    }
}
