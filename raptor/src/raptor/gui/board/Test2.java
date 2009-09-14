package raptor.gui.board;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Display;

public class Test2 {
	public static void main(String[] args) {
		ApplicationWindow w = new ApplicationWindow(null);
		w.setBlockOnOpen(true);
		w.open();
		Display.getCurrent().dispose();
	}
}
