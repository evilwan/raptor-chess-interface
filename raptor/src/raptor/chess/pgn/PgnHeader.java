package raptor.chess.pgn;

/**
 * An enum containing frequently used PgnHeaders.
 */
public enum PgnHeader implements Comparable<PgnHeader> {

	/**
	 * A required PGN header. The name of the tournament or match.
	 */
	Event,
	/**
	 * A required PGN header. The location of the event City, Region COUNTRY
	 * where COUNTRY is the three letter international olympic committee code.
	 */
	Site,
	/**
	 * A required PGN header. YYYY.MM.DD format. ?? are used for unknown values.
	 */
	Date,
	/**
	 * A required PGN header. The playing round ordinal of the game within the
	 * event. ? is used for unknown values.
	 */
	Round,
	/**
	 * A required PGN header. Whites name in last,first name format.
	 */
	White,
	/**
	 * A required PGN header. Blacks name in last,first name format.
	 */
	Black,
	/**
	 * A required PGN header. The result of the game. Possible values
	 * "1-0","0-1","1/2-1/2","*"(ongoing).
	 */
	Result,
	/**
	 * A required PGN header. Represents the date the game occured. Can be ? for
	 * unknown. Should be in yyyy.mm.dd format (e.g. 2009.10.07).
	 */
	EventDate,
	/**
	 * The FEN , Forsyth Edwards Notation, of the initial starting position.
	 */
	FEN,
	/**
	 * A detailed description of the result (e.g. Black wins by white
	 * discconection).
	 */
	ResultDescription,
	/**
	 * Whites rating or elo.
	 */
	WhiteElo,
	/**
	 * Blacks rating or elo.
	 */
	BlackElo,
	/**
	 * The number of half moves made in the game.
	 */
	PlyCount,
	/**
	 * The games ECO code.
	 */
	ECO,
	/**
	 * A description of the opening (e.g. Sicilian dragon,Yugoslav
	 * attack,7...O-O)
	 */
	Opening,
	/**
	 * Denotes the variant of the game being played.
	 * classic,suicide,crazyhouse,losers,atomic,etc.
	 */
	Variant,
	/**
	 * The terminiation reason (e.g. White had a heart attack).
	 */
	Termination,
	/**
	 * The time the game took place.
	 * 
	 */
	Time,
	/**
	 * The time control of the game in MM+S format, e.g. (60+0). MM = minutes +0
	 * == increment in seconds. TimeControl
	 */
	TimeControl,
	/**
	 * White CLocks starting time in 0:01:00.000 format.
	 */
	WhiteClock,
	/**
	 * Black CLock's initial time in 0:01:00.000 format.
	 */
	BlackClock,
	/**
	 * The annotator of the game.
	 */
	Annotator,
	/**
	 * Total white lag in milliseconds.
	 */
	WhiteLagMillis,
	/**
	 * Total black lag in milliseconds.
	 */
	BlackLagMillis,
	/**
	 * The amount of time remaining on whites clock in milliseconds.
	 */
	WhiteRemainingMillis,
	/**
	 * THe amount of time remaining on blacks clock in milliseconds.
	 */
	BlackRemainingMillis,
	/**
	 * A header that is set to 1 if white should be placed on the top when
	 * viewing the game, 0 if white should be placed on bottom.
	 */
	WhiteOnTop;

	public static transient final String UNKNOWN_VALUE = "?";

	public static transient final PgnHeader[] REQUIRED_HEADERS = new PgnHeader[] {
			Event, Site, Date, Round, White, Black, Result };

	public boolean isRequired() {
		boolean result = false;
		for (PgnHeader pgnHeader : REQUIRED_HEADERS) {
			if (this == pgnHeader) {
				result = true;
				break;
			}
		}
		return result;
	}
}
