package ui.scoreboard;
import model.*;


class PlayerPainterFactory {
    private PlayerPainterFontHelper helper = new PlayerPainterFontHelper();
    public PlayerPainter getPainter(Player p, Game g) {
        return new PlayerPainter(p,g,helper);
    }
}
