package raptor.service;

import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.App;
import raptor.pref.PreferenceKeys;

public class SoundService {

	private static final Log LOG = LogFactory.getLog(SoundService.class);

	private static final int BUFFER_SIZE = 128000;

	private static final SoundService instance = new SoundService();

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
		if (App.getInstance().getPreferences().getBoolean(
				PreferenceKeys.SOUND_ENABLED)) {
			final String url = "file:resources/common/sounds/" + sound + ".wav";
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					LOG.debug("Playing sound " + url);

					SourceDataLine dataLine = null;
					AudioInputStream stream = null;
					try {
						stream = AudioSystem.getAudioInputStream(new URL(url));

						// At present, ALAW and ULAW encodings must be
						// converted
						// to PCM_SIGNED before it can be played
						AudioFormat format = stream.getFormat();
						if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
							format = new AudioFormat(
									AudioFormat.Encoding.PCM_SIGNED, format
											.getSampleRate(), format
											.getSampleSizeInBits(), format
											.getChannels(), format
											.getFrameSize(), format
											.getFrameRate(), true);

							// big endian
							stream = AudioSystem.getAudioInputStream(format,
									stream);
						}

						// Create the dataLine
						DataLine.Info info = new DataLine.Info(
								SourceDataLine.class, stream.getFormat(),
								((int) stream.getFrameLength() * format
										.getFrameSize()));
						dataLine = (SourceDataLine) AudioSystem.getLine(info);

						// This method does not return until the audio file
						// is
						// completely loaded
						dataLine.open(stream.getFormat());

						// Start playing
						dataLine.start();

						byte[] buffer = new byte[BUFFER_SIZE];
						int r = stream.read(buffer, 0, BUFFER_SIZE);
						while (r != -1) {
							if (r > 0) {
								dataLine.write(buffer, 0, r);
							}
							r = stream.read(buffer, 0, BUFFER_SIZE);
						}
						dataLine.drain();

					} catch (Exception e) {
						LOG.error("Error playing sound: " + url, e);
					} finally {
						dataLine.close();
						try {
							stream.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
	}
}
