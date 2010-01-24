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
package raptor.connector.ics;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.service.GameService.GameInfo;
import raptor.service.GameService.GameInfo.GameInfoCategory;
import raptor.util.RaptorStringTokenizer;

public class GameInfoParser {
	private static final Log LOG = LogFactory.getLog(GameInfoParser.class);
	private static final String END_MESSAGE = "games displayed.";

	public GameInfoParser() {
	}

	public GameInfo[] parse(String message) {
		if (message.endsWith(END_MESSAGE)) {
			String trimmed = message.trim();
			int firstSpaceIndex = trimmed.indexOf(" ");
			if (firstSpaceIndex == -1) {
				return null;
			}
			List<GameInfo> infos = new ArrayList<GameInfo>(400);

			RaptorStringTokenizer lineTok = new RaptorStringTokenizer(trimmed,
					"\n", true);

			while (lineTok.hasMoreTokens()) {
				String line = lineTok.nextToken();
				int gamesDisplayedIndex = line.indexOf("games displayed.");
				if (gamesDisplayedIndex != -1) {
					break;
				}
				int examIndex = line.indexOf("(Exam. ");
				int setupIndex = line.indexOf("(Setup ");
				if (examIndex != -1) {
					// 5 (Exam. 0 LectureBot 0 A.Zaitzev ) [ uu 0 0] W: 20
					// 12 (Exam. 0 endgamebot 0 endgamebot) [ uu 0 0] W: 4
					// 19 (Exam. 0 WesBot 0 WesBot ) [ uu 0 0] W: 1
					// 286 (Exam. 100 Whiteplayer 100 Blackplaye) [ uu 0 0] W: 1
					// 126 (Exam. 0 GuestZMQF 1019 eugeniodal) [ bu 10 0] W: 7
					// 52 (Exam. 0 winpower 1471 laterna ) [ br 5 0] W: 5
					// 57 (Exam. 1529 DimLantern 0 subsu ) [ bu 3 0] W: 7
					// 179 (Exam. 1199 RJJ 1147 Blogjam ) [ br 10 0] B: 2
					// 10 (Exam. 2581 GMAndersson 0 GMPeralta ) [ su120 0] W: 28
					// 103 (Exam. 1634 nohwell 1612 AsTal ) [ br 3 0] B: 22
					// 219 (Exam. 1743 SerDer 1875 ThickPuppy) [ Br 2 0] W: 20
					// 295 (Exam. 1813 IntRob 1813 IntRob ) [ uu 0 0] W: 4
					// 51 (Exam. 1945 bigdonkey 1786 Castlelate) [ br 3 0] W: 37
					// 10 (Exam. 2085 FDeAndresGo 2337 FMKarstenL) [ su120 0] W:
					// 15
					RaptorStringTokenizer examineTok = new RaptorStringTokenizer(
							line, " ()[]:", true);
					GameInfo info = new GameInfo();
					info.setId(examineTok.nextToken());
					examineTok.nextToken();
					info.setWhiteElo(examineTok.nextToken());
					info.setWhiteName(examineTok.nextToken());
					info.setBlackElo(examineTok.nextToken());
					info.setBlackName(examineTok.nextToken());
					info.setBeingExamined(true);
					String flags = examineTok.nextToken();
					if (flags.length() > 3) {
						int firstDigitIndex = -1;
						for (int i = 0; i < flags.length(); i++) {
							if (Character.isDigit(flags.charAt(i))) {
								firstDigitIndex = i;
								break;
							}
						}
						parseFlags(flags.substring(0, firstDigitIndex), info);
						info.setTime(Integer.parseInt(flags
								.substring(firstDigitIndex)));
					} else {
						parseFlags(flags, info);
						info.setTime(Integer.parseInt(examineTok.nextToken()));
					}
					info.setInc(Integer.parseInt(examineTok.nextToken()));
					info.setWhitesMove(examineTok.nextToken().equals("W"));
					info
							.setMoveNumber(Integer.parseInt(examineTok
									.nextToken()));
					infos.add(info);
				} else if (setupIndex == -1) {
					// 158 2007 ventroy 1967 chelou [ lr 1 0] 0:42 - 0:41
					// (17-20) W: 20
					// 26 2092 GriffySr 1884 Benyz [ sr 15 0] 14:56 - 6:50 ( 4-
					// 7) B: 38
					// 50 2117 Rookie 1861 wirbel [ br 5 3] 1:21 - 0:28 (16-16)
					// B: 43
					// 309 2173 drinkeh 1825 CapitanSgr [ sr 23 18] 15:20 -
					// 20:15 (35-35) B: 16
					// 313 1982 FungoPhil 2019 rasty [ br 3 0] 1:32 - 1:03
					// (34-32) W: 25
					// 294 2032 douthy 1988 MStraus [ Sr 1 0] 0:11 - 0:05 ( 6-
					// 4) W: 30
					// 3 2082 harlestonch 2274 Plnik [ sr 15 0] 1:46 - 3:49
					// (28-26) B: 39
					// 183 2270 MichaelDeVe 2548 GMUlibin [ su120 0] 1:40:00
					// -1:18:00 (36-36) W: 12
					// 119 2449 IMRomanko 2390 IMZaiatz [ su120 0] 2:00:00
					// -2:00:00 (39-39) B: 1

					int parenIndex = line.indexOf("(");
					int endParenIndex = line.indexOf(")");
					line = line.substring(0, parenIndex)
							+ line.substring(endParenIndex + 1, line.length());

					RaptorStringTokenizer gameTok = new RaptorStringTokenizer(
							line, " []", true);
					GameInfo info = new GameInfo();
					info.setId(gameTok.nextToken());
					info.setWhiteElo(gameTok.nextToken());
					info.setWhiteName(gameTok.nextToken());
					info.setBlackElo(gameTok.nextToken());
					info.setBlackName(gameTok.nextToken());
					info.setBeingExamined(false);
					String flags = gameTok.nextToken();
					if (flags.length() > 3) {
						int firstDigitIndex = -1;
						for (int i = 0; i < flags.length(); i++) {
							if (Character.isDigit(flags.charAt(i))) {
								firstDigitIndex = i;
								break;
							}
						}
						parseFlags(flags.substring(0, firstDigitIndex), info);
						info.setTime(Integer.parseInt(flags
								.substring(firstDigitIndex)));
					} else {
						parseFlags(flags, info);
						info.setTime(Integer.parseInt(gameTok.nextToken()));
					}
					info.setInc(Integer.parseInt(gameTok.nextToken()));
					gameTok.nextToken();
					String nextToken = gameTok.nextToken();
					if (nextToken.length() == 1) {
						gameTok.nextToken();
					}
					info.setWhitesMove(gameTok.nextToken().equals("W:"));
					info.setMoveNumber(Integer.parseInt(gameTok.nextToken()));
					infos.add(info);
				}
			}
			return infos.toArray(new GameInfo[0]);
		}
		return null;
	}

