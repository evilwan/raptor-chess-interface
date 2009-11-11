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

import static raptor.chess.util.GameUtils.bitscanForward;
import static raptor.chess.util.GameUtils.getBitboard;
import static raptor.chess.util.GameUtils.getSquare;
import static raptor.chess.util.ZobristUtils.zobrist;

/**
 * Contains utility methods for fischer random chess. Since java doesnt allow
 * multiple inheritance this is used to avoid duplicate code.
 * 
 * NOTE: the xoring for the zobrist is broken. It would need to be fixed to rely
 * on that for a computer program.
 */
public class FischerRandomUtils implements GameConstants {

	/**
	 * Returns true if all the squares between startFile and endFile are empty
	 * (excluding the passed in files).
	 */
	public static boolean emptyBetweenFiles(ClassicGame game, int rank,
			int startFile, int endFile) {
		boolean result = true;
		for (int i = startFile + 1; i < endFile; i++) {
			result = game.getPiece(getSquare(rank, i)) == EMPTY;
			if (!result) {
				break;
			}
		}
		return result;
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	public static void generatePseudoKingCastlingMoves(ClassicGame game,
			long fromBB, PriorityMoveList moves, int initialKingFile,
			int initialShortRookFile, int initialLongRookFile) {
		int kingSquare = game.getColorToMove() == WHITE ? getSquare(0,
				initialKingFile) : getSquare(7, initialKingFile);
		long kingSquareBB = getBitboard(kingSquare);

		if (game.getColorToMove() == WHITE
				&& (game.getCastling(game.getColorToMove()) & CASTLE_SHORT) != 0
				&& fromBB == kingSquareBB
				&& emptyBetweenFiles(game, 0, initialKingFile,
						initialShortRookFile)
				&& isKingEmptyOrRook(game, SQUARE_F1, WHITE)
				&& isKingEmptyOrRook(game, SQUARE_G1, WHITE)
				&& !isCastlePathInCheck(game, kingSquare, SQUARE_G1, WHITE)) {

			moves.appendLowPriority(new Move(kingSquare, SQUARE_G1, KING,
					WHITE, EMPTY, Move.SHORT_CASTLING_CHARACTERISTIC));
		}

		if (game.getColorToMove() == BLACK
				&& (game.getCastling(game.getColorToMove()) & CASTLE_SHORT) != 0
				&& fromBB == kingSquareBB
				&& emptyBetweenFiles(game, 7, initialKingFile,
						initialShortRookFile)
				&& isKingEmptyOrRook(game, SQUARE_F8, BLACK)
				&& isKingEmptyOrRook(game, SQUARE_G8, BLACK)
				&& !isCastlePathInCheck(game, kingSquare, SQUARE_G8, BLACK)) {

			moves.appendLowPriority(new Move(kingSquare, SQUARE_G8, KING,
					BLACK, EMPTY, Move.SHORT_CASTLING_CHARACTERISTIC));
		}

		if (game.getColorToMove() == WHITE
				&& (game.getCastling(game.getColorToMove()) & CASTLE_LONG) != 0
				&& fromBB == kingSquareBB
				&& emptyBetweenFiles(game, 0, initialLongRookFile,
						initialKingFile)
				&& isKingEmptyOrRook(game, SQUARE_D1, WHITE)
				&& isKingEmptyOrRook(game, SQUARE_C1, WHITE)
				&& !isCastlePathInCheck(game, kingSquare, SQUARE_C1, WHITE)) {
			moves.appendLowPriority(new Move(kingSquare, SQUARE_C1, KING,
					WHITE, EMPTY, Move.LONG_CASTLING_CHARACTERISTIC));
		}

		if (game.getColorToMove() == BLACK
				&& (game.getCastling(game.getColorToMove()) & CASTLE_LONG) != 0
				&& fromBB == kingSquareBB
				&& emptyBetweenFiles(game, 7, initialLongRookFile,
						initialKingFile)
				&& isKingEmptyOrRook(game, SQUARE_D8, BLACK)
				&& isKingEmptyOrRook(game, SQUARE_C8, BLACK)
				&& !isCastlePathInCheck(game, kingSquare, SQUARE_C8, BLACK)) {
			moves.appendLowPriority(new Move(kingSquare, SQUARE_C8, KING,
					BLACK, EMPTY, Move.LONG_CASTLING_CHARACTERISTIC));
		}
	}

	/**
	 * Returns true if any of the squares between king startSquare and
	 * kingEndSquare are in check (including the start/end squares).
	 * kingStartSquare can either be < or > kingEndSquare.
	 */
	public static boolean isCastlePathInCheck(Game game, int kingStartSquare,
			int kingEndSquare, int color) {
		boolean result = false;
		if (kingStartSquare < kingEndSquare) {
			for (int i = kingStartSquare; !result && i < kingEndSquare; i++) {
				result = game.isInCheck(color, getBitboard(i));
			}
		} else {
			for (int i = kingEndSquare; !result && i < kingStartSquare; i++) {
				result = game.isInCheck(color, getBitboard(i));
			}
		}
		return result;
	}

	/**
	 * Returns true if the specified square is either empty or a king or rook of
	 * the specified color.
	 */
	public static boolean isKingEmptyOrRook(ClassicGame game, int square,
			int color) {
		return game.board[square] == EMPTY || game.board[square] == KING
				&& (game.getColorBB(color) & getBitboard(square)) != 0
				|| game.board[square] == ROOK
				&& (game.getColorBB(color) & getBitboard(square)) != 0;
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	public static void makeCastlingMove(ClassicGame game, Move move,
			int initialKingFile, int initialShortRookFile,
			int initialLongRookFile) {

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
				updateZobristCastle(game, WHITE, kingFromSquare,
						rookFromSquare, SQUARE_G1, SQUARE_F1);
			} else {
				rookFromSquare = getSquare(0, initialLongRookFile);
				kingToBB = C1;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = D1;
				updateZobristCastle(game, WHITE, kingFromSquare,
						rookFromSquare, SQUARE_C1, SQUARE_D1);
			}
		} else {
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = getSquare(7, initialShortRookFile);
				kingToBB = G8;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = F8;
				updateZobristCastle(game, BLACK, kingFromSquare,
						rookFromSquare, SQUARE_G8, SQUARE_F8);
			} else {
				rookFromSquare = getSquare(7, initialLongRookFile);
				kingToBB = C8;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = D8;
				updateZobristCastle(game, BLACK, kingFromSquare,
						rookFromSquare, SQUARE_C8, SQUARE_D8);
			}
		}

