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

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.RaptorConnectorWindowItem;
import raptor.RaptorWindowItem;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.chess.BughouseGame;
import raptor.chess.Game;
import raptor.chess.Move;
import raptor.chess.Result;
import raptor.chess.Variant;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;
import raptor.connector.Connector;
import raptor.connector.ConnectorListener;
import raptor.connector.MessageCallback;
import raptor.connector.ics.timeseal.TimesealingSocket;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.script.ChatScriptContext;
import raptor.script.ParameterScriptContext;
import raptor.script.RaptorChatScriptContext;
import raptor.script.RaptorParameterScriptContext;
import raptor.script.RaptorScriptContext;
import raptor.script.RegularExpressionScript;
import raptor.script.ScriptConnectorType;
import raptor.script.ScriptContext;
import raptor.service.BughouseService;
import raptor.service.ChatService;
import raptor.service.GameService;
import raptor.service.ScriptService;
import raptor.service.SeekService;
import raptor.service.SoundService;
import raptor.service.ThreadService;
import raptor.service.UserTagService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.service.GameService.Offer;
import raptor.service.GameService.Offer.OfferType;
import raptor.service.ScriptService.ScriptServiceListener;
import raptor.swt.BugButtonsWindowItem;
import raptor.swt.BugWhoWindowItem;
import raptor.swt.GamesWindowItem;
import raptor.swt.SWTUtils;
import raptor.swt.SeekTableWindowItem;
import raptor.swt.chat.ChatConsoleWindowItem;
import raptor.swt.chat.ChatUtils;
import raptor.swt.chat.controller.ChannelController;
import raptor.swt.chat.controller.MainController;
import raptor.swt.chat.controller.RegExController;
import raptor.swt.chess.ChessBoardUtils;
import raptor.util.RaptorStringTokenizer;
import raptor.util.RaptorStringUtils;
import raptor.util.RegExUtils;

/**
 * An ics (internet chess server) connector. You will need to supply yuor own
 * IcsConnectorContext because they are all different. You might also need to
 * override some methods in order to get it working.
 */
public abstract class IcsConnector implements Connector {
	protected class MessageCallbackEntry {
		protected boolean isOneShot;
		protected int missCount;
		protected Pattern regularExpression;
		protected MessageCallback callback;
	}

	private static final Log LOG = LogFactory.getLog(IcsConnector.class);
	public static final String LOGIN_CHARACTERS_TO_FILTER = "ÿûÿü";

	protected BughouseService bughouseService;

	protected ChatService chatService;

	protected List<ConnectorListener> connectorListeners = Collections
			.synchronizedList(new ArrayList<ConnectorListener>(10));

	protected IcsConnectorContext context;

	protected String currentProfileName;

	protected Thread daemonThread;
	protected GameService gameService;
	protected Map<String, Object> scriptHash = new HashMap<String, Object>();
	protected Set<String> peopleToSpeakTellsFrom = new HashSet<String>();
	protected Set<String> channelToSpeakTellsFrom = new HashSet<String>();
	protected SeekService seekService;
	protected boolean isSpeakingAllPersonTells = false;
	protected List<String> autoCompleteList = new ArrayList<String>(1000);
	protected List<Pattern> patternsToBlock = new ArrayList<Pattern>(20);

	/**
	 * Adds the game windows to the RaptorAppWindow.
	 */
	protected GameServiceListener gameServiceListener = new GameServiceAdapter() {

		@Override
		public void gameCreated(Game game) {
			if (game instanceof BughouseGame) {
				if (isSimulBugConnector && game.isInState(Game.PLAYING_STATE)) {
					// Always make white the primary and black the other board.
					if (StringUtils.equals(game.getHeader(PgnHeader.White),
							getUserName())) {
						ChessBoardUtils.openBoard(IcsUtils.buildController(
								game, IcsConnector.this));
					} else {
						ChessBoardUtils.openBoard(IcsUtils.buildController(
								game, IcsConnector.this), true);
					}
				} else if (((BughouseGame) game).getOtherBoard() == null) {
					ChessBoardUtils.openBoard(IcsUtils.buildController(game,
							IcsConnector.this));

				} else {
					ChessBoardUtils.openBoard(IcsUtils.buildController(game,
							IcsConnector.this, true), true);
				}
			} else {
				ChessBoardUtils.openBoard(IcsUtils.buildController(game,
						IcsConnector.this));
			}
		}

		@Override
		public void offerReceived(Offer offer) {
			if (offer.getType() == OfferType.partner && offer.isReceiving()) {
				onPartnershipReceived();
			}
		}
	};

	protected boolean hasSentLogin = false;

	protected boolean hasVetoPower = true;

	protected boolean hasSentPassword = false;
	protected List<ChatType> ignoringChatTypes = new ArrayList<ChatType>();
	protected StringBuilder inboundMessageBuffer = new StringBuilder(25000);
	protected ByteBuffer inputBuffer = ByteBuffer.allocate(25000);

	protected ReadableByteChannel inputChannel;

	protected boolean isConnecting;

	protected boolean isLoggedIn = false;

	protected boolean isSimulBugConnector = false;

	protected String simulBugPartnerName;
	protected Runnable keepAlive = new Runnable() {
		public void run() {
			if (isConnected()
					&& getPreferences().getBoolean(
							context.getShortName() + "-keep-alive")) {
				if (System.currentTimeMillis() - lastSendTime > 1000 * 60 * 50) {
					String command = getPreferences().getString(
							context.getPreferencePrefix()
									+ PreferenceKeys.KEEP_ALIVE_COMMAND);

					if (StringUtils.isBlank(command)) {
						command = "date";
					}
					sendMessage(command, true);
					publishEvent(new ChatEvent("", ChatType.INTERNAL, "The \""
							+ command + "\" was just sent as a keep alive."));
				}
				ThreadService.getInstance()
						.scheduleOneShot(1000 * 60 * 5, this);
			}
		}
	};

	protected long lastPingTime;
	protected long lastSendTime;
	protected long lastSendPingTime;
	protected ChatConsoleWindowItem mainConsoleWindowItem;
	protected Socket socket;
	protected String userName;
	protected String userFollowing;
	protected List<String> extendedCensorList = new ArrayList<String>(300);
	protected String[] bughouseSounds = SoundService.getInstance()
			.getBughouseSoundKeys();
	protected RegularExpressionScript[] regularExpressionScripts = null;

	protected List<MessageCallbackEntry> messageCallbackEntries = new ArrayList<MessageCallbackEntry>(
			20);
	protected ScriptServiceListener scriptServiceListener = new ScriptServiceListener() {
		public void onParameterScriptsChanged() {
		}

		public void onRegularExpressionScriptsChanged() {
			if (isConnected()) {
				refreshChatScripts();
			}
		}
	};

	/**
	 * Constructs an IcsConnector with the specified context.
	 * 
	 * @param context
	 */
	protected IcsConnector(IcsConnectorContext context) {
		this.context = context;
		chatService = new ChatService(this);
		seekService = new SeekService(this);
		gameService = new GameService();
		gameService.addGameServiceListener(gameServiceListener);
		setBughouseService(new BughouseService(this));
		prepopulateAutoCompleteList();
	}

	protected void setRegexPatternsToBlock() {
		patternsToBlock.clear();
		String[] regexPatterns = getPreferences().getStringArray(
				context.getPreferencePrefix()
						+ PreferenceKeys.REGULAR_EXPRESSIONS_TO_BLOCK);
		if (regexPatterns != null) {
			for (String regex : regexPatterns) {
				Pattern pattern = RegExUtils.getPattern(regex);
				if (pattern == null) {
					LOG.error("Invalid regex pattern: " + pattern
							+ ". Will be ignored.");
				} else {
					patternsToBlock.add(pattern);
				}
			}
		}
	}

	public void acceptSeek(String adId) {
		sendMessage("play " + adId, true);
	}

	/**
	 * Adds a connector listener to the connector.
	 */
	public void addConnectorListener(ConnectorListener listener) {
		connectorListeners.add(listener);
	}

	public void addExtendedCensor(String person) {
		extendedCensorList.add(IcsUtils.stripTitles(person).toLowerCase());
		Collections.sort(extendedCensorList);
		writeExtendedCensorList();
	}

