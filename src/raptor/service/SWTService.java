package raptor.service;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageRegistry;
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

import raptor.gui.board.Background;
import raptor.gui.board.Set;

public class SWTService {
	private static final Log LOG = LogFactory.getLog(SWTService.class);

	public static final String COMMON_PROPERTIES = "resources/common/common.properties";

	// In order to update entries in the font and color registries, all font
	// properties must end in font and all color properties must end in color.
	public static final String BOARD_SET = "board-set";
	public static final String BOARD_BACKGROUND = "board-background";
	public static final String BOARD_IS_SHOW_COORDINATES = "board-show-coordinates";
	public static final String BOARD_PIECE_SIZE_ADJUSTMENT = "board-piece-size-adjustment-percentage";
	public static final String BOARD_HIGHLIGHT_BORDER_WIDTH = "board-highlight-width-percentage";
	public static final String BOARD_IS_SHOWING_PIECE_JAIL = "board-is-showing-piece-jail";

	public static final String BOARD_COORDINATES_COLOR = "board-coordinates-color";
	public static final String BOARD_BACKGROUND_COLOR = "board-background-color";
	public static final String BOARD_HIGHLIGHT_COLOR = "board-highlight-color";
	public static final String BOARD_ACTIVE_CLOCK_COLOR = "board-active-clock-color";
	public static final String BOARD_INACTIVE_CLOCK_COLOR = "board-inactive-clock-color";
	public static final String BOARD_LAG_COLOR = "board-lag-color";
	public static final String BOARD_PLAYER_NAME_COLOR = "board-player-name-color";
	public static final String BOARD_PIECE_JAIL_COLOR = "board-piece-jail-color";

	public static final String BOARD_COORDINATES_FONT = "board-coordinates-font";
	public static final String BOARD_CLOCK_FONT = "board-clock-font";
	public static final String BOARD_LAG_FONT = "board-lag-font";
	public static final String BOARD_PLAYER_NAME_FONT = "board-player-name-font";
	public static final String BOARD_PIECE_JAIL_FONT = "board-piece-jail-font";

	public static final String CHAT_MAX_CONSOLE_CHARS = "chat-max-console-chars";
	public static final String CHAT_TIMESTAMP_CONSOLE = "chat-timestamp-console";

	public static final String CHAT_OUTPUT_FONT = "chat-output-font";
	public static final String CHAT_INPUT_FONT = "chat-input-font";
	public static final String CHAT_LAG_FONT = "chat-lag-font";
	
	public static final String CHAT_OUTPUT_BACKGROUND_COLOR = "chat-output-background-color";
	public static final String CHAT_INPUT_BACKGROUND_COLOR = "chat-input-background-color";
	public static final String CHAT_OUTPUT_TEXT_COLOR = "chat-output-text-color";
	public static final String CHAT_INPUT_DEFAULT_TEXT_COLOR = "chat-input-default-text-color";
	public static final String CHAT_LINK_COLOR = "chat-link-color";


	public static final String[] colorKeys = { BOARD_BACKGROUND_COLOR,
			BOARD_COORDINATES_COLOR, BOARD_HIGHLIGHT_COLOR,
			BOARD_ACTIVE_CLOCK_COLOR, BOARD_INACTIVE_CLOCK_COLOR,
			BOARD_LAG_COLOR, BOARD_LAG_COLOR };

	public static final String[] fontkeys = { BOARD_COORDINATES_FONT,
			BOARD_CLOCK_FONT, BOARD_LAG_FONT, BOARD_PLAYER_NAME_FONT,
			BOARD_PIECE_JAIL_FONT };

	private static final SWTService instance = new SWTService();

	public static SWTService getInstance() {
		return instance;
	}

	private PreferenceStore store = null;
	private ImageRegistry imageRegistry = new ImageRegistry(Display
			.getCurrent());
	private FontRegistry fontRegistry = new FontRegistry(Display.getCurrent());
	private ColorRegistry colorRegistry = new ColorRegistry(Display
			.getCurrent());
	private Set set;

