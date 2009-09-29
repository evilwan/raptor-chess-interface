package raptor.connector.fics.chat;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.fics.FicsUtils;
import raptor.util.RaptorStringTokenizer;

public class ShoutEventParser extends ChatEventParser {
	private static final String SHOUT_1 = "-->";

	private static final String SHOUT_2 = "shouts:";

	public ShoutEventParser() {
	}

	/**
	 * Returns null if text does not match the event this class produces.
	 */
	@Override
	public ChatEvent parse(String text) {
		if (text.length() < 1500) {
			if (text.startsWith("\n" + SHOUT_1)) {
				String name = text.substring(SHOUT_1.length()).trim();
				RaptorStringTokenizer stringtokenizer = new RaptorStringTokenizer(
						name, " ");
				String s1 = FicsUtils.removeTitles(stringtokenizer.nextToken());
				return new ChatEvent(s1, ChatType.SHOUT, text);

			}
			RaptorStringTokenizer stringtokenizer1 = new RaptorStringTokenizer(
					text, " ");
			if (stringtokenizer1.hasMoreTokens()) {
				String s2 = stringtokenizer1.nextToken();
				if (stringtokenizer1.hasMoreTokens()) {
					String s3 = stringtokenizer1.nextToken();
					if (s3.equals(SHOUT_2))
						return new ChatEvent(FicsUtils.removeTitles(s2),
								ChatType.SHOUT, text);
				}
			}
			return null;
		}
		return null;
	}
}