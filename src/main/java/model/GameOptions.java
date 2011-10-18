package model;

import java.util.ArrayList;
import java.util.Collection;

public class GameOptions {
	private final Collection<Achievement> achievements;
	private final Collection<PlayerColor> playerColors;
	
	public GameOptions(Collection<Achievement> achievements, Collection<PlayerColor> playerColors) {
		this.achievements = new ArrayList<Achievement>(achievements);
		this.playerColors = new ArrayList<PlayerColor>(playerColors);
	}
	
	public Collection<Achievement> getAchievements() {
		return achievements;
	}

	public Collection<PlayerColor> getPlayerColors() {
		return playerColors;
	}

}
