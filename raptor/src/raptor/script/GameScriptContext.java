package raptor.script;

public interface GameScriptContext extends ScriptContext {
	public int getBlackLagSeconds();

	public String getBlackName();

	public int getBlackRating();

	public long getBlackTimeRemaining();

	public String getGameId();

	public int getIncrement();

	public String getLastMove();

	public String getResult();

	public int getTime();

	public int getTotalLagSeconds();

	public int getWhiteLagSeconds();

	public String getWhiteName();

	public int getWhiteRating();

	public long getWhiteTimeRemaining();

	public boolean isAtomic();

	public boolean isBughouse();

	public boolean isClassic();

	public boolean isCrazyhouse();

	public boolean isDroppable();

	public boolean isFischerRandom();

	public boolean isLosers();

	public boolean isSuicide();

	public boolean wasLastMoveCheck();

	public boolean wasLastMoveCheckmate();

	public boolean wasLastMoveStalemate();
}
