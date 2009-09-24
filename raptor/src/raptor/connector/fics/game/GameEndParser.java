package raptor.connector.fics.game;

import raptor.connector.fics.game.message.GameEndMessage;
import raptor.util.RaptorStringTokenizer;

public class GameEndParser {

	public static final String GAME_END = "{Game";

	public GameEndMessage parse(String message) {
		GameEndMessage result = null;
		if (message.startsWith(GAME_END)) {
			result = new GameEndMessage();
			RaptorStringTokenizer tok = new RaptorStringTokenizer(message,
					" ()", true);

			// parse past {Game
			tok.nextToken();

			result.gameId = tok.nextToken();

			result.whiteName = tok.nextToken();

			// parse past vs.
			tok.nextToken();

			result.blackName = tok.nextToken();

			// find description. Its between ) and }
			int closingParenIndex = message.indexOf(")");
			int closingBraceIndex = message.indexOf("}");

			if (closingParenIndex == -1 || closingBraceIndex == -1) {
				throw new IllegalArgumentException(
						"Could not find description in gameEndEvent:" + message);
			}
			result.description = message.substring(closingParenIndex + 1,
					closingBraceIndex).trim();

			String afterClosingBrace = message.substring(closingBraceIndex + 1,
					message.length()).trim();

			if (result.description.indexOf("aborted") != -1)
				result.type = GameEndMessage.ABORTED;
			else if (result.description.indexOf("adjourned") != -1)
				result.type = GameEndMessage.ADJOURNED;
			else if (result.description.indexOf('*') != -1)
				result.type = GameEndMessage.UNDETERMINED;
			else if (afterClosingBrace.startsWith("0-1"))
				result.type = GameEndMessage.BLACK_WON;
			else if (afterClosingBrace.startsWith("1-0"))
				result.type = GameEndMessage.WHITE_WON;
			else if (afterClosingBrace.startsWith("1/2"))
				result.type = GameEndMessage.DRAW;
			else
				result.type = GameEndMessage.UNDETERMINED;
		}
		return result;
	}
}
