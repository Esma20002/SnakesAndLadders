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
    private int[] positions = new int[2]; // Oyuncu 0 ve 1'in pozisyonları (1–100)
    private final Map<Integer, Integer> snakes = new HashMap<>();
    private final Map<Integer, Integer> ladders = new HashMap<>();
    private final Random random = new Random();

    public GameManager() {
        positions[0] = 1; // Oyuncu 1 başlangıçta 1. karede
        positions[1] = 1; // Oyuncu 2 de aynı şekilde

        // Örnek yılanlar
        snakes.put(99, 21);
        snakes.put(76, 32);

        // Örnek merdivenler
        ladders.put(3, 22);
        ladders.put(15, 44);
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
}

