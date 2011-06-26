/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import raptor.Raptor;
import raptor.script.ParameterScript;
import raptor.script.ChatEventScript;
import raptor.script.ScriptConnectorType;
import raptor.script.ScriptUtils;
import raptor.util.RaptorLogger;

/**
 * This service manages ChatEventScripts and ParamterScripts. Scripts
 * are either system scripts or user scripts.
 * <p>
 * System scripts are loaded from the installed Raptor/resources/scripts folder.
 * System scripts also can never be deleted.
 * </p>
 * <p>
 * User scripts are loaded from the users .raptor/scripts directory. User
 * scripts can be deleted. Whenever a script is saved, it is saved as a user
 * script. When a scripts are loaded, a user script will always take precedence
 * over a System script.
 * <p/>
 */
public class ScriptService {
	public static interface ScriptServiceListener {
		public void onParameterScriptsChanged();

		public void onChatEventScriptsChanged();
	}

	private static final RaptorLogger LOG = RaptorLogger.getLog(ScriptService.class);
	public static boolean serviceCreated = false;
	private static final ScriptService singletonInstance = new ScriptService();

	public static ScriptService getInstance() {
		return singletonInstance;
	}

	public Map<String, ChatEventScript> nameToChatEventScript = new HashMap<String, ChatEventScript>();

	public Map<String, ParameterScript> nameToParameterScript = new HashMap<String, ParameterScript>();

	public List<ScriptServiceListener> listeners = Collections
			.synchronizedList(new ArrayList<ScriptServiceListener>(5));

	private ScriptService() {
		reload();
		serviceCreated = true;
	}

	public void addScriptServiceListener(ScriptServiceListener listener) {
		listeners.add(listener);
	}

	/**
	 * Deletes the specified script. System scripts , or the scripts in
	 * resources/script are never touched.
	 */
	public boolean deleteParameterScript(String scriptShortName) {
		nameToParameterScript.remove(scriptShortName.toUpperCase());
		fireParameterScriptsChanged();
		return new File(Raptor.USER_RAPTOR_HOME_PATH + "/scripts/parameter/"
				+ scriptShortName + ".properties").delete();
	}

	/**
	 * Deletes the specified script. System scripts , or the scripts in
	 * resources/script are never touched.
	 */
	public boolean deleteChatEventScript(String scriptName) {
		nameToChatEventScript.remove(scriptName.toUpperCase());
		fireChatEventScriptsChanged();
		return new File(Raptor.USER_RAPTOR_HOME_PATH
				+ "/scripts/chatEvent/" + scriptName + ".properties")
				.delete();
	}

	public void dispose() {
		listeners.clear();
		nameToChatEventScript.clear();
		nameToParameterScript.clear();
	}

	public ParameterScript getParameterScript(String shortName) {
		return nameToParameterScript.get(shortName.toUpperCase());
	}

	/**
	 * Returns all parameter scripts sorted by name
	 */
	public ParameterScript[] getParameterScripts() {
		ArrayList<ParameterScript> result = new ArrayList<ParameterScript>(
				nameToParameterScript.values());
		Collections.sort(result);
		return result.toArray(new ParameterScript[0]);
	}

	/**
	 * Returns all active paramter scripts that match the specified connector
	 * type and paramter script type. The result is sorted by name.
	 */
	public ParameterScript[] getParameterScripts(
			ScriptConnectorType connectorType, ParameterScript.Type type) {
		ArrayList<ParameterScript> result = new ArrayList<ParameterScript>(20);
		for (ParameterScript script : nameToParameterScript.values()) {
			if (script.getConnectorType() == connectorType
					&& script.getType() == type && script.isActive()) {
				result.add(script);
			}
		}
		Collections.sort(result);
		return result.toArray(new ParameterScript[0]);
	}

	public ChatEventScript getChatEventScript(String name) {
		return nameToChatEventScript.get(name.toUpperCase());
	}

	/**
	 * Returns all chat event scripts sorted by name.
	 */
	public ChatEventScript[] getChatEventScripts() {
		ArrayList<ChatEventScript> result = new ArrayList<ChatEventScript>(
				nameToChatEventScript.values());
		Collections.sort(result);
		return result.toArray(new ChatEventScript[0]);
	}

	/**
	 * Returns all chat event scripts sorted by name.
	 */
	public ChatEventScript[] getChatEventScripts(
			ScriptConnectorType connectorType) {
		ArrayList<ChatEventScript> result = new ArrayList<ChatEventScript>();
		for (ChatEventScript script : nameToChatEventScript
				.values()) {
			if (script.getConnectorType() == connectorType) {
				result.add(script);
			}
		}
		Collections.sort(result);
		return result.toArray(new ChatEventScript[0]);
	}

	/**
	 * Reloads all of the scripts.
	 */
	public void reload() {
		nameToChatEventScript.clear();
		nameToParameterScript.clear();
		loadParameterScripts();
		loadChatEventScripts();
	}

