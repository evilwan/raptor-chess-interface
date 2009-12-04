/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.service.ActionScriptService;
import raptor.service.AliasService;
import raptor.service.ChessBoardCacheService;
import raptor.service.ConnectorService;
import raptor.service.EcoService;
import raptor.service.ScriptService;
import raptor.service.SoundService;
import raptor.service.ThreadService;
import raptor.service.UCIEngineService;
import raptor.swt.ChessSetOptimizationDialog;
import raptor.swt.InputDialog;
import raptor.swt.RaptorCursorRegistry;
import raptor.swt.RaptorImageRegistry;
import raptor.swt.chess.ChessBoardUtils;
import raptor.util.BrowserUtils;
import raptor.util.FileUtils;

/**
 * Raptor is a singleton representing the application. It contains methods to
 * get various pieces of the application (e.g. the
 * RaptorWindow,preferences,etc).
 * 
 * This classes main is the application main.
 */
public class Raptor implements PreferenceKeys {

	public static final File DEFAULT_HOME_DIR = new File("defaultHomeDir/");
	public static final String APP_HOME_DIR = ".raptor/";
	public static final File USER_RAPTOR_DIR = new File(System
			.getProperty("user.home")
			+ "/" + APP_HOME_DIR);
	public static final String USER_RAPTOR_HOME_PATH = USER_RAPTOR_DIR
			.getAbsolutePath();
	public static final String ICONS_DIR = "resources/icons/";
	public static final String IMAGES_DIR = "resources/images/";
	public static final String RESOURCES_SCRIPTS = "resources/scripts";
	public static final String RESOURCES_DIR = "resources/";
	public static final String GAMES_PGN_FILE = USER_RAPTOR_HOME_PATH
			+ "/games/raptorGames.pgn";
	public static final String ENGINES_DIR = USER_RAPTOR_HOME_PATH + "/engines";
	private static Raptor instance;
	private static Display display;

	static {
		// Forces log4j to check for changes to its properties file and reload
		// them every 5 seconds.
		// This must always be called before any other code or it will not work.
		PropertyConfigurator.configureAndWatch("resources/log4j.properties",
				5000);
	}

	/**
	 * Don't make this static.
	 */
	private final Log LOG = LogFactory.getLog(Raptor.class);

	protected RaptorImageRegistry imageRegistry = new RaptorImageRegistry(
			Display.getCurrent());

	protected FontRegistry fontRegistry = new FontRegistry(Display.getCurrent());

	protected ColorRegistry colorRegistry = new ColorRegistry(Display
			.getCurrent());

	protected RaptorCursorRegistry cursorRegistry = new RaptorCursorRegistry(
			Display.getCurrent());

	protected RaptorPreferenceStore preferences;

	protected RaptorWindow raptorWindow;

	protected Clipboard clipboard;

	protected boolean isShutdown = false;

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
			Display.setAppName("Raptor");
			display = new Display();

			createInstance();

			display.addListener(SWT.Close, new Listener() {
				public void handleEvent(Event event) {
					getInstance().shutdown();
				}
			});

			instance.raptorWindow = new RaptorWindow();
			instance.raptorWindow.setBlockOnOpen(true);

			// Auto login the connectors.
			Connector[] connectors = ConnectorService.getInstance()
					.getConnectors();
			for (final Connector connector : connectors) {
				// Wait 750 milliseconds so the RaptorWindow has time to be
				// created.
				ThreadService.getInstance().scheduleOneShot(750,
						new Runnable() {
							public void run() {
								connector.onAutoConnect();
							}
						});
			}

