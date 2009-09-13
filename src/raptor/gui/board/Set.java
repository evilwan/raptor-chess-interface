package raptor.gui.board;

import java.io.File;
import java.io.FilenameFilter;
import java.io.ObjectStreamException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;

import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.util.GameUtils;

public class Set {
	private static final String SET_DIR = "resources/common/set/";

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

	private Image[] initialImages = null;

	private Device device = null;

	private String setName;
	
	public static boolean isWhitePiece(int setPieceType) {
		return setPieceType > 0 && setPieceType < 7;
	}
	
	public static boolean isBlackPiece(int setPieceType) {
		return setPieceType > 7 && setPieceType < 13;
	}
	
	public static boolean arePiecesSameColor(int piece1,int piece2) {
		return (isWhitePiece(piece1) && isWhitePiece(piece2)) ||
		       (isBlackPiece(piece1) && isBlackPiece(piece2));
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

	public Set(Device device, String setName) {
		super();
		this.device = device;
		this.setName = setName;
		initImages();
	}

	public static String[] getChessSetNames() {
		List<String> result = new LinkedList<String>();

		File file = new File(SET_DIR);
		File[] files = file.listFiles(new FilenameFilter() {

			public boolean accept(File arg0, String arg1) {
				return arg1.startsWith("SET.") && arg1.indexOf("WBISHOP") != -1;
			}

		});

		for (int i = 0; i < files.length; i++) {
			StringTokenizer tok = new StringTokenizer(files[i].getName(), ".");
			tok.nextToken();
			result.add(tok.nextToken());
		}
		Collections.sort(result);
		return (String[]) result.toArray(new String[0]);
	}

	public boolean equals(Object o) {
		if (o != null) {
			return getName().equals(((Set) o).getName());
		} else {
			return false;
		}
	}

	public int hashCode() {
		return getName().hashCode();
	}

	Object readResolve() throws ObjectStreamException {
		initImages();
		return this;
	}

	private void initImages() {
		final String piecePrefix = "SET." + setName + ".";

		String suffix = "BMP";

		initialImages = new Image[] {
				getInitialChessPieceImage(piecePrefix + "BBISHOP." + suffix),
				getInitialChessPieceImage(piecePrefix + "BKNIGHT." + suffix),
				getInitialChessPieceImage(piecePrefix + "BQUEEN." + suffix),
				getInitialChessPieceImage(piecePrefix + "BPAWN." + suffix),
				getInitialChessPieceImage(piecePrefix + "BKING." + suffix),
				getInitialChessPieceImage(piecePrefix + "BROOK." + suffix),
				getInitialChessPieceImage(piecePrefix + "WBISHOP." + suffix),
				getInitialChessPieceImage(piecePrefix + "WKNIGHT." + suffix),
				getInitialChessPieceImage(piecePrefix + "WQUEEN." + suffix),
				getInitialChessPieceImage(piecePrefix + "WPAWN." + suffix),
				getInitialChessPieceImage(piecePrefix + "WKING." + suffix),
				getInitialChessPieceImage(piecePrefix + "WROOK." + suffix) };

	}

	public String getName() {
		return setName;
	}

	public Image getChessPieceImage(int chessPiece) {
		switch (chessPiece) {
		case BB: {
			return initialImages[0];
		}
		case BK: {
			return initialImages[4];
		}
		case BN: {
			return initialImages[1];
		}
		case BP: {
			return initialImages[3];
		}
		case BQ: {
			return initialImages[2];
		}
		case BR: {
			return initialImages[5];
		}
		case WB: {
			return initialImages[6];
		}
		case WK: {
			return initialImages[10];
		}
		case WN: {
			return initialImages[7];
		}
		case WP: {
			return initialImages[9];
		}
		case WQ: {
			return initialImages[8];
		}
		case WR: {
			return initialImages[11];
		}
		case EMPTY: {
			return null;
		}
		default: {
			throw new IllegalArgumentException("Invalid piece " + chessPiece);
		}
		}
	}

	public Image getIcon(int type) {
		return getScaledImage(type, 35, 35);
	}

	public Image getScaledImage(int type, int width, int height) {

		if (width <= 0 || height <= 0) {
			width = 10;
			height = 10;
		}

		Image image = getChessPieceImage(type);
		if (image != null) {
			return new Image(device, image.getImageData().scaledTo(width,
					height));
		} else {
			return null;
		}
	}

	private Image getInitialChessPieceImage(String location) {
		try {
			ImageData ideaData = new ImageData(SET_DIR + location);
			int transPixel = ideaData.palette.getPixel(new RGB(255, 255, 0));
			ideaData.transparentPixel = transPixel;
			return new Image(device, ideaData);
		} catch (Exception ioe) {
			throw new IllegalArgumentException("Error loading image "
					+ location, ioe);
		}
	}
}
