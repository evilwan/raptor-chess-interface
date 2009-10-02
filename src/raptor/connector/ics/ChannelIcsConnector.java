package raptor.connector.ics;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceStore;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.Connector;
import raptor.connector.ConnectorListener;
import raptor.game.Game;
import raptor.game.Move;
import raptor.game.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.script.GameScript;
import raptor.service.ChatService;
import raptor.service.GameService;
import raptor.service.SoundService;
import raptor.service.ThreadService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.chat.ChatConsoleWindowItem;
import raptor.swt.chat.controller.MainController;
import raptor.swt.chess.ChessBoardWindowItem;

/**
 * A work in progress.
 */
public abstract class ChannelIcsConnector implements Connector {
	private static final Log LOG = LogFactory.getLog(ChannelIcsConnector.class);
	protected StringBuilder inboundMessageBuffer = new StringBuilder(25000);
	protected boolean isLoggedIn = false;
	protected boolean hasSentLogin = false;
	protected boolean hasSentPassword = false;
	protected String currentProfileName;
	protected IcsConnectorContext context;
	protected ChatService chatService;
	protected GameService gameService;
	protected Thread daemonThread;
	protected HashMap<String, GameScript> gameScriptsMap = new HashMap<String, GameScript>();
	protected List<ConnectorListener> connectorListeners = Collections
			.synchronizedList(new ArrayList<ConnectorListener>(10));
	protected ByteBuffer inputBuffer = ByteBuffer.allocate(25000);
	// protected ReadableByteChannel inputChannel;
	// protected Socket socket;
	protected SocketChannel channel;
	protected String userName;
	protected long lastSendTime;
	protected boolean isConnecting;
	protected boolean isTimesealing;
	private ByteBuffer writeBuffer = ByteBuffer.allocate(2000);
	private long creationTime = System.currentTimeMillis();
	private final byte[] cryptBuffer = new byte[5000];
	private byte[] timesealKey = "Timestamp (FICS) v1.0 - programmed by Henrik Gram."
			.getBytes();

	protected ChatConsoleWindowItem mainConsoleWindowItem;

	/**
	 * Adds the game windows to the RaptorAppWindow.
	 */
	protected GameServiceListener gameServiceListener = new GameServiceAdapter() {

		@Override
		public void gameCreated(Game game) {
			Raptor.getInstance().getRaptorWindow().addRaptorWindowItem(
					new ChessBoardWindowItem(IcsUtils.buildController(game,
							ChannelIcsConnector.this)));
		}
	};

	/**
	 * Constructs an IcsConnector with the specified context.
	 * 
	 * @param context
	 */
	protected ChannelIcsConnector(IcsConnectorContext context) {
		this.context = context;
		chatService = new ChatService(this);
		gameService = new GameService();
		refreshGameScripts();
		gameService.addGameServiceListener(gameServiceListener);

	}

	/**
	 * Adds a connector listener to the connector.
	 */
	public void addConnectorListener(ConnectorListener listener) {
		connectorListeners.add(listener);
	}

	/**
	 * Adds a game script to the set of scripts this connector manages and saves
	 * the script.
	 */
	public void addGameScript(GameScript script) {
		gameScriptsMap.put(script.getName(), script);
		script.save();
	}

	/**
	 * Connects to ics using the settings in preferences.
	 */
	public void connect() {
		connect(Raptor.getInstance().getPreferences().getString(
				context.getPreferencePrefix() + "profile"));
	}

