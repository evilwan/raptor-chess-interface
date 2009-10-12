package raptor.script;

public interface ScriptContext {
	public void alert(String message);

	public void openUrl(String url);

	public void playBughouseSound(String soundName);

	public void playSound(String soundName);

	public String prompt(String message);

	public void send(String message);

	public void speak(String message);
}
