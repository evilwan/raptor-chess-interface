package raptor.speech;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;

public class SpeechUtils {
	public static Speech getSpeech() {
		if (Raptor.getInstance().getPreferences().contains(
				PreferenceKeys.SPEECH_PROCESS_NAME)) {
			return new ProcessSpeech(Raptor.getInstance().getPreferences()
					.getString(PreferenceKeys.SPEECH_PROCESS_NAME));
		} else {
			String osName = System.getProperty("os.name");
			if (osName.startsWith("Mac OS")) {
				return new OSXSpeech();
			} else {
				return null;
			}
		}
	}
}
