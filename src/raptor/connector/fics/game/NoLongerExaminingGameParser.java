package raptor.connector.fics.game;

import raptor.connector.fics.game.message.NoLongerExaminingGameMessage;
import raptor.util.RaptorStringTokenizer;

public class NoLongerExaminingGameParser {
	public static final String NO_LONGER_EXAMINING = "You are no longer examining game";

	public NoLongerExaminingGameMessage parse(String message) {
		NoLongerExaminingGameMessage result = null;
		if (message.startsWith(NO_LONGER_EXAMINING)) {
			result = new NoLongerExaminingGameMessage();
			RaptorStringTokenizer tok = new RaptorStringTokenizer(message, " .",
					true);

			// parse past You are no longer examinng game.
			tok.nextToken();
			tok.nextToken();
			tok.nextToken();
			tok.nextToken();
			tok.nextToken();
			tok.nextToken();

			result.gameId = tok.nextToken();
		}
		return result;
	}
}
