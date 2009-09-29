package raptor.chat;

public enum ChatType {
	/**
	 * Used to identify types that don't match any of the others.
	 */
	UNKNOWN,
	/**
	 * Used for direct tells and say. Source will be the person sending the
	 * tell.
	 */
	TELL,
	/**
	 * Kibitzes pertaining to a game. Will always have a game id set and a
	 * source.
	 */
	KIBITZ,
	/**
	 * Whispers pertaining to a game. Will always have a source and a game id.
	 */
	WHISPER,
	/**
	 * Global shouts. Will always have a source.
	 */
	SHOUT,
	/**
	 * Global c-shouts. Will always have a source.
	 */
	CSHOUT,
	/**
	 * Channel tells. The source will be the person sending the tell. Will
	 * always have a channel.
	 */
	CHAN_TELL,
	/**
	 * Tells from a bughouse partner. Source will be the partners name.
	 */
	PARTNER_TELL,
	/**
	 * Message pertaining to a bughouse partnership being created. Source will
	 * be the name of the partner.
	 */
	PARTNERSHIP_CREATED,
	/**
	 * Message pertaining to a bughouse partnership being destroyed.
	 */
	PARTNERSHIP_DESTROYED,
	/**
	 * Message sent when you are following a persons games. Source will be the
	 * person you are following.
	 */
	FOLLOWING,
	/**
	 * Message sent when you are no longer following a persons games.
	 */
	NOT_FOLLOWING,
	/**
	 * Message sent when someone challenges you to a match.
	 */
	CHALLENGE,

	/**
	 * Used for messages sent to a connector.
	 */
	OUTBOUND,

	/**
	 * Used for messages sent from within Raptor. Error messages and
	 * informational messages.
	 */
	INTERNAL
}
