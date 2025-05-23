package server;

import common.AttackResult;
import common.Continent;
import common.Player;
import common.Territory;
import java.io.Serializable;
import java.util.*;

/**
 * Server tarafı için GameState sınıfı. Oyun mantığını içerir.
 */
public class ServerGameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Territory> territories = new HashMap<>();
    private Map<String, Continent> continents = new HashMap<>();
    private Map<String, Player> players = new HashMap<>();
    private List<String> playerList = new ArrayList<>();
    private boolean gameStarted = false;
    private String currentPlayer;

    /**
     * Oyunu başlatır ve haritayı oluşturur.
     */
    public void initializeGame(List<String> playerNames) {
        // Oyun başlatılmadan önce sıfırla
        territories.clear();
        continents.clear();
        players.clear();
        playerList.clear();

        // Kıtaları oluştur
        createContinents();

        // Bölgeleri oluştur
        createTerritories();

        // Oyuncuları oluştur
        for (String name : playerNames) {
            Player player = new Player(name);
            players.put(name, player);
            playerList.add(name);
        }

        if (!playerList.isEmpty()) {
            currentPlayer = playerList.get(0);
        }

        // Bölgeleri rastgele dağıt
        distributeTerritoriesRandomly();

        // Her bölgeye başlangıçta bir birlik yerleştir
        for (Territory territory : territories.values()) {
            territory.setArmies(1);
        }

        // Her oyuncuya başlangıç birliklerini hesapla
        int initialArmies = calculateInitialArmies(playerNames.size());
        for (Player player : players.values()) {
            player.setReinforcementArmies(initialArmies - player.getTerritories().size());
        }

        gameStarted = true;
    }

    /**
     * Başlangıç birlik sayısını hesaplar.
     */
    private int calculateInitialArmies(int playerCount) {
        switch (playerCount) {
            case 2:
                return 40;
            case 3:
                return 35;
            case 4:
                return 30;
            case 5:
                return 25;
            default:
                return 20;
        }
    }

    /**
     * Kıtaları oluşturur.
     */
    private void createContinents() {
        continents.put("KUZEY_AMERIKA", new Continent("KUZEY_AMERIKA", 5));
        continents.put("GUNEY_AMERIKA", new Continent("GUNEY_AMERIKA", 2));
        continents.put("AVRUPA", new Continent("AVRUPA", 5));
        continents.put("AFRIKA", new Continent("AFRIKA", 3));
        continents.put("ASYA", new Continent("ASYA", 7));
        continents.put("AVUSTRALYA", new Continent("AVUSTRALYA", 2));
    }

    /**
     * Bölgeleri oluşturur ve komşuluk ilişkilerini tanımlar.
     */
    private void createTerritories() {
        // Kuzey Amerika bölgeleri
        territories.put("ALASKA", new Territory("ALASKA", "KUZEY_AMERIKA"));
        territories.put("KUZEYBATI_BOLGE", new Territory("KUZEYBATI_BOLGE", "KUZEY_AMERIKA"));
        territories.put("GRÖNLAND", new Territory("GRÖNLAND", "KUZEY_AMERIKA"));
        territories.put("ALBERTA", new Territory("ALBERTA", "KUZEY_AMERIKA"));
        territories.put("ONTARIO", new Territory("ONTARIO", "KUZEY_AMERIKA"));
        territories.put("QUEBEC", new Territory("QUEBEC", "KUZEY_AMERIKA"));
        territories.put("BATI_ABD", new Territory("BATI_ABD", "KUZEY_AMERIKA"));
        territories.put("DOGU_ABD", new Territory("DOGU_ABD", "KUZEY_AMERIKA"));
        territories.put("ORTA_AMERIKA", new Territory("ORTA_AMERIKA", "KUZEY_AMERIKA"));

        // Güney Amerika bölgeleri
        territories.put("VENEZUELA", new Territory("VENEZUELA", "GUNEY_AMERIKA"));
        territories.put("PERU", new Territory("PERU", "GUNEY_AMERIKA"));
        territories.put("BREZILYA", new Territory("BREZILYA", "GUNEY_AMERIKA"));
        territories.put("ARJANTIN", new Territory("ARJANTIN", "GUNEY_AMERIKA"));

        // Avrupa bölgeleri
        territories.put("IZLANDA", new Territory("IZLANDA", "AVRUPA"));
        territories.put("ISKANDINAVYA", new Territory("ISKANDINAVYA", "AVRUPA"));
        territories.put("BUYUK_BRITANYA", new Territory("BUYUK_BRITANYA", "AVRUPA"));
        territories.put("BATI_AVRUPA", new Territory("BATI_AVRUPA", "AVRUPA"));
        territories.put("KUZEY_AVRUPA", new Territory("KUZEY_AVRUPA", "AVRUPA"));
        territories.put("GUNEY_AVRUPA", new Territory("GUNEY_AVRUPA", "AVRUPA"));
        territories.put("UKRAYNA", new Territory("UKRAYNA", "AVRUPA"));

        // Afrika bölgeleri
        territories.put("KUZEY_AFRIKA", new Territory("KUZEY_AFRIKA", "AFRIKA"));
        territories.put("MISIR", new Territory("MISIR", "AFRIKA"));
        territories.put("DOGU_AFRIKA", new Territory("DOGU_AFRIKA", "AFRIKA"));
        territories.put("KONGO", new Territory("KONGO", "AFRIKA"));
        territories.put("GUNEY_AFRIKA", new Territory("GUNEY_AFRIKA", "AFRIKA"));
        territories.put("MADAGASKAR", new Territory("MADAGASKAR", "AFRIKA"));

        // Asya bölgeleri
        territories.put("URAL", new Territory("URAL", "ASYA"));
        territories.put("SIBIRYA", new Territory("SIBIRYA", "ASYA"));
        territories.put("YAKUTSK", new Territory("YAKUTSK", "ASYA"));
        territories.put("KAMCHATKA", new Territory("KAMCHATKA", "ASYA"));
        territories.put("IRKUTSK", new Territory("IRKUTSK", "ASYA"));
        territories.put("MOĞOLISTAN", new Territory("MOĞOLISTAN", "ASYA"));
        territories.put("JAPONYA", new Territory("JAPONYA", "ASYA"));
        territories.put("AFGANISTAN", new Territory("AFGANISTAN", "ASYA"));
        territories.put("ÇIN", new Territory("ÇIN", "ASYA"));
        territories.put("ORTA_DOĞU", new Territory("ORTA_DOĞU", "ASYA"));
        territories.put("HINDISTAN", new Territory("HINDISTAN", "ASYA"));
        territories.put("SIAM", new Territory("SIAM", "ASYA"));

        // Avustralya bölgeleri
        territories.put("ENDONEZYA", new Territory("ENDONEZYA", "AVUSTRALYA"));
        territories.put("YENI_GINE", new Territory("YENI_GINE", "AVUSTRALYA"));
        territories.put("BATI_AVUSTRALYA", new Territory("BATI_AVUSTRALYA", "AVUSTRALYA"));
        territories.put("DOGU_AVUSTRALYA", new Territory("DOGU_AVUSTRALYA", "AVUSTRALYA"));

        // Komşuluk ilişkilerini tanımla
        // Kuzey Amerika komşulukları
        addNeighbors("ALASKA", "KUZEYBATI_BOLGE", "KAMCHATKA", "ALBERTA");
        addNeighbors("KUZEYBATI_BOLGE", "ALASKA", "ALBERTA", "ONTARIO", "GRÖNLAND");
        addNeighbors("GRÖNLAND", "KUZEYBATI_BOLGE", "ONTARIO", "QUEBEC", "IZLANDA");
        addNeighbors("ALBERTA", "ALASKA", "KUZEYBATI_BOLGE", "ONTARIO", "BATI_ABD");
        addNeighbors("ONTARIO", "ALBERTA", "KUZEYBATI_BOLGE", "GRÖNLAND", "QUEBEC", "BATI_ABD", "DOGU_ABD");
        addNeighbors("QUEBEC", "ONTARIO", "GRÖNLAND", "DOGU_ABD");
        addNeighbors("BATI_ABD", "ALBERTA", "ONTARIO", "DOGU_ABD", "ORTA_AMERIKA");
        addNeighbors("DOGU_ABD", "ONTARIO", "QUEBEC", "BATI_ABD", "ORTA_AMERIKA");
        addNeighbors("ORTA_AMERIKA", "BATI_ABD", "DOGU_ABD", "VENEZUELA");

        // Güney Amerika komşulukları
        addNeighbors("VENEZUELA", "ORTA_AMERIKA", "PERU", "BREZILYA");
        addNeighbors("PERU", "VENEZUELA", "BREZILYA", "ARJANTIN");
        addNeighbors("BREZILYA", "VENEZUELA", "PERU", "ARJANTIN", "KUZEY_AFRIKA");
        addNeighbors("ARJANTIN", "PERU", "BREZILYA");

        // Avrupa komşulukları
        addNeighbors("IZLANDA", "GRÖNLAND", "BUYUK_BRITANYA", "ISKANDINAVYA");
        addNeighbors("ISKANDINAVYA", "IZLANDA", "BUYUK_BRITANYA", "KUZEY_AVRUPA", "UKRAYNA");
        addNeighbors("BUYUK_BRITANYA", "IZLANDA", "ISKANDINAVYA", "KUZEY_AVRUPA", "BATI_AVRUPA");
        addNeighbors("BATI_AVRUPA", "BUYUK_BRITANYA", "KUZEY_AVRUPA", "GUNEY_AVRUPA", "KUZEY_AFRIKA");
        addNeighbors("KUZEY_AVRUPA", "BUYUK_BRITANYA", "ISKANDINAVYA", "UKRAYNA", "GUNEY_AVRUPA", "BATI_AVRUPA");
        addNeighbors("GUNEY_AVRUPA", "BATI_AVRUPA", "KUZEY_AVRUPA", "UKRAYNA", "ORTA_DOĞU", "MISIR", "KUZEY_AFRIKA");
        addNeighbors("UKRAYNA", "ISKANDINAVYA", "KUZEY_AVRUPA", "GUNEY_AVRUPA", "ORTA_DOĞU", "AFGANISTAN", "URAL");

        // Afrika komşulukları
        addNeighbors("KUZEY_AFRIKA", "BATI_AVRUPA", "GUNEY_AVRUPA", "MISIR", "DOGU_AFRIKA", "KONGO", "BREZILYA");
        addNeighbors("MISIR", "GUNEY_AVRUPA", "ORTA_DOĞU", "DOGU_AFRIKA", "KUZEY_AFRIKA");
        addNeighbors("DOGU_AFRIKA", "MISIR", "ORTA_DOĞU", "KUZEY_AFRIKA", "KONGO", "GUNEY_AFRIKA", "MADAGASKAR");
        addNeighbors("KONGO", "KUZEY_AFRIKA", "DOGU_AFRIKA", "GUNEY_AFRIKA");
        addNeighbors("GUNEY_AFRIKA", "KONGO", "DOGU_AFRIKA", "MADAGASKAR");
        addNeighbors("MADAGASKAR", "DOGU_AFRIKA", "GUNEY_AFRIKA");

        // Asya komşulukları
        addNeighbors("URAL", "UKRAYNA", "AFGANISTAN", "ÇIN", "SIBIRYA");
        addNeighbors("SIBIRYA", "URAL", "ÇIN", "MOĞOLISTAN", "IRKUTSK", "YAKUTSK");
        addNeighbors("YAKUTSK", "SIBIRYA", "IRKUTSK", "KAMCHATKA");
        addNeighbors("KAMCHATKA", "YAKUTSK", "IRKUTSK", "MOĞOLISTAN", "JAPONYA", "ALASKA");
        addNeighbors("IRKUTSK", "SIBIRYA", "YAKUTSK", "KAMCHATKA", "MOĞOLISTAN");
        addNeighbors("MOĞOLISTAN", "SIBIRYA", "IRKUTSK", "KAMCHATKA", "JAPONYA", "ÇIN");
        addNeighbors("JAPONYA", "KAMCHATKA", "MOĞOLISTAN");
        addNeighbors("AFGANISTAN", "UKRAYNA", "URAL", "ÇIN", "HINDISTAN", "ORTA_DOĞU");
        addNeighbors("ÇIN", "URAL", "SIBIRYA", "MOĞOLISTAN", "AFGANISTAN", "HINDISTAN", "SIAM");
        addNeighbors("ORTA_DOĞU", "UKRAYNA", "GUNEY_AVRUPA", "MISIR", "DOGU_AFRIKA", "AFGANISTAN", "HINDISTAN");
        addNeighbors("HINDISTAN", "ORTA_DOĞU", "AFGANISTAN", "ÇIN", "SIAM");
        addNeighbors("SIAM", "HINDISTAN", "ÇIN", "ENDONEZYA");

        // Avustralya komşulukları
        addNeighbors("ENDONEZYA", "SIAM", "YENI_GINE", "BATI_AVUSTRALYA");
        addNeighbors("YENI_GINE", "ENDONEZYA", "BATI_AVUSTRALYA", "DOGU_AVUSTRALYA");
        addNeighbors("BATI_AVUSTRALYA", "ENDONEZYA", "YENI_GINE", "DOGU_AVUSTRALYA");
        addNeighbors("DOGU_AVUSTRALYA", "YENI_GINE", "BATI_AVUSTRALYA");
    }

    /**
     * İki bölge arasında komşuluk ilişkisi kurar.
     */
    private void addNeighbors(String territory, String... neighbors) {
        for (String neighbor : neighbors) {
            Territory t1 = territories.get(territory);
            Territory t2 = territories.get(neighbor);

            if (t1 != null && t2 != null) {
                t1.addNeighbor(neighbor);
                t2.addNeighbor(territory);
            }
        }
    }

    /**
     * Bölgeleri oyunculara rastgele dağıtır.
     */
    private void distributeTerritoriesRandomly() {
        if (playerList.isEmpty()) {
            return;
        }

        List<String> territoryNames = new ArrayList<>(territories.keySet());
        Collections.shuffle(territoryNames);

        int playerIndex = 0;
        for (String territoryName : territoryNames) {
            String playerName = playerList.get(playerIndex);
            Player player = players.get(playerName);
            Territory territory = territories.get(territoryName);

            territory.setOwner(playerName);
            player.addTerritory(territoryName);

            playerIndex = (playerIndex + 1) % playerList.size();
        }
    }

    /**
     * Takviye birlik sayısını hesaplar.
     */
    public int calculateReinforcementArmies(String playerName) {
        Player player = players.get(playerName);
        if (player == null) {
            return 0;
        }

        // Temel birlik: Bölge sayısı / 3 (minimum 3)
        int baseArmies = Math.max(3, player.getTerritories().size() / 3);

        // Kıta bonusu
        int continentBonus = 0;
        for (Continent continent : continents.values()) {
            boolean controlsContinent = true;
            for (Territory territory : territories.values()) {
                if (territory.getContinent().equals(continent.getName()) && !territory.getOwner().equals(playerName)) {
                    controlsContinent = false;
                    break;
                }
            }
            if (controlsContinent) {
                continentBonus += continent.getBonus();
            }
        }

        return baseArmies + continentBonus;
    }

    /**
     * Bir oyuncunun takviye birliklerini ayarlar.
     */
    public void setReinforcementArmies(String playerName, int armies) {
        Player player = players.get(playerName);
        if (player != null) {
            player.setReinforcementArmies(armies);
        }
    }

    /**
     * Birlik yerleştirme hareketinin geçerli olup olmadığını kontrol eder.
     */
    public boolean canPlaceArmy(String playerName, String territoryName, int armies) {
        Player player = players.get(playerName);
        Territory territory = territories.get(territoryName);

        if (player == null || territory == null) {
            return false;
        }
        if (!territory.getOwner().equals(playerName)) {
            return false;
        }
        if (player.getReinforcementArmies() < armies) {
            return false;
        }

        return true;
    }

   /**
 * Birlik yerleştirir.
 */
