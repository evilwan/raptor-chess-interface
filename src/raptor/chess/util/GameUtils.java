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
package raptor.chess.util;

import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.chess.AtomicGame;
import raptor.chess.BughouseGame;
import raptor.chess.CrazyhouseGame;
import raptor.chess.FischerRandomGame;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.LosersGame;
import raptor.chess.Result;
import raptor.chess.SuicideGame;
import raptor.chess.Game.Type;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.pgn.PgnUtils;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.util.RaptorStringUtils;

//KoggeStone
//http://www.open-aurec.com/wbforum/viewtopic.php?f=4&t=49948&sid=abd6ee7224f34b11a5211aa167f01ac4
public class GameUtils implements GameConstants {
	public static final Log LOG = LogFactory.getLog(GameUtils.class);

	private static final long DE_BRUJIN = 0x03f79d71b4cb0a89L;
	private static final int[] DE_BRUJIN_MAGICS_TABLE = { 0, 1, 48, 2, 57, 49,
			28, 3, 61, 58, 50, 42, 38, 29, 17, 4, 62, 55, 59, 36, 53, 51, 43,
			22, 45, 39, 33, 30, 24, 18, 12, 5, 63, 47, 56, 27, 60, 41, 37, 16,
			54, 35, 52, 21, 44, 32, 23, 11, 46, 26, 40, 15, 34, 20, 31, 10, 25,
			14, 19, 9, 13, 8, 7, 6, };

	private static long[] KING_ATTACKS = new long[64];
	private static long[] KNIGHT_ATTACKS = new long[64];

