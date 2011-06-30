import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

class ScoreBoardMouseListener extends MouseInputAdapter {

    private List<ScoreBoard.HotZone> hotZones;
    private ScoreBoard scoreBoard;
    private Game game;
    private Thread controlsHider;
    public ScoreBoardMouseListener(ScoreBoard sb, Game g, List<ScoreBoard.HotZone> hz) {
        this.hotZones = hz;
        this.scoreBoard = sb;
        this.game = g;
        this.controlsHider = new Thread() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        scoreBoard.setShowMouseControls(false);
                    }
                    catch (Exception ex) { 
                    }
                }
            }
        };
 
    }

    public void mouseClicked(MouseEvent me) {
        synchronized(hotZones) {
            for (ScoreBoard.HotZone hz : hotZones) {
                if (hz.zone.contains(me.getPoint())) {
                    if (hz.operation == ScoreBoard.ops.INC) {
                        hz.player.updateVP(+1);
                    }
                    else if (hz.operation == ScoreBoard.ops.DEC) {
                        hz.player.updateVP(-1);
                    }
                    else if (hz.operation == ScoreBoard.ops.LA) {
                        game.setAchievement(hz.player.getPlayerColor(), Achievement.LargestArmy);
                    }
                    else if (hz.operation == ScoreBoard.ops.LR) {
                        game.setAchievement(hz.player.getPlayerColor(), Achievement.LongestRoad);
                    }
                    return;
                }

            }
        }
    }

    public void mouseMoved(MouseEvent me) {
        synchronized(controlsHider) {
            scoreBoard.setShowMouseControls(true);
            if (controlsHider.getState() != Thread.State.NEW) {
                try {
                    controlsHider.interrupt();
                }
                catch (IllegalThreadStateException ex) {
                    // so the thread wasn't running, fair enough.
                }
            }
            else {
                controlsHider.start();
            }
        } 
    }
}
