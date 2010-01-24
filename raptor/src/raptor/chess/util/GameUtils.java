/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.chess.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.util.RaptorStringUtils;

//KoggeStone
//http://www.open-aurec.com/wbforum/viewtopic.php?f=4&t=49948&sid=abd6ee7224f34b11a5211aa167f01ac4
public class GameUtils implements GameConstants {

	private static final long DE_BRUJIN = 0x03f79d71b4cb0a89L;
	private static final int[] DE_BRUJIN_MAGICS_TABLE = { 0, 1, 48, 2, 57, 49,
			28, 3, 61, 58, 50, 42, 38, 29, 17, 4, 62, 55, 59, 36, 53, 51, 43,
			22, 45, 39, 33, 30, 24, 18, 12, 5, 63, 47, 56, 27, 60, 41, 37, 16,
			54, 35, 52, 21, 44, 32, 23, 11, 46, 26, 40, 15, 34, 20, 31, 10, 25,
			14, 19, 9, 13, 8, 7, 6, };

	private static int[] EP_DIR = { SOUTH, NORTH };
	private static int[] EP_OPP_DIR = { NORTH, SOUTH };

	private static long[] KING_ATTACKS = new long[64];
	private static long[] KNIGHT_ATTACKS = new long[64];

	public static final Log LOG = LogFactory.getLog(GameUtils.class);

	private static int[] OPPOSITE_COLOR = { BLACK, WHITE };

	static {
		initKingAttacks();
		initKnightAttacks();
	}

	/**
	 * Clears the next one bit in the bitboard.
	 */
	public static final long bitscanClear(long bitboard) {
		return bitboard & bitboard - 1;
	}

	/**
	 * Returns the next 1 bit in the bitboard. Returns 0 if bitboard is 0.
	 */
	public static final int bitscanForward(long bitboard) {
		// Slower on intel
		// return Long.numberOfTrailingZeros(bitboard);
		return bitScanForwardDeBruijn64(bitboard);
	}

	public static int bitScanForwardDeBruijn64(long b) {
		int idx = (int) ((b & -b) * DE_BRUJIN >>> 58);
		return DE_BRUJIN_MAGICS_TABLE[idx];
	}

	public static final long clearMulti(long bitboard, long squaresToClear) {
		return bitboard & ~squaresToClear;
	}

