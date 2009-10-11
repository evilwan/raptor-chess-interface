package raptor.chess;

/**
 * An enum used to classify variants in chess.
 */
public enum Variant {
	/**
	 * Atomic chess.
	 */
	atomic,
	/**
	 * Bughosue chess.
	 */
	bughouse,
	/**
	 * Normal chess.
	 */
	classic,
	/**
	 * 
	 * Crazyhouse chess.
	 */
	crazyhouse,
	/**
	 * Fischer random chess.
	 */
	fischerRandom,
	/**
	 * Losers chess.
	 */
	losers,
	/**
	 * Suicide chess.
	 */
	suicide,
	/**
	 * Wild Chess. Follow the classical chess rules but starts from different
	 * positions.
	 */
	wild;
}
