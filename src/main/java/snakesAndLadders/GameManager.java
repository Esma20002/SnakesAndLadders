/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package snakesAndLadders;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * GameManager sınıfı, Snakes and Ladders oyununun çekirdek mantığını yönetir.
 * Oyuncuların pozisyonları, yılan ve merdiven kuralları, zar atma işlemleri burada kontrol edilir.
 *
 * @author nesma
 */
public class GameManager {

    private int[] positions = new int[]{0, 0};  // Her iki oyuncunun tahtadaki pozisyonları (0: henüz tahtada değil)
    private final Map<Integer, Integer> snakes = new HashMap<>();  // Yılanların başlangıç ve bitiş noktalarını tutar (başlangıç → düşeceği yer)
    private final Map<Integer, Integer> ladders = new HashMap<>();  // Merdivenlerin başlangıç ve bitiş noktalarını tutar (başlangıç → çıkacağı yer)
    private final Random random = new Random(); // Zar atmak için rastgele sayı üreteci
    
    // Yapıcı metod: Oyunun başında yılan ve merdiven konumlarını belirler
    public GameManager() {

        // Yılanlar
        snakes.put(29, 9);
        snakes.put(38, 15);
        snakes.put(47, 5);
        snakes.put(53, 33);
        snakes.put(62, 37);
        snakes.put(86, 54);
        snakes.put(92, 70);
        snakes.put(97, 25);

        // Merdivenler
        ladders.put(2, 23);
        ladders.put(8, 34);
        ladders.put(20, 77);
        ladders.put(32, 68);
        ladders.put(41, 79);
        ladders.put(74, 88);
        ladders.put(85, 95);
        ladders.put(82, 100);
    }

    // Zar atma işlemi (1 ile 6 arasında rastgele sayı üretir)
    public int rollDice() {
        return random.nextInt(6) + 1;
    }

    // Oyuncunun pozisyonunu zar sonucuna göre güncelle
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

        // Pozisyonu güncelle
        positions[playerId] = newPos;
        return newPos;
    }
    
    // Belirli bir oyuncunun mevcut pozisyonunu döner
    public int getPlayerPosition(int playerId) {
        return positions[playerId];  // Oyuncunun tahtadaki pozisyonu
    }
    
    // Oyuncunun kazanıp kazanmadığını kontrol eder
    public boolean hasPlayerWon(int playerId) {
        return positions[playerId] == 100; // true → oyuncu kazandı, false → henüz kazanmadı
    }
    
    // Oyunu sıfırlar; her iki oyuncunun pozisyonu başa döner
    public void resetGame() {
        positions[0] = 0;
        positions[1] = 0;
    }

}
