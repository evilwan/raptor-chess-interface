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
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;

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
import raptor.connector.ics.timeseal.MessageListener;
import raptor.connector.ics.timeseal.MessageProducer;
import raptor.connector.ics.timeseal.TimesealSocketMessageProducer;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.script.ChatEventScript;
import raptor.script.ChatScriptContext;
import raptor.script.ParameterScriptContext;
import raptor.script.RaptorChatScriptContext;
import raptor.script.RaptorParameterScriptContext;
import raptor.script.RaptorScriptContext;
import raptor.script.ScriptConnectorType;
import raptor.script.ScriptContext;
import raptor.service.BughouseService;
import raptor.service.ChatService;
import raptor.service.GameService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.service.GameService.Offer;
import raptor.service.GameService.Offer.OfferType;
import raptor.service.ScriptService;
import raptor.service.ScriptService.ScriptServiceListener;
import raptor.service.SeekService;
import raptor.service.SeekService.SeekType;
import raptor.service.SoundService;
import raptor.service.ThreadService;
import raptor.service.UserTagService;
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
import raptor.util.RaptorLogger;
import raptor.util.RaptorStringTokenizer;
import raptor.util.RaptorStringUtils;
import raptor.util.RegExUtils;

/**
 * An ics (internet chess server) connector. You will need to supply yuor own
 * IcsConnectorContext because they are all different. You might also need to
 * override some methods in order to get it working.
 */
public abstract class IcsConnector implements Connector, MessageListener {
	protected static class MessageCallbackEntry {
		protected boolean isOneShot;
		protected int missCount;
		protected Pattern regularExpression;
		protected MessageCallback callback;
	}

	// max mmessage size is 400 on fics right now.
	private static final int MAX_SEND_MESSAGE_LENGTH = 400;

	// max mmessage size is 400 on fics right now.
	private static final int MAX_MESSAGE_MESSAGE_LENGTH = 800;

	private static final RaptorLogger LOG = RaptorLogger
			.getLog(IcsConnector.class);
	public static final String LOGIN_CHARACTERS_TO_FILTER = "����؞";

	protected BughouseService bughouseService;

	protected ChatService chatService;

	protected List<ConnectorListener> connectorListeners = Collections
			.synchronizedList(new ArrayList<ConnectorListener>(10));

	protected IcsConnectorContext context;

	protected String currentProfileName;

	protected GameService gameService;
	protected Map<String, Object> scriptHash = new HashMap<String, Object>();
	protected Set<String> peopleToSpeakTellsFrom = new HashSet<String>();
	protected Set<String> channelToSpeakTellsFrom = new HashSet<String>();
	protected Set<String> gamesToSpeakTellsFrom = new HashSet<String>();
	protected SeekService seekService;
	protected boolean isSpeakingAllPersonTells = false;
	protected List<String> autoCompleteList = new ArrayList<String>(1000);
	protected List<Pattern> patternsToBlock = new ArrayList<Pattern>(20);
	protected MessageProducer messageProducer;

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

	// protected ByteBuffer inputBuffer = ByteBuffer.allocate(25000);

	// protected ReadableByteChannel inputChannel;

	protected boolean isConnecting;

	protected boolean isLoggedIn = false;

	protected boolean isSimulBugConnector = false;

