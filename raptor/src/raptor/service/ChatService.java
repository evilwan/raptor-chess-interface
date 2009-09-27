package raptor.service;

import java.util.ArrayList;
import java.util.List;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatLogger;
import raptor.connector.Connector;

/**
 * A service which invokes chatEventOccured on added ChatListeners when a
 * ChatEvents arrive on a connector.
 */
public class ChatService {

	public static interface ChatListener {
		public void chatEventOccured(ChatEvent e);
	}

	protected List<ChatListener> listeners = new ArrayList<ChatListener>(5);
	protected Connector connector = null;
	protected ChatLogger logger = null;

	/**
	 * Constructs a chat service for the specified connector.
	 * 
	 * @param connector
	 */
	public ChatService(Connector connector) {
		this.connector = null;
		logger = new ChatLogger(Raptor.USER_RAPTOR_HOME_PATH + "/chatcache/"
				+ connector.getShortName() + ".txt");
	}

	/**
	 * Adds a ChatServiceListener to the chat service. Please remove the
	 * listener when you no longer need the ChatService to avoid memory leaks.
	 */
	public void addChatServiceListener(ChatListener listener) {
		listeners.add(listener);
	}

	/**
	 * Disposes all resources the ChatService is using.
	 */
	public void dispose() {
		listeners.clear();
		if (logger != null) {
			logger.delete();
		}
		listeners = null;
		logger = null;
		connector = null;
	}

	/**
	 * Returns the Chat Services Chat Logger.
	 */
	public ChatLogger getChatLogger() {
		return logger;
	}

	/**
	 * Returns the Connector backing this ChatService.
	 */
	public Connector getConnector() {
		return connector;
	}

	/**
	 * Chat events are published asynchronously.
	 */
	public void publishChatEvent(final ChatEvent event) {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				for (ChatListener listener : listeners) {
					listener.chatEventOccured(event);
				}
				logger.write(event);
			}
		});
	}

	/**
	 * Removes a listener from the ChatService.
	 */
	public void removeChatServiceListener(ChatListener listener) {
		listeners.remove(listener);
	}
}
