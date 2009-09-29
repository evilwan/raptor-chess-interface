package raptor.swt.chess;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import raptor.Raptor;
import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.Move;
import raptor.game.util.GameUtils;
import raptor.pref.RaptorPreferenceStore;
import raptor.service.ThreadService;
import raptor.util.RaptorStringUtils;
import raptor.util.SVGUtil;

public class BoardUtils implements BoardConstants {
	private static final Log LOG = LogFactory.getLog(BoardUtils.class);
	public static final String CHESS_SET_DIR = "resources/common/set/";
	public static final int DARK_IMAGE_INDEX = 1;
	public static final int LIGHT_IMAGE_INDEX = 0;
	public static final String PIECE_IMAGE_SUFFIX = ".svg";
	public static final String SQUARE_BACKGROUND_DIR = "resources/common/square/";
	public static final String SQUARE_BACKGROUND_IMAGE_SUFFIX = ".png";
	private static SecureRandom secureRandom = new SecureRandom();

	public static boolean arePiecesSameColor(int piece1, int piece2) {
		return (isWhitePiece(piece1) && isWhitePiece(piece2))
				|| (isBlackPiece(piece1) && isBlackPiece(piece2));
	}

	public static Move createDropMove(int fromSquare, int toSquare) {
		int coloredPiece = BoardUtils.pieceJailSquareToPiece(fromSquare);
		int colorlessPiece = BoardUtils.pieceFromColoredPiece(coloredPiece);

		return new Move(toSquare, colorlessPiece, BoardUtils
				.isWhitePiece(coloredPiece) ? WHITE : BLACK);
	}

	/**
	 * Returns a chess piece suitable for using as an icon when dragging it.
	 */
	public static Image getChessPieceDragImage(int type) {
		return getChessPieceImage(type, 35, 35);
	}

	/**
	 * Returns an icon size chess piece, suitable for displaying in a toolbar or
	 * coolbar.
	 */
	public static Image getChessPieceIconImage(String set, int type) {
		return getChessPieceImage(set, type, 35, 35);
	}

	/**
	 * Returns the image from the users image cache matching the type,width, and
	 * height. If the image is in the localImageRegistry it is returned.
	 * Otherwise the users image cache is checked, if its not there then it is
	 * loaded form the svg file and cached in the users image cache.
	 */
	public static Image getChessPieceImage(int type, int width, int height) {
		if (type == EMPTY) {
			return null;
		} else {

			return getChessPieceImage(getChessSetName(), type, width, height);
		}
	}

	/**
	 * Returns the image with the specified of the specified name,type,width and
	 * height. If the image is in the localImageRegistry it is returned.
	 * Otherwise the users image cache is checked, if its not there then it is
	 * loaded form the svg file and cached in the users image cache.
	 */
	public static Image getChessPieceImage(String name, int type, int width,
			int height) {
		if (type == EMPTY) {
			return null;
		} else {

			if (width <= 0 || height <= 0) {
				width = 10;
				height = 10;
			}

			String key = name + "_" + type + "_" + width + "x" + height;
			Image image = Raptor.getInstance().getImageRegistry().get(key);

			if (image == null) {
				Image result = new Image(Display.getCurrent(),
						loadChessPieceFromUserCache(name, type, width, height));
				Raptor.getInstance().getImageRegistry().put(key, result);
				return result;
			} else {
				return image;
			}
		}
	}

	/**
	 * Returns the users current chess set name.
	 */
	public static String getChessSetName() {
		return Raptor.getInstance().getPreferences().getString(
				BOARD_CHESS_SET_NAME);
	}

	/**
	 * Returns a list of all chess set names.
	 */
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

	public static int getColoredPiece(int piece, boolean isWhite) {
		switch (piece) {
		case PAWN:
			return isWhite ? WP : BP;
		case KNIGHT:
			return isWhite ? WN : BN;
		case BISHOP:
			return isWhite ? WB : BB;
		case ROOK:
			return isWhite ? WR : BR;
		case QUEEN:
			return isWhite ? WQ : BQ;
		case KING:
			return isWhite ? WK : BK;
		case EMPTY:
			return EMPTY;
		default:
			throw new IllegalArgumentException("Invalid piece " + piece);
		}
	}

