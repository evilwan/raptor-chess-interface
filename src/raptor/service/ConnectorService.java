package raptor.service;

import java.util.HashMap;
import java.util.Map;

import raptor.connector.Connector;
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

	public ConnectorService() {
		FicsConnector ficsConnector = new FicsConnector();
		shortNameToConnector.put(ficsConnector.getShortName(), ficsConnector);
	}

	/**
	 * Returns an array of all connectors.
	 */
	public Connector[] getConnectors() {
		return shortNameToConnector.values().toArray(new Connector[0]);

	}

	/**
	 * Returns a connector given its short name.
	 */
	public Connector getConnector(String shortName) {
       return shortNameToConnector.get(shortName);
	}

}
