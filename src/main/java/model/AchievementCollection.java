package model;

import java.util.ArrayList;

public class AchievementCollection extends ArrayList<Achievement> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public AchievementCollection() {
	}

	public Achievement findByChar(char c) {
		for (Achievement a : this)
			if (a.getCharacter() == c)
				return a;
		return null;
 	}
}
