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

public interface PreferenceKeys {
	// In order to update entries in the font and color registries, all font
	// properties must end in font and all color properties must end in color.
	public static final String BOARD_CHESS_SET_NAME = "board-chess-set-name";
	public static final String BOARD_SQUARE_BACKGROUND_NAME = "board-square-background-name";
	public static final String BOARD_IS_SHOW_COORDINATES = "board-show-coordinates";
	public static final String BOARD_PIECE_SIZE_ADJUSTMENT = "board-piece-size-adjustment-percentage";
	public static final String BOARD_LAYOUT = "board-layout";
	public static final String BOARD_HIGHLIGHT_BORDER_WIDTH = "board-highlight-width-percentage";
	public static final String BOARD_IS_SHOWING_PIECE_JAIL = "board-is-showing-piece-jail";
	public static final String BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN = "board-clock-show-millis-when-less-than";
	public static final String BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN = "board-clock-show-seconds-when-less-than";
	public static final String BOARD_PREMOVE_ENABLED = "board-premove-enabled";
	public static final String BOARD_SMARTMOVE_ENABLED = "board-smartmove-enabled";
	public static final String BOARD_PLAY_MOVE_SOUND_WHEN_OBSERVING = "board-play-move-sound-when-observing";
	public static final String BOARD_QUEUED_PREMOVE_ENABLED = "board-queued-premove-enabled";
	public static final String BOARD_IS_SHOWING_PIECE_UNICODE_CHARS = "board-is-showing-piece-unicode-chars";
	public static final String BOARD_IS_USING_CROSSHAIRS_CURSOR = "board-is-using-crosshairs-cursor";
	public static final String BOARD_IS_PLAYING_10_SECOND_COUNTDOWN_SOUNDS = "board-is-playing-10-second-countdown-sounds";

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
	public static final String CHAT_UNDERLINE_SINGLE_QUOTES = "chat-underline-single-quotes";
	public static final String CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO = "chat-event-";

	public static final String CHAT_OUTPUT_BACKGROUND_COLOR = "chat-output-background-color";
	public static final String CHAT_INPUT_BACKGROUND_COLOR = "chat-input-background-color";
	public static final String CHAT_OUTPUT_TEXT_COLOR = "chat-output-text-color";
	public static final String CHAT_INPUT_DEFAULT_TEXT_COLOR = "chat-input-default-text-color";
	public static final String CHAT_CONSOLE_BACKGROUND_COLOR = "chat-console-background-color";
	public static final String CHAT_PROMPT_COLOR = "chat-prompt-color";
	public static final String CHAT_QUOTE_UNDERLINE_COLOR = "chat-quote-underline-color";
	public static final String CHAT_LINK_UNDERLINE_COLOR = "chat-link-underline-color";

	public static final String APP_PING_FONT = "app-lag-font";
	public static final String APP_PING_COLOR = "app-lag-color";
	public static final String APP_STATUS_BAR_FONT = "app-status-bar-font";
	public static final String APP_STATUS_BAR_COLOR = "app-starus-bar-color";
	public static final String APP_NAME = "app-name";
	public static final String APP_HOME_URL = "app-home-url";
	public static final String APP_LAYOUT = "app-layout";
	public static final String APP_SASH_WIDTH = "app-sash-width";
	public static final String APP_LINUX_UNIX_BROWSER_NAME = "app-linux-unix-browser-name";
	public static final String APP_OPEN_LINKS_IN_EXTERNAL_BROWSER = "app-open-links-in-external-browser";
	public static final String APP_SOUND_ENABLED = "sound-enabled";

	// ONLY PASS THESE VALUES INTO get/set SashWeights and get/set Quad
	public static final String QUAD1_QUAD234567_QUAD8_SASH_WEIGHTS = "quad1-quad234567-quad8-sash-weights";
	public static final String QUAD2_QUAD234567_SASH_WEIGHTS = "quad2-quad234567-sash-weights";
	public static final String QUAD3_QUAD4_SASH_WEIGHTS = "quad3-quad4-sash-weights";
	public static final String QUAD56_QUAD7_SASH_WEIGHTS = "quad56-quad7-sash-weights";
	public static final String QUAD34_QUAD567_SASH_WEIGHTS = "quad34-quad567-sash-weights";
	public static final String QUAD5_QUAD6_SASH_WEIGHTS = "quad5-quad6-sash-weights";
	public static final String MAIN_TAB_QUADRANT = "main-quadrant";
	public static final String CHANNEL_TAB_QUADRANT = "channel-quadrant";
	public static final String PERSON_TAB_QUADRANT = "person-quadrant";
	public static final String REGEX_TAB_QUADRANT = "regex-quadrant";
	public static final String PARTNER_TELL_TAB_QUADRANT = "partner-quadrant";
	public static final String GAME_QUADRANT = "game-quadrant";
	public static final String BUGHOUSE_GAME_2_QUADRANT = "bughouse-game-2-quadrant";
	public static final String BROWSER_QUADRANT = "browser-quadrant";
	public static final String BUG_ARENA_QUADRANT = "bug-arena-quadrant";
	public static final String SEEK_GRAPH_QUADRANT = "seek-graph-quadrant";
	public static final String BUG_BUTTONS_QUADRANT = "bug-buttons-quadrant";
	public static final String WINDOW_BOUNDS = "window-bounds";

