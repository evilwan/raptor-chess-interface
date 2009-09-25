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

	public ChatService getChatService();

	public String getDescription();

	public GameScript getGameScript(String name);

	public GameScript[] getGameScripts();

	public GameService getGameService();

	public PreferenceStore getPreferences();

	public String getPrompt();

	public String getShortName();

	public String getTellToString(String handle);

	public boolean isConnected();

	public void makeMove(Game game, Move move);

	public void onAbortKeyPress();

	public void onAcceptKeyPress();

	public void onDeclineKeyPress();

	public void onDraw(Game game);

	public void onExamineModeBack(Game game);

	public void onExamineModeCommit(Game game);

	public void onExamineModeFirst(Game game);

	public void onExamineModeForward(Game game);

	public void onExamineModeLast(Game game);

	public void onExamineModeRevert(Game game);

	public void onRematchKeyPress();

	public void onUnexamine(Game game);

	public void onUnobserve(Game game);

	public void refreshGameScripts();

	public void removeGameScript(GameScript script);

	public void sendMessage(String msg);

	public void setPreferences(PreferenceStore preferences);
	
	public void onError(String message,Throwable t);
	
	public void onError(String message);
}
