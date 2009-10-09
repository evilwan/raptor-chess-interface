package raptor.chess.pgn;

import raptor.chess.util.GameUtils;

public class Highlight implements MoveAnnotation {
	static final long serialVersionUID = 1;
	public int square;

	public Highlight(int square) {
		this.square = square;
	}

	public int getSquare() {
		return square;
	}

	@Override
	public String toString() {
		return GameUtils.getSan(square);
	}
}
