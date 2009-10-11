package raptor.chess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.chess.util.GameUtils;
import raptor.chess.util.ZobristUtils;
import raptor.util.RaptorStringTokenizer;

/**
 * Contains methods to create Games from fen and starting positions.
 */
public class GameFactory implements GameConstants {

	public static final Log LOG = LogFactory.getLog(GameFactory.class);

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
	public static final Game createFromFen(String fen, Variant variant) {
		Game result = null;

		switch (variant) {
		case classic:
		case wild:
			result = new ClassicGame();
			break;
		case losers:
			result = new LosersGame();
			break;
		case atomic:
			result = new AtomicGame();
			break;
		case suicide:
			result = new SuicideGame();
			break;
		case fischerRandom:
			result = new FischerRandomGame();
			break;
		case bughouse:
			result = new BughouseGame();
			break;
		case crazyhouse:
			result = new CrazyhouseGame();
			break;
		default:
			throw new IllegalArgumentException("Variant " + variant
					+ " is not supported");
		}

		RaptorStringTokenizer tok = new RaptorStringTokenizer(fen, " ", false);
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
							+ variant);
		}

		result.setZobristPositionHash(ZobristUtils
				.zobristHashPositionOnly(result));
		result.setZobristGameHash(result.getZobristPositionHash()
				^ ZobristUtils.zobrist(result.getColorToMove(), result
						.getEpSquare(), result.getCastling(WHITE), result
						.getCastling(BLACK)));

		result.incrementRepCount();

		if (variant == Variant.crazyhouse || variant == Variant.bughouse) {
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
			for (int color = 0; color < 2; color++) {
				for (int piece = PAWN; piece <= KING; piece++) {

					if (result.getDropCount(color, piece) < 0) {
						result.setDropCount(color, piece, 0);
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

	public static final Game createStartingPosition(Variant variant) {
		if (variant == Variant.suicide) {
			return createFromFen(STARTING_SUICIDE_POSITION_FEN, variant);
		} else {
			return createFromFen(STARTING_POSITION_FEN, variant);
		}
	}
}