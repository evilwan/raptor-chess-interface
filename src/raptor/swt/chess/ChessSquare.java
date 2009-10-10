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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import raptor.Raptor;
import raptor.chess.GameConstants;
import raptor.chess.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.swt.SWTUtils;

/**
 * A class representing a chess square on a chess board. This class can also be
 * used to represent a piece jail or drop square.
 */
public class ChessSquare extends Canvas implements BoardConstants {
	static final Log LOG = LogFactory.getLog(ChessSquare.class);
	public static final String CLICK_INITIATOR = "CLICK_INITIATOR";
	public static final String DRAG_INITIATOR = "DRAG_INITIATOR";
	public static final String LAST_DROP_TIME = "LAST_DROP_TIME";
	public static final String DROP_HANDLED = "DROP_HANNDLED";

	protected ChessBoard board;
	protected boolean ignoreBackgroundImage = false;
	protected int id;
	protected boolean isHighlighted;
	protected boolean isLight;
	protected int piece;
	protected Image pieceImage;
	protected boolean isHidingPiece;

	/**
	 * Forces a layout when the size of the square changes.
	 */
	protected ControlListener controlListener = new ControlListener() {

		public void controlMoved(ControlEvent e) {
		}

		public void controlResized(ControlEvent e) {
			clearCache();
		}
	};

