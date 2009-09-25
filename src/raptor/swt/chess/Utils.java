package raptor.swt.chess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.util.GameUtils;
import raptor.swt.chess.controller.ExamineController;
import raptor.swt.chess.controller.ObserveController;

public class Utils implements Constants {
	private static final Log LOG = LogFactory.getLog(Utils.class);

	public static boolean arePiecesSameColor(int piece1, int piece2) {
		return (isWhitePiece(piece1) && isWhitePiece(piece2))
				|| (isBlackPiece(piece1) && isBlackPiece(piece2));
	}

	public static ChessBoardController buildController(Game game) {
		ChessBoardController controller = null;

		if (game.isInState(Game.OBSERVING_STATE)
				|| game.isInState(Game.OBSERVING_EXAMINED_STATE)) {
			controller = new ObserveController();

		} else if (game.isInState(Game.EXAMINING_STATE)) {
			controller = new ExamineController();
		} else {
			LOG.error("Could not find controller type for game state. "
					+ "Ignoring game. state= " + game.getState());
		}
		return controller;
	}

	public static int getColoredPiece(int piece, boolean isWhite) {
		switch (piece) {
		case PAWN:
			return isWhite ? WP : BP;
		case KNIGHT:
			return isWhite ? WN : BN;
		case BISHOP:
			return isWhite ? WB : BB;
		case ROOK:
			return isWhite ? WR : BR;
		case QUEEN:
			return isWhite ? WQ : BQ;
		case KING:
			return isWhite ? WK : BK;
		case EMPTY:
			return EMPTY;
		default:
			throw new IllegalArgumentException("Invalid piece " + piece);
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

	public static String halfMoveIndexToDescription(int halfMoveIndex,
			int colorToMove) {
		int fullMoveIndex = (halfMoveIndex / 2) + 1;

		return colorToMove == WHITE ? fullMoveIndex + ") " : fullMoveIndex
				+ ") ... ";
	}

	public static boolean isBlackPiece(int setPieceType) {
		return setPieceType > 7 && setPieceType < 13;
	}

	public static boolean isPieceJailSquare(int pieceJailSquare) {
		return pieceJailSquare > 100 ? true : false;
	}

	public static boolean isWhitePiece(int setPieceType) {
		return setPieceType > 0 && setPieceType < 7;
	}

	public static int pieceFromColoredPiece(int coloredPiece) {
		switch (coloredPiece) {
		case EMPTY:
			return GameConstants.EMPTY;
		case WP:
		case BP:
			return GameConstants.PAWN;
		case WN:
		case BN:
			return GameConstants.KNIGHT;
		case WB:
		case BB:
			return GameConstants.BISHOP;
		case WR:
		case BR:
			return GameConstants.ROOK;
		case WQ:
		case BQ:
			return GameConstants.QUEEN;
		case WK:
		case BK:
			return GameConstants.KING;
		default:
			throw new IllegalArgumentException("Invalid coloredPiece "
					+ coloredPiece);

		}
	}

	public static int pieceJailSquareToPiece(int pieceJailSquare) {
		return pieceJailSquare - 100;
	}

}
