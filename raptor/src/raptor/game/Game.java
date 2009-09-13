package raptor.game;

import static raptor.game.util.GameUtils.bitscanClear;
import static raptor.game.util.GameUtils.bitscanForward;
import static raptor.game.util.GameUtils.diagonalMove;
import static raptor.game.util.GameUtils.getBitmap;
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

import java.util.Arrays;

import raptor.game.util.GameUtils;
import raptor.game.util.ZobristHash;

public class Game implements GameConstants {

	public static final int BLITZ = 0;
	public static final int LIGHTNING = 1;
	public static final int STANDARD = 2;
	public static final int WILD = 3;
	public static final int FISCHER_RANDOM = 4;
	public static final int SUCIDE = 5;
	public static final int ATOMIC = 6;
	public static final int LOSERS = 7;
	public static final int CRAZY_HOUSE = 8;
	public static final int BUGHOUSE = 9;

	public static final int ACTIVE_STATE = 2;
	public static final int INACTIVE_STATE = 4;
	public static final int EXAMINING_STATE = 8;
	public static final int PLAYING_STATE = 16;
	public static final int UNTIMED_STTE = 32;
	public static final int DROPPABLE_STATE = 64;

	public static final int IN_PROGRESS_RESULT = 0;
	public static final int WHTIE_WON_RESULT = 1;
	public static final int BLACK_WON_RESULT = 2;
	public static final int DRAW_RESULT = 3;
	public static final int UNDETERMINED_RESULT = 4;

	protected String id;
	protected int state;
	protected int result;
	protected int type;

	protected String whiteName;
	protected String blackName;
	protected String whiteRating;
	protected String blackRating;
	protected String typeDescription;
	protected long initialTimeMillis;
	protected long initialIncMillis;
	protected long whiteTimeMillis;
	protected long blackTimeMillis;
	protected long whiteLagMillis;
	protected long blackLagMillis;
	protected long startTime;
	protected String site;
	protected String event;
	protected String resultDescription;

	protected MoveList moves = new MoveList();
	protected int halfMoveCount;
	protected long[] colorBB = new long[2];
	protected long[][] pieceBB = new long[2][7];
	protected int[] board = new int[64];
	protected long occupiedBB;
	protected long emptyBB;
	protected long notColorToMoveBB;
	protected int[] castling = new int[2];
	protected int initialEpSquare = EMPTY_SQUARE;
	protected int epSquare = EMPTY_SQUARE;
	protected int colorToMove;
	protected int fiftyMoveCount;
	protected int[][] pieceCounts = new int[2][7];
	protected int[][] dropCounts = new int[2][7];
	protected long zobristPositionHash;
	protected long zobristGameHash;
	protected int[] moveRepHash = new int[MOVE_REP_CACHE_SIZE];

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getResultDescription() {
		return resultDescription;
	}

	public void setResultDescription(String resultDescription) {
		this.resultDescription = resultDescription;
	}

	public String getWhiteName() {
		return whiteName;
	}

	public void setWhiteName(String whiteName) {
		this.whiteName = whiteName;
	}

	public String getBlackName() {
		return blackName;
	}

	public void setBlackName(String blackName) {
		this.blackName = blackName;
	}

	public String getWhiteRating() {
		return whiteRating;
	}

	public void setWhiteRating(String whiteRating) {
		this.whiteRating = whiteRating;
	}

	public String getBlackRating() {
		return blackRating;
	}

