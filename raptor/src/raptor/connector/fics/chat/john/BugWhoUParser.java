package raptor.connector.fics.chat.john;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

public class BugWhoUParser {
	private static final Logger LOGGER = Logger.getLogger(BugWhoUParser.class);

	public BugWhoUParser(String text) {
		text = text
				.replaceAll(
						"[0-9]+ players displayed \\(of [0-9+]\\). \\(\\*\\) indicates system administrator.",
						"");
	}

	public Bugger[] parse(String text, boolean showUnrateds, boolean showGuests) {
		StringTokenizer tokens = new StringTokenizer(text, "^~:#.& ", true);
		ArrayList<Bugger> arr = new ArrayList<Bugger>();

		while (tokens.hasMoreTokens()) {
			Bugger p = new Bugger();
			String rating = tokens.nextToken();
			if (rating.equals("----") && !showUnrateds)
				continue;
			if (rating.equals("++++") && !showGuests)
				continue;
			p.setRating(rating);
			String status = tokens.nextToken();
			if (status.length() > 1) { /* something wrong */
			}
			char modifier = status.charAt(0);
			p.setStatus(modifier);
			String username = tokens.nextToken();
			p.setUsername(username);

			arr.add(p);
		}
		Bugger[] out = arr.toArray(new Bugger[arr.size()]);
		LOGGER.info("usernames = " + java.util.Arrays.toString(out));
		return out;
	}

}
