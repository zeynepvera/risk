package client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIComponentFactory {
    
    /**
     * Stilize edilmiş buton oluşturur
     */
    public static JButton createStylishButton(String text, int width, int height, 
                                            Color baseColor, Color hoverColor) {
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
        
        return button;
    }
    
    /**
     * Gradient buton oluşturur
     */
    public static JButton createGradientButton(String text, Color base) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, base.brighter(), 0, getHeight(), base.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        return button;
    }
    
    /**
     * Menü butonu oluşturur
     */
    public static JButton createMenuButton(String text, int width, int height) {
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
     * Stilize edilmiş text field oluşturur
     */
    public static JTextField createStyledTextField(String initialText, String tooltip) {
        JTextField field = new JTextField(initialText, 15);
        field.setToolTipText(tooltip);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(null);  // En basit çözüm - border'ı kaldırdık
        return field;
    }
}