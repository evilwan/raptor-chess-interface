package raptor.game;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

public class GameComparator implements Comparator<Game> {
	protected String pgnHeader;
	protected boolean isAscending;

	public GameComparator(String pgnHeader, boolean isAscending) {
		this.pgnHeader = pgnHeader;
		this.isAscending = isAscending;
	}

	public int compare(Game game1, Game game2) {
		String value1 = StringUtils.defaultString(game1.getHeader(pgnHeader));
		String value2 = StringUtils.defaultString(game2.getHeader(pgnHeader));

		return (isAscending ? 1 : -1) * value1.compareTo(value2);
	}
}
