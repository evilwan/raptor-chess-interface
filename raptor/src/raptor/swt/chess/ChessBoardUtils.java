/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.swt.chess;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.action.RaptorAction;
import raptor.action.SeparatorAction;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.action.game.AutoBishopAction;
import raptor.action.game.AutoDrawAction;
import raptor.action.game.AutoKingAction;
import raptor.action.game.AutoKnightAction;
import raptor.action.game.AutoQueenAction;
import raptor.action.game.AutoRookAction;
import raptor.action.game.BackAction;
import raptor.action.game.ClearPremovesAction;
import raptor.action.game.CommitAction;
import raptor.action.game.FirstAction;
import raptor.action.game.ForceUpdateAction;
import raptor.action.game.ForwardAction;
import raptor.action.game.LastAction;
import raptor.action.game.MoveListAction;
import raptor.action.game.RevertAction;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.Move;
import raptor.chess.Variant;
import raptor.chess.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.service.ActionService;
import raptor.service.ThreadService;
import raptor.swt.chess.controller.ToolBarItemKey;
import raptor.util.SVGUtil;

public class ChessBoardUtils implements BoardConstants {
	public static final String CHESS_SET_DIR = Raptor.RESOURCES_DIR + "set/";
	public static final int DARK_IMAGE_INDEX = 1;
	public static final int LIGHT_IMAGE_INDEX = 0;
	private static final Log LOG = LogFactory.getLog(ChessBoardUtils.class);
	public static final String PIECE_IMAGE_SUFFIX = ".svg";
	private static SecureRandom secureRandom = new SecureRandom();
	public static final String SQUARE_BACKGROUND_DIR = Raptor.RESOURCES_DIR
			+ "square/";
	public static final String SQUARE_BACKGROUND_IMAGE_SUFFIX = ".png";

