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
package raptor.service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.sound.SoundPlayer;
import raptor.sound.SoundUtils;
import raptor.speech.Speech;
import raptor.speech.SpeechUtils;
import raptor.util.RaptorLogger;

/**
 * A Singleton service that plays sounds.
 */
public class SoundService {
	private static final RaptorLogger LOG = RaptorLogger.getLog(SoundService.class);
	private static final SoundService instance = new SoundService();

	/**
	 * Returns the singleton instance.
	 */
	public static SoundService getInstance() {
		return instance;
	}

	protected String[] bughouseSounds;
	protected SoundPlayer soundPlayer;

	protected Speech speech = null;

	private SoundService() {
		init();
	}

	public void dispose() {
		// Don't bother closing the clips. You get screeching noises if you do.
		if (soundPlayer != null) {
			soundPlayer.dispose();
		}
		if (speech != null) {
			speech.dispose();
		}
	}

	public String[] getBughouseSoundKeys() {
		return bughouseSounds;
	}

	public void initSoundPlayer() {
		if (soundPlayer != null) {
			try {
				soundPlayer.dispose();
			} catch (Throwable t) {
			}
		}
		soundPlayer = SoundUtils.getSoundPlayer();
		if (soundPlayer != null) {
			soundPlayer.init();
			LOG.info("Initialized soundPlayer: " + soundPlayer);
		} else {
			LOG.error("There is no sound player currently configured!");
			soundPlayer = null;
		}
	}

	public void initSpeech() {
		try {
			speech = SpeechUtils.getSpeech();
			if (speech != null) {
				speech.init();
				LOG.info("Initialized speech: " + speech);
			} else {
				LOG.info("No speech is currently configured.");
			}
		} catch (Throwable t) {
			Raptor.getInstance().onError("Error initializing speech", t);
			speech = null;
		}
	}

	public boolean isSpeechSetup() {
		return Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.APP_SOUND_ENABLED)
				&& speech != null;
	}

	/**
	 * Plays the specified sound.
	 * 
	 * @param pathToSound
	 *            The fully qualified path to the sound.
	 */
	public void play(final String pathToSound) {
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.APP_SOUND_ENABLED)
				&& soundPlayer != null) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					soundPlayer.play(pathToSound);
				}
			});
		}
	}

	/**
	 * Specify the name of a file in resources/sounds/bughouse without the .wav
	 * to play the sound i.e. "+".
	 */
	public void playBughouseSound(final String sound) {
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.APP_SOUND_ENABLED)
				&& soundPlayer != null) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					soundPlayer.playBughouseSound(sound);
				}
			});
		}
	}

	/**
	 * Specify the name of a file in resources/common/sounds without the .wav to
	 * play the sound i.e. "alert".
	 */
	public void playSound(final String sound) {
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.APP_SOUND_ENABLED)
				&& soundPlayer != null) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					soundPlayer.playSound(sound);
				}
			});
		}
	}

	/**
	 * Speaks the specified text. Returns true if speech is configured and the
	 * result was spoken.
	 */
	public boolean textToSpeech(String text) {
		boolean result = false;
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.APP_SOUND_ENABLED)) {
			if (speech == null
					&& Raptor.getInstance().getPreferences().contains(
							PreferenceKeys.SPEECH_PROCESS_NAME)) {
				initSpeech();
			}
			if (speech != null) {
				speech.speak(text);
				result = true;
				if (LOG.isDebugEnabled()) {
					LOG.debug("Spoke " + text);
				}
			}
		}
		return result;
	}

	/**
	 * I have tried caching the Clips. However i ran out of lines. So now i just
	 * create a new clip each time.
	 */
	protected void init() {
		LOG.info("Initializing sound service.");
		long startTime = System.currentTimeMillis();
		try {
			List<String> bughouseSoundsList = new ArrayList<String>(20);
			File file = new File(Raptor.RESOURCES_DIR + "sounds/bughouse");
			File[] files = file.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".wav");
				}
			});
			for (File currentFile : files) {

				String key = currentFile.getName();
				int dotIndex = key.indexOf(".");
				key = key.substring(0, dotIndex);
				bughouseSoundsList.add(key);
			}
			bughouseSounds = bughouseSoundsList.toArray(new String[0]);
		} catch (Throwable t) {
			LOG.error("Error loading sounds", t);
		}

		initSoundPlayer();
		initSpeech();

		LOG.info("Initializing sound service complete: "
				+ (System.currentTimeMillis() - startTime) + "ms");
	}
}
