package model;
import java.util.EventObject;

public class GameEvent extends EventObject {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private final Game game;
    private final Player player;

    public GameEvent(Game game, Player player) {
        super(game);
        
        // this is actually checked by EventObject but doesn't hurt to be explicit.
        if (game == null)
            throw new RuntimeException("Game cannot be null in GameEvent");
        this.game = game;
        this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public Player getPlayer() {
        return player;
    }
    
    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
	if (obj == this)
	    return true;
	
	if (!(obj instanceof GameEvent))
	    return false;
	
	GameEvent other = (GameEvent)obj; 
	
	if (!this.game.equals(other.game)) {
	    return false;
	}
	
	if (this.player == null) {
	    if (other.player != null) {
		return false;
	    }
	}
	else if (!this.player.equals(other.player)) {
	    return false;
	}
	
	return true;
    }
}
