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
import raptor.action.RaptorAction.Category;
import raptor.action.RaptorAction.ContainerOrderComparator;
import raptor.action.RaptorAction.NameComparator;
import raptor.action.RaptorAction.RaptorActionContainer;

/**
 * This service manages only ActionScripts.
 */
public class ActionScriptService {
	private static final Log LOG = LogFactory.getLog(ActionScriptService.class);

	private static final ActionScriptService singletonInstance = new ActionScriptService();

	public static interface ActionServiceListener {
		public void onActionsChanged();
	}

	public Map<String, RaptorAction> nameToActionMap = new HashMap<String, RaptorAction>();

	public List<ActionServiceListener> listeners = Collections
			.synchronizedList(new ArrayList<ActionServiceListener>(5));

	public static ActionScriptService getInstance() {
		return singletonInstance;
	}

	private ActionScriptService() {
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
		return new File(Raptor.USER_RAPTOR_HOME_PATH + "/scripts/action/"
				+ actionName + ".properties").delete();
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
	 * Returns all actions in the specified category sorted alphabetically.
	 */
	public RaptorAction[] getActions(Category category) {
		ArrayList<RaptorAction> actions = new ArrayList<RaptorAction>(20);
		for (RaptorAction action : nameToActionMap.values()) {
			if (action.getCategory() == category) {
				actions.add(action);
			}
		}
		Collections.sort(actions, new NameComparator());
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
		String fileName = Raptor.USER_RAPTOR_HOME_PATH + "/scripts/action/"
				+ action.getName() + ".properties";
		FileOutputStream fileOut = null;
		try {
			RaptorActionFactory.save(action).store(
					fileOut = new FileOutputStream(fileName),
					"Saved in Raptor by ActionScriptService.");
		} catch (IOException ioe) {
			Raptor.getInstance().onError(
					"Error saving action: " + action.getName(), ioe);
		} finally {
			try {
				fileOut.flush();
				fileOut.close();
			} catch (Throwable t) {
			}
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

		File systemScripts = new File(Raptor.RESOURCES_DIR + "scripts/action");
		File[] files = systemScripts.listFiles(new FilenameFilter() {

			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".properties");
			}
		});

		if (files != null) {
			for (File file : files) {
				FileInputStream fileIn = null;
				try {
					Properties properties = new Properties();
					properties.load(fileIn = new FileInputStream(file));
					RaptorAction action = RaptorActionFactory.load(properties);
					nameToActionMap.put(action.getName(), action);
					action.setSystemAction(true);
					count++;
				} catch (IOException ioe) {
					Raptor.getInstance().onError(
							"Error loading action " + file.getName() + ",ioe");
				} finally {
					try {
						fileIn.close();
					} catch (Throwable t) {
					}
				}
			}
		}

		File userActions = new File(Raptor.USER_RAPTOR_HOME_PATH
				+ "/scripts/action");
		File[] userFiles = userActions.listFiles(new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".properties");
			}
		});

		if (userFiles != null) {
			for (File file : userFiles) {
				FileInputStream fileIn = null;
				try {
					Properties properties = new Properties();
					properties.load(fileIn = new FileInputStream(file));
					RaptorAction action = RaptorActionFactory.load(properties);
					nameToActionMap.put(action.getName(), action);
					action.setSystemAction(false);
					count++;
				} catch (IOException ioe) {
					Raptor.getInstance().onError(
							"Error loading action " + file.getName() + ",ioe");
				} finally {
					try {
						fileIn.close();
					} catch (Throwable t) {
					}
				}

			}
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("Loaded " + count + " actions in "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
	}
}
