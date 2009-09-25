package raptor.connector.fics;

import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.connector.fics.game.message.G1Message;
import raptor.connector.fics.game.message.Style12Message;
import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.Game.PositionState;
import raptor.game.util.GameUtils;
import raptor.game.util.ZobristHash;
import raptor.swt.chess.Utils;

public class FicsUtils implements GameConstants {
	private static final Log LOG = LogFactory.getLog(FicsUtils.class);

	public static final String LEGAL_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890 "
			+ "!@#$%^&*()-=_+`~[{]}\\|;:'\",<.>/?";

	public static final String BLITZ_IDENTIFIER = "blitz";

	public static final String LIGHTNING_IDENTIFIER = "lightning";

	public static final String WILD_IDENTIFIER = "wild";

	public static final String STANDARD_IDENTIFIER = "standard";

	public static final String SUICIDE_IDENTIFIER = "suicide";

	public static final String ATOMIC_IDENTIFIER = "atomic";

	public static final String BUGHOUSE_IDENTIFIER = "bughouse";

	public static final String LOSERS_IDENTIFIER = "losers";

	public static final String CRAZYHOUSE_IDENTIFIER = "crazyhouse";

	public static final String UNTIMED_IDENTIFIER = "untimed";

	public static final int UNTIMED_GAME_TYPE = 999;

	/**
	 * Returns true if a move was added, false if no move was added (This can
	 * occur on a refresh or a game end.).
	 */
	public static boolean addCurrentMove(Game game, Style12Message message) {
		boolean result = false;
		if ((!message.isWhitesMoveAfterMoveIsMade && game.getColorToMove() != WHITE)
				|| (message.isWhitesMoveAfterMoveIsMade && game
						.getColorToMove() == WHITE)) {
			// At the end of a game multiple <12> messages are sent.
			// The are also sent when a refresh is sent.
			game.setWhiteRemainingeTimeMillis(message.whiteRemainingTimeMillis);
			game.setBlackRemainingTimeMillis(message.blackRemainingTimeMillis);

		} else {
			if (message.san.equals("none")) {
				LOG.warn("Received a none for san in a style 12 event.");
			} else {
				game.makeSanMove(message.san);
			}

			game.setWhiteRemainingeTimeMillis(message.whiteRemainingTimeMillis);
			game.setBlackRemainingTimeMillis(message.blackRemainingTimeMillis);

			if (message.isWhitesMoveAfterMoveIsMade) {
				game.setBlackLagMillis(game.getBlackLagMillis()
						+ message.lagInMillis);
			} else {
				game.setWhiteLagMillis(game.getWhiteLagMillis()
						+ message.lagInMillis);
			}
			result = true;
		}
		return result;
	}

	/**
	 * Adjusts the game for takebacks. If this method returns true the game was
	 * updated and no more processing needs to be done. If this method returns
	 * false the game either didnt need any updating, or was rolled back to
	 * adjust for the takebacks and still requires adjusting to the move in the
	 * style 12 message.
	 */
	public static boolean adjustToTakebacks(Game game, Style12Message message) {
		boolean result = false;

		// First make sure the move counts match.
		// Takebacks will roll them back.
		int messageHalfMoveCount = 0;
		if (message.fullMoveNumber == 1) {
			messageHalfMoveCount = message.isWhitesMoveAfterMoveIsMade ? 1 : 0;
		} else if (message.fullMoveNumber == 2) {
			messageHalfMoveCount = message.isWhitesMoveAfterMoveIsMade ? 3 : 2;
		} else {
			// Half move to full move conversion table.
			// hm fm
			// 0 1 1 1
			// 2 3 2 2
			// 3 4 3 3
			// 5 6 4 4
			// 7 8 5 5
			// 9 10 6 6
			messageHalfMoveCount = (message.fullMoveNumber - 1) * 2
					+ (message.isWhitesMoveAfterMoveIsMade ? -1 : 0);
		}

		// Now check to see if we have all the moves inside the game to roll
		// back.
		int rollBacks = game.getHalfMoveCount() - messageHalfMoveCount;

		if (rollBacks != 0) {
			if (game.getMoves().getSize() < rollBacks) {
				// We have to do a hard reset of the games position because we
				// dont have the moves to rollback.
				resetGame(game, message);
				result = true;
			} else {
				System.err.println("Rolling back " + rollBacks);
				// Rollback the moves.
				for (int i = 0; i < rollBacks; i++) {
					game.rollback();
				}
				result = true;
			}
		}

		return result;
	}