	public static void addActionsToToolbar(
			final ChessBoardController controller,
			RaptorActionContainer container, ToolBar toolbar,
			boolean isUserWhite) {
		RaptorAction[] toolbarActions = ActionService.getInstance().getActions(
				container);

		for (RaptorAction action : toolbarActions) {
			ToolItem item = createToolItem(action, controller, toolbar,
					isUserWhite);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Added " + action + " to toolbar " + item);
			}
		}
		new ToolItem(toolbar, SWT.SEPARATOR);

	}

	/**
	 * Appends the game to the users game pgn file.
	 */
	public static void appendGameToPgnFile(Game game) {
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.APP_IS_LOGGING_GAMES)) {
			String pgn = game.toPgn();
			File file = new File(Raptor.GAMES_PGN_FILE);
			FileWriter fileWriter = null;
			try {
				fileWriter = new FileWriter(file, true);
				fileWriter.append("\n\n");
				fileWriter.append(pgn);
			} catch (IOException ioe) {
				LOG.error("Error saving game", ioe);
			} finally {
				try {
					fileWriter.close();
				} catch (IOException ioe) {
				}
			}
		}
	}

	public static boolean arePiecesSameColor(int piece1, int piece2) {
		return isWhitePiece(piece1) && isWhitePiece(piece2)
				|| isBlackPiece(piece1) && isBlackPiece(piece2);
	}

	public static Move createDropMove(int fromSquare, int toSquare) {
		int coloredPiece = ChessBoardUtils.pieceJailSquareToPiece(fromSquare);
		int colorlessPiece = ChessBoardUtils
				.pieceFromColoredPiece(coloredPiece);

		return new Move(toSquare, colorlessPiece, ChessBoardUtils
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

		for (File file2 : files) {
			StringTokenizer tok = new StringTokenizer(file2.getName(), ".");
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

	/**
	 * Returns the cursor for the specified piece type.
	 * 
	 * @param type
	 *            The piece type.
	 * @return
	 */
	public static Cursor getCursorForPiece(int type, int width, int height) {

		String key = getChessSetName() + "_" + type + "_" + width + "x"
				+ height;

		Cursor result = Raptor.getInstance().getCursorRegistry().get(key);

		if (result == null) {
			ImageData pieceImageData = getChessPieceImage(type, width, height)
					.getImageData();

			int hotx = pieceImageData.width / 2;
			int hoty = pieceImageData.height / 2;

			result = new Cursor(Raptor.getInstance().getCursorRegistry()
					.getDisplay(), pieceImageData, hotx, hoty);
			Raptor.getInstance().getCursorRegistry().put(key, result);
		}
		return result;
	}

	/**
	 * Returns the quadrant to use for the specified controller. This should not
	 * be used if its the "other" bughouse board. Use
	 * getQuadrantForController(controller,true) for that.
	 */
	public static Quadrant getQuadrantForController(
			ChessBoardController controller) {
		return getQuadrantForController(controller, false);
	}

	public static Quadrant getQuadrantForController(
			ChessBoardController controller, boolean isBughouseOtherBoard) {
		if (controller.getConnector() == null) {
			if (isBughouseOtherBoard) {
				return Raptor.getInstance().getPreferences().getQuadrant(
						PreferenceKeys.APP_BUGHOUSE_GAME_2_QUADRANT);
			} else {
				return Raptor.getInstance().getPreferences().getQuadrant(
						PreferenceKeys.APP_CHESS_BOARD_QUADRANT);
			}
		} else {
			if (isBughouseOtherBoard) {
				return Raptor.getInstance().getPreferences().getQuadrant(
						controller.getConnector().getShortName() + "-"
								+ PreferenceKeys.BUGHOUSE_GAME_2_QUADRANT);

			} else {
				return Raptor.getInstance().getPreferences().getQuadrant(
						controller.getConnector().getShortName() + "-"
								+ PreferenceKeys.CHESS_BOARD_QUADRANT);
			}
		}
	}

	/**
	 * Returns the Image for users current background name
	 */
	public static Image getSquareBackgroundImage(boolean isLight, int width,
			int height) {
		return getSquareBackgroundImage(getSquareBackgroundName(), isLight,
				width, height);
	}

	/**
	 * Returns the Image for users current background name
	 */
	public static Image getSquareBackgroundImage(String name, boolean isLight,
			int width, int height) {

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

		for (File file2 : files) {
			StringTokenizer tok = new StringTokenizer(file2.getName(), ".");
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
	 * Returns a to move indicator indicating the side to move with the
	 * specified width/height this image will always be a square.
	 */
	public static Image getToMoveIndicatorImage(boolean isSideToMove, int width) {
		if (width < 5) {
			width = 5;
		}

		String key = "toMoveIndicator_" + isSideToMove + "_" + width;
		Image result = Raptor.getInstance().getImageRegistry().get(key);
		if (result == null) {
			Image mold = getToMoveIndicatorImageMold(isSideToMove);
			result = new Image(mold.getDevice(), mold.getImageData().scaledTo(
					width, width));
			Raptor.getInstance().getImageRegistry().put(key, result);
		}
		return result;
	}

	/**
	 * Returns a to move indicator indicating the side to move with the
	 * specified width/height this image will always be a square.
	 */
	public static Image getToMoveIndicatorImageMold(boolean isSideToMove) {
		String key = "toMoveIndicator_" + isSideToMove;

		Image result = Raptor.getInstance().getImageRegistry().get(key);
		if (result == null) {
			result = new Image(Raptor.getInstance().getWindow().getShell()
					.getDisplay(), Raptor.IMAGES_DIR + "circle_"
					+ (isSideToMove ? "green" : "gray") + "30x30.png");
			Raptor.getInstance().getImageRegistry().put(key, result);
		}
		return result;
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
		int fullMoveIndex = halfMoveIndex / 2 + 1;

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
		return GameUtils.isDropSquare(pieceJailSquare);
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

			String svgFileName = getSVGChessPieceName(setName, type);
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

	/**
	 * Opens a ChessBoardWindowItem for the specified controller. It is
	 * preferred that you use this method and not create a ChessBoardWindowItem
	 * and add it to RaptorWindow, as it contains code to take-over inactive
	 * window items if they are available. This can greatly increase
	 * performance.
	 * 
	 * This method only handles controllers that are not the "other" bughosue
	 * board. Use openBoard(controller,true) to do that.
	 */
	public static void openBoard(ChessBoardController controller) {
		openBoard(controller, false);
	}

	/**
	 * Opens a ChessBoardWindowItem for the specified controller. It is
	 * preferred that you use this method, as it contains code to take-over
	 * inactive window items if they are available. This can greatly increase
	 * performance.
	 */
	public static void openBoard(final ChessBoardController controller,
			final boolean isBughouseOtherBoard) {
		Raptor.getInstance().getDisplay().asyncExec(new Runnable() {
			public void run() {
				Quadrant quadrant = getQuadrantForController(controller,
						isBughouseOtherBoard);
				ChessBoardWindowItem item = Raptor.getInstance().getWindow()
						.getChessBoardWindowItemToTakeOver(quadrant);

				if (item == null) {
					item = new ChessBoardWindowItem(controller,
							isBughouseOtherBoard);
					Raptor.getInstance().getWindow().addRaptorWindowItem(item);
				} else {
					item.takeOver(controller, isBughouseOtherBoard);
					Raptor.getInstance().getWindow().forceFocus(item);
				}
			}
		});
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

	protected static ToolItem createToolItem(final RaptorAction action,
			final ChessBoardController controller, ToolBar toolbar,
			boolean isUserWhite) {
		ToolItem result = null;
		if (action instanceof SeparatorAction) {
			result = new ToolItem(toolbar, SWT.SEPARATOR);
			return result;
		} else if (action instanceof AutoDrawAction) {
			result = new ToolItem(toolbar, SWT.CHECK);
			controller.addToolItem(ToolBarItemKey.AUTO_DRAW, result);
		} else if (action instanceof BackAction) {
			result = new ToolItem(toolbar, SWT.PUSH);
			controller.addToolItem(ToolBarItemKey.BACK_NAV, result);
		} else if (action instanceof ForwardAction) {
			result = new ToolItem(toolbar, SWT.PUSH);
			controller.addToolItem(ToolBarItemKey.NEXT_NAV, result);
		} else if (action instanceof FirstAction) {
			result = new ToolItem(toolbar, SWT.PUSH);
			controller.addToolItem(ToolBarItemKey.FIRST_NAV, result);
		} else if (action instanceof LastAction) {
			result = new ToolItem(toolbar, SWT.PUSH);
			controller.addToolItem(ToolBarItemKey.LAST_NAV, result);
		} else if (action instanceof RevertAction) {
			result = new ToolItem(toolbar, SWT.PUSH);
			controller.addToolItem(ToolBarItemKey.REVERT_NAV, result);
		} else if (action instanceof CommitAction) {
			result = new ToolItem(toolbar, SWT.PUSH);
			controller.addToolItem(ToolBarItemKey.COMMIT_NAV, result);
		} else if (action instanceof ClearPremovesAction) {
			result = new ToolItem(toolbar, SWT.CHECK);
			controller.addToolItem(ToolBarItemKey.CLEAR_PREMOVES, result);
		} else if (action instanceof MoveListAction) {
			result = new ToolItem(toolbar, SWT.CHECK);
			controller.addToolItem(ToolBarItemKey.MOVE_LIST, result);
		} else if (action instanceof ForceUpdateAction) {
			result = new ToolItem(toolbar, SWT.CHECK);
			controller.addToolItem(ToolBarItemKey.FORCE_UPDATE, result);
		} else if (action instanceof AutoQueenAction) {
			result = new ToolItem(toolbar, SWT.RADIO);
			controller.addToolItem(ToolBarItemKey.AUTO_QUEEN, result);
			result.setText(GameUtils
					.getPieceRepresentation(isUserWhite ? GameConstants.WQ
							: GameConstants.BQ));
		} else if (action instanceof AutoKnightAction) {
			result = new ToolItem(toolbar, SWT.RADIO);
			controller.addToolItem(ToolBarItemKey.AUTO_KNIGHT, result);
			result.setText(GameUtils
					.getPieceRepresentation(isUserWhite ? GameConstants.WN
							: GameConstants.BN));
		} else if (action instanceof AutoBishopAction) {
			result = new ToolItem(toolbar, SWT.RADIO);
			controller.addToolItem(ToolBarItemKey.AUTO_BISHOP, result);
			result.setText(GameUtils
					.getPieceRepresentation(isUserWhite ? GameConstants.WB
							: GameConstants.BB));
		} else if (action instanceof AutoRookAction) {
			result = new ToolItem(toolbar, SWT.RADIO);
			controller.addToolItem(ToolBarItemKey.AUTO_ROOK, result);
			result.setText(GameUtils
					.getPieceRepresentation(isUserWhite ? GameConstants.WR
							: GameConstants.BR));
		} else if (action instanceof AutoKingAction
				&& controller.getGame().getVariant() == Variant.suicide) {
			result = new ToolItem(toolbar, SWT.RADIO);
			controller.addToolItem(ToolBarItemKey.AUTO_KING, result);
			result.setText(GameUtils
					.getPieceRepresentation(isUserWhite ? GameConstants.WK
							: GameConstants.BK));
		} else if (action instanceof AutoKingAction) {
			return null;
		} else {
			result = new ToolItem(toolbar, SWT.PUSH);
		}

		if (StringUtils.isBlank(result.getText())
				&& StringUtils.isBlank(action.getIcon())) {
			result.setText(action.getName());
		} else if (StringUtils.isNotBlank(action.getIcon())) {
			result.setImage(Raptor.getInstance().getIcon(action.getIcon()));
		}

		if (StringUtils.isNotBlank(action.getDescription())) {
			result.setToolTipText(action.getDescription());
		}

		result.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RaptorAction loadedAction = ActionService.getInstance()
						.getAction(action.getName());
				loadedAction.setChessBoardControllerSource(controller);
				loadedAction.run();
			}
		});
		return result;
	}

}
