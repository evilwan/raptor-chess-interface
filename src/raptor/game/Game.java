/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.game;

import static raptor.game.util.GameUtils.bitscanClear;
import static raptor.game.util.GameUtils.bitscanForward;
import static raptor.game.util.GameUtils.diagonalMove;
import static raptor.game.util.GameUtils.getBitboard;
import static raptor.game.util.GameUtils.getOppositeColor;
import static raptor.game.util.GameUtils.getSan;
import static raptor.game.util.GameUtils.getString;
import static raptor.game.util.GameUtils.kingMove;
import static raptor.game.util.GameUtils.knightMove;
import static raptor.game.util.GameUtils.moveOne;
import static raptor.game.util.GameUtils.orthogonalMove;
import static raptor.game.util.GameUtils.pawnCapture;
import static raptor.game.util.GameUtils.pawnDoublePush;
import static raptor.game.util.GameUtils.pawnEpCapture;
import static raptor.game.util.GameUtils.pawnSinglePush;
import static raptor.game.util.GameUtils.rankFileToSquare;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import raptor.game.pgn.PgnHeader;
import raptor.game.util.GameUtils;
import raptor.game.util.SanUtil;
import raptor.game.util.ZobristHash;
import raptor.game.util.SanUtil.SanValidations;
import raptor.util.RaptorStringUtils;

/**
 * A game class which uses bitboards and Zobrist hashing.
 */
public class Game implements GameConstants {

	/**
	 * A class containing state information about the game. This class does'nt
	 * contain things like the games state, white/black times, white/black
	 * names, etc. However it does contain the bitmaps, zobrists, moves, and
	 * other non text information.
	 */
	public static final class PositionState {
		public int[] board = new int[64];
		public int[] castling = new int[2];
		public long[] colorBB = new long[2];
		public int colorToMove;
		public int[][] dropCounts = new int[2][7];
		public long emptyBB;
		public int epSquare = EMPTY_SQUARE;
		public int fiftyMoveCount;
		public int halfMoveCount;
		public int initialEpSquare = EMPTY_SQUARE;
		public long zobristGameHash;
		public long zobristPositionHash;
		public int[] moveRepHash = new int[MOVE_REP_CACHE_SIZE];
		public long notColorToMoveBB;
		public long occupiedBB;
		public long[][] pieceBB = new long[2][7];
		public int[][] pieceCounts = new int[2][7];
		public MoveList moves = new MoveList();

		public PositionState deepCopy(boolean ignoreHashes) {
			PositionState result = new PositionState();
			result.moves = moves.deepCopy();
			result.halfMoveCount = halfMoveCount;
			System.arraycopy(colorBB, 0, result.colorBB, 0,
					result.colorBB.length);
			for (int i = 0; i < pieceBB.length; i++) {
				System.arraycopy(pieceBB[i], 0, result.pieceBB[i], 0,
						pieceBB[i].length);
			}
			System.arraycopy(board, 0, result.board, 0, board.length);
			result.occupiedBB = occupiedBB;
			result.emptyBB = emptyBB;
			result.notColorToMoveBB = notColorToMoveBB;
			System.arraycopy(castling, 0, result.castling, 0, castling.length);
			result.initialEpSquare = initialEpSquare;
			result.epSquare = epSquare;
			result.colorToMove = colorToMove;
			result.fiftyMoveCount = fiftyMoveCount;
			for (int i = 0; i < pieceCounts.length; i++) {
				System.arraycopy(pieceCounts[i], 0, result.pieceCounts[i], 0,
						pieceCounts[i].length);
			}
			for (int i = 0; i < dropCounts.length; i++) {
				System.arraycopy(dropCounts[i], 0, result.dropCounts[i], 0,
						dropCounts[i].length);
			}
			result.zobristPositionHash = zobristPositionHash;
			result.zobristGameHash = zobristGameHash;

			if (!ignoreHashes) {
				System.arraycopy(moveRepHash, 0, result.moveRepHash, 0,
						moveRepHash.length);
			}
			return result;
		}
	}

	public static enum Type {
		CLASSIC, ATOMIC, BUGHOUSE, CRAZYHOUSE, LOSERS, SUICIDE, WILD, FISCHER_RANDOM
	}

	/**
	 * The active state bitmask. Set when a game is actively being played.
	 */
	public static final int ACTIVE_STATE = 1;

	/**
	 * The inactive state bitmask. Set when a game is no longer being played.
	 */
	public static final int INACTIVE_STATE = ACTIVE_STATE << 1;

	/**
	 * The examining state bitmask. Set when a game is in an examined state.
	 */
	public static final int EXAMINING_STATE = ACTIVE_STATE << 2;

	/**
	 * The playing state bitmask. Set when a user is playing the game.
	 */
	public static final int PLAYING_STATE = ACTIVE_STATE << 3;

	/**
	 * The untimed state bitmask. Set when the game is not timed.
	 */
	public static final int UNTIMED_STATE = ACTIVE_STATE << 4;

	/**
	 * The droppable state bitmask. Set when the game is a droppable game, e.g.
	 * crazyhouse or bughouse. Set when the game is not timed.
	 */
	public static final int DROPPABLE_STATE = ACTIVE_STATE << 5;

	/**
	 * The clock is ticking state bitmask. Set when the color to moves clock is
	 * ticking.
	 */
	public static final int IS_CLOCK_TICKING_STATE = ACTIVE_STATE << 6;

	/**
	 * The observing an examined game bitmask Set when the game is an
	 * observation of an examinied game.
	 */
	public static final int OBSERVING_EXAMINED_STATE = ACTIVE_STATE << 7;

	/**
	 * The observing state bitmask. Set when the game is an observation.
	 */
	public static final int OBSERVING_STATE = ACTIVE_STATE << 8;

	/**
	 * The setup bitmask. Set when the game is in a setup state e.g. the
	 * position is being set up.
	 */
	public static final int SETUP_STATE = ACTIVE_STATE << 9;

	protected long initialWhiteIncMillis;
	protected long initialWhiteTimeMillis;
	protected long initialBlackIncMillis;
	protected long initialBlackTimeMillis;
	protected PositionState positionState = new PositionState();
	protected long blackLagMillis;
	protected long blackRemainingTimeMillis;
	protected boolean isSettingMoveSan = false;
	protected String id;
	protected Result result = Result.ON_GOING;
	protected int state;
	protected Type type = Type.CLASSIC;
	protected long whiteLagMillis;
	protected long whiteRemainingTimeMillis;
	protected Map<String, String> pgnHeaderMap = new HashMap<String, String>();

	/**
	 * Currently places captures and promotions ahead of non captures.
	 */
	protected void addMove(Move move, PriorityMoveList moves) {
		if (move.isCapture() || move.isPromotion()) {
			moves.appendHighPriority(move);
		} else {
			moves.appendLowPriority(move);
		}
	}

	/**
	 * Adds the state flag to the games state.
	 */
	public void addState(int state) {
		setState(getState() | state);
	}

	/**
	 * Returns true if a king of each color is on the board.
	 */
	public boolean areBothKingsOnBoard() {
		return getPieceBB(WHITE, KING) != 0L && getPieceBB(BLACK, KING) != 0L;
	}

	public boolean canBlackCastleLong() {
		return (positionState.castling[BLACK] & CASTLE_LONG) != 0;
	}

	public boolean canBlackCastleShort() {
		return (positionState.castling[BLACK] & CASTLE_SHORT) != 0;
	}

	public boolean canWhiteCastleLong() {
		return (positionState.castling[WHITE] & CASTLE_LONG) != 0;
	}

	public boolean canWhiteCastleShort() {
		return (positionState.castling[WHITE] & CASTLE_SHORT) != 0;
	}

	/**
	 * Clears the specified state constant from the games state.
	 */
	public void clearState(int state) {
		setState(getState() & ~state);
	}

