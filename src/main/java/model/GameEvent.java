package model;
import java.util.EventObject;

public class GameEvent extends EventObject {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Game game;
    private Player player;

    public GameEvent(Game game, Player player) {
        super(game);
        this.game = game;
        this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public Player getPlayer() {
        return player;
    }
}