	public String[] autoComplete(String word) {
		if (word != null && word.length() > 0) {
			String lowerCaseWord = word.toLowerCase();
			List<String> result = new ArrayList<String>(5);

			for (int i = 0; i < autoCompleteList.size(); i++) {
				if (autoCompleteList.get(i).startsWith(lowerCaseWord)) {
					result.add(autoCompleteList.get(i));
				} else if (result.size() > 0) {
					break;
				}
			}
			return (String[]) result.toArray(new String[0]);
		} else {
			return new String[0];
		}
	}

	public String[] breakUpMessage(StringBuilder message) {
		if (message.length() <= 330) {
			return new String[] { message + "\n" };
		} else {
			int firstSpace = message.indexOf(" ");
			List<String> result = new ArrayList<String>(5);
			if (firstSpace != -1) {
				int secondSpace = message.indexOf(" ", firstSpace + 1);
				if (secondSpace != -1) {
					String beginingText = message.substring(0, secondSpace + 1);
					String wrappedText = WordUtils.wrap(message.toString(),
							330, "\n", true);
					String[] wrapped = wrappedText.split("\n");
					result.add(wrapped[0] + "\n");
					for (int i = 1; i < wrapped.length; i++) {
						result.add(beginingText + wrapped[i] + "\n");
					}
				} else {
					result.add(message.substring(0, 330) + "\n");
					publishEvent(new ChatEvent(
							null,
							ChatType.INTERNAL,
							"Your message was too long and Raptor could not find a nice "
									+ "way to break it up. Your message was trimmed to:\n"
									+ result.get(0)));
				}
			} else {
				result.add(message.substring(0, 330) + "\n");
				publishEvent(new ChatEvent(
						null,
						ChatType.INTERNAL,
						"Your message was too long and Raptor could not find a nice "
								+ "way to break it up. Your message was trimmed to:\n"
								+ result.get(0)));
			}
			return result.toArray(new String[0]);
		}
	}

	public int clearExtendedCensor() {
		int result = extendedCensorList.size();
		extendedCensorList.clear();
		writeExtendedCensorList();
		return result;
	}

	public void closeAllConnectorWindowsItems() {

		if (!Raptor.getInstance().isDisposed()
				&& getPreferences().getBoolean(
						getContext().getPreferencePrefix()
								+ "close-tabs-on-disconnect")) {
			RaptorConnectorWindowItem[] items = Raptor.getInstance()
					.getWindow().getWindowItems(this);
			for (RaptorWindowItem item : items) {
				Raptor.getInstance().getWindow().disposeRaptorWindowItem(item);
			}
		}
	}

	/**
	 * Connects to ics using the settings in preferences.
	 */
	public void connect() {
		connect(Raptor.getInstance().getPreferences().getString(
				context.getPreferencePrefix() + "profile"));
	}

	/**
	 * Disconnects from the ics.
	 */
	public void disconnect() {
		synchronized (this) {
			if (isConnected()) {
				if (isLoggedIn) {
					storeTabStates();
				}
				closeAllConnectorWindowsItems();
				try {
					ScriptService.getInstance().removeScriptServiceListener(
							scriptServiceListener);
					if (inputChannel != null) {
						try {
							inputChannel.close();
						} catch (Throwable t) {
						}
					}
					if (socket != null) {
						try {
							socket.close();
						} catch (Throwable t) {
						}
					}

					if (daemonThread != null) {
						try {
							if (daemonThread.isAlive()) {
								daemonThread.interrupt();
							}
						} catch (Throwable t) {
						}
					}
					if (keepAlive != null) {
						ThreadService.getInstance().getExecutor().remove(
								keepAlive);
					}
				} catch (Throwable t) {
				} finally {
					socket = null;
					inputChannel = null;
					daemonThread = null;
					isSimulBugConnector = false;
					simulBugPartnerName = null;
					peopleToSpeakTellsFrom.clear();
					channelToSpeakTellsFrom.clear();
					isSpeakingAllPersonTells = false;
					messageCallbackEntries.clear();
					extendedCensorList.clear();
				}

				try {
					String messageLeftInBuffer = drainInboundMessageBuffer();
					if (StringUtils.isNotBlank(messageLeftInBuffer)) {
						parseMessage(messageLeftInBuffer);
					}

				} catch (Throwable t) {
				} finally {
				}
			}

			isConnecting = false;

			publishEvent(new ChatEvent(null, ChatType.INTERNAL, "Disconnected"));

			Raptor.getInstance().getWindow().setPingTime(this, -1);
			fireDisconnected();
			LOG.info("Disconnected from " + getShortName());
		}
	}

	public void dispose() {
		if (isConnected()) {
			disconnect();
		}
		if (connectorListeners != null) {
			connectorListeners.clear();
			connectorListeners = null;
		}

		if (chatService != null) {
			chatService.dispose();
			chatService = null;
		}
		if (gameService != null) {
			gameService.removeGameServiceListener(gameServiceListener);
			gameService.dispose();
			gameService = null;
		}

		if (inputBuffer != null) {
			inputBuffer.clear();
			inputBuffer = null;
		}
		if (keepAlive != null) {
			ThreadService.getInstance().getExecutor().remove(keepAlive);
		}

		if (inputChannel != null) {
			try {
				inputChannel.close();
			} catch (Throwable t) {
			}
			inputChannel = null;
		}

		LOG.info("Disposed " + getShortName() + "Connector");
	}

	public BughouseService getBughouseService() {
		return bughouseService;
	}

	public String[][] getChannelActions(String channel) {
		String channelActions = Raptor.getInstance().getPreferences()
				.getString(
						getContext().getPreferencePrefix()
								+ PreferenceKeys.CHANNEL_COMMANDS);
		String[] channelActionsArray = RaptorStringUtils.stringArrayFromString(
				channelActions, ',');

		String[][] result = new String[channelActionsArray.length][2];

		for (int i = 0; i < channelActionsArray.length; i++) {
			String action = channelActionsArray[i];
			action = action.replace("$channel", channel);
			action = action.replace("$userName", userName);
			result[i][0] = action;
			result[i][1] = action;
		}
		return result;
	}

	public String getChannelTabPrefix(String channel) {
		return "tell " + channel + " ";
	}

	public ChatScriptContext getChatScriptContext(ChatEvent event) {
		return new RaptorChatScriptContext(this, event);
	}

	public ChatService getChatService() {
		return chatService;
	}

	public IcsConnectorContext getContext() {
		return context;
	}

	public String getDescription() {
		return context.getDescription();
	}

	/**
	 * Returns the prefix to use when the user sends tells about a game. On fics
	 * this is 'whisper '. e.g. ('whisper ')
	 */
	public String getGameChatTabPrefix(String gameId) {
		return "whisper ";
	}

	public String[][] getGameIdActions(String gameId) {
		String matchActions = Raptor.getInstance().getPreferences().getString(
				getContext().getPreferencePrefix()
						+ PreferenceKeys.GAME_COMMANDS);
		String[] matchActionsArray = RaptorStringUtils.stringArrayFromString(
				matchActions, ',');

		String[][] result = new String[matchActionsArray.length][2];

		for (int i = 0; i < matchActionsArray.length; i++) {
			String action = matchActionsArray[i];
			action = action.replace("$gameId", gameId);
			action = action.replace("$userName", userName);
			result[i][0] = action;
			result[i][1] = action;
		}
		return result;
	}

