package raptor.swt.chess;

import raptor.game.GameConstants;
import raptor.pref.PreferenceKeys;

public interface BoardConstants extends GameConstants, PreferenceKeys {

	public int[] DROPPABLE_PIECES = { WP, WB, WN, WR, WQ, WK, BP, BB, BN, BR,
			BQ, BK };
	public int[] DROPPABLE_PIECE_COLOR = { WHITE, WHITE, WHITE, WHITE, WHITE,
			WHITE, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK };
	public int[] INITIAL_DROPPABLE_PIECE_COUNTS = { 8, 2, 2, 2, 1, 1, 8, 2, 2,
			2, 1, 1 };

	public static final String[] PIECE_TO_NAME = { "", "wp", "wb", "wn", "wr",
			"wq", "wk", "bp", "bb", "bn", "br", "bq", "bk" };
}
