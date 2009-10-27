package raptor.sound;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;

/**
 * The Linux Audio Sound player. For some reason the old Decaf way of doing it
 * worked. That is what this is.
 * 
 * NOTE: This has issues in OS X Carbon.
 */
public class LinuxSoundPlayer implements SoundPlayer {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(LinuxSoundPlayer.class);

	private static final int BUFFER_SIZE = 128000;

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
		SourceDataLine dataLine = null;
		AudioInputStream stream = null;
		try {
			stream = AudioSystem.getAudioInputStream(new File(fileName));

			// At present, ALAW and ULAW encodings must be
			// converted
			// to PCM_SIGNED before it can be played
			AudioFormat format = stream.getFormat();
			if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
				format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
						format.getSampleRate(), format.getSampleSizeInBits(),
						format.getChannels(), format.getFrameSize(), format
								.getFrameRate(), true); // big
				// endian
				stream = AudioSystem.getAudioInputStream(format, stream);
			}

			// Create the dataLine
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, stream
					.getFormat(), ((int) stream.getFrameLength() * format
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
			Raptor.getInstance().onError("Error playing sound: " + fileName, e);
		} finally {
			if (dataLine != null) {
				dataLine.close();
			}
		}
	}
}
