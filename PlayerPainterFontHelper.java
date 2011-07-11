import java.awt.Font;
import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferStrategy;
import java.awt.event.KeyListener;
import javax.swing.event.MouseInputListener;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import javax.swing.JFrame;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.*;
import java.awt.Color;
import java.util.Date;
 
public class PlayerPainterFontHelper {
    private Font font;
    private int height;
    private int width;
    int lineHeight;

    int bigTextHeight;
    int maxScoreWidth;
    int cursorDrop;
    int widthOfA;
    int widthOfR;

    public boolean setProperties(Graphics graphics, int width, int height, int lineHeight) {
        if (!graphics.getFont().equals(this.font) || this.height != height || this.width != width || this.lineHeight != lineHeight) {
            // update variables
            this.font = graphics.getFont(); 
            this.height = height;
            this.width = width;
            this.lineHeight = lineHeight;

            // work out the font size to use
            this.font = font.deriveFont(60f);
            FontMetrics oldBigFontMetrics = graphics.getFontMetrics(font);
            float ratio = (lineHeight * 0.5f) / (oldBigFontMetrics.getHeight());
            font = font.deriveFont(ratio * font.getSize());
            graphics.setFont(font);
            FontMetrics metrics = graphics.getFontMetrics(font);

            // export the useful values
            this.bigTextHeight =  (int)(metrics.getMaxCharBounds(graphics).getHeight());
            this.maxScoreWidth = metrics.stringWidth("10 "); 
            this.cursorDrop = (int)(metrics.getMaxAscent()) + (lineHeight - bigTextHeight) / 2;
            this.widthOfA = metrics.stringWidth("A");
            this.widthOfR = metrics.stringWidth("R");


            return false;
        }
        else {
            return true;
        }
    }
}
