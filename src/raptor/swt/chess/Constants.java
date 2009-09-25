package raptor.swt.chess;

import raptor.game.GameConstants;
import raptor.pref.PreferenceKeys;

public interface Constants extends GameConstants, PreferenceKeys {

	public static final int WP_PIECE_JAIL_SQUARE = WP + 100;
	public static final int WB_PIECE_JAIL_SQUARE = WB + 100;
	public static final int WN_PIECE_JAIL_SQUARE = WN + 100;
	public static final int WR_PIECE_JAIL_SQUARE = WR + 100;
	public static final int WQ_PIECE_JAIL_SQUARE = WQ + 100;
	public static final int WK_PIECE_JAIL_SQUARE = WK + 100;
	public static final int BP_PIECE_JAIL_SQUARE = BP + 100;
	public static final int BB_PIECE_JAIL_SQUARE = BB + 100;
	public static final int BN_PIECE_JAIL_SQUARE = BN + 100;
	public static final int BR_PIECE_JAIL_SQUARE = BR + 100;
	public static final int BQ_PIECE_JAIL_SQUARE = BQ + 100;
	public static final int BK_PIECE_JAIL_SQUARE = BK + 100;

	public int[] DROPPABLE_PIECES = { WP, WB, WN, WR, WQ, WK, BP, BB, BN, BR,
			BQ, BK };
	public int[] DROPPABLE_PIECE_COLOR = { WHITE, WHITE, WHITE, WHITE, WHITE,
			WHITE, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK };
	public int[] INITIAL_DROPPABLE_PIECE_COUNTS = { 8, 2, 2, 2, 1, 1, 8, 2, 2,
			2, 1, 1 };

	public static final String[] PIECE_TO_NAME = { "", "wp", "wb", "wn", "wr",
			"wq", "wk", "bp", "bb", "bn", "br", "bq", "bk" };
}
