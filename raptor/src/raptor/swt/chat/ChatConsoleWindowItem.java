package raptor.swt.chat;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.swt.ItemChangedListener;

public class ChatConsoleWindowItem implements RaptorWindowItem {
	public static final int TEXT_BLOCK = 5000;
	ChatConsole console;
	ChatConsoleController controller;

	public ChatConsoleWindowItem(ChatConsoleController controller) {
		this.controller = controller;
	}

	public void addItemChangedListener(ItemChangedListener listener) {
		controller.addItemChangedListener(listener);
	}

	public boolean confirmReparenting() {
		return MessageDialog
				.openConfirm(
						Raptor.getInstance().getRaptorWindow().getShell(),
						"Confirm",
						"Moving a chat console results in losing all previous messages. Do you wish to proceed?");
	}

	public void dispose() {
		console.dispose();
	}

	public Composite getControl() {
		return console;
	}

	public Image getImage() {
		return controller.getIconImage();
	}

	public Quadrant getPreferredQuadrant() {
		return controller.getPreferredQuadrant();

	}

	public String getTitle() {
		return controller.getTitle();
	}

	public void init(Composite parent) {
		console = new ChatConsole(parent, SWT.NONE);
		console.setController(controller);
		controller.setChatConsole(console);
		console.createControls();
		controller.init();
	}

	public boolean isCloseable() {
		return controller.isCloseable();
	}

	public void onActivate() {
		console.getDisplay().asyncExec(new Runnable() {
			public void run() {
				controller.onForceAutoScroll();
			}
		});
	}

	public void onPassivate() {
	}

	public void onReparent(Composite newParent) {
		// Grab the controller from the console since it may have changed.
		controller = console.getController();
		controller.onPreReparent();
		console.setController(null);

		ChatConsole newConsole = new ChatConsole(newParent, SWT.NONE);
		newConsole.setController(controller);
		newConsole.createControls();
		controller.setChatConsole(newConsole);

		console.setVisible(false);
		console.dispose();

		controller.onPostReparent();
		console = newConsole;
		console.redraw();
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
		controller.removeItemChangedListener(listener);
	}
}