	/**
	 * Clears out all the games position state.
	 */
	public static void clearGamePosition(Game game) {
		game.setPositionState(new PositionState());
	}

	public static Game createGame(G1Message g1) {
		Game result = null;
		int gameType = FicsUtils.identifierToGameType(g1.gameTypeDescription);
		switch (gameType) {
		case Game.BLITZ:
			result = new Game();
			result.setType(Game.BLITZ);
			break;
		case Game.STANDARD:
			result = new Game();
			result.setType(Game.STANDARD);
			break;
		case Game.LIGHTNING:
			result = new Game();
			result.setType(Game.LIGHTNING);
			break;
		default:
			LOG.error("Uhandled game type " + g1.gameTypeDescription);
			throw new IllegalStateException("Unsupported game type" + gameType);

		}

		result.setId(g1.gameId);
		result.setGameDescription(g1.gameTypeDescription);
		result.setSettingMoveSan(true);
		result.setStartTime(System.currentTimeMillis());
		result.setSite("freechess.org");
		result.setInitialWhiteTimeMillis(g1.initialWhiteTimeMillis);
		result.setInitialBlackTimeMillis(g1.initialBlackTimeMillis);
		result.setInitialWhiteIncMillis(g1.initialWhiteIncMillis);
		result.setInitialBlackIncMillis(g1.initialBlackIncMillis);
		result.setBlackRating(g1.blackRating);
		result.setWhiteRating(g1.whiteRating);
		result.setEvent(result.getInitialWhiteTimeMillis() / 60000 + " "
				+ result.getInitialWhiteIncMillis() / 1000 + " "
				+ (!g1.isRated ? "unrated" : "rated") + " "
				+ result.getGameDescription());

		return result;
	}

	public static Game createGame(Style12Message message) {
		if (message.relation == Style12Message.EXAMINING_GAME_RELATION
				|| message.relation == Style12Message.ISOLATED_POSITION_RELATION) {
			Game game = new Game();
			game.setId(message.gameId);
			game
					.setGameDescription(message.relation == Style12Message.EXAMINING_GAME_RELATION ? "Examining"
							: "SettingUp");
			game.setSettingMoveSan(true);
			game.setStartTime(System.currentTimeMillis());
			game.setSite("freechess.org");
			game.setInitialWhiteTimeMillis(0);
			game.setInitialWhiteIncMillis(0);
			game.setInitialBlackTimeMillis(0);
			game.setInitialBlackIncMillis(0);
			game.setBlackRating("");
			game.setWhiteRating("");
			game.setEvent(game.getGameDescription());

			updateNonPositionFields(game, message);
			updatePosition(game, message);
			verifyLegal(game);
			return game;
		} else {
			LOG.error("Cant create an examined game for relation "
					+ message.relation);
			throw new IllegalStateException(
					"Cant created a examined or setup game from a game with relation "
							+ message.relation);
		}
	}

	/**
	 * Filters out illegal chars, and appends a \n to the passed in message.
	 * This also converts unicode chars into Maciejg format. See
	 * maciejgFormatToUnicode for more info.
	 */
	public static void filterOutbound(StringBuilder message) {
		for (int i = 0; i < message.length(); i++) {
			char currentChar = message.charAt(i);
			if (LEGAL_CHARACTERS.indexOf(currentChar) == -1) {
				if (currentChar > 256) {
					int charAsInt = currentChar;
					String stringVersion = Integer.toString(charAsInt, 16);
					String replacement = "&#x" + stringVersion + ";";
					message.replace(i, i + 1, replacement);
					i += replacement.length() - 1;
				} else {
					message.deleteCharAt(i);
					i--;
				}
			}
		}
		message.append('\n');
	}