		if (kingToBB != kingFromBB) {
			long kingFromTo = kingToBB | kingFromBB;
			game.setPiece(bitscanForward(kingToBB), KING);
			game.setPiece(kingFromSquare, EMPTY);
			game.xor(move.getColor(), KING, kingFromTo);
			game.xor(move.getColor(), kingFromTo);
			game.setOccupiedBB(game.getOccupiedBB() ^ kingFromTo);
			game.setEmptyBB(game.getEmptyBB() ^ kingFromTo);
		}

		if (rookFromBB != rookToBB) {
			long rookFromTo = rookToBB | rookFromBB;
			game.setPiece(bitscanForward(rookToBB), ROOK);
			if (rookFromBB != kingToBB) {
				game.setPiece(rookFromSquare, EMPTY);
			}
			game.xor(move.getColor(), ROOK, rookFromTo);

			game.xor(move.getColor(), rookFromTo);
			game.setOccupiedBB(game.getOccupiedBB() ^ rookFromTo);
			game.setEmptyBB(game.getEmptyBB() ^ rookFromTo);
		}

		game.setCastling(game.getColorToMove(), CASTLE_NONE);
		game.setEpSquare(EMPTY_SQUARE);
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	public static void rollbackCastlingMove(ClassicGame game, Move move,
			int initialKingFile, int initialShortRookFile,
			int initialLongRookFile) {
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
				updateZobristCastle(game, WHITE, kingFromSquare,
						rookFromSquare, SQUARE_G1, SQUARE_F1);
			} else {
				rookFromSquare = getSquare(0, initialLongRookFile);
				kingToBB = C1;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = D1;
				updateZobristCastle(game, WHITE, kingFromSquare,
						rookFromSquare, SQUARE_C1, SQUARE_D1);
			}
		} else {
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = getSquare(7, initialShortRookFile);
				kingToBB = G8;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = F8;
				updateZobristCastle(game, BLACK, kingFromSquare,
						rookFromSquare, SQUARE_G8, SQUARE_F8);
			} else {
				rookFromSquare = getSquare(7, initialLongRookFile);
				kingToBB = C8;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = D8;
				updateZobristCastle(game, BLACK, kingFromSquare,
						rookFromSquare, SQUARE_C8, SQUARE_D8);
			}
		}

		if (rookFromBB != rookToBB) {
			long rookFromTo = rookToBB | rookFromBB;
			game.setPiece(bitscanForward(rookToBB), EMPTY);
			game.setPiece(rookFromSquare, ROOK);
			game.xor(move.getColor(), ROOK, rookFromTo);
			game.xor(move.getColor(), rookFromTo);
			game.setOccupiedBB(game.getOccupiedBB() ^ rookFromTo);
			game.setEmptyBB(game.getEmptyBB() ^ rookFromTo);
		}

		if (kingToBB != kingFromBB) {
			long kingFromTo = kingToBB | kingFromBB;
			if (kingToBB != rookFromBB) {
				game.setPiece(bitscanForward(kingToBB), EMPTY);
			}
			game.setPiece(kingFromSquare, KING);
			game.xor(move.getColor(), KING, kingFromTo);
			game.xor(move.getColor(), kingFromTo);
			game.setOccupiedBB(game.getOccupiedBB() ^ kingFromTo);
			game.setEmptyBB(game.getEmptyBB() ^ kingFromTo);
		}

		game.setEpSquareFromPreviousMove();
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	public static void updateCastlingRightsForNonEpNonCastlingMove(
			ClassicGame game, Move move, int initialShortRookFile,
			int initialLongRookFile) {

		int shortRookSquare = game.getColorToMove() == WHITE ? getSquare(0,
				initialShortRookFile) : getSquare(7, initialShortRookFile);
		int longRookSquare = game.getColorToMove() == WHITE ? getSquare(0,
				initialLongRookFile) : getSquare(7, initialLongRookFile);

		switch (move.getPiece()) {
		case KING:
			game.setCastling(game.getColorToMove(), CASTLE_NONE);
			break;
		default:
			if (move.getPiece() == ROOK && move.getFrom() == longRookSquare
					&& game.getColorToMove() == WHITE
					|| move.getCapture() == ROOK
					&& move.getTo() == longRookSquare
					&& game.getColorToMove() == BLACK) {
				game.setCastling(WHITE, game.getCastling(WHITE) & CASTLE_SHORT);
			} else if (move.getPiece() == ROOK
					&& move.getFrom() == shortRookSquare
					&& game.getColorToMove() == WHITE
					|| move.getCapture() == ROOK
					&& move.getTo() == shortRookSquare
					&& game.getColorToMove() == BLACK) {
				game.setCastling(WHITE, game.getCastling(WHITE) & CASTLE_LONG);
			} else if (move.getPiece() == ROOK
					&& move.getFrom() == longRookSquare
					&& game.getColorToMove() == BLACK
					|| move.getCapture() == ROOK
					&& move.getTo() == longRookSquare
					&& game.getColorToMove() == WHITE) {
				game.setCastling(BLACK, game.getCastling(BLACK) & CASTLE_SHORT);
			} else if (move.getPiece() == ROOK
					&& move.getFrom() == shortRookSquare
					&& game.getColorToMove() == BLACK
					|| move.getCapture() == ROOK
					&& move.getTo() == shortRookSquare
					&& game.getColorToMove() == WHITE) {
				game.setCastling(BLACK, game.getCastling(BLACK) & CASTLE_LONG);
			}
			break;
		}
	}

	/**
	 * Updates the zobrist position hash with the specified castling information
	 */
	public static void updateZobristCastle(ClassicGame game, int color,
			int kingStartSquare, int rookStartSquare, int kingEndSquare,
			int rookEndSquare) {
		game.zobristPositionHash ^= zobrist(color, KING, kingStartSquare)
				^ zobrist(color, KING, kingStartSquare)
				^ zobrist(color, ROOK, rookStartSquare)
				^ zobrist(color, ROOK, rookEndSquare);
	}
}