	public void removeScriptServiceListener(ScriptServiceListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Saves the game script. Scripts are always saved in the users home
	 * directory. System scripts, or the scripts in resources/script are never
	 * touched.
	 */
	public void save(ParameterScript script) {
		String fileName = Raptor.USER_RAPTOR_HOME_PATH + "/scripts/parameter/"
				+ script.getName() + ".properties";
		FileOutputStream fileOut = null;
		try {
			Properties properties = ScriptUtils.serialize(script);
			properties.store(fileOut = new FileOutputStream(fileName),
					"Saved in Raptor by the ScriptService");
			fileOut.flush();
		} catch (IOException ioe) {
			Raptor.getInstance().onError("Error saving parameter script", ioe);
		} finally {
			try {
				fileOut.close();
			} catch (Throwable t) {
			}
		}
		nameToParameterScript.put(script.getName().toUpperCase(), script);
		fireParameterScriptsChanged();
	}

	/**
	 * Saves the chat script. Scripts are always saved in the users home
	 * directory. System scripts , or the scripts in resources/script are never
	 * touched.
	 */
	public void save(ChatEventScript script) {
        File userScripts = new File(Raptor.USER_RAPTOR_HOME_PATH
				+ "/scripts/chatEvent");
		
		if (!userScripts.exists()) 
				userScripts.mkdir();
		
		
		String fileName = Raptor.USER_RAPTOR_HOME_PATH
				+ "/scripts/chatEvent/" + script.getName()
				+ ".properties";
		FileOutputStream fileOut = null;

		try {
			Properties properties = ScriptUtils.serialize(script);
			properties.store(fileOut = new FileOutputStream(fileName),
					"Saved in Raptor by the ScriptService");
			fileOut.flush();
		} catch (IOException ioe) {
			Raptor.getInstance().onError(
					"Error saving chat event script", ioe);
		} finally {
			try {
				fileOut.close();
			} catch (Throwable t) {
			}
		}
		nameToChatEventScript.put(script.getName().toUpperCase(),
				script);
		fireChatEventScriptsChanged();
	}

	protected void fireParameterScriptsChanged() {
		synchronized (listeners) {
			for (ScriptServiceListener listener : listeners) {
				listener.onParameterScriptsChanged();
			}
		}
	}

	protected void fireChatEventScriptsChanged() {
		synchronized (listeners) {
			for (ScriptServiceListener listener : listeners) {
				listener.onChatEventScriptsChanged();
			}
		}
	}

	protected void loadParameterScripts() {
		int count = 0;
		long startTime = System.currentTimeMillis();

		File systemScripts = new File("resources/scripts/parameter");
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
					ParameterScript script = ScriptUtils
							.unserializeParameterScript(properties);
					nameToParameterScript.put(script.getName().toUpperCase(),
							script);
					script.setSystemScript(true);
					count++;
				} catch (IOException ioe) {
					Raptor.getInstance().onError(
							"Error loading system parameter expression script "
									+ file.getName() + ",ioe");
				} finally {
					try {
						fileIn.close();
					} catch (Throwable t) {
					}
				}
			}
		}

		File userScripts = new File(Raptor.USER_RAPTOR_HOME_PATH
				+ "/scripts/parameter");
		File[] userFiles = userScripts.listFiles(new FilenameFilter() {
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
					ParameterScript script = ScriptUtils
							.unserializeParameterScript(properties);
					nameToParameterScript.put(script.getName().toUpperCase(),
							script);
					script.setSystemScript(false);
					count++;
				} catch (IOException ioe) {
					Raptor.getInstance().onError(
							"Error loading user parameter expression script "
									+ file.getName() + ",ioe");
				} finally {
					try {
						fileIn.close();
					} catch (Throwable t) {
					}
				}
			}
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("Loaded " + count + " Parameter scripts in "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
	}

	protected void loadChatEventScripts() {
		int count = 0;
		long startTime = System.currentTimeMillis();

		File systemScripts = new File("resources/scripts/chatEvent");
		
		if (!systemScripts.exists()) 
			systemScripts.mkdir();
		
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
					ChatEventScript script = ScriptUtils
							.unserializeChatEventScript(properties);
					nameToChatEventScript.put(script.getName()
							.toUpperCase(), script);
					script.setSystemScript(true);
					count++;
				} catch (IOException ioe) {
					Raptor.getInstance().onError(
							"Error loading system chat event script "
									+ file.getName() + ",ioe");
				} finally {
					try {
						fileIn.close();
					} catch (Throwable t) {
					}
				}
			}
		}

		File userScripts = new File(Raptor.USER_RAPTOR_HOME_PATH
				+ "/scripts/chatEvent");
		File[] userFiles = userScripts.listFiles(new FilenameFilter() {
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
					ChatEventScript script = ScriptUtils
							.unserializeChatEventScript(properties);
					nameToChatEventScript.put(script.getName()
							.toUpperCase(), script);
					script.setSystemScript(false);
					count++;
				} catch (IOException ioe) {
					Raptor.getInstance().onError(
							"Error loading user chat event script "
									+ file.getName() + ",ioe");
				} finally {
					try {
						fileIn.close();
					} catch (Throwable t) {
					}
				}
			}
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("Loaded " + count + " chat event scripts in "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
	}
}
