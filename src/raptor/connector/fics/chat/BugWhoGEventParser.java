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
package raptor.connector.fics.chat;

public class BugWhoGEventParser {

	// private static final Logger LOGGER = Logger
	// .getLogger(BugWhoGEventParser.class);

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

	// public BugWhoGEventParser(int icsId) {
	// super(icsId);
	// }
	//
	// @Override
	// public IcsNonGameEvent parse(String text) {
	// if (text.startsWith(GAMES_IN_PROGRESS)) {
	// StringTokenizer lines = new StringTokenizer(text, "\r\n");
	// String currentLine = lines.nextToken();
	// List<BugWhoGGame> games = new LinkedList<BugWhoGGame>();
	//							
	//
	// while (lines.hasMoreTokens()) {
	// currentLine = lines.nextToken();
	//
	// if (currentLine.endsWith(DISPLAYED))
	// {
	// break;
	// }
	// else if (currentLine.trim().equals(""))
	// {
	// continue;
	// }
	//
	// int spaceIndex = currentLine.indexOf(" ",1);
	// BugWhoGGame game = new BugWhoGGame();
	//				
	// if (spaceIndex == -1)
	// {
	// break;
	// }
	// else
	// {
	// try
	// {
	// game.setGame1Id(Integer.parseInt(currentLine.substring(0,spaceIndex).trim()));
	// game.setGame1Description(currentLine.substring(spaceIndex +
	// 1,currentLine.length()).trim());
	// currentLine = lines.nextToken();
	// spaceIndex = currentLine.indexOf(" ",1);
	// game.setGame2Id(Integer.parseInt(currentLine.substring(0,spaceIndex).trim()));
	// game.setGame2Description(currentLine.substring(spaceIndex +
	// 1,currentLine.length()).trim());
	// games.add(game);
	// }
	// catch (Exception e)
	// {
	// LOGGER.error("Unexpected error occured:",e);
	// break;
	// }
	// }
	// }
	//
	// return new BugWhoGEvent(getIcsId(), text, games);
	// } else {
	// return null;
	// }
	// }
	//
	// private static final String GAMES_IN_PROGRESS =
	// "Bughouse games in progress";
	//
	// private static final String DISPLAYED = "displayed.";
}
