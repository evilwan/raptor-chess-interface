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
package raptor.connector.ics;

import java.util.ArrayList;
import java.util.List;

import raptor.service.GameService.GameInfo;
import raptor.service.GameService.GameInfo.GameInfoCategory;
import raptor.util.RaptorLogger;
import raptor.util.RaptorStringTokenizer;

public class GameInfoParser {
	private static final RaptorLogger LOG = RaptorLogger.getLog(GameInfoParser.class);
	private static final String END_MESSAGE = "games displayed.";

	public GameInfoParser() {
	}

	public GameInfo[] parse(String message) {
		if (message.endsWith(END_MESSAGE)) {
			String trimmed = message.trim();
			int firstSpaceIndex = trimmed.indexOf(' ');
			if (firstSpaceIndex == -1) {	// Note: test unnecessary because END_MESSAGE contains a space
				return null;
			}
			List<GameInfo> infos = new ArrayList<GameInfo>(400);

			RaptorStringTokenizer lineTok = new RaptorStringTokenizer(trimmed, "\n", true);

			while (lineTok.hasMoreTokens()) {
				String line = lineTok.nextToken();
				if (LOG.isDebugEnabled()) {
				    LOG.debug("~~~ parsing game line: \"" + line + "\"");
				}
				try {
				    GameInfo info = parseGameLine(line);
				    //
				    // Errors during parsing are reported as null: best to silently ignore
				    // those lines for now?
				    //
				    if(info != null) {
					infos.add(info);
					if (LOG.isDebugEnabled()) {
					    LOG.debug("~~~ parsed game line: \"" + info + "\"");
					}
				    }
				} catch(EndOfGameListException ex) {
				    //
				    // Last line in game list seen
				    //
				    break;
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
    /**
     * Parse one single line in the "games" command output.
     * @param line is the non-trimmed game description line to parse
     * @return <code>GameInfo</code> for game described in line, or <code>null</code> in case of
     * an empty or Setup input line
     * @throws EndOfGameListException if reached end of game list
     */
    private GameInfo parseGameLine(String line) throws EndOfGameListException {
	//
	// Optimization: retrieve input length only once. Also ignore null inputs: would be serious
	// application error, so let the thing crash and log if it happens
	//
	int len = line.length();
	//
	// Quick checks
	//
	if(len == 0) {
	    return null;
	}
	if(line.endsWith(END_MESSAGE)) {
	    throw new EndOfGameListException(END_MESSAGE);
	}
	//
	// Ok, got a "regular" game line: could be one of three formats:
	//
	// (Setup...
	// (Exam. ...
	// 123 2200 Whiteplayer...
	//
	// For some reason the "Setup" lines are ignored by Raptor.
	//
	//
	// Skip leading whitespace
	//
	int i = 0;
	while((i < len) && (Character.isWhitespace(line.charAt(i)))) {
	    ++i;
	}
	if(i >= len) {
	    //
	    // All whitespace
	    //
	    return null;
	}
	//
	// By now 'i' points to first non-space character in line
	//
	StringBuilder sb = new StringBuilder();
	int j = i;
	while((j < len) && Character.isDigit(line.charAt(j))) {
	    sb.append(line.charAt(j));
	    ++j;
	}
	if(j >= len) {
	    return null;
	}
	if(i == j) {
	    //
	    // Parse error but probably best to silently skip and move on to
	    // next game in list?
	    //
	    return null;
	}
	GameInfo info = new GameInfo();
	info.setId(sb.toString());
	sb.delete(0, sb.length());
	i = j;
	while((i < len) && Character.isWhitespace(line.charAt(i))) {
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	if(line.charAt(i) == '(') {
	    //
	    // Can be "Exam." or "Setup"
	    //
	    return parseExamGame(line, len, ++i, info, sb);
	} else {
	    //
	    // Should be regular game
	    //
	    return parseRegularGame(line, len, i, info, sb);
	}
    }

    //
    // Sample lines to be parsed by this method:
    //
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
    // 10 (Exam. 2085 FDeAndresGo 2337 FMKarstenL) [ su120 0] W: 15
    // 420 (Exam. 2065 Stockfish(C 2065 NakaH_Rybk) [ uu  0   0] W: 56
    //
    // Note that the game index is already parsed when we enter the method below: 'i' points to
    // 'Exam.'
    //
    // Notice that last line: contains first part of '(C)' in the name.
    //
    private GameInfo parseExamGame(String line, int len, int i, GameInfo info, StringBuilder sb) {
	//
	// Only interested in "Exam." games for now
	//
	while((i < len) && Character.isWhitespace(line.charAt(i))) {
	    ++i;
	}
	if(i + 5 >= len) {	// take care of length of "Exam."
	    return null;
	}
	if(!line.substring(i, i + 5).equals("Exam.")) {
	    return null;
	}
	i += 5;
	while((i < len) && Character.isWhitespace(line.charAt(i))) {
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	//
	// Rating player white
	//
	while((i < len) && !Character.isWhitespace(line.charAt(i))) {
	    sb.append(line.charAt(i));
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	info.setWhiteElo(sb.toString());
	sb.delete(0, sb.length());
	//
	// Skip whitespace
	//
	while((i < len) && Character.isWhitespace(line.charAt(i))) {
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	//
	// Name player white
	//
	while((i < len) && !Character.isWhitespace(line.charAt(i))) {
	    sb.append(line.charAt(i));
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	info.setWhiteName(sb.toString());
	sb.delete(0, sb.length());
	//
	// Skip whitespace
	//
	while((i < len) && Character.isWhitespace(line.charAt(i))) {
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	//
	// Rating player black
	//
	while((i < len) && !Character.isWhitespace(line.charAt(i))) {
	    sb.append(line.charAt(i));
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	info.setBlackElo(sb.toString());
	sb.delete(0, sb.length());
	//
	// Skip whitespace
	//
	while((i < len) && Character.isWhitespace(line.charAt(i))) {
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	//
	// Name player black
	//
	while((i < len) && !Character.isWhitespace(line.charAt(i))) {
	    sb.append(line.charAt(i));
	    ++i;
	}
	//
	// Special case if name ends with ')': means full length for name black was filled up
	// so that there was no space between the name and the closing ')'
	//
	if(line.charAt(i - 1) == ')') {
	    info.setBlackName(sb.substring(0, sb.length() - 1));
	    --i;
	} else {
	    info.setBlackName(sb.toString());
	}
	sb.delete(0, sb.length());
	if(i >= len) {
	    return null;
	}
	//
	// Advance until closing ')' found
	//
	while((i < len) && (line.charAt(i) != ')')) {
	    ++i;
	}
	//
	// Keep track that this one is being examined
	//
	info.setBeingExamined(true);
	//
	// Need to start with whitespace behind ')', so increment 'i'
	//
	i = parseFlagsAndTimes(line, len, ++i, info, sb);
	if(i < 0) {
	    return null;
	}
	//
	// Check which side to move
	//
	info.setWhitesMove(line.charAt(i++) == 'W');
	//
	// Skip whitespace
	//
	while((i < len) && (Character.isWhitespace(line.charAt(i)) || (line.charAt(i) == ':'))) {
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	//
	// Move number
	//
	while((i < len) && Character.isDigit(line.charAt(i))) {
	    sb.append(line.charAt(i));
	    ++i;
	}
	info.setMoveNumber(Integer.parseInt(sb.toString()));
	return info;
    }

    //
    // Sample lines to be parsed by this method:
    //
    // 158 2007 ventroy 1967 chelou [ lr 1 0] 0:42 - 0:41 (17-20) W: 20
    // 26 2092 GriffySr 1884 Benyz [ sr 15 0] 14:56 - 6:50 ( 4- 7) B: 38
    // 50 2117 Rookie 1861 wirbel [ br 5 3] 1:21 - 0:28 (16-16) B: 43
    // 309 2173 drinkeh 1825 CapitanSgr [ sr 23 18] 15:20 - 20:15 (35-35) B: 16
    // 313 1982 FungoPhil 2019 rasty [ br 3 0] 1:32 - 1:03 (34-32) W: 25
    // 294 2032 douthy 1988 MStraus [ Sr 1 0] 0:11 - 0:05 ( 6- 4) W: 30
    // 3 2082 harlestonch 2274 Plnik [ sr 15 0] 1:46 - 3:49 (28-26) B: 39
    // 183 2270 MichaelDeVe 2548 GMUlibin [ su120 0] 1:40:00 -1:18:00 (36-36) W: 12
    // 119 2449 IMRomanko 2390 IMZaiatz [ su120 0] 2:00:00 -2:00:00 (39-39) B: 1
    //
    // Note that the game index is already parsed when we enter the method below: 'i' points to
    // rating of white
    //
    private GameInfo parseRegularGame(String line, int len, int i, GameInfo info, StringBuilder sb) {
	//
	// Rating player white
	//
	while((i < len) && !Character.isWhitespace(line.charAt(i))) {
	    sb.append(line.charAt(i));
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	info.setWhiteElo(sb.toString());
	sb.delete(0, sb.length());
	//
	// Skip whitespace
	//
	while((i < len) && Character.isWhitespace(line.charAt(i))) {
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	//
	// Name player white
	//
	while((i < len) && !Character.isWhitespace(line.charAt(i))) {
	    sb.append(line.charAt(i));
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	info.setWhiteName(sb.toString());
	sb.delete(0, sb.length());
	//
	// Skip whitespace
	//
	while((i < len) && Character.isWhitespace(line.charAt(i))) {
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	//
	// Rating player black
	//
	while((i < len) && !Character.isWhitespace(line.charAt(i))) {
	    sb.append(line.charAt(i));
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	info.setBlackElo(sb.toString());
	sb.delete(0, sb.length());
	//
	// Skip whitespace
	//
	while((i < len) && Character.isWhitespace(line.charAt(i))) {
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	//
	// Name player black
	//
	while((i < len) && !Character.isWhitespace(line.charAt(i))) {
	    sb.append(line.charAt(i));
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	info.setBlackName(sb.toString());
	sb.delete(0, sb.length());
	//
	// Keep track that this one is not being examined
	//
	info.setBeingExamined(false);
	i = parseFlagsAndTimes(line, len, i, info, sb);
	if(i < 0) {
	    return null;
	}
	//
	// Skip everything until we find a 'W' or a 'B'
	//
	while((i < len) && (line.charAt(i) != 'W') && (line.charAt(i) != 'B')) {
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	//
	// Check which side to move
	//
	info.setWhitesMove(line.charAt(i++) == 'W');
	//
	// Skip whitespace
	//
	while((i < len) && (Character.isWhitespace(line.charAt(i)) || (line.charAt(i) == ':'))) {
	    ++i;
	}
	if(i >= len) {
	    return null;
	}
	//
	// Move number
	//
	while((i < len) && Character.isDigit(line.charAt(i))) {
	    sb.append(line.charAt(i));
	    ++i;
	}
	info.setMoveNumber(Integer.parseInt(sb.toString()));
	return info;
    }

    //
    // Parse part between '[...]'
    //
    private int parseFlagsAndTimes(String line, int len, int i, GameInfo info, StringBuilder sb) {
	//
	// Skip whitespace
	//
	while((i < len) && Character.isWhitespace(line.charAt(i))) {
	    ++i;
	}
	if(i >= len) {
	    return -1;
	}
	//
	// We are expecting a '['
	//
	if(line.charAt(i) != '[') {
	    return -1;
	}
	++i;
	//
	// Skip whitespace
	//
	while((i < len) && Character.isWhitespace(line.charAt(i))) {
	    ++i;
	}
	if(i >= len) {
	    return -1;
	}
	//
	// Get flags
	//
	while((i < len) && !Character.isWhitespace(line.charAt(i))&& !Character.isDigit(line.charAt(i))) {
	    sb.append(line.charAt(i));
	    ++i;
	}
	if(i >= len) {
	    return -1;
	}
	parseFlags(sb.toString(), info);
	sb.delete(0, sb.length());
	//
	// Skip whitespace
	//
	while((i < len) && Character.isWhitespace(line.charAt(i))) {
	    ++i;
	}
	if(i >= len) {
	    return -1;
	}
	//
	// Get time
	//
	while((i < len) && Character.isDigit(line.charAt(i))) {
	    sb.append(line.charAt(i));
	    ++i;
	}
	if(i >= len) {
	    return -1;
	}
	info.setTime(Integer.parseInt(sb.toString()));
	sb.delete(0, sb.length());
	//
	// Skip whitespace
	//
	while((i < len) && Character.isWhitespace(line.charAt(i))) {
	    ++i;
	}
	if(i >= len) {
	    return -1;
	}
	//
	// Get increment
	//
	while((i < len) && Character.isDigit(line.charAt(i))) {
	    sb.append(line.charAt(i));
	    ++i;
	}
	if(i >= len) {
	    return -1;
	}
	info.setInc(Integer.parseInt(sb.toString()));
	sb.delete(0, sb.length());
	//
	// Skip whitespace
	//
	while((i < len) && (Character.isWhitespace(line.charAt(i)) || (line.charAt(i) == ']'))) {
	    ++i;
	}
	if(i >= len) {
	    return -1;
	}
	//
	// All OK for flags & time info parsing
	//
	return i;
    }

    private class EndOfGameListException extends Exception {
	public EndOfGameListException(String msg) {
	    super(msg);
	}
    }
}
