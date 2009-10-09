package raptor.chess.pgn;

import java.io.Serializable;

public enum PgnHeader implements Serializable {

	EVENT("Event"), // The name of the tournament or match.
	SITE("Site"), // The location of the event City, Region COUNTRY
	// where COUNTRY is the three letter international
	// olympic committee code.
	DATE("Date"), // YYYY.MM.DD format. ?? are used for unknown values.
	ROUND("Round"), // The playing round ordinal of the game within the event. ?
	// is used for unknown values.
	WHITE("White"), // Player of white pieces in last,first name format.
	BLACK("Black"), // Player of white pieces in last,first name format.
	RESULT("Result"), // The result of the game. Possible values
	// "1-0","0-1","1/2-1/2","*"(ongoing).
	EVENTDATE("EventDate"), // The event date ? for unknown values.
	PLYCOUNT("PlyCount"), // The half move count.
	VARIANT("Variant"), // The variant of the game
	TERMINATION("Termination"), // The termination reason
	TIME_CONTROL("TimeControl"), // The time control ICC uses totalSecs+inc
	FEN("FEN"), // The fen for the initial position.
	ICC_RESULT("ICCResult"), // Descriptive like Black resigns.
	RESULT_DESCRIPTION("ResultDescription"), WHITE_ELO("WhiteElo"), // Whites
	// ELO
	BLACK_ELO("BlackElo"), // Blacks ELO
	OPENING("Opening"), // A description of the opening: Sicilian
	// dragon,Yugoslav attack,7...O-O
	ECO("ECO"), // The ECO code.
	NIC("NIC"), // I have no idea something icc uses
	TIME("Time"), // The time the game took place.
	ANNOTATOR("Annotator");// The annotator of the game.

	public static transient final String UNKNOWN_VALUE = "?";

	public static transient final PgnHeader[] STR_HEADERS = new PgnHeader[] {
			EVENT, SITE, DATE, ROUND, WHITE, BLACK, RESULT };

	public static boolean isStrHeader(String header) {
		boolean result = false;
		for (PgnHeader pgnHeader : STR_HEADERS) {
			if (pgnHeader.getName().equals(header)) {
				result = true;
				break;
			}
		}
		return result;
	}

	private String tagName;

	private PgnHeader(String tagName) {
		this.tagName = tagName;
	}

	public String getName() {
		return tagName;
	}
}
