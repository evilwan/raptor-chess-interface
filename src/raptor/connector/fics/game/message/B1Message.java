package raptor.connector.fics.game.message;

/**
 * Special information for bughouse games --------------------------------------
 * 
 * When showing positions from bughouse games, a second line showing piece
 * holding is given, with "<b1>" at the beginning, for example:
 * 
 * <b1> game 6 white [PNBBB] black [PNB]
 * 
 * Also, when pieces are "passed" during bughouse, a short data string -- not
 * the entire board position -- is sent. For example:
 * 
 * <b1> game 52 white [NB] black [N] <- BN
 * 
 * The final two letters indicate the piece that was passed; in the above
 * example, a knight (N) was passed to Black.
 * 
 * A prompt may preceed the <b1> header.
 */
public class B1Message {
	public String gameId;

	/**
	 * Indexed by GameConstants piece type, valued by the number of pieces.
	 */
	public int[] blackHoldings;
	/**
	 * Indexed by GameConstants piece type, valued by the number of pieces.
	 */
	public int[] whiteHoldings;

	@Override
	public String toString() {
		return "B1Message: gameId=" + gameId;
	}
}