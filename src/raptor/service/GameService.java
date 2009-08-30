package raptor.service;

import raptor.game.Game;

public class GameService {
	private static final GameService instance = new GameService();

	public interface GameServiceListener {
		public void gameCreated(int gameId);

		public void gameStateChanged(int gameId);

		public void gameEnded(int gameId);

		public void gameDestroyed(int gameId);
	}

	public void addGameServiceListener(GameServiceListener listener) {
	}

	public void removeGameServiceListener(GameServiceListener listener) {
	}

	public Game getGame(int gameId) {
		return null;
	}

	public void publishGameCreated(int gameId) {

	}

	public void publishGameStateChanged(int gameId) {

	}

	public void publishGameEnded(int gameId) {

	}

	public void publishGameDestroyed(int gameId) {

	}

	public static GameService getInstance() {
		return instance;
	}
}
