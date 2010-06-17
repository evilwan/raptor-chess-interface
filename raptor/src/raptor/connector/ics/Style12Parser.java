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

import raptor.chess.GameConstants;
import raptor.connector.ics.game.message.Style12Message;
import raptor.util.RaptorLogger;
import raptor.util.RaptorStringTokenizer;

/**
 * <12>rnbqkbnr pppppppp -------- -------- ----P--- -------- PPPP-PPP RNBQKBNR B
 * 4 1 1 1 1 0 100 guestBLARG guestcday 1 10 0 39 39 600 600 1 P/e2-e4 (0:00) e4
 * 1 0 0
 * 
 * style12
 * 
 * Style 12 is a type of machine parseable output that many of the FICS
 * interfaces use. The output is documented here for those who wish to write new
 * interfaces. Style 12 is also fully compatible with ICC (The Internet Chess
 * Club).
 * 
 * The data is all on one line (displayed here as two lines, so it will show on
 * your screen). Here is an example: [Note: the beginning and ending quotation
 * marks are *not* part of the data string; they are needed in this help file
 * because some interfaces cannot display the string when in a text file.] "
 * <12>rnbqkb-r pppppppp -----n-- -------- ----P--- -------- PPPPKPPP RNBQ-BNR B
 * -1 0 0 1 1 0 7 Newton Einstein 1 2 12 39 39 119 122 2 K/e1-e2 (0:06) Ke2 0"
 * 
 * This string always begins on a new line, and there are always exactly 31 non-
 * empty fields separated by blanks. The fields are:
 * 
 * <pre>
 * the string &quot; &lt;12&gt;&quot; to identify this line.
 * 
 * eight fields representing the board position. The first one is White's 8th
 * rank (also Black's 1st rank), then White's 7th rank (also Black's 2nd), etc,
 * regardless of who's move it is.
 * 
 * color whose turn it is to move (&quot;B&quot; or &quot;W&quot;)
 * 
 * -1 if the previous move was NOT a double pawn push, otherwise the chess board
 * file (numbered 0--7 for a--h) in which the double push was made
 * 
 * can White still castle short? (0=no, 1=yes) 
 * can White still castle long? 
 * can Black still castle short? 
 * can Black still castle long?
 * 
 * the number of moves made since the last irreversible move. (0 if last move
 * was irreversible. If the value is &gt;= 100, the game can be declared a draw due
 * to the 50 move rule.)
 * 
 * The game number
 * 
 * White's name 
 * Black's name
 * 
 * my relation to this game: -3 isolated position, such as for &quot;ref 3&quot; or the
 * &quot;sposition&quot; command -2 I am observing game being examined 2 I am the examiner
 * of this game -1 I am playing, it is my opponent's move 1 I am playing and it
 * is my move 0 I am observing a game being played
 * 
 * initial time (in seconds) of the match increment (In seconds) of the match
 * 
 * White material strength Black material strength
 * 
 * White's remaining time Black's remaining time
 * 
 * the number of the move about to be made (standard chess numbering -- White's
 * and Black's first moves are both 1, etc.)
 * 
 * verbose coordinate notation for the previous move (&quot;none&quot; if there were none)
 * [note this used to be broken for examined games]
 * 
 * time taken to make previous move &quot;(min:sec)&quot;.
 * 
 * pretty notation for the previous move (&quot;none&quot; if there is none)
 * 
 * flip field for board orientation: 1 = Black at bottom, 0 = White at bottom.
 * 
 * 1 If clock is ticking. 0 if it is not. The amount of lag that occured last
 * move in milliseconds. 0 if none.
 * 
 * </pre>
 * 
 * In the future, new fields may be added to the end of the data string, so
 * programs should parse from left to right.
 * 
 */
public class Style12Parser implements GameConstants {
	@SuppressWarnings("unused")
	private static final RaptorLogger LOG = RaptorLogger.getLog(Style12Parser.class);
	public static final String STYLE_12 = "<12>";

	/**
	 * Parses a string in (0:00.000) format into a long.
	 */
	public static long timeTakenStringToInt(String timeTakenString) {
		RaptorStringTokenizer tok = new RaptorStringTokenizer(timeTakenString,
				":().", true);
		int minutes = Integer.parseInt(tok.nextToken());
		int seconds = Integer.parseInt(tok.nextToken());
		int millis = Integer.parseInt(tok.nextToken());

		return (minutes * 60 + seconds) * 1000 + millis;
	}

