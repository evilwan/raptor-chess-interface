package raptor.pref;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatTypes;

public class RaptorPreferenceStore extends PreferenceStore implements
		PreferenceKeys {
	private static final Log LOG = LogFactory
			.getLog(RaptorPreferenceStore.class);
	public static final File RAPTOR_PROPERTIES = new File(Raptor
			.USER_RAPTOR_DIR, "raptor.properties");
	public static final String PREFERENCE_PROPERTIES_FILE = "raptor.properties";

	

	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent arg0) {
			if (arg0.getProperty().endsWith("color")) {
				Raptor.getInstance().getColorRegistry().put(arg0.getProperty(), PreferenceConverter
						.getColor(RaptorPreferenceStore.this, arg0
								.getProperty()));
			} else if (arg0.getProperty().endsWith("font")) {
				Raptor.getInstance().getFontRegistry().put(arg0.getProperty(), PreferenceConverter
						.getFontDataArray(RaptorPreferenceStore.this, arg0
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
		if (event.getType() == ChatTypes.CHAN_TELL) {
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

	

	public Point getPoint(String key) {
		return PreferenceConverter.getPoint(this, key);
	}

	public Rectangle getRectangle(String key) {
		return PreferenceConverter.getRectangle(this, key);
	}

	public void loadDefaults() {
		String defaultFontName = Raptor.getInstance().getFontRegistry()
				.defaultFont().getFontData()[0]
				.getName();
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

		// Chat
		setDefault(CHAT_MAX_CONSOLE_CHARS, 500000);
		setDefault(CHAT_TIMESTAMP_CONSOLE, true);
		setDefault(CHAT_TIMESTAMP_CONSOLE_FORMAT, "'['hh:mma']'");

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
		PreferenceConverter.setDefault(this, CHAT_LINK_COLOR,
				new RGB(0, 0, 255));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatTypes.CHALLENGE
						+ "-color", new RGB(100, 149, 237));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatTypes.CSHOUT
						+ "-color", new RGB(221, 160, 221));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatTypes.SHOUT
						+ "-color", new RGB(221, 160, 221));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatTypes.KIBITZ
						+ "-color", new RGB(100, 149, 237));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatTypes.WHISPER
						+ "-color", new RGB(100, 149, 237));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatTypes.OUTBOUND
						+ "-color", new RGB(128, 128, 128));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatTypes.PARTNER_TELL
						+ "-color", new RGB(255, 0, 0));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatTypes.TELL
						+ "-color", new RGB(255, 0, 0));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatTypes.CHAN_TELL
						+ "-" + 1 + "-color", new RGB(255, 200, 0));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatTypes.CHAN_TELL
						+ "-" + 4 + "-color", new RGB(0, 255, 0));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatTypes.CHAN_TELL
						+ "-" + 50 + "-color", new RGB(255, 175, 175));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatTypes.CHAN_TELL
						+ "-" + 53 + "-color", new RGB(255, 0, 255));
		PreferenceConverter.setDefault(this,
				CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO + ChatTypes.INTERNAL
						+ "-color", new RGB(255, 0, 0));
		PreferenceConverter.setDefault(this, CHAT_PROMPT_COLOR, new RGB(128,
				128, 128));
		PreferenceConverter.setDefault(this, CHAT_QUOTE_UNDERLINE_COLOR,
				new RGB(0, 255, 0));
		PreferenceConverter.setDefault(this, CHAT_LINK_UNDERLINE_COLOR,
				new RGB(0, 0, 238));

		setDefault(APP_NAME, "Raptor v.Alpha.1");
		PreferenceConverter.setDefault(this, APP_LAG_FONT,
				new FontData[] { new FontData(defaultFontName, 12, 0) });
		PreferenceConverter.setDefault(this, APP_LAG_COLOR, new RGB(0, 0, 0));

		setDefault(SOUND_ENABLED, true);

		// Fics
		setDefault(FICS_KEEP_ALIVE, true);
		setDefault(FICS_AUTO_CONNECT, false);
		setDefault(FICS_USER_NAME, "");
		setDefault(FICS_PASSWORD, "");
		setDefault(FICS_IS_NAMED_GUEST, false);
		setDefault(FICS_IS_ANON_GUEST, false);
		setDefault(FICS_LOGIN_SCRIPT, "set seek 0\nset autoflag 1");
		setDefault(FICS_IS_LOGGING_GAMES, false);
		setDefault(FICS_SERVER_URL, "freechess.org");
		setDefault(FICS_PORT, 5000);
		setDefault(FICS_TIMESEAL_ENABLED, true);

		LOG.info("Loaded defaults " + PREFERENCE_PROPERTIES_FILE);
	}

	public void setPoint(String key, Point point) {
		PreferenceConverter.setValue(this, key, point);
	}

	public void setRectangle(String key, Rectangle rectangle) {
		PreferenceConverter.setValue(this, key, rectangle);
	}
}
