package com.mycompany.snakesandladders;

//import .*;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
/**
 *
 * @author nesma
 */
public class Server2 {

    private ServerSocket serverSocket;
    private final List<Socket> clients = new ArrayList<>();
    private final List<PrintWriter> writers = new ArrayList<>();
    private final List<String> playerNames = new ArrayList<>();
    private int currentPlayer = 0; // sıra 0. oyuncudan başlasın
    private List<BufferedReader> readers = new ArrayList<>();
    private GameManager gameManager = new GameManager();//
    private boolean[] restartVotes = new boolean[2];

    private void handleClient(Socket client, PrintWriter out, int playerId) {
        try (
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));) {
            out.println("PLAYER_ID:" + playerId);

            String name = in.readLine(); // oyuncu adı geldiğinde ekle
            synchronized (playerNames) {
                playerNames.add(name);
            }

            System.out.println("Oyuncu " + (playerId + 1) + " bağlandı: " + name);
            out.println("Hoş geldin, " + name + "!");

            // 🔽 TAM BURAYA KOYACAKSIN:
            if (playerNames.size() == 2 && playerId == 1) {
                broadcast("Oyun başladı! Oyuncular: " + playerNames.get(0) + " vs " + playerNames.get(1));
                sendTo(0, "Sıra sende!");
                sendTo(1, "Bekle...");
            }

            if (playerId == 0) {
                out.println("2. oyuncu bekleniyor...");
                // hem 2. bağlantı hem de 2 isim gelmeden çıkma
                while (clients.size() < 2 || playerNames.size() < 2) {
                    Thread.sleep(500);
                }
            }

            while (true) {
                String message = in.readLine();
                if (message == null) {
                    break;
                }

                System.out.println("Gelen mesaj: " + message);

                if (message.equals("restart_request")) {
                    restartVotes[playerId] = true;
                    broadcast("OYUNCU " + playerNames.get(playerId) + " yeniden başlatmak istiyor.");

                    if (restartVotes[0] && restartVotes[1]) {
                        gameManager.resetGame(); // Bunu GameManager sınıfına ekleyeceğiz
                        broadcast("OYUN_YENIDEN_BASLADI");
                        broadcast("PLAYER_MOVE:0:0");
                        broadcast("PLAYER_MOVE:1:0");
                        currentPlayer = 0;
                        sendTo(0, "Sıra sende!");
                        sendTo(1, "Bekle...");
                        restartVotes[0] = restartVotes[1] = false;
                    } else {
                        int other = (playerId + 1) % 2;
                        sendTo(other, "RESTART_ONAY_ISTEGI");
                    }
                }

                // Eğer oyun başlamamışsa roll mesajını kabul etme
                if (playerNames.size() < 2) {
                    out.println("Oyun henüz başlamadı. Lütfen bekleyin...");
                    continue;
                }

                if (message.equals("roll") && playerId == currentPlayer) {
                    int dice = new Random().nextInt(6) + 1;
                    int newPos = gameManager.movePlayer(playerId, dice);

                    broadcast(playerNames.get(playerId) + " zar attı: " + dice + ", yeni pozisyon: " + newPos);
                    broadcast("PLAYER_MOVE:" + playerId + ":" + newPos);

                    // Kazanma kontrolü
                    if (gameManager.hasPlayerWon(playerId)) {
                        broadcast("🏆 " + playerNames.get(playerId) + " oyunu kazandı!");
                        // Oyun bitti ama istemci bağlantısı açık kalsın
                        // Kullanıcılar restart talebi gönderebilir
                        continue; // döngüde kal, sadece yeni "roll" komutlarını işlemez
                    }

                    if (!message.equals("restart_request") && playerNames.size() == 2 && playerId != currentPlayer) {
                        continue; // sadece restart_request'e izin ver, diğer komutları yoksay
                    }

                    currentPlayer = (currentPlayer + 1) % 2;
                    sendTo(currentPlayer, "Sıra sende!");
                    sendTo((currentPlayer + 1) % 2, "Bekle...");
                }

            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Bağlantı hatası: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server2 server = new Server2();
        server.start(2000);
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server başlatıldı. Port: " + port);

            while (clients.size() < 2) {
                Socket client = serverSocket.accept();
                clients.add(client);

                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                writers.add(out);

                int playerId = clients.size() - 1;
                System.out.println("Yeni bağlantı: Oyuncu " + (playerId + 1) + " - " + client.getInetAddress());

                new Thread(() -> handleClient(client, out, playerId)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcast(String message) {
        for (PrintWriter writer : writers) {
            writer.println(message);
        }
    }

    private void sendTo(int id, String message) {
        writers.get(id).println(message);
    }

}
