package raptor.connector.ics.chat;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.ics.IcsUtils;
import raptor.util.RaptorStringTokenizer;

public class WhisperEventParser extends ChatEventParser {
	public static final String IDENTIFIER = "whispers:";

	public WhisperEventParser() {
	}

	/**
	 * Returns null if text does not match the event this class produces.
	 */
	@Override
	public ChatEvent parse(String text) {
		if (text.length() < 1500) {
			RaptorStringTokenizer stringtokenizer = new RaptorStringTokenizer(
					text, " ");
			if (stringtokenizer.hasMoreTokens()) {
				String source = stringtokenizer.nextToken();
				if (stringtokenizer.hasMoreTokens()) {
					String s2 = stringtokenizer.nextToken();
					if (s2.equals(IDENTIFIER)) {
						int j = text.indexOf("[");
						int k = text.indexOf("]");

						return new ChatEvent(IcsUtils.removeTitles(source),
								ChatType.WHISPER, text, text
										.substring(j + 1, k));
					}
				}
			}
			return null;
		}
		return null;
	}
}