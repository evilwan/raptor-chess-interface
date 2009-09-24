package raptor.connector.fics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.chat.ChatEvent;
import raptor.chat.ChatTypes;
import raptor.connector.fics.chat.CShoutEventParser;
import raptor.connector.fics.chat.ChallengeEventParser;
import raptor.connector.fics.chat.ChannelTellEventParser;
import raptor.connector.fics.chat.ChatEventParser;
import raptor.connector.fics.chat.FollowingEventParser;
import raptor.connector.fics.chat.KibitzEventParser;
import raptor.connector.fics.chat.PartnerTellEventParser;
import raptor.connector.fics.chat.PartnershipCreatedEventParser;
import raptor.connector.fics.chat.PartnershipEndedEventParser;
import raptor.connector.fics.chat.ShoutEventParser;
import raptor.connector.fics.chat.TellEventParser;
import raptor.connector.fics.chat.WhisperEventParser;
import raptor.connector.fics.game.B1Parser;
import raptor.connector.fics.game.G1Parser;
import raptor.connector.fics.game.GameEndParser;
import raptor.connector.fics.game.IllegalMoveParser;
import raptor.connector.fics.game.RemovingObsGameParser;
import raptor.connector.fics.game.Style12Parser;
import raptor.connector.fics.game.message.B1Message;
import raptor.connector.fics.game.message.G1Message;
import raptor.connector.fics.game.message.GameEndMessage;
import raptor.connector.fics.game.message.IllegalMoveMessage;
import raptor.connector.fics.game.message.RemovingObsGameMessage;
import raptor.connector.fics.game.message.Style12Message;
import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.util.GameUtils;
import raptor.game.util.ZobristHash;
import raptor.service.GameService;
import raptor.util.RaptorStringTokenizer;

public class FicsParser implements GameConstants {
	public static final int MAX_GAME_MESSAGE = 1000;
	private static final Log LOG = LogFactory.getLog(FicsParser.class);

	protected Style12Parser style12Parser;
	protected G1Parser g1Parser;
	protected B1Parser b1Parser;
	protected GameEndParser gameEndParser;
	protected IllegalMoveParser illegalMoveParser;
	protected RemovingObsGameParser removingObsGameParser;

	protected List<ChatEventParser> nonGameEventParsers = new ArrayList<ChatEventParser>(
			30);

	/**
	 * A map keyed by game id. Used to temporarily store G1 messages until the
	 * first style 12 message comes along. A new game requires a 12 message as
	 * well as a G1.
	 */
	protected Map<String, G1Message> unprocessedG1Messages = new HashMap<String, G1Message>();

	public FicsParser() {
		style12Parser = new Style12Parser();
		gameEndParser = new GameEndParser();
		b1Parser = new B1Parser();
		g1Parser = new G1Parser();
		illegalMoveParser = new IllegalMoveParser();
		removingObsGameParser = new RemovingObsGameParser();

		// handle user tell types of events first so others can't be spoofed
		// nonGameEventParsers.add(new BugWhoGEventParser(icsId));
		// nonGameEventParsers.add(new BugWhoPEventParser(icsId));
		// nonGameEventParsers.add(new BugWhoUEventParser(icsId));
		// nonGameEventParsers.add(new SoughtEventParser());
		nonGameEventParsers.add(new ChannelTellEventParser());
		nonGameEventParsers.add(new CShoutEventParser());
		nonGameEventParsers.add(new ShoutEventParser());
		nonGameEventParsers.add(new KibitzEventParser());
		nonGameEventParsers.add(new PartnerTellEventParser());
		nonGameEventParsers.add(new TellEventParser());
		nonGameEventParsers.add(new WhisperEventParser());

		// Non tell types of events.
		// nonGameEventParsers.add(new MoveListParser(icsId));
		nonGameEventParsers.add(new ChallengeEventParser());
		nonGameEventParsers.add(new PartnershipCreatedEventParser());
		nonGameEventParsers.add(new PartnershipEndedEventParser());
		nonGameEventParsers.add(new FollowingEventParser());
	}

	public ChatEvent[] parse(String inboundEvent) {
		List<ChatEvent> events = new ArrayList<ChatEvent>(5);
		for (ChatEventParser parser : nonGameEventParsers) {
			ChatEvent event = parser.parse(inboundEvent);
			if (event != null) {
				events.add(event);
			}
		}
		if (events.isEmpty()) {
			events.add(new ChatEvent(null, ChatTypes.UNKNOWN, inboundEvent));
		}
		return events.toArray(new ChatEvent[0]);
	}

