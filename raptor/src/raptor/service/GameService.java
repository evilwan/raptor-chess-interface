/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

	/**
	 * An adapter class which provides default implementations for the
	 * GameServiceListener interface.
	 */
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

	/**
	 * Returns an array of all active games in the game service.
	 */
	public Game[] getAllActiveGames() {
		List<Game> result = new ArrayList<Game>(5);
		for (Game game : gameMap.values()) {
			if (game.isInState(Game.ACTIVE_STATE)) {
				result.add(game);
			}
		}
		return result.toArray(new Game[0]);
	}

	/**
	 * Returns the game with the specified id.
	 */
	public Game getGame(String gameId) {
		return gameMap.get(gameId);
	}

	/**
	 * Returns the number of games this game service is managing.
	 */
	public int getGameCount() {
		return gameMap.values().size();
	}

	/**
	 * Removes a game from the game service.
	 */
	public void removeGame(Game game) {
		gameMap.remove(game.getId());
	}

	/**
	 * Removes a game service listener.
	 */
	public void removeGameServiceListener(GameServiceListener listener) {
		listeners.remove(listener);
	}
}
