package raptor.connector.ics.chat;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;

public class BugWhoAllEventParser extends ChatEventParser {
	private static final String START_MESSAGE = "Bughouse games in progress";
	private static final String END_MESSAGE = ". (*) indicates system administrator.";

	public BugWhoAllEventParser() {
		super();
	}

	/**
	 * Returns null if text does not match the event this class produces.
	 */
	@Override
	public ChatEvent parse(String text) {
		ChatEvent result = null;
		if (text.endsWith(END_MESSAGE) && text.startsWith(START_MESSAGE)
				|| text.startsWith(START_MESSAGE, 1)) {
			result = new ChatEvent(null, ChatType.BUGWHO_ALL, text);
		}
		return result;
	}
}