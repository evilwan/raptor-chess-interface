package raptor.connector.fics.game;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.connector.fics.game.message.IllegalMoveMessage;

public class IllegalMoveParser {

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(IllegalMoveParser.class);

	public static final String ILLEGAL_MOVE_START = "Illegal move (";

	public IllegalMoveMessage parse(String message) {
		IllegalMoveMessage result = null;
		if (message.startsWith(ILLEGAL_MOVE_START)) {
			result = new IllegalMoveMessage();
			int closingParenIndex = message.indexOf(")");
			result.move = message.substring(ILLEGAL_MOVE_START.length(),
					closingParenIndex);
		}
		return result;
	}
}
