/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
public class ResultDecorator implements BoardConstants {
	public static enum ResultDecoration {
		WhiteWin, BlackWin, Draw, Undetermined
	}

	protected class SquareDecorator implements PaintListener {
		protected ChessSquare square;

		public SquareDecorator(ChessSquare square) {
			this.square = square;
			square.addPaintListener(this);
		}

		public void dispose() {
			square.removePaintListener(this);
		}

		public void paintControl(PaintEvent e) {
			if (decoration != null) {
				String text = null;
				switch (square.getId()) {
				case SQUARE_D4:
					if (!board.isWhiteOnTop()) {
						switch (decoration) {
						case WhiteWin:
							text = "1";
							break;
						case BlackWin:
							text = "0";
							break;
						case Draw:
							text = "\u00BD";
							break;
						default:
							break;
						}
					}
					break;
				case SQUARE_E4:
					if (!board.isWhiteOnTop()) {
						switch (decoration) {
						case WhiteWin:
							text = "-";
							break;
						case BlackWin:
							text = "-";
							break;
						case Draw:
							text = "-";
							break;
						default:
							text = "*";
							break;
						}
					}
					break;
				case SQUARE_F4:
					if (!board.isWhiteOnTop()) {
						switch (decoration) {
						case WhiteWin:
							text = "0";
							break;
						case BlackWin:
							text = "1";
							break;
						case Draw:
							text = "\u00BD";
							break;
						default:
							break;
						}
					}
					break;
				case SQUARE_E5:
					if (board.isWhiteOnTop()) {
						switch (decoration) {
						case WhiteWin:
							text = "1";
							break;
						case BlackWin:
							text = "0";
							break;
						case Draw:
							text = "\u00BD";
							break;
						default:
							break;
						}
					}
					break;
				case SQUARE_D5:
					if (board.isWhiteOnTop()) {
						switch (decoration) {
						case WhiteWin:
							text = "-";
							break;
						case BlackWin:
							text = "-";
							break;
						case Draw:
							text = "-";
							break;
						default:
							text = "*";
							break;
						}
					}
					break;
				case SQUARE_C5:
					if (board.isWhiteOnTop()) {
						switch (decoration) {
						case WhiteWin:
							text = "0";
							break;
						case BlackWin:
							text = "1";
							break;
						case Draw:
							text = "\u00BD";
							break;
						default:
							break;
						}
					}
					break;

				}

				if (StringUtils.isNotBlank(text)) {
					drawResultText(e, text);
				}
			}
		}
	}

	protected ChessBoard board;
	protected Font resultFont;
	protected int frame = -1;
	protected int resultFontHeight;
	protected ResultDecoration decoration;

	protected List<SquareDecorator> decorators = new ArrayList<SquareDecorator>(
			6);

	public ResultDecorator(ChessBoard board) {
		this.board = board;
		decorators.add(new SquareDecorator(board.getSquare(SQUARE_E4)));
		decorators.add(new SquareDecorator(board.getSquare(SQUARE_D4)));
		decorators.add(new SquareDecorator(board.getSquare(SQUARE_F4)));
		decorators.add(new SquareDecorator(board.getSquare(SQUARE_E5)));
		decorators.add(new SquareDecorator(board.getSquare(SQUARE_D5)));
		decorators.add(new SquareDecorator(board.getSquare(SQUARE_C5)));
	}

	public void dispose() {
		for (SquareDecorator decorator : decorators) {
			decorator.dispose();
		}
		decorators.clear();
	}

	public ChessBoard getBoard() {
		return board;
	};

	public ResultDecoration getDecoration() {
		return decoration;
	}

	/**
	 * Can be set to null to hide the decoration.
	 * 
	 * @param decoration
	 */
	public void setDecoration(ResultDecoration decoration) {
		this.decoration = decoration;
	}

	public void setDecorationFromResult(Result result) {
		if (!Raptor.getInstance().getPreferences().getBoolean(
				RESULTS_IS_SHOWING)) {
			decoration = null;
			return;
		}
		switch (result) {
		case BLACK_WON:
			decoration = ResultDecoration.BlackWin;
			break;
		case WHITE_WON:
			decoration = ResultDecoration.WhiteWin;
			break;
		case DRAW:
			decoration = ResultDecoration.Draw;
			break;
		case ON_GOING:
			decoration = ResultDecoration.Undetermined;
			break;
		case UNDETERMINED:
			decoration = ResultDecoration.Undetermined;
			break;
		}

		if (Raptor.getInstance().getPreferences().getBoolean(
				RESULTS_FADE_AWAY_MODE)) {
			frame = 10;
			Raptor.getInstance().getDisplay().timerExec(
					Raptor.getInstance().getPreferences().getInt(
							PreferenceKeys.RESULTS_ANIMATION_DELAY),
					new Runnable() {
						public void run() {
							frame--;
							redrawSquares();
							if (frame != 0) {
								Raptor
										.getInstance()
										.getDisplay()
										.timerExec(
												Raptor
														.getInstance()
														.getPreferences()
														.getInt(
																PreferenceKeys.RESULTS_ANIMATION_DELAY),
												this);
							} else {
								frame = -1;
								decoration = null;
							}
							redrawSquares();
						}
					});
		}
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
					PreferenceKeys.RESULTS_COLOR));

			e.gc.setFont(getResultFont(e.height));

			Point extent = e.gc.stringExtent(text);

			if (frame != -1) {
				e.gc.setAdvanced(true);
				e.gc.setAlpha(25 * frame);
				e.gc.drawString(text, e.width / 2 - extent.x / 2, e.height / 2
						- extent.y / 2, true);
				e.gc.setAlpha(255);
			} else {
				e.gc.drawString(text, e.width / 2 - extent.x / 2, e.height / 2
						- extent.y / 2, true);
			}
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
		int fontPercentOfSquareSize = Raptor.getInstance().getPreferences()
				.getInt(PreferenceKeys.RESULTS_WIDTH_PERCENTAGE);
		if (resultFont == null) {
			resultFont = Raptor.getInstance().getPreferences().getFont(
					PreferenceKeys.RESULTS_FONT);
			resultFont = SWTUtils.getProportionalFont(resultFont,
					fontPercentOfSquareSize, height);
			resultFontHeight = height;
		} else {
			if (resultFontHeight != height) {
				Font newFont = SWTUtils.getProportionalFont(resultFont,
						fontPercentOfSquareSize, height);
				resultFontHeight = height;
				resultFont = newFont;
			}
		}
		return resultFont;
	}

	/**
	 * Redraws all squares that have arrow segments.
	 */
	protected void redrawSquares() {
		for (SquareDecorator decorator : decorators) {
			decorator.square.redraw();
		}
	}
}
