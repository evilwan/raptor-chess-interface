package raptor.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import raptor.game.Game;

public class GameService {
	public interface GameServiceListener {
		public void gameCreated(Game game);

		public void gameInactive(Game game);

		public void gameStateChanged(Game game);
	}

	protected HashMap<String, Game> gameMap = new HashMap<String, Game>();
	protected List<GameServiceListener> listeners = new ArrayList<GameServiceListener>(
			20);

	public void addGame(Game game) {
		gameMap.put(game.getId(), game);
	}

	public void addGameServiceListener(GameServiceListener listener) {
		listeners.add(listener);
	}

	public void dispose() {
		gameMap.clear();
	}

	public void fireGameCreated(String gameId) {
		Game game = getGame(gameId);
		if (game != null) {
			for (GameServiceListener listener : listeners) {
				listener.gameCreated(game);
			}
		}
	}

	public void fireGameInactive(String gameId) {
		Game game = getGame(gameId);
		if (game != null) {
			for (GameServiceListener listener : listeners) {
				listener.gameInactive(game);
			}
		}
	}

	public void fireGameStateChanged(String gameId) {
		Game game = getGame(gameId);
		if (game != null) {
			for (GameServiceListener listener : listeners) {
				listener.gameStateChanged(game);
			}
		}
	}

	public Game getGame(String gameId) {
		return gameMap.get(gameId);
	}

	public void removeGame(Game game) {
		gameMap.remove(game.getId());
	}

	public void removeGameServiceListener(GameServiceListener listener) {
		listeners.remove(listener);
	}
}
