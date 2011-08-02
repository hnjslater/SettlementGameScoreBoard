import java.util.EventObject;

class PlayerEvent extends EventObject {
    private Player player;

    public PlayerEvent(Player player) {
        super(player);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}

