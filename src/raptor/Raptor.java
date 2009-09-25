package raptor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import raptor.connector.Connector;
import raptor.connector.fics.FicsConnector;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.service.ThreadService;
import raptor.swt.LoginDialog;
import raptor.util.FileUtil;

public class Raptor implements PreferenceKeys {
	private static final Log LOG = LogFactory.getLog(Raptor.class);
	public static final File DEFAULT_HOME_DIR = new File("defaultHomeDir/");
	public static final String APP_HOME_DIR = ".raptor/";

	private static Raptor instance;

	public static void createInstance() {
		instance = new Raptor();
	}

	public static Raptor getInstance() {
		return instance;
	}

	public static File getRaptorUserDir() {
		return new File(System.getProperty("user.home") + "/" + APP_HOME_DIR);
	}

	public static void main(String args[]) {

		try {
			Display display = new Display();
			createInstance();

			if (!instance.getPreferences().getBoolean(FICS_AUTO_CONNECT)) {
				LoginDialog loginDialog = new LoginDialog();
				loginDialog.open();
				if (!loginDialog.wasLoginPressed()) {
					instance.shutdown();
					return;
				}
			}

			instance.appWindow = new RaptorWindow();
			instance.appWindow.setBlockOnOpen(true);

			display.timerExec(500, new Runnable() {
				public void run() {
					Shell shell = instance.appWindow.getShell();
					if (shell == null) {
						System.err.println("Why is shell null");
					} else {
						shell.addListener(SWT.Close, new Listener() {
							public void handleEvent(Event e) {
								Raptor.getInstance().shutdown();
							}
						});
					}
				}
			});

			display.timerExec(1000, new Runnable() {
				public void run() {
					instance.getFicsConnector().connect();
				}
			});
			instance.appWindow.open();

		} catch (Throwable t) {
			LOG.error("Error occured in main:", t);
		} finally {
			try {
				if (!Display.getCurrent().isDisposed()) {
					Display.getCurrent().dispose();
				}
			} catch (Throwable t) {
				LOG.error("Error occured disposing display:", t);
			} finally {
				System.exit(1);
			}
		}
	}

	protected Connector ficsConnector;

	protected RaptorPreferenceStore preferences;

	protected RaptorWindow appWindow;

	public Raptor() {
		preferences = new RaptorPreferenceStore();
		ficsConnector = new FicsConnector();
		ficsConnector.setPreferences(preferences);
	}

	public RaptorWindow getAppWindow() {
		return appWindow;
	}

	public Connector getFicsConnector() {
		return ficsConnector;
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

	public void shutdown() {
		try {
			ficsConnector.dispose();
		} catch (Throwable t) {
		}

		try {
			ThreadService.getInstance().dispose();
		} catch (Throwable t) {

		}

		LOG.info("Shutdown Raptor");
	}
}
