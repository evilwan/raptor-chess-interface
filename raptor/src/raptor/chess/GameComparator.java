package raptor.chess;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

import raptor.chess.pgn.PgnHeader;

/**
 * Compares games by PgnHeader values.
 */
public class GameComparator implements Comparator<Game> {
	protected boolean isAscending;
	protected PgnHeader pgnHeader;

	public GameComparator(PgnHeader pgnHeader, boolean isAscending) {
		this.pgnHeader = pgnHeader;
		this.isAscending = isAscending;
	}

	public int compare(Game game1, Game game2) {
		String value1 = StringUtils.defaultString(game1.getHeader(pgnHeader));
		String value2 = StringUtils.defaultString(game2.getHeader(pgnHeader));

		return (isAscending ? 1 : -1) * value1.compareTo(value2);
	}
}
