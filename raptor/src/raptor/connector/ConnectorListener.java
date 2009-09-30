package raptor.connector;

public interface ConnectorListener {
	/**
	 * Invoked when a connector connects.
	 */
	public void onConnect();

	/**
	 * Invoked when a connector is connecting.
	 */
	public void onConnecting();

	/**
	 * Invoked when a connector disconnects.
	 */
	public void onDisconnect();
}