	/**
	 * Returns the game type constant for the specified identifier.
	 * 
	 */
	public static int identifierToGameType(String identifier) {
		int result = -1;

		if (identifier.indexOf(SUICIDE_IDENTIFIER) != -1)
			result = Game.SUICIDE;
		else if (identifier.indexOf(BUGHOUSE_IDENTIFIER) != -1)
			result = Game.BUGHOUSE;
		else if (identifier.indexOf(CRAZYHOUSE_IDENTIFIER) != -1)
			result = Game.CRAZY_HOUSE;
		else if (identifier.indexOf(STANDARD_IDENTIFIER) != -1)
			result = Game.STANDARD;
		else if (identifier.indexOf(WILD_IDENTIFIER) != -1)
			result = Game.WILD;
		else if (identifier.indexOf(LIGHTNING_IDENTIFIER) != -1)
			result = Game.LIGHTNING;
		else if (identifier.indexOf(BLITZ_IDENTIFIER) != -1)
			result = Game.BLITZ;
		else if (identifier.indexOf(ATOMIC_IDENTIFIER) != -1)
			result = Game.ATOMIC;
		else if (identifier.indexOf(LOSERS_IDENTIFIER) != -1)
			result = Game.LOSERS;
		else if (identifier.indexOf(UNTIMED_IDENTIFIER) != -1)
			result = UNTIMED_GAME_TYPE;

		else
			throw new IllegalArgumentException("Unknown identifier "
					+ identifier
					+ " encountered. Please notify someone on the raptor team "
					+ "so they can implement this new game type.");

		return result;
	}

	/**
	 * Maciejg format, named after him because of his finger notes. Unicode
	 * chars are represented as &#x3b1; &#x3b2; &#x3b3; &#x3b4; &#x3b5; &#x3b6;
	 * unicode equivalent \u03B1,\U03B2,...
	 */
	public static String maciejgFormatToUnicode(String inputString) {
		StringBuilder builder = new StringBuilder(inputString);
		int unicodePrefix = 0;
		while ((unicodePrefix = builder.indexOf("&#x", unicodePrefix)) != -1) {
			int endIndex = builder.indexOf(";", unicodePrefix);
			if (endIndex != -1 && (endIndex - unicodePrefix) <= 8) {
				String unicodeHex = builder.substring(unicodePrefix + 3,
						endIndex).toUpperCase();
				try {
					int intValue = Integer.parseInt(unicodeHex, 16);
					String replacement = new String(
							new char[] { (char) intValue });
					builder.replace(unicodePrefix, endIndex + 1, replacement);
				} catch (NumberFormatException nfe) {
					unicodePrefix = endIndex;
				}
			}
		}
		return builder.toString();
	}

	public static int removeRatingDecorators(String rating) {
		String ratingWithoutDecorators = "";

		for (int i = 0; i < rating.length(); i++) {
			if (Character.isDigit(rating.charAt(i))) {
				ratingWithoutDecorators += rating.charAt(i);
			}
		}
		return Integer.parseInt(ratingWithoutDecorators);
	}

	public static String removeTitles(String playerName) {
		StringTokenizer stringtokenizer = new StringTokenizer(playerName,
				"()~!@#$%^&*_+|}{';/., :[]");
		if (stringtokenizer.hasMoreTokens())
			return stringtokenizer.nextToken();
		else
			return playerName;
	}

	public static void resetGame(Game game, Style12Message message) {
		FicsUtils.clearGamePosition(game);
		FicsUtils.updateNonPositionFields(game, message);
		FicsUtils.updatePosition(game, message);
		verifyLegal(game);
	}

