package raptor.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;


public class UpdateManager {	

	public static final String APP_HOME_DIR = ".raptor/";
	public static final File USER_RAPTOR_PREF = new File(System
			.getProperty("user.home")
			+ "/" + APP_HOME_DIR + "/raptor.properties");
	
	int appVersionMajor = 98;
	int appVersionMinor = 3;
	int appVersionSubMinor = 0;
	
	public void invokeMain(String args[]) {
	    File f = new File("/home/bodia/Raptor_v98/Raptor.jar");

		try {
			URLClassLoader u = new URLClassLoader(new URL[]{f.toURI().toURL()});
		    Class c = u.loadClass("raptor.Raptor");
		      Method m = c.getMethod("main", new Class[] { args.getClass() });
		      m.setAccessible(true);
		      m.invoke(null, new Object[] { args });
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	boolean isUpdateOn() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(USER_RAPTOR_PREF));
			String currentLine = null;
			while ((currentLine = reader.readLine()) != null) {
				if (currentLine.startsWith("app-update"))
					return Boolean.parseBoolean(currentLine.substring(11));
				else if (currentLine.startsWith("app-version")) {
					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	boolean updateAvailable() {
		return false;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		UpdateManager manager = new UpdateManager();
		if (manager.isUpdateOn()) {
			
		}
		
		manager.invokeMain(args);
	}

}
