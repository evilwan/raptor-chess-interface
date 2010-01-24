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
package raptor.speech;

//import javax.speech.EngineCreate;
//import javax.speech.EngineList;
//import javax.speech.synthesis.Synthesizer;
//import javax.speech.synthesis.SynthesizerModeDesc;
//import javax.speech.synthesis.Voice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * I decided not to use FreeTTS speech in Raptor like I did in decaf. It is way
 * to big like 10 megs and in addition to that the default voices are awful and
 * to get good ones it takes lots of configuration. Instead of offering free tts
 * there is a process speech. Windows users can set it up via a command line
 * program. See the speech preferences for more information.
 * 
 * I am still keeping this around in case I decide to support it in the future.
 */
public class FreeTTSSpeech implements Speech {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(FreeTTSSpeech.class);
	@SuppressWarnings("unused")
	private static final String VOICE_NAME = "kevin16";

	public static void main(String[] args) {
		FreeTTSSpeech speech = new FreeTTSSpeech();
		speech.init();
		speech.speak("Hello this is a test");
	}

	// private Synthesizer synthesizer;

	public FreeTTSSpeech() {
	}

	public void dispose() {
		// if (synthesizer != null) {
		// try {
		// synthesizer.deallocate();
		// } catch (Exception e) {
		// }
		// }
	}

	@Override
	public void finalize() throws Exception {
		dispose();
	}

	public void init() {
		// try {
		// // istAllVoices();
		// SynthesizerModeDesc desc = new SynthesizerModeDesc(null, "general",
		// /*
		// * use
		// * "time"
		// * or
		// * "general"
		// */
		// java.util.Locale.US, Boolean.FALSE, null);
		//
		// FreeTTSEngineCentral central = new FreeTTSEngineCentral();
		// EngineList list = central.createEngineList(desc);
		//
		// for (Object o : list) {
		// System.out.println(o);
		// }
		//
		// if (list != null && list.size() > 0) {
		// EngineCreate creator = (EngineCreate) list.get(0);
		// synthesizer = (Synthesizer) creator.createEngine();
		//
		// } else {
		// LOG.warn("No voices");
		// }
		// if (synthesizer == null) {
		// LOG.warn("No voices");
		// }
		// synthesizer.allocate();
		// synthesizer.resume();
		//
		// /*
		// * Choose the voice.
		// */
		// desc = (SynthesizerModeDesc) synthesizer.getEngineModeDesc();
		// Voice[] voices = desc.getVoices();
		// Voice voice = null;
		//
		// for (int i = 0; i < voices.length; i++) {
		// System.out.println(voices[i].getName());
		// }
		// for (int i = 0; i < voices.length; i++) {
		// if (voices[i].getName().equals(VOICE_NAME)) {
		// voice = voices[i];
		// break;
		// }
		// }
		// if (voice == null) {
		// LOG.error("Synthesizer does not have a voice named "
		// + VOICE_NAME + ".");
		// throw new RuntimeException(
		// "Synthesizer does not have a voice named " + VOICE_NAME
		// + ".");
		// }
		//
		// synthesizer.getSynthesizerProperties().setSpeakingRate(140);
		//
		// synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public void speak(final String text) {
		// if (synthesizer != null) {
		// ThreadService.getInstance().run(new Runnable() {
		// public void run() {
		// if (synthesizer != null) {
		// synchronized (synthesizer) {
		// try {
		// synthesizer.speak(text, null);
		// synthesizer
		// .waitEngineState(Synthesizer.QUEUE_EMPTY);
		// } catch (Exception e) {
		// LOG.error(e);
		// }
		// }
		// }
		// }
		// });
		// }
	}
}
