package raptor.script;

/**
 * This interface contains what is available for ChatScripts in addition to the
 * methods in ScriptContext.
 * 
 * These methods are not available for the toolbar scripts, only chat scripts.
 */
public interface ChatScriptContext extends ScriptContext {
	/**
	 * Returns the message currently being parsed.
	 */
	public String getMessage();

	/**
	 * If the message is a channel message, this will return the messages
	 * channel. Otherwise it will return null.
	 */
	public String getMessageChannel();

	/**
	 * Returns the source of the message. For tell and ptell events this is the
	 * name of the user sending the tell.
	 */
	public String getMessageSource();
}
