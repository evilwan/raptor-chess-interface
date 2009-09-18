package raptor.pref;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import raptor.App;

public class RaptorPreferenceStore extends PreferenceStore implements
		PreferenceKeys {
	private static final Log LOG = LogFactory
			.getLog(RaptorPreferenceStore.class);
	public static final File RAPTOR_PROPERTIES = new File(App.getInstance()
			.getRaptorUserDir(), "raptor.properties");
	public static final String PREFERENCE_PROPERTIES_FILE = "raptor.properties";
	public static final String ICONS_DIR = "resources/common/icons/";

	private ImageRegistry imageRegistry = new ImageRegistry(Display
			.getCurrent());
	private FontRegistry fontRegistry = new FontRegistry(Display.getCurrent());
	private ColorRegistry colorRegistry = new ColorRegistry(Display
			.getCurrent());

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
			LOG.error("Error reading or writing to file "
					+ RAPTOR_PROPERTIES.getAbsolutePath(), e);
			throw new RuntimeException(e);
		}

		addPropertyChangeListener(propertyChangeListener);
		LOG.info("Loaded preferences from "
				+ RAPTOR_PROPERTIES.getAbsolutePath());

	}

	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent arg0) {
			if (arg0.getProperty().endsWith("color")) {
				colorRegistry.put(arg0.getProperty(), PreferenceConverter
						.getColor(RaptorPreferenceStore.this, arg0
								.getProperty()));
			} else if (arg0.getProperty().endsWith("font")) {
				fontRegistry.put(arg0.getProperty(), PreferenceConverter
						.getFontDataArray(RaptorPreferenceStore.this, arg0
								.getProperty()));
			}
		}
	};

	public void loadDefaults() {
		String defaultFontName = fontRegistry.defaultFont().getFontData()[0]
				.getName();

		// Board
		setDefault(BOARD_CHESS_SET_NAME, "Fantasy");
		setDefault(BOARD_SQUARE_BACKGROUND_NAME, "Wood2");
		setDefault(BOARD_IS_SHOW_COORDINATES, true);
		setDefault(BOARD_PIECE_SIZE_ADJUSTMENT, .03);
		setDefault(BOARD_HIGHLIGHT_BORDER_WIDTH, .05);
		setDefault(BOARD_IS_SHOWING_PIECE_JAIL, true);
		setDefault(BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN, 1000L * 60L + 1L);
		setDefault(BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN,
				1000L * 60L * 10L + 1L);
		setDefault(BOARD_CLOCK_FORMAT, "HH:mm");
		setDefault(BOARD_CLOCK_SECONDS_FORMAT, "hh:mm:ss");
		setDefault(BOARD_CLOCK_MILLIS_FORMAT, "hh:mm:ss.S");
		setDefault(BOARD_CLOCK_LAG_FORMAT, "mm:ss.S");

		PreferenceConverter.setDefault(this, BOARD_BACKGROUND_COLOR, new RGB(0,
				0, 0));
		PreferenceConverter.setDefault(this, BOARD_COORDINATES_COLOR, new RGB(
				0, 0, 0));
		PreferenceConverter.setDefault(this, BOARD_HIGHLIGHT_COLOR, new RGB(0,
				255, 255));
		PreferenceConverter.setDefault(this, BOARD_ACTIVE_CLOCK_COLOR, new RGB(
				255, 0, 0));
		PreferenceConverter.setDefault(this, BOARD_INACTIVE_CLOCK_COLOR,
				new RGB(128, 128, 128));
		PreferenceConverter.setDefault(this, BOARD_LAG_COLOR, new RGB(128, 128,
				128));
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
				new FontData[] { new FontData(defaultFontName, 14, 0) });
		PreferenceConverter.setDefault(this, BOARD_PLAYER_NAME_FONT,
				new FontData[] { new FontData(defaultFontName, 14, 0) });
		PreferenceConverter.setDefault(this, BOARD_PIECE_JAIL_FONT,
				new FontData[] { new FontData(defaultFontName, 14, 0) });
		PreferenceConverter.setDefault(this, BOARD_OPENING_DESC_FONT,
				new FontData[] { new FontData(defaultFontName, 10, 0) });
		PreferenceConverter.setDefault(this,BOARD_STATUS_FONT,
				new FontData[] { new FontData(defaultFontName, 10, 0) });
		PreferenceConverter.setDefault(this, BOARD_GAME_DESCRIPTION_FONT,
				new FontData[] { new FontData(defaultFontName, 12, 0) });
		PreferenceConverter.setDefault(this, BOARD_PREMOVES_FONT,
				new FontData[] { new FontData(defaultFontName, 12, 0) });

		// Chat
		setDefault(CHAT_MAX_CONSOLE_CHARS, 500000);
		setDefault(CHAT_TIMESTAMP_CONSOLE, false);

		PreferenceConverter.setDefault(this, CHAT_INPUT_FONT,
				new FontData[] { new FontData(defaultFontName, 16, 0) });
		PreferenceConverter.setDefault(this, CHAT_OUTPUT_FONT,
				new FontData[] { new FontData(defaultFontName, 16, 0) });
		PreferenceConverter.setDefault(this, CHAT_LAG_FONT,
				new FontData[] { new FontData(defaultFontName, 12, 0) });

		PreferenceConverter.setDefault(this, CHAT_INPUT_BACKGROUND_COLOR,
				new RGB(0, 0, 0));
		PreferenceConverter.setDefault(this, CHAT_INPUT_DEFAULT_TEXT_COLOR,
				new RGB(255, 255, 255));
		PreferenceConverter.setDefault(this, CHAT_OUTPUT_BACKGROUND_COLOR,
				new RGB(255, 255, 255));
		PreferenceConverter.setDefault(this, CHAT_OUTPUT_TEXT_COLOR, new RGB(0,
				0, 0));
		PreferenceConverter.setDefault(this, CHAT_LINK_COLOR,
				new RGB(0, 0, 255));

		// Fics
		setDefault(FICS_KEEP_ALIVE, true);
		setDefault(FICS_AUTO_CONNECT, true);
		setDefault(FICS_USER_NAME, "");
		setDefault(FICS_PASSWORD, "");
		setDefault(FICS_IS_NAMED_GUEST, false);
		setDefault(FICS_IS_ANON_GUEST, false);
		setDefault(FICS_LOGIN_SCRIPT, "");
		setDefault(FICS_IS_LOGGING_GAMES, false);

		LOG.info("Loaded defaults " + PREFERENCE_PROPERTIES_FILE);
	}

	public Color getColor(String key) {
		try {
			if (!colorRegistry.hasValueFor(key)) {
				RGB rgb = PreferenceConverter.getColor(this, key);
				colorRegistry.put(key, rgb);
			}
			return colorRegistry.get(key);
		} catch (Throwable t) {
			LOG.error("Error in getColor(" + key + ") Returning black.", t);
			return new Color(Display.getCurrent(), new RGB(0, 0, 0));
		}
	}

	public Font getFont(String key) {
		try {
			if (!fontRegistry.hasValueFor(key)) {
				FontData[] fontData = PreferenceConverter.getFontDataArray(
						this, key);
				fontRegistry.put(key, fontData);
			}
			return fontRegistry.get(key);
		} catch (Throwable t) {
			LOG.error("Error in getFont(" + key + ") Returning default font.",
					t);
			return fontRegistry.defaultFont();
		}
	}

	public Point getPoint(String key) {
		return PreferenceConverter.getPoint(this, key);
	}

	public Rectangle getRectangle(String key) {
		return PreferenceConverter.getRectangle(this, key);
	}

	public void setPoint(String key, Point point) {
		PreferenceConverter.setValue(this, key, point);
	}

	public void setRectangle(String key, Rectangle rectangle) {
		PreferenceConverter.setValue(this, key, rectangle);
	}

	public Image getImage(String fileName) {
		Image result = imageRegistry.get(fileName);
		if (result == null) {
			try {
				ImageData data = new ImageData(fileName);
				imageRegistry.put(fileName, result = new Image(Display
						.getCurrent(), data));
			} catch (RuntimeException e) {
				LOG.error("Error loading image " + fileName, e);
				throw e;
			}
		}
		return result;
	}

	public Image getIcon(String key) {
		String fileName = ICONS_DIR + key + ".png";
		return getImage(fileName);
	}
}
