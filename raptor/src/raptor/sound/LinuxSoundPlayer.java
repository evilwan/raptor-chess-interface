package raptor.sound;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;

/**
 * The Linux Audio Sound player. Uses SourceDataLine to play sounds. For some
 * reason Clip sounds do not work well in linux.
 */
public class LinuxSoundPlayer implements SoundPlayer {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(LinuxSoundPlayer.class);

	protected Map<String, Boolean> soundsPlaying = new HashMap<String, Boolean>();

	public void dispose() {
	}

	public void init() {
	}

	/**
	 * Specify the name of a file in resources/sounds/bughouse without the .wav
	 * to play the sound i.e. "+".
	 */
	public void play(final String pathToSound) {
		Boolean isPlaying = soundsPlaying.get(pathToSound);
		// This prevents excessive bug sounds from being played.
		// That can result in maxing out the number of lines
		// available and cause all kinds of problems in OSX 10.4
		if (isPlaying == null || !isPlaying) {
			soundsPlaying.put(pathToSound, true);
			SourceDataLine auline = null;
			try {
				File soundFile = new File(pathToSound);
				AudioInputStream audioInputStream = AudioSystem
						.getAudioInputStream(soundFile);

				AudioFormat audioFormat = audioInputStream.getFormat();
				DataLine.Info info = new DataLine.Info(SourceDataLine.class,
						audioFormat, audioFormat.getSampleSizeInBits());
				boolean bIsSupportedDirectly = AudioSystem
						.isLineSupported(info);
				if (!bIsSupportedDirectly) {
					AudioFormat sourceFormat = audioFormat;
					AudioFormat targetFormat = new AudioFormat(
							AudioFormat.Encoding.PCM_SIGNED, sourceFormat
									.getSampleRate(), 16, sourceFormat
									.getChannels(),
							sourceFormat.getChannels() * 2, sourceFormat
									.getSampleRate(), false);
					audioInputStream.close();
					audioInputStream = AudioSystem.getAudioInputStream(
							targetFormat, audioInputStream);
					audioFormat = audioInputStream.getFormat();
				}

				AudioFormat format = audioInputStream.getFormat();
				auline = (SourceDataLine) AudioSystem.getLine(info);
				auline.open(format);

				auline.start();
				int nBytesRead = 0;
				byte[] abData = new byte[524288];

				try {
					while (nBytesRead != -1) {
						nBytesRead = audioInputStream.read(abData, 0,
								abData.length);
						if (nBytesRead >= 0) {
							auline.write(abData, 0, nBytesRead);
						}
					}
				} finally {
					try {
						auline.drain();
					} catch (Throwable t) {
					}
				}
			} catch (Throwable t) {
				Raptor.getInstance().onError(
						"Error playing sound " + pathToSound, t);
			} finally {
				try {
					auline.close();
				} catch (Throwable t) {
				}
				soundsPlaying.put(pathToSound, false);
			}
		}
	}

	public void playBughouseSound(final String sound) {
		play(Raptor.RESOURCES_DIR + "sounds/bughouse/" + sound + ".wav");
	}

	public void playSound(final String sound) {
		play(Raptor.RESOURCES_DIR + "sounds/" + sound + ".wav");
	}
}
