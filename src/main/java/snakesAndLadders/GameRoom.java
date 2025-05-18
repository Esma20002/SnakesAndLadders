/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package snakesAndLadders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.System.in;
import java.net.Socket;
import java.util.Random;

/**
 *
 * @author nesma
 */
public class GameRoom {

    private Socket[] clients = new Socket[2];
    private PrintWriter[] writers = new PrintWriter[2];
    private BufferedReader[] readers = new BufferedReader[2];
    private String[] playerNames = new String[2];
    private GameManager gameManager = new GameManager();
    private boolean[] restartVotes = new boolean[2];
    private int currentPlayer = 0;

    public GameRoom(Socket client1, Socket client2) {
        clients[0] = client1;
        clients[1] = client2;
    }

    public void startGame() {
        for (int i = 0; i < 2; i++) {
            int playerId = i;
            new Thread(() -> handleClient(playerId)).start();
        }
    }

    private void handleClient(int playerId) {
        try {
            readers[playerId] = new BufferedReader(new InputStreamReader(clients[playerId].getInputStream()));
            writers[playerId] = new PrintWriter(clients[playerId].getOutputStream(), true);

            writers[playerId].println("PLAYER_ID:" + playerId);
            String name = readers[playerId].readLine();
            playerNames[playerId] = name;

            writers[playerId].println("Hoş geldinn, " + name + "!");
            if (playerNames[0] != null && playerNames[1] != null) {
                
                broadcast("Oyun başladı! " + playerNames[0] + " vs " + playerNames[1]);
                sendTo(0, "Sıra sende!");
                sendTo(1, "Bekle...");
            }
            
            
 


            while (true) {
                String message = readers[playerId].readLine();
                //String message = in.readLine();

                if (message == null) {
                    System.out.println(">> Oyuncu çıkışı algılandı ve diğerine haber verildi.");
                    int other = (playerId + 1) % 2;
                    sendTo(other, "DIGER_OYUNCU_CIKTI");
                    break;
                }
                if (message == null) {
                    System.out.println("Oyuncu " + (playerId + 1) + " bağlantıyı kapattı.");

                    int other = (playerId + 1) % 2;

                    // Diğer oyuncuya haber ver
                    try {
                        sendTo(other, "DIGER_OYUNCU_CIKTI");
                    } catch (Exception e) {
                        System.out.println("Diğer oyuncuya mesaj gönderilemedi.");
                    }

                    // Her iki oyuncunun bağlantılarını kapat
                    try {
                        readers[playerId].close();
                        writers[playerId].close();
                        clients[playerId].close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    try {
                        readers[other].close();
                        writers[other].close();
                        clients[other].close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    break;
                }

                if (message.equals("roll") && playerId == currentPlayer) {
                    int dice = new Random().nextInt(6) + 1;
                    int newPos = gameManager.movePlayer(playerId, dice);
                    broadcast(playerNames[playerId] + " zar attı: " + dice + ", yeni pozisyon: " + newPos);
                    broadcast("PLAYER_MOVE:" + playerId + ":" + newPos);
                    System.out.println("PLAYER_MOVE:" + playerId + ":" + newPos);
                    
                    

                    if (gameManager.hasPlayerWon(playerId)) {
                        broadcast("🏆 " + playerNames[playerId] + " oyunu kazandı!");
                        continue;
                    }

                    currentPlayer = (currentPlayer + 1) % 2;
                    sendTo(currentPlayer, "Sıra sende!");
                    sendTo((currentPlayer + 1) % 2, "Bekle...");
                }

                if (message.equals("restart_request")) {
                    restartVotes[playerId] = true;

                    int other = (playerId + 1) % 2;

                    if (restartVotes[0] && restartVotes[1]) {
                        gameManager.resetGame();
                        broadcast("OYUN_YENIDEN_BASLADI");
                        broadcast("PLAYER_MOVE:0:0");
                        broadcast("PLAYER_MOVE:1:0");
                        System.out.println("OYUN_YENIDEN_BASLADI");
                        System.out.println("PLAYER_MOVE:0:0");
                        System.out.println("PLAYER_MOVE:1:0");
                        
                        
                        
                        currentPlayer = 0;
                        sendTo(0, "Sıra sende!");
                        sendTo(1, "Bekle...");
                        restartVotes[0] = restartVotes[1] = false;
                    } else {
                        sendTo(other, "RESTART_ONAY_ISTEGI");
                    }
                }

                if (message.equals("restart_rejected")) {
                    restartVotes[0] = restartVotes[1] = false; // red geldiyse sıfırla
                    broadcast("Yeniden başlatma isteği reddedildi.");
                    
                }

            }
          } catch (IOException e) {
    System.out.println("Bir oyuncunun bağlantısı kesildi: " + e.getMessage());

    int other = (playerId + 1) % 2;
    if (writers[other] != null) {
        try {
            writers[other].println("DIGER_OYUNCU_CIKTI");
            System.out.println(">> Oyuncu çıkışı algılandı ve diğerine haber verildi.");
        } catch (Exception ex) {
            System.out.println(">> Diğer oyuncuya mesaj iletilemedi: " + ex.getMessage());
        }
    }

    // Bağlantıları güvenli şekilde kapat
    try { clients[playerId].close(); } catch (Exception ignore) {}
    try { clients[other].close(); } catch (Exception ignore) {}
}

//        } catch (IOException e) {
//            e.printStackTrace();
//
//        }
    }

    private void broadcast(String msg) {
        for (PrintWriter writer : writers) {
            writer.println(msg);
        }
    }
    
//    private void broadcast(String msg) {
//    // Eğer mesaj teknikse (ham veri gibi), sadece logla
//    if (msg.startsWith("PLAYER_MOVE") || msg.startsWith("PLAYER_ID")) {
//        System.out.println("Sunucu logu (gizli mesaj): " + msg);
//        return;
//    }
//
//    // Aksi halde tüm oyunculara gönder
//    for (PrintWriter writer : writers) {
//        writer.println(msg);
//    }
//}


    private void sendTo(int id, String msg) {
        writers[id].println(msg);
    }
}
