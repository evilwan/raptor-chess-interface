package raptor.connector;

import raptor.chat.ChatEvent;

/**
 * A callback which is invoked when a ChatEvent arrives from the server.
 */
public interface MessageCallback {

	/**
	 * Invoked when a ChatEvent arrives from the server.
	 */
	public void matchReceived(ChatEvent event);
}
