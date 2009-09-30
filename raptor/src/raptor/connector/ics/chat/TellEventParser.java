package raptor.connector.ics.chat;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.ics.IcsUtils;
import raptor.util.RaptorStringTokenizer;

public class TellEventParser extends ChatEventParser {
	public TellEventParser() {

	}

	/**
	 * Returns null if text does not match the event this class produces.
	 */
	@Override
	public ChatEvent parse(String text) {
		if (text.length() < 1500) {
			text = text.trim();
			RaptorStringTokenizer tok = new RaptorStringTokenizer(text, " \r\n");
			if (tok.hasMoreTokens()) {
				String source = tok.nextToken();
				if (tok.hasMoreTokens()) {
					String s2 = tok.nextToken();
					if (s2.equals("says:")) {
						return new ChatEvent(IcsUtils.removeTitles(source),
								ChatType.TELL, text);

					} else if (s2.equals("tells")) {
						if (tok.hasMoreTokens()) {
							String s3 = tok.nextToken();
							if (s3.equals("you:")) {
								return new ChatEvent(IcsUtils
										.removeTitles(source), ChatType.TELL,
										text);
							}
						}
					}
				}
			}
			return null;
		}
		return null;
	}

}