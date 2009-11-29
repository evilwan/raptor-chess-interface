package raptor.connector;

import raptor.chat.ChatEvent;

/**
 * A callback which is invoked when a ChatEvent arrives from the server.
 */
public interface MessageCallback {

	/**
	 * Invoked when a ChatEvent arrives from the server.
	 * 
	 * @return True if the next event matching should be sent to this
	 *         MessageCallback, false otherwise.
	 */
	public boolean matchReceived(ChatEvent event);
}
