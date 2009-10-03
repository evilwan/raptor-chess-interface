package raptor.swt.chat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import raptor.Quadrant;
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

	public Control getToolbar(Composite parent) {
		return controller.getToolbar(parent);
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

	public void removeItemChangedListener(ItemChangedListener listener) {
		if (controller != null) {
			controller.removeItemChangedListener(listener);
		}
	}
}
