package raptor.connector.fics.chat;

import raptor.chat.ChatEvent;
import raptor.chat.ChatTypes;

public abstract class ChatEventParser implements ChatTypes {
	public ChatEventParser() {
	}

	/**
	 * Returns null if text does not match the event this class produces.
	 */
	public abstract ChatEvent parse(String text);
}