/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import raptor.Raptor;
import raptor.chat.Bugger;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.chat.Partnership;
import raptor.chat.Seek;
import raptor.chess.BughouseGame;
import raptor.chess.FischerRandomBughouseGame;
import raptor.chess.FischerRandomCrazyhouseGame;
import raptor.chess.FischerRandomGame;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.Result;
import raptor.chess.SetupGame;
import raptor.chess.Variant;
import raptor.chess.WildGame;
import raptor.chess.pgn.PgnHeader;
import raptor.connector.Connector;
import raptor.connector.ics.bughouse.BugWhoGParser;
import raptor.connector.ics.bughouse.BugWhoPParser;
import raptor.connector.ics.bughouse.BugWhoUParser;
import raptor.connector.ics.chat.AbortRequestedEventParser;
import raptor.connector.ics.chat.BugWhoAllEventParser;
import raptor.connector.ics.chat.CShoutEventParser;
import raptor.connector.ics.chat.ChallengeEventParser;
import raptor.connector.ics.chat.ChannelTellEventParser;
import raptor.connector.ics.chat.ChatEventParser;
import raptor.connector.ics.chat.DrawOfferedEventParser;
import raptor.connector.ics.chat.FingerEventParser;
import raptor.connector.ics.chat.FollowingEventParser;
import raptor.connector.ics.chat.HistoryEventParser;
import raptor.connector.ics.chat.JournalEventParser;
import raptor.connector.ics.chat.KibitzEventParser;
import raptor.connector.ics.chat.NotificationEventParser;
import raptor.connector.ics.chat.PartnerTellEventParser;
import raptor.connector.ics.chat.PartnershipCreatedEventParser;
import raptor.connector.ics.chat.PartnershipEndedEventParser;
import raptor.connector.ics.chat.QTellParser;
import raptor.connector.ics.chat.ShoutEventParser;
import raptor.connector.ics.chat.TellEventParser;
import raptor.connector.ics.chat.ToldEventParser;
import raptor.connector.ics.chat.VariablesEventParser;
import raptor.connector.ics.chat.WhisperEventParser;
import raptor.connector.ics.game.message.B1Message;
import raptor.connector.ics.game.message.G1Message;
import raptor.connector.ics.game.message.GameEndMessage;
import raptor.connector.ics.game.message.IllegalMoveMessage;
import raptor.connector.ics.game.message.MovesMessage;
import raptor.connector.ics.game.message.NoLongerExaminingGameMessage;
import raptor.connector.ics.game.message.RemovingObsGameMessage;
import raptor.connector.ics.game.message.Style12Message;
import raptor.pref.PreferenceKeys;
import raptor.service.ConnectorService;
import raptor.service.GameService;
import raptor.service.GameService.GameInfo;
import raptor.service.GameService.Offer;
import raptor.service.GameService.Offer.OfferType;
import raptor.swt.UserInfoDialog;
import raptor.util.RaptorLogger;
import raptor.util.RaptorStringTokenizer;

/**
 * An implementation of IcsParser that is both BICS and FICS friendly.
 */
public class IcsParser implements GameConstants {
	private static final RaptorLogger LOG = RaptorLogger
			.getLog(IcsParser.class);
	public static final int MAX_GAME_MESSAGE = 1000;

	protected B1Parser b1Parser;
	protected IcsConnector connector;
	protected G1Parser g1Parser;
	protected GameEndParser gameEndParser;
	protected IllegalMoveParser illegalMoveParser;
	protected MovesParser movesParser;
	protected NoLongerExaminingGameParser noLongerExaminingParser;
	protected List<ChatEventParser> nonGameEventParsers = new ArrayList<ChatEventParser>(
			30);
	protected RemovingObsGameParser removingObsGameParser;
	protected FollowingEventParser followingParser;
	protected Style12Parser style12Parser;
	protected SoughtParser soughtParser;

	protected BugWhoGParser bugWhoGParser;
	protected BugWhoPParser bugWhoPParser;
	protected BugWhoUParser bugWhoUParser;

	protected UserInfoDialog fullUserInfoDialog;

	protected GameInfoParser gameInfoParser;

	/**
	 * Used for BICS parsing. Bics sends a B1 right after you make a move then a
	 * style 12 and another B1. This breaks Raptor, since it uses the game
	 * object to keep track of the state. So it is currently being used to
	 * ignore all B1s if the game is zh and its a BICS parser if they were not
	 * preceded by a style 12.
	 */
	protected boolean containedStyle12;

	protected TakebackParser takebackParser;
	protected boolean isBicsParser = false;

	/**
	 * A map keyed by game id. Used to temporarily store G1 messages until the
	 * first style 12 message comes along. A new game requires a 12 message as
	 * well as a G1.
	 */
	protected Map<String, G1Message> unprocessedG1Messages = new HashMap<String, G1Message>();

	/**
	 * A map keyed by game id. Used to temporarily store style12 messages from
	 * newly examined games until the moves message comes along. From the moves
	 * message you can identify the variant and create the game correctly.
	 */
	protected Map<String, Style12Message> exaimineGamesWaitingOnMoves = new HashMap<String, Style12Message>();

	/**
	 * A map keyed by game id. Used to temporarily store style12 messages from
	 * newly examined games until the moves message comes along. From the moves
	 * message you can identify the variant and create the game correctly.
	 */
	protected Map<String, B1Message> exaimineB1sWaitingOnMoves = new HashMap<String, B1Message>();

	/**
	 * BICS does'nt support the partner board in G1 messages so you have to
	 * resort to this to link the bug games together.
	 */
	protected List<String> bugGamesWithoutBoard2 = new ArrayList<String>(10);

	private ChatEvent finger, var;