	/**
	 * Connects with the specified profile name.
	 */
	protected void connect(final String profileName) {
		if (isConnected()) {
			throw new IllegalStateException("You are already connected to "
					+ getShortName() + " . Disconnect before invoking connect.");
		}

		resetConnectionStateVars();

		currentProfileName = profileName;

		final String profilePrefix = context.getPreferencePrefix()
				+ profileName + "-";

		if (LOG.isDebugEnabled()) {
			LOG.debug("Profile Prefix=" + profilePrefix);
		}

		if (mainConsoleWindowItem == null) {
			// Add the main console tab to the raptor window.
			createMainConsoleWindowItem();
			Raptor.getInstance().getRaptorWindow().addRaptorWindowItem(
					mainConsoleWindowItem, false);
		} else if (!Raptor.getInstance().getRaptorWindow().isBeingManaged(
				mainConsoleWindowItem)) {
			// Add a new main console to the raptor window since the existing
			// one is no longer being managed (it was already disposed).
			createMainConsoleWindowItem();
			Raptor.getInstance().getRaptorWindow().addRaptorWindowItem(
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
						isTimesealing = true;
					}

					channel = SocketChannel.open();
					channel.configureBlocking(true);
					channel.connect(new InetSocketAddress(getPreferences()
							.getString(profilePrefix + "server-url"),
							getPreferences().getInt(profilePrefix + "port")));

					if (isTimesealing) {
						send(getInitialTimesealString());
					}

					publishEvent(new ChatEvent(null, ChatType.INTERNAL,
							"Connected"));

					SoundService.getInstance().playSound("alert");

					daemonThread = new Thread(new Runnable() {
						public void run() {
							messageLoop();
						}
					});
					daemonThread.setName(getShortName() + " ConnectorDaemon");
					daemonThread.setPriority(Thread.MAX_PRIORITY);
					daemonThread.start();

					if (LOG.isInfoEnabled()) {
						LOG.info(getShortName() + " Connection successful");
					}
				} catch (Exception ce) {
					publishEvent(new ChatEvent(null, ChatType.INTERNAL,
							"Error: " + ce.getMessage()));
					disconnect();
					return;
				}

			}
		});
		isConnecting = true;

		fireConnecting();
	}

	protected void createMainConsoleWindowItem() {
		mainConsoleWindowItem = new ChatConsoleWindowItem(new MainController(
				this));
	}

	private void crypt(String message, ByteBuffer bufferToWriteTo,
			long timestamp) {
		if (isTimesealing) {
			byte[] bytesToWrite = message.getBytes();
			int bytesInLength = bytesToWrite.length;
			System.arraycopy(bytesToWrite, 0, cryptBuffer, 0,
					bytesToWrite.length);
			cryptBuffer[bytesInLength++] = 24;
			byte abyte1[] = Long.toString(timestamp).getBytes();
			System.arraycopy(abyte1, 0, cryptBuffer, bytesInLength,
					abyte1.length);
			bytesInLength += abyte1.length;
			cryptBuffer[bytesInLength++] = 25;
			int j = bytesInLength;
			for (bytesInLength += 12 - bytesInLength % 12; j < bytesInLength;)
				cryptBuffer[j++] = 49;

			for (int k = 0; k < bytesInLength; k++)
				cryptBuffer[k] = (byte) (cryptBuffer[k] | 0x80);

			for (int i1 = 0; i1 < bytesInLength; i1 += 12) {
				byte byte0 = cryptBuffer[i1 + 11];
				cryptBuffer[i1 + 11] = cryptBuffer[i1];
				cryptBuffer[i1] = byte0;
				byte0 = cryptBuffer[i1 + 9];
				cryptBuffer[i1 + 9] = cryptBuffer[i1 + 2];
				cryptBuffer[i1 + 2] = byte0;
				byte0 = cryptBuffer[i1 + 7];
				cryptBuffer[i1 + 7] = cryptBuffer[i1 + 4];
				cryptBuffer[i1 + 4] = byte0;
			}

			int l1 = 0;
			for (int j1 = 0; j1 < bytesInLength; j1++) {
				cryptBuffer[j1] = (byte) (cryptBuffer[j1] ^ timesealKey[l1]);
				l1 = (l1 + 1) % timesealKey.length;
			}

			for (int k1 = 0; k1 < bytesInLength; k1++)
				cryptBuffer[k1] = (byte) (cryptBuffer[k1] - 32);

			cryptBuffer[bytesInLength++] = -128;
			cryptBuffer[bytesInLength++] = 10;
			bufferToWriteTo.put(cryptBuffer, 0, bytesInLength);
		} else {
			bufferToWriteTo.put(message.getBytes());
		}
	}

	/**
	 * Disconnects from the ics.
	 */
	public void disconnect() {
		synchronized (this) {
			if (isConnected()) {
				try {
					if (channel != null) {
						try {
							channel.close();
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
				} catch (Throwable t) {
				} finally {
					channel = null;
					daemonThread = null;
				}

				try {
					String messageLeftInBuffer = drainInboundMessageBuffer();
					if (StringUtils.isNotBlank(messageLeftInBuffer)) {
						parseMessage(messageLeftInBuffer);
					}

				} catch (Throwable t) {
				} finally {
				}
				resetConnectionStateVars();
				publishEvent(new ChatEvent(null, ChatType.INTERNAL,
						"Disconnected"));
				fireDisconnected();
				LOG.error("Disconnected from " + getShortName());
			}
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

		if (gameScriptsMap != null) {
			gameScriptsMap.clear();
			gameScriptsMap = null;
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

		if (channel != null) {
			try {
				channel.close();
			} catch (Throwable t) {
			}
			channel = null;
		}

		LOG.info("Disposed " + getShortName() + "Connector");
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

	protected void fireConnected() {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				synchronized (connectorListeners) {
					for (ConnectorListener listener : connectorListeners) {
						System.err.println("Invoking connected. " + listener);
						listener.onConnect();
						System.err.println("Done connected.");
					}
				}
			}
		});
	}

	protected void fireConnecting() {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				synchronized (connectorListeners) {
					for (ConnectorListener listener : connectorListeners) {
						System.err.println("Invoking connecting. " + listener);
						listener.onConnecting();
						System.err.println("Done connecting.");
					}
				}
			}
		});
	}

	protected void fireDisconnected() {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				synchronized (connectorListeners) {
					for (ConnectorListener listener : connectorListeners) {
						System.err.println("Invoking disconnecting. "
								+ listener);
						listener.onConnecting();
						System.err.println("Done disconnecting.");

					}
				}
			}
		});
	}

	public String[][] getChannelActions(String channel) {
		return new String[][] {
				new String[] { "Add Channel " + channel, "+channel " + channel },
				new String[] { "Remove Channel " + channel,
						"-channel " + channel },
				new String[] { "In Channel " + channel, "in " + channel } };
	}

	public String getChannelTabPrefix(String channel) {
		return "tell " + channel + " ";
	}

	public ChatService getChatService() {
		return chatService;
	}

	public String getDescription() {
		return context.getDescription();
	}

	public String[][] getGameIdActions(String gameId) {
		return new String[][] {
				new String[] { "Observe game " + gameId, "observe " + gameId },
				new String[] { "All observers in game " + gameId,
						"allobs " + gameId },
				{ "Move list for game " + gameId, "moves " + gameId } };
	}

	public GameScript getGameScript(String name) {
		return gameScriptsMap.get(name);
	}

	public GameScript[] getGameScripts() {
		return gameScriptsMap.values().toArray(new GameScript[0]);
	}

	public GameService getGameService() {
		return gameService;
	}

	protected String getInitialTimesealString() {
		return Raptor.getInstance().getPreferences().getString(
				PreferenceKeys.TIMESEAL_INIT_STRING);
	}

	public String getPartnerTellPrefix() {
		return "ptell ";
	}

	public String[][] getPersonActions(String person) {
		return new String[][] {
				new String[] { "Finger " + person, "finger " + person },
				new String[] { "Observe " + person, "observe " + person },
				new String[] { "History " + person, "history " + person },
				new String[] { "Follow " + person, "follow " + person },
				new String[] { "Partner " + person, "partner " + person },
				new String[] { "Vars " + person, "vars " + person },
				new String[] { "Censor " + person, "+censor " + person },
				new String[] { "Uncensor " + person, "-censor " + person },
				new String[] { "Noplay " + person, "+noplay " + person },
				new String[] { "Unnoplay " + person, "noplay " + person } };
	}

	public String getPersonTabPrefix(String person) {
		return "tell " + person + " ";
	}

	public PreferenceStore getPreferences() {
		return Raptor.getInstance().getPreferences();
	}

	public String getPrompt() {
		return context.getPrompt();
	}

	public String getShortName() {
		return context.getShortName();
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

	public boolean isConnected() {
		return channel != null && channel.isConnected() && daemonThread != null;
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

	public void makeMove(Game game, Move move) {
		sendMessage(move.toString());
	}

	/**
	 * The messageLoop. Reads the inputChannel and then invokes publishInput
	 * with the text read. Should really never be invoked.
	 */
	protected void messageLoop() {
		try {
			while (true) {
				if (isConnected()) {
					int numRead = channel.read(inputBuffer);
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
			LOG.debug("Error occured in read", t);
			if (t instanceof IOException) {
				LOG
						.debug(
								context.getShortName()
										+ "Connector "
										+ "IOException occured in DaemonRun (These are common when disconnecting and ignorable)",
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

	public void onAbortKeyPress() {
		sendMessage("abort");
	}

	public void onAcceptKeyPress() {
		sendMessage("accept");
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
		sendMessage("decline");
	}

	public void onDraw(Game game) {
		sendMessage("draw");
	}

	public void onError(String message) {
		onError(message, null);
	}

	public void onError(String message, Throwable t) {
		String errorMessage = IcsUtils
				.cleanupMessage("Critical error occured! We are trying to make Raptor "
						+ "bug free and we need your help! Please take a moment to report this "
						+ "error at\nhttp://code.google.com/p/raptor-chess-interface/issues/list\n\n Issue: "
						+ message
						+ (t == null ? "" : "\n"
								+ ExceptionUtils.getFullStackTrace(t)));
		publishEvent(new ChatEvent(null, ChatType.INTERNAL, errorMessage));
	}

	public void onExamineModeBack(Game game) {
		sendMessage("back");
	}

	public void onExamineModeCommit(Game game) {
		sendMessage("commit");
	}

	public void onExamineModeFirst(Game game) {
		sendMessage("back 300");
	}

	public void onExamineModeForward(Game game) {
		sendMessage("forward 1");
	}

	public void onExamineModeLast(Game game) {
		sendMessage("forward 300");
	}

	public void onExamineModeRevert(Game game) {
		sendMessage("revert");
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
		String profilePrefix = "fics-" + currentProfileName + "-";
		if (isLoginPrompt) {
			if (getPreferences().getBoolean(profilePrefix + "is-anon-guest")
					&& !hasSentLogin) {
				parseMessage(message);
				hasSentLogin = true;
				sendMessage("guest");
			} else if (!hasSentLogin) {
				parseMessage(message);
				hasSentLogin = true;
				String handle = getPreferences().getString(
						profilePrefix + "user-name");
				if (StringUtils.isNotBlank(handle)) {
					sendMessage(handle);
				}
			} else {
				parseMessage(message);
			}
		} else {
			if (getPreferences().getBoolean(profilePrefix + "is-anon-guest")
					&& !hasSentPassword) {
				hasSentPassword = true;
				parseMessage(message);
				sendMessage("");
			} else if (getPreferences().getBoolean(
					profilePrefix + "is-named-guest")
					&& !hasSentPassword) {
				hasSentPassword = true;
				parseMessage(message);
				sendMessage("");
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
		System.err.println("Buffers contents = " + inboundMessageBuffer);
		if (lastSendTime != 0) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					long currentTime = System.currentTimeMillis();
					Raptor.getInstance().getRaptorWindow().setPingTime(
							ChannelIcsConnector.this,
							currentTime - lastSendTime);
					lastSendTime = 0;
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
				int nameStartIndex = inboundMessageBuffer.indexOf(context
						.getLoggedInMessage())
						+ context.getLoggedInMessage().length();
				int endIndex = inboundMessageBuffer.indexOf("****",
						nameStartIndex);
				if (endIndex != -1) {
					userName = IcsUtils.removeTitles(inboundMessageBuffer
							.substring(nameStartIndex, endIndex).trim());
					LOG.info(context.getShortName() + "Connector "
							+ "login complete. userName=" + userName);
					isLoggedIn = true;
					onSuccessfulLogin();
					// Since we are now logged in, just buffer the text
					// received and
					// invoke parseMessage when the prompt arrives.
				} else {
					// We have yet to receive the **** closing message so
					// wait
					// until it arrives.
					System.err.println("Danging: " + inboundMessageBuffer);
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
								parseMessage(event);
							} else {
								System.err.println("Danging: "
										+ inboundMessageBuffer);
							}
						}
					}
				}
			}
		}
	}

	public void onRematchKeyPress() {
		sendMessage("rematch");
	}

	/**
	 * Resigns the specified game.
	 */
	public void onResign(Game game) {
		sendMessage("resign");
	}

	public void onSetupClear(Game game) {
		sendMessage("bsetup clear");
	}

	public void onSetupClearSquare(Game game, int square) {
		sendMessage("x@" + GameUtils.getSan(square));
	}

	public void onSetupComplete(Game game) {
		sendMessage("bsetup done");
	}

	public void onSetupFromFEN(Game game, String fen) {
		sendMessage("bsetup fen " + fen);
	}

	public void onSetupStartPosition(Game game) {
		sendMessage("bsetup start");
	}

	protected abstract void onSuccessfulLogin();

	public void onUnexamine(Game game) {
		sendMessage("unexamine");
	}

	public void onUnobserve(Game game) {
		sendMessage("unobs " + game.getId());
	}

	public String parseChannel(String word) {
		return IcsUtils.stripChannel(word);
	}

	public String parseGameId(String word) {
		return IcsUtils.stripGameId(word);
	}

	/**
	 * Processes a message. If the user is logged in, message will be all of the
	 * text received since the last prompt from fics. If the user is not logged
	 * in, message will be all of the text received since the last login prompt.
	 * 
	 * message will always use \n as the line delimiter.
	 */
	protected void parseMessage(String message) {
		// Handle and remove game messages.
		String afterGameMessages = context.parser.parseOutAndProcessGameEvents(
				getGameService(), message);

		// Don't send anything if the message is only the prompt.
		if (!afterGameMessages.trim().equals(context.getPrompt())) {

			// Parse what is left into ChatEvents and publish them.
			ChatEvent[] events = context.getParser().parse(afterGameMessages);
			for (ChatEvent event : events) {
				event.setMessage(IcsUtils
						.maciejgFormatToUnicode(afterGameMessages));
				publishEvent(event);
			}
		}
	}

	public String parsePerson(String word) {
		return IcsUtils.stripWord(word);
	}

	/**
	 * Publishes the specified event to the chat service. Currently all messages
	 * are published on separate threads via ThreadService.
	 */
	protected void publishEvent(final ChatEvent event) {
		if (chatService != null) { // Could have been disposed.
			if (LOG.isDebugEnabled()) {
				LOG.debug("Publishing event : " + event);
			}

			// It is interesting to note messages are handled sequentially
			// up to this point. chatService will publish the event
			// asynchronously.
			chatService.publishChatEvent(event);
		}
	}

	public void refreshGameScripts() {
		gameScriptsMap.clear();
		GameScript[] scripts = GameScript.getGameScripts(this);
		for (GameScript script : scripts) {
			gameScriptsMap.put(script.getName(), script);
		}
	}

	/**
	 * Removes a connector listener from the connector.
	 */
	public void removeConnectorListener(ConnectorListener listener) {
		connectorListeners.remove(listener);
	}

	public void removeGameScript(GameScript script) {
		script.delete();
		gameScriptsMap.remove(script.getName());
	}

	/**
	 * Resets state variables related to the connection state.
	 */
	protected void resetConnectionStateVars() {
		isLoggedIn = false;
		hasSentLogin = false;
		hasSentPassword = false;
		isTimesealing = false;
		isConnecting = false;
		channel = null;
		daemonThread = null;
	}

	private boolean send(String message) {
		boolean result = true;
		synchronized (writeBuffer) {
			try {
				System.out.println("Writing...'" + message + "'");
				crypt(message, writeBuffer, System.currentTimeMillis()
						- creationTime);
				writeBuffer.rewind();
				System.out.println("Buffer..." + writeBuffer);
				channel.write(writeBuffer);
				writeBuffer.clear();
				lastSendTime = System.currentTimeMillis();
			} catch (Throwable t) {
				publishEvent(new ChatEvent(null, ChatType.INTERNAL, "Error: "
						+ t.getMessage()));
				disconnect();
				result = false;
			}
		}
		return result;
	}

	public void sendMessage(String message) {
		sendMessage(message, false);
	}

	public void sendMessage(String message, boolean isHidingFromUser) {
		if (isConnected()) {
			StringBuilder builder = new StringBuilder(message);
			IcsUtils.filterOutbound(builder);
			message = builder.toString();

			if (LOG.isDebugEnabled()) {
				LOG.debug(context.getShortName() + "Connector Sending: "
						+ message.trim());
			}

			if (send(message.toString())) {
				if (!isHidingFromUser) {
					// Wrap the text before publishing an outbound event.
					publishEvent(new ChatEvent(null, ChatType.OUTBOUND, message
							.trim()));
				}
			}
		} else {
			publishEvent(new ChatEvent(null, ChatType.INTERNAL,
					"Error: Unable to send " + message + " to "
							+ getShortName()
							+ ". There is currently no connection."));
		}
	}
}
