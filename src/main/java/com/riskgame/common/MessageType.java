package com.riskgame.common;

import java.io.Serializable;

/**
 * Mesaj türlerini tanımlayan enum.
 */
public enum MessageType implements Serializable {
    LOGIN,              // Giriş
    LOGOUT,             // Çıkış
    CHAT,               // Sohbet
    SERVER_FULL,        // Sunucu dolu
    PLAYER_JOINED,      // Oyuncu katıldı
    PLAYER_LEFT,        // Oyuncu ayrıldı
    GAME_READY,         // Oyun hazır
    START_GAME,         // Oyunu başlat
    GAME_STARTED,       // Oyun başladı
    GAME_ENDED,         // Oyun bitti
    PLAYER_ELIMINATED,  // Oyuncu elendi
    TURN_CHANGED,       // Sıra değişti
    GAME_STATE,         // Oyun durumu
    GAME_ACTION,        // Oyun hareketi
    MOVE_APPLIED,       // Hareket uygulandı
    PLAYER_READY,  // Oyuncu hazır
    INVALID_MOVE        // Geçersiz hareket
}
