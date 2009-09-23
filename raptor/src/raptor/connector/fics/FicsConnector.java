package raptor.connector.fics;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceStore;

import raptor.chat.ChatEvent;
import raptor.chat.ChatTypes;
import raptor.connector.Connector;
import raptor.game.Game;
import raptor.game.Move;
import raptor.pref.PreferenceKeys;
import raptor.script.GameScript;
import raptor.service.ChatService;
import raptor.service.GameService;
import raptor.service.SoundService;
import raptor.service.ThreadService;
import raptor.util.RaptorStringTokenizer;
import free.freechess.timeseal.TimesealingSocket;

public class FicsConnector implements Connector, PreferenceKeys {

	/**
	 * The runnable that is executed in the DaemonThread. This class handles
	 * reading the text between prompts and invoking parseMessage when it
	 * encounters a new message.
	 * 
	 * This class also handles the auto login logic as well.
	 */
	protected class DaemonRunnable implements Runnable {
		protected StringBuilder inboundMessageBuffer = new StringBuilder(25000);
		protected boolean isLoggedIn = false;
		protected boolean hasSentLogin = false;
		protected boolean hasSentPassword = false;

		/**
		 * Removes all of the characters from inboundMessageBuffer and returns
		 * the string removed.
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
		 * Processes a login message. Handles sending the user name and password
		 * information and the enter if prompted to hit enter if logging in as a
		 * guest.
		 * 
		 * @param message
		 * @param isLoginPrompt
		 */
		protected void onLoginEvent(String message, boolean isLoginPrompt) {
			if (isLoginPrompt) {
				if (getPreferences().getBoolean(FICS_IS_ANON_GUEST)
						&& !hasSentLogin) {
					parseMessage(message);
					hasSentLogin = true;
					sendMessage("guest");
				} else if (!hasSentLogin) {
					parseMessage(message);
					hasSentLogin = true;
					String handle = getPreferences().getString(
							PreferenceKeys.FICS_USER_NAME);
					if (StringUtils.isNotBlank(handle)) {
						sendMessage(handle);
					}
				} else {
					parseMessage(message);
				}
			} else {
				if (getPreferences().getBoolean(FICS_IS_ANON_GUEST)
						&& !hasSentPassword) {
					hasSentPassword = true;
					parseMessage(message);
					sendMessage("");
				} else if (getPreferences().getBoolean(FICS_IS_NAMED_GUEST)
						&& !hasSentPassword) {
					hasSentPassword = true;
					parseMessage(message);
					sendMessage("");
				} else if (!hasSentPassword) {
					hasSentPassword = true;
					parseMessage(message);
					String password = getPreferences().getString(
							PreferenceKeys.FICS_PASSWORD);
					if (StringUtils.isNotBlank(password)) {
						// Don't show the users password.
						sendMessage(password, true);
					}
				} else {
					System.err.println("Handling : catch all");
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
			if (isLoggedIn) {

				// If we are logged in. Then parse out all the text between the
				// prompts.
				int promptIndex = -1;
				while ((promptIndex = inboundMessageBuffer.indexOf(RAW_PROMPT)) != -1) {
					String message = drainInboundMessageBuffer(promptIndex
							+ RAW_PROMPT.length());
					parseMessage(message);
				}
			} else {
				// We are not logged in.
				// There are several complex cases here depending on the prompt
				// we are waiting on.
				// There is a login prompt, a password prompt, an enter prompt,
				// and also you have to handle invalid logins.
				int loggedInMessageIndex = inboundMessageBuffer
						.indexOf(LOGGED_IN_MSG);
				if (loggedInMessageIndex != -1) {
					int nameStartIndex = inboundMessageBuffer
							.indexOf(LOGGED_IN_MSG)
							+ LOGGED_IN_MSG.length();
					int endIndex = inboundMessageBuffer.indexOf("****",
							nameStartIndex);
					if (endIndex != -1) {
						userName = FicsUtils.removeTitles(inboundMessageBuffer
								.substring(nameStartIndex, endIndex).trim());
						LOG.info("login complete. userName=" + userName);
						isLoggedIn = true;
						onSuccessfulLogin();
						// Since we are now logged in, just buffer the text
						// received and
						// invoke parseMessage when the prompt arrives.
					} else {
						// We have yet to receive the **** closing message so
						// wait
						// until it arrives.
					}
				} else {
					int loginIndex = inboundMessageBuffer.indexOf(LOGIN_PROMPT);
					if (loginIndex != -1) {
						String event = drainInboundMessageBuffer(loginIndex
								+ LOGIN_PROMPT.length());
						onLoginEvent(event, true);
					} else {
						int enterPromptIndex = inboundMessageBuffer
								.indexOf(ENTER_PROMPT);
						if (enterPromptIndex != -1) {
							String event = drainInboundMessageBuffer(enterPromptIndex
									+ ENTER_PROMPT.length());
							onLoginEvent(event, false);
						} else {
							int passwordPromptIndex = inboundMessageBuffer
									.indexOf(PASSWORD_PROMPT);
							if (passwordPromptIndex != -1) {
								String event = drainInboundMessageBuffer(passwordPromptIndex
										+ PASSWORD_PROMPT.length());
								onLoginEvent(event, false);

							} else {
								int errorMessageIndex = inboundMessageBuffer
										.indexOf(LOGIN_ERROR_MESSAGE);
								if (errorMessageIndex != -1) {
									String event = drainInboundMessageBuffer();
									parseMessage(event);
								}
							}
						}
					}
				}
			}
		}

		/**
		 * Cleans up the message by ensuring only \n is used as a line
		 * terminator. \r\n and \r may be used depending on the operating
		 * system.
		 */
		public String cleanupMessage(String message) {
			return StringUtils.remove(message, '\r');
		}

		/**
		 * The run method. Reads the inputChannel and then invokes publishInput
		 * with the text read.
		 */
		public void run() {
			try {
				while (true) {
					if (isConnected()) {
						int numRead = inputChannel.read(inputBuffer);
						if (numRead > 0) {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Read " + numRead + " bytes.");
							}
							inputBuffer.rewind();
							byte[] bytes = new byte[numRead];
							inputBuffer.get(bytes);
							inboundMessageBuffer
									.append(cleanupMessage(new String(bytes)));
							onNewInput();
							inputBuffer.clear();
						} else {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Read 0 bytes disconnecting.");
							}
							disconnect();
							break;
						}
						Thread.sleep(50);
					} else {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Not connected disconnecting.");
						}
						disconnect();
						break;
					}
				}
			} catch (Throwable t) {
				LOG.warn("Error occured in read", t);
				disconnect();
			} finally {
				LOG.debug("Leaving readInput");
			}
		}

