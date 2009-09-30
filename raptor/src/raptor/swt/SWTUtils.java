package raptor.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;

/**
 * A class containing SWT and JFace utilities.
 */
public class SWTUtils {

	/**
	 * Returns a HORIZONTAL fill layout without any margins or spacing.
	 */
	public static FillLayout createMarginlessFillLayout() {
		FillLayout result = new FillLayout();
		result.marginHeight = 0;
		result.marginWidth = 0;
		result.spacing = 0;
		result.type = SWT.HORIZONTAL;
		return result;
	}

	/**
	 * Returns a GridLayout without any margins or spacing.
	 */
	public static GridLayout createMarginlessGridLayout(int columns,
			boolean areColumnsSameSize) {
		GridLayout result = new GridLayout(columns, areColumnsSameSize);
		result.marginLeft = 0;
		result.marginTop = 0;
		result.marginRight = 0;
		result.marginBottom = 0;
		result.horizontalSpacing = 0;
		result.verticalSpacing = 0;
		result.marginHeight = 0;
		result.marginWidth = 0;
		return result;
	}

	/**
	 * Returns a new font resizing the font size by resizePercentage of the
	 * controls height. The old font is not disposed. It is up to the caller to
	 * dispose the font.
	 * 
	 * @param display
	 *            The display used to create the new font.
	 * @param oldFont
	 *            The old font to adjust.
	 * @param resizePercentage
	 *            The percent of the controls height the new fonts height should
	 *            be. (i.e. 80 for 80%).
	 * @param controlHeight
	 *            The controls height to adjust for.
	 * @return The new font.
	 */
	public static Font getProportionalFont(Display display, Font oldFont,
			int resizePercentage, int controlHeight) {
		return new Font(display, oldFont.getFontData()[0].getName(),
				(int) (controlHeight * (resizePercentage / 100.0)), oldFont
						.getFontData()[0].getStyle());
	}

}