public void placeArmy(String playerName, String territoryName, int armies) {
    System.out.println("\n=== BIRLIK YERLESTIRME ISLEMI ===");
    System.out.println("placeArmy çağrıldı: Oyuncu=" + playerName + ", Bölge=" + territoryName + ", Birlik=" + armies);
    
    if (!canPlaceArmy(playerName, territoryName, armies)) {
        System.out.println("HATA: canPlaceArmy kontrolleri başarısız!");
        return;
    }

    Player player = players.get(playerName);
    Territory territory = territories.get(territoryName);

    // Birlik eklemeden önce
    System.out.println("Önceki birlik sayısı: " + territory.getArmies());
    
    // Birlik ekleme - bu metodda sorun olabilir
    territory.addArmies(armies);
    player.setReinforcementArmies(player.getReinforcementArmies() - armies);
    
    // Birlik ekledikten sonra
    System.out.println("Yeni birlik sayısı: " + territory.getArmies());
    System.out.println("Kalan takviye birlik: " + player.getReinforcementArmies());
    System.out.println("=== BIRLIK YERLESTIRME TAMAMLANDI ===\n");
}
    /**
     * Saldırı hareketinin geçerli olup olmadığını kontrol eder.
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
     * Saldırı yapar ve sonucu döndürür.
     */

