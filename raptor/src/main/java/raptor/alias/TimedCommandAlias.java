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
package raptor.alias;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import raptor.service.ThreadService;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class TimedCommandAlias extends RaptorAlias {

	public static HashMap<Runnable, Boolean> runningTimedCommands = new HashMap<Runnable, Boolean>();

	public TimedCommandAlias() {
		super(
				"timed",
				"Sends a repeating command every ### minutes. "
						+ "You can stop all timed commands with 'timed kill'",
				"'timed ### message OR timed [kill | remove]'. Example: 'timed 1 tell 24 partner' will "
						+ "send tell 24 partner every 1 minute. To stop all timed commands use 'timed kill'.");
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			final String command) {
		if (StringUtils.startsWith(command, "timed")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(command, " ",
					true);
			tok.nextToken();
			final String firstWord = tok.nextToken();

			if (firstWord == null) {
				return null;
			} else if (firstWord.equalsIgnoreCase("kill")
					|| firstWord.equalsIgnoreCase("remove")) {
				int commandsKilled = 0;
				for (Runnable key : runningTimedCommands.keySet()) {
					runningTimedCommands.put(key, false);
					commandsKilled++;
				}
				return new RaptorAliasResult(null, "Killed " + commandsKilled
						+ " timed commands.");
			} else if (NumberUtils.isDigits(firstWord)) {
				final String message = tok.getWhatsLeft();

				Runnable runnable = new Runnable() {
					public void run() {
						if (!controller.isDisposed()
								&& runningTimedCommands.get(this) != null
								&& runningTimedCommands.get(this)) {
							controller.getConnector().sendMessage(message);
							ThreadService.getInstance().scheduleOneShot(
									Integer.parseInt(firstWord) * 1000 * 60,
									this);
						}
					}
				};

				ThreadService.getInstance().scheduleOneShot(
						Integer.parseInt(firstWord) * 1000 * 60, runnable);
				runningTimedCommands.put(runnable, true);
				return new RaptorAliasResult(tok.getWhatsLeft(),
						"Commands will be sent every " + firstWord
								+ " minutes. Use 'timed kill' to stop it.");
			} else {
				return new RaptorAliasResult(null, "Invalid syntax: Usage"
						+ getUsage());
			}
		}
		return null;
	}
}