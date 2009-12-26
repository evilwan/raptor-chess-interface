package raptor.connector.ics.chat;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.util.RaptorStringTokenizer;

public class HistoryEventParser extends ChatEventParser {
	private static final String BEGINING_MESSAGE = "History for";

	public HistoryEventParser() {
		super();
	}

	/**
	 * Returns null if text does not match the event this class produces.
	 */
	@Override
	public ChatEvent parse(String text) {
		ChatEvent result = null;
		if (text.startsWith(BEGINING_MESSAGE)
				|| text.startsWith(BEGINING_MESSAGE, 1)) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(text, " \n:",
					true);
			tok.nextToken();
			tok.nextToken();
			String userName = tok.nextToken();

			if (userName != null) {
				result = new ChatEvent(userName, ChatType.HISTORY, text);
			}
		}
		return result;
	}
}