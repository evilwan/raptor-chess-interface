package raptor.connector.ics.chat;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;

public class ChallengeEventParser extends ChatEventParser {
	private static final String IDENTIFIER = "Challenge: ";

	public ChallengeEventParser() {
		super();
	}

	@Override
	public ChatEvent parse(String text) {
		if (text.length() < 600 && text.indexOf(IDENTIFIER) != -1)
			return new ChatEvent(null, ChatType.CHALLENGE, text);
		else
			return null;

	}
}