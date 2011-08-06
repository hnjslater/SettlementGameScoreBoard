package ui.scoreboard;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
 
public class PlayerPainterFontHelper {
    Font font;
    int lineHeight;

    int bigTextHeight;
    int maxScoreWidth;
    int cursorDrop;
    int widthOfA;
    int widthOfR;

    public void setProperties(Graphics graphics, int width, int height, int lineHeight) {
            // update variables
            this.font = graphics.getFont(); 
            this.lineHeight = lineHeight;

            // work out the font size to use
            this.font = font.deriveFont(60f);
            FontMetrics oldBigFontMetrics = graphics.getFontMetrics(this.font);
            float ratio = (lineHeight * 0.5f) / (oldBigFontMetrics.getHeight());
            this.font = this.font.deriveFont(ratio * this.font.getSize());
            graphics.setFont(this.font);
            FontMetrics metrics = graphics.getFontMetrics(font);

            // export the useful values
            this.bigTextHeight =  (int)(metrics.getMaxCharBounds(graphics).getHeight());
            this.maxScoreWidth = metrics.stringWidth("10 "); 
            this.cursorDrop = (int)(metrics.getMaxAscent()) + (lineHeight - bigTextHeight) / 2;
            this.widthOfA = metrics.stringWidth("A");
            this.widthOfR = metrics.stringWidth("R");
    }
}
