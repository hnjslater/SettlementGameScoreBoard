package ui.scoreboard;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.event.MouseInputListener;

import model.Achievement;
import model.Game;
import model.GameEvent;
import model.GameListener;
import model.Player;
import model.PlayerColor;

public class ScoreBoard implements GameListener {
 
    public enum ScoreBoardState {
        DEFAULT,
        SELECT_PLAYER_TO_EDIT,
        SELECT_PLAYER_TO_DELETE,
        SELECT_PLAYER_TO_ADD,
        EDIT_PLAYER
    }
    private JFrame frame;
    private ui.Controller controller;
    private Game game;
    private ScoreBoardHelper helper = new ScoreBoardHelper();
    private KeyListener keyController;
    private MouseInputListener mouseController;
    private boolean showHelp = false;
    private boolean showColorHelp = true;
    private boolean showMouseControls = false;
    private boolean fullScreen = false;
    private boolean isAnimating = true;
    private Player winner;
    private final Object winnerLock = new Object();
    private List<GUIObject> guiObjects;
    private List<HotZone> hotZones;
    private PlayerPainterFactory factory;

    private volatile boolean running;
    private Thread graphicsThread;
    private Thread tickThread;

 
   

    
    // might as well cache these;
    private Font bigFont = new Font( "Helvectica", 0, 64 );

