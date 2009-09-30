package raptor.pref;

public interface PreferenceKeys {
	// In order to update entries in the font and color registries, all font
	// properties must end in font and all color properties must end in color.
	public static final String BOARD_CHESS_SET_NAME = "board-chess-set-name";
	public static final String BOARD_SQUARE_BACKGROUND_NAME = "board-square-background-name";
	public static final String BOARD_IS_SHOW_COORDINATES = "board-show-coordinates";
	public static final String BOARD_PIECE_SIZE_ADJUSTMENT = "board-piece-size-adjustment-percentage";
	public static final String BOARD_HIGHLIGHT_BORDER_WIDTH = "board-highlight-width-percentage";
	public static final String BOARD_IS_SHOWING_PIECE_JAIL = "board-is-showing-piece-jail";
	public static final String BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN = "board-clock-show-millis-when-less-than";
	public static final String BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN = "board-clock-show-seconds-when-less-than";
	public static final String BOARD_PREMOVE_ENABLED = "board-premove-enabled";
	public static final String BOARD_SMARTMOVE_ENABLED = "board-smartmove-enabled";
	public static final String BOARD_PLAY_MOVE_SOUND_WHEN_OBSERVING = "board-play-move-sound-when-observing";
	public static final String BOARD_QUEUED_PREMOVE_ENABLED = "board-queued-premove-enabled";

	public static final String BOARD_COORDINATES_COLOR = "board-coordinates-color";
	public static final String BOARD_BACKGROUND_COLOR = "board-background-color";
	public static final String BOARD_HIGHLIGHT_COLOR = "board-highlight-color";
	public static final String BOARD_ACTIVE_CLOCK_COLOR = "board-active-clock-color";
	public static final String BOARD_INACTIVE_CLOCK_COLOR = "board-inactive-clock-color";
	public static final String BOARD_LAG_COLOR = "board-lag-color";
	public static final String BOARD_LAG_OVER_20_SEC_COLOR = "board-over-20-sec-lag-color";
	public static final String BOARD_PLAYER_NAME_COLOR = "board-player-name-color";
	public static final String BOARD_PIECE_JAIL_LABEL_COLOR = "board-piece-jail-label-color";
	public static final String BOARD_PIECE_JAIL_BACKGROUND_COLOR = "board-piece-jail-background-color";
	public static final String BOARD_OPENING_DESC_COLOR = "board-opening-desc-color";
	public static final String BOARD_GAME_DESCRIPTION_COLOR = "board-game-description-color";
	public static final String BOARD_PREMOVES_COLOR = "board-premoves-color";
	public static final String BOARD_STATUS_COLOR = "board-status-color";
	public static final String BOARD_RESULT_COLOR = "board-result-color";
	public static final String BOARD_IS_SHOWING_PIECE_UNICODE_CHARS = "board-is-showing-piece-unicode-chars";

	public static final String BOARD_COORDINATES_FONT = "board-coordinates-font";
	public static final String BOARD_CLOCK_FONT = "board-clock-font";
	public static final String BOARD_LAG_FONT = "board-lag-font";
	public static final String BOARD_PLAYER_NAME_FONT = "board-player-name-font";
	public static final String BOARD_PIECE_JAIL_FONT = "board-piece-jail-font";
	public static final String BOARD_OPENING_DESC_FONT = "board-opening-desc-font";
	public static final String BOARD_GAME_DESCRIPTION_FONT = "board-game-description-font";
	public static final String BOARD_PREMOVES_FONT = "board-premoves-font";
	public static final String BOARD_STATUS_FONT = "board-status-font";
	public static final String BOARD_RESULT_FONT = "board-result-font";

	public static final String CHAT_MAX_CONSOLE_CHARS = "chat-max-console-chars";
	public static final String CHAT_TIMESTAMP_CONSOLE = "chat-timestamp-console";
	public static final String CHAT_TIMESTAMP_CONSOLE_FORMAT = "chat-timestamp-console-format";
	public static final String CHAT_OUTPUT_FONT = "chat-output-font";
	public static final String CHAT_INPUT_FONT = "chat-input-font";
	public static final String CHAT_PROMPT_FONT = "chat-prompt-font";
	public static final String CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO = "chat-event-";

	public static final String CHAT_OUTPUT_BACKGROUND_COLOR = "chat-output-background-color";
	public static final String CHAT_INPUT_BACKGROUND_COLOR = "chat-input-background-color";
	public static final String CHAT_OUTPUT_TEXT_COLOR = "chat-output-text-color";
	public static final String CHAT_INPUT_DEFAULT_TEXT_COLOR = "chat-input-default-text-color";
	public static final String CHAT_CONSOLE_BACKGROUND_COLOR = "chat-console-background-color";
	public static final String CHAT_PROMPT_COLOR = "chat-prompt-color";
	public static final String CHAT_QUOTE_UNDERLINE_COLOR = "chat-quote-underline-color";
	public static final String CHAT_LINK_UNDERLINE_COLOR = "chat-link-underline-color";

	public static final String MISC_BROWSER_NAME = "misc-browser-name";

	public static final String APP_LAG_FONT = "app-lag-font";
	public static final String APP_LAG_COLOR = "app-lag-color";
	public static final String APP_STATUS_BAR_FONT = "app-status-bar-font";
	public static final String APP_STATUS_BAR_COLOR = "app-starus-bar-color";
	public static final String APP_NAME = "app-name";
	public static final String APP_MAIN_TAB_QUADRANT = "app-channel-tell-quadrant";
	public static final String APP_CHANNEL_TAB_QUADRANT = "app-channel-tell-quadrant";
	public static final String APP_PERSON_TAB_QUADRANT = "app-person-tell-quadrant";
	public static final String APP_REGEX_TAB_QUADRANT = "app-regex-tab-quadrant";
	public static final String APP_PARTNER_TELL_TAB_QUADRANT = "app-partner-tell-quadrant";
	public static final String APP_GAME_QUADRANT = "app-game-quadrant";

	public static final String SOUND_ENABLED = "sound-enabled";

	// Connector preferences should always use the short name of the connector
	// followed by the preference.
	public static final String FICS_SERVER_URL = "fics-server-url";
	public static final String FICS_PORT = "fics-port";
	public static final String FICS_KEEP_ALIVE = "fics-keepalive";
	public static final String FICS_AUTO_CONNECT = "fics-auto-connect";
	public static final String FICS_USER_NAME = "fics-user-name";
	public static final String FICS_PASSWORD = "fics-password";
	public static final String FICS_IS_NAMED_GUEST = "fics-is-named-guest";
	public static final String FICS_IS_ANON_GUEST = "fics-is-anon-guest";
	public static final String FICS_LOGIN_SCRIPT = "fics-login-script";
	public static final String FICS_IS_LOGGING_GAMES = "fics-is-logging-games";
	public static final String FICS_TIMESEAL_ENABLED = "fics-timeseal-enabled";

	public static final String CHAT_LINK_COLOR = "chat-link-color";
}
