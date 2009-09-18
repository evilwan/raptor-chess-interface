package raptor.connector.fics;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.connector.Connector;
import raptor.game.Game;
import raptor.game.Move;
import raptor.script.GameScript;
import raptor.service.ChatService;
import raptor.service.GameService;

public class FicsConnector implements Connector {
	static final Log LOG = LogFactory.getLog(FicsConnector.class);

	private HashMap<String, GameScript> gameScriptsMap = new HashMap<String, GameScript>();

	public FicsConnector() {
		refreshGameScripts();
	}

	public String getDescription() {
		return "Free Internet Chess Server";
	}

	public String getShortName() {
		return "fics";
	}

	public void connect() {
	}

	public void disconnect() {
	}

	public ChatService getChatService() {
		// TODO Auto-generated method stub
		return null;
	}

	public GameService getGameService() {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendMessage(String msg) {
		LOG.info("Fics Conector Sending: " + msg);
	}

	public void makeMove(Game game, Move move) {
		sendMessage(move.getSan());
	}

	public void onDraw(Game game) {
		sendMessage("draw");
	}

	public void onExamineModeBack(Game game) {
		sendMessage("back");
	}

	public void onExamineModeCommit(Game game) {
		sendMessage("commit");
	}

	public void onExamineModeFirst(Game game) {
		sendMessage("back 300");
	}

	public void onExamineModeForward(Game game) {
		sendMessage("forward 1");
	}

	public void onExamineModeLast(Game game) {
		sendMessage("forward 300");
	}

	public void onExamineModeRevert(Game game) {
		sendMessage("revert");
	}

	public GameScript[] getGameScripts() {
		return gameScriptsMap.values().toArray(new GameScript[0]);
	}

	public void addGameScript(GameScript script) {
		gameScriptsMap.put(script.getName(), script);
		script.save();
	}

	public GameScript getGameScript(String name) {
		return gameScriptsMap.get(name);
	}

	public void removeGameScript(GameScript script) {
		script.delete();
		gameScriptsMap.remove(script.getName());
		
	}
	
	public void refreshGameScripts() {
		gameScriptsMap.clear();
		GameScript[] scripts = GameScript.getGameScripts(this);
		for (GameScript script : scripts) {
			gameScriptsMap.put(script.getName(), script);
		}
	}
	
	
}
