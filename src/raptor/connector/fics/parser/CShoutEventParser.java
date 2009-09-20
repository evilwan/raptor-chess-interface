package raptor.connector.fics.parser;

import raptor.chat.ChatEvent;
import raptor.connector.fics.FicsUtils;
import raptor.util.RaptorStringTokenizer;

public class CShoutEventParser extends ChatEventParser {
	public CShoutEventParser() {
		super();
	}

	/**
	 * Returns null if text does not match the event this class produces.
	 */
	public ChatEvent parse(String text) {
		RaptorStringTokenizer stringtokenizer = new RaptorStringTokenizer(text,
				" ");
		if (stringtokenizer.hasMoreTokens()) {
			String s1 = stringtokenizer.nextToken();
			if (stringtokenizer.hasMoreTokens()) {
				String s2 = stringtokenizer.nextToken();
				if (s2.equals(IDENTIFIER))
					return new ChatEvent(FicsUtils.removeTitles(s1), CSHOUT,
							text);
			}
		}
		return null;

	}

	private static final String IDENTIFIER = "c-shouts:";
}