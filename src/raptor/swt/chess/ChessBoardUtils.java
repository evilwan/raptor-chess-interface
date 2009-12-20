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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorWindowItem;
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
import raptor.action.game.CastleLongAction;
import raptor.action.game.CastleShortAction;
import raptor.action.game.ClearPremovesAction;
import raptor.action.game.CommitAction;
import raptor.action.game.FirstAction;
import raptor.action.game.ForceUpdateAction;
import raptor.action.game.ForwardAction;
import raptor.action.game.LastAction;
import raptor.action.game.MatchWinnerAction;
import raptor.action.game.MoveListAction;
import raptor.action.game.RematchAction;
import raptor.action.game.RevertAction;
import raptor.action.game.ToggleEngineAnalysisAction;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.GameCursor;
import raptor.chess.Move;
import raptor.chess.Variant;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.service.ActionScriptService;
import raptor.service.ThreadService;
import raptor.service.UCIEngineService;
import raptor.swt.chess.controller.BughouseSuggestController;
import raptor.swt.chess.controller.ExamineController;
import raptor.swt.chess.controller.InactiveController;
import raptor.swt.chess.controller.ObserveController;
import raptor.swt.chess.controller.ToolBarItemKey;
import raptor.util.RaptorRunnable;
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

	public static final Object PGN_PREPEND_SYNCH = new Object();

	public static void addActionsToToolbar(
			final ChessBoardController controller,
			RaptorActionContainer container, ToolBar toolbar,
			boolean isUserWhite) {
		RaptorAction[] toolbarActions = ActionScriptService.getInstance()
				.getActions(container);

		for (RaptorAction action : toolbarActions) {
			ToolItem item = createToolItem(action, controller, toolbar,
					isUserWhite);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Added " + action + " to toolbar " + item);
			}
		}

		if (!controller.getPreferences().getBoolean(
				PreferenceKeys.BOARD_COOLBAR_MODE)) {
			new ToolItem(toolbar, SWT.SEPARATOR);
		}
	}

	public static void adjustCoolbar(ChessBoard board, ToolBar toolbar) {
		clearCoolbar(board);
		toolbar.pack();
		CoolItem coolItem = new CoolItem(board.getCoolbar(), SWT.NONE);
		coolItem.setControl(toolbar);
		toolbar.pack();
		coolItem
				.setPreferredSize(toolbar.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		board.getCoolbar().setVisible(true);
		board.getControl().layout();
	}

	public static boolean arePiecesSameColor(int piece1, int piece2) {
		return isWhitePiece(piece1) && isWhitePiece(piece2)
				|| isBlackPiece(piece1) && isBlackPiece(piece2);
	}

	public static void clearCoolbar(ChessBoard board) {
		CoolBar coolbar = board.getCoolbar();
		CoolItem[] items = coolbar.getItems();
		for (CoolItem item : items) {
			if (item.getControl() != null && !item.getControl().isDisposed()) {
				item.getControl().dispose();
			}
			item.dispose();
		}
		board.getCoolbar().setVisible(false);
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
						PreferenceKeys.APP_CHESS_BOARD_SECONDARY_QUADRANT);
			} else {
				return Raptor.getInstance().getPreferences().getQuadrant(
						PreferenceKeys.APP_CHESS_BOARD_QUADRANT);
			}
		} else {
			if (isBughouseOtherBoard) {
				return Raptor
						.getInstance()
						.getPreferences()
						.getQuadrant(
								controller.getConnector().getShortName()
										+ "-"
										+ PreferenceKeys.CHESS_BOARD_SECONDARY_QUADRANT);

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

	public static boolean isChessSetOptimized(String setName) {
		File file = new File(getUserImageCachePieceName(setName,
				GameConstants.WP, 6, 6));
		File file2 = new File(getUserImageCachePieceName(setName,
				GameConstants.BP, 100, 100));
		return file.exists() && file2.exists();

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
			Raptor.getInstance().onError("Error loading SVG image.", ioe);
			return null;
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
		Raptor.getInstance().getDisplay().asyncExec(
				new RaptorRunnable(controller.getConnector()) {
					@Override
					public void execute() {
						Quadrant quadrant = getQuadrantForController(
								controller, isBughouseOtherBoard);
						ChessBoardWindowItem item = null;

						Quadrant primaryQuadrant = controller.getConnector() == null ? Raptor
								.getInstance()
								.getPreferences()
								.getQuadrant(
										PreferenceKeys.APP_CHESS_BOARD_QUADRANT)
								: Raptor
										.getInstance()
										.getPreferences()
										.getQuadrant(
												controller.getConnector()
														.getShortName()
														+ "-"
														+ PreferenceKeys.CHESS_BOARD_QUADRANT);
						Quadrant secondaryQuadrant = controller.getConnector() == null ? Raptor
								.getInstance()
								.getPreferences()
								.getQuadrant(
										PreferenceKeys.APP_CHESS_BOARD_SECONDARY_QUADRANT)
								: Raptor
										.getInstance()
										.getPreferences()
										.getQuadrant(
												controller.getConnector()
														.getShortName()
														+ "-"
														+ PreferenceKeys.CHESS_BOARD_SECONDARY_QUADRANT);

						if (Raptor.getInstance().getPreferences().getBoolean(
								PreferenceKeys.BOARD_TAKEOVER_INACTIVE_GAMES)) {
							item = Raptor
									.getInstance()
									.getWindow()
									.getChessBoardWindowItemToTakeOver(quadrant);
							if (item == null
									&& controller.getGame().getVariant() != Variant.bughouse
									&& controller.getGame().getVariant() != Variant.fischerRandomBughouse) {
								item = Raptor
										.getInstance()
										.getWindow()
										.getChessBoardWindowItemToTakeOver(
												quadrant == primaryQuadrant ? secondaryQuadrant
														: primaryQuadrant);
							}

							if (item != null) {
								item.getBoard().hideEngineAnalysisWidget();
								item.getBoard().hideMoveList();
							}
						}

						if (item == null) {
							item = new ChessBoardWindowItem(controller,
									isBughouseOtherBoard);

							// This block of code overrides the quadrant if
							// there is an
							// active controller already there observing a chess
							// game.
							if ((item.getPreferredQuadrant() == primaryQuadrant || item
									.getPreferredQuadrant() == secondaryQuadrant)
									&& !Variant.isBughouse(item.getController()
											.getGame().getVariant())) {
								RaptorWindowItem[] items = Raptor.getInstance()
										.getWindow().getWindowItems(
												item.getPreferredQuadrant());
								boolean swapQuadrants = false;
								for (RaptorWindowItem currentItem : items) {
									if (currentItem instanceof ChessBoardWindowItem) {
										if (((ChessBoardWindowItem) currentItem)
												.getConnector() != null) {
											swapQuadrants = true;
										}
									}
								}
								if (swapQuadrants) {
									if (item.getPreferredQuadrant() == primaryQuadrant) {
										item.setQuadrant(secondaryQuadrant);
									} else if (item.getPreferredQuadrant() == secondaryQuadrant) {
										item.setQuadrant(primaryQuadrant);
									}
								}
							}

							Raptor.getInstance().getWindow()
									.addRaptorWindowItem(item);
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

	/**
	 * Prepends the game to the users game pgn file.
	 */
	public static void prependGameToPgnFile(Game game) {
		if (Variant.isBughouse(game.getVariant())) {
			return;
		}
		if (Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.APP_IS_LOGGING_GAMES)
				&& game.getMoveList().getSize() > 0) {

			// synchronized on PGN_PREPEND_SYNCH so just one thread at a time
			// writes to the file.
			synchronized (PGN_PREPEND_SYNCH) {

				if (game instanceof GameCursor) {
					game = ((GameCursor) game).getMasterGame();
				}

				String whiteRating = game.getHeader(PgnHeader.WhiteElo);
				String blackRating = game.getHeader(PgnHeader.BlackElo);

				whiteRating = StringUtils.remove(whiteRating, 'E');
				whiteRating = StringUtils.remove(whiteRating, 'P');
				blackRating = StringUtils.remove(blackRating, 'E');
				blackRating = StringUtils.remove(blackRating, 'P');

				if (!NumberUtils.isDigits(whiteRating)) {
					game.removeHeader(PgnHeader.WhiteElo);
				}
				if (!NumberUtils.isDigits(blackRating)) {
					game.removeHeader(PgnHeader.BlackElo);
				}

				String pgn = game.toPgn();
				File file = new File(Raptor.GAMES_PGN_FILE);
				FileWriter fileWriter = null;
				BufferedReader fileReader = null;
				try {
					if (file.exists()) {
						// Write the new pgn to a temp file.
						File tempFile = File.createTempFile(
								"RaptorUserPgnFile", ".pgn");
						fileWriter = new FileWriter(tempFile);
						fileWriter.append(pgn + "\n\n");

						// Now write the rest of the pgn to the temp file.
						fileReader = new BufferedReader(new FileReader(file));
						String currentLine = null;
						while ((currentLine = fileReader.readLine()) != null) {
							fileWriter.append(currentLine + "\n");
						}

						// flush and close.
						fileWriter.flush();
						fileWriter.close();
						fileReader.close();

						// now write the temp file contents to the main file.
						fileReader = new BufferedReader(
								new FileReader(tempFile));
						fileWriter = new FileWriter(file);

						while ((currentLine = fileReader.readLine()) != null) {
							fileWriter.write(currentLine + "\n");
						}
						fileWriter.flush();

						tempFile.delete();

					} else {
						fileWriter = new FileWriter(file, false);
						fileWriter.append(pgn);
						fileWriter.flush();
					}
				} catch (IOException ioe) {
					LOG.error("Error saving game", ioe);
				} finally {
					try {
						if (fileWriter != null) {
							fileWriter.close();
						}
						if (fileReader != null) {
							fileReader.close();
						}
					} catch (IOException ioe) {
					}
				}
			}
		}
	}

	protected static ToolItem createToolItem(final RaptorAction action,
			final ChessBoardController controller, ToolBar toolbar,
			boolean isUserWhite) {
		ToolItem result = null;
		if (action instanceof SeparatorAction) {
			result = new ToolItem(toolbar, SWT.SEPARATOR);
			return result;
		} else if (action instanceof RematchAction) {
			if (controller.getConnector() == null) {
				return null;
			}
			result = new ToolItem(toolbar, SWT.PUSH);
			result.setText(action.getName());
		} else if (action instanceof MatchWinnerAction) {
			if (controller instanceof BughouseSuggestController) {
				return null;
			}
			result = new ToolItem(toolbar, SWT.CHECK);
			controller.addToolItem(ToolBarItemKey.MATCH_WINNER, result);

		} else if (action instanceof ToggleEngineAnalysisAction) {
			if (controller instanceof InactiveController
					|| controller instanceof ExamineController
					|| controller instanceof ObserveController) {

				if (Variant.isClassic(controller.getGame().getVariant())
						&& UCIEngineService.getInstance().getDefaultEngine() != null) {
					result = new ToolItem(toolbar, SWT.CHECK);
					controller.addToolItem(
							ToolBarItemKey.TOGGLE_ANALYSIS_ENGINE, result);

					if (controller.getBoard() != null
							&& controller.getBoard().isShowingEngineAnaylsis()) {
						result.setSelection(true);
					}
				}
			}

			if (result == null) {
				return null;
			}
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
			int pieceSize = Raptor.getInstance().getPreferences().getInt(
					PreferenceKeys.APP_TOOLBAR_PIECE_SIZE);
			result.setImage(getChessPieceImage("Portable", isUserWhite ? WQ
					: BQ, pieceSize, pieceSize));
		} else if (action instanceof AutoKnightAction) {
			result = new ToolItem(toolbar, SWT.RADIO);
			controller.addToolItem(ToolBarItemKey.AUTO_KNIGHT, result);
			int pieceSize = Raptor.getInstance().getPreferences().getInt(
					PreferenceKeys.APP_TOOLBAR_PIECE_SIZE);
			result.setImage(getChessPieceImage("Portable", isUserWhite ? WN
					: BN, pieceSize, pieceSize));
		} else if (action instanceof AutoBishopAction) {
			result = new ToolItem(toolbar, SWT.RADIO);
			controller.addToolItem(ToolBarItemKey.AUTO_BISHOP, result);
			int pieceSize = Raptor.getInstance().getPreferences().getInt(
					PreferenceKeys.APP_TOOLBAR_PIECE_SIZE);
			result.setImage(getChessPieceImage("Portable", isUserWhite ? WB
					: BB, pieceSize, pieceSize));
		} else if (action instanceof AutoRookAction) {
			result = new ToolItem(toolbar, SWT.RADIO);
			controller.addToolItem(ToolBarItemKey.AUTO_ROOK, result);
			int pieceSize = Raptor.getInstance().getPreferences().getInt(
					PreferenceKeys.APP_TOOLBAR_PIECE_SIZE);
			result.setImage(getChessPieceImage("Portable", isUserWhite ? WR
					: BR, pieceSize, pieceSize));
		} else if (action instanceof AutoKingAction
				&& controller.getGame().getVariant() == Variant.suicide) {
			result = new ToolItem(toolbar, SWT.RADIO);
			controller.addToolItem(ToolBarItemKey.AUTO_KING, result);
			int pieceSize = Raptor.getInstance().getPreferences().getInt(
					PreferenceKeys.APP_TOOLBAR_PIECE_SIZE);
			result.setImage(getChessPieceImage("Portable", isUserWhite ? WK
					: BK, pieceSize, pieceSize));
		} else if (action instanceof AutoKingAction) {
			return null;
		} else if (action instanceof CastleLongAction
				&& controller.getGame().isInState(Game.FISCHER_RANDOM_STATE)) {
			result = new ToolItem(toolbar, SWT.PUSH);
			controller.addToolItem(ToolBarItemKey.CASTLE_LONG, result);
		} else if (action instanceof CastleLongAction) {
			return null;
		} else if (action instanceof CastleShortAction
				&& controller.getGame().isInState(Game.FISCHER_RANDOM_STATE)) {
			result = new ToolItem(toolbar, SWT.PUSH);
			controller.addToolItem(ToolBarItemKey.CASTLE_SHORT, result);
		} else if (action instanceof CastleShortAction) {
			return null;
		} else {
			result = new ToolItem(toolbar, SWT.PUSH);
		}

		if (StringUtils.isBlank(result.getText())
				&& StringUtils.isBlank(action.getIcon())
				&& result.getImage() == null) {
			result.setText(action.getName());
		} else if (StringUtils.isNotBlank(action.getIcon())
				&& result.getImage() == null) {
			result.setImage(Raptor.getInstance().getIcon(action.getIcon()));
		}

		if (StringUtils.isNotBlank(action.getDescription())) {
			result.setToolTipText(action.getDescription());
		}

		result.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RaptorAction loadedAction = ActionScriptService.getInstance()
						.getAction(action.getName());
				loadedAction.setChessBoardControllerSource(controller);
				loadedAction.run();
			}
		});
		return result;
	}
}
