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

import raptor.chat.BugGame;
import raptor.chat.Bugger;
import raptor.chat.Bugger.BuggerStatus;
import raptor.util.RaptorLogger;
import raptor.util.RaptorStringTokenizer;

/**
 * This code was adapted from some code johnthegreat for Raptor.
 */
public class BugWhoGParser {

	/**
	 * Bughouse games in progress 65 1613 crankinhaus 1692 RooRooBear [ Br 2 0]
	 * 1:12 - 1:35 (35-36) B: 19 280 1794 Tinker 1847 sadness [ Br 2 0] 1:13 -
	 * 1:32 (43-42) B: 18
	 * 
	 * 27 1986 Nathaniel 1964 PariahCare [ Br 3 0] 1:37 - 1:25 (31-41) B: 25 110
	 * 1514 HawaiianKin 1615 Poindexter [ Br 3 0] 1:35 - 1:19 (47-37) B: 30
	 * 
	 * 66 2130 gorbunaak 1713 FigureOfLi [ Br 2 0] 1:03 - 1:30 (28-33) W: 19 187
	 * 2029 nikechessni 1799 Jlexa [ Br 2 0] 1:17 - 1:19 (50-45) W: 18
	 * 
	 * 3 games displayed.
	 * 
	 * 2 partnerships displayed.
	 */

	private static final RaptorLogger LOG = RaptorLogger
			.getLog(BugWhoUParser.class);

	public static final String ID = "Bughouse games in progress\n";
	public static final String ID2 = "\nBughouse games in progress\n";

	public BugWhoGParser() {
	}

	public BugGame[] parse(String message) {
		try {
			if (message.startsWith(ID) && !message.contains(BugWhoPParser.ID)) {
				message = message.substring(ID.length(), message.length());
				message = message.replaceAll("[0-9]+ games displayed.", "");
				message = message.replaceAll("1 game displayed.", "");
				message = message.replaceAll("\nfics%", "");
				return process(message.trim());
			} else if (message.startsWith(ID2)
					&& !message.contains(BugWhoPParser.ID)) {
				message = message.replaceAll("[0-9]+ games displayed.", "");
				message = message.replaceAll("1 game displayed.", "");
				message = message.replaceAll("\nfics%", "");
				return process(message.trim());
			}
		} catch (Exception e) {
			// Just log it for now and eat it. Soft crash on these there are
			// subtle bugs in the message parsing.
			LOG.error("Unexpected error parsing BugWho G message\r" + message,
					e);
			return null;
		}
		return null;
	}

	private BugGame[] process(String text) {
		if (text.isEmpty()) {
			return new BugGame[0];
		}

		// Replace ---- with ```` so the tokens can be parsed correctly then
		// substitute it back after parsing.
		text = StringUtils.replace(text, "----", "````");

		RaptorStringTokenizer tok = new RaptorStringTokenizer(text, " \n[]-()",
				true);
		List<BugGame> result = new ArrayList<BugGame>(10);
		while (tok.hasMoreTokens()) {
			BugGame game = new BugGame();
			game.setGame1Id(tok.nextToken());
			game.setGame1White(new Bugger());
			game.getGame1White().setRating(tok.nextToken());
			if (game.getGame1White().getRating().equals("````")) {
				game.getGame1White().setRating("----");
			}
			game.getGame1White().setName(tok.nextToken());
			game.getGame1White().setStatus(BuggerStatus.Available);
			game.setGame1Black(new Bugger());
			game.getGame1Black().setRating(tok.nextToken());
			if (game.getGame1Black().getRating().equals("````")) {
				game.getGame1Black().setRating("----");
			}
			game.getGame1Black().setName(tok.nextToken());
			game.getGame1Black().setStatus(BuggerStatus.Available);
			game.setRated(tok.nextToken().indexOf('r') != -1);
			game.setTimeControl(tok.nextToken() + " " + tok.nextToken());
			for (int i = 0; i < 6; i++) {
				tok.nextToken();
			}
			game.setGame2Id(tok.nextToken());
			game.setGame2White(new Bugger());
			game.getGame2White().setRating(tok.nextToken());
			if (game.getGame2White().getRating().equals("````")) {
				game.getGame2White().setRating("----");
			}

			game.getGame2White().setName(tok.nextToken());
			game.getGame2White().setStatus(BuggerStatus.Available);
			game.setGame2Black(new Bugger());
			game.getGame2Black().setRating(tok.nextToken());
			if (game.getGame2Black().getRating().equals("````")) {
				game.getGame2Black().setRating("----");
			}
			game.getGame2Black().setName(tok.nextToken());
			game.getGame2Black().setStatus(BuggerStatus.Available);

			for (int i = 0; i < 9; i++) {
				tok.nextToken();
			}
			result.add(game);

		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Games = " + result);
		}
		return result.toArray(new BugGame[0]);
	}
}
