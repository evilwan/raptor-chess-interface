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
package raptor.connector.ics;

import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.chat.ChatType;
import raptor.chat.Bugger.BuggerStatus;
import raptor.chess.AtomicGame;
import raptor.chess.BughouseGame;
import raptor.chess.ClassicGame;
import raptor.chess.CrazyhouseGame;
import raptor.chess.FischerRandomBughouseGame;
import raptor.chess.FischerRandomCrazyhouseGame;
import raptor.chess.FischerRandomGame;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.GameFactory;
import raptor.chess.LosersGame;
import raptor.chess.Move;
import raptor.chess.SetupGame;
import raptor.chess.SuicideGame;
import raptor.chess.Variant;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.pgn.PgnUtils;
import raptor.chess.pgn.TimeTakenForMove;
import raptor.chess.util.GameUtils;
import raptor.chess.util.ZobristUtils;
import raptor.connector.Connector;
import raptor.connector.ics.game.message.G1Message;
import raptor.connector.ics.game.message.MovesMessage;
import raptor.connector.ics.game.message.Style12Message;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardUtils;
import raptor.swt.chess.controller.BughouseSuggestController;
import raptor.swt.chess.controller.ExamineController;
import raptor.swt.chess.controller.InactiveController;
import raptor.swt.chess.controller.ObserveController;
import raptor.swt.chess.controller.PlayingController;
import raptor.swt.chess.controller.SetupController;
import raptor.util.RaptorStringTokenizer;

public class IcsUtils implements GameConstants {
	public static final String ATOMIC_IDENTIFIER = "atomic";

	public static final String BLITZ_IDENTIFIER = "blitz";

	public static final String BUGHOUSE_IDENTIFIER = "bughouse";

	public static final String CHANNEL_STRIP_CHARS = "()~!@?#$%^&*_+|}{'\";/?<>.,:[]";

	public static final String CRAZYHOUSE_IDENTIFIER = "crazyhouse";

	public static final String FISCHER_RANDOM_IDENTIFIER = "wild/fr";

	public static final String LEGAL_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890 "
			+ "!@#$%^&*()-=_+`~[{]}\\|;:'\",<.>/?";

	public static final String LIGHTNING_IDENTIFIER = "lightning";

	private static final Log LOG = LogFactory.getLog(IcsUtils.class);

	public static final String LOSERS_IDENTIFIER = "losers";

	public static final String STANDARD_IDENTIFIER = "standard";

	public static final String STRIP_CHARS = "()~!@?#$%^&*_+|}{'\";/?<>.,:[]1234567890";

	public static final String SUICIDE_IDENTIFIER = "suicide";

	public static final String UNTIMED_IDENTIFIER = "untimed";

	public static final String VALID_PERSON_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static final String WILD_IDENTIFIER = "wild";

	/**
	 * Returns true if a move was added, false if no move was added (This can
	 * occur on a refresh or a game end.).
	 */
	public static boolean addCurrentMove(Game game, Style12Message message) {
		boolean result = false;
		if (!message.isWhitesMoveAfterMoveIsMade
				&& game.getColorToMove() != WHITE
				|| message.isWhitesMoveAfterMoveIsMade
				&& game.getColorToMove() == WHITE) {
			// At the end of a game multiple <12> messages are sent.
			// The are also sent when a refresh is sent.
			game.setHeader(PgnHeader.WhiteRemainingMillis, ""
					+ message.whiteRemainingTimeMillis);
			game.setHeader(PgnHeader.BlackRemainingMillis, ""
					+ message.blackRemainingTimeMillis);

			if (message.timeTakenForLastMoveMillis != 0) {
				game.getMoveList().get(game.getMoveList().getSize() - 1)
						.addAnnotation(
								new TimeTakenForMove(
										message.timeTakenForLastMoveMillis));
			}
		} else {
			if (message.san.equals("none")) {
				Raptor
						.getInstance()
						.onError(
								"Received a none for san in a style 12 event. This should have contained a move.");
			} else {
				Move move = game.makeSanMove(message.san);
				move.addAnnotation(new TimeTakenForMove(
						message.timeTakenForLastMoveMillis));
			}

			game.setHeader(PgnHeader.WhiteRemainingMillis, ""
					+ message.whiteRemainingTimeMillis);
			game.setHeader(PgnHeader.BlackRemainingMillis, ""
					+ message.blackRemainingTimeMillis);

			if (message.isWhitesMoveAfterMoveIsMade) {
				String lag = StringUtils.defaultString(game
						.getHeader(PgnHeader.BlackLagMillis), "0");
				game.setHeader(PgnHeader.BlackLagMillis, ""
						+ (Long.parseLong(lag) + message.lagInMillis));
			} else {
				String lag = StringUtils.defaultString(game
						.getHeader(PgnHeader.WhiteLagMillis), "0");
				game.setHeader(PgnHeader.WhiteLagMillis, ""
						+ (Long.parseLong(lag) + message.lagInMillis));

			}
			result = true;
		}

		if (message.isClockTicking) {
			game.addState(Game.IS_CLOCK_TICKING_STATE);
		} else {
			game.clearState(Game.IS_CLOCK_TICKING_STATE);
		}
		return result;
	}

