package raptor.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import raptor.gui.board.Background;
import raptor.gui.board.Set;

public class PreferenceService {
	private static final Log log = LogFactory.getLog(PreferenceService.class);

	public static final String COMMON_PROPERTIES = "resources/common/common.properties";

	public static final String BOARD_SET_KEY = "board-set";
	public static final String BOARD_BACKGROUND_KEY = "board-background";
	public static final String BOARD_BACKGROUND_COLOR_KEY = "board-background-color";
	public static final String BOARD_IS_SHOW_COORDINATES_KEY = "board-show-coordinates";
	public static final String BOARD_COORDINATES_COLOR = "board-coordinates-color";
	public static final String BOARD_COORDINATES_FONT = "board-coordinates-font";
	public static final String BOARD_PIECE_SIZE_ADJUSTMENT_KEY = "board-piece-size-adjustment-percentage";
	public static final String BOARD_HIGHLIGHT_BORDER_WIDTH_KEY = "board-highlight-width-percentage";
	public static final String BOARD_HIGHLIGHT_COLOR_KEY = "board-highlight-color";
	public static final String BOARD_ACTIVE_CLOCK_COLOR = "board-active-clock-color";
	public static final String BOARD_INACTIVE_CLOCK_COLOR = "board-inactive-clock-color";
	public static final String BOARD_CLOCK_FONT = "board-clock-font";
	public static final String BOARD_LAG_FONT = "board-lag-font";
	public static final String BOARD_PLAYER_NAME_FONT = "board-player-name-font";
	public static final String BPARD_LAG_COLOR = "board-lag-color";
	public static final String BOARD_PIECE_JAIL_FONT = "board-piece-jail-font";
	public static final String BOARD_PIECE_JAIL_COLOR = "board-piece-jail-color";
	public static final String BOARD_IS_SHOWING_PIECE_JAIL = "board-is-showing-piece-jail";

	private static final PreferenceService instance = new PreferenceService();

	private PreferenceStore preferenceStore = null;
	private FontRegistry fontRegistry = new FontRegistry();
	private ColorRegistry colorRegistry = new ColorRegistry();
	private Set set;
	private Background background;

	public PreferenceService() {
		try {
			preferenceStore = new PreferenceStore(COMMON_PROPERTIES);
			defaultConfig();
			saveConfig();

			// Preload set and background.
			getChessSet();
			getSquareBackground();

		} catch (Exception e) {
			log.error("Error reading " + COMMON_PROPERTIES
					+ " reverting to default properties", e);
		}
	}

	public static PreferenceService getInstance() {
		return instance;
	}

	public void saveConfig() {
		try {
			preferenceStore.save();
		} catch (Exception e) {
			log.error("Unexpected error saving configuration: ", e);
		}
	}

