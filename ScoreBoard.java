import java.awt.Color;
import java.util.*;
import java.awt.font.TextAttribute;
import java.awt.RenderingHints;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Font;
import java.awt.image.BufferStrategy;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import javax.swing.JFrame;

final class ScoreBoard implements GameListener {
 
    public enum ScoreBoardState {
        DEFAULT,
        SELECT_PLAYER_TO_EDIT,
        SELECT_PLAYER_TO_DELETE,
        SELECT_PLAYER_TO_ADD,
        EDIT_PLAYER
    }
    private String helpMessage;
    private PlayerColor editingPlayer;
    private JFrame frame;
    private Game game;
    private ScoreBoardHelper helper = new ScoreBoardHelper();
    private KeyListener controller;
    private MouseListener mouseController;
    private boolean showHelp = true;
    private boolean showColorHelp = true;
    private boolean fullScreen = false;
    private Player winner;
    private List<GUIObject> guiObjects;
    private List<HotZone> hotZones;

 
    public enum ops {INC, DEC};
    public class HotZone {
        public HotZone(Player p, ops o, Shape z) {
            this.player = p;
            this.operation = o;
            this.zone = z;
        }
        public ops operation;
        public Player player;
        public Shape zone;
    }

    
    // might as well cache these;
    private Font bigFont = new Font( "Helvectica", 0, 64 );
    private Font littleFont = new Font( "Courier", 0, 22 );

    public ScoreBoard(Game game) {
        this.game = game;
        game.addGameListener(this);
        this.helpMessage = helper.getHelpMessage(ScoreBoardState.DEFAULT);
        this.controller = new ScoreBoardKeyListener(game, this);

        this.hotZones = Collections.synchronizedList(new LinkedList<HotZone>()); 
        this.mouseController = new ScoreBoardMouseListener(hotZones);

        toggleFullScreen();

        guiObjects = Collections.synchronizedList(new LinkedList<GUIObject>());
   }

    public void setShowHelp(boolean showHelp) {
        this.showHelp = showHelp;
    }

    public boolean getShowHelp() {
        return showHelp;
    }
    
    public void setShowColorHelp(boolean showHelp) {
        this.showColorHelp = showHelp;
    }

    public boolean getShowColorHelp() {
        return showColorHelp;
    }

    public void setState(ScoreBoardState state) {
        this.helpMessage = helper.getHelpMessage(state);
        this.editingPlayer = PlayerColor.None;
    }

    public void setStateAchievement(Achievement achievement) {
        this.helpMessage = "Enter Color of player who has the " + achievement.toString() + ".";
        this.editingPlayer = PlayerColor.None;
    }

    public void setStateEditing(PlayerColor playerColor) {
        this.helpMessage = "Editing " + playerColor.toString() + ".";
        this.editingPlayer = playerColor;
    }

    public void renderLoop() {
        while (true) {
            paint();
        }
    }

    public void tickLoop() {
        while (true) {

            int width = frame.getWidth();
            int height = frame.getHeight();


            long time = (new Date()).getTime();
            synchronized(guiObjects) { 
                List<GUIObject> toremove = new ArrayList<GUIObject>();
                for (GUIObject p : guiObjects) {
                    if (p.getY(time) > height || p.getX(time) < 0 || p.getX(time) > width) {
                        toremove.add(p);
                    }
                }
                guiObjects.removeAll(toremove);
            }


            if (guiObjects.size() < 20  && winner != null) {
                int startx = (int)(Math.floor(Math.random() * width));
                int starty = (int)(Math.floor(Math.random() * height));
                for (int i = 0; i < 30; i++) {
                    guiObjects.add(new Particle(startx,starty,helper.getGraphicsColor(winner.getPlayerColor()),time));
                }
            }

            try {
                Thread.sleep(10);
            }
            catch (InterruptedException ex) {
                // If we've been interrupted, do the update;
            }
        }
    }

    public void run() throws InterruptedException {
        Thread graphics = new Thread() {
            public void run() {
                tickLoop();
            }
        };

        Thread ticker = new Thread() {
            public void run() {
                renderLoop();
            }
        };

        graphics.start();
        ticker.start();
        graphics.join();
        ticker.join();
    }

    public synchronized void toggleFullScreen() {

        // To be honest, this is all a bit messy.
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        if (frame != null) {
            frame.setVisible(false);
            gs.setFullScreenWindow(null);
            frame.dispose();
        }

        frame = new JFrame(gs.getDefaultConfiguration());
        frame.addKeyListener(controller);
        frame.addMouseListener(mouseController);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.validate();

        if (gs.isFullScreenSupported() && !fullScreen) {
            frame.setUndecorated(true);
            frame.setResizable(false);
            frame.setIgnoreRepaint(true);
            gs.setFullScreenWindow(frame);

            fullScreen = true;
        }
        else {
            frame.setSize(800,600);
            frame.setResizable(true);
            frame.setIgnoreRepaint(false);
            frame.setUndecorated(false);
            frame.setVisible(true);

            fullScreen = false;
        }
        frame.createBufferStrategy(2);
    }


