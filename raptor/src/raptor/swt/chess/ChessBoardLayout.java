package raptor.swt.chess;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

public abstract class ChessBoardLayout extends Layout implements Constants {
	protected ChessBoard board;
	

	public static final int GAME_DESCRIPTION_LABEL = 0;
	public static final int STATUS_LABEL = 1;
	public static final int CURRENT_PREMOVE_LABEL = 2;
	public static final int OPENING_DESCRIPTION_LABEL = 3;
	public static final int CLOCK_LABEL = 4;
	public static final int LAG_LABEL = 5;
	public static final int NAME_RATING_LABEL = 6;
	public static final int COOLBAR = 7;
	public static final int TO_MOVE_INDICATOR = 8;
	
	public ChessBoardLayout(ChessBoard chessBoard) {
		super();
	}
	
	public abstract int getStyle(int controlConstant);

	public ChessBoard getBoard() {
		return board;
	}

	public void setBoard(ChessBoard board) {
		this.board = board;
	}

	public void dispose() {
	}

	public abstract String getName();
	
	protected Point getOneCharSizeInFont(Font font,GC gc) {
		 return new Point(gc.getFontMetrics().getAverageCharWidth(),gc.getFontMetrics().getAscent()
		 + gc.getFontMetrics().getDescent());
	}

	@Override
	protected Point computeSize(Composite composite, int hint, int hint2,
			boolean flushCache) {
		return composite.getSize();
	}

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
