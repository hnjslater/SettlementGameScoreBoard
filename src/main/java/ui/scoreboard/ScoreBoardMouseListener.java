package ui.scoreboard;
import model.*;


import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

class ScoreBoardMouseListener extends MouseInputAdapter {

    private List<HotZone> hotZones;
    private ScoreBoard scoreBoard;
    private Game game;
    private Thread controlsHider;
    public ScoreBoardMouseListener(ScoreBoard sb, Game g, List<HotZone> hz) {
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
        try {
        resetHideCounter();
        synchronized(hotZones) {
            for (HotZone hz : hotZones) {
                if (hz.zone.contains(me.getPoint())) {
                    if (hz.operation == HotZone.ops.INC) {
                        hz.player.setVP(hz.player.getSettlementVP()+1);
                    }
                    else if (hz.operation == HotZone.ops.DEC) {
                        hz.player.setVP(hz.player.getSettlementVP()-1);
                    }
                    else if (hz.operation == HotZone.ops.ACH) {
                        if (hz.player != null) {
                            hz.player.add(hz.achievement);
                        }
                        else {
                            game.getPlayers(hz.achievement).get(0).remove(hz.achievement);
                        }
                    }
                    return;
                }
            }
        }
        }
        catch (Exception ex) {
        	ex.printStackTrace();
        	System.out.print(ex.getMessage());
            // Very Iffy....
        }
    }

    private void resetHideCounter() {
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

    public void mouseMoved(MouseEvent me) {
        resetHideCounter();
    }
}
