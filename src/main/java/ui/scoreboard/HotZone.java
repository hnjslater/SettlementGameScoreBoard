package ui.scoreboard;
import model.*;


import java.awt.Shape;
public class HotZone {

    public enum ops {INC, DEC, LR, LA};

    public HotZone(Player p, ops o, Shape z) {
        this.player = p;
        this.operation = o;
        this.zone = z;
    }
    public ops operation;
    public Player player;
    public Shape zone;
}
