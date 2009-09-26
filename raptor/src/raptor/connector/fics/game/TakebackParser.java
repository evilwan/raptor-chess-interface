package raptor.connector.fics.game;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.util.RaptorStringTokenizer;

/**
 * This parser is a bit unusual in that it maintains state as messages are
 * processed.
 * 
 * You can invoke getTakeBackMessage(gameId) to receive the last take back offer
 * requested and whether or not it was accepted.
 * 
 * It is important to clearTakebackMessages after there is no longer a need to
 * keep this state.
 */
public class TakebackParser {

	public static class TakebackMessage {
		public int halfMovesRequested = -1;
		public boolean wasAccepted;
		public String gameId = "";
	}

	protected Map<String, TakebackMessage> gameToTakebackMessages = new HashMap<String, TakebackMessage>();

	public static final String IDENTIFIER = "Game";
	public static final String REQUEST_TAKE_BACK = "take back";
	public static final String ACCEPTD_TAKE_BACK = "accepts the takeback request";
	private static final Log LOG = LogFactory.getLog(TakebackParser.class);

	/**
	 * Returns the take back message for the specified game id. Returns null if
	 * there are none.
	 */
	public TakebackMessage getTakebackMessage(String gameId) {
		return gameToTakebackMessages.get(gameId);
	}

	/**
	 * Clears the take back message for the specified gameId.
	 */
	public void clearTakebackMessages(String gameId) {
		gameToTakebackMessages.remove(gameId);
	}

	/**
	 * Returns true if line is a take back accepted message.
	 */
	public boolean parse(String line) {
		if (line.startsWith(IDENTIFIER) && line.contains(REQUEST_TAKE_BACK)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing takeback offer.");
			}
			// Game 117: raptorb requests to take back 1 half move(s).
			TakebackMessage message = new TakebackMessage();
			RaptorStringTokenizer tok = new RaptorStringTokenizer(line, " ",
					true);

			tok.nextToken();

			message.gameId = tok.nextToken();
			message.gameId = message.gameId.substring(0, message.gameId
					.length() - 1);

			// parse past HANDLE requests to take back
			tok.nextToken();
			tok.nextToken();
			tok.nextToken();
			tok.nextToken();
			tok.nextToken();
			message.halfMovesRequested = Integer.parseInt(tok.nextToken());

			gameToTakebackMessages.put(message.gameId, message);

			return false;
		} else if (line.startsWith(IDENTIFIER)
				&& line.contains(ACCEPTD_TAKE_BACK)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing accepted takeback.");
			}

			// Game 117: raptora accepts the takeback request.
			RaptorStringTokenizer tok = new RaptorStringTokenizer(line, " ",
					true);
			tok.nextToken();
			String gameId = tok.nextToken();
			gameId = gameId.substring(0, gameId.length() - 1);

			TakebackMessage message = getTakebackMessage(gameId);
			if (message != null) {
				message.wasAccepted = true;
			} else {
				LOG
						.debug("Received a takback accepted for a takeback message that was never received.");
				// Leave halfMoveRequested at -1. This way code which uses the
				// parser can determine this state and react appropriately.
				message = new TakebackMessage();
				message.wasAccepted = true;
				message.gameId = gameId;
				gameToTakebackMessages.put(message.gameId, message);
			}
			return true;
		}
		return false;
	}

}
