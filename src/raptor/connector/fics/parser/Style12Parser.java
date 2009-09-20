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

import java.util.StringTokenizer;

import org.apache.log4j.Logger;



public class Style12Parser {
//	private static final Logger LOGGER = Logger.getLogger(Style12Parser.class);
//
//	private static final String TRUE_FLAG = "1";
//
//	private static final String WHITE_ID = "W";
//
//	private static final String STYLE_12 = "<12>";
//
//	private static final int POSITION_START_INDEX = STYLE_12.length() + 1;
//
//	private static final int POSITION_END_INDEX = POSITION_START_INDEX + 64 + 7;
//
//	private B1Parser b1Parser;
//
//	private int icsId;
//
//	public Style12Parser(int icsId) {
//		this.icsId = icsId;
//		b1Parser = new B1Parser(icsId);
//	}
//
//	/**
//	 * <12>rnbqkbnr pppppppp -------- -------- ----P--- -------- PPPP-PPP
//	 * RNBQKBNR B 4 1 1 1 1 0 100 guestBLARG guestcday 1 10 0 39 39 600 600 1
//	 * P/e2-e4 (0:00) e4 1 0 0
//	 * 
//	 * style12
//	 * 
//	 * Style 12 is a type of machine parseable output that many of the FICS
//	 * interfaces use. The output is documented here for those who wish to write
//	 * new interfaces. Style 12 is also fully compatible with ICC (The Internet
//	 * Chess Club).
//	 * 
//	 * The data is all on one line (displayed here as two lines, so it will show
//	 * on your screen). Here is an example: [Note: the beginning and ending
//	 * quotation marks are *not* part of the data string; they are needed in
//	 * this help file because some interfaces cannot display the string when in
//	 * a text file.] " <12>rnbqkb-r pppppppp -----n-- -------- ----P--- --------
//	 * PPPPKPPP RNBQ-BNR B -1 0 0 1 1 0 7 Newton Einstein 1 2 12 39 39 119 122 2
//	 * K/e1-e2 (0:06) Ke2 0"
//	 * 
//	 * This string always begins on a new line, and there are always exactly 31
//	 * non- empty fields separated by blanks. The fields are:
//	 * 
//	 * the string " <12>" to identify this line. eight fields representing the
//	 * board position. The first one is White's 8th rank (also Black's 1st
//	 * rank), then White's 7th rank (also Black's 2nd), etc, regardless of who's
//	 * move it is. color whose turn it is to move ("B" or "W") -1 if the
//	 * previous move was NOT a double pawn push, otherwise the chess board file
//	 * (numbered 0--7 for a--h) in which the double push was made can White
//	 * still castle short? (0=no, 1=yes) can White still castle long? can Black
//	 * still castle short? can Black still castle long? the number of moves made
//	 * since the last irreversible move. (0 if last move was irreversible. If
//	 * the value is >= 100, the game can be declared a draw due to the 50 move
//	 * rule.) The game number White's name Black's name my relation to this
//	 * game: -3 isolated position, such as for "ref 3" or the "sposition"
//	 * command -2 I am observing game being examined 2 I am the examiner of this
//	 * game -1 I am playing, it is my opponent's move 1 I am playing and it is
//	 * my move 0 I am observing a game being played initial time (in seconds) of
//	 * the match increment In seconds) of the match White material strength
//	 * Black material strength White's remaining time Black's remaining time the
//	 * number of the move about to be made (standard chess numbering -- White's
//	 * and Black's first moves are both 1, etc.) verbose coordinate notation for
//	 * the previous move ("none" if there were none) [note this used to be
//	 * broken for examined games] time taken to make previous move "(min:sec)".
//	 * pretty notation for the previous move ("none" if there is none) flip
//	 * field for board orientation: 1 = Black at bottom, 0 = White at bottom. 1
//	 * If clock is ticking. 0 if it is not. The amount of lag that occured last
//	 * move in milliseconds. 0 if none.
//	 * 
//	 * In the future, new fields may be added to the end of the data string, so
//	 * programs should parse from left to right. Special information for
//	 * bughouse games --------------------------------------
//	 * 
//	 * When showing positions from bughouse games, a second line showing piece
//	 * holding is given, with " <b1>" at the beginning, for example:
//	 * 
//	 * <b1>game 6 white [PNBBB] black [PNB] Type [next] to see next page. fics%
//	 * JasonRD (++++) seeking 7 0 unrated blitz f ("play 59" to respond) fics%
//	 * next
//	 * 
//	 * Also, when pieces are "passed" during bughouse, a short data string --
//	 * not the entire board position -- is sent. For example:
//	 * 
//	 * <b1>game 52 white [NB] black [N] <- BN
//	 * 
//	 * The final two letters indicate the piece that was passed; in the above
//	 * example, a knight (N) was passed to Black.
//	 * 
//	 * A prompt may preceed the <b1>header.
//	 * 
//	 */
//
//	public MoveEvent parse(String style12Line) {
//		String positionString = style12Line.substring(POSITION_START_INDEX,
//				POSITION_END_INDEX);
//		int[][] positionArray = parsePosition(positionString);
//
//		String nonPositionString = style12Line
//				.substring(POSITION_END_INDEX + 1);
//
//		StringTokenizer tok = new StringTokenizer(nonPositionString, " \r\n");
//		boolean isWhitesMove = tok.nextToken().equals(WHITE_ID);
//
//		LOGGER.debug("Style12: isWhitesMove=" + isWhitesMove);
//		int doublePawnPushFile = Integer.parseInt(tok.nextToken());
//		boolean canWhiteCastleShort = tok.nextToken().equals(TRUE_FLAG);
//		boolean canWhiteCastleLong = tok.nextToken().equals(TRUE_FLAG);
//		boolean canBlackCastleShort = tok.nextToken().equals(TRUE_FLAG);
//		boolean canBlackCastleLong = tok.nextToken().equals(TRUE_FLAG);
//		int numberOfMovesSinceLastIrreversible = Integer.parseInt(tok
//				.nextToken());
//		int gameId = Integer.parseInt(tok.nextToken());
//		String whitesName = tok.nextToken();
//		String blacksName = tok.nextToken();
//		int relation = Integer.parseInt(tok.nextToken());
//		int initialTime = Integer.parseInt(tok.nextToken());
//		int initialInc = Integer.parseInt(tok.nextToken());
//		int whiteStrength = Integer.parseInt(tok.nextToken());
//		int blackStrength = Integer.parseInt(tok.nextToken());
//		long whiteRemainingTime = Long.parseLong(tok.nextToken());
//		long blackRemainingTime = Long.parseLong(tok.nextToken());
//		int standardChessMoveNumber = Integer.parseInt(tok.nextToken());
//		String verboseNotationString = tok.nextToken();
//		long timeTakenForLastMove = timeTakenStringToInt(tok.nextToken());
//		String prettyNotationString = tok.nextToken();
//		boolean isWhiteOnTop = !tok.nextToken().equals(TRUE_FLAG);
//		boolean isClockTicking = tok.nextToken().equals(TRUE_FLAG);
//		int lagInMillis = Integer.parseInt(tok.nextToken());
//		
//		
//		LOGGER.error("isWhiteOnTop = " + isWhiteOnTop);
//
//		Position position = new Position(positionArray, canWhiteCastleShort,
//				canWhiteCastleLong, canBlackCastleShort, canBlackCastleLong,
//				doublePawnPushFile, isWhitesMove);
//
//		return new MoveEvent(icsId, gameId, position,
//				numberOfMovesSinceLastIrreversible, whitesName, blacksName,
//				relation, initialTime, initialInc, whiteStrength,
//				blackStrength, whiteRemainingTime, blackRemainingTime,
//				standardChessMoveNumber, verboseNotationString,
//				timeTakenForLastMove, prettyNotationString, isWhiteOnTop,
//				isClockTicking, lagInMillis);
//	}
//
//	public MoveEvent parse(String style12Line, String b1Line) {
//		MoveEvent moveEvent = parse(style12Line);
//		moveEvent.setHoldingsChangedEvent(b1Parser.parse(b1Line));
//		return moveEvent;
//	}
//
//	/**
//	 * Parses a string in (0:00.000) format into an int.
//	 */
//	private static long timeTakenStringToInt(String timeTakenString) {
//		StringTokenizer tok = new StringTokenizer(timeTakenString, ":().");
//		int minutes = Integer.parseInt(tok.nextToken());
//		int seconds = Integer.parseInt(tok.nextToken());
//		int millis = Integer.parseInt(tok.nextToken());
//
//		return (minutes * 60 + seconds) * 1000 + millis;
//	}
//
//	/**
//	 * Parses a style 12 position string with the first character always a8
//	 * second a7.
//	 * 
//	 */
//	private static int[][] parsePosition(String positionString) {
//
//		int[][] result = new int[8][];
//		int positionCounter = 0;
//		for (int i = 0; i < 8; i++) {
//			result[i] = new int[8];
//
//			if (i != 0) {
//				// increment past the space.
//				positionCounter++;
//			}
//
//			for (int j = 0; j < 8; j++) {
//				switch (positionString.charAt(positionCounter++)) {
//				case '-': {
//					result[i][j] = Piece.EMPTY;
//					break;
//				}
//
//				case 'p': {
//					result[i][j] = Piece.BLACK_PAWN;
//					break;
//				}
//
//				case 'n': {
//					result[i][j] = Piece.BLACK_KNIGHT;
//					break;
//				}
//
//				case 'b': {
//					result[i][j] = Piece.BLACK_BISHOP;
//					break;
//				}
//
//				case 'r': {
//					result[i][j] = Piece.BLACK_ROOK;
//					break;
//				}
//
//				case 'q': {
//					result[i][j] = Piece.BLACK_QUEEN;
//					break;
//				}
//
//				case 'k': {
//					result[i][j] = Piece.BLACK_KING;
//					break;
//				}
//
//				case 'P': {
//					result[i][j] = Piece.WHITE_PAWN;
//					break;
//				}
//
//				case 'N': {
//					result[i][j] = Piece.WHITE_KNIGHT;
//					break;
//				}
//
//				case 'B': {
//					result[i][j] = Piece.WHITE_BISHOP;
//					break;
//				}
//
//				case 'R': {
//					result[i][j] = Piece.WHITE_ROOK;
//					break;
//				}
//
//				case 'Q': {
//					result[i][j] = Piece.WHITE_QUEEN;
//					break;
//				}
//
//				case 'K': {
//					result[i][j] = Piece.WHITE_KING;
//					break;
//				}
//
//				default: {
//					throw new IllegalArgumentException(
//							"Invalid piece encountered. '"
//									+ positionString.charAt(positionCounter)
//									+ "' " + positionCounter + " "
//									+ positionString);
//				}
//				}
//			}
//		}
//		return result;
//	}
}
