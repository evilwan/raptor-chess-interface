package raptor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Display;

import raptor.connector.Connector;
import raptor.connector.fics.FicsConnector;
import raptor.game.util.ECOParser;
import raptor.pref.RaptorPreferenceStore;
import raptor.util.FileUtil;

public class App {
	private static final Log LOG = LogFactory.getLog(App.class);
	public static final File DEFAULT_HOME_DIR = new File("defaultHomeDir/");
	public static final String APP_HOME_DIR = ".raptor/";

	private static App instance;

	private Connector ficsConnector;
	private RaptorPreferenceStore preferences;

	public static void main(String args[]) {

		Display display = new Display();
		createInstance();
		AppWindow window = new AppWindow();
		window.setBlockOnOpen(true);
		window.open();
		display.dispose();
	}

	public App() {
		preferences = new RaptorPreferenceStore();
		ficsConnector = new FicsConnector();
		ficsConnector.setPreferences(preferences);
	}
	
	public static void createInstance() {
		instance = new App();
	}

	public static App getInstance() {
		return instance;
	}

	public Connector getFicsConnector() {
		return ficsConnector;
	}

	public static File getRaptorUserDir() {
		return new File(System.getProperty("user.home") + "/" + APP_HOME_DIR);
	}

	public RaptorPreferenceStore getPreferences() {
		return preferences;
	}

	public void install() {
		File raptorHome = getRaptorUserDir();
		if (!raptorHome.exists()) {
			LOG.info("Copying default homw directory to "
					+ getRaptorUserDir().getAbsolutePath());
			try {
				FileUtil.copyFiles(DEFAULT_HOME_DIR, getRaptorUserDir());
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
	}
}
