/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.snakesandladders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author nesma
 */
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {
    private ServerSocket serverSocket;
    private final ArrayList<ClientHandler> clients = new ArrayList<>();
    private final ArrayList<String> playerNames = new ArrayList<>();
    private final int MAX_PLAYERS = 2;

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server başlatıldı. Oyuncular bekleniyor...");

            while (clients.size() < MAX_PLAYERS) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, clients.size());
                clients.add(handler);
                new Thread(handler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private PrintWriter writer;
        private int playerId;

        public ClientHandler(Socket socket, int id) {
            this.clientSocket = socket;
            this.playerId = id;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                // Oyuncu adını al
                String name = reader.readLine();
                if (name == null || name.trim().isEmpty()) {
                         System.out.println("Geçersiz kullanıcı ismi, bağlantı kesildi.");
                          return;
                }

                playerNames.add(name);
                System.out.println("Oyuncu " + (playerId + 1) + ": " + name + " bağlandı.");

                if (playerNames.size() == 1) {
                    writer.println("İkinci oyuncu bekleniyor...");
                    
                    
                } else if (playerNames.size() == 2) {
                    broadcast("Oyun başladı! Oyuncular: " + playerNames.get(0) + " vs " + playerNames.get(1));
                    //writer.println("heyy");
                }

            } catch (IOException e) {
                System.out.println("Oyuncu bağlantısı kesildi.");
            }
        }

        public void sendMessage(String msg) {
            writer.println(msg);
        }
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        Server s = new Server();
        s.start(2000);
    }
}