	/**
	 * Returns true if the game was adjusted for takebacks. False otherwise.
	 */
	public static boolean adjustToTakebacks(Game game, Style12Message message,
			Connector connector) {
		boolean result = false;

		// fics ----- game halfmove
		// 1 w b ---- 0 1
		// 2 w b ---- 2 3
		// 3 w b ---- 4 5
		// 4 w b ---- 6 7
		// 5 w b ---- 7 8
		int currentHalfMove = (message.fullMoveNumber - 1) * 2
				+ (message.isWhitesMoveAfterMoveIsMade ? 0 : 1);
		if (LOG.isDebugEnabled()) {
			LOG.debug("adjustToTakebacks calculatedHalfMove " + currentHalfMove
					+ " 12FulLMoveNumber " + message.fullMoveNumber
					+ " 12isWhitesMoveAfter "
					+ message.isWhitesMoveAfterMoveIsMade + " gameHalfMove "
					+ game.getHalfMoveCount());
		}

		if (currentHalfMove != game.getHalfMoveCount() + 1) {

			if (game.getHalfMoveCount() < currentHalfMove) {
				if (LOG.isDebugEnabled()) {
					LOG
							.debug("Didnt have all the moves needed for rollback. Resetting game and sending moves request.");
				}
				resetGame(game, message);
				connector.sendMessage("moves " + message.gameId, true,
						ChatType.MOVES);
				result = true;
			} else {

				while (game.getHalfMoveCount() > currentHalfMove) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Rolled back a move.");
					}
					if (game.getLastMove() != null) {
						game.rollback();
					} else {
						resetGame(game, message);
						connector.sendMessage("moves " + message.gameId, true,
								ChatType.MOVES);
						result = false;
						break;
					}
				}
				result = true;
			}

