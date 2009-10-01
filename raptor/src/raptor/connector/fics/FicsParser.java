package raptor.connector.fics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.fics.game.B1Parser;
import raptor.connector.fics.game.G1Parser;
import raptor.connector.fics.game.GameEndParser;
import raptor.connector.fics.game.IllegalMoveParser;
import raptor.connector.fics.game.NoLongerExaminingGameParser;
import raptor.connector.fics.game.RemovingObsGameParser;
import raptor.connector.fics.game.Style12Parser;
import raptor.connector.fics.game.TakebackParser;
import raptor.connector.fics.game.message.B1Message;
import raptor.connector.fics.game.message.G1Message;
import raptor.connector.fics.game.message.GameEndMessage;
import raptor.connector.fics.game.message.IllegalMoveMessage;
import raptor.connector.fics.game.message.NoLongerExaminingGameMessage;
import raptor.connector.fics.game.message.RemovingObsGameMessage;
import raptor.connector.fics.game.message.Style12Message;
import raptor.connector.ics.IcsParser;
import raptor.connector.ics.IcsUtils;
import raptor.connector.ics.chat.CShoutEventParser;
import raptor.connector.ics.chat.ChallengeEventParser;
import raptor.connector.ics.chat.ChannelTellEventParser;
import raptor.connector.ics.chat.ChatEventParser;
import raptor.connector.ics.chat.FollowingEventParser;
import raptor.connector.ics.chat.KibitzEventParser;
import raptor.connector.ics.chat.PartnerTellEventParser;
import raptor.connector.ics.chat.PartnershipCreatedEventParser;
import raptor.connector.ics.chat.PartnershipEndedEventParser;
import raptor.connector.ics.chat.ShoutEventParser;
import raptor.connector.ics.chat.TellEventParser;
import raptor.connector.ics.chat.WhisperEventParser;
import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.Game.Result;
import raptor.service.GameService;
import raptor.util.RaptorStringTokenizer;

public class FicsParser implements IcsParser, GameConstants {
	public static final int MAX_GAME_MESSAGE = 1000;
	private static final Log LOG = LogFactory.getLog(FicsParser.class);

	protected Style12Parser style12Parser;
	protected G1Parser g1Parser;
	protected B1Parser b1Parser;
	protected GameEndParser gameEndParser;
	protected IllegalMoveParser illegalMoveParser;
	protected RemovingObsGameParser removingObsGameParser;
	protected TakebackParser takebackParser;
	protected NoLongerExaminingGameParser noLongerExaminingParser;

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
		takebackParser = new TakebackParser();
		noLongerExaminingParser = new NoLongerExaminingGameParser();

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
			events.add(new ChatEvent(null, ChatType.UNKNOWN, inboundEvent));
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
			if (LOG.isDebugEnabled()) {
				LOG.debug("Raw message in: " + inboundEvent);
			}
			StringBuilder result = new StringBuilder(inboundEvent.length());
			RaptorStringTokenizer tok = new RaptorStringTokenizer(inboundEvent,
					"\n");

