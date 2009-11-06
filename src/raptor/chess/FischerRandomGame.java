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
package raptor.chess;

import static raptor.chess.util.GameUtils.bitscanClear;
import static raptor.chess.util.GameUtils.bitscanForward;
import static raptor.chess.util.GameUtils.getBitboard;
import static raptor.chess.util.GameUtils.getFile;
import static raptor.chess.util.GameUtils.getSquare;
import static raptor.chess.util.ZobristUtils.zobrist;
import raptor.chess.pgn.PgnHeader;

/**
 * <pre>
 * White &quot;a&quot;-side castling (0-0-0):
 * --------------------------------
 * Before: Kg1; Rf1, e1, d1, c1, b1 or a1	     After: Kc1; Rd1.
 * Before: Kf1; Re1, d1, c1, b1, or a1	     After: Kc1; Rd1.
 * Before: Ke1; Rd1, c1, b1, or a1 	     After: Kc1; Rd1.
 * Before: Kd1; Rc1, b1 or a1		     After: Kc1; Rd1.
 * Before: Kc1; Rb1 or a1			     After: Kc1; Rd1.
 * Before: Kb1; Ra1			     After: Kc1; Rd1.
 * 
 * White &quot;h&quot;-side castling (0-0):
 * ------------------------------
 * Before: Kb1; Rc1, d1, e1, f1, g1 or h1.      After: Kg1; Rf1.
 * Before: Kc1; Rd1, e1, f1, g1 or h1	     After: Kg1; Rf1.
 * Before: Kd1; Re1, f1, g1 or h1		     After: Kg1; Rf1.
 * Before: Ke1; Rf1, g1 or h1		     After: Kg1; Rf1.
 * Before: Kf1; Rg1 or h1			     After: Kg1; Rf1.
 * Before: Kg1; Rh1			     After: Kg1; Rf1.
 * Black &quot;a&quot;-side castling (... 0-0-0):
 * ------------------------------------
 * Before: Kg8; Rf8, e8, d8, c8, b8 or a8	     After: Kc8; Rd8.
 * Before: Kf8; Re8, d8, c8, b8 or a8	     After: Kc8; Rd8.
 * Before: Ke8; Rd8, c8, b8 or a8		     After: Kc8; Rd8.
 * Before: Kd8; Rc8, b8 or a8		     After: Kc8; Rd8.
 * Before: Kc8; Rb8 or a8			     After: Kc8; Rd8.
 * Before: Kb8; Ra8			     After: Kc8; Rd8.
 * 
 * Black &quot;h&quot;-side castling (... 0-0): 
 * ----------------------------------
 * Before: Kb8; Rc8, d8, e8, f8, g8 or h8	     After: Kg8; Rf8.
 * Before: Kc8; Rd8, e8, f8, g8 or h8	     After: Kg8; Rf8.
 * Before: Kd8; Re8, f8, g8 or h8		     After: Kg8; Rf8.
 * Before: Ke8; Rf8, g8 or h8		     After: Kg8; Rf8.
 * Before: Kf8; Rg8 or h8			     After: Kg8; Rf8.
 * Before: Kg8; Rh8			     After: Kg8; Rf8.
 * </pre>
 * 
 * TO DO: add in castling support.
 */
public class FischerRandomGame extends ClassicGame {

	protected int initialLongRookFile;
	protected int initialShortRookFile;
	protected int initialKingFile;