	/**
	 * Handles drags and drops.
	 */
	protected Listener dndListener = new Listener() {
		public void handleEvent(Event e) {
			switch (e.type) {
			case SWT.DragDetect: {
				if (board.getController().canUserInitiateMoveFrom(id)) {
					board.getControl()
							.setData(DRAG_INITIATOR, ChessSquare.this);
					board.getControl().setData(DROP_HANDLED, false);
					updateCursorForDrag(piece);
					board.controller.userInitiatedMove(id, true);
				} else {
					board.getControl().setData(DRAG_INITIATOR, null);
					board.getControl().setData(DROP_HANDLED, false);
				}
				break;
			}
			case SWT.MouseUp: {
				ChessSquare dragSource = (ChessSquare) board.getControl()
						.getData(DRAG_INITIATOR);
				if (dragSource == null) {
					return;
				}
				updateCursorForDragEnd();

				ChessSquare dragEnd = getSquareCursorIsAt();

				if (dragEnd == null) {
					board.controller.userCancelledMove(dragSource.id, true);
					board.getControl().setData(LAST_DROP_TIME,
							System.currentTimeMillis());
					board.getControl().setData(DROP_HANDLED, false);
				} else {
					board.controller.userMadeMove(dragSource.id, dragEnd.id);
					board.getControl().setData(DROP_HANDLED, true);
				}
				board.getControl().setData(DRAG_INITIATOR, null);
				board.getControl().setData(CLICK_INITIATOR, null);
				break;
			}
			}
		}
	};
	MouseListener mouseListener = new MouseListener() {
		public void mouseDoubleClick(MouseEvent e) {
		}

		public void mouseDown(MouseEvent e) {
			if (e.button == 3) {
				board.controller.userRightClicked(id);
			}
		}

		public void mouseUp(MouseEvent e) {
			if (e.button == 2) {
				board.controller.userMiddleClicked(id);
			} else if (e.button == 1) {
				Long lastDropTime = (Long) board.getControl().getData(
						LAST_DROP_TIME);
				if (lastDropTime == null
						|| System.currentTimeMillis() - lastDropTime > 100) {
					ChessSquare initiator = (ChessSquare) board.getControl()
							.getData(CLICK_INITIATOR);

					if (initiator == null) {// Start of move
						if (board.controller.canUserInitiateMoveFrom(id)) {
							board.getControl().setData(CLICK_INITIATOR,
									ChessSquare.this);
							board.controller.userInitiatedMove(id, false);
						}
					} else {
						if (ChessSquare.this == initiator) {// Clicked
							// on
							// same
							// square
							// twice.
							board.controller.userCancelledMove(initiator.id,
									false);
							board.getControl().setData(CLICK_INITIATOR, null);
						} else if (BoardUtils.arePiecesSameColor(piece,
								initiator.piece)) {// Clicked
							// on
							// same
							// piece color
							// type.
							board.controller.userCancelledMove(initiator.id,
									false);
							board.controller.userInitiatedMove(id, false);
							board.getControl().setData(CLICK_INITIATOR,
									ChessSquare.this);
						} else {// A valid move
							board.controller.userMadeMove(initiator.id, id);
							board.getControl().setData(CLICK_INITIATOR, null);
						}
					}
				}
			}
		}
	};
	PaintListener paintListener = new PaintListener() {
		public void paintControl(PaintEvent e) {
			Point size = getSize();
			if (!ignoreBackgroundImage) {
				Image backgroundImage = getBackgrondImage(isLight, size.x,
						size.y);
				if (backgroundImage != null) {
					e.gc.drawImage(backgroundImage, 0, 0);
				}
			} else {
				e.gc.fillRectangle(0, 0, size.x, size.y);
			}

			int borderHighlightWidth = getHighlightBorderWidth();

			e.gc.setForeground(getForeground());
			if (isHighlighted) {
				for (int i = 0; i < getHighlightBorderWidth(); i++) {
					e.gc.drawRectangle(i, i, size.x - 1 - i * 2, size.x - 1 - i
							* 2);
				}
			}

			int imageSide = getImageSize(borderHighlightWidth);

			if (pieceImage == null && piece != EMPTY) {
				pieceImage = getChessPieceImage(piece, imageSide, imageSide);
			}

			if (pieceImage != null && !isHidingPiece()) {
				int pieceImageX = (size.x - imageSide) / 2;
				int pieceImageY = (size.y - imageSide) / 2;
				e.gc.drawImage(pieceImage, pieceImageX, pieceImageY);
			}

			String fileLabel = getFileLabel();
			if (fileLabel != null) {
				e.gc.setForeground(getPreferences().getColor(
						BOARD_COORDINATES_COLOR));
				e.gc.setFont(SWTUtils.getProportionalFont(getPreferences()
						.getFont(BOARD_COORDINATES_FONT), 20, size.y));

				int fontHeight = e.gc.getFontMetrics().getAscent()
						+ e.gc.getFontMetrics().getDescent() + 0;

				e.gc.drawString(fileLabel, size.x
						- e.gc.getFontMetrics().getAverageCharWidth() - 2,
						size.y - fontHeight, true);
			}

			String rankLabel = getRankLabel();
			if (rankLabel != null) {
				e.gc.setForeground(getPreferences().getColor(
						BOARD_COORDINATES_COLOR));
				e.gc.setFont(SWTUtils.getProportionalFont(getPreferences()
						.getFont(BOARD_COORDINATES_FONT), 20, size.y));

				e.gc.drawString(rankLabel, 0, 0, true);
			}

		}
	};

	/**
	 * Creates a ChessSquare tied to the specified board.
	 * 
	 * @param id
	 *            The square id. An integer representing the squares index in
	 *            GameConstants (e.g.
	 *            GameConstants.SQUARE_A1,GameConstants.SQUARE_A2, etc).
	 *            Drop/Piece Jail squares should use the constants
	 *            (GameConstants
	 *            .BP_DROP_FROM_SQUARE,GameConstants.BN_DROP_FROM_SQUARE,etc).
	 * @param isLight
	 *            True if this is a square with a light background, false if its
	 *            a square with a dark background.
	 */
	public ChessSquare(Composite parent, ChessBoard chessBoard, int id,
			boolean isLight) {
		super(parent, SWT.DOUBLE_BUFFERED);
		board = chessBoard;
		this.id = id;
		this.isLight = isLight;
		addPaintListener(paintListener);
		addControlListener(controlListener);
		addMouseListener(mouseListener);
		addListener(SWT.DragDetect, dndListener);
		addListener(SWT.MouseUp, dndListener);

	}

