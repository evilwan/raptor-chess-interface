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
package raptor.connector.fics.chat.john;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

public class BugWhoUParser {
	private static final Logger LOGGER = Logger.getLogger(BugWhoUParser.class);

	public BugWhoUParser(String text) {
		text = text
				.replaceAll(
						"[0-9]+ players displayed \\(of [0-9+]\\). \\(\\*\\) indicates system administrator.",
						"");
	}

	public Bugger[] parse(String text, boolean showUnrateds, boolean showGuests) {
		StringTokenizer tokens = new StringTokenizer(text, "^~:#.& ", true);
		ArrayList<Bugger> arr = new ArrayList<Bugger>();

		while (tokens.hasMoreTokens()) {
			Bugger p = new Bugger();
			String rating = tokens.nextToken();
			if (rating.equals("----") && !showUnrateds) {
				continue;
			}
			if (rating.equals("++++") && !showGuests) {
				continue;
			}
			p.setRating(rating);
			String status = tokens.nextToken();
			if (status.length() > 1) { /* something wrong */
			}
			char modifier = status.charAt(0);
			p.setStatus(modifier);
			String username = tokens.nextToken();
			p.setUsername(username);

			arr.add(p);
		}
		Bugger[] out = arr.toArray(new Bugger[arr.size()]);
		LOGGER.info("usernames = " + java.util.Arrays.toString(out));
		return out;
	}

}
