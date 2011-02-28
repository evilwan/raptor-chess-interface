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

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;

/**
 * Manages the squares being highlighted on a chess board.
 */
public class SquareHighlighter {
	protected class HighlightDecorator implements PaintListener {
		protected ChessSquare square;
		protected List<Highlight> highlights = new ArrayList<Highlight>(10);

		public HighlightDecorator(ChessSquare square) {
			this.square = square;
			square.addPaintListener(this);
		}

		public void add(Highlight highlight) {
			highlights.add(highlight);
		}

		public void clear(boolean isForcing) {
			if (isForcing) {
				highlights.clear();
				square.setDirty(true);
			} else {
				for (int i = 0; i < highlights.size(); i++) {
					if (!highlights.get(i).isFadeAway()) {
						highlights.remove(i);
						i--;
						square.setDirty(true);
					}
				}
			}
		}

		public void paintControl(PaintEvent e) {
			// Don't put log statements in here it gets called quite often.
			int squareSide = square.getSize().x;
			for (Highlight highlight : highlights) {
				int width = (int) (Raptor.getInstance().getPreferences()
						.getInt(PreferenceKeys.HIGHLIGHT_WIDTH_PERCENTAGE) / 100.0 * square
						.getSize().x);
				if (width % 2 != 0) {
					width++;
				}

				if (highlight.frame != -1) {
					e.gc.setAdvanced(true);
					e.gc.setAlpha(25 * highlight.frame);
				}

				e.gc.setForeground(highlight.getColor());
				for (int i = 0; i < width; i++) {
					e.gc.drawRectangle(i, i, squareSide - 1 - i * 2, squareSide
							- 1 - i * 2);
				}

				if (highlight.frame != -1) {
					e.gc.setAlpha(255);
				}
			}
		}

		public void remove(Highlight highlight, boolean isForced) {
			if (!highlight.isFadeAway() || isForced) {
				highlights.remove(highlight);
			}
		}
	}

	protected ChessBoard board;
	protected HighlightDecorator[] decorators = new HighlightDecorator[64];

	protected HighlightDecorator[] dropSquareDecorators = new HighlightDecorator[13];

	/**
	 * Constructs a SquareHighlighter tied to the specified chess board.
	 */
	public SquareHighlighter(ChessBoard board) {
		this.board = board;
		for (int i = 0; i < 64; i++) {
			decorators[i] = new HighlightDecorator(board.getSquare(i));
		}

		for (int i = 0; i < 13; i++) {
			// Not all piece jail squares contain objects some indexes are null
			// so you need to check.
			if (board.getPieceJailSquares()[i] != null) {
				dropSquareDecorators[i] = new HighlightDecorator(board
						.getPieceJailSquares()[i]);
			}
		}
	}

	/**
	 * Adds the highlight and immediately highlight the squares. Highlights that
	 * are not fade away will need to be removed to clear them.
	 */
	public void addHighlight(final Highlight highlight) {
		if (decorators == null) {
			return;
		}
		
		if (ChessBoardUtils.isPieceJailSquare(highlight.getStartSquare())) {
			dropSquareDecorators[ChessBoardUtils
					.pieceJailSquareToPiece(highlight.getStartSquare())]
					.add(highlight);
		} else {
			decorators[highlight.getStartSquare()].add(highlight);
		}

		if (highlight.getEndSquare() != -1) {
			if (ChessBoardUtils.isPieceJailSquare(highlight.getEndSquare())) {
				dropSquareDecorators[ChessBoardUtils
						.pieceJailSquareToPiece(highlight.getEndSquare())]
						.add(highlight);
			} else {
				decorators[highlight.getEndSquare()].add(highlight);
			}
		}

		redrawSquares(false);
		if (highlight.isFadeAway) {
			Raptor.getInstance().getDisplay().timerExec(
					Raptor.getInstance().getPreferences().getInt(
							PreferenceKeys.HIGHLIGHT_ANIMATION_DELAY),
					new Runnable() {
						public void run() {
							highlight.frame--;
							redrawSquares(true);
							if (highlight.frame != 0) {
								Raptor
										.getInstance()
										.getDisplay()
										.timerExec(
												Raptor
														.getInstance()
														.getPreferences()
														.getInt(
																PreferenceKeys.HIGHLIGHT_ANIMATION_DELAY),
												this);
							} else {
								removeHighlight(highlight, true);
							}
						}
					});
		}
	}

