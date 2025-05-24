package server.game;

import common.AttackResult;
import common.Territory;
import common.Player;
import java.util.*;

/**
 * Saldırı sistemi ve savaş hesaplamalarından sorumlu sınıf
 */
public class BattleManager {
    
    private Map<String, Territory> territories;
    private Map<String, Player> players;
    
    public BattleManager(Map<String, Territory> territories, Map<String, Player> players) {
        this.territories = territories;
        this.players = players;
    }
    
    /**
     * Saldırı yapılabilir mi kontrol eder
     */
    public boolean canAttack(String playerName, String sourceTerritory, String targetTerritory, int armies) {
        Territory source = territories.get(sourceTerritory);
        Territory target = territories.get(targetTerritory);

        if (source == null || target == null) {
            return false;
        }
        if (!source.getOwner().equals(playerName)) {
            return false;
        }
        if (target.getOwner().equals(playerName)) {
            return false;
        }
        if (!source.isNeighbor(targetTerritory)) {
            return false;
        }
        if (source.getArmies() <= armies) {
            return false; // En az bir birlik geride kalmalı
        }
        if (armies < 1 || armies > 3) {
            return false; // 1-3 birlikle saldırılabilir
        }
        return true;
    }
    
    /**
     * Saldırı gerçekleştirir (İstemciden gelen zar sonuçları ile)
     */
    public AttackResult attack(String playerName, String sourceTerritory, String targetTerritory, 
                              int attackingArmies, int[] clientAttackDice, int[] clientDefenseDice) {
        if (!canAttack(playerName, sourceTerritory, targetTerritory, attackingArmies)) {
            return new AttackResult(false, "Geçersiz saldırı!", null);
        }

        Territory source = territories.get(sourceTerritory);
        Territory target = territories.get(targetTerritory);
        String defenderName = target.getOwner();

        System.out.println("\n=== SALDIRI BAŞLATILIYOR ===");
        System.out.println("Saldıran: " + playerName + " (" + sourceTerritory + ", " + source.getArmies() + " birlik)");
        System.out.println("Savunan: " + defenderName + " (" + targetTerritory + ", " + target.getArmies() + " birlik)");
        System.out.println("Saldıran birlik sayısı: " + attackingArmies);

        // İstemciden gelen zarları kullan veya sunucuda yeni zarlar at
        int[] attackDice = (clientAttackDice != null && clientAttackDice.length == attackingArmies) 
                           ? clientAttackDice 
                           : rollDice(attackingArmies);
        
        int defenseArmies = Math.min(2, target.getArmies());
        int[] defenseDice = (clientDefenseDice != null && clientDefenseDice.length == defenseArmies)
                           ? clientDefenseDice
                           : rollDice(defenseArmies);
        
        System.out.println("Saldıran zarları: " + Arrays.toString(attackDice));
        System.out.println("Savunan zarları: " + Arrays.toString(defenseDice));
        
        // Zarları sırala (büyükten küçüğe)
        Arrays.sort(attackDice);
        reverseArray(attackDice);
        Arrays.sort(defenseDice);
        reverseArray(defenseDice);
        
        System.out.println("Sıralanmış saldıran zarları: " + Arrays.toString(attackDice));
        System.out.println("Sıralanmış savunan zarları: " + Arrays.toString(defenseDice));

        // Karşılaştır ve kayıpları hesapla
        BattleResult battleResult = calculateBattleResult(attackDice, defenseDice);
        
        System.out.println("Toplam kayıplar - Saldıran: " + battleResult.attackerLosses + 
                          ", Savunan: " + battleResult.defenderLosses);
        
        // Kayıpları uygula
        System.out.println("Başlangıç birlik sayıları - Saldıran: " + source.getArmies() + 
                          ", Savunan: " + target.getArmies());
        source.removeArmies(battleResult.attackerLosses);
        target.removeArmies(battleResult.defenderLosses);
        System.out.println("Kayıplardan sonra - Saldıran: " + source.getArmies() + 
                          ", Savunan: " + target.getArmies());

        // Saldırgan kazandı mı kontrol et
        AttackResult result = processAttackResult(playerName, sourceTerritory, targetTerritory, 
                                                 attackingArmies, attackDice, defenseDice, 
                                                 battleResult);

        System.out.println("=== SALDIRI TAMAMLANDI ===\n");
        return result;
    }
    
    /**
     * Saldırı gerçekleştirir (Sunucu zarları atar)
     */
    public AttackResult attack(String playerName, String sourceTerritory, String targetTerritory, int attackingArmies) {
        return attack(playerName, sourceTerritory, targetTerritory, attackingArmies, null, null);
    }
    