	public IcsParser(boolean isBicsParser) {
		this.isBicsParser = isBicsParser;
		gameEndParser = new GameEndParser();
		b1Parser = new B1Parser();
		g1Parser = new G1Parser();
		illegalMoveParser = new IllegalMoveParser();
		removingObsGameParser = new RemovingObsGameParser();
		takebackParser = new TakebackParser();
		noLongerExaminingParser = new NoLongerExaminingGameParser();
		movesParser = new MovesParser();
		followingParser = new FollowingEventParser();
		style12Parser = new Style12Parser();

		if (!isBicsParser) {
			soughtParser = new SoughtParser();
			bugWhoGParser = new BugWhoGParser();
			bugWhoPParser = new BugWhoPParser();
			bugWhoUParser = new BugWhoUParser();
			gameInfoParser = new GameInfoParser();
		}

		nonGameEventParsers.add(new PartnerTellEventParser());
		nonGameEventParsers.add(new ToldEventParser());
		nonGameEventParsers.add(new ChannelTellEventParser());
		nonGameEventParsers.add(new CShoutEventParser());
		nonGameEventParsers.add(new ShoutEventParser());
		nonGameEventParsers.add(new KibitzEventParser());
		nonGameEventParsers.add(new TellEventParser());
		nonGameEventParsers.add(new WhisperEventParser());
		nonGameEventParsers.add(new QTellParser());

		// Non tell types of events.
		nonGameEventParsers.add(new ChallengeEventParser());
		nonGameEventParsers.add(new PartnershipCreatedEventParser());
		nonGameEventParsers.add(new PartnershipEndedEventParser());
		nonGameEventParsers.add(new FollowingEventParser());
		nonGameEventParsers.add(new DrawOfferedEventParser());
		nonGameEventParsers.add(new AbortRequestedEventParser());
		nonGameEventParsers.add(new HistoryEventParser());
		nonGameEventParsers.add(new JournalEventParser());
		nonGameEventParsers.add(new FingerEventParser());
		nonGameEventParsers.add(new BugWhoAllEventParser());
		nonGameEventParsers.add(new NotificationEventParser());
		nonGameEventParsers.add(new VariablesEventParser());

	}

