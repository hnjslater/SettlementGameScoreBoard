package model;

public interface GameListener {
    public void playerAdded(GameEvent e);
    public void playerRemoved(GameEvent e);
    public void winnerChanged(GameEvent e);
}
