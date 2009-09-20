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

public class B1Parser {
//	private int icsId;
//
//	public B1Parser(int icsId) {
//		this.icsId = icsId;
//	}
//
//	public HoldingsChangedEvent parse(String b1Line) {
//		StringTokenizer tok = new StringTokenizer(b1Line, " {}><-\r\n");
//		tok.nextToken();
//		tok.nextToken();
//		int j = Integer.parseInt(tok.nextToken());
//		tok.nextToken();
//		String s1 = tok.nextToken();
//		int ai[] = lightPiecesToIntArray(s1.substring(1, s1.length() - 1));
//		tok.nextToken();
//		String s2 = tok.nextToken();
//		int ai1[] = darkPiecesToIntArray(s2.substring(1, s2.length() - 1));
//		return new HoldingsChangedEvent(icsId, j, ai1, ai);
//	}
//
//	private static int[] lightPiecesToIntArray(String s) {
//		int ai[] = new int[s.length()];
//		for (int i = 0; i < s.length(); i++) {
//			int piece = -1;
//			switch (s.charAt(i)) {
//			case 'P': // 'P'
//			case 'p': // 'p'
//				piece = Piece.WP;
//				break;
//
//			case 'N': // 'N'
//			case 'n': // 'n'
//				piece = Piece.WN;
//				break;
//
//			case 'B': // 'B'
//			case 'b': // 'b'
//				piece = Piece.WB;
//				break;
//
//			case 'R': // 'R'
//			case 'r': // 'r'
//				piece = Piece.WR;
//				break;
//
//			case 'Q': // 'Q'
//			case 'q': // 'q'
//				piece = Piece.WQ;
//				break;
//
//			default:
//				throw new RuntimeException("Invalid piece " + s.charAt(i));
//			}
//			ai[i] = piece;
//		}
//
//		return ai;
//	}
//
//	private static int[] darkPiecesToIntArray(String s) {
//		int ai[] = new int[s.length()];
//		for (int i = 0; i < s.length(); i++) {
//			int piece = 0;
//			switch (s.charAt(i)) {
//			case 'P': // 'P'
//			case 'p': // 'p'
//				piece = Piece.BP;
//				break;
//
//			case 'N': // 'N'
//			case 'n': // 'n'
//				piece = Piece.BN;
//				break;
//
//			case 'B': // 'B'
//			case 'b': // 'b'
//				piece = Piece.BB;
//				break;
//
//			case 'R': // 'R'
//			case 'r': // 'R'
//				piece = Piece.BR;
//				break;
//			case 'Q': // 'Q'
//			case 'q': // 'q'
//				piece = Piece.BQ;
//				break;
//
//			default:
//				throw new RuntimeException("Invalid piece " + s.charAt(i));
//			}
//			ai[i] = piece;
//		}
//
//		return ai;
//	}

}