/**
 * Saldırı yapar ve sonucu döndürür. İstemciden gelen zarları kullanabilir.
 */
public AttackResult attack(String playerName, String sourceTerritory, String targetTerritory, int attackingArmies, int[] clientAttackDice, int[] clientDefenseDice) {
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
    int[] attackDice;
    int[] defenseDice;
    
    if (clientAttackDice != null && clientAttackDice.length == attackingArmies) {
        // İstemciden gelen zarları kullan
        attackDice = clientAttackDice;
        System.out.println("İstemciden gelen saldıran zarları kullanılıyor: " + Arrays.toString(attackDice));
    } else {
        // Sunucuda yeni zarlar at
        attackDice = rollDice(attackingArmies);
        System.out.println("Sunucuda atılan saldıran zarları: " + Arrays.toString(attackDice));
    }
    
    int defenseArmies = Math.min(2, target.getArmies());
    
    if (clientDefenseDice != null && clientDefenseDice.length == defenseArmies) {
        // İstemciden gelen zarları kullan
        defenseDice = clientDefenseDice;
        System.out.println("İstemciden gelen savunan zarları kullanılıyor: " + Arrays.toString(defenseDice));
    } else {
        // Sunucuda yeni zarlar at
        defenseDice = rollDice(defenseArmies);
        System.out.println("Sunucuda atılan savunan zarları: " + Arrays.toString(defenseDice));
    }
    
    // Zarları sırala
    Arrays.sort(attackDice);
    reverseArray(attackDice);
    Arrays.sort(defenseDice);
    reverseArray(defenseDice);
    
    System.out.println("Sıralanmış saldıran zarları: " + Arrays.toString(attackDice));
    System.out.println("Sıralanmış savunan zarları: " + Arrays.toString(defenseDice));

    // Karşılaştır ve kayıpları hesapla
    int attackerLosses = 0;
    int defenderLosses = 0;

    for (int i = 0; i < Math.min(attackDice.length, defenseDice.length); i++) {
        System.out.println("Karşılaştırma " + (i+1) + ": Saldıran zar: " + attackDice[i] + " vs Savunan zar: " + defenseDice[i]);
        if (attackDice[i] > defenseDice[i]) {
            defenderLosses++;
            System.out.println(" -> Savunan kaybetti (Savunan: -1 birlik)");
        } else {
            attackerLosses++;
            System.out.println(" -> Saldıran kaybetti (Saldıran: -1 birlik)");
        }
    }

    System.out.println("Toplam kayıplar - Saldıran: " + attackerLosses + ", Savunan: " + defenderLosses);
    
    // Kayıpları uygula
    System.out.println("Başlangıç birlik sayıları - Saldıran: " + source.getArmies() + ", Savunan: " + target.getArmies());
    source.removeArmies(attackerLosses);
    target.removeArmies(defenderLosses);
    System.out.println("Kayıplardan sonra - Saldıran: " + source.getArmies() + ", Savunan: " + target.getArmies());

    // Saldırgan kazandı mı kontrol et
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
    descBuilder.append("Saldıran ").append(attackerLosses).append(" birlik kaybetti, Savunan ").append(defenderLosses).append(" birlik kaybetti.");
    
    String eliminatedPlayer = null;

    if (target.getArmies() == 0) {
        // Bölge ele geçirildi
        Player defender = players.get(defenderName);
        Player attacker = players.get(playerName);

        defender.removeTerritory(targetTerritory);
        attacker.addTerritory(targetTerritory);

        target.setOwner(playerName);
        target.setArmies(attackingArmies);
        source.removeArmies(attackingArmies);

        conquered = true;
        descBuilder.append(" ").append(playerName).append(" bölgeyi ele geçirdi!");
        System.out.println("BÖLGE ELE GEÇİRİLDİ! " + targetTerritory + " artık " + playerName + " oyuncusunun!");

        // Savunan oyuncu elendi mi kontrol et
        if (defender.getTerritories().isEmpty()) {
            eliminatedPlayer = defenderName;
            descBuilder.append(" ").append(defenderName).append(" oyundan elendi!");
            System.out.println(defenderName + " OYUNDAN ELENDİ!");
        }
    }

    System.out.println("=== SALDIRI TAMAMLANDI ===\n");
    
    // Zar sonuçlarını içeren zenginleştirilmiş AttackResult
    AttackResult result = new AttackResult(conquered, descBuilder.toString(), eliminatedPlayer);
    result.setAttackDice(attackDice);
    result.setDefenseDice(defenseDice);
    result.setAttackerLosses(attackerLosses);
    result.setDefenderLosses(defenderLosses);
    
    return result;
}

