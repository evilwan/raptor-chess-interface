package raptor.connector.fics.game;

import raptor.connector.fics.game.message.RemovingObsGameMessage;
import raptor.util.RaptorStringTokenizer;

public class RemovingObsGameParser {

	public static final String REMOVING_GAME = "Removing game ";

	public RemovingObsGameMessage parse(String message) {
		RemovingObsGameMessage result = null;
		if (message.startsWith(REMOVING_GAME)) {
			result = new RemovingObsGameMessage();
			RaptorStringTokenizer tok = new RaptorStringTokenizer(message, " ",
					true);

			// parse past Removing game
			tok.nextToken();
			tok.nextToken();

			result.gameId = tok.nextToken();
		}
		return result;
	}
}
