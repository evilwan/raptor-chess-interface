package raptor.chess;

/**
 * This class contains information about an atomic explosion. The explosion
 * information does not include the piece being captured. Only the other pieces
 * that exploded of both colors for a move.
 * 
 * This class is used to handle rollbacks.
 */
public class AtomicExplosionInfo {
	/**
	 * Piece exploded.
	 */
	int piece;
	/**
	 * Color of piece exploded.
	 */
	int color;
	/**
	 * Square of piece exploded;
	 */
	int square;
}
