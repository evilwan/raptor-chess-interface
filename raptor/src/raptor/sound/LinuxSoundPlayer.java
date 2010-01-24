/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
