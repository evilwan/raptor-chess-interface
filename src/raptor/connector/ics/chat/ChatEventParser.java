package raptor.connector.ics.chat;

import raptor.chat.ChatEvent;

public abstract class ChatEventParser {
	public ChatEventParser() {
	}

	/**
	 * Returns null if text does not match the event this class produces.
	 */
	public abstract ChatEvent parse(String text);
}