package raptor.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.engine.uci.UCIEngine;
import raptor.engine.uci.UCIOption;
import raptor.engine.uci.options.UCIButton;
import raptor.util.RaptorStringUtils;

public class UCIEngineService {
	private static final Log LOG = LogFactory.getLog(UCIEngineService.class);

	public static final UCIEngineService singletonInstance = new UCIEngineService();

	protected Map<String, UCIEngine> userNameToEngine = new HashMap<String, UCIEngine>();

	public static UCIEngineService getInstance() {
		return singletonInstance;
	}

	private UCIEngineService() {
		loadEngines();
	}

	public void deleteConfiguration(String userName) {
		synchronized (userNameToEngine) {
			try {
				UCIEngine engine = userNameToEngine.get(userName);
				if (engine != null) {
					engine.disconnect();

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
			engine.disconnect();
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
		engine.setMultiplyBlackScoreByMinus1(properties
				.contains("multiplyBlackScoreByMinus1") ? properties
				.getProperty("multiplyBlackScoreByMinus1").equals("true")
				: false);
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
		for (Object key : properties.keySet()) {
			String keyString = (String) key;
			if (!keyString.equals("isUCI") && !keyString.equals("processPath")
					&& !keyString.equals("isDefault")
					&& !keyString.equals("goAnalysisParams")
					&& !keyString.equals("userName")
					&& !keyString.equals("multiplyBlackScoreByMinus1")
					&& !keyString.equals("parameters")) {
				engine.setOverrideOption(keyString, "" + properties.get(key));
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
		properties.put("isDefault", "" + engine.isDefault());
		properties.put("processPath", engine.getProcessPath());
		properties.put("userName", engine.getUserName());
		properties.put("goAnalysisParams", engine.getGoAnalysisParameters());
		properties.put("multiplyBlackScoreByMinus1", ""
				+ engine.isMultiplyBlackScoreByMinus1());
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
}
