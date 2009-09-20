package raptor.connector.fics.parser;

import raptor.chat.ChatEvent;
import raptor.connector.fics.FicsUtils;
import raptor.util.RaptorStringTokenizer;

public class PartnershipCreatedEventParser extends ChatEventParser {
	public PartnershipCreatedEventParser() {
	}

	public ChatEvent parse(String text) {
		if (text.length() < 100) {
			int i = text.indexOf(IDENTIFIER);
			if (i != -1) {
				RaptorStringTokenizer stringtokenizer = new RaptorStringTokenizer(
						text.substring(i + "You agree to be".length(), text
								.length()), " '");
				return new ChatEvent(FicsUtils.removeTitles(stringtokenizer
						.nextToken()), PARTNERSHIP_CREATED, text);
			}
			int j = text.indexOf(IDENTIFIER_2);
			if (j != -1) {
				RaptorStringTokenizer stringtokenizer1 = new RaptorStringTokenizer(
						text, " ");
				String s1 = stringtokenizer1.nextToken();
				String s2 = stringtokenizer1.nextToken();
				if (s2.equals("agrees"))
					return new ChatEvent(FicsUtils.removeTitles(s1),
							PARTNERSHIP_CREATED, text);
			}
			return null;
		}
		return null;

	}

	private static final String IDENTIFIER = "You agree to be";

	private static final String IDENTIFIER_2 = "agrees to be your partner.";
}