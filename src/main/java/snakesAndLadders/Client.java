package snakesAndLadders;

//import .*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
/**
 * İstemci (Client) sınıfı, sunucuya bağlanmak ve mesaj alışverişi yapmak için kullanılır.
 * Bu sınıf, oyun istemcisinin sunucuya bağlanmasını, mesaj göndermesini ve mesaj almasını sağlar.
 * 
 * Bağlantı işlemi: connect()
 * Veri gönderme işlemi: send()
 * Veri alma işlemi: receive()
 *
 * @author nesma
 */
public class Client {

    private Socket socket;     // Sunucu ile bağlantı kurmak için soket
    private PrintWriter out;   // Sunucuya mesaj göndermek için çıktı akışı
    private BufferedReader in; // Sunucudan mesaj almak için giriş akışı

    //Sunucuya bağlanmayı dener
    public boolean connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);  // Otomatik flush'lı çıktı
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Giriş akışı
            return true; // Bağlantı başarılı
        } catch (IOException e) {
            return false;
        }
    }
    
    //Sunucuya mesaj gönderir
    public void send(String message) {
        if (out != null) {
            out.println(message); // Mesajı sunucuya gönder
        }
    }
    
    //Sunucudan gelen mesajı alır
    public String receive() {
        try {
            return in.readLine(); // Bir satır veri oku
        } catch (IOException e) {
            return null;
        }
    }
}
