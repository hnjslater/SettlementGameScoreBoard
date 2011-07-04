import java.util.List;

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
}