    public ScoreBoard(ui.Controller controller, Game game) {
        this.game = game;
        this.controller = controller;

        guiObjects = Collections.synchronizedList(new LinkedList<GUIObject>());
        this.factory = new PlayerPainterFactory();


        for (Player player : game.getLeaderBoard()) {
            guiObjects.add(factory.getPainter(player, game));
        }
        game.addGameListener(this);
        helper.getHelpMessage(ScoreBoardState.DEFAULT);
        this.keyController = new ScoreBoardKeyListener(game, this);

        this.hotZones = Collections.synchronizedList(new LinkedList<HotZone>()); 
        this.mouseController = new ScoreBoardMouseListener(this,game,hotZones);
        this.running = false;
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

    public void setShowMouseControls(boolean showMouseControls) {
        synchronized(guiObjects) {
            for (GUIObject o : guiObjects) {
                o.setShowMouseAffordances(showMouseControls);
            }
        }
    }

    public boolean getShowMouseControls() {
        return showMouseControls;
    }
    public void setState(ScoreBoardState state) {
        helper.getHelpMessage(state);
        finishEditing();
    }

    public void setStateAchievement(Achievement achievement) {
        //"Enter Color of player who has the " + achievement.toString() + ".";
        finishEditing();
    }

    public void setStateEditing(PlayerColor playerColor) {
        //"Editing " + playerColor.toString() + ".";

        PlayerPainter painter = null;
        synchronized(guiObjects) {
            for (GUIObject o : guiObjects) {
                if (o instanceof PlayerPainter && ((PlayerPainter)o).player.getPlayerColor() ==  playerColor) {
                    painter = (PlayerPainter)o;
                }
            }
        }
        painter.setEditing(true);
    }

    private void finishEditing() {
        synchronized(guiObjects) {
            for (GUIObject o : guiObjects) {
                if (o instanceof PlayerPainter) {
                    ((PlayerPainter)o).setEditing(false);
                }
            }
        }
    }

    public void renderLoop() {
        while (running) {
            paint();
            // limit framerate if there's no animation currently happening
            try {
                if (!isAnimating) {
                    Thread.sleep(100);
                }
                else {
                    Thread.yield();
                }
            }
            catch (Exception ex) {
                // Don't really care if I get woken.
            }
        }
    }
    public void tickLoop() {
        int width;
        int height;
        while (running) {
            synchronized(controller.getFrameLock()) { 
                width = frame.getContentPane().getWidth();
                height = frame.getContentPane().getHeight();
            }


            long time = (new Date()).getTime();
            synchronized(guiObjects) { 

                List<GUIObject> toremove = new ArrayList<GUIObject>();
                for (GUIObject p : guiObjects) {
                    if ((p.getY(time) > height || p.getX(time) < 0 || p.getX(time) > width) && p instanceof Particle) {
                        toremove.add(p);
                    }
                }
                guiObjects.removeAll(toremove);
            }

            synchronized(winnerLock) {
                if (guiObjects.size() < 20  && winner != null) {
                    int startx = (int)(Math.floor(Math.random() * width));
                    int starty = (int)(Math.floor(Math.random() * height));
                    for (int i = 0; i < 30; i++) {
                        guiObjects.add(new Particle(startx,starty,helper.getGraphicsColor(winner.getPlayerColor()),time));
                    }
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
        running = true;

        
        graphicsThread = new Thread() {
            public void run() {
                tickLoop();
            }
        };

        tickThread = new Thread() {
            public void run() {
                renderLoop();
            }
        };
        fullScreen = controller.setFullScreen(true);
        frame = controller.getJFrame();
        frame.addKeyListener(keyController);
        frame.addMouseListener(mouseController);
        frame.addMouseMotionListener(mouseController);
        graphicsThread.start();
        tickThread.start();
        graphicsThread.join();
        tickThread.join();
        frame.removeKeyListener(keyController);
        frame.removeMouseListener(mouseController);
        frame.removeMouseMotionListener(mouseController);

    }

    public void stop() {
        synchronized(controller.getFrameLock()) {
            frame.removeMouseListener(mouseController);
            frame.removeMouseMotionListener(mouseController);
            frame.removeKeyListener(keyController);
            running = false;
        }
    }
/*
    public synchronized void toggleFullScreen() {
        synchronized(controller.getFrameLock()) {
            controller.setFullScreen(!fullScreen);
            this.fullScreen = !fullScreen;
            frame = controller.getJFrame();
            frame.addKeyListener(keyController);
            frame.addMouseListener(mouseController);
            frame.addMouseMotionListener(mouseController);
        }
    }
*/

    private synchronized void paint () {
        BufferStrategy bf = frame.getBufferStrategy();
        Graphics graphics = bf.getDrawGraphics();

        // background
        if (game.getWinner() != null && game.getWinner().getPlayerColor().equals(PlayerColor.Black)) {
            graphics.setColor( java.awt.Color.WHITE );
        }
        else {
            graphics.setColor( java.awt.Color.BLACK );
        }
        graphics.fillRect( 0, 0, frame.getWidth(),frame.getHeight() );

        // FIXME a bit iffy if not fullscreen
        if (!fullScreen) {
            graphics.translate(0, frame.getHeight() - frame.getContentPane().getHeight());
        }

        // antialiasing
        ((Graphics2D)graphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                         RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ((Graphics2D)graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setFont(bigFont);

        long now = (new Date()).getTime();

        synchronized(guiObjects) { 
            isAnimating = false;
            synchronized(hotZones) { // otherwise sometimes a click will happen when hotZones is empty
                hotZones.clear();
                for (GUIObject p : guiObjects) {
                    isAnimating |= p.paint(graphics, now, frame.getContentPane().getWidth(), frame.getContentPane().getHeight(), hotZones);
                }
            }
        }
        graphics.dispose();
        bf.show();
    }

    public void winnerChanged(GameEvent wce) {
	synchronized (winnerLock) {
	boolean bgColorChanged = false;
	if ((winner != null && winner.getPlayerColor().equals(PlayerColor.Black)) || (wce.getPlayer() != null && wce.getPlayer().getPlayerColor().equals(PlayerColor.Black)))
	    bgColorChanged = true;
        winner = wce.getPlayer();
        if (bgColorChanged) {
            Player white = game.getPlayer(PlayerColor.White);
            if (white != null) {
                synchronized(guiObjects) {
                    for (GUIObject o : guiObjects) {
                        if (o instanceof PlayerPainter && ((PlayerPainter)o).player.equals(white)) {
                            o.invalidate();
                        }
                    }
                }
            }
        	
        }
	}
            
    }

    public void playerRemoved(GameEvent e) {
        PlayerPainter toRemove = null;
        synchronized(guiObjects) {
            for (GUIObject o : guiObjects) {
                if (o instanceof PlayerPainter && ((PlayerPainter)o).player.equals(e.getPlayer())) {
                    toRemove = (PlayerPainter)o;
                }
            }
        }
        if (toRemove != null) {
            guiObjects.remove(toRemove);
        }
        synchronized(guiObjects) {
            for (GUIObject o : guiObjects) {
                o.invalidate();
            }
        }
    }

    public void playerAdded(GameEvent e) {
        guiObjects.add(factory.getPainter(e.getPlayer(), game));
        synchronized(guiObjects) {
            for (GUIObject o : guiObjects) {
                o.invalidate();
            }
        }
    }
       
}
