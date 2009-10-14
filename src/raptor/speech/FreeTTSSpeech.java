package raptor.speech;

import javax.speech.EngineCreate;
import javax.speech.EngineList;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.service.ThreadService;

import com.sun.speech.freetts.jsapi.FreeTTSEngineCentral;

public class FreeTTSSpeech implements Speech {
	private static final Log LOG = LogFactory.getLog(FreeTTSSpeech.class);
	private static final String VOICE_NAME = "kevin16";

	public static void main(String[] args) {
		FreeTTSSpeech speech = new FreeTTSSpeech();
		speech.init();
		speech.speak("Hello this is a test");
	}

	private Synthesizer synthesizer;

	public FreeTTSSpeech() {
	}

	public void dispose() {
		if (synthesizer != null) {
			try {
				synthesizer.deallocate();
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void finalize() throws Exception {
		dispose();
	}

	public void init() {
		try {
			// istAllVoices();
			SynthesizerModeDesc desc = new SynthesizerModeDesc(null, "general", /*
																				 * use
																				 * "time"
																				 * or
																				 * "general"
																				 */
			java.util.Locale.US, Boolean.FALSE, null);

			FreeTTSEngineCentral central = new FreeTTSEngineCentral();
			EngineList list = central.createEngineList(desc);

			for (Object o : list) {
				System.out.println(o);
			}

			if (list != null && list.size() > 0) {
				EngineCreate creator = (EngineCreate) list.get(0);
				synthesizer = (Synthesizer) creator.createEngine();

			} else {
				LOG.warn("No voices");
			}
			if (synthesizer == null) {
				LOG.warn("No voices");
			}
			synthesizer.allocate();
			synthesizer.resume();

			/*
			 * Choose the voice.
			 */
			desc = (SynthesizerModeDesc) synthesizer.getEngineModeDesc();
			Voice[] voices = desc.getVoices();
			Voice voice = null;

			for (int i = 0; i < voices.length; i++) {
				System.out.println(voices[i].getName());
			}
			for (int i = 0; i < voices.length; i++) {
				if (voices[i].getName().equals(VOICE_NAME)) {
					voice = voices[i];
					break;
				}
			}
			if (voice == null) {
				LOG.error("Synthesizer does not have a voice named "
						+ VOICE_NAME + ".");
				throw new RuntimeException(
						"Synthesizer does not have a voice named " + VOICE_NAME
								+ ".");
			}

			synthesizer.getSynthesizerProperties().setSpeakingRate(140);

			synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void speak(final String text) {
		if (synthesizer != null) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					if (synthesizer != null) {
						synchronized (synthesizer) {
							try {
								synthesizer.speak(text, null);
								synthesizer
										.waitEngineState(Synthesizer.QUEUE_EMPTY);
							} catch (Exception e) {
								LOG.error(e);
							}
						}
					}
				}
			});
		}
	}
}
