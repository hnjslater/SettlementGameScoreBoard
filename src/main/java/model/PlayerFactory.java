package model;

import java.util.concurrent.atomic.AtomicInteger;

public interface PlayerFactory {
    public Player createPlayer(PlayerColor playerCOlor, AtomicInteger integer, GameConstraints constraints);
}