	/**
	 * Parses and removes all of the game events from inboundEvent. Adjusts the
	 * games in service. Returns a String with the game events removed.
	 */
	public String parseOutAndProcessGameEvents(GameService service,
			String inboundEvent) {
		if (inboundEvent.length() > MAX_GAME_MESSAGE) {
			return inboundEvent;
		} else {
			StringBuilder result = new StringBuilder(inboundEvent.length());
			RaptorStringTokenizer tok = new RaptorStringTokenizer(inboundEvent,
					"\n");

			while (tok.hasMoreTokens()) {
				String line = tok.nextToken();
				System.err.println("Processing raw line: " + line);

				G1Message g1Message = g1Parser.parse(line);
				if (g1Message != null) {
					process(g1Message, service);
					continue;
				}

				Style12Message style12Message = style12Parser.parse(line);
				if (style12Message != null) {
					process(style12Message, service);
					continue;
				}

				B1Message b1Message = b1Parser.parse(line);
				if (b1Message != null) {
					process(b1Message, service);
					continue;
				}

				GameEndMessage gameEndMessage = gameEndParser.parse(line);
				if (gameEndMessage != null) {
					process(gameEndMessage, service);
					result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
					continue;
				}

				IllegalMoveMessage illegalMoveMessage = illegalMoveParser
						.parse(line);
				if (illegalMoveMessage != null) {
					process(illegalMoveMessage, service);
					result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
					continue;
				}

				RemovingObsGameMessage removingObsGameMessage = removingObsGameParser
						.parse(line);
				if (removingObsGameMessage != null) {
					process(removingObsGameMessage, service);
					result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
					continue;
				}
				result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
			}
			return result.toString();
		}
	}

	public void process(G1Message message, GameService service) {
		unprocessedG1Messages.put(message.gameId, message);
		LOG.debug("Processing g1: " + message);
	}

	public void process(Style12Message message, GameService service) {
		LOG.debug("Processing style 12: " + message);
		long startTime = System.currentTimeMillis();

		Game game = service.getGame(message.gameId);
		if (game != null) {

			if ((!message.isWhitesMoveAfterMoveIsMade && game.getColorToMove() != WHITE)
					|| (message.isWhitesMoveAfterMoveIsMade && game
							.getColorToMove() == WHITE)) {
				// At the end of a game multiple <12> messages are sent.
				// The are also sent when a refresh is sent.
				game
				.setWhiteRemainingeTimeMillis(message.whiteRemainingTimeMillis);
		        game
				.setBlackRemainingTimeMillis(message.blackRemainingTimeMillis);
		        service.fireGameStateChanged(message.gameId,false);

			} else {
				if (message.san.equals("none")) {
					LOG.warn("Received a none for san in a style 12 event.");
				} else {
					game.makeSanMove(message.san);
				}

				game
						.setWhiteRemainingeTimeMillis(message.whiteRemainingTimeMillis);
				game
						.setBlackRemainingTimeMillis(message.blackRemainingTimeMillis);

				if (message.isWhitesMoveAfterMoveIsMade) {
					game.setBlackLagMillis(game.getBlackLagMillis()
							+ message.lagInMillis);
				} else {
					game.setWhiteLagMillis(game.getWhiteLagMillis()
							+ message.lagInMillis);
				}

				service.fireGameStateChanged(message.gameId,true);
			}
		} else {
			G1Message g1Message = unprocessedG1Messages.get(message.gameId);
			if (g1Message == null) {
				LOG
						.error("Encountered a style 12 message which was not in the GameService and did not have an unprocessed G1 message.");
				return;
			} else {
				unprocessedG1Messages.remove(message.gameId);
				int gameType = FicsUtils
						.identifierToGameType(g1Message.gameTypeDescription);
				switch (gameType) {
				case Game.BLITZ:
					game = new Game();
					game.setType(Game.BLITZ);
					break;
				case Game.STANDARD:
					game = new Game();
					game.setType(Game.STANDARD);
					break;
				case Game.LIGHTNING:
					game = new Game();
					game.setType(Game.LIGHTNING);
					break;
				default:
					LOG.error("Uhandled game type "
							+ g1Message.gameTypeDescription);
					return;
				}
				game.setId(message.gameId);
				game.setGameDescription(g1Message.gameTypeDescription);
				game.setSettingMoveSan(true);
				game.setStartTime(System.currentTimeMillis());
				game.setSite("freechess.org");

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
					game.setState(Game.OBSERVING_STATE
							| Game.IS_CLOCK_TICKING_STATE);
					break;
				case Style12Message.PLAYING_MY_MOVE_RELATION:
				case Style12Message.PLAYING_OPPONENTS_MOVE_RELATION:
					game.setState(Game.PLAYING_STATE
							| Game.IS_CLOCK_TICKING_STATE);
					;
					break;
				}
				game.setState(game.getState() | Game.ACTIVE_STATE);

				if (message.relation == Style12Message.EXAMINING_GAME_RELATION) {
					game.setState(Game.EXAMINING_STATE);
				} else if (message.relation == Style12Message.ISOLATED_POSITION_RELATION) {
					game.setState(Game.EXAMINING_STATE);
				}

				game
						.setInitialWhiteTimeMillis(g1Message.initialWhiteTimeMillis);
				game
						.setInitialBlackTimeMillis(g1Message.initialBlackTimeMillis);
				game.setInitialWhiteIncMillis(g1Message.initialWhiteIncMillis);
				game.setInitialBlackIncMillis(g1Message.initialBlackIncMillis);

				game.setBlackName(message.blackName);
				game.setBlackRating(g1Message.blackRating);

				game.setWhiteName(message.whiteName);
				game.setWhiteRating(g1Message.whiteRating);

				game
						.setWhiteRemainingeTimeMillis(message.whiteRemainingTimeMillis);
				game
						.setBlackRemainingTimeMillis(message.blackRemainingTimeMillis);

				FicsUtils.updateGamePosition(game, message);

				game.setColorToMove(message.isWhitesMoveAfterMoveIsMade ? WHITE
						: BLACK);

				game
						.setCastling(
								WHITE,
								message.canWhiteCastleKSide
										&& message.canWhiteCastleQSide ? CASTLE_BOTH
										: message.canWhiteCastleKSide ? CASTLE_KINGSIDE
												: message.canWhiteCastleQSide ? CASTLE_QUEENSIDE
														: CASTLE_NONE);
				game
						.setCastling(
								BLACK,
								message.canBlackCastleKSide
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

				game
						.setFiftyMoveCount(message.numberOfMovesSinceLastIrreversible);

				int fullMoveCount = message.fullMoveNumber;
				game
						.setHalfMoveCount(game.getColorToMove() == BLACK ? fullMoveCount * 2 - 1
								: fullMoveCount * 2 - 2);

				game.setEmptyBB(~game.getOccupiedBB());
				game.setNotColorToMoveBB(~game
						.getColorBB(game.getColorToMove()));

				game.setZobristPositionHash(ZobristHash
						.zobristHashPositionOnly(game));
				game.setZobristGameHash(game.getZobristPositionHash()
						^ ZobristHash.zobrist(game.getColorToMove(), game
								.getEpSquare(), game.getCastling(WHITE), game
								.getCastling(BLACK)));

				game.incrementRepCount();

				game.setEvent(game.getInitialWhiteTimeMillis() / 60000 + " "
						+ game.getInitialWhiteIncMillis() / 1000 + " "
						+ (!g1Message.isRated ? "unrated" : "rated") + " "
						+ game.getGameDescription());

				if (!game.isLegalPosition()) {
					LOG.warn("Position is not legal: " + game.toString());
				}

				service.addGame(game);
				service.fireGameCreated(game.getId());
			}
		}

		LOG.debug("Processed style 12: " + message + " in "
				+ (System.currentTimeMillis() - startTime));
	}

