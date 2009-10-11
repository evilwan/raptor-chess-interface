package raptor.chess;

/**
 * An enum used to classify variants in chess.
 */
public enum Variant {
	/**
	 * Normal chess.
	 */
	classic,
	/**
	 * Atomic chess.
	 */
	atomic,
	/**
	 * Bughosue chess.
	 */
	bughouse,
	/**
	 * 
	 * Crazyhouse chess.
	 */
	crazyhouse,
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
	wild,
	/**
	 * Fischer random chess.
	 */
	fischerRandom;
}
