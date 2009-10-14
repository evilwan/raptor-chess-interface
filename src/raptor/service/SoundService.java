/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.speech.Speech;
import raptor.speech.SpeechUtils;

/**
 * A Singleton service that plays sounds.
 */
public class SoundService {
	private static final Log LOG = LogFactory.getLog(SoundService.class);
	private static final SoundService instance = new SoundService();

	/**
	 * Returns the singleton instance.
	 */
	public static SoundService getInstance() {
		return instance;
	}

	protected Map<String, String> soundToClip = new HashMap<String, String>();

	protected Map<String, String> bugSoundToClip = new HashMap<String, String>();

	protected Speech speech = null;

	private SoundService() {
		init();
	}

	public void dispose() {
		soundToClip.clear();
		if (speech != null) {
			speech.dispose();
		}
	}

	public String[] getBughouseSoundKeys() {
		return bugSoundToClip.keySet().toArray(new String[0]);
	}

	/**
	 * I have tried caching the Clips. However i ran out of lines. So now i just
	 * create a new clip each time.
	 */
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
				String key = currentFile.getName();
				int dotIndex = key.indexOf(".");
				soundToClip.put(key.substring(0, dotIndex), currentFile
						.getAbsolutePath());
			}

			file = new File("resources/common/sounds/bughouse");
			files = file.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".wav");
				}
			});
			for (File currentFile : files) {

				String key = currentFile.getName();
				int dotIndex = key.indexOf(".");
				bugSoundToClip.put(key.substring(0, dotIndex), currentFile
						.getAbsolutePath());
			}

		} catch (Throwable t) {
			LOG.error("Error loading sounds", t);
		}

		try {
			speech = SpeechUtils.getSpeech();
			speech.init();
			LOG.info("Initialized speech: " + speech);
		} catch (Throwable t) {
			LOG.error("Error initializing speech", t);
			speech = null;
		}
		LOG.info("Initializing sound service complete: "
				+ (System.currentTimeMillis() - startTime));
	}

	/**
	 * Specify the name of a file in resources/common/sounds/bughouse without
	 * the .wav to play the sound i.e. "+".
	 */
	public void playBughouseSound(final String sound) {
		/**
		 * I have tried caching the Clips. However i ran out of lines. So now i
		 * just create a new clip each time.
		 */
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.APP_SOUND_ENABLED)) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					String soundFile = bugSoundToClip.get(sound);
					if (soundFile == null) {
						Raptor.getInstance().onError("Unknown sound " + sound);
					}
					try {

						Clip clip = AudioSystem.getClip();
						AudioInputStream stream = AudioSystem
								.getAudioInputStream(new FileInputStream(
										soundFile));
						clip.open(stream);
						clip.setFramePosition(0);
						clip.start();
						Thread.sleep(100);
						while (clip.isRunning()) {
							Thread.sleep(100);
						}
						clip.close();

					} catch (Throwable t) {
						Raptor.getInstance().onError(
								"Error playing sound " + sound, t);
					}
				}
			});
		}
	}

	/**
	 * Specify the name of a file in resources/common/sounds without the .wav to
	 * play the sound i.e. "alert".
	 */
	public void playSound(final String sound) {
		/**
		 * I have tried caching the Clips. However i ran out of lines. So now i
		 * just create a new clip each time.
		 */
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.APP_SOUND_ENABLED)) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					String soundFile = soundToClip.get(sound);
					if (soundFile == null) {
						Raptor.getInstance().onError("Unknown sound " + sound);
					}
					try {

						Clip clip = AudioSystem.getClip();
						AudioInputStream stream = AudioSystem
								.getAudioInputStream(new FileInputStream(
										soundFile));
						clip.open(stream);
						clip.setFramePosition(0);
						clip.start();
						Thread.sleep(100);
						while (clip.isRunning()) {
							Thread.sleep(100);
						}
						clip.close();

					} catch (Throwable t) {
						Raptor.getInstance().onError(
								"Error playing sound " + sound, t);
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
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.APP_SOUND_ENABLED)) {
			LOG.info("Speaking " + text);
			if (speech != null) {
				speech.speak(text);
			}
		}
	}
}
