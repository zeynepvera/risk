/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package client;
import client.GameClient;

public class MainFrame extends javax.swing.JFrame implements GameClient.GameClientListener {

    public MainFrame() {
        initComponents();
    }
private GameClient client;


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnBaglan = new javax.swing.JButton();
        lblDurum = new javax.swing.JLabel();
        btnHarita = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtHarita = new javax.swing.JTextArea();
        txtOyuncuAdi = new javax.swing.JTextField();
        btnHazir = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnBaglan.setText("Bağlan");
        btnBaglan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBaglanActionPerformed(evt);
            }
        });

        lblDurum.setText("Bağlı Değil");

        btnHarita.setText("Haritayı Göster");
        btnHarita.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHaritaActionPerformed(evt);
            }
        });

        txtHarita.setColumns(30
        );
        txtHarita.setRows(6);
        jScrollPane1.setViewportView(txtHarita);

        jScrollPane2.setViewportView(jScrollPane1);

        txtOyuncuAdi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtOyuncuAdiActionPerformed(evt);
            }
        });

        btnHazir.setText("Hazırım");
        btnHazir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHazirActionPerformed(evt);
            }
        });

        jLabel1.setText("Oyuncu Adi:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addComponent(lblDurum)
                .addGap(44, 44, 44)
                .addComponent(btnBaglan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 256, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnHarita)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(150, 150, 150))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(117, 117, 117)
                        .addComponent(jLabel1)
                        .addGap(29, 29, 29)
                        .addComponent(txtOyuncuAdi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(187, 187, 187)
                        .addComponent(btnHazir)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(143, 143, 143)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBaglan)
                    .addComponent(lblDurum))
                .addGap(60, 60, 60)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(btnHarita)
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(txtOyuncuAdi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addComponent(btnHazir)
                .addContainerGap(111, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBaglanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBaglanActionPerformed

client = new GameClient("localhost", 6000);
client.listener = this;
lblDurum.setText("Durum: Bağlantı başarılı");

try {
    client = new GameClient("localhost", 6000);
    lblDurum.setText("Durum: Bağlantı başarılı");
} catch (Exception e) {
    lblDurum.setText("Durum: Bağlantı hatası");
    e.printStackTrace();
}


    }//GEN-LAST:event_btnBaglanActionPerformed

    private void btnHaritaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHaritaActionPerformed
String harita =
        "A - Komşular: B, C\n" +
        "B - Komşular: A, D\n" +
        "C - Komşular: A, E\n" +
        "D - Komşular: B, F\n" +
        "E - Komşular: C, F\n" +
        "F - Komşular: D, E\n";

txtHarita.setText(harita);


    }//GEN-LAST:event_btnHaritaActionPerformed

    private void txtOyuncuAdiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtOyuncuAdiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtOyuncuAdiActionPerformed

    private void btnHazirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHazirActionPerformed
String oyuncuAdi = txtOyuncuAdi.getText().trim();
if (oyuncuAdi.isEmpty()) {
    lblDurum.setText("Lütfen adınızı girin.");
    return;
}

client.sendMessage("PLAYERNAME#" + oyuncuAdi);
lblDurum.setText("Hazır oldunuz: " + oyuncuAdi);
btnHazir.setEnabled(false);
txtOyuncuAdi.setEditable(false);


    }//GEN-LAST:event_btnHazirActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBaglan;
    private javax.swing.JButton btnHarita;
    private javax.swing.JButton btnHazir;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblDurum;
    private javax.swing.JTextArea txtHarita;
    private javax.swing.JTextField txtOyuncuAdi;
    // End of variables declaration//GEN-END:variables

   @Override
public void onMapDataReceived(String mapText) {
    txtHarita.setText(mapText);
}
@Override
public void onPlayerInfoReceived(int id, String name) {
    lblDurum.setText("Oyuncu ID: " + id + " | Ad: " + name);
}


}
