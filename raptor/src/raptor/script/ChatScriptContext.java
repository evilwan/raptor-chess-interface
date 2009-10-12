package raptor.script;

public interface ChatScriptContext extends ScriptContext {
	public String getMessage();

	public String getMessageChannel();

	public String getMessageSource();
}
