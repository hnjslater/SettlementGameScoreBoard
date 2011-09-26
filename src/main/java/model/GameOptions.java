package model;

import java.util.Collection;
import java.util.TreeSet;

public class GameOptions {
	private final Collection<Achievement> achievements;
	private final Collection<PlayerColor> playerColors;
	
	public GameOptions(Collection<Achievement> achievements, Collection<PlayerColor> playerColors) {
		this.achievements = new TreeSet<Achievement>(achievements);
		this.playerColors = new TreeSet<PlayerColor>(playerColors);
	}
	
	public Collection<Achievement> getAchievements() {
		return achievements;
	}

	public Collection<PlayerColor> getPlayerColors() {
		return playerColors;
	}

}
