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
package raptor.chess.pgn;

import raptor.chess.Result;
import raptor.util.RaptorStringTokenizer;
import raptor.util.RaptorStringUtils;

/**
 * The SimplePgnParser.
 * 
 */
public class SimplePgnParser extends AbstractPgnParser {

	/**
	 * OLD SLOW REGEX private static final String STARTS_WITH_MOVE_NUMBER_REGEX
	 * = "((\\d*)(([.])|([.][.][.])))(.*)";
	 * 
	 * private static final String STARTS_WITH_GAME_END_REGEX =
	 * "(([1][-][0])|([0][-][1])|([1][////][2][-][1][////][2])|([*]))(.*)";
	 * 
	 * private static final String STARTS_WITH_NAG_REGEX =
	 * "([$]\\d\\d?\\d?)(.*)";
	 */

	public static final String GAME_START_WORD = "[Event";

	protected int columnNumber;

	protected String currentLine;

	protected int lineNumber;

	protected RaptorStringTokenizer lineTokenizer;

	protected String pgn;

	public SimplePgnParser(String pgn) {
		if (pgn == null || pgn.length() == 0) {
			throw new IllegalArgumentException("pgn cant be null or empty.");
		}
		this.pgn = pgn;
		lineTokenizer = new RaptorStringTokenizer(pgn, "\n\r", true);
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void parse() {

		boolean isSearchingForHeaders = true;

		readNextLine();
		while (currentLine != null) {

			if (currentLine.trim().length() == 0) {
				readNextLine();
				continue;
			}

			if (isSearchingForHeaders) {
				while (isSearchingForHeaders && currentLine != null
						&& currentLine.length() > 0) {
					int openBracketIndex = currentLine.indexOf('[');
					int closeBracketIndex = currentLine.indexOf(']');

					if (openBracketIndex != -1 && closeBracketIndex != -1) {
						String suggestedHeader = currentLine.substring(
								openBracketIndex, closeBracketIndex + 1);

						String[] headers = parseForHeader(suggestedHeader);

						if (headers != null) {
							if (openBracketIndex != 0) {
								fireUnknown(currentLine.substring(0,
										openBracketIndex));
								columnNumber += openBracketIndex;
							}

							if (headers[0].equals("Event")) {
								fireGameStart();
							}

							fireHeader(headers[0], headers[1]);
							currentLine = currentLine
									.substring(closeBracketIndex + 1);

						} else // We encountered something that was'nt a header.
						// Assume it is part of the game.
						{
							isSearchingForHeaders = false;
							break;
						}
					} else // We encountered something that was'nt a header.
					// Assume it is part of the game.
					{
						isSearchingForHeaders = false;
						break;
					}

				}
			}

			RaptorStringTokenizer wordTok = new RaptorStringTokenizer(
					currentLine, " ", true);
			String nextWord = wordTok.nextToken();

			do {
				if (nextWord == null || "".equals(nextWord)) {
					// We have reached the end of the line, read in the next
					// line.
					readNextLine();
					break;
				} else if (nextWord.equals(")")) {
					fireSublineEnd();
					nextWord = wordTok.nextToken();
				} else if (nextWord.startsWith(")")) {
					fireSublineStart();
					nextWord = nextWord.substring(1);
				} else if (nextWord.startsWith("(")) {
					// Determine if its a comment or a sub-line.
					// ( comments can not span multiple lines in PGN.
					int closingParenIndex = nextWord.indexOf(')');
					if (closingParenIndex != -1) {
						// Definitely a comment.
						if (nextWord.length() > 2) {
							fireAnnotation(nextWord.substring(1, nextWord
									.length()));
						} else {
							// Just eat it its a () comment and move on.
						}
					} else if (nextWord.length() > 1) {
						if (nextWord.charAt(1) == '{'
								|| Character.isDigit(nextWord.charAt(1))) {
							// Definitely a subline.
							fireSublineStart();
							nextWord = nextWord.substring(1);
						} else {
							// Definitely a comment.
							wordTok.changeDelimiters(")");
							String annotation = nextWord.substring(1) + " "
									+ wordTok.nextToken();
							wordTok.changeDelimiters(" ");
							fireAnnotation(annotation);
							nextWord = wordTok.nextToken();
						}
					} else {
						String nextNextWord = wordTok.peek();
						if (nextNextWord.charAt(0) == '{'
								|| Character.isDigit(nextNextWord.charAt(0))) {
							// Definitely a subline.
							fireSublineStart();
							if (nextWord.length() > 1) {
								nextWord = nextWord.substring(1);
							} else {
								nextWord = wordTok.nextToken();
							}
						} else {
							wordTok.changeDelimiters(")");
							String annotation = nextWord.substring(1) + " "
									+ wordTok.nextToken();
							wordTok.changeDelimiters(" ");
							fireAnnotation(annotation);
							nextWord = wordTok.nextToken();
						}
					}

				} else if (nextWord.startsWith("{")) {
					int closingBrace = nextWord.indexOf("}");
					if (closingBrace != -1) {
						String annotation = nextWord.substring(1, closingBrace);
						fireAnnotation(annotation);

						if (nextWord.length() > closingBrace + 1) {
							nextWord = nextWord.substring(closingBrace + 1,
									nextWord.length());
						} else {
							nextWord = wordTok.nextToken();
						}
					} else {
						if (wordTok.indexInWhatsLeft('}') != -1) {
							wordTok.changeDelimiters("}");
							String annotation = nextWord.substring(1) + " "
									+ wordTok.nextToken();
							wordTok.changeDelimiters(" ");
							fireAnnotation(annotation);
							nextWord = wordTok.nextToken();
						} else {
							StringBuilder annotation = new StringBuilder(
									nextWord.substring(1) + " "
											+ wordTok.getWhatsLeft());

							int closingBraceIndex = -1;
							do {
								readNextLine();

								if (currentLine != null) {
									closingBraceIndex = currentLine
											.indexOf('}');
									if (closingBraceIndex == -1) {
										annotation.append(" " + currentLine);
									} else {
										annotation.append(" "
												+ currentLine.substring(0,
														closingBraceIndex));
									}
								}
							} while (currentLine != null
									&& closingBraceIndex == -1);

							if (currentLine == null) {
								fireUnknown(annotation.toString());
								// This will actually break all the way out of
								// the
								// main loop since currentLine is null.
								break;
							} else {
								fireAnnotation(annotation.toString());
								wordTok = new RaptorStringTokenizer(currentLine
										.substring(closingBraceIndex + 1),
										" \t", true);
								nextWord = wordTok.nextToken();
							}
						}
					}
				} else {					
					String[] moveNumberSplit = splitOutGameMoveNumber(nextWord);
					if (moveNumberSplit != null) {
						int moveNumber = Integer.parseInt(moveNumberSplit[0]);
						fireMoveNumber(moveNumber);

						if (moveNumberSplit.length == 1) {
							nextWord = wordTok.nextToken();
						} else {
							nextWord = moveNumberSplit[1];
						}
					} else {
						String[] nagSplit = splitOutNag(nextWord);
						if (nagSplit != null) {
							Nag nag = Nag.get(nagSplit[0]);
							if (nag == null) {
								fireUnknown(nagSplit[0]);
							} else {
								fireMoveNag(nag);
							}
							nextWord = nagSplit.length == 1 ? wordTok
									.nextToken() : nagSplit[1];

						} else {
							String[] gameEndSplit = splitOutGameEnd(nextWord);
							if (gameEndSplit != null) {
								fireGameEnd(Result.get(gameEndSplit[0]));

								String whatsLeft = wordTok.getWhatsLeft();

								if (gameEndSplit.length != 1) {
									whatsLeft = gameEndSplit[1] + whatsLeft;
								}
								isSearchingForHeaders = true;
								currentLine = whatsLeft;
								break;
							} else if (nextWord.equals(GAME_START_WORD)) {
								isSearchingForHeaders = true;
								currentLine = nextWord + " "
										+ wordTok.getWhatsLeft();
								break;
							} else if (nextWord.endsWith(")")) {
								nextWord = nextWord.substring(0, nextWord
										.length() - 1);
								fireMoveWord(nextWord);
								fireSublineEnd();
								nextWord = wordTok.nextToken();
							} else {
								fireMoveWord(nextWord);
								nextWord = wordTok.nextToken();
							}
						}
					}
				}

			} while (true);
		}

	}

	/**
	 * Returns null if string is not a header, otherwise returns a String[2]
	 * where index 0 is header name, and index1 is the value. This method is
	 * fast and does'nt use REGEX for parsing.
	 */
	public String[] parseForHeader(String string) {
		if (string.length() >= 7 && string.startsWith("[")
				&& string.endsWith("]")) {
			int quoteIndex = string.indexOf('\"');
			if (quoteIndex != -1) {
				int quote2Index = string.lastIndexOf('\"');

				if (quote2Index > quoteIndex
						&& RaptorStringUtils.count(string, '\"') == 2
						&& quote2Index + 2 == string.length()) {
					int spaceIndex = string.indexOf(' ');
					if (spaceIndex != -1 && spaceIndex < quoteIndex
							&& spaceIndex >= 2) {
						return new String[] { string.substring(1, spaceIndex),
								string.substring(quoteIndex + 1, quote2Index) };
					}
				}
			}
		}
		return null;
	}

	protected void readNextLine() {
		currentLine = lineTokenizer.nextToken();
		lineNumber++;
	}

	/**
	 * If word token is not a game end indicator null is returned. Else if word
	 * token is just a game end indicator then a String[0] is returned with the
	 * word token as the game end indicator. Else a String[2] is returned where
	 * index 0 is the result indicator, and 1 is the rest of the string.
	 */
	private String[] splitOutGameEnd(String wordToken) {
		if (wordToken.startsWith(Result.BLACK_WON.getDescription())) {
			return splitOutStartString(wordToken, Result.BLACK_WON
					.getDescription());
		} else if (wordToken.startsWith(Result.WHITE_WON.getDescription())) {
			return splitOutStartString(wordToken, Result.WHITE_WON
					.getDescription());
		} else if (wordToken.startsWith(Result.DRAW.getDescription())) {
			return splitOutStartString(wordToken, Result.DRAW.getDescription());
		} else if (wordToken.startsWith(Result.ON_GOING.getDescription())) {
			return splitOutStartString(wordToken, Result.ON_GOING
					.getDescription());
		} else {
			return null;
		}
	}

	/**
	 * Move numbers are in the format 1. or 1... If wordToken isn't a move
	 * number or doesn't start with a move number then null is returned.
	 * Otherwise if it contains a move number and nothing else then a String[1]
	 * is returned with the move number stripped of all '.'s. Otherwise a
	 * String[2] is returned where index 0 is the move number with '.' stripped
	 * and index 1 is the rest of the word token.
	 */
	private String[] splitOutGameMoveNumber(String wordToken) {
		if (Character.isDigit(wordToken.charAt(0))) {
			// Remove the . or ...

			int firstDotIndex = wordToken.indexOf('.');
			if (firstDotIndex != -1) {
				int firstThreeDotIndex = wordToken.indexOf("...");
				if (firstThreeDotIndex != -1) {
					if (wordToken.length() > firstThreeDotIndex + 3) {
						return new String[] {
								wordToken.substring(0, firstThreeDotIndex),
								wordToken.substring(firstThreeDotIndex + 3) };
					} else {
						return new String[] { wordToken.substring(0,
								firstThreeDotIndex) };
					}
				} else {
					if (wordToken.length() > firstDotIndex + 1) {
						return new String[] {
								wordToken.substring(0, firstDotIndex),
								wordToken.substring(firstDotIndex + 1) };
					} else {
						return new String[] { wordToken.substring(0,
								firstDotIndex) };
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns null if wordToken isn't a nag. If word token is only a nag then a
	 * String[1] is returned where index 0 = wordToken. If word token starts
	 * with a nag then a String[2] is returned where 0=nag and 1 = rest of word.
	 * This method is fast and does'nt use REGEX for parsing.
	 */
	private String[] splitOutNag(String wordToken) {

		if (wordToken.startsWith("$")) {
			int digitEndIndex = 1;
			for (int i = 1; i < wordToken.length(); i++) {
				if (!Character.isDigit(wordToken.charAt(i))) {
					break;
				} else {
					digitEndIndex++;
				}
			}

			if (digitEndIndex <= 1) {
				return null;
			} else if (wordToken.length() == digitEndIndex) {
				return new String[] { wordToken };
			} else {
				return new String[] { wordToken.substring(0, digitEndIndex),
						wordToken.substring(digitEndIndex, wordToken.length()) };
			}
		}
		return null;
	}

	private String[] splitOutStartString(String wordToken, String startString) {
		return wordToken.length() == startString.length() ? new String[] { startString }
				: new String[] { startString,
						wordToken.substring(startString.length()) };
	}
}
