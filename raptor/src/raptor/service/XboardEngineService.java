package raptor.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import raptor.Raptor;
import raptor.chess.Variant;
import raptor.engine.xboard.XboardEngine;
import raptor.util.RaptorLogger;

public class XboardEngineService {
	private static final RaptorLogger LOG = RaptorLogger
			.getLog(XboardEngineService.class);
	
	public static XboardEngineService singletonInstance;
	public static boolean serviceCreated = false;
	
	protected Map<String, XboardEngine> nameToEngine = new HashMap<String, XboardEngine>();

	public static XboardEngineService getInstance() {
		if (singletonInstance != null)
			return singletonInstance;

		singletonInstance = new XboardEngineService();
		return singletonInstance;
	}

	private XboardEngineService() {
		loadEngines();
		serviceCreated = true;
	}
	
	public void dispose() {
		disconnectAll();
	}
	
	protected void disconnectAll() {
		for (XboardEngine engine : getXboardEngines()) {
			engine.quit();
		}
	}
	
	public void deleteConfiguration(String userName) {
		synchronized (nameToEngine) {
			try {
				XboardEngine engine = nameToEngine.get(userName);
				if (engine != null) {
					engine.quit();

					new File(Raptor.ENGINES_DIR + "/" + userName
							+ ".properties").delete();
					nameToEngine.remove(userName);
				}
			} catch (Throwable t) {
				Raptor.getInstance().onError(
						"Error deleting " + Raptor.ENGINES_DIR + "/" + userName
								+ ".properties", t);
			}
		}
	}

	private void loadEngines() {

		long startTime = System.currentTimeMillis();
		LOG.info("Initiailizing XboardEngineService.");
		int count = 0;

		nameToEngine.clear();

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
					
					if (properties.getProperty("isUCI").equals("true"))
						continue;
					
					XboardEngine engine = xboardEngineFromProperties(properties);
					nameToEngine.put(engine.getEngineName(), engine);
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
		LOG.info("Loaded " + count + " XboardEngines in "
				+ (System.currentTimeMillis() - startTime));
	}
	
	protected XboardEngine xboardEngineFromProperties(Properties properties) {
		XboardEngine engine = new XboardEngine();
		engine.setProcessPath(properties.getProperty("processPath"));
		engine.setEngineName(properties.getProperty("userName"));
		engine.setDefault(properties.getProperty("isDefault").equals("true"));
		engine.setSupportedVariants(properties.getProperty("variants"));

		return engine;
	}
	
	public void saveConfiguration(XboardEngine engine) {
		synchronized (nameToEngine) {
			FileOutputStream fileOut = null;
			try {
				Properties properties = xboardEngineToProperties(engine);
				properties.store(fileOut = new FileOutputStream(
						Raptor.ENGINES_DIR + "/" + engine.getEngineName()
								+ ".properties"), "Generated in Raptor");
				nameToEngine.put(engine.getEngineName(), engine);
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

	private Properties xboardEngineToProperties(XboardEngine engine) {
		Properties properties = new Properties();

		if (!engine.isConnected()) {
			if (!engine.connect()) {
				throw new RuntimeException("Could not connect to engine.");
			}
		}
		
		properties.put("isUCI", "false");
		properties.put("isDefault", "" + engine.isDefault());
		properties.put("processPath", engine.getProcessPath());
		properties.put("userName", engine.getEngineName());
		properties.put("variants", engine.supportedVariantsInString());
		
		return properties;
	}

	public XboardEngine getXboardEngine(String name) {
		synchronized (nameToEngine) {
			return nameToEngine.get(name);
		}
	}

	public XboardEngine[] getXboardEngines() {
		synchronized (nameToEngine) {
			return nameToEngine.values().toArray(new XboardEngine[0]);
		}
	}

	public XboardEngine getDefaultEngine() {
		synchronized (nameToEngine) {
			XboardEngine result = null;
			XboardEngine[] engines = getXboardEngines();

			for (XboardEngine engine : engines) {
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

	public boolean hasEnginesSupportingVariant(Variant variant) {
		for (XboardEngine eng: getXboardEngines()) {
			if (eng.doesSupportVariant(variant))
				return true;
		}
		return false;
	}

}
