package ui.scoreboard;
import model.*;


import java.awt.Color;
import java.util.*;

/** Class for all the boring methods ScoreBoard needs */
class ScoreBoardHelper {

    private Map<Character, PlayerColor> charToPlayerColor;
    private Map<PlayerColor, Character> playerColorToChar;
    private Map<ScoreBoard.ScoreBoardState, String> helpMessages;
    private String colorHelp;

    public ScoreBoardHelper() {
        charToPlayerColor = new HashMap<Character, PlayerColor>();
        playerColorToChar = new HashMap<PlayerColor, Character>();
        colorHelp="";

        charToPlayerColor.put('r', PlayerColor.Red);
        charToPlayerColor.put('n', PlayerColor.Brown);
        charToPlayerColor.put('g', PlayerColor.Green);
        charToPlayerColor.put('o', PlayerColor.Orange);
        charToPlayerColor.put('w', PlayerColor.White);
        charToPlayerColor.put('b', PlayerColor.Blue);
        charToPlayerColor.put('d',PlayerColor.Wood);
        charToPlayerColor.put('k', PlayerColor.Black);

        for (Map.Entry<Character,PlayerColor> e : charToPlayerColor.entrySet() ) {
            playerColorToChar.put(e.getValue(),e.getKey());
            colorHelp += "[" + e.getKey() + "] " + e.getValue() + " ";
        }
        colorHelp += "[0] No Player [LOWER] +1VP [CAP] -1VP";
        helpMessages = new HashMap<ScoreBoard.ScoreBoardState, String>();
        helpMessages.put(ScoreBoard.ScoreBoardState.DEFAULT, "[a] Set Largest Army [l] Set Longest Road [e] Edit Player Name [+/-] Add/Remove Player [h] help");
        helpMessages.put(ScoreBoard.ScoreBoardState.SELECT_PLAYER_TO_EDIT, "Enter Color of player to edit, hit any non-color key to cancel.");
        helpMessages.put(ScoreBoard.ScoreBoardState.SELECT_PLAYER_TO_DELETE, "Enter Color of player to remove, hit any non-color key to cancel.");
        helpMessages.put(ScoreBoard.ScoreBoardState.SELECT_PLAYER_TO_ADD, "Enter Color of player to add, hit any non-color key to cancel.");
    }

    public PlayerColor getPlayerColor(char c) {
        return charToPlayerColor.get(Character.toLowerCase(c));
    }

    public char getColorChar(PlayerColor pc) {
        return playerColorToChar.get(pc);
    }

    public Color getGraphicsColor(PlayerColor color) {
        switch (color) {
            case Red:
                return Color.RED;
            case Brown:
                return new Color(119,49,9);
            case Blue:
                return Color.BLUE;
            case White:
                return Color.WHITE;
            case Green:
                return Color.GREEN;
            case Orange:
//                return new Color(204,85,0);
                return Color.ORANGE;
            case Wood:
        	return new Color(228, 217, 111);
            case Black:
        	return Color.BLACK;
            default: 
                return new Color(200,200,200);
        }
    }
    public String getHelpMessage(ScoreBoard.ScoreBoardState state) {
        return helpMessages.get(state);
    }

    public String getColorHelp() {
        return colorHelp;
    }
}
