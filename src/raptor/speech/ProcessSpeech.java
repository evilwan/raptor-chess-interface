package raptor.speech;

import raptor.service.ThreadService;

public class ProcessSpeech implements Speech {

	protected String command;

	public ProcessSpeech(String command) {
		this.command = command;
	}

	public void dispose() {
	}

	public void init() {
	}

	public void speak(final String text) {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				synchronized (ProcessSpeech.this) {
					try {
						Process process = Runtime.getRuntime().exec(
								new String[] { command, text });
						process.waitFor();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}
