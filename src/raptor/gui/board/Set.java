package raptor.gui.board;

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
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.util.GameUtils;
import raptor.service.SWTService;

public class Set {
	private static final Log LOG = LogFactory.getLog(Set.class);
	public static final String SET_DIR = "resources/common/set/";

	public static final int EMPTY = 0;
	public static final int WP = 1;
	public static final int WB = 2;
	public static final int WN = 3;
	public static final int WR = 4;
	public static final int WQ = 5;
	public static final int WK = 6;
	public static final int BP = 7;
	public static final int BB = 8;
	public static final int BN = 9;
	public static final int BR = 10;
	public static final int BQ = 11;
	public static final int BK = 12;

	public static final String[] PIECE_TO_NAME = { "", "wp", "wb", "wn", "wr",
			"wq", "wk", "bp", "bb", "bn", "br", "bq", "bk" };

	public static boolean arePiecesSameColor(int piece1, int piece2) {
		return (isWhitePiece(piece1) && isWhitePiece(piece2))
				|| (isBlackPiece(piece1) && isBlackPiece(piece2));
	}

	public static String[] getChessSetNames() {
		List<String> result = new LinkedList<String>();

		File file = new File(SET_DIR);
		File[] files = file.listFiles(new FilenameFilter() {

			public boolean accept(File arg0, String arg1) {
				File file = new File(arg0.getAbsolutePath() + "/" + arg1);
				return file.isDirectory() && !file.getName().startsWith(".");
			}

		});

		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			StringTokenizer tok = new StringTokenizer(files[i].getName(), ".");
			result.add(tok.nextToken());
		}

		Collections.sort(result);
		return result.toArray(new String[0]);
	}

	public static Image[] getImageMolds(String setName) {
		LOG.info("getting chess set image samples " + setName);
		long startTine = System.currentTimeMillis();

		String prefix = SET_DIR + setName + "/";
		String[] fileNames = new File(prefix).list(new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return arg1.indexOf(".") > 1;
			}
		});
		String suffix = fileNames[0].substring(fileNames[0].lastIndexOf('.'),
				fileNames[0].length());

		Image[] images = new Image[PIECE_TO_NAME.length];

		for (int i = 1; i < PIECE_TO_NAME.length; i++) {
			String fileName = prefix + PIECE_TO_NAME[i] + suffix;
			String imageKey = setName + "_" + i + "_stock";
			Image image = SWTService.getInstance().getImageRegistry().get(
					imageKey);
			if (image == null) {
				image = loadImage(fileName);
				SWTService.getInstance().getImageRegistry()
						.put(imageKey, image);
			}
			images[i] = image;
		}
		LOG.info("Retrieved chess set image samples " + setName + " "
				+ (System.currentTimeMillis() - startTine) + "ms");
		return images;
	}

	public static int getSetPieceFromGamePiece(int square, Game game) {
		long squareBB = GameUtils.getBitmap(square);
		int gamePiece = game.getPiece(square);

		switch (gamePiece) {
		case GameConstants.EMPTY:
			return EMPTY;
		case WP:
		case BP:
			return (game.getColorBB(GameConstants.WHITE) & squareBB) == 0 ? BP
					: WP;
		case WN:
		case BN:
			return (game.getColorBB(GameConstants.WHITE) & squareBB) == 0 ? BN
					: WN;
		case WB:
		case BB:
			return (game.getColorBB(GameConstants.WHITE) & squareBB) == 0 ? BB
					: WB;
		case WR:
		case BR:
			return (game.getColorBB(GameConstants.WHITE) & squareBB) == 0 ? BR
					: WR;
		case WQ:
		case BQ:
			return (game.getColorBB(GameConstants.WHITE) & squareBB) == 0 ? BQ
					: WQ;
		case WK:
		case BK:
			return (game.getColorBB(GameConstants.WHITE) & squareBB) == 0 ? BK
					: WK;
		default:
			throw new IllegalArgumentException("Invalid gamePiece" + gamePiece);

		}

	}

	public static boolean isBlackPiece(int setPieceType) {
		return setPieceType > 7 && setPieceType < 13;
	}

	public static boolean isWhitePiece(int setPieceType) {
		return setPieceType > 0 && setPieceType < 7;
	}

	private static Image loadImage(String location) {
		try {
			if (location.endsWith("bmp") || location.endsWith("BMP")) {
				ImageData ideaData = new ImageData(location);
				int transPixel = ideaData.palette
						.getPixel(new RGB(255, 255, 0));
				ideaData.transparentPixel = transPixel;
				return new Image(Display.getCurrent(), ideaData);
			} else {
				return new Image(Display.getCurrent(), location);
			}
		} catch (Exception ioe) {
			throw new IllegalArgumentException("Error loading image "
					+ location, ioe);
		}
	}

	ImageRegistry imageRegistry = new ImageRegistry();

	private String setName;

	public Set(String setName) {
		super();
		this.setName = setName;
	}

	public void dispose() {
		imageRegistry.dispose();
	}

	@Override
	public boolean equals(Object o) {
		if (o != null) {
			return getName().equals(((Set) o).getName());
		} else {
			return false;
		}
	}

	public Image getIcon(int type) {
		return getScaledImage(type, 35, 35);
	}

	public Image getImageMold(int type) {
		if (type == EMPTY) {
			return null;
		} else {
			String imageKey = setName + "_" + type + "_stock";
			Image image = SWTService.getInstance().getImageRegistry().get(
					imageKey);
			if (image == null) {
				return getImageMolds(setName)[type];
			}
			return image;
		}
	}

	public String getName() {
		return setName;
	}

	public Image getScaledImage(int type, int width, int height) {
		if (type == EMPTY) {
			return null;
		} else {

			if (width <= 0 || height <= 0) {
				width = 10;
				height = 10;
			}

			String key = getName() + "_" + type + "_" + width + "x" + height;
			Image image = imageRegistry.get(key);

			if (image == null) {
				Image stockImage = getImageMold(type);
				if (stockImage != null) {
					Image result = new Image(Display.getCurrent(), stockImage
							.getImageData().scaledTo(width, height));
					imageRegistry.put(key, result);
					return result;
				} else {
					LOG.error("Could not find stock image for set " + getName()
							+ " " + type);
					throw new IllegalStateException(
							"Could not find stock image for set " + getName()
									+ " " + type);
				}
			} else {
				return image;
			}
		}
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}
}
