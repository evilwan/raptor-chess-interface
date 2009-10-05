package raptor.swt.chess;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

public abstract class ChessBoardLayout extends Layout implements BoardConstants {
	public static enum Field {
		GAME_DESCRIPTION_LABEL, STATUS_LABEL, CURRENT_PREMOVE_LABEL, OPENING_DESCRIPTION_LABEL, CLOCK_LABEL, LAG_LABEL, NAME_RATING_LABEL, TO_MOVE_INDICATOR;
	}

	protected ChessBoard board;

	public ChessBoardLayout(ChessBoard board) {
		super();
		this.board = board;
	}

	@Override
	protected Point computeSize(Composite composite, int hint, int hint2,
			boolean flushCache) {
		return composite.getSize();
	}

	public void dispose() {
		board = null;
	}

	public ChessBoard getBoard() {
		return board;
	}

	public abstract String getName();

	public abstract int getStyle(Field field);

	protected void layoutChessBoard(Point topLeft, int squareSideSize) {
		int x = topLeft.x;
		int y = topLeft.y;

		if (!board.isWhiteOnTop()) {
			for (int i = 7; i > -1; i--) {
				for (int j = 0; j < 8; j++) {
					board.getSquare(i, j).setBounds(x, y, squareSideSize,
							squareSideSize);
					x += squareSideSize;
				}
				x = topLeft.x;
				y += squareSideSize;

			}
		} else {
			for (int i = 0; i < 8; i++) {
				for (int j = 7; j > -1; j--) {
					board.getSquare(i, j).setBounds(x, y, squareSideSize,
							squareSideSize);
					x += squareSideSize;
				}
				x = topLeft.x;
				y += squareSideSize;
			}
		}
	}
}
