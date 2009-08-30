package raptor.service;

import raptor.connector.Connector;

public class ConnectorService {
	private static final ConnectorService instance = new ConnectorService();

	private long lag;
	private Connector connector;

	public static ConnectorService getInstance() {
		return instance;
	}

	public void send(String message) {
		getConnector().send(message);
	}

	public void disconnect() {
		getConnector().disconnect();
	}

	public long getLag() {
		return lag;
	}

	public void setLag(long lag) {
		this.lag = lag;
	}

	public Connector getConnector() {
		return connector;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}
}
