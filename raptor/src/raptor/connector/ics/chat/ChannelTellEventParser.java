package raptor.connector.ics.chat;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.ics.IcsUtils;
import raptor.util.RaptorStringTokenizer;

public class ChannelTellEventParser extends ChatEventParser {
	public ChannelTellEventParser() {
		super();
	}

	/**
	 * Returns null if text does not match the event this class produces.
	 */
	@Override
	public ChatEvent parse(String text) {
		if (text.length() < 1500) {
			int i = text.indexOf("): ");
			if (i != -1) {
				RaptorStringTokenizer stringtokenizer = new RaptorStringTokenizer(
						text, ":");
				if (stringtokenizer.hasMoreTokens()) {
					String s1 = stringtokenizer.nextToken();
					int j = s1.lastIndexOf(")");
					int k = s1.lastIndexOf("(");
					if (k < j && k != -1 && j != -1) {

						ChatEvent event = new ChatEvent(IcsUtils
								.removeTitles(s1), ChatType.CHAN_TELL, text);
						event.setChannel(text.substring(k + 1, j));
						return event;
					}

				}
			}
			return null;
		}
		return null;
	}

}