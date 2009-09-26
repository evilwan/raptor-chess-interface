package raptor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
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
	public static final File USER_RAPTOR_DIR = new File(System
			.getProperty("user.home")
			+ "/" + APP_HOME_DIR);
	public static final String USER_RAPTOR_HOME_PATH = USER_RAPTOR_DIR
			.getAbsolutePath();
	public static final String ICONS_DIR = "resources/common/icons/";
	public static final String IMAGES_DIR = "resources/common/images/";

	private static Raptor instance;

	public static void createInstance() {
		instance = new Raptor();
		instance.init();
	}

	public static Raptor getInstance() {
		return instance;
	}

	public static void main(String args[]) {
		Display display = null;
		try {
			display = new Display();

			createInstance();

			instance.appWindow = new RaptorWindow();

			// Create the login dialog if needed.
			if (!instance.getPreferences().getBoolean(FICS_AUTO_CONNECT)) {
				LoginDialog loginDialog = new LoginDialog();
				loginDialog.open();
				if (!loginDialog.wasLoginPressed()) {
					instance.shutdown();
					return;
				}
			}

			instance.appWindow.setBlockOnOpen(true);

			// Add a hook to call shutdown.
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

			// Start the fics connector.
			display.timerExec(1000, new Runnable() {
				public void run() {
					instance.getFicsConnector().connect();
				}
			});

			// Open the app window
			instance.appWindow.open();

		} catch (Throwable t) {
			LOG.error("Error occured in main:", t);
		} finally {
			try {
				if (display != null && !display.isDisposed()) {
					display.dispose();
				}
			} catch (Throwable t) {
				LOG.error("Error occured disposing display:", t);
			} finally {
				System.exit(1);
			}
		}
	}

	protected ImageRegistry imageRegistry = new ImageRegistry(Display
			.getCurrent());

	protected FontRegistry fontRegistry = new FontRegistry(Display.getCurrent());

	protected ColorRegistry colorRegistry = new ColorRegistry(Display
			.getCurrent());

	protected Connector ficsConnector;

	protected RaptorPreferenceStore preferences;

	protected RaptorWindow appWindow;

	public Raptor() {
	}

	public void init() {
		install();
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
		try {
			FileUtil.copyFiles(DEFAULT_HOME_DIR, USER_RAPTOR_DIR);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public ImageRegistry getImageRegistry() {
		return imageRegistry;
	}

	public ColorRegistry getColorRegistry() {
		return colorRegistry;
	}

	public FontRegistry getFontRegistry() {
		return fontRegistry;
	}

	/**
	 * The name of the file in the resources/common/icons directory to load. Do
	 * not append the suffix. All files in this directory end in .png and this
	 * method handles that for you.
	 */
	public Image getIcon(String nameOfFileInIconsWithoutPng) {
		String fileName = ICONS_DIR + nameOfFileInIconsWithoutPng + ".png";
		return getImage(fileName);
	}

	/**
	 * Returns the image with the specified relative path.
	 */
	public Image getImage(String fileName) {
		Image result = imageRegistry.get(fileName);
		if (result == null) {
			try {
				ImageData data = new ImageData(fileName);
				imageRegistry.put(fileName, result = new Image(Display
						.getCurrent(), data));
			} catch (RuntimeException e) {
				LOG.error("Error loading image " + fileName, e);
				throw e;
			}
		}
		return result;
	}

	public void shutdown() {
		try {
			ficsConnector.dispose();
		} catch (Throwable t) {
		}

		try {
			imageRegistry.dispose();
		} catch (Throwable t) {
		}

		try {
			ThreadService.getInstance().dispose();
		} catch (Throwable t) {

		}

		LOG.info("Shutdown Raptor");
	}
}