	public static int getColoredPiece(int square, Game game) {
		long squareBB = GameUtils.getBitboard(square);
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

	/**
	 * Returns the Image for users current background name
	 */
	public static Image getSquareBackgroundImage(boolean isLight, int width,
			int height) {
		String name = getSquareBackgroundName();

		if (width <= 0 || height <= 0) {
			width = 10;
			height = 10;
		}

		String key = name + "_" + isLight + "_" + width + "x" + height;

		Image result = Raptor.getInstance().getImageRegistry().get(key);

		if (result == null) {
			result = new Image(Display.getCurrent(), getSquareBackgroundMold(
					name, isLight).getImageData().scaledTo(width, height));
			Raptor.getInstance().getImageRegistry().put(key, result);
			return result;
		} else {
			return result;
		}
	}

	/**
	 * Returns the path to the backgrund image name.
	 */
	public static String getSquareBackgroundImageName(
			String squareBackgroundName, boolean isLight) {
		return SQUARE_BACKGROUND_DIR + squareBackgroundName + "/"
				+ (isLight ? "light" : "dark") + SQUARE_BACKGROUND_IMAGE_SUFFIX;
	}

	/**
	 * Returns an image in the default size of the specified background name and
	 * color.
	 */
	public static Image getSquareBackgroundMold(String name, boolean isLight) {
		return Raptor.getInstance().getImage(
				getSquareBackgroundImageName(name, isLight));
	}

	/**
	 * Returns the users current square background name.
	 */
	public static String getSquareBackgroundName() {
		return Raptor.getInstance().getPreferences().getString(
				BOARD_SQUARE_BACKGROUND_NAME);
	}

	/**
	 * Returns a list of all the square background names.
	 */
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

	/**
	 * Returns the path to the specified svg chess piece.
	 */
	public static String getSVGChessPieceName(String chessSetName, int piece) {
		return CHESS_SET_DIR + chessSetName + "/" + PIECE_TO_NAME[piece]
				+ PIECE_IMAGE_SUFFIX;
	}

	/**
	 * Returns the path to the specified chess piece in the users image cache.
	 */
	public static String getUserImageCachePieceName(String chessSetName,
			int piece, int width, int height) {
		return Raptor.USER_RAPTOR_HOME_PATH + "/imagecache/" + chessSetName
				+ "_" + PIECE_TO_NAME[piece] + "_" + width + "_" + height
				+ ".png";

	}

	public static String halfMoveIndexToDescription(int halfMoveIndex,
			int colorToMove) {
		int fullMoveIndex = (halfMoveIndex / 2) + 1;

		return colorToMove == WHITE ? fullMoveIndex + ") " : fullMoveIndex
				+ ") ... ";
	}

	public static boolean isBlackPiece(int setPieceType) {
		return setPieceType >= 7 && setPieceType < 13;
	}

	public static boolean isPieceJailSquare(int pieceJailSquare) {
		return pieceJailSquare > 100 ? true : false;
	}

	public static boolean isWhitePiece(int setPieceType) {
		return setPieceType > 0 && setPieceType < 7;
	}

	public static String lagToString(long lag) {

		if (lag < 0) {
			lag = 0;
		}

		int seconds = (int) (lag / 1000L);
		int tenths = (int) (lag % 1000) / 100;

		return "Lag " + seconds + "." + tenths + " sec";
	}

	/**
	 * Attempts to load the png file from the users cache. If the file wasnt
	 * there it loads the SVG file, which will save the png file for later
	 * loading.
	 */
	public static ImageData loadChessPieceFromUserCache(String setName,
			int type, int width, int height) {
		long startTime = System.currentTimeMillis();

		String userCacheFilePath = getUserImageCachePieceName(setName, type,
				width, height);
		try {
			return new ImageData(userCacheFilePath);
		} catch (Throwable t) {
			LOG.debug("Could not find " + userCacheFilePath
					+ " in the users cache. Loading from svg.");
			return loadChessPieceImageFromSVG(setName, type, width, height);
		} finally {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Loaded piece from user cache in "
						+ (System.currentTimeMillis() - startTime));
			}
		}
	}

	/**
	 * Loads the image from the svg file and saves a png version in the users
	 * cache on a seperate thread.
	 */
	public static ImageData loadChessPieceImageFromSVG(final String setName,
			final int type, final int width, final int height) {
		try {
			long startTime = System.currentTimeMillis();

			String svgFileName = getSVGChessPieceName(getChessSetName(), type);
			final ImageData svgImageData = SVGUtil.loadSVG(svgFileName, width,
					height);

			long delay = 10000 + secureRandom.nextInt(200) * 1000;

			// Now save it in the users piece cache so it is available next
			// time.
			ThreadService.getInstance().scheduleOneShot(delay, new Runnable() {
				public void run() {
					try {
						ImageLoader loader = new ImageLoader();
						loader.data = new ImageData[] { svgImageData };
						String userCacheFileName = getUserImageCachePieceName(
								setName, type, width, height);
						if (LOG.isDebugEnabled()) {
							LOG.debug("Saving " + userCacheFileName);
						}
						loader.save(userCacheFileName, SWT.IMAGE_PNG);
					} catch (Throwable t) {
						LOG.error("Error writing image to image cache.", t);
					}
				}
			});

			if (LOG.isDebugEnabled()) {
				LOG.debug("Loading " + svgFileName + " in "
						+ (System.currentTimeMillis() - startTime));
			}
			return svgImageData;

		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public static String pieceCountToString(int count) {
		if (count < 2) {
			return "";
		} else {
			return "" + count;
		}
	}

	public static int pieceFromColoredPiece(int coloredPiece) {
		switch (coloredPiece) {
		case EMPTY:
			return GameConstants.EMPTY;
		case WP:
		case BP:
			return GameConstants.PAWN;
		case WN:
		case BN:
			return GameConstants.KNIGHT;
		case WB:
		case BB:
			return GameConstants.BISHOP;
		case WR:
		case BR:
			return GameConstants.ROOK;
		case WQ:
		case BQ:
			return GameConstants.QUEEN;
		case WK:
		case BK:
			return GameConstants.KING;
		default:
			throw new IllegalArgumentException("Invalid coloredPiece "
					+ coloredPiece);

		}
	}

	public static int pieceJailSquareToPiece(int pieceJailSquare) {
		return pieceJailSquare - 100;
	}

	public static String timeToString(long timeMillis) {

		long timeLeft = timeMillis;

		if (timeLeft < 0) {
			timeLeft = 0;
		}

		RaptorPreferenceStore prefs = Raptor.getInstance().getPreferences();

		if (timeLeft >= prefs.getLong(BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN)) {
			int hour = (int) (timeLeft / (60000L * 60));
			timeLeft -= hour * 60 * 1000 * 60;
			int minute = (int) (timeLeft / 60000L);
			return RaptorStringUtils.defaultTimeString(hour, 2) + ":"
					+ RaptorStringUtils.defaultTimeString(minute, 2);

		} else if (timeLeft >= prefs
				.getLong(BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN)) {
			int hour = (int) (timeLeft / (60000L * 60));
			timeLeft -= hour * 60 * 1000 * 60;
			int minute = (int) (timeLeft / 60000L);
			timeLeft -= minute * 60 * 1000;
			int seconds = (int) (timeLeft / 1000L);
			return RaptorStringUtils.defaultTimeString(minute, 2) + ":"
					+ RaptorStringUtils.defaultTimeString(seconds, 2);

		} else {
			int hour = (int) (timeLeft / (60000L * 60));
			timeLeft -= hour * 60 * 1000 * 60;
			int minute = (int) (timeLeft / 60000L);
			timeLeft -= minute * 60 * 1000;
			int seconds = (int) (timeLeft / 1000L);
			timeLeft -= seconds * 1000;
			int tenths = (int) (timeLeft / 100L);
			return RaptorStringUtils.defaultTimeString(minute, 2) + ":"
					+ RaptorStringUtils.defaultTimeString(seconds, 2) + "."
					+ RaptorStringUtils.defaultTimeString(tenths, 1);
		}
	}

}
