package raptor.game.util;

import java.util.StringTokenizer;

import raptor.game.FischerRandomGame;
import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.LosersGame;
import raptor.game.SuicideGame;
import raptor.game.Game.Type;

//KoggeStone
//http://www.open-aurec.com/wbforum/viewtopic.php?f=4&t=49948&sid=abd6ee7224f34b11a5211aa167f01ac4
public class GameUtils implements GameConstants {
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

	public static final long bitscanClear(long bitboard) {
		return bitboard & (bitboard - 1);
	}

	/**
	 * Returns 0 if bitboard is 0.
	 */
	public static final int bitscanForward(long bitboard) {
		// Slower on intel
		// return Long.numberOfTrailingZeros(bitboard);
		return bitScanForwardDeBruijn64(bitboard);
	}

	public static int bitScanForwardDeBruijn64(long b) {
		int idx = (int) (((b & -b) * DE_BRUJIN) >>> 58);
		return DE_BRUJIN_MAGICS_TABLE[idx];
	}

	public static final long clearMulti(long bitboard, long squaresToClear) {
		return bitboard & ~squaresToClear;
	}

	public static final Game createFromFen(String fen, Type gameType) {
		// rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1

		Game result = null;

		switch (gameType) {
		case CLASSIC:
		case WILD:
			result = new Game();
			break;
		case LOSERS:
			result = new LosersGame();
			break;
		case SUICIDE:
			result = new SuicideGame();
			break;
		case FISCHER_RANDOM:
			result = new FischerRandomGame();
			break;
		case BUGHOUSE:
		case CRAZYHOUSE:
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
						: whiteCastleKSide ? CASTLE_KINGSIDE
								: whiteCastleQSide ? CASTLE_QUEENSIDE
										: CASTLE_NONE);
		result.setCastling(BLACK,
				blackCastleKSide && blackCastleQSide ? CASTLE_BOTH
						: blackCastleKSide ? CASTLE_KINGSIDE
								: blackCastleQSide ? CASTLE_QUEENSIDE
										: CASTLE_NONE);

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
					"Resulting position was illegal for FEN: " + fen + "\n"
							+ result);
		}

		result.setZobristPositionHash(ZobristHash
				.zobristHashPositionOnly(result));
		result.setZobristGameHash(result.getZobristPositionHash()
				^ ZobristHash.zobrist(result.getColorToMove(), result
						.getEpSquare(), result.getCastling(WHITE), result
						.getCastling(BLACK)));

		result.incrementRepCount();
		return result;
	}

	public static final Game createStartingPosition(Type gameType) {
		return createFromFen(STARTING_POSITION_FEN, gameType);
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
		g |= p & (g >>> 9);
		p &= (p >>> 9);
		g |= p & (g >>> 18);
		p &= (p >>> 18);
		return g |= p & (g >>> 36);
	}

	public static long fillDownOccluded(long g, long p) {
		g |= p & (g >>> 8);
		p &= (p >>> 8);
		g |= p & (g >>> 16);
		p &= (p >>> 16);
		return g |= p & (g >>> 32);
	}

	public static long fillDownRightfccluded(long g, long p) {
		p &= 0xfefefefefefefefeL;
		g |= p & (g >>> 7);
		p &= (p >>> 7);
		g |= p & (g >>> 14);
		p &= (p >>> 14);
		return g |= p & (g >>> 28);
	}

	public static long fillLeftOccluded(long g, long p) {
		p &= 0x7f7f7f7f7f7f7f7fL;
		g |= p & (g >>> 1);
		p &= (p >>> 1);
		g |= p & (g >>> 2);
		p &= (p >>> 2);
		return g |= p & (g >>> 4);
	}

	/**
	 * The routine fillUpOccluded() smears the set bits of bitboard g upwards,
	 * but only along set bits of p; a reset bit in p is enough to halt a smear.
	 * In the above, g = moving piece(s); p = empty squares.
	 */
	public static long fillRightOccluded(long g, long p) {
		p &= 0xfefefefefefefefeL;
		g |= p & (g << 1);
		p &= (p << 1);
		g |= p & (g << 2);
		p &= (p << 2);
		return g |= p & (g << 4);
	}

	public static long fillUpLeftOccluded(long g, long p) {
		p &= 0x7f7f7f7f7f7f7f7fL;
		g |= p & (g << 7);
		p &= (p << 7);
		g |= p & (g << 14);
		p &= (p << 14);
		return g |= p & (g << 28);
	}

	public static long fillUpOccluded(long g, long p) {
		g |= p & (g << 8);
		p &= (p << 8);
		g |= p & (g << 16);
		p &= (p << 16);
		return g |= p & (g << 32);
	}

	public static long fillUpRightOccluded(long g, long p) {
		p &= 0xfefefefefefefefeL;
		g |= p & (g << 9);
		p &= (p << 9);
		g |= p & (g << 18);
		p &= (p << 18);
		return g |= p & (g << 36);
	}

	public static final long getBitboard(int square) {
		return SQUARE_TO_COORDINATE[square];
	}

	public static int getFile(int square) {
		return square % 8;
	}

	public static final int getOppositeColor(int color) {
		return OPPOSITE_COLOR[color];
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
		return rankFileToSquare(RANK_FROM_SAN.indexOf((san.charAt(1))),
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
			for (int k = 0; k < bitBoards.length; k++) {
				result.append(" ");
				for (int j = 0; j < 8; j++) {
					result
							.append(((bitBoards[k] & SQUARE_TO_COORDINATE[rankFileToSquare(
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

	public static final boolean isInBounds(int rank, int file) {
		return rank >= 0 && rank <= 7 && file >= 0 && file <= 7;
	}

	public static final boolean isOnEdge(int zeroBasedRank, int zeroBasedFile) {
		return zeroBasedRank == 0 || zeroBasedRank == 7 || zeroBasedFile == 0
				|| zeroBasedFile == 7;
	}

	public static boolean isPromotion(boolean isWhiteToMove, Game game,
			int fromSquare, int toSquare) {
		if (isWhiteToMove) {
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
		return colorToMove == WHITE ? (((toMovePawns & NOT_AFILE) << 7) | ((toMovePawns & NOT_HFILE) << 9))
				& enemyPieces
				: (((toMovePawns & NOT_HFILE) >> 7) | ((toMovePawns & NOT_AFILE) >>> 9))
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
		return (moveOne(direction, toMovePawns) & empty);
	}

	public static final int populationCount(long bitboard) {
		return Long.bitCount(bitboard);
	}

	/**
	 * Returns the square given a 0 based rank and file.
	 */
	public static final int rankFileToSquare(int rank, int file) {
		return (rank * 8) + file;
	}

	public static long shiftDown(long b) {
		return b >>> 8;
	}

	public static long shiftDownLeft(long b) {
		return (b >>> 9) & 0x7f7f7f7f7f7f7f7fL;
	}

	public static long shiftDownRight(long b) {
		return (b >>> 7) & 0xfefefefefefefefeL;
	}

	public static long shiftLeft(long b) {
		return (b >>> 1) & 0x7f7f7f7f7f7f7f7fL;
	}

	// KoggeStone algorithm
	public static long shiftRight(long b) {
		return (b << 1) & 0xfefefefefefefefeL;
	}

	public static long shiftUp(long b) {
		return b << 8;
	}

	public static long shiftUpLeft(long b) {
		return (b << 7) & 0x7f7f7f7f7f7f7f7fL;
	}

	public static long shiftUpRight(long b) {
		return (b << 9) & 0xfefefefefefefefeL;
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

	public static long whitePawnCaptureEast(long whitePawns, long empty) {
		return ((whitePawns & 9187201950435737471L) << 9) & empty;
	}

	public static long whitePawnCaptureWest(long whitePawns, long empty) {
		return ((whitePawns & -72340172838076674L) << 7) & empty;
	}

	public static long whitePawnLegalDoublePush(long whitePawns, long empty) {
		long rank4 = 0x00000000FF000000L;
		long singlePush = whiteSinglePushTargets(whitePawns, empty);
		return (northOne(singlePush) & empty & rank4);
	}

	public static long whiteSinglePushTargets(long whitePawns, long empty) {
		return (northOne(whitePawns) & empty);
	}
}
