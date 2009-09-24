package raptor.connector;

import org.eclipse.jface.preference.PreferenceStore;

import raptor.game.Game;
import raptor.game.Move;
import raptor.script.GameScript;
import raptor.service.ChatService;
import raptor.service.GameService;

public interface Connector {
	public void connect();

	public void disconnect();

	public String getShortName();

	public String getDescription();

	public String getPrompt();

	public GameService getGameService();

	public ChatService getChatService();

	public void sendMessage(String msg);

	public void makeMove(Game game, Move move);

	public void onExamineModeBack(Game game);

	public void onExamineModeForward(Game game);

	public void onExamineModeFirst(Game game);

	public void onExamineModeLast(Game game);

	public void onExamineModeRevert(Game game);

	public void onExamineModeCommit(Game game);
	
	public void onUnobserve(Game game);

	public void onDraw(Game game);

	public void onAcceptKeyPress();

	public void onDeclineKeyPress();

	public void onAbortKeyPress();

	public void onRematchKeyPress();

	public String getTellToString(String handle);

	public GameScript[] getGameScripts();

	public GameScript getGameScript(String name);

	public void removeGameScript(GameScript script);

	public void addGameScript(GameScript script);

	public void refreshGameScripts();

	public PreferenceStore getPreferences();

	public void setPreferences(PreferenceStore preferences);

	public void dispose();

	public boolean isConnected();
}
