package raptor.service;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PreferenceService {
	private static final Log log = LogFactory.getLog(PreferenceService.class);
	
	public static final String COMMON_PROPERTIES = "resources/common/common.properties";

	public static final String BACKGROUND_KEY = "board-background";
	public static final String SET_KEY = "board-set";
	private static final PreferenceService instance = new PreferenceService();

	private PropertiesConfiguration commonConfiguration = null;

	public PreferenceService() {
		try {
			commonConfiguration = new PropertiesConfiguration(COMMON_PROPERTIES);
		} catch (Exception e) {
			log.error("Error reading " + COMMON_PROPERTIES
					+ " reverting to default properties",e);
			commonConfiguration = new PropertiesConfiguration();
			defaultConfig();
			saveConfig();
		}
	}

	public static PreferenceService getInstance() {
		return instance;
	}
	
	public void saveConfig() {
		try
		{
		commonConfiguration.save(COMMON_PROPERTIES);
		}
		catch (Exception e) {
			log.error("Unexpected error saving configuration: ",e);
		}
	}
	
	public void defaultConfig() {
		commonConfiguration.clear();
		commonConfiguration.setProperty(BACKGROUND_KEY, "CrumpledPaper");
		commonConfiguration.setProperty(SET_KEY, "WCN");
	}

	public Configuration getConfig() {
		return commonConfiguration;
	}
}
