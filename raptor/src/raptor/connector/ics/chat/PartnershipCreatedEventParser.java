package raptor.connector.ics.chat;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.ics.IcsUtils;
import raptor.util.RaptorStringTokenizer;

public class PartnershipCreatedEventParser extends ChatEventParser {
	private static final String IDENTIFIER = "You agree to be";

	private static final String IDENTIFIER_2 = "agrees to be your partner.";

	public PartnershipCreatedEventParser() {
	}

	@Override
	public ChatEvent parse(String text) {
		if (text.length() < 100) {
			int i = text.indexOf(IDENTIFIER);
			if (i != -1) {
				RaptorStringTokenizer stringtokenizer = new RaptorStringTokenizer(
						text.substring(i + "You agree to be".length(), text
								.length()), " '");
				return new ChatEvent(IcsUtils.removeTitles(stringtokenizer
						.nextToken()), ChatType.PARTNERSHIP_CREATED, text);
			}
			int j = text.indexOf(IDENTIFIER_2);
			if (j != -1) {
				RaptorStringTokenizer stringtokenizer1 = new RaptorStringTokenizer(
						text, " ");
				String s1 = stringtokenizer1.nextToken();
				String s2 = stringtokenizer1.nextToken();
				if (s2.equals("agrees"))
					return new ChatEvent(IcsUtils.removeTitles(s1),
							ChatType.PARTNERSHIP_CREATED, text);
			}
			return null;
		}
		return null;

	}
}