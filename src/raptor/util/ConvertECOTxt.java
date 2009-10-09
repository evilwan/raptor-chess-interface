package raptor.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import raptor.chess.Game;
import raptor.chess.util.GameUtils;

/**
 * Converts resources/common/ECO.txt into resources/common/ECOFen.txt
 */
public class ConvertECOTxt {
	public static void main(String args[]) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(
				"resources/common/ECO.txt"));
		FileWriter writer = new FileWriter("resources/common/ECOFen.txt");

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
				Game game = GameUtils.createStartingPosition(Game.Type.CLASSIC);
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
				String fenPosition = game.toFENPosition();
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
