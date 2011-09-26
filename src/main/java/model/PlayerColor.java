package model;

import java.awt.Color;

public class PlayerColor implements Comparable<PlayerColor> {
	private Color color;
	private String name;
	private char character;
	public PlayerColor(String name, Color color, char character) {
		this.color = color;
		this.name = name;
		this.character = character;
	}
	
	public Color getColor() {
		return this.color;
	}
	
	public String getName() {
		return this.name;
	}

	public char getChar() {
		return this.character;
	}

	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PlayerColor))
			return false;
		PlayerColor otherPlayer = (PlayerColor)o;
		return this.getColor().equals(otherPlayer.getColor()) && this.getName().equals(otherPlayer.getName());
	}

	@Override
	public int compareTo(PlayerColor arg0) {
		return this.getName().compareTo(arg0.getName());
	}
	
	
}
