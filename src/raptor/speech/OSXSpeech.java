package raptor.speech;

import raptor.service.ThreadService;

public class OSXSpeech implements Speech {

	public String getDescription() {
		return "OSX native speech.";
	}

	public static void main(String args[]) {
		OSXSpeech speech = new OSXSpeech();
		speech.speak("Hello, This is a test.");
	}

	public void init() {
	}

	public void dispose() {
	}

	public void speak(final String text) {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				synchronized (OSXSpeech.this) {
					try {
						Process process = Runtime.getRuntime().exec(new String[] { "say", text });
						process.waitFor();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}
