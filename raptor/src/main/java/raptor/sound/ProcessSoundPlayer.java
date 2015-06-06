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

import raptor.util.RaptorLogger;
 

import raptor.Raptor;
import raptor.pref.PreferenceKeys;

/**
 * Plays sounds using the process defined with the preference key:
 * SOUND_PROCESS_NAME.
 */
public class ProcessSoundPlayer implements SoundPlayer {
	@SuppressWarnings("unused")
	private static final RaptorLogger LOG = RaptorLogger.getLog(ProcessSoundPlayer.class);

	public void dispose() {
	}

	public void init() {
	}

	public void play(String pathToSound) {
		try {
			String preference = Raptor.getInstance().getPreferences().getString(
					PreferenceKeys.SOUND_PROCESS_NAME);
			String[] params = preference.split(" ");
			String[] paramsWithPath = new String[params.length + 1];
			System.arraycopy(params,0,paramsWithPath,0,params.length);
			paramsWithPath[params.length] = pathToSound;
			new ProcessBuilder(paramsWithPath).start().waitFor();
		} catch (Exception e) {
			Raptor.getInstance().onError(
					"Error occured playing sound file: " + pathToSound, e);
		}
	}

	public void playBughouseSound(final String sound) {
		play(Raptor.RESOURCES_DIR + "sounds/bughouse/" + sound + ".wav");
	}

	public void playSound(final String sound) {
		play(Raptor.RESOURCES_DIR + "sounds/" + sound + ".wav");
	}
}
