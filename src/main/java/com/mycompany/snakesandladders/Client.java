/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.snakesandladders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JOptionPane;

/**
 *
 * @author nesma
 */
public class Client {
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private OyunEkrani oyunEkrani;

    public void start(String ip, int port, String oyuncuIsmi) {
        try {
            clientSocket = new Socket(ip, port);
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);

            // Oyuncu adını gönder
            writer.println(oyuncuIsmi);

            // GUI başlat
            oyunEkrani = new OyunEkrani(oyuncuIsmi);
     
            // Sunucudan gelen mesajları GUI’ye aktar
            String line;
            while ((line = reader.readLine()) != null) {
                oyunEkrani.guncelleDurum(line);
            }
            
            oyunEkrani.guncelleDurum("Bağlantı koptu.");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Sunucuya bağlanılamadı: " + e.getMessage());
        }
    }
}