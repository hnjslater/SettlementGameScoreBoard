package ui.scoreboard;
import model.*;


class PlayerPainterFactory {
    private PlayerPainterFontHelper helper = new PlayerPainterFontHelper();
    public PlayerPainter getPainter(Player p, Game g, ScoreBoard b) {
        return new PlayerPainter(p,g,b,helper);
    }
}
