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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.chat.Bugger;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.chat.Partnership;
import raptor.chat.Seek;
import raptor.chess.BughouseGame;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.Result;
import raptor.chess.Variant;
import raptor.chess.pgn.PgnHeader;
import raptor.connector.ics.bughouse.BugWhoGParser;
import raptor.connector.ics.bughouse.BugWhoPParser;
import raptor.connector.ics.bughouse.BugWhoUParser;
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
import raptor.connector.ics.chat.ToldEventParser;
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
import raptor.service.GameService;
import raptor.util.RaptorStringTokenizer;

/**
 * An implementation of IcsParser that is both BICS and FICS friendly.
 */
public class IcsParser implements GameConstants {
	private static final Log LOG = LogFactory.getLog(IcsParser.class);
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

	protected TakebackParser takebackParser;
	protected boolean isBicsParser = false;

	/**
	 * A map keyed by game id. Used to temporarily store G1 messages until the
	 * first style 12 message comes along. A new game requires a 12 message as
	 * well as a G1.
	 */
	protected Map<String, G1Message> unprocessedG1Messages = new HashMap<String, G1Message>();

	/**
	 * BICS does'nt support the partner board in G1 messages so you have to
	 * resort to this to link the bug games together.
	 */
	protected List<String> bugGamesWithoutBoard2 = new ArrayList<String>(10);

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
		}

		nonGameEventParsers.add(new PartnerTellEventParser());
		nonGameEventParsers.add(new ToldEventParser());
		nonGameEventParsers.add(new ChannelTellEventParser());
		nonGameEventParsers.add(new CShoutEventParser());
		nonGameEventParsers.add(new ShoutEventParser());
		nonGameEventParsers.add(new KibitzEventParser());
		nonGameEventParsers.add(new TellEventParser());
		nonGameEventParsers.add(new WhisperEventParser());

		// Non tell types of events.
		nonGameEventParsers.add(new ChallengeEventParser());
		nonGameEventParsers.add(new PartnershipCreatedEventParser());
		nonGameEventParsers.add(new PartnershipEndedEventParser());
		nonGameEventParsers.add(new FollowingEventParser());
	}

	public ChatEvent[] parse(String inboundMessage) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Raw message in: " + inboundMessage);
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
						// Its not a game,bugwho,or sought event so now try the
						// other parsers.
						for (ChatEventParser parser : nonGameEventParsers) {
							ChatEvent event = parser.parse(afterGameEvents);
							if (event != null) {
								events.add(event);
								break;
							}
						}
						// Its an unhandled event
						if (events.isEmpty()) {
							events.add(new ChatEvent(null, ChatType.UNKNOWN,
									afterGameEvents));
						}
					} else {
						events.add(soughtEvent);
					}
				} else {
					events.add(bugWhoEvent);
				}
			}
		}

		return events.toArray(new ChatEvent[0]);
	}

	public void setConnector(IcsConnector connector) {
		this.connector = connector;

	}

	/**
	 * Parses and removes all of the game events from inboundEvent. Adjusts the
	 * games in service. Returns a String with the game events removed.
	 */
	protected String parseGameEvents(String inboundMessage) {
		if (inboundMessage.length() > MAX_GAME_MESSAGE) {
			return inboundMessage;
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Raw message in: " + inboundMessage);
			}

			StringBuilder result = new StringBuilder(inboundMessage.length());
			RaptorStringTokenizer tok = new RaptorStringTokenizer(
					inboundMessage, "\n");

			while (tok.hasMoreTokens()) {
				String line = tok.nextToken();
				if (LOG.isDebugEnabled()) {
					LOG.debug("Processing raw line: " + line);
				}

				G1Message g1Message = g1Parser.parse(line);
				if (g1Message != null) {
					process(g1Message, connector.getGameService());
					continue;
				}

				Style12Message style12Message = style12Parser.parse(line);
				if (style12Message != null) {
					process(style12Message, connector.getGameService(),
							inboundMessage);
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
					process(removingObsGameMessage, connector.getGameService());
					result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
					continue;
				}

				NoLongerExaminingGameMessage noLonerExaminingGameMessage = noLongerExaminingParser
						.parse(line);
				if (noLonerExaminingGameMessage != null) {
					process(noLonerExaminingGameMessage, connector
							.getGameService());
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

				result.append(line + (tok.hasMoreTokens() ? "\n" : ""));
			}
			return result.toString();
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
			connector.onError("Received B1 for a game not in the GameService. "
					+ message, new Exception());
		} else {
			for (int i = 1; i < message.whiteHoldings.length; i++) {
				game.setDropCount(WHITE, i, message.whiteHoldings[i]);
			}
			for (int i = 1; i < message.blackHoldings.length; i++) {
				game.setDropCount(BLACK, i, message.blackHoldings[i]);
			}
			service.fireDroppablePiecesChanged(message.gameId);
		}
	}

	protected void process(G1Message message, GameService service) {
		unprocessedG1Messages.put(message.gameId, message);
	}

	protected void process(GameEndMessage message, GameService service) {
		Game game = service.getGame(message.gameId);
		if (game == null) {
			connector.onError(
					"Received game end for a game not in the GameService. "
							+ message, new Exception());
		} else {
			switch (message.type) {
			case GameEndMessage.ABORTED:
			case GameEndMessage.ADJOURNED:
			case GameEndMessage.UNDETERMINED:
				game.setHeader(PgnHeader.Result, Result.UNDETERMINED
						.getDescription());
				break;
			case GameEndMessage.BLACK_WON:
				game.setHeader(PgnHeader.Result, Result.BLACK_WON
						.getDescription());
				break;
			case GameEndMessage.WHITE_WON:
				game.setHeader(PgnHeader.Result, Result.WHITE_WON
						.getDescription());
				break;
			case GameEndMessage.DRAW:
				game.setHeader(PgnHeader.Result, Result.DRAW.getDescription());
				break;
			default:
				LOG.error("Undetermined game end type. " + message);
				break;
			}
			game.setHeader(PgnHeader.ResultDescription, message.description);
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
			if (LOG.isWarnEnabled()) {
				LOG
						.warn("Received a MovesMessage for a game not being managed. This can occur if the user manually types in the moves command. "
								+ message.gameId);
			}
		} else {
			IcsUtils.updateGamesMoves(game, message);
			service.fireGameMovesAdded(game.getId());
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
					"Interrupted by uexamine.");
			game.setHeader(PgnHeader.Result, Result.UNDETERMINED
					.getDescription());
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
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Received removing obs game message for a game not in the GameService."
								+ message);
			}
		} else {
			game.setHeader(PgnHeader.ResultDescription,
					"Interrupted by unobserve");
			game.setHeader(PgnHeader.Result, Result.UNDETERMINED
					.getDescription());
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
							LOG
									.debug("Position was a move firing state changed.");
						}
						service.fireGameStateChanged(message.gameId, true);
					} else { // I'm not sure this block of code is ever hit
						// anymore.
						// TO DO: look at removing it.
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
					service.fireGameStateChanged(message.gameId, false);
				}
			}
		} else {
			G1Message g1Message = unprocessedG1Messages.get(message.gameId);
			if (g1Message == null) {
				// Examined/Bsetup game starts flow through here
				if (LOG.isDebugEnabled()) {
					LOG.debug("Processing new ex or bsetup game.");
				}
				game = IcsUtils.createGame(message, entireMessage);
				service.addGame(game);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Firing game created.");
				}

				// Respect the flip variable if its set.
				game.setHeader(PgnHeader.WhiteOnTop, message.isWhiteOnTop ? "1"
						: "0");
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

				if (game.getVariant() == Variant.bughouse) {

					if (g1Message.parterGameId.equals("0")) {
						// BICS currently does'nt set a partner id so you have
						// to
						// do this.
						if (bugGamesWithoutBoard2.isEmpty()) {
							if ((game.isInState(Game.PLAYING_STATE) && connector
									.getPreferences()
									.getBoolean(
											PreferenceKeys.BUGHOUSE_PLAYING_OPEN_PARTNER_BOARD))
									|| (!game.isInState(Game.PLAYING_STATE) && connector
											.getPreferences()
											.getBoolean(
													PreferenceKeys.BUGHOUSE_OBSERVING_OPEN_PARTNER_BOARD))) {
								bugGamesWithoutBoard2.add(message.gameId);
								connector
										.sendMessage(
												"pobserve "
														+ (message.isWhiteOnTop ? message.blackName
																: message.whiteName),
												true);
							}
						} else {
							Game otherBoard = service
									.getGame(bugGamesWithoutBoard2.get(0));
							if (otherBoard == null) {
								connector
										.onError(
												"Could not find game with id "
														+ bugGamesWithoutBoard2
																.get(0)
														+ " in the GameService. Please get BICS to add a partner game id to its G1 message.\n"
														+ " You can complain to both johnthegreat and aramen.",
												new Exception());
							} else {
								((BughouseGame) game)
										.setOtherBoard((BughouseGame) otherBoard);
								((BughouseGame) otherBoard)
										.setOtherBoard((BughouseGame) game);

								if (StringUtils
										.defaultIfEmpty(
												otherBoard
														.getHeader(PgnHeader.WhiteOnTop),
												"0").equals("0")) {
									game.setHeader(PgnHeader.WhiteOnTop, "1");
								} else {
									game.setHeader(PgnHeader.WhiteOnTop, "0");
								}
							}
							bugGamesWithoutBoard2.clear();
						}
					} else { // Fics mode partner id is set.
						if (!connector.getGameService().isManaging(
								g1Message.parterGameId)) {
							if ((game.isInState(Game.PLAYING_STATE) && connector
									.getPreferences()
									.getBoolean(
											PreferenceKeys.BUGHOUSE_PLAYING_OPEN_PARTNER_BOARD))
									|| (!game.isInState(Game.PLAYING_STATE) && connector
											.getPreferences()
											.getBoolean(
													PreferenceKeys.BUGHOUSE_OBSERVING_OPEN_PARTNER_BOARD)))
								connector.sendMessage("observe "
										+ g1Message.parterGameId, true);
						} else {
							Game otherBoard = service
									.getGame(g1Message.parterGameId);
							((BughouseGame) game)
									.setOtherBoard((BughouseGame) otherBoard);
							((BughouseGame) otherBoard)
									.setOtherBoard((BughouseGame) game);
							if (StringUtils.defaultIfEmpty(
									otherBoard.getHeader(PgnHeader.WhiteOnTop),
									"0").equals("0")) {
								game.setHeader(PgnHeader.WhiteOnTop, "1");
							} else {
								game.setHeader(PgnHeader.WhiteOnTop, "0");
							}
						}
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

}
