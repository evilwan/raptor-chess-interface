package raptor.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import raptor.game.Game;

/**
 * A class which manages active games that belong to a connector.
 */
public class GameService {
	public static class GameServiceAdapter implements GameServiceListener {
		public void gameCreated(Game game) {
		}

		public void gameInactive(Game game) {
		}

		public void gameStateChanged(Game game, boolean isNewMove) {
		}

		public void illegalMove(Game game, String move) {
		}

		public void setupGameBecameExamined(Game game) {

		}
	}

	public static interface GameServiceListener {

		/**
		 * Invoked when a game is created.
		 */
		public void gameCreated(Game game);

		/**
		 * Invoked when a game becomes inactive, i.e. no longer active on the
		 * connector. After this method is invoked on all listeners the game is
		 * removed from the GameService.
		 */
		public void gameInactive(Game game);

		/**
		 * Invoked when the state of a game changes. This can be from a move
		 * being made or from something external in the Connector.
		 */
		public void gameStateChanged(Game game, boolean isNewMove);

		/**
		 * Invoked when a user makes a move on a connector that is invalid.
		 */
		public void illegalMove(Game game, String move);

		/**
		 * Invoked when a game which was previously in setup mode has entered
		 * examine mode.
		 */
		public void setupGameBecameExamined(Game game);
	}

	protected HashMap<String, Game> gameMap = new HashMap<String, Game>();

	protected List<GameServiceListener> listeners = Collections
			.synchronizedList(new ArrayList<GameServiceListener>(20));

	public void addGame(Game game) {
		gameMap.put(game.getId(), game);
	}

	public void addGameServiceListener(GameServiceListener listener) {
		listeners.add(listener);
	}

	public void dispose() {
		gameMap.clear();
	}

	/**
	 * This method should only be invoked from a connector.
	 */
	public void fireGameCreated(String gameId) {
		Game game = getGame(gameId);
		if (game != null) {
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.gameCreated(game);
				}
			}
		}
	}

	/**
	 * This method should only be invoked from a connector.
	 * 
	 * Invoked when the game is no longer active. After notifying all of the
	 * listeners the game will be removed from this GameService since it is no
	 * longer updatable by the connector.
	 */
	public void fireGameInactive(String gameId) {
		Game game = getGame(gameId);
		if (game != null) {
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.gameInactive(game);
				}
			}
			removeGame(game);
		}
	}

	/**
	 * This method should only be invoked from a connector.
	 */
	public void fireGameStateChanged(String gameId, boolean isNewMove) {
		Game game = getGame(gameId);
		if (game != null) {
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.gameStateChanged(game, isNewMove);
				}
			}
		}
	}

	/**
	 * This method should only be invoked from a connector.
	 */
	public void fireIllegalMove(String gameId, String move) {
		Game game = getGame(gameId);
		if (game != null) {
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.illegalMove(game, move);
				}
			}
		}
	}

	/**
	 * This method should only be invoked from a connector.
	 */
	public void fireSetupGameBecameExamined(String gameId) {
		Game game = getGame(gameId);
		if (game != null) {
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.setupGameBecameExamined(game);
				}
			}
		}
	}

	public Game[] getAllActiveGames() {
		List<Game> result = new ArrayList<Game>(5);
		for (Game game : gameMap.values()) {
			if (game.isInState(Game.ACTIVE_STATE)) {
				result.add(game);
			}
		}
		return result.toArray(new Game[0]);
	}

	public Game getGame(String gameId) {
		return gameMap.get(gameId);
	}

	public int getGameCount() {
		return gameMap.values().size();
	}

	public void removeGame(Game game) {
		gameMap.remove(game.getId());
	}

	public void removeGameServiceListener(GameServiceListener listener) {
		listeners.remove(listener);
	}
}
