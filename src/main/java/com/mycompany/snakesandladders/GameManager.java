/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.mycompany.snakesandladders;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author nesma
 */
public class GameManager {

    private int[] positions = new int[]{0, 0}; // Oyuncular tahtanın dışında başlar
    //private int[] positions = new int[2]; // Oyuncu 0 ve 1'in pozisyonları (1–100)
    private final Map<Integer, Integer> snakes = new HashMap<>();
    private final Map<Integer, Integer> ladders = new HashMap<>();
    private final Random random = new Random();

    public GameManager() {

        // Örnek yılanlar
        snakes.put(29, 9);
        snakes.put(38, 15);
        snakes.put(47, 5);
        snakes.put(53, 33);
        snakes.put(62, 37);
        snakes.put(86, 54);
        snakes.put(92, 70);
        snakes.put(97, 25);

        // Örnek merdivenler
        ladders.put(2, 23);
        ladders.put(8, 34);
        ladders.put(20, 77);
        ladders.put(32, 68);
        ladders.put(41, 79);
        ladders.put(74, 88);
        ladders.put(85, 95);
        ladders.put(82, 100);
    }

    // Zar at ve yeni pozisyonu hesapla
    public int rollDice() {
        return random.nextInt(6) + 1;
    }

    // Oyuncunun pozisyonunu güncelle
    public int movePlayer(int playerId, int dice) {
        int newPos = positions[playerId] + dice;

        if (newPos > 100) {
            newPos = 100; // 100'ü aşamaz
        }

        // Yılan varsa düşür
        if (snakes.containsKey(newPos)) {
            newPos = snakes.get(newPos);
        }

        // Merdiven varsa çıkar
        if (ladders.containsKey(newPos)) {
            newPos = ladders.get(newPos);
        }

        positions[playerId] = newPos;
        return newPos;
    }

    public int getPlayerPosition(int playerId) {
        return positions[playerId];
    }

    public boolean hasPlayerWon(int playerId) {
        return positions[playerId] == 100;
    }

    public void resetGame() {
        positions[0] = 0;
        positions[1] = 0;
    }

}
