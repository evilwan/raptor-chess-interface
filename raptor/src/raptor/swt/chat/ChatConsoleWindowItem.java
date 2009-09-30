package raptor.swt.chat;

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

	public boolean confirmClose() {
		return controller.confirmClose();
	}

	public boolean confirmQuadrantMove() {
		return Raptor
				.getInstance()
				.confirm(
						"Moving a chat console might result in a loss of previous messages\n"
								+ "depending on how many there are. Do you wish to proceed?");
	}

	public void dispose() {
		if (console != null) {
			console.dispose();
			console = null;
		}
		if (controller != null) {
			controller.dispose();
			controller = null;
		}
	}

	public Composite getControl() {
		return console;
	}

	public Image getImage() {
		return controller != null ? controller.getIconImage() : null;
	}

	public Quadrant getPreferredQuadrant() {
		return controller != null ? controller.getPreferredQuadrant()
				: Quadrant.I;

	}

	public String getTitle() {
		return controller != null ? controller.getTitle() : "ERROR";
	}

	public void init(Composite parent) {
		console = new ChatConsole(parent, SWT.NONE);
		console.setController(controller);
		controller.setChatConsole(console);
		console.createControls();
		controller.init();
	}

	public boolean isCloseable() {
		return controller != null ? controller.isCloseable() : true;
	}

	public void onActivate() {
		if (console != null && !console.isDisposed() && controller != null) {
			console.getDisplay().syncExec(new Runnable() {
				public void run() {
					controller.onForceAutoScroll();
					controller.chatConsole.outputText.forceFocus();
				}
			});
		}
	}

	public void onPassivate() {
	}

	public void onReparent(Composite newParent) {
		if (controller != null) {
			// Grab the controller from the console since it may have changed.
			controller = console.getController();
			controller.onPreReparent();
			console.setController(null);
			console.dispose();

			console = new ChatConsole(newParent, SWT.NONE);
			console.setController(controller);
			console.createControls();
			controller.setChatConsole(console);

			controller.onPostReparent();
			console.redraw();
		}
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
		if (controller != null) {
			controller.removeItemChangedListener(listener);
		}
	}
}
