package raptor.swt.chess;

import raptor.game.GameConstants;
import raptor.pref.PreferenceKeys;

public interface Constants extends GameConstants, PreferenceKeys {
	public static final int WP = 1;
	public static final int WB = 2;
	public static final int WN = 3;
	public static final int WR = 4;
	public static final int WQ = 5;
	public static final int WK = 6;
	public static final int BP = 7;
	public static final int BB = 8;
	public static final int BN = 9;
	public static final int BR = 10;
	public static final int BQ = 11;
	public static final int BK = 12;

	public static final int WP_PIECE_JAIL_SQUARE = WP + 100;
	public static final int WB_PIECE_JAIL_SQUARE = WB + 100;
	public static final int WN_PIECE_JAIL_SQUARE = WN + 100;
	public static final int WR_PIECE_JAIL_SQUARE = WR + 100;
	public static final int WQ_PIECE_JAIL_SQUARE = WR + 100;
	public static final int BP_PIECE_JAIL_SQUARE = BP + 100;
	public static final int BB_PIECE_JAIL_SQUARE = BB + 100;
	public static final int BN_PIECE_JAIL_SQUARE = BN + 100;
	public static final int BR_PIECE_JAIL_SQUARE = BR + 100;
	public static final int BQ_PIECE_JAIL_SQUARE = BQ + 100;

	public int[] DROPPABLE_PIECES = { WP, WB, WN, WR, WQ, BP, BB, BN, BR, BQ };

	public static final String[] PIECE_TO_NAME = { "", "wp", "wb", "wn", "wr",
			"wq", "wk", "bp", "bb", "bn", "br", "bq", "bk" };
}
