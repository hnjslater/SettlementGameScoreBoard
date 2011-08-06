package ui;

import model.Game;
import ui.scoreboard.ScoreBoard;
import ui.setupscreen.SetupScreen;

import javax.swing.JFrame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class Controller {
    JFrame frame;
    Game game;
    ScoreBoard scoreBoard;
    SetupScreen setupScreen;
    boolean fullscreen;
    Object frameLock;
    public Controller(Game g) {
        this.frameLock = new Object();
        this.game = g;
        this.scoreBoard = new ScoreBoard(this, g);
        this.setupScreen = new SetupScreen(this, g);
    }

    public void run() throws InterruptedException {
        setFullScreen(true);
        while (true) {
            setupScreen.run();
            scoreBoard.run();
        }
    }

    public JFrame getJFrame() {
        return frame;
    }

    public void setFullScreen(boolean fullscreen) {
        synchronized(frameLock) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gs = ge.getDefaultScreenDevice();

            if (frame == null || this.fullscreen != (fullscreen && gs.isFullScreenSupported())) {
                // Firstly lets tidy up that old frame:
                if (frame != null) {
                    frame.setVisible(false);
                    frame.dispose();
                    frame = null;
                }

                // Now create a new one:

                frame = new JFrame(gs.getDefaultConfiguration());

                if (fullscreen) {
                    frame.setUndecorated(true);
                    frame.setResizable(false);
                    frame.setIgnoreRepaint(true);
                    gs.setFullScreenWindow(frame);
                    this.fullscreen = true;
                }
                else {
                    frame.setSize(800,600);
                    frame.setResizable(true);
                    frame.setIgnoreRepaint(false);
                    frame.setUndecorated(false);
                    frame.setVisible(true);
                    this.fullscreen = false;
                }
                frame.createBufferStrategy(2);
            }
        }
    }

    public Object getFrameLock() {
        return frameLock;
    }
}
