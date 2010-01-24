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
package raptor.alias;

import org.apache.commons.lang.StringUtils;

import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class AbbreviatedChannelTellAlias extends RaptorAlias {
	public AbbreviatedChannelTellAlias() {
		super(
				"###Message",
				"Expands '###Message' into 'tell ### msg'",
				"'###message' where ### is a number between 0 and 255. "
						+ "Example: '36Why am I here?' will expand out into 'tell 37 Why am I here?'.");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (StringUtils.isBlank(command) || command.equals("0-0")) {
			return null;
		}
		RaptorStringTokenizer tok = new RaptorStringTokenizer(command, " ",
				true);
		String firstWord = tok.nextToken();
		String channel = "";

		int firstNonDigitIndex = -1;
		for (int i = 0; i < firstWord.length(); i++) {
			if (Character.isDigit(firstWord.charAt(i))) {
				channel += firstWord.charAt(i);
			} else {
				firstNonDigitIndex = i;
				break;
			}
		}

		if (firstNonDigitIndex > 0) {
			try {
				int channelNumber = Integer.parseInt(channel);
				if (channelNumber >= 0 && channelNumber <= 255) {
					return new RaptorAliasResult("tell " + channel + " "
							+ firstWord.substring(firstNonDigitIndex) + " "
							+ tok.getWhatsLeft(), null);
				}
			} catch (Throwable t) {
				return null;
			}
		}
		return null;
	}

}