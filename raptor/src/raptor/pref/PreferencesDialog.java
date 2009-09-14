package raptor.pref;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Display;

/**
 * This class demonstrates JFace preferences and field editors
 */
public class PreferencesDialog {
	/**
	 * Runs the application
	 */
	public void run() {
		Display display = Display.getCurrent();

		// Create the preference manager
		PreferenceManager mgr = new PreferenceManager();

		// Add the nodes
		mgr.addToRoot(new PreferenceNode("chessBoardGraphics",
				new ChessBoardGraphicsPage()));
		mgr.addToRoot(new PreferenceNode("colors", new ColorsPage()));
		mgr.addToRoot(new PreferenceNode("fonts", new FontsPage()));
		mgr.addToRoot(new PreferenceNode("chessBoardBehavior",
				new ChessBoardBehaviorPage()));

		// Create the preferences dialog
		PreferenceDialog dlg = new PreferenceDialog(null, mgr);

		// Open the dialog
		dlg.open();
	}

	/**
	 * The application entry point
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		new PreferencesDialog().run();
	}
}
