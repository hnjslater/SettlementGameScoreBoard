package ui.scoreboard;
import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import model.PlayerColor;

/** Class for all the boring methods ScoreBoard needs */
class ScoreBoardHelper {

    private Map<ScoreBoard.ScoreBoardState, String> helpMessages;
    private String colorHelp;

    public ScoreBoardHelper() {
        colorHelp += "[0] No Player [LOWER] +1VP [CAP] -1VP";
        helpMessages = new HashMap<ScoreBoard.ScoreBoardState, String>();
        helpMessages.put(ScoreBoard.ScoreBoardState.DEFAULT, "[a] Set Largest Army [l] Set Longest Road [e] Edit Player Name [+/-] Add/Remove Player [h] help");
        helpMessages.put(ScoreBoard.ScoreBoardState.SELECT_PLAYER_TO_EDIT, "Enter Color of player to edit, hit any non-color key to cancel.");
        helpMessages.put(ScoreBoard.ScoreBoardState.SELECT_PLAYER_TO_DELETE, "Enter Color of player to remove, hit any non-color key to cancel.");
        helpMessages.put(ScoreBoard.ScoreBoardState.SELECT_PLAYER_TO_ADD, "Enter Color of player to add, hit any non-color key to cancel.");
    }

    public PlayerColor getPlayerColor(Collection<PlayerColor> colors, char c) {
    	char clower = Character.toLowerCase(c);
    	for (PlayerColor pc : colors) {
    		if (pc.getChar() == clower)
    			return pc;
    	}
    	return null;
    }

    public char getColorChar(PlayerColor pc) {
        return pc.getChar();
    }

    public Color getGraphicsColor(PlayerColor color) {
    	return color.getColor();        
    }
    public String getHelpMessage(ScoreBoard.ScoreBoardState state) {
        return helpMessages.get(state);
    }

    public String getColorHelp() {
        return colorHelp;
    }
}