	public static final String APP_LAYOUT1_QUAD1_QUAD234567_QUAD8_SASH_WEIGHTS = "app-Layout1-quad1-quad234567-quad8-sash-weights";
	public static final String APP_LAYOUT1_QUAD2_QUAD234567_SASH_WEIGHTS = "app-Layout1-quad2-quad234567-sash-weights";
	public static final String APP_LAYOUT1_QUAD3_QUAD4_SASH_WEIGHTS = "app-Layout1-quad3-quad4-sash-weights";
	public static final String APP_LAYOUT1_QUAD56_QUAD7_SASH_WEIGHTS = "app-Layout1-quad56-quad7-sash-weights";
	public static final String APP_LAYOUT1_QUAD34_QUAD567_SASH_WEIGHTS = "app-Layout1-quad34-quad567-sash-weights";
	public static final String APP_LAYOUT1_QUAD5_QUAD6_SASH_WEIGHTS = "app-Layout1-quad5-quad6-sash-weights";
	public static final String APP_LAYOUT1_MAIN_TAB_QUADRANT = "app-Layout1-main-quadrant";
	public static final String APP_LAYOUT1_CHANNEL_TAB_QUADRANT = "app-Layout1-channel-quadrant";
	public static final String APP_LAYOUT1_PERSON_TAB_QUADRANT = "app-Layout1-person-quadrant";
	public static final String APP_LAYOUT1_REGEX_TAB_QUADRANT = "app-Layout1-regex-quadrant";
	public static final String APP_LAYOUT1_PARTNER_TELL_TAB_QUADRANT = "app-Layout1-partner-quadrant";
	public static final String APP_LAYOUT1_GAME_QUADRANT = "app-Layout1-game-quadrant";
	public static final String APP_LAYOUT1_BUGHOUSE_GAME_2_QUADRANT = "app-Layout1-bughouse-game-2-quadrant";
	public static final String APP_LAYOUT1_BROWSER_QUADRANT = "app-Layout1-browser-quadrant";
	public static final String APP_LAYOUT1_BUG_ARENA_QUADRANT = "app-Layout1-bug-arena-quadrant";
	public static final String APP_LAYOUT1_SEEK_GRAPH_QUADRANT = "app-Layout1-seek-graph-quadrant";
	public static final String APP_LAYOUT1_BUG_BUTTONS_QUADRANT = "app-Layout1-bug-buttons-quadrant";
	public static final String APP_LAYOUT1_WINDOW_BOUNDS = "app-Layout1-window-bounds";

	public static final String APP_LAYOUT2_QUAD1_QUAD234567_QUAD8_SASH_WEIGHTS = "app-Layout2-quad1-quad234567-quad8-sash-weights";
	public static final String APP_LAYOUT2_QUAD2_QUAD234567_SASH_WEIGHTS = "app-Layout2-quad2-quad234567-sash-weights";
	public static final String APP_LAYOUT2_QUAD3_QUAD4_SASH_WEIGHTS = "app-Layout2-quad3-quad4-sash-weights";
	public static final String APP_LAYOUT2_QUAD56_QUAD7_SASH_WEIGHTS = "app-Layout2-quad56-quad7-sash-weights";
	public static final String APP_LAYOUT2_QUAD34_QUAD567_SASH_WEIGHTS = "app-Layout2-quad34-quad567-sash-weights";
	public static final String APP_LAYOUT2_QUAD5_QUAD6_SASH_WEIGHTS = "app-Layout2-quad5-quad6-sash-weights";
	public static final String APP_LAYOUT2_MAIN_TAB_QUADRANT = "app-Layout2-main-quadrant";
	public static final String APP_LAYOUT2_CHANNEL_TAB_QUADRANT = "app-Layout2-channel-quadrant";
	public static final String APP_LAYOUT2_PERSON_TAB_QUADRANT = "app-Layout2-person-quadrant";
	public static final String APP_LAYOUT2_REGEX_TAB_QUADRANT = "app-Layout2-regex-tab-quadrant";
	public static final String APP_LAYOUT2_PARTNER_TELL_TAB_QUADRANT = "app-Layout2-partner-quadrant";
	public static final String APP_LAYOUT2_GAME_QUADRANT = "app-Layout2-game-quadrant";
	public static final String APP_LAYOUT2_BUGHOUSE_GAME2_QUADRANT = "app-Layout2-bughouse-game-2-quadrant";
	public static final String APP_LAYOUT2_BROWSER_QUADRANT = "app-Layout2-browser-quadrant";
	public static final String APP_LAYOUT2_BUG_ARENA_QUADRANT = "app-Layout2-bug-arena-quadrant";
	public static final String APP_LAYOUT2_SEEK_GRAPH_QUADRANT = "app-Layout2-seek-graph-quadrant";
	public static final String APP_LAYOUT2_BUG_BUTTONS_QUADRANT = "app-Layout2-bug-buttons-quadrant";
	public static final String APP_LAYOUT2_WINDOW_BOUNDS = "app-Layout2-window-bounds";

