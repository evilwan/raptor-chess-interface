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
		getShell().setImage(
				Raptor.getInstance().getImage(
						"resources/common/images/raptorIcon.gif"));

		sashForm = new SashForm(parent, SWT.VERTICAL | SWT.SMOOTH);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		chessBoards = new ChessBoards(sashForm, SWT.NONE);
		chatConsoles = new ChatConsoles(sashForm, SWT.NONE);

		chatConsoles.addChatConsole(new MainConsoleController(), Raptor
				.getInstance().getFicsConnector(), false, "Main");

		maximizeChatConsoles();

		setStatus("Sample status message");
		return sashForm;
	}

	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuBar = new MenuManager("Raptor");
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

	public boolean isChatConsolesMaximized() {
		return sashForm.getMaximizedControl() == chatConsoles;
	}

	public boolean isChessBoardssMaximized() {
		return sashForm.getMaximizedControl() == chessBoards;
	}

	public void restore() {
		chatConsoles.restore();
		chessBoards.restore();
		sashForm.setWeights(storedWeights);
		sashForm.setMaximizedControl(null);
		// sashForm.layout(false);
	}

}