	/**
	 * Returns true if the highlight is currently being used.
	 * 
	 * @param highlight
	 *            The Highlight
	 * @return The result.
	 */
	public boolean containsHighlight(Highlight highlight) {
		if (decorators == null) {
			return false;
		}
		
		boolean result = false;
		for (HighlightDecorator decorator : decorators) {
			result = decorator.highlights.contains(highlight);
			if (result) {
				break;
			}
		}

		if (!result) {
			for (HighlightDecorator decorator : dropSquareDecorators) {
				if (decorator != null) {
					result = decorator.highlights.contains(highlight);
					if (result) {
						break;
					}
				}
			}
		}
		return result;
	}

	public void dispose() {
		if (decorators != null) {
			removeAllHighlights(true);
			if (!board.getControl().isDisposed()) {
				for (int i = 0; i < decorators.length; i++) {
					decorators[i].square.removePaintListener(decorators[i]);
					decorators[i] = null;
				}
				for (int i = 0; i < dropSquareDecorators.length; i++) {
					// Not all piece jail squares contain objects some indexes
					// are null
					// so you need to check.
					if (dropSquareDecorators[i] != null) {
						dropSquareDecorators[i].square
								.removePaintListener(dropSquareDecorators[i]);
					}
					dropSquareDecorators[i] = null;
				}
				decorators = null;
				dropSquareDecorators = null;
				board = null;
			}
		}
	}

	/**
	 * Removes all non fade away highlights.
	 */
	public void removeAllHighlights() {
		if (decorators == null) {
			return;
		}
		removeAllHighlights(true);
	}

	/**
	 * Removes the highlight. Fade away highlights can't be removed, they are
	 * removed internally.
	 */
	public void removeHighlight(Highlight highlight) {
		if (decorators == null) {
			return;
		}
		removeHighlight(highlight, true);
	}

	/**
	 * Redraws all squares that have arrow segments.
	 */
	protected void redrawSquares(boolean forceUpdate) {
		if (decorators == null) {
			return;
		}

		// Use for loops hwere with int. If you dont you can get concurrent
		// modification errors.
		for (int i = 0; i < decorators.length; i++) {
			if (!decorators[i].highlights.isEmpty()) {
				if (forceUpdate) {
				    decorators[i].square.redraw();
				}
				else {
					decorators[i].square.setDirty(true);
				}
			}
		}

		for (int i = 0; i < dropSquareDecorators.length; i++) {
			if (dropSquareDecorators[i] != null
					&& !dropSquareDecorators[i].highlights.isEmpty()) {
				if (forceUpdate) {
					dropSquareDecorators[i].square.redraw();
				}
				else {
					dropSquareDecorators[i].square.setDirty(true);
				}
			}
		}
	}

	/**
	 * Removes all non fade away highlights.
	 */
	protected void removeAllHighlights(boolean isForcing) {
		if (decorators == null) {
			return;
		}
		
		for (HighlightDecorator decorator : decorators) {
			decorator.clear(true);
			decorator.square.setDirty(true);
		}

		for (HighlightDecorator decorator : dropSquareDecorators) {
			if (decorator != null) {
				decorator.clear(true);
			}
		}
	}

	/**
	 * Removes the highlight. Fade away highlights can't be removed, they are
	 * removed internally.
	 */
	protected void removeHighlight(Highlight highlight, boolean isForced) {
		if (decorators == null) {
			return;
		}
		for (HighlightDecorator decorator : decorators) {
			decorator.remove(highlight, true);
		}

		for (HighlightDecorator decorator : dropSquareDecorators) {
			if (decorator != null) {
				decorator.remove(highlight, true);
			}
		}
	}
}
