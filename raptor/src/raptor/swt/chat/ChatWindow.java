package raptor.swt.chat;

import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;

import raptor.pref.PreferencesDialog;
import raptor.service.ChatEvent;
import raptor.service.ConnectorService;

public class ChatWindow extends ApplicationWindow {
	private static final Log LOG = LogFactory.getLog(ChatWindow.class);

	ChatConsole mainConsole = null;
	CTabFolder tabFolder = null;

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

	public void addChannelTab(int channel) {

	}

	public void addDirectTellTab(String personName) {

	}

	public ChatConsole getTabConsole(int index) {
		return (ChatConsole) tabFolder.getItem(index).getControl();
	}

	public void sendOutput(String text) {
		ConnectorService.getInstance().send(text);

		boolean wasAccepted = false;
		for (int i = 0; i < tabFolder.getItemCount(); i++) {
			ChatConsole currentTab = getTabConsole(i); 
			
			if (currentTab.acceptInbound(text,
					ChatEvent.OUTBOUND)) {
				if (!tabFolder.getItem(i).isShowing())
				{
					currentTab.setDirty(true);
					tabFolder.getItem(i).setText("*" + currentTab.getTitle() + "*");
				}
				wasAccepted = true;
			}
		}

		if (!wasAccepted) {
			mainConsole.acceptInbound(text, ChatEvent.OUTBOUND);
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		GridLayout gridLayout = new GridLayout(1, false);
		System.out.println("createdContents");

		parent.setLayout(gridLayout);
		mainConsole = new ChatConsole(parent, SWT.NONE, this);
		mainConsole.setLayoutData(new GridData(GridData.FILL_BOTH));

		tabFolder = new CTabFolder(parent, SWT.BORDER);
		tabFolder.setVisible(false);

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
