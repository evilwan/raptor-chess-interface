package raptor.util;

import raptor.util.RaptorLogger;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

/**
 * A proxy class to interface with Log4j. It will be useful if one would like
 * to disable logging completely at some point, thats why it isn't inherited
 * from Log class.
 */
public class RaptorLogger {
	/**
	 * Forces log4j to check for changes to its properties file and reload them 
	 * every 5 seconds. This must always be called before any other code or it 
	 * will not work.
	 */
	public static void initializeLogger() {
		PropertyConfigurator.configureAndWatch("resources/log4j.properties",
				60000);
	}
	
	public static RaptorLogger getLog(Class<?> clazz) {
		return new RaptorLogger(clazz);
	}
	
	private Log serfLogger;
	
	private RaptorLogger(Class<?> clazz) {
		serfLogger = LogFactory.getLog(clazz);
	}

	public void info(String string, Throwable t) {
		serfLogger.info(string, t);		
	}

	public void error(String string, Throwable t) {
		serfLogger.error(string, t);	
	}
	
	public void debug(String string, Throwable t) {
		serfLogger.debug(string, t);	
	}
	
	public void warn(String string, Throwable t) {
		serfLogger.warn(string, t);	
	}
	
	public void info(String string) {
		serfLogger.info(string);		
	}

	public void error(String string) {
		serfLogger.error(string);	
	}
	
	public void debug(String string) {
		serfLogger.debug(string);	
	}
	
	public void warn(String string) {
		serfLogger.warn(string);	
	}

	public boolean isWarnEnabled() {
		return serfLogger.isWarnEnabled();
	}

	public boolean isDebugEnabled() {
		return serfLogger.isDebugEnabled();
	}

	public boolean isInfoEnabled() {
		return serfLogger.isInfoEnabled();
	}

	public static void releaseAll() {
		LogFactory.releaseAll();		
	}
}
