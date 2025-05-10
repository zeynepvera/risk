
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
        cmbKaynak = new javax.swing.JComboBox<>();
        cmbHedef = new javax.swing.JComboBox<>();
        btnSaldir = new javax.swing.JButton();

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

        cmbKaynak.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbKaynakİtemStateChanged(evt);
            }
        });
        cmbKaynak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbKaynakActionPerformed(evt);
            }
        });

        cmbHedef.setBackground(new java.awt.Color(153, 255, 153));
        cmbHedef.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbHedefActionPerformed(evt);
            }
        });

        btnSaldir.setText("Saldır");
        btnSaldir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaldirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblDurum)
                    .addComponent(jLabel1))
                .addGap(74, 74, 74)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnBaglan)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtOyuncuAdi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(64, 64, 64)
                        .addComponent(btnHazir)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 168, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cmbKaynak, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(48, 48, 48)
                        .addComponent(cmbHedef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45)
                        .addComponent(btnSaldir))
                    .addComponent(btnHarita))
                .addGap(53, 53, 53))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(txtOyuncuAdi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnHazir)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(30, 30, 30)
                .addComponent(btnHarita)
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDurum)
                    .addComponent(btnBaglan))
                .addGap(90, 90, 90)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbKaynak, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbHedef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSaldir))
                .addContainerGap(277, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBaglanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBaglanActionPerformed

      
       try {
        client = new GameClient("localhost", 6000);
        client.listener = this;
        lblDurum.setText("Durum: Bağlantı başarılı");
    } catch (Exception e) {
        lblDurum.setText("Durum: Bağlantı hatası");
        e.printStackTrace();
    }


    }//GEN-LAST:event_btnBaglanActionPerformed

    private void btnHaritaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHaritaActionPerformed
        String harita
                = "A - Komşular: B, C\n"
                + "B - Komşular: A, D\n"
                + "C - Komşular: A, E\n"
                + "D - Komşular: B, F\n"
                + "E - Komşular: C, F\n"
                + "F - Komşular: D, E\n";

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

    private void cmbHedefActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbHedefActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbHedefActionPerformed

    private void cmbKaynakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbKaynakActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbKaynakActionPerformed

    private void cmbKaynakİtemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbKaynakİtemStateChanged

        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            String kaynak = (String) cmbKaynak.getSelectedItem();
            cmbHedef.removeAllItems();
            if (kaynak.equals("A")) {
                cmbHedef.addItem("B");
                cmbHedef.addItem("C");
            } else if (kaynak.equals("B")) {
                cmbHedef.addItem("A");
                cmbHedef.addItem("D");
            } else if (kaynak.equals("C")) {
                cmbHedef.addItem("A");
                cmbHedef.addItem("E");
            } else if (kaynak.equals("D")) {
                cmbHedef.addItem("B");
                cmbHedef.addItem("F");
            } else if (kaynak.equals("E")) {
                cmbHedef.addItem("C");
                cmbHedef.addItem("F");
            } else if (kaynak.equals("F")) {
                cmbHedef.addItem("D");
                cmbHedef.addItem("E");
            }}
    }//GEN-LAST:event_cmbKaynakİtemStateChanged

    private void btnSaldirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaldirActionPerformed

        String kaynak = (String) cmbKaynak.getSelectedItem();
        String hedef = (String) cmbHedef.getSelectedItem();

        if (kaynak != null && hedef != null && client != null) {
            client.sendMessage("ATTACK#" + kaynak + "," + hedef);
            lblDurum.setText("Saldırılıyor: " + kaynak + " → " + hedef);
        }


    }//GEN-LAST:event_btnSaldirActionPerformed

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
    private javax.swing.JButton btnSaldir;
    private javax.swing.JComboBox<String> cmbHedef;
    private javax.swing.JComboBox<String> cmbKaynak;
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

        if (client != null) {
            // Haritayı al
            cmbKaynak.removeAllItems();
            cmbKaynak.addItem("A");
            cmbKaynak.addItem("B");
        }
    }

}
