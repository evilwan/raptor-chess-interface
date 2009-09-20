package raptor.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SpeechService {
	private static final Log LOG = LogFactory.getLog(SpeechService.class);
	private static final SpeechService instance = new SpeechService();

	public static SpeechService getInstance() {
		return instance;
	}

	public void speak(String message) {
		LOG.debug("Speaking " + message);
	}
}
