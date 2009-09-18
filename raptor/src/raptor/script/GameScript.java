package raptor.script;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.jface.preference.PreferenceStore;

import raptor.App;
import raptor.connector.Connector;
import raptor.swt.chess.ChessBoard;
import raptor.util.RaptorStringUtils;

public class GameScript implements Comparable<GameScript> {
	static final Log LOG = LogFactory.getLog(GameScript.class);

	public static final String SCRIPT_DIR = App.getInstance()
			.getRaptorUserDir().getAbsolutePath()
			+ "/scripts/";

	public static final String DESCRIPTION = "description";
	public static final String SCRIPT = "script";
	public static final String IS_AVAILABLE_IN_EXAMINE_STATE = "isAvailableInExamineState";
	public static final String IS_AVAILABLE_IN_PLAYING_STATE = "isAvailableInPlayingState";
	public static final String IS_AVAILABLE_IN_OBSERVE_STATE = "isAvailableInObserveState";
	public static final String IS_AVAILABLE_IN_SETUP_STATE = "isAvailableInSetupState";
	public static final String IS_AVAILABLE_IN_FREEFORM_STATE = "isAvailableInFreeformState";

	protected String name;
	protected Connector connector;
	protected PreferenceStore store;

	public static GameScript[] getGameScripts(Connector connector) {
		String scriptDirectory = SCRIPT_DIR + connector.getShortName()
				+ "/game/";

		List<GameScript> result = new ArrayList<GameScript>(20);

		File[] files = new File(scriptDirectory).listFiles(new FileFilter() {
			public boolean accept(File arg0) {
				return arg0.getName().endsWith(".properties");
			}
		});
        if(files != null) {
		for (int i = 0; i < files.length; i++) {
			String name = RaptorStringUtils.replaceAll(files[i].getName(),
					".properties", "");
			result.add(new GameScript(connector, name));
		}

		Collections.sort(result);

		return result.toArray(new GameScript[0]);
        }
        else {
        	return new GameScript[0];
        }
	}

	public GameScript() {
		store = new PreferenceStore();
	}

	public GameScript(Connector connector, String name) {
		store = new PreferenceStore(SCRIPT_DIR + connector.getShortName()
				+ "/game/" + name + ".properties");
		try {
			store.load();
		} catch (IOException ioe) {
			LOG.error("Error occured loading game script " + SCRIPT_DIR
					+ connector.getShortName() + "/game/" + name
					+ ".properties", ioe);
			throw new RuntimeException(ioe);
		}
		this.name = name;
		this.connector = connector;
	}

	public Connector getConnector() {
		return connector;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	public String getDescription() {
		return store.getString(DESCRIPTION);
	}

	public void setDescription(String description) {
		store.setValue(DESCRIPTION, description);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScript() {
		return store.getString(SCRIPT);
	}

	public void setScript(String script) {
		store.setValue(SCRIPT, script);
	}

	public boolean isAvailableInExamineState() {
		return store.getBoolean(IS_AVAILABLE_IN_EXAMINE_STATE);
	}

	public void setAvailableInExamineState(boolean isAvailableInExamineState) {
		store
				.setValue(IS_AVAILABLE_IN_EXAMINE_STATE,
						isAvailableInExamineState);
	}

	public boolean isAvailableInPlayingState() {
		return store.getBoolean(IS_AVAILABLE_IN_PLAYING_STATE);
	}

	public void setAvailableInPlayingState(boolean isAvailableInPlayingState) {
		store
				.setValue(IS_AVAILABLE_IN_PLAYING_STATE,
						isAvailableInPlayingState);
	}

	public boolean isAvailableInObserveState() {
		return store.getBoolean(IS_AVAILABLE_IN_OBSERVE_STATE);
	}

	public void setAvailableInObserveState(boolean isAvailableInObserveState) {
		store
				.setValue(IS_AVAILABLE_IN_OBSERVE_STATE,
						isAvailableInObserveState);
	}

	public boolean isAvailableInSetupState() {
		return store.getBoolean(IS_AVAILABLE_IN_SETUP_STATE);
	}

	public void setAvailableInSetupState(boolean isAvailableInSetupState) {
		store.setValue(IS_AVAILABLE_IN_SETUP_STATE, isAvailableInSetupState);
	}

	public boolean isAvailableInFreeformState() {
		return store.getBoolean(IS_AVAILABLE_IN_FREEFORM_STATE);
	}

	public void setAvailableInFreeformState(boolean isAvailableInFreeformState) {
		store.setValue(IS_AVAILABLE_IN_FREEFORM_STATE,
				isAvailableInFreeformState);
	}

	public void execute(ChessBoard board) {

		String script = getScript();
		try {
			VelocityContext context = new VelocityContext();
			context.put("board", board);

			StringWriter writer = new StringWriter();
			Velocity.evaluate(context, writer, "evaluating game script "
					+ getName(), getScript());
			/* show the World */
			connector.sendMessage(writer.toString());
			writer.close();
		} catch (IOException ioe) {
			LOG.error("Error occured executing game script " + name + " "
					+ script, ioe);
			throw new RuntimeException(ioe);
		}
	}

	public boolean equals(GameScript script) {
		return this.connector == script.connector
				&& this.name.equals(script.name);
	}

	public void delete() {
		new File(SCRIPT_DIR + connector.getShortName() + "/game/" + name
				+ ".properties").delete();
	}

	public void save() {
		store.setFilename(SCRIPT_DIR + connector.getShortName() + "/game/"
				+ name + ".properties");
		try {
			store.save();
		} catch (IOException ioe) {
			LOG.error("Error occured saving game script " + SCRIPT_DIR
					+ connector.getShortName() + "/game/" + name
					+ ".properties", ioe);
			throw new RuntimeException(ioe);
		}
	}

	public int compareTo(GameScript o) {
		return this.getName().compareTo(o.getName());
	}

}