    private synchronized void paint () {
        int height = frame.getHeight();
        int width = frame.getWidth();
        // FIXME a bit iffy if not fullscreen

        BufferStrategy bf = frame.getBufferStrategy();
        Graphics graphics = bf.getDrawGraphics();


        // background
        graphics.setColor( java.awt.Color.BLACK );
        graphics.fillRect( 0, 0, width,height );

        // antialiasing
        ((Graphics2D)graphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                         RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ((Graphics2D)graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);


        // Not sure if I should be caching these? 
        int bigTextHeight = (int)(graphics.getFontMetrics(bigFont).getMaxCharBounds(graphics).getHeight());
        int littleTextHeight = (int)(graphics.getFontMetrics(littleFont).getMaxCharBounds(graphics).getHeight());
        int littleTextDescent = (int)(graphics.getFontMetrics(littleFont).getMaxDescent());
        int lineHeight = 0;
        if (game.getNumberOfPlayers() > 0) {
            if (showHelp && guiObjects.size() == 0) {
                lineHeight = (height - (littleTextHeight * 2)) / (game.getNumberOfPlayers());
            }
            else {
                lineHeight = height / game.getNumberOfPlayers();
            }
        }
        int hozOffset = 0; 
        int scoreWidth = graphics.getFontMetrics(bigFont).stringWidth("00 "); 

        graphics.setFont( bigFont );

        PlayerColor longestRoad = game.getAchievement(Achievement.LongestRoad);
        PlayerColor largestArmy = game.getAchievement(Achievement.LargestArmy);

        // Put the cursor in the right place
        int cursorDrop =  (int)(graphics.getFontMetrics(bigFont).getMaxAscent()) + (lineHeight - bigTextHeight) / 2;;
        int cursor = hozOffset;
        synchronized(hotZones) { // otherwise sometimes a click will happen when hotZones is empty
        hotZones.clear();
        for ( Player p : game.getLeaderBoard() ) {
            graphics.setColor( helper.getGraphicsColor(p.getPlayerColor()) );
            graphics.drawString( new Integer( p.getVP()).toString(), 50, cursor+cursorDrop );
            String displayName = p.getName();

            // Hacky cursor
            if (editingPlayer == p.getPlayerColor())
                displayName += "|";

            // Achievements
            if (p.getPlayerColor() == longestRoad)
                displayName += " (LR)";
            if (p.getPlayerColor() == largestArmy)
                displayName += " (LA)";

            // Finally the player name!
            graphics.drawString( displayName, 50+scoreWidth, cursor+cursorDrop );

            if (getShowColorHelp()) {
                graphics.drawChars( new char[] {helper.getColorChar(p.getPlayerColor())}, 0, 1, width-70, cursor+cursorDrop );
            }


            //FIXME all of the following code really....
            // Plus box:
            int unit = lineHeight/21;
            Ellipse2D box = new Ellipse2D.Double(width-150-unit*7-5, cursor+lineHeight/3-5, unit*7+10, unit*7+10);
            Polygon pol = new Polygon();
            pol.addPoint(unit*3,    0);
            pol.addPoint(unit*4,    0);
            pol.addPoint(unit*4,    unit*3);
            pol.addPoint(unit*7,    unit*3);
            pol.addPoint(unit*7,    unit*4);
            pol.addPoint(unit*4,    unit*4);
            pol.addPoint(unit*4,    unit*7);
            pol.addPoint(unit*3,    unit*7);
            pol.addPoint(unit*3,    unit*4);
            pol.addPoint(0,         unit*4);
            pol.addPoint(0,         unit*3);
            pol.addPoint(unit*3,    unit*3);
            pol.translate((int)(box.getX()+5), (int)(box.getY()+5));
            graphics.fillOval((int)box.getX(), (int)box.getY(), (int)box.getWidth(), (int)box.getHeight());
            graphics.setColor(Color.BLACK);
            graphics.fillPolygon(pol);


            hotZones.add(new HotZone(p,ops.INC,box));
            graphics.setColor(helper.getGraphicsColor(p.getPlayerColor()));
            // Minus box:
            Ellipse2D box2 = new Ellipse2D.Double(width-150+10, cursor+lineHeight/3-5, unit*7+10, unit*7+10);
            graphics.fillOval((int)box2.getX(), (int)box2.getY(), (int)box2.getWidth(), (int)box2.getHeight());
            graphics.setColor(Color.BLACK);
            graphics.fillRect((int)(box2.getX()+5), (int)(box2.getY()+5+unit*3), unit*7, unit);


            hotZones.add(new HotZone(p,ops.DEC,box2));


            cursor += lineHeight;
        }
        }

        if (showHelp && guiObjects.size() == 0) {
            graphics.setColor( Color.WHITE );
            graphics.setFont(littleFont);
            graphics.drawString( helpMessage, 0, height-littleTextDescent-littleTextHeight);
            graphics.drawString( helper.getColorHelp(), 0, height-littleTextDescent);
        }
        long now = (new Date()).getTime();
        synchronized(guiObjects) { 
            for (GUIObject p : guiObjects) {
                p.paint(graphics, now);
            }
        }

        graphics.dispose();
        bf.show();
    }

    public void winnerChanged(WinnerChangedEvent wce) {
        winner = wce.getPlayer();
    }
}

