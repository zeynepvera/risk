
package common;

import java.io.Serializable;
import java.util.List;

public class Territory implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<String> neighbors;
    private String owner;
    private int troops;

    public Territory(String name, List<String> neighbors) {
        this.name = name;
        this.neighbors = neighbors;
        this.troops = 0;
    }

    public String getName() {
        return name;
    }

    public List<String> getNeighbors() {
        return neighbors;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getTroops() {
        return troops;
    }

    public void setTroops(int troops) {
        this.troops = troops;
    }

    public void addTroops(int count) {
        this.troops += count;
    }

    public void removeTroops(int count) {
        this.troops -= count;
    }
}