    /**
     * Zar karşılaştırması yapar ve kayıpları hesaplar
     */
    private BattleResult calculateBattleResult(int[] attackDice, int[] defenseDice) {
        int attackerLosses = 0;
        int defenderLosses = 0;

        for (int i = 0; i < Math.min(attackDice.length, defenseDice.length); i++) {
            System.out.println("Karşılaştırma " + (i+1) + ": Saldıran zar: " + attackDice[i] + 
                              " vs Savunan zar: " + defenseDice[i]);
            if (attackDice[i] > defenseDice[i]) {
                defenderLosses++;
                System.out.println(" -> Savunan kaybetti (Savunan: -1 birlik)");
            } else {
                attackerLosses++;
                System.out.println(" -> Saldıran kaybetti (Saldıran: -1 birlik)");
            }
        }
        
        return new BattleResult(attackerLosses, defenderLosses);
    }
    
    /**
     * Saldırı sonucunu işler ve AttackResult oluşturur
     */
    private AttackResult processAttackResult(String playerName, String sourceTerritory, String targetTerritory,
                                           int attackingArmies, int[] attackDice, int[] defenseDice,
                                           BattleResult battleResult) {
        Territory source = territories.get(sourceTerritory);
        Territory target = territories.get(targetTerritory);
        String defenderName = target.getOwner();
        
        boolean conquered = false;
        StringBuilder descBuilder = new StringBuilder();
        descBuilder.append("Saldırı sonucu: ");
        
        // Zar detaylarını ekle
        descBuilder.append("Zarlar [Saldıran: ");
        for (int i = 0; i < attackDice.length; i++) {
            descBuilder.append(attackDice[i]);
            if (i < attackDice.length - 1) descBuilder.append(",");
        }
        descBuilder.append(" vs Savunan: ");
        for (int i = 0; i < defenseDice.length; i++) {
            descBuilder.append(defenseDice[i]);
            if (i < defenseDice.length - 1) descBuilder.append(",");
        }
        descBuilder.append("]. ");
        
        // Kayıpları ekle
        descBuilder.append("Saldıran ").append(battleResult.attackerLosses)
                   .append(" birlik kaybetti, Savunan ").append(battleResult.defenderLosses)
                   .append(" birlik kaybetti.");
        
        String eliminatedPlayer = null;

        if (target.getArmies() == 0) {
            // Bölge ele geçirildi
            conquered = conquestTerritory(playerName, sourceTerritory, targetTerritory, attackingArmies);
            eliminatedPlayer = checkPlayerElimination(defenderName);
            
            descBuilder.append(" ").append(playerName).append(" bölgeyi ele geçirdi!");
            System.out.println("BÖLGE ELE GEÇİRİLDİ! " + targetTerritory + " artık " + playerName + " oyuncusunun!");

            if (eliminatedPlayer != null) {
                descBuilder.append(" ").append(defenderName).append(" oyundan elendi!");
                System.out.println(defenderName + " OYUNDAN ELENDİ!");
            }
        }

        // Zar sonuçlarını içeren zenginleştirilmiş AttackResult
        AttackResult result = new AttackResult(conquered, descBuilder.toString(), eliminatedPlayer);
        result.setAttackDice(attackDice);
        result.setDefenseDice(defenseDice);
        result.setAttackerLosses(battleResult.attackerLosses);
        result.setDefenderLosses(battleResult.defenderLosses);
        
        return result;
    }
    
    /**
     * Bölgeyi ele geçirme işlemini yapar
     */
    private boolean conquestTerritory(String playerName, String sourceTerritory, String targetTerritory, int attackingArmies) {
        Territory source = territories.get(sourceTerritory);
        Territory target = territories.get(targetTerritory);
        String defenderName = target.getOwner();
        
        Player defender = players.get(defenderName);
        Player attacker = players.get(playerName);

        defender.removeTerritory(targetTerritory);
        attacker.addTerritory(targetTerritory);

        target.setOwner(playerName);
        target.setArmies(attackingArmies);
        source.removeArmies(attackingArmies);
        
        return true;
    }
    
    /**
     * Oyuncunun elendi mi kontrol eder
     */
    private String checkPlayerElimination(String playerName) {
        Player player = players.get(playerName);
        if (player != null && player.getTerritories().isEmpty()) {
            return playerName;
        }
        return null;
    }
    
    /**
     * Zar atar
     */
    private int[] rollDice(int count) {
        Random random = new Random();
        int[] dice = new int[count];
        for (int i = 0; i < count; i++) {
            dice[i] = random.nextInt(6) + 1; // 1-6 arası
        }
        return dice;
    }
    
    /**
     * Diziyi tersine çevirir
     */
    private void reverseArray(int[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            int temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }
    
    /**
     * Savaş sonucunu tutan inner class
     */
    private static class BattleResult {
        final int attackerLosses;
        final int defenderLosses;
        
        BattleResult(int attackerLosses, int defenderLosses) {
            this.attackerLosses = attackerLosses;
            this.defenderLosses = defenderLosses;
        }
    }
}