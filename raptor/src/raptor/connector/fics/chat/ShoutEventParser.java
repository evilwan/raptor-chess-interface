package raptor.connector.fics.chat;

import raptor.chat.ChatEvent;
import raptor.connector.fics.FicsUtils;
import raptor.util.RaptorStringTokenizer;

public class ShoutEventParser extends ChatEventParser {
	public ShoutEventParser() {
	}

	/**
	 * Returns null if text does not match the event this class produces.
	 */
	public ChatEvent parse(String text) {
		if (text.length() < 1500) {
			if (text.startsWith(SHOUT_1)) {
				RaptorStringTokenizer stringtokenizer = new RaptorStringTokenizer(
						text.substring("SHOUT_1".length()), " ");
				String s1 = FicsUtils.removeTitles(stringtokenizer.nextToken());
				return new ChatEvent(s1, SHOUT, text);

			}
			RaptorStringTokenizer stringtokenizer1 = new RaptorStringTokenizer(
					text, " ");
			if (stringtokenizer1.hasMoreTokens()) {
				String s2 = stringtokenizer1.nextToken();
				if (stringtokenizer1.hasMoreTokens()) {
					String s3 = stringtokenizer1.nextToken();
					if (s3.equals(SHOUT_2))
						return new ChatEvent(FicsUtils.removeTitles(s2), SHOUT,
								text);
				}
			}
			return null;
		}
		return null;
	}

	private static final String SHOUT_1 = "-->";

	private static final String SHOUT_2 = "shouts:";
}