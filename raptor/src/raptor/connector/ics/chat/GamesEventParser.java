package raptor.connector.ics.chat;

import org.apache.commons.lang.math.NumberUtils;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;

public class GamesEventParser extends ChatEventParser {
	private static final String END_MESSAGE = "games displayed.";

	public GamesEventParser() {
		super();
	}

	/**
	 * Returns null if text does not match the event this class produces.
	 */
	@Override
	public ChatEvent parse(String text) {
		ChatEvent result = null;
		if (text.endsWith(END_MESSAGE)) {
			String message = text.trim();
			int firstSpaceIndex = message.indexOf(" ");

			if (firstSpaceIndex != -1) {
				String firstWord = message.substring(0, firstSpaceIndex).trim();
				if (NumberUtils.isDigits(firstWord)) {
					result = new ChatEvent(null, ChatType.GAMES, text);
				}
			}
		}
		return result;
	}
}