	private Background background;

	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent arg0) {
			if (arg0.getProperty().endsWith("color")) {
				colorRegistry.put(arg0.getProperty(), PreferenceConverter
						.getColor(store, arg0.getProperty()));
			} else if (arg0.getProperty().endsWith("font")) {
				fontRegistry.put(arg0.getProperty(), PreferenceConverter
						.getFontDataArray(store, arg0.getProperty()));
			} else if (arg0.getProperty().equals(BOARD_SET)) {
				LOG.info("Disposing chess set " + set.getName());
				set.dispose();
				set = new Set((String) arg0.getNewValue());
			} else if (arg0.getProperty().equals(BOARD_BACKGROUND)) {
				LOG.info("Disposing background set " + background.getName());
				background.dispose();
				background = new Background((String) arg0.getNewValue());
			}
		}
	};

	public SWTService() {
		try {
			LOG.info("Loading preference store " + COMMON_PROPERTIES);
			store = new PreferenceStore();
			store.setFilename(COMMON_PROPERTIES);
			loadDefaults();
			store.load(new FileInputStream(COMMON_PROPERTIES));
			// Preload set and background.
			getChessSet();
			getSquareBackground();

		} catch (Exception e) {
			LOG
					.warn(
							"Error reading preferences"
									+ COMMON_PROPERTIES
									+ " reverting to default properties (**If this is the first time launching the app this will always occur**)",
							e);
			store = new PreferenceStore();
			store.setFilename(COMMON_PROPERTIES);
			loadDefaults();
			try {
				store.save();
			} catch (IOException e2) {
				LOG.error("***SERIOUS ERROR*** Could not save store "
						+ COMMON_PROPERTIES, e2);
				throw new RuntimeException(e2);
			}
		}

		store.addPropertyChangeListener(propertyChangeListener);
		LOG.info("Loaded preferences");
	}

	public Set getChessSet() {
		if (set == null) {
			set = new Set(getStore().getString(BOARD_SET));
		}
		return set;
	}

	public Color getColor(String key) {
		try {
			if (!colorRegistry.hasValueFor(key)) {
				RGB rgb = PreferenceConverter.getColor(store, key);
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
						store, key);
				fontRegistry.put(key, fontData);
			}
			return fontRegistry.get(key);
		} catch (Throwable t) {
			LOG.error("Error in getFont(" + key + ") Returning default font.",
					t);
			return fontRegistry.defaultFont();
		}
	}

	public ImageRegistry getImageRegistry() {
		return imageRegistry;
	}

	public Point getPoint(String key) {
		return PreferenceConverter.getPoint(store, key);
	}

	public Rectangle getRectangle(String key) {
		return PreferenceConverter.getRectangle(store, key);
	}

	public Background getSquareBackground() {
		if (background == null) {
			background = new Background(getStore().getString(
					BOARD_BACKGROUND));
		}
		return background;
	}

	public PreferenceStore getStore() {
		return store;
	}

	public void loadDefaults() {
		String defaultFontName = fontRegistry.defaultFont().getFontData()[0]
				.getName();
		
		//Board
		store.setDefault(BOARD_SET, "Fantasy");
		store.setDefault(BOARD_BACKGROUND, "Wood2");
		store.setDefault(BOARD_IS_SHOW_COORDINATES, true);
		store.setDefault(BOARD_PIECE_SIZE_ADJUSTMENT, .03);
		store.setDefault(BOARD_HIGHLIGHT_BORDER_WIDTH, .05);
		store.setDefault(BOARD_IS_SHOWING_PIECE_JAIL, true);

		PreferenceConverter.setDefault(store, BOARD_BACKGROUND_COLOR, new RGB(255,255,255));
		PreferenceConverter.setDefault(store, BOARD_COORDINATES_COLOR, new RGB(0,0,0));
		PreferenceConverter.setDefault(store, BOARD_HIGHLIGHT_COLOR, new RGB(0,255,255));
		PreferenceConverter.setDefault(store, BOARD_ACTIVE_CLOCK_COLOR, new RGB(255,0,0));
		PreferenceConverter.setDefault(store, BOARD_INACTIVE_CLOCK_COLOR, new RGB(128,128,128));
		PreferenceConverter.setDefault(store, BOARD_LAG_COLOR, new RGB(128,128,128));
		PreferenceConverter.setDefault(store, BOARD_PLAYER_NAME_COLOR, new RGB(0,0,0));
		PreferenceConverter.setDefault(store, BOARD_PIECE_JAIL_COLOR, new RGB(0,255,0));
	
		PreferenceConverter.setDefault(store, BOARD_COORDINATES_FONT, new FontData[] { new FontData(
				defaultFontName, 14, 0) });
		PreferenceConverter.setDefault(store, BOARD_CLOCK_FONT, new FontData[] { new FontData(
				defaultFontName, 24, SWT.BOLD) });
		PreferenceConverter.setDefault(store, BOARD_LAG_FONT, new FontData[] { new FontData(
				defaultFontName, 14, 0) });
		PreferenceConverter.setDefault(store, BOARD_PLAYER_NAME_FONT, new FontData[] { new FontData(
				defaultFontName, 14, 0) });
		PreferenceConverter.setDefault(store, BOARD_PIECE_JAIL_FONT, new FontData[] { new FontData(
				defaultFontName, 14, 0) });
		
		store.setDefault(BOARD_COORDINATES_FONT,
				StringConverter.asString(new FontData[] { new FontData(
						defaultFontName, 14, 0) }));
		store.setDefault(BOARD_CLOCK_FONT, StringConverter
				.asString(new FontData[] { new FontData(defaultFontName, 24,
						SWT.BOLD) }));
		store.setDefault(BOARD_LAG_FONT,
				StringConverter.asString(new FontData[] { new FontData(
						defaultFontName, 14, 0) }));
		store.setDefault(BOARD_PLAYER_NAME_FONT,
				StringConverter.asString(new FontData[] { new FontData(
						defaultFontName, 14, 0) }));
		store.setDefault(BOARD_PIECE_JAIL_FONT,
				StringConverter.asString(new FontData[] { new FontData(
						defaultFontName, 14, 0) }));
		
		//Chat
		store.setDefault(CHAT_MAX_CONSOLE_CHARS, 500000);
		store.setDefault(CHAT_TIMESTAMP_CONSOLE, false);
		
		PreferenceConverter.setDefault(store, CHAT_INPUT_FONT, new FontData[] { new FontData(
				defaultFontName, 16, 0) });
		PreferenceConverter.setDefault(store, CHAT_OUTPUT_FONT, new FontData[] { new FontData(
				defaultFontName, 16, 0) });
		PreferenceConverter.setDefault(store, CHAT_LAG_FONT, new FontData[] { new FontData(
				defaultFontName, 12, 0) });
		
		PreferenceConverter.setDefault(store, CHAT_INPUT_BACKGROUND_COLOR, new RGB(0,0,0));
		PreferenceConverter.setDefault(store, CHAT_INPUT_DEFAULT_TEXT_COLOR, new RGB(255,255,255));
		PreferenceConverter.setDefault(store, CHAT_OUTPUT_BACKGROUND_COLOR, new RGB(255,255,255));
		PreferenceConverter.setDefault(store, CHAT_OUTPUT_TEXT_COLOR, new RGB(0,0,0));
		PreferenceConverter.setDefault(store, CHAT_LINK_COLOR, new RGB(0,0,255));

		LOG.info("Loaded defaults " + COMMON_PROPERTIES);
	}

	public void setChessSet(Set set) {
		if (this.set != null) {
			this.set.dispose();
		}
		this.set = set;
	}

	public void setImageRegistry(ImageRegistry imageRegistry) {
		this.imageRegistry = imageRegistry;
	}

	public void setPoint(String key, Point point) {
		PreferenceConverter.setValue(store, key, point);
	}

	public void setRectangle(String key, Rectangle rectangle) {
		PreferenceConverter.setValue(store, key, rectangle);
	}

	public void setSquareBackground(Background background) {
		if (this.background != null) {
			this.background.dispose();
		}
		this.background = background;
	}
}
