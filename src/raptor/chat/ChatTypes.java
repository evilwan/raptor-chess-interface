package raptor.chat;

public interface ChatTypes {
	public static final int UNKNOWN = 0;
	public static final int TELL = 1;
	public static final int KIBITZ = 2;
	public static final int WHISPER = 3;
	public static final int SHOUT = 4;
	public static final int CSHOUT = 5;
	public static final int CHAN_TELL = 6;
	public static final int SAY = 7;
	public static final int PARTNER_TELL = 8;
	public static final int PARTNERSHIP_CREATED = 9;
	public static final int PARTNERSHIP_DESTROYED = 10;
	public static final int FOLLOWING = 11;
	public static final int NOT_FOLLOWING = 12;
	public static final int CHALLENGE = 13;
	public static final int OUTBOUND = 14;
}
