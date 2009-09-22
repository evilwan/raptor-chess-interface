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
import raptor.connector.fics.parser.FicsParser;
import raptor.game.Game;
import raptor.game.Move;
import raptor.pref.PreferenceKeys;
import raptor.script.GameScript;
import raptor.service.ChatService;
import raptor.service.GameService;
import raptor.service.SoundService;
import raptor.service.ThreadService;
import raptor.util.RaptorStringUtils;
import free.freechess.timeseal.TimesealingSocket;

public class FicsConnector implements Connector, PreferenceKeys {

	/**
	 * The runnable that is executed in the DaemonThread. This class handles
	 * interacting with reading the text between prompts and invoking
	 * parseMessage when it encounters a new message.
	 * 
	 * This class also handles the auto login login, as well as logging in.
	 */
	protected class DaemonRunnable implements Runnable {
		protected void onLoginEvent(String message, boolean isLoginPrompt) {
			if (isLoginPrompt) {
				if (getPreferences().getBoolean(FICS_IS_ANON_GUEST)
						&& !hasSentLogin) {
					System.err.println("Handling login: as anon-guest");
					parseMessage(message);
					hasSentLogin = true;
					sendMessage("guest");
				} else if (!hasSentLogin) {
					System.err.println("Handling login: as non anon-guest");
					parseMessage(message);
					hasSentLogin = true;
					String handle = getPreferences().getString(
							PreferenceKeys.FICS_USER_NAME);
					if (StringUtils.isNotBlank(handle)) {
						sendMessage(handle);
					}
				} else {
					System.err.println("Handling login: catch all");
					parseMessage(message);
				}
			} else {
				if (getPreferences().getBoolean(FICS_IS_ANON_GUEST)
						&& !hasSentPassword) {
					System.err.println("Handling : as anon guest.");
					hasSentPassword = true;
					parseMessage(message);
					sendMessage("");
				} else if (getPreferences().getBoolean(FICS_IS_NAMED_GUEST)
						&& !hasSentPassword) {
					System.err.println("Handling : as named guest.");
					hasSentPassword = true;
					parseMessage(message);
					sendMessage("");
				} else if (!hasSentPassword) {
					System.err.println("Handling : as user.");
					hasSentPassword = true;
					parseMessage(message);
					String password = getPreferences().getString(
							PreferenceKeys.FICS_PASSWORD);
					if (StringUtils.isNotBlank(password)) {
						sendMessage(password);
					}
				} else {
					System.err.println("Handling : catch all");
					parseMessage(message);
				}
			}
		}

		protected void publishInput(String message) {
			if (!isLoggedIn) {
				inboundMessageBuffer.append(message);
				int loggedInMessageIndex = inboundMessageBuffer
						.indexOf(LOGGED_IN_MSG);
				if (loggedInMessageIndex != -1) {
					int nameStartIndex = message.indexOf(LOGGED_IN_MSG)
							+ LOGGED_IN_MSG.length();
					int endIndex = message.indexOf("****", nameStartIndex);
					if (endIndex != -1) {
						userName = FicsUtils.removeTitles(message.substring(
								nameStartIndex, endIndex).trim());
						System.err.println("login complete. userName="
								+ userName);
						isLoggedIn = true;
						onSuccessfulLogin();

						// The user is now logged in no need to drain the queue
						// let the isLoggedIn block handle that when a prompt
						// arrives.
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
						event = RaptorStringUtils.replaceAll(event, "\n\r",
								"\n");
						onLoginEvent(event, true);
					} else {
						int enterPromptIndex = inboundMessageBuffer
								.indexOf(ENTER_PROMPT);
						if (enterPromptIndex != -1) {
							String event = drainInboundMessageBuffer(enterPromptIndex
									+ ENTER_PROMPT.length());
							event = RaptorStringUtils.replaceAll(event, "\n\r",
									"\n");
							onLoginEvent(event, false);
						} else {
							int passwordPromptIndex = inboundMessageBuffer
									.indexOf(PASSWORD_PROMPT);
							if (passwordPromptIndex != -1) {
								String event = drainInboundMessageBuffer(passwordPromptIndex
										+ PASSWORD_PROMPT.length());
								event = RaptorStringUtils.replaceAll(event,
										"\n\r", "\n");
								onLoginEvent(event, false);

							} else {
								int errorMessageIndex = inboundMessageBuffer
										.indexOf(LOGIN_ERROR_MESSAGE);
								if (errorMessageIndex != -1) {
									inboundMessageBuffer.append(message);
									String event = drainInboundMessageBuffer();
									event = RaptorStringUtils.replaceAll(event,
											"\n\r", "\n");
									parseMessage(event);
								} else {
									System.err.println("Dangling:\n" + message);
								}
							}
						}
					}
				}
			}
			if (isLoggedIn) {
				inboundMessageBuffer.append(message);
				int promptIndex = -1;
				while ((promptIndex = inboundMessageBuffer.indexOf(PROMPT)) != -1) {
					String event = drainInboundMessageBuffer(promptIndex
							+ PROMPT.length());
					message = RaptorStringUtils.replaceAll(event, "\n\r", "\n")
							.trim();
					parseMessage(message);
				}
			}
		}