	public void setBlackRating(String blackRating) {
		this.blackRating = blackRating;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTypeDescription() {
		return typeDescription;
	}

	public void setTypeDescription(String typeDescription) {
		this.typeDescription = typeDescription;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getInitialTimeMillis() {
		return initialTimeMillis;
	}

	public void setInitialTimeMillis(long initialTimeMillis) {
		this.initialTimeMillis = initialTimeMillis;
	}

	public long getInitialIncMillis() {
		return initialIncMillis;
	}

	public void setInitialIncMillis(long initialIncMillis) {
		this.initialIncMillis = initialIncMillis;
	}

	public long getWhiteTimeMillis() {
		return whiteTimeMillis;
	}

	public void setWhiteTimeMillis(long whiteTimeMillis) {
		this.whiteTimeMillis = whiteTimeMillis;
	}

	public long getBlackTimeMillis() {
		return blackTimeMillis;
	}

	public void setBlackTimeMillis(long blackTimeMillis) {
		this.blackTimeMillis = blackTimeMillis;
	}

	public long getWhiteLagMillis() {
		return whiteLagMillis;
	}

	public void setWhiteLagMillis(long whiteLagMillis) {
		this.whiteLagMillis = whiteLagMillis;
	}

	public long getBlackLagMillis() {
		return blackLagMillis;
	}

	public void setBlackLagMillis(long blackLagMillis) {
		this.blackLagMillis = blackLagMillis;
	}

	public int getRepHash() {
		return (int) (zobristPositionHash & MOVE_REP_CACHE_SIZE_MINUS_1);
	}

	public int getRepCount() {
		return moveRepHash[getRepHash()];
	}

	public void incrementRepCount() {
		moveRepHash[getRepHash()]++;
	}

	public void decrementRepCount() {
		moveRepHash[getRepHash()]--;
	}

	public long getZobristGameHash() {
		return zobristGameHash;
	}

	public void setZobristGameHash(long hash) {
		zobristGameHash = hash;
	}

	public long getZobristPositionHash() {
		return zobristPositionHash;
	}

	public void setZobristPositionHash(long hash) {
		zobristPositionHash = hash;
	}

	public Position getPosition() {
		return new Position(this);
	}

	public void setDropCount(int color, int piece, int count) {
		if ((piece & PROMOTED_MASK) != 0) {
			piece = PAWN;
		}
		pieceCounts[color][piece] = count;
	}

	public int getDropCount(int color, int piece) {
		return pieceCounts[color][piece];
	}

	public void setPieceCount(int color, int piece, int count) {
		pieceCounts[color][piece] = count;
	}

	public int getPieceCount(int color, int piece) {
		return pieceCounts[color][piece];
	}

	public void setPieceBB(int color, int piece, long bb) {
		pieceBB[color][piece] = bb;
	}

	public void setColorBB(int color, long bb) {
		colorBB[color] = bb;
	}

	public int[] getBoard() {
		return board;
	}

	public void setBoard(int[] board) {
		this.board = board;
	}

	public long getOccupiedBB() {
		return occupiedBB;
	}

	public void setOccupiedBB(long occupiedBB) {
		this.occupiedBB = occupiedBB;
	}

	public long getEmptyBB() {
		return emptyBB;
	}

	public void setEmptyBB(long emptyBB) {
		this.emptyBB = emptyBB;
	}

	public long getNotColorToMoveBB() {
		return notColorToMoveBB;
	}

	public void setNotColorToMoveBB(long notColorToMoveBB) {
		this.notColorToMoveBB = notColorToMoveBB;
	}

	public int getEpSquare() {
		return epSquare;
	}

	public void setEpSquare(int epSquare) {
		this.epSquare = epSquare;
	}

	public long getPieceBB(int piece) {
		return pieceBB[0][piece] | pieceBB[1][piece];
	}

	public long getPieceBB(int color, int piece) {
		return pieceBB[color][piece];
	}

	public void xor(int color, int piece, long bb) {
		pieceBB[color][piece] ^= bb;
	}

	public void xor(int color, long bb) {
		colorBB[color] ^= bb;
	}

	public long getColorBB(int color) {
		return colorBB[color];
	}

	public int getPiece(int square) {
		return board[square] & NOT_PROMOTED_MASK;
	}

	public void setPiece(int square, int piece) {
		board[square] = piece;
	}

	public int getPieceWithPromoteMask(int square) {
		return board[square];
	}

	public void setCastling(int color, int castling) {
		this.castling[color] = castling;
	}

	public int getCastling(int color) {
		return castling[color];
	}

	public int getColorToMove() {
		return colorToMove;
	}

	public void setColorToMove(int color) {
		this.colorToMove = color;
	}

	public int getInitialEpSquare() {
		return initialEpSquare;
	}

	public void setInitialEpSquare(int initialEpSquare) {
		this.initialEpSquare = initialEpSquare;
	}

	public boolean areBothKingsOnBoard() {
		return getPieceBB(WHITE, KING) != 0L && getPieceBB(BLACK, KING) != 0L;
	}

	public int getHalfMoveCount() {
		return halfMoveCount;
	}

	public void setHalfMoveCount(int halfMoveCount) {
		this.halfMoveCount = halfMoveCount;
	}

	public int getFiftyMoveCount() {
		return fiftyMoveCount;
	}

	public void setFiftyMoveCount(int fiftyMoveCount) {
		this.fiftyMoveCount = fiftyMoveCount;
	}

	// rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
	public String toFEN() {
		StringBuilder result = new StringBuilder(77);
		for (int j = 7; j > -1; j--) {
			int consecutiveEmpty = 0;
			for (int i = 0; i < 8; i++) {
				int square = rankFileToSquare(j, i);
				int piece = getPiece(square);

				if (piece == EMPTY) {
					consecutiveEmpty++;
				} else {
					long squareBB = getBitmap(square);
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

		result.append(colorToMove == WHITE ? " w" : " b");

		result.append(" " + getFenCastle());
		result.append(" " + getSan(epSquare));
		result.append(" " + fiftyMoveCount);
		result.append(" " + halfMoveCount);

		return result.toString();
	}

	private String getFenCastle() {
		String whiteCastlingFen = getCastling(WHITE) == CASTLE_NONE ? ""
				: getCastling(WHITE) == CASTLE_BOTH ? "KQ"
						: getCastling(WHITE) == CASTLE_KINGSIDE ? "K" : "Q";
		String blackCastlingFen = getCastling(BLACK) == CASTLE_NONE ? ""
				: getCastling(BLACK) == CASTLE_BOTH ? "kq"
						: getCastling(BLACK) == CASTLE_KINGSIDE ? "k" : "q";

		return whiteCastlingFen.equals("") && blackCastlingFen.equals("") ? " -"
				: whiteCastlingFen + blackCastlingFen;
	}

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
				int color = (getBitmap(square) & getColorBB(colorToMove)) != 0L ? colorToMove
						: getOppositeColor(colorToMove);

				result.append("|" + COLOR_PIECE_TO_CHAR[color].charAt(piece));
			}
			result.append("|   ");

			switch (i) {
			case 7:
				break;
			case 6:
				result.append("To Move: " + COLOR_DESCRIPTION[colorToMove]
						+ " " + "Last Move: "
						+ (moves.getSize() == 0 ? "" : moves.getLast()));
				break;
			case 5:
				result.append("Moves: " + halfMoveCount + " EP: "
						+ getSan(epSquare) + " Castle: " + getFenCastle());
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
		// result.append("\n MoveList:" + moves);
		return result.toString();
	}

	/**
	 * Use with care expensive operation.
	 */
	public String toString() {
		StringBuilder result = new StringBuilder(1000);

		result.append(getString(new String[] { "emptyBB", "occupiedBB",
				"notColorToMoveBB", "color[WHITE]", "color[BLACK]" },
				new long[] { emptyBB, occupiedBB, notColorToMoveBB,
						getColorBB(WHITE), getColorBB(BLACK) })
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

		String legalMovesString = Arrays.toString(getLegalMoves().asArray());
		// "DISABLED";

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
				int color = (getBitmap(square) & getColorBB(colorToMove)) != 0L ? colorToMove
						: getOppositeColor(colorToMove);

				result.append("|" + COLOR_PIECE_TO_CHAR[color].charAt(piece));
			}
			result.append("|   ");

			switch (i) {
			case 7:
				result.append("To Move: " + COLOR_DESCRIPTION[colorToMove]
						+ " " + "Last Move: "
						+ (moves.getSize() == 0 ? "" : moves.getLast()));
				break;

			case 6:
				result.append("Piece counts [" + getPieceCount(WHITE, PAWN)
						+ " " + getPieceCount(WHITE, KNIGHT) + " "
						+ getPieceCount(WHITE, BISHOP) + " "
						+ getPieceCount(WHITE, ROOK) + " "
						+ getPieceCount(WHITE, QUEEN) + " "
						+ getPieceCount(WHITE, KING) + "]["
						+ getPieceCount(BLACK, PAWN) + " "
						+ getPieceCount(BLACK, KNIGHT) + " "
						+ getPieceCount(BLACK, BISHOP) + " "
						+ getPieceCount(BLACK, ROOK) + " "
						+ getPieceCount(BLACK, QUEEN) + " "
						+ getPieceCount(BLACK, KING) + "]");
				break;
			case 5:
				result.append("Moves: " + halfMoveCount + " EP: "
						+ getSan(epSquare) + " Castle: " + getFenCastle());
				break;
			case 4:
				result.append("Move List: " + moves);
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
		return result.toString();
	}

	public MoveList getMoves() {
		return moves;
	}

	public Move makeLanMove(String lan) {
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
	
	public Move makeMove(int startSquare,int endSquare) throws IllegalArgumentException {
		Move move = null;

		Move[] legals = getLegalMoves().asArray();

		for (int i = 0; move == null && i < legals.length; i++) {
			Move candidate = legals[i];
			if (candidate.getFrom() == startSquare && candidate.getTo() == endSquare) {
				move = candidate;
			}
		}

		if (move == null) {
			throw new IllegalArgumentException("Invalid move: " + startSquare + " " + endSquare + " \n"
					+ toString());
		} else {
			forceMove(move);
		}

		return move;
	}

	public boolean move(Move move) {
		// first make the move.
		forceMove(move);
		if (!isLegalPosition()) {
			rollback();
			return false;
		}
		return true;
	}

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

	public boolean isLegalPosition() {
		return areBothKingsOnBoard()
				&& !isInCheck(getOppositeColor(getColorToMove()));
	}

	public PriorityMoveList getPseudoLegalMoves() {
		PriorityMoveList result = new PriorityMoveList();
		genPseudoQueenMoves(result);
		genPseudoKnightMoves(result);
		genPseudoBishopMoves(result);
		genPseudoRookMoves(result);
		genPseudoPawnMoves(result);
		genPseudoKingMoves(result);
		return result;
	}

	public boolean isCheckmate() {
		return isCheckmate(getLegalMoves());
	}

	public boolean isCheckmate(PriorityMoveList moveList) {
		return moveList.getSize() == 0 && isInCheck(getColorToMove());
	}

	public boolean isStalemate() {
		return isStalemate(getLegalMoves());
	}

	public boolean isStalemate(PriorityMoveList moveList) {
		return moveList.getSize() == 0 && !isInCheck(getColorToMove());
	}

	public boolean isInCheck(int color) {
		return isInCheck(color, getPieceBB(color, KING));
	}

	public boolean isInCheck(int color, long pieceBB) {
		long kingBB = pieceBB;
		int kingSquare = bitscanForward(kingBB);
		int oppositeColor = getOppositeColor(color);

		return !(((pawnCapture(oppositeColor, getPieceBB(oppositeColor, PAWN),
				kingBB) == 0L) && (orthogonalMove(kingSquare, getEmptyBB(),
				getOccupiedBB()) & (getPieceBB(oppositeColor, ROOK) | getPieceBB(
				oppositeColor, QUEEN))) == 0L)
				&& ((diagonalMove(kingSquare, getEmptyBB(), getOccupiedBB()) & (getPieceBB(
						oppositeColor, BISHOP) | getPieceBB(oppositeColor,
						QUEEN))) == 0L)
				&& ((kingMove(kingSquare) & getPieceBB(oppositeColor, KING)) == 0L) && ((knightMove(kingSquare) & getPieceBB(
				oppositeColor, KNIGHT)) == 0L));
	}

	public void forceMove(Move move) {
		move.setLastCastleState(getCastling(getColorToMove()));

		switch (move.getMoveCharacteristic()) {
		case Move.EN_PASSANT_CHARACTERISTIC:
			makeEPMove(move);
			break;
		case Move.KINGSIDE_CASTLING_CHARACTERISTIC:
		case Move.QUEENSIDE_CASTLING_CHARACTERISTIC:
			makeCastlingMove(move);
			break;
		default:
			makeNonEpNonCastlingMove(move);
			break;
		}

		int oppToMove = getOppositeColor(getColorToMove());

		move.setPrevious50MoveCount(getFiftyMoveCount());
		if (move.isCapture()) {
			setPieceCount(oppToMove, move.getCapture(), getPieceCount(
					oppToMove, move.getCapture()) - 1);
			setFiftyMoveCount(0);
		} else if (move.getPiece() == PAWN) {
			setFiftyMoveCount(0);
		} else {
			setFiftyMoveCount(getFiftyMoveCount() + 1);
		}

		setColorToMove(oppToMove);
		setNotColorToMoveBB(~getColorBB(getColorToMove()));
		setHalfMoveCount(getHalfMoveCount() + 1);

		getMoves().append(move);

		updateZobristHash();
		incrementRepCount();
	}

	public void rollback() {

		Move move = getMoves().removeLast();
		decrementRepCount();

		switch (move.getMoveCharacteristic()) {
		case Move.EN_PASSANT_CHARACTERISTIC:
			rollbackEpMove(move);
			break;
		case Move.KINGSIDE_CASTLING_CHARACTERISTIC:
		case Move.QUEENSIDE_CASTLING_CHARACTERISTIC:
			rollbackCastlingMove(move);
			break;
		default:
			rollbackNonEpNonCastlingMove(move);
			break;
		}

		if (move.isCapture()) {
			setPieceCount(getColorToMove(), move.getCapture(), getPieceCount(
					getColorToMove(), move.getCapture()) + 1);
		}

		setColorToMove(getOppositeColor(getColorToMove()));
		setNotColorToMoveBB(~getColorBB(getColorToMove()));
		setHalfMoveCount(getHalfMoveCount() - 1);

		setFiftyMoveCount(move.getPrevious50MoveCount());
		setCastling(getColorToMove(), move.getLastCastleState());

		updateZobristHash();
	}

	public void makeNonEpNonCastlingMove(Move move) {
		long fromBB = getBitmap(move.getFrom());
		long toBB = getBitmap(move.getTo());
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

			setPiece(move.getTo(), move.getPiecePromotedTo()
					& NOT_PROMOTED_MASK);
			setPiece(move.getFrom(), EMPTY);

			// capture is handled in forceMove.
			setPieceCount(getColorToMove(), PAWN, getPieceCount(
					getColorToMove(), PAWN) - 1);
			setPieceCount(getColorToMove(), move.getPiecePromotedTo()
					& NOT_PROMOTED_MASK, getPieceCount(getColorToMove(), move
					.getPiecePromotedTo()
					& NOT_PROMOTED_MASK) + 1);
		} else {
			xor(move.getColor(), move.getPiece(), fromToBB);

			setPiece(move.getTo(), move.getPiece());
			setPiece(move.getFrom(), EMPTY);
		}

		switch (move.getPiece()) {
		case KING:
			setCastling(getColorToMove(), CASTLE_NONE);
			break;
		default:
			if ((move.getPiece() == ROOK && move.getFrom() == SQUARE_A1 && getColorToMove() == WHITE)
					|| (move.getCapture() == ROOK && move.getTo() == SQUARE_A1 && getColorToMove() == BLACK)) {
				setCastling(WHITE, getCastling(WHITE) & CASTLE_KINGSIDE);
			} else if ((move.getPiece() == ROOK && move.getFrom() == SQUARE_H1 && getColorToMove() == WHITE)
					|| (move.getCapture() == ROOK && move.getTo() == SQUARE_H1 && getColorToMove() == BLACK)) {
				setCastling(WHITE, getCastling(WHITE) & CASTLE_QUEENSIDE);
			} else if ((move.getPiece() == ROOK && move.getFrom() == SQUARE_A8 && getColorToMove() == BLACK)
					|| (move.getCapture() == ROOK && move.getTo() == SQUARE_A8 && getColorToMove() == WHITE)) {
				setCastling(BLACK, getCastling(BLACK) & CASTLE_KINGSIDE);
			} else if ((move.getPiece() == ROOK && move.getFrom() == SQUARE_H8 && getColorToMove() == BLACK)
					|| (move.getCapture() == ROOK && move.getTo() == SQUARE_H8 && getColorToMove() == WHITE)) {
				setCastling(BLACK, getCastling(BLACK) & CASTLE_QUEENSIDE);
			}
			break;
		}

		setEpSquare(move.getEpSquare());
	}

	public void rollbackNonEpNonCastlingMove(Move move) {
		int oppositeColor = getOppositeColor(move.getColor());
		long fromBB = getBitmap(move.getFrom());
		long toBB = getBitmap(move.getTo());
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
			setPieceCount(move.getColor(), PAWN, getPieceCount(move.getColor(),
					PAWN) + 1);
			setPieceCount(move.getColor(), move.getPiecePromotedTo()
					& NOT_PROMOTED_MASK, getPieceCount(move.getColor(), move
					.getPiecePromotedTo()
					& NOT_PROMOTED_MASK) - 1);

		} else {
			xor(move.getColor(), move.getPiece(), fromToBB);
		}

		setPiece(move.getFrom(), move.getPiece());
		setPiece(move.getTo(), move.getCapture());

		setEpSquareFromPreviousMove();
	}

	public void makeCastlingMove(Move move) {
		long kingFromBB, kingToBB, rookFromBB, rookToBB;

		if (move.getColor() == WHITE) {
			kingFromBB = E1;
			if (move.getMoveCharacteristic() == Move.KINGSIDE_CASTLING_CHARACTERISTIC) {
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
			if (move.getMoveCharacteristic() == Move.KINGSIDE_CASTLING_CHARACTERISTIC) {
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

	public void rollbackCastlingMove(Move move) {
		long kingFromBB, kingToBB, rookFromBB, rookToBB;

		if (move.getColor() == WHITE) {
			kingFromBB = E1;
			if (move.getMoveCharacteristic() == Move.KINGSIDE_CASTLING_CHARACTERISTIC) {
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
			if (move.getMoveCharacteristic() == Move.KINGSIDE_CASTLING_CHARACTERISTIC) {
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

	public void makeEPMove(Move move) {
		long fromBB = getBitmap(move.getFrom());
		long toBB = getBitmap(move.getTo());
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

	public void rollbackEpMove(Move move) {
		int oppositeColor = getOppositeColor(getColorToMove());
		long fromBB = getBitmap(move.getFrom());
		long toBB = getBitmap(move.getTo());
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

	public void genPseudoPawnEPCaptures(int fromSquare, long fromBB,
			int oppositeColor, PriorityMoveList moves) {
		if (getEpSquare() != EMPTY) {

			long toBB = pawnEpCapture(getColorToMove(), fromBB, getPieceBB(
					oppositeColor, PAWN), getBitmap(getEpSquare()));

			if (toBB != 0) {
				int toSquare = bitscanForward(toBB);

				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						PAWN, EMPTY, EMPTY_SQUARE,
						Move.EN_PASSANT_CHARACTERISTIC), moves);
			}
		}
	}

	public void genPseudoPawnCaptures(int fromSquare, long fromBB,
			int oppositeColor, PriorityMoveList moves) {

		long toBB = pawnCapture(getColorToMove(), fromBB,
				getColorBB(oppositeColor));

		while (toBB != 0L) {
			int toSquare = bitscanForward(toBB);
			if ((toBB & (RANK8_OR_RANK1)) != 0L) {
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPieceWithPromoteMask(toSquare), PROMOTED_KNIGHT,
						EMPTY_SQUARE, Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPiece(toSquare), PROMOTED_BISHOP, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPiece(toSquare), PROMOTED_QUEEN, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPiece(toSquare), PROMOTED_ROOK, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
			} else {
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						getPiece(toSquare)), moves);
			}
			toBB = bitscanClear(toBB);
		}
	}

	public void genPseudoPawnSinglePush(int fromSquare, long fromBB,
			int oppositeColor, PriorityMoveList moves) {

		long toBB = pawnSinglePush(getColorToMove(), fromBB, getEmptyBB());

		while (toBB != 0) {
			int toSquare = bitscanForward(toBB);

			if ((toBB & (RANK8_OR_RANK1)) != 0L) {
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY, PROMOTED_KNIGHT, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY, PROMOTED_BISHOP, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY, PROMOTED_QUEEN, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY, PROMOTED_ROOK, EMPTY_SQUARE,
						Move.PROMOTION_CHARACTERISTIC), moves);
			} else {
				addMove(new Move(fromSquare, toSquare, PAWN, getColorToMove(),
						EMPTY), moves);
			}

			toBB = bitscanClear(toBB);
		}
	}

	public void genPseudoPawnDoublePush(int fromSquare, long fromBB,
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

	public void genPseudoPawnMoves(PriorityMoveList moves) {
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
			long fromBB = getBitmap(fromSquare);

			genPseudoPawnEPCaptures(fromSquare, fromBB, oppositeColor, moves);
			genPseudoPawnCaptures(fromSquare, fromBB, oppositeColor, moves);
			genPseudoPawnSinglePush(fromSquare, fromBB, oppositeColor, moves);
			genPseudoPawnDoublePush(fromSquare, fromBB, oppositeColor,
					epModifier, moves);

			pawnsBB = bitscanClear(pawnsBB);
		}
	}

	public void genPseudoKnightMoves(PriorityMoveList moves) {

		long fromBB = getPieceBB(getColorToMove(), KNIGHT);

		while (fromBB != 0) {
			int fromSquare = bitscanForward(fromBB);

			long toBB = knightMove(fromSquare) & getNotColorToMoveBB();

			while (toBB != 0) {
				int toSquare = bitscanForward(toBB);
				int contents = getPieceWithPromoteMask(toSquare);

				addMove(new Move(fromSquare, toSquare, KNIGHT,
						getColorToMove(), contents), moves);

				toBB = bitscanClear(toBB);
				toSquare = bitscanForward(toBB);
			}

			fromBB = bitscanClear(fromBB);
		}
	}

	public void genPseudoBishopMoves(PriorityMoveList moves) {
		long fromBB = getPieceBB(getColorToMove(), BISHOP);

		while (fromBB != 0) {
			int fromSquare = bitscanForward(fromBB);

			long toBB = diagonalMove(fromSquare, getEmptyBB(), getOccupiedBB())
					& getNotColorToMoveBB();

			while (toBB != 0) {
				int toSquare = bitscanForward(toBB);

				int contents = getPieceWithPromoteMask(toSquare);

				addMove(new Move(fromSquare, toSquare, BISHOP,
						getColorToMove(), contents), moves);
				toBB = bitscanClear(toBB);
			}
			fromBB = bitscanClear(fromBB);
		}
	}

	public void genPseudoQueenMoves(PriorityMoveList moves) {
		long fromBB = getPieceBB(getColorToMove(), QUEEN);

		while (fromBB != 0) {
			int fromSquare = bitscanForward(fromBB);

			long toBB = (orthogonalMove(fromSquare, getEmptyBB(),
					getOccupiedBB()) | (diagonalMove(fromSquare, getEmptyBB(),
					getOccupiedBB())))
					& getNotColorToMoveBB();

			while (toBB != 0) {
				int toSquare = bitscanForward(toBB);

				int contents = getPieceWithPromoteMask(toSquare);
				addMove(new Move(fromSquare, toSquare, QUEEN, getColorToMove(),
						contents), moves);
				toBB = bitscanClear(toBB);
			}

			fromBB = bitscanClear(fromBB);
		}
	}

	public void genPseudoRookMoves(PriorityMoveList moves) {
		long fromBB = getPieceBB(getColorToMove(), ROOK);

		while (fromBB != 0) {
			int fromSquare = bitscanForward(fromBB);

			long toBB = orthogonalMove(fromSquare, getEmptyBB(),
					getOccupiedBB())
					& getNotColorToMoveBB();

			while (toBB != 0) {
				int toSquare = bitscanForward(toBB);

				int contents = getPieceWithPromoteMask(toSquare);
				addMove(new Move(fromSquare, toSquare, ROOK, getColorToMove(),
						contents), moves);
				toBB = bitscanClear(toBB);
			}

			fromBB = bitscanClear(fromBB);
		}
	}

	public void genPseudoKingCastlingMoves(long fromBB, PriorityMoveList moves) {
		// The king destination square isnt checked, its checked when legal
		// getMoves() are checked.

		if (getColorToMove() == WHITE
				&& (getCastling(getColorToMove()) & CASTLE_KINGSIDE) != 0
				&& fromBB == E1 && getPiece(SQUARE_G1) == EMPTY
				&& getPiece(SQUARE_F1) == EMPTY && !isInCheck(WHITE, E1)
				&& !isInCheck(WHITE, F1)) {
			moves.appendLowPriority(new Move(SQUARE_E1, SQUARE_G1, KING,
					getColorToMove(), EMPTY,
					Move.KINGSIDE_CASTLING_CHARACTERISTIC));
		}

		if (getColorToMove() == WHITE
				&& (getCastling(getColorToMove()) & CASTLE_QUEENSIDE) != 0
				&& fromBB == E1 && getPiece(SQUARE_D1) == EMPTY
				&& getPiece(SQUARE_C1) == EMPTY && getPiece(SQUARE_B1) == EMPTY
				&& !isInCheck(WHITE, E1) && !isInCheck(WHITE, D1)) {
			moves.appendLowPriority(new Move(SQUARE_E1, SQUARE_C1, KING,
					getColorToMove(), EMPTY,
					Move.QUEENSIDE_CASTLING_CHARACTERISTIC));
		}

		if (getColorToMove() == BLACK
				&& (getCastling(getColorToMove()) & CASTLE_KINGSIDE) != 0
				&& fromBB == E8 && getPiece(SQUARE_G8) == EMPTY
				&& getPiece(SQUARE_F8) == EMPTY && !isInCheck(BLACK, E8)
				&& !isInCheck(BLACK, F8)) {
			moves.appendLowPriority(new Move(SQUARE_E8, SQUARE_G8, KING,
					getColorToMove(), EMPTY,
					Move.KINGSIDE_CASTLING_CHARACTERISTIC));

		}

		if (getColorToMove() == BLACK
				&& (getCastling(getColorToMove()) & CASTLE_QUEENSIDE) != 0
				&& fromBB == E8 && getPiece(SQUARE_D8) == EMPTY
				&& getPiece(SQUARE_C8) == EMPTY && getPiece(SQUARE_B8) == EMPTY
				&& !isInCheck(BLACK, E8) && !isInCheck(BLACK, D8)) {
			moves.appendLowPriority(new Move(SQUARE_E8, SQUARE_C8, KING,
					getColorToMove(), EMPTY,
					Move.QUEENSIDE_CASTLING_CHARACTERISTIC));
		}
	}

	public void genPseudoKingMoves(PriorityMoveList moves) {
		long fromBB = getPieceBB(getColorToMove(), KING);
		int fromSquare = bitscanForward(fromBB);
		long toBB = kingMove(fromSquare) & getNotColorToMoveBB();

		genPseudoKingCastlingMoves(fromBB, moves);

		while (toBB != 0) {
			int toSquare = bitscanForward(toBB);

			int contents = getPieceWithPromoteMask(toSquare);

			addMove(new Move(fromSquare, toSquare, KING, getColorToMove(),
					contents), moves);
			toBB = bitscanClear(toBB);
			toSquare = bitscanForward(toBB);
		}
	}

	public void setEpSquareFromPreviousMove() {
		switch (getMoves().getSize()) {
		case 0:
			setEpSquare(getInitialEpSquare());
			break;
		default:
			setEpSquare(getMoves().getLast().getEpSquare());
			break;
		}
	}

	void updateZobristPOCapture(Move move, int oppositeColor) {
		zobristPositionHash ^= ZobristHash.zobrist(move.getColor(), move
				.isPromotion() ? move.getPiecePromotedTo() & NOT_PROMOTED_MASK
				: move.getPiece() & NOT_PROMOTED_MASK, move.getTo())
				^ ZobristHash.zobrist(oppositeColor, move.getCapture()
						& NOT_PROMOTED_MASK, move.getTo())
				^ ZobristHash.zobrist(move.getColor(), move.getPiece()
						& NOT_PROMOTED_MASK, move.getFrom());
	}

	void updateZobristPONoCapture(Move move, int oppositeColor) {
		zobristPositionHash ^= ZobristHash.zobrist(move.getColor(), move
				.isPromotion() ? move.getPiecePromotedTo() & NOT_PROMOTED_MASK
				: move.getPiece() & NOT_PROMOTED_MASK, move.getTo())
				^ ZobristHash.zobrist(move.getColor(), move.getPiece()
						& NOT_PROMOTED_MASK, move.getFrom());
	}

	void updateZobristHash() {
		zobristGameHash = zobristPositionHash
				^ ZobristHash.zobrist(getColorToMove(), getEpSquare(),
						getCastling(WHITE), getCastling(BLACK));
	}

	void updateZobristPOCastleKsideWhite() {
		zobristPositionHash ^= ZobristHash.zobrist(WHITE, KING, SQUARE_E1)
				^ ZobristHash.zobrist(WHITE, KING, SQUARE_G1)
				^ ZobristHash.zobrist(WHITE, ROOK, SQUARE_H1)
				^ ZobristHash.zobrist(WHITE, ROOK, SQUARE_F1);
	}

	void updateZobristPOCastleKsideBlack() {
		zobristPositionHash ^= ZobristHash.zobrist(BLACK, KING, SQUARE_E8)
				^ ZobristHash.zobrist(BLACK, KING, SQUARE_G8)
				^ ZobristHash.zobrist(BLACK, ROOK, SQUARE_H8)
				^ ZobristHash.zobrist(BLACK, ROOK, SQUARE_F8);
	}

	void updateZobristPOCastleQsideWhite() {
		zobristPositionHash ^= ZobristHash.zobrist(WHITE, KING, SQUARE_E1)
				^ ZobristHash.zobrist(WHITE, KING, SQUARE_C1)
				^ ZobristHash.zobrist(WHITE, ROOK, SQUARE_A1)
				^ ZobristHash.zobrist(WHITE, ROOK, SQUARE_D1);
	}

	void updateZobristPOCastleQsideBlack() {
		zobristPositionHash ^= ZobristHash.zobrist(BLACK, KING, SQUARE_E8)
				^ ZobristHash.zobrist(BLACK, KING, SQUARE_C8)
				^ ZobristHash.zobrist(BLACK, ROOK, SQUARE_A8)
				^ ZobristHash.zobrist(BLACK, ROOK, SQUARE_D8);
	}

	void updateZobristEP(Move move, int captureSquare) {
		zobristPositionHash ^= ZobristHash.zobrist(move.getColor(), PAWN, move
				.getFrom())
				^ ZobristHash.zobrist(move.getColor(), PAWN, move.getTo())
				^ ZobristHash.zobrist(move.getCaptureColor(), PAWN,
						captureSquare);
	}

	/**
	 * Currently places captures ahead of non captures.
	 */
	static void addMove(Move move, PriorityMoveList moves) {
		if (move.isCapture() || move.isPromotion()) {
			moves.appendHighPriority(move);
		} else {
			moves.appendLowPriority(move);
		}
	}

}
