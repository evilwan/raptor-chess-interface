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
package raptor.connector.ics;

import raptor.connector.ics.game.message.GameEndMessage;
import raptor.util.RaptorStringTokenizer;

public class GameEndParser {

	public static final String EXCLUDE = "Creating";
	public static final String GAME_END = "{Game";

	public GameEndMessage parse(String message) {
		GameEndMessage result = null;
		if (message.startsWith(GAME_END) && !message.contains(EXCLUDE)) {
			result = new GameEndMessage();
			RaptorStringTokenizer tok = new RaptorStringTokenizer(message,
					" ()", true);

			// parse past {Game
			tok.nextToken();

			result.gameId = tok.nextToken();

			result.whiteName = tok.nextToken();

			// parse past vs.
			tok.nextToken();

			result.blackName = tok.nextToken();

			// find description. Its between ) and }
			int closingParenIndex = message.indexOf(")");
			int closingBraceIndex = message.indexOf("}");

			if (closingParenIndex == -1 || closingBraceIndex == -1) {
				throw new IllegalArgumentException(
						"Could not find description in gameEndEvent:" + message);
			}
			result.description = message.substring(closingParenIndex + 1,
					closingBraceIndex).trim();

			String afterClosingBrace = message.substring(closingBraceIndex + 1,
					message.length()).trim();

			if (result.description.indexOf("aborted") != -1) {
				result.type = GameEndMessage.ABORTED;
			} else if (result.description.indexOf("adjourned") != -1) {
				result.type = GameEndMessage.ADJOURNED;
			} else if (result.description.indexOf('*') != -1) {
				result.type = GameEndMessage.UNDETERMINED;
			} else if (afterClosingBrace.startsWith("0-1")) {
				result.type = GameEndMessage.BLACK_WON;
			} else if (afterClosingBrace.startsWith("1-0")) {
				result.type = GameEndMessage.WHITE_WON;
			} else if (afterClosingBrace.startsWith("1/2")) {
				result.type = GameEndMessage.DRAW;
			} else {
				result.type = GameEndMessage.UNDETERMINED;
			}
		}
		return result;
	}
}
