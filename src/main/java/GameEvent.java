import java.util.EventObject;

class GameEvent extends EventObject {
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