	protected void parseFlags(String flags, GameInfo info) {
		if (flags.startsWith("p")) {
			info.setPrivate(true);
			flags = flags.substring(1);
		}
		switch (flags.charAt(0)) {
		case 'b':
			info.setCategory(GameInfoCategory.blitz);
			break;
		case 'l':
			info.setCategory(GameInfoCategory.lightning);
			break;
		case 'u':
			info.setCategory(GameInfoCategory.untimed);
			break;
		case 'e':
			info.setCategory(GameInfoCategory.examined);
			break;
		case 's':
			info.setCategory(GameInfoCategory.standard);
			break;
		case 'w':
			info.setCategory(GameInfoCategory.wild);
			break;
		case 'x':
			info.setCategory(GameInfoCategory.atomic);
			break;
		case 'z':
			info.setCategory(GameInfoCategory.crazyhouse);
			break;
		case 'B':
			info.setCategory(GameInfoCategory.bughouse);
			break;
		case 'L':
			info.setCategory(GameInfoCategory.losers);
			break;
		case 'S':
			info.setCategory(GameInfoCategory.suicide);
			break;
		case 'n':
			info.setCategory(GameInfoCategory.nonstandard);
			break;
		default:
			info.setCategory(GameInfoCategory.nonstandard);
			LOG.warn("Encountered invalid category parsing games: "
					+ flags.charAt(0));
			break;
		}
		switch (flags.charAt(1)) {
		case 'r':
			info.setRated(true);
			break;
		case 'u':
			info.setRated(false);
			break;
		default:
			info.setRated(false);
			LOG.warn("Encountered invalid rated flag parsing games: "
					+ flags.charAt(1));
			break;
		}
	}
}
