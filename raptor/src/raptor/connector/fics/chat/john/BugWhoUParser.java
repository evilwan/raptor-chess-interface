package raptor.connector.fics.chat.john;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

public class BugWhoUParser {
	private static final Logger LOGGER = Logger.getLogger(BugWhoUParser.class);
	
	public static class UnpartneredBugger {
		private String rating;
		private char status;
		private String username;
		
		public void setRating(String rating) {
			this.rating = rating;
		}
		public String getRating() {
			return rating;
		}
		
		public void setStatus(char status) {
			this.status = status;
		}
		public char getStatus() {
			return status;
		}
		
		public void setUsername(String username) {
			this.username = username;
		}
		public String getUsername() {
			return username;
		}
		
		public String toString() {
			return getUsername();
		}
		
	}
	
	public BugWhoUParser(String text) {
		text = text.replaceAll("[0-9]+ players displayed \\(of [0-9+]\\). \\(\\*\\) indicates system administrator.","");
	}
	
	public UnpartneredBugger[] parse(String text,boolean showUnrateds,boolean showGuests) {
		StringTokenizer tokens = new StringTokenizer(text,"^~:#.& ",true);
		ArrayList<UnpartneredBugger> arr = new ArrayList<UnpartneredBugger>();
		
		while(tokens.hasMoreTokens()) {
			UnpartneredBugger p = new UnpartneredBugger();
			String rating = tokens.nextToken();
			if (rating.equals("----") && !showUnrateds) continue;
			if (rating.equals("++++") && !showGuests) continue;
			p.setRating(rating);
			String status = tokens.nextToken();
			if (status.length() > 1) { /* something wrong */ }
			char modifier = status.charAt(0);
			p.setStatus(modifier);
			String username = tokens.nextToken();
			p.setUsername(username);
			
			arr.add(p);
		}
		UnpartneredBugger[] out = arr.toArray(new UnpartneredBugger[arr.size()]);
		LOGGER.info("usernames = " + java.util.Arrays.toString(out));
		return out;
	}
	
}
