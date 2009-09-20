package raptor.connector.fics.parser;

import raptor.chat.ChatEvent;
import raptor.connector.fics.FicsUtils;
import raptor.util.RaptorStringTokenizer;

public class KibitzEventParser extends ChatEventParser {
	public KibitzEventParser() {
		super();
	}

	/**
	 * Returns null if text does not match the event this class produces.
	 */
	public ChatEvent parse(String text) {
		if (text.length() < 1500) {
			RaptorStringTokenizer stringtokenizer = new RaptorStringTokenizer(
					text, " ");
			if (stringtokenizer.hasMoreTokens()) {
				String s1 = stringtokenizer.nextToken();
				if (stringtokenizer.hasMoreTokens()) {
					String s2 = stringtokenizer.nextToken();
					if (s2.equals("kibitzes:")) {
						int i = text.indexOf("kibitzes:");
						int j = text.indexOf("[") + 1;
						int k = text.indexOf("]");
						try {
							return new ChatEvent(FicsUtils.removeTitles(s1),
									KIBITZ, text, text.substring(j, k));
						} catch (Exception exception) {
							exception.printStackTrace();
						}
					}
				}
			}
			return null;
		}
		return null;
	}
}