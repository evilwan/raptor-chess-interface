package raptor.service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;

/**
 * A Singleton service that plays sounds.
 */
public class SoundService {

	private static final Log LOG = LogFactory.getLog(SoundService.class);

	private static final SoundService instance = new SoundService();

	protected Map<String, Clip> soundToClip = new HashMap<String, Clip>();

	/**
	 * Returns the singleton instance.
	 */
	public static SoundService getInstance() {
		return instance;
	}

	private SoundService() {
		init();
	}

	protected void init() {
		LOG.info("Initializing sound service.");
		long startTime = System.currentTimeMillis();
		try {
			File file = new File("resources/common/sounds/");
			File[] files = file.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".wav");
				}
			});
			for (File currentFile : files) {
				Clip clip = AudioSystem.getClip();
				AudioInputStream stream = AudioSystem
						.getAudioInputStream(currentFile);
				clip.open(stream);
				String key = currentFile.getName();
				int dotIndex = key.indexOf(".");
				soundToClip.put(key.substring(0, dotIndex), clip);
				LOG.debug("Loaded sound " + currentFile.getName());
			}
		} catch (Throwable t) {
		}
		LOG.info("Initializing sound service complete: "
				+ (System.currentTimeMillis() - startTime));
	}

	public void dispose() {
		soundToClip.clear();
		// Trying to close all of the clips results in noises in OSX 10.4
	}

	/**
	 * Specify the name of a file in resources/common/sounds without the .wav to
	 * play the sound i.e. "alert".
	 * 
	 * @param sound
	 */
	public void playSound(final String sound) {
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.APP_SOUND_ENABLED)) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {

					Clip clip = soundToClip.get(sound);
					if (clip == null) {
						throw new IllegalArgumentException("Unknown sound "
								+ sound);
					}
					synchronized (clip) {
						if (!clip.isRunning()) {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Playing Sound " + sound + ".");
							}
							clip.setFramePosition(0);
							clip.start();
						} else {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Sound " + sound
										+ " is already playing.");
							}
						}
					}
				}
			});
		}
	}

	/**
	 * Speaks the specified text.
	 * 
	 * @param text
	 *            text to speak.
	 * 
	 *            This method is not yet implemented.
	 */
	public void textToSpeech(String text) {

		// list all available voices
		// System.out.println();
		// System.out.println("All voices available:");
		// VoiceManager voiceManager = VoiceManager.getInstance();
		// Voice[] voices = voiceManager.getVoices();
		// for (int i = 0; i < voices.length; i++) {
		// System.out.println("    " + voices[i].getName() + " ("
		// + voices[i].getDomain() + " domain)");
		// }
		//
		// String voiceName = "kevin16";
		// Voice helloVoice = voiceManager.getVoice(voiceName);
		// if (helloVoice == null) {
		// System.err.println("Cannot find a voice named " + voiceName
		// + ".  Please specify a different voice.");
		// } else {
		// System.out.println("allocate voice");
		// helloVoice.allocate();
		// System.out.println("speak text");
		// helloVoice.speak(text);
		// System.out.println("deallocate voice");
		// helloVoice.deallocate();
		// }
	}
}
