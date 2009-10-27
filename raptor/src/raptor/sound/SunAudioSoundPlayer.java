package raptor.sound;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

/**
 * The Sun Audio Sound player.
 */
public class SunAudioSoundPlayer implements SoundPlayer {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(SunAudioSoundPlayer.class);

	public void dispose() {
	}

	public void init() {
	}

	public void playBughouseSound(final String sound) {
		play(Raptor.RESOURCES_DIR + "sounds/bughouse/" + sound + ".wav");
	}

	public void playSound(final String sound) {
		play(Raptor.RESOURCES_DIR + "sounds/" + sound + ".wav");
	}

	protected void play(String fileName) {
		InputStream inputStream = null;
		AudioStream audioStream = null;
		try {
			// open the sound file as a Java input stream
			inputStream = new FileInputStream(fileName);
			// create an audiostream from the inputstream
			audioStream = new AudioStream(inputStream);
			// play the audio clip with the audioplayer class
			AudioPlayer.player.start(audioStream);
		} catch (Exception e) {
			Raptor.getInstance().onError(
					"Error occured playing sound file: " + fileName, e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Throwable t) {
			}
			try {
				if (audioStream != null) {
					audioStream.close();
				}
			} catch (Throwable t) {
			}
		}
	}
}
