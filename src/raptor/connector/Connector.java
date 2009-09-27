package raptor.connector;

import org.eclipse.jface.preference.PreferenceStore;

import raptor.game.Game;
import raptor.game.Move;
import raptor.script.GameScript;
import raptor.service.ChatService;
import raptor.service.GameService;

public interface Connector {
	public void addGameScript(GameScript script);

	public void connect();

	public void disconnect();

	public void dispose();

	/**
	 * Returns descriptions and messages to send to the connector. This is
	 * intended to be used to generate pop-up menus. Returns a String[n][2]
	 * where 0 is the description and 1 is the message to send to the connector.
	 */
	public String[][] getChannelActions(String channel);

	public ChatService getChatService();

	public String getDescription();

	/**
	 * Returns descriptions and messages to send to the connector. This is
	 * intended to be used to generate pop-up menus. Returns a String[n][2]
	 * where 0 is the description and 1 is the message to send to the connector.
	 */
	public String[][] getGameIdActions(String gameId);

	public GameScript getGameScript(String name);

	public GameScript[] getGameScripts();

	public GameService getGameService();

	/**
	 * Returns descriptions and messages to send to the connector. This is
	 * intended to be used to generate pop-up menus. Returns a String[n][2]
	 * where 0 is the description and 1 is the message to send to the connector.
	 */
	public String[][] getPersonActions(String person);

	public PreferenceStore getPreferences();

	public String getPrompt();

	public String getShortName();

	public String getTellToString(String handle);

	public boolean isConnected();

	public boolean isLikelyChannel(String channel);

	public boolean isLikelyGameId(String channel);

	public boolean isLikelyPerson(String word);

	public void makeMove(Game game, Move move);

	public void onAbortKeyPress();

	public void onAcceptKeyPress();

	public void onDeclineKeyPress();

	public void onDraw(Game game);

	public void onError(String message);

	public void onError(String message, Throwable t);

	public void onExamineModeBack(Game game);

	public void onExamineModeCommit(Game game);

	public void onExamineModeFirst(Game game);

	public void onExamineModeForward(Game game);

	public void onExamineModeLast(Game game);

	public void onExamineModeRevert(Game game);

	public void onRematchKeyPress();

	public void onSetupClear(Game game);

	public void onSetupClearSquare(Game game, int square);

	public void onSetupComplete(Game game);

	public void onSetupFromFEN(Game game, String fen);

	public void onSetupStartPosition(Game game);

	public void onUnexamine(Game game);

	public void onUnobserve(Game game);

	public String parseChannel(String word);

	public String parseGameId(String word);

	public String parsePerson(String word);

	public void refreshGameScripts();

	public void removeGameScript(GameScript script);

	public void sendMessage(String msg);

	public void setPreferences(PreferenceStore preferences);

}
