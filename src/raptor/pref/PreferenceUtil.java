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

		// Add the nodes
//		mgr
//				.addToRoot(new PreferenceNode("raptor",
//						new RaptorPage()));
		mgr.addToRoot(new PreferenceNode("chessBoardGraphics",
				new ChessBoardGraphicsPage()));
		mgr.addToRoot(new PreferenceNode("colors", new ColorsPage()));
		mgr.addToRoot(new PreferenceNode("fonts", new FontsPage()));
		mgr.addToRoot(new PreferenceNode("chessBoardBehavior",
				new ChessBoardBehaviorPage()));
		mgr.addToRoot(new PreferenceNode("clocks", new ClocksPage()));

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
