/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.pref;

/**
 * Contains all of the preference keys. In order to update entries in the font
 * and color registries, all font properties must end in font and all color
 * properties must end in color.
 */
public interface PreferenceKeys {
	// THe following are not preference names but suffixes of preference names.
	public static final String CHESS_BOARD_QUADRANT = "chess-board-quadrant";
	public static final String MAIN_TAB_QUADRANT = "main-quadrant";
	public static final String PARTNER_TELL_TAB_QUADRANT = "partner-quadrant";
	public static final String PERSON_TAB_QUADRANT = "person-quadrant";
	public static final String REGEX_TAB_QUADRANT = "regex-quadrant";
	public static final String SEEK_TABLE_QUADRANT = "seek-table-quadrant";
	public static final String BUG_WHO_QUADRANT = "bug-who-quadrant";
	public static final String BUG_BUTTONS_QUADRANT = "bug-buttons-quadrant";
	public static final String BUGHOUSE_GAME_2_QUADRANT = "bughouse-game-2-quadrant";
	public static final String CHANNEL_TAB_QUADRANT = "channel-quadrant";
	public static final String QUAD1_QUAD234567_QUAD8_SASH_WEIGHTS = "quad1-quad234567-quad8-sash-weights";
	public static final String QUAD2_QUAD234567_SASH_WEIGHTS = "quad2-quad234567-sash-weights";
	public static final String QUAD3_QUAD4_SASH_WEIGHTS = "quad3-quad4-sash-weights";
	public static final String QUAD34_QUAD567_SASH_WEIGHTS = "quad34-quad567-sash-weights";
	public static final String QUAD5_QUAD6_SASH_WEIGHTS = "quad5-quad6-sash-weights";
	public static final String QUAD56_QUAD7_SASH_WEIGHTS = "quad56-quad7-sash-weights";
	public static final String WINDOW_BOUNDS = "window-bounds";
	public static final String CHANNEL_REGEX_TAB_INFO = "channel-reg-ex-tab-info";

	// Starting from here and on down the constants are only preference names.
	public static final String APP_NAME = "app-name";
	public static final String APP_IS_LOGGING_GAMES = "app-is-logging-games";
	public static final String APP_OPEN_LINKS_IN_EXTERNAL_BROWSER = "app-open-links-in-external-browser";
	public static final String APP_PING_COLOR = "app-lag-color";
	public static final String APP_PING_FONT = "app-lag-font";
	public static final String APP_SASH_WIDTH = "app-sash-width";
	public static final String APP_SOUND_ENABLED = "sound-enabled";
	public static final String APP_STATUS_BAR_COLOR = "app-starus-bar-color";
	public static final String APP_STATUS_BAR_FONT = "app-status-bar-font";
	public static final String APP_HOME_URL = "app-home-url";
	public static final String APP_LAYOUT = "app-layout";
	public static final String APP_BROWSER_QUADRANT = "app-browser-quadrant";
	public static final String APP_PGN_RESULTS_QUADRANT = "app-pgn-results-quadrant";
	public static final String APP_WINDOW_ITEM_POLL_INTERVAL = "app-window-item-poll-interval";
	public static final String APP_CHESS_BOARD_QUADRANT = "app-"
			+ CHESS_BOARD_QUADRANT;
	public static final String APP_BUGHOUSE_GAME_2_QUADRANT = "app-"
			+ BUGHOUSE_GAME_2_QUADRANT;
	public static final String APP_LINUX_UNIX_BROWSER_NAME = "app-linux-unix-browser-name";
	public static final String APP_IS_LAUNCHNG_HOME_PAGE = "app-is-launching-home-page";

