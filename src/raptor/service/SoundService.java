package raptor.service;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;

/**
 * A Singleton service that plays sounds.
 */
public class SoundService {

	private static final Log LOG = LogFactory.getLog(SoundService.class);

	private static final int BUFFER_SIZE = 128000;

	private static final SoundService instance = new SoundService();

	/**
	 * Returns the singleton instance.
	 */
	public static SoundService getInstance() {
		return instance;
	}

	/**
	 * Specify the name of a file in resources/common/sounds without the .wav to
	 * play the sound i.e. "alert".
	 * 
	 * @param sound
	 */
	public void playSound(final String sound) {
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.SOUND_ENABLED)) {
			final String fileName = "resources/common/sounds/" + sound + ".wav";
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					LOG.debug("Playing sound " + fileName);

					File soundFile = new File(fileName);
					AudioInputStream audioInputStream = null;

					try {
						audioInputStream = AudioSystem
								.getAudioInputStream(soundFile);
					} catch (Exception t) {
						LOG.error("Error getting audio input stream", t);
					}

					AudioFormat audioFormat = audioInputStream.getFormat();
					DataLine.Info info = new DataLine.Info(
							SourceDataLine.class, audioFormat);
					SourceDataLine line = null;

					try {
						try {
							line = (SourceDataLine) AudioSystem.getLine(info);
							line.open(audioFormat);
						} catch (LineUnavailableException e) {
							LOG.error("No line available", e);
						} catch (Throwable t) {
							LOG.error("Error getting line", t);
						}

						line.start();

						int nBytesRead = 0;

						byte[] abData = new byte[BUFFER_SIZE];
						while (nBytesRead != -1) {
							try {
								nBytesRead = audioInputStream.read(abData, 0,
										abData.length);
							} catch (IOException e) {
								e.printStackTrace();
							}
							if (nBytesRead >= 0) {
								for (int i = 0; i < nBytesRead; i += 4) {
									byte[] abData2 = { abData[i + 0],
											abData[i + 1], abData[i + 2],
											abData[i + 3] };
									line.write(abData2, 0, 4);
								}
							}
						}
					} finally {
						try {
							line.drain();
						} catch (Throwable t) {
						}
						try {
							line.close();
						} catch (Throwable t) {
						}
						try {
							audioInputStream.close();
						} catch (Throwable t) {
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