	public FischerRandomGame() {
		setHeader(PgnHeader.Variant, Variant.fischerRandom.name());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FischerRandomGame deepCopy(boolean ignoreHashes) {
		FischerRandomGame result = new FischerRandomGame();
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
	 * Returns true if all the squares between startFile and endFile are empty
	 * (excluding the passed in files).
	 */
	protected boolean emptyBetweenFiles(int rank, int startFile, int endFile) {
		boolean result = true;
		for (int i = startFile + 1; i < endFile; i++) {
			result = getPiece(getSquare(rank, i)) == EMPTY;
			if (!result) {
				break;
			}
		}
		return result;
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void generatePseudoKingCastlingMoves(long fromBB,
			PriorityMoveList moves) {
		int kingSquare = getColorToMove() == WHITE ? getSquare(0,
				initialKingFile) : getSquare(7, initialKingFile);
		long kingSquareBB = getBitboard(kingSquare);

		if (getColorToMove() == WHITE
				&& (getCastling(getColorToMove()) & CASTLE_SHORT) != 0
				&& fromBB == kingSquareBB
				&& emptyBetweenFiles(0, initialKingFile, initialShortRookFile)
				&& isKingEmptyOrRook(SQUARE_F1, WHITE)
				&& isKingEmptyOrRook(SQUARE_G1, WHITE)
				&& !isCastlePathInCheck(kingSquare, SQUARE_G1, WHITE)) {

			moves.appendLowPriority(new Move(kingSquare, SQUARE_G1, KING,
					WHITE, EMPTY, Move.SHORT_CASTLING_CHARACTERISTIC));
		}

		if (getColorToMove() == BLACK
				&& (getCastling(getColorToMove()) & CASTLE_SHORT) != 0
				&& fromBB == kingSquareBB
				&& emptyBetweenFiles(7, initialKingFile, initialShortRookFile)
				&& isKingEmptyOrRook(SQUARE_F8, BLACK)
				&& isKingEmptyOrRook(SQUARE_G8, BLACK)
				&& !isCastlePathInCheck(kingSquare, SQUARE_G8, BLACK)) {

			moves.appendLowPriority(new Move(kingSquare, SQUARE_G8, KING,
					BLACK, EMPTY, Move.SHORT_CASTLING_CHARACTERISTIC));
		}

		if (getColorToMove() == WHITE
				&& (getCastling(getColorToMove()) & CASTLE_LONG) != 0
				&& fromBB == kingSquareBB
				&& emptyBetweenFiles(0, initialLongRookFile, initialKingFile)
				&& isKingEmptyOrRook(SQUARE_D1, WHITE)
				&& isKingEmptyOrRook(SQUARE_C1, WHITE)
				&& !isCastlePathInCheck(kingSquare, SQUARE_C1, WHITE)) {
			moves.appendLowPriority(new Move(kingSquare, SQUARE_C1, KING,
					WHITE, EMPTY, Move.LONG_CASTLING_CHARACTERISTIC));
		}

		if (getColorToMove() == BLACK
				&& (getCastling(getColorToMove()) & CASTLE_LONG) != 0
				&& fromBB == kingSquareBB
				&& emptyBetweenFiles(7, initialLongRookFile, initialKingFile)
				&& isKingEmptyOrRook(SQUARE_D8, BLACK)
				&& isKingEmptyOrRook(SQUARE_C8, BLACK)
				&& !isCastlePathInCheck(kingSquare, SQUARE_C8, BLACK)) {
			moves.appendLowPriority(new Move(kingSquare, SQUARE_C8, KING,
					BLACK, EMPTY, Move.LONG_CASTLING_CHARACTERISTIC));
		}
	}

	/**
	 * Returns true if any of the squares between king startSquare and
	 * kingEndSquare are in check (including the start/end squares).
	 * kingStartSquare can either be < or > kingEndSquare.
	 */
	protected boolean isCastlePathInCheck(int kingStartSquare,
			int kingEndSquare, int color) {
		boolean result = false;
		if (kingStartSquare < kingEndSquare) {
			for (int i = kingStartSquare; !result && i < kingEndSquare; i++) {
				result = isInCheck(color, getBitboard(i));
			}
		} else {
			for (int i = kingEndSquare; !result && i < kingStartSquare; i++) {
				result = isInCheck(color, getBitboard(i));
			}
		}
		return result;
	}

	/**
	 * Returns true if the specified square is either empty or a king or rook of
	 * the specified color.
	 */
	protected boolean isKingEmptyOrRook(int square, int color) {
		return board[square] == EMPTY || board[square] == KING
				&& (getColorBB(color) & getBitboard(square)) != 0
				|| board[square] == ROOK
				&& (getColorBB(color) & getBitboard(square)) != 0;
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void makeCastlingMove(Move move) {

		int kingFromSquare = move.getColor() == WHITE ? getSquare(0,
				initialKingFile) : getSquare(7, initialKingFile);
		long kingFromBB = getBitboard(kingFromSquare);
		long kingToBB, rookFromBB, rookToBB;
		int rookFromSquare;

		if (move.getColor() == WHITE) {
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = getSquare(0, initialShortRookFile);
				kingToBB = G1;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = F1;
				updateZobristCastle(WHITE, kingFromSquare, rookFromSquare,
						SQUARE_G1, SQUARE_F1);
			} else {
				rookFromSquare = getSquare(0, initialLongRookFile);
				kingToBB = C1;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = D1;
				updateZobristCastle(WHITE, kingFromSquare, rookFromSquare,
						SQUARE_C1, SQUARE_D1);
			}
		} else {
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = getSquare(7, initialShortRookFile);
				kingToBB = G8;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = F8;
				updateZobristCastle(BLACK, kingFromSquare, rookFromSquare,
						SQUARE_G8, SQUARE_F8);
			} else {
				rookFromSquare = getSquare(7, initialLongRookFile);
				kingToBB = C8;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = D8;
				updateZobristCastle(BLACK, kingFromSquare, rookFromSquare,
						SQUARE_C8, SQUARE_D8);
			}
		}

		if (kingToBB != kingFromBB) {
			long kingFromTo = kingToBB | kingFromBB;
			setPiece(bitscanForward(kingToBB), KING);
			setPiece(kingFromSquare, EMPTY);
			xor(move.getColor(), KING, kingFromTo);
			xor(move.getColor(), kingFromTo);
			setOccupiedBB(getOccupiedBB() ^ kingFromTo);
			setEmptyBB(getEmptyBB() ^ kingFromTo);
		}

		if (rookFromBB != rookToBB) {
			long rookFromTo = rookToBB | rookFromBB;
			setPiece(bitscanForward(rookToBB), ROOK);
			setPiece(rookFromSquare, EMPTY);
			xor(move.getColor(), ROOK, rookFromTo);
			xor(move.getColor(), rookFromTo);
			setOccupiedBB(getOccupiedBB() ^ rookFromTo);
			setEmptyBB(getEmptyBB() ^ rookFromTo);
		}

		setCastling(getColorToMove(), CASTLE_NONE);
		setEpSquare(EMPTY_SQUARE);
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void rollbackCastlingMove(Move move) {
		int kingFromSquare = move.getColor() == WHITE ? getSquare(0,
				initialKingFile) : getSquare(7, initialKingFile);
		long kingFromBB = getBitboard(kingFromSquare);
		long kingToBB, rookFromBB, rookToBB;
		int rookFromSquare;

		if (move.getColor() == WHITE) {
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = getSquare(0, initialShortRookFile);
				kingToBB = G1;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = F1;
				updateZobristCastle(WHITE, kingFromSquare, rookFromSquare,
						SQUARE_G1, SQUARE_F1);
			} else {
				rookFromSquare = getSquare(0, initialLongRookFile);
				kingToBB = C1;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = D1;
				updateZobristCastle(WHITE, kingFromSquare, rookFromSquare,
						SQUARE_C1, SQUARE_D1);
			}
		} else {
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = getSquare(7, initialShortRookFile);
				kingToBB = G8;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = F8;
				updateZobristCastle(BLACK, kingFromSquare, rookFromSquare,
						SQUARE_G8, SQUARE_F8);
			} else {
				rookFromSquare = getSquare(7, initialLongRookFile);
				kingToBB = C8;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = D8;
				updateZobristCastle(BLACK, kingFromSquare, rookFromSquare,
						SQUARE_C8, SQUARE_D8);
			}
		}

		if (kingToBB != kingFromBB) {
			long kingFromTo = kingToBB | kingFromBB;
			setPiece(bitscanForward(kingToBB), EMPTY);
			setPiece(kingFromSquare, KING);
			xor(move.getColor(), KING, kingFromTo);
			xor(move.getColor(), kingFromTo);
			setOccupiedBB(getOccupiedBB() ^ kingFromTo);
			setEmptyBB(getEmptyBB() ^ kingFromTo);
		}

		if (rookFromBB != rookToBB) {
			long rookFromTo = rookToBB | rookFromBB;
			setPiece(bitscanForward(rookToBB), EMPTY);
			setPiece(rookFromSquare, ROOK);
			xor(move.getColor(), ROOK, rookFromTo);
			xor(move.getColor(), rookFromTo);
			setOccupiedBB(getOccupiedBB() ^ rookFromTo);
			setEmptyBB(getEmptyBB() ^ rookFromTo);
		}

		setEpSquareFromPreviousMove();
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void updateCastlingRightsForNonEpNonCastlingMove(Move move) {

		int shortRookSquare = getColorToMove() == WHITE ? getSquare(0,
				initialShortRookFile) : getSquare(7, initialShortRookFile);
		int longRookSquare = getColorToMove() == WHITE ? getSquare(0,
				initialLongRookFile) : getSquare(7, initialLongRookFile);

		switch (move.getPiece()) {
		case KING:
			setCastling(getColorToMove(), CASTLE_NONE);
			break;
		default:
			if (move.getPiece() == ROOK && move.getFrom() == longRookSquare
					&& getColorToMove() == WHITE || move.getCapture() == ROOK
					&& move.getTo() == longRookSquare
					&& getColorToMove() == BLACK) {
				setCastling(WHITE, getCastling(WHITE) & CASTLE_SHORT);
			} else if (move.getPiece() == ROOK
					&& move.getFrom() == shortRookSquare
					&& getColorToMove() == WHITE || move.getCapture() == ROOK
					&& move.getTo() == shortRookSquare
					&& getColorToMove() == BLACK) {
				setCastling(WHITE, getCastling(WHITE) & CASTLE_LONG);
			} else if (move.getPiece() == ROOK
					&& move.getFrom() == longRookSquare
					&& getColorToMove() == BLACK || move.getCapture() == ROOK
					&& move.getTo() == longRookSquare
					&& getColorToMove() == WHITE) {
				setCastling(BLACK, getCastling(BLACK) & CASTLE_SHORT);
			} else if (move.getPiece() == ROOK
					&& move.getFrom() == shortRookSquare
					&& getColorToMove() == BLACK || move.getCapture() == ROOK
					&& move.getTo() == shortRookSquare
					&& getColorToMove() == WHITE) {
				setCastling(BLACK, getCastling(BLACK) & CASTLE_LONG);
			}
			break;
		}
	}

	/**
	 * Updates the zobrist position hash with the specified castling information
	 */
	protected void updateZobristCastle(int color, int kingStartSquare,
			int rookStartSquare, int kingEndSquare, int rookEndSquare) {
		zobristPositionHash ^= zobrist(color, KING, kingStartSquare)
				^ zobrist(color, KING, kingStartSquare)
				^ zobrist(color, ROOK, rookStartSquare)
				^ zobrist(color, ROOK, rookEndSquare);
	}

}
