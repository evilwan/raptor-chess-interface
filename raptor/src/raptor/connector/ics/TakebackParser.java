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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.util.RaptorStringTokenizer;

/**
 * This parser is a bit unusual in that it maintains state as messages are
 * processed.
 * 
 * You can invoke getTakeBackMessage(gameId) to receive the last take back offer
 * requested and whether or not it was accepted.
 * 
 * It is important to clearTakebackMessages after there is no longer a need to
 * keep this state.
 */
public class TakebackParser {

	public static class TakebackMessage {
		public String gameId = "";
		public int halfMovesRequested = -1;
		public boolean wasAccepted;
	}

	public static final String ACCEPTED_TAKE_BACK = "accepts the takeback request";

	public static final String YOU_ACCEPTED_TAKE_BACK = "You accept the takeback request from";

	public static final String IDENTIFIER = "Game";
	private static final Log LOG = LogFactory.getLog(TakebackParser.class);

	public static final String REQUEST_TAKE_BACK = "would like to take back";

	protected Map<String, TakebackMessage> gameToTakebackMessages = new HashMap<String, TakebackMessage>();

	/**
	 * Clears the take back message for the specified gameId.
	 */
	public void clearTakebackMessages(String gameId) {
		gameToTakebackMessages.remove(gameId);
	}

	/**
	 * Returns the take back message for the specified game id. Returns null if
	 * there are none.
	 */
	public TakebackMessage getTakebackMessage(String gameId) {
		return gameToTakebackMessages.get(gameId);
	}

	/**
	 * Returns true if line is a take back accepted message.
	 */
	public boolean parse(String line) {
		if (line.startsWith(IDENTIFIER) && line.contains(REQUEST_TAKE_BACK)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing takeback offer.");
			}
			// Game 117: raptorb requests to take back 1 half move(s).
			TakebackMessage message = new TakebackMessage();
			RaptorStringTokenizer tok = new RaptorStringTokenizer(line, " ",
					true);

			tok.nextToken();

			message.gameId = tok.nextToken();
			message.gameId = message.gameId.substring(0, message.gameId
					.length() - 1);

			// parse past HANDLE requests to take back
			tok.nextToken();
			tok.nextToken();
			tok.nextToken();
			tok.nextToken();
			tok.nextToken();
			message.halfMovesRequested = Integer.parseInt(tok.nextToken());

			gameToTakebackMessages.put(message.gameId, message);
			System.err.println("Taleback offered.");
			return false;
		} else if (line.startsWith(IDENTIFIER)
				&& line.contains(ACCEPTED_TAKE_BACK)
				|| line.startsWith(YOU_ACCEPTED_TAKE_BACK)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing accepted takeback.");
			}

			// Game 117: raptora accepts the takeback request.
			RaptorStringTokenizer tok = new RaptorStringTokenizer(line, " ",
					true);
			tok.nextToken();
			String gameId = tok.nextToken();
			gameId = gameId.substring(0, gameId.length() - 1);

			TakebackMessage message = getTakebackMessage(gameId);
			if (message != null) {
				message.wasAccepted = true;
			} else {
				LOG
						.debug("Received a takback accepted for a takeback message that was never received.");
				// Leave halfMoveRequested at -1. This way code which uses the
				// parser can determine this state and react appropriately.
				message = new TakebackMessage();
				message.wasAccepted = true;
				message.gameId = gameId;
				gameToTakebackMessages.put(message.gameId, message);
			}
			System.err.println("Taleback accepted.");
			return true;
		}
		return false;
	}

}
