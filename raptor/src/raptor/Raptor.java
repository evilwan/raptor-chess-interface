package raptor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.service.ConnectorService;
import raptor.service.ThreadService;
import raptor.util.FileUtil;

/**
 * Raptor is a singleton representing the application. It contains methods to
 * get various pieces of the application (e.g. the
 * RaptorWindow,preferences,etc).
 * 
 * This classes main is the application main.
 */
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
	public static final String RESOURCES_COMMON_DIR = "resources/common/";

	private static Raptor instance;

	private static Display display;

	public static void createInstance() {
		instance = new Raptor();
		instance.init();
	}

	/**
	 * Returns the singleton raptor instance.
	 * 
	 * @return
	 */
	public static Raptor getInstance() {
		return instance;
	}

	/**
	 * The applications main method. Takes no arguments.
	 */
	public static void main(String args[]) {
		try {
			display = new Display();

			createInstance();

			instance.raptorWindow = new RaptorWindow();
			instance.raptorWindow.setBlockOnOpen(true);

			// Auto login the connectors.
			Connector[] connectors = ConnectorService.getInstance()
					.getConnectors();
			for (final Connector connector : connectors) {
				ThreadService.getInstance().scheduleOneShot(250,
						new Runnable() {
							public void run() {
								connector.onAutoConnect();
							}
						});
			}

			// Open the app window
			instance.raptorWindow.open();
		} catch (Throwable t) {
			LOG.error("Error occured in main:", t);
		} finally {
			instance.shutdown();
		}
	}

	protected ImageRegistry imageRegistry = new ImageRegistry(Display
			.getCurrent());

	protected FontRegistry fontRegistry = new FontRegistry(Display.getCurrent());

	protected ColorRegistry colorRegistry = new ColorRegistry(Display
			.getCurrent());

	protected RaptorPreferenceStore preferences;

	protected RaptorWindow raptorWindow;

	public Raptor() {
	}

	/**
	 * Displays an alert message centered in the RaptorWindow.
	 */
	public void alert(final String message) {
		getInstance().getRaptorWindow().getShell().getDisplay().asyncExec(
				new Runnable() {
					public void run() {
						MessageDialog
								.openInformation(Raptor.getInstance()
										.getRaptorWindow().getShell(), "Alert",
										message);
					}
				});

	}

	/**
	 * Displays a confirm message centered in the RaptorWindow. If the user
	 * presses yes true is returned, otherwise false.
	 */
	public boolean confirm(final String question) {
		return MessageDialog.openConfirm(Raptor.getInstance().getRaptorWindow()
				.getShell(), "Confirm", question);
	}

	/**
	 * Returns the color registry. All colors should be placed in this registry
	 * so they can be properly managed.
	 */
	public ColorRegistry getColorRegistry() {
		return colorRegistry;
	}

	/**
	 * Returns the font registry. All fonts should be placed in this registry so
	 * they can be properly managed.
	 */
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

	/**
	 * Returns the image registry. All images should be registered in this
	 * registry.
	 * 
	 * @return
	 */
	public ImageRegistry getImageRegistry() {
		return imageRegistry;
	}

	/**
	 * Returns the RaptorPreferenceStore used by the application. All
	 * preferences should be stored and loaded form here.
	 */
	public RaptorPreferenceStore getPreferences() {
		return preferences;
	}

	/**
	 * Returns the RaptorWindow (the main application window.
	 */
	public RaptorWindow getRaptorWindow() {
		return raptorWindow;
	}

	/**
	 * Initializes raptor.
	 */
	private void init() {
		install();
		preferences = new RaptorPreferenceStore();
	}

	/**
	 * Installs raptor. Currently this places everything in the default home
	 * directory in the users raptor directory. This is so new releases will
	 * always take effect.
	 */
	private void install() {
		try {
			FileUtil.copyFiles(DEFAULT_HOME_DIR, USER_RAPTOR_DIR);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	/**
	 * Handles an error in a way the user is notified and can report an issue.
	 * If possible try and use a connectors on error if you have access to one,
	 * otherwise you can use this.
	 */
	public void onError(final String error) {
		onError(error, null);

	}

	/**
	 * Handles an error in a way the user is notified and can report an issue.
	 * If possible try and use a connectors on error if you have access to one,
	 * otherwise you can use this.
	 */
	public void onError(final String error, final Throwable throwable) {
		LOG.error(error, throwable);
		getInstance().getRaptorWindow().getShell().getDisplay().asyncExec(
				new Runnable() {
					public void run() {
						MessageDialog
								.openError(
										Raptor.getInstance().getRaptorWindow()
												.getShell(),
										"Error",
										"Critical error occured! We are trying to make Raptor "
												+ "bug free and we need your help! Please take a moment to report this "
												+ "error at\nhttp://code.google.com/p/raptor-chess-interface/issues/list\n\n Issue: "
												+ error + "\n" + throwable != null ? ExceptionUtils
												.getFullStackTrace(throwable)
												: "");
					}
				});

	}

	/**
	 * Cleanly shuts down raptor. Please use this method instead of System.exit!
	 */
	public void shutdown() {
		try {
			imageRegistry.dispose();
		} catch (Throwable t) {
			LOG.error("Error shutting down raptor", t);
		}

		try {
			ConnectorService.getInstance().dispose();
		} catch (Throwable t) {
			LOG.error("Error shutting down raptor", t);
		}

		try {
			ThreadService.getInstance().dispose();
		} catch (Throwable t) {
			LOG.error("Error shutting down raptor", t);
		}

		try {
			if (raptorWindow != null) {
				raptorWindow.close();
			}
		} catch (Throwable t) {
			LOG.error("Error shutting down raptor", t);
		}

		try {
			if (display != null && !display.isDisposed()) {
				display.dispose();
			}
		} catch (Throwable t) {
			LOG.error("Error shutting down raptor", t);
		}

		LOG.info("Shutdown Raptor");
		System.exit(1);
	}

}
