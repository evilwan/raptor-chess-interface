package raptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import raptor.game.Game;
import raptor.game.util.GameUtils;
import raptor.pref.PreferencesDialog;
import raptor.swt.chess.ChessBoard;
import raptor.swt.chess.ChessBoardResources;
import raptor.swt.chess.controller.FreeFormController;
import raptor.swt.chess.layout.RightOrientedLayout;

public class AppWindow extends ApplicationWindow {
	Log LOG = LogFactory.getLog(AppWindow.class);

	ChessBoard board;
	Game game;

	public AppWindow() {
		super(null);

		game = GameUtils.createStartingPosition();
		game.setId("1");
		game.setType(Game.BLITZ);
		game.setState(Game.ACTIVE_STATE | Game.IS_CLOCK_TICKING_STATE);
		game.setWhiteName("AReallyLongName");
		game.setWhiteRating("1934");
		game.setBlackName("shorty");
		game.setBlackRating("----");
		game.setBlackRemainingTimeMillis(60000 * 3);
		game.setWhitRemainingeTimeMillis(60000 * 3);
		game.setInitialBlackIncMillis(0);
		game.setInitialBlackTimeMillis(60000 * 3);
		game.setInitialWhiteIncMillis(0);
		game.setInitialWhiteTimeMillis(60000 * 3);
		game.setWhiteLagMillis(3567L);
		game.setBlackLagMillis(29876L);
		game.setResultDescription("");
		game.setGameDescription("Playing (Game 123) 3 3 blitz");
		game.setSettingMoveSan(true);

		addMenuBar();
		addStatusLine();
		addCoolBar(SWT.FLAT);
	}

	protected MenuManager createMenuManager() {
		MenuManager menuBar = new MenuManager("");
		MenuManager connectionsMenu = new MenuManager("&Connections");
		MenuManager configureMenu = new MenuManager("&Configure");
		MenuManager windowMenu = new MenuManager("&Window");
		MenuManager helpMenu = new MenuManager("&Help");

		connectionsMenu.add(new Action("Connect to &fics") {
			public void run() {
			}
		});
		configureMenu.add(new Action("Preferences") {
			public void run() {
				new PreferencesDialog().run();
			}
		});
		helpMenu.add(new Action("&About") {
			public void run() {
			}
		});
		windowMenu.add(new Action("&Cascade") {
			public void run() {
			}
		});

		menuBar.add(connectionsMenu);
		menuBar.add(configureMenu);
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);
		return menuBar;
	}

	protected StatusLineManager createStatusLineManager() {
		StatusLineManager slm = new StatusLineManager();
		slm.setMessage("Lag 34ms");
		return slm;
	}

	protected CoolBarManager createCoolBarManager(int style) {
		CoolBarManager coolbarManager = new CoolBarManager(style);
		coolbarManager.add(new Action("Test") {
			public void run() {
				LOG.info("Test running");
			}
		});
		return coolbarManager;
	}

	@Override
	protected Control createContents(Composite parent) {
		getShell().setText("Raptor vAlpha1");
		board = new ChessBoard(parent, SWT.NONE);
		board.setGame(game);
		board.setConnector(App.getInstance().getFicsConnector());
		board.setController(new FreeFormController(board));
		board.setBoardLayout(new RightOrientedLayout(board));
		board.setPreferences(App.getInstance().getPreferences());
		board.setResources(new ChessBoardResources(board));
		board.createControls();
		board.setLayoutData(new GridData(GridData.FILL_BOTH));
		board.getController().adjustToGameInitial();
		setStatus("Sample status message");
		return board;
	}

	@Override
	protected void initializeBounds() {
		getShell().setSize(800, 600);
		getShell().setLocation(0, 0);
	}

}
