import java.awt.Rectangle;
import java.awt.Graphics;

abstract class GUIObject extends Rectangle {
    public GUIObject(int x, int y, int w, int h) {
        super(x,y,w,h);
    }
    public abstract void paint(Graphics g);
    public abstract void tick();
}