			display.timerExec(500, new Runnable() {
				public void run() {
					// Launch the home page after a half second it requires a
					// RaptorWindow.
					if (getInstance().getPreferences().getBoolean(
							APP_IS_LAUNCHNG_HOME_PAGE)) {
						BrowserUtils.openUrl(getInstance().getPreferences()
								.getString(PreferenceKeys.APP_HOME_URL));
					}

					// Initialize this after a half second it requires a
					// RaptorWindow.
					Raptor.getInstance().cursorRegistry.setDefaultCursor(Raptor
							.getInstance().getWindow().getShell().getCursor());

					// Initialize this after a half second. It requires a
					// RaptorWindow.
					ChessBoardCacheService.getInstance();

					// Initialize the UCIEngineService after a half second.
					// Requires a raptor window in case there is an error.
					UCIEngineService.getInstance();

					String currentChessSet = getInstance().getPreferences()
							.getString(PreferenceKeys.BOARD_CHESS_SET_NAME);

					// Prompt for set optimization if set is not optimized..
					if (!ChessBoardUtils.isChessSetOptimized(currentChessSet)
							&& Raptor
									.getInstance()
									.confirm(
											"Your current chess set "
													+ currentChessSet
													+ " is not optimized. "
													+ "It is strongly suggested that you optimize your current chess sets. "
													+ "It will boost Raptor performance."
													+ "Would you like to optimize?")) {
						ChessSetOptimizationDialog dialog = new ChessSetOptimizationDialog(
								Raptor.getInstance().getWindow().getShell(),
								"Chess Set " + currentChessSet
										+ " Optimization.", currentChessSet);
						dialog.open();

					}
				}
			});

