package ui.scoreboard;
import model.*;


import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

class ScoreBoardMouseListener extends MouseInputAdapter {

    private List<HotZone> hotZones;
    @SuppressWarnings("unused")
	private ScoreBoard scoreBoard;
    @SuppressWarnings("unused")
	private Game game;
    public ScoreBoardMouseListener(ScoreBoard sb, Game g, List<HotZone> hz) {
        this.hotZones = hz;
        this.scoreBoard = sb;
        this.game = g;
 
    }

    public void mouseClicked(MouseEvent me) {
        try {
        	synchronized(hotZones) {
        		for (HotZone hz : hotZones) {
        		}
        	}
        }
        catch (Exception ex) {
        	ex.printStackTrace();
        	System.out.print(ex.getMessage());
            // Very Iffy....
        }
    }
}