			// At the end of a game multiple <12> messages are sent.
			// The are also sent when a refresh is sent.
			game.setHeader(PgnHeader.WhiteRemainingMillis, ""
					+ message.whiteRemainingTimeMillis);
			game.setHeader(PgnHeader.BlackRemainingMillis, ""
					+ message.blackRemainingTimeMillis);
		}

		if (message.isClockTicking) {
			game.addState(Game.IS_CLOCK_TICKING_STATE);
		} else {
			game.clearState(Game.IS_CLOCK_TICKING_STATE);
		}
		return result;
	}

	public static ChessBoardController buildController(Game game,
			Connector connector) {
		return buildController(game, connector, false);
	}

	public static ChessBoardController buildController(Game game,
			Connector connector, boolean isBughouseOtherBoard) {
		ChessBoardController controller = null;

		if (game.isInState(Game.OBSERVING_STATE)
				|| game.isInState(Game.OBSERVING_EXAMINED_STATE)) {
			if (isBughouseOtherBoard) {
				if (((BughouseGame) game).getOtherBoard().isInState(
						Game.PLAYING_STATE)) {
					Game otherGame = ((BughouseGame) game).getOtherBoard();
					boolean isPartnerWhite = !StringUtils.equals(otherGame
							.getHeader(PgnHeader.White), connector
							.getUserName());
					controller = new BughouseSuggestController(game, connector,
							isPartnerWhite);
				} else {
					controller = new ObserveController(game, connector);
				}
			} else {
				controller = new ObserveController(game, connector);
			}

		} else if (game.isInState(Game.SETUP_STATE)) {
			controller = new SetupController(game, connector);
		} else if (game.isInState(Game.EXAMINING_STATE)) {
			controller = new ExamineController(game, connector);
		} else if (game.isInState(Game.PLAYING_STATE)) {
			controller = new PlayingController(game, connector);
		} else {
			// Used for sposition.
			controller = new InactiveController(game);
		}
		return controller;
	}

	/**
	 * Cleans up the message by ensuring only \n is used as a line terminator.
	 * \r\n and \r may be used depending on the operating system.
	 */
	public static String cleanupMessage(String message) {
		return StringUtils.remove(message, '\r');
	}

	/**
	 * Clears out all the games position state.
	 */
	public static void clearGamePosition(Game game) {
		game.clear();
	}

	public static Game createExaminedGame(
			Style12Message gameStateStyle12Message, MovesMessage movesMessage) {
		Game result = null;
		if (movesMessage.style12 == null) {
			result = GameFactory
					.createStartingPosition(identifierToGameType(movesMessage.gameType));
		} else {
			result = createGameFromVariant(
					identifierToGameType(movesMessage.gameType),
					gameStateStyle12Message, false);
			updatePosition(result, movesMessage.style12);
			if (result.getVariant() == Variant.fischerRandom) {
				((FischerRandomGame) result).initialPositionIsSet();
			} else if (result.getVariant() == Variant.fischerRandomBughouse) {
				((FischerRandomBughouseGame) result).initialPositionIsSet();
			} else if (result.getVariant() == Variant.fischerRandomCrazyhouse) {
				((FischerRandomCrazyhouseGame) result).initialPositionIsSet();
			}
			updateNonPositionFields(result, movesMessage.style12);
			result.setHeader(PgnHeader.FEN, result.toFen());
		}
		result.addState(Game.UPDATING_SAN_STATE);
		result.addState(Game.UPDATING_ECO_HEADERS_STATE);
		result.setId(movesMessage.gameId);

		for (int i = 0; i < movesMessage.moves.length; i++) {
			try {
				if (result.isInState(Game.DROPPABLE_STATE)) {
					result.setDropCount(WHITE, PAWN, 1);
					result.setDropCount(WHITE, QUEEN, 1);
					result.setDropCount(WHITE, ROOK, 1);
					result.setDropCount(WHITE, KNIGHT, 1);
					result.setDropCount(WHITE, BISHOP, 1);
					result.setDropCount(BLACK, PAWN, 1);
					result.setDropCount(BLACK, QUEEN, 1);
					result.setDropCount(BLACK, ROOK, 1);
					result.setDropCount(BLACK, KNIGHT, 1);
					result.setDropCount(BLACK, BISHOP, 1);
				}
				Move move = result.makeSanMove(movesMessage.moves[i]);
				move.addAnnotation(new TimeTakenForMove(
						movesMessage.timePerMove[i]));
			} catch (IllegalArgumentException iae) {
				LOG.error("Could not parse san", iae);
				Raptor.getInstance().onError("Error update game with moves",
						iae);
			}
		}
		updateNonPositionFields(result, gameStateStyle12Message);
		result.setHeader(PgnHeader.Date, PgnUtils.longToPgnDate(System
				.currentTimeMillis()));
		result.setHeader(PgnHeader.Round, "?");
		result.setHeader(PgnHeader.Site, "freechess.org");
		result.setHeader(PgnHeader.TimeControl, PgnUtils
				.timeIncMillisToTimeControl(0, 0));
		result.setHeader(PgnHeader.BlackRemainingMillis, "" + 0);
		result.setHeader(PgnHeader.WhiteRemainingMillis, "" + 0);
		result.setHeader(PgnHeader.WhiteClock, PgnUtils.timeToClock(0));
		result.setHeader(PgnHeader.BlackClock, PgnUtils.timeToClock(0));
		result.setHeader(PgnHeader.BlackElo, "");
		result.setHeader(PgnHeader.WhiteElo, "");
		result.setHeader(PgnHeader.Event, "Examining " + movesMessage.gameType
				+ " game");
		result.setHeader(PgnHeader.Variant, result.getVariant().toString());
		return result;
	}

	public static Game createGame(G1Message g1, Style12Message style12,
			boolean isBics) {
		Variant variant = IcsUtils.identifierToGameType(g1.gameTypeDescription);
		Game result = createGameFromVariant(variant, style12, isBics);
		result.setId(g1.gameId);
		result.addState(Game.UPDATING_SAN_STATE);
		result.addState(Game.UPDATING_ECO_HEADERS_STATE);
		result.setHeader(PgnHeader.Date, PgnUtils.longToPgnDate(System
				.currentTimeMillis()));
		result.setHeader(PgnHeader.Round, "?");
		result.setHeader(PgnHeader.Site, "freechess.org");
		result.setHeader(PgnHeader.TimeControl, PgnUtils
				.timeIncMillisToTimeControl(g1.initialWhiteTimeMillis,
						g1.initialWhiteIncMillis));
		result.setHeader(PgnHeader.BlackRemainingMillis, ""
				+ g1.initialBlackTimeMillis);
		result.setHeader(PgnHeader.WhiteRemainingMillis, ""
				+ g1.initialWhiteTimeMillis);
		result.setHeader(PgnHeader.WhiteClock, PgnUtils
				.timeToClock(g1.initialWhiteTimeMillis));
		result.setHeader(PgnHeader.BlackClock, PgnUtils
				.timeToClock(g1.initialBlackTimeMillis));
		result.setHeader(PgnHeader.BlackElo, g1.blackRating);
		result.setHeader(PgnHeader.WhiteElo, g1.whiteRating);
		result.setHeader(PgnHeader.Event, g1.initialWhiteTimeMillis / 60000
				+ " " + g1.initialWhiteIncMillis / 1000 + " "
				+ (!g1.isRated ? "unrated" : "rated") + " "
				+ (g1.isPrivate ? "private " : "") + g1.gameTypeDescription);

		return result;
	}

	public static Game createGame(Style12Message message, String entireMessage) {
		if (message.relation == Style12Message.EXAMINING_GAME_RELATION) {

			boolean isSetup = entireMessage.contains("Entering setup mode.\n");
			Game game = isSetup ? new SetupGame() : new ClassicGame();
			game.setId(message.gameId);
			game.addState(Game.UPDATING_SAN_STATE);
			game.addState(Game.UPDATING_ECO_HEADERS_STATE);
			game.setHeader(PgnHeader.Date, PgnUtils.longToPgnDate(System
					.currentTimeMillis()));
			game.setHeader(PgnHeader.Round, "?");
			game.setHeader(PgnHeader.Site, "freechess.org");
			game.setHeader(PgnHeader.TimeControl, PgnUtils
					.timeIncMillisToTimeControl(0, 0));
			game.setHeader(PgnHeader.BlackRemainingMillis, "" + 0);
			game.setHeader(PgnHeader.WhiteRemainingMillis, "" + 0);
			game.setHeader(PgnHeader.WhiteClock, PgnUtils.timeToClock(0));
			game.setHeader(PgnHeader.BlackClock, PgnUtils.timeToClock(0));
			game.setHeader(PgnHeader.BlackElo, "");
			game.setHeader(PgnHeader.WhiteElo, "");
			game.setHeader(PgnHeader.Event, isSetup ? "Setting Up Position"
					: "Examining Game");
			updateNonPositionFields(game, message);
			updatePosition(game, message);
			verifyLegal(game);
			return game;
		} else if (message.relation == Style12Message.ISOLATED_POSITION_RELATION) {
			Game game = new ClassicGame();
			game.setId(message.gameId);
			game.addState(Game.UPDATING_SAN_STATE);
			game.addState(Game.UPDATING_ECO_HEADERS_STATE);
			game.setHeader(PgnHeader.Date, PgnUtils.longToPgnDate(System
					.currentTimeMillis()));
			game.setHeader(PgnHeader.Round, "?");
			game.setHeader(PgnHeader.Site, "freechess.org");
			game.setHeader(PgnHeader.TimeControl, PgnUtils
					.timeIncMillisToTimeControl(0, 0));
			game.setHeader(PgnHeader.BlackRemainingMillis, "" + 0);
			game.setHeader(PgnHeader.WhiteRemainingMillis, "" + 0);
			game.setHeader(PgnHeader.WhiteClock, PgnUtils.timeToClock(0));
			game.setHeader(PgnHeader.BlackClock, PgnUtils.timeToClock(0));
			game.setHeader(PgnHeader.BlackElo, "");
			game.setHeader(PgnHeader.WhiteElo, "");
			game.setHeader(PgnHeader.Event, "Isolated Position");
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

	public static Game createGameFromVariant(Variant variant, Game existingGame) {
		Game result = null;
		switch (variant) {
		case classic:
			result = new ClassicGame();
			break;
		case wild:
			result = new ClassicGame();
			result.setHeader(PgnHeader.Variant, Variant.wild.name());
			break;
		case suicide:
			result = new SuicideGame();
			break;
		case losers:
			result = new LosersGame();
			break;
		case atomic:
			result = new AtomicGame();
			break;
		case crazyhouse:
			result = existingGame instanceof FischerRandomCrazyhouseGame ? new FischerRandomCrazyhouseGame()
					: new CrazyhouseGame();
			break;
		case bughouse:
			result = existingGame instanceof FischerRandomBughouseGame ? new FischerRandomBughouseGame()
					: new BughouseGame();
			break;
		case fischerRandom:
			result = new FischerRandomGame();
			break;
		default:
			LOG.error("Unsupported variant: " + variant);
			throw new IllegalStateException("Unsupported game type" + variant);
		}
		return result;
	}

	public static Game createGameFromVariant(Variant variant,
			Style12Message message, boolean isBicsMessage) {
		Game result = null;

		boolean isFrBugOrFrZh = false;

		if (isBicsMessage
				&& (variant == Variant.bughouse || variant == Variant.crazyhouse)) {
			isFrBugOrFrZh = message.position[0][0] != ROOK
					|| message.position[0][1] != KNIGHT
					|| message.position[0][2] != BISHOP
					|| message.position[0][3] != QUEEN
					|| message.position[0][4] != KING
					|| message.position[0][5] != BISHOP
					|| message.position[0][6] != KNIGHT
					|| message.position[0][7] != ROOK;
		}

		switch (variant) {
		case classic:
			result = new ClassicGame();
			break;
		case wild:
			result = new ClassicGame();
			result.setHeader(PgnHeader.Variant, Variant.wild.name());
			break;
		case suicide:
			result = new SuicideGame();
			break;
		case losers:
			result = new LosersGame();
			break;
		case atomic:
			result = new AtomicGame();
			break;
		case crazyhouse:
			result = isFrBugOrFrZh ? new FischerRandomCrazyhouseGame()
					: new CrazyhouseGame();
			break;
		case bughouse:
			result = isFrBugOrFrZh ? new FischerRandomBughouseGame()
					: new BughouseGame();
			break;
		case fischerRandom:
			result = new FischerRandomGame();
			break;
		default:
			LOG.error("Unsupported variant: " + variant);
			throw new IllegalStateException("Unsupported game type" + variant);
		}
		return result;
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
	}

	public static BuggerStatus getBuggserStatus(String status) {
		if (status.equals(":")) {
			return BuggerStatus.Closed;
		} else if (status.equals("^")) {
			return BuggerStatus.Playing;
		} else if (status.equals(".")) {
			return BuggerStatus.Idle;
		} else if (status.equals("~")) {
			return BuggerStatus.Simul;
		} else if (status.equals("#")) {
			return BuggerStatus.Examining;
		} else if (status.equals("&")) {
			return BuggerStatus.InTourney;
		} else {
			return BuggerStatus.Available;
		}
	}

	/**
	 * Returns the game type constant for the specified identifier.
	 * 
	 */
	public static Variant identifierToGameType(String identifier) {
		Variant result = null;

		if (identifier.indexOf(SUICIDE_IDENTIFIER) != -1) {
			result = Variant.suicide;
		} else if (identifier.indexOf(BUGHOUSE_IDENTIFIER) != -1) {
			result = Variant.bughouse;
		} else if (identifier.indexOf(CRAZYHOUSE_IDENTIFIER) != -1) {
			result = Variant.crazyhouse;
		} else if (identifier.indexOf(STANDARD_IDENTIFIER) != -1) {
			result = Variant.classic;
		} else if (identifier.indexOf(FISCHER_RANDOM_IDENTIFIER) != -1) {
			result = Variant.fischerRandom;
		} else if (identifier.indexOf(WILD_IDENTIFIER) != -1) {
			result = Variant.wild;
		} else if (identifier.indexOf(LIGHTNING_IDENTIFIER) != -1) {
			result = Variant.classic;
		} else if (identifier.indexOf(BLITZ_IDENTIFIER) != -1) {
			result = Variant.classic;
		} else if (identifier.indexOf(ATOMIC_IDENTIFIER) != -1) {
			result = Variant.atomic;
		} else if (identifier.indexOf(LOSERS_IDENTIFIER) != -1) {
			result = Variant.losers;
		} else if (identifier.indexOf(UNTIMED_IDENTIFIER) != -1) {
			result = Variant.classic;
		} else {
			throw new IllegalArgumentException("Unknown identifier "
					+ identifier
					+ " encountered. Please notify someone on the raptor team "
					+ "so they can implement this new game type.");
		}

		return result;
	}

	/**
	 * Removes the ICS channel wrapping around a specified channel word
	 * returning only the channel number.
	 * 
	 * Returns -1 if the specified word is not a channel.
	 */
	public static boolean isLikelyChannel(String word) {
		boolean result = false;
		if (word != null) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(word,
					CHANNEL_STRIP_CHARS, true);
			if (tok.hasMoreTokens()) {
				String current = tok.nextToken();
				try {
					int channel = Integer.parseInt(current);
					return channel >= 0 && channel <= 255;
				} catch (NumberFormatException nfe) {
					if (tok.hasMoreTokens()) {
						try {
							current = tok.nextToken();
							int channel = Integer.parseInt(current);
							return channel >= 0 && channel <= 255;
						} catch (NumberFormatException nfe2) {
							if (tok.hasMoreTokens()) {
								try {
									current = tok.nextToken();
									int channel = Integer.parseInt(current);
									return channel >= 0 && channel <= 255;
								} catch (NumberFormatException nfe3) {
									if (tok.hasMoreTokens()) {
										try {
											current = tok.nextToken();
											int channel = Integer
													.parseInt(current);
											return channel >= 0
													&& channel <= 255;
										} catch (NumberFormatException nfe4) {
										}
									}
								}
							}
						}
					}
				}

			}
		}
		return result;
	}

	public static boolean isLikelyGameId(String word) {
		boolean result = false;
		if (word != null) {
			try {
				int gameId = Integer.parseInt(stripWord(word));
				return gameId > 0 && gameId <= 5000;
			} catch (NumberFormatException nfe) {
				return false;
			}
		}
		return result;
	}

	/**
	 * Returns true if the specified word is probably a persons name.
	 */
	public static boolean isLikelyPerson(String word) {
		String strippedWord = stripWord(word);
		if (word != null && strippedWord.length() > 2) {
			boolean result = true;
			for (int i = 0; result && i < strippedWord.length(); i++) {
				result = VALID_PERSON_CHARS.indexOf(word.charAt(i)) != -1;
			}
			return result;
		} else {
			return false;
		}
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
			if (endIndex == -1) {
				break;
			}
			String maciejgWord = builder.substring(unicodePrefix + 3, endIndex);
			maciejgWord = StringUtils.replaceChars(maciejgWord, " \\\n", "")
					.toUpperCase();
			if (maciejgWord.length() <= 5) {
				try {
					int intValue = Integer.parseInt(maciejgWord, 16);
					String unicode = new String(new char[] { (char) intValue });
					builder.replace(unicodePrefix, endIndex + 1, unicode);
				} catch (NumberFormatException nfe) {
					unicodePrefix = endIndex + 1;
				}
			} else {
				unicodePrefix = endIndex + 1;
			}
		}
		return builder.toString();
	}

	/**
	 * Removes all line breaks and excessive spaces from the specified message.
	 * 
	 * @param msg
	 *            THe message to remove line breaks from.
	 * @return The message without any line breaks.
	 */
	public static String removeLineBreaks(String msg) {
		StringBuilder result = new StringBuilder(msg.length());
		RaptorStringTokenizer tok = new RaptorStringTokenizer(msg, "\n\\ ",
				true);
		if (tok.hasMoreTokens()) {
			result.append(tok.nextToken());
		}
		while (tok.hasMoreTokens()) {
			result.append(" " + tok.nextToken());
		}
		return result.toString();
	}

	public static void resetGame(Game game, Style12Message message) {
		IcsUtils.clearGamePosition(game);
		IcsUtils.updateNonPositionFields(game, message);
		IcsUtils.updatePosition(game, message);
		verifyLegal(game);
	}

	public static String stripChannel(String word) {
		String result = null;
		if (word != null) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(word,
					CHANNEL_STRIP_CHARS, true);
			if (tok.hasMoreTokens()) {
				String current = tok.nextToken();
				try {
					int channel = Integer.parseInt(current);
					if (channel >= 0 && channel <= 255) {
						return "" + channel;
					}
				} catch (NumberFormatException nfe) {
					if (tok.hasMoreTokens()) {
						try {
							current = tok.nextToken();
							int channel = Integer.parseInt(current);
							if (channel >= 0 && channel <= 255) {
								return "" + channel;
							}
						} catch (NumberFormatException nfe2) {
							if (tok.hasMoreTokens()) {
								try {
									current = tok.nextToken();
									int channel = Integer.parseInt(current);
									if (channel >= 0 && channel <= 255) {
										return "" + channel;
									}
								} catch (NumberFormatException nfe3) {
									if (tok.hasMoreTokens()) {
										try {
											current = tok.nextToken();
											int channel = Integer
													.parseInt(current);
											if (channel >= 0 && channel <= 255) {
												return "" + channel;
											}
										} catch (NumberFormatException nfe4) {
										}
									}
								}
							}
						}
					}
				}

			}
		}
		return result;
	}

	public static String stripGameId(String gameId) {
		return gameId;
	}

	public static String stripTitles(String playerName) {
		StringTokenizer stringtokenizer = new StringTokenizer(playerName,
				"()~!@#$%^&*_+|}{';/.,:[]");
		if (stringtokenizer.hasMoreTokens()) {
			return stringtokenizer.nextToken();
		} else {
			return playerName;
		}
	}

	/**
	 * Returns the word with all characters in: ()~!@?#$%^&*_+|}{'\";/?<>.,
	 * :[]1234567890\t\r\n removed.
	 */
	public static String stripWord(String word) {
		if (word != null) {
			RaptorStringTokenizer stringtokenizer = new RaptorStringTokenizer(
					word, STRIP_CHARS, true);
			if (stringtokenizer.hasMoreTokens()) {
				return stringtokenizer.nextToken();
			} else {
				return word;
			}
		}
		return null;
	}

	/**
	 * Converts a time in: (0:00.000) mmm:ss.MMM format into a long value
	 * representing milliseconds.
	 * 
	 * @param timeString
	 *            The time string.
	 * @return The result.
	 */
	public static final long timeToLong(String timeString) {
		RaptorStringTokenizer tok = new RaptorStringTokenizer(timeString,
				"(:.)", true);
		long minutes = Integer.parseInt(tok.nextToken());
		long seconds = Integer.parseInt(tok.nextToken());
		long millis = Integer.parseInt(tok.nextToken());
		return minutes * 1000 * 60 + seconds * 1000 + millis;
	}

	/**
	 * Updates the moves that are missing in the game to the ones in the move
	 * message.
	 */
	public static void updateGamesMoves(Game game, MovesMessage message,
			boolean isBics) {
		int halfMoveCountGameStartedOn = game.getHalfMoveCount()
				- game.getMoveList().getSize();

		if (halfMoveCountGameStartedOn != 0) {
			Game gameClone = null;
			if (message.style12 == null) {
				gameClone = GameFactory.createStartingPosition(game
						.getVariant());
			} else {
				gameClone = createGameFromVariant(game.getVariant(), game);
				updatePosition(gameClone, message.style12);
				if (gameClone.getVariant() == Variant.fischerRandom) {
					((FischerRandomGame) gameClone).initialPositionIsSet();
				} else if (gameClone.getVariant() == Variant.fischerRandomBughouse) {
					((FischerRandomBughouseGame) gameClone)
							.initialPositionIsSet();
				} else if (gameClone.getVariant() == Variant.fischerRandomCrazyhouse) {
					((FischerRandomCrazyhouseGame) gameClone)
							.initialPositionIsSet();
				}
				updateNonPositionFields(gameClone, message.style12);
				game.setHeader(PgnHeader.FEN, gameClone.toFen());
			}
			gameClone.addState(Game.UPDATING_SAN_STATE);
			gameClone.addState(Game.UPDATING_ECO_HEADERS_STATE);

			for (int i = 0; i < halfMoveCountGameStartedOn; i++) {
				try {
					if (gameClone.isInState(Game.DROPPABLE_STATE)) {
						gameClone.setDropCount(WHITE, PAWN, 1);
						gameClone.setDropCount(WHITE, QUEEN, 1);
						gameClone.setDropCount(WHITE, ROOK, 1);
						gameClone.setDropCount(WHITE, KNIGHT, 1);
						gameClone.setDropCount(WHITE, BISHOP, 1);
						gameClone.setDropCount(BLACK, PAWN, 1);
						gameClone.setDropCount(BLACK, QUEEN, 1);
						gameClone.setDropCount(BLACK, ROOK, 1);
						gameClone.setDropCount(BLACK, KNIGHT, 1);
						gameClone.setDropCount(BLACK, BISHOP, 1);
					}
					Move move = gameClone.makeSanMove(message.moves[i]);
					move.addAnnotation(new TimeTakenForMove(
							message.timePerMove[i]));
				} catch (IllegalArgumentException iae) {
					LOG.error("Could not parse san", iae);
					Raptor.getInstance().onError(
							"Error update game with moves", iae);
				}
			}

			Move[] moves = gameClone.getMoveList().asArray();
			game.getMoveList().prepend(moves);
			game.setInitialEpSquare(gameClone.getInitialEpSquare());
			if (StringUtils.isBlank(game.getHeader(PgnHeader.ECO))
					&& StringUtils.isNotBlank(gameClone
							.getHeader(PgnHeader.ECO))) {
				game.setHeader(PgnHeader.ECO, gameClone
						.getHeader(PgnHeader.ECO));
			}
			if (StringUtils.isBlank(game.getHeader(PgnHeader.Opening))
					&& StringUtils.isNotBlank(gameClone
							.getHeader(PgnHeader.Opening))) {
				game.setHeader(PgnHeader.Opening, gameClone
						.getHeader(PgnHeader.Opening));
			}
		}
	}

	/**
	 * Handles updating everything but the position related fields in the game.
	 * i.e. the bitboards,pieces, etc.
	 */
	public static void updateNonPositionFields(Game game, Style12Message message) {
		switch (message.relation) {
		case Style12Message.EXAMINING_GAME_RELATION:
			game.addState(Game.EXAMINING_STATE);
			break;
		case Style12Message.ISOLATED_POSITION_RELATION:
			break;
		case Style12Message.OBSERVING_EXAMINED_GAME_RELATION:
			game.addState(Game.OBSERVING_EXAMINED_STATE);
			break;
		case Style12Message.OBSERVING_GAME_RELATION:
			game.addState(Game.OBSERVING_STATE);
			break;
		case Style12Message.PLAYING_MY_MOVE_RELATION:
		case Style12Message.PLAYING_OPPONENTS_MOVE_RELATION:
			game.addState(Game.PLAYING_STATE);
			break;
		}

		if (message.isClockTicking) {
			game.addState(Game.IS_CLOCK_TICKING_STATE);
		} else {
			game.clearState(Game.IS_CLOCK_TICKING_STATE);
		}

		game.addState(Game.ACTIVE_STATE);

		game
				.setHeader(PgnHeader.Black, IcsUtils
						.stripTitles(message.blackName));
		game
				.setHeader(PgnHeader.White, IcsUtils
						.stripTitles(message.whiteName));

		game.setHeader(PgnHeader.WhiteRemainingMillis, ""
				+ message.whiteRemainingTimeMillis);
		game.setHeader(PgnHeader.BlackRemainingMillis, ""
				+ message.blackRemainingTimeMillis);

		game
				.setColorToMove(message.isWhitesMoveAfterMoveIsMade ? WHITE
						: BLACK);

		game.setCastling(WHITE, message.canWhiteCastleKSide
				&& message.canWhiteCastleQSide ? CASTLE_BOTH
				: message.canWhiteCastleKSide ? CASTLE_SHORT
						: message.canWhiteCastleQSide ? CASTLE_LONG
								: CASTLE_NONE);
		game.setCastling(BLACK, message.canBlackCastleKSide
				&& message.canBlackCastleQSide ? CASTLE_BOTH
				: message.canBlackCastleKSide ? CASTLE_SHORT
						: message.canBlackCastleQSide ? CASTLE_LONG
								: CASTLE_NONE);

		if (message.doublePawnPushFile == -1) {
			game.setEpSquare(EMPTY_SQUARE);
			game.setInitialEpSquare(EMPTY_SQUARE);
		} else {
			int doublePawnPushSquare = GameUtils.getSquare(
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
					int square = GameUtils.getSquare(i, j);
					int pieceColor = ChessBoardUtils
							.isWhitePiece(style12.position[i][j]) ? WHITE
							: BLACK;
					int piece = ChessBoardUtils
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

		game.setZobristPositionHash(ZobristUtils.zobristHashPositionOnly(game));
		game.setZobristGameHash(game.getZobristPositionHash()
				^ ZobristUtils.zobrist(game.getColorToMove(), game
						.getEpSquare(), game.getCastling(WHITE), game
						.getCastling(BLACK)));

		if (game.isInState(Game.SETUP_STATE)) {
			game.setPieceCount(WHITE, PAWN, 1);
			game.setPieceCount(WHITE, KNIGHT, 1);
			game.setPieceCount(WHITE, BISHOP, 1);
			game.setPieceCount(WHITE, ROOK, 1);
			game.setPieceCount(WHITE, QUEEN, 1);
			game.setPieceCount(WHITE, KING, 1);
			game.setPieceCount(BLACK, PAWN, 1);
			game.setPieceCount(BLACK, KNIGHT, 1);
			game.setPieceCount(BLACK, BISHOP, 1);
			game.setPieceCount(BLACK, ROOK, 1);
			game.setPieceCount(BLACK, QUEEN, 1);
			game.setPieceCount(BLACK, KING, 1);
		}
	}

	public static void verifyLegal(Game game) {
		if (!game.isLegalPosition()) {
			throw new IllegalStateException("Position is not legal: "
					+ game.toString());
		}
	}

	protected void replaceMaciejgUnicodeWithUnicode() {

	}
}