	public void defaultConfig() {
		String defaultFontName = fontRegistry.defaultFont().getFontData()[0]
				.getName();
		preferenceStore.setDefault(BOARD_SET_KEY, "WCN");
		preferenceStore.setDefault(BOARD_BACKGROUND_KEY, "CrumpledPaper");
		preferenceStore.setDefault(BOARD_BACKGROUND_COLOR_KEY, StringConverter
				.asString(new RGB(0, 0, 0)));
		preferenceStore.setDefault(BOARD_IS_SHOW_COORDINATES_KEY, true);
		preferenceStore.setDefault(BOARD_COORDINATES_COLOR, StringConverter
				.asString(new RGB(128, 128, 128)));
		preferenceStore.setDefault(BOARD_COORDINATES_FONT,
				StringConverter.asString(new FontData[] { new FontData(
						defaultFontName, 14, 0) }));
		preferenceStore.setDefault(BOARD_PIECE_SIZE_ADJUSTMENT_KEY, .03);
		preferenceStore.setDefault(BOARD_HIGHLIGHT_BORDER_WIDTH_KEY, .05);
		preferenceStore.setDefault(BOARD_HIGHLIGHT_COLOR_KEY, StringConverter
				.asString(new RGB(0, 255, 255)));
		preferenceStore.setDefault(BOARD_ACTIVE_CLOCK_COLOR, StringConverter
				.asString(new RGB(255, 0, 0)));
		preferenceStore.setDefault(BOARD_INACTIVE_CLOCK_COLOR, StringConverter
				.asString(new RGB(128, 128, 128)));
		preferenceStore.setDefault(BOARD_CLOCK_FONT, StringConverter
				.asString(new FontData[] { new FontData(defaultFontName, 24,
						SWT.BOLD) }));
		preferenceStore.setDefault(BOARD_LAG_FONT,
				StringConverter.asString(new FontData[] { new FontData(
						defaultFontName, 14, 0) }));
		preferenceStore.setDefault(BOARD_PLAYER_NAME_FONT,
				StringConverter.asString(new FontData[] { new FontData(
						defaultFontName, 14, 0) }));
		preferenceStore.setDefault(BPARD_LAG_COLOR, StringConverter
				.asString(new RGB(128, 128, 128)));
		preferenceStore.setDefault(BOARD_PIECE_JAIL_FONT,
				StringConverter.asString(new FontData[] { new FontData(
						defaultFontName, 14, 0) }));
		preferenceStore.setDefault(BOARD_PIECE_JAIL_COLOR, StringConverter
				.asString(new RGB(0, 255, 0)));
		preferenceStore.setDefault(BOARD_IS_SHOWING_PIECE_JAIL, true);
	}

	public PreferenceStore getStore() {
		return preferenceStore;
	}

	public Font getFont(String key) {
		if (!fontRegistry.hasValueFor(key)) {
			String value = preferenceStore.getString(key);
			if (value == null) {
				Font font = fontRegistry.defaultFont();
				fontRegistry.put(key, font.getFontData());
			} else {
				FontData[] fontData = StringConverter.asFontDataArray(value);
				fontRegistry.put(key, fontData);
			}
		}
		return fontRegistry.get(key);
	}

	public void setFont(String key, Font font) {
		preferenceStore.setValue(key, StringConverter.asString(font
				.getFontData()));
	}

	public Color getColor(String key) {
		if (!colorRegistry.hasValueFor(key)) {
			String value = preferenceStore.getString(key);
			if (value == null) {
				colorRegistry.put(key, new RGB(0, 0, 0));
			} else {
				colorRegistry.put(key, StringConverter.asRGB(value));
			}
			return colorRegistry.get(key);
		}
		return colorRegistry.get(key);

	}

	public void setColor(String key, Color color) {
		preferenceStore.setValue(key, StringConverter.asString(color.getRGB()));
	}

	public void setPoint(String key, Point point) {
		preferenceStore.setValue(key, StringConverter.asString(point));
	}

	public Point getPoint(String key) {
		return StringConverter.asPoint(preferenceStore.getString(key));
	}

	public Rectangle getRectangle(String key) {
		return StringConverter.asRectangle(preferenceStore.getString(key));
	}

	public void setRectangle(String key, Rectangle rectangle) {
		preferenceStore.setValue(key, StringConverter.asString(rectangle));
	}

	public String[] getArray(String key) {
		return StringConverter.asArray(preferenceStore.getString(key));
	}

	public Set getChessSet() {
		if (set == null) {
			set = new Set(getStore().getString(BOARD_SET_KEY));
		}
		return set;
	}

	public void setChessSet(Set set) {
		if (this.set != null) {
			this.set.dispose();
		}
		this.set = set;
	}

	public Background getSquareBackground() {
		if (background == null) {
			background = new Background(getStore().getString(
					BOARD_BACKGROUND_KEY));
		}
		return background;
	}

	public void setSquareBackground(Background background) {
		if (this.background != null) {
			this.background.dispose();
		}
		this.background = background;
	}
}
