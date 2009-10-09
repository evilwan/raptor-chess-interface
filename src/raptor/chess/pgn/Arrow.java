package raptor.chess.pgn;

import raptor.chess.util.GameUtils;

public class Arrow implements MoveAnnotation {
	static final long serialVersionUID = 1;
	int startSquare;
	int endSquare;

	public Arrow(int startSquare, int endSquare) {
		this.startSquare = startSquare;
		this.endSquare = endSquare;
	}

	public int getEndSquare() {
		return endSquare;
	}

	public int getStartSquare() {
		return startSquare;
	}

	@Override
	public String toString() {
		return GameUtils.getSan(startSquare) + " "
				+ GameUtils.getSan(endSquare);
	}
}