	public GameService getGameService() {
		return gameService;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getLastSendTime() {
		return lastSendTime;
	}

	public ParameterScriptContext getParameterScriptContext(
			Map<String, Object> parameterMap) {
		return new RaptorParameterScriptContext(this, parameterMap);
	}

	public String getPartnerTellPrefix() {
		return "ptell ";
	}

	public String[] getPeopleOnExtendedCensor() {
		return extendedCensorList.toArray(new String[0]);
	}

	public String[][] getPersonActions(String person) {
		String matchActions = Raptor.getInstance().getPreferences().getString(
				getContext().getPreferencePrefix()
						+ PreferenceKeys.PERSON_COMMANDS);
		String[] matchActionsArray = RaptorStringUtils.stringArrayFromString(
				matchActions, ',');

		String[][] result = new String[matchActionsArray.length][2];

		for (int i = 0; i < matchActionsArray.length; i++) {
			String action = matchActionsArray[i];
			action = action.replace("$person", person);
			action = action.replace("$userName", userName);
			result[i][0] = action;
			result[i][1] = action;
		}
		return result;
	}

	public String getPersonTabPrefix(String person) {
		return "tell " + person + " ";
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPingTime() {
		return lastPingTime;
	}

	public RaptorPreferenceStore getPreferences() {
		return Raptor.getInstance().getPreferences();
	}

	public String getPrompt() {
		return context.getPrompt();
	}

	public ScriptConnectorType getScriptConnectorType() {
		return ScriptConnectorType.ICS;
	}

	public ScriptContext getScriptContext() {
		return new RaptorScriptContext(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getScriptVariable(String variableName) {
		return scriptHash.get(variableName);
	}

	public SeekService getSeekService() {
		return seekService;
	}

	public String getShortName() {
		return context.getShortName();
	}

	public String getSimulBugPartnerName() {
		return simulBugPartnerName;
	}

	public String getTellToString(String handle) {
		return "tell " + handle + " ";
	}

	/**
	 * Returns the name of the current user logged in.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * {@inheritDoc}
	 */
	public void invokeOnNextMatch(String regularExpression,
			MessageCallback callback) {
		MessageCallbackEntry messageCallbackEntry = new MessageCallbackEntry();
		messageCallbackEntry.regularExpression = RegExUtils
				.getPattern(regularExpression);
		messageCallbackEntry.isOneShot = true;
		messageCallbackEntry.callback = callback;
		messageCallbackEntries.add(messageCallbackEntry);
	}

	public boolean isConnected() {
		return socket != null && inputChannel != null && daemonThread != null;
	}

	public boolean isConnecting() {
		return isConnecting;
	}

	public boolean isLikelyChannel(String word) {
		return IcsUtils.isLikelyChannel(word);
	}

	public boolean isLikelyGameId(String word) {
		return IcsUtils.isLikelyGameId(word);
	}

	public boolean isLikelyPartnerTell(String outboundMessage) {
		return StringUtils.startsWithIgnoreCase(outboundMessage, "pt");
	}

	public boolean isLikelyPerson(String word) {
		return IcsUtils.isLikelyPerson(word);
	}

	public boolean isOnExtendedCensor(String person) {
		return extendedCensorList.contains(IcsUtils.stripTitles(person)
				.toLowerCase());
	}

	public boolean isSimulBugConnector() {
		return isSimulBugConnector;
	}

	public void kibitz(Game game, String kibitz) {
		sendMessage("primary " + game.getId(), true);
		sendMessage("kibitz " + kibitz);

	}

	public void makeMove(Game game, Move move) {
		sendMessage(move.getLan(), true);
	}

	public void matchBughouse(String playerName, boolean isRated, int time,
			int inc) {
		sendMessage("$$match " + playerName + " " + time + " " + inc + " "
				+ (isRated ? "rated" : "unrated") + " bughouse");
	}

	public void matchWinner(Game game) {
		String winner = game.getResult() == Result.WHITE_WON ? game
				.getHeader(PgnHeader.White)
				: game.getResult() == Result.BLACK_WON ? game
						.getHeader(PgnHeader.Black) : null;

		if (winner != null) {
			String timeControl = game.getHeader(PgnHeader.TimeControl);
			RaptorStringTokenizer tok = new RaptorStringTokenizer(timeControl,
					"+", true);
			try {
				String minutes = "" + Integer.parseInt(tok.nextToken()) / 60;
				String seconds = tok.nextToken();

				String match = "match "
						+ winner
						+ " "
						+ minutes
						+ " "
						+ seconds
						+ " "
						+ (game.getHeader(PgnHeader.Event).contains("unrated") ? "u"
								: "r") + " " + Variant.getIcsMatchType(game);

				sendMessage(match);
			} catch (NumberFormatException nfe) {
				return;
			}
		}
	}

	public void onAbortKeyPress() {
		sendMessage("$$abort", true);
	}

	public void onAcceptKeyPress() {
		sendMessage("$$accept", true);
	}

	/**
	 * Auto logs in if that is configured.
	 */
	public void onAutoConnect() {
		if (Raptor.getInstance().getPreferences().getBoolean(
				context.getPreferencePrefix() + "auto-connect")) {
			connect();
		}
	}

	public void onDeclineKeyPress() {
		sendMessage("$$decline", true);
	}

	public void onDraw(Game game) {
		sendMessage("$$draw", true);
	}

	public void onError(String message) {
		onError(message, null);
	}

	public void onError(String message, Throwable t) {
		LOG.error(message, t);
		String errorMessage = IcsUtils
				.cleanupMessage("Critical error occured! We are trying to make Raptor "
						+ "bug free and we need your help! Please take a moment to report this "
						+ "error by selecting the menu: \n Help -> Report Issue\n\n Error: "
						+ message
						+ (t == null ? "" : "\n"
								+ ExceptionUtils.getFullStackTrace(t)));
		publishEvent(new ChatEvent(null, ChatType.INTERNAL, errorMessage));
	}

	public void onExamineModeBack(Game game) {
		sendMessage("$$back", true);
	}

	public void onExamineModeCommit(Game game) {
		sendMessage("$$commit", true);
	}

	public void onExamineModeFirst(Game game) {
		sendMessage("$$back 300", true);
	}

	public void onExamineModeForward(Game game) {
		sendMessage("$$forward 1", true);
	}

	public void onExamineModeLast(Game game) {
		sendMessage("$$forward 300", true);
	}

	public void onExamineModeRevert(Game game) {
		sendMessage("$$revert", true);
	}

	public void onObserveGame(String gameId) {
		sendMessage("$$observe " + gameId, true);
	}

	/**
	 * This should show all of the observers watching the specified game.
	 */
	public void onObservers(Game game) {
		sendMessage("allobservers " + game.getId(), true);
	}

	public void onPartner(String bugger) {
		sendMessage("$$partner " + bugger, true);
	}

	public void onRematch() {
		sendMessage("$$rematch", true);
	}

	/**
	 * Resigns the specified game.
	 */
	public void onResign(Game game) {
		sendMessage("$$resign", true);
	}

	public void onSetupClear(Game game) {
		sendMessage("$$bsetup clear", true);
	}

	public void onSetupClearSquare(Game game, int square) {
		sendMessage("$$x@" + GameUtils.getSan(square), true);
	}

	public void onSetupComplete(Game game) {
		sendMessage("bsetup done", true);
	}

	public void onSetupFromFEN(Game game, String fen) {
		sendMessage("$$bsetup fen " + fen, true);
	}

	public void onSetupStartPosition(Game game) {
		sendMessage("$$bsetup start", true);
	}

	public void onUnexamine(Game game) {
		sendMessage("$$unexamine", true);
	}

	public void onUnobserve(Game game) {
		sendMessage("$$unobs " + game.getId(), true);
	}

	public String parseChannel(String word) {
		return IcsUtils.stripChannel(word);
	}

	public String parseGameId(String word) {
		return IcsUtils.stripGameId(word);
	}

	public String parsePerson(String word) {
		return IcsUtils.stripWord(word);
	}

	public void processMessageCallbacks(final ChatEvent event) {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				synchronized (messageCallbackEntries) {
					for (int i = 0; i < messageCallbackEntries.size(); i++) {
						MessageCallbackEntry entry = messageCallbackEntries
								.get(i);
						if (RegExUtils.matches(entry.regularExpression, event
								.getMessage())) {
							if (LOG.isDebugEnabled()) {
								LOG
										.debug("Invoking callback "
												+ entry.callback);
							}
							if (!entry.callback.matchReceived(event)) {
								messageCallbackEntries.remove(i);
								i--;
							}
						} else {
							entry.missCount++;
						}
					}
				}
			}
		});
	}

	protected boolean isBlockedByRegularExpressionBlocks(ChatEvent event) {
		String message = event.getMessage().trim();
		boolean result = false;
		for (Pattern pattern : patternsToBlock) {
			if (RegExUtils.matches(pattern, message)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Publishes the specified event to the chat service. Currently all messages
	 * are published on separate threads via ThreadService.
	 */
	public void publishEvent(final ChatEvent event) {
		if (chatService != null) { // Could have been disposed.
			if (LOG.isDebugEnabled()) {
				LOG.debug("Publishing event : " + event);
			}

			updateAutoComplete(event);

			if (isBlockedByExtendedCensor(event)) {
				return;
			}
			if (isBlockedByRegularExpressionBlocks(event)) {
				return;
			}

			event.setMessage(substituteTitles(event.getMessage(), event
					.getType()));
			handleOpeningTabs(event);
			processRegularExpressionScripts(event);

			if (event.getType() == ChatType.PARTNERSHIP_DESTROYED) {
				isSimulBugConnector = false;
				simulBugPartnerName = null;
			}

			// Sets the user following. This is used in the IcsParser to
			// determine if white is on top or not.
			if (event.getType() == ChatType.FOLLOWING) {
				userFollowing = event.getSource();
			} else if (event.getType() == ChatType.NOT_FOLLOWING) {
				userFollowing = null;
			}

			if (event.getType() == ChatType.PARTNER_TELL) {
				playBughouseSounds(event);
				if (!event.hasSoundBeenHandled()
						&& getPreferences().getBoolean(
								PreferenceKeys.BUGHOUSE_SPEAK_PARTNER_TELLS)) {
					event.setHasSoundBeenHandled(speak(getTextAfterColon(event
							.getMessage())));
				}
			}

			if (event.getType() == ChatType.CHANNEL_TELL) {
				if (!event.getSource().equals(getUserName())
						&& channelToSpeakTellsFrom.contains(event.getChannel())) {
					event.setHasSoundBeenHandled(speak(IcsUtils
							.stripTitles(event.getSource())
							+ " "
							+ event.getChannel()
							+ " "
							+ getTextAfterColon(event.getMessage())));
				}
			}

			if (event.getType() == ChatType.TELL) {
				if (isSpeakingAllPersonTells
						|| peopleToSpeakTellsFrom.contains(event.getSource())) {
					event.setHasSoundBeenHandled(speak(IcsUtils
							.stripTitles(event.getSource())
							+ " " + getTextAfterColon(event.getMessage())));
				}
			}

			int ignoreIndex = ignoringChatTypes.indexOf(event.getType());
			if (ignoreIndex != -1) {
				ignoringChatTypes.remove(ignoreIndex);
			} else {
				// It is interesting to note messages are handled sequentially
				// up to this point. chatService will publish the event
				// asynchronously.
				chatService.publishChatEvent(event);
				processMessageCallbacks(event);
			}
		}
	}

	/**
	 * Removes a connector listener from the connector.
	 */
	public void removeConnectorListener(ConnectorListener listener) {
		connectorListeners.remove(listener);
	}

	public boolean removeExtendedCensor(String person) {
		boolean result = extendedCensorList.remove(IcsUtils.stripTitles(person)
				.toLowerCase());
		if (result) {
			Collections.sort(extendedCensorList);
			writeExtendedCensorList();
		}
		return result;
	}

	public String removeLineBreaks(String message) {
		return IcsUtils.removeLineBreaks(message);
	}

	/**
	 * Restores the saved states for this connector.
	 */
	public void restoreTabStates() {
		String preference = StringUtils.defaultString(Raptor.getInstance()
				.getPreferences().getString(
						getContext().getShortName() + "-" + currentProfileName
								+ "-" + PreferenceKeys.CHANNEL_REGEX_TAB_INFO));

		RaptorStringTokenizer tok = new RaptorStringTokenizer(preference, "`",
				true);
		while (tok.hasMoreTokens()) {
			String type = tok.nextToken();
			String value = tok.nextToken();
			@SuppressWarnings("unused")
			String quadString = tok.nextToken();

			if (type.equals("Channel")) {
				if (!Raptor.getInstance().getWindow().containsChannelItem(this,
						value)) {
					ChatUtils.openChannelTab(this, value, false);
				}
			} else if (type.equals("RegEx")) {
				if (!Raptor.getInstance().getWindow().containsRegExItem(this,
						value)) {
					ChatUtils.openRegularExpressionTab(this, value, false);
				}
			} else if (type.equals("SeekTableWindowItem")) {
				SWTUtils.openSeekTableWindowItem(this);
			} else if (type.equals("BugWhoWindowItem")) {
				SWTUtils.openBugWhoWindowItem(this);
			} else if (type.equals("BugButtonsWindowItem")) {
				SWTUtils.openBugButtonsWindowItem(this);
			} else if (type.equals("GamesWindowItem")) {
				SWTUtils.openGamesWindowItem(this);
			}
		}

		RaptorConnectorWindowItem[] items = Raptor.getInstance().getWindow()
				.getWindowItems(this);
		for (RaptorConnectorWindowItem item : items) {
			if (item instanceof ChatConsoleWindowItem) {
				ChatConsoleWindowItem windowItem = (ChatConsoleWindowItem) item;
				if (windowItem.getController() instanceof MainController) {
					Raptor.getInstance().getWindow().forceFocus(item);
				}
			}
		}
	}

	public void sendBugAvailableTeamsMessage() {
		sendMessage("$$bugwho p", true, ChatType.BUGWHO_AVAILABLE_TEAMS);
	}

	public void sendBugGamesMessage() {
		sendMessage("$$bugwho g", true, ChatType.BUGWHO_GAMES);
	}

	public void sendBugUnpartneredBuggersMessage() {
		sendMessage("$$bugwho u", true, ChatType.BUGWHO_UNPARTNERED_BUGGERS);
	}

	public void sendGetSeeksMessage() {
		sendMessage("$$sought all", true, ChatType.SEEKS);
	}

	public void sendMessage(String message) {
		sendMessage(message, false, null);
	}

	/**
	 * Sends a message to the connector. A ChatEvent of OUTBOUND type should
	 * only be published if isHidingFromUser is false.
	 */
	public void sendMessage(String message, boolean isHidingFromUser) {
		sendMessage(message, isHidingFromUser, null);
	}

	/**
	 * Sends a message to the connector. A ChatEvent of OUTBOUND type should
	 * only be published containing the message if isHidingFromUser is false.
	 * The next message the connector reads in that is of the specified type
	 * should not be published to the ChatService.
	 */
	public void sendMessage(String message, boolean isHidingFromUser,
			ChatType hideNextChatType) {
		if (isConnected()) {

			if (vetoMessage(message)) {
				return;
			}

			handleUnexamineOnSendMessage(message);

			StringBuilder builder = new StringBuilder(message);
			IcsUtils.filterOutbound(builder);

			if (LOG.isDebugEnabled()) {
				LOG.debug(context.getShortName() + "Connector Sending: "
						+ builder.toString().trim());
			}

			if (hideNextChatType != null) {
				ignoringChatTypes.add(hideNextChatType);
			}

			// Only one thread at a time should write to the socket.
			synchronized (socket) {
				try {
					String[] messages = breakUpMessage(builder);
					for (String current : messages) {
						socket.getOutputStream().write(current.getBytes());
						socket.getOutputStream().flush();
					}
					if (message.startsWith("$$")) {
						// Don't update last send time on a $$ since idle time
						// isn't effected on the server.
						lastSendPingTime = System.currentTimeMillis();
					} else {
						lastSendTime = lastSendPingTime = System
								.currentTimeMillis();
					}

				} catch (Throwable t) {
					publishEvent(new ChatEvent(null, ChatType.INTERNAL,
							"Error: " + t.getMessage()));
					disconnect();
				}
			}

			if (!isHidingFromUser) {
				publishEvent(new ChatEvent(null, ChatType.OUTBOUND, message
						.trim()));
			}
		} else {
			publishEvent(new ChatEvent(null, ChatType.INTERNAL,
					"Error: Unable to send " + message + " to "
							+ getShortName()
							+ ". There is currently no connection."));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPrimaryGame(Game game) {
		if (getGameService().getAllActiveGames().length > 1) {
			sendMessage("primary " + game.getId(), true);
		}
	}

	/**
	 * /** {@inheritDoc}
	 */
	public void setScriptVariable(String variableName, Object value) {
		scriptHash.put(variableName, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSimulBugConnector(boolean isSimulBugConnector) {
		this.isSimulBugConnector = isSimulBugConnector;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSimulBugPartnerName(String simulBugPartnerName) {
		this.simulBugPartnerName = simulBugPartnerName;
	}

	public void setSpeakingAllPersonTells(boolean isSpeakingAllPersonTells) {
		this.isSpeakingAllPersonTells = isSpeakingAllPersonTells;
	}

	public void setSpeakingChannelTells(String channel,
			boolean isSpeakingChannelTells) {
		if (isSpeakingChannelTells) {
			if (!channelToSpeakTellsFrom.contains(channel)) {
				channelToSpeakTellsFrom.add(channel);
			}
		} else {
			channelToSpeakTellsFrom.remove(channel);
		}
	}

	public void setSpeakingPersonTells(String person,
			boolean isSpeakingPersonTells) {
		if (isSpeakingPersonTells) {
			if (!peopleToSpeakTellsFrom.contains(person)) {
				peopleToSpeakTellsFrom.add(person);
			}
		} else {
			peopleToSpeakTellsFrom.remove(person);
		}
	}

	/**
	 * Stores off the tab states that matter to this connector so they can be
	 * restored when reconnected.
	 */
	public void storeTabStates() {
		if (!Raptor.getInstance().getWindow().getShell().isDisposed()) {
			String preference = "";
			RaptorConnectorWindowItem[] items = Raptor.getInstance()
					.getWindow().getWindowItems(this);

			// Sort to order channels.
			Arrays.sort(items, new Comparator<RaptorConnectorWindowItem>() {

				public int compare(RaptorConnectorWindowItem arg0,
						RaptorConnectorWindowItem arg1) {
					if (arg0 instanceof ChatConsoleWindowItem
							&& arg1 instanceof ChatConsoleWindowItem) {
						ChatConsoleWindowItem chatConsole1 = (ChatConsoleWindowItem) arg0;
						ChatConsoleWindowItem chatConsole2 = (ChatConsoleWindowItem) arg1;

						if (chatConsole1.getController() instanceof ChannelController
								&& chatConsole2.getController() instanceof ChannelController) {
							Integer integer1 = new Integer(
									((ChannelController) chatConsole1
											.getController()).getChannel());
							Integer integer2 = new Integer(
									((ChannelController) chatConsole2
											.getController()).getChannel());
							return integer1.compareTo(integer2);

						} else if (!(chatConsole1.getController() instanceof ChannelController)
								&& chatConsole2.getController() instanceof ChannelController) {
							return 1;
						} else if (chatConsole1.getController() instanceof ChannelController
								&& !(chatConsole2.getController() instanceof ChannelController)) {
							return -1;
						} else {
							return 0;
						}
					} else if (arg0 instanceof ChatConsoleWindowItem
							&& !(arg1 instanceof ChatConsoleWindowItem)) {
						return -1;
					} else if (arg1 instanceof ChatConsoleWindowItem
							&& !(arg0 instanceof ChatConsoleWindowItem)) {
						return 1;
					} else {
						return 0;
					}
				}
			});

			for (RaptorConnectorWindowItem item : items) {
				if (item instanceof ChatConsoleWindowItem) {
					ChatConsoleWindowItem chatConsoleItem = (ChatConsoleWindowItem) item;
					if (chatConsoleItem.getController() instanceof ChannelController) {
						ChannelController controller = (ChannelController) chatConsoleItem
								.getController();
						preference += (preference.equals("") ? "" : "`")
								+ "Channel`"
								+ controller.getChannel()
								+ "`"
								+ Raptor.getInstance().getWindow().getQuadrant(
										item).toString();
					} else if (chatConsoleItem.getController() instanceof RegExController) {
						RegExController controller = (RegExController) chatConsoleItem
								.getController();
						preference += (preference.equals("") ? "" : "`")
								+ "RegEx`"
								+ controller.getPattern()
								+ "`"
								+ Raptor.getInstance().getWindow().getQuadrant(
										item).toString();
					}
				} else if (item instanceof SeekTableWindowItem) {
					preference += (preference.equals("") ? "" : "`")
							+ "SeekTableWindowItem` " + "` ";
				} else if (item instanceof BugWhoWindowItem) {
					preference += (preference.equals("") ? "" : "`")
							+ "BugWhoWindowItem` " + "` ";
				} else if (item instanceof BugButtonsWindowItem) {
					preference += (preference.equals("") ? "" : "`")
							+ "BugButtonsWindowItem` " + "` ";
				} else if (item instanceof GamesWindowItem) {
					preference += (preference.equals("") ? "" : "`")
							+ "GamesWindowItem` ` ";
				}
			}
			Raptor.getInstance().getPreferences().setValue(
					getContext().getShortName() + "-" + currentProfileName
							+ "-" + PreferenceKeys.CHANNEL_REGEX_TAB_INFO,
					preference);
			Raptor.getInstance().getPreferences().save();
		}
	}

	public void whisper(Game game, String whisper) {
		sendMessage("primary " + game.getId(), true);
		sendMessage("whisper " + whisper);
	}

	protected void addToAutoComplete(String word) {
		String lowerCaseWord = word.toLowerCase();
		// if (StringUtils.containsOnly(word,
		// "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ+-=")) {
		if (!autoCompleteList.contains(lowerCaseWord)) {
			autoCompleteList.add(lowerCaseWord);
			Collections.sort(autoCompleteList);
		}
		// } else {
		// LOG.warn("Vetoed addToAutoComplete: " + word);
		// }
	}

	/**
	 * Connects with the specified profile name.
	 */
	protected void connect(final String profileName) {
		if (isConnected()) {
			throw new IllegalStateException("You are already connected to "
					+ getShortName() + " . Disconnect before invoking connect.");
		}
		setRegexPatternsToBlock();
		loadExtendedCensorList();
		resetConnectionStateVars();

		currentProfileName = profileName;

		final String profilePrefix = context.getPreferencePrefix()
				+ profileName + "-";

		if (LOG.isDebugEnabled()) {
			LOG.debug("Profile " + currentProfileName + " Prefix="
					+ profilePrefix);
		}

		if (mainConsoleWindowItem == null) {
			// Add the main console tab to the raptor window.
			createMainConsoleWindowItem();
			Raptor.getInstance().getWindow().addRaptorWindowItem(
					mainConsoleWindowItem, false);
		} else if (!Raptor.getInstance().getWindow().isBeingManaged(
				mainConsoleWindowItem)) {
			// Add a new main console to the raptor window since the existing
			// one is no longer being managed (it was already disposed).
			createMainConsoleWindowItem();
			Raptor.getInstance().getWindow().addRaptorWindowItem(
					mainConsoleWindowItem, false);
		}
		// If its being managed no need to do anything it should adjust itself
		// as the state of the connector changes from the ConnectorListener it
		// registered.

		if (LOG.isInfoEnabled()) {
			LOG.info(getShortName() + " Connecting to "
					+ getPreferences().getString(profilePrefix + "server-url")
					+ " " + getPreferences().getInt(profilePrefix + "port"));
		}
		publishEvent(new ChatEvent(null, ChatType.INTERNAL, "Connecting to "
				+ getPreferences().getString(profilePrefix + "server-url")
				+ " "
				+ getPreferences().getInt(profilePrefix + "port")
				+ (getPreferences().getBoolean(
						profilePrefix + "timeseal-enabled") ? " with "
						: " without ") + "timeseal ..."));

		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				try {
					if (getPreferences().getBoolean(
							profilePrefix + "timeseal-enabled")) {
						// TO DO: rewrite TimesealingSocket to use a
						// SocketChannel.
						socket = new TimesealingSocket(
								getPreferences().getString(
										profilePrefix + "server-url"),
								getPreferences().getInt(profilePrefix + "port"),
								getInitialTimesealString());

						publishEvent(new ChatEvent(null, ChatType.INTERNAL,
								"Timeseal connection string "
										+ getInitialTimesealString()));
					} else {
						socket = new Socket(getPreferences().getString(
								profilePrefix + "server-url"), getPreferences()
								.getInt(profilePrefix + "port"));
					}
					publishEvent(new ChatEvent(null, ChatType.INTERNAL,
							"Connected"));

					inputChannel = Channels.newChannel(socket.getInputStream());

					SoundService.getInstance().playSound("alert");

					daemonThread = new Thread(new Runnable() {
						public void run() {
							messageLoop();
						}
					});
					daemonThread.setName("FicsConnectorDaemon");
					daemonThread.setPriority(Thread.MAX_PRIORITY);
					daemonThread.start();

					if (LOG.isInfoEnabled()) {
						LOG.info(getShortName() + " Connection successful");
					}
				} catch (Throwable ce) {
					publishEvent(new ChatEvent(null, ChatType.INTERNAL,
							"Error: " + ce.getMessage()));
					disconnect();
					return;
				}
			}
		});
		isConnecting = true;

		if (getPreferences().getBoolean(context.getShortName() + "-keep-alive")) {
			ThreadService.getInstance().scheduleOneShot(30 * 60 * 1000,
					keepAlive);
		}

		ScriptService.getInstance().addScriptServiceListener(
				scriptServiceListener);
		refreshChatScripts();

		fireConnecting();
	}

	protected void createMainConsoleWindowItem() {
		mainConsoleWindowItem = new ChatConsoleWindowItem(new MainController(
				this));
	}

	/**
	 * Removes all of the characters from inboundMessageBuffer and returns the
	 * string removed.
	 */
	protected String drainInboundMessageBuffer() {
		return drainInboundMessageBuffer(inboundMessageBuffer.length());
	}

	/**
	 * Removes characters 0-index from inboundMessageBuffer and returns the
	 * string removed.
	 */
	protected String drainInboundMessageBuffer(int index) {
		String result = inboundMessageBuffer.substring(0, index);
		inboundMessageBuffer.delete(0, index);
		return result;
	}

	/**
	 * Removes tailing prompts form the text.
	 * 
	 * @param text
	 *            The text.
	 * @param connector
	 *            The connector to obtain the prompt from.
	 * @return The result.
	 */
	protected String filterTrailingPrompts(String text) {
		while (text.startsWith(context.getPrompt() + " ")) {
			text = text.substring(context.getPrompt().length() + 1);
		}

		if (text.endsWith(context.getRawPrompt())) {
			return text.substring(0, text.length()
					- context.getRawPrompt().length());
		} else if (text.endsWith("\n" + context.getPrompt())) {
			return text.substring(0, text.length()
					- (context.getPrompt().length() + 1));
		}
		return text;
	}

	protected void fireConnected() {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				if (connectorListeners == null) {
					return;
				}
				synchronized (connectorListeners) {
					for (ConnectorListener listener : connectorListeners) {
						listener.onConnect();
					}
				}
			}
		});
	}

	protected void fireConnecting() {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				if (connectorListeners == null) {
					return;
				}
				synchronized (connectorListeners) {
					for (ConnectorListener listener : connectorListeners) {
						listener.onConnecting();
					}
				}
			}
		});
	}

