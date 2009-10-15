package raptor.swt.chess;

import org.eclipse.swt.graphics.Color;

/**
 * A class representing an arrow between two chess squares. There are two types
 * of arrows: those that fade away and those that do not.
 */
public class Arrow {
	protected int startSquare;
	protected int endSquare;
	protected Color color;
	protected int frame;
	protected boolean isFadeAway;

	public Arrow(int startSquare, int endSquare, Color color) {
		this.startSquare = startSquare;
		this.endSquare = endSquare;
		this.color = color;
		frame = -1;
	}

	public Arrow(int startSquare, int endSquare, Color color, boolean isFadeAway) {
		this.startSquare = startSquare;
		this.endSquare = endSquare;
		this.color = color;
		this.isFadeAway = isFadeAway;
		if (!isFadeAway) {
			frame = -1;
		} else {
			frame = 5;
		}
	}

	public Color getColor() {
		return color;
	}

	public int getEndSquare() {
		return endSquare;
	}

	public int getStartSquare() {
		return startSquare;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setEndSquare(int endSquare) {
		this.endSquare = endSquare;
	}

	public void setStartSquare(int startSquare) {
		this.startSquare = startSquare;
	}
}
