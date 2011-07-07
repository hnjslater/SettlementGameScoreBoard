import java.util.List;
import java.awt.Font;

/** Encapsulates all the context ScoreBoard shares with its PlayerPainters */
class GUIObjectContext {
    int bigTextHeight;
    int maxScoreWidth;
    int cursorDrop;
    int lineHeight;
    List<HotZone> hotZones;
    int frameWidth;
    int frameHeight;
    Player editing;
    boolean showMouseButtons;
    Font bigFont;
}
