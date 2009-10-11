package raptor.connector.ics.chat;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.util.RaptorStringTokenizer;

public class ToldEventParser extends ChatEventParser {
	// (told TheTactician)
	public static final String STARTING_TEXT = "\n(told ";
	public static final String STARTING_TEXT_2 = "(told ";

	/**
	 * Returns null if text does not match the event this class produces.
	 */
	@Override
	public ChatEvent parse(String text) {
		if (text.startsWith(STARTING_TEXT) || text.startsWith(STARTING_TEXT_2)) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(text, " )",
					true);
			tok.nextToken();
			String source = tok.nextToken();
			try {
				//Ignore the told message to channels.
				Integer.parseInt(source);
			} catch (NumberFormatException nfe) {
				return new ChatEvent(source, ChatType.TOLD, text);
			}
			return null;
		}
		return null;
	}

}
