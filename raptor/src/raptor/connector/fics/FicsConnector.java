package raptor.connector.fics;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
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
import raptor.service.ThreadService;
import raptor.util.RaptorStringUtils;
import free.freechess.timeseal.TimesealingSocket;

public class FicsConnector implements Connector, PreferenceKeys {
	public static final String ENTER_PROMPT = "\":";
	private static final Log LOG = LogFactory.getLog(FicsConnector.class);
	public static final String LOGGED_IN_MSG = "**** Starting FICS session as ";
	public static final String LOGIN_PROMPT = "login: ";
	public static final String PASSWORD_PROMPT = "password:";
	public static final String PROMPT = "\n\rfics% ";
	public static final int PROMPT_LENGTH = PROMPT.length();

	protected ChatService chatService = new ChatService();
	protected HashMap<String, GameScript> gameScriptsMap = new HashMap<String, GameScript>();
	protected boolean hasSentLogin = false;
	protected boolean hasSentPassword = false;
	protected StringBuilder inboundMessageBuffer = new StringBuilder(25000);
	protected ByteBuffer inputBuffer = ByteBuffer.allocate(25000);
	protected ReadableByteChannel inputChannel;
	protected boolean isLoggedIn = false;
	protected WritableByteChannel outputChannel;
	protected FicsParser parser = new FicsParser();
	protected PreferenceStore preferences;
	protected Socket socket;

	protected String userName;

	public FicsConnector() {
		refreshGameScripts();
	}

	public void addGameScript(GameScript script) {
		gameScriptsMap.put(script.getName(), script);
		script.save();
	}

	public void connect() {
		LOG.info("Connecting to " + preferences.getString(FICS_SERVER_URL)
				+ " " + preferences.getInt(FICS_PORT));

		try {
			isLoggedIn = false;
			hasSentLogin = false;
			hasSentPassword = false;

			LOG.info("Trying to connect");
			try {
				onMessageEvent("Connecting to "
						+ preferences.getString(FICS_SERVER_URL)
						+ " "
						+ preferences.getInt(FICS_PORT)
						+ (getPreferences().getBoolean(FICS_TIMESEAL_ENABLED) ? " with "
								: " without ") + "timeseal ...");

				if (getPreferences().getBoolean(FICS_TIMESEAL_ENABLED)) {
					socket = new TimesealingSocket(preferences
							.getString(FICS_SERVER_URL), preferences
							.getInt(FICS_PORT));
				} else {
					socket = new Socket(preferences.getString(FICS_SERVER_URL),
							preferences.getInt(FICS_PORT));
				}
				onMessageEvent("Connected");

			} catch (Exception ce) {
				onMessageEvent("Error: " + ce.getMessage());
				return;
			}

			inputChannel = Channels.newChannel(socket.getInputStream());

			LOG.info("Socket connected");

			ThreadService.getInstance().runDaemon(new Thread(new Runnable() {
				public void run() {
					readInput();
				}
			}));
		} catch (Throwable t) {
			LOG.error("Exception occured obtaining socket channel connection:",
					t);
			disconnect();
		}

		LOG.info("Connection successful");
	}

	public void disconnect() {
		try {
			socket.close();
		} catch (Throwable t) {
			LOG.error("Error disposing channel", t);
		}
		LOG.error("Disconnected from FicsConnection.");
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

	public String getShortName() {
		return "fics";
	}

	public void makeMove(Game game, Move move) {
		sendMessage(move.getSan());
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

	protected void onLoginEvent(String message, boolean isLoginPrompt) {
		if (isLoginPrompt) {
			if (getPreferences().getBoolean(FICS_IS_ANON_GUEST)
					&& !hasSentLogin) {
				System.err.println("Handling login: as anon-guest");
				onMessageEvent(message);
				hasSentLogin = true;
				sendMessage("guest");
			} else if (!hasSentLogin) {
				System.err.println("Handling login: as non anon-guest");
				onMessageEvent(message);
				hasSentLogin = true;
				String handle = getPreferences().getString(
						PreferenceKeys.FICS_USER_NAME);
				if (StringUtils.isNotBlank(handle)) {
					sendMessage(handle);
				}
			} else {
				System.err.println("Handling login: catch all");
				onMessageEvent(message);
			}
		} else {
			if (getPreferences().getBoolean(FICS_IS_ANON_GUEST)
					&& !hasSentPassword) {
				System.err.println("Handling : as anon guest.");
				hasSentPassword = true;
				onMessageEvent(message);
				sendMessage("");
			} else if (getPreferences().getBoolean(FICS_IS_NAMED_GUEST)
					&& !hasSentPassword) {
				System.err.println("Handling : as named guest.");
				hasSentPassword = true;
				onMessageEvent(message);
				sendMessage("");
			} else if (!hasSentPassword) {
				System.err.println("Handling : as user.");
				hasSentPassword = true;
				onMessageEvent(message);
				String password = getPreferences().getString(
						PreferenceKeys.FICS_PASSWORD);
				if (StringUtils.isNotBlank(password)) {
					sendMessage(password);
				}
			} else {
				System.err.println("Handling : catch all");
				onMessageEvent(message);
			}
		}
	}

	protected void onMessageEvent(String message) {
		ChatEvent[] events = parser.parse(message);
		for (ChatEvent event : events) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Publishing event : " + event);
			}
			chatService.publishChatEvent(event);
		}
	}