	/**
	 * Decrements the drop count for the specified piece. This method handles
	 * promotion masks as well.
	 * 
	 * @param color
	 *            WHITE or BLACK.
	 * @param piece
	 *            The un-colored piece constant.
	 */
	public void decrementDropCount(int color, int piece) {

		if ((piece & PROMOTED_MASK) != 0) {
			piece = PAWN;

		}
		positionState.dropCounts[color][piece]--;
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
	public void decrementPieceCount(int color, int piece) {
		if ((piece & PROMOTED_MASK) != 0) {
			piece = PAWN;
		}
		positionState.pieceCounts[color][piece]--;
	}

	/**
	 * Decrements the current positions repetition count.
	 */
	public void decrementRepCount() {
		positionState.moveRepHash[getRepHash()]--;
	}

	/**
	 * @param ignoreHashes
	 *            Whether to include copying hash tables.
	 * @return An deep clone copy of this Game object.
	 */
	public Game deepCopy(boolean ignoreHashes) {
		Game result = new Game();
		overwrite(result, ignoreHashes);
		return result;
	}

	/**
	 * Makes a move with out any legality checking.
	 */
	public void forceMove(Move move) {
		move.setLastCastleState(getCastling(getColorToMove()));
		setSan(move);
		switch (move.getMoveCharacteristic()) {
		case Move.EN_PASSANT_CHARACTERISTIC:
			makeEPMove(move);
			break;
		case Move.SHORT_CASTLING_CHARACTERISTIC:
		case Move.LONG_CASTLING_CHARACTERISTIC:
			makeCastlingMove(move);
			break;
		case Move.DROP_CHARACTERISTIC:
			makeDropMove(move);
			break;
		default:
			makeNonEpNonCastlingMove(move);
			break;
		}

		int oppToMove = getOppositeColor(getColorToMove());

		move.setPrevious50MoveCount(getFiftyMoveCount());
		if (move.isCapture()) {
			decrementPieceCount(oppToMove, move.getCaptureWithPromoteMask());
			incrementDropCount(getColorToMove(), move
					.getCaptureWithPromoteMask());
			setFiftyMoveCount(0);
		} else if (move.isDrop()) {
			incrementPieceCount(getColorToMove(), move.getPiece());
			decrementDropCount(getColorToMove(), move.getPiece());
			setFiftyMoveCount(0);
		} else if (move.getPiece() == PAWN) {
			setFiftyMoveCount(0);
		} else {
			setFiftyMoveCount(getFiftyMoveCount() + 1);
		}

		setColorToMove(oppToMove);
		setNotColorToMoveBB(~getColorBB(getColorToMove()));
		setHalfMoveCount(getHalfMoveCount() + 1);

		getMoveList().append(move);

		updateZobristHash();
		incrementRepCount();
	}

	/**
	 * Generates all of the pseudo legal bishop moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	public void generatePseudoBishopMoves(PriorityMoveList moves) {
		long fromBB = getPieceBB(getColorToMove(), BISHOP);

		while (fromBB != 0) {
			int fromSquare = bitscanForward(fromBB);

			long toBB = diagonalMove(fromSquare, getEmptyBB(), getOccupiedBB())
					& getNotColorToMoveBB();

			while (toBB != 0) {
				int toSquare = bitscanForward(toBB);

				int contents = getPieceWithPromoteMask(toSquare);

				addMove(new Move(fromSquare, toSquare,
						getPieceWithPromoteMask(fromSquare), getColorToMove(),
						contents), moves);
				toBB = bitscanClear(toBB);
			}
			fromBB = bitscanClear(fromBB);
		}
	}

	/**
	 * Generates all of the pseudo legal king castling moves in the position and
	 * adds them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	public void generatePseudoKingCastlingMoves(long fromBB,
			PriorityMoveList moves) {
		// The king destination square isnt checked, its checked when legal
		// getMoves() are checked.

		if (getColorToMove() == WHITE
				&& (getCastling(getColorToMove()) & CASTLE_SHORT) != 0
				&& fromBB == E1 && getPiece(SQUARE_G1) == EMPTY
				&& getPiece(SQUARE_F1) == EMPTY && !isInCheck(WHITE, E1)
				&& !isInCheck(WHITE, F1)) {
			moves
					.appendLowPriority(new Move(SQUARE_E1, SQUARE_G1, KING,
							getColorToMove(), EMPTY,
							Move.SHORT_CASTLING_CHARACTERISTIC));
		}

		if (getColorToMove() == WHITE
				&& (getCastling(getColorToMove()) & CASTLE_LONG) != 0
				&& fromBB == E1 && getPiece(SQUARE_D1) == EMPTY
				&& getPiece(SQUARE_C1) == EMPTY && getPiece(SQUARE_B1) == EMPTY
				&& !isInCheck(WHITE, E1) && !isInCheck(WHITE, D1)) {
			moves
					.appendLowPriority(new Move(SQUARE_E1, SQUARE_C1, KING,
							getColorToMove(), EMPTY,
							Move.LONG_CASTLING_CHARACTERISTIC));
		}

		if (getColorToMove() == BLACK
				&& (getCastling(getColorToMove()) & CASTLE_SHORT) != 0
				&& fromBB == E8 && getPiece(SQUARE_G8) == EMPTY
				&& getPiece(SQUARE_F8) == EMPTY && !isInCheck(BLACK, E8)
				&& !isInCheck(BLACK, F8)) {
			moves
					.appendLowPriority(new Move(SQUARE_E8, SQUARE_G8, KING,
							getColorToMove(), EMPTY,
							Move.SHORT_CASTLING_CHARACTERISTIC));

		}

		if (getColorToMove() == BLACK
				&& (getCastling(getColorToMove()) & CASTLE_LONG) != 0
				&& fromBB == E8 && getPiece(SQUARE_D8) == EMPTY
				&& getPiece(SQUARE_C8) == EMPTY && getPiece(SQUARE_B8) == EMPTY
				&& !isInCheck(BLACK, E8) && !isInCheck(BLACK, D8)) {
			moves
					.appendLowPriority(new Move(SQUARE_E8, SQUARE_C8, KING,
							getColorToMove(), EMPTY,
							Move.LONG_CASTLING_CHARACTERISTIC));
		}
	}

	/**
	 * Generates all of the pseudo legal king moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	public void generatePseudoKingMoves(PriorityMoveList moves) {
		long fromBB = getPieceBB(getColorToMove(), KING);
		int fromSquare = bitscanForward(fromBB);
		long toBB = kingMove(fromSquare) & getNotColorToMoveBB();

		generatePseudoKingCastlingMoves(fromBB, moves);

		while (toBB != 0) {
			int toSquare = bitscanForward(toBB);

			int contents = getPieceWithPromoteMask(toSquare);

			addMove(new Move(fromSquare, toSquare, KING, getColorToMove(),
					contents), moves);
			toBB = bitscanClear(toBB);
			toSquare = bitscanForward(toBB);
		}
	}

	/**
	 * Generates all of the pseudo legal knight moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	public void generatePseudoKnightMoves(PriorityMoveList moves) {

		long fromBB = getPieceBB(getColorToMove(), KNIGHT);

		while (fromBB != 0) {
			int fromSquare = bitscanForward(fromBB);

			long toBB = knightMove(fromSquare) & getNotColorToMoveBB();

			while (toBB != 0) {
				int toSquare = bitscanForward(toBB);
				int contents = getPieceWithPromoteMask(toSquare);

				addMove(new Move(fromSquare, toSquare,
						getPieceWithPromoteMask(fromSquare), getColorToMove(),
						contents), moves);

				toBB = bitscanClear(toBB);
				toSquare = bitscanForward(toBB);
			}

			fromBB = bitscanClear(fromBB);
		}
	}

	/**
	 * Generates all of the pseudo legal pawn captures in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	public void generatePseudoPawnCaptures(int fromSquare, long fromBB,
			int oppositeColor, PriorityMoveList moves) {

		long toBB = pawnCapture(getColorToMove(), fromBB,
				getColorBB(oppositeColor));

		while (toBB != 0L) {
			int toSquare = bitscanForward(toBB);
			if ((toBB & RANK8_OR_RANK1) != 0L) {
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPieceWithPromoteMask(toSquare), KNIGHT,
						EMPTY_SQUARE, Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPieceWithPromoteMask(toSquare), BISHOP,
						EMPTY_SQUARE, Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPieceWithPromoteMask(toSquare), QUEEN, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPieceWithPromoteMask(toSquare), ROOK, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
			} else {
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPieceWithPromoteMask(toSquare)), moves);
			}
			toBB = bitscanClear(toBB);
		}
	}

	/**
	 * Generates all of the pseudo legal double pawn pushes in the position and
	 * adds them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	public void generatePseudoPawnDoublePush(int fromSquare, long fromBB,
			int oppositeColor, int epModifier, PriorityMoveList moves) {

		long toBB = pawnDoublePush(getColorToMove(), fromBB, getEmptyBB());

		while (toBB != 0) {
			int toSquare = bitscanForward(toBB);
			addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
					EMPTY, EMPTY, toSquare + epModifier,
					Move.DOUBLE_PAWN_PUSH_CHARACTERISTIC), moves);
			toBB = bitscanClear(toBB);
		}

	}

	/**
	 * Generates all of the pseudo En-Passant moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	public void generatePseudoPawnEPCaptures(int fromSquare, long fromBB,
			int oppositeColor, PriorityMoveList moves) {
		if (getEpSquare() != EMPTY) {

			long toBB = pawnEpCapture(getColorToMove(), fromBB, getPieceBB(
					oppositeColor, PAWN), getBitboard(getEpSquare()));

			if (toBB != 0) {
				int toSquare = bitscanForward(toBB);

				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						PAWN, EMPTY, EMPTY_SQUARE,
						Move.EN_PASSANT_CHARACTERISTIC), moves);
			}
		}
	}

	/**
	 * Generates all of the pseudo legal pawn moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	public void generatePseudoPawnMoves(PriorityMoveList moves) {
		long pawnsBB = getPieceBB(getColorToMove(), PAWN);
		int oppositeColor, epModifier;

		if (getColorToMove() == WHITE) {
			oppositeColor = BLACK;
			epModifier = -8;
		} else {
			oppositeColor = WHITE;
			epModifier = +8;
		}

		while (pawnsBB != 0) {
			int fromSquare = bitscanForward(pawnsBB);
			long fromBB = getBitboard(fromSquare);

			generatePseudoPawnEPCaptures(fromSquare, fromBB, oppositeColor,
					moves);
			generatePseudoPawnCaptures(fromSquare, fromBB, oppositeColor, moves);
			generatePseudoPawnSinglePush(fromSquare, fromBB, oppositeColor,
					moves);
			generatePseudoPawnDoublePush(fromSquare, fromBB, oppositeColor,
					epModifier, moves);

			pawnsBB = bitscanClear(pawnsBB);
		}
	}

	/**
	 * Generates all of the pseudo legal single push pawn moves in the position
	 * and adds them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	public void generatePseudoPawnSinglePush(int fromSquare, long fromBB,
			int oppositeColor, PriorityMoveList moves) {

		long toBB = pawnSinglePush(getColorToMove(), fromBB, getEmptyBB());

		while (toBB != 0) {
			int toSquare = bitscanForward(toBB);

			if ((toBB & RANK8_OR_RANK1) != 0L) {
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY, KNIGHT, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY, BISHOP, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY, QUEEN, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY, ROOK, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
			} else {
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY), moves);
			}

			toBB = bitscanClear(toBB);
		}
	}

	/**
	 * Generates all of the pseudo legal queen moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	public void generatePseudoQueenMoves(PriorityMoveList moves) {
		long fromBB = getPieceBB(getColorToMove(), QUEEN);

		while (fromBB != 0) {
			int fromSquare = bitscanForward(fromBB);

			long toBB = (orthogonalMove(fromSquare, getEmptyBB(),
					getOccupiedBB()) | diagonalMove(fromSquare, getEmptyBB(),
					getOccupiedBB()))
					& getNotColorToMoveBB();

			while (toBB != 0) {
				int toSquare = bitscanForward(toBB);

				int contents = getPieceWithPromoteMask(toSquare);
				addMove(new Move(fromSquare, toSquare,
						getPieceWithPromoteMask(fromSquare), getColorToMove(),
						contents), moves);
				toBB = bitscanClear(toBB);
			}

			fromBB = bitscanClear(fromBB);
		}
	}

	/**
	 * Generates all of the pseudo legal rook moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	public void generatePseudoRookMoves(PriorityMoveList moves) {
		long fromBB = getPieceBB(getColorToMove(), ROOK);

		while (fromBB != 0) {
			int fromSquare = bitscanForward(fromBB);

			long toBB = orthogonalMove(fromSquare, getEmptyBB(),
					getOccupiedBB())
					& getNotColorToMoveBB();

			while (toBB != 0) {
				int toSquare = bitscanForward(toBB);

				int contents = getPieceWithPromoteMask(toSquare);
				addMove(new Move(fromSquare, toSquare,
						getPieceWithPromoteMask(fromSquare), getColorToMove(),
						contents), moves);
				toBB = bitscanClear(toBB);
			}

			fromBB = bitscanClear(fromBB);
		}
	}

	public HashSet<String> getAllHeaders() {
		return new HashSet<String>(pgnHeaderMap.keySet());
	}

	public ArrayList<String> getAllNonStrHeaders() {
		ArrayList<String> result = new ArrayList<String>(pgnHeaderMap.size());
		PgnHeader[] strHeaders = PgnHeader.STR_HEADERS;

		for (String key : pgnHeaderMap.keySet()) {
			boolean isRequiredHeader = false;
			for (PgnHeader header : strHeaders) {
				if (header.getName().equals(key)) {
					isRequiredHeader = true;
				}
			}
			if (!isRequiredHeader) {
				result.add(key);
			}
		}

		return result;
	}

	/**
	 * @return Black's lag in milliseconds.
	 */
	public long getBlackLagMillis() {
		return blackLagMillis;
	}

	/**
	 * Returns the PgnHeader BLACK
	 * 
	 * @return Black's name.
	 */
	public String getBlackName() {
		return getHeader(PgnHeader.BLACK);
	}

	/**
	 * Returns the PgnHeader BLACK_ELO
	 * 
	 * @return Black's rating.
	 */
	public String getBlackRating() {
		return getHeader(PgnHeader.BLACK_ELO);
	}

	/**
	 * @return The amount of time that black has on the clock in milliseconds.
	 */
	public long getBlackRemainingTimeMillis() {
		return blackRemainingTimeMillis;
	}

	/**
	 * Returns the games board. This is an int[64] containg non-colored piece
	 * constants where there are pieces.
	 * 
	 * @param moves
	 *            A move list.
	 */
	public int[] getBoard() {
		return positionState.board;
	}

	/**
	 * Returns the castling constant for the specified color.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @return The castling constant.
	 */
	public int getCastling(int color) {
		return positionState.castling[color];
	}

	/**
	 * Returns a bitboard with 1s in the squares of the pieces of the specified
	 * color.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 */
	public long getColorBB(int color) {
		return positionState.colorBB[color];
	}

	/**
	 * Returns the color to move, WHITE or BLACK.
	 */
	public int getColorToMove() {
		return positionState.colorToMove;
	}

	/**
	 * Returns the PgnHeader DATE.
	 */
	public String getDate() {
		return getHeader(PgnHeader.DATE);
	}

	/**
	 * Returns the drop piece count for the specified color and piece. This is
	 * useful for games such as bughouse or crazyhosue
	 */
	public int getDropCount(int color, int piece) {
		return positionState.dropCounts[color][piece];
	}

	public String getDropCountsString() {
		return "Drop counts [WP=" + getDropCount(WHITE, PAWN) + " WN="
				+ getDropCount(WHITE, KNIGHT) + " WB="
				+ getDropCount(WHITE, BISHOP) + " WR="
				+ getDropCount(WHITE, ROOK) + " WQ="
				+ getDropCount(WHITE, QUEEN) + " WK="
				+ getDropCount(WHITE, KING) + "][BP="
				+ getDropCount(BLACK, PAWN) + " BN= "
				+ getDropCount(BLACK, KNIGHT) + " BB="
				+ getDropCount(BLACK, BISHOP) + " BR="
				+ getDropCount(BLACK, ROOK) + " BQ="
				+ getDropCount(BLACK, QUEEN) + " BK="
				+ getDropCount(BLACK, KING) + "]";
	}

	/**
	 * Returns a bitboard with 1s in the squares which are empty.
	 * 
	 * @return
	 */
	public long getEmptyBB() {
		return positionState.emptyBB;
	}

	/**
	 * If the last move was a double pawn push, this method returns the square
	 * otherwise returns EMPTY.
	 */
	public int getEpSquare() {
		return positionState.epSquare;
	}

	/**
	 * Returns the games EVENT PgnHeader.
	 */
	public String getEvent() {
		return getHeader(PgnHeader.EVENT);
	}

	protected String getFenCastle() {
		String whiteCastlingFen = getCastling(WHITE) == CASTLE_NONE ? ""
				: getCastling(WHITE) == CASTLE_BOTH ? "KQ"
						: getCastling(WHITE) == CASTLE_SHORT ? "K" : "Q";
		String blackCastlingFen = getCastling(BLACK) == CASTLE_NONE ? ""
				: getCastling(BLACK) == CASTLE_BOTH ? "kq"
						: getCastling(BLACK) == CASTLE_SHORT ? "k" : "q";

		return whiteCastlingFen.equals("") && blackCastlingFen.equals("") ? " -"
				: whiteCastlingFen + blackCastlingFen;
	}

	/**
	 * Returns the number of half moves since the last irreversible move. This
	 * is useful for determining the 50 move draw rule.
	 */
	public int getFiftyMoveCount() {
		return positionState.fiftyMoveCount;
	}

	/**
	 * Returns the full move count. The next move will have this number.
	 */
	public int getFullMoveCount() {
		return getHalfMoveCount() / 2 + 1;
	}

	/**
	 * Returns the number of half positionState.moves made.
	 */
	public int getHalfMoveCount() {
		return positionState.halfMoveCount;
	}

	public String getHeader(PgnHeader header) {
		return pgnHeaderMap.get(header.getName());
	}

	public String getHeader(String header) {
		return pgnHeaderMap.get(header);
	}

	/**
	 * @return The game number on the ICS server.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Black's initial increment in milliseconds.
	 */
	public long getInitialBlackIncMillis() {
		return initialBlackIncMillis;
	}

	/**
	 * @return Black's initial time in milliseconds.
	 */
	public long getInitialBlackTimeMillis() {
		return initialBlackTimeMillis;
	}

	public int getInitialEpSquare() {
		return positionState.initialEpSquare;
	}

	/**
	 * @return White's initial increment in milliseconds.
	 */
	public long getInitialWhiteIncMillis() {
		return initialWhiteIncMillis;
	}

	/**
	 * @return Black's initial time in milliseconds.
	 */
	public long getInitialWhiteTimeMillis() {
		return initialWhiteTimeMillis;
	}

	/**
	 * Returns the last move made, null if there was not one.
	 */
	public Move getLastMove() {
		if (positionState.moves.getSize() != 0) {
			return positionState.moves.get(positionState.moves.getSize() - 1);
		} else {
			return null;
		}
	}

	/**
	 * Returns a move list of all legal moves in the position.
	 */
	public PriorityMoveList getLegalMoves() {
		PriorityMoveList result = getPseudoLegalMoves();

		for (int i = 0; i < result.getHighPrioritySize(); i++) {
			Move move = result.getHighPriority(i);
			forceMove(move);

			if (!isLegalPosition()) {
				result.removeHighPriority(i);
				i--;
			}

			rollback();
		}

		for (int i = 0; i < result.getLowPrioritySize(); i++) {
			Move move = result.getLowPriority(i);
			forceMove(move);

			if (!isLegalPosition()) {
				result.removeLowPriority(i);
				i--;
			}

			rollback();
		}

		return result;
	}

	/**
	 * Returns a move list of the moves that have been made in the position.
	 * 
	 */
	public MoveList getMoveList() {
		return positionState.moves;
	}

	/**
	 * Returns a bitboard with 1s on all of the squares that do not contain the
	 * color to moves pieces.
	 */
	public long getNotColorToMoveBB() {
		return positionState.notColorToMoveBB;
	}

	/**
	 * Returns a bitboard with 1s on all squares that are occupied in the
	 * position.
	 */
	public long getOccupiedBB() {
		return positionState.occupiedBB;
	}

	/**
	 * Returns the piece at the specified square with its promotion mask
	 * removed.
	 */
	public int getPiece(int square) {
		return positionState.board[square] & NOT_PROMOTED_MASK;
	}

	/**
	 * Returns a bitboard with 1s on all of the squares containing the piece.
	 * 
	 * @param piece
	 *            The un-colored piece constant.
	 */
	public long getPieceBB(int piece) {
		return positionState.pieceBB[0][piece]
				| positionState.pieceBB[1][piece];
	}

	/**
	 * Returns a bitboard with 1s on all squares containing the piece.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The un-colored piece constant.
	 */
	public long getPieceBB(int color, int piece) {
		return positionState.pieceBB[color][piece];
	}

	/**
	 * Returns the number of pieces on the board.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The un-colored piece constant.
	 */
	public int getPieceCount(int color, int piece) {
		return positionState.pieceCounts[color][piece & NOT_PROMOTED_MASK];
	}

	public String getPieceCountsString() {
		return "Piece counts [WP=" + getPieceCount(WHITE, PAWN) + " WN="
				+ getPieceCount(WHITE, KNIGHT) + " WB="
				+ getPieceCount(WHITE, BISHOP) + " WR="
				+ getPieceCount(WHITE, ROOK) + " WQ="
				+ getPieceCount(WHITE, QUEEN) + " WK="
				+ getPieceCount(WHITE, KING) + "][BP="
				+ getPieceCount(BLACK, PAWN) + " BN= "
				+ getPieceCount(BLACK, KNIGHT) + " BB="
				+ getPieceCount(BLACK, BISHOP) + " BR="
				+ getPieceCount(BLACK, ROOK) + " BQ="
				+ getPieceCount(BLACK, QUEEN) + " BK="
				+ getPieceCount(BLACK, KING) + "]";
	}

	/**
	 * Returns the piece at the specified square with its promotion mask.
	 */
	public int getPieceWithPromoteMask(int square) {
		return positionState.board[square];
	}

	/**
	 * Returns the games position state object.
	 */
	public PositionState getPositionState() {
		return positionState;
	}

	/**
	 * Returns a move list containing all pseudo legal moves.
	 */
	public PriorityMoveList getPseudoLegalMoves() {
		PriorityMoveList result = new PriorityMoveList();
		generatePseudoQueenMoves(result);
		generatePseudoKnightMoves(result);
		generatePseudoBishopMoves(result);
		generatePseudoRookMoves(result);
		generatePseudoPawnMoves(result);
		generatePseudoKingMoves(result);
		return result;
	}

	/**
	 * Returns the number of times this position has occured.
	 */
	public int getRepCount() {
		return positionState.moveRepHash[getRepHash()];
	}

	/**
	 * Returns a hash that can be used to reference positionState.moveRepHash.
	 * The hash is created using the zobrist position hash.
	 * 
	 * @return The hash.
	 */
	public int getRepHash() {
		return (int) (positionState.zobristPositionHash & MOVE_REP_CACHE_SIZE_MINUS_1);
	}

	/**
	 * Returns the games result constant.
	 */
	public Result getResult() {
		return result;
	}

	/**
	 * Returns the description of the result. Returns the PgnHeader
	 * RESULT_DESCRIPTION if its empty then returns PgnHeader.RESULT.
	 */
	public String getResultDescription() {
		String result = getHeader(PgnHeader.RESULT_DESCRIPTION);
		if (StringUtils.isBlank(result)) {
			result = getHeader(PgnHeader.RESULT);
		}
		return result;
	}

	/**
	 * Returns the site the game was played at. This is added so its easy to
	 * parse to/from PGN. This sets the PgnHeader SITE
	 */
	public String getRound() {
		return getHeader(PgnHeader.ROUND);
	}

	/**
	 * Returns the PgnHeader SITE
	 */
	public String getSite() {
		return getHeader(PgnHeader.SITE);
	}

	/**
	 * Returns an integer with 1s in all of the states the game has set.
	 * 
	 * @return The games state/
	 */
	public int getState() {
		return state;
	}

	/**
	 * Returns the games type constant.
	 * 
	 * @return
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return White's lag time in milliseconds.
	 */
	public long getWhiteLagMillis() {
		return whiteLagMillis;
	}

	/**
	 * Returns the PgnHeader WHITE
	 * 
	 * @return White's name.
	 */
	public String getWhiteName() {
		return getHeader(PgnHeader.WHITE);
	}

	/**
	 * Returns the PgnHeader WHITE_ELO
	 * 
	 * @return White's rating.
	 */
	public String getWhiteRating() {
		return getHeader(PgnHeader.WHITE_ELO);
	}

	/**
	 * @return White's remaining amount of time on the clock in milliseconds.
	 */
	public long getWhiteRemainingTimeMillis() {
		return whiteRemainingTimeMillis;
	}

	/**
	 * Returns the games Zobrist game hash. The game hash includes state
	 * information such as color to move, castling, and ep info.
	 * 
	 * @return The Zobrist game hash.
	 */
	public long getZobristGameHash() {
		return positionState.zobristGameHash;
	}

	/**
	 * Returns the games Zobrist position hash. The position hash is the Zobrist
	 * WITHOUT state info such as color to move, castling, ep info.
	 * 
	 * @return THe Zobrist position hash.
	 */
	public long getZobristPositionHash() {
		return positionState.zobristPositionHash;
	}

	/**
	 * Returns true if the specified color has at least one drop piece.
	 * 
	 * @param color
	 *            THe color to check,
	 * @return True if the color has a drop piece, otherwise false.
	 */
	public boolean hasNonPawnDropPiece(int color) {
		boolean result = false;
		for (int i = 2; i < positionState.dropCounts[color].length; i++) {
			if (positionState.dropCounts[color][i] > 0) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Increments the drop count. This method handles incrementing pieces with a
	 * promote mask.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The uncolored piece constant.
	 */
	public void incrementDropCount(int color, int piece) {
		if ((piece & PROMOTED_MASK) != 0) {
			piece = PAWN;
		}
		positionState.dropCounts[color][piece]++;
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
	public void incrementPieceCount(int color, int piece) {
		if ((piece & PROMOTED_MASK) != 0) {
			piece &= NOT_PROMOTED_MASK;
			// When a promoted piece is captured it is a pawn.
			// So this is rolling back a promoted piece capture.
			// Decrease the pawn to add it back to the piece counts.
			// Then increase the piece count of the piece captured.
			positionState.pieceCounts[color][PAWN]--;
		}
		positionState.pieceCounts[color][piece]++;
	}

	/**
	 * Increments the move hash repetition count for the current position.
	 */
	public void incrementRepCount() {
		positionState.moveRepHash[getRepHash()]++;
	}

	/**
	 * Returns true if the current position is check-mate.
	 */
	public boolean isCheckmate() {
		return isCheckmate(getLegalMoves());
	}

	/**
	 * Returns true if the current position is check-mate given a list of moves.
	 * This method is provided so move generation does'nt have to be done twice
	 * if it was already performed and a moveList is available.
	 */
	public boolean isCheckmate(PriorityMoveList moveList) {
		return moveList.getSize() == 0 && isInCheck(getColorToMove());
	}

	/**
	 * Returns true if the side to move is in check.
	 * 
	 * @return true if in check, otherwise false.
	 */
	public boolean isInCheck() {
		return isInCheck(positionState.colorToMove, getPieceBB(
				positionState.colorToMove, KING));
	}

	/**
	 * Returns true if the specified color is in check in the specified
	 * position.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @return true if in check, otherwise false.
	 */
	public boolean isInCheck(int color) {
		return isInCheck(color, getPieceBB(color, KING));
	}

	/**
	 * Returns true if the specified square would be in check if it contained a
	 * king.
	 * 
	 * @param color
	 *            WHITE or BLACK.
	 * @param pieceBB
	 *            A bitboard representing the square to to check.
	 * @return true if in check, false otherwise.
	 */
	public boolean isInCheck(int color, long pieceBB) {
		long kingBB = pieceBB;
		int kingSquare = bitscanForward(kingBB);
		int oppositeColor = getOppositeColor(color);

		return !(pawnCapture(oppositeColor, getPieceBB(oppositeColor, PAWN),
				kingBB) == 0L
				&& (orthogonalMove(kingSquare, getEmptyBB(), getOccupiedBB()) & (getPieceBB(
						oppositeColor, ROOK) | getPieceBB(oppositeColor, QUEEN))) == 0L
				&& (diagonalMove(kingSquare, getEmptyBB(), getOccupiedBB()) & (getPieceBB(
						oppositeColor, BISHOP) | getPieceBB(oppositeColor,
						QUEEN))) == 0L
				&& (kingMove(kingSquare) & getPieceBB(oppositeColor, KING)) == 0L && (knightMove(kingSquare) & getPieceBB(
				oppositeColor, KNIGHT)) == 0L);
	}

	/**
	 * Returns true if one of the state flags is in the specified state.
	 */
	public boolean isInState(int state) {
		return (getState() & state) != 0;
	}

	/**
	 * This is one of the methods that needs to be overridden in subclasses.
	 * 
	 * @return If the position is legal.
	 */
	public boolean isLegalPosition() {
		return areBothKingsOnBoard()
				&& !isInCheck(getOppositeColor(getColorToMove()));
	}

	/**
	 * Returns true if SAN, short algebraic notation, is being set on all mvoes
	 * generated by this class. This is an expensive operation and should be
	 * turned off for engines.
	 */
	public boolean isSettingMoveSan() {
		return isSettingMoveSan;
	}

	/**
	 * Returns true if the current position is stalemate.
	 */
	public boolean isStalemate() {
		return isStalemate(getLegalMoves());
	}

	/**
	 * Returns true if the current position is stalemate given a list of moves.
	 * This method is provided so move generation doesnt have to be done twice
	 * if it was already performed and a moveList is available.
	 */
	public boolean isStalemate(PriorityMoveList moveList) {
		return moveList.getSize() == 0 && !isInCheck(getColorToMove());
	}

	/**
	 * @return If it is currently white's move in this Game.
	 */
	public boolean isWhitesMove() {
		return positionState.colorToMove == WHITE;
	}

	protected void makeCastlingMove(Move move) {
		long kingFromBB, kingToBB, rookFromBB, rookToBB;

		if (move.getColor() == WHITE) {
			kingFromBB = E1;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				kingToBB = G1;
				rookFromBB = H1;
				rookToBB = F1;
				updateZobristPOCastleKsideWhite();
			} else {
				kingToBB = C1;
				rookFromBB = A1;
				rookToBB = D1;
				updateZobristPOCastleQsideWhite();
			}
		} else {
			kingFromBB = E8;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				kingToBB = G8;
				rookFromBB = H8;
				rookToBB = F8;
				updateZobristPOCastleKsideBlack();
			} else {
				kingToBB = C8;
				rookFromBB = A8;
				rookToBB = D8;
				updateZobristPOCastleQsideBlack();
			}
		}

		setPiece(bitscanForward(kingFromBB), EMPTY);
		setPiece(bitscanForward(kingToBB), KING);
		setPiece(bitscanForward(rookFromBB), EMPTY);
		setPiece(bitscanForward(rookToBB), ROOK);

		long kingFromTo = kingToBB | kingFromBB;
		long rookFromTo = rookToBB | rookFromBB;

		xor(move.getColor(), KING, kingFromTo);
		xor(move.getColor(), kingFromTo);
		setOccupiedBB(getOccupiedBB() ^ kingFromTo);
		setEmptyBB(getEmptyBB() ^ kingFromTo);

		xor(move.getColor(), ROOK, rookFromTo);
		xor(move.getColor(), rookFromTo);
		setOccupiedBB(getOccupiedBB() ^ rookFromTo);
		setEmptyBB(getEmptyBB() ^ rookFromTo);

		setCastling(getColorToMove(), CASTLE_NONE);

		setEpSquare(EMPTY_SQUARE);
	}

	/**
	 * Makes a drop move given the piece to drop and the destination square.
	 * 
	 * @param piece
	 *            THe piece to drop.
	 * @param destination
	 *            The destination square.
	 * @return The move made.
	 * @throws IllegalArgumentException
	 *             thrown if the move is invalid.
	 */
	public Move makeDropMove(int piece, int destination)
			throws IllegalArgumentException {
		throw new UnsupportedOperationException("Not supported in classical");
	}

	/**
	 * Makes a drop move.
	 * 
	 * @param move
	 */
	protected void makeDropMove(Move move) {
		long toBB = getBitboard(move.getTo());
		int oppositeColor = getOppositeColor(move.getColor());

		xor(move.getColor(), toBB);
		xor(move.getColor(), move.getPiece(), toBB);
		setOccupiedBB(getOccupiedBB() ^ toBB);
		setEmptyBB(getEmptyBB() ^ toBB);

		updateZobristDrop(move, oppositeColor);

		setPiece(move.getTo(), move.getPiece());
		setEpSquare(EMPTY_SQUARE);
	}

	protected void makeEPMove(Move move) {
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;
		long captureBB = getColorToMove() == WHITE ? moveOne(SOUTH, toBB)
				: moveOne(NORTH, toBB);

		int captureSquare = bitscanForward(captureBB);

		xor(move.getColor(), move.getPiece(), fromToBB);
		xor(move.getColor(), fromToBB);
		setOccupiedBB(getOccupiedBB() ^ fromToBB);
		setEmptyBB(getEmptyBB() ^ fromToBB);

		xor(move.getCaptureColor(), move.getPiece(), captureBB);
		xor(move.getCaptureColor(), captureBB);
		setOccupiedBB(getOccupiedBB() ^ captureBB);
		setEmptyBB(getEmptyBB() ^ captureBB);

		setPiece(move.getFrom(), EMPTY);
		setPiece(move.getTo(), PAWN);
		setPiece(captureSquare, EMPTY);

		updateZobristEP(move, captureSquare);
		setEpSquare(EMPTY_SQUARE);
	}

	/**
	 * Makes the specified move in LAN, long algebraic notation.
	 * 
	 * @param lan
	 *            The move in LAN.
	 * @return The move made.
	 * @throws IllegalArgumentException
	 *             Thrown if the move was invalid.
	 */
	public Move makeLanMove(String lan) throws IllegalArgumentException {
		Move move = null;

		Move[] legals = getLegalMoves().asArray();

		for (int i = 0; move == null && i < legals.length; i++) {
			Move candidate = legals[i];
			if (candidate.getLan().equals(lan)) {
				move = candidate;
			}
		}

		if (move == null) {
			throw new IllegalArgumentException("Invalid move: " + lan + " \n"
					+ toString());
		} else {
			forceMove(move);
		}

		return move;
	}

	/**
	 * Makes a move using the start/end square.
	 * 
	 * @param startSquare
	 *            The start square.
	 * @param endSquare
	 *            The end square.
	 * @return The move made.
	 * @throws IllegalArgumentException
	 *             Thrown if the move is illegal.
	 */
	public Move makeMove(int startSquare, int endSquare)
			throws IllegalArgumentException {
		Move move = null;

		Move[] legals = getLegalMoves().asArray();

		for (int i = 0; move == null && i < legals.length; i++) {
			Move candidate = legals[i];
			if (candidate.getFrom() == startSquare
					&& candidate.getTo() == endSquare) {
				move = candidate;
			}
		}

		if (move == null) {
			throw new IllegalArgumentException("Invalid move: "
					+ GameUtils.getSan(startSquare) + " "
					+ GameUtils.getSan(endSquare) + " \n" + toString());
		} else {
			forceMove(move);
		}

		return move;
	}

	/**
	 * Makes a move using the start/end square and the specified promotion
	 * piece.
	 * 
	 * @param startSquare
	 *            The start square.
	 * @param endSquare
	 *            The end square.
	 * @param promotionPiece
	 *            The non colored piece constant representing the promoted
	 *            piece.
	 * @return The move made.
	 * @throws IllegalArgumentException
	 *             Thrown if the move is illegal.
	 */
	public Move makeMove(int startSquare, int endSquare, int promotePiece)
			throws IllegalArgumentException {
		Move move = null;

		Move[] legals = getLegalMoves().asArray();

		for (int i = 0; move == null && i < legals.length; i++) {
			Move candidate = legals[i];
			if (candidate.getFrom() == startSquare
					&& candidate.getTo() == endSquare
					&& candidate.getPiecePromotedTo() == promotePiece) {
				move = candidate;
			}
		}

		if (move == null) {
			throw new IllegalArgumentException("Invalid move: "
					+ GameUtils.getSan(startSquare) + "-"
					+ GameUtils.getSan(endSquare) + "="
					+ GameConstants.PIECE_TO_SAN.charAt(promotePiece) + "\n"
					+ toString());
		} else {
			forceMove(move);
		}

		return move;
	}

	protected void makeNonEpNonCastlingMove(Move move) {
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;
		int oppositeColor = getOppositeColor(move.getColor());

		xor(move.getColor(), fromToBB);

		if (move.isCapture()) {
			setOccupiedBB(getOccupiedBB() ^ fromBB);
			setEmptyBB(getEmptyBB() ^ fromBB);

			xor(oppositeColor, move.getCapture(), toBB);
			xor(oppositeColor, toBB);
			updateZobristPOCapture(move, oppositeColor);
		} else {
			setOccupiedBB(getOccupiedBB() ^ fromToBB);
			setEmptyBB(getEmptyBB() ^ fromToBB);
			updateZobristPONoCapture(move, oppositeColor);
		}

		if (move.isPromotion()) {
			xor(move.getColor(), move.getPiece(), fromBB);

			xor(move.getColor(), move.getPiecePromotedTo() & NOT_PROMOTED_MASK,
					toBB);

			setPiece(move.getTo(), move.getPiecePromotedTo() | PROMOTED_MASK);
			setPiece(move.getFrom(), EMPTY);

			// capture is handled in forceMove.
			// promoted piece never has a promote mask only captures do.
			// Promotes do not effect drop pieces.
			decrementPieceCount(getColorToMove(), PAWN);
			incrementPieceCount(getColorToMove(), move.getPiecePromotedTo());
		} else {
			xor(move.getColor(), move.getPiece(), fromToBB);

			setPiece(move.getTo(), move.getPieceWithPromoteMask());
			setPiece(move.getFrom(), EMPTY);
		}

		switch (move.getPiece()) {
		case KING:
			setCastling(getColorToMove(), CASTLE_NONE);
			break;
		default:
			if (move.getPiece() == ROOK && move.getFrom() == SQUARE_A1
					&& getColorToMove() == WHITE || move.getCapture() == ROOK
					&& move.getTo() == SQUARE_A1 && getColorToMove() == BLACK) {
				setCastling(WHITE, getCastling(WHITE) & CASTLE_SHORT);
			} else if (move.getPiece() == ROOK && move.getFrom() == SQUARE_H1
					&& getColorToMove() == WHITE || move.getCapture() == ROOK
					&& move.getTo() == SQUARE_H1 && getColorToMove() == BLACK) {
				setCastling(WHITE, getCastling(WHITE) & CASTLE_LONG);
			} else if (move.getPiece() == ROOK && move.getFrom() == SQUARE_A8
					&& getColorToMove() == BLACK || move.getCapture() == ROOK
					&& move.getTo() == SQUARE_A8 && getColorToMove() == WHITE) {
				setCastling(BLACK, getCastling(BLACK) & CASTLE_SHORT);
			} else if (move.getPiece() == ROOK && move.getFrom() == SQUARE_H8
					&& getColorToMove() == BLACK || move.getCapture() == ROOK
					&& move.getTo() == SQUARE_H8 && getColorToMove() == WHITE) {
				setCastling(BLACK, getCastling(BLACK) & CASTLE_LONG);
			}
			break;
		}

		setEpSquare(move.getEpSquare());
	}

	/**
	 * Makes a move given SAN, short algebraic notation.
	 * 
	 * @param shortAlgebraic
	 *            The move in SAN.
	 * @return THe move made.
	 * @throws IllegalArgumentException
	 *             Thrown if the move was invalid.
	 */
	public Move makeSanMove(String shortAlgebraic)
			throws IllegalArgumentException {
		SanValidations validations = SanUtil.getValidations(shortAlgebraic);

		// Examples:
		// e4 (a pawn move to e4).
		// e8=Q (a pawn promotion without a capture).
		// de=Q (a pawn promotion from a capture).
		// ed (e pawn captures d pawn).
		// Ne3 (a Knight moving to e3).
		// N5e3 (disambiguity for two knights which can move to e3, the 5th rank
		// knight is the one that should move).
		// Nfe3 (disambiguity for two knights which can move to e3, the knight
		// on the f file is the one that should move).
		// Nf1e3 (disambiguity for three knights which cam move to e3, the f1
		// knight is the one that should move).

		if (!validations.isValidStrict()) {
			throw new IllegalArgumentException("Invalid short algebraic: "
					+ shortAlgebraic);
		}

		int candidatePromotedPiece = EMPTY;

		Move result = null;

		if (validations.isDropMoveStrict()) {
			for (Move move : getPseudoLegalMoves().asArray()) {
				if ((move.getMoveCharacteristic() & Move.DROP_CHARACTERISTIC) != 0
						&& move.getPiece() == SanUtil.sanToPiece(validations
								.getStrictSan().charAt(0))
						&& move.getTo() == GameUtils.rankFileToSquare(
								GameConstants.RANK_FROM_SAN.indexOf(validations
										.getStrictSan().charAt(3)),
								GameConstants.FILE_FROM_SAN.indexOf(validations
										.getStrictSan().charAt(2)))) {
					result = move;
					break;
				}
			}
		} else {
			PriorityMoveList moveList = getPseudoLegalMoves();
			// Lots of removing in droppable games will occur here.
			// If this code is ever used for a ZH or BUG bot you might want to
			// rewrite this.
			for (int i = 0; i < moveList.getHighPrioritySize(); i++) {
				if (moveList.getHighPriority(i).isDrop()) {
					moveList.removeHighPriority(i);
					i--;
				}
			}
			for (int i = 0; i < moveList.getLowPrioritySize(); i++) {
				if (moveList.getLowPriority(i).isDrop()) {
					moveList.removeLowPriority(i);
					i--;
				}
			}
			Move[] pseudoLegals = moveList.asArray();

			if (validations.isCastleKSideStrict()) {

				for (Move move : pseudoLegals) {
					if ((move.getMoveCharacteristic() & Move.SHORT_CASTLING_CHARACTERISTIC) != 0) {
						result = move;
						break;
					}
				}
			} else if (validations.isCastleQSideStrict()) {
				for (Move move : pseudoLegals) {
					if ((move.getMoveCharacteristic() & Move.LONG_CASTLING_CHARACTERISTIC) != 0) {
						result = move;
						break;
					}
				}
			} else {
				MoveList matches = new MoveList(10);
				if (validations.isPromotion()) {
					char pieceChar = validations.getStrictSan().charAt(
							validations.getStrictSan().length() - 1);
					candidatePromotedPiece = SanUtil.sanToPiece(pieceChar);
				}

				if (validations.isPawnMove()) {
					int candidatePieceMoving = PAWN;
					if (validations.isEpOrAmbigPxStrict()
							|| validations.isEpOrAmbigPxPromotionStrict()) {

						int end = GameUtils.rankFileToSquare(
								GameConstants.RANK_FROM_SAN.indexOf(validations
										.getStrictSan().charAt(2)),
								GameConstants.FILE_FROM_SAN.indexOf(validations
										.getStrictSan().charAt(1)));

						int startRank = GameUtils.getRank(end)
								+ (positionState.colorToMove == WHITE ? -1 : +1);

						if (startRank > 7 || startRank < 0) {
							throw new IllegalArgumentException(
									"Invalid short algebraic: "
											+ shortAlgebraic);
						}

						int start = GameUtils.rankFileToSquare(startRank,
								GameConstants.FILE_FROM_SAN.indexOf(validations
										.getStrictSan().charAt(0)));

						for (Move move : pseudoLegals) {
							if (move.getPiece() == candidatePieceMoving
									&& move.isCapture()
									&& move.getFrom() == start
									&& move.getTo() == end
									&& move.getPiecePromotedTo() == candidatePromotedPiece) {
								matches.append(move);
							}
						}
					} else {
						// handle captures
						if (validations.isPxStrict()
								|| validations.isPxPPromotionStrict()) {
							int startFile = GameConstants.FILE_FROM_SAN
									.indexOf(validations.getStrictSan().charAt(
											0));
							int endFile = GameConstants.FILE_FROM_SAN
									.indexOf(validations.getStrictSan().charAt(
											1));

							for (Move move : pseudoLegals) {
								if (move.getPiece() == candidatePieceMoving
										&& GameUtils.getFile(move.getFrom()) == startFile
										&& GameUtils.getFile(move.getTo()) == endFile
										&& move.isCapture()
										&& move.getPiecePromotedTo() == candidatePromotedPiece) {
									matches.append(move);
								}
							}
						}
						// handle non captures.
						else {
							int end = GameUtils.rankFileToSquare(
									GameConstants.RANK_FROM_SAN
											.indexOf(validations.getStrictSan()
													.charAt(1)),
									GameConstants.FILE_FROM_SAN
											.indexOf(validations.getStrictSan()
													.charAt(0)));

							for (Move move : pseudoLegals) {
								if (move.getPiece() == candidatePieceMoving
										&& !move.isCapture()
										&& move.getTo() == end
										&& move.getPiecePromotedTo() == candidatePromotedPiece) {
									matches.append(move);
								}
							}
						}
					}

				} else {
					int candidatePieceMoving = SanUtil.sanToPiece(validations
							.getStrictSan().charAt(0));
					int end = GameUtils.rankFileToSquare(
							GameConstants.RANK_FROM_SAN
									.indexOf(validations.getStrictSan()
											.charAt(
													validations.getStrictSan()
															.length() - 1)),
							GameConstants.FILE_FROM_SAN
									.indexOf(validations.getStrictSan()
											.charAt(
													validations.getStrictSan()
															.length() - 2)));

					if (validations.isDisambigPieceRankStrict()) {
						int startRank = RANK_FROM_SAN.indexOf(validations
								.getStrictSan().charAt(1));
						for (Move move : pseudoLegals) {
							if (move.getPiece() == candidatePieceMoving
									&& move.getTo() == end
									&& GameUtils.getRank(move.getFrom()) == startRank) {
								matches.append(move);
							}
						}
					} else if (validations.isDisambigPieceFileStrict()) {
						int startFile = FILE_FROM_SAN.indexOf(validations
								.getStrictSan().charAt(1));
						for (Move move : pseudoLegals) {
							if (move.getPiece() == candidatePieceMoving
									&& move.getTo() == end
									&& GameUtils.getFile(move.getFrom()) == startFile) {
								matches.append(move);
							}
						}
					} else if (validations.isDisambigPieceRankFileStrict()) {
						int startSquare = GameUtils.rankFileToSquare(
								GameConstants.RANK_FROM_SAN.indexOf(validations
										.getStrictSan().charAt(2)),
								GameConstants.FILE_FROM_SAN.indexOf(validations
										.getStrictSan().charAt(1)));
						FILE_FROM_SAN.indexOf(validations.getStrictSan()
								.charAt(1));
						for (Move move : pseudoLegals) {
							if (move.getPiece() == candidatePieceMoving
									&& move.getTo() == end
									&& move.getFrom() == startSquare) {
								matches.append(move);
							}
						}
					} else {
						for (Move move : pseudoLegals) {
							if (move.getPiece() == candidatePieceMoving
									&& move.getTo() == end) {
								matches.append(move);
							}
						}
					}
				}

				if (matches.getSize() == 0) {
					throw new IllegalArgumentException("Invalid move "
							+ shortAlgebraic + "\n" + toString());
				} else if (matches.getSize() == 1) {
					result = matches.get(0);
				} else {
					// now do legality checking on whats left.
					int kingSquare = GameUtils.bitscanForward(getPieceBB(
							positionState.colorToMove, KING));
					int cachedColorToMove = positionState.colorToMove;
					int matchesCount = 0;

					if (kingSquare != EMPTY_SQUARE) { // Now trim illegals
						for (int i = 0; i < matches.getSize(); i++) {
							Move current = matches.get(i);
							synchronized (this) {
								try {
									forceMove(current);
									if (current.getPiece() == KING) {
										int newKingCoordinates = GameUtils
												.bitscanForward(getPieceBB(
														cachedColorToMove, KING));
										if (!isInCheck(
												cachedColorToMove,
												GameUtils
														.getBitboard(newKingCoordinates))) {
											result = current;
											matchesCount++;
										} else {
										}
									} else {
										if (!isInCheck(cachedColorToMove,
												getBitboard(kingSquare))) {
											result = current;
											matchesCount++;
										} else {
										}
									}
									rollback();
								} catch (IllegalArgumentException ie) {
								}
							}
						}
					}

					if (matchesCount == 0) {
						throw new IllegalArgumentException("Invalid move "
								+ shortAlgebraic + "\n" + toString());
					} else if (matchesCount > 1) {
						throw new IllegalArgumentException("Ambiguous move "
								+ shortAlgebraic + "\n" + toString());

					}
				}
			}
		}

		if (result == null) {
			throw new IllegalArgumentException("Illegal move " + shortAlgebraic
					+ "\n " + toString());
		}

		result.setSan(shortAlgebraic);
		forceMove(result);
		return result;
	}

	/**
	 * Makes a move. If the move is illegal false is returned.
	 * 
	 * @param move
	 *            The move to make.
	 * @return true if the move was legal, false otherwise.
	 */
	public boolean move(Move move) {
		// first make the move.
		forceMove(move);
		if (!isLegalPosition()) {
			rollback();
			return false;
		}
		return true;
	}

	/**
	 * Copys the information from this game into the passed in game.
	 */
	public void overwrite(Game gameToOverwrite, boolean ignoreHashes) {
		gameToOverwrite.id = id;
		gameToOverwrite.state = state;
		gameToOverwrite.type = type;
		gameToOverwrite.initialWhiteTimeMillis = initialWhiteTimeMillis;
		gameToOverwrite.initialBlackTimeMillis = initialBlackTimeMillis;
		gameToOverwrite.initialWhiteIncMillis = initialWhiteIncMillis;
		gameToOverwrite.initialBlackIncMillis = initialBlackIncMillis;
		gameToOverwrite.whiteRemainingTimeMillis = whiteRemainingTimeMillis;
		gameToOverwrite.blackRemainingTimeMillis = blackRemainingTimeMillis;
		gameToOverwrite.whiteLagMillis = whiteLagMillis;
		gameToOverwrite.blackLagMillis = blackLagMillis;
		gameToOverwrite.isSettingMoveSan = isSettingMoveSan;
		gameToOverwrite.pgnHeaderMap = new HashMap<String, String>(pgnHeaderMap);
		gameToOverwrite.positionState = positionState.deepCopy(ignoreHashes);
	}

	public void removeHeader(String header) {
		pgnHeaderMap.remove(header);
	}

	/**
	 * Rolls back the last move made.
	 */
	public void rollback() {

		Move move = getMoveList().removeLast();
		decrementRepCount();

		switch (move.getMoveCharacteristic()) {
		case Move.EN_PASSANT_CHARACTERISTIC:
			rollbackEpMove(move);
			break;
		case Move.SHORT_CASTLING_CHARACTERISTIC:
		case Move.LONG_CASTLING_CHARACTERISTIC:
			rollbackCastlingMove(move);
			break;
		case Move.DROP_CHARACTERISTIC:
			rollbackDropMove(move);
			break;
		default:
			rollbackNonEpNonCastlingMove(move);
			break;
		}

		int oppositeToMove = getOppositeColor(getColorToMove());

		if (move.isCapture()) {
			incrementPieceCount(getColorToMove(), move
					.getCaptureWithPromoteMask());
			decrementDropCount(oppositeToMove, move.getCaptureWithPromoteMask());
		} else if (move.isDrop()) {
			decrementPieceCount(oppositeToMove, move.getPiece());
			incrementDropCount(oppositeToMove, move.getPiece());
		}

		setColorToMove(oppositeToMove);
		setNotColorToMoveBB(~getColorBB(getColorToMove()));
		setHalfMoveCount(getHalfMoveCount() - 1);

		setFiftyMoveCount(move.getPrevious50MoveCount());
		setCastling(getColorToMove(), move.getLastCastleState());

		updateZobristHash();
	}

	protected void rollbackCastlingMove(Move move) {
		long kingFromBB, kingToBB, rookFromBB, rookToBB;

		if (move.getColor() == WHITE) {
			kingFromBB = E1;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				kingToBB = G1;
				rookFromBB = H1;
				rookToBB = F1;
				updateZobristPOCastleKsideWhite();

			} else {
				kingToBB = C1;
				rookFromBB = A1;
				rookToBB = D1;
				updateZobristPOCastleQsideWhite();
			}
		} else {
			kingFromBB = E8;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				kingToBB = G8;
				rookFromBB = H8;
				rookToBB = F8;
				updateZobristPOCastleKsideBlack();
			} else {
				kingToBB = C8;
				rookFromBB = A8;
				rookToBB = D8;
				updateZobristPOCastleQsideBlack();
			}
		}

		setPiece(bitscanForward(kingFromBB), KING);
		setPiece(bitscanForward(kingToBB), EMPTY);
		setPiece(bitscanForward(rookFromBB), ROOK);
		setPiece(bitscanForward(rookToBB), EMPTY);

		long kingFromTo = kingToBB | kingFromBB;
		long rookFromTo = rookToBB | rookFromBB;

		xor(move.getColor(), KING, kingFromTo);
		xor(move.getColor(), kingFromTo);
		setOccupiedBB(getOccupiedBB() ^ kingFromTo);
		setEmptyBB(getEmptyBB() ^ kingFromTo);

		xor(move.getColor(), ROOK, rookFromTo);
		xor(move.getColor(), rookFromTo);
		setOccupiedBB(getOccupiedBB() ^ rookFromTo);
		setEmptyBB(getEmptyBB() ^ rookFromTo);

		setEpSquareFromPreviousMove();
	}

