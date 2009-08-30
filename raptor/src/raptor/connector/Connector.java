package raptor.connector;

public interface Connector {
	public void send(String msg);
	public void disconnect();
}
