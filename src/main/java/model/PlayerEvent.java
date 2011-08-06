package model;
import java.util.EventObject;

public class PlayerEvent extends EventObject {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Player player;

    public PlayerEvent(Player player) {
        super(player);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}

