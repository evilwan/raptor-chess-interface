package raptor.connector.fics.game.message;

public class GameEndMessage {

	public String toString() {
		return "GameEndMessage: " + gameId;
	}

	public static final int WHITE_WON = 0;
	public static final int BLACK_WON = 1;
	public static final int DRAW = 2;
	public static final int ADJOURNED = 3;
	public static final int ABORTED = 4;
	public static final int UNDETERMINED = 5;

	public String gameId;
	public int type;
	public String whiteName;
	public String blackName;
	public String description;
}