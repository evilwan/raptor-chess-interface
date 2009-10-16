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
			} else {
				for (int i = 0; i < highlights.size(); i++) {
					if (!highlights.get(i).isFadeAway()) {
						highlights.remove(i);
						i--;
					}
				}
			}
		}

		public void paintControl(PaintEvent e) {
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
					e.gc.setAlpha(50 * highlight.frame);
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
		if (BoardUtils.isPieceJailSquare(highlight.getStartSquare())) {
			dropSquareDecorators[BoardUtils.pieceJailSquareToPiece(highlight
					.getStartSquare())].add(highlight);
		} else {
			decorators[highlight.getStartSquare()].add(highlight);
		}

		if (highlight.getEndSquare() != -1) {
			if (BoardUtils.isPieceJailSquare(highlight.getEndSquare())) {
				dropSquareDecorators[BoardUtils
						.pieceJailSquareToPiece(highlight.getEndSquare())]
						.add(highlight);
			} else {
				decorators[highlight.getEndSquare()].add(highlight);
			}
		}

		board.redrawSquares();
		if (highlight.isFadeAway) {
			Raptor.getInstance().getDisplay().timerExec(
					Raptor.getInstance().getPreferences().getInt(
							PreferenceKeys.HIGHLIGHT_ANIMATION_DELAY),
					new Runnable() {
						public void run() {
							highlight.frame--;
							board.redrawSquares();
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
								.removePaintListener(decorators[i]);
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
		removeAllHighlights(false);
	}

	/**
	 * Removes the highlight. Fade away highlights can't be removed, they are
	 * removed internally.
	 */
	public void removeHighlight(Highlight highlight) {
		removeHighlight(highlight, true);
	}

	/**
	 * Removes all non fade away highlights.
	 */
	protected void removeAllHighlights(boolean isForcing) {
		for (HighlightDecorator decorator : decorators) {
			decorator.clear(true);
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