	protected long lagNotifyCounter = 0;

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
					publishEvent(new ChatEvent("", ChatType.INTERNAL,
							"The messsage: \"" + command
									+ "\" was just sent as a keep alive."));
				}
				ThreadService.getInstance()
						.scheduleOneShot(1000 * 60 * 5, this);
			}
		}

		public String toString() {
			return "IcsConnector.KeepAlive Runnable";
		}
	};

	protected long lastPingTime;
	protected long lastSendTime;
	protected long lastSendPingTime;
	protected ChatConsoleWindowItem mainConsoleWindowItem;
	protected String userName;
	protected String userFollowing;
	protected List<String> extendedCensorList = new ArrayList<String>(300);
	protected String[] bughouseSounds = SoundService.getInstance()
			.getBughouseSoundKeys();
	protected ChatEventScript[] chatEventScripts = null;

	protected List<MessageCallbackEntry> messageCallbackEntries = new ArrayList<MessageCallbackEntry>(
			20);
	protected ScriptServiceListener scriptServiceListener = new ScriptServiceListener() {
		public void onParameterScriptsChanged() {
		}

		public void onChatEventScriptsChanged() {
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

	@Override
	public void connectionClosed(StringBuilder buffer) {
		parseMessage(drainInboundMessageBuffer(buffer));
		disconnect();
	}

	@Override
	public MenuManager getMenuManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PreferencePage getRootPreferencePage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PreferenceNode[] getSecondaryPreferenceNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLoggedInUserPlayingAGame() {
		// TODO Auto-generated method stub
		return false;
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
					LOG.error("Invalid regex pattern. Will be ignored.");
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

	/**
	 * Returns true if the specified word is likely a command preceding a person
	 * name. e.g. finger, history, tell, etc.
	 * 
	 * @param word
	 *            The word to check
	 * @return The result.
	 */
	public boolean isLikelyCommandPrecedingPersonName(String command) {
		command = command.toLowerCase();
		return "tell".startsWith(command) || "history".startsWith(command)
				|| "variables".startsWith(command)
				|| "match".startsWith(command) || "ivars".startsWith(command)
				|| "journal".startsWith(command)
				|| "resign".startsWith(command)
				|| "message".startsWith(command) || "shout".startsWith(command)
				|| "cshout".startsWith(command) || "finger".startsWith(command);

	}

	/**
	 * Returns true if the specified word is in the connectors auto-complete
	 * list.
	 * 
	 * @param word
	 *            The word
	 * @return True if in auto complete, false otherwise.
	 */
	public boolean isInAutoComplete(String word) {
		return autoCompleteList.contains(word.toLowerCase());
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
		// There are two limits. Max communication size (400 on fics) for tells,
		// etc, and Max message communication size (800 on fics) for sending
		// messages.
		// This algorithm handles breaking up text that is too long for each.

		if (message.length() <= MAX_SEND_MESSAGE_LENGTH) {
			return new String[] { message + "\n" };
		} else {
			int firstSpace = message.indexOf(" ");
			int messageLimit = MAX_SEND_MESSAGE_LENGTH;

			if (firstSpace != -1) {
				String command = message.substring(0, firstSpace);
				if (command.equalsIgnoreCase("message")
						|| command.equalsIgnoreCase("mess")
						|| command.equalsIgnoreCase("mes")) {
					messageLimit = MAX_MESSAGE_MESSAGE_LENGTH;
				}
			}

			if (message.length() <= messageLimit) {
				return new String[] { message + "\n" };
			}

			List<String> result = new ArrayList<String>(5);
			if (firstSpace != -1) {
				int secondSpace = message.indexOf(" ", firstSpace + 1);
				if (secondSpace != -1) {
					String beginingText = message.substring(0, secondSpace + 1);
					String wrappedText = WordUtils.wrap(message.toString(),
							messageLimit, "\n", true);
					String[] wrapped = wrappedText.split("\n");
					result.add(wrapped[0] + "\n");
					for (int i = 1; i < wrapped.length; i++) {
						result.add(beginingText + wrapped[i] + "\n");
					}
				} else {
					result.add(message.substring(0, messageLimit) + "\n");
					publishEvent(new ChatEvent(
							null,
							ChatType.INTERNAL,
							L10n.getInstance().getString("icsConnal")
									+ result.get(0)));
				}
			} else {
				result.add(message.substring(0, messageLimit) + "\n");
				publishEvent(new ChatEvent(
						null,
						ChatType.INTERNAL,
						L10n.getInstance().getString("icsConnal")
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
		connect(Raptor.getInstance().getPreferences()
				.getString(context.getPreferencePrefix() + "profile"));
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

					if (messageProducer != null) {
						try {
							messageProducer.close();
						} catch (Throwable t) {
						}
					}
					if (keepAlive != null) {
						ThreadService.getInstance().getExecutor()
								.remove(keepAlive);
					}
				} catch (Throwable t) {
				} finally {
					messageProducer = null;
					isSimulBugConnector = false;
					simulBugPartnerName = null;
					peopleToSpeakTellsFrom.clear();
					channelToSpeakTellsFrom.clear();
					gamesToSpeakTellsFrom.clear();
					isSpeakingAllPersonTells = false;
					messageCallbackEntries.clear();
					extendedCensorList.clear();
				}
			}

			isConnecting = false;

			publishEvent(new ChatEvent(null, ChatType.INTERNAL, L10n.getInstance()
					.getString("disconn")));

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

		// if (inputBuffer != null) {
		// inputBuffer.clear();
		// inputBuffer = null;
		// }
		if (keepAlive != null) {
			ThreadService.getInstance().getExecutor().remove(keepAlive);
		}

		LOG.info("Disposed " + getShortName() + "Connector");
	}

	public BughouseService getBughouseService() {
		return bughouseService;
	}

	public String[][] getChannelActions(String channel) {
		String channelActions = Raptor
				.getInstance()
				.getPreferences()
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
		String matchActions = Raptor
				.getInstance()
				.getPreferences()
				.getString(
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

	public String[][] getPersonQuickActions(String person) {
		if (StringUtils.isBlank(person)) {
			return new String[0][0];
		}

		String matchActions = Raptor
				.getInstance()
				.getPreferences()
				.getString(
						getContext().getPreferencePrefix()
								+ PreferenceKeys.PERSON_QUICK_COMMANDS);
		String[] matchActionsArray = RaptorStringUtils.stringArrayFromString(
				matchActions, ',');

		String[][] result = new String[matchActionsArray.length + 1][2];

		for (int i = 0; i < matchActionsArray.length; i++) {
			String action = matchActionsArray[i];
			action = action.replace("$person", person);
			action = action.replace("$userName", userName);
			result[i][0] = action;
			result[i][1] = action;
		}

		result[matchActionsArray.length][0] = L10n.getInstance()
							.getString("fullUinfoOf", person);
		result[matchActionsArray.length][1] = person;

		return result;
	}

	public String[][] getPersonActions(String person) {
		if (StringUtils.isBlank(person)) {
			return new String[0][0];
		}

		String matchActions = Raptor
				.getInstance()
				.getPreferences()
				.getString(
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
		return messageProducer != null;
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
	public boolean onAutoConnect() {
		if (Raptor.getInstance().getPreferences()
				.getBoolean(context.getPreferencePrefix() + "auto-connect")) {
			connect();
			return true;
		}
		return false;
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
				.cleanupMessage(L10n.getInstance().getString("rapErr")
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
						if (RegExUtils.matches(entry.regularExpression,
								event.getMessage())) {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Invoking callback " + entry.callback);
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

			public String toString() {
				return "IcsConnector.ProcessMessageCallbacks Runnable";
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
		// System.err.println("In publish event: " + event);

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

			event.setMessage(substituteTitles(event.getMessage(),
					event.getType()));
			handleOpeningTabs(event);
			processChatEventScripts(event);

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
					// + " "
					// + event.getChannel()
					// + " "
							+ getTextAfterColon(event.getMessage())));
				}
			}

			if (event.getType() == ChatType.TELL) {
				if (isSpeakingAllPersonTells
						|| peopleToSpeakTellsFrom.contains(event.getSource())) {
					event.setHasSoundBeenHandled(speak(IcsUtils
							.stripTitles(event.getSource())
							+ " "
							+ getTextAfterColon(event.getMessage())));
				}
			}

			if (event.getType() == ChatType.WHISPER
					|| event.getType() == ChatType.KIBITZ) {
				if (!event.getSource().equals(getUserName())
						&& gamesToSpeakTellsFrom.contains(event.getGameId())) {
					event.setHasSoundBeenHandled(speak(IcsUtils
							.stripTitles(event.getSource())
							+ getTextAfterColon(event.getMessage())));
				}
			}

			int ignoreIndex = ignoringChatTypes.indexOf(event.getType());
			if (ignoreIndex != -1) {
				try {
					ignoringChatTypes.remove(ignoreIndex);
				} catch (ArrayIndexOutOfBoundsException aiobe) {
					// Eat it there could be a synchronization problem.�
				}
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
		String preference = StringUtils.defaultString(Raptor
				.getInstance()
				.getPreferences()
				.getString(
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
				if (!Raptor.getInstance().getWindow()
						.containsChannelItem(this, value)) {
					ChatUtils.openChannelTab(this, value, false);
				}
			} else if (type.equals("RegEx")) {
				if (!Raptor.getInstance().getWindow()
						.containsRegExItem(this, value)) {
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
		if (isConnected() && isLoggedIn()) {
			sendMessage("$$bugwho p", true, ChatType.BUGWHO_AVAILABLE_TEAMS);
		}
	}

	public void sendBugGamesMessage() {
		if (isConnected() && isLoggedIn()) {
			sendMessage("$$bugwho g", true, ChatType.BUGWHO_GAMES);
		}
	}

	public void sendBugUnpartneredBuggersMessage() {
		if (isConnected() && isLoggedIn()) {
			sendMessage("$$bugwho u", true, ChatType.BUGWHO_UNPARTNERED_BUGGERS);
		}
	}

	public void sendGetSeeksMessage() {
		if (isLoggedIn && isConnected()) {
			SeekType seekType = SeekType.valueOf(getPreferences().getString(
					PreferenceKeys.SEEK_OUTPUT_TYPE));

			switch (seekType) {
			case AllSeeks:
				sendMessage("$$sought all", true, ChatType.SEEKS);
				break;
			case FormulaFiltered:
				sendMessage("$$sought", true, ChatType.SEEKS);
				break;
			}
		}
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
		// long start = System.currentTimeMillis();
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

			try {
				String[] messages = breakUpMessage(builder);
				for (String current : messages) {
					if (!current.endsWith("\n")) {
						current += "\n";
					}
					messageProducer.send(current);
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
				t.printStackTrace(); // Used to track down issues when
										// developing. Dont remove.
				publishEvent(new ChatEvent(null, ChatType.INTERNAL, 
						L10n.getInstance().getString("err")
						+ t.getMessage()));
				disconnect();
			}

			if (!isHidingFromUser) {
				publishEvent(new ChatEvent(null, ChatType.OUTBOUND,
						message.trim()));
			}
		} else {
			publishEvent(new ChatEvent(null, ChatType.INTERNAL,
					L10n.getInstance().getString("unnToSend", 
							message, getShortName())));
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

	public void setSpeakingWhisperTells(String gameId,
			boolean isSpeakingWhisperTells) {
		if (isSpeakingWhisperTells) {
			if (!gamesToSpeakTellsFrom.contains(gameId)) {
				gamesToSpeakTellsFrom.add(gameId);
			}
		} else {
			gamesToSpeakTellsFrom.remove(gameId);
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
								+ Raptor.getInstance().getWindow()
										.getQuadrant(item).toString();
					} else if (chatConsoleItem.getController() instanceof RegExController) {
						RegExController controller = (RegExController) chatConsoleItem
								.getController();
						preference += (preference.equals("") ? "" : "`")
								+ "RegEx`"
								+ controller.getPattern()
								+ "`"
								+ Raptor.getInstance().getWindow()
										.getQuadrant(item).toString();
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
			Raptor.getInstance()
					.getPreferences()
					.setValue(
							getContext().getShortName() + "-"
									+ currentProfileName + "-"
									+ PreferenceKeys.CHANNEL_REGEX_TAB_INFO,
							preference);
			Raptor.getInstance().getPreferences().save();
		}
	}

	public void whisper(Game game, String whisper) {
		sendMessage("primary " + game.getId(), true);
		sendMessage("whisper " + whisper);
	}

	protected void addToAutoComplete(String word) {
		final String lowerCaseWord = word.toLowerCase();

		if (!autoCompleteList.contains(lowerCaseWord)) {
			autoCompleteList.add(lowerCaseWord);
			Collections.sort(autoCompleteList);
		}
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
			Raptor.getInstance().getWindow()
					.addRaptorWindowItem(mainConsoleWindowItem, false);
		} else if (!Raptor.getInstance().getWindow()
				.isBeingManaged(mainConsoleWindowItem)) {
			// Add a new main console to the raptor window since the existing
			// one is no longer being managed (it was already disposed).
			createMainConsoleWindowItem();
			Raptor.getInstance().getWindow()
					.addRaptorWindowItem(mainConsoleWindowItem, false);
		}
		// If its being managed no need to do anything it should adjust itself
		// as the state of the connector changes from the ConnectorListener it
		// registered.

		if (LOG.isInfoEnabled()) {
			LOG.info(getShortName() + " Connecting to "
					+ getPreferences().getString(profilePrefix + "server-url")
					+ " " + getPreferences().getInt(profilePrefix + "port"));
		}
		publishEvent(new ChatEvent(null, ChatType.INTERNAL, L10n.getInstance().getString("connTo")
				+ getPreferences().getString(profilePrefix + "server-url")
				+ " "
				+ getPreferences().getInt(profilePrefix + "port")
				+ " " + (getPreferences().getBoolean(
						profilePrefix + "timeseal-enabled") ? L10n.getInstance()
								.getString("withTs") : L10n.getInstance()
								.getString("withoutTs"))));

		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				try {
					isConnecting = true;

					boolean isTimesealEnabled = getPreferences().getBoolean(
							profilePrefix + "timeseal-enabled");

					messageProducer = new TimesealSocketMessageProducer(
							getPreferences().getString(
									profilePrefix + "server-url"),
							getPreferences().getInt(profilePrefix + "port"),
							getInitialTimesealString(), isTimesealEnabled,
							IcsConnector.this);

					publishEvent(new ChatEvent(null, ChatType.INTERNAL,
							L10n.getInstance().getString("timesString")
									+ getInitialTimesealString()));
					publishEvent(new ChatEvent(null, ChatType.INTERNAL,
							L10n.getInstance().getString("connected")));

					SoundService.getInstance().playSound("alert");

					if (LOG.isInfoEnabled()) {
						LOG.info(getShortName() + " Connection successful");
					}
				} catch (Throwable ce) {
					publishEvent(new ChatEvent(null, ChatType.INTERNAL,
							"Error: " + ce.getMessage()));

					// An error occured connecting so set auto connect to false
					// so the user can easily fix it on next connect.
					// It could be an invalid username/pw url/port.
					Raptor.getInstance()
							.getPreferences()
							.setValue(
									context.getPreferencePrefix()
											+ "auto-connect", false);

					disconnect();
					return;
				}
			}

			public String toString() {
				return "IcsConnector.connection intiliazation runnable";
			}
		});

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
	protected String drainInboundMessageBuffer(StringBuilder builder) {
		return drainInboundMessageBuffer(builder, builder.length());
	}

	/**
	 * Removes characters 0-index from inboundMessageBuffer and returns the
	 * string removed.
	 */
	protected String drainInboundMessageBuffer(StringBuilder builder, int index) {
		String result = builder.substring(0, index);
		builder.delete(0, index);
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

		if (getPreferences().getBoolean(
				context.getPreferencePrefix()
						+ PreferenceKeys.REMOVE_BLANK_LINES)) {
			text = text.replace("\n\n", "\n");
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

			public String toString() {
				return "IcsConnector.fireConnected runnable";
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

			public String toString() {
				return "IcsConnector.fireConnecting runnable";
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

			public String toString() {
				return "IcsConnector.fireDisconnected runnable";
			}
		});
	}

	protected String getInitialTimesealString() {
		return "TIMESTAMP|iv|OpenSeal|";
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
		if (!isConnected()) {
			return;
		}

		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				if (getPreferences().getBoolean(
						PreferenceKeys.CHAT_OPEN_PERSON_TAB_ON_PERSON_TELLS)
						&& event.getType() == ChatType.TELL) {
					ChatUtils.openPersonTab(IcsConnector.this,
							event.getSource(), false);
				} else if (getPreferences().getBoolean(
						PreferenceKeys.CHAT_OPEN_CHANNEL_TAB_ON_CHANNEL_TELLS)
						&& event.getType() == ChatType.CHANNEL_TELL) {
					ChatUtils.openChannelTab(IcsConnector.this,
							event.getChannel(), false);
				} else if (getPreferences().getBoolean(
						PreferenceKeys.CHAT_OPEN_PARTNER_TAB_ON_PTELLS)
						&& event.getType() == ChatType.PARTNER_TELL) {
					ChatUtils.openPartnerTab(IcsConnector.this, false);
				}
			}

			public String toString() {
				return "IcsConnector.handleOpeningTabs runnable";
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
		if (StringUtils.isBlank(message)) {
			return;
		}
		String firstWord = message.split(" ")[0];
		if (StringUtils.equalsIgnoreCase(firstWord, "getgame")
				|| StringUtils.equalsIgnoreCase(firstWord, "examine")
				|| StringUtils.equalsIgnoreCase(firstWord, "$$examine")
				|| StringUtils.equalsIgnoreCase(firstWord, "examin")
				|| StringUtils.equalsIgnoreCase(firstWord, "exami")
				|| StringUtils.equalsIgnoreCase(firstWord, "exam")
				|| StringUtils.equalsIgnoreCase(firstWord, "exa")
				|| StringUtils.equalsIgnoreCase(firstWord, "ex")
				|| StringUtils.equalsIgnoreCase(firstWord, "play")
				|| StringUtils.equalsIgnoreCase(firstWord, "seek")
				|| StringUtils.equalsIgnoreCase(firstWord, "match")
				|| StringUtils.equalsIgnoreCase(firstWord, "matc")
				|| StringUtils.equalsIgnoreCase(firstWord, "mat")
				|| StringUtils.equalsIgnoreCase(firstWord, "ma")
				|| StringUtils.equalsIgnoreCase(firstWord, "m")) {
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
						L10n.getInstance().getString("blockTell", event.getSource())));
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
	 * Returns true if the user is currently logged in.
	 */
	public boolean isLoggedIn() {
		return isConnected() && isLoggedIn;
	}

	/**
	 * This method is invoked by the run method when there is new text to be
	 * handled. It buffers text until a prompt is found then invokes
	 * parseMessage.
	 * 
	 * This method also handles login logic which is tricky.
	 */
	public void messageArrived(StringBuilder buffer) {

		// System.err.println("Message arrived (buffer): " + buffer);

		if (lastSendPingTime != 0 && (lagNotifyCounter % 10 == 0)) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					long currentTime = System.currentTimeMillis();
					lastPingTime = currentTime - lastSendPingTime;
					Raptor.getInstance().getWindow()
							.setPingTime(IcsConnector.this, lastPingTime);
					lastSendPingTime = 0;
				}

				public String toString() {
					return "IcsConnector.messageArrived runnable";
				}
			});
		}

		if (isLoggedIn) {

			// If we are logged in. Then parse out all the text between the
			// prompts.
			int promptIndex = -1;
			while ((promptIndex = buffer.indexOf(context.getRawPrompt())) != -1) {
				String message = drainInboundMessageBuffer(buffer, promptIndex
						+ context.getRawPrompt().length());
				parseMessage(message);
			}
		} else {

			// We are not logged in.
			// There are several complex cases here depending on the prompt
			// we are waiting on.
			// There is a login prompt, a password prompt, an enter prompt,
			// and also you have to handle invalid logins.
			int loggedInMessageIndex = buffer.indexOf(context
					.getLoggedInMessage());
			if (loggedInMessageIndex != -1) {
				for (int i = 0; i < buffer.length(); i++) {
					char character = buffer.charAt(i);
					if (LOGIN_CHARACTERS_TO_FILTER.indexOf(character) != -1) {
						buffer.deleteCharAt(i);
						i--;
					}
				}
				int nameStartIndex = buffer.indexOf(context
						.getLoggedInMessage())
						+ context.getLoggedInMessage().length();
				int endIndex = buffer.indexOf("****", nameStartIndex);

				if (endIndex != -1) {

					userName = IcsUtils.stripTitles(buffer.substring(
							nameStartIndex, endIndex).trim());
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
				int loginIndex = buffer.indexOf(context.getLoginPrompt());
				if (loginIndex != -1) {
					String event = drainInboundMessageBuffer(buffer, loginIndex
							+ context.getLoginPrompt().length());
					onLoginEvent(event, true);
				} else {
					int enterPromptIndex = buffer.indexOf(context
							.getEnterPrompt());
					if (enterPromptIndex != -1) {
						String event = drainInboundMessageBuffer(buffer,
								enterPromptIndex
										+ context.getEnterPrompt().length());
						onLoginEvent(event, false);
					} else {
						int passwordPromptIndex = buffer.indexOf(context
								.getPasswordPrompt());
						if (passwordPromptIndex != -1) {
							String event = drainInboundMessageBuffer(buffer,
									passwordPromptIndex
											+ context.getPasswordPrompt()
													.length());
							onLoginEvent(event, false);

						} else {
							int errorMessageIndex = buffer.indexOf(context
									.getLoginErrorMessage());
							if (errorMessageIndex != -1) {
								String event = drainInboundMessageBuffer(buffer);
								event = StringUtils.replaceChars(event,
										"����", "");
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
	protected void parseMessage(final String message) {
		try {
			String filteredMessage = filterTrailingPrompts(message);

			// This call will handle all game events, and return back a list of
			// ChatEvents to process.
			final ChatEvent[] events = context.getParser().parse(
					filteredMessage);

			ThreadService.getInstance().run(new Runnable() {
				public void run() {

					for (ChatEvent event : events) {
						event.setMessage(IcsUtils.maciejgFormatToUnicode(event
								.getMessage()));
						publishEvent(event);
					}
				}
			});
		} catch (RuntimeException re) {
			throw new RuntimeException("Error occured parsing message: "
					+ message, re);
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
						"\n?", true);
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
	protected void processChatEventScripts(final ChatEvent event) {
		if (chatEventScripts != null && chatEventScripts.length > 0) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					for (ChatEventScript script : chatEventScripts) {
						if (script.isActive()
								&& script.getChatType() == event.getType()
								|| script.getChatType() == ChatType.ALL) {
							script.execute(getChatScriptContext(event));
						}
					}
				}

				public String toString() {
					return "IcsConnector.processChatEventScripts runnable";
				}
			});
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
		chatEventScripts = ScriptService.getInstance().getChatEventScripts(
				getScriptConnectorType());
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
		if (!isConnected()) {
			return;
		}
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				if (StringUtils.isNotBlank(event.getSource())) {
					addToAutoComplete(event.getSource());
				}
			}

			public String toString() {
				return "IcsConnector.updateAutoComplete runnable";
			}
		});
	}

	protected boolean vetoMessage(String message) {
		if (!hasVetoPower) {
			return false;
		}
		boolean result = false;
		if (message.startsWith("set ptime")) {
			publishEvent(new ChatEvent(null, ChatType.INTERNAL, L10n
					.getInstance().getString("rapPtime")));
			return true;
		}
		return result;
	}

	protected abstract void writeExtendedCensorList();

	private void setBughouseService(BughouseService bughouseService) {
		this.bughouseService = bughouseService;
	}
}