	protected void rollbackDropMove(Move move) {
		long toBB = getBitboard(move.getTo());
		int oppositeColor = getOppositeColor(move.getColor());

		xor(move.getColor(), toBB);
		xor(move.getColor(), move.getPiece(), toBB);
		setOccupiedBB(getOccupiedBB() ^ toBB);
		setEmptyBB(getEmptyBB() ^ toBB);

		updateZobristDrop(move, oppositeColor);

		setPiece(move.getTo(), EMPTY);
		setEpSquare(move.getEpSquare());
	}

	protected void rollbackEpMove(Move move) {
		int oppositeColor = getOppositeColor(getColorToMove());
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;

		long captureBB = oppositeColor == WHITE ? moveOne(SOUTH, toBB)
				: moveOne(NORTH, toBB);
		int captureSquare = bitscanForward(captureBB);

		xor(oppositeColor, move.getPiece(), fromToBB);
		xor(oppositeColor, fromToBB);
		setOccupiedBB(getOccupiedBB() ^ fromToBB);
		setEmptyBB(getEmptyBB() ^ fromToBB);

		xor(getColorToMove(), move.getCapture(), captureBB);
		xor(getColorToMove(), captureBB);

		setPiece(move.getTo(), EMPTY);
		setPiece(move.getFrom(), PAWN);
		setPiece(captureSquare, PAWN);

		updateZobristEP(move, captureSquare);
		setEpSquareFromPreviousMove();
	}

