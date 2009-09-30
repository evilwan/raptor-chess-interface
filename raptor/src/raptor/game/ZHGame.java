package raptor.game;

import static raptor.game.util.GameUtils.bitscanClear;
import static raptor.game.util.GameUtils.bitscanForward;
import static raptor.game.util.GameUtils.getBitboard;
import static raptor.game.util.GameUtils.getOppositeColor;
import static raptor.game.util.GameUtils.getSan;
import static raptor.game.util.GameUtils.getString;
import static raptor.game.util.GameUtils.rankFileToSquare;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.WordUtils;

import raptor.game.util.GameUtils;

public class ZHGame extends Game {
	public ZHGame() {
		setType(Type.CRAZYHOUSE);
		addState(Game.DROPPABLE_STATE);
	}

	/**
	 * Overridden to adjust the drop count as well.
	 */
	@Override
	public void setPieceCount(int color, int piece, int count) {
		positionState.pieceCounts[color][piece & NOT_PROMOTED_MASK] = count;
	}

	/**
	 * Overridden to invoke incrementDropCount as well as the
	 * super.incrementPieceCount.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The uncolored piece constant.
	 */
	@Override
	public void incrementPieceCount(int color, int piece) {
		decrementDropCount(GameUtils.getOppositeColor(color), piece);
		super.incrementPieceCount(color, piece);
	}

	/**
	 * Overridden to invoke decrementDropCount as well as the
	 * super.incrementPieceCount.
	 * 
	 * @param color
	 *            WHITE or BLACK.
	 * @param piece
	 *            The un-colored piece constant.
	 */
	@Override
	public void decrementPieceCount(int color, int piece) {
		incrementDropCount(GameUtils.getOppositeColor(color), piece);
		super.decrementPieceCount(color, piece);
	}

	/**
	 * Increments the piece count. This method handles incrementing pieces with
	 * a promote mask.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The uncolored piece constant.
	 */
	public void incrementDropCount(int color, int piece) {
		if ((piece & PROMOTED_MASK) != 0) {
			piece = PAWN;
		} else {
			positionState.dropCounts[color][piece] = positionState.dropCounts[color][piece] + 1;
		}
	}

	/**
	 * Decrements the piece count for the specified piece. This method handles
	 * promotion masks as well.
	 * 
	 * @param color
	 *            WHITE or BLACK.
	 * @param piece
	 *            The un-colored piece constant.
	 */
	public void decrementDropCount(int color, int piece) {
		piece = piece & PROMOTED_MASK;
		positionState.dropCounts[color][piece] = positionState.dropCounts[color][piece] - 1;
	}

	/**
	 * Overridden to invoke genDropMoves as well as super.getPseudoLegalMoves.
	 */
	@Override
	public PriorityMoveList getPseudoLegalMoves() {
		PriorityMoveList result = super.getPseudoLegalMoves();
		genDropMoves(result);
		return result;
	}

	/**
	 * Generates all of the pseudo legal drop moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	public void genDropMoves(PriorityMoveList moves) {

		if (getDropCount(getColorToMove(), PAWN) > 0) {

			long emptyBB = getEmptyBB() & NOT_RANK1 & NOT_RANK8;
			while (emptyBB != 0) {
				int toSquare = bitscanForward(emptyBB);

				addMove(new Move(toSquare, PAWN, getColorToMove()), moves);
				emptyBB = bitscanClear(emptyBB);
			}
		}

		if (getDropCount(getColorToMove(), KNIGHT) > 0) {

			long emptyBB = getEmptyBB();
			while (emptyBB != 0) {
				int toSquare = bitscanForward(emptyBB);

				addMove(new Move(toSquare, KNIGHT, getColorToMove()), moves);
				emptyBB = bitscanClear(emptyBB);
			}
		}

		if (getDropCount(getColorToMove(), BISHOP) > 0) {

			long emptyBB = getEmptyBB() & NOT_RANK1 & NOT_RANK8;
			while (emptyBB != 0) {
				int toSquare = bitscanForward(emptyBB);

				addMove(new Move(toSquare, BISHOP, getColorToMove()), moves);
				emptyBB = bitscanClear(emptyBB);
			}
		}

		if (getDropCount(getColorToMove(), ROOK) > 0) {

			long emptyBB = getEmptyBB() & NOT_RANK1 & NOT_RANK8;
			while (emptyBB != 0) {
				int toSquare = bitscanForward(emptyBB);

				addMove(new Move(toSquare, ROOK, getColorToMove()), moves);
				emptyBB = bitscanClear(emptyBB);
			}
		}

		if (getDropCount(getColorToMove(), QUEEN) > 0) {

			long emptyBB = getEmptyBB() & NOT_RANK1 & NOT_RANK8;
			while (emptyBB != 0) {
				int toSquare = bitscanForward(emptyBB);

				addMove(new Move(toSquare, QUEEN, getColorToMove()), moves);
				emptyBB = bitscanClear(emptyBB);
			}
		}
	}

	/**
	 * Should be called before the move is made to update the san field.
	 */
	@Override
	protected void setSan(Move move) {
		if (move.isDrop()) {
			move.setSan(PIECE_TO_SAN.charAt(move.getPiece())
					+ GameUtils.getSan(move.getTo()));
		} else {
			super.setSan(move);
		}
	}