	protected void fireDisconnected() {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				if (connectorListeners == null) {
					return;
				}
				synchronized (connectorListeners) {
					for (ConnectorListener listener : connectorListeners) {
						listener.onConnecting();
					}
				}
			}
		});
	}

	protected String getInitialTimesealString() {
		return Raptor.getInstance().getPreferences().getString(
				PreferenceKeys.TIMESEAL_INIT_STRING);
	}

	protected String getTextAfterColon(String message) {
		int colonIndex = message.indexOf(":");
		if (colonIndex != -1) {
			return message.substring(colonIndex + 1, message.length());
		} else {
			return message;
		}
	}

	/**
	 * Opens new tabs based on the users preferences.
	 */
	protected void handleOpeningTabs(final ChatEvent event) {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				if (getPreferences().getBoolean(
						PreferenceKeys.CHAT_OPEN_PERSON_TAB_ON_PERSON_TELLS)
						&& event.getType() == ChatType.TELL) {
					ChatUtils.openPersonTab(IcsConnector.this, event
							.getSource(), false);
				} else if (getPreferences().getBoolean(
						PreferenceKeys.CHAT_OPEN_CHANNEL_TAB_ON_CHANNEL_TELLS)
						&& event.getType() == ChatType.CHANNEL_TELL) {
					ChatUtils.openChannelTab(IcsConnector.this, event
							.getChannel(), false);
				} else if (getPreferences().getBoolean(
						PreferenceKeys.CHAT_OPEN_PARTNER_TAB_ON_PTELLS)
						&& event.getType() == ChatType.PARTNER_TELL) {
					ChatUtils.openPartnerTab(IcsConnector.this, false);
				}
			}
		});
	}

	/**
	 * Handles automatically sending an unexamine if the user is examining a
	 * game.
	 * 
	 * @param message
	 *            The message being sent.
	 */
	protected void handleUnexamineOnSendMessage(String message) {
		if (StringUtils.startsWithIgnoreCase(message, "getgame")
				|| StringUtils.startsWithIgnoreCase(message, "examine")
				|| StringUtils.startsWithIgnoreCase(message, "$$examine")
				|| StringUtils.startsWithIgnoreCase(message, "examin")
				|| StringUtils.startsWithIgnoreCase(message, "exami")
				|| StringUtils.startsWithIgnoreCase(message, "exam")
				|| StringUtils.startsWithIgnoreCase(message, "exa")
				|| StringUtils.startsWithIgnoreCase(message, "ex")
				|| StringUtils.startsWithIgnoreCase(message, "bsetup")
				|| StringUtils.startsWithIgnoreCase(message, "$$bsetup")
				|| StringUtils.startsWithIgnoreCase(message, "play ")
				|| StringUtils.startsWithIgnoreCase(message, "seek ")
				|| StringUtils.startsWithIgnoreCase(message, "match ")
				|| StringUtils.startsWithIgnoreCase(message, "matc ")
				|| StringUtils.startsWithIgnoreCase(message, "mat ")
				|| StringUtils.startsWithIgnoreCase(message, "ma ")
				|| StringUtils.startsWithIgnoreCase(message, "m ")) {
			Game[] games = gameService.getAllActiveGames();
			Game examinedGame = null;
			for (Game game : games) {
				if (game.isInState(Game.EXAMINING_STATE)
						|| game.isInState(Game.SETUP_STATE)) {
					examinedGame = game;
					break;
				}
			}

			if (examinedGame != null) {
				onUnexamine(examinedGame);
			}
		}
	}

	protected boolean isBlockedByExtendedCensor(ChatEvent event) {
		boolean result = false;
		switch (event.getType()) {
		case TELL:
			if (isOnExtendedCensor(event.getSource())) {
				publishEvent(new ChatEvent(null, ChatType.INTERNAL,
						"Blocked tell sent from " + event.getSource()
								+ " because he/she is on extended censor."));
				result = true;
			}
			break;
		case CHANNEL_TELL:
		case NOTIFICATION_ARRIVAL:
		case NOTIFICATION_DEPARTURE:
		case SHOUT:
		case CSHOUT:
		case KIBITZ:
		case WHISPER:
		case TOLD:
			result = isOnExtendedCensor(event.getSource());
			break;
		}
		return result;
	}

	protected abstract void loadExtendedCensorList();

	/**
	 * The messageLoop. Reads the inputChannel and then invokes publishInput
	 * with the text read. Should really never be invoked.
	 */
	protected void messageLoop() {
		try {
			while (true) {
				if (isConnected()) {
					int numRead = inputChannel.read(inputBuffer);
					if (numRead > 0) {
						if (LOG.isDebugEnabled()) {
							LOG.debug(context.getShortName() + "Connector "
									+ "Read " + numRead + " bytes.");
						}
						inputBuffer.rewind();
						byte[] bytes = new byte[numRead];
						inputBuffer.get(bytes);
						inboundMessageBuffer.append(IcsUtils
								.cleanupMessage(new String(bytes)));
						try {
							// Useful for debugging /r/n issues.
							// String string = inboundMessageBuffer.toString();
							// string = string.replace("\r", "\\r");
							// string = string.replace("\n", "\\n");
							// System.err.println(string);

							onNewInput();
						} catch (Throwable t) {
							onError(context.getShortName() + "Connector "
									+ "Error in DaemonRun.onNewInput", t);
						}
						inputBuffer.clear();
					} else {
						if (LOG.isDebugEnabled()) {
							LOG.debug(context.getShortName() + "Connector "
									+ "Read 0 bytes disconnecting.");
						}
						disconnect();
						break;
					}
					Thread.sleep(50);
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug(context.getShortName() + "Connector "
								+ "Not connected disconnecting.");
					}
					disconnect();
					break;
				}
			}
		} catch (Throwable t) {
			if (t instanceof InterruptedException) {
			}
			if (t instanceof IOException) {
				LOG
						.debug(
								context.getShortName()
										+ "Connector "
										+ "IOException occured in messageLoop (These are common when disconnecting and ignorable)",
								t);
			} else {
				onError(context.getShortName()
						+ "Connector Error in DaemonRun Thwoable", t);
			}
			disconnect();
		} finally {
			LOG.debug(context.getShortName() + "Connector Leaving readInput");
		}
	}

	/**
	 * Processes a login message. Handles sending the user name and password
	 * information and the enter if prompted to hit enter if logging in as a
	 * guest.
	 * 
	 * @param message
	 * @param isLoginPrompt
	 */
	protected void onLoginEvent(String message, boolean isLoginPrompt) {
		String profilePrefix = context.getPreferencePrefix()
				+ currentProfileName + "-";

		message = StringUtils.replaceChars(message, LOGIN_CHARACTERS_TO_FILTER,
				"");
		if (isLoginPrompt) {
			if (getPreferences().getBoolean(profilePrefix + "is-anon-guest")
					&& !hasSentLogin) {
				parseMessage(message);
				hasSentLogin = true;
				sendMessage("guest", true);
			} else if (!hasSentLogin) {
				parseMessage(message);
				hasSentLogin = true;
				String handle = getPreferences().getString(
						profilePrefix + "user-name");
				if (StringUtils.isNotBlank(handle)) {
					sendMessage(handle, true);
				}
			} else {
				parseMessage(message);
			}
		} else {
			if (getPreferences().getBoolean(profilePrefix + "is-anon-guest")
					&& !hasSentPassword) {
				hasSentPassword = true;
				parseMessage(message);
				sendMessage("", true);
			} else if (getPreferences().getBoolean(
					profilePrefix + "is-named-guest")
					&& !hasSentPassword) {
				hasSentPassword = true;
				parseMessage(message);
				sendMessage("", true);
			} else if (!hasSentPassword) {
				hasSentPassword = true;
				parseMessage(message);
				String password = getPreferences().getString(
						profilePrefix + "password");
				if (StringUtils.isNotBlank(password)) {
					// Don't show the users password.
					sendMessage(password, true);
				}
			} else {
				parseMessage(message);
			}
		}
	}

	/**
	 * This method is invoked by the run method when there is new text to be
	 * handled. It buffers text until a prompt is found then invokes
	 * parseMessage.
	 * 
	 * This method also handles login logic which is tricky.
	 */
	protected void onNewInput() {

		if (lastSendPingTime != 0) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					long currentTime = System.currentTimeMillis();
					lastPingTime = currentTime - lastSendPingTime;
					Raptor.getInstance().getWindow().setPingTime(
							IcsConnector.this, lastPingTime);
					lastSendPingTime = 0;
				}
			});
		}
		if (isLoggedIn) {

			// If we are logged in. Then parse out all the text between the
			// prompts.
			int promptIndex = -1;
			while ((promptIndex = inboundMessageBuffer.indexOf(context
					.getRawPrompt())) != -1) {
				String message = drainInboundMessageBuffer(promptIndex
						+ context.getRawPrompt().length());
				parseMessage(message);
			}
		} else {

			// We are not logged in.
			// There are several complex cases here depending on the prompt
			// we are waiting on.
			// There is a login prompt, a password prompt, an enter prompt,
			// and also you have to handle invalid logins.
			int loggedInMessageIndex = inboundMessageBuffer.indexOf(context
					.getLoggedInMessage());
			if (loggedInMessageIndex != -1) {
				for (int i = 0; i < inboundMessageBuffer.length(); i++) {
					char character = inboundMessageBuffer.charAt(i);
					if (LOGIN_CHARACTERS_TO_FILTER.indexOf(character) != -1) {
						inboundMessageBuffer.deleteCharAt(i);
						i--;
					}
				}
				int nameStartIndex = inboundMessageBuffer.indexOf(context
						.getLoggedInMessage())
						+ context.getLoggedInMessage().length();
				int endIndex = inboundMessageBuffer.indexOf("****",
						nameStartIndex);

				if (endIndex != -1) {

					userName = IcsUtils.stripTitles(inboundMessageBuffer
							.substring(nameStartIndex, endIndex).trim());
					LOG.info(context.getShortName() + "Connector "
							+ "login complete. userName=" + userName);
					isLoggedIn = true;
					onSuccessfulLogin();
					restoreTabStates();
					// Since we are now logged in, just buffer the text
					// received and
					// invoke parseMessage when the prompt arrives.
				} else {
					// We have yet to receive the **** closing message so
					// wait
					// until it arrives.
				}
			} else {
				int loginIndex = inboundMessageBuffer.indexOf(context
						.getLoginPrompt());
				if (loginIndex != -1) {
					String event = drainInboundMessageBuffer(loginIndex
							+ context.getLoginPrompt().length());
					onLoginEvent(event, true);
				} else {
					int enterPromptIndex = inboundMessageBuffer.indexOf(context
							.getEnterPrompt());
					if (enterPromptIndex != -1) {
						String event = drainInboundMessageBuffer(enterPromptIndex
								+ context.getEnterPrompt().length());
						onLoginEvent(event, false);
					} else {
						int passwordPromptIndex = inboundMessageBuffer
								.indexOf(context.getPasswordPrompt());
						if (passwordPromptIndex != -1) {
							String event = drainInboundMessageBuffer(passwordPromptIndex
									+ context.getPasswordPrompt().length());
							onLoginEvent(event, false);

						} else {
							int errorMessageIndex = inboundMessageBuffer
									.indexOf(context.getLoginErrorMessage());
							if (errorMessageIndex != -1) {
								String event = drainInboundMessageBuffer();
								event = StringUtils.replaceChars(event, "ÿûÿü",
										"");
								parseMessage(event);
							}
						}
					}
				}
			}
		}
	}

	protected void onPartnershipReceived() {
		if (getPreferences().getBoolean(
				PreferenceKeys.BUGHOUSE_IS_PLAYING_PARTNERSHIP_OFFERED_SOUND)) {
			SoundService.getInstance().playSound("partnershipOffered");
		}
	}

	protected abstract void onSuccessfulLogin();

	/**
	 * Processes a message. If the user is logged in, message will be all of the
	 * text received since the last prompt from fics. If the user is not logged
	 * in, message will be all of the text received since the last login prompt.
	 * 
	 * message will always use \n as the line delimiter.
	 */
	protected void parseMessage(String message) {
		message = filterTrailingPrompts(message);
		ChatEvent[] events = null;
		try {
			events = context.getParser().parse(message);
		} catch (RuntimeException re) {
			throw new RuntimeException("Error occured parsing message: "
					+ message, re);
		}

		for (ChatEvent event : events) {
			event.setMessage(IcsUtils
					.maciejgFormatToUnicode(event.getMessage()));
			publishEvent(event);
		}
	}

	/**
	 * Plays the bughouse sound for the specified ptell. Sets hasBeenHandled to
	 * true on the event if a bughouse sound is played.
	 */
	protected void playBughouseSounds(ChatEvent event) {
		if (getPreferences().getBoolean(PreferenceKeys.APP_SOUND_ENABLED)) {
			String ptell = event.getMessage();
			int colonIndex = ptell.indexOf(":");
			if (colonIndex != -1) {
				String message = ptell
						.substring(colonIndex + 1, ptell.length()).trim();
				RaptorStringTokenizer tok = new RaptorStringTokenizer(message,
						"\n", true);
				message = tok.nextToken().trim();
				for (String bugSound : bughouseSounds) {
					if (bugSound.equalsIgnoreCase(message)) {
						event.setHasSoundBeenHandled(true);
						// This launches it on another thread.
						SoundService.getInstance().playBughouseSound(bugSound);
						break;
					}
				}
			} else {
				onError("Received a ptell event without a colon",
						new Exception());
			}
		}
	}

	protected void prepopulateAutoCompleteList() {
		addToAutoComplete("tell");
		addToAutoComplete("say");
		addToAutoComplete("kibitz");
		addToAutoComplete("whisper");
		addToAutoComplete("journal");
		addToAutoComplete("examine");
		addToAutoComplete("history");
		addToAutoComplete("finger");
		addToAutoComplete("variables");
		addToAutoComplete("shout");
		addToAutoComplete("cshout");
		addToAutoComplete("message");
		addToAutoComplete("clear");
		addToAutoComplete("quit");
		addToAutoComplete("bsetup");
		addToAutoComplete("sposition");
		addToAutoComplete("channelbot");
		addToAutoComplete("mamer");
		addToAutoComplete("watchbot");
		addToAutoComplete("puzzlebot");
		addToAutoComplete("endgamebot");
		addToAutoComplete("forward");
		addToAutoComplete("back");
		addToAutoComplete("revert");
		addToAutoComplete("refresh");
		addToAutoComplete("commit");
		addToAutoComplete("help");
		addToAutoComplete("formula");
		addToAutoComplete("set");
		addToAutoComplete("follow");
		addToAutoComplete("observe");
		addToAutoComplete("pstat");
		addToAutoComplete("oldpstat");
		addToAutoComplete("best");
		addToAutoComplete("worst");
		addToAutoComplete("rank");
		addToAutoComplete("hrank");
		addToAutoComplete("date");
		addToAutoComplete("up");
		addToAutoComplete("ping");
		addToAutoComplete("follow");
		addToAutoComplete("help");
		addToAutoComplete("znotify");
		addToAutoComplete("+notify");
		addToAutoComplete("-notify");
		addToAutoComplete("=notify");
		addToAutoComplete("+channel");
		addToAutoComplete("-channel");
		addToAutoComplete("=channel");
		addToAutoComplete("+gnotify");
		addToAutoComplete("-gnotify");
		addToAutoComplete("=gnotify");
		addToAutoComplete("+censor");
		addToAutoComplete("-censor");
		addToAutoComplete("=censor");
		addToAutoComplete("+noplay");
		addToAutoComplete("-noplay");
		addToAutoComplete("=noplay");
		addToAutoComplete("match");
		addToAutoComplete("bughouse");
		addToAutoComplete("suicide");
		addToAutoComplete("losers");
		addToAutoComplete("atmoic");
		addToAutoComplete("wild");
		addToAutoComplete("ptell");
		addToAutoComplete("abort");
		addToAutoComplete("adjourn");
		addToAutoComplete("aliashelp");
		addToAutoComplete("+tag");
		addToAutoComplete("-tag");
		addToAutoComplete("=tag");
		addToAutoComplete("+extcensor");
		addToAutoComplete("-extcensor");
		addToAutoComplete("=extcensor");
		addToAutoComplete("clear");
		addToAutoComplete("timestamp");
		addToAutoComplete("sound");
		addToAutoComplete("performance");
	}

	/**
	 * Processes the scripts for the specified chat event. Script processing is
	 * kicked off on a different thread.
	 */
	protected void processRegularExpressionScripts(final ChatEvent event) {
		if (event.getType() != ChatType.INTERNAL
				&& event.getType() != ChatType.OUTBOUND
				&& event.getType() != ChatType.MOVES
				&& event.getType() != ChatType.BUGWHO_AVAILABLE_TEAMS
				&& event.getType() != ChatType.BUGWHO_GAMES
				&& event.getType() != ChatType.BUGWHO_UNPARTNERED_BUGGERS
				&& event.getType() != ChatType.SEEKS) {
			if (regularExpressionScripts != null) {
				ThreadService.getInstance().run(new Runnable() {
					public void run() {
						for (RegularExpressionScript script : regularExpressionScripts) {
							if (script.isActive()
									&& script.matches(event.getMessage())) {
								script.execute(getChatScriptContext(event));
							}
						}
					}
				});
			}
		}
	}

	protected String substituteTitles(String message, ChatType type) {
		// Currently only handles fics formatting.
		String result = message;
		if (type == ChatType.SHOUT) {
			message = message.trim();
			if (message.startsWith("--> ")) {
				int spaceIndex = message.indexOf(' ', "--> ".length());
				if (spaceIndex != -1) {
					String word = message
							.substring("--> ".length(), spaceIndex);
					IcsUtils.stripWord(word);
					String[] titles = UserTagService.getInstance()
							.getTags(word);
					Arrays.sort(titles);
					if (titles.length > 0) {
						for (String title : titles) {
							word += "(" + title + ")";
						}
						result = "--> " + word + message.substring(spaceIndex);
					}
				}
			} else {
				int firstSpace = message.indexOf(' ');
				if (firstSpace != -1) {
					String firstWord = message.substring(0, firstSpace);
					String[] titles = UserTagService.getInstance().getTags(
							firstWord);
					Arrays.sort(titles);
					if (titles.length > 0) {
						for (String title : titles) {
							firstWord += "(" + title + ")";
						}
						result = firstWord + message.substring(firstSpace);
					}
				}
			}
		} else if (type == ChatType.CSHOUT) {
			message = message.trim();
			int firstSpace = message.indexOf(' ');
			if (firstSpace != -1) {
				String firstWord = message.substring(0, firstSpace);
				String[] titles = UserTagService.getInstance().getTags(
						firstWord);
				Arrays.sort(titles);
				if (titles.length > 0) {
					for (String title : titles) {
						firstWord += "(" + title + ")";
					}
					result = firstWord + message.substring(firstSpace);
				}
			}
		} else if (type == ChatType.TELL) {
			message = message.trim();
			int firstSpace = message.indexOf(' ');
			if (firstSpace != -1) {
				String firstWord = message.substring(0, firstSpace);
				String[] titles = UserTagService.getInstance().getTags(
						firstWord);
				Arrays.sort(titles);
				if (titles.length > 0) {
					for (String title : titles) {
						firstWord += "(" + title + ")";
					}
					result = firstWord + message.substring(firstSpace);
				}
			}
		} else if (type == ChatType.CHANNEL_TELL) {
			message = message.trim();
			int firstNonLetterChar = 0;
			for (int i = 0; i < message.length(); i++) {
				if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
						.indexOf(message.charAt(i)) == -1) {
					firstNonLetterChar = i;
					break;
				}
			}

			if (firstNonLetterChar != -1) {
				String firstWord = message.substring(0, firstNonLetterChar);
				String[] titles = UserTagService.getInstance().getTags(
						firstWord);
				Arrays.sort(titles);
				if (titles.length > 0) {
					for (String title : titles) {
						firstWord += "(" + title + ")";
					}
					result = firstWord + message.substring(firstNonLetterChar);
				}
			}
		} else if (type == ChatType.CHALLENGE) {
			message = message.trim();
			if (message.startsWith("Challenge: ")) {
				int playerStart = "Challenge: ".length();
				int playerEnd = message.indexOf(" ", playerStart);
				if (playerEnd != -1) {
					String name = message.substring(playerStart, playerEnd);
					String[] titles = UserTagService.getInstance()
							.getTags(name);
					Arrays.sort(titles);
					if (titles.length > 0) {
						for (String title : titles) {
							name += "(" + title + ")";
						}
					}
					int secondPlayerStart = message.indexOf(")");
					if (secondPlayerStart != -1) {
						secondPlayerStart = secondPlayerStart + 2;
						int secondPlayerEnd = message.indexOf(" ",
								secondPlayerStart);
						if (secondPlayerEnd != -1) {
							String secondName = message.substring(
									secondPlayerStart, secondPlayerEnd);
							titles = UserTagService.getInstance().getTags(
									secondName);
							Arrays.sort(titles);
							if (titles.length > 0) {
								for (String title : titles) {
									secondName += "(" + title + ")";
								}
							}

							result = message.substring(0, playerStart)
									+ name
									+ message.substring(playerEnd + 1,
											secondPlayerStart)
									+ secondName
									+ message.substring(secondPlayerEnd + 1,
											message.length());
						}
					}
				}
			}

		}
		return result;
	}

	protected void refreshChatScripts() {
		regularExpressionScripts = ScriptService.getInstance()
				.getRegularExpressionScripts(getScriptConnectorType());
	}

	/**
	 * Resets state variables related to the connection state.
	 */
	protected void resetConnectionStateVars() {
		isLoggedIn = false;
		hasSentLogin = false;
		hasSentPassword = false;
		userFollowing = null;
		isSimulBugConnector = false;
		simulBugPartnerName = null;
		ignoringChatTypes.clear();
		peopleToSpeakTellsFrom.clear();
		channelToSpeakTellsFrom.clear();
		messageCallbackEntries.clear();
		isSpeakingAllPersonTells = false;
	}

	protected void setUserFollowing(String userFollowing) {
		this.userFollowing = userFollowing;
	}

	protected boolean speak(String message) {
		message = StringUtils.remove(message, "fics%").trim();
		return SoundService.getInstance().textToSpeech(message);
	}

	protected void updateAutoComplete(final ChatEvent event) {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				if (StringUtils.isNotBlank(event.getSource())) {
					addToAutoComplete(event.getSource());
				}
			}
		});
	}

	protected boolean vetoMessage(String message) {
		if (!hasVetoPower) {
			return false;
		}
		boolean result = false;
		if (message.startsWith("set ptime")) {
			publishEvent(new ChatEvent(null, ChatType.INTERNAL,
					"Raptor will not work with ptime set. Command vetoed."));
			return true;
		}
		return result;
	}

	protected abstract void writeExtendedCensorList();

	private void setBughouseService(BughouseService bughouseService) {
		this.bughouseService = bughouseService;
	}
}
