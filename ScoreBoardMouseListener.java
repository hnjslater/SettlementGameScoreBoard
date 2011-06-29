import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

class ScoreBoardMouseListener extends MouseAdapter {

    private List<ScoreBoard.HotZone> hotZones;

    public ScoreBoardMouseListener(List<ScoreBoard.HotZone> hz) {
        this.hotZones = hz;
    }

    public void mouseClicked(MouseEvent me) {
        synchronized(hotZones) {
            for (ScoreBoard.HotZone hz : hotZones) {
                if (hz.zone.contains(me.getPoint())) {
                    if (hz.operation == ScoreBoard.ops.INC)
                        hz.player.updateVP(+1);
                    else
                        hz.player.updateVP(-1);
                }

            }
        }
    }
}
