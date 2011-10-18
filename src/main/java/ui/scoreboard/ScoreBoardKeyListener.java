package ui.scoreboard;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import model.Achievement;
import model.Game;
import model.Player;
import ui.scoreboard.ScoreBoard.ScoreBoardState;


class ScoreBoardKeyListener extends KeyAdapter {
	private Game game;
	private ScoreBoard board;
	private ScoreBoardHelper helper; // Sorry

	public ScoreBoardKeyListener(Game game, ScoreBoard board) {
		this.game = game;
		this.board = board;
		this.helper = new ScoreBoardHelper();
	}

	public void keyTyped(KeyEvent e) {
		try {
			if (board.getState() == ScoreBoard.ScoreBoardState.DEFAULT) {

				if (e.getKeyChar() == 's') {
					board.stop();
				}
				else {
					// This will throw if e.getKeyChar isn't a character which maps to a color
					Player p = game.getPlayer(helper.getPlayerColor(game.getPlayerColors(),e.getKeyChar()));

					board.setStateEditing(p);
				}
			}                 
			else if (board.getState() == ScoreBoard.ScoreBoardState.EDIT_PLAYER) {
				if (Character.isUpperCase(e.getKeyChar())) {
					board.achievementUnselected(Achievement.findByChar(game.getAchievements(),e.getKeyChar()));
				}
				else
					board.achievementSelected(Achievement.findByChar(game.getAchievements(),e.getKeyChar()));
			}
                		
		}
		catch (Exception ex) {
			board.setState(ScoreBoardState.DEFAULT);
		}

	} 
}
