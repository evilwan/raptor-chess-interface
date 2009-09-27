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

	public String getChannel() {
		return channel;
	}

	public String getGameId() {
		return gameId;
	}

	/**
	 * @return Entire message involved in this ChatEvent.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return Username of the person involved in this ChatEvent.
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @return What time this ChatEvent occurred, in milliseconds.
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @return The type of ChatEvent. 
	 * @see ChatTypes.
	 */
	public int getType() {
		return type;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @param time The time that this chat event occurred.
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @param type The type of ChatEvent this is.
	 * @see ChatTypes.
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Dumps information about this ChatEvent to a string.
	 */
	@Override
	public String toString() {
		return "ChatEvent: source=" + source + " type=" + type + " gameId="
				+ gameId + " message='" + message + "'";
	}

}
