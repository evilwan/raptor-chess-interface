package raptor.connector.fics;

import java.util.ArrayList;
import java.util.List;

public class GameBotService {
	protected FicsConnector connector;

	public GameBotService(FicsConnector connector) {
		this.connector = connector;
	}

	public interface GameBotListener {
		public void gameBotPageArrived(String[][] rows, boolean hasNextPage);
		public void gameBotPlayerNotInDB(String playerName);
	}

	protected List<GameBotListener> gameBotListeners = new ArrayList<GameBotListener>(
			10);

	public void history(String playerName) {
		if (connector.isLoggedIn()) {
			connector.sendMessage("tell gamebot hi " + playerName + " -bot",true);
		}
	}
	
	public void examine(String gameId) {
		if (connector.isLoggedIn()) {
			connector.sendMessage("tell gamebot ex " + gameId + " -bot", true);
		}
	}

	public void nextPage() {
		if (connector.isLoggedIn()) {
			connector.sendMessage("tell gamebot next " + " -bot", true);
		}
	}

	public void fireGameBotPageArrived(String[][] rows, boolean hasNextPage) {
		for (GameBotListener listener : gameBotListeners) {
			listener.gameBotPageArrived(rows, hasNextPage);
		}
	}

	public void fireGameBotPlayerNotInDb(String playerName) {
		for (GameBotListener listener : gameBotListeners) {
			listener.gameBotPlayerNotInDB(playerName);
		}
	}
	
	public boolean hasGameBotListener() {
		return gameBotListeners.size() > 0;
	}

	public void addGameBotListener(GameBotListener listener) {
		gameBotListeners.add(listener);
	}

	public void removeGameBotListener(GameBotListener listener) {
		gameBotListeners.remove(listener);
	}
}
