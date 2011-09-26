package ui.scoreboard;
import model.*;


import java.awt.Shape;
public class HotZone {

    public enum ops {INC, DEC, ACH};

    public HotZone(Player p, ops o, Shape z, Achievement a) {
        this.player = p;
        this.operation = o;
        this.zone = z;
        this.achievement = a;
    }
    public ops operation;
    public Player player;
    public Shape zone;
    public Achievement achievement;
}