	public static final String APP_LAYOUT1_QUAD1_QUAD234567_QUAD8_SASH_WEIGHTS = "app-Layout1-quad1-quad234567-quad8-sash-weights";
	public static final String APP_LAYOUT1_QUAD2_QUAD234567_SASH_WEIGHTS = "app-Layout1-quad2-quad234567-sash-weights";
	public static final String APP_LAYOUT1_QUAD3_QUAD4_SASH_WEIGHTS = "app-Layout1-quad3-quad4-sash-weights";
	public static final String APP_LAYOUT1_QUAD34_QUAD567_SASH_WEIGHTS = "app-Layout1-quad34-quad567-sash-weights";
	public static final String APP_LAYOUT1_QUAD5_QUAD6_SASH_WEIGHTS = "app-Layout1-quad5-quad6-sash-weights";
	public static final String APP_LAYOUT1_QUAD56_QUAD7_SASH_WEIGHTS = "app-Layout1-quad56-quad7-sash-weights";
	public static final String APP_LAYOUT1_WINDOW_BOUNDS = "app-Layout1-window-bounds";

	public static final String APP_LAYOUT2_QUAD1_QUAD234567_QUAD8_SASH_WEIGHTS = "app-Layout2-quad1-quad234567-quad8-sash-weights";
	public static final String APP_LAYOUT2_QUAD2_QUAD234567_SASH_WEIGHTS = "app-Layout2-quad2-quad234567-sash-weights";
	public static final String APP_LAYOUT2_QUAD3_QUAD4_SASH_WEIGHTS = "app-Layout2-quad3-quad4-sash-weights";
	public static final String APP_LAYOUT2_QUAD34_QUAD567_SASH_WEIGHTS = "app-Layout2-quad34-quad567-sash-weights";
	public static final String APP_LAYOUT2_QUAD5_QUAD6_SASH_WEIGHTS = "app-Layout2-quad5-quad6-sash-weights";
	public static final String APP_LAYOUT2_QUAD56_QUAD7_SASH_WEIGHTS = "app-Layout2-quad56-quad7-sash-weights";
	public static final String APP_LAYOUT2_WINDOW_BOUNDS = "app-Layout2-window-bounds";

	public static final String APP_LAYOUT3_QUAD1_QUAD234567_QUAD8_SASH_WEIGHTS = "app-Layout3-quad1-quad234567-quad8-sash-weights";
	public static final String APP_LAYOUT3_QUAD2_QUAD234567_SASH_WEIGHTS = "app-Layout3-quad2-quad234567-sash-weights";
	public static final String APP_LAYOUT3_QUAD3_QUAD4_SASH_WEIGHTS = "app-Layout3-quad3-quad4-sash-weights";
	public static final String APP_LAYOUT3_QUAD34_QUAD567_SASH_WEIGHTS = "app-Layout3-quad34-quad567-sash-weights";
	public static final String APP_LAYOUT3_QUAD5_QUAD6_SASH_WEIGHTS = "app-Layout3-quad5-quad6-sash-weights";
	public static final String APP_LAYOUT3_QUAD56_QUAD7_SASH_WEIGHTS = "app-Layout3-quad56-quad7-sash-weights";
	public static final String APP_LAYOUT3_WINDOW_BOUNDS = "app-Layout3-window-bounds";

	public static final String ACTION_SEPARATOR_SEQUENCE = "action-separator-sequence";

	public static final String BUG_ARENA_PARTNERS_INDEX = "bughouse-arena-partners-index";
	public static final String BUG_ARENA_MAX_PARTNERS_INDEX = "bughouse-arena-max-partners-index";
	public static final String BUG_ARENA_TEAMS_INDEX = "bughouse-arena-teams-index";
	public static final String BUG_ARENA_TEAMS_IS_RATED = "bughosue-arena-teams-is-rated";

