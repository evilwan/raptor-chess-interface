package raptor.connector.ics;

import java.util.StringTokenizer;

public class UserInfoParser {
	
	private String fingerString, varString;
	
	public UserInfoParser(String fingerString, String varString) {
		this.fingerString = fingerString;
		this.varString = varString;
	}
	
	public String getOnFor() {
		StringTokenizer tokenizer = new StringTokenizer(fingerString, "\n");
		while (tokenizer.hasMoreTokens()) {
			String t = tokenizer.nextToken();
			if (t.startsWith("On for:")) {
				return t;
			}
	     }
        return "Undetermined";
	}
	
	public String getInterface() {
		StringTokenizer tokenizer = new StringTokenizer(varString, "\n");
		while (tokenizer.hasMoreTokens()) {			
			String t = tokenizer.nextToken();
			if (t.startsWith("Interface")) {
				return t.replace("\"", "");
			}
	     }
        return "Interface Undetermined";
	}
}
