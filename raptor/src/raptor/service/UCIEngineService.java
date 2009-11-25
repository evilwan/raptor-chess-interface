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

import raptor.Raptor;
import raptor.engine.uci.UCIEngine;
import raptor.engine.uci.UCIOption;
import raptor.engine.uci.options.UCIButton;
import raptor.util.RaptorStringUtils;

public class UCIEngineService {
	public static final UCIEngineService singletonInstance = new UCIEngineService();

	protected Map<String, UCIEngine> userNameToEngine = new HashMap<String, UCIEngine>();

	public static UCIEngineService getInstance() {
		return singletonInstance;
	}

	public UCIEngineService() {
		loadEngines();
	}

	public void deleteConfiguration(String userName) {
		try {
			new File(Raptor.ENGINES_DIR + "/" + userName + ".properties")
					.delete();
			disconnectAll();
			loadEngines();
		} catch (Throwable t) {
			Raptor.getInstance().onError(
					"Error deleting " + Raptor.ENGINES_DIR + "/" + userName
							+ ".properties", t);
		}
	}

	public void dispose() {
		disconnectAll();
	}

	public UCIEngine getDefaultEngine() {
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

	public UCIEngine getUCIEngine(String name) {
		return userNameToEngine.get(name);
	}

	public UCIEngine[] getUCIEngines() {
		return userNameToEngine.values().toArray(new UCIEngine[0]);
	}

	public void saveConfiguration(UCIEngine engine) {
		try {
			Properties properties = uciEngineToProperties(engine);
			properties.store(new FileOutputStream(Raptor.ENGINES_DIR + "/"
					+ engine.getUserName() + ".properties"),
					"Generated in Raptor");
			disconnectAll();
			loadEngines();
		} catch (IOException ioe) {
			Raptor.getInstance().onError("Error saving engine: " + engine, ioe);
		}
	}

	protected void disconnectAll() {
		for (UCIEngine engine : getUCIEngines()) {
			engine.disconnect();
		}
	}

	protected void loadEngines() {
		userNameToEngine.clear();

		File engineProperties = new File(Raptor.ENGINES_DIR);
		File[] files = engineProperties.listFiles(new FilenameFilter() {

			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".properties");
			}
		});

		if (files != null) {
			for (File file : files) {
				try {
					Properties properties = new Properties();
					properties.load(new FileInputStream(file));
					UCIEngine engine = uciEngineFromProperties(properties);
					userNameToEngine.put(engine.getUserName(), engine);
				} catch (IOException ioe) {
					Raptor.getInstance().onError(
							"Error loading file " + file.getName() + ",ioe");
				}
			}
		}
	}

	protected UCIEngine uciEngineFromProperties(Properties properties) {
		UCIEngine engine = new UCIEngine();
		engine.setDefault(properties.getProperty("isDefault").equals("true"));
		engine.setProcessPath(properties.getProperty("processPath"));
		engine.setUserName(properties.getProperty("userName"));
		String parameters = properties.getProperty("parameters");
		if (StringUtils.isNotBlank(parameters)) {
			engine.setParameters(RaptorStringUtils.stringArrayFromString(
					parameters, '@'));
		}

		if (!engine.connect()) {
			throw new RuntimeException("Could not connect to engine: "
					+ engine.getProcessPath());
		}

		// Set the configured values.
		for (Object key : properties.keySet()) {
			String keyString = (String) key;
			if (!keyString.equals("isUCI") && !keyString.equals("processPath")
					&& !keyString.equals("userName")
					&& !keyString.equals("parameters")) {
				UCIOption option = engine.getOption(keyString);
				if (option != null) {
					option.setValue(properties.getProperty(keyString));
				}
			}
		}

		engine.disconnect();
		return engine;
	}

	protected Properties uciEngineToProperties(UCIEngine engine) {
		Properties properties = new Properties();

		if (!engine.isConnected()) {
			if (!engine.connect()) {
				throw new RuntimeException("Couldnt connect to engine.");
			}
		}

		properties.put("isUCI", "" + true);
		properties.put("isDefault", "" + engine.isDefault());
		properties.put("processPath", engine.getProcessPath());
		properties.put("userName", engine.getUserName());
		if (engine.getParameters() == null || engine.getParameters().length > 0) {
			properties.put("parameters", RaptorStringUtils.toDelimitedString(
					engine.getParameters(), "@"));
		}
		for (String optionName : engine.getOptionNames()) {
			UCIOption option = engine.getOption(optionName);
			if (!(option instanceof UCIButton) && !option.isDefaultValue()) {
				properties.put(option, option.getValue());
			}
		}
		return properties;

	}

}