	public void process(B1Message message, GameService service) {
		LOG.debug("Processing b1: " + message
				+ " <Ignoiring not yet implemented>");
	}

	public void process(IllegalMoveMessage message, GameService service) {
		LOG.debug("Processing illegal move: " + message
				+ " <Ignoiring not yet implemented>");
	}

	public void process(GameEndMessage message, GameService service) {
		Game game = service.getGame(message.gameId);
		if (game == null) {
			LOG.error("Received game end for a game not in the GameService. "
					+ message);
		} else {
			switch (message.type) {
			case GameEndMessage.ABORTED:
			case GameEndMessage.ADJOURNED:
			case GameEndMessage.UNDETERMINED:
				game.setResult(Game.UNDETERMINED_RESULT);
				break;
			case GameEndMessage.BLACK_WON:
				game.setResult(Game.BLACK_WON_RESULT);
				break;
			case GameEndMessage.WHITE_WON:
				game.setResult(Game.WHTIE_WON_RESULT);
				break;
			case GameEndMessage.DRAW:
				game.setResult(Game.DRAW_RESULT);
				break;
			default:
				LOG.error("Undetermined game end type. " + message);
				break;
			}
			game.setResultDescription(message.description);
			game.clearState(Game.ACTIVE_STATE | Game.IS_CLOCK_TICKING_STATE);
			game.addState(Game.INACTIVE_STATE);
			service.fireGameInactive(game.getId());
			service.removeGame(game);
		}
		LOG.debug("Processed game end: " + message);
	}

	public void process(RemovingObsGameMessage message, GameService service) {
		Game game = service.getGame(message.gameId);
		if (game == null) {
			LOG
					.error("Received removing obs game message for a game not in the GameService. "
							+ message);
		} else {
			game.setResultDescription("Interrupted by unobserve");
			game.setResult(Game.UNDETERMINED_RESULT);
			game.clearState(Game.ACTIVE_STATE | Game.IS_CLOCK_TICKING_STATE);
			game.setState(game.getState() | Game.INACTIVE_STATE);
			service.fireGameInactive(game.getId());
			service.removeGame(game);
		}
		LOG.debug("Processed removing obs game: " + message);
	}

}