	public static final String SEEK_TABLE_RATINGS_INDEX = "seek-table-ratings-index";
	public static final String SEEK_TABLE_MAX_RATINGS_INDEX = "seek-table-max-ratings-index"; // added
	// by
	// johnthegreat
	public static final String SEEK_TABLE_RATED_INDEX = "seek-table-rated-index";
	public static final String SEEK_TABLE_SHOW_COMPUTERS = "seek-table-show-computers";
	public static final String SEEK_TABLE_SHOW_LIGHTNING = "seek-table-show-lightning";
	public static final String SEEK_TABLE_SHOW_BLITZ = "seek-table-show-blitz";
	public static final String SEEK_TABLE_SHOW_STANDARD = "seek-table-show-standard";
	public static final String SEEK_TABLE_SHOW_CRAZYHOUSE = "seek-table-show-crazyhouse";
	public static final String SEEK_TABLE_SHOW_FR = "seek-table-show-fr";
	public static final String SEEK_TABLE_SHOW_WILD = "seek-table-show-wild";
	public static final String SEEK_TABLE_SHOW_ATOMIC = "seek-table-show-atomic";
	public static final String SEEK_TABLE_SHOW_SUICIDE = "seek-table-show-suicide";
	public static final String SEEK_TABLE_SHOW_LOSERS = "seek-table-show-losers";
	public static final String SEEK_TABLE_SHOW_UNTIMED = "seek-table-show-untimed";

	public static final String BUGHOUSE_PLAYING_OPEN_PARTNER_BOARD = "bughouse-playing-open-partner-board";
	public static final String BUGHOUSE_OBSERVING_OPEN_PARTNER_BOARD = "bughouse-observing-open-partner-board";

	public static final String BUG_BUTTONS_FONT = "bugbuttons-font";

	public static final String BOARD_SHOW_BUGHOUSE_SIDE_UP_TIME = "board-show-bughouse-side-up-time";
	public static final String BOARD_PIECE_JAIL_LABEL_PERCENTAGE = "board-piece-jail-label-percentage";
	public static final String BOARD_ACTIVE_CLOCK_COLOR = "board-active-clock-color";
	public static final String BOARD_BACKGROUND_COLOR = "board-background-color";
	public static final String BOARD_COOLBAR_MODE = "board-coolbar-mode";
	public static final String BOARD_COOLBAR_ON_TOP = "board-coolbar-on-top";
	public static final String BOARD_CHESS_SET_NAME = "board-chess-set-name";
	public static final String BOARD_CLOCK_FONT = "board-clock-font";
	public static final String BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN = "board-clock-show-millis-when-less-than";
	public static final String BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN = "board-clock-show-seconds-when-less-than";
	public static final String BOARD_COORDINATES_COLOR = "board-coordinates-color";
	public static final String BOARD_COORDINATES_FONT = "board-coordinates-font";
	public static final String BOARD_GAME_DESCRIPTION_COLOR = "board-game-description-color";
	public static final String BOARD_GAME_DESCRIPTION_FONT = "board-game-description-font";
	public static final String BOARD_INACTIVE_CLOCK_COLOR = "board-inactive-clock-color";
	public static final String BOARD_IS_PLAYING_10_SECOND_COUNTDOWN_SOUNDS = "board-is-playing-10-second-countdown-sounds";
	public static final String BOARD_IS_SHOW_COORDINATES = "board-show-coordinates";
	public static final String BOARD_IS_SHOWING_PIECE_JAIL = "board-is-showing-piece-jail";
	public static final String BOARD_IS_SHOWING_PIECE_UNICODE_CHARS = "board-is-showing-piece-unicode-chars";
	public static final String BOARD_IS_USING_CROSSHAIRS_CURSOR = "board-is-using-crosshairs-cursor";
	public static final String BOARD_LAG_COLOR = "board-lag-color";
	public static final String BOARD_LAG_FONT = "board-lag-font";
	public static final String BOARD_LAG_OVER_20_SEC_COLOR = "board-over-20-sec-lag-color";
	public static final String BOARD_LAYOUT = "board-layout";
	public static final String BOARD_OPENING_DESC_COLOR = "board-opening-desc-color";
	public static final String BOARD_OPENING_DESC_FONT = "board-opening-desc-font";
	public static final String BOARD_PIECE_JAIL_BACKGROUND_COLOR = "board-piece-jail-background-color";
	public static final String BOARD_PIECE_JAIL_FONT = "board-piece-jail-font";
	public static final String BOARD_PIECE_JAIL_LABEL_COLOR = "board-piece-jail-label-color";
	public static final String BOARD_PIECE_SIZE_ADJUSTMENT = "board-piece-size-adjustment-percentage";
	public static final String BOARD_PLAY_MOVE_SOUND_WHEN_OBSERVING = "board-play-move-sound-when-observing";
	public static final String BOARD_PLAYER_NAME_COLOR = "board-player-name-color";
	public static final String BOARD_PLAYER_NAME_FONT = "board-player-name-font";
	public static final String BOARD_PREMOVE_ENABLED = "board-premove-enabled";
	public static final String BOARD_PREMOVES_COLOR = "board-premoves-color";
	public static final String BOARD_PREMOVES_FONT = "board-premoves-font";
	public static final String BOARD_QUEUED_PREMOVE_ENABLED = "board-queued-premove-enabled";
	public static final String BOARD_SMARTMOVE_ENABLED = "board-smartmove-enabled";
	public static final String BOARD_SQUARE_BACKGROUND_NAME = "board-square-background-name";
	public static final String BOARD_STATUS_COLOR = "board-status-color";
	public static final String BOARD_STATUS_FONT = "board-status-font";
	public static final String BOARD_TAKEOVER_INACTIVE_GAMES = "board-takeover-inactive-games";
	public static final String BOARD_PIECE_SHADOW_ALPHA = "board-hiding_alpha";
	public static final String BOARD_PIECE_JAIL_SHADOW_ALPHA = "board-piece-jail-empty-alpha";
	public static final String BOARD_COORDINATES_SIZE_PERCENTAGE = "board-coordinates-size-percentage";

