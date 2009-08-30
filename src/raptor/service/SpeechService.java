package raptor.service;

public class SpeechService {
	private static final SpeechService instance = new SpeechService();

	public static SpeechService getInstance() {
		return instance;
	}

	public void speak(String message) {

	}
}
