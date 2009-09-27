package raptor.connector.fics.chat.john;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/*
 bugwho p
 Partnerships not playing bughouse
 1944  RooRooBear / 1664  oub
 1 partnership displayed.
 */

public class BugWhoPParser {
	private static final Logger LOGGER = Logger.getLogger(BugWhoPParser.class);

	public BugWhoPParser(String text) {
		text = text.replaceAll("[0-9]+ partnerships displayed.", "");
		text = text.replaceAll("1 partnership displayed.", "");
	}

	private Bugger assist(StringTokenizer tokens) {
		Bugger p = new Bugger();
		String rating = tokens.nextToken();
		p.setRating(rating);
		String status = tokens.nextToken();
		if (status.length() > 1) { /* something wrong */
		}
		char modifier = status.charAt(0);
		p.setStatus(modifier);
		String username = tokens.nextToken();
		p.setUsername(username);
		return p;
	}

	public Partnership[] parse(String text, boolean showUnrateds,
			boolean showGuests) {
		StringTokenizer tokens = new StringTokenizer(text, "^~:#.& ", true);
		ArrayList<Partnership> arr = new ArrayList<Partnership>();

		while (tokens.hasMoreTokens()) {
			Partnership p = new Partnership();
			Bugger[] buggers = new Bugger[2];
			buggers[0] = assist(tokens);
			tokens.nextToken(); // "/"
			buggers[1] = assist(tokens);

			arr.add(p);
		}
		Partnership[] out = arr.toArray(new Partnership[arr.size()]);
		LOGGER.info("usernames = " + java.util.Arrays.toString(out));
		return out;
	}

}
