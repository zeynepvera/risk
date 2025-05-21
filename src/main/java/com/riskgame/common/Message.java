package com.riskgame.common;

import java.io.Serializable;

/**
 * Client ve server arasında iletişim için kullanılan mesaj sınıfı.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String sender;
    private String content;
    private MessageType type;
    private GameState gameState;
    private GameAction gameAction;
    
    public Message(String sender, String content, MessageType type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
    }
    
    public String getSender() {
        return sender;
    }
    
    public String getContent() {
        return content;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
    
    public GameAction getGameAction() {
        return gameAction;
    }
    
    public void setGameAction(GameAction gameAction) {
        this.gameAction = gameAction;
    }
    
    @Override
    public String toString() {
        return "Message{type=" + type + ", sender='" + sender + "', content='" + 
              (content != null ? content.substring(0, Math.min(20, content.length())) + "..." : "null") + "'}";
    }
}