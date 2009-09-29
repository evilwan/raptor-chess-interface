package raptor.connector.fics.chat;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.fics.FicsUtils;
import raptor.util.RaptorStringTokenizer;

public class CShoutEventParser extends ChatEventParser {
	private static final String IDENTIFIER = "c-shouts:";

	public CShoutEventParser() {
		super();
	}

	/**
	 * Returns null if text does not match the event this class produces.
	 */
	@Override
	public ChatEvent parse(String text) {
		RaptorStringTokenizer stringtokenizer = new RaptorStringTokenizer(text,
				" ");
		if (stringtokenizer.hasMoreTokens()) {
			String s1 = stringtokenizer.nextToken();
			if (stringtokenizer.hasMoreTokens()) {
				String s2 = stringtokenizer.nextToken();
				if (s2.equals(IDENTIFIER))
					return new ChatEvent(FicsUtils.removeTitles(s1),
							ChatType.CSHOUT, text);
			}
		}
		return null;

	}
}