	protected void onSuccessfulLogin() {
		System.err.println("onSuccessfulLogin: ");
	}

	protected void publishInput(String message) {
		if (!isLoggedIn) {
			if (message.contains(LOGGED_IN_MSG)) {
				int nameStartIndex = message.indexOf(LOGGED_IN_MSG)
						+ LOGGED_IN_MSG.length();
				int endIndex = message.indexOf("****", nameStartIndex);
				userName = FicsUtils.removeTitles(message.substring(
						nameStartIndex, endIndex).trim());
				System.err.println("login complete. userName=" + userName);
				isLoggedIn = true;
				onSuccessfulLogin();
			} else {
				inboundMessageBuffer.append(message);
				int loginIndex = inboundMessageBuffer.indexOf(LOGIN_PROMPT);
				if (loginIndex != -1) {
					String event = inboundMessageBuffer.substring(0, loginIndex
							+ LOGIN_PROMPT.length());
					inboundMessageBuffer.delete(0, loginIndex
							+ LOGIN_PROMPT.length());
					event = RaptorStringUtils.replaceAll(event, "\n\r", "\n");
					onLoginEvent(event, true);
				} else {
					int enterPromptIndex = inboundMessageBuffer
							.indexOf(ENTER_PROMPT);
					if (enterPromptIndex != -1) {
						String event = inboundMessageBuffer.substring(0,
								enterPromptIndex + ENTER_PROMPT.length());
						inboundMessageBuffer.delete(0, enterPromptIndex
								+ ENTER_PROMPT.length());
						event = RaptorStringUtils.replaceAll(event, "\n\r",
								"\n");
						onLoginEvent(event, false);
					} else {
						int passwordPromptIndex = inboundMessageBuffer
								.indexOf(PASSWORD_PROMPT);
						if (passwordPromptIndex != -1) {
							String event = inboundMessageBuffer.substring(0,
									passwordPromptIndex
											+ PASSWORD_PROMPT.length());
							inboundMessageBuffer.delete(0, passwordPromptIndex
									+ PASSWORD_PROMPT.length());
							event = RaptorStringUtils.replaceAll(event, "\n\r",
									"\n");
							onLoginEvent(event, false);

						} else {
							System.err.println("Dangling:\n" + message);
						}
					}
				}
			}
		}
		if (isLoggedIn) {
			inboundMessageBuffer.append(message);
			int promptIndex = -1;
			while ((promptIndex = inboundMessageBuffer.indexOf(PROMPT)) != -1) {
				String event = inboundMessageBuffer.substring(0, promptIndex
						+ PROMPT.length());
				inboundMessageBuffer.delete(0, promptIndex + PROMPT.length());
				message = RaptorStringUtils.replaceAll(event, "\n\r", "\n")
						.trim();
				onMessageEvent(message);
			}
		}
	}

	protected void readInput() {
		try {
			while (true) {
				int numRead = inputChannel.read(inputBuffer);
				if (numRead > 0) {
					System.err.println("Read " + numRead + " bytes.");
					inputBuffer.rewind();
					byte[] bytes = new byte[numRead];
					inputBuffer.get(bytes);
					publishInput(new String(bytes));
					inputBuffer.clear();
				}
				Thread.sleep(50);
			}
		} catch (Throwable t) {
			LOG.error("Error occured in read", t);
			disconnect();
		}
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
		StringBuilder builder = new StringBuilder(msg);
		FicsUtils.filterOutbound(builder);
		msg = builder.toString();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Fics Conector Sending: " + msg.trim());
		}

		synchronized (this) {
			try {
				socket.getOutputStream().write(msg.getBytes());
				socket.getOutputStream().flush();
			} catch (Throwable t) {
				LOG.error("Error occured in send", t);
				disconnect();
			}
		}

		chatService.publishChatEvent(new ChatEvent(null, ChatTypes.OUTBOUND,
				msg.trim()));
	}

	public void setPreferences(PreferenceStore preferences) {
		this.preferences = preferences;
	}
}
