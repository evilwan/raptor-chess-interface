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

/*
 bugwho p
 Partnerships not playing bughouse
 1944  RooRooBear / 1664  oub
 1 partnership displayed.
 */

public class BugWhoPParser {
	private static final Logger LOGGER = Logger.getLogger(BugWhoPParser.class);

	public BugWhoPParser(String text) {
		text = text.replaceAll("[0-9]+ partnerships displayed.", "");
		text = text.replaceAll("1 partnership displayed.", "");
	}

	public Partnership[] parse(String text, boolean showUnrateds,
			boolean showGuests) {
		StringTokenizer tokens = new StringTokenizer(text, "^~:#.& ", true);
		ArrayList<Partnership> arr = new ArrayList<Partnership>();

		while (tokens.hasMoreTokens()) {
			Partnership p = new Partnership();
			Bugger[] buggers = new Bugger[2];
			buggers[0] = assist(tokens);
			tokens.nextToken(); // "/"
			buggers[1] = assist(tokens);

			arr.add(p);
		}
		Partnership[] out = arr.toArray(new Partnership[arr.size()]);
		LOGGER.info("usernames = " + java.util.Arrays.toString(out));
		return out;
	}

	private Bugger assist(StringTokenizer tokens) {
		Bugger p = new Bugger();
		String rating = tokens.nextToken();
		p.setRating(rating);
		String status = tokens.nextToken();
		if (status.length() > 1) { /* something wrong */
		}
		char modifier = status.charAt(0);
		p.setStatus(modifier);
		String username = tokens.nextToken();
		p.setUsername(username);
		return p;
	}

}