	/**
	 * Handles updating everything but the position related fields in the game.
	 * i.e. the bitboards,pieces, etc.
	 */
	public static void updateNonPositionFields(Game game, Style12Message message) {
		switch (message.relation) {
		case Style12Message.EXAMINING_GAME_RELATION:
			game.setState(Game.EXAMINING_STATE);
			break;
		case Style12Message.ISOLATED_POSITION_RELATION:
			game.setState(Game.SETUP_STATE);
			break;
		case Style12Message.OBSERVING_EXAMINED_GAME_RELATION:
			game.setState(Game.OBSERVING_EXAMINED_STATE);
			break;
		case Style12Message.OBSERVING_GAME_RELATION:
			game.setState(Game.OBSERVING_STATE | Game.IS_CLOCK_TICKING_STATE);
			break;
		case Style12Message.PLAYING_MY_MOVE_RELATION:
		case Style12Message.PLAYING_OPPONENTS_MOVE_RELATION:
			game.setState(Game.PLAYING_STATE | Game.IS_CLOCK_TICKING_STATE);
			break;
		}
		game.addState(Game.ACTIVE_STATE);

		game.setBlackName(message.blackName);
		game.setWhiteName(message.whiteName);

		game.setWhiteRemainingeTimeMillis(message.whiteRemainingTimeMillis);
		game.setBlackRemainingTimeMillis(message.blackRemainingTimeMillis);

		game
				.setColorToMove(message.isWhitesMoveAfterMoveIsMade ? WHITE
						: BLACK);

		game.setCastling(WHITE, message.canWhiteCastleKSide
				&& message.canWhiteCastleQSide ? CASTLE_BOTH
				: message.canWhiteCastleKSide ? CASTLE_KINGSIDE
						: message.canWhiteCastleQSide ? CASTLE_QUEENSIDE
								: CASTLE_NONE);
		game.setCastling(BLACK, message.canBlackCastleKSide
				&& message.canBlackCastleQSide ? CASTLE_BOTH
				: message.canBlackCastleKSide ? CASTLE_KINGSIDE
						: message.canBlackCastleQSide ? CASTLE_QUEENSIDE
								: CASTLE_NONE);

		if (message.doublePawnPushFile == -1) {
			game.setEpSquare(EMPTY_SQUARE);
			game.setInitialEpSquare(EMPTY_SQUARE);
		} else {
			int doublePawnPushSquare = GameUtils.rankFileToSquare(
					message.isWhitesMoveAfterMoveIsMade ? 4 : 5,
					message.doublePawnPushFile);
			game.setEpSquare(doublePawnPushSquare);
			game.setInitialEpSquare(doublePawnPushSquare);
		}

		game.setFiftyMoveCount(message.numberOfMovesSinceLastIrreversible);

		int fullMoveCount = message.fullMoveNumber;
		game
				.setHalfMoveCount(game.getColorToMove() == BLACK ? fullMoveCount * 2 - 1
						: fullMoveCount * 2 - 2);

		game.incrementRepCount();
	}

	/**
	 * Should be invoked after the castling,EP,and to move data has been set.
	 */
	public static void updatePosition(Game game, Style12Message style12) {
		for (int i = 0; i < style12.position.length; i++) {
			for (int j = 0; j < style12.position[i].length; j++) {
				if (style12.position[i][j] != EMPTY) {
					int square = GameUtils.rankFileToSquare(i, j);
					int pieceColor = Utils.isWhitePiece(style12.position[i][j]) ? WHITE
							: BLACK;
					int piece = Utils
							.pieceFromColoredPiece(style12.position[i][j]);
					long squareBB = GameUtils.getBitboard(square);

					game.setPieceCount(pieceColor, piece, game.getPieceCount(
							pieceColor, piece) + 1);
					game.getBoard()[square] = piece;
					game.setColorBB(pieceColor, game.getColorBB(pieceColor)
							| squareBB);
					game.setOccupiedBB(game.getOccupiedBB() | squareBB);
					game.setPieceBB(pieceColor, piece, game.getPieceBB(
							pieceColor, piece)
							| squareBB);
				}
			}
		}

		game.setEmptyBB(~game.getOccupiedBB());
		game.setNotColorToMoveBB(~game.getColorBB(game.getColorToMove()));

		game.setZobristPositionHash(ZobristHash.zobristHashPositionOnly(game));
		game.setZobristGameHash(game.getZobristPositionHash()
				^ ZobristHash.zobrist(game.getColorToMove(),
						game.getEpSquare(), game.getCastling(WHITE), game
								.getCastling(BLACK)));
	}

	public static void verifyLegal(Game game) {
		if (!game.isLegalPosition()) {
			throw new IllegalStateException("Position is not legal: "
					+ game.toString());
		}
	}

	/**
	 * Cleans up the message by ensuring only \n is used as a line terminator.
	 * \r\n and \r may be used depending on the operating system.
	 */
	public static String cleanupMessage(String message) {
		return StringUtils.remove(message, '\r');
	}
}