	// protected boolean isBicsStyle = false;

	/**
	 * BICS always sends the positions in the style 12 with white on the bottom
	 * even if it says it does'nt. If is white on bottom is set on fics then the
	 * white pieces are on bottom, otherwise its flipped.
	 * 
	 * @param isBicsStyle
	 */
	public Style12Parser()// boolean isBicsStyle)
	{
		// this.isBicsStyle = isBicsStyle;
	}

	public Style12Message parse(String message) {
		Style12Message result = null;
		if (message.startsWith(STYLE_12)) {
			result = new Style12Message();
			RaptorStringTokenizer tok = new RaptorStringTokenizer(message,
					" <>\n", true);

			// parse past <12>.
			tok.nextToken();

			StringBuilder positionString = new StringBuilder(64);
			for (int i = 0; i < 8; i++) {
				positionString.append(tok.nextToken());
			}

			result.isWhitesMoveAfterMoveIsMade = tok.nextToken().equals("W");

			result.doublePawnPushFile = Integer.parseInt(tok.nextToken());

			result.canWhiteCastleKSide = tok.nextToken().equals("1");
			result.canWhiteCastleQSide = tok.nextToken().equals("1");
			result.canBlackCastleKSide = tok.nextToken().equals("1");
			result.canBlackCastleQSide = tok.nextToken().equals("1");

			result.numberOfMovesSinceLastIrreversible = Integer.parseInt(tok
					.nextToken());

			result.gameId = tok.nextToken();

			result.whiteName = tok.nextToken();
			result.blackName = tok.nextToken();

			result.relation = Integer.parseInt(tok.nextToken());

			result.initialTimeMillis = Integer.parseInt(tok.nextToken()) * 1000 * 60;
			result.initialIncMillis = Integer.parseInt(tok.nextToken()) * 1000 * 60;

			result.whiteStrength = Integer.parseInt(tok.nextToken());
			result.blackStrength = Integer.parseInt(tok.nextToken());

			result.whiteRemainingTimeMillis = Long.parseLong(tok.nextToken());
			result.blackRemainingTimeMillis = Long.parseLong(tok.nextToken());

			result.fullMoveNumber = Integer.parseInt(tok.nextToken());

			result.lan = tok.nextToken();

			result.timeTakenForLastMoveMillis = timeTakenStringToInt(tok
					.nextToken());

			result.san = tok.nextToken();

			result.isWhiteOnTop = tok.nextToken().equals("1");

			result.position = parsePosition(positionString, result.isWhiteOnTop);

			result.isClockTicking = tok.nextToken().equals("1");

			result.lagInMillis = Integer.parseInt(tok.nextToken());
		}
		return result;
	}

	/**
	 * Parses a style 12 position string without the spaces between the ranks.
	 * 
	 * <pre>
	 * [0][0] is a1 [0][7] is h1
	 * [1][1] is a2 [1][1] is b2
	 * ...
	 * [7][0] is a8 [7][7] is h8
	 * </pre>
	 */
	public int[][] parsePosition(StringBuilder positionString,
			boolean isWhiteOnTop) {

		int[][] result = new int[8][];
		int positionCounter = 0;

		// if (isWhiteOnTop) {
		// positionString = positionString.reverse();
		// }

		for (int i = 7; i >= 0; i--) {
			result[i] = new int[8];

			for (int j = 0; j < 8; j++) {
				switch (positionString.charAt(positionCounter++)) {
				case '-':
					result[i][j] = EMPTY;
					break;
				case 'p':
					result[i][j] = BP;
					break;
				case 'n':
					result[i][j] = BN;
					break;
				case 'b':
					result[i][j] = BB;
					break;
				case 'r':
					result[i][j] = BR;
					break;
				case 'q':
					result[i][j] = BQ;
					break;
				case 'k':
					result[i][j] = BK;
					break;
				case 'P':
					result[i][j] = WP;
					break;
				case 'N':
					result[i][j] = WN;
					break;
				case 'B':
					result[i][j] = WB;
					break;
				case 'R':
					result[i][j] = WR;
					break;
				case 'Q':
					result[i][j] = WQ;
					break;
				case 'K':
					result[i][j] = WK;
					break;
				default: {
					throw new IllegalArgumentException(
							"Invalid piece encountered. '"
									+ positionString
											.charAt(positionCounter - 1) + "' "
									+ positionCounter + " " + positionString);
				}
				}
			}
		}
		return result;
	}
}
