package model;

import java.util.Collection;

public class Achievement implements Comparable<Achievement> {
	private String name;
	private int victory_points;
	private String short_name;
	private char character;
	private String ID;
	private int max_in_game;
	public Achievement(String name, int victory_points, String short_name, char character, String ID, int max_in_game) {
		this.name = name.intern();
		this.victory_points = victory_points;
		this.short_name = short_name.intern();
		this.character = character;
		this.ID = ID.intern();
		this.max_in_game = max_in_game;
	}

	public String getName() {
		return name;
	}
	
	public int getVictoryPoints() {
		return victory_points;
	}
	
	public String getShortName() {
		return short_name;
	}
	
	public char getCharacter() {
		return character;
	}
	
	public int getMaxInGame() {
		return this.max_in_game;
	}
	
	public String getID() {
		return this.ID;
	}
	
	public static Achievement valueOf(Collection<Achievement> achievements, String a) {
		for (Achievement ai : achievements) {
			if (ai.getID().equals(a)) {
				return ai;
			}
		}
		for (Achievement ai : achievements) {
			if (ai.getName().equals(a)) {
				return ai;
			}
		}
		return null;
	}

	@Override
	public int compareTo(Achievement arg0) {
		return this.getName().compareTo(arg0.getName());
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Achievement)) {
			return false;
		}
		Achievement other = (Achievement) obj;
		if (this.ID == null) {
			if (other.ID != null) {
				return false;
			}
		} else if (!this.ID.equals(other.ID)) {
			return false;
		}
		if (this.character != other.character) {
			return false;
		}
		if (this.max_in_game != other.max_in_game) {
			return false;
		}
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.short_name == null) {
			if (other.short_name != null) {
				return false;
			}
		} else if (!this.short_name.equals(other.short_name)) {
			return false;
		}
		if (this.victory_points != other.victory_points) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.ID == null) ? 0 : this.ID.hashCode());
		result = prime * result + this.character;
		result = prime * result + this.max_in_game;
		result = prime * result
				+ ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result
				+ ((this.short_name == null) ? 0 : this.short_name.hashCode());
		result = prime * result + this.victory_points;
		return result;
	}

	
}
