import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

abstract class GUIObject {
    protected int x;
    protected int y;
    protected int w;
    protected int h;
    public GUIObject(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
    /** paint graphics
    * returns true if representation has changed
    */
    public abstract boolean paint(Graphics g, long time);
    public abstract int getX(long time);
    public abstract int getY(long time);
}