	protected void rollbackNonEpNonCastlingMove(Move move) {
		int oppositeColor = getOppositeColor(move.getColor());
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;

		xor(move.getColor(), fromToBB);

		if (move.isCapture()) {
			setOccupiedBB(getOccupiedBB() ^ fromBB);
			setEmptyBB(getEmptyBB() ^ fromBB);

			xor(oppositeColor, move.getCapture(), toBB);
			xor(oppositeColor, toBB);

			updateZobristPOCapture(move, oppositeColor);

		} else {
			setOccupiedBB(getOccupiedBB() ^ fromToBB);
			setEmptyBB(getEmptyBB() ^ fromToBB);

			updateZobristPONoCapture(move, oppositeColor);
		}

		if (move.isPromotion()) {
			xor(move.getColor(), move.getPiece(), fromBB);
			xor(move.getColor(), move.getPiecePromotedTo() & NOT_PROMOTED_MASK,
					toBB);

			// capture is handled in rollback.
			// promoted pieces never have a promote mask.
			// Promotions do not change drop counts.
			incrementPieceCount(move.getColor(), PAWN);
			decrementPieceCount(move.getColor(), move.getPiecePromotedTo());
		} else {
			xor(move.getColor(), move.getPiece(), fromToBB);
		}

		setPiece(move.getFrom(), move.getPieceWithPromoteMask());
		setPiece(move.getTo(), move.getCaptureWithPromoteMask());

		setEpSquareFromPreviousMove();
	}

