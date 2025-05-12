package common;

import java.io.Serializable;
import java.util.HashMap;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;         // Mesaj tipi: "attack", "place", "start", "end", "update" vs.
    private String fromPlayer;   // Gönderen oyuncunun adı veya ID'si
    private HashMap<String, Object> data;  // Komutla ilgili veri (nereden nereye, kaç asker vb.)

    public Message(String type, String fromPlayer) {
        this.type = type;
        this.fromPlayer = fromPlayer;
        this.data = new HashMap<>();
    }

    public String getType() {
        return type;
    }

    public String getFromPlayer() {
        return fromPlayer;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void addData(String key, Object value) {
        this.data.put(key, value);
    }

    public Object getData(String key) {
        return this.data.get(key);
    }
}
