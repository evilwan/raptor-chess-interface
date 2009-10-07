package raptor.swt.chess;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;

import raptor.Raptor;
import raptor.game.Game.Result;
import raptor.pref.PreferenceKeys;
import raptor.swt.SWTUtils;

/**
 * Paints the game result over the chess board if it is inactive.
 */
public class ResultChessBoardDecoration implements BoardConstants {

	protected ChessBoard board;
	protected int resultFontHeight;

	protected Font resultFont;

	protected boolean isHiding = false;

	public ResultChessBoardDecoration(ChessBoard board) {
		this.board = board;
		init();
	}

	/**
	 * Draws the result text centered in the specified component.
	 * 
	 * @param e
	 *            The paint event to draw in.
	 * @param text
	 *            The text to draw.
	 */
	protected void drawResultText(PaintEvent e, String text) {
		if (text != null) {
			e.gc.setForeground(Raptor.getInstance().getPreferences().getColor(
					PreferenceKeys.BOARD_RESULT_COLOR));

			e.gc.setFont(getResultFont(e.height));

			Point extent = e.gc.stringExtent(text);
			e.gc.drawString(text, e.width / 2 - extent.x / 2, e.height / 2
					- extent.y / 2, true);
		}
	}

	/**
	 * Returns the font to use for the result text drawn over the board. This
	 * font grows and shrinks depending on the size of the square the text is
	 * drawn in.
	 * 
	 * @param width
	 *            The width of the square.
	 * @param height
	 *            The height of the square.
	 */
	protected synchronized Font getResultFont(int height) {
		if (resultFont == null) {
			resultFont = Raptor.getInstance().getPreferences().getFont(
					PreferenceKeys.BOARD_RESULT_FONT);
			resultFont = SWTUtils.getProportionalFont(resultFont, 80, height);
			resultFontHeight = height;
		} else {
			if (resultFontHeight != height) {
				Font newFont = SWTUtils.getProportionalFont(resultFont, 80,
						height);
				resultFontHeight = height;
				resultFont = newFont;
			}
		}
		return resultFont;
	}

	protected void init() {
		// Add paint listeners to the squares to redraw the result.
		board.getSquare(SQUARE_D4).addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (!isHiding
						&& !board.isWhiteOnTop()
						&& board.getController().getGame().getResult() != Result.IN_PROGRESS) {
					String text = null;
					if (board.getController().getGame().getResult() == Result.WHITE_WON) {
						text = "1";
					} else if (board.getController().getGame().getResult() == Result.BLACK_WON) {
						text = "0";
					} else if (board.getController().getGame().getResult() == Result.DRAW) {
						text = ".5";
					} else if (board.getController().getGame().getResult() == Result.UNDETERMINED) {

					}
					drawResultText(e, text);
				}
			}
		});

		board.getSquare(SQUARE_E4).addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (!isHiding
						&& !board.isWhiteOnTop()
						&& board.getController().getGame().getResult() != Result.IN_PROGRESS) {

					String text = null;
					if (board.getController().getGame().getResult() == Result.WHITE_WON) {
						text = "-";
					} else if (board.getController().getGame().getResult() == Result.BLACK_WON) {
						text = "-";
					} else if (board.getController().getGame().getResult() == Result.DRAW) {
						text = "-";
					} else if (board.getController().getGame().getResult() == Result.UNDETERMINED) {
						text = "*";
					}
					drawResultText(e, text);
				}
			}
		});

		board.getSquare(SQUARE_F4).addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (!isHiding
						&& !board.isWhiteOnTop()
						&& board.getController().getGame().getResult() != Result.IN_PROGRESS) {

					String text = null;
					if (board.getController().getGame().getResult() == Result.WHITE_WON) {
						text = "0";
					} else if (board.getController().getGame().getResult() == Result.BLACK_WON) {
						text = "1";
					} else if (board.getController().getGame().getResult() == Result.DRAW) {
						text = ".5";
					}
					drawResultText(e, text);
				}
			}
		});

		board.getSquare(SQUARE_E5).addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (!isHiding
						&& board.isWhiteOnTop()
						&& board.getController().getGame().getResult() != Result.IN_PROGRESS) {
					String text = null;
					if (board.getController().getGame().getResult() == Result.WHITE_WON) {
						text = "1";
					} else if (board.getController().getGame().getResult() == Result.BLACK_WON) {
						text = "0";
					} else if (board.getController().getGame().getResult() == Result.DRAW) {
						text = ".5";
					} else if (board.getController().getGame().getResult() == Result.UNDETERMINED) {

					}
					drawResultText(e, text);
				}
			}
		});

		board.getSquare(SQUARE_D5).addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (!isHiding
						&& board.isWhiteOnTop()
						&& board.getController().getGame().getResult() != Result.IN_PROGRESS) {

					String text = null;
					if (board.getController().getGame().getResult() == Result.WHITE_WON) {
						text = "-";
					} else if (board.getController().getGame().getResult() == Result.BLACK_WON) {
						text = "-";
					} else if (board.getController().getGame().getResult() == Result.DRAW) {
						text = "-";
					} else if (board.getController().getGame().getResult() == Result.UNDETERMINED) {
						text = "*";
					}
					drawResultText(e, text);
				}
			}
		});

		board.getSquare(SQUARE_C5).addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (!isHiding
						&& board.isWhiteOnTop()
						&& board.getController().getGame().getResult() != Result.IN_PROGRESS) {

					String text = null;
					if (board.getController().getGame().getResult() == Result.WHITE_WON) {
						text = "0";
					} else if (board.getController().getGame().getResult() == Result.BLACK_WON) {
						text = "1";
					} else if (board.getController().getGame().getResult() == Result.DRAW) {
						text = ".5";
					}
					drawResultText(e, text);
				}
			}
		});
	}

	/**
	 * If is hiding = true, hides the result being painted over the chess board.
	 * Otherwise shows it.
	 * 
	 * isHiding is false by default.
	 */
	public void setHiding(boolean isHiding) {
		this.isHiding = isHiding;
		board.redrawSquares();
	}

}