	/**
	 * Creates a ChessSquare not tied to a board. Useful in preferences. Use
	 * with care, this does'nt add any listeners besides the PaointListener and
	 * board will be null.
	 */
	public ChessSquare(Composite parent, int id, boolean isLight) {
		super(parent, SWT.DOUBLE_BUFFERED);
		this.id = id;
		this.isLight = isLight;
		addPaintListener(paintListener);
	}

	/**
	 * Clears any cached images this square is maintaining. Useful when swapping
	 * out chess sets or square backgrounds.
	 */
	public void clearCache() {
		pieceImage = null;
	}

	/**
	 * Provided so it can easily be overridden.By default uses the
	 * RaptorPreferenceStore setting.
	 */
	protected Image getBackgrondImage(boolean isLight, int width, int height) {
		return BoardUtils.getSquareBackgroundImage(isLight, width, height);
	}

	/**
	 * Provided so it can easily be overridden.By default uses the
	 * RaptorPreferenceStore setting.
	 */
	protected Image getChessPieceImage(int piece, int width, int height) {
		return BoardUtils.getChessPieceImage(piece, width, height);
	}

	/**
	 * Provided so it can easily be overridden.By default uses the
	 * RaptorPreferenceStore setting.
	 */
	protected String getFileLabel() {
		if (Raptor.getInstance().getPreferences().getBoolean(
				BOARD_IS_SHOW_COORDINATES)
				&& !BoardUtils.isPieceJailSquare(id)) {
			if (board.isWhiteOnTop) {
				if ((GameUtils.getBitboard(id) & GameConstants.RANK8) != 0) {
					return "" + GameConstants.SQUARE_TO_FILE_SAN.charAt(id);
				}
			} else {
				if ((GameUtils.getBitboard(id) & GameConstants.RANK1) != 0) {
					return "" + GameConstants.SQUARE_TO_FILE_SAN.charAt(id);
				}
			}
		}
		return null;
	}

	/**
	 * Provided so it can easily be overridden.By default uses the
	 * RaptorPreferenceStore setting.
	 */
	protected int getHighlightBorderWidth() {
		return (int) (getSize().x * getPreferences().getDouble(
				BOARD_HIGHLIGHT_BORDER_WIDTH));
	}

	/**
	 * Provided so it can easily be overridden.By default uses the
	 * RaptorPreferenceStore setting.
	 */
	protected Color getHighlightColor() {
		return getForeground();
	}

	/**
	 * An integer representing the squares index in GameConstants (e.g.
	 * GameConstants.SQUARE_A1,GameConstants.SQUARE_A2, etc). Drop/Piece Jail
	 * squares should use the constants (GameConstants
	 * .BP_DROP_FROM_SQUARE,GameConstants.BN_DROP_FROM_SQUARE,etc).
	 */
	public int getId() {
		return id;
	}

	/**
	 * Provided so it can easily be overridden.By default uses the
	 * RaptorPreferenceStore setting.
	 */
	protected int getImageSize(int borderWidth) {
		double imageSquareSideAdjustment = getPreferences().getDouble(
				BOARD_PIECE_SIZE_ADJUSTMENT);
		int imageSide = (int) ((getSize().x - borderWidth * 2) * (1.0 - imageSquareSideAdjustment));
		if (imageSide % 2 != 0) {
			imageSide = imageSide - 1;
		}

		return imageSide;
	}

	/**
	 * An integer representing the colored piece type in GameConstants. (e.g.
	 * GameConstants.WP,GameConstants.WN,GameConstants.WQ,GameConstants.EMPTY,
	 * etc).
	 */
	public int getPiece() {
		return piece;
	}

	/**
	 * Provided so it can easily be overridden.By default returns the preference
	 * store in Raptor.getInstance.
	 */
	protected RaptorPreferenceStore getPreferences() {
		return Raptor.getInstance().getPreferences();
	}

