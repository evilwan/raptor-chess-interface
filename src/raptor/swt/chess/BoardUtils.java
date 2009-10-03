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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.Raptor;
import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.Move;
import raptor.game.util.GameUtils;
import raptor.pref.PreferenceKeys;
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

	/**
	 * Adds the promotion icons to the toolbar.
	 * 
	 * @param isUserWhite
	 *            True if white pieces should be added, otherwise false.
	 * @param controller
	 *            The controller to add the ToolItems to.
	 * @param toolbar
	 *            The toolbar to add the items to.
	 */
	public static void addNavIconsToToolbar(
			final ChessBoardController controller, ToolBar toolbar) {
		LOG.debug("Adding addNavIconsToToolbar to toolbar");

		if (controller.isNavigatable()) {
			ToolItem firstButtonItem = new ToolItem(toolbar, SWT.FLAT);
			firstButtonItem.setImage(Raptor.getInstance().getIcon("first"));
			firstButtonItem.setToolTipText("Go to the first move played");
			firstButtonItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					controller.onNavFirst();

				}
			});
			controller.addToolItem(ChessBoardController.BACK_NAV,
					firstButtonItem);
		}

		if (controller.isNavigatable()) {
			ToolItem backButton = new ToolItem(toolbar, SWT.FLAT);
			backButton.setImage(Raptor.getInstance().getIcon("back"));
			backButton.setToolTipText("Go to the previous move played");
			backButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					controller.onNavBack();

				}
			});
			controller.addToolItem(ChessBoardController.BACK_NAV, backButton);
		}

		if (controller.isRevertable()) {
			ToolItem revertButton = new ToolItem(toolbar, SWT.FLAT);
			revertButton.setImage(Raptor.getInstance().getIcon(
					"counterClockwise"));
			revertButton.setToolTipText("Revert back to main-variation.");
			revertButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					controller.onNavRevert();

				}
			});
			controller.addToolItem(ChessBoardController.REVERT_NAV,
					revertButton);
		}

		if (controller.isCommitable()) {
			ToolItem commitButton = new ToolItem(toolbar, SWT.FLAT);
			commitButton.setImage(Raptor.getInstance().getIcon("clockwise"));
			commitButton.setToolTipText("Commit sub-variation.");
			commitButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					controller.onNavCommit();

				}
			});
			controller.addToolItem(ChessBoardController.COMMIT_NAV,
					commitButton);
		}

		if (controller.isNavigatable()) {
			ToolItem nextButton = new ToolItem(toolbar, SWT.FLAT);
			nextButton.setImage(Raptor.getInstance().getIcon("next"));
			nextButton.setToolTipText("Go to the next move played");
			nextButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					controller.onNavForward();

				}
			});
			controller.addToolItem(ChessBoardController.NEXT_NAV, nextButton);
		}

		if (controller.isNavigatable()) {
			ToolItem lastButton = new ToolItem(toolbar, SWT.FLAT);
			lastButton.setImage(Raptor.getInstance().getIcon("last"));
			lastButton.setToolTipText("Go to the last move played");
			lastButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					controller.onNavLast();

				}
			});
			controller.addToolItem(ChessBoardController.LAST_NAV, lastButton);
		}

		ToolItem flipButton = new ToolItem(toolbar, SWT.FLAT);
		flipButton.setImage(Raptor.getInstance().getIcon("flip"));
		flipButton.setToolTipText("Flips the chess board.");
		controller.addToolItem(ChessBoardController.FLIP, flipButton);
		flipButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				controller.onFlip();
			}
		});

		ToolItem fenButton = new ToolItem(toolbar, SWT.FLAT);
		fenButton.setText("FEN");
		fenButton
				.setToolTipText("Shows the FEN (Forsyth Edwards Notation) of the current position.");
		fenButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Raptor.getInstance().promptForText(
						"FEN for game " + controller.getGame().getId() + ":",
						controller.getGame().toFEN());
			}
		});

		if (controller.isPremoveable()) {
			ToolItem premoveButton = new ToolItem(toolbar, SWT.FLAT);
			premoveButton.setImage(Raptor.getInstance().getIcon("redx"));
			premoveButton.setToolTipText("Clears all premoves.");
			controller.addToolItem(ChessBoardController.PREMOVE, premoveButton);
			premoveButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					controller.onClearPremoves();
				}
			});
		}

		if (controller.isAutoDrawable()) {
			ToolItem autoDrawButton = new ToolItem(toolbar, SWT.CHECK);
			autoDrawButton.setImage(Raptor.getInstance().getIcon("draw"));
			autoDrawButton
					.setToolTipText("Offer a draw after every move you make.");
			controller.addToolItem(ChessBoardController.AUTO_DRAW,
					autoDrawButton);
		}
	}

	/**
	 * Adds the promotion icons to the toolbar.
	 * 
	 * @param isUserWhite
	 *            True if white pieces should be added, otherwise false.
	 * @param controller
	 *            The controller to add the ToolItems to.
	 * @param toolbar
	 *            The toolbar to add the items to.
	 */
	public static final void addPromotionIconsToToolbar(boolean isUserWhite,
			ChessBoardController controller, ToolBar toolbar) {
		ToolItem queenPromote = new ToolItem(toolbar, SWT.RADIO);
		queenPromote
				.setText(getPieceRepresentation(isUserWhite ? GameConstants.WQ
						: GameConstants.BQ));
		controller.addToolItem(ChessBoardController.AUTO_QUEEN, queenPromote);
		queenPromote.setToolTipText("Auto Queen");
		queenPromote.setSelection(true);

		ToolItem knightPromote = new ToolItem(toolbar, SWT.RADIO);
		knightPromote
				.setText(getPieceRepresentation(isUserWhite ? GameConstants.WN
						: GameConstants.BN));
		controller.addToolItem(ChessBoardController.AUTO_KNIGHT, knightPromote);
		knightPromote.setToolTipText("Auto Knight");
		queenPromote.setSelection(false);

		ToolItem bishopPromote = new ToolItem(toolbar, SWT.RADIO);
		bishopPromote
				.setText(getPieceRepresentation(isUserWhite ? GameConstants.WB
						: GameConstants.BB));
		controller.addToolItem(ChessBoardController.AUTO_BISHOP, bishopPromote);
		knightPromote.setToolTipText("Auto Bishop");
		bishopPromote.setSelection(false);

		ToolItem rookPromote = new ToolItem(toolbar, SWT.RADIO);
		rookPromote
				.setText(getPieceRepresentation(isUserWhite ? GameConstants.WR
						: GameConstants.BR));
		controller.addToolItem(ChessBoardController.AUTO_ROOK, rookPromote);
		knightPromote.setToolTipText("Auto Rook");
		rookPromote.setSelection(false);
	}

	public static void addSetupIconsToToolbar(
			final ChessBoardController controller, ToolBar toolbar) {

		ToolItem setupInitial = new ToolItem(toolbar, SWT.FLAT);
		setupInitial.setText("Initial");
		setupInitial.setToolTipText("Sets up the initial position.");
		setupInitial.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				controller.onSetupStart();

			}
		});
		controller.addToolItem(ChessBoardController.SETUP_START, setupInitial);

		ToolItem setupClear = new ToolItem(toolbar, SWT.FLAT);
		setupClear.setText("Clear");
		setupClear.setToolTipText("Clears all pieces from the chess board.");
		setupClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				controller.onSetupClear();
			}
		});
		controller.addToolItem(ChessBoardController.SETUP_CLEAR, setupClear);

		ToolItem setupDone = new ToolItem(toolbar, SWT.FLAT);
		setupDone.setText("Done");
		setupDone.setToolTipText("Completes setup mode.");
		setupDone.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				controller.onSetupDone();
			}
		});
		controller.addToolItem(ChessBoardController.SETUP_DONE, setupDone);

		ToolItem fromFenButton = new ToolItem(toolbar, SWT.FLAT);
		fromFenButton.setText("FromFEN");
		fromFenButton
				.setToolTipText("Sets up the position from a specified FEN (Forsyth Edwards Notation) string.");
		fromFenButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String result = Raptor.getInstance().promptForText(
						"Enter the FEN to set the position to:");
				if (result != null) {
					controller.onSetupFen(result);
				}
			}
		});
	}

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

	public static Move createMove(Game game, int fromSquare, int toSquare) {
		try {
			Move result = game.makeMove(fromSquare, toSquare);
			game.rollback();
			return result;
		} catch (IllegalArgumentException iae) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("IllegalArgumentException in game.makeMove()", iae);
			}
			return null;
		}
	}

	public static Move createMove(Game game, int fromSquare, int toSquare,
			int nonColoredPromotionPiece) {
		try {
			Move result = game.makeMove(fromSquare, toSquare,
					nonColoredPromotionPiece);
			game.rollback();
			return result;
		} catch (IllegalArgumentException iae) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("IllegalArgumentException in game.makeMove()", iae);
			}
			return null;
		}
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

	public static String getPieceRepresentation(int coloredPiece) {
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.BOARD_IS_SHOWING_PIECE_UNICODE_CHARS)) {
			switch (coloredPiece) {
			case WK:
				return "\u2654";
			case WQ:
				return "\u2655";
			case WR:
				return "\u2656";
			case WB:
				return "\u2657";
			case WN:
				return "\u2658";
			case WP:
				return "\u2659";
			case BK:
				return "\u256A";
			case BQ:
				return "\u265B";
			case BR:
				return "\u265C";
			case BB:
				return "\u265D";
			case BN:
				return "\u265E";
			case BP:
				return "\u265F";
			}
		} else {
			switch (coloredPiece) {
			case WK:
				return "N";
			case WQ:
				return "Q";
			case WR:
				return "R";
			case WB:
				return "B";
			case WN:
				return "N";
			case WP:
				return "P";
			case BK:
				return "k";
			case BQ:
				return "q";
			case BR:
				return "r";
			case BB:
				return "b";
			case BN:
				return "n";
			case BP:
				return "p";
			}
		}

		throw new IllegalArgumentException("Invalid piece: " + coloredPiece);
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

	public static boolean isJailSquareBlackPiece(int pieceJailSquare) {
		return isBlackPiece(pieceJailSquareToPiece(pieceJailSquare));
	}

	public static boolean isJailSquareWhitePiece(int pieceJailSquare) {
		return isWhitePiece(pieceJailSquareToPiece(pieceJailSquare));
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
