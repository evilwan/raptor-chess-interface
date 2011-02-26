package raptor.connector.ics.timeseal;

public interface MessageProducer {
    public void send(String message);
    public void close();
}
