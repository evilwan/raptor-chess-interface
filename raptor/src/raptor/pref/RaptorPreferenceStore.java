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
import raptor.util.RaptorStringUtils;

/**
 * The RaptorPreferenceStore. Automatically loads and saves itself at
 * Raptor.USER_RAPTOR_DIR/raptor.properties . Had additional data type support.
 */
public class RaptorPreferenceStore extends PreferenceStore implements
		PreferenceKeys {
	private static final Log LOG = LogFactory
			.getLog(RaptorPreferenceStore.class);
	public static final File RAPTOR_PROPERTIES = new File(
			Raptor.USER_RAPTOR_DIR, "raptor.properties");
	public static final String PREFERENCE_PROPERTIES_FILE = "raptor.properties";

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
		if (event.getType() == ChatType.CHAN_TELL) {
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

	public Quadrant getCurrentLayoutQuadrant(String key) {
		key = "app-" + getString(APP_LAYOUT) + "-" + key;
		return getQuadrant(key);
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
		String defaultMonospacedFontName = "Courier";

		// Board
		setDefault(BOARD_CHESS_SET_NAME, "Fantasy");
		setDefault(BOARD_SQUARE_BACKGROUND_NAME, "Wood2");
		setDefault(BOARD_IS_SHOW_COORDINATES, true);
		setDefault(BOARD_PIECE_SIZE_ADJUSTMENT, .03);
		setDefault(BOARD_HIGHLIGHT_BORDER_WIDTH, .05);
		setDefault(BOARD_IS_SHOWING_PIECE_JAIL, true);
		setDefault(BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN, 1000L * 10L + 1L);
		setDefault(BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN,
				1000L * 60L * 10L + 1L);
		setDefault(BOARD_PREMOVE_ENABLED, true);
		setDefault(BOARD_SMARTMOVE_ENABLED, true);
		setDefault(BOARD_PLAY_MOVE_SOUND_WHEN_OBSERVING, true);
		setDefault(BOARD_IS_SHOWING_PIECE_UNICODE_CHARS, true);
		setDefault(BOARD_QUEUED_PREMOVE_ENABLED, true);
		setDefault(BOARD_IS_USING_CROSSHAIRS_CURSOR, false);

		PreferenceConverter.setDefault(this, BOARD_BACKGROUND_COLOR, new RGB(0,
				0, 0));
		PreferenceConverter.setDefault(this, BOARD_COORDINATES_COLOR, new RGB(
				0, 0, 0));
		PreferenceConverter.setDefault(this, BOARD_HIGHLIGHT_COLOR, new RGB(0,
				255, 255));
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

		// Chat
		setDefault(CHAT_MAX_CONSOLE_CHARS, 500000);
		setDefault(CHAT_TIMESTAMP_CONSOLE, false);
		setDefault(CHAT_TIMESTAMP_CONSOLE_FORMAT, "'['hh:mma']'");
		setDefault(CHAT_UNDERLINE_SINGLE_QUOTES, false);

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
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.CHAN_TELL + "-"
						+ 1 + "-color", new RGB(255, 200, 0));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.CHAN_TELL + "-"
						+ 4 + "-color", new RGB(0, 255, 0));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.CHAN_TELL + "-"
						+ 50 + "-color", new RGB(255, 175, 175));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.CHAN_TELL + "-"
						+ 53 + "-color", new RGB(255, 0, 255));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatType.INTERNAL
						+ "-color", new RGB(255, 0, 0));
		PreferenceConverter.setDefault(this, CHAT_PROMPT_COLOR, new RGB(128,
				128, 128));
		PreferenceConverter.setDefault(this, CHAT_QUOTE_UNDERLINE_COLOR,
				new RGB(0, 255, 0));
		PreferenceConverter.setDefault(this, CHAT_LINK_UNDERLINE_COLOR,
				new RGB(0, 0, 238));

		setDefault(APP_NAME, "Raptor v.Alpha.1");
		setDefault(APP_SASH_WIDTH, 8);
		PreferenceConverter.setDefault(this, APP_PING_FONT,
				new FontData[] { new FontData(defaultFontName, 12, 0) });
		PreferenceConverter.setDefault(this, APP_PING_COLOR, new RGB(0, 0, 0));

		PreferenceConverter.setDefault(this, APP_STATUS_BAR_FONT,
				new FontData[] { new FontData(defaultFontName, 12, 0) });
		PreferenceConverter.setDefault(this, APP_STATUS_BAR_COLOR, new RGB(0,
				0, 0));

		setDefault(APP_HOME_URL,
				"http://code.google.com/p/raptor-chess-interface/wiki/RaptorHomePage");
		setDefault(APP_SOUND_ENABLED, true);
		setDefault(APP_LAYOUT, "Layout1");
		setDefault(APP_OPEN_LINKS_IN_EXTERNAL_BROWSER, false);

		// Layout 1 settings.
		setDefault(APP_LAYOUT1_MAIN_TAB_QUADRANT, Quadrant.V);
		setDefault(APP_LAYOUT1_CHANNEL_TAB_QUADRANT, Quadrant.V);
		setDefault(APP_LAYOUT1_PERSON_TAB_QUADRANT, Quadrant.V);
		setDefault(APP_LAYOUT1_REGEX_TAB_QUADRANT, Quadrant.V);
		setDefault(APP_LAYOUT1_PARTNER_TELL_TAB_QUADRANT, Quadrant.V);
		setDefault(APP_LAYOUT1_GAME_QUADRANT, Quadrant.III);
		setDefault(APP_LAYOUT1_BUGHOUSE_GAME_2_QUADRANT, Quadrant.IV);
		setDefault(APP_LAYOUT1_BROWSER_QUADRANT, Quadrant.III);
		setDefault(APP_LAYOUT1_BUG_ARENA_QUADRANT, Quadrant.VII);
		setDefault(APP_LAYOUT1_SEEK_GRAPH_QUADRANT, Quadrant.VII);
		setDefault(APP_LAYOUT1_BUG_BUTTONS_QUADRANT, Quadrant.II);
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
		setDefault(APP_LAYOUT2_MAIN_TAB_QUADRANT, Quadrant.V);
		setDefault(APP_LAYOUT2_CHANNEL_TAB_QUADRANT, Quadrant.VI);
		setDefault(APP_LAYOUT2_PERSON_TAB_QUADRANT, Quadrant.VI);
		setDefault(APP_LAYOUT2_REGEX_TAB_QUADRANT, Quadrant.VI);
		setDefault(APP_LAYOUT2_PARTNER_TELL_TAB_QUADRANT, Quadrant.VI);
		setDefault(APP_LAYOUT2_GAME_QUADRANT, Quadrant.III);
		setDefault(APP_LAYOUT2_BUGHOUSE_GAME2_QUADRANT, Quadrant.IV);
		setDefault(APP_LAYOUT2_BROWSER_QUADRANT, Quadrant.III);
		setDefault(APP_LAYOUT2_BUG_ARENA_QUADRANT, Quadrant.VII);
		setDefault(APP_LAYOUT2_SEEK_GRAPH_QUADRANT, Quadrant.VII);
		setDefault(APP_LAYOUT2_BUG_BUTTONS_QUADRANT, Quadrant.I);
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
		setDefault(APP_LAYOUT3_MAIN_TAB_QUADRANT, Quadrant.V);
		setDefault(APP_LAYOUT3_CHANNEL_TAB_QUADRANT, Quadrant.VI);
		setDefault(APP_LAYOUT3_PERSON_TAB_QUADRANT, Quadrant.VI);
		setDefault(APP_LAYOUT3_REGEX_TAB_QUADRANT, Quadrant.VI);
		setDefault(APP_LAYOUT3_PARTNER_TELL_TAB_QUADRANT, Quadrant.VI);
		setDefault(APP_LAYOUT3_GAME_QUADRANT, Quadrant.III);
		setDefault(APP_LAYOUT3_BUGHUOSE_GAME_2_QUADRANT, Quadrant.IV);
		setDefault(APP_LAYOUT3_BROWSER_QUADRANT, Quadrant.III);
		setDefault(APP_LAYOUT3_BUG_ARENA_QUADRANT, Quadrant.VII);
		setDefault(APP_LAYOUT3_SEEK_GRAPH_QUADRANT, Quadrant.VII);
		setDefault(APP_LAYOUT3_BUG_BUTTONS_QUADRANT, Quadrant.VIII);
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
		setDefault(FICS_KEEP_ALIVE, true);
		setDefault(FICS_AUTO_CONNECT, false);
		setDefault(FICS_LOGIN_SCRIPT, "set seek 0\nset autoflag 1");
		setDefault(FICS_IS_LOGGING_GAMES, false);
		setDefault(FICS_AUTO_CONNECT, false);
		setDefault(FICS_PROFILE, "Primary");
		setDefault(FICS_COMMANDS_HELP_URL,
				"http://www.freechess.org/Help/AllFiles.html");
		setDefault(FICS_FREECHESS_ORG_URL, "http://www.freechess.org");
		setDefault(FICS_FICS_GAMES_URL, "http://www.ficsgames.com");
		setDefault(FICS_ADJUDICATE_URL,
				"http://www.freechess.org/Adjudicate/index.html");
		setDefault(FICS_TEAM_LEAGUE_URL, "http://teamleague.org/");
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
		setDefault(BICS_KEEP_ALIVE, true);
		setDefault(BICS_AUTO_CONNECT, false);
		setDefault(BICS_LOGIN_SCRIPT, "set seek 0\nset autoflag 1");
		setDefault(BICS_IS_LOGGING_GAMES, false);
		setDefault(BICS_AUTO_CONNECT, false);
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
		setDefault(BICS_SECONDARY_SERVER_URL, "dev.chess.sipay.ru");
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

		setDefault(TIMESEAL_INIT_STRING, "TIMESTAMP|iv|Open Seal|");
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

	public void setCurrentLayoutQuadrant(String key, Quadrant quadrant) {
		key = "app-" + getString(APP_LAYOUT) + "-" + key;
		setValue(key, quadrant.name());
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
