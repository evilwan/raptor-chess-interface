package raptor.game;

public class Position {
	public int hashcode;
	public long[][] pieceBB = new long[2][7];
	public int colorToMove;

	public Position(Game game) {
		hashcode = (int) (game.getOccupiedBB() ^ game.getOccupiedBB() >>> 32);
		System
				.arraycopy(game.pieceBB[0], 0, pieceBB[0], 0,
						game.pieceBB.length);
		System
				.arraycopy(game.pieceBB[1], 0, pieceBB[1], 0,
						game.pieceBB.length);
		colorToMove = game.getColorToMove();
	}
	
	public int hashCode() {
		return hashcode;
	}
	
	public boolean equals(Object o) {
		Position position = (Position) o;
		boolean result = colorToMove == position.colorToMove;
		
		for (int i = 0; result && i < pieceBB.length; i++) {
			for (int j = 0; result && j < pieceBB[i].length; j++) {
				result = pieceBB[i][j] == position.pieceBB[i][j];
			}
		}
		return result;
	}
}
