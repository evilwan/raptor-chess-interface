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
package raptor.connector.ics.bughouse;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.bughouse.Bugger;
import raptor.bughouse.Partnership;
import raptor.bughouse.Bugger.BuggerStatus;
import raptor.connector.ics.IcsUtils;
import raptor.util.RaptorStringTokenizer;

/*
 bugwho p
 Partnerships not playing bughouse
 1944  RooRooBear / 1664  oub
 1 partnership displayed.
 */

public class BugWhoPParser {
	private static final Log LOG = LogFactory.getLog(BugWhoUParser.class);

	public static final String ID = "Partnerships not playing bughouse\n";
	public static final String ID2 = "\nPartnerships not playing bughouse\n";

	public BugWhoPParser() {

	}

	public Partnership[] parse(String message) {
		if (message.startsWith(ID)) {
			message = message.substring(ID.length(), message.length());
			message = message.replaceAll("[0-9]+ partnerships displayed.", "");
			message = message.replaceAll("1 partnership displayed.", "");
			message = message.replaceAll("\nfics%", "");
			return process(message.trim());
		} else if (message.startsWith(ID2)) {
			message = message.replaceAll("[0-9]+ partnerships displayed.", "");
			message = message.replaceAll("1 partnership displayed.", "");
			message = message.replaceAll("\nfics%", "");
			return process(message.trim());
		}
		return null;
	}

	private Bugger parseBugger(RaptorStringTokenizer tok) {
		Bugger bugger = new Bugger();
		String rating = tok.nextToken();
		int specialCharIndex = StringUtils.indexOfAny(rating, new char[] { '^',
				'~', ':', '#', '.', '&' });
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
			specialCharIndex = StringUtils.indexOfAny(name, new char[] { '^',
					'~', ':', '#', '.', '&' });
			if (specialCharIndex != -1) {
				bugger.setStatus(IcsUtils
						.getBuggserStatus(name.substring(0, 1)));
				bugger.setName(IcsUtils.stripTitles(name.substring(1, name
						.length())));
			} else {
				bugger.setStatus(BuggerStatus.Available);
				bugger.setName(IcsUtils.stripTitles(name));

			}
		}
		return bugger;
	}

	private Partnership[] process(String text) {
		if (text.equals("")) {
			return new Partnership[0];
		}
		RaptorStringTokenizer tok = new RaptorStringTokenizer(text, " /\n",
				true);
		ArrayList<Partnership> result = new ArrayList<Partnership>();

		while (tok.hasMoreTokens()) {
			Partnership partnership = new Partnership();
			partnership.setBugger1(parseBugger(tok));
			partnership.setBugger2(parseBugger(tok));
			result.add(partnership);
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("partnerships = " + result);
		}
		return result.toArray(new Partnership[0]);
	}

}