			// Open the app window
			instance.raptorWindow.open();
		} catch (Throwable t) {
			instance.LOG
					.error(
							"Error occured in main: (If this is a widget is disposed error just ignore it its nothing)",
							t);
		} finally {
			if (instance != null) {
				instance.shutdown();
			}
		}
	}

	public Raptor() {
		clipboard = new Clipboard(getDisplay());
	}

	/**
	 * Displays an alert message centered in the RaptorWindow.
	 */
	public void alert(final String message) {
		if (!isDisposed()) {
			getInstance().getWindow().getShell().getDisplay().asyncExec(
					new Runnable() {
						public void run() {
							MessageDialog.openInformation(Raptor.getInstance()
									.getWindow().getShell(), "Alert", message);
						}
					});
		}
	}

	/**
	 * Displays a confirm message centered in the RaptorWindow. If the user
	 * presses yes true is returned, otherwise false.
	 */
	public boolean confirm(final String question) {
		if (!isDisposed()) {
			return MessageDialog.openConfirm(Raptor.getInstance().getWindow()
					.getShell(), "Confirm", question);
		}
		return false;
	}

	public Clipboard getClipboard() {
		return clipboard;
	}

	/**
	 * Returns the color registry. All colors should be placed in this registry
	 * so they can be properly managed.
	 */
	public ColorRegistry getColorRegistry() {
		return colorRegistry;
	}

	/**
	 * Returns the cursor registry. All cursors should be placed in this
	 * registry so they can be properly managed.
	 */
	public RaptorCursorRegistry getCursorRegistry() {
		return cursorRegistry;
	}

	public Display getDisplay() {
		return display;
	}

	/**
	 * Returns the font registry. All fonts should be placed in this registry so
	 * they can be properly managed.
	 */
	public FontRegistry getFontRegistry() {
		return fontRegistry;
	}

	/**
	 * The name of the file in the resources/icons directory to load. Do not
	 * append the suffix. All files in this directory end in .png and this
	 * method handles that for you.
	 */
	public Image getIcon(String nameOfFileInIconsWithoutPng) {
		String iconSize = getPreferences().getString(
				PreferenceKeys.APP_ICON_SIZE);
		String fileName = ICONS_DIR + "/" + iconSize + "/"
				+ nameOfFileInIconsWithoutPng + ".png";
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
	public RaptorImageRegistry getImageRegistry() {
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
	public RaptorWindow getWindow() {
		return raptorWindow;
	}

	public boolean isDisposed() {
		return isShutdown || getInstance() == null
				|| getInstance().getWindow() == null
				|| getInstance().getWindow().getShell() != null
				&& getInstance().getWindow().getShell().isDisposed();
	}

	public boolean isShutdown() {
		return isShutdown;
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
		if (!isDisposed()) {
			getInstance().getWindow().getShell().getDisplay().asyncExec(
					new Runnable() {
						public void run() {
							MessageDialog
									.openError(
											Raptor.getInstance().getWindow()
													.getShell(),
											"Error",
											"Critical error occured! We are trying to make Raptor "
													+ "bug free and we need your help! Please take a moment to report this "
													+ "error at\nhttp://code.google.com/p/raptor-chess-interface/issues/list\n\n Issue: "
													+ error
													+ "\n"
													+ (throwable != null ? ExceptionUtils
															.getMessage(throwable)
															: ""));
						}
					});
		}

	}

	/**
	 * Prompts a user for the answer to a question. The user enters text. The
	 * text the user entered is returned.
	 */
	public String promptForText(final String question) {
		if (!isDisposed()) {
			InputDialog dialog = new InputDialog(Raptor.getInstance()
					.getWindow().getShell(), "Enter Text", question);
			return dialog.open();
		} else {
			return null;
		}
	}

	/**
	 * Prompts a user for the answer to a question. The user enters text. The
	 * text the user entered is returned.
	 * 
	 * @answer the initial text to place in the users answer.
	 */
	public String promptForText(final String question, String answer) {
		if (!isDisposed()) {
			InputDialog dialog = new InputDialog(Raptor.getInstance()
					.getWindow().getShell(), "Enter Text", question);
			if (answer != null) {
				dialog.setInput(answer);
			}
			return dialog.open();
		} else {
			return null;
		}
	}

	public void setClipboard(Clipboard clipboard) {
		this.clipboard = clipboard;
	}

	/**
	 * Cleanly shuts down raptor. Please use this method instead of System.exit!
	 */
	public void shutdown() {
		shutdown(false);
	}

	/**
	 * Cleanly shuts down raptor. Please use this method instead of System.exit!
	 */
	public void shutdown(boolean isIgnoringPreferenceSaves) {
		if (isShutdown()) {
			return;
		}
		isShutdown = true;

		if (clipboard != null) {
			clipboard.dispose();
		}

		if (!isIgnoringPreferenceSaves) {
			getPreferences().save();
		}

		try {
			ConnectorService.getInstance().dispose();
		} catch (Throwable t) {
			LOG.warn("Error shutting down ConnectorService", t);
		}

		try {
			EcoService.getInstance().dispose();
		} catch (Throwable t) {
			LOG.warn("Error shutting down EcoService", t);
		}

		try {
			SoundService.getInstance().dispose();
		} catch (Throwable t) {
			LOG.warn("Error shutting down SoundService", t);
		}

		try {
			ChessBoardCacheService.getInstance().dispose();
		} catch (Throwable t) {
			LOG.warn("Error shutting ChessBoardCacheService", t);
		}

		try {
			UCIEngineService.getInstance().dispose();
		} catch (Throwable t) {
			LOG.warn("Error shutting UCIEngineService", t);
		}

		try {
			ThreadService.getInstance().dispose();
		} catch (Throwable t) {
			LOG.warn("Error shutting down ThreadService", t);
		}

		try {
			ActionScriptService.getInstance().dispose();
		} catch (Throwable t) {
			LOG.warn("Error shutting down ActionService", t);
		}

		try {
			ScriptService.getInstance().dispose();
		} catch (Throwable t) {
			LOG.warn("Error shutting down ScriptService", t);
		}

		try {
			AliasService.getInstance().dispose();
		} catch (Throwable t) {
			LOG.warn("Error shutting down AliasService", t);
		}

		try {
			if (raptorWindow != null && !raptorWindow.getShell().isDisposed()) {
				raptorWindow.close();
			}
		} catch (Throwable t) {
			// LOG.warn("Error shutting down raptor window", t);
		}

		try {
			if (display != null && !display.isDisposed()) {
				display.dispose();
			}
		} catch (Throwable t) {
			// Eat this one its prob an already disposed exception.
			// LOG.warn("Error shutting down display", t);
		}

		LOG.info("Shutdown Raptor");
		System.exit(1);
	}

	/**
	 * Initializes raptor.
	 */
	private void init() {
		preferences = new RaptorPreferenceStore();

		install();

		// Make sure all of the Singleton services get loaded.
		ThreadService.getInstance();
		EcoService.getInstance();
		ConnectorService.getInstance();
		SoundService.getInstance();
		ScriptService.getInstance();
		ActionScriptService.getInstance();
		UCIEngineService.getInstance();
		AliasService.getInstance();
	}

	/**
	 * Installs raptor. Currently this places everything in the default home
	 * directory in the users raptor directory. This is so new releases will
	 * always take effect.
	 */
	private void install() {
		try {
			FileUtils.copyFiles(DEFAULT_HOME_DIR, USER_RAPTOR_DIR);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

}
