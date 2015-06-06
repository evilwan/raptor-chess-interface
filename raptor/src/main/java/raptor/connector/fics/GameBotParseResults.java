package raptor.connector.fics;

public class GameBotParseResults {
	protected String[][] rows;
	protected boolean hasNextPage;
	protected boolean isPlayerInDb;
	protected boolean isIncomplete;
	protected String playerName;

	public String[][] getRows() {
		return rows;
	}

	public void setRows(String[][] rows) {
		this.rows = rows;
	}

	public boolean hasNextPage() {
		return hasNextPage;
	}

	public void setHasNextPage(boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
	}

	public boolean isPlayerInDb() {
		return isPlayerInDb;
	}

	public void setPlayerInDb(boolean playerNotInDb) {
		this.isPlayerInDb = playerNotInDb;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public boolean isIncomplete() {
		return isIncomplete;
	}

	public void setIncomplete(boolean isIncomplete) {
		this.isIncomplete = isIncomplete;
	}
}
