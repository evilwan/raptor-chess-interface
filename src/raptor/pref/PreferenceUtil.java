package raptor.pref;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;

import raptor.Raptor;
import raptor.connector.Connector;
import raptor.service.ConnectorService;

/**
 * A class containing utility methods for Preferences.
 */
public class PreferenceUtil {

	/**
	 * Launches the preference dialog.
	 * 
	 * All connectors in the ConnectorService have their preference nodes added.
	 */
	public static void launchPreferenceDialog() {
		// Create the preference manager
		PreferenceManager mgr = new PreferenceManager();

		mgr.addToRoot(new PreferenceNode("raptor", new RaptorPage()));
		mgr
				.addToRoot(new PreferenceNode("raptorWindow",
						new RaptorWindowPage()));
		mgr.addTo("raptorWindow", new PreferenceNode("layout1",
				new RaptorWindowLayoutPage("1", "app-Layout1")));
		mgr.addTo("raptorWindow", new PreferenceNode("layout2",
				new RaptorWindowLayoutPage("2", "app-Layout2")));
		mgr.addTo("raptorWindow", new PreferenceNode("layout3",
				new RaptorWindowLayoutPage("3", "app-Layout3")));

		mgr.addToRoot(new PreferenceNode("chatConsole", new ChatConsolePage()));
		mgr.addTo("chatConsole", new PreferenceNode("messageColors",
				new ChatConsoleMessageColors()));
		mgr.addTo("chatConsole", new PreferenceNode("channelColors",
				new ChatConsoleChannelColorsPage()));

		mgr.addToRoot(new PreferenceNode("chessBoard", new ChessBoardPage()));
		mgr.addTo("chessBoard", new PreferenceNode("behavior",
				new ChessBoardBehaviorPage()));
		mgr.addTo("chessBoard", new PreferenceNode("clocks",
				new ChessBoardClocksPage()));
		mgr.addTo("chessBoard", new PreferenceNode("colors",
				new ChessBoardColorsPage()));
		mgr.addTo("chessBoard", new PreferenceNode("fonts",
				new ChessBoardFontsPage()));

		// Add the connector preference nodes.
		Connector[] connectors = ConnectorService.getInstance().getConnectors();
		for (Connector connector : connectors) {

			PreferencePage root = connector.getRootPreferencePage();
			if (root != null) {
				mgr
						.addToRoot(new PreferenceNode(connector.getShortName(),
								root));
				PreferenceNode[] secondaries = connector
						.getSecondaryPreferenceNodes();
				if (secondaries != null && secondaries.length > 0) {
					for (PreferenceNode node : secondaries) {
						mgr.addTo(connector.getShortName(), node);
					}
				}
			}
		}

		// Create the preferences dialog
		PreferenceDialog dlg = new PreferenceDialog(Raptor.getInstance()
				.getRaptorWindow().getShell(), mgr);

		// Open the dialog
		dlg.open();
	}
}