	public static final String ARROW_OPPONENT_COLOR = "arrow-opponent-color";
	public static final String ARROW_PREMOVE_COLOR = "arrow-premove-color";
	public static final String ARROW_MY_COLOR = "arrow-my-color";
	public static final String ARROW_OBS_COLOR = "arrow-obs-color";
	public static final String ARROW_SHOW_ON_OBS_MOVES = "arrow-show-on-obs-moves";
	public static final String ARROW_SHOW_ON_MOVE_LIST_MOVES = "arrow-show-on-move-list-moves";
	public static final String ARROW_SHOW_ON_MY_PREMOVES = "arrow-show-on-my-premoves";
	public static final String ARROW_SHOW_ON_MY_MOVES = "arrow-show-on-my-moves";
	public static final String ARROW_ANIMATION_DELAY = "arrow-animotion-delay";
	public static final String ARROW_WIDTH_PERCENTAGE = "arrow-width-percentage";
	public static final String ARROW_FADE_AWAY_MODE = "arrow-fade-away-mode";

	public static final String HIGHLIGHT_PREMOVE_COLOR = "hilight-premove-color";
	public static final String HIGHLIGHT_OPPONENT_COLOR = "hilight-opponent-color";
	public static final String HIGHLIGHT_MY_COLOR = "hilight-my-color";
	public static final String HIGHLIGHT_OBS_COLOR = "hilight-obs-color";
	public static final String HIGHLIGHT_SHOW_ON_OBS_MOVES = "hilight-show-on-obs-moves";
	public static final String HIGHLIGHT_SHOW_ON_MOVE_LIST_MOVES = "hilight-show-on-move-list-moves";
	public static final String HIGHLIGHT_SHOW_ON_MY_PREMOVES = "hilight-show-on-my-premoves";
	public static final String HIGHLIGHT_SHOW_ON_MY_MOVES = "hilight-show-on-my-moves";
	public static final String HIGHLIGHT_FADE_AWAY_MODE = "hilight-fade-away-mode";
	public static final String HIGHLIGHT_ANIMATION_DELAY = "highlight-animation-delay";
	public static final String HIGHLIGHT_WIDTH_PERCENTAGE = "highlight-width-percentage";

