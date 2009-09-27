package raptor.connector.fics.game.message;

public class Style12Message {
	public static final int ISOLATED_POSITION_RELATION = -3;
	public static final int OBSERVING_EXAMINED_GAME_RELATION = -2;
	public static final int EXAMINING_GAME_RELATION = 2;
	public static final int PLAYING_OPPONENTS_MOVE_RELATION = -1;
	public static final int PLAYING_MY_MOVE_RELATION = 1;
	public static final int OBSERVING_GAME_RELATION = 0;
	public String gameId;
	public int[][] position;

	public int numberOfMovesSinceLastIrreversible;
	public String whiteName;
	public String blackName;
	public int relation;
	public long initialTimeMillis;
	public long initialIncMillis;
	public int whiteStrength;
	public int blackStrength;
	public long whiteRemainingTimeMillis;
	public long blackRemainingTimeMillis;
	public int fullMoveNumber;
	public String lan;
	public long timeTakenForLastMoveMillis;
	public String san;
	public boolean isWhiteOnTop;
	public boolean isClockTicking;
	public boolean isWhitesMoveAfterMoveIsMade;
	public boolean canWhiteCastleKSide;
	public boolean canWhiteCastleQSide;
	public boolean canBlackCastleKSide;
	public boolean canBlackCastleQSide;
	public int doublePawnPushFile;
	public int lagInMillis;

	public Style12Message() {
	}

	@Override
	public String toString() {
		return "Style12: gameId=" + gameId + " " + san;
	}
}