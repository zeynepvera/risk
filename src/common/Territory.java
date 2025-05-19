package common;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
/**
 * Bölge sınıfı. Oyun haritasındaki bir bölgeyi temsil eder.
 */
public class Territory implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String continent;
    private String owner;
    private int armies;
    private Set<String> neighbors;
    
    public Territory(String name, String continent) {
        this.name = name;
        this.continent = continent;
        this.armies = 0;
        this.neighbors = new HashSet<>();
    }
    
    public void addNeighbor(String territory) {
        neighbors.add(territory);
    }
    
    public boolean isNeighbor(String territory) {
        return neighbors.contains(territory);
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
        System.out.println("Territory.setOwner: " + name + " bölgesinin sahibi " + owner + " olarak ayarlandı.");
    }
    
    public void setArmies(int armies) {
        int oldValue = this.armies;
        this.armies = armies;
        System.out.println("Territory.setArmies: " + name + " bölgesinde birlik sayısı " + oldValue + " -> " + armies + " olarak değiştirildi.");
    }
    
    public void addArmies(int count) {
        int oldValue = this.armies;
        this.armies += count;
        System.out.println("Territory.addArmies: " + name + " bölgesine " + count + " birlik eklendi. Yeni toplam: " + this.armies + " (önceki: " + oldValue + ")");
    }
    
    public void removeArmies(int count) {
        int oldValue = this.armies;
        this.armies = Math.max(0, this.armies - count);
        System.out.println("Territory.removeArmies: " + name + " bölgesinden " + count + " birlik çıkarıldı. Yeni toplam: " + this.armies + " (önceki: " + oldValue + ")");
    }
    
    public String getName() {
        return name;
    }
    
    public String getContinent() {
        return continent;
    }
    
    public String getOwner() {
        return owner;
    }
    
    public int getArmies() {
        return armies;
    }
    
    public Set<String> getNeighbors() {
        return neighbors;
    }
    
    @Override
    public String toString() {
        return "Territory{name='" + name + "', owner='" + owner + "', armies=" + armies + "}";
    }
    
    // Derin kopyalama için bir kopya constructor'ı ekleyin
    public Territory(Territory other) {
        this.name = other.name;
        this.continent = other.continent;
        this.owner = other.owner;
        this.armies = other.armies;
        this.neighbors = new HashSet<>(other.neighbors);
    }
}