	public static final String RESULTS_COLOR = "results-color";
	public static final String RESULTS_ANIMATION_DELAY = "results-animation-delay";
	public static final String RESULTS_WIDTH_PERCENTAGE = "results-width-percentage";
	public static final String RESULTS_FADE_AWAY_MODE = "results-fade-away-mode";
	public static final String RESULTS_FONT = "results-font";
	public static final String RESULTS_IS_SHOWING = "results-is-showing";

	public static final String CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO = "chat-event-";
	public static final String CHAT_CONSOLE_BACKGROUND_COLOR = "chat-console-background-color";
	public static final String CHAT_INPUT_BACKGROUND_COLOR = "chat-input-background-color";
	public static final String CHAT_INPUT_DEFAULT_TEXT_COLOR = "chat-input-default-text-color";
	public static final String CHAT_INPUT_FONT = "chat-input-font";
	public static final String CHAT_LINK_UNDERLINE_COLOR = "chat-link-underline-color";
	public static final String CHAT_MAX_CONSOLE_CHARS = "chat-max-console-chars";
	public static final String CHAT_OUTPUT_BACKGROUND_COLOR = "chat-output-background-color";
	public static final String CHAT_OUTPUT_FONT = "chat-output-font";
	public static final String CHAT_OUTPUT_TEXT_COLOR = "chat-output-text-color";
	public static final String CHAT_PROMPT_COLOR = "chat-prompt-color";
	public static final String CHAT_PROMPT_FONT = "chat-prompt-font";
	public static final String CHAT_QUOTE_UNDERLINE_COLOR = "chat-quote-underline-color";
	public static final String CHAT_TIMESTAMP_CONSOLE = "chat-timestamp-console";
	public static final String CHAT_TIMESTAMP_CONSOLE_FORMAT = "chat-timestamp-console-format";
	public static final String CHAT_UNDERLINE_SINGLE_QUOTES = "chat-underline-single-quotes";
	public static final String CHAT_IS_PLAYING_CHAT_ON_PTELL = "chat-is-playing-chat-on-ptell";
	public static final String CHAT_IS_PLAYING_CHAT_ON_PERSON_TELL = "chat-is-playing-chat-on-person-tell";
	public static final String CHAT_IS_SMART_SCROLL_ENABLED = "chat-is-smart-scroll-enabled";

	// Connector preferences should always use the short name of the connector
	// followed by the preference.
	public static final String FICS_AUTO_CONNECT = "fics-auto-connect";
	public static final String FICS_SHOW_BUGBUTTONS_ON_CONNECT = "fics-show-bugbuttons-on-connect";
	public static final String FICS_KEEP_ALIVE = "fics-keep-alive";
	public static final String FICS_LOGIN_SCRIPT = "fics-login-script";
	public static final String FICS_PROFILE = "fics-profile";

	public static final String FICS_PRIMARY_IS_ANON_GUEST = "fics-Primary-is-anon-guest";
	public static final String FICS_PRIMARY_IS_NAMED_GUEST = "fics-Primary-is-named-guest";
	public static final String FICS_PRIMARY_PASSWORD = "fics-Primary-password";
	public static final String FICS_PRIMARY_PORT = "fics-Primary-port";
	public static final String FICS_PRIMARY_SERVER_URL = "fics-Primary-server-url";
	public static final String FICS_PRIMARY_TIMESEAL_ENABLED = "fics-Primary-timeseal-enabled";
	public static final String FICS_PRIMARY_USER_NAME = "fics-Primary-user-name";

