package raptor.connector.fics.parser;

import raptor.chat.ChatEvent;

public class ChallengeEventParser extends ChatEventParser {
	public ChallengeEventParser() {
		super();
	}

	public ChatEvent parse(String text) {
		if (text.length() < 600 && text.indexOf(IDENTIFIER) != -1)
			return new ChatEvent(null, CHALLENGE, text);
		else
			return null;

	}

	private static final String IDENTIFIER = "Challenge: ";
}