	public static String convertSanToUseUnicode(String san, boolean isWhitesMove) {
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.APP_IS_SHOWING_CHESS_PIECE_UNICODE_CHARS)) {

			StringBuilder result = new StringBuilder(san.length());
			for (int i = 0; i < san.length(); i++) {
				int piece = PIECE_TO_SAN.indexOf(san.charAt(i));
				switch (piece) {
				case -1:
					result.append(san.charAt(i));
					break;
				case PAWN:
					result.append(isWhitesMove ? "\u2659" : "\u265F");
					break;
				case KNIGHT:
					result.append(isWhitesMove ? "\u2658" : "\u265E");
					break;
				case BISHOP:
					result.append(isWhitesMove ? "\u2657" : "\u265D");
					break;
				case ROOK:
					result.append(isWhitesMove ? "\u2656" : "\u265C");
					break;
				case QUEEN:
					result.append(isWhitesMove ? "\u2655" : "\u265B");
					break;
				case KING:
					result.append(isWhitesMove ? "\u2654" : "\u265A");
					break;
				default:
					throw new IllegalArgumentException(
							"Unknown piece cosntant: " + piece);
				}
			}

			return result.toString();
		} else {
			return san;
		}

	}

	public static final long diagonalMove(int square, long emptySquares,
			long occupied) {
		long seed = getBitboard(square);
		return shiftUpRight(fillUpRightOccluded(seed, emptySquares))
				| shiftUpLeft(fillUpLeftOccluded(seed, emptySquares))
				| shiftDownLeft(fillDownLeftOccluded(seed, emptySquares))
				| shiftDownRight(fillDownRightfccluded(seed, emptySquares));
	}

	public static long fillDownLeftOccluded(long g, long p) {
		p &= 0x7f7f7f7f7f7f7f7fL;
		g |= p & g >>> 9;
		p &= p >>> 9;
		g |= p & g >>> 18;
		p &= p >>> 18;
		return g |= p & g >>> 36;
	}

	public static long fillDownOccluded(long g, long p) {
		g |= p & g >>> 8;
		p &= p >>> 8;
		g |= p & g >>> 16;
		p &= p >>> 16;
		return g |= p & g >>> 32;
	}

	public static long fillDownRightfccluded(long g, long p) {
		p &= 0xfefefefefefefefeL;
		g |= p & g >>> 7;
		p &= p >>> 7;
		g |= p & g >>> 14;
		p &= p >>> 14;
		return g |= p & g >>> 28;
	}

	public static long fillLeftOccluded(long g, long p) {
		p &= 0x7f7f7f7f7f7f7f7fL;
		g |= p & g >>> 1;
		p &= p >>> 1;
		g |= p & g >>> 2;
		p &= p >>> 2;
		return g |= p & g >>> 4;
	}

	/**
	 * The routine fillUpOccluded() smears the set bits of bitboard g upwards,
	 * but only along set bits of p; a reset bit in p is enough to halt a smear.
	 * In the above, g = moving piece(s); p = empty squares.
	 */
	public static long fillRightOccluded(long g, long p) {
		p &= 0xfefefefefefefefeL;
		g |= p & g << 1;
		p &= p << 1;
		g |= p & g << 2;
		p &= p << 2;
		return g |= p & g << 4;
	}

	public static long fillUpLeftOccluded(long g, long p) {
		p &= 0x7f7f7f7f7f7f7f7fL;
		g |= p & g << 7;
		p &= p << 7;
		g |= p & g << 14;
		p &= p << 14;
		return g |= p & g << 28;
	}

	public static long fillUpOccluded(long g, long p) {
		g |= p & g << 8;
		p &= p << 8;
		g |= p & g << 16;
		p &= p << 16;
		return g |= p & g << 32;
	}

	public static long fillUpRightOccluded(long g, long p) {
		p &= 0xfefefefefefefefeL;
		g |= p & g << 9;
		p &= p << 9;
		g |= p & g << 18;
		p &= p << 18;
		return g |= p & g << 36;
	}

	public static final long getBitboard(int square) {
		return SQUARE_TO_COORDINATE[square];
	}

	public static String getChessPieceCharacter(int piece) {
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.APP_IS_SHOWING_CHESS_PIECE_UNICODE_CHARS)) {
			switch (piece) {
			case PAWN:
				return "\u2659";
			case KNIGHT:
				return "\u2658";
			case BISHOP:
				return "\u2657";
			case ROOK:
				return "\u2656";
			case QUEEN:
				return "\u2655";
			case KING:
				return "\u2654";
			case EMPTY:
				return " ";
			default:
				throw new IllegalArgumentException("Unknown piece cosntant: "
						+ piece);
			}
		} else {
			return "" + PIECE_TO_SAN.charAt(piece);
		}

	}

	public static String getChessPieceCharacter(int piece, boolean isWhite) {
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.APP_IS_SHOWING_CHESS_PIECE_UNICODE_CHARS)) {
			switch (piece) {
			case PAWN:
				return isWhite ? "\u2659" : "\u265F";
			case KNIGHT:
				return isWhite ? "\u2658" : "\u265E";
			case BISHOP:
				return isWhite ? "\u2657" : "\u265D";
			case ROOK:
				return isWhite ? "\u2656" : "\u265C";
			case QUEEN:
				return isWhite ? "\u2655" : "\u265B";
			case KING:
				return isWhite ? "\u2654" : "\u265A";
			case EMPTY:
				return " ";
			default:
				throw new IllegalArgumentException("Unknown piece cosntant: "
						+ piece);
			}
		} else {
			return isWhite ? ("" + PIECE_TO_SAN.charAt(piece)).toLowerCase()
					: "" + PIECE_TO_SAN.charAt(piece);
		}

	}

	public static int getColoredPiece(int square, Game game) {
		long squareBB = GameUtils.getBitboard(square);
		int gamePiece = game.getPiece(square);

		switch (gamePiece) {
		case GameConstants.EMPTY:
			return EMPTY;
		case WP:
		case BP:
			return (game.getColorBB(GameConstants.WHITE) & squareBB) == 0 ? BP
					: WP;
		case WN:
		case BN:
			return (game.getColorBB(GameConstants.WHITE) & squareBB) == 0 ? BN
					: WN;
		case WB:
		case BB:
			return (game.getColorBB(GameConstants.WHITE) & squareBB) == 0 ? BB
					: WB;
		case WR:
		case BR:
			return (game.getColorBB(GameConstants.WHITE) & squareBB) == 0 ? BR
					: WR;
		case WQ:
		case BQ:
			return (game.getColorBB(GameConstants.WHITE) & squareBB) == 0 ? BQ
					: WQ;
		case WK:
		case BK:
			return (game.getColorBB(GameConstants.WHITE) & squareBB) == 0 ? BK
					: WK;
		default:
			throw new IllegalArgumentException("Invalid gamePiece" + gamePiece);

		}
	}

	public static int getColoredPiece(int uncoloredPiece, int color) {
		switch (uncoloredPiece) {
		case EMPTY:
			return EMPTY;
		case PAWN:
			return color == WHITE ? WP : BP;
		case KNIGHT:
			return color == WHITE ? WN : BN;
		case BISHOP:
			return color == WHITE ? WB : BB;
		case ROOK:
			return color == WHITE ? WR : BR;
		case QUEEN:
			return color == WHITE ? WQ : BQ;
		case KING:
			return color == WHITE ? WK : BK;
		default:
			throw new IllegalArgumentException("Invalid uncolored piece: "
					+ uncoloredPiece);

		}
	}

	public static int getColoredPieceFromDropSquare(int dropSquare) {
		return dropSquare - 100;
	}

	public static int getDropSquareFromColoredPiece(int coloredPiece) {
		return coloredPiece + 100;
	}

	public static int getFile(int square) {
		return square % 8;
	}

	public static final int getOppositeColor(int color) {
		return OPPOSITE_COLOR[color];
	}

	/**
	 * This method will return the unicode piece constants if the property is
	 * set.
	 */
	public static String getPieceRepresentation(int coloredPiece) {
		return getPieceRepresentation(coloredPiece, true);
	}

	public static String getPieceRepresentation(int coloredPiece,
			boolean useUnicode) {
		if (useUnicode
				&& Raptor.getInstance().getPreferences().getBoolean(
						PreferenceKeys.APP_IS_SHOWING_CHESS_PIECE_UNICODE_CHARS)) {
			switch (coloredPiece) {
			case WK:
				return "\u2654";
			case WQ:
				return "\u2655";
			case WR:
				return "\u2656";
			case WB:
				return "\u2657";
			case WN:
				return "\u2658";
			case WP:
				return "\u2659";
			case BK:
				return "\u265A";
			case BQ:
				return "\u265B";
			case BR:
				return "\u265C";
			case BB:
				return "\u265D";
			case BN:
				return "\u265E";
			case BP:
				return "\u265F";
			case EMPTY:
				return "EMPTY";
			}
		} else {
			switch (coloredPiece) {
			case WK:
				return "K";
			case WQ:
				return "Q";
			case WR:
				return "R";
			case WB:
				return "B";
			case WN:
				return "N";
			case WP:
				return "P";
			case BK:
				return "K";
			case BQ:
				return "Q";
			case BR:
				return "R";
			case BB:
				return "B";
			case BN:
				return "N";
			case BP:
				return "P";
			case EMPTY:
				return "EMPTY";
			}
		}

		throw new IllegalArgumentException("Invalid piece: " + coloredPiece);
	}

	/**
	 * Returns a fake SAN (short algebraic notation) version of the move. The
	 * SAN does not reflect ambiguity. Handles drop squares as from squares.
	 * 
	 * This version will use unicode values for the pieces if the preference is
	 * set.
	 */
	public static String getPseudoSan(Game game, int fromSquare, int toSquare) {
		return getPseudoSan(game, fromSquare, toSquare, true);
	}

	/**
	 * Returns a fake SAN (short algebraic notation) version of the move. The
	 * SAN does not reflect ambiguity. Handles drop squares as from squares.
	 */
	public static String getPseudoSan(Game game, int fromSquare, int toSquare,
			boolean useUnicodePieces) {
		boolean isDrop = isDropSquare(fromSquare);
		boolean isToPieceEmpty = GameUtils.isDropSquare(toSquare) ? true : game
				.getPiece(toSquare) == EMPTY;

		int fromPiece = -1;

		if (isDrop) {
			fromPiece = getColoredPieceFromDropSquare(fromSquare);
		} else {
			fromPiece = getColoredPiece(fromSquare, game);
		}

		if (fromPiece == WK && fromSquare == SQUARE_E1 && toSquare == SQUARE_G1
				|| fromPiece == BK && fromSquare == SQUARE_E8
				&& toSquare == SQUARE_G8) {
			return "O-O";
		} else if (fromPiece == WK && fromSquare == SQUARE_E1
				&& toSquare == SQUARE_C1 || fromPiece == BK
				&& fromSquare == SQUARE_E8 && toSquare == SQUARE_C8) {
			return "O-O-O";
		} else if (isDrop) {
			return getPieceRepresentation(fromPiece, useUnicodePieces) + "@"
					+ GameUtils.getSan(toSquare);
		} else if ((fromPiece == WP || fromPiece == BP) && isToPieceEmpty) {
			return GameUtils.getSan(toSquare);
		} else {
			return getPieceRepresentation(fromPiece, useUnicodePieces)
					+ (isToPieceEmpty ? "" : "x") + GameUtils.getSan(toSquare);
		}
	}

	/**
	 * Returns a fake SAN (short algebraic notation) version of the move. The
	 * SAN does not reflect ambiguity. Handles drop squares as from squares.
	 */
	public static String getPseudoSan(int fromPiece, int toPiece,
			int fromSquare, int toSquare) {
		boolean isDrop = isDropSquare(fromSquare);
		boolean isToPieceEmpty = toPiece == EMPTY;

		if (fromPiece == WK && fromSquare == SQUARE_E1 && toSquare == SQUARE_G1
				|| fromPiece == BK && fromSquare == SQUARE_E8
				&& toSquare == SQUARE_G8) {
			return "O-O";
		} else if (fromPiece == WK && fromSquare == SQUARE_E1
				&& toSquare == SQUARE_C1 || fromPiece == BK
				&& fromSquare == SQUARE_E8 && toSquare == SQUARE_C8) {
			return "O-O-O";
		} else if (isDrop) {
			return getPieceRepresentation(fromPiece) + "@"
					+ GameUtils.getSan(toSquare);
		} else if ((fromPiece == WP || fromPiece == BP) && isToPieceEmpty) {
			return GameUtils.getSan(toSquare);
		} else {
			return getPieceRepresentation(fromPiece)
					+ (isToPieceEmpty ? "" : "x") + GameUtils.getSan(toSquare);
		}
	}

	public static int getRank(int square) {
		return square / 8;
	}

	/**
	 * Returns the SAN,short algebraic notation for the square. If square is a
	 * DROP square returns a constant suitable for debugging.
	 * 
	 * @param square
	 *            The square.
	 */
	public static final String getSan(int square) {
		if (square == 64) {
			return "-";
		} else if (square < 100) {
			return "" + FILE_FROM_SAN.charAt(square % 8)
					+ RANK_FROM_SAN.charAt(square / 8);
		} else {
			switch (square) {
			case WN_DROP_FROM_SQUARE:
				return "WN_DROP";
			case WP_DROP_FROM_SQUARE:
				return "WP_DROP";
			case WB_DROP_FROM_SQUARE:
				return "WB_DROP";
			case WR_DROP_FROM_SQUARE:
				return "WR_DROP";
			case WQ_DROP_FROM_SQUARE:
				return "WQ_DROP";
			case WK_DROP_FROM_SQUARE:
				return "WK_DROP";
			case BN_DROP_FROM_SQUARE:
				return "BN_DROP";
			case BP_DROP_FROM_SQUARE:
				return "BP_DROP";
			case BB_DROP_FROM_SQUARE:
				return "BB_DROP";
			case BR_DROP_FROM_SQUARE:
				return "BR_DROP";
			case BQ_DROP_FROM_SQUARE:
				return "BQ_DROP";
			case BK_DROP_FROM_SQUARE:
				return "BK_DROP";
			default:
				throw new IllegalArgumentException("Invalid square " + square);

			}
		}
	}

	/**
	 * Returns the square given a 0 based rank and file.
	 */
	public static final int getSquare(int rank, int file) {
		return rank * 8 + file;
	}

	/**
	 * Returns the square representing the bit board.
	 */
	public static int getSquare(long bitboard) {
		return bitscanForward(bitboard);
	}

	public static final int getSquare(String san) {
		return getSquare(RANK_FROM_SAN.indexOf(san.charAt(1)), FILE_FROM_SAN
				.indexOf(san.charAt(0)));
	}

	public static final String getString(long board) {
		StringBuilder result = new StringBuilder(200);

		for (int i = 7; i > -1; i--) {
			result.append(" ");
			for (int j = 0; j < 8; j++) {
				result
						.append(((board & SQUARE_TO_COORDINATE[getSquare(i, j)]) == 0 ? 0
								: 1)
								+ " ");
			}

			if (i != 0) {
				result.append("\n");
			}
		}

		return result.toString();
	}

	public static final String getString(String[] labels, long[] bitBoards) {
		StringBuilder result = new StringBuilder(200 * bitBoards.length);

		for (int i = 0; i < labels.length; i++) {
			result.append(" ");

			if (labels[i].length() > 18) {
				labels[i] = labels[i].substring(0, 18);
			}
			int spaces = 18 - labels[i].length();
			result.append(labels[i] + SPACES.substring(0, spaces));
		}
		result.append("\n");

		for (int i = 7; i > -1; i--) {
			for (long bitBoard : bitBoards) {
				result.append(" ");
				for (int j = 0; j < 8; j++) {
					result.append(((bitBoard & SQUARE_TO_COORDINATE[getSquare(
							i, j)]) == 0 ? 0 : 1)
							+ " ");
				}
				result.append("  ");
			}

			if (i != 0) {
				result.append("\n");
			}
		}

		return result.toString();
	}

	public static int getUncoloredPiece(int coloredPiece) {
		switch (coloredPiece) {
		case WK:
		case BK:
			return KING;
		case WQ:
		case BQ:
			return QUEEN;
		case WR:
		case BR:
			return ROOK;
		case WB:
		case BB:
			return BISHOP;
		case WN:
		case BN:
			return KNIGHT;
		case WP:
		case BP:
			return PAWN;
		case EMPTY:
			return EMPTY;
		default:
			throw new IllegalArgumentException("Invalid colored piece:"
					+ coloredPiece);
		}
	}

	public static boolean isBlackPiece(Game game, int square) {
		return (game.getColorBB(BLACK) & getBitboard(square)) != 0;
	}

	public static boolean isBlackPiece(int coloredPiece) {
		return coloredPiece > 6 && coloredPiece < 13 || coloredPiece > 106
				&& coloredPiece < 113;
	}

	public static boolean isDropSquare(int square) {
		return square >= 100 && square <= 112;
	}

	public static final boolean isInBounds(int rank, int file) {
		return rank >= 0 && rank <= 7 && file >= 0 && file <= 7;
	}

	public static final boolean isOnEdge(int zeroBasedRank, int zeroBasedFile) {
		return zeroBasedRank == 0 || zeroBasedRank == 7 || zeroBasedFile == 0
				|| zeroBasedFile == 7;
	}

	public static boolean isPromotion(boolean isWhiteToMove, Game game,
			int fromSquare, int toSquare) {
		if (isDropSquare(fromSquare)) {
			return false;
		} else if (isWhiteToMove) {
			return game.getPiece(fromSquare) == PAWN && getRank(toSquare) == 7;
		} else {
			return game.getPiece(fromSquare) == PAWN && getRank(toSquare) == 0;
		}
	}

	public static boolean isPromotion(Game game, int fromSquare, int toSquare) {
		return isPromotion(game.getColorToMove() == WHITE, game, fromSquare,
				toSquare);
	}

	public static boolean isWhitePiece(Game game, int square) {
		return (game.getColorBB(WHITE) & getBitboard(square)) != 0;
	}

	public static boolean isWhitePiece(int coloredPiece) {
		return coloredPiece > 0 && coloredPiece < 7 || coloredPiece > 100
				&& coloredPiece < 107;
	}

	public static final long kingMove(int square) {
		return KING_ATTACKS[square];
	}

	public static final long knightMove(int square) {
		return KNIGHT_ATTACKS[square];
	}

	public static final long moveOne(int direction, long bitboard) {
		switch (direction) {
		case NORTH:
			return bitboard << 8;
		case NORTHEAST:
			return bitboard << 9;
		case NORTHWEST:
			return bitboard << 7;
		case SOUTH:
			return bitboard >>> 8;
		case SOUTHEAST:
			return bitboard >>> 7;
		case SOUTHWEST:
			return bitboard >>> 9;
		case EAST:
			return bitboard << 1;
		case WEST:
			return bitboard >>> 1;
		default:
			throw new IllegalArgumentException("Unknown direction: "
					+ direction);
		}
	}

	public static long northOne(long bitboard) {
		return bitboard << 8;
	}

	public static final long orthogonalMove(int square, long emptySquares,
			long occupied) {
		long seed = getBitboard(square);
		return shiftRight(fillRightOccluded(seed, emptySquares))
				| shiftLeft(fillLeftOccluded(seed, emptySquares))
				| shiftUp(fillUpOccluded(seed, emptySquares))
				| shiftDown(fillDownOccluded(seed, emptySquares));
	}

	public static final long pawnCapture(int colorToMove, long toMovePawns,
			long enemyPieces) {
		return colorToMove == WHITE ? ((toMovePawns & NOT_AFILE) << 7 | (toMovePawns & NOT_HFILE) << 9)
				& enemyPieces
				: ((toMovePawns & NOT_HFILE) >> 7 | (toMovePawns & NOT_AFILE) >>> 9)
						& enemyPieces;
	}

	public static final long pawnDoublePush(int colorToMove, long toMovePawns,
			long empty) {
		int direction = colorToMove == WHITE ? NORTH : SOUTH;
		long rankBB = colorToMove == WHITE ? RANK4 : RANK5;

		return moveOne(direction, moveOne(direction, toMovePawns) & empty)
				& empty & rankBB;
	}

	public static final long pawnEpCapture(int colorToMove, long toMovePawns,
			long enemyPawns, long epSquare) {
		enemyPawns &= moveOne(EP_DIR[colorToMove], epSquare);
		enemyPawns = moveOne(EP_OPP_DIR[colorToMove], enemyPawns);
		return pawnCapture(colorToMove, toMovePawns, enemyPawns);
	}

	public static final long pawnSinglePush(int colorToMove, long toMovePawns,
			long empty) {
		int direction = colorToMove == WHITE ? NORTH : SOUTH;
		return moveOne(direction, toMovePawns) & empty;
	}

	public static final int populationCount(long bitboard) {
		return Long.bitCount(bitboard);
	}

	public static long shiftDown(long b) {
		return b >>> 8;
	}

	public static long shiftDownLeft(long b) {
		return b >>> 9 & 0x7f7f7f7f7f7f7f7fL;
	}

	public static long shiftDownRight(long b) {
		return b >>> 7 & 0xfefefefefefefefeL;
	}

	public static long shiftLeft(long b) {
		return b >>> 1 & 0x7f7f7f7f7f7f7f7fL;
	}

	// KoggeStone algorithm
	public static long shiftRight(long b) {
		return b << 1 & 0xfefefefefefefefeL;
	}

	public static long shiftUp(long b) {
		return b << 8;
	}

	public static long shiftUpLeft(long b) {
		return b << 7 & 0x7f7f7f7f7f7f7f7fL;
	}

	public static long shiftUpRight(long b) {
		return b << 9 & 0xfefefefefefefefeL;
	}

	public static long southOne(long bitboard) {
		return bitboard >>> 8;
	}

	public static final int sparsePopulationCount(long bitboard) {
		// Faster on sparser BBs <= 8 pieces.
		int result = 0;
		while (bitboard != 0) {
			result++;
			bitboard &= bitboard - 1; // reset LS1B
		}
		return result;
	}

	public static String timeToString(long timeMillis, boolean allowFlash) {

		long timeLeft = timeMillis;

		boolean isFlashing = false;

		if (timeLeft < 0) {
			isFlashing = timeLeft / 1000 % 2 == 0;
			timeLeft = 0;
		}
		RaptorPreferenceStore prefs = Raptor.getInstance().getPreferences();

		if (isFlashing && allowFlash) {
			// Flashes when time has expired.
			if (timeLeft < prefs
					.getLong(PreferenceKeys.BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN)) {
				return "  :  . ";
			} else {
				return "  :  ";
			}
		} else if (timeLeft <= 0) {
			if (timeLeft < prefs
					.getLong(PreferenceKeys.BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN)) {
				return "00:00.0";
			} else {
				return "00:00";
			}
		} else {

			if (timeLeft >= prefs
					.getLong(PreferenceKeys.BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN)) {
				int hour = (int) (timeLeft / (60000L * 60));
				timeLeft -= hour * 60000L * 60;
				int minute = (int) (timeLeft / 60000L);
				timeLeft -= minute * 60 * 1000;

				if (allowFlash) {
					return RaptorStringUtils.defaultTimeString(hour, 2)
							+ (timeMillis / 1000 % 2 == 0 ? ":" : " ") // Adds
							// the
							// blinking :
							+ RaptorStringUtils.defaultTimeString(minute, 2);
				} else {
					return RaptorStringUtils.defaultTimeString(hour, 2) + ":"
							+ RaptorStringUtils.defaultTimeString(minute, 2);
				}

			} else if (timeLeft >= prefs
					.getLong(PreferenceKeys.BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN)) {
				int hour = (int) (timeLeft / (60000L * 60));
				timeLeft -= hour * 60 * 1000 * 60;
				int minute = (int) (timeLeft / 60000L);
				timeLeft -= minute * 60 * 1000;
				int seconds = (int) (timeLeft / 1000L);
				return RaptorStringUtils.defaultTimeString(minute, 2) + ":"
						+ RaptorStringUtils.defaultTimeString(seconds, 2);

			} else {
				int hour = (int) (timeLeft / (60000L * 60));
				timeLeft -= hour * 60 * 1000 * 60;
				int minute = (int) (timeLeft / 60000L);
				timeLeft -= minute * 60 * 1000;
				int seconds = (int) (timeLeft / 1000L);
				timeLeft -= seconds * 1000;
				int tenths = (int) (timeLeft / 100L);
				return RaptorStringUtils.defaultTimeString(minute, 2) + ":"
						+ RaptorStringUtils.defaultTimeString(seconds, 2) + "."
						+ RaptorStringUtils.defaultTimeString(tenths, 1);
			}
		}
	}

	public static long whitePawnCaptureEast(long whitePawns, long empty) {
		return (whitePawns & 9187201950435737471L) << 9 & empty;
	}

	public static long whitePawnCaptureWest(long whitePawns, long empty) {
		return (whitePawns & -72340172838076674L) << 7 & empty;
	}

	public static long whitePawnLegalDoublePush(long whitePawns, long empty) {
		long rank4 = 0x00000000FF000000L;
		long singlePush = whiteSinglePushTargets(whitePawns, empty);
		return northOne(singlePush) & empty & rank4;
	}

	public static long whiteSinglePushTargets(long whitePawns, long empty) {
		return northOne(whitePawns) & empty;
	}

	private static final void initKingAttacks() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				long bitMap = 0L;
				if (isInBounds(i, j + 1)) {
					bitMap |= getBitboard(getSquare(i, j + 1));
				}
				if (isInBounds(i, j - 1)) {
					bitMap |= getBitboard(getSquare(i, j - 1));
				}

				if (isInBounds(i + 1, j)) {
					bitMap |= getBitboard(getSquare(i + 1, j));
				}
				if (isInBounds(i + 1, j + 1)) {
					bitMap |= getBitboard(getSquare(i + 1, j + 1));
				}
				if (isInBounds(i + 1, j - 1)) {
					bitMap |= getBitboard(getSquare(i + 1, j - 1));
				}

				if (isInBounds(i - 1, j)) {
					bitMap |= getBitboard(getSquare(i - 1, j));
				}
				if (isInBounds(i - 1, j + 1)) {
					bitMap |= getBitboard(getSquare(i - 1, j + 1));
				}
				if (isInBounds(i - 1, j - 1)) {
					bitMap |= getBitboard(getSquare(i - 1, j - 1));
				}

				KING_ATTACKS[getSquare(i, j)] = bitMap;
			}
		}
	}

	private static final void initKnightAttacks() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				long bitMap = 0L;
				if (isInBounds(i + 2, j + 1)) {
					bitMap |= getBitboard(getSquare(i + 2, j + 1));
				}
				if (isInBounds(i + 2, j - 1)) {
					bitMap |= getBitboard(getSquare(i + 2, j - 1));
				}

				if (isInBounds(i - 2, j + 1)) {
					bitMap |= getBitboard(getSquare(i - 2, j + 1));
				}
				if (isInBounds(i - 2, j - 1)) {
					bitMap |= getBitboard(getSquare(i - 2, j - 1));
				}

				if (isInBounds(i + 1, j + 2)) {
					bitMap |= getBitboard(getSquare(i + 1, j + 2));
				}
				if (isInBounds(i + 1, j - 2)) {
					bitMap |= getBitboard(getSquare(i + 1, j - 2));
				}

				if (isInBounds(i - 1, j + 2)) {
					bitMap |= getBitboard(getSquare(i - 1, j + 2));
				}
				if (isInBounds(i - 1, j - 2)) {
					bitMap |= getBitboard(getSquare(i - 1, j - 2));
				}

				KNIGHT_ATTACKS[getSquare(i, j)] = bitMap;
			}
		}
	}

}
