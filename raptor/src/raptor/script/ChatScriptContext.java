package raptor.script;

public interface ChatScriptContext extends ScriptContext {
	public String getMessage();

	public String getMessageSource();

	public String getMessageType();

	public String getPartnerName();

	public String getUserFollowing();

	public int getUserIdleSeconds();

	public long getUserLagMilliseconds();

	public String getUserName();

	public void setIgnoreMessage(boolean isIgnoring);

	public void setMessage(String message);

	public void setMessageSource(String source);

	public void setMessageType(String type);
}
