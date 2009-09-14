package raptor.connector;

public interface Connector {
	public void disconnect();

	public void send(String msg);
}
