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
package raptor.connector.ics.chat;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.ics.IcsUtils;
import raptor.util.RaptorStringTokenizer;

public class PartnershipCreatedEventParser extends ChatEventParser {
	private static final String IDENTIFIER = "You agree to be";

	private static final String IDENTIFIER_2 = "agrees to be your partner.";

	public PartnershipCreatedEventParser() {
	}

	@Override
	public ChatEvent parse(String text) {
		if (text.length() < 100) {
			int i = text.indexOf(IDENTIFIER);
			if (i != -1) {
				RaptorStringTokenizer stringtokenizer = new RaptorStringTokenizer(
						text.substring(i + "You agree to be".length(), text
								.length()), " '");
				return new ChatEvent(IcsUtils.removeTitles(stringtokenizer
						.nextToken()), ChatType.PARTNERSHIP_CREATED, text);
			}
			int j = text.indexOf(IDENTIFIER_2);
			if (j != -1) {
				RaptorStringTokenizer stringtokenizer1 = new RaptorStringTokenizer(
						text, " ");
				String s1 = stringtokenizer1.nextToken();
				String s2 = stringtokenizer1.nextToken();
				if (s2.equals("agrees")) {
					return new ChatEvent(IcsUtils.removeTitles(s1),
							ChatType.PARTNERSHIP_CREATED, text);
				}
			}
			return null;
		}
		return null;

	}
}