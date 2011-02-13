/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import raptor.service.SeekService.SeekType;
import raptor.swt.BugPartners;
import raptor.swt.GamesWindowItem;
import raptor.swt.SWTUtils;
import raptor.swt.SeekTableWindowItem;
import raptor.swt.chess.SquareBackgroundImageEffect;
import raptor.swt.chess.controller.InactiveMouseAction;
import raptor.swt.chess.controller.ObservingMouseAction;
import raptor.swt.chess.controller.PlayingMouseAction;
import raptor.util.OSUtils;
import raptor.util.RaptorLogger;
import raptor.util.RaptorStringUtils;

/**
 * The RaptorPreferenceStore. Automatically loads and saves itself at
 * Raptor.USER_RAPTOR_DIR/raptor.properties . Had additional data type support.
 */
public class RaptorPreferenceStore extends PreferenceStore implements
		PreferenceKeys {
	private static final RaptorLogger LOG = RaptorLogger.getLog(RaptorPreferenceStore.class);
	public static final String PREFERENCE_PROPERTIES_FILE = "raptor.properties";
	public static final File RAPTOR_PROPERTIES = new File(
			Raptor.USER_RAPTOR_DIR, "raptor.properties");
	protected String defaultMonospacedFontName;
	protected String defaultFontName;
	protected int defaultLargeFontSize;
	protected int defaultSmallFontSize;
	protected int defaultMediumFontSize;
	protected int defaultTinyFontSize;

	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			String key = event.getProperty();
			if (key.endsWith("color")) {
				Raptor.getInstance().getColorRegistry().put(
						key,
						PreferenceConverter.getColor(
								RaptorPreferenceStore.this, key));
			} else if (key.endsWith("font")) {
				// Adjust all the zoomed fonts, as well as the font being
				// changed,
				// in the FontRegistry.

				// Add all the fonts to change to a list to avoid the
				// concurrency issues.
				List<String> fontKeysToChange = new ArrayList<String>();
				for (Object fontRegistryKey : Raptor.getInstance()
						.getFontRegistry().getKeySet()) {
					String fontKey = fontRegistryKey.toString();
					if (fontKey.startsWith(key)) {
						fontKeysToChange.add(fontKey);
					}
				}

				for (String fontKey : fontKeysToChange) {
					if (fontKey.equals(key)) {
						Raptor.getInstance().getFontRegistry().put(
								key,
								PreferenceConverter.getFontDataArray(
										RaptorPreferenceStore.this, key));
					} else {
						double zoomFactor = Double.parseDouble(fontKey
								.substring(key.length() + 1));

						Raptor.getInstance().getFontRegistry().put(fontKey,
								zoomFont(key, zoomFactor));
					}
				}
			}
		}
	};

	protected void resetChessSetIfDeleted() {
		String chessSet = getString(BOARD_CHESS_SET_NAME);
		File file = new File(Raptor.RESOURCES_DIR + "set/" + chessSet);
		if (!file.exists() || !file.isDirectory()) {
			setValue(BOARD_CHESS_SET_NAME,
					getDefaultString(BOARD_CHESS_SET_NAME));
		}
	}

	public RaptorPreferenceStore() {
		super();
		FileInputStream fileIn = null;
		FileOutputStream fileOut = null;

		try {
			LOG.info("Loading RaptorPreferenceStore store "
					+ PREFERENCE_PROPERTIES_FILE);
			loadDefaults();
			if (RAPTOR_PROPERTIES.exists()) {
				load(fileIn = new FileInputStream(RAPTOR_PROPERTIES));
				resetChessSetIfDeleted();
			} else {
				RAPTOR_PROPERTIES.getParentFile().mkdir();
				RAPTOR_PROPERTIES.createNewFile();
				save(fileOut = new FileOutputStream(RAPTOR_PROPERTIES),
						"Last saved on " + new Date());
			}
		} catch (Exception e) {
			LOG.error("Error reading or writing to file ", e);
			throw new RuntimeException(e);
		} finally {
			if (fileIn != null) {
				try {
					fileIn.close();
				} catch (Throwable t) {
				}
			}
			if (fileOut != null) {
				try {
					fileOut.flush();
					fileOut.close();
				} catch (Throwable t) {
				}
			}
		}

		addPropertyChangeListener(propertyChangeListener);
		LOG.info("Loaded preferences from "
				+ RAPTOR_PROPERTIES.getAbsolutePath());
	}

	/**
	 * Returns null for CHANNEL_TELL type.
	 */
	public Color getColor(ChatType type) {
		String key = null;
		if (type == ChatType.CHANNEL_TELL) {
			return null;
		} else {
			key = getKeyForChatType(type);
		}
		return getColorForKeyWithoutDefault(key);
	}

	protected Color getColorForKeyWithoutDefault(String key) {
		Color result = null;
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
	 * Returns the foreground color to use for the specified chat event. Returns
	 * null if no special color should be used.
	 */
	public Color getColor(ChatEvent event) {
		String key = null;
		if (event.getType() == ChatType.CHANNEL_TELL) {
			key = CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + event.getType() + "-"
					+ event.getChannel() + "-color";
		} else {
			key = getKeyForChatType(event.getType());
		}
		return getColorForKeyWithoutDefault(key);
	}

	/**
	 * Returns null for CHANNEL_TELL type.
	 * 
	 * @return
	 */
	public String getKeyForChatType(ChatType type) {
		String result = null;
		if (type == ChatType.CHANNEL_TELL) {
			result = null;
		} else if (type == ChatType.BUGWHO_AVAILABLE_TEAMS
				|| type == ChatType.BUGWHO_GAMES
				|| type == ChatType.BUGWHO_UNPARTNERED_BUGGERS) {
			result = CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.BUGWHO_ALL
					+ "-color";
		} else if (type == ChatType.NOTIFICATION_DEPARTURE) {
			result = CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
					+ ChatType.NOTIFICATION_ARRIVAL + "-color";
		} else {
			result = CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + type + "-color";
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

	public String getDefaultMonospacedFont() {
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

	protected FontData[] zoomFont(String key, double zoomFactor) {
		FontData[] fontData = PreferenceConverter.getFontDataArray(this, key);
		// Convert font to zoom factor.
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setHeight((int) (fontData[i].getHeight() * zoomFactor));
		}
		return fontData;

	}

	protected String getZoomFontKey(String key, double zoomFactor) {
		return key + "-" + zoomFactor;
	}

	public Font getFont(String key, boolean isAdjustingForZoomFactor) {
		try {
			String adjustedKey = key;
			if (isAdjustingForZoomFactor) {
				double zoomFactor = getDouble(APP_ZOOM_FACTOR);
				adjustedKey = getZoomFontKey(key, zoomFactor);
			}

			if (!Raptor.getInstance().getFontRegistry()
					.hasValueFor(adjustedKey)) {
				FontData[] fontData = null;

				if (!isAdjustingForZoomFactor) {
					fontData = PreferenceConverter.getFontDataArray(this, key);
				} else {
					fontData = zoomFont(key, getDouble(APP_ZOOM_FACTOR));
				}

				Raptor.getInstance().getFontRegistry().put(adjustedKey,
						fontData);
			}
			return Raptor.getInstance().getFontRegistry().get(adjustedKey);
		} catch (Throwable t) {
			LOG.error("Error in getFont(" + key + ") Returning default font.",
					t);
			return Raptor.getInstance().getFontRegistry().defaultFont();
		}
	}

	/**
	 * Returns the font for the specified key. Returns the default font if key
	 * was not found.
	 * 
	 * Fonts returned from this method will be adjusted to the APP_ZOOM_FACTOR
	 * preference.
	 */
	public Font getFont(String key) {
		return getFont(key, true);
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
		defaultFontName = Raptor.getInstance().getFontRegistry().defaultFont()
				.getFontData()[0].getName();
		defaultMonospacedFontName = getDefaultMonospacedFont();

		setDefaultMonitorBasedSizes();

		// Action
		setDefault(ACTION_SEPARATOR_SEQUENCE, 400);

		// App settings.
		setDefault(APP_NAME, "Raptor .98 RC4f3");
		setDefault(APP_IS_SHOWING_CHESS_PIECE_UNICODE_CHARS, !OSUtils
				.isLikelyWindowsXP());
		setDefault(APP_SASH_WIDTH, 8);
		PreferenceConverter.setDefault(this, APP_PING_FONT,
				new FontData[] { new FontData(defaultFontName,
						defaultSmallFontSize, 0) });
		PreferenceConverter.setDefault(this, APP_PING_COLOR, new RGB(0, 0, 0));

		PreferenceConverter.setDefault(this, APP_STATUS_BAR_FONT,
				new FontData[] { new FontData(defaultFontName,
						defaultSmallFontSize, 0) });
		PreferenceConverter.setDefault(this, APP_STATUS_BAR_COLOR, new RGB(0,
				0, 0));
		setDefault(APP_HOME_URL,
				"http://code.google.com/p/raptor-chess-interface/");
		setDefault(APP_SOUND_ENABLED, true);
		setDefault(APP_USER_TAGS,
				"+Partner,-Partner,Cool,Dupe,Friend,Jerk,Lagger,Noob,Premover,Troll,Strange");
		setDefault(APP_PGN_FILE, Raptor.USER_RAPTOR_HOME_PATH
				+ "/games/raptorGames.pgn");
		setDefault(APP_LAYOUT, "Layout1");
		setDefault(APP_OPEN_LINKS_IN_EXTERNAL_BROWSER, false);
		setDefault(APP_BROWSER_QUADRANT, Quadrant.II);
		setDefault(APP_CHESS_BOARD_QUADRANTS, new String[] {
				Quadrant.II.toString(), Quadrant.III.toString(),
				Quadrant.IV.toString(), Quadrant.V.toString() });
		setDefault(APP_PGN_RESULTS_QUADRANT, Quadrant.III);
		setDefault(APP_IS_LAUNCHNG_HOME_PAGE, true);
		setDefault(APP_WINDOW_ITEM_POLL_INTERVAL, 5);
		setDefault(APP_IS_LOGGING_CONSOLE, false);
		setDefault(APP_IS_LOGGING_PERSON_TELLS, false);
		setDefault(APP_IS_LOGGING_CHANNEL_TELLS, false);

		// Layout 1 settings.
		setDefault(APP_WINDOW_BOUNDS, new Rectangle(0, 0, -1, -1));
		setDefault(APP_QUAD9_QUAD12345678_SASH_WEIGHTS, new int[] { 10, 90 });
		setDefault(APP_QUAD1_QUAD2345678_SASH_WEIGHTS, new int[] { 50, 50 });
		setDefault(APP_QUAD2345_QUAD678_SASH_WEIGHTS, new int[] { 70, 30 });
		setDefault(APP_QUAD2_QUAD3_QUAD4_QUAD5_SASH_WEIGHTS, new int[] { 25,
				25, 25, 25 });
		setDefault(APP_QUAD67_QUAD8_SASH_WEIGHTS, new int[] { 70, 30 });
		setDefault(APP_QUAD6_QUAD7_SASH_WEIGHTS, new int[] { 50, 50 });
		setDefault(APP_ZOOM_FACTOR, 1.0);

		if (OSUtils.isLikelyWindows() && !OSUtils.isLikelyWindows7()) {
			setDefault(SPEECH_PROCESS_NAME, "SayStatic");
		}

		if (OSUtils.isLikelyLinux()) {
			try {
				if (Runtime.getRuntime().exec(new String[] { "which", "play" })
						.waitFor() == 0) {
					setDefault(PreferenceKeys.SOUND_PROCESS_NAME, "aplay");
				} else if (Runtime.getRuntime().exec(
						new String[] { "which", "aplay" }).waitFor() == 0) {
					setDefault(PreferenceKeys.SOUND_PROCESS_NAME, "play");
				}
			} catch (Throwable t) {
				LOG
						.warn(
								"Error launching which to determine sound process in linux.",
								t);
			}
		}

		// Board
		setDefault(BOARD_ALLOW_MOUSE_WHEEL_NAVIGATION_WHEEL_PLAYING, false);
		setDefault(BOARD_SHOW_PLAYING_GAME_STATS_ON_GAME_END, true);
		setDefault(BOARD_PLAY_CHALLENGE_SOUND, true);
		setDefault(BOARD_PLAY_ABORT_REQUEST_SOUND, true);
		setDefault(BOARD_PLAY_DRAW_OFFER_SOUND, true);
		setDefault(BOARD_USER_MOVE_INPUT_MODE, "DragAndDrop");
		setDefault(BOARD_SHOW_BUGHOUSE_SIDE_UP_TIME, true);
		setDefault(BOARD_PIECE_JAIL_LABEL_PERCENTAGE, 40);
		setDefault(BOARD_COOLBAR_MODE, true);
		setDefault(BOARD_COOLBAR_ON_TOP, true);
		setDefault(BOARD_CHESS_SET_NAME, "Wiki");
		setDefault(BOARD_SQUARE_BACKGROUND_NAME, "GreenMarble");
		setDefault(BOARD_IS_SHOW_COORDINATES, true);
		setDefault(BOARD_PIECE_SIZE_ADJUSTMENT, .06);
		setDefault(BOARD_IS_SHOWING_PIECE_JAIL, false);
		setDefault(BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN, Integer.MIN_VALUE);
		setDefault(BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN,
				1000L * 60L * 60L + 1L);
		setDefault(BOARD_IS_PLAYING_10_SECOND_COUNTDOWN_SOUNDS, true);
		setDefault(BOARD_PREMOVE_ENABLED, true);
		setDefault(BOARD_PLAY_MOVE_SOUND_WHEN_OBSERVING, true);
		setDefault(BOARD_QUEUED_PREMOVE_ENABLED, false);
		setDefault(BOARD_IS_USING_CROSSHAIRS_CURSOR, false);
		setDefault(BOARD_LAYOUT, "raptor.swt.chess.layout.RightOrientedLayout");
		setDefault(BOARD_TAKEOVER_INACTIVE_GAMES, true);
		setDefault(BOARD_PIECE_JAIL_SHADOW_ALPHA, 30);
		setDefault(BOARD_PIECE_SHADOW_ALPHA, 40);
		setDefault(BOARD_COORDINATES_SIZE_PERCENTAGE, 26);
		setDefault(BOARD_ANNOUNCE_CHECK_WHEN_OPPONENT_CHECKS_ME, false);
		setDefault(BOARD_ANNOUNCE_CHECK_WHEN_I_CHECK_OPPONENT, false);
		setDefault(BOARD_SPEAK_MOVES_OPP_MAKES, false);
		setDefault(BOARD_SPEAK_MOVES_I_MAKE, false);
		setDefault(BOARD_SPEAK_WHEN_OBSERVING, false);
		setDefault(BOARD_SPEAK_RESULTS, false);
		setDefault(BOARD_IGNORE_OBSERVED_GAMES_IF_PLAYING, false);
		setDefault(BOARD_MOVE_LIST_CLASS,
				"raptor.swt.chess.movelist.TextAreaMoveList");
		setDefault(BOARD_IS_USING_SOLID_BACKGROUND_COLORS, false);
		setDefault(BOARD_SQUARE_BACKGROUND_IMAGE_EFFECT,
				SquareBackgroundImageEffect.RandomCrop.toString());
		setDefault(BOARD_TRAVERSE_WITH_MOUSE_WHEEL, true);		

		PreferenceConverter.setDefault(this, BOARD_BACKGROUND_COLOR, new RGB(0,
				0, 0));
		PreferenceConverter.setDefault(this, BOARD_COORDINATES_COLOR, new RGB(
				0, 0, 0));
		PreferenceConverter.setDefault(this, BOARD_ACTIVE_CLOCK_COLOR, new RGB(
				0, 255, 0));
		PreferenceConverter.setDefault(this, BOARD_INACTIVE_CLOCK_COLOR,
				new RGB(128, 128, 128));
		PreferenceConverter.setDefault(this, BOARD_CONTROL_COLOR, new RGB(128,
				128, 128));
		PreferenceConverter.setDefault(this, BOARD_LAG_OVER_20_SEC_COLOR,
				new RGB(255, 0, 0));
		PreferenceConverter.setDefault(this, BOARD_PIECE_JAIL_LABEL_COLOR,
				new RGB(0, 255, 0));
		PreferenceConverter.setDefault(this, BOARD_PIECE_JAIL_BACKGROUND_COLOR,
				new RGB(0, 0, 0));
		PreferenceConverter
				.setDefault(this, BOARD_LIGHT_SQUARE_SOLID_BACKGROUND_COLOR,
						new RGB(0, 153, 197));
		PreferenceConverter.setDefault(this,
				BOARD_DARK_SQUARE_SOLID_BACKGROUND_COLOR, new RGB(0, 0, 0));

		PreferenceConverter.setDefault(this, BOARD_COORDINATES_FONT,
				new FontData[] { new FontData(defaultFontName,
						defaultMediumFontSize, 0) });
		PreferenceConverter
				.setDefault(this, BOARD_CLOCK_FONT,
						new FontData[] { new FontData(
								defaultMonospacedFontName, 24, 0) });
		PreferenceConverter.setDefault(this, BOARD_LAG_FONT,
				new FontData[] { new FontData(defaultFontName,
						defaultTinyFontSize, 0) });
		PreferenceConverter.setDefault(this, BOARD_PLAYER_NAME_FONT,
				new FontData[] { new FontData(defaultFontName,
						defaultLargeFontSize, 0) });
		PreferenceConverter.setDefault(this, BOARD_PIECE_JAIL_FONT,
				new FontData[] { new FontData(defaultFontName,
						defaultMediumFontSize, 0) });
		PreferenceConverter.setDefault(this, BOARD_OPENING_DESC_FONT,
				new FontData[] { new FontData(defaultFontName,
						defaultTinyFontSize, 0) });
		PreferenceConverter.setDefault(this, BOARD_STATUS_FONT,
				new FontData[] { new FontData(defaultFontName,
						defaultTinyFontSize, 0) });
		PreferenceConverter.setDefault(this, BOARD_GAME_DESCRIPTION_FONT,
				new FontData[] { new FontData(defaultFontName,
						defaultTinyFontSize, 0) });
		PreferenceConverter.setDefault(this, BOARD_PREMOVES_FONT,
				new FontData[] { new FontData(defaultFontName,
						defaultTinyFontSize, 0) });

		// Controller button preferences.
		setDefault(PLAYING_CONTROLLER + LEFT_MOUSE_BUTTON_ACTION,
				PlayingMouseAction.None.toString());
		setDefault(PLAYING_CONTROLLER + RIGHT_MOUSE_BUTTON_ACTION,
				PlayingMouseAction.PopupMenu.toString());
		setDefault(PLAYING_CONTROLLER + MIDDLE_MOUSE_BUTTON_ACTION,
				PlayingMouseAction.SmartMove.toString());
		setDefault(PLAYING_CONTROLLER + MISC1_MOUSE_BUTTON_ACTION,
				PlayingMouseAction.None.toString());
		setDefault(PLAYING_CONTROLLER + MISC2_MOUSE_BUTTON_ACTION,
				PlayingMouseAction.None.toString());
		setDefault(PLAYING_CONTROLLER + LEFT_DOUBLE_CLICK_MOUSE_BUTTON_ACTION,
				PlayingMouseAction.None.toString());
		setDefault(OBSERVING_CONTROLLER + LEFT_MOUSE_BUTTON_ACTION,
				ObservingMouseAction.MakePrimaryGame.toString());
		setDefault(OBSERVING_CONTROLLER + RIGHT_MOUSE_BUTTON_ACTION,
				ObservingMouseAction.AddGameChatTab.toString());
		setDefault(OBSERVING_CONTROLLER + MIDDLE_MOUSE_BUTTON_ACTION,
				ObservingMouseAction.MatchWinner.toString());
		setDefault(OBSERVING_CONTROLLER + MISC1_MOUSE_BUTTON_ACTION,
				ObservingMouseAction.None.toString());
		setDefault(OBSERVING_CONTROLLER + MISC2_MOUSE_BUTTON_ACTION,
				ObservingMouseAction.None.toString());
		setDefault(
				OBSERVING_CONTROLLER + LEFT_DOUBLE_CLICK_MOUSE_BUTTON_ACTION,
				ObservingMouseAction.None.toString());
		setDefault(INACTIVE_CONTROLLER + LEFT_MOUSE_BUTTON_ACTION,
				InactiveMouseAction.None.toString());
		setDefault(INACTIVE_CONTROLLER + RIGHT_MOUSE_BUTTON_ACTION,
				InactiveMouseAction.None.toString());
		setDefault(INACTIVE_CONTROLLER + MIDDLE_MOUSE_BUTTON_ACTION,
				InactiveMouseAction.Rematch.toString());
		setDefault(INACTIVE_CONTROLLER + MISC1_MOUSE_BUTTON_ACTION,
				InactiveMouseAction.None.toString());
		setDefault(INACTIVE_CONTROLLER + MISC2_MOUSE_BUTTON_ACTION,
				InactiveMouseAction.None.toString());
		setDefault(INACTIVE_CONTROLLER + LEFT_DOUBLE_CLICK_MOUSE_BUTTON_ACTION,
				InactiveMouseAction.None.toString());

		// BugArena
		setDefault(BUG_ARENA_PARTNERS_INDEX, 0);
		setDefault(BUG_ARENA_MAX_PARTNERS_INDEX,
				BugPartners.getRatings().length - 1);
		setDefault(BUG_ARENA_TEAMS_INDEX, 0);
		setDefault(BUG_ARENA_TEAMS_IS_RATED, true);
		setDefault(BUG_ARENA_SELECTED_TAB, 0);
		setDefault(BUG_ARENA_HI_LOW_INDEX,0);
		setDefault(BUGHOUSE_SHOW_BUGWHO_ON_PARTNERSHIP,true);
		

		setDefault(SEEK_OUTPUT_TYPE, SeekType.FormulaFiltered.toString());

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
		setDefault(SEEK_TABLE_SELECTED_TAB, 2);

		PreferenceConverter.setDefault(this, SEEK_GRAPH_COMPUTER_COLOR,
				new RGB(0, 0, 255));
		PreferenceConverter.setDefault(this, SEEK_GRAPH_MANY_COLOR, new RGB(
				255, 255, 102));
		PreferenceConverter.setDefault(this, SEEK_GRAPH_RATED_COLOR, new RGB(0,
				255, 0));
		PreferenceConverter.setDefault(this, SEEK_GRAPH_UNRATED_COLOR, new RGB(
				255, 0, 0));

		// Games table
		setDefault(GAMES_TABLE_SELECTED_TAB, 1);
		setDefault(GAMES_TABLE_RATINGS_INDEX, 0);
		setDefault(GAMES_TABLE_MAX_RATINGS_INDEX,
				GamesWindowItem.getRatings().length - 1);
		setDefault(GAMES_TABLE_RATED_INDEX, 0);
		setDefault(GAMES_TABLE_SHOW_BUGHOUSE, true);
		setDefault(GAMES_TABLE_SHOW_LIGHTNING, true);
		setDefault(GAMES_TABLE_SHOW_BLITZ, true);
		setDefault(GAMES_TABLE_SHOW_STANDARD, true);
		setDefault(GAMES_TABLE_SHOW_CRAZYHOUSE, true);
		setDefault(GAMES_TABLE_SHOW_EXAMINED, true);
		setDefault(GAMES_TABLE_SHOW_WILD, true);
		setDefault(GAMES_TABLE_SHOW_ATOMIC, true);
		setDefault(GAMES_TABLE_SHOW_SUICIDE, true);
		setDefault(GAMES_TABLE_SHOW_LOSERS, true);
		setDefault(GAMES_TABLE_SHOW_UNTIMED, true);
		setDefault(GAMES_TABLE_SHOW_NONSTANDARD, true);
		setDefault(GAMES_TABLE_SHOW_PRIVATE, true);

		// Arrows
		PreferenceConverter.setDefault(this, ARROW_OBS_OPP_COLOR, new RGB(255,
				0, 255));
		PreferenceConverter
				.setDefault(this, ARROW_MY_COLOR, new RGB(0, 0, 255));
		PreferenceConverter.setDefault(this, ARROW_PREMOVE_COLOR, new RGB(0, 0,
				255));
		PreferenceConverter.setDefault(this, ARROW_OBS_COLOR,
				new RGB(0, 0, 255));
		setDefault(ARROW_SHOW_ON_OBS_AND_OPP_MOVES, true);
		setDefault(ARROW_SHOW_ON_MOVE_LIST_MOVES, true);
		setDefault(ARROW_SHOW_ON_MY_PREMOVES, true);
		setDefault(ARROW_SHOW_ON_MY_MOVES, false);
		setDefault(ARROW_ANIMATION_DELAY, 200L);
		setDefault(ARROW_FADE_AWAY_MODE, true);
		setDefault(ARROW_WIDTH_PERCENTAGE, 15);

		// Highlights
		PreferenceConverter.setDefault(this, HIGHLIGHT_OBS_OPP_COLOR, new RGB(
				255, 0, 255));
		PreferenceConverter.setDefault(this, HIGHLIGHT_MY_COLOR, new RGB(0, 0,
				255));
		PreferenceConverter.setDefault(this, HIGHLIGHT_PREMOVE_COLOR, new RGB(
				0, 0, 255));
		PreferenceConverter.setDefault(this, HIGHLIGHT_OBS_COLOR, new RGB(0, 0,
				255));
		setDefault(HIGHLIGHT_SHOW_ON_OBS_AND_OPP_MOVES, true);
		setDefault(HIGHLIGHT_SHOW_ON_MOVE_LIST_MOVES, true);
		setDefault(HIGHLIGHT_SHOW_ON_MY_PREMOVES, true);
		setDefault(HIGHLIGHT_SHOW_ON_MY_MOVES, false);
		setDefault(HIGHLIGHT_FADE_AWAY_MODE, false);
		setDefault(HIGHLIGHT_ANIMATION_DELAY, 200L);
		setDefault(HIGHLIGHT_WIDTH_PERCENTAGE, 3);

		// Game Results
		PreferenceConverter.setDefault(this, RESULTS_COLOR, new RGB(255, 0, 0));
		PreferenceConverter.setDefault(this, RESULTS_FONT,
				new FontData[] { new FontData(defaultMonospacedFontName, 40,
						SWT.BOLD) });
		setDefault(RESULTS_IS_SHOWING, true);
		setDefault(RESULTS_FADE_AWAY_MODE, true);
		setDefault(RESULTS_ANIMATION_DELAY, 400L);
		setDefault(RESULTS_WIDTH_PERCENTAGE, 80);

		// Chat
		setDefault(CHAT_MAX_CONSOLE_CHARS, 100000);
		setDefault(CHAT_TIMESTAMP_CONSOLE, false);
		setDefault(CHAT_TIMESTAMP_CONSOLE_FORMAT, "'['hh:mma']'");
		setDefault(CHAT_IS_PLAYING_CHAT_ON_PTELL, true);
		setDefault(CHAT_IS_PLAYING_CHAT_ON_PERSON_TELL, true);
		setDefault(CHAT_IS_SMART_SCROLL_ENABLED, true);
		setDefault(CHAT_OPEN_CHANNEL_TAB_ON_CHANNEL_TELLS, false);
		setDefault(CHAT_OPEN_PERSON_TAB_ON_PERSON_TELLS, false);
		setDefault(CHAT_OPEN_PARTNER_TAB_ON_PTELLS, false);
		setDefault(CHAT_REMOVE_SUB_TAB_MESSAGES_FROM_MAIN_TAB, true);
		setDefault(CHAT_UNDERLINE_URLS, true);
		setDefault(CHAT_UNDERLINE_QUOTED_TEXT, true);
		setDefault(CHAT_UNDERLINE_SINGLE_QUOTES, false);
		setDefault(CHAT_PLAY_NOTIFICATION_SOUND_ON_ARRIVALS, true);
		setDefault(CHAT_PLAY_NOTIFICATION_SOUND_ON_DEPARTURES, false);
		setDefault(CHAT_UNDERLINE_COMMANDS, true);
		setDefault(CHAT_COMMAND_LINE_SPELL_CHECK, true);

		PreferenceConverter.setDefault(this, CHAT_INPUT_FONT,
				new FontData[] { new FontData(defaultMonospacedFontName,
						defaultLargeFontSize, 0) });
		PreferenceConverter.setDefault(this, CHAT_OUTPUT_FONT,
				new FontData[] { new FontData(defaultMonospacedFontName,
						defaultMediumFontSize, 0) });

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
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.DRAW_REQUEST
						+ "-color", new RGB(255, 0, 0));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.ABORT_REQUEST
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
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.PLAYING_STATISTICS + "-color", new RGB(100,
						149, 237));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.QTELL
						+ "-color", new RGB(128, 128, 128));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.FINGER
						+ "-color", new RGB(128, 128, 128));

		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.HISTORY
						+ "-color", new RGB(128, 128, 128));

		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.GAMES
						+ "-color", new RGB(128, 128, 128));

		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.BUGWHO_ALL
						+ "-color", new RGB(128, 128, 128));

		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.NOTIFICATION_ARRIVAL + "-color", new RGB(
						255, 0, 0));

		PreferenceConverter.setDefault(this, CHAT_PROMPT_COLOR, new RGB(128,
				128, 128));
		PreferenceConverter.setDefault(this, CHAT_QUOTE_UNDERLINE_COLOR,
				new RGB(0, 255, 0));
		PreferenceConverter.setDefault(this, CHAT_LINK_UNDERLINE_COLOR,
				new RGB(11, 133, 238));

		// Bug house buttons settings.
		PreferenceConverter.setDefault(this, BUG_BUTTONS_FONT,
				new FontData[] { new FontData(defaultFontName,
						defaultSmallFontSize, SWT.BOLD) });
		// Bug house
		setDefault(BUGHOUSE_PLAYING_OPEN_PARTNER_BOARD, true);
		setDefault(BUGHOUSE_OBSERVING_OPEN_PARTNER_BOARD, true);
		setDefault(BUGHOUSE_SPEAK_COUNTDOWN_ON_PARTNER_BOARD, true);
		setDefault(BUGHOUSE_SPEAK_PARTNER_TELLS, true);
		setDefault(BUGHOUSE_IS_PLAYING_PARTNERSHIP_OFFERED_SOUND, true);

		// Fics
		setDefault(FICS_KEEP_ALIVE, false);
		setDefault(FICS_AUTO_CONNECT, false);
		setDefault(FICS_LOGIN_SCRIPT, "set seek 0\nset autoflag 1\n");
		setDefault(FICS_AUTO_CONNECT, false);
		setDefault(FICS_PROFILE, "Primary");
		setDefault(FICS_CLOSE_TABS_ON_DISCONNECT, false);
		setDefault(FICS_SHOW_BUGBUTTONS_ON_PARTNERSHIP, true);
		setDefault(FICS_NO_WRAP_ENABLED, true);
		setDefault(FICS_CHANNEL_COMMANDS,
				"+channel $channel,-channel $channel,in $channel");
		setDefault(FICS_PERSON_QUICK_COMMANDS,
				"finger $person,follow $person,partner $person");
		setDefault(
				FICS_PERSON_COMMANDS,
				"history $person,journal $person,"
						+ "observe $person,oldpstat $userName $person,pstat $userName $person,"
						+ "stored $person,variables $person,separator,"
						+ "+censor $person,-censor $person,+gnotify $person,-gnotify $person,+noplay $person,-noplay $person,+notify $person,-notify $person,separator,"
						+ "match $person 1 0,match $person 3 0,match $person 5 0,match $person 15 0");
		setDefault(FICS_GAME_COMMANDS,
				"observe $gameId,allobservers $gameId,moves $gameId");
		setDefault(
				FICS_REGULAR_EXPRESSIONS_TO_BLOCK,
				"defprompt set\\.,gameinfo set\\.,ms set\\.,startpos set\\.,"
						+ "pendinfo set\\.,nowrap set\\.,smartmove set\\.,premove set\\.,"
						+ "Style 12 set\\.,Your prompt will now not show the time\\.,"
						+ "You will not see seek ads\\.,You will not see seek ads.\\.,"
						+ "Auto-flagging enabled\\.,lock set\\.,set seek 0,set autoflag 1,"
						+ "allresults set\\.,Bell off\\.,set interface Raptor .*,"
						+ "You are not examining or setting up a game\\.");

		setDefault(FICS_SEEK_GAME_TYPE, "");
		setDefault(FICS_SEEK_MINUTES, "5");
		setDefault(FICS_SEEK_INC, "0");
		setDefault(FICS_SEEK_MIN_RATING, "Any");
		setDefault(FICS_SEEK_MAX_RATING, "Any");
		setDefault(FICS_SEEK_MANUAL, false);
		setDefault(FICS_SEEK_FORMULA, true);
		setDefault(FICS_SEEK_RATED, true);
		setDefault(FICS_SEEK_COLOR, "");
		setDefault(FICS_KEEP_ALIVE_COMMAND,
				"set busy is away from the keyboard.");

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
		setDefault(BICS_PROFILE, "Primary");
		setDefault(BICS_CLOSE_TABS_ON_DISCONNECT, false);
		setDefault(BICS_SHOW_BUGBUTTONS_ON_PARTNERSHIP, true);
		setDefault(BICS_KEEP_ALIVE_COMMAND,
				"set busy is away from the keyboard.");
		setDefault(BICS_CHANNEL_COMMANDS,
				"+channel $channel,-channel $channel,in $channel");
		setDefault(BICS_PERSON_QUICK_COMMANDS,
				"finger $person,follow $person,partner $person");
		setDefault(
				BICS_PERSON_COMMANDS,
				"history $person,journal $person,"
						+ "observe $person,oldpstat $userName $person,pstat $userName $person,"
						+ "stored $person,variables $person,separator,"
						+ "+censor $person,-censor $person,+gnotify $person,-gnotify $person,+noplay $person,-noplay $person,+notify $person,-notify $person,separator,"
						+ "match $person 1 0 zh,match $person 3 0 zh,match $person 1 0 zh fr,match $person 3 0 zh fr,match $person 2 0 bughouse,"
						+ "match $person 2 0 bughouse fr, match $person 2 0 bughouse w5");
		setDefault(BICS_GAME_COMMANDS,
				"observe $gameId,allobservers $gameId,moves $gameId");
		setDefault(
				BICS_REGULAR_EXPRESSIONS_TO_BLOCK,
				"defprompt set\\.,gameinfo set\\.,ms set\\.,startpos set\\.,"
						+ "pendinfo set\\.,nowrap set\\.,smartmove set\\.,premove set\\.,"
						+ "Style 12 set\\.,Your prompt will now not show the time\\.,"
						+ "You will not see seek ads\\.,You will not see seek ads.\\.,"
						+ "Auto-flagging enabled\\.,lock set\\.");

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
		setDefault("fics-" + MAIN_TAB_QUADRANT, Quadrant.VI);
		setDefault("fics-" + CHANNEL_TAB_QUADRANT, Quadrant.VI);
		setDefault("fics-" + PERSON_TAB_QUADRANT, Quadrant.VI);
		setDefault("fics-" + REGEX_TAB_QUADRANT, Quadrant.VI);
		setDefault("fics-" + PARTNER_TELL_TAB_QUADRANT, Quadrant.VI);
		setDefault("fics-" + BUG_WHO_QUADRANT, Quadrant.VIII);
		setDefault("fics-" + SEEK_TABLE_QUADRANT, Quadrant.VIII);
		setDefault("fics-" + BUG_BUTTONS_QUADRANT, Quadrant.IX);
		setDefault("fics-" + GAME_CHAT_TAB_QUADRANT, Quadrant.VI);
		setDefault("fics-" + GAMES_TAB_QUADRANT, Quadrant.VIII);
		setDefault("fics-" + GAME_BOT_QUADRANT, Quadrant.VIII);

		setDefault("fics2-" + MAIN_TAB_QUADRANT, Quadrant.VII);
		setDefault("fics2-" + CHANNEL_TAB_QUADRANT, Quadrant.VII);
		setDefault("fics2-" + PERSON_TAB_QUADRANT, Quadrant.VII);
		setDefault("fics2-" + REGEX_TAB_QUADRANT, Quadrant.VII);
		setDefault("fics2-" + PARTNER_TELL_TAB_QUADRANT, Quadrant.VII);
		setDefault("fics2-" + BUG_WHO_QUADRANT, Quadrant.VIII);
		setDefault("fics2-" + SEEK_TABLE_QUADRANT, Quadrant.VIII);
		setDefault("fics2-" + BUG_BUTTONS_QUADRANT, Quadrant.IX);
		setDefault("fics2-" + GAME_CHAT_TAB_QUADRANT, Quadrant.VII);
		setDefault("fics2-" + GAMES_TAB_QUADRANT, Quadrant.VIII);
		setDefault("fics2-" + GAME_BOT_QUADRANT, Quadrant.VIII);

		setDefault("bics-" + MAIN_TAB_QUADRANT, Quadrant.VI);
		setDefault("bics-" + CHANNEL_TAB_QUADRANT, Quadrant.VI);
		setDefault("bics-" + PERSON_TAB_QUADRANT, Quadrant.VI);
		setDefault("bics-" + REGEX_TAB_QUADRANT, Quadrant.VI);
		setDefault("bics-" + PARTNER_TELL_TAB_QUADRANT, Quadrant.VI);
		setDefault("bics-" + BUG_WHO_QUADRANT, Quadrant.VIII);
		setDefault("bics-" + SEEK_TABLE_QUADRANT, Quadrant.VIII);
		setDefault("bics-" + BUG_BUTTONS_QUADRANT, Quadrant.IX);
		setDefault("bics-" + GAME_CHAT_TAB_QUADRANT, Quadrant.VI);
		setDefault("bics-" + GAMES_TAB_QUADRANT, Quadrant.VIII);

		setDefault("bics2-" + MAIN_TAB_QUADRANT, Quadrant.VII);
		setDefault("bics2-" + CHANNEL_TAB_QUADRANT, Quadrant.VII);
		setDefault("bics2-" + PERSON_TAB_QUADRANT, Quadrant.VII);
		setDefault("bics2-" + REGEX_TAB_QUADRANT, Quadrant.VII);
		setDefault("bics2-" + PARTNER_TELL_TAB_QUADRANT, Quadrant.VII);
		setDefault("bics2-" + BUG_WHO_QUADRANT, Quadrant.VIII);
		setDefault("bics2-" + SEEK_TABLE_QUADRANT, Quadrant.VIII);
		setDefault("bics2-" + BUG_BUTTONS_QUADRANT, Quadrant.IX);
		setDefault("bics2-" + GAME_CHAT_TAB_QUADRANT, Quadrant.VII);
		setDefault("bics2-" + GAMES_TAB_QUADRANT, Quadrant.VIII);

		// Timeseal 1 connect string
		// setDefault(TIMESEAL_INIT_STRING, "TIMESTAMP|iv|OpenSeal|");
		setDefault(TIMESEAL_INIT_STRING, "TIMESEAL2|raptorUser|OpenSeal|");

		LOG.info("Loaded defaults " + PREFERENCE_PROPERTIES_FILE);
	}

	@Override
	public void save() {
		FileOutputStream fileOut = null;
		try {
			save(fileOut = new FileOutputStream(RAPTOR_PROPERTIES),
					"Last saved on " + new Date());
			fileOut.flush();
		} catch (IOException ioe) {
			LOG.error("Error saving raptor preferences:", ioe);
			throw new RuntimeException(ioe);
		} finally {
			try {
				fileOut.close();
			} catch (Throwable t) {
			}
		}
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

	protected void setDefaultMonitorBasedSizes() {
		Rectangle fullViewBounds = Display.getCurrent().getPrimaryMonitor()
				.getBounds();
		int toolbarPieceSize = 12;

		String iconSize = "tiny";
		defaultLargeFontSize = 12;
		defaultMediumFontSize = 10;
		defaultSmallFontSize = 8;
		defaultTinyFontSize = 6;
		if (fullViewBounds.height >= 1200) {
			iconSize = "large";
			toolbarPieceSize = 24;
			defaultLargeFontSize = 18;
			defaultMediumFontSize = 16;
			defaultSmallFontSize = 14;
			defaultTinyFontSize = 12;
		} else if (fullViewBounds.height >= 1024) {
			iconSize = "medium";
			toolbarPieceSize = 20;
			defaultLargeFontSize = 16;
			defaultMediumFontSize = 14;
			defaultSmallFontSize = 12;
			defaultTinyFontSize = 10;
		} else if (fullViewBounds.height >= 670) {
			iconSize = "small";
			toolbarPieceSize = 16;
			defaultLargeFontSize = 14;
			defaultMediumFontSize = 12;
			defaultSmallFontSize = 10;
			defaultTinyFontSize = 8;
		}
		getDefaultMonospacedFont();

		setDefault(PreferenceKeys.APP_ICON_SIZE, iconSize);
		setDefault(PreferenceKeys.APP_TOOLBAR_PIECE_SIZE, toolbarPieceSize);
	}

}
