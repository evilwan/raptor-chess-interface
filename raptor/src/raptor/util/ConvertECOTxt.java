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
package raptor.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import raptor.chess.Game;
import raptor.chess.GameFactory;
import raptor.chess.Variant;

/**
 * Converts resources/ECO.txt into resources/ECOFen.txt
 */
public class ConvertECOTxt {
	public static void main(String args[]) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(
				"projectFiles/ECO.txt"));
		FileWriter writer = new FileWriter("resources/ECOFen.txt");

		String currentLine = null;
		int lineNumber = 0;
		while ((currentLine = reader.readLine()) != null) {
			lineNumber++;
			System.err.println("Parsing line number: " + lineNumber);
			if (StringUtils.isNotBlank(currentLine)
					&& !currentLine.startsWith("//")) {
				RaptorStringTokenizer lineTok = new RaptorStringTokenizer(
						currentLine, "|");
				String moves = lineTok.nextToken();
				Game game = GameFactory.createStartingPosition(Variant.classic);
				RaptorStringTokenizer movesTokenizer = new RaptorStringTokenizer(
						moves, " ", true);
				while (movesTokenizer.hasMoreTokens()) {
					String currentSan = movesTokenizer.nextToken();
					try {
						game.makeSanMove(currentSan);
					} catch (IllegalArgumentException iae) {
						currentSan = StringUtils.replaceChars(currentSan, 'B',
								'b');

						try {
							game.makeSanMove(currentSan);
						} catch (IllegalArgumentException iae2) {
							currentSan = StringUtils.replaceChars(currentSan,
									'b', 'B');
							game.makeSanMove(currentSan);
						}
					}
				}
				String fenPosition = game.toFenPosition() + " "
						+ (game.isWhitesMove() ? 'w' : 'b');
				writer.write(fenPosition + "|");
				while (lineTok.hasMoreTokens()) {
					writer.write(lineTok.nextToken() + "|");
				}
				writer.write("\n");
			}
		}
		writer.flush();
		writer.close();
		reader.close();
	}
}