	/**
	 * Sets black's lag time in milliseconds.
	 * 
	 * @param blackLagMillis
	 *            Lag time to set.
	 */
	public void setBlackLagMillis(long blackLagMillis) {
		this.blackLagMillis = blackLagMillis;
	}

	/**
	 * Sets black's name. This sets the PgnHeader BLACK
	 * 
	 * @param blackName
	 *            Name to set.
	 */
	public void setBlackName(String blackName) {
		setHeader(PgnHeader.BLACK, blackName);
	}

	/**
	 * Sets black's rating. This sets the PgnHeader BLACK_ELO
	 * 
	 * @param blackRating
	 */
	public void setBlackRating(String blackRating) {
		setHeader(PgnHeader.BLACK_ELO, blackRating);
	}

	/**
	 * Sets black's remaining time in milliseconds.
	 * 
	 * @param blackTimeMillis
	 */
	public void setBlackRemainingTimeMillis(long blackTimeMillis) {
		blackRemainingTimeMillis = blackTimeMillis;
	}

	/**
	 * Sets the positionStates board. This does NOT update any bitboards. Use
	 * with care.
	 * 
	 * @param board
	 *            The positiosn board.
	 */
	public void setBoard(int[] board) {
		positionState.board = board;
	}

	/**
	 * Sets the castling state for the specified color.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param castling
	 *            The new castling state constant.
	 */
	public void setCastling(int color, int castling) {
		positionState.castling[color] = castling;
	}

