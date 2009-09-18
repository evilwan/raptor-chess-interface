package raptor.service;

public class ConnectorService {
	private static final ConnectorService instance = new ConnectorService();

	public static ConnectorService getInstance() {
		return instance;
	}

}
