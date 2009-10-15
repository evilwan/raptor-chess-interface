package raptor.swt.chess;

import org.eclipse.swt.graphics.Color;

/**
 * A class representing a highlight of chess squares. A highlight may fade away
 * or it may be constant. It can also represent a single square or contain a
 * start and end square. Currently only the start and end squares are
 * highlighted however in the future the pattern between those squares may also
 * be highlighted.
 */
public class Highlight {
	protected int startSquare;
	protected int endSquare;
	protected Color color;
	protected int frame;
	protected boolean isFadeAway;

	public Highlight(int square, Color color) {
		startSquare = square;
		endSquare = -1;
		this.color = color;
		frame = -1;
	}

	public Highlight(int square, Color color, boolean isFadeAway) {
		startSquare = square;
		endSquare = -1;
		this.color = color;
		this.isFadeAway = isFadeAway;
		if (!isFadeAway) {
			frame = -1;
		} else {
			frame = 5;
		}
	}

	public Highlight(int startSquare, int endSquare, Color color) {
		this.startSquare = startSquare;
		this.endSquare = endSquare;
		this.color = color;
		frame = -1;
	}

	public Highlight(int startSquare, int endSquare, Color color,
			boolean isFadeAway) {
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

	public boolean isFadeAway() {
		return isFadeAway;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setEndSquare(int endSquare) {
		this.endSquare = endSquare;
	}

	public void setFadeAway(boolean isFadeAway) {
		this.isFadeAway = isFadeAway;
	}

	public void setStartSquare(int startSquare) {
		this.startSquare = startSquare;
	}
}