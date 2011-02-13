/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import raptor.Raptor;
import raptor.util.RaptorLogger;

/**
 * Uses Clips to play sounds. Ignores the encoding.
 */
public class JavaxSampledSoundPlayer implements SoundPlayer {
	private static final RaptorLogger LOG = RaptorLogger.getLog(JavaxSampledSoundPlayer.class);

	protected Map<String, Boolean> soundsPlaying = new HashMap<String, Boolean>();

	public void dispose() {
		// Don't bother closing the clips. You get screeching noises if you do.
		LOG.info("Disposing Sounds");
		soundsPlaying.clear();
	}

	/**
	 * I have tried caching the Clips. However i ran out of lines. So now i just
	 * create a new clip each time.
	 */
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
			try {
				File soundFile = new File(pathToSound);
				final Clip clip = AudioSystem.getClip();
				final AudioInputStream stream = AudioSystem
						.getAudioInputStream(soundFile);
				clip.addLineListener(new LineListener() {
					public void update(LineEvent arg0) {
						LineEvent.Type type = arg0.getType();
						if (type == LineEvent.Type.STOP) {
							try {
								soundsPlaying.put(pathToSound, false);
								stream.close();
							} catch (Throwable t) {
							}
						}
					}
				});
				clip.open(stream);
				clip.start();
			} catch (Throwable t) {
				Raptor.getInstance().onError(
						"Error playing sound " + pathToSound, t);
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
