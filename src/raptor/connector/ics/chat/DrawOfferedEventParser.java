package raptor.connector.ics.chat;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;

public class DrawOfferedEventParser extends ChatEventParser {
	private static final String IDENTIFIER = "offers you a draw.";

	public DrawOfferedEventParser() {
		super();
	}

	@Override
	public ChatEvent parse(String text) {
		if (text.length() < 600 && text.indexOf(IDENTIFIER) != -1) {
			return new ChatEvent(null, ChatType.DRAW_REQUEST, text);
		} else {
			return null;
		}

	}
}
