package ui.scoreboard;

import java.awt.Point;
import java.awt.Shape;

public abstract class HotZone {

	private Shape s;
	
	public HotZone(Shape s) {
		this.s = s;
	}
	
	public boolean maybeClicked(Point p) {
		if (s.contains(p)) {
			this.clicked();
			return true;
		}
		else {
			return false;
		}
	}


	protected abstract void clicked();
	

}
