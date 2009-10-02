package raptor.game;

/**
 * An implementation of a bughouse game. This approach involves linking two
 * bughouse games together. And setting the others droppable piece counts as
 * pieces are captured.
 */
public class BughouseGame extends ZHGame {

	protected BughouseGame otherBoard;

	public BughouseGame() {
		setType(Type.BUGHOUSE);
	}

	/**
	 * Decrements the drop count of the other game.
	 */
	@Override
	public void decrementDropCount(int color, int piece) {
		piece = piece & PROMOTED_MASK;
		otherBoard.positionState.dropCounts[color][piece] = otherBoard.positionState.dropCounts[color][piece] - 1;
	}

	public BughouseGame getOtherBoard() {
		return otherBoard;
	}

	/**
	 * Increments the drop count of the other game.
	 */
	@Override
	public void incrementDropCount(int color, int piece) {
		if ((piece & PROMOTED_MASK) != 0) {
			piece = PAWN;
		} else {
			otherBoard.positionState.dropCounts[color][piece] = otherBoard.positionState.dropCounts[color][piece] + 1;
		}
	}

	public void setOtherBoard(BughouseGame bughouseGame) {
		this.otherBoard = bughouseGame;
	}

}
