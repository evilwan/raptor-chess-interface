package raptor.game.pgn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class StreamingPgnParser extends SimplePgnParser {
	private BufferedReader reader;
	int charsParsed = 0;
	int maxChars = -1;

	public StreamingPgnParser(Reader reader, int maxChars) throws IOException {
		super("garbage");
		this.reader = new BufferedReader(reader, 5000);
		this.maxChars = maxChars;
	}

	@Override
	protected void readNextLine() {
		try {
			currentLine = reader.readLine();
			if (currentLine != null) {
				charsParsed += currentLine.length();
				if (charsParsed > maxChars) {
					currentLine = null;
				}
				lineNumber++;
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
}
