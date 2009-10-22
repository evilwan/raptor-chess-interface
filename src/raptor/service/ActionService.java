package raptor.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.action.RaptorAction;
import raptor.action.RaptorActionFactory;
import raptor.action.ScriptedAction;
import raptor.action.SeparatorAction;
import raptor.action.RaptorAction.ContainerOrderComparator;
import raptor.action.RaptorAction.RaptorActionContainer;

public class ActionService {
	private static final Log LOG = LogFactory.getLog(ActionService.class);

	private static final ActionService singletonInstance = new ActionService();

	public static interface ActionServiceListener {
		public void onActionsChanged();
	}

	public Map<String, RaptorAction> nameToActionMap = new HashMap<String, RaptorAction>();

	public List<ActionServiceListener> listeners = Collections
			.synchronizedList(new ArrayList<ActionServiceListener>(5));

	public static ActionService getInstance() {
		return singletonInstance;
	}

	private ActionService() {
		reload();
	}

	public void addActionServiceListener(ActionServiceListener listener) {
		listeners.add(listener);
	}

	/**
	 * Deletes the specified script. System scripts , or the scripts in
	 * resources/script are never touched.
	 */
	public boolean deleteAction(String actionName) {
		nameToActionMap.remove(actionName);
		fireActionsChanged();
		return new File(Raptor.USER_RAPTOR_HOME_PATH + "/actions/" + actionName
				+ ".properties").delete();
	}

	public void dispose() {
		listeners.clear();
		nameToActionMap.clear();
	}

	public RaptorAction getAction(int modifierKey, int keyCode) {
		RaptorAction result = null;
		for (RaptorAction action : nameToActionMap.values()) {
			if (action.getKeyCode() == keyCode
					&& action.getModifierKey() == modifierKey) {
				result = action;
				break;
			}
		}
		return result;
	}

	/**
	 * Returns the action with the specified unique name.
	 */
	public RaptorAction getAction(String name) {
		return nameToActionMap.get(name);
	}

	/**
	 * Returns all actions in the specified container.
	 */
	public RaptorAction[] getActions(RaptorActionContainer container) {
		ArrayList<RaptorAction> actions = new ArrayList<RaptorAction>(20);
		for (RaptorAction action : nameToActionMap.values()) {
			if (action.isIn(container)) {
				actions.add(action);
			}
		}
		Collections.sort(actions, new ContainerOrderComparator(container));
		return actions.toArray(new RaptorAction[0]);
	}

	/**
	 * Returns all chat scripts sorted by name.
	 */
	public RaptorAction[] getAllActions() {
		ArrayList<RaptorAction> actions = new ArrayList<RaptorAction>(
				nameToActionMap.values());

		for (int i = 0; i < actions.size(); i++) {
			if (actions.get(i) instanceof SeparatorAction) {
				actions.remove(i);
				i--;
			}
		}

		return actions.toArray(new RaptorAction[0]);
	}

	/**
	 * Returns all scripted actions.
	 */
	public ScriptedAction[] getAllScriptedActions() {
		ArrayList<ScriptedAction> actions = new ArrayList<ScriptedAction>(
				nameToActionMap.size());

		for (RaptorAction action : nameToActionMap.values()) {
			if (action instanceof ScriptedAction) {
				actions.add((ScriptedAction) action);
			}
		}

		return actions.toArray(new ScriptedAction[0]);
	}

	/**
	 * Reloads all of the scripts.
	 */
	public void reload() {
		nameToActionMap.clear();
		loadActions();
	}

	public void removeActionServiceListener(ActionServiceListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Saves the chat script. Scripts are always saved in the users home
	 * directory. System scripts , or the scripts in resources/script are never
	 * touched.
	 */
	public void saveAction(RaptorAction action) {
		String fileName = Raptor.USER_RAPTOR_HOME_PATH + "/actions/"
				+ action.getName() + ".properties";
		try {
			RaptorActionFactory.save(action).store(
					new FileOutputStream(fileName), "Saved in Raptor");
		} catch (IOException ioe) {
			Raptor.getInstance().onError(
					"Error saving action: " + action.getName(), ioe);
		}
		nameToActionMap.put(action.getName(), action);
		fireActionsChanged();
	}

	protected void fireActionsChanged() {
		synchronized (listeners) {
			for (ActionServiceListener listener : listeners) {
				listener.onActionsChanged();
			}
		}
	}

	protected void loadActions() {
		int count = 0;
		long startTime = System.currentTimeMillis();

		File systemScripts = new File(Raptor.RESOURCES_DIR + "actions");
		File[] files = systemScripts.listFiles(new FilenameFilter() {

			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".properties");
			}
		});

		for (File file : files) {
			try {
				Properties properties = new Properties();
				properties.load(new FileInputStream(file));
				RaptorAction action = RaptorActionFactory.load(properties);
				nameToActionMap.put(action.getName(), action);
				action.setSystemAction(true);
				count++;
			} catch (IOException ioe) {
				Raptor.getInstance().onError(
						"Error loading action " + file.getName() + ",ioe");
			}
		}

		File userActions = new File(Raptor.USER_RAPTOR_HOME_PATH + "/actions");
		File[] userFiles = userActions.listFiles(new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".properties");
			}
		});

		for (File file : userFiles) {
			try {
				Properties properties = new Properties();
				properties.load(new FileInputStream(file));
				RaptorAction action = RaptorActionFactory.load(properties);
				nameToActionMap.put(action.getName(), action);
				action.setSystemAction(false);
				count++;
			} catch (IOException ioe) {
				Raptor.getInstance().onError(
						"Error loading action " + file.getName() + ",ioe");
			}
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("Loaded " + count + " actions in "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
	}
}
