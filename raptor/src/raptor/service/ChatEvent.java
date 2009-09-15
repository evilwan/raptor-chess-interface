package raptor.service;

public class ChatEvent {

	public static final int UNKNOWN = 0;
	public static final int TELL = 1;
	public static final int KIBITZ = 2;
	public static final int WHISPER = 3;
	public static final int SHOUT = 4;
	public static final int CSHOUT = 5;
	public static final int CHAN_TELL = 6;
	public static final int SAY = 7;
	public static final int PARTNER_TELL = 8;
	public static final int OUTBOUND = 9;

	protected String source;
	protected int type;
	protected int gameId;
	protected String message;
	protected long time;

	public int getGameId() {
		return gameId;
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

	public void setGameId(int gameId) {
		this.gameId = gameId;
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
}
