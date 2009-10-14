package raptor.speech;

public class SpeechUtils {
	public static Speech getSpeech() {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
			return new OSXSpeech();
		} else {
			return new FreeTTSSpeech();
		}
	}
}
