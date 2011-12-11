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
package raptor.connector.ics.bughouse;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import raptor.chat.Bugger;
import raptor.chat.Bugger.BuggerStatus;
import raptor.connector.ics.IcsUtils;
import raptor.util.RaptorLogger;
import raptor.util.RaptorStringTokenizer;

/**
 * This code was adapted from some code johnthegreat for Raptor.
 */
public class BugWhoUParser {
	private static final RaptorLogger LOG = RaptorLogger
			.getLog(BugWhoUParser.class);

	public static final String ID = "Unpartnered players with bugopen on\n";
	public static final String ID2 = "\nUnpartnered players with bugopen on\n";

	public BugWhoUParser() {
	}

	public Bugger[] parse(String message) {
		try {
			if (message.startsWith(ID)) {
				message = message.substring(ID.length(), message.length());
				message = message
						.replaceAll(
								"[0-9]+ players displayed \\(of [0-9]+\\). \\(\\*\\) indicates system administrator.",
								"");
				message = message.replaceAll("\nfics%", "");
				return process(message.trim());
			} else if (message.startsWith(ID2)) {
				message = message.substring(ID2.length(), message.length());
				message = message
						.replaceAll(
								"[0-9]+ player displayed \\(of [0-9]+\\). \\(\\*\\) indicates system administrator.",
								"");
				message = message.replaceAll("\nfics%", "");
				return process(message.trim());
			}
		} catch (Throwable t) {
			// Just log it for now and eat it. Soft crash on these there are
			// subtle bugs in the message parsing.
			LOG.error("Unexpected error parsing BugWho U message\r" + message,
					t);
			return null;
		}

		return null;
	}

	public Bugger[] process(String message) {
		if (message.equals("")) {
			return new Bugger[0];
		}
		RaptorStringTokenizer tok = new RaptorStringTokenizer(message, " \n",
				true);
		List<Bugger> result = new ArrayList<Bugger>(30);
		while (tok.hasMoreTokens()) {
			Bugger bugger = new Bugger();
			String rating = tok.nextToken();
			int specialCharIndex = StringUtils.indexOfAny(rating, new char[] {
					'^', '~', ':', '#', '.', '&' });
			if (specialCharIndex != -1) {
				// This is the case where everything runs together.
				bugger.setRating(rating.substring(0, specialCharIndex));
				bugger.setStatus(IcsUtils.getBuggserStatus(rating.substring(
						specialCharIndex, specialCharIndex + 1)));
				bugger.setName(IcsUtils.stripTitles(rating.substring(
						specialCharIndex + 1, rating.length())));
			} else {
				// This is the case where status could be the first char of the
				// name.
				bugger.setRating(rating);
				String name = tok.nextToken();
				specialCharIndex = StringUtils.indexOfAny(name, new char[] {
						'^', '~', ':', '#', '.', '&' });
				if (specialCharIndex != -1) {
					bugger.setStatus(IcsUtils.getBuggserStatus(name.substring(
							0, 1)));
					bugger.setName(IcsUtils.stripTitles(name.substring(1,
							name.length())));
				} else {
					bugger.setStatus(BuggerStatus.Available);
					bugger.setName(IcsUtils.stripTitles(name));

				}
			}

			result.add(bugger);
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("processed buggers = " + result);
		}
		return result.toArray(new Bugger[0]);
	}
}
