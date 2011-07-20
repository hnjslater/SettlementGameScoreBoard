import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Color;
import java.awt.geom.*;
import java.awt.Shape;
import java.util.List;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Date;



class PlayerPainter extends GUIObject implements PlayerListener {
    public Player player;
    private Game game;
    private ScoreBoardHelper helper = new ScoreBoardHelper();
    private BufferedImage playerName;
    Boolean playerNameDirty = true;
    Object playerNameDirtyLock = new Object();
    final int margin = 25;
    int letterWidth;
    int lineHeight;
    private PlayerPainterFontHelper paintHelper;
    private boolean editing;
    private int frameWidth;
    private int frameHeight;

    private int startY = -1;
    private int endY = -1;
    private long startTime;

    public PlayerPainter(Player p, Game g, PlayerPainterFontHelper paintHelper) {
        super(0,0,0,0);
        this.player = p;
        this.game = g;
        this.playerName = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
        this.paintHelper = paintHelper;
        p.addPlayerListener(this);
    }

    // GUIObject methods ///////////////////////////////////////////
    public boolean paint(Graphics graphics, long time, int frameWidth, int frameHeight, List<HotZone> hotZones) {

        int lineHeight = 0;
        if (game.getNumberOfPlayers() < 3)
            lineHeight = frameHeight / 3;
        else
            lineHeight = frameHeight / game.getNumberOfPlayers();

        if (startY == -1) {
            startY =(lineHeight * game.getLeaderBoard().lastIndexOf(player)); 
            endY = startY;
        }
        if (this.frameHeight != frameHeight || this.frameWidth != frameWidth || this.lineHeight != lineHeight || playerNameDirty) {
            paintHelper.setProperties(graphics, frameWidth, frameHeight, lineHeight);
            this.frameHeight = frameHeight;
            this.frameWidth = frameWidth;
            this.lineHeight = lineHeight;
            updateImage(graphics.getFont());
        }
        else {
            graphics.setFont(paintHelper.font);
        }
        int cursor = getY(time);
        Player p = player;
        graphics.setColor( helper.getGraphicsColor(player.getPlayerColor()) );

        graphics.drawImage(playerName,0,cursor + (lineHeight - playerName.getHeight()) /2,null);
        char[] colorChar = new char[] { helper.getColorChar(player.getPlayerColor())} ;
        graphics.drawChars(colorChar , 0, 1, frameWidth-letterWidth-margin, cursor+paintHelper.cursorDrop );

        if (showMouseAffordances) {
                float unit = lineHeight/23f;

                if (game.getWinner() == null) {
                    // Longest Road button

                    Ellipse2D roadButton = new Ellipse2D.Double((int)(frameWidth-150-unit*48), (int)(cursor+unit*4), (int)(unit*15), (int)(unit*15));
                    if (!player.getAchievements().contains(Achievement.LongestRoad)) {
                        graphics.fillOval((int)roadButton.getX(), (int)roadButton.getY(), (int)roadButton.getWidth(), (int)roadButton.getHeight());
                        hotZones.add(new HotZone(p,HotZone.ops.LR,roadButton));
                        graphics.setColor(Color.BLACK);
                        graphics.drawString( "R", (int)(frameWidth-150-unit*48 + (unit*15 - paintHelper.widthOfA)/2), cursor+paintHelper.cursorDrop );
                    }
                    else {
                        Stroke normal = ((Graphics2D)graphics).getStroke();
                        Stroke wider = new BasicStroke(4);
                        ((Graphics2D)graphics).setStroke(wider);
                        graphics.drawOval((int)roadButton.getX(), (int)roadButton.getY(), (int)roadButton.getWidth(), (int)roadButton.getHeight());
                        ((Graphics2D)graphics).setStroke(normal);
                        hotZones.add(new HotZone(null,HotZone.ops.LR,roadButton));
                    }
                   

                    // Largest Army button
                    Ellipse2D armyButton = new Ellipse2D.Double(frameWidth-150-unit*32, cursor+unit*4, unit*15, unit*15);
                    graphics.setColor(helper.getGraphicsColor(p.getPlayerColor()));
                    if (!player.getAchievements().contains(Achievement.LargestArmy)) {
                        graphics.fillOval((int)armyButton.getX(), (int)armyButton.getY(), (int)armyButton.getWidth(), (int)armyButton.getHeight());
                        hotZones.add(new HotZone(p,HotZone.ops.LA,armyButton));
                        graphics.setColor(Color.BLACK);
                        graphics.drawString( "A", (int)(frameWidth-150-unit*32 + (unit*15 - paintHelper.widthOfA)/2), cursor+paintHelper.cursorDrop );
                    }
                    else {
                        Stroke normal = ((Graphics2D)graphics).getStroke();
                        Stroke wider = new BasicStroke(4);
                        ((Graphics2D)graphics).setStroke(wider);
                        graphics.drawOval((int)armyButton.getX(), (int)armyButton.getY(), (int)armyButton.getWidth(), (int)armyButton.getHeight());
                        ((Graphics2D)graphics).setStroke(normal);
                        hotZones.add(new HotZone(null,HotZone.ops.LA,armyButton));
                    }

                    // Plus box:
                    graphics.setColor(helper.getGraphicsColor(p.getPlayerColor()));
                    Ellipse2D plusButton = new Ellipse2D.Double(frameWidth-150-unit*16, cursor+unit*4, unit*15, unit*15);
                    graphics.setColor(helper.getGraphicsColor(p.getPlayerColor()));
                    Polygon pol = new Polygon();
                    pol.addPoint((int)(unit*3), 0);
                    pol.addPoint((int)(unit*4), 0);
                    pol.addPoint((int)(unit*4), (int)(unit*3));
                    pol.addPoint((int)(unit*7), (int)(unit*3));
                    pol.addPoint((int)(unit*7), (int)(unit*4));
                    pol.addPoint((int)(unit*4), (int)(unit*4));
                    pol.addPoint((int)(unit*4), (int)(unit*7));
                    pol.addPoint((int)(unit*3), (int)(unit*7));
                    pol.addPoint((int)(unit*3), (int)(unit*4));
                    pol.addPoint(0, (int)(unit*4));
                    pol.addPoint(0, (int)(unit*3));
                    pol.addPoint((int)(unit*3), (int)(unit*3));
                    pol.translate((int)(plusButton.getX()+unit*4), (int)(plusButton.getY()+unit*4));
                    graphics.fillOval((int)plusButton.getX(), (int)plusButton.getY(), (int)plusButton.getWidth(), (int)plusButton.getHeight());
                    graphics.setColor(Color.BLACK);
                    graphics.fillPolygon(pol);
                    hotZones.add(new HotZone(p,HotZone.ops.INC,plusButton));
                }
                // Minus box:
                if ((game.getWinner() == null && p.getSettlementVP() > 2) || game.getWinner() == p) {
                    graphics.setColor(helper.getGraphicsColor(p.getPlayerColor()));
                    Ellipse2D box2 = new Ellipse2D.Double(frameWidth-150, cursor+unit*4, unit*15, unit*15);
                    graphics.fillOval((int)box2.getX(), (int)box2.getY(), (int)box2.getWidth(), (int)box2.getHeight());
                    graphics.setColor(Color.BLACK);
                    graphics.fillRect((int)(box2.getX()+unit*4), (int)(box2.getY()+unit*7), (int)(unit*7), (int)(unit));


                    hotZones.add(new HotZone(player,HotZone.ops.DEC,box2));
                }
        }
        // if they are equal, then we are not animating
        return startY == endY;
    }
    public int getX(long time) {
        return 0;
    }
    public int getY(long time) {
        //return endY;
        int currentY = 0;
        int direction = 0;
        if (endY > startY)
            direction = +1;
        else if (endY < startY)
            direction = -1;
        long duration = time - startTime;

        if (duration < 0)
            duration = 0;

        currentY =(int)(duration * direction)/2 + startY;

        if ((direction == -1) == (currentY < endY)) {
            startY = endY;
            currentY = startY;
        }
        
        return currentY;

    }
    public void invalidate() {
        synchronized(playerNameDirtyLock) {
            playerNameDirty = true;
        }
    }
    public void setEditing(boolean editing) {
        this.editing = editing;
        invalidate();
    }