	public static final String FICS_SECONDARY_IS_ANON_GUEST = "fics-Secondary-is-anon-guest";
	public static final String FICS_SECONDARY_IS_NAMED_GUEST = "fics-Secondary-is-named-guest";
	public static final String FICS_SECONDARY_PASSWORD = "fics-Secondary-password";
	public static final String FICS_SECONDARY_PORT = "fics-Secondary-port";
	public static final String FICS_SECONDARY_SERVER_URL = "fics-Secondary-server-url";
	public static final String FICS_SECONDARY_TIMESEAL_ENABLED = "fics-Secondary-timeseal-enabled";
	public static final String FICS_SECONDARY_USER_NAME = "fics-Secondary-user-name";

	public static final String FICS_TERTIARY_IS_ANON_GUEST = "fics-Tertiary-is-anon-guest";
	public static final String FICS_TERTIARY_IS_NAMED_GUEST = "fics-Tertiary-is-named-guest";
	public static final String FICS_TERTIARY_PASSWORD = "fics-Tertiary-password";
	public static final String FICS_TERTIARY_PORT = "fics-Tertiary-port";
	public static final String FICS_TERTIARY_SERVER_URL = "fics-Tertiary-server-url";
	public static final String FICS_TERTIARY_TIMESEAL_ENABLED = "fics-Tertiary-timeseal-enabled";
	public static final String FICS_TERTIARY_USER_NAME = "fics-Tertiary-user-name";

	public static final String BICS_AUTO_CONNECT = "bics-auto-connect";
	public static final String BICS_SHOW_BUGBUTTONS_ON_CONNECT = "bics-show-bugbuttons-on-connect";
	public static final String BICS_KEEP_ALIVE = "bics-keep-alive";
	public static final String BICS_LOGIN_SCRIPT = "bics-login-script";
	public static final String BICS_PROFILE = "bics-profile";

	public static final String BICS_PRIMARY_IS_ANON_GUEST = "bics-Primary-is-anon-guest";
	public static final String BICS_PRIMARY_IS_NAMED_GUEST = "bics-Primary-is-named-guest";
	public static final String BICS_PRIMARY_PASSWORD = "bics-Primary-password";
	public static final String BICS_PRIMARY_PORT = "bics-Primary-port";
	public static final String BICS_PRIMARY_SERVER_URL = "bics-Primary-server-url";
	public static final String BICS_PRIMARY_TIMESEAL_ENABLED = "bics-Primary-timeseal-enabled";
	public static final String BICS_PRIMARY_USER_NAME = "bics-Primary-user-name";

	public static final String BICS_SECONDARY_IS_ANON_GUEST = "bics-Secondary-is-anon-guest";
	public static final String BICS_SECONDARY_IS_NAMED_GUEST = "bics-Secondary-is-named-guest";
	public static final String BICS_SECONDARY_PASSWORD = "bics-Secondary-password";
	public static final String BICS_SECONDARY_PORT = "bics-Secondary-port";
	public static final String BICS_SECONDARY_SERVER_URL = "bics-Secondary-server-url";
	public static final String BICS_SECONDARY_TIMESEAL_ENABLED = "bics-Secondary-timeseal-enabled";
	public static final String BICS_SECONDARY_USER_NAME = "bics-Secondary-user-name";

	public static final String BICS_TERTIARY_IS_ANON_GUEST = "bics-Tertiary-is-anon-guest";
	public static final String BICS_TERTIARY_IS_NAMED_GUEST = "bics-Tertiary-is-named-guest";
	public static final String BICS_TERTIARY_PASSWORD = "bics-Tertiary-password";
	public static final String BICS_TERTIARY_PORT = "bics-Tertiary-port";
	public static final String BICS_TERTIARY_SERVER_URL = "bics-Tertiary-server-url";
	public static final String BICS_TERTIARY_TIMESEAL_ENABLED = "bics-Tertiary-timeseal-enabled";
	public static final String BICS_TERTIARY_USER_NAME = "bics-Tertiary-user-name";

	public static final String SPEECH_PROCESS_NAME = "speech_process_name";

	public static final String SOUND_PROCESS_NAME = "sound_process_name";

	public static final String TIMESEAL_INIT_STRING = "timeseal-init-string";
}
