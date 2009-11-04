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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.swt.BugPartnersWindowItem;
import raptor.swt.SWTUtils;
import raptor.swt.SeekTableWindowItem;
import raptor.util.RaptorStringUtils;

/**
 * The RaptorPreferenceStore. Automatically loads and saves itself at
 * Raptor.USER_RAPTOR_DIR/raptor.properties . Had additional data type support.
 */
public class RaptorPreferenceStore extends PreferenceStore implements
		PreferenceKeys {
	private static final Log LOG = LogFactory
			.getLog(RaptorPreferenceStore.class);
	public static final String PREFERENCE_PROPERTIES_FILE = "raptor.properties";
	public static final File RAPTOR_PROPERTIES = new File(
			Raptor.USER_RAPTOR_DIR, "raptor.properties");

	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent arg0) {
			if (arg0.getProperty().endsWith("color")) {
				Raptor.getInstance().getColorRegistry()
						.put(
								arg0.getProperty(),
								PreferenceConverter.getColor(
										RaptorPreferenceStore.this, arg0
												.getProperty()));
			} else if (arg0.getProperty().endsWith("font")) {
				Raptor.getInstance().getFontRegistry()
						.put(
								arg0.getProperty(),
								PreferenceConverter.getFontDataArray(
										RaptorPreferenceStore.this, arg0
												.getProperty()));
			}
		}
	};

	public RaptorPreferenceStore() {
		super();
		try {
			LOG.info("Loading RaptorPreferenceStore store "
					+ PREFERENCE_PROPERTIES_FILE);
			loadDefaults();
			if (RAPTOR_PROPERTIES.exists()) {
				load(new FileInputStream(RAPTOR_PROPERTIES));
			} else {
				RAPTOR_PROPERTIES.getParentFile().mkdir();
				RAPTOR_PROPERTIES.createNewFile();
				save(new FileOutputStream(RAPTOR_PROPERTIES), "Last saved on "
						+ new Date());
			}
			setFilename(RAPTOR_PROPERTIES.getAbsolutePath());
		} catch (Exception e) {
			LOG.error("Error reading or writing to file ", e);
			throw new RuntimeException(e);
		}

		addPropertyChangeListener(propertyChangeListener);
		LOG.info("Loaded preferences from "
				+ RAPTOR_PROPERTIES.getAbsolutePath());
	}

	/**
	 * Returns the foreground color to use for the specified chat event. Returns
	 * null if no special color should be used.
	 */
	public Color getColor(ChatEvent event) {
		Color result = null;

		String key = null;
		if (event.getType() == ChatType.CHANNEL_TELL) {
			key = CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + event.getType() + "-"
					+ event.getChannel() + "-color";
		} else {
			key = CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + event.getType()
					+ "-color";
		}
		try {
			if (!Raptor.getInstance().getColorRegistry().hasValueFor(key)) {
				// We don't want the default color if not found we want to
				// return null, so use
				// StringConverter instead of PreferenceConverter.
				String value = getString(key);
				if (StringUtils.isNotBlank(value)) {
					RGB rgb = StringConverter.asRGB(value, null);
					if (rgb != null) {
						Raptor.getInstance().getColorRegistry().put(key, rgb);
					} else {
						return null;
					}
				} else {
					return null;
				}
			}
			result = Raptor.getInstance().getColorRegistry().get(key);
		} catch (Throwable t) {
			result = null;
		}
		return result;
	}

	/**
	 * Returns the color for the specified key. Returns BLACK if the key was not
	 * found.
	 */
	public Color getColor(String key) {
		try {
			if (!Raptor.getInstance().getColorRegistry().hasValueFor(key)) {
				RGB rgb = PreferenceConverter.getColor(this, key);
				if (rgb != null) {
					Raptor.getInstance().getColorRegistry().put(key, rgb);
				}
			}
			return Raptor.getInstance().getColorRegistry().get(key);
		} catch (Throwable t) {
			LOG.error("Error in getColor(" + key + ") Returning black.", t);
			return new Color(Display.getCurrent(), new RGB(0, 0, 0));
		}
	}

	public Rectangle getCurrentLayoutRectangle(String key) {
		key = "app-" + getString(APP_LAYOUT) + "-" + key;
		return getRectangle(key);
	}

	public int[] getCurrentLayoutSashWeights(String key) {
		key = "app-" + getString(APP_LAYOUT) + "-" + key;
		return getIntArray(key);
	}

	public RGB getDefaultColor(String key) {
		return PreferenceConverter.getDefaultColor(this, key);
	}

	public int[] getDefaultIntArray(String key) {
		return RaptorStringUtils.intArrayFromString(getDefaultString(key));
	}

	public String[] getDefaultStringArray(String key) {
		return RaptorStringUtils.stringArrayFromString(getDefaultString(key));
	}

	public String getDefauultMonospacedFont() {
		FontData[] fonts = Raptor.getInstance().getDisplay().getFontList(null,
				true);
		String[] preferredFontNames = null;

		String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
			preferredFontNames = SWTUtils.OSX_MONOSPACED_FONTS;
		} else if (osName.startsWith("Windows")) {
			preferredFontNames = SWTUtils.WINDOWS_MONOSPACED_FONTS;
		} else {
			preferredFontNames = SWTUtils.OTHER_MONOSPACED_FONTS;
		}

		String result = null;
		outer: for (int i = 0; i < preferredFontNames.length; i++) {
			for (FontData fontData : fonts) {
				if (fontData.getName().equalsIgnoreCase(preferredFontNames[i])) {
					result = preferredFontNames[i];
					break outer;
				}
			}
		}
		if (result == null) {
			result = "Courier";
		}

		return result;
	}

	/**
	 * Returns the font for the specified key. Returns the default font if key
	 * was not found.
	 */
	public Font getFont(String key) {
		try {
			if (!Raptor.getInstance().getFontRegistry().hasValueFor(key)) {
				FontData[] fontData = PreferenceConverter.getFontDataArray(
						this, key);
				Raptor.getInstance().getFontRegistry().put(key, fontData);
			}
			return Raptor.getInstance().getFontRegistry().get(key);
		} catch (Throwable t) {
			LOG.error("Error in getFont(" + key + ") Returning default font.",
					t);
			return Raptor.getInstance().getFontRegistry().defaultFont();
		}
	}

	public int[] getIntArray(String key) {
		return RaptorStringUtils.intArrayFromString(getString(key));
	}

	public Point getPoint(String key) {
		return PreferenceConverter.getPoint(this, key);
	}

	public Quadrant getQuadrant(String key) {
		return Quadrant.valueOf(getString(key));
	}

	public Rectangle getRectangle(String key) {
		return PreferenceConverter.getRectangle(this, key);
	}

	public String[] getStringArray(String key) {
		return RaptorStringUtils.stringArrayFromString(getString(key));
	}

	public void loadDefaults() {
		String defaultFontName = Raptor.getInstance().getFontRegistry()
				.defaultFont().getFontData()[0].getName();
		String defaultMonospacedFontName = getDefauultMonospacedFont();

		// Action
		setDefault(ACTION_SEPARATOR_SEQUENCE, 200);

		// Board
		setDefault(BOARD_CHESS_SET_NAME, "Fantasy");
		setDefault(BOARD_SQUARE_BACKGROUND_NAME, "Wood2");
		setDefault(BOARD_IS_SHOW_COORDINATES, true);
		setDefault(BOARD_PIECE_SIZE_ADJUSTMENT, .06);
		setDefault(BOARD_IS_SHOWING_PIECE_JAIL, true);
		setDefault(BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN, 1000L * 10L + 1L);
		setDefault(BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN,
				1000L * 60L * 10L + 1L);
		setDefault(BOARD_IS_PLAYING_10_SECOND_COUNTDOWN_SOUNDS, true);
		setDefault(BOARD_PREMOVE_ENABLED, true);
		setDefault(BOARD_SMARTMOVE_ENABLED, true);
		setDefault(BOARD_PLAY_MOVE_SOUND_WHEN_OBSERVING, true);
		setDefault(BOARD_IS_SHOWING_PIECE_UNICODE_CHARS, true);
		setDefault(BOARD_QUEUED_PREMOVE_ENABLED, true);
		setDefault(BOARD_IS_USING_CROSSHAIRS_CURSOR, false);
		setDefault(BOARD_LAYOUT, "raptor.swt.chess.layout.RightOrientedLayout");
		setDefault(BOARD_TAKEOVER_INACTIVE_GAMES, true);
		setDefault(BOARD_PIECE_JAIL_SHADOW_ALPHA, 30);
		setDefault(BOARD_PIECE_SHADOW_ALPHA, 60);
		setDefault(BOARD_COORDINATES_SIZE_PERCENTAGE, 26);

		PreferenceConverter.setDefault(this, BOARD_BACKGROUND_COLOR, new RGB(0,
				0, 0));
		PreferenceConverter.setDefault(this, BOARD_COORDINATES_COLOR, new RGB(
				0, 0, 0));
		PreferenceConverter.setDefault(this, BOARD_ACTIVE_CLOCK_COLOR, new RGB(
				0, 255, 0));
		PreferenceConverter.setDefault(this, BOARD_INACTIVE_CLOCK_COLOR,
				new RGB(128, 128, 128));
		PreferenceConverter.setDefault(this, BOARD_LAG_COLOR, new RGB(128, 128,
				128));
		PreferenceConverter.setDefault(this, BOARD_LAG_OVER_20_SEC_COLOR,
				new RGB(255, 0, 0));
		PreferenceConverter.setDefault(this, BOARD_PLAYER_NAME_COLOR, new RGB(
				128, 128, 128));
		PreferenceConverter.setDefault(this, BOARD_PIECE_JAIL_LABEL_COLOR,
				new RGB(0, 255, 0));
		PreferenceConverter.setDefault(this, BOARD_PIECE_JAIL_BACKGROUND_COLOR,
				new RGB(0, 0, 0));
		PreferenceConverter.setDefault(this, BOARD_OPENING_DESC_COLOR, new RGB(
				128, 128, 128));
		PreferenceConverter.setDefault(this, BOARD_GAME_DESCRIPTION_COLOR,
				new RGB(128, 128, 128));
		PreferenceConverter.setDefault(this, BOARD_PREMOVES_COLOR, new RGB(128,
				128, 128));
		PreferenceConverter.setDefault(this, BOARD_STATUS_COLOR, new RGB(128,
				128, 128));
		PreferenceConverter.setDefault(this, BOARD_RESULT_COLOR, new RGB(255,
				0, 0));

		PreferenceConverter.setDefault(this, BOARD_COORDINATES_FONT,
				new FontData[] { new FontData(defaultFontName, 14, 0) });
		PreferenceConverter.setDefault(this, BOARD_CLOCK_FONT,
				new FontData[] { new FontData(defaultFontName, 24, SWT.BOLD) });
		PreferenceConverter.setDefault(this, BOARD_LAG_FONT,
				new FontData[] { new FontData(defaultFontName, 10, 0) });
		PreferenceConverter.setDefault(this, BOARD_PLAYER_NAME_FONT,
				new FontData[] { new FontData(defaultFontName, 14, 0) });
		PreferenceConverter.setDefault(this, BOARD_PIECE_JAIL_FONT,
				new FontData[] { new FontData(defaultFontName, 14, 0) });
		PreferenceConverter.setDefault(this, BOARD_OPENING_DESC_FONT,
				new FontData[] { new FontData(defaultFontName, 10, 0) });
		PreferenceConverter.setDefault(this, BOARD_STATUS_FONT,
				new FontData[] { new FontData(defaultFontName, 10, 0) });
		PreferenceConverter.setDefault(this, BOARD_GAME_DESCRIPTION_FONT,
				new FontData[] { new FontData(defaultFontName, 12, 0) });
		PreferenceConverter.setDefault(this, BOARD_PREMOVES_FONT,
				new FontData[] { new FontData(defaultFontName, 12, 0) });
		PreferenceConverter.setDefault(this, BOARD_RESULT_FONT,
				new FontData[] { new FontData(defaultMonospacedFontName, 40,
						SWT.BOLD) });

		// BugArena
		setDefault(BUG_ARENA_PARTNERS_INDEX, 0);
		setDefault(BUG_ARENA_MAX_PARTNERS_INDEX, BugPartnersWindowItem
				.getRatings().length - 1);
		setDefault(BUG_ARENA_TEAMS_INDEX, 0);
		setDefault(BUG_ARENA_TEAMS_IS_RATED, true);

		// SeekTable
		setDefault(SEEK_TABLE_RATINGS_INDEX, 0);
		setDefault(SEEK_TABLE_MAX_RATINGS_INDEX, SeekTableWindowItem
				.getRatings().length - 1);
		setDefault(SEEK_TABLE_RATED_INDEX, 0);
		setDefault(SEEK_TABLE_SHOW_COMPUTERS, true);
		setDefault(SEEK_TABLE_SHOW_LIGHTNING, true);
		setDefault(SEEK_TABLE_SHOW_BLITZ, true);
		setDefault(SEEK_TABLE_SHOW_STANDARD, true);
		setDefault(SEEK_TABLE_SHOW_CRAZYHOUSE, true);
		setDefault(SEEK_TABLE_SHOW_FR, true);
		setDefault(SEEK_TABLE_SHOW_WILD, true);
		setDefault(SEEK_TABLE_SHOW_ATOMIC, true);
		setDefault(SEEK_TABLE_SHOW_SUICIDE, true);
		setDefault(SEEK_TABLE_SHOW_LOSERS, true);
		setDefault(SEEK_TABLE_SHOW_UNTIMED, true);

		PreferenceConverter.setDefault(this, ARROW_OPPONENT_COLOR, new RGB(255,
				0, 255));
		PreferenceConverter
				.setDefault(this, ARROW_MY_COLOR, new RGB(0, 0, 255));
		PreferenceConverter.setDefault(this, ARROW_OBS_COLOR,
				new RGB(0, 0, 255));
		setDefault(ARROW_SHOW_ON_OBS_MOVES, true);
		setDefault(ARROW_SHOW_ON_MOVE_LIST_MOVES, true);
		setDefault(ARROW_SHOW_ON_MY_PREMOVES, true);
		setDefault(ARROW_SHOW_ON_MY_MOVES, false);
		setDefault(ARROW_ANIMATION_DELAY, 300L);
		setDefault(ARROW_FADE_AWAY_MODE, true);
		setDefault(ARROW_WIDTH_PERCENTAGE, 15);

		PreferenceConverter.setDefault(this, HIGHLIGHT_OPPONENT_COLOR, new RGB(
				255, 0, 255));
		PreferenceConverter.setDefault(this, HIGHLIGHT_MY_COLOR, new RGB(0, 0,
				255));
		PreferenceConverter.setDefault(this, HIGHLIGHT_OBS_COLOR, new RGB(0, 0,
				255));
		setDefault(HIGHLIGHT_SHOW_ON_OBS_MOVES, true);
		setDefault(HIGHLIGHT_SHOW_ON_MOVE_LIST_MOVES, true);
		setDefault(HIGHLIGHT_SHOW_ON_MY_PREMOVES, true);
		setDefault(HIGHLIGHT_SHOW_ON_MY_MOVES, false);
		setDefault(HIGHLIGHT_FADE_AWAY_MODE, false);
		setDefault(HIGHLIGHT_ANIMATION_DELAY, 300L);
		setDefault(HIGHLIGHT_WIDTH_PERCENTAGE, 3);

		// Chat
		setDefault(CHAT_MAX_CONSOLE_CHARS, 500000);
		setDefault(CHAT_TIMESTAMP_CONSOLE, false);
		setDefault(CHAT_TIMESTAMP_CONSOLE_FORMAT, "'['hh:mma']'");
		setDefault(CHAT_UNDERLINE_SINGLE_QUOTES, false);
		setDefault(CHAT_IS_PLAYING_CHAT_ON_PTELL, true);
		setDefault(CHAT_IS_PLAYING_CHAT_ON_PERSON_TELL, true);
		setDefault(CHAT_IS_SMART_SCROLL_ENABLED, true);

		PreferenceConverter
				.setDefault(this, CHAT_INPUT_FONT,
						new FontData[] { new FontData(
								defaultMonospacedFontName, 16, 0) });
		PreferenceConverter
				.setDefault(this, CHAT_OUTPUT_FONT,
						new FontData[] { new FontData(
								defaultMonospacedFontName, 16, 0) });
		PreferenceConverter
				.setDefault(this, CHAT_PROMPT_FONT,
						new FontData[] { new FontData(
								defaultMonospacedFontName, 16, 0) });

		PreferenceConverter.setDefault(this, CHAT_INPUT_BACKGROUND_COLOR,
				new RGB(0, 0, 0));
		PreferenceConverter.setDefault(this, CHAT_CONSOLE_BACKGROUND_COLOR,
				new RGB(0, 0, 0));
		PreferenceConverter.setDefault(this, CHAT_INPUT_DEFAULT_TEXT_COLOR,
				new RGB(128, 128, 128));
		PreferenceConverter.setDefault(this, CHAT_OUTPUT_BACKGROUND_COLOR,
				new RGB(255, 255, 255));
		PreferenceConverter.setDefault(this, CHAT_OUTPUT_TEXT_COLOR, new RGB(0,
				0, 0));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.CHALLENGE
						+ "-color", new RGB(100, 149, 237));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.CSHOUT
						+ "-color", new RGB(221, 160, 221));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.SHOUT
						+ "-color", new RGB(221, 160, 221));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.KIBITZ
						+ "-color", new RGB(100, 149, 237));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.WHISPER
						+ "-color", new RGB(100, 149, 237));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.OUTBOUND
						+ "-color", new RGB(128, 128, 128));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.PARTNER_TELL
						+ "-color", new RGB(255, 0, 0));
		PreferenceConverter
				.setDefault(this, CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.TELL + "-color", new RGB(255, 0, 0));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.CHANNEL_TELL
						+ "-" + 1 + "-color", new RGB(255, 200, 0));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.CHANNEL_TELL
						+ "-" + 4 + "-color", new RGB(0, 255, 0));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.CHANNEL_TELL
						+ "-" + 50 + "-color", new RGB(255, 175, 175));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.CHANNEL_TELL
						+ "-" + 53 + "-color", new RGB(255, 0, 255));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.INTERNAL
						+ "-color", new RGB(255, 0, 0));
		PreferenceConverter.setDefault(this, CHAT_PROMPT_COLOR, new RGB(128,
				128, 128));
		PreferenceConverter.setDefault(this, CHAT_QUOTE_UNDERLINE_COLOR,
				new RGB(0, 255, 0));
		PreferenceConverter.setDefault(this, CHAT_LINK_UNDERLINE_COLOR,
				new RGB(0, 0, 238));

		// Bug house buttons settings.
		PreferenceConverter.setDefault(this, BUG_BUTTONS_FONT,
				new FontData[] { new FontData(defaultFontName, 16, SWT.BOLD) });
		// Bug house
		setDefault(BUGHOUSE_PLAYING_OPEN_PARTNER_BOARD, true);
		setDefault(BUGHOUSE_OBSERVING_OPEN_PARTNER_BOARD, true);

		// App settings.
		setDefault(APP_NAME, "Raptor v.Alpha");
		setDefault(APP_SASH_WIDTH, 8);
		PreferenceConverter.setDefault(this, APP_PING_FONT,
				new FontData[] { new FontData(defaultFontName, 12, 0) });
		PreferenceConverter.setDefault(this, APP_PING_COLOR, new RGB(0, 0, 0));

		PreferenceConverter.setDefault(this, APP_STATUS_BAR_FONT,
				new FontData[] { new FontData(defaultFontName, 12, 0) });
		PreferenceConverter.setDefault(this, APP_STATUS_BAR_COLOR, new RGB(0,
				0, 0));

		setDefault(APP_HOME_URL,
				"http://code.google.com/p/raptor-chess-interface/");
		setDefault(APP_SOUND_ENABLED, true);
		setDefault(APP_IS_LOGGING_GAMES, true);
		setDefault(APP_LAYOUT, "Layout1");
		setDefault(APP_OPEN_LINKS_IN_EXTERNAL_BROWSER, false);
		setDefault(APP_BROWSER_QUADRANT, Quadrant.III);
		setDefault(APP_PGN_RESULTS_QUADRANT, Quadrant.III);
		setDefault(APP_CHESS_BOARD_QUADRANT, Quadrant.III);
		setDefault(APP_BUGHOUSE_GAME_2_QUADRANT, Quadrant.IV);
		setDefault(APP_OPEN_LINKS_IN_EXTERNAL_BROWSER, false);
		setDefault(APP_IS_LAUNCHNG_HOME_PAGE, true);
		setDefault(APP_WINDOW_ITEM_POLL_INTERVAL, 3);

		// Layout 1 settings.
		setDefault(APP_LAYOUT1_WINDOW_BOUNDS, new Rectangle(0, 0, -1, -1));
		setDefault(APP_LAYOUT1_QUAD1_QUAD234567_QUAD8_SASH_WEIGHTS, new int[] {
				10, 80, 10 });
		setDefault(APP_LAYOUT1_QUAD2_QUAD234567_SASH_WEIGHTS, new int[] { 10,
				90 });
		setDefault(APP_LAYOUT1_QUAD3_QUAD4_SASH_WEIGHTS, new int[] { 50, 50 });
		setDefault(APP_LAYOUT1_QUAD56_QUAD7_SASH_WEIGHTS, new int[] { 70, 30 });
		setDefault(APP_LAYOUT1_QUAD34_QUAD567_SASH_WEIGHTS,
				new int[] { 50, 50 });
		setDefault(APP_LAYOUT1_QUAD5_QUAD6_SASH_WEIGHTS, new int[] { 50, 50 });

		// Layout 2 settings.
		setDefault(APP_LAYOUT2_WINDOW_BOUNDS, new Rectangle(0, 0, -1, -1));
		setDefault(APP_LAYOUT2_QUAD1_QUAD234567_QUAD8_SASH_WEIGHTS, new int[] {
				10, 80, 10 });
		setDefault(APP_LAYOUT2_QUAD2_QUAD234567_SASH_WEIGHTS, new int[] { 10,
				90 });
		setDefault(APP_LAYOUT2_QUAD3_QUAD4_SASH_WEIGHTS, new int[] { 50, 50 });
		setDefault(APP_LAYOUT2_QUAD56_QUAD7_SASH_WEIGHTS, new int[] { 70, 30 });
		setDefault(APP_LAYOUT2_QUAD34_QUAD567_SASH_WEIGHTS,
				new int[] { 50, 50 });
		setDefault(APP_LAYOUT2_QUAD5_QUAD6_SASH_WEIGHTS, new int[] { 50, 50 });

		// Layout 3 settings.
		setDefault(APP_LAYOUT3_WINDOW_BOUNDS, new Rectangle(0, 0, -1, -1));
		setDefault(APP_LAYOUT3_QUAD1_QUAD234567_QUAD8_SASH_WEIGHTS, new int[] {
				10, 80, 10 });
		setDefault(APP_LAYOUT3_QUAD2_QUAD234567_SASH_WEIGHTS, new int[] { 10,
				90 });
		setDefault(APP_LAYOUT3_QUAD3_QUAD4_SASH_WEIGHTS, new int[] { 50, 50 });
		setDefault(APP_LAYOUT3_QUAD56_QUAD7_SASH_WEIGHTS, new int[] { 70, 30 });
		setDefault(APP_LAYOUT3_QUAD34_QUAD567_SASH_WEIGHTS,
				new int[] { 50, 50 });
		setDefault(APP_LAYOUT3_QUAD5_QUAD6_SASH_WEIGHTS, new int[] { 50, 50 });

		// Fics
		setDefault(FICS_KEEP_ALIVE, false);
		setDefault(FICS_AUTO_CONNECT, false);
		setDefault(FICS_LOGIN_SCRIPT, "set seek 0\nset autoflag 1\n\n");
		setDefault(FICS_SHOW_BUGBUTTONS_ON_CONNECT, false);
		setDefault(FICS_AUTO_CONNECT, false);
		setDefault(FICS_PROFILE, "Primary");

		// Fics Primary
		setDefault(FICS_PRIMARY_USER_NAME, "");
		setDefault(FICS_PRIMARY_PASSWORD, "");
		setDefault(FICS_PRIMARY_IS_NAMED_GUEST, false);
		setDefault(FICS_PRIMARY_IS_ANON_GUEST, false);
		setDefault(FICS_PRIMARY_SERVER_URL, "freechess.org");
		setDefault(FICS_PRIMARY_PORT, 5000);
		setDefault(FICS_PRIMARY_TIMESEAL_ENABLED, true);
		// Fics Secondary
		setDefault(FICS_SECONDARY_USER_NAME, "");
		setDefault(FICS_SECONDARY_PASSWORD, "");
		setDefault(FICS_SECONDARY_IS_NAMED_GUEST, false);
		setDefault(FICS_SECONDARY_IS_ANON_GUEST, false);
		setDefault(FICS_SECONDARY_SERVER_URL, "freechess.org");
		setDefault(FICS_SECONDARY_PORT, 5000);
		setDefault(FICS_SECONDARY_TIMESEAL_ENABLED, true);
		// Fics Tertiary
		setDefault(FICS_TERTIARY_USER_NAME, "");
		setDefault(FICS_TERTIARY_PASSWORD, "");
		setDefault(FICS_TERTIARY_IS_NAMED_GUEST, false);
		setDefault(FICS_TERTIARY_IS_ANON_GUEST, false);
		setDefault(FICS_TERTIARY_SERVER_URL, "freechess.org");
		setDefault(FICS_TERTIARY_PORT, 5000);
		setDefault(FICS_TERTIARY_TIMESEAL_ENABLED, true);

		// Bics
		setDefault(BICS_KEEP_ALIVE, false);
		setDefault(BICS_AUTO_CONNECT, false);
		setDefault(BICS_LOGIN_SCRIPT, "set autoflag 1\n\n");
		setDefault(BICS_AUTO_CONNECT, false);
		setDefault(BICS_SHOW_BUGBUTTONS_ON_CONNECT, false);
		setDefault(BICS_PROFILE, "Primary");
		// Bics Primary
		setDefault(BICS_PRIMARY_USER_NAME, "");
		setDefault(BICS_PRIMARY_PASSWORD, "");
		setDefault(BICS_PRIMARY_IS_NAMED_GUEST, false);
		setDefault(BICS_PRIMARY_IS_ANON_GUEST, false);
		setDefault(BICS_PRIMARY_SERVER_URL, "chess.sipay.ru");
		setDefault(BICS_PRIMARY_PORT, 5000);
		setDefault(BICS_PRIMARY_TIMESEAL_ENABLED, true);
		// Bics Secondary
		setDefault(BICS_SECONDARY_USER_NAME, "");
		setDefault(BICS_SECONDARY_PASSWORD, "");
		setDefault(BICS_SECONDARY_IS_NAMED_GUEST, false);
		setDefault(BICS_SECONDARY_IS_ANON_GUEST, false);
		setDefault(BICS_SECONDARY_SERVER_URL, "chess.sipay.ru");
		setDefault(BICS_SECONDARY_PORT, 5000);
		setDefault(BICS_SECONDARY_TIMESEAL_ENABLED, true);
		// Bics Tertiary
		setDefault(BICS_TERTIARY_USER_NAME, "");
		setDefault(BICS_TERTIARY_PASSWORD, "");
		setDefault(BICS_TERTIARY_IS_NAMED_GUEST, false);
		setDefault(BICS_TERTIARY_IS_ANON_GUEST, false);
		setDefault(BICS_TERTIARY_SERVER_URL, "chess.sipay.ru");
		setDefault(BICS_TERTIARY_PORT, 5000);
		setDefault(BICS_TERTIARY_TIMESEAL_ENABLED, true);

		// Quadrant settings.
		setDefault("fics-" + MAIN_TAB_QUADRANT, Quadrant.V);
		setDefault("fics-" + CHANNEL_TAB_QUADRANT, Quadrant.V);
		setDefault("fics-" + PERSON_TAB_QUADRANT, Quadrant.V);
		setDefault("fics-" + REGEX_TAB_QUADRANT, Quadrant.V);
		setDefault("fics-" + PARTNER_TELL_TAB_QUADRANT, Quadrant.V);
		setDefault("fics-" + CHESS_BOARD_QUADRANT, Quadrant.III);
		setDefault("fics-" + BUGHOUSE_GAME_2_QUADRANT, Quadrant.IV);
		setDefault("fics-" + BUG_WHO_QUADRANT, Quadrant.VII);
		setDefault("fics-" + SEEK_TABLE_QUADRANT, Quadrant.VII);
		setDefault("fics-" + BUG_BUTTONS_QUADRANT, Quadrant.II);

		setDefault("fics2-" + MAIN_TAB_QUADRANT, Quadrant.VI);
		setDefault("fics2-" + CHANNEL_TAB_QUADRANT, Quadrant.VI);
		setDefault("fics2-" + PERSON_TAB_QUADRANT, Quadrant.VI);
		setDefault("fics2-" + REGEX_TAB_QUADRANT, Quadrant.VI);
		setDefault("fics2-" + PARTNER_TELL_TAB_QUADRANT, Quadrant.VI);
		setDefault("fics2-" + CHESS_BOARD_QUADRANT, Quadrant.III);
		setDefault("fics2-" + BUGHOUSE_GAME_2_QUADRANT, Quadrant.IV);
		setDefault("fics2-" + BUG_WHO_QUADRANT, Quadrant.VII);
		setDefault("fics2-" + SEEK_TABLE_QUADRANT, Quadrant.VII);
		setDefault("fics2-" + BUG_BUTTONS_QUADRANT, Quadrant.II);

		setDefault("bics-" + MAIN_TAB_QUADRANT, Quadrant.V);
		setDefault("bics-" + CHANNEL_TAB_QUADRANT, Quadrant.V);
		setDefault("bics-" + PERSON_TAB_QUADRANT, Quadrant.V);
		setDefault("bics-" + REGEX_TAB_QUADRANT, Quadrant.V);
		setDefault("bics-" + PARTNER_TELL_TAB_QUADRANT, Quadrant.V);
		setDefault("bics-" + CHESS_BOARD_QUADRANT, Quadrant.III);
		setDefault("bics-" + BUGHOUSE_GAME_2_QUADRANT, Quadrant.IV);
		setDefault("bics-" + BUG_WHO_QUADRANT, Quadrant.VII);
		setDefault("bics-" + SEEK_TABLE_QUADRANT, Quadrant.VII);
		setDefault("bics-" + BUG_BUTTONS_QUADRANT, Quadrant.II);

		setDefault("bics2-" + MAIN_TAB_QUADRANT, Quadrant.VI);
		setDefault("bics2-" + CHANNEL_TAB_QUADRANT, Quadrant.VI);
		setDefault("bics2-" + PERSON_TAB_QUADRANT, Quadrant.VI);
		setDefault("bics2-" + REGEX_TAB_QUADRANT, Quadrant.VI);
		setDefault("bics2-" + PARTNER_TELL_TAB_QUADRANT, Quadrant.VI);
		setDefault("bics2-" + CHESS_BOARD_QUADRANT, Quadrant.III);
		setDefault("bics2-" + BUGHOUSE_GAME_2_QUADRANT, Quadrant.IV);
		setDefault("bics2-" + BUG_WHO_QUADRANT, Quadrant.VII);
		setDefault("bics2-" + SEEK_TABLE_QUADRANT, Quadrant.VII);
		setDefault("bics2-" + BUG_BUTTONS_QUADRANT, Quadrant.II);

		setDefault(TIMESEAL_INIT_STRING, "TIMESTAMP|iv|OpenSeal|");

		LOG.info("Loaded defaults " + PREFERENCE_PROPERTIES_FILE);
	}

	@Override
	public void save() {
		try {
			super.save();
		} catch (IOException ioe) {
			LOG.error("Error saving raptor preferences:", ioe);
			throw new RuntimeException(ioe);
		}
	}

	public void setCurrentLayoutRectangle(String key, Rectangle rectangle) {
		key = "app-" + getString(APP_LAYOUT) + "-" + key;
		setValue(key, rectangle);
	}

	public void setCurrentLayoutSashWeights(String key, int[] array) {
		key = "app-" + getString(APP_LAYOUT) + "-" + key;
		setValue(key, array);
	}

	public void setDefault(String key, Font font) {
		PreferenceConverter.setValue(this, key, font.getFontData());
	}

	public void setDefault(String key, FontData[] fontData) {
		PreferenceConverter.setValue(this, key, fontData);
	}

	public void setDefault(String key, int[] values) {
		setDefault(key, RaptorStringUtils.toString(values));
	}

	public void setDefault(String key, Point point) {
		PreferenceConverter.setValue(this, key, point);
	}

	public void setDefault(String key, Quadrant quadrant) {
		setDefault(key, quadrant.name());
	}

	public void setDefault(String key, Rectangle rectangle) {
		PreferenceConverter.setDefault(this, key, rectangle);
	}

	public void setDefault(String key, RGB rgb) {
		PreferenceConverter.setValue(this, key, rgb);
	}

	public void setDefault(String key, String[] values) {
		setDefault(key, RaptorStringUtils.toString(values));
	}

	public void setValue(String key, Font font) {
		PreferenceConverter.setValue(this, key, font.getFontData());
	}

	public void setValue(String key, FontData[] fontData) {
		PreferenceConverter.setValue(this, key, fontData);
	}

	public void setValue(String key, int[] values) {
		setValue(key, RaptorStringUtils.toString(values));
	}

	public void setValue(String key, Point point) {
		PreferenceConverter.setValue(this, key, point);
	}

	public void setValue(String key, Quadrant quadrant) {
		setValue(key, quadrant.name());
	}

	public void setValue(String key, Rectangle rectangle) {
		PreferenceConverter.setValue(this, key, rectangle);
	}

	public void setValue(String key, RGB rgb) {
		PreferenceConverter.setValue(this, key, rgb);
	}

	public void setValue(String key, String[] values) {
		setValue(key, RaptorStringUtils.toString(values));
	}

}
