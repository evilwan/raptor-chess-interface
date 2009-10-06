package raptor.swt;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
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
	 * Returns a RowLayout without any margins or spacing.
	 */
	public static RowLayout createMarginlessRowLayout(int type) {
		RowLayout result = new RowLayout();
		result.marginLeft = 0;
		result.marginTop = 0;
		result.marginRight = 0;
		result.marginBottom = 0;
		result.fill = true;
		result.marginHeight = 0;
		result.marginWidth = 0;
		result.justify = false;
		result.spacing = 0;
		result.wrap = false;
		return result;
	}

	/**
	 * Returns a new font resizing the font size by resizePercentage of the
	 * controls height. This method caches fonts in Raptor's font registry so
	 * there is no need to dispose of any fonts.
	 * 
	 * @param fontToAdjust
	 *            The old font to adjust.
	 * @param resizePercentage
	 *            The percent of the controls height the new fonts height should
	 *            be. (i.e. 80 for 80%).
	 * @param controlHeight
	 *            The controls height to adjust for.
	 * @return The new font.
	 */
	public static Font getProportionalFont(Font fontToAdjust,
			int resizePercentage, int controlHeight) {

		Point pixelsPerInch = Raptor.getInstance().getDisplay().getDPI();
		double pointsPerPixel = 72.0 / pixelsPerInch.y;

		double heightInPoints = controlHeight * pointsPerPixel;

		int requestedHeightInPoints = (int) (heightInPoints * resizePercentage / 100.0);

		String key = fontToAdjust.getFontData()[0].getName() + "_"
				+ requestedHeightInPoints + "_"
				+ fontToAdjust.getFontData()[0].getStyle();

		if (Raptor.getInstance().getFontRegistry().hasValueFor(key)) {
			return Raptor.getInstance().getFontRegistry().get(key);
		} else {
			FontData[] oldFontDataArray = fontToAdjust.getFontData();
			FontData[] fontDataArray = new FontData[oldFontDataArray.length];
			for (int i = 0; i < fontDataArray.length; i++) {
				fontDataArray[i] = new FontData();
				fontDataArray[i].setHeight(requestedHeightInPoints);
				fontDataArray[i].setLocale(oldFontDataArray[i].getLocale());
				fontDataArray[i].setName(oldFontDataArray[i].getName());
				fontDataArray[i].setStyle(oldFontDataArray[i].getStyle());
			}
			Raptor.getInstance().getFontRegistry().put(key, fontDataArray);
			return Raptor.getInstance().getFontRegistry().get(key);
		}
	}

}
