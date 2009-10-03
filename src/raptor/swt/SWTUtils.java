package raptor.swt;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.Raptor;

/**
 * A class containing SWT and JFace utilities.
 */
public class SWTUtils {

	/**
	 * Centers the shell in the RaptorWindow.
	 */
	public static void centerInRaptorWindow(Dialog dialog) {
		Shell parent = Raptor.getInstance().getRaptorWindow().getShell();

		Rectangle parentSize = parent.getBounds();
		Point mySize = dialog.getShell().computeSize(0, 0);

		int locationX, locationY;
		locationX = (parentSize.width - mySize.x) / 2 + mySize.x;
		locationY = (parentSize.height - mySize.y) / 2 - mySize.y;

		dialog.getShell().setLocation(new Point(locationX, locationY));

	}

	/**
	 * Disposes of all the items in a toolbar.
	 * 
	 * @param toolbar
	 *            THe toolbar to clear.
	 */
	public static void clearToolbar(ToolBar toolbar) {
		if (toolbar != null) {
			ToolItem[] toolItems = toolbar.getItems();
			for (int i = 0; i < toolItems.length; i++) {
				toolItems[i].dispose();
			}
		}
	}

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
