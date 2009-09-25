package raptor.connector.fics.chat;

import java.util.StringTokenizer;

import raptor.chat.ChatEvent;
import raptor.connector.fics.FicsUtils;

public class FollowingEventParser extends ChatEventParser {

	// You will now be following pindik's games.
	private static final String IDENTIFIER = "You will now be following";

	private static final String IDENTIFIER2 = "You will not follow any player's games.";

	public FollowingEventParser() {
	}

	/**
	 * Returns null if text does not match the event this class produces.
	 */
	@Override
	public ChatEvent parse(String text) {
		if (text.length() < 200) {
			int identifierIndex = text.indexOf(IDENTIFIER);
			if (identifierIndex != -1) {
				StringTokenizer tok = new StringTokenizer(text.substring(
						identifierIndex + IDENTIFIER.length(), text.length()),
						" '(");
				return new ChatEvent(FicsUtils.removeTitles(tok.nextToken()),
						FOLLOWING, text);
			} else {
				if (text.indexOf(IDENTIFIER2) != -1) {
					return new ChatEvent(null, NOT_FOLLOWING, text);
				} else {
					return null;
				}
			}

		} else
			return null;
	}

}