package raptor.game;

/**
 * An implementation of a bughouse game. This approach involves linking two
 * bughouse games together. And setting the others droppable piece counts as
 * pieces are captured.
 */
public class BughouseGame extends ZHGame {

	public BughouseGame() {
		setType(Type.BUGHOUSE);
	}

	protected BughouseGame otherBoard;

	public void setOtherBoard(BughouseGame bughouseGame) {
		this.otherBoard = bughouseGame;
	}

	public BughouseGame getOtherBoard() {
		return otherBoard;
	}

	/**
	 * Increments the drop count of the other game.
	 */
	public void incrementDropCount(int color, int piece) {
		if ((piece & PROMOTED_MASK) != 0) {
			piece = PAWN;
		} else {
			otherBoard.positionState.dropCounts[color][piece] = otherBoard.positionState.dropCounts[color][piece] + 1;
		}
	}

	/**
	 * Decrements the drop count of the other game.
	 */
	public void decrementDropCount(int color, int piece) {
		piece = piece & PROMOTED_MASK;
		otherBoard.positionState.dropCounts[color][piece] = otherBoard.positionState.dropCounts[color][piece] - 1;
	}

}