    /// Player Listener methods

    public void playerVPChanged(PlayerEvent pe) {
        synchronized(playerNameDirtyLock) {
            playerNameDirty = true;
        }
    }

    public void playerRenamed(PlayerEvent pe) {
        synchronized(playerNameDirtyLock) {
            playerNameDirty = true;
        }
    }

    public void playerRankChanged(PlayerEvent pe) {
        long now = (new Date()).getTime();
        startY = getY(now);
        endY = (lineHeight * game.getLeaderBoard().lastIndexOf(player)); 
        startTime = now;
    }
    
    // private methods
    private void updateImage(Font bigFont) {

        // Do as much as we can outside of the synchronized to reduce the change of a deadlock
        String displayName = player.getName();
        //Hacky cursor
        if (editing)
            displayName += "|";

        // Achievements
        if (player.getAchievements().contains(Achievement.LongestRoad))
            displayName += " (LR)";
        if (player.getAchievements().contains(Achievement.LargestArmy))
            displayName += " (LA)";

        String playerVP =  new Integer( player.getVP()).toString();
        synchronized(playerNameDirtyLock) {
            Graphics2D graphics = playerName.createGraphics();
            graphics.setFont(bigFont);
            FontMetrics metrics = graphics.getFontMetrics();
            int maxWidth = metrics.stringWidth(displayName);
            letterWidth = metrics.charWidth(helper.getColorChar(player.getPlayerColor()));
            int maxDescent = metrics.getMaxAscent();
            int maxHeight = maxDescent + metrics.getMaxDescent();
            graphics.dispose();
            playerName = new BufferedImage(maxWidth + paintHelper.maxScoreWidth + margin, maxHeight, BufferedImage.TYPE_INT_ARGB);
            graphics = playerName.createGraphics();

            graphics.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                         java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                         java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.setColor( helper.getGraphicsColor(player.getPlayerColor()) );
            graphics.setFont(bigFont);
            graphics.drawString(playerVP, margin, maxDescent);
            graphics.drawString(displayName, paintHelper.maxScoreWidth + margin, maxDescent);
            playerNameDirty = false;
        }
    }
}
