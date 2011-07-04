import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Color;
import java.awt.geom.*;
import java.awt.Shape;
import java.util.List;



class PlayerPainter extends GUIObject {
    public Player player;
    private Game game;
    private ScoreBoardHelper helper = new ScoreBoardHelper();
    private Font bigFont = new Font( "Helvectica", 0, 64 );
    private GUIObjectContext context;

    public PlayerPainter(Player p, Game g, GUIObjectContext context) {
        super(0,0,0,0);
        this.player = p;
        this.game = g;
        this.context = context;
    }
    public boolean paint(Graphics graphics, long time) {
        int cursor = getY(0);
        Player p = player;

        graphics.setFont( bigFont );

        graphics.setColor( helper.getGraphicsColor(player.getPlayerColor()) );
        graphics.drawString( new Integer( player.getVP()).toString(), 50, cursor+context.cursorDrop );
        String displayName = player.getName();

        //Hacky cursor
        if (context.editing == p)
            displayName += "|";

        // Achievements
        if (player.getAchievements().contains(Achievement.LongestRoad))
            displayName += " (LR)";
        if (player.getAchievements().contains(Achievement.LargestArmy))
            displayName += " (LA)";

        graphics.drawString( displayName, 50+context.maxScoreWidth, cursor+context.cursorDrop );
        graphics.drawChars( new char[] {helper.getColorChar(player.getPlayerColor())}, 0, 1, context.frameWidth-70, cursor+context.cursorDrop );

        if (context.showMouseButtons) {
                int unit = context.lineHeight/23;

                if (game.getWinner() == null) {
                    // Longest Road button

                    Ellipse2D roadButton = new Ellipse2D.Double(context.frameWidth-150-unit*48, cursor+unit*6, unit*15, unit*15);
                    if (!player.getAchievements().contains(Achievement.LongestRoad)) {
                        graphics.fillOval((int)roadButton.getX(), (int)roadButton.getY(), (int)roadButton.getWidth(), (int)roadButton.getHeight());
                        context.hotZones.add(new HotZone(p,HotZone.ops.LR,roadButton));
                        graphics.setColor(Color.BLACK);
                        graphics.drawString( "R", context.frameWidth-150-unit*45, cursor+context.cursorDrop );
                    }
                    else {
                        Stroke normal = ((Graphics2D)graphics).getStroke();
                        Stroke wider = new BasicStroke(4);
                        ((Graphics2D)graphics).setStroke(wider);
                        graphics.drawOval((int)roadButton.getX(), (int)roadButton.getY(), (int)roadButton.getWidth(), (int)roadButton.getHeight());
                        ((Graphics2D)graphics).setStroke(normal);
                        context.hotZones.add(new HotZone(null,HotZone.ops.LR,roadButton));
                    }
                   

                    // Largest Army button
                    Ellipse2D armyButton = new Ellipse2D.Double(context.frameWidth-150-unit*32, cursor+unit*6, unit*15, unit*15);
                    graphics.setColor(helper.getGraphicsColor(p.getPlayerColor()));
                    if (!player.getAchievements().contains(Achievement.LargestArmy)) {
                        graphics.fillOval((int)armyButton.getX(), (int)armyButton.getY(), (int)armyButton.getWidth(), (int)armyButton.getHeight());
                        context.hotZones.add(new HotZone(p,HotZone.ops.LA,armyButton));
                        graphics.setColor(Color.BLACK);
                        graphics.drawString( "A", context.frameWidth-150-unit*29, cursor+context.cursorDrop );
                    }
                    else {
                        Stroke normal = ((Graphics2D)graphics).getStroke();
                        Stroke wider = new BasicStroke(4);
                        ((Graphics2D)graphics).setStroke(wider);
                        graphics.drawOval((int)armyButton.getX(), (int)armyButton.getY(), (int)armyButton.getWidth(), (int)armyButton.getHeight());
                        ((Graphics2D)graphics).setStroke(normal);
                        context.hotZones.add(new HotZone(null,HotZone.ops.LA,armyButton));
                    }

                    // Plus box:
                    graphics.setColor(helper.getGraphicsColor(p.getPlayerColor()));
                    Ellipse2D plusButton = new Ellipse2D.Double(context.frameWidth-150-unit*16, cursor+unit*6, unit*15, unit*15);
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
                    context.hotZones.add(new HotZone(p,HotZone.ops.INC,plusButton));
                }
                // Minus box:
                if ((game.getWinner() == null && p.getSettlementVP() > 2) || game.getWinner() == p) {
                    graphics.setColor(helper.getGraphicsColor(p.getPlayerColor()));
                    Ellipse2D box2 = new Ellipse2D.Double(context.frameWidth-150, cursor+unit*6, unit*15, unit*15);
                    graphics.fillOval((int)box2.getX(), (int)box2.getY(), (int)box2.getWidth(), (int)box2.getHeight());
                    graphics.setColor(Color.BLACK);
                    graphics.fillRect((int)(box2.getX()+unit*4), (int)(box2.getY()+unit*7), unit*7, unit);


                    context.hotZones.add(new HotZone(player,HotZone.ops.DEC,box2));
                }
        }
        return false;
    }
    public int getX(long time) {
        return 0;
    }
    public int getY(long time) {
        return (context.lineHeight * game.getLeaderBoard().lastIndexOf(player)); 
    }
    public void setY(int Y) {
        this.y = Y;
    }
}
