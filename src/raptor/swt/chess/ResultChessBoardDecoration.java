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

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;

import raptor.Raptor;
import raptor.chess.Result;
import raptor.pref.PreferenceKeys;
import raptor.swt.SWTUtils;

/**
 * Paints the game result over the chess board if it is inactive.
 */
public class ResultChessBoardDecoration implements BoardConstants {

	protected ChessBoard board;
	protected boolean isHiding = false;

	protected Font resultFont;

	protected int resultFontHeight;

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
						&& board.getController().getGame().getResult() != Result.ON_GOING) {
					String text = null;
					if (board.getController().getGame().getResult() == Result.WHITE_WON) {
						text = "1";
					} else if (board.getController().getGame().getResult() == Result.BLACK_WON) {
						text = "0";
					} else if (board.getController().getGame().getResult() == Result.DRAW) {
						text = "\u00BD";
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
						&& board.getController().getGame().getResult() != Result.ON_GOING) {

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
						&& board.getController().getGame().getResult() != Result.ON_GOING) {

					String text = null;
					if (board.getController().getGame().getResult() == Result.WHITE_WON) {
						text = "0";
					} else if (board.getController().getGame().getResult() == Result.BLACK_WON) {
						text = "1";
					} else if (board.getController().getGame().getResult() == Result.DRAW) {
						text = "\u00BD";
					}
					drawResultText(e, text);
				}
			}
		});

		board.getSquare(SQUARE_E5).addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (!isHiding
						&& board.isWhiteOnTop()
						&& board.getController().getGame().getResult() != Result.ON_GOING) {
					String text = null;
					if (board.getController().getGame().getResult() == Result.WHITE_WON) {
						text = "1";
					} else if (board.getController().getGame().getResult() == Result.BLACK_WON) {
						text = "0";
					} else if (board.getController().getGame().getResult() == Result.DRAW) {
						text = "\u00BD";
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
						&& board.getController().getGame().getResult() != Result.ON_GOING) {

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
						&& board.getController().getGame().getResult() != Result.ON_GOING) {

					String text = null;
					if (board.getController().getGame().getResult() == Result.WHITE_WON) {
						text = "0";
					} else if (board.getController().getGame().getResult() == Result.BLACK_WON) {
						text = "1";
					} else if (board.getController().getGame().getResult() == Result.DRAW) {
						text = "\u00BD";
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
