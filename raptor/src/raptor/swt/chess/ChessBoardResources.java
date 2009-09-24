package raptor.swt.chess;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import raptor.Raptor;

public class ChessBoardResources implements Constants {
	public static final String CHESS_SET_DIR = "resources/common/set/";
	public static final int DARK_IMAGE_INDEX = 1;
	public static final int LIGHT_IMAGE_INDEX = 0;
	private static final Log LOG = LogFactory.getLog(ChessBoardResources.class);
	public static final String PIECE_IMAGE_SUFFIX = ".png";
	public static final String SQUARE_BACKGROUND_DIR = "resources/common/square/";
	public static final String SQUARE_BACKGROUND_IMAGE_SUFFIX = ".png";

	public static String getChessPieceImageName(String chessSetName, int piece) {
		return CHESS_SET_DIR + chessSetName + "/" + PIECE_TO_NAME[piece]
				+ PIECE_IMAGE_SUFFIX;
	}

	public static String[] getChessSetNames() {
		List<String> result = new LinkedList<String>();

		File file = new File(CHESS_SET_DIR);
		File[] files = file.listFiles(new FilenameFilter() {

			public boolean accept(File arg0, String arg1) {
				File file = new File(arg0.getAbsolutePath() + "/" + arg1);
				return file.isDirectory() && !file.getName().startsWith(".");
			}

		});

		for (int i = 0; i < files.length; i++) {
			StringTokenizer tok = new StringTokenizer(files[i].getName(), ".");
			result.add(tok.nextToken());
		}

		Collections.sort(result);
		return result.toArray(new String[0]);
	}

	public static String getSquareBackgroundImageName(
			String squareBackgroundName, boolean isLight) {
		return SQUARE_BACKGROUND_DIR + squareBackgroundName + "/"
				+ (isLight ? "light" : "dark") + SQUARE_BACKGROUND_IMAGE_SUFFIX;
	}

	public static String[] getSquareBackgroundNames() {
		List<String> result = new LinkedList<String>();

		File file = new File(SQUARE_BACKGROUND_DIR);
		File[] files = file.listFiles(new FilenameFilter() {

			public boolean accept(File arg0, String arg1) {
				File file = new File(arg0.getAbsolutePath() + "/" + arg1);
				return file.isDirectory() && !file.getName().startsWith(".");
			}

		});

		for (int i = 0; i < files.length; i++) {
			StringTokenizer tok = new StringTokenizer(files[i].getName(), ".");
			result.add(tok.nextToken());
		}

		Collections.sort(result);
		return result.toArray(new String[0]);
	}

	ChessBoard board;
	ImageRegistry localImageRegistry = new ImageRegistry();

	public ChessBoardResources(ChessBoard board) {
		this.board = board;
	}

	public void dispose() {
		localImageRegistry.dispose();
		board = null;
		LOG.debug("Disposed ChessBoardResources");
	}

	public Image getChessPieceDragImage(int type) {
		return getChessPieceImage(type, 35, 35);
	}

	public Image getChessPieceIconImage(String set, int type) {
		return getChessPieceImage(set, type, 35, 35);
	}

	public Image getChessPieceImage(String name, int type, int width, int height) {
		if (type == EMPTY) {
			return null;
		} else {

			if (width <= 0 || height <= 0) {
				width = 10;
				height = 10;
			}

			String key = name + "_" + type + "_" + width + "x" + height;
			Image image = localImageRegistry.get(key);

			if (image == null) {
				Image stockImage = getChessPieceImageMold(name, type);
				if (stockImage != null) {
					Image result = new Image(Display.getCurrent(), stockImage
							.getImageData().scaledTo(width, height));
					localImageRegistry.put(key, result);
					return result;
				} else {
					LOG.error("Could not find stock image for set " + name
							+ " " + type);
					throw new IllegalStateException(
							"Could not find stock image for set " + name + " "
									+ type);
				}
			} else {
				return image;
			}
		}
	}

	public Image getChessPieceImage(int type, int width, int height) {
		if (type == EMPTY) {
			return null;
		} else {

			return getChessPieceImage(getChessSetName(), type, width, height);
		}
	}

	public static Image getChessPieceImageMold(String name, int type) {
		if (type == EMPTY) {
			return null;
		} else {

			return Raptor.getInstance().getPreferences().getImage(
					getChessPieceImageName(name, type));
		}
	}

	public String getChessSetName() {
		return board.getPreferences().getString(BOARD_CHESS_SET_NAME);
	}

	public Image getSquareBackgroundImage(boolean isLight, int width, int height) {
		String name = getSquareBackgroundName();

		if (width <= 0 || height <= 0) {
			width = 10;
			height = 10;
		}

		String key = name + "_" + isLight + "_" + width + "x" + height;

		Image result = localImageRegistry.get(key);

		if (result == null) {
			result = new Image(Display.getCurrent(), getSquareBackgroundMold(
					name, isLight).getImageData().scaledTo(width, height));
			localImageRegistry.put(key, result);
			return result;
		} else {
			return result;
		}
	}

	public static Image getSquareBackgroundMold(String name, boolean isLight) {
		return Raptor.getInstance().getPreferences().getImage(
				getSquareBackgroundImageName(name, isLight));
	}

	public String getSquareBackgroundName() {
		return board.getPreferences().getString(BOARD_SQUARE_BACKGROUND_NAME);

	}
}