	/**
	 * Sets the color bitboard for a specified color. This bitboard contains 1s
	 * on all of the squares that contain the specified colors pieces.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param bb
	 *            The new bitboard.
	 */
	public void setColorBB(int color, long bb) {
		positionState.colorBB[color] = bb;
	}

	/**
	 * Sets the color to move. WHITE or BLACK.
	 */
	public void setColorToMove(int color) {
		positionState.colorToMove = color;
	}

	/**
	 * Sets the time in milliseconds the game was created. This is added so its
	 * easy to parse to/from PGN.
	 */
	public void setDate(long startTime) {
		setHeader(PgnHeader.DATE, new Date(startTime).toString());
	}

	/**
	 * Sets the drop count of the specified piece. This is useful for droppable
	 * games like bughouse and crazyhouse.
	 */
	public void setDropCount(int color, int piece, int count) {
		if ((piece & PROMOTED_MASK) != 0) {
			piece = PAWN;
		}
		positionState.dropCounts[color][piece] = count;
	}

	/**
	 * Sets the empty bitboard. This is the bitboard with 1s in all of the empty
	 * squares.
	 */
	public void setEmptyBB(long emptyBB) {
		positionState.emptyBB = emptyBB;
	}

	/**
	 * Sets the EP square. This is the move of the last double pawn push. Can be
	 * EMPTY.
	 */
	public void setEpSquare(int epSquare) {
		positionState.epSquare = epSquare;
	}

