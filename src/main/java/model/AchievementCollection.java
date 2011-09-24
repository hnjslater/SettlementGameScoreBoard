package model;

import java.util.ArrayList;
import java.util.Collection;

public class AchievementCollection extends ArrayList<Achievement> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public AchievementCollection(Collection<Achievement> achievements) {
		super(achievements);
	}

	public Achievement findByChar(char c) {
		for (Achievement a : this)
			if (a.getCharacter() == c)
				return a;
		return null;
 	}
	
	public Achievement LongestRoad() {
		return findByChar('l');
	}
	public Achievement LargestArmy() {
		return findByChar('a');
	}
}