	/**
	 * Handles game events, and returns an array of ChatEvents which were
	 * parsed.
	 * 
	 * @param inboundMessage
	 * @return
	 */
	public ChatEvent[] parse(String inboundMessage) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Raw message in " + connector.getContext().getShortName()
					+ ": " + inboundMessage);
		}
		List<ChatEvent> events = new ArrayList<ChatEvent>(5);

		// First handle the Moves message.
		String afterMovesMessage = parseMovesMessage(inboundMessage, events);

		if (LOG.isDebugEnabled()) {
			LOG.debug("After handling moves message: " + afterMovesMessage);
		}

		// Next handle game events.
		if (StringUtils.isNotBlank(afterMovesMessage)) {
			String afterGameEvents = parseGameEvents(afterMovesMessage);

			if (LOG.isDebugEnabled()) {
				LOG.debug("After handling game events: " + afterGameEvents);
			}

			// Now process what is left over as chat events.
			// Don't send it if its only a prompt.
			if (StringUtils.isNotBlank(afterGameEvents)
					&& !afterGameEvents.trim().equals(
							connector.getContext().getPrompt())) {

				// Now process bug who events.
				ChatEvent bugWhoEvent = processBugWho(afterGameEvents);
				if (bugWhoEvent == null) {
					// Now process sought events.
					ChatEvent soughtEvent = processSought(afterGameEvents);

					if (soughtEvent == null) {
						ChatEvent gameInfoEvent = processGameInfo(afterGameEvents);
						if (gameInfoEvent == null) {
							// Its not a game,gameInfo,bugwho,or sought event so
							// now try the other parsers.
							for (ChatEventParser parser : nonGameEventParsers) {
								ChatEvent event = parser.parse(afterGameEvents);
								if (event != null) {
									events.add(event);
									break;
								}
							}
							// Its an unhandled event
							if (events.isEmpty()) {
								events.add(new ChatEvent(null,
										ChatType.UNKNOWN, afterGameEvents));
							}
						} else {
							events.add(gameInfoEvent);
						}
					} else {
						events.add(soughtEvent);
					}
				} else {
					events.add(bugWhoEvent);
				}
			}
		}

		if (fullUserInfoDialog != null) {
			for (ChatEvent event : events) {
				if (event.getType() == ChatType.FINGER) {
					LOG.debug("Received finger for usinfo: "
							+ event.getMessage());
					finger = event;
				} else if (event.getType() == ChatType.VARIABLES) {
					LOG.debug("Received var for usinfo: " + event.getMessage());
					var = event;
				}
			}

			if (finger != null && events.contains(finger))
				events.remove(finger);

			if (var != null && events.contains(var))
				events.remove(var);

			if (finger != null && var != null) {
				UserInfoParser pars = new UserInfoParser(finger.getMessage(),
						var.getMessage());
				LOG.debug("Parser output: " + pars.getOnFor() + " "
						+ pars.getInterface());
				fullUserInfoDialog.updateData(pars.getOnFor(),
						pars.getInterface(), "", finger.getMessage());
				setParseFullUserInfo(null);
			}
		}

		return events.toArray(new ChatEvent[0]);
	}

	/**
	 * Invoked when a user is examining a game and it becomes a setup position.
	 */
	public void processExaminedGameBecameSetup() {
		Game[] activeGames = connector.getGameService().getAllActiveGames();
		for (Game game : activeGames) {
			if (game.isInState(Game.EXAMINING_STATE)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Handling transition from examined game to bsetup.");
				}
				SetupGame setupGame = new SetupGame();
				game.overwrite(setupGame, true);

				// Set all the drop pieces.
				setupGame.setPieceCount(WHITE, PAWN, 1);
				setupGame.setPieceCount(WHITE, KNIGHT, 1);
				setupGame.setPieceCount(WHITE, BISHOP, 1);
				setupGame.setPieceCount(WHITE, ROOK, 1);
				setupGame.setPieceCount(WHITE, QUEEN, 1);
				setupGame.setPieceCount(WHITE, KING, 1);
				setupGame.setPieceCount(BLACK, PAWN, 1);
				setupGame.setPieceCount(BLACK, KNIGHT, 1);
				setupGame.setPieceCount(BLACK, BISHOP, 1);
				setupGame.setPieceCount(BLACK, ROOK, 1);
				setupGame.setPieceCount(BLACK, QUEEN, 1);
				setupGame.setPieceCount(BLACK, KING, 1);

				// Adjust the state since it was cleared after the overwrite.
				setupGame.clearState(Game.EXAMINING_STATE);
				setupGame.clearState(Game.OBSERVING_EXAMINED_STATE);
				setupGame.clearState(Game.OBSERVING_STATE);
				setupGame.addState(Game.SETUP_STATE);
				setupGame.addState(Game.DROPPABLE_STATE);
				connector.getGameService().addGame(setupGame);
				connector.getGameService().fireExaminedGameBecameSetup(
						game.getId());
				break;
			}
		}
	}

	public void setConnector(IcsConnector connector) {
		this.connector = connector;

	}

	/**
	 * If a game is being played on any connector and the preference
	 * BOARD_IGNORE_OBSERVED_GAMES_IF_PLAYING is set then this will veto the
	 * games creation.
	 */
	public boolean vetoGameCreation(Game game, G1Message g1Message) {
		boolean result = false;
		if (game.isInState(Game.OBSERVING_STATE)
				&& Raptor
						.getInstance()
						.getPreferences()
						.getBoolean(
								PreferenceKeys.BOARD_IGNORE_OBSERVED_GAMES_IF_PLAYING)
				&& (!isBughouse(game) || (isBughouse(game) && !connector
						.getGameService().isManaging(g1Message.parterGameId)))) {
			for (Connector connector : ConnectorService.getInstance()
					.getConnectors()) {
				if (connector.isLoggedInUserPlayingAGame()) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	protected void adjustBughouseHeadersAndFollowPartnersGamesForBics(
			Game game, Style12Message message, GameService service) {
		// BICS currently does'nt set a partner id so you have
		// to
		// do this.
		if (bugGamesWithoutBoard2.isEmpty()) {
			if (observePartnerBoardForGame(game)) {
				bugGamesWithoutBoard2.add(message.gameId);
				connector.sendMessage("pobserve "
						+ (message.isWhiteOnTop ? message.blackName
								: message.whiteName), true);
			}
		} else {
			Game otherBoard = service.getGame(bugGamesWithoutBoard2.get(0));
			if (otherBoard == null) {
				connector
						.onError(
								"Could not find game with id "
										+ bugGamesWithoutBoard2.get(0)
										+ " in the GameService. Please get BICS to add a partner game id to its G1 message.\n"
										+ " You can complain to both johnthegreat and aramen.",
								new Exception());
			} else {
				((BughouseGame) game).setOtherBoard((BughouseGame) otherBoard);
				((BughouseGame) otherBoard).setOtherBoard((BughouseGame) game);

				if (StringUtils.defaultIfEmpty(
						otherBoard.getHeader(PgnHeader.WhiteOnTop), "0")
						.equals("0")) {
					game.setHeader(PgnHeader.WhiteOnTop, "1");
				} else {
					game.setHeader(PgnHeader.WhiteOnTop, "0");
				}
			}
			bugGamesWithoutBoard2.clear();
		}
	}

	protected void adjustBughouseHeadersAndFollowPartnersGamesForFics(
			Game game, G1Message g1Message, Style12Message message,
			GameService service) {
		if (!connector.getGameService().isManaging(g1Message.parterGameId)) {
			if (observePartnerBoardForGame(game)) {
				connector
						.sendMessage("observe " + g1Message.parterGameId, true);
			}
		} else {
			Game otherBoard = service.getGame(g1Message.parterGameId);
			((BughouseGame) game).setOtherBoard((BughouseGame) otherBoard);
			((BughouseGame) otherBoard).setOtherBoard((BughouseGame) game);
			if (StringUtils.defaultIfEmpty(
					otherBoard.getHeader(PgnHeader.WhiteOnTop), "0")
					.equals("0")) {
				game.setHeader(PgnHeader.WhiteOnTop, "1");
			} else {
				game.setHeader(PgnHeader.WhiteOnTop, "0");
			}
		}
	}

	protected void adjustWhiteOnTopHeader(Game game, Style12Message message) {
		if (message.isWhiteOnTop) {
			// Respect the flip variable.
			game.setHeader(PgnHeader.WhiteOnTop, "1");
		} else if (StringUtils.equalsIgnoreCase(message.whiteName,
				connector.userName)) {
			game.setHeader(PgnHeader.WhiteOnTop, "0");
		} else if (StringUtils.equalsIgnoreCase(message.whiteName,
				connector.userFollowing)) {
			game.setHeader(PgnHeader.WhiteOnTop, "0");
		} else if (StringUtils.equalsIgnoreCase(message.blackName,
				connector.userName)) {
			game.setHeader(PgnHeader.WhiteOnTop, "1");
		} else if (StringUtils.equalsIgnoreCase(message.blackName,
				connector.userFollowing)) {
			game.setHeader(PgnHeader.WhiteOnTop, "1");
		} else {
			game.setHeader(PgnHeader.WhiteOnTop, "0");
		}
	}

	protected boolean isBughouse(Game game) {
		return game.getVariant() == Variant.bughouse
				|| game.getVariant() == Variant.fischerRandomBughouse;
	}

	protected boolean isCrazyhouse(Game game) {
		return game.getVariant() == Variant.crazyhouse
				|| game.getVariant() == Variant.fischerRandomCrazyhouse;
	}

	protected boolean observePartnerBoardForGame(Game game) {
		boolean result = false;

		// This lets you observe if you are simuled but obsing another game,
		// otherwise
		// it wont let you observe.
		if (connector.isSimulBugConnector()) {
			String white = IcsUtils
					.stripTitles(game.getHeader(PgnHeader.White));
			String black = IcsUtils
					.stripTitles(game.getHeader(PgnHeader.Black));

			if (StringUtils.equalsIgnoreCase(white, connector.getUserName())
					|| StringUtils.equalsIgnoreCase(white,
							connector.getSimulBugPartnerName())
					|| StringUtils.equalsIgnoreCase(black,
							connector.getUserName())
					|| StringUtils.equalsIgnoreCase(black,
							connector.getSimulBugPartnerName())) {
				return false;
			}
		}

		if (game.isInState(Game.PLAYING_STATE)) {
			result = connector.getPreferences().getBoolean(
					PreferenceKeys.BUGHOUSE_PLAYING_OPEN_PARTNER_BOARD);
		} else if (game.isInState(Game.OBSERVING_STATE)) {
			result = connector.getPreferences().getBoolean(
					PreferenceKeys.BUGHOUSE_OBSERVING_OPEN_PARTNER_BOARD);
		}
		return result;
	}

	/**
	 * Parses and removes all of the game events from inboundEvent. Adjusts the
	 * games in service. Returns a String with the game events removed.
	 */
	protected String parseGameEvents(String inboundMessage) {
		containedStyle12 = false;
		if (inboundMessage.length() > MAX_GAME_MESSAGE) {
			return inboundMessage;
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Raw message in "
						+ connector.getContext().getShortName() + ": "
						+ inboundMessage);
			}

			boolean trimAtEnd = false;

			StringBuilder result = new StringBuilder(inboundMessage.length());
			RaptorStringTokenizer tok = new RaptorStringTokenizer(
					inboundMessage, "\n");

			while (tok.hasMoreTokens()) {
				String line = tok.nextToken();
				if (LOG.isDebugEnabled()) {
					LOG.debug("Processing raw line "
							+ connector.getContext().getShortName() + ": "
							+ line);
				}

				G1Message g1Message = g1Parser.parse(line);
				if (g1Message != null) {
					process(g1Message, connector.getGameService());
					trimAtEnd = true;
					continue;
				}

				Style12Message style12Message = style12Parser.parse(line);
				if (style12Message != null) {
					process(style12Message, connector.getGameService(),
							inboundMessage);
					containedStyle12 = true;
					continue;
				}

				B1Message b1Message = b1Parser.parse(line);
				if (b1Message != null) {
					process(b1Message, connector.getGameService());
					continue;
				}

				GameEndMessage gameEndMessage = gameEndParser.parse(line);
				if (gameEndMessage != null) {
					process(gameEndMessage, connector.getGameService());
					result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
					trimAtEnd = true;
					continue;
				}

				IllegalMoveMessage illegalMoveMessage = illegalMoveParser
						.parse(line);
				if (illegalMoveMessage != null) {
					process(illegalMoveMessage, connector.getGameService());
					result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
					continue;
				}

				RemovingObsGameMessage removingObsGameMessage = removingObsGameParser
						.parse(line);
				if (removingObsGameMessage != null) {
					process(removingObsGameMessage, inboundMessage,
							connector.getGameService());
					result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
					continue;
				}

				if (processPendInfo(line)) {
					trimAtEnd = true;
					continue;
				}

				NoLongerExaminingGameMessage noLonerExaminingGameMessage = noLongerExaminingParser
						.parse(line);
				if (noLonerExaminingGameMessage != null) {
					process(noLonerExaminingGameMessage,
							connector.getGameService());
					result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
					continue;
				}

				takebackParser.parse(line);

				ChatEvent followingEvent = followingParser.parse(line);
				if (followingEvent != null
						&& followingEvent.getType() == ChatType.FOLLOWING) {
					connector.setUserFollowing(followingEvent.getSource());
					// Don't eat this line. Let it be appended so the event gets
					// published.
					// It is just being used here to set the user we are
					// following so white on top
					// can be set properly.
				}

				if (line.startsWith("Entering setup mode.")
						&& !inboundMessage.contains("<12>")) {
					processExaminedGameBecameSetup();
				} else if (line.startsWith("Game ")
						&& line.endsWith("enters setup mode.")
						&& !inboundMessage.contains("<12>")) {
					processExaminedGameBecameSetup();
				}

				result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
			}
			return trimAtEnd ? result.toString().trim() : result.toString();
		}
	}

	/**
	 * Parses out the Moves message from inboundEvent. It is assumed moves
	 * messages will never contain other messages.
	 * 
	 * @param inboundEvent
	 *            The inbound message.
	 * @param events
	 *            The chatEvents
	 */
	protected String parseMovesMessage(String inboundMessage,
			List<ChatEvent> events) {
		MovesMessage movesMessage = movesParser.parse(inboundMessage);
		if (movesMessage != null) {
			process(movesMessage, connector.getGameService());
			events.add(new ChatEvent(null, ChatType.MOVES, inboundMessage));
			return null;
		} else {
			return inboundMessage;
		}
	}

	protected void process(B1Message message, GameService service) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Processing b1: " + message);
		}
		Game game = service.getGame(message.gameId);
		if (game == null) {
			if (exaimineGamesWaitingOnMoves.containsKey(message.gameId)) {
				exaimineB1sWaitingOnMoves.put(message.gameId, message);

			} else {
				if (LOG.isInfoEnabled()) {
					LOG.info("Received B1 for a game not in the GameService. "
							+ "You could be ignoring a bug or zh game and get this.");
				}
			}
		} else {
			if (isBicsParser && isCrazyhouse(game) && !containedStyle12) {
				// See the documentation on the variable for an explanation of
				// why this is done.
				return;
			}
			updateGameForB1(game, message);
			service.fireDroppablePiecesChanged(message.gameId);
		}
	}

	protected void process(G1Message message, GameService service) {
		unprocessedG1Messages.put(message.gameId, message);
	}

	protected void process(GameEndMessage message, GameService service) {
		Game game = service.getGame(message.gameId);
		if (game == null) {
			// Bug game other boards might not be in the game service.
			// So no need to send a connector.onError.
		} else {
			switch (message.type) {
			case GameEndMessage.ABORTED:
			case GameEndMessage.ADJOURNED:
			case GameEndMessage.UNDETERMINED:
				game.setHeader(PgnHeader.Result,
						Result.UNDETERMINED.getDescription());
				break;
			case GameEndMessage.BLACK_WON:
				game.setHeader(PgnHeader.Result,
						Result.BLACK_WON.getDescription());
				break;
			case GameEndMessage.WHITE_WON:
				game.setHeader(PgnHeader.Result,
						Result.WHITE_WON.getDescription());
				break;
			case GameEndMessage.DRAW:
				game.setHeader(PgnHeader.Result, Result.DRAW.getDescription());
				break;
			default:
				LOG.error("Undetermined game end type. " + message);
				break;
			}
			game.setHeader(PgnHeader.ResultDescription, message.description);
			game.setHeader(PgnHeader.PlyCount, "" + game.getHalfMoveCount());
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

	protected void process(MovesMessage message, GameService service) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Processing movesMessage: " + message);
		}
		Game game = service.getGame(message.gameId);
		if (game == null) {
			// Check to see if this was for a newly examined game.
			Style12Message style12 = exaimineGamesWaitingOnMoves
					.get(message.gameId);
			if (style12 != null) {
				exaimineGamesWaitingOnMoves.remove(message.gameId);
				game = IcsUtils.createExaminedGame(style12, message);
				B1Message b1Message = exaimineB1sWaitingOnMoves.get(game
						.getId());
				if (b1Message != null) {
					updateGameForB1(game, b1Message);
					exaimineB1sWaitingOnMoves.remove(game.getId());
				}
				service.addGame(game);
				// Respect the flip variable if its set.
				game.setHeader(PgnHeader.WhiteOnTop, style12.isWhiteOnTop ? "1"
						: "0");
				service.fireGameCreated(game.getId());
				if (LOG.isDebugEnabled()) {
					LOG.debug("Firing game created.");
				}
			} else {
				LOG.warn("Received a MovesMessage for a game not being managed. This can occur if the user manually types in the moves command. "
						+ message.gameId);
			}
		} else {
			Style12Message style12 = exaimineGamesWaitingOnMoves
					.get(message.gameId);
			if (style12 != null) {
				// Both observed games becoming examined games and
				// setup games becoming examined games will flow through here.

				// Distinguishes between setup and observed.
				boolean isSetup = game.isInState(Game.SETUP_STATE)
						&& !game.isInState(Game.OBSERVING_EXAMINED_STATE);

				exaimineGamesWaitingOnMoves.remove(message.gameId);
				game = IcsUtils.createExaminedGame(style12, message);
				B1Message b1Message = exaimineB1sWaitingOnMoves.get(game
						.getId());
				if (b1Message != null) {
					updateGameForB1(game, b1Message);
				}
				exaimineB1sWaitingOnMoves.remove(game.getId());

				service.addGame(game);
				// Respect the flip variable if its set.
				game.setHeader(PgnHeader.WhiteOnTop, style12.isWhiteOnTop ? "1"
						: "0");

				if (isSetup) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Firing fireSetupGameBecameExamined.");
					}
					game.addState(Game.EXAMINING_STATE);
					game.clearState(Game.OBSERVING_EXAMINED_STATE);
					service.fireSetupGameBecameExamined(game.getId());
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Firing fireObservedGameBecameExamined.");
					}
					// Need a delay for puzzlebot sometimes it opens too fast.
					try {
						Thread.sleep(250);
					} catch (InterruptedException ie) {
					}
					game.addState(Game.EXAMINING_STATE);
					game.clearState(Game.OBSERVING_EXAMINED_STATE);
					service.fireObservedGameBecameExamined(game.getId());
				}
			} else {
				IcsUtils.updateGamesMoves(game, message);
				service.fireGameMovesAdded(game.getId());
			}
		}
	}

	protected void process(NoLongerExaminingGameMessage message,
			GameService service) {
		Game game = service.getGame(message.gameId);
		if (game == null) {
			connector.onError(
					"Received no longer examining game message for a game not in the GameService. "
							+ message, new Exception());
		} else {
			game.setHeader(PgnHeader.ResultDescription,
					"Interrupted by unexamine.");
			game.setHeader(PgnHeader.Result,
					Result.UNDETERMINED.getDescription());
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

	protected void process(RemovingObsGameMessage message,
			String entireMessage, GameService service) {
		Game game = service.getGame(message.gameId);
		if (game == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Received removing obs game message for a game not in the GameService."
						+ message);
			}
		} else if (!entireMessage.contains("has made you an examiner of game ")) {
			game.setHeader(PgnHeader.ResultDescription,
					"Interrupted by unobserve");
			game.setHeader(PgnHeader.Result,
					Result.UNDETERMINED.getDescription());
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
			if (LOG.isDebugEnabled()) {
				LOG.debug("Game state: " + game.getState());
			}
			if (game.isInState(Game.SETUP_STATE)
					|| game.isInState(Game.EXAMINING_STATE)
					|| game.isInState(Game.OBSERVING_EXAMINED_STATE)) {
				processStyle12ExamineAndSetupAdjustment(game, message, service,
						entireMessage);

			} else {
				processStyle12Adjustment(game, message, service, entireMessage);
			}
		} else {
			G1Message g1Message = unprocessedG1Messages.get(message.gameId);
			if (g1Message == null) {
				processStyle12Creation(message, service, entireMessage);

			} else {
				processG1Creation(g1Message, message, service, entireMessage);
			}
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Processed style 12: " + message + " in "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	protected ChatEvent processBugWho(String message) {
		ChatEvent result = null;

		// Bics bugwho is different. Someone needs to write bics bugwho to get
		// it working.
		if (bugWhoUParser != null) {
			Bugger[] buggers = bugWhoUParser.parse(message);
			if (buggers == null) {
				Partnership[] partnerships = bugWhoPParser.parse(message);
				if (partnerships == null) {
					raptor.chat.BugGame[] bugGames = bugWhoGParser
							.parse(message);
					if (bugGames != null) {
						connector.getBughouseService().setGamesInProgress(
								bugGames);
						result = new ChatEvent(null, ChatType.BUGWHO_GAMES,
								message);
					}
				} else {
					connector.getBughouseService().setAvailablePartnerships(
							partnerships);
					result = new ChatEvent(null,
							ChatType.BUGWHO_AVAILABLE_TEAMS, message);
				}
			} else {
				connector.getBughouseService().setUnpartneredBuggers(buggers);
				result = new ChatEvent(null,
						ChatType.BUGWHO_UNPARTNERED_BUGGERS, message);
			}
		}
		return result;
	}

	/**
	 * Observed/Playing games starts flow here (i.e. All games that contain a G1
	 * message)
	 */
	protected void processG1Creation(G1Message g1Message,
			Style12Message message, GameService service, String entireMessage) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Processing new obs/playing game.");
		}
		unprocessedG1Messages.remove(message.gameId);

		Game game = IcsUtils.createGame(g1Message, message, isBicsParser);
		IcsUtils.updateNonPositionFields(game, message);
		IcsUtils.updatePosition(game, message);
		IcsUtils.verifyLegal(game);

		if (game instanceof FischerRandomGame) {
			((FischerRandomGame) game).initialPositionIsSet();
		} else if (game instanceof FischerRandomCrazyhouseGame) {
			((FischerRandomCrazyhouseGame) game).initialPositionIsSet();
		} else if (game instanceof FischerRandomBughouseGame) {
			((FischerRandomBughouseGame) game).initialPositionIsSet();
		} else if (game instanceof WildGame) {
			((WildGame) game).initialPositionIsSet();
		}

		if (game.getVariant() == Variant.wild
				|| game.getVariant() == Variant.fischerRandom) {
			game.setHeader(PgnHeader.FEN, game.toFen());
		}

		if (vetoGameCreation(game, g1Message)) {
			connector.publishEvent(new ChatEvent(null, ChatType.INTERNAL,
					"Vetoing observing game " + game.getId()
							+ " because you are playing a game.", null));
			connector.onUnobserve(game);
		} else {
			service.addGame(game);

			adjustWhiteOnTopHeader(game, message);

			if (isBughouse(game)) {

				/**
				 * BICS does'nt have the partner game id in the G1 so you have
				 * to handle BICS and FICS differently.
				 */
				if (g1Message.parterGameId.equals("0")) {
					adjustBughouseHeadersAndFollowPartnersGamesForBics(game,
							message, service);
				} else {
					adjustBughouseHeadersAndFollowPartnersGamesForFics(game,
							g1Message, message, service);
				}
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Firing game created.");
			}
			service.fireGameCreated(game.getId());

			/**
			 * Send a request for the moves.
			 */
			if (message.fullMoveNumber > 1 || message.fullMoveNumber == 1
					&& !message.isWhitesMoveAfterMoveIsMade) {
				connector.sendMessage("moves " + message.gameId, true,
						ChatType.MOVES);
			}
		}
	}

	protected ChatEvent processGameInfo(String message) {
		ChatEvent result = null;
		if (gameInfoParser != null) {
			GameInfo[] gameInfos = gameInfoParser.parse(message);
			if (gameInfos != null) {
				connector.getGameService().fireGameInfoChanged(gameInfos);
				result = new ChatEvent(null, ChatType.GAMES, message);
			}
		}
		return result;

	}

	/**
	 * Handles the fics pendinfo messages.
	 * 
	 * @param line
	 *            THe line being parsed.
	 * @return True if it was a pendinfo message and was processed, false
	 *         otherwise.
	 */
	protected boolean processPendInfo(String line) {
		if (line.startsWith("<pf>")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(line, " =",
					true);
			Offer offer = new Offer();
			offer.setReceiving(true);
			tok.nextToken();
			offer.setId(tok.nextToken());
			tok.nextToken();
			offer.setSource(tok.nextToken());
			tok.nextToken();
			String type = tok.nextToken();
			if (type.equals("partner")) {
				offer.setType(OfferType.partner);
				offer.setDeclineCommand("decline " + offer.getId());
				offer.setDeclinable(true);
				offer.setDeclineAllCommand("decline t all");
				offer.setCommand("accept " + offer.getId());
				offer.setDescription("Accept partnership offer from "
						+ offer.getSource());
				offer.setDeclineDescription("Decline partnership offer from "
						+ offer.getSource());
			} else if (type.equals("match")) {
				offer.setType(OfferType.match);
				offer.setCommand("accept " + offer.getId());
				offer.setDeclinable(true);
				offer.setDeclineCommand("decline " + offer.getId());
				offer.setDeclineAllCommand("decline t all");
				tok.nextToken();
				String challengeDescription = tok.getWhatsLeft();
				offer.setDeclineDescription("Decline challenge "
						+ challengeDescription);
				offer.setDescription("Accept challenge " + challengeDescription);
			} else if (type.equals("draw")) {
				offer.setType(OfferType.draw);
				offer.setCommand("accept " + offer.getId());
				offer.setDeclinable(true);
				offer.setDeclineCommand("decline " + offer.getId());
				offer.setDeclineAllCommand("decline t all");
				offer.setDeclineDescription("Decline draw offer from "
						+ offer.getSource());
				offer.setDescription("Accept draw offer from "
						+ offer.getSource());
			} else if (type.equals("adjourn")) {
				offer.setType(OfferType.adjourn);
				offer.setCommand("accept " + offer.getId());
				offer.setDeclinable(true);
				offer.setDeclineCommand("decline " + offer.getId());
				offer.setDeclineAllCommand("decline t all");
				offer.setDescription("Accept adjourn offer from "
						+ offer.getSource());
				offer.setDeclineDescription("Decline adjourn offer from "
						+ offer.getSource());
			} else if (type.equals("abort")) {
				offer.setType(OfferType.abort);
				offer.setCommand("accept " + offer.getId());
				offer.setDeclinable(true);
				offer.setDeclineCommand("decline " + offer.getId());
				offer.setDeclineAllCommand("decline t all");
				offer.setDescription("Accept abort offer from "
						+ offer.getSource());
				offer.setDeclineDescription("Decline abort offer from "
						+ offer.getSource());
			} else if (type.equals("takeback")) {
				offer.setType(OfferType.takeback);
				offer.setCommand("accept " + offer.getId());
				offer.setDeclinable(true);
				offer.setDeclineCommand("decline " + offer.getId());
				offer.setDeclineAllCommand("decline t all");
				tok.nextToken();
				String taokebackMoves = tok.getWhatsLeft();
				offer.setDescription("Accept takeback " + taokebackMoves
						+ " halfmoves offer from " + offer.getSource());
				offer.setDeclineDescription("Decline takeback "
						+ taokebackMoves + " halfmoves offer from "
						+ offer.getSource());
			} else {
				return true;
			}
			connector.getGameService().fireOfferReceived(offer);
			return true;
		} else if (line.startsWith("<pt>")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(line, " =",
					true);
			Offer offer = new Offer();
			offer.setReceiving(false);
			tok.nextToken();
			offer.setId(tok.nextToken());
			tok.nextToken();
			offer.setSource(tok.nextToken());
			tok.nextToken();
			String type = tok.nextToken();

			if (type.equals("partner")) {
				offer.setType(OfferType.partner);
				offer.setCommand("withdraw " + offer.getId());
				offer.setDescription("Withdraw partnership offer to "
						+ offer.getSource());
			} else if (type.equals("match")) {
				offer.setType(OfferType.match);
				offer.setCommand("withdraw " + offer.getId());
				tok.nextToken();
				offer.setDescription("Withdraw challenge " + tok.getWhatsLeft());
			} else if (type.equals("draw")) {
				offer.setType(OfferType.draw);
				offer.setCommand("withdraw " + offer.getId());
				offer.setDescription("Withdraw draw offer to "
						+ offer.getSource());
			} else if (type.equals("adjourn")) {
				offer.setType(OfferType.adjourn);
				offer.setCommand("withdraw " + offer.getId());
				offer.setDescription("Withdraw adjourn offer to "
						+ offer.getSource());
			} else if (type.equals("abort")) {
				offer.setType(OfferType.abort);
				offer.setCommand("withdraw " + offer.getId());
				offer.setDescription("Withdraw abort offer to "
						+ offer.getSource());
			} else if (type.equals("takeback")) {
				offer.setType(OfferType.takeback);
				offer.setCommand("withdraw " + offer.getId());
				tok.nextToken();
				offer.setDescription("Withdraw takeback " + tok.getWhatsLeft()
						+ " halfmoves offer to " + offer.getSource());
			} else {
				return true;
			}
			connector.getGameService().fireOfferReceived(offer);
			return true;
		} else if (line.startsWith("<pr>")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(line, " =",
					true);
			tok.nextToken();
			connector.getGameService().fireOfferRemoved(tok.nextToken());
			return true;
		}
		return false;
	}

	protected ChatEvent processSought(String message) {
		ChatEvent result = null;
		if (soughtParser != null) {
			Seek[] seeks = soughtParser.parse(message);
			if (seeks != null) {
				connector.getSeekService().setSeeks(seeks);
				result = new ChatEvent(null, ChatType.SEEKS, message);
			}
		}
		return result;
	}

	/**
	 * Playing,Observing style 12 adjustments flow through here.
	 */
	protected void processStyle12Adjustment(Game game, Style12Message message,
			GameService service, String entireMessage) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Processing obs/playing/ex position move.");
		}

		// No game state transition occured.
		if (LOG.isDebugEnabled()) {
			LOG.debug("No state transitions occured. Processing style12 on existing game.");
		}

		// Takebacks may have effected the state of the game so first
		// adjsut to those.
		// adjust takebacks will also do nothing on refreshes and end
		// games
		// but will return true.
		if (!IcsUtils.adjustToTakebacks(game, message, connector)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Making move in obs/playing position.");
			}
			// Now add the move to the game.
			// Game Ends and Refreshes dont involve adding a move.
			if (IcsUtils.addCurrentMove(game, message)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Position was a move firing state changed.");
				}
				service.fireGameStateChanged(message.gameId, true);
			} else { // I'm not sure this block of code is ever hit
				// anymore.
				// TO DO: look at removing it.
				if (LOG.isDebugEnabled()) {
					LOG.debug("Position was not a move firing state changed.");
				}
				service.fireGameStateChanged(message.gameId, false);
			}
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Adjusted for takebacks.");
			}
			service.fireGameStateChanged(message.gameId, false);
		}

	}

	/**
	 * Examined/Bsetup/Isolated Positions game starts flow through here (i.e.
	 * All games that didnt have a G1 message)
	 */
	protected void processStyle12Creation(Style12Message message,
			GameService service, String entireMessage) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Processing new ex or bsetup game.");
		}

		if (message.relation == Style12Message.OBSERVING_EXAMINED_GAME_RELATION
				|| message.relation == Style12Message.OBSERVING_GAME_RELATION) {
			// This is probably from a game that was vetoed because the user was
			// playing.
			// Just return the unobserve has'nt taken effect yet.
			if (LOG.isInfoEnabled()) {
				LOG.info("A style 12 message was received for an observed game "
						+ "that wasnt being managed. Assuming this was because you "
						+ "are playing a game and have the ignore observed games if playing "
						+ "preference enabled.");
			}
			return;
		}

		Game game = IcsUtils.createGame(message, entireMessage);

		if (message.relation == Style12Message.EXAMINING_GAME_RELATION
				&& !game.isInState(Game.SETUP_STATE)) {
			exaimineGamesWaitingOnMoves.put(game.getId(), message);
			connector.sendMessage("moves " + message.gameId, true,
					ChatType.MOVES);
		} else {
			service.addGame(game);
			// Respect the flip variable if its set.
			game.setHeader(PgnHeader.WhiteOnTop, message.isWhiteOnTop ? "1"
					: "0");
			service.fireGameCreated(game.getId());
			if (LOG.isDebugEnabled()) {
				LOG.debug("Firing game created.");
			}
		}
	}

	/**
	 * Setup style 12 adjustments flow through here.
	 */
	protected void processStyle12ExamineAndSetupAdjustment(Game game,
			Style12Message message, GameService service, String entireMessage) {
		// Examined/BSetup/obs ex moves flow through here.
		if (LOG.isDebugEnabled()) {
			LOG.debug("Processing bsetup or examine position move.");
		}
		if (game.isInState(Game.OBSERVING_EXAMINED_STATE)
				&& message.relation == Style12Message.EXAMINING_GAME_RELATION) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Handling observer became examiner transition.");
			}

			// Handles a user becoming an examiner of a game he/she was
			// observing.
			// The transition will take place when the moves are received.

			// Send the moves message.
			exaimineGamesWaitingOnMoves.put(game.getId(), message);
			connector.sendMessage("moves " + message.gameId, true,
					ChatType.MOVES);
		} else if (entireMessage.contains("- entering examine mode.")
				|| entireMessage.contains(". Entering examine mode.")) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Handling bsetup to examine mode transition.");
			}
			// Handles one case of the transition from bsetup mode to examine
			// mode.
			Game examineGame = IcsUtils.createGame(message, entireMessage);
			if (message.relation == Style12Message.EXAMINING_GAME_RELATION
					&& !examineGame.isInState(Game.SETUP_STATE)) {
				exaimineGamesWaitingOnMoves.put(game.getId(), message);
				connector.sendMessage("moves " + message.gameId, true,
						ChatType.MOVES);
			}
		} else if (game.isInState(Game.EXAMINING_STATE)
				&& (entireMessage.contains("Entering setup mode.\n") || entireMessage
						.contains("enters setup mode."))) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Handling examined game became setup game transition.");
			}
			// Handles an examined game becoming a setup game.
			processExaminedGameBecameSetup();
		} else if (entireMessage.contains("entering examine mode.\n")
				|| entireMessage.contains("Entering examine mode.\n")) {
			// Add this game to the games waiting on moves.
			// Send a moves message.
			// Transition from BSETUP to EXAMINE when moves arrives.
			exaimineGamesWaitingOnMoves.put(game.getId(), message);
			connector.sendMessage("moves " + message.gameId, true,
					ChatType.MOVES);
		} else {
			// Clear out the game and start over.
			// There is no telling what happened
			// in a setup or examine game.
			IcsUtils.resetGame(game, message);

			service.fireGameStateChanged(message.gameId, true);
		}
	}

	protected void updateGameForB1(Game game, B1Message message) {
		if (isBughouse(game)
				&& (game.isInState(Game.EXAMINING_STATE) || game
						.isInState(Game.OBSERVING_EXAMINED_STATE))) {
			// On Fics when examining a bug game you don't get pieces.
			// To handle all the issues around that just set 1 of everything.
			game.setDropCount(WHITE, PAWN, 1);
			game.setDropCount(WHITE, QUEEN, 1);
			game.setDropCount(WHITE, ROOK, 1);
			game.setDropCount(WHITE, KNIGHT, 1);
			game.setDropCount(WHITE, BISHOP, 1);
			game.setDropCount(BLACK, PAWN, 1);
			game.setDropCount(BLACK, QUEEN, 1);
			game.setDropCount(BLACK, ROOK, 1);
			game.setDropCount(BLACK, KNIGHT, 1);
			game.setDropCount(BLACK, BISHOP, 1);
		} else {
			for (int i = 1; i < message.whiteHoldings.length; i++) {
				game.setDropCount(WHITE, i, message.whiteHoldings[i]);
			}
			for (int i = 1; i < message.blackHoldings.length; i++) {
				game.setDropCount(BLACK, i, message.blackHoldings[i]);
			}
		}
	}

	public synchronized void setParseFullUserInfo(UserInfoDialog b) {
		fullUserInfoDialog = b;
		if (b == null) {
			finger = null;
			var = null;
		}
	}

}
