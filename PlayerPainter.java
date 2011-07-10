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



class PlayerPainter extends GUIObject implements PlayerListener {
    public Player player;
    private Game game;
    private ScoreBoardHelper helper = new ScoreBoardHelper();
    private BufferedImage playerName;
    Boolean playerNameDirty = true;
    final int margin = 25;
    int letterWidth;
    int lineHeight;
    private PlayerPainterFontHelper paintHelper;
    private boolean editing;
    private int frameWidth;
    private int frameHeight;

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
        if (game.getNumberOfPlayers() < 3)
            lineHeight = frameHeight / 3;
        else
            lineHeight = frameHeight / game.getNumberOfPlayers();

        boolean changed = paintHelper.setProperties(graphics, frameWidth, frameHeight, lineHeight);

        if (playerNameDirty || changed) {
            updateImage(graphics.getFont());
        }
        int cursor = getY(0);
        Player p = player;
        graphics.setColor( helper.getGraphicsColor(player.getPlayerColor()) );




        graphics.drawImage(playerName,0,cursor + (lineHeight - playerName.getHeight()) /2,null);
        graphics.drawChars( new char[] {helper.getColorChar(player.getPlayerColor())}, 0, 1, frameWidth-letterWidth-margin, cursor+paintHelper.cursorDrop );

        if (showMouseAffordances) {
                int unit = lineHeight/23;

                if (game.getWinner() == null) {
                    // Longest Road button

                    Ellipse2D roadButton = new Ellipse2D.Double(frameWidth-150-unit*48, cursor+unit*6, unit*15, unit*15);
                    if (!player.getAchievements().contains(Achievement.LongestRoad)) {
                        graphics.fillOval((int)roadButton.getX(), (int)roadButton.getY(), (int)roadButton.getWidth(), (int)roadButton.getHeight());
                        hotZones.add(new HotZone(p,HotZone.ops.LR,roadButton));
                        graphics.setColor(Color.BLACK);
                        graphics.drawString( "R", frameWidth-150-unit*45, cursor+paintHelper.cursorDrop );
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
                    Ellipse2D armyButton = new Ellipse2D.Double(frameWidth-150-unit*32, cursor+unit*6, unit*15, unit*15);
                    graphics.setColor(helper.getGraphicsColor(p.getPlayerColor()));
                    if (!player.getAchievements().contains(Achievement.LargestArmy)) {
                        graphics.fillOval((int)armyButton.getX(), (int)armyButton.getY(), (int)armyButton.getWidth(), (int)armyButton.getHeight());
                        hotZones.add(new HotZone(p,HotZone.ops.LA,armyButton));
                        graphics.setColor(Color.BLACK);
                        graphics.drawString( "A", frameWidth-150-unit*29, cursor+paintHelper.cursorDrop );
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
                    Ellipse2D plusButton = new Ellipse2D.Double(frameWidth-150-unit*16, cursor+unit*6, unit*15, unit*15);
                    graphics.setColor(helper.getGraphicsColor(p.getPlayerColor()));
                    Polygon pol = new Polygon();
                    pol.addPoint(unit*3, 0);
                    pol.addPoint(unit*4, 0);
                    pol.addPoint(unit*4, unit*3);
                    pol.addPoint(unit*7, unit*3);
                    pol.addPoint(unit*7, unit*4);
                    pol.addPoint(unit*4, unit*4);
                    pol.addPoint(unit*4, unit*7);
                    pol.addPoint(unit*3, unit*7);
                    pol.addPoint(unit*3, unit*4);
                    pol.addPoint(0, unit*4);
                    pol.addPoint(0, unit*3);
                    pol.addPoint(unit*3, unit*3);
                    pol.translate((int)(plusButton.getX()+unit*4), (int)(plusButton.getY()+unit*4));
                    graphics.fillOval((int)plusButton.getX(), (int)plusButton.getY(), (int)plusButton.getWidth(), (int)plusButton.getHeight());
                    graphics.setColor(Color.BLACK);
                    graphics.fillPolygon(pol);
                    hotZones.add(new HotZone(p,HotZone.ops.INC,plusButton));
                }
                // Minus box:
                if ((game.getWinner() == null && p.getSettlementVP() > 2) || game.getWinner() == p) {
                    graphics.setColor(helper.getGraphicsColor(p.getPlayerColor()));
                    Ellipse2D box2 = new Ellipse2D.Double(frameWidth-150, cursor+unit*6, unit*15, unit*15);
                    graphics.fillOval((int)box2.getX(), (int)box2.getY(), (int)box2.getWidth(), (int)box2.getHeight());
                    graphics.setColor(Color.BLACK);
                    graphics.fillRect((int)(box2.getX()+unit*4), (int)(box2.getY()+unit*7), unit*7, unit);


                    hotZones.add(new HotZone(player,HotZone.ops.DEC,box2));
                }
        }
        return false;
    }
    public int getX(long time) {
        return 0;
    }
    public int getY(long time) {
        return (lineHeight * game.getLeaderBoard().lastIndexOf(player)); 
    }
    public void setY(int Y) {
        this.y = Y;
    }
    public void invalidate() {
        synchronized(playerNameDirty) {
            playerNameDirty = true;
        }
    }
    public void setEditing(boolean editing) {
        this.editing = editing;
        invalidate();
    }

    /// Player Listener methods

    public void playerVPChanged(PlayerEvent pe) {
        synchronized(playerNameDirty) {
            playerNameDirty = true;
        }
    }

    public void playerRenamed(PlayerEvent pe) {
        synchronized(playerNameDirty) {
            playerNameDirty = true;
        }
    }
    
    // private methods
    // FIXME needs to know when the font's changed...
    private void updateImage(Font bigFont) {
        synchronized(playerNameDirty) {
            String displayName = player.getName();
            //Hacky cursor
            if (editing)
                displayName += "|";

            // Achievements
            if (player.getAchievements().contains(Achievement.LongestRoad))
                displayName += " (LR)";
            if (player.getAchievements().contains(Achievement.LargestArmy))
                displayName += " (LA)";

            Graphics2D graphics = playerName.createGraphics();
            graphics.setFont(bigFont);
            FontMetrics metrics = graphics.getFontMetrics();
            int maxWidth = metrics.stringWidth(displayName);
            letterWidth = metrics.charWidth(helper.getColorChar(player.getPlayerColor()));
            int maxDescent = metrics.getMaxAscent();
            int maxHeight = maxDescent + metrics.getMaxDescent();
            graphics.dispose();
            playerName = new BufferedImage(maxWidth + paintHelper.maxScoreWidth + margin, maxHeight, BufferedImage.TYPE_INT_RGB);
            graphics = playerName.createGraphics();

            graphics.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                         java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                         java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.setColor( helper.getGraphicsColor(player.getPlayerColor()) );
            graphics.setFont(bigFont);
            graphics.drawString( new Integer( player.getVP()).toString(), margin, maxDescent);
            graphics.drawString(displayName, paintHelper.maxScoreWidth + margin, maxDescent);
            playerNameDirty = false;
        }
    }
}
