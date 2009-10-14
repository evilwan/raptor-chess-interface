package raptor.speech;

public interface Speech {
	public void dispose();

	public void init();

	public void speak(String text);
}
