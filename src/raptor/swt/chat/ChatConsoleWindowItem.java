package raptor.swt.chat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.sun.tools.javac.util.Log;

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
		int chars = console.getInputText().getCharCount();

		if (!console.isReparentable() && chars > 25000) {
			return Raptor
					.getInstance()
					.confirm(
							"You have over 25,000 characters in this console. Moving to another quadrant might"
									+ "take a while to update as the previous messages are loaded. Do you wish to proceed?");
		} else {
			return true;
		}
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

	public boolean onReparent(Composite newParent) {
		boolean result = false;
		if (controller != null) {
			if (!console.setParent(newParent)) {
				// Grab the controller from the console since it may have
				// changed.
				controller = console.getController();
				controller.onPreReparent();
				console.setController(null);
				console.setVisible(false);
				console.dispose();

				console = new ChatConsole(newParent, SWT.NONE);
				console.setController(controller);
				console.createControls();
				controller.setChatConsole(console);

				controller.onPostReparent();
				ChatUtils.appendPreviousChatsToController(console);
				console.redraw();
			} else {
				result = true;
			}
		}
		return result;
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
		if (controller != null) {
			controller.removeItemChangedListener(listener);
		}
	}
}