	protected String getRankLabel() {
		if (Raptor.getInstance().getPreferences().getBoolean(
				BOARD_IS_SHOW_COORDINATES)
				&& !BoardUtils.isPieceJailSquare(id)) {
			if (board.isWhiteOnTop) {
				if ((GameUtils.getBitboard(id) & GameConstants.HFILE) != 0) {
					return "" + GameConstants.SQUARE_TO_RANK_SAN.charAt(id);
				}
			} else {
				if ((GameUtils.getBitboard(id) & GameConstants.AFILE) != 0) {
					return "" + GameConstants.SQUARE_TO_RANK_SAN.charAt(id);
				}
			}
		}
		return null;
	}

	/**
	 * Returns the square the cursor is at if its ChessBoard is equal to this
	 * ChessSquares ChessBoard. Otherwise returns null.
	 */
	protected ChessSquare getSquareCursorIsAt() {
		Control control = getDisplay().getCursorControl();

		while (control != null && !(control instanceof ChessSquare)) {
			control = control.getParent();
		}

		ChessSquare result = null;

		if (control instanceof ChessSquare) {
			result = (ChessSquare) control;
			if (result.board != board) {
				result = null;
			}
		}
		return result;
	}

	/**
	 * Highlights this square. This method does not invoke redraw. That is left
	 * to the caller
	 */
	public void highlight() {
		if (!isHighlighted) {
			isHighlighted = true;
		}
	}

	/**
	 * Returns true if this square is hiding its piece, otherwise false. This is
	 * useful during drag operations when the board is refreshed.
	 */
	public boolean isHidingPiece() {
		return isHidingPiece;
	}

	/**
	 * Returns true if this square has a light background, false otherwise.
	 * 
	 * @return
	 */
	public boolean isLight() {
		return isHighlighted;
	}

	/**
	 * Sets whether or not the piece is being hidden. This is useful during dnd
	 * operations when you want to prevent board refreshes.
	 * 
	 * @param hidePiece
	 *            True if the piece should be hidden, false otherwise.
	 */
	public void setHidingPiece(boolean isHidingPiece) {
		this.isHidingPiece = isHidingPiece;
	}

	/**
	 * Sets the vaue of this squares background. True if light, false if dark.
	 * 
	 * @param isLight
	 */
	public void setLight(boolean isLight) {
		isHighlighted = isLight;
	}

	/**
	 * Sets the colored chess piece. This method does not redraw after the piece
	 * is set. It is up to the caller to invoke that.
	 * 
	 * @param piece
	 *            An integer representing the colored piece type in
	 *            GameConstants. (e.g.
	 *            GameConstants.WP,GameConstants.WN,GameConstants
	 *            .WQ,GameConstants.EMPTY, etc).
	 */
	public void setPiece(int piece) {
		if (this.piece != piece) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Setting piece in square " + id + " " + piece);
			}
			this.piece = piece;
			pieceImage = null;
		}
	}

	/**
	 * Unhighlights this square. This method does not redraw after invoked. That
	 * is up to the caller.
	 */
	public void unhighlight() {
		if (isHighlighted) {
			isHighlighted = false;
		}
	}

	/**
	 * Updates the cursor for a drag with the specified piece.
	 */
	protected void updateCursorForDrag(int piece) {
		if (getPreferences().getBoolean(
				PreferenceKeys.BOARD_IS_USING_CROSSHAIRS_CURSOR)) {
			getShell().setCursor(
					Raptor.getInstance().getDisplay().getSystemCursor(
							SWT.CURSOR_CROSS));
		} else {
			int imageSide = getImageSize(getHighlightBorderWidth());
			getShell().setCursor(
					BoardUtils.getCursorForPiece(piece, imageSide, imageSide));
		}
	}

	/**
	 * Updates the cursor after a drop is finished.
	 */
	protected void updateCursorForDragEnd() {
		getShell().setCursor(
				Raptor.getInstance().getCursorRegistry().getDefaultCursor());
	}
}