	protected void setEpSquareFromPreviousMove() {
		switch (getMoveList().getSize()) {
		case 0:
			setEpSquare(getInitialEpSquare());
			break;
		default:
			setEpSquare(getMoveList().getLast().getEpSquare());
			break;
		}
	}

	/**
	 * Sets the games event. Intended to be used with PGN headers when going
	 * from/to PGN.
	 * 
	 * This sets the PgnHeader EVENT
	 */
	public void setEvent(String event) {
		setHeader(PgnHeader.EVENT, event);
	}

	/**
	 * Returns the current 50 move count. This is the count since the last
	 * non-reversible move. This count is used to determine the 50 move draw
	 * rule.
	 */
	public void setFiftyMoveCount(int fiftyMoveCount) {
		positionState.fiftyMoveCount = fiftyMoveCount;
	}

	/**
	 * Sets the number of half moves played.
	 * 
	 * @param halfMoveCount
	 */
	public void setHalfMoveCount(int halfMoveCount) {
		positionState.halfMoveCount = halfMoveCount;
	}

	public void setHeader(PgnHeader header, String value) {
		value = RaptorStringUtils.removeAll(value, '`');
		value = RaptorStringUtils.removeAll(value, '|');
		value = value.trim();

		pgnHeaderMap.put(header.getName(), value);
	}

	public void setHeader(String header, String value) {
		value = RaptorStringUtils.removeAll(value, '`');
		value = RaptorStringUtils.removeAll(value, '|');
		value = value.trim();
		pgnHeaderMap.put(header, value);
	}

	/**
	 * Sets the games id.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Sets black's initial increment in milliseconds.
	 * 
	 * @param initialBlackIncMillis
	 */
	public void setInitialBlackIncMillis(long initialBlackIncMillis) {
		this.initialBlackIncMillis = initialBlackIncMillis;
	}

	/**
	 * Sets black's initial time in milliseconds.
	 * 
	 * @param initialBlackTimeMillis
	 */
	public void setInitialBlackTimeMillis(long initialBlackTimeMillis) {
		this.initialBlackTimeMillis = initialBlackTimeMillis;
	}

	/**
	 * Returns the initial ep square used to create this game. This is useful
	 * when rolling back the first move. The ep square is the square of the last
	 * double pawn push. Can be EMPTY.
	 */
	public void setInitialEpSquare(int initialEpSquare) {
		positionState.initialEpSquare = initialEpSquare;
	}

	/**
	 * Sets white's initial increment in milliseconds.
	 * 
	 * @param initialWhiteIncMillis
	 */
	public void setInitialWhiteIncMillis(long initialWhiteIncMillis) {
		this.initialWhiteIncMillis = initialWhiteIncMillis;
	}

	/**
	 * Sets white's initial time in milliseconds.
	 * 
	 * @param initialWhiteTimeMillis
	 */
	public void setInitialWhiteTimeMillis(long initialWhiteTimeMillis) {
		this.initialWhiteTimeMillis = initialWhiteTimeMillis;
	}

	/**
	 * Sets the not color to move bitboard to the specified bitboard. This is a
	 * bitmap containing the negation of the bitmap containing 1s where all of
	 * the color to moves pieces are location.
	 * 
	 * @param notColorToMoveBB
	 */
	public void setNotColorToMoveBB(long notColorToMoveBB) {
		positionState.notColorToMoveBB = notColorToMoveBB;
	}

	/**
	 * Sets the occupied bitboard. This is the bitboard with all of the occupied
	 * squares set to 1.
	 * 
	 * @param occupiedBB
	 *            The occupied bitboard.
	 */
	public void setOccupiedBB(long occupiedBB) {
		positionState.occupiedBB = occupiedBB;
	}

	/**
	 * Sets a piece at a specified square. This only updated the
	 * positionState.board object and does NOT update the bitmaps.
	 * 
	 * @param square
	 *            The square.
	 * @param piece
	 *            The un-colored piece constant.
	 */
	public void setPiece(int square, int piece) {
		positionState.board[square] = piece;
	}

	/**
	 * Sets the piece bitboard to the specified bitboard.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The un-colored piece constant.
	 * @param bb
	 *            The new bitboard.
	 */
	public void setPieceBB(int color, int piece, long bb) {
		positionState.pieceBB[color][piece] = bb;
	}

	/**
	 * Sets the piece count for the specified piece. This method handles
	 * un-masking the piece so its ok to pass in promotion masked pieces.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The un-colored piece constant.
	 * @param count
	 *            The new piece count.
	 */
	public void setPieceCount(int color, int piece, int count) {
		positionState.pieceCounts[color][piece & NOT_PROMOTED_MASK] = count;
	}

	/**
	 * Sets the games state object. Should obviously be used with care.
	 */
	public void setPositionState(PositionState positionState) {
		this.positionState = positionState;
	}

	/**
	 * Sets the result to one of the result constants.
	 * 
	 * @param result
	 *            The result.
	 */
	public void setResult(Result result) {
		this.result = result;
		setHeader(PgnHeader.RESULT, result.toString());
	}

	/**
	 * Sets the result description. This sets the PgnHeader RESULT_DESCRIPTION
	 * 
	 * @param resultDescription
	 *            THe result description.
	 */
	public void setResultDescription(String resultDescription) {
		setHeader(PgnHeader.RESULT_DESCRIPTION, resultDescription);
	}

	/**
	 * Returns the site the game was played at. This is added so its easy to
	 * parse to/from PGN. This sets the PgnHeader SITE
	 */
	public void setRound(String round) {
		setHeader(PgnHeader.ROUND, round);
	}

	/**
	 * Should be called before the move is made to update the san field.
	 */
	protected void setSan(Move move) {
		if (isSettingMoveSan() && move.getSan() == null) {
			// TO DO: possible add + or ++ for check/checkmate
			String shortAlgebraic = null;

			if (move.isDrop()) {
				move.setSan(PIECE_TO_SAN.charAt(move.getPiece()) + "@"
						+ GameUtils.getSan(move.getTo()));
			} else if (move.isCastleShort()) {
				shortAlgebraic = "O-O";
			} else if (move.isCastleLong()) {
				shortAlgebraic = "O-O-O";
			} else if (move.getPiece() == PAWN
					&& (move.getMoveCharacteristic() & Move.EN_PASSANT_CHARACTERISTIC) != 0) // e.p.
			// is
			// optional but
			// the x is
			// required.
			// (pawn eps
			// are never
			// unambiguous)
			{
				shortAlgebraic = SanUtil.squareToFileSan(move.getFrom()) + "x"
						+ SanUtil.squareToSan(move.getTo());
			} else if (move.getPiece() == PAWN && move.isCapture()) // Possible
			// formats ed
			// ed5 edQ
			// (pawn captures
			// can be
			// ambiguous)
			{
				int oppositeColorToMove = GameUtils
						.getOppositeColor(getColorToMove());
				long fromBB = getPieceBB(getColorToMove(), PAWN);
				int movesFound = 0;
				while (fromBB != 0) {
					int fromSquare = bitscanForward(fromBB);

					long allPawnCapturesBB = GameUtils.pawnCapture(
							getColorToMove(), getBitboard(fromSquare),
							getColorBB(oppositeColorToMove));

					while (allPawnCapturesBB != 0) {
						int toSquare = bitscanForward(allPawnCapturesBB);
						if (GameUtils.getFile(toSquare) == GameUtils
								.getFile(move.getTo())) {
							movesFound++;
						}
						allPawnCapturesBB = bitscanClear(allPawnCapturesBB);
					}
					fromBB = bitscanClear(fromBB);
				}

				if (movesFound > 1) {
					shortAlgebraic = SanUtil.squareToFileSan(move.getFrom())
							+ "x"
							+ SanUtil.squareToSan(move.getTo())
							+ (move.isPromotion() ? "="
									+ PIECE_TO_SAN.charAt(move
											.getPiecePromotedTo()) : "");
				} else {
					shortAlgebraic = SanUtil.squareToFileSan(move.getFrom())
							+ "x"
							+ SanUtil.squareToFileSan(move.getTo())
							+ (move.isPromotion() ? "="
									+ PIECE_TO_SAN.charAt(move
											.getPiecePromotedTo()) : "");
				}
			} else if (move.getPiece() == PAWN) // e4 (pawn positionState.moves
			// are never
			// ambiguous)
			{
				shortAlgebraic = SanUtil.squareToSan(move.getTo())
						+ (move.isPromotion() ? "="
								+ PIECE_TO_SAN
										.charAt(move.getPiecePromotedTo()) : "");
			} else {
				long fromBB = getPieceBB(getColorToMove(), move.getPiece());
				long toBB = GameUtils.getBitboard(move.getTo());

				int sameFilesFound = 0;
				int sameRanksFound = 0;
				int matchesFound = 0;

				if (move.getPiece() != KING) {
					while (fromBB != 0) {
						int fromSquare = bitscanForward(fromBB);
						long resultBB = 0;

						switch (move.getPiece()) {
						case KNIGHT:
							resultBB = GameUtils.knightMove(fromSquare) & toBB;
							break;
						case BISHOP:
							resultBB = diagonalMove(fromSquare, getEmptyBB(),
									getOccupiedBB())
									& getNotColorToMoveBB() & toBB;
							break;
						case ROOK:
							resultBB = orthogonalMove(fromSquare, getEmptyBB(),
									getOccupiedBB())
									& getNotColorToMoveBB() & toBB;
							break;
						case QUEEN:
							resultBB = orthogonalMove(fromSquare, getEmptyBB(),
									getOccupiedBB())
									& getNotColorToMoveBB()
									& toBB
									| diagonalMove(fromSquare, getEmptyBB(),
											getOccupiedBB())
									& getNotColorToMoveBB() & toBB;
							break;
						}

						if (resultBB != 0) {
							int toSquare = bitscanForward(resultBB);

							if (toSquare == move.getTo()) {
								matchesFound++;
								if (GameUtils.getFile(fromSquare) == GameUtils
										.getFile(move.getFrom())) {
									sameFilesFound++;
								}
								if (GameUtils.getRank(fromSquare) == GameUtils
										.getRank(move.getFrom())) {
									sameRanksFound++;
								}
							}
						}
						fromBB = bitscanClear(fromBB);
					}
				}

				shortAlgebraic = "" + PIECE_TO_SAN.charAt(move.getPiece());
				boolean hasHandledAmbiguity = false;
				if (sameRanksFound > 1) {
					shortAlgebraic += SanUtil.squareToFileSan(move.getFrom());
					hasHandledAmbiguity = true;
				}
				if (sameFilesFound > 1) {
					shortAlgebraic += SanUtil.squareToRankSan(move.getFrom());
					hasHandledAmbiguity = true;
				}
				if (matchesFound > 1 && !hasHandledAmbiguity) {
					shortAlgebraic += SanUtil.squareToFileSan(move.getFrom());
				}

				shortAlgebraic += (move.isCapture() ? "x" : "")
						+ SanUtil.squareToSan(move.getTo());
			}

			move.setSan(shortAlgebraic);
		}
	}

