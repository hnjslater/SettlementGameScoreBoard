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
    final Object frameLock;
    public Controller(Game g) {
        this.frameLock = new Object();
        this.game = g;
        this.scoreBoard = new ScoreBoard(this, g);
        this.setupScreen = new SetupScreen(this, g);
    }

    public void run() throws InterruptedException {
        while (true) {
            setupScreen.run();
            scoreBoard.run();
        }
    }

    public JFrame getJFrame() {
        return frame;
    }

    public boolean setFullScreen(boolean fullscreen) {
        synchronized(frameLock) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            
            boolean fsTargetState = this.fullscreen != (fullscreen && gs.isFullScreenSupported());

            if (frame == null || this.fullscreen != fsTargetState) {
                // Firstly lets tidy up that old frame:
                if (frame != null) {
                    frame.setVisible(false);
                    frame.dispose();
                    frame = null;
                }

                // Now create a new one:
                //  There appears to be no way to go from fullscreen to a normal window
                //  without destroying the window (setUndecorated throws an exception)

                frame = new JFrame(gs.getDefaultConfiguration());
                frame.setTitle("Settlement Game Score Board");
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
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
            return fsTargetState;
        }
    }

    public Object getFrameLock() {
        return frameLock;
    }
}
