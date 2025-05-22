package snakesAndLadders;

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
public class Server {

    private ServerSocket serverSocket;
    private List<Socket> waitingPlayers = new ArrayList<>();
    private List<String> waitingNames = new ArrayList<>();

    public static void main(String[] args) {
        new Server().start(2000);
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server başlatıldı. Port: " + port);

            while (true) {
//                Socket client = serverSocket.accept();
//                System.out.println("Yeni oyuncu bağlandı: " + client.getInetAddress());
//              
//                waitingPlayers.add(client);
//
//                if (waitingPlayers.size() >= 2) {
//                    Socket player1 = waitingPlayers.remove(0);
//                    Socket player2 = waitingPlayers.remove(0);
//
//                    GameRoom room = new GameRoom(player1, player2);
//                    room.startGame();
//                }
                Socket client = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter writer = new PrintWriter(client.getOutputStream(), true);

                // Oyuncudan adı al
                String name = reader.readLine();
                System.out.println("Oyuncu adı: " + name);

                // 🎯 ID belirle (0 mı, 1 mi?)
                int playerId = waitingPlayers.size(); // Eklenmeden önce kaç kişi vardı

                // ID gönder
                writer.println("PLAYER_ID:" + playerId);

                // Hoş geldin mesajı da gönder
                writer.println("Hoş geldin, " + name + "!");

                // 🔔 Sadece ilk oyuncuya "Diğer oyuncu bekleniyor..." mesajı gönder
                if (playerId == 0) {
                    writer.println("Diğer oyuncu bekleniyor...");
                }

                // Listeye ekle
                waitingPlayers.add(client);
                waitingNames.add(name);

                // Eğer 2 oyuncu tamamlandıysa GameRoom başlat
                if (waitingPlayers.size() >= 2) {
                    Socket player1 = waitingPlayers.remove(0);
                    Socket player2 = waitingPlayers.remove(0);
                    String name1 = waitingNames.remove(0);
                    String name2 = waitingNames.remove(0);

                    GameRoom room = new GameRoom(player1, name1, player2, name2);
                    room.startGame();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