		public void run() {
			try {
				while (true) {
					if (isConnected()) {
						int numRead = inputChannel.read(inputBuffer);
						if (numRead > 0) {
							System.err.println("Read " + numRead + " bytes.");
							inputBuffer.rewind();
							byte[] bytes = new byte[numRead];
							inputBuffer.get(bytes);
							publishInput(new String(bytes));
							inputBuffer.clear();
						} else {
							System.err.println("Read 0 bytes disconnecting.");
							disconnect();
							break;
						}
						Thread.sleep(50);
					} else {
						System.err.println("Not connected disconnecting.");
						disconnect();
						break;
					}
				}
			} catch (Throwable t) {
				LOG.error("Error occured in read", t);
				disconnect();
			} finally {
				LOG.debug("Leaving readInput");
			}
		}
	}

	public static final String ENTER_PROMPT = "\":";
	private static final Log LOG = LogFactory.getLog(FicsConnector.class);
	public static final String LOGGED_IN_MSG = "**** Starting FICS session as ";
	public static final String LOGIN_ERROR_MESSAGE = "\n\r*** ";
	public static final String LOGIN_PROMPT = "login: ";
	public static final String PASSWORD_PROMPT = "password:";
	public static final String PROMPT = "\n\rfics% ";
	public static final int PROMPT_LENGTH = PROMPT.length();

	protected ChatService chatService = new ChatService();
	protected Thread daemonThread;
	protected HashMap<String, GameScript> gameScriptsMap = new HashMap<String, GameScript>();
	protected boolean hasSentLogin = false;
	protected boolean hasSentPassword = false;
	protected StringBuilder inboundMessageBuffer = new StringBuilder(25000);
	protected ByteBuffer inputBuffer = ByteBuffer.allocate(25000);
	protected ReadableByteChannel inputChannel;
	protected boolean isLoggedIn = false;
	protected FicsParser parser = new FicsParser();
	protected PreferenceStore preferences;
	protected String prompt;

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
		LOG.info("Connecting to " + preferences.getString(FICS_SERVER_URL)
				+ " " + preferences.getInt(FICS_PORT));

		isLoggedIn = false;
		hasSentLogin = false;
		hasSentPassword = false;

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

					daemonThread = new Thread(new DaemonRunnable());
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
						// There are no synchronized blocks to worry about
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
		if (inboundMessageBuffer.length() > 0) {
			parseMessage(drainInboundMessageBuffer());
			publishEvent(new ChatEvent(null, ChatTypes.INTERNAL, "Disconnected"));
		}
		LOG.error("Disconnected from FicsConnection.");
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
		return "fics%";
	}

	public String getShortName() {
		return "fics";
	}

	public String getTellToString(String handle) {
		return "tell " + handle + " ";
	}

	public boolean isConnected() {
		return socket != null && inputChannel != null && daemonThread != null;
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
		System.err.println("onSuccessfulLogin: ");
	}

	/**
	 * Parses the message into ChatEvents and publishes them.
	 */
	protected void parseMessage(String message) {
		// Some fics messages need wrapping even though most are fixed at 80
		// columns.
		// The notify list when you log in is an example.
		// message = FicsUtils.wrapText(message);
		ChatEvent[] events = parser.parse(message);
		for (ChatEvent event : events) {
			event.setMessage(FicsUtils.maciejgFormatToUnicode(message));
			publishEvent(event);
		}
	}

	/**
	 * Publishes the specified event to the chat service. Currently all messages
	 * are published on seperate threads via ThreadService.
	 */
	protected void publishEvent(final ChatEvent event) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Publishing event : " + event);
		}
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

	public void sendMessage(String msg) {
		if (isConnected()) {
			StringBuilder builder = new StringBuilder(msg);
			FicsUtils.filterOutbound(builder);
			msg = builder.toString();

			if (LOG.isDebugEnabled()) {
				LOG.debug("Fics Conector Sending: " + msg.trim());
			}

			// Only one thread at a time should write to the socket.
			synchronized (socket) {
				try {
					socket.getOutputStream().write(msg.getBytes());
					socket.getOutputStream().flush();
				} catch (Throwable t) {
					publishEvent(new ChatEvent(null, ChatTypes.INTERNAL,
							"Error: " + t.getMessage()));
					disconnect();
				}
			}

			// Wrap the text before publishing an outbound event.
			publishEvent(new ChatEvent(null, ChatTypes.OUTBOUND, FicsUtils
					.wrapText(msg.trim())));
		} else {
			publishEvent(new ChatEvent(null, ChatTypes.INTERNAL, FicsUtils
					.wrapText("Error: Unable to send " + msg
							+ ". There is currently no connection.")));
		}
	}

	public void setPreferences(PreferenceStore preferences) {
		this.preferences = preferences;
	}

}