			while (tok.hasMoreTokens()) {
				String line = tok.nextToken();
				if (LOG.isDebugEnabled()) {
					LOG.debug("Processing raw line: " + line);
				}

				G1Message g1Message = g1Parser.parse(line);
				if (g1Message != null) {
					process(g1Message, service);
					continue;
				}

				Style12Message style12Message = style12Parser.parse(line);
				if (style12Message != null) {
					process(style12Message, service, inboundEvent);
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

				NoLongerExaminingGameMessage noLonerExaminingGameMessage = noLongerExaminingParser
						.parse(line);
				if (noLonerExaminingGameMessage != null) {
					process(noLonerExaminingGameMessage, service);
					result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
					continue;
				}

				takebackParser.parse(line);

				result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
			}
			return result.toString();
		}
	}

	protected void process(B1Message message, GameService service) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Processing b1: " + message);
		}
		Game game = service.getGame(message.gameId);
		if (game == null) {
			LOG.error("Received B1 for a game not in the GameService. "
					+ message);
		} else {
			for (int i = 1; i < message.whiteHoldings.length; i++) {
				game.setDropCount(WHITE, i, message.whiteHoldings[i]);
			}
			for (int i = 1; i < message.blackHoldings.length; i++) {
				game.setDropCount(BLACK, i, message.blackHoldings[i]);
			}
			service.fireGameStateChanged(message.gameId, false);
		}
	}

	protected void process(G1Message message, GameService service) {
		unprocessedG1Messages.put(message.gameId, message);
	}

	protected void process(GameEndMessage message, GameService service) {
		Game game = service.getGame(message.gameId);
		if (game == null) {
			LOG.error("Received game end for a game not in the GameService. "
					+ message);
		} else {
			switch (message.type) {
			case GameEndMessage.ABORTED:
			case GameEndMessage.ADJOURNED:
			case GameEndMessage.UNDETERMINED:
				game.setResult(Result.UNDETERMINED);
				break;
			case GameEndMessage.BLACK_WON:
				game.setResult(Result.BLACK_WON);
				break;
			case GameEndMessage.WHITE_WON:
				game.setResult(Result.WHITE_WON);
				break;
			case GameEndMessage.DRAW:
				game.setResult(Result.DRAW);
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
			takebackParser.clearTakebackMessages(game.getId());
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Processed game end: " + message);
		}
	}

	protected void process(IllegalMoveMessage message, GameService service) {
		// Except for simuls you can only place one game at a time.
		// For now ignore simuls and just send this to the first active game
		// found in the game service.
		Game[] allActive = service.getAllActiveGames();
		for (Game game : allActive) {
			service.fireIllegalMove(game.getId(), message.move);
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Processed illegal move: " + message);
		}
	}

	protected void process(NoLongerExaminingGameMessage message,
			GameService service) {
		Game game = service.getGame(message.gameId);
		if (game == null) {
			LOG
					.error("Received no longer examining game message for a game not in the GameService. "
							+ message);
		} else {
			game.setResultDescription("Interrupted by uexamine.");
			game.setResult(Result.UNDETERMINED);
			game.clearState(Game.ACTIVE_STATE | Game.IS_CLOCK_TICKING_STATE);
			game.addState(Game.INACTIVE_STATE);
			service.fireGameInactive(game.getId());
			service.removeGame(game);
			takebackParser.clearTakebackMessages(game.getId());
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Processed no longer examining game message: " + message);
		}
	}

	protected void process(RemovingObsGameMessage message, GameService service) {
		Game game = service.getGame(message.gameId);
		if (game == null) {
			LOG
					.error("Received removing obs game message for a game not in the GameService. "
							+ message);
		} else {
			game.setResultDescription("Interrupted by unobserve");
			game.setResult(Result.UNDETERMINED);
			game.clearState(Game.ACTIVE_STATE | Game.IS_CLOCK_TICKING_STATE);
			game.addState(Game.INACTIVE_STATE);
			service.fireGameInactive(game.getId());
			service.removeGame(game);
			takebackParser.clearTakebackMessages(game.getId());
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Processed removing obs game: " + message);
		}
	}

	protected void process(Style12Message message, GameService service,
			String entireMessage) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Processing style 12: " + message);
		}
		long startTime = System.currentTimeMillis();

		Game game = service.getGame(message.gameId);
		if (game != null) {
			if (game.isInState(Game.EXAMINING_STATE)
					|| game.isInState(Game.SETUP_STATE)) {
				// Examined/BSetup moves flow through here.
				if (LOG.isDebugEnabled()) {
					LOG.debug("Processing bsetup or examine position move.");
				}

				if (entireMessage
						.contains("Game is validated - entering examine mode.\n")) {

					// Games changing from Setup to Examine mode flow through
					// here.
					game.clearState(Game.SETUP_STATE);
					game.clearState(Game.DROPPABLE_STATE);
					IcsUtils.resetGame(game, message);
					service.fireSetupGameBecameExamined(message.gameId);
				} else {
					// Clear out the game and start over.
					// There is no telling what happened
					// in a setup or examine game.
					IcsUtils.resetGame(game, message);
					service.fireGameStateChanged(message.gameId, true);
				}
			} else {
				// Playing/Obsing moves flow through here.
				if (LOG.isDebugEnabled()) {
					LOG.debug("Processing obs/playing position move.");
				}

				// Takebacks may have effected the state of the game so first
				// adjsut to those.
				if (!IcsUtils.adjustToTakebacks(game, message, takebackParser)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Making move in obs/playing position.");
					}
					// Now add the move to the game.
					// Game Ends and Refreshes dont involve adding a move.
					if (IcsUtils.addCurrentMove(game, message)) {
						if (LOG.isDebugEnabled()) {
							LOG
									.debug("Position was a move firing state changed.");
						}
						service.fireGameStateChanged(message.gameId, true);
					} else {
						if (LOG.isDebugEnabled()) {
							LOG
									.debug("Position was not a move firing state changed.");
						}
						service.fireGameStateChanged(message.gameId, false);
					}
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Adjusted for takebacks.");
					}
					service.fireGameStateChanged(message.gameId, true);
				}
			}
		} else {
			G1Message g1Message = unprocessedG1Messages.get(message.gameId);
			if (g1Message == null) {
				// Examined/Bsetup game starts flow through here
				LOG.debug("Processing new ex or bsetup game.");
				game = IcsUtils.createGame(message, entireMessage);
				service.addGame(game);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Firing game created.");
				}
				service.fireGameCreated(game.getId());
			} else {
				// Observed/Playing games starts flow through here
				if (LOG.isDebugEnabled()) {
					LOG.debug("Processing new obs/playing game.");
				}
				unprocessedG1Messages.remove(message.gameId);

				game = IcsUtils.createGame(g1Message);
				IcsUtils.updateNonPositionFields(game, message);
				IcsUtils.updatePosition(game, message);
				IcsUtils.verifyLegal(game);

				service.addGame(game);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Firing game created.");
				}
				service.fireGameCreated(game.getId());
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Processed style 12: " + message + " in "
					+ (System.currentTimeMillis() - startTime));
		}
	}

}
