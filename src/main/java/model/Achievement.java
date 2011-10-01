package model;

import java.util.Collection;

public class Achievement implements Comparable<Achievement> {
	private String name;
	private int victory_points;
	private String short_name;
	private char character;
	public Achievement(String name, int victory_points, String short_name, char character) {
		this.name = name;
		this.victory_points = victory_points;
		this.short_name = short_name;
		this.character = character;
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
	
	public static Achievement valueOf(Collection<Achievement> achievements, String a) {
		for (Achievement ai : achievements) {
			if (ai.getName().equals(a))
				return ai;
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
	
}