	private static int[] EP_DIR = { SOUTH, NORTH };
	private static int[] EP_OPP_DIR = { NORTH, SOUTH };

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
				PreferenceKeys.BOARD_IS_SHOWING_PIECE_UNICODE_CHARS)) {

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

	/**
	 * Creates a game from fen of the specified type.
	 * 
	 * <pre>
	 * rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
	 * </pre>
	 * 
	 * @param fen
	 *            The FEN (Forsyth Edwards Notation)
	 * @param gameType
	 *            The game type.
	 * @return The game.
	 */
	public static final Game createFromFen(String fen, Type gameType) {
		Game result = null;

		switch (gameType) {
		case CLASSIC:
		case WILD:
			result = new Game();
			break;
		case LOSERS:
			result = new LosersGame();
			break;
		case ATOMIC:
			result = new AtomicGame();
			break;
		case SUICIDE:
			result = new SuicideGame();
			break;
		case FISCHER_RANDOM:
			result = new FischerRandomGame();
			break;
		case BUGHOUSE:
			result = new BughouseGame();
			break;
		case CRAZYHOUSE:
			result = new CrazyhouseGame();
			break;
		default:
			throw new IllegalArgumentException("Type " + gameType
					+ " is not supported");
		}

		StringTokenizer tok = new StringTokenizer(fen, " ");
		String boardStr = tok.nextToken();
		String toMoveStr = tok.nextToken();
		String castlingInfoStr = tok.nextToken();
		String epSquareStr = tok.nextToken();
		String fiftyMoveRuleCountStr = tok.nextToken();
		String fullMoveCountStr = tok.nextToken();

		int boardIndex = 56;
		for (int i = 0; i < boardStr.length(); i++) {
			char piece = fen.charAt(i);
			if (piece == '/') {
				boardIndex -= 16;
			} else if (Character.isDigit(piece)) {
				boardIndex += Integer.parseInt("" + piece);
			} else {
				int pieceColor = Character.isUpperCase(piece) ? WHITE : BLACK;
				int pieceInt = PIECE_TO_SAN.indexOf(new String(
						new char[] { piece }).toUpperCase().charAt(0));
				long pieceSquare = GameUtils.getBitboard(boardIndex);

				result.setPieceCount(pieceColor, pieceInt, result
						.getPieceCount(pieceColor, pieceInt) + 1);
				result.getBoard()[boardIndex] = pieceInt;
				result.setColorBB(pieceColor, result.getColorBB(pieceColor)
						| pieceSquare);
				result.setOccupiedBB(result.getOccupiedBB() | pieceSquare);
				result.setPieceBB(pieceColor, pieceInt, result.getPieceBB(
						pieceColor, pieceInt)
						| pieceSquare);
				boardIndex++;
			}
		}

		result.setColorToMove(toMoveStr.equals("w") ? WHITE : BLACK);

		boolean whiteCastleKSide = castlingInfoStr.indexOf('K') != -1;
		boolean whiteCastleQSide = castlingInfoStr.indexOf('Q') != -1;
		boolean blackCastleKSide = castlingInfoStr.indexOf('k') != -1;
		boolean blackCastleQSide = castlingInfoStr.indexOf('q') != -1;

		result.setCastling(WHITE,
				whiteCastleKSide && whiteCastleQSide ? CASTLE_BOTH
						: whiteCastleKSide ? CASTLE_SHORT
								: whiteCastleQSide ? CASTLE_LONG : CASTLE_NONE);
		result.setCastling(BLACK,
				blackCastleKSide && blackCastleQSide ? CASTLE_BOTH
						: blackCastleKSide ? CASTLE_SHORT
								: blackCastleQSide ? CASTLE_LONG : CASTLE_NONE);

		if (!epSquareStr.equals("-")) {
			result.setEpSquare(GameUtils.getSquare(epSquareStr));
			result.setInitialEpSquare(result.getEpSquare());
		} else {
			result.setEpSquare(EMPTY_SQUARE);
			result.setInitialEpSquare(EMPTY_SQUARE);
		}

		if (!fiftyMoveRuleCountStr.equals("-")) {
			result.setFiftyMoveCount(Integer.parseInt(fiftyMoveRuleCountStr));
		}

		if (!fullMoveCountStr.equals("-")) {
			int fullMoveCount = Integer.parseInt(fullMoveCountStr);
			result
					.setHalfMoveCount(result.getColorToMove() == BLACK ? fullMoveCount * 2 - 1
							: fullMoveCount * 2 - 2);
		}

		result.setEmptyBB(~result.getOccupiedBB());
		result.setNotColorToMoveBB(~result.getColorBB(result.getColorToMove()));

		if (!result.isLegalPosition()) {
			throw new IllegalArgumentException(
					"Resulting position was illegal for FEN: " + fen + " "
							+ gameType);
		}

		result.setZobristPositionHash(ZobristHash
				.zobristHashPositionOnly(result));
		result.setZobristGameHash(result.getZobristPositionHash()
				^ ZobristHash.zobrist(result.getColorToMove(), result
						.getEpSquare(), result.getCastling(WHITE), result
						.getCastling(BLACK)));

		result.incrementRepCount();

		if (gameType == Game.Type.CRAZYHOUSE) {
			// This wont work if setup from a FEN where promotions have
			// occurred.
			// There is no way of telling if a piece was promoted or not.

			result.setDropCount(WHITE, PAWN, 8 - result.getPieceCount(BLACK,
					PAWN));
			result.setDropCount(WHITE, KNIGHT, 2 - result.getPieceCount(BLACK,
					KNIGHT));
			result.setDropCount(WHITE, BISHOP, 2 - result.getPieceCount(BLACK,
					BISHOP));
			result.setDropCount(WHITE, ROOK, 2 - result.getPieceCount(BLACK,
					ROOK));
			result.setDropCount(WHITE, QUEEN, 1 - result.getPieceCount(BLACK,
					QUEEN));
			result.setDropCount(WHITE, KING, 0);

			result.setDropCount(BLACK, PAWN, 8 - result.getPieceCount(WHITE,
					PAWN));
			result.setDropCount(BLACK, KNIGHT, 2 - result.getPieceCount(WHITE,
					KNIGHT));
			result.setDropCount(BLACK, BISHOP, 2 - result.getPieceCount(WHITE,
					BISHOP));
			result.setDropCount(BLACK, ROOK, 2 - result.getPieceCount(WHITE,
					ROOK));
			result.setDropCount(BLACK, QUEEN, 1 - result.getPieceCount(WHITE,
					QUEEN));
			result.setDropCount(BLACK, KING, 0);

			// If there are any negative values just set them to 0.
			for (int i = 0; i < result.getPositionState().dropCounts.length; i++) {
				for (int j = 0; j < result.getPositionState().dropCounts[i].length; j++) {

					if (result.getPositionState().dropCounts[i][j] < 0) {
						result.setDropCount(i, j, 0);
						if (LOG.isWarnEnabled()) {
							LOG
									.warn("Set a zh drop value to 0 because it was less than 0 initially. "
											+ fen);
						}
					}
				}
			}
		}
		return result;
	}

	public static final Game createStartingPosition(Type gameType) {
		if (gameType == Type.SUICIDE) {
			return createFromFen(STARTING_SUICIDE_POSITION_FEN, gameType);
		} else {
			return createFromFen(STARTING_POSITION_FEN, gameType);
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

	private static int getIndex(PgnHeader[] headers, String header) {
		int result = -1;
		for (int i = 0; i < headers.length; i++) {
			if (headers[i].getName().equals(header)) {
				result = i;
				break;
			}
		}
		return result;
	}

	public static final int getOppositeColor(int color) {
		return OPPOSITE_COLOR[color];
	}

	public static String getPieceRepresentation(int coloredPiece) {
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.BOARD_IS_SHOWING_PIECE_UNICODE_CHARS)) {
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
			}
		} else {
			switch (coloredPiece) {
			case WK:
				return "N";
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
				return "k";
			case BQ:
				return "q";
			case BR:
				return "r";
			case BB:
				return "b";
			case BN:
				return "n";
			case BP:
				return "p";
			}
		}

		throw new IllegalArgumentException("Invalid piece: " + coloredPiece);
	}

	/**
	 * Returns a fake SAN (short algebraic notation) version of the move. The
	 * SAN does not reflect ambiguity. Handles drop squares as from squares.
	 */
	public static String getPseudoSan(Game game, int fromSquare, int toSquare) {
		boolean isDrop = isDropSquare(fromSquare);
		boolean isToPieceEmpty = game.getPiece(toSquare) == EMPTY;

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
			return getPieceRepresentation(fromPiece) + "@"
					+ GameUtils.getSan(toSquare);
		} else if ((fromPiece == WP || fromPiece == BP) && isToPieceEmpty) {
			return GameUtils.getSan(toSquare);
		} else {
			return getPieceRepresentation(fromPiece)
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

	public static final int getSquare(String san) {
		return rankFileToSquare(RANK_FROM_SAN.indexOf(san.charAt(1)),
				FILE_FROM_SAN.indexOf(san.charAt(0)));
	}

	public static final String getString(long board) {
		StringBuilder result = new StringBuilder(200);

		for (int i = 7; i > -1; i--) {
			result.append(" ");
			for (int j = 0; j < 8; j++) {
				result.append(((board & SQUARE_TO_COORDINATE[rankFileToSquare(
						i, j)]) == 0 ? 0 : 1)
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
					result
							.append(((bitBoard & SQUARE_TO_COORDINATE[rankFileToSquare(
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

	private static final void initKingAttacks() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				long bitMap = 0L;
				if (isInBounds(i, j + 1)) {
					bitMap |= getBitboard(rankFileToSquare(i, j + 1));
				}
				if (isInBounds(i, j - 1)) {
					bitMap |= getBitboard(rankFileToSquare(i, j - 1));
				}

				if (isInBounds(i + 1, j)) {
					bitMap |= getBitboard(rankFileToSquare(i + 1, j));
				}
				if (isInBounds(i + 1, j + 1)) {
					bitMap |= getBitboard(rankFileToSquare(i + 1, j + 1));
				}
				if (isInBounds(i + 1, j - 1)) {
					bitMap |= getBitboard(rankFileToSquare(i + 1, j - 1));
				}

				if (isInBounds(i - 1, j)) {
					bitMap |= getBitboard(rankFileToSquare(i - 1, j));
				}
				if (isInBounds(i - 1, j + 1)) {
					bitMap |= getBitboard(rankFileToSquare(i - 1, j + 1));
				}
				if (isInBounds(i - 1, j - 1)) {
					bitMap |= getBitboard(rankFileToSquare(i - 1, j - 1));
				}

				KING_ATTACKS[rankFileToSquare(i, j)] = bitMap;
			}
		}
	}

	private static final void initKnightAttacks() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				long bitMap = 0L;
				if (isInBounds(i + 2, j + 1)) {
					bitMap |= getBitboard(rankFileToSquare(i + 2, j + 1));
				}
				if (isInBounds(i + 2, j - 1)) {
					bitMap |= getBitboard(rankFileToSquare(i + 2, j - 1));
				}

				if (isInBounds(i - 2, j + 1)) {
					bitMap |= getBitboard(rankFileToSquare(i - 2, j + 1));
				}
				if (isInBounds(i - 2, j - 1)) {
					bitMap |= getBitboard(rankFileToSquare(i - 2, j - 1));
				}

				if (isInBounds(i + 1, j + 2)) {
					bitMap |= getBitboard(rankFileToSquare(i + 1, j + 2));
				}
				if (isInBounds(i + 1, j - 2)) {
					bitMap |= getBitboard(rankFileToSquare(i + 1, j - 2));
				}

				if (isInBounds(i - 1, j + 2)) {
					bitMap |= getBitboard(rankFileToSquare(i - 1, j + 2));
				}
				if (isInBounds(i - 1, j - 2)) {
					bitMap |= getBitboard(rankFileToSquare(i - 1, j - 2));
				}

				KNIGHT_ATTACKS[rankFileToSquare(i, j)] = bitMap;
			}
		}
	}

	public static boolean isBlackPiece(Game game, int square) {
		return (game.getColorBB(BLACK) & getBitboard(square)) != 0;
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

	/**
	 * Returns the square given a 0 based rank and file.
	 */
	public static final int rankFileToSquare(int rank, int file) {
		return rank * 8 + file;
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

	public static String timeToString(long timeMillis) {

		long timeLeft = timeMillis;

		if (timeLeft < 0) {
			timeLeft = 0;
		}

		RaptorPreferenceStore prefs = Raptor.getInstance().getPreferences();

		if (timeLeft >= prefs
				.getLong(PreferenceKeys.BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN)) {
			int hour = (int) (timeLeft / (60000L * 60));
			timeLeft -= hour * 60 * 1000 * 60;
			int minute = (int) (timeLeft / 60000L);
			return RaptorStringUtils.defaultTimeString(hour, 2) + ":"
					+ RaptorStringUtils.defaultTimeString(minute, 2);

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

	public static String toPgn(Game game) {
		StringBuilder builder = new StringBuilder(2500);
		PgnHeader[] requiredHeaders = PgnHeader.STR_HEADERS;
		for (PgnHeader requiredHeader : requiredHeaders) {
			String headerValue = game.getHeader(requiredHeader);

			if (headerValue == null || headerValue.equals("")) {
				headerValue = PgnHeader.UNKNOWN_VALUE;
				game.setHeader(requiredHeader, headerValue);
			}
			PgnUtils
					.buildHeader(builder, requiredHeader.getName(), headerValue);
			builder.append("\n");
		}

		Set<String> allHeaders = game.getAllHeaders();
		for (String header : allHeaders) {
			if (getIndex(requiredHeaders, header) == -1) {
				String headerValue = game.getHeader(header);
				if (headerValue == null || headerValue.equals("")) {
					headerValue = PgnHeader.UNKNOWN_VALUE;
					game.setHeader(header, headerValue);
				}

				PgnUtils.buildHeader(builder, header, headerValue);
				builder.append("\n");
			}
		}
		builder.append("\n");

		boolean nextMoveRequiresNumber = true;
		int charsInCurrentLine = 0;

		// TO DO: add breaking up lines in comments.
		for (int i = 0; i < game.getHalfMoveCount(); i++) {
			int charsBefore = builder.length();
			nextMoveRequiresNumber = PgnUtils.buildMoveInfo(builder, game
					.getMoveList().get(i), nextMoveRequiresNumber);
			charsInCurrentLine += builder.length() - charsBefore;

			if (charsInCurrentLine > 75) {
				charsInCurrentLine = 0;
				builder.append("\n");
			} else {
				builder.append(" ");
			}
		}

		if (game.isCheckmate() || game.isStalemate()) {
			builder.append(game.getResult().getDescription());
		} else {
			builder.append(Result.ON_GOING.getDescription());
		}

		return builder.toString();
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

}
