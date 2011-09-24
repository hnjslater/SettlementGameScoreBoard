package model;

import java.awt.Color;

public class PlayerColor {
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
	
	public String toString() {
		return getName();
	}
	
	public char getChar() {
		return this.character;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof PlayerColor))
			return false;
		PlayerColor otherPlayer = (PlayerColor)o;
		return this.getColor().equals(otherPlayer.getColor()) && this.getName().equals(otherPlayer.getName());
	}
	
	
}
