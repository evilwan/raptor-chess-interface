package raptor.gui.chat;

import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import raptor.pref.PreferencesDialog;

public class ChatWindow extends ApplicationWindow {
	private static final Log LOG = LogFactory.getLog(ChatWindow.class);

	ChatConsole chatConsole = null;

	public static void main(String[] args) throws Exception {
		final ChatWindow window = new ChatWindow();
		window.getToolBarManager2().add(new Action("Preferences", SWT.BORDER) {
			@Override
			public void run() {
				PreferencesDialog dialog = new PreferencesDialog();
				dialog.run();
			}
		});
		window.getToolBarManager2().add(new Action("AddText", SWT.BORDER) {
			@Override
			public void run() {
				try {
					FileReader fileIn = new FileReader(
							"resources/common/ECO.txt");
					char[] data = new char[1000];
					int charsRead = 0;

					while ((charsRead = fileIn.read(data)) != -1) {
						window.chatConsole.acceptInbound(new String(data, 0,
								charsRead), 0);
					}
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
		});

		window.setBlockOnOpen(true);
		window.open();
		Display.getCurrent().dispose();

	}

	public ChatWindow() {
		super(null);
		addToolBar(SWT.HORIZONTAL);
	}

	@Override
	protected Control createContents(Composite parent) {
		GridLayout gridLayout = new GridLayout(1,false);
		System.out.println("createdContents");
		parent.setLayout(gridLayout);
		chatConsole = new ChatConsole(parent, SWT.NONE);
		chatConsole.setLayoutData(new GridData(GridData.FILL_BOTH));
		return parent;
	}

	public void dispose() {

		LOG.info("Disposed ChatWindow ");
	}

	@Override
	protected void initializeBounds() {
		getShell().setSize(800, 600);
		getShell().setLocation(0, 0);
	}
}
