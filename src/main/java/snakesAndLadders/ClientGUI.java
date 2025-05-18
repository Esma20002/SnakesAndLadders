package snakesAndLadders;

//import .*;
import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/**
 * AA
 *
 * @author nesma
 */
public class ClientGUI extends javax.swing.JFrame {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JLabel[] players = new JLabel[2];
    private Point[] boardPositions = new Point[101];
    private int myPlayerId = -1;

    public ClientGUI() {
        initComponents();
        setResizable(false);  // Pencerenin boyutu değiştirilemesin

        btnRoll.addActionListener(e -> sendRollCommand());
        connectToServer();
        

        btnRestartt.addActionListener(e -> {
            System.out.println("Butona basıldı!");
            out.println("restart_request");
            btnRestartt.setEnabled(true); // tekrar tıklanmasın, onay bekleniyor zaten
        });

        startListeningFromServer();
        btnRoll.setEnabled(false);

        ImageIcon boardImage = new ImageIcon(getClass().getResource("/images/board.png"));
        lbl_game.setIcon(boardImage);
        lbl_game.setSize(boardImage.getIconWidth(), boardImage.getIconHeight());
        lbl_game.setLayout(null);

        initializeBoardPositions();
    }

    private void initializeBoardPositions() {
        int cellSize = lbl_game.getWidth() / 10;
        int startY = lbl_game.getHeight() - cellSize;
        int index = 1;
        boolean leftToRight = true;

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int x = leftToRight ? col * cellSize : (9 - col) * cellSize;
                int y = startY - (row * cellSize);
                boardPositions[index++] = new Point(x + cellSize / 4, y + cellSize / 4);
            }
            leftToRight = !leftToRight;
        }
    }

    private void sendRollCommand() {
        out.println("roll");
        btnRoll.setEnabled(false);
    }

    private void startListeningFromServer() {
        Thread listener = new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
//                    txtMessages.append("Sunucu: " + serverMessage + "\n");

                    if (!serverMessage.startsWith("PLAYER_MOVE") && !serverMessage.startsWith("PLAYER_ID") && !serverMessage.startsWith("OYUN_YENIDEN_BASLADI") && !serverMessage.startsWith("RESTART_ONAY_ISTEGI") && !serverMessage.startsWith("DIGER_OYUNCU_CIKTI")) {
                        txtMessages.append("Sunucu: " + serverMessage + "\n");
                    }

                    txtMessages.setCaretPosition(txtMessages.getDocument().getLength());

                    if (serverMessage.equals("DIGER_OYUNCU_CIKTI")) {
                        JOptionPane.showMessageDialog(this, "Diğer oyuncu oyundan ayrıldı. Oyun sona erdi.");
                        btnRoll.setEnabled(false);
                        btnRestartt.setEnabled(false);

                        // Ekranı kapat
                        dispose(); // pencereyi kapat
                        System.exit(0); // programı tamamen sonlandır
                        break;
                    }

                    if (serverMessage.equals("RESTART_ONAY_ISTEGI")) {
                        int choice = JOptionPane.showConfirmDialog(this,
                                "Diğer oyuncu oyunu yeniden başlatmak istiyor. Onaylıyor musunuz?",
                                "Yeniden Başlatma İsteği",
                                JOptionPane.YES_NO_OPTION);

                        if (choice == JOptionPane.YES_OPTION) {
                            out.println("restart_request");
                        } else {
                            txtMessages.append("Yeniden başlatma isteği reddedildi.\n");
                            out.println("restart_rejected"); // 🟠 RED cevabını sunucuya bildir

                            // ❗ Karşı taraf red verdiği için, isteği gönderen oyuncunun butonu tekrar aktif edilmeli
                            btnRestartt.setEnabled(true);  // tekrar yeniden başlatmak isteyebilsin
                        }
//                        } else {
//                            txtMessages.append("Yeniden başlatma isteği reddedildi.\n");
//                        }
                    }
                    if (serverMessage.equals("OYUN_YENIDEN_BASLADI")) {
                        txtMessages.append("🔄 Oyun yeniden başlatıldı!\n");
                        players[0].setLocation(-100, -100);
                        players[1].setLocation(-100, -100);
                        btnRestartt.setEnabled(true);
                    }

                    if (serverMessage.startsWith("PLAYER_ID:")) {
                        myPlayerId = Integer.parseInt(serverMessage.split(":")[1]);
                        int otherPlayerId = (myPlayerId + 1) % 2;

                        // Sabit renkler
                        players[0] = new JLabel(new ImageIcon(getClass().getResource("/images/turuncu.png")));

                        players[1] = new JLabel(new ImageIcon(getClass().getResource("/images/siyah.png")));

                        for (JLabel player : players) {
                            player.setSize(20, 20);
                            player.setLocation(-100, -100);
                            lbl_game.add(player);
                        }
                        lbl_game.repaint();
                    }

                    if (serverMessage.equals("Sıra sende!")) {
                        btnRoll.setEnabled(true);
                    } else if (serverMessage.equals("Bekle...")) {
                        btnRoll.setEnabled(false);
                    }

                    if (serverMessage.startsWith("PLAYER_MOVE:")) {
                        String[] parts = serverMessage.split(":");
                        int playerId = Integer.parseInt(parts[1]);
                        int pos = Integer.parseInt(parts[2]);

                        if (pos == 0) {
                            players[playerId].setLocation(-100, -100);
                        } else {
                            // players[playerId].setLocation(boardPositions[pos]);
                            Point basePos = boardPositions[pos];
                            int offsetX = (playerId == 1) ? 10 : 0;  // sadece oyuncu 1 sağa kayar
                            int offsetY = (playerId == 1) ? 10 : 0;  // istersen aşağı da kaydır

                            players[playerId].setLocation(basePos.x + offsetX, basePos.y + offsetY);
                        }
                    }
                    if (serverMessage.contains("🏆")) {
                        JOptionPane.showMessageDialog(this,
                                serverMessage,
                                "Oyun Bitti", JOptionPane.INFORMATION_MESSAGE);

                        btnRoll.setEnabled(false);
                        btnRestartt.setEnabled(true); // İsteğe bağlı: yeniden başlatmaya izin ver
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
            socket = new Socket("localhost", 2000);//16.171.148.64(localhost)
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
            out.println(name);
        }
    }