	/**
	 * Returns a dump of the game class suitable for debugging. Quite a lot of
	 * information is produced and its an expensive operation, use with care.
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(1000);

		result.append(getString(new String[] { "emptyBB", "occupiedBB",
				"notColorToMoveBB", "color[WHITE]", "color[BLACK]" },
				new long[] { positionState.emptyBB, positionState.occupiedBB,
						positionState.notColorToMoveBB, getColorBB(WHITE),
						getColorBB(BLACK) })
				+ "\n\n");

		result.append(getString(new String[] { "[WHITE][PAWN]",
				"[WHITE][KNIGHT]", "[WHITE][BISHOP]", "[WHITE][ROOK]",
				"[WHITE][QUEEN]", "[WHITE][KING]" }, new long[] {
				getPieceBB(WHITE, PAWN), getPieceBB(WHITE, KNIGHT),
				getPieceBB(WHITE, BISHOP), getPieceBB(WHITE, ROOK),
				getPieceBB(WHITE, QUEEN), getPieceBB(WHITE, KING) })
				+ "\n\n");

		result.append(getString(new String[] { "[BLACK][PAWN]",
				"[BLACK][KNIGHT]", "[BLACK][BISHOP]", "[BLACK][ROOK]",
				"[BLACK][QUEEN]", "[BLACK][KING]" }, new long[] {
				getPieceBB(BLACK, PAWN), getPieceBB(BLACK, KNIGHT),
				getPieceBB(BLACK, BISHOP), getPieceBB(BLACK, ROOK),
				getPieceBB(BLACK, QUEEN), getPieceBB(BLACK, KING) })
				+ "\n\n");

		result.append("FEN=" + toFEN() + "\n\n");

		String legalMovesString = Arrays.toString(getLegalMoves().asArray());
		// "DISABLED";

		for (int i = 7; i > -1; i--) {
			for (int j = 0; j < 8; j++) {
				int square = rankFileToSquare(i, j);
				int piece = getPiece(square);
				int color = (getBitboard(square) & getColorBB(positionState.colorToMove)) != 0L ? positionState.colorToMove
						: getOppositeColor(positionState.colorToMove);

				result.append("|" + COLOR_PIECE_TO_CHAR[color].charAt(piece));
			}
			result.append("|   ");

			switch (i) {
			case 7:
				result.append("Type: "
						+ type.name()
						+ " Droppable: "
						+ isInState(Game.DROPPABLE_STATE)
						+ " To Move: "
						+ COLOR_DESCRIPTION[positionState.colorToMove]
						+ " "
						+ "Last Move: "
						+ (positionState.moves.getSize() == 0 ? ""
								: positionState.moves.getLast()));
				break;

			case 6:
				result.append("Drop counts [WP=" + getDropCount(WHITE, PAWN)
						+ " WN=" + getDropCount(WHITE, KNIGHT) + " WB="
						+ getDropCount(WHITE, BISHOP) + " WR="
						+ getDropCount(WHITE, ROOK) + " WQ="
						+ getDropCount(WHITE, QUEEN) + " WK="
						+ getDropCount(WHITE, KING) + "][BP="
						+ getDropCount(BLACK, PAWN) + "BN= "
						+ getDropCount(BLACK, KNIGHT) + " BB="
						+ getDropCount(BLACK, BISHOP) + " BR="
						+ getDropCount(BLACK, ROOK) + " BQ="
						+ getDropCount(BLACK, QUEEN) + " BK="
						+ getDropCount(BLACK, KING) + "]");
				break;
			case 5:
				result.append("Piece counts [WP=" + getPieceCount(WHITE, PAWN)
						+ " WN=" + getPieceCount(WHITE, KNIGHT) + " WB="
						+ getPieceCount(WHITE, BISHOP) + " WR="
						+ getPieceCount(WHITE, ROOK) + " WQ="
						+ getPieceCount(WHITE, QUEEN) + " WK="
						+ getPieceCount(WHITE, KING) + "][BP="
						+ getPieceCount(BLACK, PAWN) + "BN= "
						+ getPieceCount(BLACK, KNIGHT) + " BB="
						+ getPieceCount(BLACK, BISHOP) + " BR="
						+ getPieceCount(BLACK, ROOK) + " BQ="
						+ getPieceCount(BLACK, QUEEN) + " BK="
						+ getPieceCount(BLACK, KING) + "]");
				break;
			case 4:

				result.append("Moves: " + positionState.halfMoveCount + " EP: "
						+ getSan(positionState.epSquare) + " Castle: "
						+ getFenCastle());
				break;
			case 3:
				result.append("State: " + state + " Type=" + type + " Result="
						+ result);
				break;
			case 2:
				result.append("Event: " + event + " Site=" + site + " Time="
						+ new Date(startTime));
				break;
			case 1:
				result.append("WhiteName: " + whiteName + " BlackName="
						+ blackName + " WhiteTime=" + whiteRemainingTimeMilis
						+ " whiteLag=" + whiteLagMillis
						+ " blackRemainingTImeMillis = "
						+ blackRemainingTimeMillis + " blackLag="
						+ blackLagMillis);

				break;
			default:
				result.append("initialWhiteTimeMillis: "
						+ initialWhiteTimeMillis + " initialBlackTimeMillis="
						+ initialBlackTimeMillis + " initialWhiteIncMillis="
						+ initialWhiteIncMillis + " initialBlackIncMillis="
						+ initialBlackIncMillis);
				break;
			}
			result.append("\n");
		}

		result.append(WordUtils.wrap("Legals=" + legalMovesString, 80, "\n",
				true));
		result.append(WordUtils.wrap("Movelist=" + positionState.moves, 80,
				"\n", true));

		return result.toString();
	}

}
