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

import static raptor.chess.util.GameUtils.*;
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
			initialLongRookFile = firstRook;
			initialShortRookFile = secondRook;
		}
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

	protected boolean emptyBetweenFiles(int rank, int startFile, int endFile) {
		// This is ugly should be rewritten using a bit board trick.
		boolean result = true;
		for (int i = startFile + 1; i < endFile; i++) {
			result = getPiece(getSquare(rank, i)) == EMPTY;
			if (!result) {
				break;
			}
		}
		return result;
	}
	
	protected boolean inCheckBetweenFiles(int rank, int startFile, int endFile,int color) {
		// This is ugly should be rewritten using a bit board trick.
		boolean result = false;
		for (int i = startFile + 1; i < endFile; i++) {
			int square = getSquare(rank,i);
			result = isInCheck(color,square);
			if (result) {
				break;
			}
		}
		return result;
	}

	/**
	 * Generates all of the pseudo legal king castling moves in the position and
	 * adds them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	@Override
	protected void generatePseudoKingCastlingMoves(long fromBB,
			PriorityMoveList moves) {
		// The king destination square isnt checked, its checked when legal
		// getMoves() are checked.
		int fromSquare = getSquare(fromBB);

		if (getColorToMove() == WHITE
				&& (getCastling(getColorToMove()) & CASTLE_SHORT) != 0
				&& fromBB == getBitboard(getSquare(0, initialKingFile))
				&& emptyBetweenFiles(0,initialLongRookFile,initialKingFile)){
		//		&& !inCheckBetweenFiles(initialLongRookFile, E1) && !isInCheck(WHITE, F1)) {
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

	@Override
	public PriorityMoveList getLegalMoves() {
		return null;
	}

	@Override
	public boolean isLegalPosition() {
		return false;
	}

	@Override
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

	@Override
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

}
