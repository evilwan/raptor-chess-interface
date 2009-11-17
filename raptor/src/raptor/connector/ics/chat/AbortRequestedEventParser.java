package raptor.connector.ics.chat;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;

public class AbortRequestedEventParser extends ChatEventParser {
	private static final String IDENTIFIER = "would like to abort the game; type \"abort\" to accept.";

	public AbortRequestedEventParser() {
		super();
	}

	@Override
	public ChatEvent parse(String text) {
		if (text.length() < 600 && text.indexOf(IDENTIFIER) != -1) {
			return new ChatEvent(null, ChatType.ABORT_REQUEST, text);
		} else {
			return null;
		}

	}
}
