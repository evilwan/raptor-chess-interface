package raptor.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceStore;

import raptor.gui.board.Background;
import raptor.gui.board.Set;

public class PreferenceService {
	private static final Log log = LogFactory.getLog(PreferenceService.class);

	public static final String COMMON_PROPERTIES = "resources/common/common.properties";

	public static final String BOARD_SET_KEY = "board-set";
	public static final String BOARD_BACKGROUND_KEY = "board-background";
	public static final String BOARD_SHOW_COORDINATES_KEY = "board-show-coordinates";
	public static final String BOARD_PIECE_SIZE_ADJUSTMENT_KEY = "board-piece-size-adjustment";
	public static final String BOARD_HIGHLIGHT_BORDER_WIDTH_KEY = "board-highlight-width";
	public static final String BOARD_SQUARE_HIGHLIGHT_COLOR_KEY = "board-square-highlight-color";

	private static final PreferenceService instance = new PreferenceService();

	private PreferenceStore preferenceStore = null;
	private Set set;
	private Background background;

	public PreferenceService() {
		try {
			preferenceStore = new PreferenceStore(COMMON_PROPERTIES);
			defaultConfig();
			saveConfig();
			
			//Preload set and background.
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
		preferenceStore.setDefault(BOARD_SET_KEY, "WCN");
		preferenceStore.setDefault(BOARD_BACKGROUND_KEY, "CrumpledPaper");
		preferenceStore.setDefault(BOARD_SHOW_COORDINATES_KEY, true);
		preferenceStore.setDefault(BOARD_PIECE_SIZE_ADJUSTMENT_KEY, .03);
		preferenceStore.setDefault(BOARD_HIGHLIGHT_BORDER_WIDTH_KEY, .05);
		preferenceStore.setDefault(BOARD_SQUARE_HIGHLIGHT_COLOR_KEY, 5);
	}

	public PreferenceStore getConfig() {
		return preferenceStore;
	}
	
	public Set getChessSet() {
		if (set == null) {
			set = new Set(getConfig().getString(BOARD_SET_KEY));
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
			background = new Background(getConfig().getString(BOARD_BACKGROUND_KEY));
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
