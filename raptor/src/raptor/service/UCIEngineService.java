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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import raptor.Raptor;
import raptor.engine.uci.UCIEngine;
import raptor.engine.uci.UCIOption;
import raptor.engine.uci.options.UCIButton;
import raptor.util.RaptorLogger;
import raptor.util.RaptorStringUtils;

public class UCIEngineService {
	private static final RaptorLogger LOG = RaptorLogger.getLog(UCIEngineService.class);
	public static boolean serviceCreated = false;
	public static UCIEngineService singletonInstance;

	public static UCIEngineService getInstance() {
		if (singletonInstance != null)
			return singletonInstance;

		singletonInstance = new UCIEngineService();
		return singletonInstance;
	}

	protected Map<String, UCIEngine> userNameToEngine = new HashMap<String, UCIEngine>();

	private UCIEngineService() {
		loadEngines();
		serviceCreated = true;
	}

	public void deleteConfiguration(String userName) {
		synchronized (userNameToEngine) {
			try {
				UCIEngine engine = userNameToEngine.get(userName);
				if (engine != null) {
					engine.quit();

					new File(Raptor.ENGINES_DIR + "/" + userName
							+ ".properties").delete();
					userNameToEngine.remove(userName);
				}
			} catch (Throwable t) {
				Raptor.getInstance().onError(
						"Error deleting " + Raptor.ENGINES_DIR + "/" + userName
								+ ".properties", t);
			}
		}
	}

	public void dispose() {
		disconnectAll();
	}

	public UCIEngine getDefaultEngine() {
		synchronized (userNameToEngine) {
			UCIEngine result = null;
			UCIEngine[] engines = getUCIEngines();

			for (UCIEngine engine : engines) {
				if (engine.isDefault()) {
					result = engine;
					break;
				}
			}

			if (result == null && engines.length > 0) {
				result = engines[0];
			}
			return result;
		}
	}

	public UCIEngine getUCIEngine(String name) {
		synchronized (userNameToEngine) {
			return userNameToEngine.get(name);
		}
	}

	public UCIEngine[] getUCIEngines() {
		synchronized (userNameToEngine) {
			return userNameToEngine.values().toArray(new UCIEngine[0]);
		}
	}

	public void saveConfiguration(UCIEngine engine) {
		synchronized (userNameToEngine) {
			FileOutputStream fileOut = null;
			try {
				Properties properties = uciEngineToProperties(engine);
				properties.store(fileOut = new FileOutputStream(
						Raptor.ENGINES_DIR + "/" + engine.getUserName()
								+ ".properties"), "Generated in Raptor");
				userNameToEngine.put(engine.getUserName(), engine);
			} catch (IOException ioe) {
				Raptor.getInstance().onError("Error saving engine: " + engine,
						ioe);
			} finally {
				try {
					fileOut.flush();
					fileOut.close();
				} catch (Throwable t) {
				}
			}
		}
	}

	protected void disconnectAll() {
		for (UCIEngine engine : getUCIEngines()) {
			engine.quit();
		}
	}

	protected void loadEngines() {

		long startTime = System.currentTimeMillis();
		LOG.info("Initiailizing UCIEngineService.");
		int count = 0;

		userNameToEngine.clear();

		File engineProperties = new File(Raptor.ENGINES_DIR);
		File[] files = engineProperties.listFiles(new FilenameFilter() {

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
					
					if (properties.getProperty("isUCI").equals("false"))
						continue;
					
					UCIEngine engine = uciEngineFromProperties(properties);
					userNameToEngine.put(engine.getUserName(), engine);
					count++;
				} catch (IOException ioe) {
					Raptor.getInstance().onError(
							"Error loading file " + file.getName() + ",ioe");
				} finally {
					try {
						fileIn.close();
					} catch (Throwable t) {
					}
				}
			}
		}
		LOG.info("Loaded " + count + " UCIEngines in "
				+ (System.currentTimeMillis() - startTime));

	}

	protected UCIEngine uciEngineFromProperties(Properties properties) {
		UCIEngine engine = new UCIEngine();
		engine.setDefault(properties.getProperty("isDefault").equals("true"));
		
		String fr = properties.getProperty("chess960");
		engine.setSupportsFischerRandom(fr != null && fr.equals("true"));
		
		engine.setMultiplyBlackScoreByMinus1(properties
                .contains("multiplyBlackScoreByMinus1") && properties
                .getProperty("multiplyBlackScoreByMinus1").equals("true"));
		engine.setProcessPath(properties.getProperty("processPath"));
		engine.setUserName(properties.getProperty("userName"));
		engine.setGoAnalysisParameters(properties
				.getProperty("goAnalysisParams"));
		String parameters = properties.getProperty("parameters");
		if (StringUtils.isNotBlank(parameters)) {
			engine.setParameters(RaptorStringUtils.stringArrayFromString(
					parameters, '@'));
		}

		// Set the configured values.
		for (Entry<Object, Object> prop : properties.entrySet()) {
			String keyString = (String) prop.getKey();
			if (!keyString.equals("isUCI") && !keyString.equals("processPath")
					&& !keyString.equals("isDefault")
					&& !keyString.equals("goAnalysisParams")
					&& !keyString.equals("userName")
					&& !keyString.equals("multiplyBlackScoreByMinus1")
					&& !keyString.equals("parameters")) {
				engine.setOverrideOption(keyString, String.valueOf(prop.getValue()));
			}
		}
		return engine;
	}

	protected Properties uciEngineToProperties(UCIEngine engine) {
		Properties properties = new Properties();

		if (!engine.isConnected()) {
			if (!engine.connect()) {
				throw new RuntimeException("Could not connect to engine.");
			}
		}

		properties.put("isUCI", "" + true);
		properties.put("chess960", String.valueOf(engine.supportsFischerRandom()));
		properties.put("isDefault", String.valueOf(engine.isDefault()));
		properties.put("processPath", engine.getProcessPath());
		properties.put("userName", engine.getUserName());
		properties.put("goAnalysisParams", engine.getGoAnalysisParameters());
		properties.put("multiplyBlackScoreByMinus1", String.valueOf(engine.isMultiplyBlackScoreByMinus1()));
		if (engine.getParameters() == null || engine.getParameters().length > 0) {
			properties.put("parameters", RaptorStringUtils.toDelimitedString(
					engine.getParameters(), "@"));
		}
		for (String optionName : engine.getOverrideOptionNames()) {
			UCIOption option = engine.getOption(optionName);
			if (option == null) {
				LOG.warn("Could not find UCIOption for override property: "
						+ optionName);
				continue;
			}
			if (!(option instanceof UCIButton) && !option.isDefaultValue()) {
				properties.put(option.getName(), engine
						.getOverrideOption(optionName));
			}
		}
		return properties;
	}

	public boolean containsFischerRandomEngines() {		
		for (UCIEngine eng: getUCIEngines()) {	
			if (eng.supportsFischerRandom())
				return true;
		}
		return false;
	}

	public UCIEngine[] getFrUCIEngines() {
		List<UCIEngine> engines = new ArrayList<UCIEngine>();
		for (UCIEngine eng: getUCIEngines()) {	
			if (eng.supportsFischerRandom())
				engines.add(eng);
		}
		return engines.toArray(new UCIEngine[0]);
	}
}
