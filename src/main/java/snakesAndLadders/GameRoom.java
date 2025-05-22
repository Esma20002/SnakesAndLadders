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
 * GameRoom sınıfı, iki oyuncunun bağlandığı oyun odasında
 * oyuncular arası mesajlaşmayı, zar atmayı, oyunun yeniden başlatılmasını ve oyun sırasını yönetir. 
 *
 * @author nesma
 */
public class GameRoom {

    private Socket[] clients = new Socket[2]; // Her oyuncuya ait socket bağlantıları
    private PrintWriter[] writers = new PrintWriter[2]; // Her oyuncuya ait yazıcı (mesaj gönderici)
    private BufferedReader[] readers = new BufferedReader[2]; // Her oyuncuya ait okuyucu (mesaj alıcı)
    private String[] playerNames = new String[2]; // Oyuncu adlarını tutar
    private GameManager gameManager = new GameManager(); // Oyun kurallarını yöneten sınıf
    private boolean[] restartVotes = new boolean[2]; // Oyunu yeniden başlatmak isteyen oyuncuları takip eder
    private int currentPlayer = 0; // Sırası gelen oyuncunun ID’si (0 veya 1)
    private int requestedRestartPlayer = -1; // Yeniden başlatma isteğini ilk gönderen oyuncunun ID’si

    
    // Oyuncuların socket bağlantıları ve adları alınır
    public GameRoom(Socket client1, String name1, Socket client2, String name2) {
        clients[0] = client1;
        clients[1] = client2;
        playerNames[0] = name1;
        playerNames[1] = name2;
    }
    
    // Her oyuncu için ayrı bir thread başlatılır
    public void startGame() {
        for (int i = 0; i < 2; i++) {
            int playerId = i;
            new Thread(() -> handleClient(playerId)).start();
        }
    }
    
    // Belirli bir oyuncunun bağlantısını ve eylemlerini yönetir
    private void handleClient(int playerId) {
        try {
            // Giriş ve çıkışları tanımlanır
            readers[playerId] = new BufferedReader(new InputStreamReader(clients[playerId].getInputStream()));
            writers[playerId] = new PrintWriter(clients[playerId].getOutputStream(), true);
            
            writers[playerId].println("PLAYER_ID:" + playerId); // Oyuncuya ID bilgisi gönderilir
            
            // Her iki oyuncu da bağlandıysa oyun başlasın
            if (playerNames[0] != null && playerNames[1] != null) {

                broadcast("Oyun başladı! " + playerNames[0] + " vs " + playerNames[1]);
                sendTo(0, "Sıra sende!");
                sendTo(1, "Bekle...");
            }
            
            // Oyuncudan gelen mesajları sürekli dinle
            while (true) {
                String message = readers[playerId].readLine();

                if (message == null) { // Bağlantı koparsa
                    System.out.println(">> Oyuncu çıkışı algılandı ve diğerine haber verildi.");
                    int other = (playerId + 1) % 2;
                    sendTo(other, "DIGER_OYUNCU_CIKTI");
                    break;
                }
                
                // Zar atma işlemi
                if (message.equals("roll") && playerId == currentPlayer) {
                    int dice = new Random().nextInt(6) + 1;
                    int newPos = gameManager.movePlayer(playerId, dice);
                    broadcast(playerNames[playerId] + " zar attı: " + dice + ", yeni pozisyon: " + newPos);
                    broadcast("PLAYER_MOVE:" + playerId + ":" + newPos);
                    System.out.println("PLAYER_MOVE:" + playerId + ":" + newPos);
                    
                    // Kazanma durumu kontrolü
                    if (gameManager.hasPlayerWon(playerId)) {
                        broadcast("🏆 " + playerNames[playerId] + " oyunu kazandı!");
                        continue;
                    }
                    // Sırayı değiştir
                    currentPlayer = (currentPlayer + 1) % 2;
                    sendTo(currentPlayer, "Sıra sende!");
                    sendTo((currentPlayer + 1) % 2, "Bekle...");
                }
                
                // Yeniden başlatma isteği geldiğinde
                if (message.equals("restart_request")) {
                    restartVotes[playerId] = true;

                    //  İlk isteyen oyuncuyu kaydet
                    if (requestedRestartPlayer == -1) {
                        requestedRestartPlayer = playerId;
                    }

                    int other = (playerId + 1) % 2;
                    
                    // Her iki oyuncu onayladıysa oyunu başlat
                    if (restartVotes[0] && restartVotes[1]) {
                        gameManager.resetGame();
                        broadcast("OYUN_YENIDEN_BASLADI");
                        broadcast("PLAYER_MOVE:0:0");
                        broadcast("PLAYER_MOVE:1:0");
                        System.out.println("OYUN_YENIDEN_BASLADI");
                        System.out.println("PLAYER_MOVE:0:0");
                        System.out.println("PLAYER_MOVE:1:0");

                        // Oyun, yeniden başlatmayı isteyen oyuncudan başlasın
                        currentPlayer = requestedRestartPlayer;

                        sendTo(currentPlayer, "Sıra sende!");
                        sendTo((currentPlayer + 1) % 2, "Bekle...");

                        // Oyun yeniden başlatma bilgilerini sıfırla
                        restartVotes[0] = restartVotes[1] = false;
                        requestedRestartPlayer = -1;

                    } else {
                        sendTo(other, "RESTART_ONAY_ISTEGI"); // Diğer oyuncudan onay iste
                    }
                }
                
                // Yeniden başlatma reddedildiyse sıfırla
                if (message.equals("restart_rejected")) {
                    restartVotes[0] = restartVotes[1] = false; 
                    requestedRestartPlayer = -1; 
                    broadcast("Yeniden başlatma isteği reddedildi.");

                }

            }
        } catch (IOException e) {
            System.out.println("Bir oyuncunun bağlantısı kesildi: " + e.getMessage());

            int other = (playerId + 1) % 2;
            
            // Diğer oyuncuya bağlantı kesildi mesajı gönder
            if (writers[other] != null) {
                try {
                    writers[other].println("DIGER_OYUNCU_CIKTI");
                    System.out.println(">> Oyuncu çıkışı algılandı ve diğerine haber verildi.");
                } catch (Exception ex) {
                    System.out.println(">> Diğer oyuncuya mesaj iletilemedi: " + ex.getMessage());
                }
            }

            // Bağlantıları güvenli şekilde kapat
            try {
                clients[playerId].close();
            } catch (Exception ignore) {
            }
            try {
                clients[other].close();
            } catch (Exception ignore) {
            }
        }

    }
    
    // Her iki oyuncuya da mesaj gönderir
    private void broadcast(String msg) {
        for (PrintWriter writer : writers) {
            writer.println(msg);
        }
    }

    // Sadece belirli bir oyuncuya mesaj gönderir
    private void sendTo(int id, String msg) {
        writers[id].println(msg);
    }
}
