package raptor.service;

import raptor.connector.Connector;

public class ConnectorService {
	private static final ConnectorService instance = new ConnectorService();

	public static ConnectorService getInstance() {
		return instance;
	}

	private long lag;

	private Connector connector;

	public void disconnect() {
		getConnector().disconnect();
	}

	public Connector getConnector() {
		return connector;
	}

	public long getLag() {
		return lag;
	}

	public void send(String message) {
		getConnector().send(message);
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	public void setLag(long lag) {
		this.lag = lag;
	}
}
