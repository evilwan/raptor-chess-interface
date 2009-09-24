package raptor.swt.chat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import raptor.Raptor;
import raptor.connector.Connector;

public class ChatConsoles extends Composite {

	protected CTabFolder folder;

	public void addChatConsole(ChatConsoleController controller,
			Connector connector, boolean isCloseable, String title) {
		int style = isCloseable ? SWT.CLOSE : SWT.NONE;
		CTabItem item = new CTabItem(folder, style);
		ChatConsole chatConsole = new ChatConsole(folder, SWT.NONE);
		chatConsole.setController(controller);
		chatConsole.setPreferences(Raptor.getInstance().getPreferences());
		chatConsole.setConnector(connector);
		controller.setChatConsole(chatConsole);
		chatConsole.createControls();
		chatConsole.getController().init();
		chatConsole.pack();
		item.setControl(chatConsole);
		item.setText(title);
		folder.layout(true);
		folder.setSelection(item);
	}

	public ChatConsoles(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout());

		folder = new CTabFolder(this, SWT.BORDER);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		folder.setSimple(false);
		folder.setUnselectedImageVisible(false);
		folder.setUnselectedCloseVisible(false);

		folder.setMaximizeVisible(true);
		restore();

		folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void maximize(CTabFolderEvent event) {
				Raptor.getInstance().getAppWindow().maximizeChatConsoles();
			}

			public void restore(CTabFolderEvent event) {
				Raptor.getInstance().getAppWindow().restore();
			}

		});
		folder.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				System.err.println("Mouse double click " + e.count);
				if (e.count == 2) {
					if (isMaximized()) {
						Raptor.getInstance().getAppWindow().restore();
					} else {
						Raptor.getInstance().getAppWindow()
								.maximizeChatConsoles();
					}
				}
				super.mouseDoubleClick(e);
			}

			@Override
			public void mouseDown(MouseEvent e) {
				System.err.println("Mouse down " + e.button);
				if (e.button == 3) {
					System.err.println("On mouse down");
					Menu menu = new Menu(folder.getShell(), SWT.POP_UP);
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText("Comming soon.");
					item.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							System.out.println("Item Selected");
						}
					});
					menu.setLocation(folder.toDisplay(e.x, e.y));
					menu.setVisible(true);
					while (!menu.isDisposed() && menu.isVisible()) {
						if (!folder.getDisplay().readAndDispatch())
							folder.getDisplay().sleep();
					}
					menu.dispose();
				}
			}

		});
		pack();
	}

	protected void forceScrollCurrentConsole() {
		if (folder.getItemCount() > 0) {
			final ChatConsole currentConsole = (ChatConsole) folder.getItem(
					folder.getSelectionIndex()).getControl();
			if (currentConsole != null) {
				getDisplay().timerExec(100, new Runnable() {
					public void run() {
						currentConsole.getController().onForceAutoScroll();
						currentConsole.outputText.forceFocus();
					}
				});
			}
		}
	}

	public boolean isMaximized() {
		return folder.getMaximized();
	}

	public void restore() {
		folder.setMaximized(false);
		forceScrollCurrentConsole();
	}

	public void maximize() {
		folder.setMaximized(true);
		forceScrollCurrentConsole();
	}
}
