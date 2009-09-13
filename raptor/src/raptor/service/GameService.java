package raptor.service;

import java.util.HashMap;

import raptor.game.Game;
import raptor.game.Move;

public class GameService {
	private static final GameService instance = new GameService();
	HashMap<String, Game> gameMap = new HashMap<String, Game>();

	public interface GameServiceListener {
		public void gameCreated(String gameId);

		public void gameStateChanged(String gameId);

		public void gameEnded(String gameId);

		public void gameDestroyed(String gameId);
	}

	public void addGameServiceListener(GameServiceListener listener) {
	}

	public void removeGameServiceListener(GameServiceListener listener) {
	}

	public void addGame(Game game) {
		gameMap.put(game.getId(), game);
	}

	public void removeGame(Game game) {
		gameMap.remove(game.getId());
	}

	public Game getGame(String gameId) {
		return gameMap.get(gameId);
	}

	public void notifyUserMove(String gameId, Move move) {
        
	}

	public void notifyGameStateChanged(String gameId) {
	}

	public void notifyGameCreated(String gameId) {
	}

	public void notifyGameEnded(String gameId) {
	}

	public void notifyGameDestroyed(String gameId) {
	}

	public static GameService getInstance() {
		return instance;
	}
}
