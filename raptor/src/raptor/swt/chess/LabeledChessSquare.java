package raptor.swt.chess;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;

import raptor.swt.SWTUtils;

/**
 * A labeled chess square. Contains a label in the top right. Currently used for
 * piece jails and drop squares to show the number of pieces.
 */
public class LabeledChessSquare extends ChessSquare {
	public static final int LABEL_HEIGHT_PERCENTAGE = 30;

	protected String text = "";
	protected PaintListener paintListener = new PaintListener() {
		public void paintControl(PaintEvent arg0) {
			if (StringUtils.isNotBlank(getText())) {
				Point size = getSize();
				arg0.gc.setForeground(getPreferences().getColor(
						BOARD_PIECE_JAIL_LABEL_COLOR));
				arg0.gc.setFont(SWTUtils.getProportionalFont(getPreferences()
						.getFont(BOARD_PIECE_JAIL_FONT),
						LABEL_HEIGHT_PERCENTAGE, size.y));

				int width = arg0.gc.getFontMetrics().getAverageCharWidth()
						* text.length() + 2;

				arg0.gc.drawString(getText(), size.x - width, 0, true);
			}
		}
	};

	public LabeledChessSquare(ChessBoard board, int id) {
		super(board, id, true);
		ignoreBackgroundImage = true;
		addPaintListener(paintListener);
	}

	/**
	 * Returns the current label.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the label.
	 * 
	 * @param text
	 */
	public void setText(String text) {
		this.text = text;
	}
}
