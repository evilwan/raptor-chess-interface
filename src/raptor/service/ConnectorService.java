package raptor.service;

import java.util.HashMap;
import java.util.Map;

import raptor.connector.Connector;
import raptor.connector.bics.BicsConnector;
import raptor.connector.fics.FicsConnector;

/**
 * A service which manages all of the connectors being used by Raptor.
 */
public class ConnectorService {
	private static final ConnectorService instance = new ConnectorService();

	public static ConnectorService getInstance() {
		return instance;
	}

	Map<String, Connector> shortNameToConnector = new HashMap<String, Connector>();

	private ConnectorService() {
		FicsConnector ficsConnector = new FicsConnector();
		BicsConnector bicsConnector = new BicsConnector();
		shortNameToConnector.put(ficsConnector.getShortName(), ficsConnector);
		shortNameToConnector.put(bicsConnector.getShortName(), bicsConnector);
	}

	/**
	 * Disposes of all the connectors being managed.
	 */
	public void dispose() {
		Connector[] connectors = ConnectorService.getInstance().getConnectors();
		for (Connector connector : connectors) {
			try {
				connector.dispose();
			} catch (Throwable t) {
			}
		}
	}

	/**
	 * Returns a connector given its short name.
	 */
	public Connector getConnector(String shortName) {
		return shortNameToConnector.get(shortName);
	}

	/**
	 * Returns an array of all connectors.
	 */
	public Connector[] getConnectors() {
		return shortNameToConnector.values().toArray(new Connector[0]);

	}
}
