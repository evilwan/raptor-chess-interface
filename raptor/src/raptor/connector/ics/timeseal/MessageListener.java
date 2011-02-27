package raptor.connector.ics.timeseal;

public interface MessageListener {
    public void messageArrived(StringBuilder inboundMessageBuffer);
    public void onError(String message, Throwable t);
    public void connectionClosed(StringBuilder inboundMessageBuffer);
}
