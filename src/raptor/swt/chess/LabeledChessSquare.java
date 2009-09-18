package raptor.swt.chess;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class LabeledChessSquare extends ChessSquare {
	String text = "";
	PaintListener paintListener = new PaintListener() {
		public void paintControl(PaintEvent arg0) {
			if (StringUtils.isNotBlank(getText())) {
				Point size = getSize();
				arg0.gc.setForeground(board.preferences
						.getColor(BOARD_PIECE_JAIL_LABEL_COLOR));
				arg0.gc.setFont(board.preferences
						.getFont(BOARD_PIECE_JAIL_FONT));

				int width = arg0.gc.getFontMetrics().getAverageCharWidth()
						* text.length() + 2;

				arg0.gc.drawString(getText(), size.x - width, 0, true);
			}
		}
	};

	public LabeledChessSquare(Composite composite, ChessBoard board, int id) {
		super(composite, board, id,true);
		dontPaintBackground = true;
		addPaintListener(paintListener);
		
	}

	public void dispose() {
		removePaintListener(paintListener);
		super.dispose();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
