package raptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import raptor.game.Game;
import raptor.pref.PreferenceKeys;
import raptor.pref.PreferencesDialog;
import raptor.swt.ProfileWIndow;
import raptor.swt.chat.ChatConsoles;
import raptor.swt.chat.controller.MainConsoleController;
import raptor.swt.chess.ChessBoards;

public class RaptorWindow extends ApplicationWindow {
	Log LOG = LogFactory.getLog(RaptorWindow.class);

	ChessBoards chessBoards;
	ChatConsoles chatConsoles;
	public SashForm sashForm;
	Game game;
	int[] storedWeights = new int[] { 60, 40 };

	public RaptorWindow() {
		super(null);

		addMenuBar();
		// addStatusLine();
		// addCoolBar(SWT.FLAT);
	}

	@Override
	protected Control createContents(Composite parent) {
		getShell().setText(
				Raptor.getInstance().getPreferences().getString(
						PreferenceKeys.APP_NAME));

		sashForm = new SashForm(parent, SWT.VERTICAL | SWT.SMOOTH);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		chessBoards = new ChessBoards(sashForm, SWT.NONE);
		chatConsoles = new ChatConsoles(sashForm, SWT.NONE);

		// Game game = GameUtils.createStartingPosition(Game.STANDARD);
		// game.setId("1");
		// game.setState(Game.ACTIVE_STATE | Game.IS_CLOCK_TICKING_STATE);
		// game.setWhiteName("White");
		// game.setWhiteRating("----");
		// game.setBlackName("Black");
		// game.setBlackRating("----");
		// game.setBlackRemainingTimeMillis(60000 * 3);
		// game.setWhitRemainingeTimeMillis(60000 * 3);
		// game.setInitialBlackIncMillis(0);
		// game.setInitialBlackTimeMillis(60000 * 3);
		// game.setInitialWhiteIncMillis(0);
		// game.setInitialWhiteTimeMillis(60000 * 3);
		// game.setWhiteLagMillis(3567L);
		// game.setBlackLagMillis(29876L);
		// game.setResultDescription("");
		// game.setGameDescription("Playing (Game 123) 3 3 blitz");
		// game.setSettingMoveSan(true);
		//
		// chessBoards.add(game, new FreeFormController(), Raptor.getInstance()
		// .getFicsConnector(), game.getId(), true);
		chatConsoles.addChatConsole(new MainConsoleController(), Raptor
				.getInstance().getFicsConnector(), false, "Main");

		// chatConsole = new ChatConsole(sashForm, SWT.NONE);
		// chatConsole.setController(new MainConsoleController(chatConsole));
		// chatConsole.setPreferences(App.getInstance().getPreferences());
		// chatConsole.setConnector(App.getInstance().getFicsConnector());
		// chatConsole.createControls();
		// chatConsole.getController().init();
		// chatConsole.setLayoutData(new GridData(GridData.FILL_BOTH));

		sashForm.setWeights(storedWeights);

		setStatus("Sample status message");
		return sashForm;
	}

	// protected StatusLineManager createStatusLineManager() {
	// StatusLineManager slm = new StatusLineManager();
	// slm.setMessage("Lag 34ms");
	// return slm;
	// }
	//
	// protected CoolBarManager createCoolBarManager(int style) {
	// CoolBarManager coolbarManager = new CoolBarManager(style);
	// coolbarManager.add(new Action("Test") {
	// public void run() {
	// LOG.info("Test running");
	// }
	// });
	// return coolbarManager;
	// }

	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuBar = new MenuManager("");
		MenuManager connectionsMenu = new MenuManager("&Connections");
		MenuManager configureMenu = new MenuManager("&Configure");
		MenuManager windowMenu = new MenuManager("&Window");
		MenuManager helpMenu = new MenuManager("&Help");

		connectionsMenu.add(new Action("Connect to &fics") {
			@Override
			public void run() {
			}
		});
		connectionsMenu.add(new Action("Profile") {
			@Override
			public void run() {
				ProfileWIndow profiler = new ProfileWIndow();
				profiler.setBlockOnOpen(false);
				profiler.open();
			}
		});
		configureMenu.add(new Action("Preferences") {
			@Override
			public void run() {
				new PreferencesDialog().run();
			}
		});
		helpMenu.add(new Action("&About") {
			@Override
			public void run() {
			}
		});
		windowMenu.add(new Action("&Cascade") {
			@Override
			public void run() {
			}
		});

		menuBar.add(connectionsMenu);
		menuBar.add(configureMenu);
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);
		return menuBar;
	}

	@Override
	protected void initializeBounds() {
		Rectangle fullScreenBounds = Display.getCurrent().getPrimaryMonitor()
				.getBounds();
		getShell().setSize(fullScreenBounds.width, fullScreenBounds.height);
		getShell().setLocation(0, 0);
	}

	public void maximizeChatConsoles() {
		if (!chessBoards.isMaximized() && !chatConsoles.isMaximized()) {
			storedWeights = sashForm.getWeights();
		}
		chatConsoles.maximize();
		sashForm.setMaximizedControl(chatConsoles);
	}

	public void maximizeChessBoards() {
		if (!chessBoards.isMaximized() && !chatConsoles.isMaximized()) {
			storedWeights = sashForm.getWeights();
		}
		chessBoards.maximize();
		sashForm.setMaximizedControl(chessBoards);
	}

	public void restore() {
		chatConsoles.restore();
		chessBoards.restore();
		sashForm.setWeights(storedWeights);
		sashForm.setMaximizedControl(null);
		// sashForm.layout(false);
	}

}
