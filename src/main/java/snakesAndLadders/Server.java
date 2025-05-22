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
 * Server sınıfı, oyuncuların bağlanmasını sağlar ve her iki oyuncu hazır olduğunda bir GameRoom başlatarak oyunu başlatır.
 *
 * @author nesma
 */
public class Server {

    private ServerSocket serverSocket; 
    private List<Socket> waitingPlayers = new ArrayList<>(); // Bağlantı bekleyen oyuncuların socketleri
    private List<String> waitingNames = new ArrayList<>(); // Bağlantı bekleyen oyuncuların isimleri

    public static void main(String[] args) {
        new Server().start(2000); // Sunucuyu başlat
    }
    
    /// Belirtilen port üzerinden sunucuyu başlatır ve oyuncu bağlantılarını dinler
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server başlatıldı. Port: " + port);

            while (true) {
                // Yeni bir oyuncu bağlandığında socketi kabul et
                Socket client = serverSocket.accept();
                
                // Oyuncu ile iletişim için okuma ve yazma akışlarını oluştur
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter writer = new PrintWriter(client.getOutputStream(), true);

                // Oyuncudan adı al
                String name = reader.readLine();
                System.out.println("Oyuncu adı: " + name);

                // Oyuncunun ID'si, kaç oyuncu varsa ona göre atanır(0 veya 1)
                int playerId = waitingPlayers.size(); 

                // Oyuncu ID bilgisi gönderilir
                writer.println("PLAYER_ID:" + playerId);

                // Oyuncuya hoş geldin mesajı gönderilir
                writer.println("Hoş geldin, " + name + "!");

                // Sadece ilk oyuncuya "Diğer oyuncu bekleniyor..." mesajı gönder
                if (playerId == 0) {
                    writer.println("Diğer oyuncu bekleniyor...");
                }

                // Oyuncu socket'i ve adı listeye eklenir
                waitingPlayers.add(client);
                waitingNames.add(name);

                // Eğer 2 oyuncu tamamlandıysa GameRoom başlatılır
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
            e.printStackTrace(); //Bağlantı hatası olursa yazdır
        }
    }

}
