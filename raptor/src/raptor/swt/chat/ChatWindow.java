package raptor.swt.chat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.swt.chat.controller.MainController;

public class ChatWindow extends ApplicationWindow {
	private static final Log LOG = LogFactory.getLog(ChatWindow.class);

	public static void main(String[] args) throws Exception {
		Display display = new Display();
		Raptor.createInstance();
		final Raptor app = Raptor.getInstance();
		ChatWindow window = new ChatWindow();

		// app.getFicsConnector().getPreferences().setValue(
		// PreferenceKeys.FICS_SERVER_URL, "dev.chess.sipay.ru");
		// app.getFicsConnector().getPreferences().setValue(
		// PreferenceKeys.FICS_SERVER_URL, "chess.sipay.ru");
		app.getPreferences().setValue(PreferenceKeys.FICS_SERVER_URL,
				"freechess.org");
		app.getPreferences().setValue(PreferenceKeys.FICS_TIMESEAL_ENABLED,
				true);
		app.getPreferences().setValue(PreferenceKeys.FICS_IS_NAMED_GUEST, true);
		app.getPreferences().setValue(PreferenceKeys.FICS_USER_NAME,
				"raptorTester");
		display.timerExec(2000, new Runnable() {
			public void run() {
				app.getFicsConnector().connect();
			}
		});
		window.setBlockOnOpen(true);
		window.open();
		Display.getCurrent().dispose();
	}

	ChatConsole mainConsole = null;

	CTabFolder tabFolder = null;

	public ChatWindow() {
		super(null);
	}

	public void addChannelTab(int channel) {
	}

	public void addDirectTellTab(String personName) {
	}

	@Override
	protected Control createContents(Composite parent) {
		GridLayout gridLayout = new GridLayout(1, false);
		System.out.println("createdContents");

		parent.setLayout(gridLayout);
		mainConsole = new ChatConsole(parent, SWT.NONE);
		ChatConsoleController controller = new MainController(Raptor
				.getInstance().getFicsConnector());
		controller.setChatConsole(mainConsole);
		mainConsole.setController(controller);
		mainConsole.createControls();
		mainConsole.getController().init();
		mainConsole.setLayoutData(new GridData(GridData.FILL_BOTH));

		// tabFolder = new CTabFolder(parent, SWT.BORDER);
		// tabFolder.setVisible(true);

		return mainConsole;
	}

	public void dispose() {
		LOG.info("Disposed ChatWindow ");
	}

	public ChatConsole getTabConsole(int index) {
		return (ChatConsole) tabFolder.getItem(index).getControl();
	}

	@Override
	protected void initializeBounds() {
		getShell().setSize(800, 600);
		getShell().setLocation(0, 0);
	}
}
