package raptor.script;

public interface ScriptContext {
	public void alert(String message);

	public long getPingMillis();

	public String getUserFollowing();

	public int getUserIdleSeconds();

	public String getUserName();

	public void openChannelTab(String channel);

	public void openPartnerTab();

	public void openPersonTab(String person);

	public void openRegExTab(String regularExpression);

	public void openUrl(String url);

	public void playBughouseSound(String soundName);

	public void playSound(String soundName);

	public String prompt(String message);

	public void send(String message);

	public void speak(String message);
}