		/**
		 * Processes a message. If the user is logged in, message will be all of
		 * the text received since the last prompt from fics. If the user is not
		 * logged in, message will be all of the text received since the last
		 * login prompt.
		 * 
		 * message will always use \n as the line delimiter.
		 */
		protected void parseMessage(String message) {
			// Handle and remove game messages.
			String afterGameMessages = parser.parseOutAndProcessGameEvents(
					getGameService(), message);

			// Don't send anything if the message is only the prompt.
			if (!afterGameMessages.trim().equals(PROMPT)) {

				// Parse what is left into ChatEvents and publish them.
				ChatEvent[] events = parser.parse(afterGameMessages);
				for (ChatEvent event : events) {
					event.setMessage(FicsUtils
							.maciejgFormatToUnicode(afterGameMessages));
					publishEvent(event);
				}
			}
		}
	}

	public static final String ENTER_PROMPT = "\":";
	private static final Log LOG = LogFactory.getLog(FicsConnector.class);
	public static final String LOGGED_IN_MSG = "**** Starting FICS session as ";
	public static final String LOGIN_ERROR_MESSAGE = "\n*** ";
	public static final String LOGIN_PROMPT = "login: ";
	public static final String PASSWORD_PROMPT = "password:";
	public static final String RAW_PROMPT = "\nfics% ";
	public static final int RAW_PROMPT_LENGTH = RAW_PROMPT.length();
	public static final String PROMPT = "fics%";

	protected ChatService chatService = new ChatService();
	protected Thread daemonThread;
	protected DaemonRunnable daemonRunnable;
	protected HashMap<String, GameScript> gameScriptsMap = new HashMap<String, GameScript>();
	protected ByteBuffer inputBuffer = ByteBuffer.allocate(25000);
	protected ReadableByteChannel inputChannel;
	protected FicsParser parser = new FicsParser();
	protected PreferenceStore preferences;
	protected Socket socket;
	protected String userName;

	public FicsConnector() {
		refreshGameScripts();
	}

	/**
	 * Adds a game script to the set of scripts this connector manages and svaes
	 * the script.
	 */
	public void addGameScript(GameScript script) {
		gameScriptsMap.put(script.getName(), script);
		script.save();
	}

	/**
	 * Connects to fics using the settings in preferences.
	 */
	public void connect() {
		if (isConnected()) {
			throw new IllegalStateException(
					"You are already connected. Disconnect before invoking connect.");
		}

		LOG.info("Connecting to " + preferences.getString(FICS_SERVER_URL)
				+ " " + preferences.getInt(FICS_PORT));
		LOG.info("Trying to connect");
		publishEvent(new ChatEvent(
				null,
				ChatTypes.INTERNAL,
				"Connecting to "
						+ preferences.getString(FICS_SERVER_URL)
						+ " "
						+ preferences.getInt(FICS_PORT)
						+ (getPreferences().getBoolean(FICS_TIMESEAL_ENABLED) ? " with "
								: " without ") + "timeseal ..."));

		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				try {

					if (getPreferences().getBoolean(FICS_TIMESEAL_ENABLED)) {
						// TO DO: rewrite TimesealingSocket to use a
						// SocketChannel.
						socket = new TimesealingSocket(preferences
								.getString(FICS_SERVER_URL), preferences
								.getInt(FICS_PORT));
					} else {
						socket = new Socket(preferences
								.getString(FICS_SERVER_URL), preferences
								.getInt(FICS_PORT));
					}
					publishEvent(new ChatEvent(null, ChatTypes.INTERNAL,
							"Connected"));

					inputChannel = Channels.newChannel(socket.getInputStream());

					SoundService.getInstance().playSound("alert");
					daemonRunnable = new DaemonRunnable();
					daemonThread = new Thread(daemonRunnable);
					daemonThread.setName("FicsConnectorDaemon");
					daemonThread.setPriority(Thread.MAX_PRIORITY);
					daemonThread.start();

					LOG.info("Connection successful");
				} catch (Exception ce) {
					publishEvent(new ChatEvent(null, ChatTypes.INTERNAL,
							"Error: " + ce.getMessage()));
					disconnect();
					return;
				}

			}
		});
	}

	/**
	 * Disconnects from fics.
	 */
	@SuppressWarnings("deprecation")
	public void disconnect() {
		synchronized (this) {
			if (!isConnected()) {
				try {
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
								// Make sure the thread is dead.
								// There are no synchronized blocks to worry
								// about
								// so its ok to kill it off.
								daemonThread.stop();
							}
						} catch (Throwable t) {
						}
					}
				} catch (Throwable t) {
				} finally {
					socket = null;
					inputChannel = null;
					daemonThread = null;
				}

				try {
					if (daemonRunnable != null) {
						// If anything was buffered, process it.
						String messageLeftInBuffer = daemonRunnable
								.drainInboundMessageBuffer();
						if (StringUtils.isNotBlank(messageLeftInBuffer)) {
							daemonRunnable.parseMessage(messageLeftInBuffer);
						}
					}
				} catch (Throwable t) {
				} finally {
					daemonRunnable = null;
				}

				publishEvent(new ChatEvent(null, ChatTypes.INTERNAL,
						"Disconnected"));
				LOG.error("Disconnected from FicsConnection.");
			}
		}
	}

	public ChatService getChatService() {
		return chatService;
	}

	public String getDescription() {
		return "Free Internet Chess Server";
	}

	public GameScript getGameScript(String name) {
		return gameScriptsMap.get(name);
	}

	public GameScript[] getGameScripts() {
		return gameScriptsMap.values().toArray(new GameScript[0]);
	}

	public GameService getGameService() {
		// TODO Auto-generated method stub
		return null;
	}

	public PreferenceStore getPreferences() {
		return preferences;
	}

	public String getPrompt() {
		return PROMPT;
	}

	public String getShortName() {
		return "fics";
	}

	public String getTellToString(String handle) {
		return "tell " + handle + " ";
	}

	public boolean isConnected() {
		return socket != null && inputChannel != null && daemonThread != null
				&& daemonRunnable != null;
	}

	public void makeMove(Game game, Move move) {
		sendMessage(move.getSan());
	}

	public void onAbortKeyPress() {
		sendMessage("abort");
	}

	public void onAcceptKeyPress() {
		sendMessage("accept");
	}

	public void onDeclineKeyPress() {
		sendMessage("decline");
	}

	public void onDraw(Game game) {
		sendMessage("draw");
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

	public void onRematchKeyPress() {
		sendMessage("rematch");
	}

	protected void onSuccessfulLogin() {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				sendMessage("iset defprompt 1", true);
				sendMessage("iset gameinfo 1", true);
				sendMessage("iset ms 1", true);
				sendMessage("iset allresults 1", true);
				sendMessage(
						"iset premove "
								+ (getPreferences().getBoolean(
										BOARD_PREMOVE_ENABLED) ? "1" : "0"),
						true);
				sendMessage(
						"iset smartmove "
								+ (getPreferences().getBoolean(
										BOARD_SMARTMOVE_ENABLED) ? "1" : "0"),
						true);
				sendMessage("set interface "
						+ getPreferences().getString(APP_NAME));
				sendMessage("set style 12", true);
				sendMessage("set bell 0", true);

				String loginScript = getPreferences().getString(
						FICS_LOGIN_SCRIPT);
				if (StringUtils.isNotBlank(loginScript)) {
					RaptorStringTokenizer tok = new RaptorStringTokenizer(
							loginScript, "\n\r");
					while (tok.hasMoreTokens()) {
						try {
							Thread.sleep(50L);
						} catch (InterruptedException ie) {
						}
						sendMessage(tok.nextToken().trim());
					}
				}
			}
		});
	}

	/**
	 * Publishes the specified event to the chat service. Currently all messages
	 * are published on seperate threads via ThreadService.
	 */
	protected void publishEvent(final ChatEvent event) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Publishing event : " + event);
		}

		// It is interesting to note messages are handled sequentially
		// up to this point.
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				chatService.publishChatEvent(event);
			}
		});
	}

	public void refreshGameScripts() {
		gameScriptsMap.clear();
		GameScript[] scripts = GameScript.getGameScripts(this);
		for (GameScript script : scripts) {
			gameScriptsMap.put(script.getName(), script);
		}
	}

	public void removeGameScript(GameScript script) {
		script.delete();
		gameScriptsMap.remove(script.getName());
	}

	public void sendMessage(String message, boolean isHidingFromUser) {
		if (isConnected()) {
			StringBuilder builder = new StringBuilder(message);
			FicsUtils.filterOutbound(builder);
			message = builder.toString();

			if (LOG.isDebugEnabled()) {
				LOG.debug("Fics Conector Sending: " + message.trim());
			}

			// Only one thread at a time should write to the socket.
			synchronized (socket) {
				try {
					socket.getOutputStream().write(message.getBytes());
					socket.getOutputStream().flush();
				} catch (Throwable t) {
					publishEvent(new ChatEvent(null, ChatTypes.INTERNAL,
							"Error: " + t.getMessage()));
					disconnect();
				}
			}

			if (!isHidingFromUser) {
				// Wrap the text before publishing an outbound event.
				publishEvent(new ChatEvent(null, ChatTypes.OUTBOUND, message
						.trim()));
			}
		} else {
			publishEvent(new ChatEvent(null, ChatTypes.INTERNAL,
					"Error: Unable to send " + message
							+ ". There is currently no connection."));
		}
	}

	public void sendMessage(String message) {
		sendMessage(message, false);
	}

	public void setPreferences(PreferenceStore preferences) {
		this.preferences = preferences;
	}

}