//    private Socket socket;
//    private PrintWriter out;
//    private BufferedReader in;
//    private JLabel player1;
//    private JLabel player2;
//    private Point[] boardPositions = new Point[101]; // 1-100 için koordinat
//    private int myPlayerId = -1; // oyuncu kimliği (0 mı 1 mi?)
//
//    /**
//     * Creates new form ClientGUI
//     */
//    public ClientGUI() {
//        initComponents();
//        btnRoll.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                sendRollCommand();
//            }
//        });
//
//        connectToServer();
//        startListeningFromServer();
//        btnRoll.setEnabled(false);
//        ImageIcon boardImage = new ImageIcon(getClass().getResource("/images/board.png"));
//        lbl_game.setIcon(boardImage);
//        lbl_game.setSize(boardImage.getIconWidth(), boardImage.getIconHeight());
//        lbl_game.setLayout(null); // taşların yerleştirilebilmesi için serbest konum
//
//// Matris koordinatlarını oluştur
//        initializeBoardPositions();
//
//// Piyonları oluştur
//        player1 = new JLabel(new ImageIcon(getClass().getResource("/images/beyaz.png")));
//        player1.setSize(20, 20);
//        lbl_game.add(player1);
//
//        player2 = new JLabel(new ImageIcon(getClass().getResource("/images/siyah.png")));
//        player2.setSize(20, 20);
//        lbl_game.add(player2);
//        player1.setLocation(-100, -100); // görünmeyen bir konum
//        player2.setLocation(-100, -100);
//
//        int boardWidth = lbl_game.getWidth();  // örn: 600
//        int boardHeight = lbl_game.getHeight(); // örn: 600
//        int cellSize = boardWidth / 10; // 600px / 10 = 60px gibi
//
//    }
//
//    private void initializeBoardPositions() {
//        boardPositions = new Point[101]; // 1–100
//
//        int cellSize = lbl_game.getWidth() / 10;
//        int startY = lbl_game.getHeight() - cellSize; // alt satır
//        int index = 1;
//        boolean leftToRight = true;
//
//        for (int row = 0; row < 10; row++) {
//            for (int col = 0; col < 10; col++) {
//                int x = leftToRight ? col * cellSize : (9 - col) * cellSize;
//                int y = startY - (row * cellSize);
//                boardPositions[index++] = new Point(x + cellSize / 4, y + cellSize / 4); // taşı ortaya yerleştirmek için ufak offset
//            }
//            leftToRight = !leftToRight;
//        }
//    }
//
//    private void sendRollCommand() {
//        out.println("roll"); // Zar atma isteğini sunucuya gönder
//        btnRoll.setEnabled(false); // Kullanıcı tekrar basamasın
//    }
//
//    // Sunucudan gelen mesajları dinleyecek thread
//    private void startListeningFromServer() {
//        Thread listener = new Thread(() -> {
//            try {
//                String serverMessage;
//                while ((serverMessage = in.readLine()) != null) {
//                    txtMessages.append("Sunucu: " + serverMessage + "\n");
//
//                    // 🔽 Oyuncu ID bilgisi varsa ayarla
//                    if (serverMessage.contains("Oyuncu 1")) {
//                        myPlayerId = 0;
//                    } else if (serverMessage.contains("Oyuncu 2")) {
//                        myPlayerId = 1;
//                    }
//
//                    if (serverMessage.equals("Sıra sende!")) {
//                        btnRoll.setEnabled(true);  // Sıra oyuncudaysa zar atabilir
//                    } else if (serverMessage.equals("Bekle...")) {
//                        btnRoll.setEnabled(false); // Sıra karşı oyuncudaysa buton kilitli
//                    }
//
//                    if (serverMessage.contains("yeni pozisyon:")) {
//                        // Örnek: "Ali zar attı: 4, yeni pozisyon: 25"
//                        try {
//                            String[] parts = serverMessage.split("yeni pozisyon:");
//                            int position = Integer.parseInt(parts[1].trim());
//
//                            if (serverMessage.contains(txtName.getText())) {
////if (serverMessage.contains("Oyuncu " + (myPlayerId + 1))) {
//                                if (position == 0) {
//                                    player1.setLocation(-100, -100); // görünmesin
//                                } else {
//                                    player1.setLocation(boardPositions[position]);
//                                }
//                            } else {
//                                if (position == 0) {
//                                    player2.setLocation(-100, -100);
//                                } else {
//                                    player2.setLocation(boardPositions[position]);
//                                }
//                            }
//
//                        } catch (Exception ex) {
//                            txtMessages.append("Pozisyon ayrıştırılamadı.\n");
//                        }
//                    }
//
//                }
//            } catch (IOException e) {
//                txtMessages.append("Bağlantı koptu: " + e.getMessage() + "\n");
//            }
//        });
//        listener.start();
//    }
//
//    private void connectToServer() {
//        try {
//            socket = new Socket("localhost", 2000); // veya AWS IP
//            out = new PrintWriter(socket.getOutputStream(), true);
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        } catch (IOException e) {
//            txtMessages.append("Sunucuya bağlanılamadı.\n");
//            btnSend.setEnabled(false);
//        }
//    }
//
//    private void sendNameToServer() {
//        String name = txtName.getText().trim();
//        if (!name.isEmpty()) {
//            out.println(name); // sadece gönderiyoruz, cevabı arka plan thread'i dinliyor
//        }
//    }

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
        btnRestartt = new javax.swing.JButton();

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

        btnRestartt.setText("Yeniden Başlat");

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnRoll)
                        .addGap(248, 248, 248))
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(panel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(46, 46, 46)
                                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addComponent(btnSend)
                                .addGap(26, 26, 26)
                                .addComponent(btnRestartt, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(45, 45, 45))
                            .addComponent(lbl_game, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(56, Short.MAX_VALUE))))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(btnSend)
                    .addComponent(btnRestartt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_game, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnRoll)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(54, 54, 54))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
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
    private javax.swing.JButton btnRestartt;
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