	public static final String APP_LAYOUT3_QUAD1_QUAD234567_QUAD8_SASH_WEIGHTS = "app-Layout3-quad1-quad234567-quad8-sash-weights";
	public static final String APP_LAYOUT3_QUAD2_QUAD234567_SASH_WEIGHTS = "app-Layout3-quad2-quad234567-sash-weights";
	public static final String APP_LAYOUT3_QUAD3_QUAD4_SASH_WEIGHTS = "app-Layout3-quad3-quad4-sash-weights";
	public static final String APP_LAYOUT3_QUAD56_QUAD7_SASH_WEIGHTS = "app-Layout3-quad56-quad7-sash-weights";
	public static final String APP_LAYOUT3_QUAD34_QUAD567_SASH_WEIGHTS = "app-Layout3-quad34-quad567-sash-weights";
	public static final String APP_LAYOUT3_QUAD5_QUAD6_SASH_WEIGHTS = "app-Layout3-quad5-quad6-sash-weights";
	public static final String APP_LAYOUT3_MAIN_TAB_QUADRANT = "app-Layout3-main-quadrant";
	public static final String APP_LAYOUT3_CHANNEL_TAB_QUADRANT = "app-Layout3-channel-quadrant";
	public static final String APP_LAYOUT3_PERSON_TAB_QUADRANT = "app-Layout3-person-quadrant";
	public static final String APP_LAYOUT3_REGEX_TAB_QUADRANT = "app-Layout3-regex-tab-quadrant";
	public static final String APP_LAYOUT3_PARTNER_TELL_TAB_QUADRANT = "app-Layout3-partner-quadrant";
	public static final String APP_LAYOUT3_GAME_QUADRANT = "app-Layout3-game-quadrant";
	public static final String APP_LAYOUT3_BUGHUOSE_GAME_2_QUADRANT = "app-Layout3-bughouse-game-2-quadrant";
	public static final String APP_LAYOUT3_BROWSER_QUADRANT = "app-Layout3-browser-quadrant";
	public static final String APP_LAYOUT3_BUG_ARENA_QUADRANT = "app-Layout3-bug-arena-quadrant";
	public static final String APP_LAYOUT3_SEEK_GRAPH_QUADRANT = "app-Layout3-seek-graph-quadrant";
	public static final String APP_LAYOUT3_BUG_BUTTONS_QUADRANT = "app-Layout3-bug-buttons-quadrant";
	public static final String APP_LAYOUT3_WINDOW_BOUNDS = "app-Layout3-window-bounds";

	// Connector preferences should always use the short name of the connector
	// followed by the preference.
	public static final String FICS_KEEP_ALIVE = "fics-keepalive";
	public static final String FICS_AUTO_CONNECT = "fics-auto-connect";
	public static final String FICS_LOGIN_SCRIPT = "fics-login-script";
	public static final String FICS_IS_LOGGING_GAMES = "fics-is-logging-games";
	public static final String FICS_PROFILE = "fics-profile";
	public static final String FICS_COMMANDS_HELP_URL = "fics-commands-help-url";
	public static final String FICS_FREECHESS_ORG_URL = "fics-freechess-org-url";
	public static final String FICS_FICS_GAMES_URL = "fics-games-url";
	public static final String FICS_ADJUDICATE_URL = "fics-adjudicate-url";
	public static final String FICS_TEAM_LEAGUE_URL = "fics-team-league-url";