/**
 * Eski metodla geriye dönük uyumluluk için wrapper.
 */
public AttackResult attack(String playerName, String sourceTerritory, String targetTerritory, int attackingArmies) {
    if (!canAttack(playerName, sourceTerritory, targetTerritory, attackingArmies)) {
        return new AttackResult(false, "Geçersiz saldırı!", null);
    }

    Territory source = territories.get(sourceTerritory);
    Territory target = territories.get(targetTerritory);
    String defenderName = target.getOwner();

    // Zarları sunucu oluşturur
    int[] attackDice = rollDice(attackingArmies);
    int defenseArmies = Math.min(2, target.getArmies());
    int[] defenseDice = rollDice(defenseArmies);

    Arrays.sort(attackDice);
    reverseArray(attackDice);
    Arrays.sort(defenseDice);
    reverseArray(defenseDice);

    int attackerLosses = 0;
    int defenderLosses = 0;

    for (int i = 0; i < Math.min(attackDice.length, defenseDice.length); i++) {
        if (attackDice[i] > defenseDice[i]) {
            defenderLosses++;
        } else {
            attackerLosses++;
        }
    }

    source.removeArmies(attackerLosses);
    target.removeArmies(defenderLosses);

    boolean conquered = false;
    StringBuilder descBuilder = new StringBuilder();
    descBuilder.append("Zarlar [Saldıran: ").append(Arrays.toString(attackDice))
               .append(" vs Savunan: ").append(Arrays.toString(defenseDice)).append("]. ");
    descBuilder.append("Saldıran ").append(attackerLosses)
               .append(" birlik, savunan ").append(defenderLosses).append(" birlik kaybetti.");

    String eliminatedPlayer = null;

    if (target.getArmies() == 0) {
        Player attacker = players.get(playerName);
        Player defender = players.get(defenderName);

        target.setOwner(playerName);
        attacker.addTerritory(targetTerritory);
        defender.removeTerritory(targetTerritory);

        target.setArmies(attackingArmies);
        source.removeArmies(attackingArmies);

        conquered = true;
        descBuilder.append(" ").append(playerName).append(" bölgeyi ele geçirdi!");

        if (defender.getTerritories().isEmpty()) {
            eliminatedPlayer = defenderName;
            descBuilder.append(" ").append(defenderName).append(" oyundan elendi!");
        }
    }

    AttackResult result = new AttackResult(conquered, descBuilder.toString(), eliminatedPlayer);
    result.setAttackDice(attackDice);
    result.setDefenseDice(defenseDice);
    result.setAttackerLosses(attackerLosses);
    result.setDefenderLosses(defenderLosses);

    return result;
}
   
    private int[] rollDice(int count) {
        Random random = new Random();
        int[] dice = new int[count];
        for (int i = 0; i < count; i++) {
            dice[i] = random.nextInt(6) + 1; // 1-6 arası
        }
        return dice;
    }

    /**
     * Diziyi tersine çevirir.
     */
    private void reverseArray(int[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            int temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }

    /**
     * Birlik taşıma hareketinin geçerli olup olmadığını kontrol eder.
     */
    public boolean canFortify(String playerName, String sourceTerritory, String targetTerritory, int armies) {
        Territory source = territories.get(sourceTerritory);
        Territory target = territories.get(targetTerritory);

        if (source == null || target == null) {
            return false;
        }
        if (!source.getOwner().equals(playerName) || !target.getOwner().equals(playerName)) {
            return false;
        }
        if (!isConnected(sourceTerritory, targetTerritory, playerName)) {
            return false;
        }
        if (source.getArmies() <= armies) {
            return false; // En az bir birlik geride kalmalı
        }
        return true;
    }

    /**
     * İki bölge arasında bağlantı olup olmadığını kontrol eder.
     */
    private boolean isConnected(String sourceTerritory, String targetTerritory, String playerName) {
        Set<String> visited = new HashSet<>();
        return isConnectedDFS(sourceTerritory, targetTerritory, playerName, visited);
    }

    /**
     * DFS algoritması ile bağlantı kontrolü yapar.
     */
    private boolean isConnectedDFS(String currentTerritory, String targetTerritory, String playerName, Set<String> visited) {
        if (currentTerritory.equals(targetTerritory)) {
            return true;
        }

        visited.add(currentTerritory);
        Territory current = territories.get(currentTerritory);

        if (current == null) {
            return false;
        }

        for (String neighbor : current.getNeighbors()) {
            Territory neighborTerritory = territories.get(neighbor);
            if (neighborTerritory != null
                    && neighborTerritory.getOwner().equals(playerName)
                    && !visited.contains(neighbor)) {
                if (isConnectedDFS(neighbor, targetTerritory, playerName, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Birlik taşır.
     */
    public void fortify(String playerName, String sourceTerritory, String targetTerritory, int armies) {
        if (!canFortify(playerName, sourceTerritory, targetTerritory, armies)) {
            return;
        }

        Territory source = territories.get(sourceTerritory);
        Territory target = territories.get(targetTerritory);

        source.removeArmies(armies);
        target.addArmies(armies);
    }

    /**
     * Kazananı kontrol eder.
     */
    public String checkWinner() {
        String winner = null;
        int activePlayers = 0;

        for (Player player : players.values()) {
            if (!player.getTerritories().isEmpty()) {
                winner = player.getName();
                activePlayers++;
            }
        }

        return (activePlayers == 1) ? winner : null;
    }

    // Getter ve setter metodları
    public Map<String, Territory> getTerritories() {
        return territories;
    }

    public Map<String, Continent> getContinents() {
        return continents;
    }

    public Map<String, Player> getPlayers() {
        return players;
    }

    public List<String> getPlayerList() {
        return playerList;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
    
    public common.GameState toClientState() {
    common.GameState clientState = new common.GameState();

    // Bölgeleri kopyala
    for (Map.Entry<String, Territory> entry : this.getTerritories().entrySet()) {
        clientState.getTerritories().put(entry.getKey(), new Territory(entry.getValue()));
    }

    // Kıtaları kopyala
    for (Map.Entry<String, Continent> entry : this.getContinents().entrySet()) {
        clientState.getContinents().put(entry.getKey(), new Continent(entry.getValue().getName(), entry.getValue().getBonus()));
    }

    // Oyuncuları kopyala
    for (Map.Entry<String, Player> entry : this.getPlayers().entrySet()) {
        Player copy = new Player(entry.getValue().getName());
        copy.setReinforcementArmies(entry.getValue().getReinforcementArmies());
        for (String t : entry.getValue().getTerritories()) {
            copy.addTerritory(t);
        }
        clientState.getPlayers().put(entry.getKey(), copy);
    }

    // Sıra ve oyuncu listesi
    clientState.getPlayerList().addAll(this.getPlayerList());
    clientState.setGameStarted(this.isGameStarted());
    clientState.setCurrentPlayer(this.getCurrentPlayer());

    return clientState;
}

}
