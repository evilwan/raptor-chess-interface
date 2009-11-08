package raptor.chess;

import static raptor.chess.util.GameUtils.bitscanClear;
import static raptor.chess.util.GameUtils.bitscanForward;
import static raptor.chess.util.GameUtils.getFile;
import raptor.chess.pgn.PgnHeader;

/**
 * Fischer Random Bughouse Game.
 * 
 * NOTE: the xoring for the zobrist is broken. It would need to be fixed to rely
 * on that for a computer program.
 */
public class FischerRandomBughouseGame extends BughouseGame {
	protected int initialLongRookFile;
	protected int initialShortRookFile;
	protected int initialKingFile;

	public FischerRandomBughouseGame() {
		setHeader(PgnHeader.Variant, Variant.fischerRandomBughouse.name());
		addState(Game.DROPPABLE_STATE);
		addState(Game.FISCHER_RANDOM_STATE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FischerRandomBughouseGame deepCopy(boolean ignoreHashes) {
		FischerRandomBughouseGame result = new FischerRandomBughouseGame();
		overwrite(result, ignoreHashes);
		return result;
	}

	/**
	 * This method should be invoked after the initial position is setup. It
	 * handles setting castling information used later on during the game.
	 */
	public void initialPositionIsSet() {
		initialKingFile = getFile(bitscanForward(getPieceBB(WHITE, KING)));
		long rookBB = getPieceBB(WHITE, ROOK);
		int firstRook = getFile(bitscanForward(rookBB));
		rookBB = bitscanClear(rookBB);
		int secondRook = getFile(bitscanForward(rookBB));
		if (firstRook < initialKingFile) {
			initialLongRookFile = firstRook;
			initialShortRookFile = secondRook;
		} else {
			initialLongRookFile = secondRook;
			initialShortRookFile = firstRook;
		}
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void generatePseudoKingCastlingMoves(long fromBB,
			PriorityMoveList moves) {
		FischerRandomUtils.generatePseudoKingCastlingMoves(this, fromBB, moves,
				initialKingFile, initialShortRookFile, initialLongRookFile);
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void makeCastlingMove(Move move) {
		FischerRandomUtils.makeCastlingMove(this, move, initialKingFile,
				initialShortRookFile, initialLongRookFile);
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void rollbackCastlingMove(Move move) {
		FischerRandomUtils.rollbackCastlingMove(this, move, initialKingFile,
				initialShortRookFile, initialLongRookFile);
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void updateCastlingRightsForNonEpNonCastlingMove(Move move) {
		FischerRandomUtils.updateCastlingRightsForNonEpNonCastlingMove(this,
				move, initialShortRookFile, initialLongRookFile);
	}
}