	/**
	 * Sets whether or not the game is updating SAN, short algebraic notation,
	 * on moves produced. This is an expensive operation and should probably be
	 * avoided in chess engines.
	 * 
	 * @param isSettingMoveSan
	 *            True if the game should set SAN notation, false otherwise.
	 */
	public void setSettingMoveSan(boolean isSettingMoveSan) {
		this.isSettingMoveSan = isSettingMoveSan;
	}

	/**
	 * Returns the site the game was played at. This is added so its easy to
	 * parse to/from PGN. This sets the PgnHeader SITE
	 */
	public void setSite(String site) {
		setHeader(PgnHeader.SITE, site);
	}

	protected void setState(int state) {
		this.state = state;
	}

	/**
	 * Sets the game type to one of the game type constants. This sets the
	 * PgnHeader VARIANT
	 * 
	 * @param type
	 *            The games type.
	 */
	public void setType(Type type) {
		this.type = type;
		if (type != null) {
			setHeader(PgnHeader.VARIANT, type.toString());
		}
	}

	/**
	 * Sets white's lag time in milliseconds.
	 * 
	 * @param whiteLagMillis
	 */
	public void setWhiteLagMillis(long whiteLagMillis) {
		this.whiteLagMillis = whiteLagMillis;
	}

	/**
	 * Sets white's name. This sets the PgnHeader.WHITE
	 * 
	 * @param whiteName
	 */
	public void setWhiteName(String whiteName) {
		setHeader(PgnHeader.WHITE, whiteName);
	}

	/**
	 * Sets white's rating. This sets the PgnHeader.WHITE_ELO
	 * 
	 * @param whiteRating
	 */
	public void setWhiteRating(String whiteRating) {
		setHeader(PgnHeader.WHITE_ELO, whiteRating);

	}

	public void setWhiteRemainingeTimeMillis(long whiteTimeMillis) {
		whiteRemainingTimeMillis = whiteTimeMillis;
	}

	public void setZobristGameHash(long hash) {
		positionState.zobristGameHash = hash;
	}

	public void setZobristPositionHash(long hash) {
		positionState.zobristPositionHash = hash;
	}

	/**
	 * Returns the FEN, Forsyth Edwards notation, of the game.
	 * 
	 * @return The games position in FEN.
	 */
	public String toFEN() {
		// rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1

		StringBuilder result = new StringBuilder(77);
		for (int j = 7; j > -1; j--) {
			int consecutiveEmpty = 0;
			for (int i = 0; i < 8; i++) {
				int square = rankFileToSquare(j, i);
				int piece = getPiece(square);

				if (piece == EMPTY) {
					consecutiveEmpty++;
				} else {
					long squareBB = getBitboard(square);
					int color = (getPieceBB(WHITE, piece) & squareBB) != 0L ? WHITE
							: BLACK;
					if (consecutiveEmpty > 0) {
						result.append(consecutiveEmpty);
						consecutiveEmpty = 0;
					}
					result.append(COLOR_PIECE_TO_CHAR[color].charAt(piece));
				}
			}
			if (j != 0) {
				result.append((consecutiveEmpty != 0 ? consecutiveEmpty : "")
						+ "/");
			}
		}

		result.append(positionState.colorToMove == WHITE ? " w" : " b");

		result.append(" " + getFenCastle());
		result.append(" " + getSan(positionState.epSquare));
		result.append(" " + positionState.fiftyMoveCount);
		result.append(" " + positionState.halfMoveCount);

		return result.toString();
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
				result.append("To Move: "
						+ COLOR_DESCRIPTION[positionState.colorToMove]
						+ " "
						+ "Last Move: "
						+ (positionState.moves.getSize() == 0 ? ""
								: positionState.moves.getLast()));
				break;

			case 6:
				result.append(getPieceCountsString());
				break;
			case 5:
				result.append("Moves: " + positionState.halfMoveCount + " EP: "
						+ getSan(positionState.epSquare) + " Castle: "
						+ getFenCastle());
				break;
			case 4:
				result.append("FEN: " + toFEN());
				break;
			case 3:
				result.append("State: " + state + " Type=" + type + " Result="
						+ this.result);
				break;
			case 2:
				result.append("Event: " + getEvent() + " Site=" + getSite()
						+ " Time=" + getDate());
				break;
			case 1:
				result.append("WhiteName: " + getWhiteName() + " BlackName="
						+ getBlackName() + " WhiteTime="
						+ whiteRemainingTimeMillis + " whiteLag="
						+ whiteLagMillis + " blackRemainingTImeMillis = "
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

		String legalMovesString = Arrays.toString(getLegalMoves().asArray());
		result.append("\n");
		result.append(WordUtils.wrap("Legals=" + legalMovesString, 80, "\n",
				true));
		result.append(WordUtils.wrap("Movelist=" + positionState.moves, 80,
				"\n", true));

		List<String> squaresWithPromoteMasks = new LinkedList<String>();
		for (int i = 0; i < positionState.board.length; i++) {
			if ((getPieceWithPromoteMask(i) & PROMOTED_MASK) != 0) {
				squaresWithPromoteMasks.add(getSan(i));
			}
		}
		result.append("Squares with promote masks: " + squaresWithPromoteMasks);

		return result.toString();
	}

	/**
	 * Returns string representation of the current game suitable for displaying
	 * to a user.
	 * 
	 * @return A string representation of the current game.
	 */
	public String toUserString() {
		StringBuilder result = new StringBuilder(1000);

		String legalMovesString = Arrays.toString(getLegalMoves().asArray()); // "DISABLED";
		String legalMovesString1 = legalMovesString.length() > 75 ? legalMovesString
				.substring(0, 75)
				: legalMovesString;

		legalMovesString = legalMovesString.length() <= 75 ? ""
				: legalMovesString.substring(75, legalMovesString.length());

		String legalMovesString2 = legalMovesString.length() > 85 ? legalMovesString
				.substring(0, 85)
				: legalMovesString;

		legalMovesString = legalMovesString.length() <= 85 ? ""
				: legalMovesString.substring(85, legalMovesString.length());

		String legalMovesString3 = legalMovesString.length() > 85 ? legalMovesString
				.substring(0, 85)
				: legalMovesString;

		legalMovesString = legalMovesString.length() <= 85 ? ""
				: legalMovesString.substring(85, legalMovesString.length());

		String legalMovesString4 = legalMovesString;

		result.append("\n\n");
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
				break;
			case 6:
				result.append("To Move: "
						+ COLOR_DESCRIPTION[positionState.colorToMove]
						+ " "
						+ "Last Move: "
						+ (positionState.moves.getSize() == 0 ? ""
								: positionState.moves.getLast()));
				break;
			case 5:
				result.append("Moves: " + positionState.halfMoveCount + " EP: "
						+ getSan(positionState.epSquare) + " Castle: "
						+ getFenCastle());
				break;
			case 4:
				break;
			case 3:
				result.append("Legals: " + legalMovesString1);
				break;
			case 2:
				result.append("  " + legalMovesString2);
				break;
			case 1:
				result.append("  " + legalMovesString3);
				break;
			default:
				result.append("  " + legalMovesString4);
				break;
			}

			result.append("\n");
		}
		// result.append("\n MoveList:" + positionState.moves);
		return result.toString();
	}

	protected void updateZobristDrop(Move move, int oppositeColor) {
		positionState.zobristPositionHash ^= ZobristHash.zobrist(move
				.getColor(), move.getPiece() & NOT_PROMOTED_MASK, move.getTo());
	}

	protected void updateZobristEP(Move move, int captureSquare) {
		positionState.zobristPositionHash ^= ZobristHash.zobrist(move
				.getColor(), PAWN, move.getFrom())
				^ ZobristHash.zobrist(move.getColor(), PAWN, move.getTo())
				^ ZobristHash.zobrist(move.getCaptureColor(), PAWN,
						captureSquare);
	}

	protected void updateZobristHash() {
		positionState.zobristGameHash = positionState.zobristPositionHash
				^ ZobristHash.zobrist(getColorToMove(), getEpSquare(),
						getCastling(WHITE), getCastling(BLACK));
	}

	protected void updateZobristPOCapture(Move move, int oppositeColor) {
		positionState.zobristPositionHash ^= ZobristHash.zobrist(move
				.getColor(), move.isPromotion() ? move.getPiecePromotedTo()
				& NOT_PROMOTED_MASK : move.getPiece() & NOT_PROMOTED_MASK, move
				.getTo())
				^ ZobristHash.zobrist(oppositeColor, move.getCapture()
						& NOT_PROMOTED_MASK, move.getTo())
				^ ZobristHash.zobrist(move.getColor(), move.getPiece()
						& NOT_PROMOTED_MASK, move.getFrom());
	}

	protected void updateZobristPOCastleKsideBlack() {
		positionState.zobristPositionHash ^= ZobristHash.zobrist(BLACK, KING,
				SQUARE_E8)
				^ ZobristHash.zobrist(BLACK, KING, SQUARE_G8)
				^ ZobristHash.zobrist(BLACK, ROOK, SQUARE_H8)
				^ ZobristHash.zobrist(BLACK, ROOK, SQUARE_F8);
	}

	protected void updateZobristPOCastleKsideWhite() {
		positionState.zobristPositionHash ^= ZobristHash.zobrist(WHITE, KING,
				SQUARE_E1)
				^ ZobristHash.zobrist(WHITE, KING, SQUARE_G1)
				^ ZobristHash.zobrist(WHITE, ROOK, SQUARE_H1)
				^ ZobristHash.zobrist(WHITE, ROOK, SQUARE_F1);
	}

	protected void updateZobristPOCastleQsideBlack() {
		positionState.zobristPositionHash ^= ZobristHash.zobrist(BLACK, KING,
				SQUARE_E8)
				^ ZobristHash.zobrist(BLACK, KING, SQUARE_C8)
				^ ZobristHash.zobrist(BLACK, ROOK, SQUARE_A8)
				^ ZobristHash.zobrist(BLACK, ROOK, SQUARE_D8);
	}

	protected void updateZobristPOCastleQsideWhite() {
		positionState.zobristPositionHash ^= ZobristHash.zobrist(WHITE, KING,
				SQUARE_E1)
				^ ZobristHash.zobrist(WHITE, KING, SQUARE_C1)
				^ ZobristHash.zobrist(WHITE, ROOK, SQUARE_A1)
				^ ZobristHash.zobrist(WHITE, ROOK, SQUARE_D1);
	}

	protected void updateZobristPONoCapture(Move move, int oppositeColor) {
		positionState.zobristPositionHash ^= ZobristHash.zobrist(move
				.getColor(), move.isPromotion() ? move.getPiecePromotedTo()
				& NOT_PROMOTED_MASK : move.getPiece() & NOT_PROMOTED_MASK, move
				.getTo())
				^ ZobristHash.zobrist(move.getColor(), move.getPiece()
						& NOT_PROMOTED_MASK, move.getFrom());
	}

	/**
	 * Exclusive bitwise ors the games piece bitboard with the specified
	 * bitboard.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The non-colored piece type.
	 * @param bb
	 *            The bitmap to XOR.
	 */
	public void xor(int color, int piece, long bb) {
		positionState.pieceBB[color][piece] ^= bb;
	}

	/**
	 * Exclusive bitwise ors the games color bitboard with the specified
	 * bitboard.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param bb
	 *            The bitmap to XOR.
	 */
	public void xor(int color, long bb) {
		positionState.colorBB[color] ^= bb;
	}

}
