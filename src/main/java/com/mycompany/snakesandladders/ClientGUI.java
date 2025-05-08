package com.mycompany.snakesandladders;


//import .*;
import java.awt.Color;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author nesma
 */
public class ClientGUI extends javax.swing.JFrame {
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JLabel player1;
    private JLabel player2;
    private Point[] boardPositions = new Point[101]; // 1-100 için koordinat
    private int myPlayerId = -1; // oyuncu kimliği (0 mı 1 mi?)
    

    

    /**
     * Creates new form ClientGUI
     */
    public ClientGUI() {
        initComponents();
        btnRoll.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        sendRollCommand();
    }
});

        connectToServer();
        startListeningFromServer();
        btnRoll.setEnabled(false);
        ImageIcon boardImage = new ImageIcon(getClass().getResource("/images/board.png"));
        lbl_game.setIcon(boardImage);
        lbl_game.setSize(boardImage.getIconWidth(), boardImage.getIconHeight());
        lbl_game.setLayout(null); // taşların yerleştirilebilmesi için serbest konum
        

// Matris koordinatlarını oluştur
initializeBoardPositions();

// Piyonları oluştur
player1 = new JLabel(new ImageIcon(getClass().getResource("/images/beyaz.png")));
player1.setSize(20, 20);
lbl_game.add(player1);

player2 = new JLabel(new ImageIcon(getClass().getResource("/images/siyah.png")));
player2.setSize(20, 20);
lbl_game.add(player2);

// Başlangıç pozisyonları
player1.setLocation(boardPositions[1]);
player2.setLocation(boardPositions[1]);
int boardWidth = lbl_game.getWidth();  // örn: 600
int boardHeight = lbl_game.getHeight(); // örn: 600
int cellSize = boardWidth / 10; // 600px / 10 = 60px gibi


     
    }
    
    private void initializeBoardPositions() {
    boardPositions = new Point[101]; // 1–100

    int cellSize = lbl_game.getWidth() / 10;
    int startY = lbl_game.getHeight() - cellSize; // alt satır
    int index = 1;
    boolean leftToRight = true;

    for (int row = 0; row < 10; row++) {
        for (int col = 0; col < 10; col++) {
            int x = leftToRight ? col * cellSize : (9 - col) * cellSize;
            int y = startY - (row * cellSize);
            boardPositions[index++] = new Point(x + cellSize / 4, y + cellSize / 4); // taşı ortaya yerleştirmek için ufak offset
        }
        leftToRight = !leftToRight;
    }
}


    
    private void sendRollCommand() {
    out.println("roll"); // Zar atma isteğini sunucuya gönder
    btnRoll.setEnabled(false); // Kullanıcı tekrar basamasın
}

    // Sunucudan gelen mesajları dinleyecek thread
private void startListeningFromServer() {
    Thread listener = new Thread(() -> {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                txtMessages.append("Sunucu: " + serverMessage + "\n");
                
                
                // 🔽 Oyuncu ID bilgisi varsa ayarla
    if (serverMessage.contains("Oyuncu 1")) {
        myPlayerId = 0;
    } else if (serverMessage.contains("Oyuncu 2")) {
        myPlayerId = 1;
    }
                
                if (serverMessage.equals("Sıra sende!")) {
                    btnRoll.setEnabled(true);  // Sıra oyuncudaysa zar atabilir
                } else if (serverMessage.equals("Bekle...")) {
                    btnRoll.setEnabled(false); // Sıra karşı oyuncudaysa buton kilitli
                }
                
                
                if (serverMessage.contains("yeni pozisyon:")) {
    // Örnek: "Ali zar attı: 4, yeni pozisyon: 25"
    try {
        String[] parts = serverMessage.split("yeni pozisyon:");
        int position = Integer.parseInt(parts[1].trim());

        if (serverMessage.contains(txtName.getText())) {
            player1.setLocation(boardPositions[position]);
        } else {
            player2.setLocation(boardPositions[position]);
        }
    } catch (Exception ex) {
        txtMessages.append("Pozisyon ayrıştırılamadı.\n");
    }
}




            }
        } catch (IOException e) {
            txtMessages.append("Bağlantı koptu: " + e.getMessage() + "\n");
        }
    });
    listener.start();
}

    
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 2000); // veya AWS IP
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            txtMessages.append("Sunucuya bağlanılamadı.\n");
            btnSend.setEnabled(false);
        }
    }
    
    private void sendNameToServer() {
    String name = txtName.getText().trim();
    if (!name.isEmpty()) {
        out.println(name); // sadece gönderiyoruz, cevabı arka plan thread'i dinliyor
    }
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel1 = new java.awt.Panel();
        jLabel1 = new javax.swing.JLabel();
        btnSend = new javax.swing.JButton();
        txtName = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtMessages = new javax.swing.JTextArea();
        btnRoll = new javax.swing.JButton();
        lbl_game = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Adınzı Giriniz");

        btnSend.setText("Gönder");
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });

        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });

        txtMessages.setColumns(20);
        txtMessages.setRows(5);
        jScrollPane1.setViewportView(txtMessages);

        btnRoll.setText("Zar At");

        lbl_game.setText("jLabel2");

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnRoll)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addComponent(lbl_game, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(37, Short.MAX_VALUE))
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(46, 46, 46)
                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSend)
                        .addGap(50, 50, 50))))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(btnSend))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_game, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnRoll)))
                .addGap(53, 53, 53))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendActionPerformed
        sendNameToServer();
        btnSend.setEnabled(false); // sadece bir kez göndersin
        txtName.setEditable(false);
    }//GEN-LAST:event_btnSendActionPerformed

    private void txtNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNameActionPerformed

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
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ClientGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRoll;
    private javax.swing.JButton btnSend;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_game;
    private java.awt.Panel panel1;
    private javax.swing.JTextArea txtMessages;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables
}
