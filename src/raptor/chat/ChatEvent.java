package raptor.chat;

public class ChatEvent {
	protected String source;
	protected int type;
	protected String gameId;
	protected String message;
	protected String channel;
	protected long time;

	public ChatEvent() {
		time = System.currentTimeMillis();
	}

	public ChatEvent(String source, int type, String message) {
		this();
		this.source = source;
		this.type = type;
		this.message = message;
	}

	public ChatEvent(String source, int type, String message, String gameId) {
		this(source, type, message);
		this.gameId = gameId;
	}

	public String toString() {
		return "ChatEvent: source=" + source + " type=" + type + " gameId="
				+ gameId + " message='" + message + "'";
	}

	public String getMessage() {
		return message;
	}

	public String getSource() {
		return source;
	}

	public long getTime() {
		return time;
	}

	public int getType() {
		return type;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

}
