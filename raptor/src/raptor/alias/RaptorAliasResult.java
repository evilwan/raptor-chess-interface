package raptor.alias;

/**
 * <p>
 * An object used to store information about an applied alias. If newText is
 * null that means no further processing is required for the alias. If it is not
 * null newText should be sent to the connector. If userMessage is null that
 * means no message needs to be displayed to the user. If userMessage is not
 * null then userMessage should be displayed to the user.
 * </p>
 * 
 */
public class RaptorAliasResult {
	protected String newText;
	protected String userMessage;

	public RaptorAliasResult(String newText, String userMessage) {
		this.newText = newText;
		this.userMessage = userMessage;
	}

	public String getNewText() {
		return newText;
	}

	public String getUserMessage() {
		return userMessage;
	}

	public void setNewText(String newText) {
		this.newText = newText;
	}

	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}

}
