package raptor.connector.ics;

/**
 * The ICS (Internet Chess Servier) Context.
 */
public class IcsConnectorContext {

	/**
	 * DO NOT change the constants in here FicsConnector uses them. Instead
	 * subclass this and make changes.
	 */

	IcsParser parser;

	public IcsConnectorContext(IcsParser parser) {
		this.parser = parser;
	}

	public String getDescription() {
		return "Free Internet Chess Server";
	}

	public String getEnterPrompt() {
		return "\":";
	}

	public String getLoggedInMessage() {
		return "**** Starting FICS session as ";
	}

	public String getLoginErrorMessage() {
		return "\n*** ";
	}

	public String getLoginPrompt() {
		return "login: ";
	}

	public IcsParser getParser() {
		return parser;
	}

	public String getPasswordPrompt() {
		return "password:";
	}

	public String getPreferencePrefix() {
		return "fics-";
	}

	public String getPrompt() {
		return "fics%";
	}

	public String getRawPrompt() {
		return "\nfics% ";
	}

	public int getRawPromptLength() {
		return getRawPrompt().length();
	}

	public String getShortName() {
		return "fics";
	}

}
