/**
 *   Decaf/Decaffeinate ICS server interface
 *   Copyright (C) 2008  Carson Day (carsonday@gmail.com)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package raptor.connector.fics.parser;


public class GameEndParser {
//	private int icsId;
//
//	public GameEndParser(int icsId) {
//		this.icsId = icsId;
//	}
//
//	public GameEndEvent parse(String gameEndLine) {
//		StringTokenizer tok = new StringTokenizer(gameEndLine, " ()");
//
//		// parse past {Game
//		tok.nextToken();
//
//		int gameId = Integer.parseInt(tok.nextToken());
//
//		String whiteName = tok.nextToken();
//
//		// parse past vs.
//		tok.nextToken();
//
//		String blackName = tok.nextToken();
//
//		// find description. Its between ) and }
//		int closingParenIndex = gameEndLine.indexOf(")");
//		int closingBraceIndex = gameEndLine.indexOf("}");
//
//		if (closingParenIndex == -1 || closingBraceIndex == -1) {
//			throw new IllegalArgumentException(
//					"Could not find description in gameEndEvent:" + gameEndLine);
//		}
//		String description = gameEndLine.substring(closingParenIndex + 1,
//				closingBraceIndex).trim();
//
//		String afterClosingBrace = gameEndLine.substring(closingBraceIndex + 1,
//				gameEndLine.length()).trim();
//
//		int score = -1;
//
//		if (description.indexOf("aborted") != -1)
//			score = GameEndEvent.ABORTED;
//		else if (description.indexOf("adjourned") != -1)
//			score = GameEndEvent.ADJOURNED;
//		else if (description.indexOf('*') != -1)
//			score = GameEndEvent.UNDETERMINED;
//		else if (afterClosingBrace.startsWith("0-1"))
//			score = GameEndEvent.BLACK_WON;
//		else if (afterClosingBrace.startsWith("1-0"))
//			score = GameEndEvent.WHITE_WON;
//		else
//			score = GameEndEvent.DRAW;
//
//		return new GameEndEvent(icsId, gameId, whiteName, blackName,
//				description, score);
//
//	}
}