	public static final String FICS_PRIMARY_SERVER_URL = "fics-Primary-server-url";
	public static final String FICS_PRIMARY_PORT = "fics-Primary-port";
	public static final String FICS_PRIMARY_USER_NAME = "fics-Primary-user-name";
	public static final String FICS_PRIMARY_PASSWORD = "fics-Primary-password";
	public static final String FICS_PRIMARY_IS_NAMED_GUEST = "fics-Primary-is-named-guest";
	public static final String FICS_PRIMARY_IS_ANON_GUEST = "fics-Primary-is-anon-guest";
	public static final String FICS_PRIMARY_TIMESEAL_ENABLED = "fics-Primary-timeseal-enabled";

	public static final String FICS_SECONDARY_SERVER_URL = "fics-Secondary-server-url";
	public static final String FICS_SECONDARY_PORT = "fics-Secondary-port";
	public static final String FICS_SECONDARY_USER_NAME = "fics-Secondary-user-name";
	public static final String FICS_SECONDARY_PASSWORD = "fics-Secondary-password";
	public static final String FICS_SECONDARY_IS_NAMED_GUEST = "fics-Secondary-is-named-guest";
	public static final String FICS_SECONDARY_IS_ANON_GUEST = "fics-Secondary-is-anon-guest";
	public static final String FICS_SECONDARY_TIMESEAL_ENABLED = "fics-Secondary-timeseal-enabled";

	public static final String FICS_TERTIARY_SERVER_URL = "fics-Tertiary-server-url";
	public static final String FICS_TERTIARY_PORT = "fics-Tertiary-port";
	public static final String FICS_TERTIARY_USER_NAME = "fics-Tertiary-user-name";
	public static final String FICS_TERTIARY_PASSWORD = "fics-Tertiary-password";
	public static final String FICS_TERTIARY_IS_NAMED_GUEST = "fics-Tertiary-is-named-guest";
	public static final String FICS_TERTIARY_IS_ANON_GUEST = "fics-Tertiary-is-anon-guest";
	public static final String FICS_TERTIARY_TIMESEAL_ENABLED = "fics-Tertiary-timeseal-enabled";

	public static final String BICS_KEEP_ALIVE = "bics-keepalive";
	public static final String BICS_AUTO_CONNECT = "bics-auto-connect";
	public static final String BICS_LOGIN_SCRIPT = "bics-login-script";
	public static final String BICS_IS_LOGGING_GAMES = "bics-is-logging-games";
	public static final String BICS_PROFILE = "bics-profile";

	public static final String BICS_PRIMARY_SERVER_URL = "bics-Primary-server-url";
	public static final String BICS_PRIMARY_PORT = "bics-Primary-port";
	public static final String BICS_PRIMARY_USER_NAME = "bics-Primary-user-name";
	public static final String BICS_PRIMARY_PASSWORD = "bics-Primary-password";
	public static final String BICS_PRIMARY_IS_NAMED_GUEST = "bics-Primary-is-named-guest";
	public static final String BICS_PRIMARY_IS_ANON_GUEST = "bics-Primary-is-anon-guest";
	public static final String BICS_PRIMARY_TIMESEAL_ENABLED = "bics-Primary-timeseal-enabled";

	public static final String BICS_SECONDARY_SERVER_URL = "bics-Secondary-server-url";
	public static final String BICS_SECONDARY_PORT = "bics-Secondary-port";
	public static final String BICS_SECONDARY_USER_NAME = "bics-Secondary-user-name";
	public static final String BICS_SECONDARY_PASSWORD = "bics-Secondary-password";
	public static final String BICS_SECONDARY_IS_NAMED_GUEST = "bics-Secondary-is-named-guest";
	public static final String BICS_SECONDARY_IS_ANON_GUEST = "bics-Secondary-is-anon-guest";
	public static final String BICS_SECONDARY_TIMESEAL_ENABLED = "bics-Secondary-timeseal-enabled";

	public static final String BICS_TERTIARY_SERVER_URL = "bics-Tertiary-server-url";
	public static final String BICS_TERTIARY_PORT = "bics-Tertiary-port";
	public static final String BICS_TERTIARY_USER_NAME = "bics-Tertiary-user-name";
	public static final String BICS_TERTIARY_PASSWORD = "bics-Tertiary-password";
	public static final String BICS_TERTIARY_IS_NAMED_GUEST = "bics-Tertiary-is-named-guest";
	public static final String BICS_TERTIARY_IS_ANON_GUEST = "bics-Tertiary-is-anon-guest";
	public static final String BICS_TERTIARY_TIMESEAL_ENABLED = "bics-Tertiary-timeseal-enabled";

	public static final String TIMESEAL_INIT_STRING = "timeseal-init-string";
}
