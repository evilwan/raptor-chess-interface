package raptor.connector.fics.chat;

import raptor.chat.ChatEvent;

public class ChallengeEventParser extends ChatEventParser {
	private static final String IDENTIFIER = "Challenge: ";

	public ChallengeEventParser() {
		super();
	}

	@Override
	public ChatEvent parse(String text) {
		if (text.length() < 600 && text.indexOf(IDENTIFIER) != -1)
			return new ChatEvent(null, CHALLENGE, text);
		else
			return null;

	}
}